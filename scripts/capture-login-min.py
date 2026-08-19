"""Capture login minimal version."""
import os
from playwright.sync_api import sync_playwright

out_path = os.path.join("D:", os.sep, "App", "Appproject", "mobile", "login-minimal.png")
print(f"Will save to: {out_path}")

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    context = browser.new_context(
        viewport={"width": 412, "height": 917},
        device_scale_factor=2,
    )
    page = context.new_page()

    for attempt in range(5):
        try:
            page.goto("http://localhost:8081/login", wait_until="domcontentloaded", timeout=60000)
            page.wait_for_load_state("networkidle", timeout=60000)
            page.wait_for_timeout(3500)
            page.screenshot(path=out_path, full_page=False)
            print(f"Saved {out_path}")
            break
        except Exception as e:
            print(f"Attempt {attempt + 1}: {e}")
            page.wait_for_timeout(3000)

    browser.close()