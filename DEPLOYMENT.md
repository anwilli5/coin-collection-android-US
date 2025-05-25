# Deployment Process

This document outlines the process for building, releasing, and deploying the Coin Collection Android application. This is managed by two distinct GitHub Actions workflows:

1. **Build and Create GitHub Release** (`.github/workflows/release.yml`): Builds and signs the APK, then creates a formal GitHub Release with the APK as an attachment. GitHub Releases are picked up automatically by the F-Droid App Repository.
2. **Deploy Release to App Stores** (`.github/workflows/deploy.yml`): Takes an existing GitHub Release (and its attached APKs), downloads the APKs, and deploys them to the specified app store tracks (Google Play Internal, Google Play Production, Amazon Appstore).

## CI/CD Pipeline Overview

- **`.github/workflows/release.yml` (Build and Create GitHub Release)**:
    - **Purpose**: To create an official, signed build of the application and a corresponding GitHub Release. This release includes the android and amazon APKs.
    - **Trigger**: Manual (`workflow_dispatch`).

- **`.github/workflows/deploy.yml` (Deploy Release to App Stores)**:
    - **Purpose**: To deploy pre-built APKs (from GitHub Release) to various app store tracks.
    - **Trigger**: Manual (`workflow_dispatch`).
    - **Targets**: Select between the following deployment targets:
      - `google_play_internal_test_track` Select this first to test the release version of the app via the Google Play test track.
      - `production_google_play_and_amazon` Select this to release to all Google Play / Amazon App Store users.

## Recommended Deployment Flow

This flow ensures a consistent artifact is built, released, tested, and deployed.

**Step 1: Build and Create GitHub Release**

1.  Navigate to `Actions > Build and Create GitHub Release`.
2.  Click **"Run workflow"**.
    - **Outcome**:
        - The `release.yml` workflow runs.
        - Signed APKs are built using the provided version information in AndroidManifest.xml.
        - A GitHub Release and release tag are created.

**Step 2: Deploy to Google Play Internal Test Track for Verification**

1.  Navigate to `Actions > Deploy Release to App Stores`.
2.  Click **"Run workflow"**.
    - **Deployment target**: Select `google_play_internal_test_track`.
    - **Outcome**:
        - The `deploy.yml` workflow runs.
        - It downloads APKs from the GitHub Release created in Step 1 (e.g., from tag `v1.0.5`).
        - The downloaded Android APK is deployed to the Google Play Store's Internal test track using the `deploy_playstore_test` Fastlane lane.

**Step 3: Test Thoroughly on Google Play Internal Track**

1.  Access your Google Play Console.
2.  Distribute the internal test build to your testers.
3.  Conduct comprehensive testing of the application.

**Step 4: Deploy to Production Stores (Google Play Production & Amazon Appstore)**

1.  Once the build from Step 2 has been successfully tested and approved:
2.  Navigate to `Actions > Deploy Release to App Stores` again.
3.  Click **"Run workflow"**.
    - **Deployment target**: Select `production_google_play_and_amazon`.
    - **Outcome**:
        - The `deploy.yml` workflow runs again.
        - It downloads the android and amazon APKs from the GitHub Release (e.g., from tag `v1.0.5`).
        - The Android APK is deployed to the Google Play Store's Production track via the `deploy_playstore_production` Fastlane lane.
        - The Amazon APK is deployed to the Amazon Appstore via the `deploy_amazon_appstore` Fastlane lane.

This process ensures that the exact artifact built and attached to the GitHub Release is the one tested and subsequently deployed to production environments.

## Fastlane Lanes

The Fastlane lanes defined in `fastlane/Fastfile` are now designed to deploy pre-built APKs. They expect an `apk_path` parameter pointing to the APK to be deployed.

- `deploy_playstore_test`:
    - Deploys the APK (specified by `options[:apk_path]`) to the Google Play Store's **internal** test track.
    - Uses `options[:release_notes]` for the changelog on this track.
- `deploy_playstore_production`:
    - Deploys the APK (specified by `options[:apk_path]`) to the Google Play Store's **production** track.
    - Uses `options[:release_notes]` for the changelog on this track.
- `deploy_amazon_appstore`:
    - Deploys the APK (specified by `options[:apk_path]`) to the Amazon Appstore.
    - If `options[:release_notes]` and `options[:version_code]` are provided, writes release notes to `fastlane/metadata/android/en-US/changelogs/#{options[:version_code]}.txt` for the plugin to pick up.
