---
name: ui-regression-test
description: Run a UI sanity check, an export/import test, and a minified release-build (R8) smoke test against a connected Android emulator using the mobile-mcp MCP server and adb. Covers the flows Espresso cannot reach — the system file picker, and the obfuscated release APK that neither test suite builds. Requires a running emulator or connected device and the mobile-mcp server. NOT for general regression testing — the instrumented test suite covers everything else.
---

# UI Regression Test — Coin Collection App

## Mission

Run a navigation sanity check, an export/import test, and a release-build smoke test for the Coin Collection Android app using the mobile-mcp MCP server and adb commands against a connected emulator or device. These tests reach what the Android instrumented test suite (`androidTest/java/com/spencerpages/`) cannot: system file picker interaction that Espresso cannot automate, and the minified/obfuscated release APK, which no test suite builds. The instrumented suite covers all other regression tests.

## Scope and Preconditions

- An Android emulator is running or a physical device is connected (verify with `adb devices`)
- The workspace root is the `coin-collection-android-US` repository
- The mobile-mcp MCP server is available
- Tool names in this skill are the mobile-mcp server's bare names. Your
  client prefixes them (GitHub Copilot: `mcp_mobile-mcp_mobile_*`;
  Claude Code: `mcp__mobile-mcp__mobile_*`) and may require loading them
  through its tool-search mechanism before first use
- If any precondition is unmet, report the issue and stop

## Rules

Follow these rules throughout the entire test session.

### Coordinate Handling

- Always call `mobile_list_elements_on_screen` to get element coordinates before tapping
- Compute tap targets as center of the element: `x + width/2`, `y + height/2`
- Use `adb shell input tap <centerX> <centerY>` with native coordinates from `list_elements_on_screen`
- Never estimate coordinates from screenshot images — they are scaled down and will be inaccurate

### Interaction Patterns

- Alert dialog buttons appear as `android.widget.Button` — check exact text via `list_elements_on_screen`:
  - Tutorial dialogs: "OKAY!" button
  - Delete/Warning dialogs: "NO" / "YES" buttons
  - Some success messages have no dismiss button — use `adb shell input keyevent KEYCODE_BACK` to dismiss
- For text input: tap the `EditText` to focus it, then use `adb shell input text "value"` (use `%s` for spaces)
- To navigate back, use `adb shell input keyevent KEYCODE_BACK`

### First-Time Tutorial Dialogs

The app shows one-time tutorial dialogs on first use. Whenever an unexpected dialog appears with an "OKAY!" button, dismiss it and continue.

### Verification

- Take a screenshot (`mobile_take_screenshot`) after each major action for visual verification
- Use `list_elements_on_screen` to confirm expected elements are present
- Report each test as **PASS** or **FAIL** with a brief reason
- On app crash, report CRITICAL FAILURE and attempt to relaunch before continuing

### Scrolling

- If an element is not visible in `list_elements_on_screen`, scroll down with `adb shell input swipe 540 1500 540 500 300` and re-check
- To scroll up: `adb shell input swipe 540 500 540 1500 300`

---

## Setup

Build, install, and launch the app:

```bash
# Build and install the debug APK
./gradlew installAndroidDebug

# Clear any existing app data for a clean test
adb shell pm clear com.spencerpages.debug

# Launch the app
adb shell am start -n com.spencerpages.debug/com.coincollection.MainActivity
```

Wait 3 seconds for the app to fully launch, then take a screenshot to confirm.

A first-time tutorial dialog ("Thanks for downloading...") will appear — dismiss it by finding and tapping the "OKAY!" button.

---

## Workflow

### Navigation Sanity Check

**Goal:** Verify the app launches, key screens render without crashes, and basic navigation works. Detailed interaction testing is handled by the Android instrumented test suite.

