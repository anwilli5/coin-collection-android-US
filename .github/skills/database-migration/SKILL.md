---
name: database-migration
description: >
  Perform a database migration to add new coins, fix data, or modify the
  schema. Use when asked to add coins to an existing collection, bump the
  database version, do a database upgrade, add yearly coins, fix coin data,
  or modify the database schema. Covers incrementing DATABASE_VERSION,
  updating collection upgrade methods, and schema-level changes. NOT for
  creating an entirely new collection type — use the add-coin-collection
  skill for that.
---

# Database Migration

Add coins to existing collections, fix coin data, or modify the database
schema. The app uses SQLite with a version-gated incremental upgrade system.

## Architecture Overview

The upgrade flow runs in `DatabaseHelper.upgradeDb()`:

1. **`upgradeDbStructure()`** — App-level schema changes (new columns, table
   renames, data fixes). Only modify this for structural changes.
2. **Per-collection upgrades** — The system iterates all user collections and
   calls each collection type's `onCollectionDatabaseUpgrade()` method. This
   is where coins are added or removed.

The `DATABASE_VERSION` constant lives in `MainApplication.java` (currently
**23**). It must be incremented for any database change.

## Procedure

### 1. Plan the migration

Identify what needs to change:

- **Adding new coins to existing collections** — Most common. Only requires
  changes in collection `onCollectionDatabaseUpgrade()` methods.
- **Schema changes** (new columns, table modifications) — Requires changes in
  `DatabaseHelper.upgradeDbStructure()`.
- **Data fixes** (renaming coins, fixing mint marks) — Can go in either
  `upgradeDbStructure()` or `onCollectionDatabaseUpgrade()` depending on
  scope.

### 2. Bump DATABASE_VERSION

In `app/src/main/java/com/spencerpages/MainApplication.java`:

1. Increment `DATABASE_VERSION` (e.g., 23 → 24).
2. Add a comment documenting the new version and its app version:
   ```java
   * Version 24 - Used in Version X.Y.Z of the app
   ```

### 3a. Add coins to existing collections

For each affected collection class in
`app/src/main/java/com/spencerpages/collections/`:

Edit the `onCollectionDatabaseUpgrade()` method. Add a new version-gated
block **after** existing blocks:

```java
if (oldVersion <= PREVIOUS_VERSION) {
    // Add new coins
    ArrayList<String> newCoinIdentifiers = new ArrayList<>();
    newCoinIdentifiers.add("Coin Name 1");
    newCoinIdentifiers.add("Coin Name 2");
    total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
}
```

For year-based collections, use `addFromYear()`:
```java
if (oldVersion <= PREVIOUS_VERSION) {
    total += DatabaseHelper.addFromYear(db, collectionListInfo, NEW_YEAR);
}
```

**Critical rules:**

- Use `oldVersion <= N` (not `==`), where N is the **previous**
  `DATABASE_VERSION` value (before your bump). This ensures upgrades from any
  older version work correctly.
- The method must return the **net total** of coins added minus coins removed.
  This count updates the collection's total in the metadata table.
- Use `DatabaseHelper.addFromArrayList()` for named-identifier coins — it
  respects the user's existing mint mark configuration.
- Use `DatabaseHelper.addFromYear()` for year-based coins — it also respects
  existing mint marks.
- Add new identifiers to `COIN_IDENTIFIERS[]` (or update year ranges) in
  the same collection class so newly created collections also include them.
- Add corresponding drawable resources if the new coins have unique images.

### 3b. Schema changes (if needed)

In `app/src/main/java/com/coincollection/DatabaseHelper.java`, add a new
block in `upgradeDbStructure()`:

```java
if (oldVersion <= PREVIOUS_VERSION && !fromImport) {
    // Schema change here
    db.execSQL("ALTER TABLE ...");
}
```

**Critical rules:**

- Guard with `&& !fromImport` for structural changes — imported databases
  are created with the latest schema already.
- Use incremental version checks (`oldVersion <= N`).
- Never drop existing columns or tables — SQLite has limited ALTER support.

### 4. Update populateCollectionLists (if applicable)

If you added new coins, ensure `populateCollectionLists()` in the collection
class also produces them for newly created collections:

- **Named-identifier collections**: Add entries to `COIN_IDENTIFIERS[]`.
- **Year-based collections**: Extend the default `OPT_STOP_YEAR` or add
  special-case year handling.

### 5. Update tests

#### CollectionCreationTests

Update expected coin counts in the test method for each modified collection.
When new coins are added, the expected total for the default parameter set
will increase.

#### CollectionUpgradeTests

Add test coverage for the new database version upgrade path. Verify that
upgrading from the previous version to the new version produces the correct
coin additions.

### 6. Build and verify

```bash
./gradlew assembleDebug
./gradlew testAndroidDebugUnitTest
```

## Helper methods reference

| Method | Location | Purpose |
|--------|----------|---------|
| `DatabaseHelper.addFromYear(db, info, year)` | DatabaseHelper.java | Add coins for a year respecting user's mint marks |
| `DatabaseHelper.addFromArrayList(db, info, identifiers)` | DatabaseHelper.java | Add named coins respecting user's mint marks |
| `DatabaseHelper.runSqlUpdate(db, table, values, where, args)` | DatabaseHelper.java | Update existing rows |
| `DatabaseHelper.runSqlDelete(db, table, where, args)` | DatabaseHelper.java | Delete rows |
| `DatabaseHelper.getNextCoinSortOrder(db, table)` | DatabaseHelper.java | Get next sort order value |

## Reference files

| File | Purpose |
|------|---------|
| `app/src/main/java/com/spencerpages/MainApplication.java` | `DATABASE_VERSION` constant and version history |
| `app/src/main/java/com/coincollection/DatabaseHelper.java` | `upgradeDbStructure()` and helper methods |
| `app/src/main/java/com/spencerpages/collections/AmericanWomenQuarters.java` | Example: named-identifier upgrade pattern |
| `app/src/main/java/com/spencerpages/collections/LincolnCents.java` | Example: year-based upgrade pattern |
| `app/src/test/java/com/spencerpages/CollectionUpgradeTests.java` | Upgrade path tests |
| `app/src/test/java/com/spencerpages/CollectionCreationTests.java` | Creation count tests |

## Checklist

- [ ] `DATABASE_VERSION` incremented in `MainApplication.java` with version comment
- [ ] `onCollectionDatabaseUpgrade()` updated in each affected collection
- [ ] Version check uses `oldVersion <= PREVIOUS_VERSION` (not `==`)
- [ ] `populateCollectionLists()` updated for new coins in new collections
- [ ] New drawable resources added (if coins have unique images)
- [ ] `COIN_IDENTIFIERS[]` updated (for named-identifier collections)
- [ ] `CollectionCreationTests` expected counts updated
- [ ] `CollectionUpgradeTests` covers new version
- [ ] `./gradlew assembleDebug` succeeds
- [ ] `./gradlew testAndroidDebugUnitTest` passes
