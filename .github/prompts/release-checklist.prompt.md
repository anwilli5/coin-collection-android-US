---
description: 'Pre-release verification checklist — verify everything is ready before triggering the release workflow'
agent: 'agent'
tools:
  - 'run_in_terminal'
---

# Release Checklist

Verify all release prerequisites before triggering the GitHub Actions release
workflow. Run each check and report results.

## Checks

### 1. Unit tests pass

```bash
./gradlew testAndroidDebugUnitTest --rerun-tasks
```

All tests must pass. If any fail, report the failures and stop.

### 2. Lint is clean

```bash
./gradlew lintAndroidDebug
```

No new errors should be introduced. Warnings are acceptable if pre-existing.

### 3. Version is bumped

Read `app/src/main/AndroidManifest.xml` and check:
- `android:versionCode` — must be incremented from the last release
- `android:versionName` — must reflect the new version string

Compare with the latest git tag to confirm the version has changed:
```bash
git describe --tags --abbrev=0
```

### 4. DATABASE_VERSION is correct

If any database migrations were added since the last release:
- Verify `DATABASE_VERSION` in `MainApplication.java` was incremented
- Verify all `onCollectionDatabaseUpgrade()` methods use the correct
  version checks

If no database changes were made, confirm DATABASE_VERSION is unchanged.

### 5. Store metadata is current (if applicable)

Check if store text needs updating:
- `fastlane/metadata/android/en-US/full_description.txt`
- `fastlane/metadata/android/en-US/short_description.txt`

### 6. Screenshots are current (if UI changed)

If UI changes were made, screenshots may need regenerating:
- `images/screenshots/small/` (6 PNGs)
- `images/screenshots/medium/` (6 PNGs)
- `images/screenshots/large/` (6 PNGs)

Use the `capture-store-screenshots` skill if regeneration is needed.

### 7. Working tree is clean

```bash
git status
```

No uncommitted changes should exist.

### 8. Branch is up to date

```bash
git fetch origin main
git log HEAD..origin/main --oneline
```

No unmerged upstream changes should exist.

## Output

Report a summary table:

| Check | Status | Notes |
|-------|--------|-------|
| Unit tests | PASS/FAIL | |
| Lint | PASS/FAIL | |
| Version bumped | YES/NO | vX.Y.Z (code: NN) |
| DATABASE_VERSION | OK/NEEDS BUMP | Current: N |
| Store metadata | OK/NEEDS UPDATE | |
| Screenshots | OK/NEEDS UPDATE | |
| Working tree clean | YES/NO | |
| Branch up to date | YES/NO | |

If all checks pass, the release workflow can be triggered.
If any check fails, list the required actions before release.
