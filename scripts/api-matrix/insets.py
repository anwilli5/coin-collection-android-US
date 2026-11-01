"""Finds app elements drawn under the status bar, navigation bar or display
cutout, from a uiautomator dump and the output of `dumpsys window`.

Each Android release reports the system bars differently, so this tries the
formats in order: InsetsSource lines (API 30+), the bar windows' shown frames
(API 21-28), the display policy's stable area (API 29) and the bar windows'
surface rectangles (API 26).
"""
import re

APP_ELEMENT_KINDS = ("statusBars", "navigationBars", "displayCutout")


def _rect(text):
    left, top, right, bottom = (int(float(v)) for v in re.findall(r"-?[\d.]+", text)[:4])
    return left, top, right, bottom


def system_bars(window_dump):
    bars = {}
    for m in re.finditer(r"InsetsSource[^\n]*?type=(statusBars|navigationBars|displayCutout|ITYPE_\w+)"
                         r"[^\n]*?frame=(\[-?\d+,-?\d+\]\[-?\d+,-?\d+\])[^\n]*?visible=(true|false)", window_dump):
        kind, frame, visible = m.groups()
        kind = {"ITYPE_STATUS_BAR": "statusBars", "ITYPE_NAVIGATION_BAR": "navigationBars"}.get(kind, kind)
        if visible == "true" and kind in APP_ELEMENT_KINDS:
            rect = _rect(frame)
            if rect[2] > rect[0] and rect[3] > rect[1]:
                bars.setdefault(kind, rect)
    if not bars:
        for name, kind in (("StatusBar", "statusBars"), ("NavigationBar", "navigationBars")):
            m = re.search(r"Window #\d+ Window\{[0-9a-f]+ u0 " + name + r"\}:.*?mShownFrame=(\[[-\d.,]+\]\[[-\d.,]+\])",
                          window_dump, re.S)
            if m:
                rect = _rect(m.group(1))
                if rect[2] > rect[0] and rect[3] > rect[1]:
                    bars[kind] = rect
    if not bars:
        stable = re.search(r"mStable=\[(\d+),(\d+)\]\[(\d+),(\d+)\]", window_dump)
        full = re.search(r"mUnrestricted=\[(\d+),(\d+)\]\[(\d+),(\d+)\]", window_dump)
        if stable and full:
            left, top, right, bottom = map(int, stable.groups())
            width, height = int(full.group(3)), int(full.group(4))
            if top > 0:
                bars["statusBars"] = (0, 0, width, top)
            if bottom < height:
                bars["navigationBars"] = (0, bottom, width, height)
            elif right < width:
                bars["navigationBars"] = (right, 0, width, height)
            elif left > 0:
                bars["navigationBars"] = (0, 0, left, height)
    if not bars:
        for name, kind in (("StatusBar", "statusBars"), ("NavigationBar", "navigationBars")):
            m = re.search(r"Window #\d+ Window\{[0-9a-f]+ u0 " + name + r"\d?\}:.*?Surface: shown=true[^\n]*?"
                          r"rect=\((-?[\d.]+),(-?[\d.]+)\) ([\d.]+) x ([\d.]+)", window_dump, re.S)
            if m:
                x, y, w, h = (int(float(v)) for v in m.groups())
                if w > 0 and h > 0:
                    bars[kind] = (x, y, x + w, y + h)
    m = re.search(r"mDisplayCutout=.*?bounds=\[?Rect\((\d+), (\d+) - (\d+), (\d+)\)", window_dump)
    if m and "displayCutout" not in bars:
        bars["displayCutout"] = tuple(map(int, m.groups()))
    return bars


def overlaps(ui_root, window_dump, package):
    """(bars, problems): each problem names a bar and a visible app element
    (with text, a description, or clickable) at least half under it. Partly
    scrolled list rows can report bounds that reach under a bar while their
    visible part is clear of it, so a smaller overlap doesn't count"""
    bars = system_bars(window_dump)
    problems = []
    for node in ui_root.iter("node"):
        if node.get("package") != package:
            continue
        if not (node.get("text") or node.get("content-desc") or node.get("clickable") == "true"):
            continue
        left, top, right, bottom = _rect(node.get("bounds"))
        if right <= left or bottom <= top:
            continue
        area = (right - left) * (bottom - top)
        for kind, (x1, y1, x2, y2) in bars.items():
            covered = max(0, min(right, x2) - max(left, x1)) * max(0, min(bottom, y2) - max(top, y1))
            if covered * 2 >= area:
                what = node.get("text") or node.get("content-desc") or node.get("resource-id", "").split("/")[-1]
                problems.append(f"{kind} covers {what!r} {node.get('bounds')}")
    return bars, problems
