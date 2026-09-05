source "https://rubygems.org"

gem "fastlane", "~> 2.237.0"
gem 'fastlane-plugin-amazon_appstore', '~> 1.7.0'

# Test-only: fastlane/test/fastfile_test.rb drives the lanes with the store
# upload actions stubbed. minitest ships with Ruby but is not a default gem
# under `bundle exec`, so it has to be declared here.
group :test do
  gem "minitest", "~> 5.25"
end
