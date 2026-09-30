---
name: run-tests
description: >
  Run the project's test suites and interpret results. Use when asked to run
  tests, verify changes, check if tests pass, run unit tests, run lint, run
  instrumented tests, or validate a change. Covers unit tests (Robolectric),
  instrumented tests (Espresso), and lint checks with the correct Gradle
  commands and output locations.
---

# Run Tests

Run unit tests, instrumented tests, or lint checks and interpret the results.

## Test Suites

### Unit tests (Robolectric — no emulator needed)

The primary test suite. Runs in-JVM using Robolectric to simulate Android
APIs. This is the suite to run for most code changes.

```bash
./gradlew testAndroidDebugUnitTest
```

**What it covers:**

- Collection creation and coin counts (`CollectionCreationTests`)
- Database upgrades from all previous versions (`CollectionUpgradeTests`)
- Fixture-based upgrade tests covering all parameter configurations
  (`CollectionUpgradeAllParamsTests`)
- Upgrades of the coin lists the first app version (database version 8)
  created, from frozen fixtures (`CollectionUpgradeV1FixtureTests`)
- Targeted upgrade tests for SemiQ coins with non-default parameters
  (`CollectionUpgradeSemiQParamTests`)
- Database access patterns (`DatabaseAccessTests`)
- Export/import (CSV and JSON) (`ExportImportTests`, `ExportImportJsonTests`)
- Activity launch and lifecycle (`LaunchCoinPageTests`, `MainActivityTests`)
- Application initialization (`MainApplicationTests`)
- Collection page behavior (`CollectionPageActivityTests`)
- Collection creator parameter round-trips (`CoinPageCreatorTests`)
- Coin image ID correctness (`CoinImageIdTests`)
- Golden snapshots of every collection type's coins, images and
  image id table (`CollectionGoldenTests`, fixtures in
  `app/src/test/data/golden/`)

Also present: `GenerateV23Fixtures` and `GenerateV1Fixtures` are `@Ignore`'d
manual fixture generators — they do not run with the suite. Their fixtures
(`app/src/test/data/v23-upgrades/`, `app/src/test/data/v1-upgrades/`) are
frozen snapshots of old databases: never regenerate them to make a failing
upgrade test pass. `GenerateV1Fixtures` rebuilds its files from the V1 source
in git history, so running it only confirms they are authentic.

A `CollectionGoldenTests` failure means a collection's output changed.
If the change is intended, regenerate the fixtures and review the diff:

```bash
./gradlew testAndroidDebugUnitTest --tests "com.spencerpages.CollectionGoldenTests" -PupdateGoldens
```

Regenerating still fails if a type moved in `COLLECTION_TYPES` or an existing
image id entry changed, since both are append-only.

**Report location:** `app/build/reports/tests/testAndroidDebugUnitTest/index.html`

**Test results XML:** `app/build/test-results/testAndroidDebugUnitTest/`

### Lint

Static analysis for Android best practices, accessibility, performance, and
correctness issues.

```bash
./gradlew lintAndroidDebug
```

**Report location:** `app/build/reports/lint-results-androidDebug.html`

### Coverage

Debug unit-test runs record JaCoCo coverage of the app's classes. Build the
report and check it against the floors in `app/build.gradle`
(`coverageFloors`, line and branch):

```bash
./gradlew checkAndroidDebugUnitTestCoverage
```

This runs the unit tests first if they are not up to date. CI runs the same
check and uploads the report as the `unit-test-coverage` artifact. If it
fails, add tests for the uncovered code rather than lowering a floor; raise
the floors when coverage grows.

**Report location:** `app/build/reports/coverage/test/android/debug/index.html`

### Instrumented tests (requires running emulator)

On-device tests using Espresso and UIAutomator. Only needed when testing UI
behavior that can't be covered by Robolectric.

```bash
./gradlew connectedAndroidTest
```

**What it covers:**

- Collection page views (basic and advanced) (`CollectionPageBasicViewTests`, `CollectionPageAdvancedViewTests`)
- Edit collection parameters (`EditCollectionParametersTests`)
- Export flow (`ExportCollectionTests`)
- Main activity UI (`MainActivityTests`)
- Parcelable serialization (`ParcelableTests`)
- Rename and reorder collections (`RenameCollectionTests`, `ReorderCollectionsTests`)
- Tutorial dialog display (`TutorialDialogsTest`)
- Screenshot generation (`ScreenshotsUITest`)

**Prerequisites:** A running Android emulator or connected device. Verify with `adb devices`.

**Report location:** `app/build/reports/androidTests/connected/`

## When to Run Which Suite

| Change type | Run |
| --- | --- |
| Collection class (add/edit coins, parameters) | Unit tests |
| Database migration | Unit tests |
| Export/import logic | Unit tests |
| Activity or Fragment code | Unit tests + Instrumented (if UI) |
| Layout XML changes | Instrumented tests |
| Build configuration | Unit tests + Lint |
| Any change before committing | Unit tests + Lint |

## Procedure

### 1. Run the appropriate suite

For most changes, start with unit tests:

```bash
./gradlew testAndroidDebugUnitTest
```

If tests pass and you want a full check:

```bash
./gradlew testAndroidDebugUnitTest && ./gradlew lintAndroidDebug
```

### 2. Interpret results

- **BUILD SUCCESSFUL** — All tests passed.
- **BUILD FAILED** — Check the terminal output for the failing test name and
  assertion message. The HTML report has detailed stack traces.
- **Common failures:**
  - `CollectionCreationTests` count mismatch → Expected coin counts need
    updating after adding/removing coins.
  - `CollectionUpgradeTests` failure → Database migration logic doesn't
    produce expected results.
  - `ExportImportTests` failure → Serialization format changed; update
    expected output.
  - Compilation error → Missing import, resource, or method.

### 3. Fix and re-run

After fixing a failure, re-run only the specific test class for faster
feedback:

```bash
./gradlew testAndroidDebugUnitTest --tests "com.spencerpages.CollectionCreationTests"
```

Then run the full suite to confirm no regressions:

```bash
./gradlew testAndroidDebugUnitTest
```

## Test Conventions

- Unit tests extend `BaseTestCase`
- Use `SharedTest.COLLECTION_LIST_INFO_SCENARIOS` for parametrized test data
- Use `SharedTest.compareCollectionListInfos()` and `compareCoinSlots()` for
  deep-equality assertions
- Read fixtures under `app/src/test/data/` with `BaseTestCase.openTestData()` /
  `readTestData()` (they are on the test classpath), never by a path relative
  to the working directory
- Default test variant is `androidDebug` (not `amazonDebug`)
- Two product flavors exist: `android` and `amazon`

## Reference files

| File | Purpose |
| --- | --- |
| `app/src/test/java/com/spencerpages/` | Unit test classes |
| `app/src/androidTest/java/com/spencerpages/` | Instrumented test classes |
| `shared-test/src/main/java/com/spencerpages/SharedTest.java` | Shared test data and comparison helpers |
| `app/src/test/java/com/spencerpages/BaseTestCase.java` | Unit test base class |
| `app/src/androidTest/java/com/spencerpages/UITestHelper.java` | Instrumented test utilities |
