"""Capture forgot page with extensive warm-up retries."""
from playwright.sync_api import sync_playwright

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    context = browser.new_context(
        viewport={"width": 412, "height": 917},
        device_scale_factor=2,
    )
    page = context.new_page()

    # Warm up: hit /login first to trigger metro bundle compile
    print("Warming up metro bundler...")
    for attempt in range(5):
        try:
            page.goto("http://localhost:8081/login", wait_until="domcontentloaded", timeout=30000)
            page.wait_for_selector("text=验证码登录", timeout=15000)
            print(f"  Warmup {attempt + 1} succeeded")
            break
        except Exception as e:
            print(f"  Warmup {attempt + 1} failed: {e}")
            page.wait_for_timeout(3000)

    # Now try /forgot
    print("Loading /forgot...")
    for attempt in range(3):
        try:
            page.goto("http://localhost:8081/forgot", wait_until="domcontentloaded", timeout=60000)
            page.wait_for_load_state("networkidle", timeout=60000)
            page.wait_for_selector("input", timeout=15000)
            print(f"  /forgot attempt {attempt + 1} succeeded")
            break
        except Exception as e:
            print(f"  /forgot attempt {attempt + 1} failed: {e}")
            page.wait_for_timeout(3000)

    page.wait_for_timeout(2500)

    page.screenshot(path="d:/App/Appproject/mobile/forgot-1-page.png", full_page=False)
    print("Screenshot 1 saved (forgot page)")

    # Fill account + code + passwords
    inputs = page.locator('input').all()
    print(f"Found {len(inputs)} inputs")
    if len(inputs) >= 4:
        inputs[0].fill("panda_master")
        inputs[1].fill("123456")
        # Find password inputs (last 2 typically)
        pwd_inputs = [i for i in inputs if i.get_attribute('type') == 'password']
        if len(pwd_inputs) >= 2:
            pwd_inputs[0].fill("new_secret_123")
            pwd_inputs[1].fill("new_secret_123")
    page.wait_for_timeout(800)

    page.screenshot(path="d:/App/Appproject/mobile/forgot-2-filled.png", full_page=False)
    print("Screenshot 2 saved (all fields filled)")

    # Test login → forgot navigation
    page.goto("http://localhost:8081/login", wait_until="domcontentloaded", timeout=60000)
    page.wait_for_load_state("networkidle", timeout=60000)
    page.wait_for_selector("text=验证码登录", timeout=15000)
    page.wait_for_timeout(2000)

    page.get_by_text("密码登录", exact=True).first.click()
    page.wait_for_timeout(1000)

    page.screenshot(path="d:/App/Appproject/mobile/forgot-3-login-pwd.png", full_page=False)
    print("Screenshot 3 saved (login password mode - should show forgot link)")

    page.get_by_text("忘记密码？找回密码").first.click(timeout=10000)
    page.wait_for_timeout(2500)

    page.screenshot(path="d:/App/Appproject/mobile/forgot-4-after-click.png", full_page=False)
    print("Screenshot 4 saved (after clicking forgot link)")

    browser.close()