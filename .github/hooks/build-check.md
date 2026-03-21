---
description: 'Run a quick build check after saving Java source files to catch compilation errors early'
event: 'onFileSave'
filePattern: 'app/src/main/java/**/*.java'
---

# Build Check

Run `./gradlew assembleDebug` to verify the project compiles after source
file changes. Report any compilation errors found.

Do not run tests — this hook is only for catching compile errors quickly.
If the build fails, show the relevant error messages and suggest fixes.
