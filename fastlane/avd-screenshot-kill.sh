#!/usr/bin/env bash
#
# avd-screenshot-kill.sh — Kill all running Android emulators and restart adb.
#
# Usage:
#   ./fastlane/avd-screenshot-kill.sh
#
# Ensures a clean slate before booting a new AVD for screenshots.
# Uses multiple strategies since `adb emu kill` often leaves zombie processes.

set -uo pipefail

SOURCE_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SOURCE_DIR/shell-helpers.sh"

info "Killing running emulators..."

# Strategy 1: Ask each emulator to shut down gracefully via adb
for serial in $(adb devices 2>/dev/null | grep emulator | cut -f1); do
  info "  Sending emu kill to $serial"
  adb -s "$serial" emu kill 2>/dev/null || true
done

# Strategy 2: pkill emulator and qemu processes (SIGTERM first, then SIGKILL)
pkill -f "emulator.*Screenshot_AVD" 2>/dev/null || true
pkill -f "emulator.*-avd" 2>/dev/null || true
pkill -f qemu-system 2>/dev/null || true

info "Waiting for processes to exit..."
sleep 5

# Force-kill anything that survived the graceful shutdown
pkill -9 -f qemu-system 2>/dev/null || true
sleep 1

# Strategy 3: Restart adb server to clear stale device entries
adb kill-server 2>/dev/null || true
sleep 2
adb start-server 2>/dev/null || true

# Verify
REMAINING=$(adb devices 2>/dev/null | grep -c emulator || true)
if [ "$REMAINING" -eq 0 ]; then
  info "All emulators stopped."
else
  warn "$REMAINING emulator(s) still detected — may need manual cleanup."
fi
