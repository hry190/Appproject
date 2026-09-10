param(
    [ValidateSet("demo", "acceptance")]
    [string]$Mode = "demo",
    [string]$Device = "emulator-5554",
    [switch]$SkipApiStart,
    [switch]$SkipWorkflowCheck,
    [switch]$SkipDeviceFlow
)

$ErrorActionPreference = "Stop"
$projectRoot = (Resolve-Path -LiteralPath (Join-Path $PSScriptRoot "..")).Path
$apiRoot = Join-Path $projectRoot "services\api"
$androidRoot = Join-Path $projectRoot "android"
$pythonPath = Join-Path $apiRoot ".venv\Scripts\python.exe"
$adbPath = Join-Path $env:LOCALAPPDATA "Android\Sdk\platform-tools\adb.exe"
$healthUrl = "http://127.0.0.1:8011/healthz"
$packageName = "com.jueqiao.jianghu.$Mode"
$apkPath = Join-Path $androidRoot "app\build\outputs\apk\$Mode\app-$Mode.apk"
$evidenceDir = Join-Path $projectRoot "artifacts\creation-acceptance"
$apiProcess = $null

if (-not (Test-Path -LiteralPath $pythonPath -PathType Leaf)) {
    throw "Python environment not found: $pythonPath"
}
if (-not (Test-Path -LiteralPath $adbPath -PathType Leaf)) {
    throw "adb not found: $adbPath"
}

function Test-ContestService {
    try {
        $response = Invoke-RestMethod -Uri $healthUrl -TimeoutSec 2
        return $response.status -eq "ok"
    } catch {
        return $false
    }
}

if (-not (Test-ContestService)) {
    if ($SkipApiStart) {
        throw "Competition backend is unavailable: $healthUrl"
    }
    $apiProcess = Start-Process `
        -FilePath $pythonPath `
        -ArgumentList "scripts/serve_acceptance.py" `
        -WorkingDirectory $apiRoot `
        -WindowStyle Hidden `
        -PassThru
}

$serviceReady = $false
for ($attempt = 0; $attempt -lt 45; $attempt++) {
    if (Test-ContestService) {
        $serviceReady = $true
        break
    }
    if ($apiProcess -and $apiProcess.HasExited) {
        throw "Competition backend exited before becoming ready."
    }
    Start-Sleep -Milliseconds 500
}
if (-not $serviceReady) {
    throw "Competition backend did not become ready: $healthUrl"
}
Write-Host "✓ 比赛后端已就绪：$healthUrl"

New-Item -ItemType Directory -Path $evidenceDir -Force | Out-Null
$stamp = Get-Date -Format "yyyyMMdd-HHmmss"
$phoneSeed = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds() % 100000000
$studentPhone = "139{0:D8}" -f $phoneSeed
$teacherPhone = "138{0:D8}" -f $phoneSeed

Push-Location $androidRoot
try {
    & ".\gradlew.bat" ":app:assemble$($Mode.Substring(0, 1).ToUpper())$($Mode.Substring(1))" --no-daemon
    if ($LASTEXITCODE -ne 0) {
        throw "Android $Mode build failed."
    }
} finally {
    Pop-Location
}
if (-not (Test-Path -LiteralPath $apkPath -PathType Leaf)) {
    throw "APK was not produced: $apkPath"
}

& $adbPath -s $Device get-state | Out-Null
if ($LASTEXITCODE -ne 0) {
    throw "Android device is not ready: $Device"
}
& $adbPath -s $Device install -r $apkPath | Out-Host
if ($LASTEXITCODE -ne 0) {
    throw "APK install failed on $Device"
}
& $adbPath -s $Device shell pm clear $packageName | Out-Host
if ($LASTEXITCODE -ne 0) {
    throw "App data reset failed on $Device"
}
& $adbPath -s $Device shell am force-stop $packageName
& $adbPath -s $Device shell am start -n "$packageName/com.jueqiao.jianghu.MainActivity" | Out-Host
if ($LASTEXITCODE -ne 0) {
    throw "App launch failed on $Device"
}
if (-not $SkipDeviceFlow -and -not $SkipWorkflowCheck) {
    $deviceEvidence = Join-Path $evidenceDir "device-$Mode-$stamp"
    & $pythonPath (Join-Path $apiRoot "scripts\android_creation_acceptance.py") `
        --device $Device `
        --package $packageName `
        --student-phone $studentPhone `
        --teacher-phone $teacherPhone `
        --output $deviceEvidence
    if ($LASTEXITCODE -ne 0) {
        throw "Android creation navigation acceptance failed."
    }
}

$hash = (Get-FileHash -LiteralPath $apkPath -Algorithm SHA256).Hash
$buildEvidence = [ordered]@{
    mode = $Mode
    package = $packageName
    device = $Device
    backend = $healthUrl
    apk = (Resolve-Path -LiteralPath $apkPath).Path
    sha256 = $hash
    createdAt = (Get-Date).ToString("o")
}
$buildEvidence | ConvertTo-Json | Set-Content -LiteralPath (Join-Path $evidenceDir "latest-build.json") -Encoding UTF8
Write-Host "✓ 已启动 $packageName"
Write-Host "✓ APK SHA-256: $hash"
Write-Host "✓ 验收证据：$evidenceDir"
