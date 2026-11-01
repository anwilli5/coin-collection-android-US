---
name: release-checklist
description: >
  Run the pre-release verification checklist before triggering the GitHub
  Actions release workflow. Verifies unit tests, lint, the minified R8 release
  build, version bump in version.properties, DATABASE_VERSION, store metadata,
  screenshots, git state, and the upgrade and UI on emulators from minSdk to
  targetSdk, then reports a summary table. Use when asked to run the release
  checklist, verify release readiness, or check that everything is ready
  before a release.
disable-model-invocation: true
---

# Release Checklist

Verify all release prerequisites before triggering the GitHub Actions release
workflow. Run each check and report results.

## Checks

### 1. Unit tests pass

```bash
./gradlew testAndroidDebugUnitTest --rerun-tasks
```

All tests must pass. If any fail, report the failures and stop.

### 2. Lint is clean

```bash
./gradlew lintAndroidDebug
```

Must report no issues. Lint warnings fail the build (`warningsAsErrors` in `app/build.gradle`), so any warning is a failure here too.

### 3. Release build is R8-clean and runs

Release builds are minified, resource-shrunk, and obfuscated (`minifyEnabled` + `shrinkResources` in `app/build.gradle`) to satisfy Google Play's DEX-optimization requirement. Neither test suite covers this — unit and instrumented tests both run against the unminified `androidDebug` variant — so an R8 regression reaches users unless it is caught here.

Build both flavors:

```bash
./gradlew assembleAndroidRelease assembleAmazonRelease
```

Then confirm R8 ran and left every collection class distinct:

```bash
MAP=app/build/outputs/mapping/androidRelease/mapping.txt
test -f "$MAP" || echo "FAIL: no mapping.txt - R8 did not run"
echo "source:    $(ls app/src/main/java/com/spencerpages/collections/*.java | wc -l)"
echo "in map:    $(grep -c '^com\.spencerpages\.collections\.' "$MAP")"
echo "distinct:  $(grep '^com\.spencerpages\.collections\.' "$MAP" | awk '{print $3}' | sort -u | wc -l)"
```

All three counts must match. A shortfall in the last one means R8 merged two collection classes, which silently breaks `MainApplication.getIndexFromCollectionClass()`.

Finally run the **Release Build Smoke Test (R8)** section of the `ui-regression-test` skill against the signed release APK. A green unit-test run is not a substitute: R8 failures are runtime-only.

### 4. Version is bumped

Read `version.properties` at the repo root and check:

- `versionCode` — must be incremented from the last release
- `versionName` — must reflect the new version string

Compare with the latest git tag to confirm the version has changed:

```bash
git describe --tags --abbrev=0
```

### 5. DATABASE_VERSION is correct

If any database migrations were added since the last release:

- Verify `DATABASE_VERSION` in `MainApplication.java` was incremented
- Verify all `onCollectionDatabaseUpgrade()` methods use the correct
  version checks

If no database changes were made, confirm DATABASE_VERSION is unchanged.

### 6. Store metadata is current (if applicable)

Check if store text needs updating:

- `fastlane/metadata/android/en-US/full_description.txt`
- `fastlane/metadata/android/en-US/short_description.txt`

### 7. Screenshots are current (if UI changed)

If UI changes were made, screenshots may need regenerating:

- `images/screenshots/small/` (6 PNGs)
- `images/screenshots/medium/` (6 PNGs)
- `images/screenshots/large/` (6 PNGs)

Use the `capture-store-screenshots` skill if regeneration is needed.

### 8. Working tree is clean

```bash
git status
```

No uncommitted changes should exist.

### 9. Branch is up to date

```bash
git fetch origin main
git log HEAD..origin/main --oneline
```

No unmerged upstream changes should exist.

### 10. Works on old and new Android versions

Unit and instrumented tests run on one recent API level, so a change that
behaves differently across Android releases (system-bar insets, back handling,
SQLite versions, the system file picker) can pass them and still break on a
user's device. Run this check last, because it takes the longest. Run it on an
emulator at each level below; each one is a point where one of those
behaviors changes:

| API | Android | Why this level |
| --- | --- | --- |
| `minSdk` (21) | 5.0 | Oldest SQLite (3.8) for migrations; androidx reports no navigation bar inset; oldest file picker |
| 23 | 6.0 | First release with root window insets, so inset handling changes here |
| 26 | 8.0 | Older file picker, with no MIME type for `.json` files |
| 29 | 10 | Gesture navigation arrives |
| 33 | 13 | System back callbacks: Back goes to the most recently registered handler, which may be the app instead of the keyboard |
| 35 | 15 | Edge-to-edge enforced; the `amazon` flavor's `targetSdk` |
| `targetSdk` | newest | Edge-to-edge with no opt-out; predictive back |

