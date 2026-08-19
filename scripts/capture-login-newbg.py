"""Capture login page after bg change - with longer warmup."""
from playwright.sync_api import sync_playwright

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    context = browser.new_context(
        viewport={"width": 412, "height": 917},
        device_scale_factor=2,
    )
    page = context.new_page()

    # Multiple warmup retries for metro
    for attempt in range(5):
        try:
            page.goto("http://localhost:8081/login", wait_until="domcontentloaded", timeout=60000)
            page.wait_for_load_state("networkidle", timeout=60000)
            page.wait_for_selector("text=验证码登录", timeout=20000)
            page.wait_for_timeout(4000)
            out = "D:\\App\\Appproject\\mobile\\login-newbg.png"
            page.screenshot(path=out, full_page=False)
            print(f"Screenshot saved to {out} (attempt {attempt + 1})")
            break
        except Exception as e:
            print(f"Attempt {attempt + 1}: {e}")
            page.wait_for_timeout(3000)

    browser.close()