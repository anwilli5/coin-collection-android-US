# Deployment Process

This document covers two things:

1. **One-time setup** — registering signing keys, store credentials, and GitHub
   secrets so the CI workflows can build and publish.
2. **Cutting a release** — the runbook for shipping a new version to alpha
   testers first, and then to production (Google Play, Amazon Appstore, and
   F-Droid).

> **Context.** This app has been published to Google Play, the Amazon
> Appstore, and F-Droid for years; releases have historically been built and
> uploaded manually from a developer machine using the original legacy
> upload keystore (created ~2014). The workflows below shift that work into
> GitHub Actions while continuing to sign with the **same** legacy keystore
> — do not generate a new one. Google Play and Amazon will reject any APK
> signed with a different upload key.

The release flow is a **two-stage pipeline**: first a pre-release that only
reaches alpha testers, then a promotion that ships the *exact same* artifact
everywhere.

- **Build and Publish Pre-release** ([`.github/workflows/release.yml`](.github/workflows/release.yml))
  builds and signs both flavor APKs (`android` and `amazon`), publishes a
  GitHub **pre-release** tagged `vX.Y.Z-rc.N`, and deploys the `android` APK
  to the Google Play **alpha** (closed testing) track. It deliberately does
  **not** touch Play production, Amazon, or F-Droid.
- **Promote Pre-release to Release** ([`.github/workflows/promote.yml`](.github/workflows/promote.yml))
  takes a verified pre-release, re-attaches its exact APKs to a final
  GitHub Release tagged `vX.Y.Z`, and deploys to Google Play **production**
  and the **Amazon Appstore**. Creating the bare `vX.Y.Z` tag is what
  releases the build to **F-Droid**.
- **Deploy Release to App Stores** ([`.github/workflows/deploy.yml`](.github/workflows/deploy.yml))
  is an optional manual fallback for ad-hoc deploys of an existing GitHub
  Release to a chosen track. The standard flow uses the two workflows above.

