# Deployment Process

This document outlines the process for building, releasing, and deploying the Android application using a "build once, deploy many" strategy. This is managed by two distinct GitHub Actions workflows:

1.  **Build and Create GitHub Release** (`.github/workflows/release.yml`): Builds and signs the APK, then creates a formal GitHub Release with the APK as an attachment. This release also serves as a trigger for services like F-Droid.
2.  **Deploy Release to App Stores** (`.github/workflows/deploy.yml`): Takes an existing GitHub Release (and its attached APK), downloads the APK, and deploys it to the specified app store tracks (Google Play Internal, Google Play Production, Amazon Appstore).

This separation ensures that the same artifact built and released is the one that gets tested and deployed.

## CI/CD Pipeline Overview

*   **`.github/workflows/release.yml` (Build and Create GitHub Release)**:
    *   **Purpose**: To create an official, signed build of the application and a corresponding GitHub Release. This release includes the `app-release.apk` as an asset.
    *   **Trigger**: Manual (`workflow_dispatch`).
    *   **Key Steps**:
        *   Checks out the latest code.
        *   Sets up Java and signing credentials.
        *   Builds the release APK using Gradle, incorporating the provided `versionName` and `versionCode`.
        *   Creates a GitHub Release (e.g., "Release 1.2.3") and a Git tag (e.g., "v1.2.3").
        *   Attaches the signed `app-release.apk` to this GitHub Release.

*   **`.github/workflows/deploy.yml` (Deploy Release to App Stores)**:
    *   **Purpose**: To deploy a pre-built APK (from a GitHub Release) to various app store tracks.
    *   **Trigger**: Manual (`workflow_dispatch`).
    *   **Key Steps**:
        *   Checks out the repository (to access Fastlane configuration).
        *   Sets up Ruby and Fastlane.
        *   Downloads the `app-release.apk` from a specified GitHub Release tag.
        *   Uses Fastlane lanes to deploy this downloaded APK to the selected store(s)/track(s).

## Prerequisites: GitHub Secrets Setup

The following GitHub Secrets must be configured in the repository settings (`Settings > Secrets and variables > Actions`):

1.  **`GOOGLE_PLAY_JSON_KEY_DATA`**:
    *   **Description**: JSON key for Google Play Console service account. Allows Fastlane to authenticate with Google Play Developer API.
    *   **Used by**: `deploy.yml` (for Fastlane `supply` action).
    *   **How to obtain**: Google Play Console > Setup > API access.

2.  **`SIGNING_KEYSTORE_DATA`**:
    *   **Description**: Base64 encoded Android signing keystore file (`.jks` or `.keystore`).
    *   **Used by**: `release.yml` (for signing the APK during the Gradle build).
    *   **How to obtain**: Convert your keystore file to base64.
        *   macOS/Linux: `base64 -i your_keystore_file.jks -o keystore_base64.txt`
        *   Windows (PowerShell): `[Convert]::ToBase64String([IO.File]::ReadAllBytes("your_keystore_file.jks")) | Out-File keystore_base64.txt -Encoding ASCII`
        *   Copy the content of `keystore_base64.txt` into the secret.

3.  **`SIGNING_KEYSTORE_PASSWORD`**:
    *   **Description**: Password for the Android signing keystore.
    *   **Used by**: `release.yml`.

4.  **`SIGNING_KEY_ALIAS`**:
    *   **Description**: Alias for the signing key within the keystore.
    *   **Used by**: `release.yml`.

5.  **`SIGNING_KEY_PASSWORD`**:
    *   **Description**: Password for the specific key alias.
    *   **Used by**: `release.yml`.

6.  **`AMAZON_CLIENT_ID`**:
    *   **Description**: Client ID for Amazon App Submission API.
    *   **Used by**: `deploy.yml` (for Fastlane `amazon_appstore` plugin).
    *   **How to obtain**: Amazon Developer Console documentation.

7.  **`AMAZON_CLIENT_SECRET`**:
    *   **Description**: Client Secret for Amazon App Submission API.
    *   **Used by**: `deploy.yml`.
    *   **How to obtain**: Amazon Developer Console documentation.

## Recommended Deployment Flow ("Build Once, Deploy Many")

This flow ensures a consistent artifact is built, released, tested, and deployed.

**Step 1: Build and Create GitHub Release**

1.  Navigate to `Actions > Build and Create GitHub Release`.
2.  Click **"Run workflow"**.
3.  **Inputs**:
    *   **Version Name (e.g., 1.2.3)**: The semantic version for this release (e.g., `1.0.5`). This determines the Git tag (e.g., `v1.0.5`) and Release title.
    *   **Android Version Code (e.g., 123)**: The unique integer version code (e.g., `10500`).
    *   **Release Notes**: (Required) Detailed notes for the body of the GitHub Release.
