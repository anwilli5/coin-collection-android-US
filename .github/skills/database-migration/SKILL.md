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

The `DATABASE_VERSION` constant lives in `MainApplication.java`. It must be
incremented for any database change.

## Collection Taxonomy

Understanding which collections are affected and how is critical for annual
coin updates. Collections fall into these categories:

### Collections requiring annual upgrade blocks

Every collection that receives new coins must have an
`onCollectionDatabaseUpgrade()` block for each database version bump.
Bumping `OPTVAL_STILL_IN_PRODUCTION` or adding to `COIN_IDENTIFIERS`
only affects **newly created** collections — it does NOT add coins to
existing user collections. The upgrade block is what adds coins to
existing collections.

#### Year-based collections (use `addFromYear`)

| Collection | Notes |
| --- | --- |
| `LincolnCents` | Special 2009 bicentennial coins in `COIN_IDENTIFIERS` |
| `JeffersonNickels` | Special 2004-2005 Westward coins in `COIN_MAP` |
| `BasicDimes` | Simple — no special images by default |
| `BasicHalfDollars` | Simple — no special images by default |
| `AmericanEagleSilverDollars` | Includes burnished variants |
| `NativeAmericanDollars` | Named+year hybrid, each year has a unique image |
| `RooseveltDimes` | Complex mint mark options (silver/clad/proof/satin) |
| `KennedyHalfDollars` | Complex mint mark options (silver/clad/proof) |

#### Named-identifier collections (use `addFromArrayList`)

| Collection | Notes |
| --- | --- |
| `BasicInnovationDollars` | States as identifiers, yearly batches |
| `AmericanInnovationDollars` | Year arrays (`TWENTY_SIX`, etc.) + `COIN_IMG_IDS` |
| `CladQuarters` | Aggregate: states + parks + women + semiq sub-series |
| `SilverQuarters` | Silver proof aggregate with checkbox per sub-series |
| `SmallDollars` | SBA + Sacagawea + Presidential sub-series |

### Aggregate collections (affected indirectly)

These contain sub-series that span multiple denominations. When a
denomination gets a new year, the aggregate also changes:

| Collection | Sub-series affected by annual updates |
| --- | --- |
| `AllNickels` | Jefferson nickels sub-series |
| `SmallCents` | Lincoln cents sub-series |
| `SilverDimes` | Roosevelt dimes sub-series |
| `SilverHalfDollars` | Kennedy halves sub-series |
| `Cartwheels` | Silver Eagles sub-series |
| `CoinSets` | Includes sets for current year denominations |

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

### 2. Bump DATABASE_VERSION and OPTVAL_STILL_IN_PRODUCTION

In `app/src/main/java/com/spencerpages/MainApplication.java`:

1. Increment `DATABASE_VERSION`.
2. Add a comment documenting the new version and its app version:

   ```java
   * Version 24 - Used in Version X.Y.Z of the app
   ```

In `app/src/main/java/com/coincollection/CoinPageCreator.java`:

1. Bump `OPTVAL_STILL_IN_PRODUCTION` to the new year. This automatically
   extends all year-based collections that use it as their `OPT_STOP_YEAR`.

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

- **Every collection that receives new coins MUST have an upgrade block.**
  Bumping `OPTVAL_STILL_IN_PRODUCTION` or adding to identifier arrays only
  affects newly created collections. Existing user collections will NOT get
  new coins unless `onCollectionDatabaseUpgrade()` adds them. A missing
  upgrade block means existing users silently lose new coins.
- Use `oldVersion <= N` (not `==`), where N is the **previous**
  `DATABASE_VERSION` value (before your bump). This ensures upgrades from any
  older version work correctly.
- The method must return the **net total** of coins added minus coins removed.
  This count updates the collection's total in the metadata table.
- Use `DatabaseHelper.addFromArrayList()` for named-identifier coins — it
  respects the user's existing mint mark configuration.
