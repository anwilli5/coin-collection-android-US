# Deployment Process

This document is the runbook for **cutting a release** — shipping a new
version to internal testers first, then to production (Google Play, Amazon
Appstore, and F-Droid).

> **Prerequisite.** The one-time setup (signing secrets, store credentials,
> the Google Play internal-testing track, and the F-Droid tag filter) is
> already configured for this repository. The steps below assume those
> secrets and credentials are in place.
>
> **Context.** This app is signed with the original legacy upload keystore
> (created ~2014). Google Play and Amazon will reject any APK signed with a
> different upload key, so CI continues to sign with that **same** keystore
> via the `SIGNING_*` secrets — never generate a new one.

The release flow is a **two-stage pipeline**: first a pre-release that only
reaches internal testers, then a promotion that ships the *exact same* artifact
everywhere.

- **Build and Publish Pre-release** ([`.github/workflows/release.yml`](.github/workflows/release.yml))
  builds and signs both flavor APKs (`android` and `amazon`), publishes a
  GitHub **pre-release** tagged `vX.Y.Z-rc.N`, and deploys the `android` APK
  to the Google Play **internal** testing track. It deliberately does
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
> of F-Droid (and Play production / Amazon) while internal testing, the
> pre-release uses a distinct `vX.Y.Z-rc.N` tag and the bare `vX.Y.Z` tag is
> created only at promotion. F-Droid's metadata is already configured to
> ignore RC tags (`UpdateCheckMode: Tags ^v[0-9.]+$`), so only the final
> `vX.Y.Z` tag triggers an F-Droid build.

---

## 1. Cutting a release

The pipeline has two operator-driven stages.

- **Stage 1 — Pre-release** ([Step 1.1](#11-build-and-publish-the-pre-release))
  builds the signed APKs, publishes them as a GitHub **pre-release** tagged
  `vX.Y.Z-rc.N`, and ships the `android` APK to Google Play **internal**. The
  bare `vX.Y.Z` tag is *not* created, so F-Droid, Play production, and Amazon
  are untouched. Internal testers verify ([Step 1.2](#12-verify-on-the-internal-track)).
- **Stage 2 — Promote** ([Step 1.3](#13-promote-the-pre-release-to-a-release))
  re-attaches the *exact same* APKs to a final GitHub Release tagged
  `vX.Y.Z` and ships to Google Play **production** and **Amazon**. Creating
  the `vX.Y.Z` tag releases the build to **F-Droid** automatically.

```text
  bump version in          release.yml                    promote.yml
  AndroidManifest.xml ─►   (Stage 1, Step 1.1)       ─►   (Stage 2, Step 1.3)
  + merge to main          tag vX.Y.Z-rc.N                tag vX.Y.Z
                           GitHub pre-release             final GitHub Release
                           + Play internal                + Play production + Amazon
                                  │                               │
                                  └─► Internal testers verify     ├─► Google Play production
                                      (Step 1.2)                  ├─► Amazon Appstore
                                                                  └─► F-Droid scans vX.Y.Z tag
                                                                      (1–3 day lag)
```

### 1.1 Build and publish the pre-release

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
   - `releaseNotes`: the "what's new" text shown to Play internal testers.
4. The workflow:
   - Refuses to run if the final tag `vX.Y.Z` **or** the chosen
     `vX.Y.Z-rc.N` tag already exists.
   - Builds `:app:assembleAndroidRelease` and `:app:assembleAmazonRelease`.
   - Verifies the APKs are signed with the release key (fails on
     debug-signed APKs).
   - Creates a GitHub **pre-release** tagged `vX.Y.Z-rc.N` with both APKs
     attached.
   - Uploads the `android` APK to the Google Play **internal** track.
5. F-Droid does **not** build at this stage: the `vX.Y.Z-rc.N` tag is
   excluded by the `fdroiddata` `UpdateCheckMode` regex
   (`Tags ^v[0-9.]+$`), and the bare `vX.Y.Z` tag does not exist yet.
   Amazon and Play production are untouched.

### 1.2 Verify on the internal track

1. Open Google Play Console → **Testing → Internal testing**. Confirm
   a new release exists with the expected `versionCode`.
2. On a test device opted into the internal-test program, install the build
   from the Play Store and exercise the changed flows.
3. Watch the Play Console pre-launch report for crashes / policy issues
   before promoting.
4. If a problem is found, fix it, bump `versionCode`/`versionName` as
   needed, and run [Step 1.1](#11-build-and-publish-the-pre-release) again
   with the next `rc_number` (or new version). Only promote once a
   pre-release is green.

### 1.3 Promote the pre-release to a release

Once internal testing is green:

1. **Actions → Promote Pre-release to Release → Run workflow**.
2. Inputs:
   - `rc_tag`: the pre-release tag you verified, e.g. `v3.8.1-rc.1`.
   - `releaseNotes`: production "what's new" text for Google Play and Amazon.
     May match or differ from the internal notes.
3. The workflow:
   - Verifies the pre-release exists and the final `vX.Y.Z` release does not.
   - Downloads the exact APKs from the pre-release (no rebuild — guaranteeing
     production gets the same bytes internal testers verified).
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

## 2. Fastlane lanes (reference)

Defined in [`fastlane/Fastfile`](fastlane/Fastfile). Each workflow passes a
pre-built APK to the relevant lane via `apk_path` — the pre-release workflow
builds it fresh, the promote workflow downloads it from the pre-release.

- `deploy_playstore_test`
  - Uploads to a Google Play tester track (`alpha`, `beta`, or `internal`).
    The pre-release workflow always uses `internal`.
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

## 3. Troubleshooting

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
is signing with the wrong key. Re-verify the keystore behind the `SIGNING_*`
secrets: run `keytool -list -v -keystore ... -alias ...` against it and
compare its SHA-1 to **Play Console → Setup → App integrity →
App signing → Upload certificate**. They must match exactly.

**Release workflow fails decoding the keystore (`decoded keystore is empty`).**
`SIGNING_KEYSTORE_DATA` value is not valid base64. Recreate it and paste the
entire file contents into the secret. On macOS/BSD use
`base64 -i upload-keystore.jks -o keystore.b64`; on Linux/GNU use
`base64 -w0 upload-keystore.jks > keystore.b64`.

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

**Deploy fails: `track 'internal' is not a valid track`.**
The Internal testing track is not set up in Play Console. Create it under
**Testing → Internal testing** and add at least one tester.

**Amazon Appstore deploy fails: `version_code already submitted`.**
Same issue as Google Play — Amazon also enforces strictly increasing
versionCodes. Bump and re-release.

**F-Droid built a pre-release / RC tag.**
The `fdroiddata` `UpdateCheckMode` is no longer filtering RC tags. Confirm
the recipe still uses `UpdateCheckMode: Tags ^v[0-9.]+$` so RC tags are
ignored.

**F-Droid hasn't picked up the new release.**
F-Droid scans on its own schedule and rebuilds from source. Expect a
1–3 day lag after the final `vX.Y.Z` GitHub tag is created. Track build
status at the F-Droid build logs / your package's metadata repo.
