"""Capture checkbox toggle - both states."""
from playwright.sync_api import sync_playwright

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    context = browser.new_context(
        viewport={"width": 412, "height": 917},
        device_scale_factor=2,
    )
    page = context.new_page()

    # Warmup
    for _ in range(3):
        try:
            page.goto("http://localhost:8081/login", wait_until="domcontentloaded", timeout=30000)
            page.wait_for_load_state("networkidle", timeout=30000)
            page.wait_for_timeout(1500)
            break
        except Exception:
            page.wait_for_timeout(3000)

    # Register page - default unchecked
    for attempt in range(3):
        try:
            page.goto("http://localhost:8081/register", wait_until="domcontentloaded", timeout=60000)
            page.wait_for_load_state("networkidle", timeout=60000)
            page.wait_for_timeout(3000)
            page.screenshot(path="d:/App/Appproject/mobile/register-checkbox-1.png", full_page=False)
            print("Screenshot 1 saved (unchecked)")
            break
        except Exception as e:
            print(f"Attempt {attempt + 1}: {e}")
            page.wait_for_timeout(3000)

    # Click checkbox
    try:
        # Find the checkbox by clicking near it
        page.mouse.click(70 + 118, 1240)  # relative coords: agreement at left:59 + 235 width... but viewport is 412
        page.wait_for_timeout(500)
    except Exception as e:
        print(f"Click error: {e}")

    # Try clicking via accessibility (more reliable)
    try:
        page.locator('[role="checkbox"]').first.click()
        page.wait_for_timeout(800)
        page.screenshot(path="d:/App/Appproject/mobile/register-checkbox-2.png", full_page=False)
        print("Screenshot 2 saved (checked)")
    except Exception as e:
        print(f"Checkbox click error: {e}")

    browser.close()