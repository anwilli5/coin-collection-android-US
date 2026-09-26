source "https://rubygems.org"

gem "fastlane", "~> 2.240.1"
gem 'fastlane-plugin-amazon_appstore', '~> 1.7.0'

# Test-only: fastlane/test/fastfile_test.rb drives the lanes with the store
# upload actions stubbed. minitest ships with Ruby but is not a default gem
# under `bundle exec`, so it has to be declared here. minitest 6 moved
# Object#stub out into the separate minitest-mock gem.
group :test do
  gem "minitest", "~> 6.0"
  gem "minitest-mock", "~> 5.27"
end
