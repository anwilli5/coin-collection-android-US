---
description: 'Review recent changes for correctness — database migrations, collection upgrades, checkbox flags, tests, and resources'
agent: 'agent'
---

# Review Changes

Audit the current uncommitted changes (or a specified set of files) against
the project's known pitfall areas. Use `git diff` to identify what changed,
then verify each checklist section below. Skip sections that don't apply to
the current changes.

## 1. Database migration guards (`upgradeDbStructure`)

For each version-gated block in `DatabaseHelper.upgradeDbStructure()`:

- Classify it as **schema change** (ALTER TABLE, ADD COLUMN) or
  **data-value migration** (UPDATE existing column values, ORing flag bits)
- **Schema changes** must have `&& !fromImport` — imported databases already
  have the latest columns
- **Data-value migrations** must **omit** the `!fromImport` guard — imported
  databases have the latest schema but carry old data values from the export
  that also need updating
- All blocks use `oldVersion <= N` (incremental), never `oldVersion == N`

## 2. Collection upgrade completeness

For each collection class that changed:

- Every collection that adds coins in `populateCollectionLists()` has a
  matching `onCollectionDatabaseUpgrade()` block for the current
  `DATABASE_VERSION`
- When `populateCollectionLists()` sets `imageId` via `getImgId()`, the
  corresponding `onCollectionDatabaseUpgrade()` passes the same image ID
- `addFromYear` / `addFromArrayList` calls respect the user's mint mark
  configuration

## 3. Checkbox/flag round-trip integrity

For any new checkbox or flag added to a collection:

- Bit constant defined in `CollectionListInfo` (appended, not inserted)
- `has*()` getter method in `CollectionListInfo`
- Encoder block in `CoinPageCreator.getCheckboxFlagsFromParameters()`
- Decoder block in `CoinPageCreator.getParametersFromCollectionListInfo()`
- `ALL_CHECKBOXES_MASK` in `CollectionListInfo` updated to cover new bits
- Migration in `upgradeDbStructure()` to set the flag for existing
  collections (so coins already added by upgrade are visible)

## 4. Test coverage

- `CollectionCreationTests` updated for affected collections — new checkbox
  columns added to test arrays, expected counts adjusted
- `CollectionUpgradeTests` covers new upgrade paths
- `SharedTest.COLLECTION_LIST_INFO_SCENARIOS` updated if checkbox or mint
  mark flags changed
- Round-trip test (`CoinPageCreatorTests.test_createFromParameters`) passes

## 5. Resource completeness

- New string resources in `res/values/strings.xml` for any new UI labels
- New drawable resources for any new coin images
- **`COIN_IMG_IDS` array ordering is immutable** — the array index is the
  `imageId` persisted in user databases. Verify that:
  - New entries are **only appended** to the end of the array
  - No existing entries were reordered, removed, or inserted in the middle
  - Index comments (e.g., `// 19`) are accurate after any additions
  - Reordering or inserting would silently remap images for every existing
    user collection — this is a data-corruption-level bug
- **`COLLECTION_TYPES` array ordering is immutable** — removing an entry
  causes `getIndexFromCollectionNameStr()` to return -1, crashing the app
  for any user who has that collection. Verify that:
  - New entries are **only appended** to the end of the array
  - No existing entries were reordered, removed, or inserted in the middle
  - The display-order arrays (`BASIC_COLLECTIONS`, `ADVANCED_COLLECTIONS`,
    `MORE_COLLECTIONS`) can be freely reordered, but `COLLECTION_TYPES`
    cannot

## 6. Scope discipline

- Changes are limited to what was requested — no unrelated refactors
- No modifications to collections excluded from scope
- `DATABASE_VERSION` bumped only if shipping a new release (not for
  unreleased changes already covered by the current version)

## Output

Report a summary table with one row per section. Skip sections that don't
apply to the current diff.

| Area | Status | Findings |
| --- | --- | --- |
| DB migration guards | OK / ISSUE | details |
| Collection upgrades | OK / ISSUE | details |
| Checkbox round-trip | OK / ISSUE | details |
| Test coverage | OK / ISSUE | details |
| Resources | OK / ISSUE | details |
| Scope | OK / ISSUE | details |

If all applicable areas are OK, confirm the changes look good.
If any issues are found, list the specific problems and recommended fixes.
