---
applyTo: "app/src/test/**,app/src/androidTest/**,shared-test/**"
---

# Test Writing Guidelines

## Unit tests (app/src/test/)

- Framework: JUnit 4.13.2 + Robolectric 4.16.1 + Mockito 5
- Base class: extend `BaseTestCase`
- Test runner: Robolectric (AndroidJUnit4 via Robolectric)
- Run with: `./gradlew testAndroidDebugUnitTest`
- Default variant: `androidDebug` (not amazon)

## Instrumented tests (app/src/androidTest/)

- Framework: Espresso 3.7.0 + UIAutomator 2.3.0
- Test runner: AndroidJUnitRunner
- Run with: `./gradlew connectedAndroidTest`
- Requires a running emulator or connected device
- Helper: `UITestHelper.java` provides common test utilities
- `ScreenshotsUITest.java` is for automated store screenshots — don't modify unless updating those

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
