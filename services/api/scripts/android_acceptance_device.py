"""ADB observations/actions for the isolated acceptance package on an explicit emulator."""
import argparse
import os
from pathlib import Path
import re
import subprocess
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[3]
OUTPUT = ROOT / "artifacts" / "acceptance-20260908"
ADB = Path(os.environ["LOCALAPPDATA"]) / "Android/Sdk/platform-tools/adb.exe"
PACKAGE = "com.jueqiao.jianghu.acceptance"


def adb(*args):
    return subprocess.run([str(ADB), "-s", "emulator-5556", *args],
                          check=True, capture_output=True).stdout


def observe(name):
    OUTPUT.mkdir(parents=True, exist_ok=True)
    adb("shell", "uiautomator", "dump", "/sdcard/acceptance.xml")
    raw = adb("shell", "cat", "/sdcard/acceptance.xml")
    (OUTPUT / f"{name}.xml").write_bytes(raw)
    (OUTPUT / f"{name}.png").write_bytes(adb("exec-out", "screencap", "-p"))
    tree = ET.fromstring(raw)
    for node in tree.iter("node"):
        if node.get("package") == PACKAGE and (node.get("text") or node.get("content-desc")
                                                or node.get("clickable") == "true"):
            print(node.get("text") or node.get("content-desc") or node.get("class"),
                  node.get("bounds"), "disabled" if node.get("enabled") == "false" else "",
                  "FOCUSED" if node.get("focused") == "true" else "")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("action", choices=["observe", "tap", "input", "back", "swipe"])
    parser.add_argument("value", nargs="?", default="")
    parser.add_argument("--name", default="current")
    args = parser.parse_args()
    if args.action == "tap":
        # Coordinates must come from a preceding observed tree/screenshot.
        coords = args.value.split(",")
        assert len(coords) == 2 and all(re.fullmatch(r"\d+", c) for c in coords)
        adb("shell", "input", "tap", *coords)
    elif args.action == "input":
        assert re.fullmatch(r"[A-Za-z0-9 !.,%-]+", args.value)
        adb("shell", "input", "text", args.value.replace(" ", "%s"))
    elif args.action == "back":
        adb("shell", "input", "keyevent", "4")
    elif args.action == "swipe":
        coords = args.value.split(",")
        assert len(coords) == 4 and all(re.fullmatch(r"\d+", c) for c in coords)
        adb("shell", "input", "swipe", *coords, "450")
    observe(args.name)


if __name__ == "__main__":
    main()
