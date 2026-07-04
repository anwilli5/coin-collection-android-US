---
name: deploy-with-fastlane
description: >
  Deploy the Android app to Google Play and the Amazon Appstore with this repo's
  Fastlane lanes. Use when asked to deploy a build, upload an APK to the Play
  Store or Amazon Appstore, push to the alpha/beta/internal tester track, release
  to production, wire up supply authentication, configure release signing, or
  understand and modify the Fastfile deploy lanes and the CI release flow. NOT for
  generating store screenshots — use the capture-store-screenshots skill — and NOT
  the annual coin update itself — use the add-yearly-coins prompt.
---

# Deploy with Fastlane

This app ships to **Google Play** (`android` flavor) and the **Amazon Appstore**
(`amazon` flavor) through Fastlane lanes defined in
[fastlane/Fastfile](../../../fastlane/Fastfile). Deployment normally runs from
GitHub Actions, but every lane can be run locally once the required secrets are
present.

## Core design pattern

The deploy lanes **do not build the app**. Each one receives an already-built,
signed APK through an `apk_path` option and only uploads it. CI builds the APK
once (`assembleAndroidRelease` / `assembleAmazonRelease`), then hands the path to
the lane. Keep this separation when editing lanes — do not add `gradle` /
`build_android_app` build steps to the deploy lanes.

Do not rely on the lane's working directory for metadata paths. fastlane runs
plain-Ruby blocks and the `supply` / Amazon actions with *different* effective
working directories, so a relative `metadata_path` can be created in one place
and looked up in another (this caused `Could not find folder metadata/android`).
The Fastfile exposes an `android_metadata_path` helper that returns an **absolute**
path (`File.expand_path("metadata/android", __dir__)`, anchored to the `fastlane/`
folder); use it for both `metadata_path` and any changelog writes. The shared
`write_changelog_file(version_code, release_notes)` helper already does this.

## Lanes

| Lane | Purpose | Key options |
| --- | --- | --- |
| `test_and_lint` | Run unit tests + lint (`testAndroidDebugUnitTest`, `lintAndroidDebug`) | none |
| `build_and_screengrab` | Build debug + androidTest APKs and capture screenshots via Screengrab | none (see capture-store-screenshots skill) |
| `deploy_playstore_test` | Upload a pre-built APK to a Play **tester** track | `apk_path` (required), `track` (default `internal`; one of `internal`/`alpha`/`beta`), `release_notes`, `version_code` |
| `deploy_playstore_production` | Release to Play **production**: promotes an already-uploaded release by versionCode (default), or uploads a pre-built APK when `apk_path` is given | `version_code` (required to promote), `apk_path` (upload mode), `from_track` (default `internal`), `release_notes` |
| `deploy_amazon_appstore` | Upload a pre-built APK to the Amazon Appstore | `apk_path` (required), `release_notes`, `version_code` |

List lanes at any time:

```bash
bundle exec fastlane lanes
```

## Authentication & secrets

| Secret / env var | Used by | Notes |
| --- | --- | --- |
| `GOOGLE_PLAY_KEY_PATH` | both Play lanes | Path to the service-account JSON; read by `json_key_file(...)` in [fastlane/Appfile](../../../fastlane/Appfile). Falls back to `/tmp/google_play_key.json` (CI writes the key there from the `GOOGLE_PLAY_JSON_KEY_DATA` secret). |
| `AMAZON_CLIENT_ID` | `deploy_amazon_appstore` | Amazon Developer API client id. Lane fails fast if unset. |
| `AMAZON_CLIENT_SECRET` | `deploy_amazon_appstore` | Amazon Developer API client secret. |
| `SIGNING_KEYSTORE_PATH` | release build (CI) | Path to the keystore file. |
| `SIGNING_KEYSTORE_PASSWORD` | release build (CI) | Keystore password. |
| `SIGNING_KEY_ALIAS` | release build (CI) | Key alias. |
| `SIGNING_KEY_PASSWORD` | release build (CI) | Key password. |

