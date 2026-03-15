#!/usr/bin/env bash
#
# avd-screenshot-create.sh — Create the three AVDs used for screenshot generation.
#
# Usage:
#   ./fastlane/avd-screenshot-create.sh           # Create AVDs (skip if they already exist)
#   ./fastlane/avd-screenshot-create.sh --force   # Delete and recreate AVDs
#
# Prerequisites:
#   - Android SDK installed with $ANDROID_HOME or $ANDROID_SDK_ROOT set
#   - sdkmanager and avdmanager on PATH (or under $ANDROID_HOME/cmdline-tools/latest/bin/)
#   - Accepted SDK licenses (run: sdkmanager --licenses)
#
# AVDs created:
#   Screenshot_AVD_Small     — Pixel 6 phone           → images/screenshots/small/
#   Screenshot_AVD_Medium    — Nexus 7 (2013) tablet   → images/screenshots/medium/
#   Screenshot_AVD_Large     — Pixel C tablet          → images/screenshots/large/
#
# All AVDs use the app's targetSdkVersion with google_apis system images.

set -euo pipefail

# ---------------------------------------------------------------------------
# Auto-detect Java and Android SDK if not set
# ---------------------------------------------------------------------------
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
  for dir in "$ANDROID_HOME/cmdline-tools/latest/bin" "$ANDROID_HOME/emulator" "$ANDROID_HOME/platform-tools"; do
    if [[ -d "$dir" ]] && [[ ":$PATH:" != *":$dir:"* ]]; then
      export PATH="$dir:$PATH"
    fi
  done
fi

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------

# Read targetSdkVersion from app/build.gradle so AVDs match the app's target API
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
BUILD_GRADLE="$REPO_ROOT/app/build.gradle"

if [[ -f "$BUILD_GRADLE" ]]; then
  # Skip comment lines (starting with optional whitespace + //) when searching
  API_LEVEL=$(grep 'targetSdkVersion' "$BUILD_GRADLE" | grep -v '^\s*//' | head -1 | sed 's/[^0-9]//g')
fi

if [[ -z "${API_LEVEL:-}" ]]; then
  echo "Error: Could not read targetSdkVersion from $BUILD_GRADLE" >&2
  echo "Ensure app/build.gradle exists and contains a targetSdkVersion entry." >&2
  exit 1
fi

# Preferred image types in order of preference
IMAGE_TYPES=("google_apis" "google_apis_playstore")

# AVD definitions: name|device_profile|description
# Note: AVD names must only contain a-z A-Z 0-9 . _ - (no spaces)
AVDS=(
  "Screenshot_AVD_Small|pixel_6|Pixel 6 phone (small screenshots)"
  "Screenshot_AVD_Medium|Nexus 7|Nexus 7 2013 tablet (medium screenshots)"
  "Screenshot_AVD_Large|pixel_c|Pixel C tablet (large screenshots)"
)

# Hardware overrides applied to every AVD config.ini
COMMON_HW_OVERRIDES=(
  "hw.keyboard=yes"
  "hw.gpu.enabled=yes"
  "hw.gpu.mode=auto"
  "disk.dataPartition.size=2G"
)

# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

source "$SCRIPT_DIR/shell-helpers.sh"

# Resolve Android SDK root
resolve_sdk_root() {
  if [[ -n "${ANDROID_HOME:-}" ]]; then
    echo "$ANDROID_HOME"
  elif [[ -n "${ANDROID_SDK_ROOT:-}" ]]; then
    echo "$ANDROID_SDK_ROOT"
  else
    # Try platform-specific default locations
    for default in "$HOME/Library/Android/sdk" "$HOME/Android/Sdk" "/usr/lib/android-sdk"; do
      if [[ -d "$default" ]]; then
        echo "$default"
        return
      fi
    done
    error "Cannot find Android SDK. Set ANDROID_HOME or ANDROID_SDK_ROOT."
    exit 1
  fi
}

# Find a command either on PATH or inside the SDK
find_sdk_tool() {
  local tool=$1
  local sdk_root
  sdk_root=$(resolve_sdk_root)
  # Prefer cmdline-tools versions over legacy tools/bin (which may have
  # Java compatibility issues on newer JDKs).
  local candidates=(
    "$sdk_root/cmdline-tools/latest/bin/$tool"
    "$sdk_root/cmdline-tools/*/bin/$tool"
    "$sdk_root/tools/bin/$tool"
  )
  for candidate in "${candidates[@]}"; do
    # Handle glob expansion
    for match in $candidate; do
      if [[ -x "$match" ]]; then
        echo "$match"
        return
      fi
    done
  done
  # Fall back to PATH
  if command -v "$tool" &>/dev/null; then
    command -v "$tool"
  else
    error "Cannot find '$tool'. Install Android SDK Command-line Tools."
    exit 1
  fi
}

