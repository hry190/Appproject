# setup-reverse.ps1 —— 重建 adb reverse 映射(设备 127.0.0.1:<port> → 主机 127.0.0.1:<port>)
#
# 为什么需要这个脚本:
#   `adb kill-server` / USB 插拔 / 机器重启 都会【清空】reverse 列表,
#   而 Android app 的 AUTH_BASE_URL 指向 127.0.0.1:8010 —— reverse 一没,app 就连不上后端
#   (表现为"暂时无法连接江湖驿站",或启动时 bootstrap 失败跳登录页)。
#   截至 2026-09-18 已踩 4 次(SESSION-LOG 09-17 §23 / §34,09-18 §1)。
#
# 设计原则(修掉旧 infra/adb-reverse.ps1 被删的 4 个缺陷):
#   1. 不硬编码 adb 路径 —— 先查 PATH,再读 android/local.properties 的 sdk.dir
#   2. 不依赖机型名  —— 用 Serial 显式指定,或"只有一个在线设备"时自动选;多设备时报错要求 -Serial
#   3. 失败【一定】返回非零退出码 —— 杜绝"静默失效"
#   4. 建完【必须验证】 —— 检查 reverse --list 真含该映射(并用 HTTP 探活当加分项)
#   5. §7:已设就跳过(快速路径)—— `adb reverse --list` 查到目标映射直接 exit 0,
#           Before launch 从 ~1s 降到 <100ms。reverse 真被清空时才走完整路径。
#
# 用法:
#   .\scripts\setup-reverse.ps1                 # 自动选唯一在线设备,端口默认 8010
#   .\scripts\setup-reverse.ps1 -Serial 21908b7a
#   .\scripts\setup-reverse.ps1 -Port 8010 -SkipHttpCheck
#   .\scripts\setup-reverse.ps1 -Quiet          # 静默模式(仅出错时输出)—— 适合挂在 build 后

[CmdletBinding()]
param(
    [int]    $Port          = 8010,
    [string] $Serial        = "",
    [switch] $SkipHttpCheck,
    [switch] $Quiet
)

$ErrorActionPreference = "Stop"

function Say([string]$msg, [string]$color = "Gray") {
    if (-not $Quiet) { Write-Host $msg -ForegroundColor $color }
}
function Fail([string]$msg) {
    # 无论 Quiet 与否,失败【必须】可见
    Write-Host "✗ setup-reverse 失败: $msg" -ForegroundColor Red
    exit 1
}

# ── 1. 定位 adb ─────────────────────────────────────────────────────────────
$adb = $null

$cmd = Get-Command adb -ErrorAction SilentlyContinue
if ($cmd) { $adb = $cmd.Source }

if (-not $adb) {
    # 回退:读 android/local.properties 的 sdk.dir
    $repoRoot = Split-Path -Parent $PSScriptRoot
    $localProps = Join-Path $repoRoot "android\local.properties"
    if (Test-Path $localProps) {
        $line = Select-String -Path $localProps -Pattern '^\s*sdk\.dir\s*=' | Select-Object -First 1
        if ($line) {
            $sdkDir = ($line.Line -split '=', 2)[1].Trim()
            # local.properties 里 Windows 路径是转义的:sdk.dir=D\:\\Android\\Sdk
            $sdkDir = $sdkDir -replace '\\\\', '\' -replace '\\:', ':'
            $candidate = Join-Path $sdkDir "platform-tools\adb.exe"
            if (Test-Path $candidate) { $adb = $candidate }
        }
    }
}

if (-not $adb) {
    Fail "找不到 adb。请把 platform-tools 加进 PATH,或在 android/local.properties 里配好 sdk.dir"
}
Say "adb: $adb"

# ── 2. 选设备 ───────────────────────────────────────────────────────────────
$deviceLines = & $adb devices | Select-Object -Skip 1 | Where-Object { $_ -match '\S' }
$online = @()
foreach ($l in $deviceLines) {
    $parts = $l -split '\s+'
    if ($parts.Count -ge 2 -and $parts[1] -eq 'device') {
        $online += [PSCustomObject]@{ Serial = $parts[0] }
    }
}

if ($Serial -ne "") {
    if (-not ($online | Where-Object { $_.Serial -eq $Serial })) {
        $all = (& $adb devices | Select-Object -Skip 1 | Where-Object { $_ -match '\S' }) -join '; '
        Fail "指定的设备 '$Serial' 不在线。当前: $all"
    }
} else {
    if ($online.Count -eq 0) {
        Fail "没有在线设备。检查 USB 连接 + 手机上'允许 USB 调试'"
    }
    if ($online.Count -gt 1) {
        $list = ($online | ForEach-Object { $_.Serial }) -join ', '
        Fail "有 $($online.Count) 台在线设备($list),必须用 -Serial 指定(避免设到错误的设备上)"
    }
    $Serial = $online[0].Serial
}
Say "设备: $Serial" "Green"

# ── 3. 建立 reverse(快速路径:已设就跳过)──────────────────────────────
# §7 优化:每次 Run 前都跑这个脚本,如果 reverse 没被清空就没必要重设 + 验证 + 探活。
#   收益:Before launch 从 ~1s 降到 <100ms(只跑一次 adb reverse --list)。
#   行为:已设 → Quiet 下 0 输出 / 非 Quiet 下打一行 "已就绪" → exit 0。
$existing = (& $adb -s $Serial reverse --list) -join "`n"
if ($existing -match [regex]::Escape("tcp:$Port tcp:$Port")) {
    Say "✓ adb reverse 就绪(快速路径:已设,跳过)" "Green"
    exit 0
}

& $adb -s $Serial reverse "tcp:$Port" "tcp:$Port" | Out-Null

# ── 4. 验证(必做 —— 这是防"静默失效"的关键)────────────────────────────────
$list = (& $adb -s $Serial reverse --list) -join "`n"
if ($list -notmatch [regex]::Escape("tcp:$Port tcp:$Port")) {
    Fail "reverse 建立后,列表里查不到 'tcp:$Port tcp:$Port'。实际列表: $list"
}
Say "reverse: tcp:$Port → tcp:$Port ✓" "Green"

# ── 5. HTTP 探活(加分项,不通过只警告不失败)────────────────────────────────
if (-not $SkipHttpCheck) {
    $code = & $adb -s $Serial shell "curl -s -o /dev/null -w '%{http_code}' --max-time 6 http://127.0.0.1:$Port/docs" 2>$null
    $code = "$code".Trim()
    if ($code -eq "200") {
        Say "设备侧探活: http://127.0.0.1:$Port/docs → 200 ✓" "Green"
    } else {
        # 后端可能没起 —— 这不算本脚本失败(reverse 本身是好的)
        Write-Host "⚠ 设备侧探活返回 '$code'(不是 200)。reverse 已建好,但后端可能没跑 ——" -ForegroundColor Yellow
        Write-Host "  起后端: cd infra; .\start-dev.ps1" -ForegroundColor Yellow
    }
}

Say "✓ adb reverse 就绪" "Green"
exit 0