- Use `DatabaseHelper.addFromYear()` for year-based coins — it also respects
  existing mint marks.
- **Do NOT use `addFromYear()` for collections that use checkbox-based coin
  types instead of mint marks** (e.g., `CoinSets` which creates "Mint Set",
  "Proof Set", etc. per year). For those, write custom upgrade logic that
  mirrors the previous year's coins.
- **When `populateCollectionLists()` sets `imageId` via `getImgId()`,
  `onCollectionDatabaseUpgrade()` must also pass the corresponding
  `imageId`.** Use `addFromYear(db, info, year, getImgId("tag"))` or
  `addFromArrayList(db, info, identifiers, imageIds)`. Otherwise, upgraded
  collections will show generic fallback images instead of the special
  designs.
- Add new identifiers to `COIN_IDENTIFIERS[]` (or update year ranges) in
  the same collection class so newly created collections also include them.
- Add corresponding drawable resources if the new coins have unique images.
- **If the value of any static import or constant used in an upgrade block
  changes in a later version, old blocks referencing it will silently pick
  up the new value and break.** This applies to `COLLECTION_TYPE` constants,
  column names, table names, display constants, flag constants, and any
  other symbol whose literal value is meaningful to migration SQL or logic.
  If a constant's value must change, update every old migration block that
  used it to use the old literal value instead of the constant.

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

- Guard with `&& !fromImport` for **structural changes** (ADD COLUMN,
  ALTER TABLE) — imported databases are created with the latest schema.
- **Do NOT use `&& !fromImport` for data-value migrations** (e.g., ORing
  new bits into existing flag columns, renaming stored values). Imported
  databases have the latest schema but carry old data values from the
  export that also need updating. Use just `if (oldVersion <= N)`.
- Use incremental version checks (`oldVersion <= N`).
- Never drop existing columns or tables — SQLite has limited ALTER support.
- **Use `Xxx.COLLECTION_TYPE` qualified references** for collection type
  names in migration blocks (e.g., `LincolnCents.COLLECTION_TYPE` not
  `"Pennies"`). If a `COLLECTION_TYPE` value is later renamed via a
  separate migration, every old migration block that used the constant must
  be updated to use the old literal string value instead — only the new
  migration performing the rename should reference the constant.
- **More generally, if the value of any static import or constant used in a
  migration block changes, old migrations that reference it will silently
  pick up the new value and break.** This applies to `COLLECTION_TYPE`
  constants, column name constants (`COL_*`), table name constants
  (`TBL_*`), display constants, flag constants, and any other symbol whose
  literal value is meaningful to migration SQL or logic. If a constant's
  value must change, update every old migration block that used it to
  reference the old literal value instead of the constant.

### 4. Update populateCollectionLists (if applicable)

If you added new coins, ensure `populateCollectionLists()` in the collection
class also produces them for newly created collections:

- **Named-identifier collections**: Add entries to `COIN_IDENTIFIERS[]`.
- **Year-based collections**: Bumping `OPTVAL_STILL_IN_PRODUCTION` is
  usually sufficient.
- **AmericanInnovationDollars**: Add a new year array (e.g.,
  `TWENTY_SIX`) with the state names, add entries to `COIN_IMG_IDS`, and
  add a new loop block in `populateCollectionLists()`.
- **CladQuarters / SilverQuarters**: Add entries to `COIN_IMG_IDS` (or
  the appropriate sub-series array), add a year array, and add a loop in
  `populateCollectionLists()`.

### 5. Add special per-year images (if applicable)

Some years have special coin designs (e.g., 2009 bicentennial pennies,
2004–2005 Westward nickels, 2026 SemiQ). To wire a special
image for a specific year:

#### Collections WITHOUT `getImageIds()` override (basic collections)

These collections don't support image selection for custom coins, so
identifier-matching in `getCoinSlotImage()` or `COIN_MAP` is the correct
approach:

