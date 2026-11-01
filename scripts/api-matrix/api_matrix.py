#!/usr/bin/env python3
"""Tests the app on headless emulators across Android API levels.

Subcommands:
  upgrade       Install the previous release, import the seed backup through
                the system file picker, then install the new release over it.
                Checks the upgraded database against the other levels, every
                main screen for elements under the system bars, importing a
                JSON backup with the new release, and the crash log.
  instrumented  Run the instrumented (Espresso) test suite on each level.

Each level boots its own emulator (AVD api-matrix-<level>; --create-avds
creates missing ones), runs, and shuts it down. Results go to --out, with a
report.md summary; the exit code is 1 if any level failed.

Examples (from the repo root):
  python3 scripts/api-matrix/api_matrix.py upgrade --create-avds \\
      --old app-android-release-v3.8.4.apk --new app-android-release-v3.9.0.apk
  python3 scripts/api-matrix/api_matrix.py instrumented --levels 21 33
"""
import argparse
import json
import os
import re
import shutil
import sqlite3
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
from pathlib import Path

import insets
from android_sdk import REPO_ROOT, WINDOWS, Device, SdkError, avd_exists, bounds, create_avd, host_abi

SEED_DIR = Path(__file__).resolve().parent / "seed"
MAIN_ACTIVITY = "com.coincollection.MainActivity"
# Points where behavior the app depends on changes, between minSdk and
# targetSdk (read from app/build.gradle): 23 root window insets, 26 the last
# tested file picker without a .json MIME type, 29 gesture navigation, 33 system
# back callbacks, 35 enforced edge-to-edge (and the amazon flavor's targetSdk)
MIDDLE_LEVELS = [23, 26, 29, 33, 35]


def default_levels():
    gradle = (REPO_ROOT / "app" / "build.gradle").read_text(encoding="utf-8")
    min_sdk = int(re.search(r"^\s*minSdk\s*=\s*(\d+)", gradle, re.M).group(1))
    target_sdk = int(re.search(r"^\s*targetSdk\s*=\s*(\d+)", gradle, re.M).group(1))
    return sorted({min_sdk, target_sdk, *(level for level in MIDDLE_LEVELS if min_sdk < level < target_sdk)})


def log(level, message):
    print(f"[api{level}] {message}", flush=True)


# --- Emulator lifecycle ---------------------------------------------------------

def start_device(level, args, outdir):
    avd = f"api-matrix-{level}"
    if not avd_exists(avd):
        if not args.create_avds:
            raise SdkError(f"AVD {avd} doesn't exist: rerun with --create-avds")
        log(level, f"creating AVD {avd}")
        create_avd(avd, level, args.abi)
    log(level, f"booting {avd} (headless)")
    device = Device.boot(avd, args.port, args.emulator_arg, outdir / "emulator.log")
    log(level, f"booted: Android {device.getprop('ro.build.version.release')}, SDK {device.sdk}")
    return device


def for_each_level(args, run_level):
    """Run run_level(level, device, outdir) on every level; returns {level: result}"""
    results = {}
    for level in args.levels:
        outdir = args.out / f"api{level}"
        if outdir.exists():
            shutil.rmtree(outdir)
        outdir.mkdir(parents=True)
        device = None
        try:
            device = start_device(level, args, outdir)
            results[level] = run_level(level, device, outdir)
        except Exception as e:  # report the level as failed and carry on with the next
            log(level, f"ERROR: {e}")
            results[level] = {"error": str(e)}
        finally:
            if device is not None:
                device.kill()
    return results


# --- App helpers ------------------------------------------------------------------

def dismiss_tips(device):
    for _ in range(3):
        if not device.tap(text="Okay!"):
            return


def launch(device, package):
    device.shell("am", "start", "-W", "-n", f"{package}/{MAIN_ACTIVITY}", check=False, timeout=60)
    time.sleep(3)
    dismiss_tips(device)


