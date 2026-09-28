# diag.ps1 —— 江湖 App 连接 / 登录问题一键诊断
# 用法:.\diag.ps1              (默认查 21908b7a)
#      .\diag.ps1 -Device xxxxxxx

param([string]$Device = "21908b7a")

Write-Host "=== [1/5] 后端健康 ==="
try {
    $code = & "C:\Windows\System32\curl.exe" -s -o /dev/null -w "%{http_code}" http://127.0.0.1:8010/docs --max-time 5
    Write-Host "  127.0.0.1:8010/docs → HTTP $code"
} catch {
    Write-Host "  127.0.0.1:8010/docs → 后端无响应 (start-dev.ps1 没跑?)" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== [2/5] adb reverse ==="
$reverse = & adb -s $Device reverse --list 2>&1
if ($reverse -match "tcp:8010") {
    Write-Host "  ✓ reverse 已设"
    Write-Host ($reverse -join "`n  ")
} else {
    Write-Host "  ✗ 没设！跑 .\start-reverse.ps1 -Device $Device" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== [3/5] 手机视角访问后端 ==="
$fromPhone = & adb -s $Device shell "curl -s -o /dev/null -w '%{http_code}' --max-time 5 http://127.0.0.1:8010/docs" 2>&1
Write-Host "  device → 127.0.0.1:8010/docs → HTTP $fromPhone"

Write-Host ""
Write-Host "=== [4/5] local.properties ==="
$lp = Get-Content "D:\Appproject\android\local.properties" -ErrorAction SilentlyContinue
if ($lp) {
    $lp | Select-String "AUTH_BASE_URL" | ForEach-Object { Write-Host "  $_" }
} else {
    Write-Host "  ✗ local.properties 不存在" -ForegroundColor Red
}

Write-Host ""
Write-Host "=== [5/5] token 文件状态 ==="
$lsResult = & adb -s $Device shell "run-as com.jueqiao.jianghu ls -la shared_prefs/auth_session.xml" 2>&1
if ($LASTEXITCODE -eq 0 -and $lsResult -match "auth_session.xml") {
    Write-Host "  ✓ 文件存在:"
    Write-Host ("  " + ($lsResult -join "`n  "))
    Write-Host ""
    Write-Host "  文件内容:"
    $content = & adb -s $Device shell "run-as com.jueqiao.jianghu cat shared_prefs/auth_session.xml" 2>&1
    Write-Host ("  " + ($content -join "`n  "))
} else {
    Write-Host "  ✗ 没有 auth_session.xml(从未登录 / 已被 EncryptedTokenStore.clear() 清掉)" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "=== [结束] ==="
Write-Host "判定:"
Write-Host "  ①200 + ②有 + ③200 + ④127.0.0.1:8010 + ⑤存在  → 你看到的是 splash 没点"
Write-Host "  ①非 200                              → 后端死了,跑 start-dev.ps1"
Write-Host "  ②空                                  → reverse 丢了,跑 start-reverse.ps1"
Write-Host "  ①200 但 ③非 200                       → network_security_config 或后端白名单"
Write-Host "  ⑤文件不存在                            → 真要去登录(可能是症状 C 的 token 丢 bug)"
