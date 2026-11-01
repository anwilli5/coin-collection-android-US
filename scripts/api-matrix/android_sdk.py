"""Android SDK, emulator and on-device UI helpers for api_matrix.py.

Works on macOS, Linux and Windows: SDK tools are found through ANDROID_HOME,
ANDROID_SDK_ROOT or local.properties, and every command runs without a shell.
"""
import os
import platform
import re
import subprocess
import time
import xml.etree.ElementTree as ET
from pathlib import Path

REPO_ROOT = Path(__file__).resolve().parents[2]
WINDOWS = os.name == "nt"


class SdkError(RuntimeError):
    pass


def sdk_root():
    for var in ("ANDROID_HOME", "ANDROID_SDK_ROOT"):
        if os.environ.get(var):
            return Path(os.environ[var])
    props = REPO_ROOT / "local.properties"
    if props.exists():
        for line in props.read_text(encoding="utf-8").splitlines():
            if line.startswith("sdk.dir="):
                # local.properties escapes ':' and '\' on Windows
                return Path(line.split("=", 1)[1].replace("\\:", ":").replace("\\\\", "\\"))
    raise SdkError("Android SDK not found: set ANDROID_HOME or sdk.dir in local.properties")


def tool(*parts):
    """Path to an SDK executable, adding the Windows extension"""
    path = sdk_root().joinpath(*parts)
    if WINDOWS:
        for ext in (".exe", ".bat"):
            if path.with_name(path.name + ext).exists():
                return str(path.with_name(path.name + ext))
    return str(path)


def adb_path():
    return tool("platform-tools", "adb")


def host_abi():
    return "arm64-v8a" if platform.machine().lower() in ("arm64", "aarch64") else "x86_64"


def run(cmd, check=True, timeout=600, **kwargs):
    result = subprocess.run(cmd, capture_output=True, text=True, encoding="utf-8", errors="replace",
                            timeout=timeout, **kwargs)
    if check and result.returncode != 0:
        raise SdkError(f"{' '.join(cmd)} failed ({result.returncode}): {result.stderr.strip() or result.stdout.strip()}")
    return result


# --- AVDs ---------------------------------------------------------------------

def system_image_package(api, abi):
    """The google_apis system image for an API level. Newer levels are published
    as android-<api>.0, older ones as android-<api>"""
    listing = run([tool("cmdline-tools", "latest", "bin", "sdkmanager"), "--list"], timeout=300).stdout
    for name in (f"android-{api}", f"android-{api}.0"):
        # Newer sdkmanager versions list packages with '/' separators
        if re.search(rf"^\s*system-images[;/]{re.escape(name)}[;/]google_apis[;/]{abi}\s", listing, re.M):
            return f"system-images;{name};google_apis;{abi}"
    raise SdkError(f"No google_apis {abi} system image for API {api}")


def avd_exists(name):
    avds = run([tool("emulator", "emulator"), "-list-avds"]).stdout.split()
    return name in avds


def create_avd(name, api, abi):
    """Install the system image and create a Pixel 6 AVD with an SD card, which
    the API 21-23 file pickers need. google_apis images allow adb root, which
    pulling the app's database needs"""
    package = system_image_package(api, abi)
    run([tool("cmdline-tools", "latest", "bin", "sdkmanager"), package], input="y\n" * 10, timeout=1800)
    run([tool("cmdline-tools", "latest", "bin", "avdmanager"), "create", "avd", "-n", name, "-k", package,
         "-d", "pixel_6", "-c", "512M", "--force"], input="no\n")


# --- Emulators and devices ----------------------------------------------------

