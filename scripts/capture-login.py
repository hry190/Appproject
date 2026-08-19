"""Capture two screenshots of login page: default and after switching tabs."""
from playwright.sync_api import sync_playwright

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    context = browser.new_context(
        viewport={"width": 412, "height": 917},
        device_scale_factor=2,
    )
    page = context.new_page()

    # Warm up - try several times since metro bundling can be slow
    for attempt in range(3):
        try:
            page.goto("http://localhost:8081/login", wait_until="domcontentloaded", timeout=60000)
            break
        except Exception as e:
            print(f"Attempt {attempt + 1} failed: {e}")
            page.wait_for_timeout(3000)

    # Wait for full bundle to load
    page.wait_for_load_state("networkidle", timeout=60000)
    page.wait_for_timeout(3000)

    # Screenshot 1: default state (验证码登录 active, on left with sage green bg)
    page.screenshot(path="d:/App/Appproject/mobile/login-tab-code.png", full_page=False)
    print("Screenshot 1 saved (default: 验证码登录 active)")

    # Click 密码登录 tab
    page.get_by_text("密码登录", exact=True).first.click()
    page.wait_for_timeout(800)
    page.screenshot(path="d:/App/Appproject/mobile/login-tab-password.png", full_page=False)
    print("Screenshot 2 saved (after switch: 密码登录 active)")

    browser.close()