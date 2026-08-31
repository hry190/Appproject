Add-Type -AssemblyName System.Drawing
$dir = 'C:\Users\cai\Downloads'
$matches = Get-ChildItem $dir -Filter '*.png' | Where-Object { $_.Name -like '*.png' -and $_.Name.Length -gt 4 -and $_.Name.Substring(0, [Math]::Min(3, $_.Name.Length)) -eq [char]0x5934 }
Write-Host "Found files:"
$matches | ForEach-Object { Write-Host ("  {0}  (size={1})" -f $_.FullName, $_.Length) }
foreach ($m in $matches) {
    try {
        $img = [System.Drawing.Image]::FromFile($m.FullName)
        Write-Host ("  -> {0}x{1}" -f $img.Width, $img.Height)
        $img.Dispose()
    } catch {
        Write-Host ("  ERR: {0}" -f $_.Exception.Message)
    }
}
