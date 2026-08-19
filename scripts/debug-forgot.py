"""Debug what's actually on the forgot page."""
from playwright.sync_api import sync_playwright

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    context = browser.new_context(
        viewport={"width": 412, "height": 917},
        device_scale_factor=2,
    )
    page = context.new_page()

    page.on("pageerror", lambda err: print(f"[PAGE ERROR] {err}"))
    page.on("console", lambda msg: print(f"[CONSOLE {msg.type}] {msg.text}"))

    try:
        page.goto("http://localhost:8081/forgot", wait_until="domcontentloaded", timeout=30000)
    except Exception as e:
        print(f"goto error: {e}")

    page.wait_for_timeout(5000)

    # Capture page HTML
    html = page.content()
    print("HTML length:", len(html))
    print("HTML excerpt:", html[:500])

    # Check inputs
    inputs = page.locator('input').count()
    print(f"Inputs count: {inputs}")

    # Take screenshot anyway
    page.screenshot(path="d:/App/Appproject/mobile/forgot-debug.png", full_page=False)
    print("Debug screenshot saved")

    browser.close()