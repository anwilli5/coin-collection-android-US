---
name: release-checklist
description: >
  Run the pre-release verification checklist before triggering the GitHub
  Actions release workflow. Verifies unit tests, lint, the minified R8 release
  build, version bump in version.properties, DATABASE_VERSION, store metadata,
  screenshots, and git state, then reports a summary table. Use when asked to run the release
  checklist, verify release readiness, or check that everything is ready
  before a release.
disable-model-invocation: true
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

### 3. Release build is R8-clean and runs

Release builds are minified, resource-shrunk, and obfuscated (`minifyEnabled` + `shrinkResources` in `app/build.gradle`) to satisfy Google Play's DEX-optimization requirement. Neither test suite covers this — unit and instrumented tests both run against the unminified `androidDebug` variant — so an R8 regression reaches users unless it is caught here.

Build both flavors:

```bash
./gradlew assembleAndroidRelease assembleAmazonRelease
```

Then confirm R8 ran and left every collection class distinct:

```bash
MAP=app/build/outputs/mapping/androidRelease/mapping.txt
test -f "$MAP" || echo "FAIL: no mapping.txt - R8 did not run"
echo "source:    $(ls app/src/main/java/com/spencerpages/collections/*.java | wc -l)"
echo "in map:    $(grep -c '^com\.spencerpages\.collections\.' "$MAP")"
echo "distinct:  $(grep '^com\.spencerpages\.collections\.' "$MAP" | awk '{print $3}' | sort -u | wc -l)"
```

All three counts must match. A shortfall in the last one means R8 merged two collection classes, which silently breaks `MainApplication.getIndexFromCollectionClass()`.

Finally run the **Release Build Smoke Test (R8)** section of the `ui-regression-test` skill against the signed release APK. A green unit-test run is not a substitute: R8 failures are runtime-only.

### 4. Version is bumped

Read `version.properties` at the repo root and check:

- `versionCode` — must be incremented from the last release
- `versionName` — must reflect the new version string

Compare with the latest git tag to confirm the version has changed:

```bash
git describe --tags --abbrev=0
```

### 5. DATABASE_VERSION is correct

If any database migrations were added since the last release:

- Verify `DATABASE_VERSION` in `MainApplication.java` was incremented
- Verify all `onCollectionDatabaseUpgrade()` methods use the correct
  version checks

If no database changes were made, confirm DATABASE_VERSION is unchanged.

### 6. Store metadata is current (if applicable)

Check if store text needs updating:

- `fastlane/metadata/android/en-US/full_description.txt`
- `fastlane/metadata/android/en-US/short_description.txt`

### 7. Screenshots are current (if UI changed)

If UI changes were made, screenshots may need regenerating:

- `images/screenshots/small/` (6 PNGs)
- `images/screenshots/medium/` (6 PNGs)
- `images/screenshots/large/` (6 PNGs)

Use the `capture-store-screenshots` skill if regeneration is needed.

### 8. Working tree is clean

```bash
git status
```

No uncommitted changes should exist.

### 9. Branch is up to date

```bash
git fetch origin main
git log HEAD..origin/main --oneline
```

No unmerged upstream changes should exist.

## Output

Report a summary table:

| Check | Status | Notes |
| --- | --- | --- |
| Unit tests | PASS/FAIL | |
| Lint | PASS/FAIL | |
| Release build (R8) | PASS/FAIL | Built, mapping.txt present, smoke test green |
| Version bumped | YES/NO | vX.Y.Z (code: NN) |
| DATABASE_VERSION | OK/NEEDS BUMP | Current: N |
| Store metadata | OK/NEEDS UPDATE | |
| Screenshots | OK/NEEDS UPDATE | |
| Working tree clean | YES/NO | |
| Branch up to date | YES/NO | |

If all checks pass, the release workflow can be triggered.
If any check fails, list the required actions before release.