1. Call `list_elements_on_screen` and take a screenshot of the main activity
2. Verify these navigation items are visible: "New Collection", "Delete Collection", "Import Collection", "Export Collection", "Reorder Collections", "App Info"
3. Tap "New Collection" — a tutorial dialog will appear on first use — dismiss it by tapping "OKAY!" — verify the `CoinPageCreator` screen loads (look for screen title "Collection Page Creator")
4. Take a screenshot
5. Navigate back: `adb shell input keyevent KEYCODE_BACK`
6. Create a test collection for remaining tests:
   - Tap "New Collection" (no tutorial dialog on subsequent visits)
   - Tap the collection name `EditText` and enter: `adb shell input text "Sanity%sTest"`
   - The coin type spinner defaults to "Select Collection Type" — tap it and select "Pennies" from the dropdown
   - Call `list_elements_on_screen` to locate the "CREATE NEW COLLECTION!" button and tap it
   - Wait 3 seconds — the app opens the new collection's `CollectionPage`, and a tutorial dialog appears — dismiss it with "OKAY!"
   - Navigate back to main activity with `KEYCODE_BACK`
   - A long-press tutorial dialog ("Press and hold on a collection...") appears — dismiss it with "OKAY!"
   - Verify "Sanity Test" appears on the main activity with progress "0/N", where N is the current Pennies total (the expected count from `CollectionCreationTests`)
7. Tap the "Sanity Test" collection — verify the `CollectionPage` opens (look for `standard_collection_page` GridView; no tutorial dialog this time since it was dismissed in step 6)
8. Take a screenshot
9. Navigate back to main activity
10. Tap "App Info" — verify a dialog appears containing "Coin Collection" and a version string matching the `versionName` in `version.properties` — press BACK to dismiss
11. Tap "Reorder Collections" — a tutorial dialog appears on first use — dismiss it with "OKAY!" — verify the reorder screen loads (look for `reorder_collections_recycler_view` with the collection name and up/down arrows) — press BACK

**Pass criteria:** App launches; CoinPageCreator, CollectionPage, App Info, and Reorder screens all render without crashes.

---

### Export and Import (JSON)

**Goal:** Verify export to JSON and re-import restores data. This test uses the system file picker which cannot be automated with Espresso.

1. Ensure at least one collection exists (created in the Navigation Sanity Check above). Take a screenshot of the main activity to record current collections and their progress counts.
2. Tap "Export Collection"
3. Verify the format picker dialog appears with title "Select an export file format:" and options:
   - "JSON file"
   - "CSV file (table format)"
4. Tap "JSON file"
5. A system file picker opens with a default filename like `coin-collection-MMDDYY.json` and a "SAVE" button at the bottom of the screen
6. Call `list_elements_on_screen` to find the "SAVE" button (`android:id/button1`) in the file picker and tap it. **Note:** if a file with the same name already exists, an overwrite confirmation dialog appears — tap "OK" to proceed.
7. Wait 3 seconds for the export to complete
8. A success dialog appears: "Successfully exported collection to '...'" — this dialog has NO dismiss button. Press BACK to dismiss it.
9. Take a screenshot
10. Delete all collections using "Delete Collection" nav item, one at a time:
    - Tap "Delete Collection" — a picker dialog titled "Select a collection to delete" appears listing all collections
    - Tap the collection name to select it
    - A "Warning!" confirmation dialog appears: "Are you sure you want to delete collection named '...'?" — tap "YES"
    - Repeat for each collection
11. Verify the main activity shows only navigation items (no collection rows)
12. Tap "Import Collection"
13. The system file picker opens (in open mode — no SAVE button). Call `list_elements_on_screen` to find the exported JSON file and tap it.
14. Wait 3 seconds — the import completes silently and returns to the main activity (no success dialog)
15. Verify collections are restored with the same names and progress counts as before the export
16. Take a screenshot and compare with the screenshot from step 1

**Pass criteria:** Export creates a JSON file; import restores all collections with correct data.

---

### Release Build Smoke Test (R8)

**Goal:** Catch R8 regressions. Release builds are minified, resource-shrunk, and obfuscated (`minifyEnabled` + `shrinkResources` in `app/build.gradle`) to satisfy Google Play's DEX-optimization requirement. Both test suites run against the unminified `androidDebug` variant, so this is the only check that exercises the bytecode users actually receive.

Run this whenever `app/build.gradle`, `app/proguard-rules.txt`, `gradle.properties`, or the dependency list changes — and always before a release.

#### Build and install the release APK

A locally built release APK is unsigned unless `signing.properties` or the `SIGNING_*` environment variables are present, and `adb install` then fails with `INSTALL_PARSE_FAILED_NO_CERTIFICATES`. Sign it with the debug keystore first:

