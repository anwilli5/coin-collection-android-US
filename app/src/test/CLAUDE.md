# Test Writing Guidelines

These rules apply to test code (`app/src/test/`, `app/src/androidTest/`, and
`shared-test/`).

## Unit tests (app/src/test/)

- Framework: JUnit 4.13.2 + Robolectric 4.16.1 + Mockito 5
- Base class: extend `BaseTestCase`
- Test runner: Robolectric (AndroidJUnit4 via Robolectric)
- Run with: `./gradlew testAndroidDebugUnitTest`
- Default variant: `androidDebug` (not amazon)
- Fixtures live in `app/src/test/data/`, which Gradle puts on the test
  classpath: read them with `openTestData()` / `readTestData()` /
  `getTestDataDir()`, not a path relative to the working directory. Code that
  writes fixtures back (`-PupdateGoldens`, the `Generate*Fixtures` classes)
  uses `getTestDataSourceFile()`
- `random` is reseeded before every test, so a test gets the same data
  whichever tests ran before it

## Instrumented tests (app/src/androidTest/)

- Framework: Espresso 3.7.0 + UIAutomator 2.3.0
- Test runner: AndroidJUnitRunner
- Run with: `./gradlew connectedAndroidTest`
- Requires a running emulator or connected device
- Helper: `UITestHelper.java` provides common test utilities
- `ScreenshotsUITest.java` is for automated store screenshots — don't modify unless updating those
- Enter text with `replaceText`, never `typeText`. Typing goes through the
  emulator's soft keyboard, which autocorrects ("Lincoln Cents Test" became
  "Lincoln Center Test" on CI) and moves a dialog as it closes, so the next
  tap can miss its button
- After a tap that opens or swaps a window (dialog, activity), check the new
  window with `UITestHelper.waitForDisplayed`, not a bare
  `onView(...).check(...)`. CI emulators often take 9–12 s to give the new
  window focus, past Espresso's single 10 s wait

## Shared test library (shared-test/)

- `SharedTest.java` provides:
  - `COLLECTION_LIST_INFO_SCENARIOS[]` — pre-built collection metadata for parametrized tests
  - `COIN_SLOT_SCENARIOS[]` — sample coin slots with various states
  - `PARAMETER_SCENARIOS[]` — parameter HashMap examples
  - `compareCollectionListInfos()` — deep comparison of collection metadata
  - `compareCoinSlots()` — compare coin slots with optional fields
  - `compareCoinSlotLists()` — compare full coin lists
  - `compareParameters()` — compare parameter HashMaps
- Used by both unit and instrumented tests

## Test patterns

- Collection creation tests: create instance → getCreationParameters → set up test matrix of parameters → populateCollectionLists → assert coin count
- Database upgrade tests: create DB at old version → upgrade → verify coins added correctly
- Export/import tests: create collection → export → import → compare

## When to update tests

- Adding coins to a collection → update expected counts in `CollectionCreationTests`
- Database migration → add upgrade path test in `CollectionUpgradeTests`
- New collection type → add creation test + SharedTest scenario
- Changed collection parameters → update `SharedTest.COLLECTION_LIST_INFO_SCENARIOS`
- Any change to what a collection creates (coins, mints, images, image ids,
  parameters) → regenerate the golden files with
  `./gradlew testAndroidDebugUnitTest --tests "com.spencerpages.CollectionGoldenTests" -PupdateGoldens`
  and review the fixture diff. Never hand-edit them