def scroll_to_top(device):
    width, height = device.screen_size()
    for _ in range(4):
        device.swipe(width // 2, height // 4, width // 2, height * 3 // 4)
    time.sleep(1)


def collections(device):
    """(name, progress) of every collection on the main list, scrolling if needed"""
    found = []
    width, height = device.screen_size()
    scroll_to_top(device)
    for _ in range(6):
        root = device.dump_ui()
        names = device.find_all(root, res_id="collectionNameTextView")
        progress = device.find_all(root, res_id="progressTextView")
        new = [(n.get("text"), p.get("text")) for n, p in zip(names, progress)
               if (n.get("text"), p.get("text")) not in found]
        if not new and found:
            break
        found.extend(new)
        device.swipe(width // 2, height * 3 // 4, width // 2, height // 4)
        time.sleep(1)
    scroll_to_top(device)
    return found


def on_main_list(device):
    """Reorder Collections is a fragment over the main list, which stays in
    the hierarchy under it; only the main list has no Navigate up button"""
    root = device.dump_ui()
    return (device.find(root, res_id="main_activity_listview") is not None
            and device.find(root, desc="Navigate up") is None)


def go_to_main(device):
    for _ in range(4):
        dismiss_tips(device)
        if on_main_list(device):
            return True
        if not device.tap(desc="Navigate up", wait=2):
            device.key(4)
            time.sleep(2)
    return False


def usable_bottom(device):
    """y above which a tap can't land on the navigation bar"""
    width, height = device.screen_size()
    bar = insets.system_bars(device.shell("dumpsys", "window", check=False)).get("navigationBars")
    # A bar along the bottom edge; in landscape it can sit at the side instead
    return bar[1] if bar and bar[1] > height // 2 else height


def tap_main_item(device, text):
    """Tap a main-list item, scrolling down from the top until it's clear of
    the navigation bar: where the list draws under the bar, a tap there would
    hit the bar"""
    width, height = device.screen_size()
    limit = usable_bottom(device)
    scroll_to_top(device)
    for _ in range(5):
        node = device.find(text=text)
        if node is not None:
            top, bottom = bounds(node)[1], bounds(node)[3]
            if (top + bottom) // 2 < limit - 10:
                device.tap_node(node)
                time.sleep(2)
                return True
        device.swipe(width // 2, height * 3 // 4, width // 2, height // 4)
        time.sleep(1)
    return False


def import_backup(device, json_name, csv_name, debug_dir=None):
    """Import a backup through the system file picker, preferring the JSON one.
    Returns (format imported or None, whether the picker greyed the JSON out)"""
    for _ in range(3):
        if not tap_main_item(device, "Import Collection"):
            raise SdkError("'Import Collection' not found on the main list")
        time.sleep(1)
        # A tap during a scroll or while the activity is recreated is lost
        if not on_main_list(device):
            break
    device.tap(text="Pick Back-Up File", wait=3)  # older releases ask where to import from
    json_greyed = False
    for attempt in range(6):
        if device.has(text=json_name):
            device.tap(text=json_name, wait=3)
            if not device.has(text=json_name):
                return "json", json_greyed
            # Still in the picker: this release's picker greys JSON backups out
            json_greyed = True
            if device.tap(text=csv_name, wait=3):
                return "csv", json_greyed
            break
        # Older pickers hide device storage behind the overflow menu
        if attempt >= 1 and device.tap(desc="More options", wait=1):
            if not (device.tap(text="Show SD card", wait=1) or device.tap(text="Show internal storage", wait=1)):
                device.key(4)
        if not (device.tap(desc="Show roots", wait=2) or device.tap(desc="Open navigation drawer", wait=2)):
            device.tap(desc="Navigate up", wait=2)
        if attempt >= 1:
            if not device.tap(pattern=r"SDCARD|Internal storage|Internal shared storage|.*sdk_gphone.*|"
                                      r"Android SDK.*|SD card", wait=2):
                device.tap(text="Downloads", wait=2)
        else:
            device.tap(text="Downloads", wait=2)
        device.tap(text="Download", wait=2)
    if debug_dir is not None:
        device.screenshot(debug_dir / "import-failed.png")
        device.dump_ui(debug_dir / "import-failed.xml")
    for _ in range(3):
        device.key(4)
    return None, json_greyed


def finish_import(device, package):
    """Confirm replacing existing collections, then wait for the import"""
    device.tap(text="Yes", wait=2)
    time.sleep(8)
    dismiss_tips(device)
    if not go_to_main(device):
        launch(device, package)


# --- Database -----------------------------------------------------------------------

def pull_database(device, package, outdir):
    """Pull the app's database (google_apis images allow adb root) and dump
    every collection to JSON"""
    device.shell("am", "force-stop", package, check=False)
    device.adb("root", check=False)
    time.sleep(3)
    device.adb("wait-for-device", timeout=120)
    dbdir = outdir / "database"
    dbdir.mkdir()
    for suffix in ("", "-wal", "-shm", "-journal"):
        device.adb("pull", f"/data/data/{package}/databases/CoinCollection{suffix}", str(dbdir), check=False)
    db = dbdir / "CoinCollection"
    if not db.exists():
        raise SdkError("couldn't pull the database (adb root needs a google_apis image)")
    con = sqlite3.connect(str(db))
    con.row_factory = sqlite3.Row
    dump = {"version": con.execute("PRAGMA user_version").fetchone()[0], "collections": {}}
    for info in con.execute("SELECT * FROM collection_info ORDER BY name"):
        name = info["name"]
        rows = con.execute(f"SELECT * FROM [{name.replace(']', '')}] ORDER BY sortOrder").fetchall()
        dump["collections"][name] = {
            "info": {k: info[k] for k in info.keys() if k != "_id"},
            "coins": [{k: r[k] for k in r.keys() if k != "_id"} for r in rows],
        }
    con.close()
    (outdir / "database.json").write_text(json.dumps(dump, indent=1, sort_keys=True), encoding="utf-8")
    return dump


def compare_databases(dump, reference):
    """Differences between two database dumps, at most a few per collection"""
    diffs = []
    if dump["version"] != reference["version"]:
        diffs.append(f"database version {dump['version']} vs {reference['version']}")
    for name in sorted(set(dump["collections"]) | set(reference["collections"])):
        mine, theirs = dump["collections"].get(name), reference["collections"].get(name)
        if mine is None or theirs is None:
            diffs.append(f"{name}: {'missing' if mine is None else 'unexpected'}")
            continue
        for key in sorted(set(mine["info"]) | set(theirs["info"])):
            if mine["info"].get(key) != theirs["info"].get(key):
                diffs.append(f"{name}: {key} {mine['info'].get(key)!r} vs {theirs['info'].get(key)!r}")
        if len(mine["coins"]) != len(theirs["coins"]):
            diffs.append(f"{name}: {len(mine['coins'])} coins vs {len(theirs['coins'])}")
        for index, (a, b) in enumerate(zip(mine["coins"], theirs["coins"])):
            if a != b:
                changed = {k: (a.get(k), b.get(k)) for k in set(a) | set(b) if a.get(k) != b.get(k)}
                diffs.append(f"{name}: coin {index} {changed}")
                break
    return diffs


# --- Screens --------------------------------------------------------------------------

def capture(device, package, outdir, name, results):
    time.sleep(1)
    root = device.dump_ui(outdir / f"{name}.xml")
    window = device.shell("dumpsys", "window", check=False)
    (outdir / f"{name}.window.txt").write_text(window, encoding="utf-8")
    device.screenshot(outdir / f"{name}.png")
    bars, problems = insets.overlaps(root, window, package)
    results[name] = {"bars": bars, "problems": problems}
    if not bars:
        results[name]["problems"] = ["no system bar rectangles found in dumpsys window"]


def check_screens(device, package, collection, outdir):
    """Screenshot every main screen, in portrait and landscape, and check that
    no app element is under a system bar"""
    shots = outdir / "screens"
    shots.mkdir()
    results = {}
    width, height = device.screen_size()
    device.shell("settings", "put", "system", "accelerometer_rotation", "0", check=False)
    device.shell("settings", "put", "system", "user_rotation", "0", check=False)
    go_to_main(device)
    capture(device, package, shots, "01-main", results)
    for _ in range(3):
        device.swipe(width // 2, height * 3 // 4, width // 2, height // 4)
    capture(device, package, shots, "02-main-bottom", results)
    for _ in range(3):
        device.swipe(width // 2, height // 4, width // 2, height * 3 // 4)

    tap_main_item(device, collection)
    dismiss_tips(device)
    capture(device, package, shots, "03-grid-top", results)
    for _ in range(8):
        device.swipe(width // 2, height * 3 // 4, width // 2, height // 4)
    capture(device, package, shots, "04-grid-bottom", results)
    coin = device.find(res_id="coinImage")
    if coin is not None:
        device.tap_node(coin, long_press=True)
        time.sleep(1)
        capture(device, package, shots, "05-coin-actions", results)
        device.key(4)
        time.sleep(1)

    # Advanced view with an unsaved edit, so the "Unsaved Changes" bar shows
    device.tap(desc="More options", wait=1)
    if device.tap(text="Advanced View", wait=2):
        dismiss_tips(device)
        device.tap(text="Edit", wait=2)
        for attempt in range(3):
            device.tap(res_id="notes_edit_text", wait=2)
            device.type_text("matrix")
            time.sleep(1)
            if device.has(res_id="unsaved_message_textview"):
                break
        capture(device, package, shots, "06-advanced-keyboard", results)
        # Close the keyboard the way a user would: from API 33 an injected
        # KEYCODE_BACK skips the system's back-callback dispatch
        if device.sdk >= 33:
            device.shell("cmd", "overlay", "enable", "com.android.internal.systemui.navbar.gestural", check=False)
            time.sleep(3)
            device.back_gesture()
        else:
            device.key(4)
        time.sleep(2)
        capture(device, package, shots, "07-advanced-unsaved", results)
        problems = results["07-advanced-unsaved"]["problems"]
        # Back with the keyboard open must close only the keyboard
        if device.has(pattern=r"Leaving this page.*"):
            problems.append("Back with the keyboard open showed the leave dialog instead of closing the keyboard")
            device.tap(text="Cancel", wait=1)
        elif device.keyboard_shown():
            problems.append("Back didn't close the keyboard")
        # uiautomator doesn't report a view drawn entirely off the screen, so
        # check the "Unsaved Changes" bar's position directly
        unsaved = device.find(res_id="unsaved_message_textview")
        if unsaved is None or bounds(unsaved)[3] > usable_bottom(device):
            problems.append("the \"Unsaved Changes\" bar isn't fully visible above the navigation bar")
        device.tap(desc="Navigate up", wait=1)
        capture(device, package, shots, "08-unsaved-dialog", results)
        device.tap(text="Okay", wait=2)
    go_to_main(device)

    if tap_main_item(device, "Reorder Collections"):
        dismiss_tips(device)
        capture(device, package, shots, "09-reorder", results)
    go_to_main(device)
    if tap_main_item(device, "New Collection"):
        dismiss_tips(device)
        capture(device, package, shots, "10-new-collection", results)
    go_to_main(device)

    device.shell("settings", "put", "system", "user_rotation", "1", check=False)
    time.sleep(3)
    dismiss_tips(device)
    capture(device, package, shots, "11-main-landscape", results)
    if tap_main_item(device, collection):
        dismiss_tips(device)
        capture(device, package, shots, "12-collection-landscape", results)
    device.shell("settings", "put", "system", "user_rotation", "0", check=False)
    go_to_main(device)
    contact_sheet(shots)
    return results


def contact_sheet(shots):
    """Tile the screenshots into one image, if Pillow is installed"""
    try:
        from PIL import Image, ImageDraw
    except ImportError:
        return
    tiles = []
    for png in sorted(shots.glob("*.png")):
        image = Image.open(png)
        image = image.resize((max(1, image.width * 520 // image.height), 520))
        tile = Image.new("RGB", (image.width, 540), "white")
        tile.paste(image, (0, 20))
        ImageDraw.Draw(tile).text((4, 4), png.stem, fill="black")
        tiles.append(tile)
    if tiles:
        sheet = Image.new("RGB", (sum(t.width for t in tiles), 540), "white")
        x = 0
        for tile in tiles:
            sheet.paste(tile, (x, 0))
            x += tile.width
        sheet.save(shots.parent / "screens.png")


# --- upgrade ----------------------------------------------------------------------------

def upgrade_level(args):
    seed_json, seed_csv = args.seed, args.seed.with_suffix(".csv")
    json_name, csv_name = "api-matrix-seed.json", "api-matrix-seed.csv"

    def run_level(level, device, outdir):
        result = {"android": device.getprop("ro.build.version.release"), "problems": []}
        device.shell("mkdir", "-p", "/sdcard/Download", check=False)
        device.adb("push", str(seed_json), f"/sdcard/Download/{json_name}")
        if seed_csv.exists():
            device.adb("push", str(seed_csv), f"/sdcard/Download/{csv_name}")
        device.shell("am", "broadcast", "-a", "android.intent.action.MEDIA_SCANNER_SCAN_FILE",
                     "-d", f"file:///sdcard/Download/{json_name}", check=False)

        log(level, f"installing the previous release ({args.old.name})")
        device.install(args.old)
        launch(device, args.package)
        imported, greyed = import_backup(device, json_name, csv_name, outdir)
        if imported is None:
            raise SdkError("couldn't import the seed backup with the previous release")
        finish_import(device, args.package)
        result["seed"] = imported + (" (JSON greyed out)" if greyed else "")
        result["before"] = collections(device)
        log(level, f"seed imported as {result['seed']}: {len(result['before'])} collections")

        log(level, f"upgrading to {args.new.name}")
        device.adb("logcat", "-c", check=False)
        device.install(args.new, replace=True)
        launch(device, args.package)
        result["after"] = collections(device)
        log(level, f"after the upgrade: {result['after']}")
        if len(result["after"]) != len(result["before"]):
            result["problems"].append(f"{len(result['before'])} collections before the upgrade, "
                                      f"{len(result['after'])} after")
        result["database"] = pull_database(device, args.package, outdir)
        launch(device, args.package)

        log(level, "checking screens")
        collection = result["after"][0][0] if result["after"] else None
        result["screens"] = check_screens(device, args.package, collection, outdir) if collection else {}
        for name, screen in result["screens"].items():
            result["problems"].extend(f"{name}: {p}" for p in screen["problems"])

        log(level, "importing the JSON backup with the new release")
        imported, greyed = import_backup(device, json_name, csv_name, outdir)
        result["new_import"] = imported if imported else "failed"
        if imported != "json":
            result["problems"].append("the new release couldn't import the JSON backup"
                                      + (" (greyed out in the picker)" if greyed else ""))
        else:
            finish_import(device, args.package)
            count = len(collections(device))
            if count != len(result["before"]):
                result["problems"].append(f"JSON import gave {count} collections, expected {len(result['before'])}")

        crashes = device.adb("logcat", "-d", "-b", "crash", check=False).stdout
        result["crashes"] = [line for line in crashes.splitlines() if args.package in line][:5]
        if result["crashes"]:
            result["problems"].append("crash: " + result["crashes"][0])
        return result

    return run_level


def cmd_upgrade(args):
    results = for_each_level(args, upgrade_level(args))
    dumps = {level: r["database"] for level, r in results.items() if "database" in r}
    reference = max(dumps) if dumps else None
    for level, result in results.items():
        if level in dumps and level != reference:
            diffs = compare_databases(dumps[level], dumps[reference])
            result["problems"].extend(f"database differs from API {reference}: {d}" for d in diffs[:10])
    lines = ["# Upgrade across API levels", "",
             f"Previous release `{args.old.name}`, new release `{args.new.name}`, "
             f"seed `{args.seed.name}`." + (f" Databases are compared with API {reference}'s." if reference else ""), "",
             "| API | Android | Seed imported as | Collections | Screens | New release imports JSON | Result |",
             "| --- | --- | --- | --- | --- | --- | --- |"]
    for level, r in results.items():
        if "error" in r:
            lines.append(f"| {level} | | | | | | ERROR: {r['error']} |")
            continue
        screens = r.get("screens", {})
        bad = sum(1 for s in screens.values() if s["problems"])
        lines.append(f"| {level} | {r['android']} | {r['seed']} | {len(r['before'])} → {len(r['after'])} | "
                     f"{len(screens) - bad}/{len(screens)} clean | {r.get('new_import', '')} | "
                     f"{'PASS' if not r['problems'] else 'FAIL'} |")
    for level, r in results.items():
        if r.get("problems"):
            lines += ["", f"## API {level}", ""] + [f"- {p}" for p in r["problems"]]
    lines += ["", "Each level's screenshots are in `api<level>/screens/` (`api<level>/screens.png` "
              "tiles them when Pillow is installed). Look through them as well: the overlap check "
              "only sees elements that uiautomator reports."]
    return finish(args, results, lines)


# --- instrumented ---------------------------------------------------------------------

def instrumented_level(args):
    gradlew = str(REPO_ROOT / ("gradlew.bat" if WINDOWS else "gradlew"))

    def run_level(level, device, outdir):
        excluded = ",".join(["com.spencerpages.ScreenshotsUITest", *args.exclude_class])
        command = [gradlew, "connectedAndroidDebugAndroidTest",
                   f"-Pandroid.testInstrumentationRunnerArguments.notClass={excluded}",
                   f"-Pandroid.testInstrumentationRunnerArguments.timeout_msec={args.test_timeout * 1000}"]
        command += args.gradle_arg
        results_dirs = [REPO_ROOT / "app" / "build" / sub
                        for sub in ("reports/androidTests/connected", "outputs/androidTest-results/connected")]
        for stale in results_dirs:  # left by the previous level
            shutil.rmtree(str(stale), ignore_errors=True)
        log(level, "running the instrumented tests")
        env = dict(os.environ, ANDROID_SERIAL=device.serial)
        problems = []
        with open(outdir / "gradle.log", "w", encoding="utf-8") as gradle_log:
            process = subprocess.Popen(command, cwd=str(REPO_ROOT), env=env, stdout=gradle_log,
                                       stderr=subprocess.STDOUT)
            try:
                code = process.wait(timeout=args.level_timeout * 60)
            except subprocess.TimeoutExpired:
                # A test that hangs outside its own timeout (in a rule's
                # teardown, say) would otherwise stall every later level
                process.kill()
                process.wait()
                code = None
        for source in results_dirs:
            if source.exists():
                shutil.copytree(str(source), str(outdir / source.parent.name.replace("androidTest-results", "results")),
                                dirs_exist_ok=True)
        tests, failures = 0, []
        for xml in outdir.rglob("TEST-*.xml"):
            for case in ET.parse(xml).getroot().iter("testcase"):
                tests += 1
                if case.find("failure") is not None or case.find("error") is not None:
                    failures.append(f"{case.get('classname')}.{case.get('name')}")
        if not tests:
            # No XML when the run was killed: take the failures from the log
            text = (outdir / "gradle.log").read_text(encoding="utf-8", errors="replace")
            failures = sorted(set(re.findall(r"^(\S+ > \S+?)\[.*\bFAILED", re.sub(r"\x1b\[[0-9;]*m", "", text), re.M)))
        problems += [f"failed: {name}" for name in failures]
        if code is None:
            started = re.findall(r"started: (\S+)", device.shell("logcat", "-d", "-s", "TestRunner", check=False))
            problems.append(f"stopped after {args.level_timeout} minutes"
                            + (f"; the last test started was {started[-1]}" if started else ""))
        elif code != 0 and not failures:
            problems.append(f"Gradle exited with {code}: see gradle.log")
        log(level, f"{tests or 'unknown number of'} tests, {len(failures)} failed")
        return {"android": device.getprop("ro.build.version.release"), "tests": tests, "problems": problems}

    return run_level


def cmd_instrumented(args):
    results = for_each_level(args, instrumented_level(args))
    lines = ["# Instrumented tests across API levels", "",
             "| API | Android | Tests | Failed | Result |", "| --- | --- | --- | --- | --- |"]
    for level, r in results.items():
        if "error" in r:
            lines.append(f"| {level} | | | | ERROR: {r['error']} |")
        else:
            failed = sum(1 for p in r["problems"] if p.startswith("failed:"))
            lines.append(f"| {level} | {r['android']} | {r['tests']} | {failed} | "
                         f"{'PASS' if not r['problems'] else 'FAIL'} |")
    for level, r in results.items():
        if r.get("problems"):
            lines += ["", f"## API {level}", ""] + [f"- {p}" for p in r["problems"]]
    lines += ["", "Each level's HTML report is in `api<level>/androidTests/`, and its Gradle output in "
              "`api<level>/gradle.log`."]
    return finish(args, results, lines)


def finish(args, results, lines):
    (args.out / "report.md").write_text("\n".join(lines) + "\n", encoding="utf-8")
    (args.out / "results.json").write_text(json.dumps({str(k): v for k, v in results.items()},
                                                      indent=1, default=str), encoding="utf-8")
    print("\n".join(lines))
    print(f"\nReport: {args.out / 'report.md'}")
    return 1 if any(r.get("error") or r.get("problems") for r in results.values()) else 0


def main():
    # Windows consoles may not encode every character in the report
    sys.stdout.reconfigure(errors="replace")
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    sub = parser.add_subparsers(dest="command", required=True)
    common = argparse.ArgumentParser(add_help=False)
    common.add_argument("--levels", type=int, nargs="+", default=default_levels(),
                        help="API levels to run (default: minSdk, 23, 26, 29, 33, 35, targetSdk)")
    common.add_argument("--out", type=Path, help="results directory (default: build/api-matrix/<command>)")
    common.add_argument("--create-avds", action="store_true",
                        help="install system images and create missing api-matrix-<level> AVDs")
    common.add_argument("--abi", default=host_abi(), help="system image ABI (default: the host's)")
    common.add_argument("--port", type=int, default=5580, help="emulator console port")
    common.add_argument("--emulator-arg", action="append", default=[],
                        help="extra emulator option, e.g. --emulator-arg=-gpu --emulator-arg=swiftshader_indirect")

    upgrade = sub.add_parser("upgrade", parents=[common], help="upgrade from the previous release and check it")
    upgrade.add_argument("--old", type=Path, required=True, help="the previous release's APK")
    upgrade.add_argument("--new", type=Path, required=True, help="the new release's APK, signed with the same key")
    upgrade.add_argument("--seed", type=Path, default=SEED_DIR / "seed.json",
                         help="JSON backup to import (a .csv with the same name is the fallback for "
                              "pickers that grey JSON out)")
    upgrade.add_argument("--package", default="com.spencerpages", help="application ID of the APKs")

    instrumented = sub.add_parser("instrumented", parents=[common], help="run the instrumented test suite")
    instrumented.add_argument("--test-timeout", type=int, default=180,
                              help="seconds before a single test counts as failed (default 180)")
    instrumented.add_argument("--level-timeout", type=int, default=75,
                              help="minutes before a level's whole run is stopped (default 75)")
    instrumented.add_argument("--exclude-class", action="append", default=[],
                              help="test class to leave out, e.g. com.coincollection.DialogRestoreTests")
    instrumented.add_argument("--gradle-arg", action="append", default=[], help="extra Gradle argument")

    args = parser.parse_args()
    args.out = (args.out or REPO_ROOT / "build" / "api-matrix" / args.command).resolve()
    args.out.mkdir(parents=True, exist_ok=True)
    if args.command == "upgrade":
        args.old, args.new, args.seed = args.old.resolve(), args.new.resolve(), args.seed.resolve()
        return cmd_upgrade(args)
    return cmd_instrumented(args)


if __name__ == "__main__":
    sys.exit(main())