```bash
./gradlew assembleAndroidRelease

VN=$(grep '^versionName' version.properties | cut -d= -f2 | tr -d ' ')
APK="app/build/outputs/apk/android/release/app-android-release-v${VN}.apk"
SDK="${ANDROID_HOME:-$ANDROID_SDK_ROOT}"
BUILD_TOOLS=$(ls -d "$SDK"/build-tools/* | sort -V | tail -1)

cp "$APK" release-smoke.apk
"$BUILD_TOOLS/apksigner" sign \
  --ks "$HOME/.android/debug.keystore" \
  --ks-pass pass:android --key-pass pass:android \
  --ks-key-alias androiddebugkey \
  release-smoke.apk

adb uninstall com.spencerpages 2>/dev/null || true
adb install -r release-smoke.apk
adb logcat -c
adb shell am start -n com.spencerpages/com.coincollection.MainActivity
```

The release package is `com.spencerpages` — **not** the `com.spencerpages.debug` used everywhere else in this skill. Both can be installed at once, so confirm which one you are driving before interpreting a result.

#### Paths to exercise

R8 only breaks lookups that resolve a class, method, or resource by name, so drive the flows that resolve something indirectly. Follow the Rules section above for coordinates and tutorial dialogs (they reappear — this is a fresh package).

1. **Launch** — the main activity renders and the process stays alive.
2. **Collection creation** — "New Collection", open the collection-type spinner (`ImageSpinnerAdapter`, which scales bitmaps at runtime), pick a type, name it, create it. Confirm the new row shows the correct coin image and `0/N` count.
3. **Coin grid** — open the collection and confirm every coin image renders.
4. **Advanced view** — three-dot menu, then "Advanced View". This inflates `com.coincollection.SafeEditText` from `advanced_collection_slot.xml` — the app's only XML-referenced custom view, and the one class R8 must not rename. Type into a Notes field and tap SAVE.
5. **CSV export and import** — "Export Collection", "CSV file (table format)", save; then "Import Collection" and select that file. This is the app's only path through opencsv, its most reflection-adjacent dependency.
6. **JSON import** — import a JSON export as well, to cover the other serializer.

#### Check for R8-specific failures

A missing keep rule usually surfaces as a runtime linkage error, sometimes without a visible crash dialog. Check the log explicitly:

```bash
adb logcat -d | grep -E "ClassNotFoundException|NoSuchMethodError|NoSuchFieldError|InflateException|NotFoundException|FATAL EXCEPTION"
```

Any hit is a FAIL. Deobfuscate the stack trace before diagnosing it. For a locally built APK the mapping is in the build outputs; for a trace reported against a shipped build (a GitHub issue, an F-Droid install) download `mapping-android-release-v<versionName>.txt` from that version's GitHub release — R8 names are not reproducible, so rebuilding the tag will not produce a usable mapping.

```bash
adb logcat -d > crash.txt
"$SDK/cmdline-tools/latest/bin/retrace" \
  app/build/outputs/mapping/androidRelease/mapping.txt crash.txt
```

Fix by adding the narrowest possible `-keep` rule to `app/proguard-rules.txt`, then re-run this section. Prefer removing the reflection over adding a keep rule — `app/proguard-rules.txt` documents why the app stays reflection-free.

**Pass criteria:** All six paths behave as they do on the debug build, and the log contains none of the errors above.

#### Teardown for this section

```bash
adb uninstall com.spencerpages
rm -f release-smoke.apk crash.txt
```

---

## Teardown

After all tests complete:

1. Delete all test collections via "Delete Collection" nav item, or clear app data:

   ```bash
   adb shell pm clear com.spencerpages.debug
   ```

> **Warning:** `pm clear` erases all app data on the device. Only run this on a test device or emulator.

---

## Output

Provide a summary table:

| Test | Result | Notes |
| --- | --- | --- |
| Navigation Sanity Check | | |
| Export and Import (JSON) | | |
| Release Build Smoke Test (R8) | | |

Report total PASS / FAIL counts and any issues found.

Remind the user to run the full Android instrumented test suite for complete coverage:

```bash
./gradlew connectedAndroidDebugAndroidTest
```