The package name is pinned to `com.spencerpages` in
[fastlane/Appfile](../../../fastlane/Appfile).

### First-time supply setup

The Play lanes need a Google service-account JSON with the Play Developer API
enabled. Create it once following Fastlane's supply setup
(<https://docs.fastlane.tools/getting-started/android/setup/#setting-up-supply>),
store the JSON contents in the `GOOGLE_PLAY_JSON_KEY_DATA` repo secret, and point
`GOOGLE_PLAY_KEY_PATH` at the file. Validate a local key before deploying:

```bash
bundle exec fastlane run validate_play_store_json_key json_key:/path/to/key.json
```

## Signing & flavors

Release signing is configured in
[app/build.gradle](../../../app/build.gradle). The `signingConfigs.Android`
block prefers the four `SIGNING_*` environment variables (CI); if they are
absent it falls back to a gitignored `signing.properties`. When neither is
present `storeFile` is null and the `release` build type stays unsigned — so a
release APK built on a plain dev box is **not** uploadable.

Two product flavors share the `version` dimension:

| Flavor | Application id | Store |
| --- | --- | --- |
| `android` | `com.spencerpages` | Google Play |
| `amazon` | `com.spencerpages.amazon` (`applicationIdSuffix .amazon`) | Amazon Appstore |

Release APKs are named `app-{flavor}-{buildType}-v{versionName}.apk`, e.g.
`app-android-release-v3.7.4.apk`. The deploy commands below depend on this name.

## Changelog ("What's new") handling

`supply` (and the Amazon plugin) have **no inline `changelogs` option** — passing
one raises `Could not find option changelogs`. Instead the release notes are read
from a versionCode-named file. Each deploy lane therefore:

1. Calls `write_changelog_file(version_code, release_notes)`, which writes
   `release_notes` to `<android_metadata_path>/en-US/changelogs/<version_code>.txt`
   only when `release_notes` is set **and** `version_code > 0`. A `version_code`
   of `0` is the [deploy.yml](../../../.github/workflows/deploy.yml) sentinel that
   skips the changelog write. The path is **absolute** (anchored to `fastlane/`)
   so it always matches where `supply` later reads the changelog from.
2. Sets `skip_upload_metadata`, `skip_upload_images`, and
   `skip_upload_screenshots` to `true` on the Play lanes, so a build/track deploy
   uploads only the APK + changelog and can **never** overwrite the live store
   listing text or screenshots.

To change store listing text or screenshots, edit the files under
[fastlane/metadata/android/en-US/](../../../fastlane/metadata/android) and upload
them deliberately — they are intentionally excluded from the deploy lanes.

## Running a deploy

Build a signed release APK first (CI does this in a separate job), then call the
lane with the matching APK path. Examples mirror the CI invocations:

Play tester track (internal testing by default):

```bash
bundle exec fastlane deploy_playstore_test \
  apk_path:"app/build/outputs/apk/android/release/app-android-release-v3.7.4.apk" \
  version_name:"3.7.4" \
  version_code:"88" \
  track:"internal" \
  release_notes:"Added 2026 coins and bug fixes."
```

Play production:

```bash
# Promote the release already on the internal track (by versionCode) — the
# pre-release workflow uploaded it there, and Play forbids re-uploading a
# published APK. This is what promote.yml uses.
bundle exec fastlane deploy_playstore_production \
  version_name:"3.7.4" \
  version_code:"88" \
  release_notes:"Added 2026 coins and bug fixes."

# Upload mode (fresh versionCode never sent to any track): pass apk_path.
bundle exec fastlane deploy_playstore_production \
  apk_path:"downloaded_apks/app-android-release-v3.7.4.apk" \
  version_name:"3.7.4" \
  version_code:"88" \
  release_notes:"Added 2026 coins and bug fixes."
```

Amazon Appstore:

```bash
bundle exec fastlane deploy_amazon_appstore \
  apk_path:"downloaded_apks/app-amazon-release-v3.7.4.apk" \
  version_name:"3.7.4" \
  version_code:"88" \
  release_notes:"Added 2026 coins and bug fixes."
```

> Run these only when you intend to publish. They push to live store tracks.
> Use a tester track (internal/alpha/beta) to validate before production.

## CI release flow

Deployment is normally driven by GitHub Actions, not run by hand. See
[DEPLOYMENT.md](../../../DEPLOYMENT.md) for the full process. In short:

1. **Pre-release** — [.github/workflows/release.yml](../../../.github/workflows/release.yml)
   builds the `android` + `amazon` release APKs, creates a GitHub pre-release
   tagged `vX.Y.Z-rc.N`, and calls `deploy_playstore_test track:"internal"`.
2. **Promote** — [.github/workflows/promote.yml](../../../.github/workflows/promote.yml)
   downloads the *same* APKs from the pre-release, creates the final `vX.Y.Z`
   release, then calls `deploy_playstore_production` and `deploy_amazon_appstore`.
3. **F-Droid** watches git tags and builds automatically from the final `vX.Y.Z`
   tag (it ignores `-rc.N` tags).

[.github/workflows/deploy.yml](../../../.github/workflows/deploy.yml) is a manual
fallback that can target either Play track directly.

## Verifying changes

After editing the Fastfile or Gemfile:

```bash
bundle exec fastlane lanes          # syntax-check the Fastfile, list lanes
```

- [.github/workflows/fastlane-bundle-check.yml](../../../.github/workflows/fastlane-bundle-check.yml)
  verifies the bundle installs and the Fastfile loads on CI.
- The Fastlane version is pinned in [Gemfile](../../../Gemfile)
  (`fastlane ~> 2.236.1` plus `fastlane-plugin-amazon_appstore`). Do **not** add
  `update_fastlane` to the Fastfile — self-updating mid-run has caused flaky CI.
  Bump versions deliberately with `bundle update fastlane` and commit
  `Gemfile.lock`.

## Troubleshooting

| Symptom | Cause / fix |
| --- | --- |
| `Could not find option 'changelogs'` | `supply` has no inline changelogs option — write the versionCode-named changelog file instead (already handled by the lanes). |
| `Could not find folder metadata/android` | A relative `metadata_path` resolved against the wrong working directory. Use the `android_metadata_path` helper (absolute path) for both `metadata_path` and changelog writes. |
| Changelog/metadata lands in `fastlane/fastlane/metadata` | A path was written as `./fastlane/...` or relative. Use the absolute `android_metadata_path` helper. |
| `APK path must be provided ... and exist` | `apk_path` missing or wrong filename — confirm `app-{flavor}-{buildType}-v{versionName}.apk` exists. |
| `AMAZON_CLIENT_ID and AMAZON_CLIENT_SECRET must be set` | Export both env vars before `deploy_amazon_appstore`. |
| Play upload rejected as unsigned | The release APK was built without the `SIGNING_*` vars / `signing.properties`. |
| Locale / encoding errors from supply | Export `LC_ALL=en_US.UTF-8` and `LANG=en_US.UTF-8`. |

## Related files

| File | Purpose |
| --- | --- |
| [fastlane/Fastfile](../../../fastlane/Fastfile) | All lanes (source of truth for option names) |
| [fastlane/Appfile](../../../fastlane/Appfile) | `json_key_file` + `package_name` |
| [fastlane/Screengrabfile](../../../fastlane/Screengrabfile) | Screenshot capture config |
| [Gemfile](../../../Gemfile) | Pinned Fastlane + Amazon plugin versions |
| [app/build.gradle](../../../app/build.gradle) | Signing config, flavors, APK naming |
| [DEPLOYMENT.md](../../../DEPLOYMENT.md) | End-to-end release process |
| [fastlane/metadata/android/en-US/](../../../fastlane/metadata/android) | Store listing text, images, changelogs |
| capture-store-screenshots skill | Regenerate the 18 store screenshots |
