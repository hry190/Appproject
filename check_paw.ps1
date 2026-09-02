Add-Type -AssemblyName System.Drawing
$path = 'C:\Users\cai\Downloads\爪子 (1) 1.png'
$img = [System.Drawing.Image]::FromFile($path)
Write-Host ("Size: {0}x{1}" -f $img.Width, $img.Height)
Write-Host ("Format: {0}" -f $img.RawFormat)
$img.Dispose()

# also check transparency
$bmp = [System.Drawing.Bitmap]::FromFile($path)
$hasAlpha = $bmp.PixelFormat -band [System.Drawing.Imaging.PixelFormat]::Alpha
Write-Host ("PixelFormat: {0} (hasAlpha={1})" -f $bmp.PixelFormat, [bool]$hasAlpha)
$bmp.Dispose()
