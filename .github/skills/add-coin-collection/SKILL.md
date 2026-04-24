---
name: add-coin-collection
description: >
  Add a new coin collection type to the app. Use when asked to add a new
  collection, create a new coin type, add a coin series, new coin set, or
  implement a new collection class. Guides through creating the CollectionInfo
  subclass, registering it in MainApplication, adding resources, and writing
  tests. NOT for adding coins to an existing collection — use the
  database-migration skill for that.
---

# Add a New Coin Collection Type

Create a new coin collection type end-to-end: Java class, app registration,
drawable/string resources, and test coverage.

## Architecture Overview

Every collection type is a subclass of `CollectionInfo` (in
`com.coincollection`). The app discovers collections via the
`MainApplication.COLLECTION_TYPES[]` array. There are two main patterns:

- **Year-based** (e.g., `LincolnCents`): coins identified by year, with
  `OPT_EDIT_DATE_RANGE`, `OPT_START_YEAR`, `OPT_STOP_YEAR`.
- **Named-identifier** (e.g., `AmericanWomenQuarters`): coins identified by
  name from a `COIN_IDENTIFIERS[]` array, no year range editing.

Use the pattern that best fits the new series.

## Procedure

### 1. Design the collection

Before writing code, determine:

- **Coin type name** — display name (e.g., "Morgan Dollars"). Must be unique.
- **Pattern** — year-based or named-identifier.
- **Year range** (if year-based) — start and stop years.
- **Coin identifiers** (if named) — list of names and corresponding images.
- **Mint marks** — which mints apply (P, D, S, O, CC, W) and any
  year-specific restrictions.
- **Special images** — does each coin have a unique image, or one shared
  reverse?
- **Tier** — `BASIC_COLLECTIONS`, `ADVANCED_COLLECTIONS`, or
  `MORE_COLLECTIONS` in `MainApplication`.

### 2. Add drawable resources

Place coin images in `app/src/main/res/drawable/` (or density-specific
folders). Follow the existing naming convention:

- Reverse/default image: `<series>_reverse.png`
- Per-coin images: `<series>_<year_or_name>.png`

### 3. Add string resources

In `app/src/main/res/values/strings.xml`, add:

- Attribution string (e.g., `attr_<source>`) if coin images require it.
- Any mint mark label strings if not already present (usually `include_p`,
  `include_d`, etc. already exist).

### 4. Create the collection class

Create a new Java file in `app/src/main/java/com/spencerpages/collections/`.

Extend `CollectionInfo` and implement all 9 abstract methods:

| Method | Purpose |
| --- | --- |
| `getCoinType()` | Return unique collection name string |
| `getCoinImageIdentifier()` | Return `R.drawable.*` for list views (convention: use reverse image) |
| `getCoinSlotImage(CoinSlot, boolean)` | Return image for a specific coin slot |
| `getCreationParameters(HashMap)` | Populate user-configurable options and defaults |
| `populateCollectionLists(HashMap, ArrayList<CoinSlot>)` | Generate `CoinSlot` list from parameters |
| `onCollectionDatabaseUpgrade(SQLiteDatabase, CollectionListInfo, int, int)` | Handle DB upgrades (return 0 for new collections) |
| `getAttributionResId()` | Return attribution string resource ID |
| `getStartYear()` | Return first year (or 0 if not year-based) |
| `getStopYear()` | Return last year (or 0 if not year-based) |

Optionally override `getImageIds()` if coins have distinct images selectable
by the user. When `getImageIds()` is overridden, **all** special per-coin
images must be added to the `COIN_IMG_IDS` array (not handled via
identifier-matching in `getCoinSlotImage()`). This ensures images appear in
the user's image selection dropdown for custom coins. Use `getImgId(tag)` in
`populateCollectionLists()` to set the `imageId` on each `CoinSlot`.

**CRITICAL: Always append new entries to the END of `COIN_IMG_IDS`.** Never
insert into the middle or reorder existing entries — the array index is the
`imageId` persisted in user databases. Changing existing indices corrupts
image assignments for all existing collections.

#### Year-based template (follow `LincolnCents`)

```java
parameters.put(CoinPageCreator.OPT_EDIT_DATE_RANGE, Boolean.TRUE);
parameters.put(CoinPageCreator.OPT_START_YEAR, START_YEAR);
parameters.put(CoinPageCreator.OPT_STOP_YEAR, STOP_YEAR);
```

In `populateCollectionLists`, loop from `startYear` to `stopYear`, applying
mint mark logic per year. Maintain a `coinIndex` counter for sort ordering:

```java
coinList.add(new CoinSlot(Integer.toString(year), "P", coinIndex++));
```

#### Named-identifier template (follow `AmericanWomenQuarters`)

