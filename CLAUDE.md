# CLAUDE.md

Guidance for Claude Code when working in this repository.

## Project Overview

Java-based Android app for tracking coin collections. Gradle builds, Fastlane
automation, distributed via Google Play and Amazon Appstore (plus F-Droid from
git tags). Includes Activities, Fragments, a SQLite database, unit and
instrumented test suites, and Fastlane scripts for deployment and screenshot
generation.

## Architecture

- **Activities**: `MainActivity` (collection list), `CollectionPage` (coin
  grid), `CoinPageCreator` (collection creator/editor)
- **Database**: SQLite via `DatabaseHelper` (schema/migrations) and
  `DatabaseAdapter` (CRUD). Version-gated incremental upgrades.
- **Collection types**: ~52 classes in `com.spencerpages.collections`, each
  extending `CollectionInfo` (9 abstract methods). Registered in
  `MainApplication.COLLECTION_TYPES[]`. Two patterns: year-based (e.g.,
  `LincolnCents`) and named-identifier (e.g., `AmericanWomenQuarters`).
- **Product flavors**: `android` (Google Play) and `amazon` (Amazon Appstore).
  Default build/test variant: `androidDebug`.

## Build and Test Commands

```bash
./gradlew assembleDebug                       # Build debug APKs
./gradlew testAndroidDebugUnitTest            # Unit tests (Robolectric, primary suite)
./gradlew lintAndroidDebug                    # Lint
./gradlew connectedAndroidDebugAndroidTest    # Instrumented tests (needs emulator/device)
```

- Run a single test class:
  `./gradlew testAndroidDebugUnitTest --tests "com.spencerpages.ClassName"`
- Use `--rerun-tasks` only for full release verification — not for everyday
  test runs.
- Unit test report:
  `app/build/reports/tests/testAndroidDebugUnitTest/index.html`

## Key Facts (do not get these wrong)

- `DATABASE_VERSION` and `DATABASE_NAME` live in
  `app/src/main/java/com/spencerpages/MainApplication.java` — **not** in
  `DatabaseHelper`. Never hardcode the current version number; check the file.
- `versionCode`/`versionName` live in `app/src/main/AndroidManifest.xml` —
  atypical for Android projects (NOT in `build.gradle`). Three release
  workflows (`release.yml`, `promote.yml`, `deploy.yml`) grep them from there,
  so keep them in the manifest.
- Every Java file needs the GPL-3.0 license header block — copy it from any
  existing Java file in the repo.
- `compileSdk 37` / `minSdk 21`. The `amazon` flavor pins `targetSdk 35`
  intentionally — never "unify" it with the main `targetSdk`.

## Critical Invariants

- `MainApplication.COLLECTION_TYPES[]` and each collection's `COIN_IMG_IDS[]`
  are **append-only** — array indices are persisted in user databases.
- DB migrations use `oldVersion <= N`, never `==` — upgrades must work from
  any older version.
- Adding coins to an existing series requires an
  `onCollectionDatabaseUpgrade()` block — identifier arrays and
  `OPTVAL_STILL_IN_PRODUCTION` only affect newly created collections.
- Never reference a constant in an old migration block if its value can change
  later — old blocks would silently pick up the new value and break. If a
  constant's value changes, rewrite old blocks to use the old literal value.
- Schema changes (ALTER TABLE, ADD COLUMN) need the `!fromImport` guard;
  data-value migrations must NOT have it. See
  `app/src/main/java/com/coincollection/CLAUDE.md` for exact rules.

## Guiding Principles

- **OS-agnostic**: developed on macOS, Linux, and Windows. NEVER use
  OS-specific paths (e.g., `~/Library/...`, `/Users/...`) in code, scripts,
  or suggestions. Use the Gradle wrapper (`./gradlew` or `gradlew.bat`),
  environment variables (`$ANDROID_HOME`, `$JAVA_HOME`), or auto-detection.
- **Avoid hardcoded values**: don't hardcode API levels, SDK paths, or other
  environment-specific values — reference configuration files instead.
- **Design before coding**: for new features or significant changes, outline
  the design (class structure, method signatures, component interactions)
  before writing code.
- **Focused changes**: keep changes scoped to the task at hand; no unrelated
  refactors or improvements in the same change.

## Common Tasks

| Task | Skill |
| --- | --- |
| Add a new coin collection type | `add-coin-collection` |
| Add coins to existing collections / schema changes | `database-migration` |
| Annual coin update workflow | `add-yearly-coins` |
| Run tests / lint and interpret results | `run-tests` |
| Domain-specific review of collection/DB changes | `review-changes` |
| Pre-release verification | `release-checklist` |
| Quick emulator screenshot for UI debugging | `capture-debug-screenshot` |
| Regenerate the 18 store screenshots | `capture-store-screenshots` |
| Deploy to Play / Amazon via Fastlane | `deploy-with-fastlane` |
| Emulator UI sanity + export/import test | `ui-regression-test` |

Skills are single-sourced in `.github/skills/<name>/SKILL.md` (shared with
GitHub Copilot). `.claude/skills` is a symlink to `.github/skills` so Claude
Code auto-discovers them — edit skills only under `.github/skills/`. On
checkouts where the symlink doesn't materialize (e.g. Windows without
symlink support), read the SKILL.md from `.github/skills/` directly when a
task matches.

Directory-specific rules live in nested `CLAUDE.md` files:

- `app/src/main/java/com/spencerpages/collections/CLAUDE.md` — collection
  class rules
- `app/src/main/java/com/coincollection/CLAUDE.md` — database layer rules
- `app/src/test/CLAUDE.md` — test writing rules

## CI Gates

- **markdownlint** (`markdownlint.yml`): markdownlint-cli2 runs on all `.md`
  files (default rules, MD013 disabled) — authored markdown must be
  lint-clean.
- **gradle.yml**: runs `testAndroidDebugUnitTest` + `lintAndroidDebug` on PRs.
- **fastlane-bundle-check.yml**: verifies the bundle installs and the
  Fastfile loads when Fastfile changes.