- **LincolnCents / JeffersonNickels pattern**: Add the year to `COIN_MAP`
  in the static initializer block.
  ```java
  COIN_MAP.put("2026", R.drawable.semiq_2026_penny_obv_unc);
  ```
- **BasicDimes / BasicHalfDollars pattern**: These have no `COIN_MAP`.
  Add a simple conditional in `getCoinSlotImage()`:
  ```java
  if ("2026".equals(coinSlot.getIdentifier())) {
      return R.drawable.semiq_2026_dime_obv_unc;
  }
  ```

#### Collections WITH `getImageIds()` override

These collections support image selection for custom coins. Add the
special image to `COIN_IMG_IDS` (so it appears in the user's image
selection dropdown) and use `getImgId()` in `populateCollectionLists()`
to set the `imageId` on each `CoinSlot`. Do NOT use identifier-matching
in `getCoinSlotImage()` — that bypasses the image ID system and prevents
users from selecting the image for custom coins.

**CRITICAL: Always append new entries to the END of `COIN_IMG_IDS`.**
Never insert into the middle or reorder existing entries — the array
index is the `imageId` stored in user databases. Changing existing
indices corrupts image assignments for all existing collections.

- **RooseveltDimes / KennedyHalfDollars pattern**: Add entry to
  `COIN_IMG_IDS`, then use a per-year variable in
  `populateCollectionLists()`:
  ```java
  {"Emerging Liberty", R.drawable.semiq_2026_dime_obv_unc},  // in COIN_IMG_IDS
  // In populateCollectionLists, compute per-year override:
  int cladImgId = (i == 2026) ? getImgId("Emerging Liberty") : -1;
  // Pass cladImgId to CoinSlot constructors for that year
  ```
  **In `onCollectionDatabaseUpgrade()`**, pass the same imageId:
  ```java
  total += DatabaseHelper.addFromYear(db, collectionListInfo, 2026, getImgId("Emerging Liberty"));
  ```
- **Aggregate collections** (AllNickels, SilverDimes, SilverHalfDollars,
  SmallCents): Same approach — add the image to their `COIN_IMG_IDS`
  array and use `getImgId()` in the sub-series population loop:
  ```java
  String imgTag = (i == 2026) ? "1776-2026" : "Modern Jefferson";
  coinList.add(new CoinSlot(year, "P", coinIndex++, getImgId(imgTag)));
  ```
  **In `onCollectionDatabaseUpgrade()`**, pass the same imageId:
  ```java
  total += DatabaseHelper.addFromYear(db, collectionListInfo, 2026, getImgId("1776-2026"));
  ```
- **Named-identifier collections** (CladQuarters, AmericanInnovationDollars):
  Build a parallel `ArrayList<Integer>` of imageIds and pass it:
  ```java
  ArrayList<Integer> imageIds = new ArrayList<>();
  for (String identifier : newCoinIdentifiers) {
      imageIds.add(getImgId(identifier));
  }
  total += DatabaseHelper.addFromArrayList(db, collectionListInfo, newCoinIdentifiers, imageIds);
  ```

### 6. Add new checkbox flags (if adding a new sub-series)

When adding a new sub-series to an aggregate collection (e.g.,
SemiQ coins to multiple collections), you may need a new user
checkbox. This requires changes in four files:

1. **`CollectionListInfo.java`** — Add flag constant and accessor.
   The new flag **MUST use the next contiguous bit position** after the
   current highest flag. Then update `ALL_CHECKBOXES_MASK` to
   `(1L << (N + 1)) - 1` where N is the new highest bit. The same
   rules apply when adding mint mark flags — use the next bit after
   `MINT_FRANKLIN_PROOF` and update `ALL_MINT_MASK` accordingly.
   ```java
   public final static long NEW_FLAG = (1L << 50);
   // Update ALL_CHECKBOXES_MASK: (1L << 51) - 1
   public final static long ALL_CHECKBOXES_MASK = 0x7FFFFFFFFFFFFL;
   public boolean hasNewFlag() {
       return (getCheckboxFlagsAsLong() & NEW_FLAG) != 0;
   }
   ```
