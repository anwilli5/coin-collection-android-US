# Copilot Instructions

## Project Overview

This is a Java-based Android app for tracking coin collections. It uses Gradle for builds, Fastlane for automation, and supports both Google Play and Amazon Appstore distribution. The codebase includes Activities, Fragments, a SQLite database, test suites, and fastlane scripts for testing and screenshot generation.

## Architecture

- **Activities**: `MainActivity` (collection list), `CollectionPage` (coin grid), `CoinPageCreator` (collection creator/editor)
- **Database**: SQLite via `DatabaseHelper` (schema/migrations) and `DatabaseAdapter` (CRUD). Version-gated incremental upgrades — `DATABASE_VERSION` in `MainApplication.java`.
- **Collection types**: 50+ classes in `com.spencerpages.collections`, each extending `CollectionInfo` (9 abstract methods). Registered in `MainApplication.COLLECTION_TYPES[]`. Two patterns: year-based (e.g., `LincolnCents`) and named-identifier (e.g., `AmericanWomenQuarters`).
- **Product flavors**: `android` (Google Play) and `amazon` (Amazon Appstore). Default build/test variant: `androidDebug`.
- **Test suites**: Unit tests via Robolectric (`./gradlew testAndroidDebugUnitTest`), instrumented tests via Espresso (`./gradlew connectedAndroidTest`), lint (`./gradlew lintAndroidDebug`).

## Guiding Principles

- **OS-agnostic**: This project is developed on macOS, Linux, and Windows. NEVER use OS-specific paths (e.g., `~/Library/...`, `/Users/...`) in code, scripts, or suggestions. Always use the Gradle wrapper (`./gradlew` or `gradlew.bat`), environment variables (`$ANDROID_HOME`, `$JAVA_HOME`), or auto-detection logic instead of hard-coded paths.
- **Avoid hardcoded values**: Avoid hardcoding API levels, SDK paths, or other environment-specific values. Reference configuration files (e.g., `build.gradle`) for such information.
- **Design before coding**: For new features or significant changes, outline the design and approach before writing code and ensure a robust architecture is maintained. This includes class structures, method signatures, and interactions between components.
- **Focused changes**: Keep code changes focused on the task at hand. Avoid making unrelated improvements or refactors in the same commit or suggestion, as this can lead to scope creep and make it harder to review changes effectively.

## Common Tasks

- **Add a new coin collection**: Use the `add-coin-collection` skill — extends `CollectionInfo`, registers in `MainApplication`, adds resources and tests.
- **Add coins to existing collections**: Use the `database-migration` skill — bump `DATABASE_VERSION`, update `onCollectionDatabaseUpgrade()`, update tests.
- **Annual coin updates**: Use the `add-yearly-coins` prompt for the full workflow.
- **Run tests**: Use the `run-tests` skill for the right commands and output locations.
- **Pre-release check**: Use the `release-checklist` prompt.
- **Debug screenshots**: Use the `capture-debug-screenshot` skill with Mobile MCP.
- **Store screenshots**: Use the `capture-store-screenshots` skill for the Fastlane pipeline.

## Tools

- **Mobile MCP**: Use Mobile MCP to capture screenshots of the app running on Android emulators to test UI changes.
