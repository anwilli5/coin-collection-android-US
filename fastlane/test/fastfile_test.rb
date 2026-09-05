# Unit tests for the deploy lanes in fastlane/Fastfile.
#
# These run the real lanes with the store-upload actions stubbed out, so the
# parameters the Fastfile builds can be asserted without contacting Google Play
# or the Amazon Appstore. That covers the failure mode `fastlane lanes` cannot:
# a lane that parses fine but passes the wrong options (a renamed supply
# option, a guard that stops skipping the store listing, a mapping file that
# never gets attached).
#
# Run with:
#   bundle exec ruby fastlane/test/fastfile_test.rb
#
# No credentials are needed. supply validates its options before invoking the
# action, so the stand-in files those checks require are generated into a
# temporary directory for the duration of the run. Nothing is checked in: an
# .apk fixture would fight .gitignore, and a service-account JSON containing a
# BEGIN PRIVATE KEY block would be flagged by the repo's security scanners.

require 'minitest/autorun'
require 'fastlane'
require 'tmpdir'
require 'json'
require 'fileutils'

Fastlane.load_actions

# The Amazon lane's action comes from fastlane-plugin-amazon_appstore. Loading
# plugins here doubles as a check that the Pluginfile still resolves.
Dir.chdir(File.expand_path('..', __dir__)) { Fastlane.plugin_manager.load_plugins }