2. **`CoinPageCreator.java`** — Add encoding (in
   `getCheckboxFlagsFromParameters()`) and decoding (in
   `getParametersFromCollectionListInfo()`):
   ```java
   // Encoding:
   } else if (optionStrId.equals(R.string.include_new_flag)) {
       checkboxFlags |= CollectionListInfo.NEW_FLAG;
   // Decoding:
   } else if (optionStrId.equals(R.string.include_new_flag)) {
       parameters.put(optName, existingCollection.hasNewFlag());
   ```
3. **`strings.xml`** — Add the checkbox label string resource.
4. **Collection class** — Add `OPT_CHECKBOX_N` parameter in
   `getCreationParameters()` and read it in `populateCollectionLists()`.

**Important rules for flag constants:**

- Flag bit positions must be **unique and contiguous** — no gaps between
  the highest existing flag and the new one.
- `ALL_CHECKBOXES_MASK` must equal `(1L << (highestBit + 1)) - 1` — it
  must cover exactly the bits in use, with no extra padding.
- Same rules apply for `ALL_MINT_MASK` with mint mark flags.
- Update `testFixedCheckBoxIds()` (or `testFixedMintMarkIds()`) in
  `MainApplicationTests.java` to assert the new flag value AND the
  updated mask value. These tests are canaries that prevent future
  additions from forgetting to update the mask.

### 7. Update tests

#### CollectionCreationTests

Update expected coin counts in the test method for each modified
collection. **Also update counts for aggregate collections** that
include affected sub-series (e.g., `AllNickels` when Jefferson nickels
change, `SmallCents` when Lincoln cents change).

When a collection adds a new checkbox (like SemiQ quarters),
the test must be restructured to include the new parameter in the test
matrix and loop.

**Iterative approach**: Build first (`assembleDebug`), then run tests.
Fix the first failing row, re-run to find more failures. The test
framework only reports the first failure per test method, so multiple
iterations are expected.

#### SharedTest.COLLECTION_LIST_INFO_SCENARIOS

Update entries for collections whose max coin count or checkbox flags
changed. Fields to update:

- **Max coins**: Must match what `populateCollectionLists()` produces
  with the scenario's specific flag configuration.
- **Checkbox flags**: If a new checkbox defaults to TRUE, include its
  bit in the flags string.

Use `test_createFromParameters` to identify which scenarios need
updating — it round-trips each scenario and compares. Add a temporary
debug message like `assertTrue("Failed for: " + info.getName() + ...)`
to identify which specific scenario fails.

#### CollectionUpgradeTests

The existing upgrade tests auto-validate against freshly created
collections. They typically pass without changes when annual coins are
added, since both the upgrade path and the fresh creation should
produce the same result.

### 8. Build and verify

```bash
./gradlew assembleDebug
./gradlew testAndroidDebugUnitTest
./gradlew lintAndroidDebug
```

## Helper methods reference

| Method | Location | Purpose |
| --- | --- | --- |
| `DatabaseHelper.addFromYear(db, info, year)` | DatabaseHelper.java | Add coins for a year respecting user's mint marks |
| `DatabaseHelper.addFromYear(db, info, year, imageId)` | DatabaseHelper.java | Add coins for a year with a specific image ID |
| `DatabaseHelper.addFromArrayList(db, info, identifiers)` | DatabaseHelper.java | Add named coins respecting user's mint marks |
| `DatabaseHelper.addFromArrayList(db, info, identifiers, imageIds)` | DatabaseHelper.java | Add named coins with per-identifier image IDs |
| `DatabaseHelper.addCoin(db, tableName, identifier, mint, imageId, sortOrder)` | DatabaseHelper.java | Insert a single coin row, returns next sort order |
| `DatabaseHelper.updateEndYear(db, info, year)` | DatabaseHelper.java | Update collection's end year |
| `DatabaseHelper.runSqlUpdate(db, table, values, where, args)` | DatabaseHelper.java | Update existing rows |
| `DatabaseHelper.runSqlDelete(db, table, where, args)` | DatabaseHelper.java | Delete rows |
| `DatabaseHelper.getNextCoinSortOrder(db, table)` | DatabaseHelper.java | Get next sort order value |

