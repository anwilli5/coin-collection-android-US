---
description: 'Remind to update tests when collection source files are modified'
event: 'onFileSave'
filePattern: 'app/src/main/java/com/spencerpages/collections/*.java'
---

# Test Update Reminder

When a collection class is modified, check whether test updates are needed:

1. **CollectionCreationTests.java** — If coin counts changed (coins added or
   removed, year range changed, or identifiers modified), the expected counts
   in the corresponding test method need updating.

2. **SharedTest.java** — If creation parameters changed (new mint marks, new
   checkboxes, different defaults), the `COLLECTION_LIST_INFO_SCENARIOS`
   array may need a new or updated entry.

3. **CollectionUpgradeTests.java** — If `onCollectionDatabaseUpgrade()` was
   modified, the upgrade test should cover the new database version.

Remind the user about these files if the changes appear to affect coin
counts, parameters, or upgrade logic. Do not run tests automatically — just
flag which test files likely need attention.