```java
private static final Object[][] COIN_IDENTIFIERS = {
    {"Name 1", R.drawable.image_1},
    {"Name 2", R.drawable.image_2},
};
```

In `populateCollectionLists`, loop over `COIN_IDENTIFIERS` and create
`CoinSlot` objects with string identifiers.

#### Key rules for `populateCollectionLists`

- Always maintain a `coinIndex` counter starting at 0, incrementing for each
  `CoinSlot` added. This sets the sort order.
- Mint mark filtering: check `OPT_SHOW_MINT_MARKS` first, then individual
  `OPT_SHOW_MINT_MARK_N` booleans.
- Use `getBooleanParameter()` and `getIntegerParameter()` helpers from the
  base class.

#### Key rules for `onCollectionDatabaseUpgrade`

- For a brand-new collection, return `0` (no existing users to upgrade).
- **Important:** When coins are later added to this collection (e.g., next
  year's annual update), an upgrade block MUST be added here. Bumping
  `OPTVAL_STILL_IN_PRODUCTION` or adding to identifier arrays only affects
  newly created collections — existing user collections need the upgrade
  block to receive new coins.
- Future updates will add coins here using incremental version checks:
  `if (oldVersion <= N) { ... }`.
- Use `DatabaseHelper.addFromYear()` for year-based additions.
- Use `DatabaseHelper.addFromArrayList()` for named-identifier additions.
- Always return the total number of coins added/removed.

### 5. Register in MainApplication

In `app/src/main/java/com/spencerpages/MainApplication.java`:

1. Add the import for the new class.
2. Add `new YourCollection()` to the **end** of `COLLECTION_TYPES[]` — **do
   not reorder** existing entries (the index is used internally).
3. Add `YourCollection.class` to the appropriate display tier:
   - `BASIC_COLLECTIONS` — common/popular U.S. coin series
   - `ADVANCED_COLLECTIONS` — specialized or denomination-grouped series
   - `MORE_COLLECTIONS` — historical, niche, or less common series

### 6. Add test coverage

#### Unit tests (`CollectionCreationTests.java`)

Add a test method following the existing pattern:

```java
@Test
public void test_YourCollectionCreationCounts() {
    ParcelableHashMap parameters = new ParcelableHashMap();
    YourCollection coinClass = new YourCollection();
    coinClass.getCreationParameters(parameters);

    // Show Mint Marks, P, D, ..., Expected Result
    Object[][] tests = {
        {true, true, false, ..., EXPECTED_COUNT},
        {false, false, false, ..., EXPECTED_COUNT_NO_MINTS},
    };

    for (Object[] test : tests) {
        // Set parameters and assert coin count
    }
}
```

#### Shared test scenarios (`SharedTest.java`)

Add a `CollectionListInfo` entry to `COLLECTION_LIST_INFO_SCENARIOS[]` in
`shared-test/src/main/java/com/spencerpages/SharedTest.java` covering the
new collection type with representative parameters.

### 7. Build and verify

Run `./gradlew assembleDebug` to confirm compilation, then run
`./gradlew testAndroidDebugUnitTest` to verify all tests pass.

## Reference files

| File | Purpose |
| --- | --- |
| `app/src/main/java/com/coincollection/CollectionInfo.java` | Abstract base class (9 methods) |
| `app/src/main/java/com/spencerpages/MainApplication.java` | Registration arrays + DATABASE_VERSION |
| `app/src/main/java/com/spencerpages/collections/LincolnCents.java` | Year-based template |
| `app/src/main/java/com/spencerpages/collections/AmericanWomenQuarters.java` | Named-identifier template |
| `app/src/main/java/com/spencerpages/collections/MorganDollars.java` | Fixed-range year-based template |
| `app/src/test/java/com/spencerpages/CollectionCreationTests.java` | Test pattern |
| `shared-test/src/main/java/com/spencerpages/SharedTest.java` | Test scenario arrays |

## Checklist

- [ ] Drawable resources added for coin images
- [ ] String resources added (attribution, labels)
- [ ] Collection class created extending `CollectionInfo` with all 9 methods
- [ ] Registered in `COLLECTION_TYPES[]` (appended to end, not reordered)
- [ ] Added to appropriate display tier array
- [ ] Import added in `MainApplication.java`
- [ ] If new checkbox/mint flags added: uses next contiguous bit, mask updated
      (see database-migration skill §6 for rules)
- [ ] Unit test added in `CollectionCreationTests`
- [ ] Scenario added in `SharedTest.COLLECTION_LIST_INFO_SCENARIOS`
- [ ] `./gradlew assembleDebug` succeeds
- [ ] `./gradlew testAndroidDebugUnitTest` passes
