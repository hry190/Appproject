"""Capture one tab at a time - hardcoded paths to avoid Windows shell arg parsing issues."""
from playwright.sync_api import sync_playwright

# Hardcoded: tab path -> output filename
JOBS = [
    ("/", "tab-1-jianghu.png"),
    ("/xiulian", "tab-2-xiulian.png"),
    ("/zaowu", "tab-3-zaowu.png"),
    ("/dahui", "tab-4-dahui.png"),
    ("/xingnang", "tab-5-xingnang.png"),
]

with sync_playwright() as p:
    browser = p.chromium.launch(headless=True)
    context = browser.new_context(
        viewport={"width": 412, "height": 917},
        device_scale_factor=2,
    )
    page = context.new_page()

    # Warmup
    print("Warming up metro bundler...")
    for attempt in range(5):
        try:
            page.goto("http://localhost:8081/login", wait_until="domcontentloaded", timeout=30000)
            page.wait_for_load_state("networkidle", timeout=30000)
            page.wait_for_timeout(2000)
            print(f"  Warmup OK on attempt {attempt + 1}")
            break
        except Exception as e:
            print(f"  Warmup retry {attempt + 1}: {e}")
            page.wait_for_timeout(3000)

    # Capture each tab
    for tab_path, filename in JOBS:
        print(f"Capturing {tab_path} -> {filename}")
        success = False
        for attempt in range(3):
            try:
                page.goto(f"http://localhost:8081{tab_path}", wait_until="domcontentloaded", timeout=60000)
                page.wait_for_load_state("networkidle", timeout=60000)
                page.wait_for_timeout(4000)
                page.screenshot(path=f"d:/App/Appproject/mobile/{filename}", full_page=False)
                print(f"  Saved {filename}")
                success = True
                break
            except Exception as e:
                print(f"  Attempt {attempt + 1} failed: {e}")
                page.wait_for_timeout(3000)
        if not success:
            print(f"  Failed to capture {tab_path}")

    browser.close()