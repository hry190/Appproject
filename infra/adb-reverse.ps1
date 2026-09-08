# 智能 adb reverse:自动找到小米 K50 Pro,设端口转发
# 用法:.\adb-reverse.ps1
# 后端需先跑 .\infra\start-dev.ps1

# 列出所有 device
$devices = & "D:\Android\Sdk\platform-tools\adb.exe" devices | Select-String "device$"
Write-Host "=== 当前连接的 device ==="
$devices | ForEach-Object { Write-Host $_ }

# 找小米(K50 Pro 的 USB product 是 cupid)
$xiaomi = $devices | Select-String "cupid" | ForEach-Object { ($_ -split "`t")[0] }

if ($xiaomi) {
    Write-Host ""
    Write-Host "找到小米设备: $xiaomi"
    & "D:\Android\Sdk\platform-tools\adb.exe" -s $xiaomi reverse tcp:8010 tcp:8010
    Write-Host ""
    Write-Host "=== 当前 reverse 列表 ==="
    & "D:\Android\Sdk\platform-tools\adb.exe" reverse --list
    Write-Host ""
    Write-Host "✓ adb reverse 已设好,可以在 app 里登录了"
} else {
    Write-Host ""
    Write-Host "⚠ 没找到小米设备(cupid)"
    Write-Host "  - 确认 USB 数据线连好"
    Write-Host "  - 确认小米已开'开发者选项' + 'USB 调试'"
    Write-Host "  - 看上面 device 列表里有谁"
}
