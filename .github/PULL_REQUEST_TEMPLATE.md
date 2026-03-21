<!-- markdownlint-disable MD041 -- PR templates start with H2, not H1 -->

## Description

<!-- Brief description of the changes in this PR -->

## Checklist

- [ ] Unit tests pass (`./gradlew testAndroidDebugUnitTest`)
- [ ] Lint is clean (`./gradlew lintAndroidDebug`)
- [ ] No unrelated changes included

### If adding/modifying coins or collections

- [ ] `CollectionCreationTests` expected counts updated
- [ ] `CollectionUpgradeTests` covers new database version (if applicable)
- [ ] `SharedTest.COLLECTION_LIST_INFO_SCENARIOS` updated (if applicable)
- [ ] `DATABASE_VERSION` bumped in `MainApplication.java` (if applicable)
- [ ] New drawable resources added for coin images (if applicable)

### If changing UI

- [ ] Tested on emulator / device
- [ ] Store screenshots updated (if layout changed)

### If releasing

- [ ] `versionCode` and `versionName` bumped in `AndroidManifest.xml`
- [ ] `DATABASE_VERSION` comment updated with app version