4.  Click **"Run workflow"**.
    *   **Outcome**:
        *   The `release.yml` workflow runs.
        *   A signed `app-release.apk` is built using the provided version information.
        *   A GitHub Release (e.g., "Release 1.0.5") is created with the specified release notes.
        *   A Git tag (e.g., "v1.0.5") is created.
        *   The `app-release.apk` is attached as an asset to this GitHub Release.
        *   This release can be used by F-Droid or for direct downloads.

**Step 2: Deploy to Google Play Internal Test Track for Verification**

1.  Navigate to `Actions > Deploy Release to App Stores`.
2.  Click **"Run workflow"**.
3.  **Inputs**:
    *   **Version Name (e.g., 1.2.3)**: Enter the **exact same `versionName`** from Step 1 (e.g., `1.0.5`). This tells the workflow which GitHub Release tag to find the APK from.
    *   **Android Version Code (e.g., 123)**: Enter the **exact same `versionCode`** from Step 1. This is used for store metadata consistency.
    *   **Deployment target**: Select `google_play_internal_test_track`.
    *   **Release Notes**: (Optional) Provide release notes for the Google Play Internal track. Can be different from the GitHub Release notes if desired. Defaults to "Updated version."
4.  Click **"Run workflow"**.
    *   **Outcome**:
        *   The `deploy.yml` workflow runs.
        *   It downloads `app-release.apk` from the GitHub Release created in Step 1 (e.g., from tag `v1.0.5`).
        *   The downloaded APK is deployed to the Google Play Store's Internal test track using the `deploy_playstore` Fastlane lane.

**Step 3: Test Thoroughly on Google Play Internal Track**

1.  Access your Google Play Console.
2.  Distribute the internal test build to your testers.
3.  Conduct comprehensive testing of the application.

**Step 4: Deploy to Production Stores (Google Play Production & Amazon Appstore)**

1.  Once the build from Step 2 has been successfully tested and approved:
2.  Navigate to `Actions > Deploy Release to App Stores` again.
3.  Click **"Run workflow"**.
4.  **Inputs**:
    *   **Version Name (e.g., 1.2.3)**: Enter the **exact same `versionName`** from Step 1 & 2 (e.g., `1.0.5`).
    *   **Android Version Code (e.g., 123)**: Enter the **exact same `versionCode`** from Step 1 & 2.
    *   **Deployment target**: Select `production_google_play_and_amazon`.
    *   **Release Notes**: (Optional) Provide the final release notes for the public store listings.
5.  Click **"Run workflow"**.
    *   **Outcome**:
        *   The `deploy.yml` workflow runs again.
        *   It downloads the *same* `app-release.apk` from the GitHub Release (e.g., from tag `v1.0.5`).
        *   The APK is deployed to the Google Play Store's Production track via the `deploy_playstore_production` Fastlane lane.
        *   The APK is deployed to the Amazon Appstore via the `deploy_amazon_appstore` Fastlane lane.

This process ensures that the exact artifact built and attached to the GitHub Release is the one tested and subsequently deployed to production environments.

## Fastlane Lanes

The Fastlane lanes defined in `fastlane/Fastfile` are now designed to deploy pre-built APKs. They expect an `apk_path` parameter pointing to the APK to be deployed.

*   `deploy_playstore`:
    *   Deploys the APK (specified by `options[:apk_path]`) to the Google Play Store's **internal** test track.
    *   Uses `options[:release_notes]` for the changelog on this track.
*   `deploy_playstore_production`:
    *   Deploys the APK (specified by `options[:apk_path]`) to the Google Play Store's **production** track.
    *   Uses `options[:release_notes]` for the changelog on this track.
*   `deploy_amazon_appstore`:
    *   Deploys the APK (specified by `options[:apk_path]`) to the Amazon Appstore.
    *   If `options[:release_notes]` and `options[:version_code]` are provided, writes release notes to `fastlane/metadata/android/en-US/changelogs/#{options[:version_code]}.txt` for the plugin to pick up.

## Important Notes

*   **Build Configuration**: The `release.yml` workflow uses Gradle to build the APK. Ensure your `app/build.gradle` file is set up to correctly use the `appVersionName` and `appVersionCode` properties passed by the workflow for versioning the APK.
*   **Keystore Availability**: The `release.yml` workflow requires signing secrets to produce a signed release APK. The `deploy.yml` workflow does not need these secrets as it uses the pre-signed APK from the GitHub Release.
*   **Tagging Convention**: The `release.yml` workflow creates Git tags prefixed with `v` (e.g., `v1.2.3`). The `deploy.yml` workflow expects this convention when fetching the release asset.
*   **Consistency is Key**: Always use the same `versionName` and `versionCode` across all steps for a given release to ensure you are testing and deploying the correct build.
*   **Ruby and Java Versions**: The workflows use specific Ruby and Java versions (e.g., Ruby 2.7, Java 11). Adjust these in the workflow YAML files if your project has different requirements.

This comprehensive deployment strategy promotes reliability by building the deployment artifact once, then using that same artifact through testing and final production deployment.
