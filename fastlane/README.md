fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android test_and_lint

```sh
[bundle exec] fastlane android test_and_lint
```

Runs all the tests

### android build_and_screengrab

```sh
[bundle exec] fastlane android build_and_screengrab
```

Build debug and test APK and generate screenshots

### android deploy_playstore_test

```sh
[bundle exec] fastlane android deploy_playstore_test
```

Deploy the app to Google Play Store (Internal Test Track) using a pre-built APK.

### android deploy_playstore_production

```sh
[bundle exec] fastlane android deploy_playstore_production
```

Deploy the app to Google Play Store (Production Track) using a pre-built APK.

### android deploy_amazon_appstore

```sh
[bundle exec] fastlane android deploy_amazon_appstore
```

Deploy the app to Amazon Appstore using a pre-built APK.

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
