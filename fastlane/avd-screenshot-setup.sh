#!/bin/sh
# https://android.googlesource.com/platform/frameworks/base/+/master/packages/SystemUI/docs/demo_mode.md
CMD=$1
if [[ $CMD != "on" && $CMD != "off" ]]; then
  echo "Usage: $0 [on|off]" >&2
  exit
fi

adb root || exit 1
adb wait-for-any-device
adb shell settings put global sysui_demo_allowed 1

if [ $CMD == "on" ]; then
  adb shell am broadcast -a com.android.systemui.demo -e command enter || exit 1
  adb shell am broadcast -a com.android.systemui.demo -e command clock -e hhmm 1230
  adb shell am broadcast -a com.android.systemui.demo -e command battery -e plugged false
  adb shell am broadcast -a com.android.systemui.demo -e command battery -e level 100
  adb shell am broadcast -a com.android.systemui.demo -e command network -e wifi show -e level 4
  adb shell am broadcast -a com.android.systemui.demo -e command network -e mobile show -e datatype none -e level 4
  adb shell am broadcast -a com.android.systemui.demo -e command notifications -e visible false
elif [ $CMD == "off" ]; then
  adb shell am broadcast -a com.android.systemui.demo -e command exit
fi