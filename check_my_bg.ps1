Add-Type -AssemblyName System.Drawing
$path = 'C:\Users\cai\Downloads\室内家园要求 1 (2).png'
$img = [System.Drawing.Image]::FromFile($path)
Write-Host ("Size: {0}x{1}" -f $img.Width, $img.Height)
Write-Host ("Format: {0}" -f $img.RawFormat)
$img.Dispose()
