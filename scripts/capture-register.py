"""Test register page with TextInput interactivity."""
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

    # Go to register
    for attempt in range(3):
        try:
            page.goto("http://localhost:8081/register", wait_until="domcontentloaded", timeout=60000)
            page.wait_for_load_state("networkidle", timeout=60000)
            page.wait_for_timeout(3000)
            page.screenshot(path="d:/App/Appproject/mobile/register-textinput-1.png", full_page=False)
            print("Screenshot 1 saved (default state)")
            break
        except Exception as e:
            print(f"Attempt {attempt + 1}: {e}")
            page.wait_for_timeout(3000)

    # Fill account
    try:
        page.locator('input[placeholder*="账号"]').first.fill("panda_master")
        page.wait_for_timeout(500)
    except Exception as e:
        print(f"Fill account error: {e}")

    # Fill passwords
    try:
        page.locator('input[placeholder*="密码"]').first.fill("secret_123")
        page.locator('input[placeholder*="确认"]').first.fill("secret_123")
        page.wait_for_timeout(500)
    except Exception as e:
        print(f"Fill passwords error: {e}")

    page.screenshot(path="d:/App/Appproject/mobile/register-textinput-2.png", full_page=False)
    print("Screenshot 2 saved (filled)")

    # Click checkbox
    try:
        page.get_by_text("我已阅读").click()
        page.wait_for_timeout(500)
        page.screenshot(path="d:/App/Appproject/mobile/register-textinput-3.png", full_page=False)
        print("Screenshot 3 saved (checkbox checked)")
    except Exception as e:
        print(f"Checkbox click error: {e}")

    browser.close()