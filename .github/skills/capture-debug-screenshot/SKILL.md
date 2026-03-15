---
name: capture-debug-screenshot
description: >
  Capture a screenshot from a running Android emulator for UI debugging or
  review using the mobile-mcp MCP server. Use when asked to take a screenshot,
  capture the current screen, check the UI, show me the app, what does the
  screen look like, verify a UI change, screenshot for debugging, or screenshot
  of the emulator. Supports interactive navigation (tap, scroll, type) before
  capturing. NOT for regenerating the full set of store listing screenshots —
  use the capture-store-screenshots skill for that.
---

# Capture Debug Screenshot

Take a quick screenshot of the app running on an Android emulator for UI
review or debugging. This skill uses the **mobile-mcp** MCP server to interact
with the emulator and capture what is currently on screen.

> **Need to regenerate all 18 store listing screenshots?** Use the
> `capture-store-screenshots` skill instead — it runs the full
> Fastlane/Screengrab pipeline across three device sizes.

## Prerequisites

- An Android emulator must be running with the app installed and launched.
- The **mobile-mcp** MCP server must be configured and available.

## Procedure

### 1. Confirm a device is available

Call `mcp_mobile-mcp_mobile_list_available_devices` to verify an emulator is
running and reachable. If no device is listed, ask the user to start an
emulator first.

### 2. Capture the current screen

Call `mcp_mobile-mcp_mobile_take_screenshot` to capture and display the
current screen inline in the chat. This is the primary output of this skill.

### 3. Navigate before capturing (optional)

If the user wants to see a specific screen or verify an interaction, use these
tools **before** taking the screenshot:

| Action | Tool |
|--------|------|
| Discover UI elements | `mcp_mobile-mcp_mobile_list_elements_on_screen` |
| Tap a coordinate | `mcp_mobile-mcp_mobile_click_on_screen_at_coordinates` |
| Long-press a coordinate | `mcp_mobile-mcp_mobile_long_press_on_screen_at_coordinates` |
| Double-tap | `mcp_mobile-mcp_mobile_double_tap_on_screen` |
| Scroll / swipe | `mcp_mobile-mcp_mobile_swipe_on_screen` |
| Press a hardware button | `mcp_mobile-mcp_mobile_press_button` |
| Type text | `mcp_mobile-mcp_mobile_type_keys` |

After navigating, call `mcp_mobile-mcp_mobile_take_screenshot` again to
capture the resulting screen.

### 4. Save to disk (optional)

If the user wants to save the screenshot to a file, call
`mcp_mobile-mcp_mobile_save_screenshot` with the desired path. By default
screenshots are only displayed inline and not persisted.

## Output

The screenshot is displayed inline in the chat conversation. No files are
written to the repository unless explicitly requested.

## Tips

- Use `mcp_mobile-mcp_mobile_list_elements_on_screen` to find tap targets
  when you are unsure where a button or element is positioned.
- Chain multiple interactions (tap → wait → screenshot) to verify multi-step
  flows.
- All `mcp_mobile-mcp_mobile_*` tool names must be loaded via
  `tool_search_tool_regex` before first use — search for `mcp_mobile-mcp` to
  load them.
