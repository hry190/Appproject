Add-Type -AssemblyName System.Drawing
$dir = 'C:\Users\cai\Downloads'
# Use StartsWith via .NET String method
$files = Get-ChildItem $dir -Filter '*.png'
$target = $files | Where-Object { $_.Name.StartsWith([char]0x5934) }
foreach ($m in $target) {
    $img = [System.Drawing.Image]::FromFile($m.FullName)
    Write-Host ("{0} = {1}x{2}" -f $m.FullName, $img.Width, $img.Height)
    $img.Dispose()
}
