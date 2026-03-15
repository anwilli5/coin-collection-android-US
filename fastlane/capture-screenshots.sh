#!/usr/bin/env bash
#
# capture-screenshots.sh — End-to-end script to capture app store screenshots
# for the Coin Collection Android app across small, medium, and large screen
# sizes using Android emulators, Fastlane Screengrab, and the existing test
# infrastructure.
#
# Usage:
#   ./fastlane/capture-screenshots.sh              # Run full screenshot capture
#   ./fastlane/capture-screenshots.sh --force-avds  # Force-recreate AVDs first
#   ./fastlane/capture-screenshots.sh --size small   # Run only one size
#
# Output:
#   images/screenshots/small/   — 6 phone screenshots
#   images/screenshots/medium/  — 6 seven-inch tablet screenshots
#   images/screenshots/large/   — 6 ten-inch tablet screenshots
#
# Prerequisites:
#   - Android SDK with emulator, sdkmanager, avdmanager
#   - adb on PATH
#   - Ruby 3.x with Bundler + Fastlane
#   - Accepted SDK licenses

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

# ---------------------------------------------------------------------------
# Auto-detect Android SDK and Java if not already set
# ---------------------------------------------------------------------------
if [[ -z "${ANDROID_HOME:-}" ]]; then
  # Try platform-specific default SDK locations (macOS, Linux)
  for candidate in "$HOME/Library/Android/sdk" "$HOME/Android/Sdk" "/usr/lib/android-sdk"; do
    if [[ -d "$candidate" ]]; then
      export ANDROID_HOME="$candidate"
      break
    fi
  done
fi

if [[ -n "${ANDROID_HOME:-}" ]]; then
  # Add SDK tools to PATH if not already present
  for dir in "$ANDROID_HOME/emulator" "$ANDROID_HOME/platform-tools" "$ANDROID_HOME/cmdline-tools/latest/bin"; do
    if [[ -d "$dir" ]] && [[ ":$PATH:" != *":$dir:"* ]]; then
      export PATH="$dir:$PATH"
    fi
  done
fi

if [[ -z "${JAVA_HOME:-}" ]]; then
  # Try platform-specific default locations for Android Studio's bundled JDK
  for candidate in \
    "/Applications/Android Studio.app/Contents/jbr/Contents/Home" \
    "$HOME/.local/share/JetBrains/Toolbox/apps/AndroidStudio/jbr" \
    "/usr/lib/jvm/java-21-openjdk-amd64" \
    "/usr/lib/jvm/java-21-openjdk"; do
    if [[ -d "$candidate" ]]; then
      export JAVA_HOME="$candidate"
      export PATH="$JAVA_HOME/bin:$PATH"
      break
    fi
  done
fi

# ---------------------------------------------------------------------------
# Color helpers (shared)
# ---------------------------------------------------------------------------
source "$SCRIPT_DIR/shell-helpers.sh"

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------

# AVD name → size directory mapping (order matters)
AVD_NAMES=("Screenshot_AVD_Small" "Screenshot_AVD_Medium" "Screenshot_AVD_Large")
SIZE_DIRS=("small"                "medium"                "large")

SCREENSHOT_FILES=(
  "coin_actions_screen.png"
  "collection_creation_screen.png"
  "lincoln_cents_screen.png"
  "main_screen.png"
  "morgan_dollars_screen.png"
  "presidential_dollars_screen.png"
)

SCREENSHOTS_DIR="$REPO_ROOT/images/screenshots"

# ---------------------------------------------------------------------------
# Parse arguments
# ---------------------------------------------------------------------------
FORCE_AVDS=false
ONLY_SIZE=""

while [[ $# -gt 0 ]]; do
  case "$1" in
    --force-avds)
      FORCE_AVDS=true
      shift
      ;;
    --size)
      ONLY_SIZE="$2"
      shift 2
      ;;
    -h|--help)
      echo "Usage: $0 [--force-avds] [--size small|medium|large]"
      echo ""
      echo "Options:"
      echo "  --force-avds   Force-recreate all AVDs before capturing"
      echo "  --size SIZE    Capture only the specified size (small, medium, or large)"
      echo "  -h, --help     Show this help message"
      exit 0
      ;;
    *)
      error "Unknown option: $1"
      echo "Usage: $0 [--force-avds] [--size small|medium|large]"
      exit 1
      ;;
  esac
done

# Validate --size argument if provided
if [[ -n "$ONLY_SIZE" ]]; then
  if [[ "$ONLY_SIZE" != "small" && "$ONLY_SIZE" != "medium" && "$ONLY_SIZE" != "large" ]]; then
    error "Invalid size: $ONLY_SIZE (must be small, medium, or large)"
    exit 1
  fi
fi

# ---------------------------------------------------------------------------
# Step 1: Verify prerequisites
# ---------------------------------------------------------------------------
section "Step 1: Verifying prerequisites"

PREREQ_FAILED=false

if ! command -v emulator &>/dev/null; then
  error "emulator not found on PATH"
  error "Set ANDROID_HOME to your Android SDK install directory and add its tools to PATH:"
  error "  export ANDROID_HOME=<your-sdk-path>"
  error "  export PATH=\$ANDROID_HOME/emulator:\$ANDROID_HOME/platform-tools:\$ANDROID_HOME/cmdline-tools/latest/bin:\$PATH"
  PREREQ_FAILED=true
