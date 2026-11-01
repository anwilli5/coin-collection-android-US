---
name: api-matrix-test
description: >
  Test the app on headless emulators across Android API levels, from minSdk to
  targetSdk. Covers the upgrade from the previous release (database compared
  across levels), every main screen checked for content under the system bars,
  importing a JSON backup, the crash log, and the instrumented test suite. Use
  when asked to test on older or newer Android versions, run the API matrix,
  test a release or pre-release across API levels, or check an upgrade on old
  devices. NOT for a quick screenshot (use capture-debug-screenshot) or a
  single-emulator UI check (use ui-regression-test).
---

# API Matrix Test

Unit tests run on Robolectric, and the instrumented suite runs on one API 36
emulator in CI. Changes that behave differently across Android releases can
pass both and still break on users' devices: system-bar insets, back handling,
SQLite versions, and the system file picker. This skill runs the app on an
emulator at each API level where one of those behaviors changes.

`scripts/api-matrix/api_matrix.py` does the work. Each level boots a headless
emulator (AVD `api-matrix-<level>`), runs its checks, and shuts the emulator
down. The script works the same on macOS, Linux and Windows.

## Levels

| API | Android | Why this level |
| --- | --- | --- |
| `minSdk` (21) | 5.0 | Oldest SQLite (3.8) for migrations; androidx reports no navigation bar inset; oldest file picker |
| 23 | 6.0 | First release with root window insets, so inset handling changes here |
| 26 | 8.0 | Older file picker, with no MIME type for `.json` files |
| 29 | 10 | Gesture navigation arrives |
| 33 | 13 | System back callbacks: Back goes to the most recently registered handler |
| 35 | 15 | Edge-to-edge enforced; the `amazon` flavor's `targetSdk` |
| `targetSdk` | newest | Edge-to-edge with no opt-out; predictive back |

The script reads `minSdk` and `targetSdk` from `app/build.gradle`. The levels
in between are in `MIDDLE_LEVELS` in the script. Use `--levels` to run a
subset.

## Prerequisites

- The Android SDK with `emulator`, `platform-tools` and
  `cmdline-tools/latest`, found through `ANDROID_HOME`, `ANDROID_SDK_ROOT`, or
  `sdk.dir` in `local.properties`.
- Python 3.8 or later. Pillow is optional; with it, each level also gets a
  `screens.png` that tiles its screenshots.
- On the first run, pass `--create-avds`. It installs each level's
  `google_apis` system image (about 1.5 GB each) and creates a Pixel 6 AVD
  with an SD card. `google_apis` images allow `adb root`, which pulling the
  database needs, and the API 21–23 file pickers need the SD card. The ABI
  follows the host: `arm64-v8a` on Apple silicon and ARM Linux, `x86_64`
  elsewhere.

## Upgrade check

Get the previous release's APK and the new one. Both must be signed with the
release key, so the new one installs as an update and the database upgrade
runs. A pre-release's APK works:

```bash
mkdir -p build/api-matrix/apks
gh release download v<previous> -p 'app-android-release-*.apk' -D build/api-matrix/apks
gh release download v<new>-rc.<n> -p 'app-android-release-*.apk' -D build/api-matrix/apks
python3 scripts/api-matrix/api_matrix.py upgrade --create-avds \
    --old build/api-matrix/apks/app-android-release-v<previous>.apk \
    --new build/api-matrix/apks/app-android-release-v<new>.apk
```

At each level it:

1. Installs the previous release and imports `scripts/api-matrix/seed/seed.json`
   through the system file picker. Pickers that grey JSON out fall back to
   `seed.csv`.
2. Installs the new release over it, lists the collections, and pulls the
   database.
3. Screenshots each main screen and checks that no app element is under the
   status bar, navigation bar or cutout. The screens are the main list (top
   and bottom), a collection's grid (top and bottom), the coin-actions dialog,
   the advanced view with the keyboard up, the advanced view with an unsaved
   edit, the leave dialog, Reorder Collections and New Collection, plus the
   main list and a collection in landscape.
4. Imports the JSON backup again with the new release.
5. Checks the crash log.

Finally it compares every level's database with the newest level's.

## Instrumented tests

```bash
python3 scripts/api-matrix/api_matrix.py instrumented --create-avds
```

This runs `connectedAndroidDebugAndroidTest` (without `ScreenshotsUITest`) on
each level, one emulator at a time. A test that runs longer than
`--test-timeout` seconds (default 180) counts as failed, so a hang on one
level doesn't stall the run. Tests that need a newer release skip themselves
on older ones; for example, the gesture Back test only runs on API 33+.

## In CI

The **API Matrix** workflow (`.github/workflows/api-matrix.yml`) runs both
subcommands on GitHub's Linux runners, one job per level. It only runs when
started by hand, from the Actions tab or with:

```bash
gh workflow run api-matrix.yml --ref v<new>-rc.<n> -f new_tag=v<new>-rc.<n>
```

The upgrade jobs use the APKs attached to the `new_tag` and `old_tag`
releases. `old_tag` defaults to the latest full release. The instrumented jobs
test the code at `--ref`. `-f suites=upgrade` or `-f suites=instrumented` runs
only one kind. Each job adds its report to the run summary and uploads its
results directory as an artifact.

## Reading the results

Results go to `build/api-matrix/<command>/`, or to `--out`. `report.md` is a
summary table, with each level's problems listed below it. The exit code is 1
if any level failed.

- **Look at the screenshots too.** The overlap check only sees elements that
  uiautomator reports, so it can't catch things like clipped text or a
  half-hidden row. Review `api<level>/screens.png`, or the files in
  `api<level>/screens/`.
- **Regression or older bug?** Rerun the failing level with `--new` set to the
  previous release's APK. A problem that shows up then too already shipped.
  File it, but it doesn't block this release.
- **Database differences** are listed per collection against the newest
  level's database. A difference at one level usually means a migration that
  depends on the SQLite version.
- **`ERROR` rows** mean the level couldn't run: the AVD couldn't boot, or a
  step couldn't find its UI. Each level's directory has `emulator.log` and
  the screens it captured. Rerun just that level with `--levels`.

## Seed backup

`scripts/api-matrix/seed/` holds a JSON backup and the same data as CSV,
exported from 3.8.4 (database version 24). Its collections exercise 3.9.0's
migrations: Washington Quarters with silver and clad years and with clad years
only, American Women Quarters, Lincoln Cents with mint marks, Roosevelt Dimes
and Kennedy Half Dollars.

When a release's migrations touch other collection types, add them to the
seed:

1. Import the seed into the previous release on an emulator.
2. Create the collections the new migrations change, and mark some coins
   collected.
3. Export JSON and CSV backups, and replace `seed.json` and `seed.csv` with
   them.

## Cleanup

The AVDs and system images take about 1.5 GB per level:

```bash
"$ANDROID_HOME/cmdline-tools/latest/bin/avdmanager" delete avd -n api-matrix-<level>
"$ANDROID_HOME/cmdline-tools/latest/bin/sdkmanager" --uninstall "system-images;android-<level>;google_apis;<abi>"
```
