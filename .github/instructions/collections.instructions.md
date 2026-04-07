---
applyTo: "app/src/main/java/com/spencerpages/collections/**"
---

# Collection Type Editing Guidelines

When editing files in `com.spencerpages.collections`, follow these rules:

## Class structure

- All collections extend `CollectionInfo` (`com.coincollection.CollectionInfo`)
- 9 abstract methods must be implemented — see `CollectionInfo.java` for contracts
- Use a `public static final String COLLECTION_TYPE` constant for `getCoinType()`

## populateCollectionLists rules

- Maintain a `int coinIndex = 0` counter, incrementing for every `CoinSlot` added
- Each coin slot: `new CoinSlot(identifier, mintMark, coinIndex++)`
- Check `OPT_SHOW_MINT_MARKS` first, then individual `OPT_SHOW_MINT_MARK_N` booleans
- Use `getBooleanParameter()` and `getIntegerParameter()` helpers from the base class
- Year-based: loop from `startYear` to `stopYear` using `getIntegerParameter()`
- Named-identifier: loop over `COIN_IDENTIFIERS[]` array

## onCollectionDatabaseUpgrade rules

- **Every collection that receives new coins MUST have an upgrade block** — bumping `OPTVAL_STILL_IN_PRODUCTION` or adding to identifier arrays only affects newly created collections; existing user collections need the upgrade block to receive new coins
- Use incremental version checks: `if (oldVersion <= N)` where N is the version **before** the bump
- Never use `oldVersion == N` — upgrades must work from any older version
- Return the **net total** of coins added minus coins removed
- Use `DatabaseHelper.addFromArrayList()` for named-identifier coins
- Use `DatabaseHelper.addFromYear()` for year-based coins
- Both helpers respect the user's existing mint mark configuration
- **Use `Xxx.COLLECTION_TYPE` qualified references** for collection type names in migration code (e.g., `LincolnCents.COLLECTION_TYPE` not `"Pennies"`). If a `COLLECTION_TYPE` value is later renamed via a separate migration, every old migration block that used the constant must be updated to use the old literal string value instead — only the new migration performing the rename should reference the constant.
- **More generally, if the value of any static import or constant used in an upgrade block changes, old blocks referencing it will silently pick up the new value and break.** This applies to `COLLECTION_TYPE` constants, column name constants (`COL_*`), table name constants (`TBL_*`), display constants, flag constants, and any other symbol whose literal value is meaningful to migration SQL or logic. If a constant's value must change, update every old migration block that used it to use the old literal value instead of the constant.
- **When `populateCollectionLists()` uses `getImgId()` to set `imageId` on `CoinSlot` objects, the corresponding `onCollectionDatabaseUpgrade()` must also pass the same `imageId`** — use `addFromYear(db, info, year, getImgId("tag"))` or `addFromArrayList(db, info, identifiers, imageIds)`. Failing to do so causes upgraded collections to show fallback images instead of the correct designs.

## Mint mark options

- `OPT_SHOW_MINT_MARKS` — master toggle (Boolean)
- `OPT_SHOW_MINT_MARK_1` through `OPT_SHOW_MINT_MARK_5` — individual mint toggles (Boolean)
- `OPT_SHOW_MINT_MARK_N_STRING_ID` — string resource ID for the label (e.g., `R.string.include_p`)

## Checkbox and mint mark flag invariants

- Flag bit positions in `CollectionListInfo` must be **unique and contiguous** — no gaps
- `ALL_MINT_MASK` must equal `(1L << (highestMintBit + 1)) - 1`
- `ALL_CHECKBOXES_MASK` must equal `(1L << (highestCheckboxBit + 1)) - 1`
- When adding a new flag, also update the corresponding mask and add an assertion in `MainApplicationTests`

## Special images

- Override `getImageIds()` to return `Object[][]` of `{"description", imageResId}`
- Use `getImgId(tag)` to map tags to indices at runtime
- **NEVER reorder or insert into the middle of `COIN_IMG_IDS`** — the array index is the `imageId` stored in user databases. Always append new entries to the end of the array.

## After editing

- Update expected counts in `CollectionCreationTests.java`
- Update scenarios in `SharedTest.COLLECTION_LIST_INFO_SCENARIOS` if parameters changed
- Run `./gradlew testAndroidDebugUnitTest` to verify