else
  info "emulator: $(emulator -version 2>&1 | head -1)"
fi

if ! command -v adb &>/dev/null; then
  error "adb not found on PATH"
  PREREQ_FAILED=true
else
  info "adb: $(adb version 2>&1 | head -1)"
fi

if ! command -v ruby &>/dev/null; then
  error "ruby not found on PATH"
  PREREQ_FAILED=true
else
  info "ruby: $(ruby --version)"
fi

if ! bundle exec fastlane --version &>/dev/null; then
  error "bundle exec fastlane failed — is Fastlane installed via Bundler?"
  PREREQ_FAILED=true
else
  info "fastlane: $(bundle exec fastlane --version 2>&1 | grep -i fastlane | head -1)"
fi

if [[ "$PREREQ_FAILED" == "true" ]]; then
  error "One or more prerequisites are missing. Please install them and try again."
  exit 1
fi

info "All prerequisites satisfied."

# ---------------------------------------------------------------------------
# Step 2: Ensure AVDs exist
# ---------------------------------------------------------------------------
section "Step 2: Ensuring AVDs exist"

cd "$REPO_ROOT"

if [[ "$FORCE_AVDS" == "true" ]]; then
  info "Force-recreating AVDs..."
  ./fastlane/avd-screenshot-create.sh --force
else
  ./fastlane/avd-screenshot-create.sh
fi

# Verify AVDs are listed
LISTED_AVDS=$(emulator -list-avds 2>/dev/null)
for avd in "${AVD_NAMES[@]}"; do
  if echo "$LISTED_AVDS" | grep -q "^${avd}$"; then
    info "AVD found: $avd"
  else
    error "AVD missing: $avd"
    error "Try running with --force-avds to recreate them."
    exit 1
  fi
done

# ---------------------------------------------------------------------------
# Step 3: Ensure output directories exist
# ---------------------------------------------------------------------------
for size in small medium large; do
  mkdir -p "$SCREENSHOTS_DIR/$size"
done

# ---------------------------------------------------------------------------
# Step 4: Capture screenshots for each screen size
# ---------------------------------------------------------------------------
TOTAL_CAPTURED=0

for i in "${!AVD_NAMES[@]}"; do
  AVD="${AVD_NAMES[$i]}"
  SIZE="${SIZE_DIRS[$i]}"

  # If --size was specified, skip non-matching sizes
  if [[ -n "$ONLY_SIZE" && "$SIZE" != "$ONLY_SIZE" ]]; then
    continue
  fi

  section "Step 4.${i}: Capturing $SIZE screenshots ($AVD)"

  # 4a — Kill any running emulators
  info "Killing any running emulators..."
  ./fastlane/avd-screenshot-kill.sh

  # 4b — Boot the AVD
  info "Booting $AVD..."
  ./fastlane/avd-screenshot-boot.sh "$AVD"

  # 4c — Run Fastlane screengrab
  cd "$REPO_ROOT"
  info "Running Fastlane build_and_screengrab..."
  bundle exec fastlane build_and_screengrab

  # 4d — Move screenshots to size directory
  info "Moving screenshots to images/screenshots/$SIZE/"
  MOVED=0
  for file in "${SCREENSHOT_FILES[@]}"; do
    SRC="$SCREENSHOTS_DIR/$file"
    DEST="$SCREENSHOTS_DIR/$SIZE/$file"
    if [[ -f "$SRC" ]]; then
      mv "$SRC" "$DEST"
      MOVED=$((MOVED + 1))
    else
      warn "Expected screenshot not found: $SRC"
    fi
  done
  info "Moved $MOVED of ${#SCREENSHOT_FILES[@]} screenshots to $SIZE/"
  TOTAL_CAPTURED=$((TOTAL_CAPTURED + MOVED))

  # 4e — Shut down the emulator
  info "Shutting down emulator..."
  ./fastlane/avd-screenshot-kill.sh
done

# ---------------------------------------------------------------------------
# Step 5: Verify output
# ---------------------------------------------------------------------------
section "Step 5: Verifying output"

if [[ -n "$ONLY_SIZE" ]]; then
  EXPECTED=6
else
  EXPECTED=18
fi

ACTUAL=$(find "$SCREENSHOTS_DIR" -mindepth 2 -name '*.png' | wc -l | tr -d ' ')

for size in small medium large; do
  if [[ -n "$ONLY_SIZE" && "$size" != "$ONLY_SIZE" ]]; then
    continue
  fi
  echo "--- $size ---"
  if [[ -d "$SCREENSHOTS_DIR/$size" ]]; then
    ls -1 "$SCREENSHOTS_DIR/$size/" 2>/dev/null || echo "  (empty)"
  else
    echo "  (directory missing)"
  fi
done

echo ""
if [[ "$ACTUAL" -ge "$EXPECTED" ]]; then
  info "All $EXPECTED expected screenshots captured successfully ($ACTUAL found)."
else
  warn "Expected $EXPECTED screenshots but found $ACTUAL."
  warn "Check the logs above for errors."
  exit 1
fi

section "Done"
info "Screenshots are in $SCREENSHOTS_DIR/{small,medium,large}/"