> **Why two stages and an RC tag?** F-Droid's build server watches this
> repo's git **tags** — not GitHub's "pre-release" flag. To keep a build out
> of F-Droid (and Play production / Amazon) while alpha testing, the
> pre-release uses a distinct `vX.Y.Z-rc.N` tag and the bare `vX.Y.Z` tag is
> created only at promotion. For this to work, F-Droid's metadata must be
> told to ignore RC tags — see
> [Step 1.9](#19-restrict-f-droid-to-final-release-tags).

---

## 1. One-time setup

You only need to do this once per repository. Skip ahead to
[Cutting a release](#2-cutting-a-release) if everything below is already
configured.

### 1.1 Locate the existing legacy upload keystore

The app has been published for years, so a working upload keystore already
exists on the maintainer's development machine — typically the same
`signing.properties` + `.jks`/`.keystore` pair used for manual builds. **Use
that exact keystore.** Do not generate a new one: Google Play (and Amazon)
bind your package to the certificate that signed earlier uploads, and an
APK signed with a different key is rejected on upload.

1. On the machine where releases have historically been built, find the
   keystore. Common locations:
   - The `storeFilePath` value referenced by the repo's local
     `signing.properties` (gitignored).
   - Older paths used pre-CI such as `~/keystores/coin-collection.jks` or
     similar.
2. Record four values — all needed as GitHub Secrets:
   - The keystore file itself (the `.jks` / `.keystore`).
   - The keystore (store) password.
   - The key alias.
   - The key password.
3. Confirm the keystore's certificate matches what Google Play has on
   record. In **Google Play Console → Setup → App integrity → App signing**,
   note the **Upload certificate** SHA-1 fingerprint. Then locally:
   ```bash
   keytool -list -v -keystore /path/to/your.keystore -alias <alias>
   ```
   The `SHA1:` line under "Certificate fingerprints" must match the SHA-1
   shown in Play Console. If it doesn't, you have the wrong keystore (or
   the wrong alias — try `keytool -list -keystore /path/to/your.keystore`
   with no alias to list all entries).

> **Legacy-key compatibility notes.**
>
> - A ~2014-era keystore is almost certainly **1024-bit RSA / SHA1withRSA**.
>   That's fine for *uploading* to Google Play: Play App Signing handles
>   re-signing distributed APKs with a modern key. The upload key only has
>   to match what Play has on record — it does **not** need to satisfy
>   modern strength requirements.
> - For **Amazon Appstore**, which historically does *not* re-sign, the
>   same legacy key continues to work because the listing is already bound
>   to it. Continuing to sign with the same key preserves update
>   compatibility for existing installs.
> - For **F-Droid**, signing is irrelevant — F-Droid rebuilds from source
>   and re-signs with its own key.
> - **Do not** "upgrade" the upload key by replacing the keystore. If you
>   want to rotate to a stronger upload key in the future, that's a
>   separate Play Console workflow (**App signing → Request upload key
>   reset**); do it deliberately, not as a side effect of CI work.
>
> **Back up the keystore before doing anything else.** Losing this file
> means you can no longer publish updates to existing installs on Google
> Play (without a Play-side key reset) or Amazon (no recovery). Make at
> least one encrypted offline backup before proceeding.

### 1.2 Base64-encode the keystore

GitHub Secrets can't hold binary data, so the keystore is stored as base64.
Run this on the same machine where the legacy keystore lives.

- macOS / Linux:
  ```bash
  base64 -i /path/to/your.keystore -o keystore.b64
  ```
- Windows PowerShell:
  ```powershell
  [Convert]::ToBase64String([IO.File]::ReadAllBytes("C:\path\to\your.keystore")) | Out-File keystore.b64 -Encoding ASCII
  ```

The contents of `keystore.b64` go into the `SIGNING_KEYSTORE_DATA` secret in
[Step 1.6](#16-register-github-secrets). Delete `keystore.b64` after pasting
it into GitHub — it is the keystore in plaintext-recoverable form.

### 1.3 Create (or reuse) a Google Play service account

Fastlane authenticates to the Play Developer API with a service-account JSON
key. If the maintainer has been using Fastlane locally for manual uploads,
this service account likely already exists — reuse the same JSON file. Skip
to Step 1.4 in that case.

If there's no existing service account:

1. Open **Google Play Console → Setup → API access**.
2. **Link** your Google Cloud project (or create one) if you haven't already.
   The **Service accounts** section on this page stays hidden until a Google
   Cloud project is linked — if you don't see it, complete this step first
   and scroll down past the linked-project panel.
3. Under **Service accounts**, click **Create new service account**. This does
   not create it inline — it opens **Google Cloud Console → IAM & Admin →
   Service Accounts**. There, click **Create service account**, finish the
   wizard, then open the new account's **Keys → Add key → Create new key →
   JSON** and download the JSON file (keep it safe; it's the secret).
4. Back in Play Console → API access, refresh so the new account appears,
   then click **Grant access** next to it and give it at minimum:
   - **Release manager** role (or **Admin**), scoped to this app.
   - Permissions: `Release to production`, `Release to testing tracks`,
     `Manage testing track access`.
5. The full JSON file's contents go into the `GOOGLE_PLAY_JSON_KEY_DATA`
   secret in [Step 1.6](#16-register-github-secrets).

### 1.4 Enable the Google Play Closed testing (alpha) track

The first deploy will fail if the Closed testing track doesn't exist yet.

1. **Google Play Console → Testing → Closed testing**.
2. Click **Create track** (or use the default "Alpha" track).
3. Add at least one tester (email list or Google group). You'll need this to
   verify the test deploy in [Step 2.3](#23-verify-on-the-alpha-track).
4. Save. You do not need to create a release here — Fastlane will.

The track ID Fastlane uses is `alpha` (default), `beta`, or `internal`. Make
sure the track you intend to target actually exists in Play Console.

### 1.5 Reuse or create Amazon Appstore API credentials

The app already has an Amazon Appstore listing. If the maintainer has been
uploading to Amazon via Fastlane locally, the API credentials likely already
exist on that machine — reuse them. Otherwise:

1. Open the [Amazon Developer Console](https://developer.amazon.com/) and
   sign in with the account that owns the existing Appstore listing.
2. Go to **Settings → Security Profile → API access**. Create a new security
   profile if needed (existing profiles can be reused).
3. Generate a **Client ID** and **Client Secret** with the **App Submission
   API** scope enabled. Authorize this profile for your Appstore app.
4. These values go into `AMAZON_CLIENT_ID` and `AMAZON_CLIENT_SECRET` in
   [Step 1.6](#16-register-github-secrets).

### 1.6 Register GitHub Secrets

In the repository, go to **Settings → Secrets and variables → Actions →
New repository secret** and add all of the following. Names must match
exactly.

| Secret | Source | Used by |
| --- | --- | --- |
| `SIGNING_KEYSTORE_DATA` | base64 of the keystore from Step 1.2 | `release.yml` |
| `SIGNING_KEYSTORE_PASSWORD` | keystore password from Step 1.1 | `release.yml` |
| `SIGNING_KEY_ALIAS` | key alias from Step 1.1 | `release.yml` |
| `SIGNING_KEY_PASSWORD` | key password from Step 1.1 | `release.yml` |
| `GOOGLE_PLAY_JSON_KEY_DATA` | full JSON file from Step 1.3 | `deploy.yml` |
| `AMAZON_CLIENT_ID` | from Step 1.5 | `deploy.yml` |
| `AMAZON_CLIENT_SECRET` | from Step 1.5 | `deploy.yml` |

> `GITHUB_TOKEN` is provided automatically by Actions — do not create it.

### 1.7 Verify signing locally (recommended)

If manual builds already work on the maintainer's machine, the local
`signing.properties` is already correct and this step can be skipped — but
it's still worth running once to confirm the certificate fingerprint matches
what's in Play Console (from Step 1.1).

```bash
./gradlew :app:assembleAndroidRelease
$ANDROID_HOME/build-tools/*/apksigner verify --print-certs \
  app/android/release/app-android-release-v*.apk
```

Check two things in the output:

- The certificate **must not** show `CN=Android Debug`. If it does, the
  signing config is not being applied — re-check `signing.properties`
  paths.
- The `SHA-1 digest` (or `SHA1` fingerprint) shown must match the **Upload
  certificate** SHA-1 from Play Console (Step 1.1). If they differ, the
  APK will be rejected on upload.

> CI uses the `SIGNING_*` environment variables (sourced from the secrets
> above), not `signing.properties`. The Gradle build prefers env vars when
> they're set; see [`app/build.gradle`](app/build.gradle).

### 1.8 (Optional) Protect the workflows

These workflows can publish releases and ship to stores. Consider:

- **Branch protection on `main`**: require PR review and a green
  [Android CI](.github/workflows/gradle.yml) check before merge. Releases
  are cut from `main`, so a broken `main` blocks bad releases.
- **Environment with required reviewers** for `deploy.yml` — add an
  `environment: production` block and configure required reviewers in
  **Settings → Environments**. (Not enabled by default; left to operator.)

### 1.9 Restrict F-Droid to final release tags

F-Droid's build server tracks this repo through its metadata recipe at
[`metadata/com.spencerpages.yml`](https://gitlab.com/fdroid/fdroiddata/-/blob/master/metadata/com.spencerpages.yml)
in the `fdroiddata` project. That recipe currently uses:

```yaml
AutoUpdateMode: Version
UpdateCheckMode: Tags
```

`UpdateCheckMode: Tags` with **no filter** makes F-Droid consider *every*
tag — including the `vX.Y.Z-rc.N` pre-release tags. Because an RC tag carries
the new `versionCode` (it has to, to upload to Play alpha), F-Droid would
detect it and try to build the pre-release. To prevent that, restrict the
check to final tags by opening a merge request against `fdroiddata` that
changes the line to:

```yaml
UpdateCheckMode: Tags ^v[0-9.]+$
```

The `^v[0-9.]+$` regex matches `v3.7.4` but **not** `v3.7.4-rc.1` (the hyphen
and letters are excluded). After this change, F-Droid only builds the bare
`vX.Y.Z` tag created at promotion time. This is a **one-time** change; you do
not need to touch it for future releases.

> The app's APK naming already matches F-Droid's `Binaries:` template
> (`app-android-release-v%v.apk`, where `%v` is the `versionName` with no RC
> suffix), so no other `fdroiddata` change is required.

---

## 2. Cutting a release

The pipeline has two operator-driven stages.

- **Stage 1 — Pre-release** ([Step 2.1](#21-build-and-publish-the-pre-release))
  builds the signed APKs, publishes them as a GitHub **pre-release** tagged
  `vX.Y.Z-rc.N`, and ships the `android` APK to Google Play **alpha**. The
  bare `vX.Y.Z` tag is *not* created, so F-Droid, Play production, and Amazon
  are untouched. Alpha testers verify ([Step 2.2](#22-verify-on-the-alpha-track)).
- **Stage 2 — Promote** ([Step 2.3](#23-promote-the-pre-release-to-a-release))
  re-attaches the *exact same* APKs to a final GitHub Release tagged
  `vX.Y.Z` and ships to Google Play **production** and **Amazon**. Creating
  the `vX.Y.Z` tag releases the build to **F-Droid** automatically.


```
  bump version in          release.yml                    promote.yml
  AndroidManifest.xml ─►   (Stage 1, Step 2.1)       ─►   (Stage 2, Step 2.3)
  + merge to main          tag vX.Y.Z-rc.N                tag vX.Y.Z
                           GitHub pre-release             final GitHub Release
                           + Play alpha                   + Play production + Amazon
                                  │                               │
                                  └─► Alpha testers verify        ├─► Google Play production
                                      (Step 2.2)                  ├─► Amazon Appstore
                                                                  └─► F-Droid scans vX.Y.Z tag
                                                                      (1–3 day lag)
```

### 2.1 Build and publish the pre-release

1. Bump `android:versionName` **and** `android:versionCode` in
   [`app/src/main/AndroidManifest.xml`](app/src/main/AndroidManifest.xml).
   `versionCode` must strictly increase from the last published value on
   every store. Because the app has been published for years, the current
   value is already well into the dozens — just `+1` from whatever's on
   `main` (and confirm it exceeds the latest Play / Amazon production
   versionCode).
2. Merge the version bump to `main`.
3. In GitHub, go to **Actions → Build and Publish Pre-release → Run
   workflow** and fill in the inputs:
   - `rc_number`: the release-candidate number for this attempt (`1` the
     first time; bump to `2`, `3`, … if you need another pre-release of the
     same version). This produces the tag `vX.Y.Z-rc.N`.
   - `releaseNotes`: the "what's new" text shown to Play alpha testers.
4. The workflow:
   - Refuses to run if the final tag `vX.Y.Z` **or** the chosen
     `vX.Y.Z-rc.N` tag already exists.
   - Builds `:app:assembleAndroidRelease` and `:app:assembleAmazonRelease`.
   - Verifies the APKs are signed with the release key (fails on
     debug-signed APKs).
   - Creates a GitHub **pre-release** tagged `vX.Y.Z-rc.N` with both APKs
     attached.
   - Uploads the `android` APK to the Google Play **alpha** track.
5. F-Droid does **not** build at this stage: the `vX.Y.Z-rc.N` tag is
   excluded by the `UpdateCheckMode` regex from
   [Step 1.9](#19-restrict-f-droid-to-final-release-tags), and the bare
   `vX.Y.Z` tag does not exist yet. Amazon and Play production are untouched.

### 2.2 Verify on the alpha track

1. Open Google Play Console → **Testing → Closed testing → Alpha**. Confirm
   a new release exists with the expected `versionCode`.
2. On a test device opted into the closed-test program, install the build
   from the Play Store and exercise the changed flows.
3. Watch the Play Console pre-launch report for crashes / policy issues
   before promoting.
4. If a problem is found, fix it, bump `versionCode`/`versionName` as
   needed, and run [Step 2.1](#21-build-and-publish-the-pre-release) again
   with the next `rc_number` (or new version). Only promote once a
   pre-release is green.

### 2.3 Promote the pre-release to a release

Once alpha testing is green:

1. **Actions → Promote Pre-release to Release → Run workflow**.
2. Inputs:
   - `rc_tag`: the pre-release tag you verified, e.g. `v3.7.4-rc.1`.
   - `releaseNotes`: production "what's new" text for Google Play and Amazon.
     May match or differ from the alpha notes.
3. The workflow:
   - Verifies the pre-release exists and the final `vX.Y.Z` release does not.
   - Downloads the exact APKs from the pre-release (no rebuild — guaranteeing
     production gets the same bytes alpha testers verified).
   - Creates the final GitHub Release tagged `vX.Y.Z`, pointing at the same
     commit as the RC tag, with both APKs re-attached.
   - Uploads the `android` APK to Google Play **production** and the
     `amazon` APK to the **Amazon Appstore**.
4. Both stores review independently. Google Play production reviews
   typically take a few hours to a couple of days; Amazon Appstore reviews
   take longer.
5. F-Droid's build server picks up the new `vX.Y.Z` tag at its next scan,
   rebuilds from source, and publishes. Expect F-Droid availability to lag
   the GitHub Release by 1–3 days. No action required.

> The `vX.Y.Z-rc.N` pre-release is kept after promotion as an audit trail.
> You can delete it manually from the GitHub Releases page if you prefer.

---

## 3. Fastlane lanes (reference)

Defined in [`fastlane/Fastfile`](fastlane/Fastfile). Each workflow passes a
pre-built APK to the relevant lane via `apk_path` — the pre-release workflow
builds it fresh, the promote workflow downloads it from the pre-release.

- `deploy_playstore_test`
  - Uploads to a Google Play tester track (`alpha`, `beta`, or `internal`).
    The pre-release workflow always uses `alpha`.
  - Inputs: `apk_path`, `track`, `release_notes`, optional
    `version_name`, `version_code`.
- `deploy_playstore_production`
  - Uploads to the Google Play production track. Used by the promote workflow.
  - Inputs: `apk_path`, `release_notes`.
- `deploy_amazon_appstore`
  - Uploads to the Amazon Appstore via `fastlane-plugin-amazon_appstore`.
    Used by the promote workflow.
  - Inputs: `apk_path`, `release_notes`, `version_code`. The promote workflow
    passes the real `version_code` (read from the manifest at the RC tag),
    so the Amazon changelog file is written from `release_notes`. A
    `version_code` of `0` (the sentinel used by the optional `deploy.yml`
    fallback) skips the changelog rewrite and reuses existing metadata.

---

## 4. Troubleshooting

**Pre-release workflow fails with `Tag vX.Y.Z already exists` (or
`vX.Y.Z-rc.N already exists`).**
The final tag exists because this version was already released — bump
`android:versionName` in
[`app/src/main/AndroidManifest.xml`](app/src/main/AndroidManifest.xml) and
merge to `main`. If only the RC tag exists, just pick a higher `rc_number`
and re-run.

**Release workflow fails with `... is signed with the Android debug key`.**
One of the four `SIGNING_*` secrets is missing or wrong. Confirm all four
exist in repo settings and that `SIGNING_KEYSTORE_DATA` is the base64 of the
actual legacy upload keystore (not the debug keystore from `~/.android`,
and not a freshly generated one).

**Google Play upload rejected with `... your APK is not signed with the
upload certificate`.**
The SHA-1 of the signing certificate in the uploaded APK doesn't match the
upload certificate Google Play has on record. Almost always this means CI
is signing with the wrong key. Re-verify per [Step 1.1](#11-locate-the-existing-legacy-upload-keystore):
run `keytool -list -v -keystore ... -alias ...` against the keystore being
used, and compare its SHA-1 to **Play Console → Setup → App integrity →
App signing → Upload certificate**. They must match exactly.

**Release workflow fails decoding the keystore (`decoded keystore is empty`).**
`SIGNING_KEYSTORE_DATA` value is not valid base64. Recreate it with
`base64 -i upload-keystore.jks -o keystore.b64` and paste the entire file
contents into the secret.

**Promote workflow fails with `pre-release ... not found`.**
The pre-release workflow has not been run for that `rc_tag`, or the tag is
mistyped. Run **Build and Publish Pre-release** first, then promote the
`vX.Y.Z-rc.N` tag it created.

**Promote workflow fails with `final release vX.Y.Z already exists`.**
This pre-release was already promoted. If you need to re-ship, bump the
version and start a new pre-release.

**Deploy fails: `Google Api Error: ... versionCode N has already been used`.**
The `versionCode` in the APK has been uploaded to Play before. You cannot
re-use a versionCode on Google Play. Bump `android:versionCode` and run the
pre-release workflow again.

**Deploy fails: `... GOOGLE_PLAY_JSON_KEY_DATA did not parse as JSON`.**
The secret was pasted with surrounding quotes or had characters stripped.
Re-download the service-account JSON and paste the complete file contents
(starts with `{`, ends with `}`).

**Deploy fails: `track 'alpha' is not a valid track`.**
The Closed testing track is not set up in Play Console. Complete
[Step 1.4](#14-enable-the-google-play-closed-testing-alpha-track).

**Amazon Appstore deploy fails: `version_code already submitted`.**
Same issue as Google Play — Amazon also enforces strictly increasing
versionCodes. Bump and re-release.

**F-Droid built a pre-release / RC tag.**
The `fdroiddata` `UpdateCheckMode` is still unfiltered `Tags`. Apply the
regex from [Step 1.9](#19-restrict-f-droid-to-final-release-tags) so RC tags
are ignored.

**F-Droid hasn't picked up the new release.**
F-Droid scans on its own schedule and rebuilds from source. Expect a
1–3 day lag after the final `vX.Y.Z` GitHub tag is created. Track build
status at the F-Droid build logs / your package's metadata repo.
