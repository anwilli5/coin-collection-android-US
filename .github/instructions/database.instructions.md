---
applyTo: "app/src/main/java/com/coincollection/Database*.java"
---

# Database Layer Editing Guidelines

When editing `DatabaseHelper.java` or `DatabaseAdapter.java`, follow these rules:

## Key constants

- `DATABASE_VERSION` lives in `MainApplication.java`, **not** `DatabaseHelper`
- `DATABASE_NAME` also lives in `MainApplication.java`
- Current version: check `MainApplication.DATABASE_VERSION` (do not hardcode)

## Upgrade flow (DatabaseHelper.upgradeDb)

1. `upgradeDbStructure()` — app-level schema changes (columns, renames, data fixes)
2. Loop over all user collections → call each type's `onCollectionDatabaseUpgrade()`
3. Update collection totals based on return values

## upgradeDbStructure rules

- Guard **schema changes** (ALTER TABLE, ADD COLUMN) with `if (oldVersion <= N && !fromImport)` — imported DBs have the latest schema
- **Data-value migrations** (updating existing column values like checkbox flags) must **omit** the `!fromImport` guard — use just `if (oldVersion <= N)`. Imported databases have the latest schema but carry old data values from the export that also need updating.
- Use incremental checks (`oldVersion <= N`), never equality checks
- Never drop existing columns — SQLite has limited ALTER TABLE support
- Collection-specific coin additions do NOT go here — they go in each collection's `onCollectionDatabaseUpgrade()`
- **Use `Xxx.COLLECTION_TYPE` qualified references** for collection type names (e.g., `LincolnCents.COLLECTION_TYPE` not `"Pennies"`). If a `COLLECTION_TYPE` value is later renamed via a separate migration, every old migration block that used the constant must be updated to use the old literal string value instead — only the new migration performing the rename should reference the constant.
- **More generally, if the value of any static import or constant used in a migration block changes, old migrations referencing it will silently pick up the new value and break.** This applies to `COLLECTION_TYPE` constants, column name constants (`COL_*`), table name constants (`TBL_*`), display constants (e.g., `SIMPLE_DISPLAY`), and flag constants. If a constant's value must change, update every old migration block that used it to use the old literal value instead of the constant.

## Helper methods

- `addFromYear(db, collectionListInfo, year)` — adds coins for a year respecting user's mint marks
- `addFromArrayList(db, collectionListInfo, identifiers)` — adds named coins respecting user's mint marks
- `runSqlUpdate(db, table, values, where, args)` — update rows
- `runSqlDelete(db, table, where, args)` — delete rows
- `getNextCoinSortOrder(db, tableName)` — returns next available sort order value

## Table naming

- Collection tables use the collection name as the table name, wrapped in brackets: `[tableName]`
- Use `DatabaseAdapter.removeBrackets()` when constructing SQL to prevent injection

## After editing

- Bump `DATABASE_VERSION` in `MainApplication.java` if not already done
- Add test coverage in `CollectionUpgradeTests.java`
- Run `./gradlew testAndroidDebugUnitTest` to verify
