Add-Type -AssemblyName System.Drawing
$f = 'C:\Users\cai\Downloads\头像.png'
if (Test-Path $f) {
    $img = [System.Drawing.Image]::FromFile($f)
    Write-Host ("{0} = {1}x{2}" -f $f, $img.Width, $img.Height)
    $img.Dispose()
} else {
    Write-Host "MISSING"
    # try to find any 头像 file
    Get-ChildItem 'C:\Users\cai\Downloads' -Filter '*头像*' | ForEach-Object { Write-Object $_ }
}