If `minSdk` or `targetSdk` in `app/build.gradle` has changed, move the ends of
the matrix to match. The levels in between behave like their neighbors, so
testing every supported level costs hours without catching more.

**Emulators** (one-time setup). Use `google_apis` images, since they allow
`adb root` for pulling the database, and give each AVD an SD card, which the
API 21–23 file pickers need:

```bash
"$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" "system-images;android-<api>;google_apis;<abi>"
"$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager" create avd -n cc-api<api> \
    -k "system-images;android-<api>;google_apis;<abi>" -d pixel_6 -c 512M
```

`<abi>` is `arm64-v8a` on Apple silicon and `x86_64` elsewhere. Emulators can
run headless (`emulator -avd cc-api<api> -no-window -wipe-data`), since
screenshots work without a window.

**At each level:**

1. Install the previous release's APK
   (`gh release download v<previous> -p 'app-android-release-*.apk'`) and give
   it data: import a backup with collections that this release's database
   upgrades change.
2. Install the new release APK over it with `adb install -r`: the
   pre-release's APK, or the APK from check 3 if it was signed with the
   release key. Either way it must install as an update, not a fresh install,
   so the database upgrade runs.
3. Check that:
   - The app launches, and every collection's name and progress match the
     newest level's result. If the database changed, pull it
     (`adb root`, then
     `adb pull /data/data/com.spencerpages/databases/CoinCollection`) and
     compare the coins.
   - Nothing is drawn under the status or navigation bar, in portrait and
     landscape. Take screenshots (the `capture-debug-screenshot` skill) of:
     - the main list scrolled to App Info
     - a collection's grid scrolled to the last row
     - the coin-actions dialog
     - the advanced view with an unsaved edit, so the "Unsaved Changes" bar
       shows
     - Reorder Collections
     - New Collection
   - Back behaves correctly in the advanced view after typing the page's
     first note. The first Back closes only the keyboard; the second shows
     the "Leaving this page" dialog. From API 29, test with gesture
     navigation:
     - Turn it on with
       `adb shell cmd overlay enable com.android.internal.systemui.navbar.gestural`.
     - Swipe in from the left edge with `adb shell input swipe`, rather than
       sending `KEYCODE_BACK`. On API 33+, an injected `KEYCODE_BACK` doesn't
       behave like a user's Back.
   - A JSON backup and a CSV backup can both be selected in the import picker
     and imported. Older pickers hide device storage: use ⋮ → **Show SD card**
     (or **Show internal storage**).
   - `adb logcat -d -b crash` shows nothing from the app.

Report any level that differs from the newest one. If a difference also shows
up with the previous release's APK alone, it's an older bug; file it, but it
doesn't block the release.

## Output

Report a summary table:

| Check | Status | Notes |
| --- | --- | --- |
| Unit tests | PASS/FAIL | |
| Lint | PASS/FAIL | |
| Release build (R8) | PASS/FAIL | Built, mapping.txt present, smoke test green |
| Version bumped | YES/NO | vX.Y.Z (code: NN) |
| DATABASE_VERSION | OK/NEEDS BUMP | Current: N |
| Store metadata | OK/NEEDS UPDATE | |
| Screenshots | OK/NEEDS UPDATE | |
| Working tree clean | YES/NO | |
| Branch up to date | YES/NO | |
| Android versions | PASS/FAIL | API levels run; any differences |

If all checks pass, the release workflow can be triggered.
If any check fails, list the required actions before release.

## After the release

If the release shipped a new `DATABASE_VERSION`, add it to the upgrade-path
fixtures so future migrations are tested against what this release creates:
add a `Snapshot` for it (the release tag's commit) to
`GenerateUpgradeFixtures.SNAPSHOTS`, remove the `@Ignore` locally and run

```bash
./gradlew testAndroidDebugUnitTest --tests "com.spencerpages.GenerateUpgradeFixtures"
```

Commit only the new `app/src/test/data/upgrade-paths/v<N>.json.gz`; every
existing fixture must come out byte-identical. Then run `UpgradePathTests`.

Google Play's memory quality requirement (dynamic and bitmap memory thresholds, enforced February 2027, announced in the [August 2026 Android Developers post](https://android-developers.googleblog.com/2026/08/app-quality-memory-optimization-secure-onboarding.html)) can only be checked from real-device data. A few days after a release reaches production, open **Play Console → Android vitals** and compare the new version's memory metrics with those thresholds. If it exceeds them, open an issue.
