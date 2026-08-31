Add-Type -AssemblyName System.Drawing
$f = 'C:\Users\cai\Downloads\头像.png'
$img = [System.Drawing.Image]::FromFile($f)
Write-Host ("{0} = {1}x{2}" -f $f, $img.Width, $img.Height)
$img.Dispose()
