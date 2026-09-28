# start-reverse.ps1 —— 一次性建好 USB 真机到电脑后端的端口反向映射
# 场景:Android 真机 USB 连电脑调试 App,后端跑在电脑 localhost:8010
# 用法:在 PowerShell 里 .\start-reverse.ps1  (要替换 <device_id> 为你的)

param(
    [string]$Device = "21908b7a",   # ← 改成你的 adb device id,或运行时传 -Device xxxxxx
    [int]$Port       = 8010
)

Write-Host "=== adb devices ==="
$devices = & adb devices
$deviceLines = $devices | Select-String "device$"
if (-not ($deviceLines -match [regex]::Escape($Device))) {
    Write-Host "✗ 设备 $Device 不在线" -ForegroundColor Red
    Write-Host "当前在线设备:"
    Write-Host $devices
    exit 1
}

Write-Host ""
Write-Host "=== 当前 reverse 映射 ==="
$current = & adb -s $Device reverse --list
Write-Host ($current -join "`n")

if ($current -match "tcp:$Port") {
    Write-Host ""
    Write-Host "✓ $Port 已经映射,不需要重建"
    exit 0
}

Write-Host ""
Write-Host "=== 建立 reverse tcp:$Port → tcp:$Port ==="
& adb -s $Device reverse tcp:$Port tcp:$Port

Write-Host ""
Write-Host "=== 验证 ==="
& adb -s $Device reverse --list
$test = & adb -s $Device shell "curl -s -o /dev/null -w '%{http_code}' http://127.0.0.1:$Port/docs"
Write-Host ""
Write-Host "device → 127.0.0.1:$Port/docs → HTTP $test"
if ($test -eq "200") {
    Write-Host "✓ reverse 链路通了" -ForegroundColor Green
} else {
    Write-Host "✗ 后端可能没起,跑 infra\start-dev.ps1" -ForegroundColor Red
}
