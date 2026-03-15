#!/usr/bin/env bash
#
# avd-screenshot-boot.sh — Boot an AVD and wait for it to fully start.
#
# Usage:
#   ./fastlane/avd-screenshot-boot.sh <avd_name>
#   ./fastlane/avd-screenshot-boot.sh Screenshot_AVD_Small
#
# Starts the emulator in the background, waits for sys.boot_completed=1,
# and suppresses ANR dialogs for automated use.

set -uo pipefail

SOURCE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SOURCE_DIR/shell-helpers.sh"

AVD_NAME="${1:-}"
if [[ -z "$AVD_NAME" ]]; then
  echo "Usage: $0 <avd_name>" >&2
  echo "  e.g. $0 Screenshot_AVD_Small" >&2
  exit 1
fi

MAX_WAIT_SECS=240  # 4 minutes
POLL_INTERVAL=2

info "Booting AVD: $AVD_NAME"
emulator -avd "$AVD_NAME" -no-snapshot-load -no-audio -gpu swiftshader_indirect &
EMULATOR_PID=$!

info "Waiting for adb device..."
adb wait-for-device

info "Waiting for boot to complete (up to ${MAX_WAIT_SECS}s)..."
ELAPSED=0
while [ "$ELAPSED" -lt "$MAX_WAIT_SECS" ]; do
  STATUS=$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r\n ')
  if [ "$STATUS" = "1" ]; then
    info "Boot complete after ~${ELAPSED}s"
    break
  fi
  sleep "$POLL_INTERVAL"
  ELAPSED=$((ELAPSED + POLL_INTERVAL))
done

if [ "$ELAPSED" -ge "$MAX_WAIT_SECS" ]; then
  error "Boot timed out after ${MAX_WAIT_SECS}s"
  exit 1
fi

# Suppress ANR / "Process isn't responding" dialogs (common on slow emulators)
adb shell settings put global hide_error_dialogs 1 2>/dev/null || true

# Increase ANR timeout thresholds so slow x86_64 emulators don't trigger them
adb shell settings put global anr_show_background 0 2>/dev/null || true

# Wait for the system to fully settle after boot.
# sys.boot_completed fires early; System UI, launcher, the package manager, and
# other services often need significantly more time on software-rendered
# emulators.  Instead of a fixed sleep we poll concrete readiness signals so
# the script adapts to both fast and slow hosts.
POST_BOOT_MAX=180  # up to 3 minutes for post-boot settling
POST_BOOT_POLL=5
POST_BOOT_ELAPSED=0

echo "[INFO] Waiting for system services to settle (up to ${POST_BOOT_MAX}s)..."
while [ "$POST_BOOT_ELAPSED" -lt "$POST_BOOT_MAX" ]; do
  # 1. Boot animation must have stopped
  BOOTANIM=$(adb shell getprop init.svc.bootanim 2>/dev/null | tr -d '\r\n ')
  # 2. Package manager must respond (critical for app install / instrument)
  PM_READY=$(adb shell pm path android 2>/dev/null | tr -d '\r\n ')
  # 3. Launcher should be running (indicates home screen is up)
  LAUNCHER=$(adb shell dumpsys activity activities 2>/dev/null \
    | grep -ci "\.launcher\|com.google.android.apps.nexuslauncher" || true)

  if [ "$BOOTANIM" = "stopped" ] && [ -n "$PM_READY" ] && [ "$LAUNCHER" -gt 0 ] 2>/dev/null; then
    echo "[INFO] System settled after ~${POST_BOOT_ELAPSED}s post-boot"
    break
  fi

  sleep "$POST_BOOT_POLL"
  POST_BOOT_ELAPSED=$((POST_BOOT_ELAPSED + POST_BOOT_POLL))
done

if [ "$POST_BOOT_ELAPSED" -ge "$POST_BOOT_MAX" ]; then
  echo "[WARN] Post-boot settle timed out after ${POST_BOOT_MAX}s — continuing anyway"
fi

# Extra grace period: even after the signals above are green, give the UI
# a few more seconds to finish rendering (especially on large-screen AVDs).
echo "[INFO] Final 15s grace period..."
sleep 15

# Dismiss any ANR or crash dialogs that appeared during boot settling
adb shell am broadcast -a android.intent.action.CLOSE_SYSTEM_DIALOGS 2>/dev/null || true
adb shell input keyevent KEYCODE_HOME 2>/dev/null || true

echo "[INFO] AVD '$AVD_NAME' is ready (PID: $EMULATOR_PID)"
