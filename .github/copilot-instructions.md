# Copilot Instructions

## Project Overview

This is a Java-based Android app for tracking coin collections. It uses Gradle for builds, Fastlane for automation, and supports both Google Play and Amazon Appstore distribution. The codebase includes Activities, Fragments, a SQLite database, test suites, and fastlane scripts for testing and screenshot generation.

## Guiding Principles
- **OS-agnostic**: This project is developed on macOS, Linux, and Windows. NEVER use OS-specific paths (e.g., `~/Library/...`, `/Users/...`) in code, scripts, or suggestions. Always use the Gradle wrapper (`./gradlew` or `gradlew.bat`), environment variables (`$ANDROID_HOME`, `$JAVA_HOME`), or auto-detection logic instead of hard-coded paths.
- **Avoid hardcoded values**: Avoid hardcoding API levels, SDK paths, or other environment-specific values. Reference configuration files (e.g., `build.gradle`) for such information.
- **Design before coding**: For new features or significant changes, outline the design and approach before writing code and ensure a robust architecture is maintained. This includes class structures, method signatures, and interactions between components.
- **Focused changes**: Keep code changes focused on the task at hand. Avoid making unrelated improvements or refactors in the same commit or suggestion, as this can lead to scope creep and make it harder to review changes effectively.

## Tools
- **Mobile MCP**: Use Mobile MCP to capture screenshots of the app running on Android emulators to test UI changes.