class FastfileTest < Minitest::Test
  FASTLANE_DIR = File.expand_path('..', __dir__)
  REPO_ROOT = File.expand_path('..', FASTLANE_DIR)
  FASTFILE = File.join(FASTLANE_DIR, 'Fastfile')

  # supply only checks that these paths exist (and that the apk ends in .apk);
  # it never opens them, so placeholders are enough to drive the lanes.
  class << self
    attr_reader :tmpdir, :play_key, :apk_stand_in, :mapping_stand_in
  end

  @tmpdir = Dir.mktmpdir('fastfile-test')
  Minitest.after_run { FileUtils.remove_entry(@tmpdir) if File.directory?(@tmpdir) }

  @apk_stand_in = File.join(@tmpdir, 'stand-in.apk')
  File.write(@apk_stand_in, "not a real apk\n")

  @mapping_stand_in = File.join(@tmpdir, 'stand-in-mapping.txt')
  File.write(@mapping_stand_in, "com.example.Foo -> a:\n    void bar() -> b\n")

  # Structurally valid but entirely fake; supply parses it during option
  # validation and never authenticates because the upload action is stubbed.
  @play_key = File.join(@tmpdir, 'play_key.json')
  File.write(@play_key, JSON.pretty_generate(
    'type' => 'service_account',
    'project_id' => 'fastfile-test',
    'private_key_id' => 'fake',
    'private_key' => 'fake',
    'client_email' => 'fastfile-test@example.iam.gserviceaccount.com',
    'client_id' => '0',
    'auth_uri' => 'https://accounts.google.com/o/oauth2/auth',
    'token_uri' => 'https://oauth2.googleapis.com/token'
  ))

  FIXTURE_KEY = @play_key
  APK_STAND_IN = @apk_stand_in
  MAPPING_STAND_IN = @mapping_stand_in

  def setup
    # The Fastfile resolves its own paths through `__dir__`, which fastlane
    # evaluates with __FILE__ == "Fastfile". That makes it relative to the
    # working directory, so lanes must be driven from fastlane/ exactly as the
    # fastlane CLI does. Running from anywhere else silently yields wrong paths.
    @previous_dir = Dir.pwd
    Dir.chdir(FASTLANE_DIR)
    ENV['GOOGLE_PLAY_KEY_PATH'] = FIXTURE_KEY
    @fastfile = Fastlane::FastFile.new(FASTFILE)
  end

  def teardown
    ENV.delete('GOOGLE_PLAY_KEY_PATH')
    Dir.chdir(@previous_dir)
  end

  # Runs a lane with the Google Play upload stubbed, and returns the options
  # supply would have been called with. SupplyAction is an empty subclass of
  # UploadToPlayStoreAction, so stubbing the parent covers both spellings.
  def run_play_lane(lane, options = {})
    captured = nil
    Fastlane::Actions::UploadToPlayStoreAction.stub(:run, ->(params) { captured = params }) do
      @fastfile.runner.execute(lane, :android, options)
    end
    refute_nil(captured, "supply was never called by #{lane}")
    captured
  end

  # --- mapping file (Play crash-report deobfuscation) ---------------------

  def test_tester_lane_attaches_the_r8_mapping_file
    params = run_play_lane(:deploy_playstore_test,
                           apk_path: APK_STAND_IN,
                           track: 'internal',
                           mapping_path: MAPPING_STAND_IN)
    assert_equal([MAPPING_STAND_IN], params[:mapping_paths],
                 'release builds are obfuscated; without mapping_paths Play cannot deobfuscate crashes')
  end

  def test_production_upload_attaches_the_r8_mapping_file
    params = run_play_lane(:deploy_playstore_production,
                           apk_path: APK_STAND_IN,
                           mapping_path: MAPPING_STAND_IN)
    assert_equal('production', params[:track])
    assert_equal([MAPPING_STAND_IN], params[:mapping_paths])
  end

  def test_production_promotion_does_not_resend_a_mapping_file
    # Promotion reuses the artifact already uploaded to the tester track, whose
    # mapping went up with it. Sending one here would be meaningless.
    params = run_play_lane(:deploy_playstore_production, version_code: 60)
    assert_nil(params[:mapping_paths])
    assert_equal('internal', params[:track])
    assert_equal('production', params[:track_promote_to])
    assert_equal(60, params[:version_code])
    assert_equal(true, params[:skip_upload_apk])
    assert_equal(true, params[:skip_upload_aab])
  end

  def test_missing_mapping_file_warns_but_still_deploys
    # Guards the deliberate choice to warn rather than fail: a caller may
    # legitimately deploy a pre-built APK without the build tree present.
    built_mapping = File.join(REPO_ROOT, 'app/build/outputs/mapping/androidRelease/mapping.txt')
    skip("a local release build exists at #{built_mapping}") if File.exist?(built_mapping)

    params = run_play_lane(:deploy_playstore_test, apk_path: APK_STAND_IN, track: 'internal')
    assert_nil(params[:mapping_paths])
    assert_equal(APK_STAND_IN, params[:apk])
  end

  # --- store listing protection ------------------------------------------

  # A deploy must never overwrite the live Play listing text, images, or
  # screenshots. These flags are the only thing preventing that.
  def test_tester_lane_never_touches_the_store_listing
    params = run_play_lane(:deploy_playstore_test, apk_path: APK_STAND_IN, track: 'internal')
    assert_equal(true, params[:skip_upload_metadata])
    assert_equal(true, params[:skip_upload_images])
    assert_equal(true, params[:skip_upload_screenshots])
  end

  def test_production_lane_never_touches_the_store_listing
    params = run_play_lane(:deploy_playstore_production, version_code: 60)
    assert_equal(true, params[:skip_upload_metadata])
    assert_equal(true, params[:skip_upload_images])
    assert_equal(true, params[:skip_upload_screenshots])
  end

  # --- input validation ---------------------------------------------------

  def test_tester_lane_defaults_to_the_internal_track
    params = run_play_lane(:deploy_playstore_test, apk_path: APK_STAND_IN)
    assert_equal('internal', params[:track])
  end

  def test_tester_lane_rejects_an_unknown_track
    error = assert_raises(FastlaneCore::Interface::FastlaneError) do
      run_play_lane(:deploy_playstore_test, apk_path: APK_STAND_IN, track: 'production')
    end
    assert_match(/Invalid track/, error.message)
  end

  def test_tester_lane_requires_an_existing_apk
    assert_raises(FastlaneCore::Interface::FastlaneError) do
      run_play_lane(:deploy_playstore_test, apk_path: '/nonexistent/app.apk')
    end
  end

  def test_production_lane_requires_an_apk_or_a_version_code
    assert_raises(FastlaneCore::Interface::FastlaneError) do
      run_play_lane(:deploy_playstore_production, version_code: 0)
    end
  end

  # --- changelog selection ------------------------------------------------

  def test_changelog_is_uploaded_only_when_the_versioncode_file_exists
    # Release notes live at metadata/android/en-US/changelogs/<versionCode>.txt.
    # versionCode 0 is the deploy.yml sentinel and never has one.
    params = run_play_lane(:deploy_playstore_test, apk_path: APK_STAND_IN, version_code: 0)
    assert_equal(true, params[:skip_upload_changelogs])
  end

  def test_changelog_helpers_resolve_under_the_fastlane_directory
    expected = File.join(FASTLANE_DIR, 'metadata', 'android')
    assert_equal(expected, @fastfile.android_metadata_path)
    assert_nil(@fastfile.changelog_file_path(0))
    assert_equal(File.join(expected, 'en-US', 'changelogs', '99.txt'),
                 @fastfile.changelog_file_path(99))
  end

  # --- mapping-path helper ------------------------------------------------

  def test_mapping_path_override_takes_precedence
    assert_equal('/explicit/mapping.txt',
                 @fastfile.r8_mapping_path(mapping_path: '/explicit/mapping.txt'))
  end

  def test_blank_mapping_path_override_is_ignored
    built_mapping = File.join(REPO_ROOT, 'app/build/outputs/mapping/androidRelease/mapping.txt')
    expected = File.exist?(built_mapping) ? built_mapping : nil
    assert_equal(expected, @fastfile.r8_mapping_path(mapping_path: '   '))
  end

  # --- Amazon Appstore ----------------------------------------------------

  def run_amazon_lane(options = {})
    captured = nil
    with_amazon_credentials do
      Fastlane::Actions::UploadToAmazonAppstoreAction.stub(:run, ->(params) { captured = params }) do
        @fastfile.runner.execute(:deploy_amazon_appstore, :android, options)
      end
    end
    refute_nil(captured, 'upload_to_amazon_appstore was never called')
    captured
  end

  def with_amazon_credentials
    previous = ENV.values_at('AMAZON_CLIENT_ID', 'AMAZON_CLIENT_SECRET')
    ENV['AMAZON_CLIENT_ID'] = 'test-client-id'
    ENV['AMAZON_CLIENT_SECRET'] = 'test-client-secret'
    yield
  ensure
    ENV['AMAZON_CLIENT_ID'], ENV['AMAZON_CLIENT_SECRET'] = previous
  end

  def test_amazon_lane_uses_the_amazon_flavor_application_id
    # The Amazon listing is registered under the ".amazon" applicationIdSuffix
    # from app/build.gradle, not the Play package the Appfile sets. Inheriting
    # the Appfile value makes Amazon reject the upload with
    # "No app found with the entered Inputs".
    params = run_amazon_lane(apk_path: APK_STAND_IN)
    assert_equal('com.spencerpages.amazon', params[:package_name])
    assert_equal(APK_STAND_IN, params[:apk])
  end

  def test_amazon_lane_package_name_can_be_overridden
    params = run_amazon_lane(apk_path: APK_STAND_IN, package_name: 'com.example.other')
    assert_equal('com.example.other', params[:package_name])
  end

  def test_amazon_lane_requires_credentials
    assert_raises(FastlaneCore::Interface::FastlaneError) do
      @fastfile.runner.execute(:deploy_amazon_appstore, :android, apk_path: APK_STAND_IN)
    end
  end

  def test_amazon_lane_requires_an_existing_apk
    assert_raises(FastlaneCore::Interface::FastlaneError) do
      run_amazon_lane(apk_path: '/nonexistent/app.apk')
    end
  end
end