## Reference files

| File | Purpose |
| --- | --- |
| `app/src/main/java/com/spencerpages/MainApplication.java` | `DATABASE_VERSION` constant and version history |
| `app/src/main/java/com/coincollection/CoinPageCreator.java` | `OPTVAL_STILL_IN_PRODUCTION` and checkbox flag encoding/decoding |
| `app/src/main/java/com/coincollection/CollectionListInfo.java` | Checkbox/mint flag constants, `ALL_CHECKBOXES_MASK`, `has*()` accessors |
| `app/src/main/java/com/coincollection/DatabaseHelper.java` | `upgradeDbStructure()` and helper methods |
| `app/src/main/java/com/spencerpages/collections/AmericanWomenQuarters.java` | Example: named-identifier upgrade pattern |
| `app/src/main/java/com/spencerpages/collections/LincolnCents.java` | Example: year-based upgrade + special image via `COIN_MAP` |
| `app/src/main/java/com/spencerpages/collections/BasicDimes.java` | Example: special image via `getCoinSlotImage()` conditional |
| `app/src/main/java/com/spencerpages/collections/AmericanInnovationDollars.java` | Example: named-identifier with year arrays, no upgrade block |
| `app/src/main/java/com/spencerpages/collections/CladQuarters.java` | Example: aggregate with sub-series checkboxes |
| `app/src/test/java/com/spencerpages/CollectionUpgradeTests.java` | Upgrade path tests |
| `app/src/test/java/com/spencerpages/CollectionCreationTests.java` | Creation count tests |
| `shared-test/src/main/java/com/spencerpages/SharedTest.java` | `COLLECTION_LIST_INFO_SCENARIOS` for round-trip tests |
| `app/src/main/res/values/strings.xml` | Checkbox label strings |

## Checklist

- [ ] `DATABASE_VERSION` incremented in `MainApplication.java` with version comment
- [ ] `OPTVAL_STILL_IN_PRODUCTION` bumped in `CoinPageCreator.java`
- [ ] `onCollectionDatabaseUpgrade()` updated in collections that have upgrade blocks
- [ ] `imageId` passed in `onCollectionDatabaseUpgrade()` for collections with `getImageIds()` override
- [ ] Version check uses `oldVersion <= PREVIOUS_VERSION` (not `==`)
- [ ] `populateCollectionLists()` updated for new coins in new collections
- [ ] New drawable resources added (if coins have unique images)
- [ ] `COIN_IDENTIFIERS[]` / `COIN_IMG_IDS[]` / year arrays updated
- [ ] Special per-year images wired via `COIN_MAP` or `getCoinSlotImage()`
- [ ] New checkbox flags added (if new sub-series): constant, mask, accessor, encoding, decoding, string resource
- [ ] `CollectionCreationTests` expected counts updated (including aggregates)
- [ ] `SharedTest.COLLECTION_LIST_INFO_SCENARIOS` updated (max coins, checkbox flags)
- [ ] `CollectionUpgradeTests` verified (usually auto-passes)
- [ ] `./gradlew assembleDebug` succeeds
- [ ] `./gradlew testAndroidDebugUnitTest` passes
- [ ] Migration blocks use `Xxx.COLLECTION_TYPE` qualified references for collection type names (not raw string literals)
- [ ] If a `COLLECTION_TYPE` value was renamed, old migration blocks that used the constant are updated to use the old literal string value
- [ ] No migration block depends on a constant whose value changed — if a constant's value was modified, old migration blocks that used it must be updated to use the old literal value
- [ ] `./gradlew lintAndroidDebug` passes
