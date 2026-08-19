"""Capture one tab at a time with extended waits."""
import sys
from playwright.sync_api import sync_playwright

# Defaults
tab_path = "/"
out_filename = "tab-default.png"

# Parse args manually since shell parsing is tricky on Windows
for arg in sys.argv[1:]:
    if arg.startswith("/"):
        tab_path = arg
    elif arg.endswith(".png"):
        out_filename = arg

print(f"Tab: {tab_path}")
print(f"Output: {out_filename}")

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    context = browser.new_context(
        viewport={"width": 412, "height": 917},
        device_scale_factor=2,
    )
    page = context.new_page()

    # Warmup
    print("Warming up...")
    for attempt in range(3):
        try:
            page.goto("http://localhost:8081/login", wait_until="domcontentloaded", timeout=30000)
            page.wait_for_load_state("networkidle", timeout=30000)
            page.wait_for_timeout(2000)
            print("  Warmup OK")
            break
        except Exception as e:
            print(f"  Warmup retry: {e}")
            page.wait_for_timeout(3000)

    # Navigate to target tab
    print(f"Going to {tab_path}...")
    for attempt in range(3):
        try:
            page.goto(f"http://localhost:8081{tab_path}", wait_until="domcontentloaded", timeout=60000)
            page.wait_for_load_state("networkidle", timeout=60000)
            page.wait_for_timeout(3500)
            page.screenshot(path=f"d:/App/Appproject/mobile/{out_filename}", full_page=False)
            print(f"Saved {out_filename} (attempt {attempt + 1})")
            break
        except Exception as e:
            print(f"Attempt {attempt + 1}: {e}")
            page.wait_for_timeout(3000)

    browser.close()