# Find the best available system image for the target API level.
# Tries exact API match first, then sub-versions (e.g. android-36.1),
# with each IMAGE_TYPE in preference order.
find_system_image() {
  local arch=$1
  local sdkmanager=$2
  local sdk_root
  sdk_root=$(resolve_sdk_root)

  # Build candidate list: exact API level, then sub-versions
  local api_candidates=("android-${API_LEVEL}")
  # Also check for sub-versions like android-36.1, android-36.0-Baklava, etc.
  if [[ -d "$sdk_root/system-images" ]]; then
    for d in "$sdk_root/system-images"/android-${API_LEVEL}*; do
      if [[ -d "$d" ]]; then
        local base
        base=$(basename "$d")
        # Don't add duplicates
        local found=false
        for c in "${api_candidates[@]}"; do
          [[ "$c" == "$base" ]] && found=true
        done
        $found || api_candidates+=("$base")
      fi
    done
  fi

  # Try each combination
  for api in "${api_candidates[@]}"; do
    for img_type in "${IMAGE_TYPES[@]}"; do
      local candidate="system-images;${api};${img_type};${arch}"
      # Check if installed locally
      if [[ -d "$sdk_root/system-images/${api}/${img_type}/${arch}" ]]; then
        echo "$candidate"
        return 0
      fi
    done
  done

  # Nothing installed — try each combination via sdkmanager --list
  for api in "${api_candidates[@]}"; do
    for img_type in "${IMAGE_TYPES[@]}"; do
      local candidate="system-images;${api};${img_type};${arch}"
      if "$sdkmanager" --list 2>/dev/null | grep -q "$candidate"; then
        echo "$candidate"
        return 0
      fi
    done
  done

  # Fallback to original format
  echo "system-images;android-${API_LEVEL};${IMAGE_TYPES[0]};${arch}"
}
detect_arch() {
  local machine
  machine=$(uname -m)
  case "$machine" in
    arm64|aarch64)
      echo "arm64-v8a"
      ;;
    x86_64|amd64)
      echo "x86_64"
      ;;
    *)
      error "Unsupported architecture: $machine"
      exit 1
      ;;
  esac
}

# Check if an AVD already exists
avd_exists() {
  local name=$1
  local avdmanager=$2
  "$avdmanager" list avd -c 2>/dev/null | grep -qxF "$name"
}

# Get AVD home directory
avd_home() {
  echo "${ANDROID_AVD_HOME:-$HOME/.android/avd}"
}

# Apply hardware overrides to an AVD's config.ini
apply_hw_overrides() {
  local avd_name=$1
  local config_file
  config_file="$(avd_home)/${avd_name}.avd/config.ini"

  if [[ ! -f "$config_file" ]]; then
    warn "config.ini not found for '$avd_name', skipping hardware overrides"
    return
  fi

  for override in "${COMMON_HW_OVERRIDES[@]}"; do
    local key="${override%%=*}"
    local value="${override#*=}"
    if grep -q "^${key}=" "$config_file"; then
      sed -i'.bak' "s|^${key}=.*|${key}=${value}|" "$config_file"
    else
      echo "${key}=${value}" >> "$config_file"
    fi
  done

  # Clean up sed backup files
  rm -f "${config_file}.bak"
  info "  Applied hardware overrides to config.ini"
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------

FORCE=false
if [[ "${1:-}" == "--force" ]]; then
  FORCE=true
fi

ARCH=$(detect_arch)
SDKMANAGER=$(find_sdk_tool sdkmanager)
AVDMANAGER=$(find_sdk_tool avdmanager)
SYSTEM_IMAGE=$(find_system_image "$ARCH" "$SDKMANAGER")

info "Android SDK:    $(resolve_sdk_root)"
info "Architecture:   $ARCH"
info "System image:   $SYSTEM_IMAGE"
info "sdkmanager:     $SDKMANAGER"
info "avdmanager:     $AVDMANAGER"
echo

# Step 1: Install system image
info "Installing system image (this may take a few minutes on first run)..."
yes | "$SDKMANAGER" --install "$SYSTEM_IMAGE" 2>/dev/null || {
  # If the image is already installed, sdkmanager may return non-zero
  warn "sdkmanager returned non-zero — image may already be installed"
}
echo

# Step 2: Create each AVD
for entry in "${AVDS[@]}"; do
  IFS='|' read -r avd_name device_profile description <<< "$entry"

  echo "---"
  info "AVD: $avd_name"
  info "  Device: $device_profile"
  info "  Description: $description"

  if avd_exists "$avd_name" "$AVDMANAGER"; then
    if [[ "$FORCE" == true ]]; then
      warn "  Deleting existing AVD '$avd_name'..."
      "$AVDMANAGER" delete avd --name "$avd_name" 2>/dev/null || true
    else
      info "  Already exists — skipping (use --force to recreate)"
      continue
    fi
  fi

  info "  Creating AVD..."
  echo no | "$AVDMANAGER" create avd \
    --name "$avd_name" \
    --package "$SYSTEM_IMAGE" \
    --device "$device_profile" \
    --force \
    2>&1 | sed 's/^/  /'

  apply_hw_overrides "$avd_name"

  info "  Created successfully"
  echo
done

# Step 3: Summary
echo
echo "========================================="
info "AVD creation complete!"
echo "========================================="
echo
info "Available AVDs:"

EMULATOR=$(find_sdk_tool emulator 2>/dev/null || true)
if [[ -n "$EMULATOR" ]]; then
  "$EMULATOR" -list-avds 2>/dev/null | sed 's/^/  /'
else
  "$AVDMANAGER" list avd -c 2>/dev/null | sed 's/^/  /'
fi

echo
info "To boot an AVD:"
echo "  emulator -avd \"Screenshot_AVD_Small\" -no-snapshot-load &"
echo
info "To generate screenshots (with an AVD running):"
echo "  bundle exec fastlane build_and_screengrab"