class Device:
    """One device, addressed by adb serial"""

    def __init__(self, serial, process=None):
        self.serial = serial
        self.process = process
        self.sdk = None

    @classmethod
    def boot(cls, avd, port, extra_args=(), log_path=None):
        args = [tool("emulator", "emulator"), "-avd", avd, "-port", str(port), "-no-window", "-no-audio",
                "-no-boot-anim", "-no-snapshot", "-wipe-data", *extra_args]
        log = open(log_path, "w", encoding="utf-8") if log_path else subprocess.DEVNULL
        process = subprocess.Popen(args, stdout=log, stderr=subprocess.STDOUT)
        device = cls(f"emulator-{port}", process)
        device.wait_for_boot()
        return device

    def wait_for_boot(self, timeout=900, settle=45):
        deadline = time.time() + timeout
        while time.time() < deadline:
            if self.process is not None and self.process.poll() is not None:
                raise SdkError(f"Emulator for {self.serial} exited with code {self.process.returncode}")
            result = self.adb("shell", "getprop", "sys.boot_completed", check=False, timeout=30)
            if result.stdout.strip() == "1":
                break
            time.sleep(5)
        else:
            raise SdkError(f"{self.serial} did not boot within {timeout}s")
        # A freshly booted system stays busy for a while; UI steps run then time out
        time.sleep(settle)
        self.sdk = int(self.getprop("ro.build.version.sdk"))
        for key in ("window_animation_scale", "transition_animation_scale", "animator_duration_scale"):
            self.shell("settings", "put", "global", key, "0")
        self.key("82")  # dismiss the lock screen

    def kill(self):
        self.adb("emu", "kill", check=False, timeout=60)
        if self.process is not None:
            try:
                self.process.wait(timeout=120)
            except subprocess.TimeoutExpired:
                self.process.kill()

    def adb(self, *args, check=True, timeout=600, **kwargs):
        return run([adb_path(), "-s", self.serial, *args], check=check, timeout=timeout, **kwargs)

    def shell(self, *args, check=True, timeout=120):
        return self.adb("shell", *args, check=check, timeout=timeout).stdout

    def getprop(self, name):
        return self.shell("getprop", name).strip()

    def key(self, code):
        self.shell("input", "keyevent", str(code), check=False)

    def install(self, apk, replace=False):
        args = ["install"] + (["-r"] if replace else []) + [str(apk)]
        out = self.adb(*args, check=False, timeout=600)
        if "Success" not in out.stdout:
            raise SdkError(f"install {apk} failed: {out.stdout.strip()} {out.stderr.strip()}")

    def screenshot(self, path):
        data = subprocess.run([adb_path(), "-s", self.serial, "exec-out", "screencap", "-p"],
                              capture_output=True, timeout=60).stdout
        Path(path).write_bytes(data)

    def keyboard_shown(self):
        windows = self.shell("dumpsys", "window", check=False)
        if re.search(r"InsetsSource[^\n]*type=(ime|ITYPE_IME)[^\n]*visible=true", windows):
            return True
        ime = self.shell("dumpsys", "input_method", check=False)
        match = re.search(r"mInputShown=(true|false)", ime)
        return bool(match and match.group(1) == "true")

    # --- UI through uiautomator -------------------------------------------

    def dump_ui(self, path=None):
        """The current window hierarchy as an ElementTree root"""
        for _ in range(3):
            self.shell("uiautomator", "dump", "/data/local/tmp/ui.xml", check=False, timeout=60)
            xml = self.adb("exec-out", "cat", "/data/local/tmp/ui.xml", check=False).stdout
            if xml.strip().startswith("<?xml"):
                if path:
                    Path(path).write_text(xml, encoding="utf-8")
                return ET.fromstring(xml)
            time.sleep(2)
        raise SdkError("uiautomator dump failed")

    @staticmethod
    def _matches(node, text=None, desc=None, res_id=None, pattern=None):
        if text is not None:
            return node.get("text", "").casefold() == text.casefold()
        if desc is not None:
            return node.get("content-desc", "").casefold() == desc.casefold()
        if res_id is not None:
            return node.get("resource-id", "").endswith("/" + res_id)
        if pattern is not None:
            return re.fullmatch(pattern, node.get("text", ""), re.S | re.I) is not None
        return False

    def find(self, root=None, **match):
        root = root if root is not None else self.dump_ui()
        for node in root.iter("node"):
            if self._matches(node, **match):
                return node
        return None

    def find_all(self, root=None, **match):
        root = root if root is not None else self.dump_ui()
        return [node for node in root.iter("node") if self._matches(node, **match)]

    def has(self, **match):
        return self.find(**match) is not None

    def tap_node(self, node, long_press=False):
        left, top, right, bottom = bounds(node)
        x, y = (left + right) // 2, (top + bottom) // 2
        if long_press:
            self.shell("input", "swipe", str(x), str(y), str(x), str(y), "1200")
        else:
            self.shell("input", "tap", str(x), str(y))

    def tap(self, wait=1.5, long_press=False, **match):
        node = self.find(**match)
        if node is None:
            return False
        self.tap_node(node, long_press)
        time.sleep(wait)
        return True

    def type_text(self, text):
        self.shell("input", "text", text.replace(" ", "%s"))

    def swipe(self, x1, y1, x2, y2, ms=200):
        self.shell("input", "swipe", str(x1), str(y1), str(x2), str(y2), str(ms))

    def screen_size(self):
        match = re.search(r"(\d+)x(\d+)", self.shell("wm", "size"))
        return int(match.group(1)), int(match.group(2))

    def back_gesture(self):
        """A user's Back: an edge swipe under gesture navigation. On API 33+ an
        injected KEYCODE_BACK skips the system's back-callback dispatch"""
        width, height = self.screen_size()
        self.swipe(2, height // 2, width * 2 // 5, height // 2, 250)


def bounds(node):
    return tuple(int(v) for v in re.findall(r"\d+", node.get("bounds", "[0,0][0,0]")))
