---
description: 'Add new yearly coins to all active collection types and bump the database version'
agent: 'agent'
tools:
  - 'run_in_terminal'
---

# Add Yearly Coins

Add a new year's coins to all active (ongoing) collection types. This is an
annual maintenance task performed when the U.S. Mint issues new coins.

## Scope

This workflow covers adding coins for a new year to **existing** collection
types that have ongoing series. It does NOT cover adding entirely new
collection types — use the `add-coin-collection` skill for that.

## Prerequisites

- Know which collections need new coins for the target year
- Have any new coin images (drawables) ready if specific designs are needed
- Understand whether the series uses year-based or named-identifier coins

## Workflow

### Step 1: Identify affected collections

Active U.S. Mint series that get yearly additions typically include:
- Lincoln Cents, Jefferson Nickels (year-based)
- American Women Quarters (named-identifier, 5 per year)
- American Innovation Dollars (named-identifier, 4-5 per year)
- Native American / Sacagawea Dollars (named-identifier, 1 per year)
- Kennedy Half Dollars (year-based)
- American Eagle Silver Dollars (year-based)

Confirm with the user which series have new coins.

### Step 2: Add drawable resources

For named-identifier collections with unique coin designs, add images to
`app/src/main/res/drawable/`. Follow naming conventions from existing images
in the same series.

### Step 3: Bump DATABASE_VERSION

In `app/src/main/java/com/spencerpages/MainApplication.java`:
1. Increment `DATABASE_VERSION` (check current value first)
2. Add version history comment

### Step 4: Update each collection class

For each affected collection in `app/src/main/java/com/spencerpages/collections/`:

**For named-identifier collections** (e.g., AmericanWomenQuarters):
1. Add new entries to `COIN_IDENTIFIERS[]` array with name and drawable
2. Update `COIN_MAP` if used (static initializer handles this automatically
   when COIN_IDENTIFIERS is updated)
3. Add `onCollectionDatabaseUpgrade()` block:
   ```java
   if (oldVersion <= PREVIOUS_DB_VERSION) {
       ArrayList<String> newCoinIdentifiers = new ArrayList<>();
       newCoinIdentifiers.add("New Coin Name");
       total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers);
   }
   ```

**For year-based collections** (e.g., LincolnCents):
1. Update `OPT_STOP_YEAR` default if the new year extends past it
2. Add `onCollectionDatabaseUpgrade()` block:
   ```java
   if (oldVersion <= PREVIOUS_DB_VERSION) {
       total += DatabaseHelper.addFromYear(db, collectionListInfo, NEW_YEAR);
   }
   ```

### Step 5: Update tests

1. **CollectionCreationTests**: Update expected coin counts for each modified
   collection's test method.
2. **CollectionUpgradeTests**: Verify upgrade test coverage includes the new
   database version.

### Step 6: Build and verify

```bash
./gradlew assembleDebug
./gradlew testAndroidDebugUnitTest
```

Fix any test failures (usually expected count mismatches) and re-run.

### Step 7: Summary

Report a table of changes made:

| Collection | Coins Added | DB Version Check |
|------------|-------------|------------------|
| ... | ... | `oldVersion <= N` |

## Tips

- Check the previous year's changes for examples of the exact pattern used
  in each collection's `onCollectionDatabaseUpgrade()`.
- Named-identifier collections must have `COIN_IDENTIFIERS[]` updated **and**
  the upgrade method updated — the former is for new collections, the latter
  is for existing user databases.
- Always use `oldVersion <= N` where N is the version **before** your bump.
