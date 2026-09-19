# ⚠️ 已废弃(DEPRECATED) 2026-09-19
# ---------------------------------------------------------------------------
# 本脚本属于【已移除的 React Native / Expo 前端】:它用 Playwright 打开
# http://localhost:8081 —— 那是 Metro bundler 的端口,只有 RN 开发服务器会监听。
# 当前项目只有 Android(Compose)客户端,本脚本已无法运行,仅作历史留档。
#
# 现在要给 Android 界面截图,请改用 adb,例如:
#   adb -s <serial> shell screencap -p /sdcard/s.png
#   adb -s <serial> pull /sdcard/s.png .
# ---------------------------------------------------------------------------

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