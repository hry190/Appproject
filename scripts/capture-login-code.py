"""Verify login page in code mode with extended waits."""
from playwright.sync_api import sync_playwright

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    context = browser.new_context(
        viewport={"width": 412, "height": 917},
        device_scale_factor=2,
    )
    page = context.new_page()

    page.on("pageerror", lambda err: print(f"[PAGE ERROR] {err}"))

    # Warmup with longer wait
    for attempt in range(5):
        try:
            page.goto("http://localhost:8081/login", wait_until="domcontentloaded", timeout=30000)
            page.wait_for_load_state("networkidle", timeout=30000)
            page.wait_for_selector("text=验证码登录", timeout=15000)
            print(f"Warmup succeeded on attempt {attempt + 1}")
            break
        except Exception as e:
            print(f"Warmup attempt {attempt + 1}: {e}")
            page.wait_for_timeout(4000)

    page.wait_for_timeout(5000)  # give images time to decode

    page.screenshot(path="d:/App/Appproject/mobile/login-code-mode.png", full_page=False)
    print("Screenshot saved (验证码登录 mode)")

    browser.close()