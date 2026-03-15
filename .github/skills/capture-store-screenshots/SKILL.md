---
name: capture-store-screenshots
description: >
  Regenerate the full set of 18 app store screenshots (6 screens x 3 device
  sizes) for the Coin Collection Android app using the automated
  Fastlane/Screengrab pipeline. Use when asked to regenerate store screenshots,
  update app store images, refresh store listings, rebuild all screenshots, run
  screengrab, or recapture store listing images. NOT for ad-hoc UI debugging
  screenshots — use the capture-debug-screenshot skill for that.
---

# Regenerate App Store Screenshots

Regenerate the complete set of store-published screenshots across all three
device sizes (phone, 7" tablet, 10" tablet) using the automated Fastlane
pipeline. This produces the 18 PNGs committed to `images/screenshots/` and
used in the Google Play and Amazon Appstore listings.

This task is handled by a single script. **Do not orchestrate the steps
manually** — run the script and report the result.

> **Looking for a quick screenshot of the running app?** Use the
> `capture-debug-screenshot` skill instead — it captures the current emulator
> screen via Mobile MCP without rebuilding or running the full pipeline.

## How to Run

From the repository root:

```bash
./fastlane/capture-screenshots.sh
```

### Options

| Flag | Description |
|------|-------------|
| `--force-avds` | Delete and recreate all AVDs before capturing (use after API level changes) |
| `--size small\|medium\|large` | Capture only one screen size instead of all three |
| `-h`, `--help` | Show usage information |

### Examples

```bash
# Capture all sizes (default)
./fastlane/capture-screenshots.sh

# Force-recreate AVDs first
./fastlane/capture-screenshots.sh --force-avds

# Capture only small (phone) screenshots
./fastlane/capture-screenshots.sh --size small
```

## What the Script Does

1. **Verifies prerequisites** — checks for `emulator`, `adb`, `ruby`, and
   `bundle exec fastlane` on PATH.
2. **Creates AVDs** — runs `avd-screenshot-create.sh` (skips existing AVDs
   unless `--force-avds` is passed).
3. **For each screen size** (small → medium → large):
   - Kills any running emulators
   - Boots the AVD and waits for full boot
   - Runs `bundle exec fastlane build_and_screengrab` (builds APKs, enables
     demo mode, runs Screengrab tests, disables demo mode)
   - Moves the 6 screenshots to `images/screenshots/{size}/`
   - Shuts down the emulator
4. **Verifies output** — confirms all 18 screenshots (6 × 3 sizes) are present.

The script exits 0 on success and non-zero on failure.

## Output

| Directory | Screen Size | Device Profile |
|-----------|-------------|----------------|
| `images/screenshots/small/` | Phone (1080×2400) | Pixel 6 |
| `images/screenshots/medium/` | 7-inch tablet | Nexus 7 (2013) |
| `images/screenshots/large/` | 10-inch tablet | Pixel C |

Each directory contains six PNGs:
`main_screen.png`, `presidential_dollars_screen.png`,
`lincoln_cents_screen.png`, `morgan_dollars_screen.png`,
`collection_creation_screen.png`, `coin_actions_screen.png`.

## Prerequisites

| Requirement | Check Command |
|-------------|---------------|
| Android SDK with `emulator`, `sdkmanager`, `avdmanager` | `emulator -version` |
| `adb` on PATH | `adb version` |
| Ruby (3.x recommended) | `ruby --version` |
| Fastlane + Bundler | `bundle exec fastlane --version` |
| SDK licenses accepted | `sdkmanager --licenses` (if AVD creation fails) |

The scripts auto-detect `ANDROID_HOME` and `JAVA_HOME` at common default
locations on macOS and Linux. If your SDK is installed elsewhere, set those
variables explicitly.

## Troubleshooting

| Issue | Cause | Solution |
|-------|-------|----------|
| `Cannot find Android SDK` | `ANDROID_HOME` not set | Set `ANDROID_HOME` to your SDK install directory (macOS: `~/Library/Android/sdk`, Linux: `~/Android/Sdk`) |
| System image not found | Not installed | `sdkmanager --install "system-images;android-<api>;google_apis;arm64-v8a"` |
| Emulator fails to boot | GPU acceleration issue | Try `emulator -avd <name> -no-snapshot-load -gpu swiftshader_indirect` |
| `avdmanager: command not found` | Command-line tools missing | Install via Android Studio SDK Manager → SDK Tools |
| `bundle exec fastlane` fails | Ruby too old | Install Ruby 3.x via `brew install ruby` |
| ANR dialogs on emulator | Slow software rendering | Handled automatically by `avd-screenshot-setup.sh` |
| Screengrab tests fail | App crash or UI change | Run `./gradlew connectedAndroidDebugAndroidTest` directly |
| Screenshots not in output dir | Fastfile copy step failed | Check `fastlane/metadata/android/en-US/images/phoneScreenshots/` |

## Related Files

- Script: [`fastlane/capture-screenshots.sh`](../../../fastlane/capture-screenshots.sh)
- AVD creation: [`fastlane/avd-screenshot-create.sh`](../../../fastlane/avd-screenshot-create.sh)
- AVD boot: [`fastlane/avd-screenshot-boot.sh`](../../../fastlane/avd-screenshot-boot.sh)
- AVD kill: [`fastlane/avd-screenshot-kill.sh`](../../../fastlane/avd-screenshot-kill.sh)
- Demo mode: [`fastlane/avd-screenshot-setup.sh`](../../../fastlane/avd-screenshot-setup.sh)
- Fastlane lanes: [`fastlane/Fastfile`](../../../fastlane/Fastfile)
- Screengrab config: [`fastlane/Screengrabfile`](../../../fastlane/Screengrabfile)
- Screenshot tests: [`app/src/androidTest/java/com/spencerpages/ScreenshotsUITest.java`](../../../app/src/androidTest/java/com/spencerpages/ScreenshotsUITest.java)
