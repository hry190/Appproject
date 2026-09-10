param(
    [string]$ApkPath = (Join-Path $PSScriptRoot "..\android\app\build\outputs\apk\debug\app-debug.apk"),
    [switch]$WhatIf
)

$ErrorActionPreference = "Stop"
$adbPath = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
$resolvedApk = (Resolve-Path -LiteralPath $ApkPath).Path
$workspaceRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path

if (-not (Test-Path -LiteralPath $adbPath)) {
    throw "adb not found: $adbPath"
}
if (-not (Test-Path -LiteralPath $resolvedApk -PathType Leaf)) {
    throw "APK not found: $resolvedApk"
}
if (-not $resolvedApk.StartsWith($workspaceRoot, [StringComparison]::OrdinalIgnoreCase)) {
    throw "APK must be inside the project workspace: $resolvedApk"
}

$connectedDevices = @(
    (& $adbPath devices) |
        Select-String "^(?<id>[^\s]+)\s+device$" |
        ForEach-Object { $_.Matches[0].Groups["id"].Value }
)
if ($connectedDevices.Count -eq 0) {
    throw "No ready Android emulator/device found."
}

foreach ($device in $connectedDevices) {
    Write-Host "[$device] remove legacy acceptance package and current project package"
    if (-not $WhatIf) {
        & $adbPath -s $device uninstall com.jueqiao.jianghu.acceptance | Out-Host
        & $adbPath -s $device uninstall com.jueqiao.jianghu | Out-Host
    }

    Write-Host "[$device] install $resolvedApk"
    if (-not $WhatIf) {
        & $adbPath -s $device install $resolvedApk | Out-Host
        if ($LASTEXITCODE -ne 0) {
            throw "APK install failed on $device"
        }

        $projectPackages = @(& $adbPath -s $device shell pm list packages | Select-String "com\.jueqiao\.jianghu")
        if ($projectPackages.Count -ne 1 -or $projectPackages[0].ToString().Trim() -ne "package:com.jueqiao.jianghu") {
            throw "Unexpected project packages on ${device}: $($projectPackages -join ', ')"
        }
    }
}

Write-Host "Done: each connected device has only com.jueqiao.jianghu from the selected APK."
