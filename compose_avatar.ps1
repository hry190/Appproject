Add-Type -AssemblyName System.Drawing

# Source images
$ellipsePath = 'C:\Users\cai\Downloads\Ellipse 31.png'
$v593 = 'C:\Users\cai\Downloads\Vector 593.png'   # 3x8
$v594 = 'C:\Users\cai\Downloads\Vector 594.png'   # 3x10
$v595 = 'C:\Users\cai\Downloads\Vector 595.png'   # 16x5

# Output
$outPath = 'e:\app\Appproject\android\app\src\main\res\drawable\avatar_with_lines.png'

# Load base (Ellipse 31, 40x40)
$base = [System.Drawing.Image]::FromFile($ellipsePath)
$bmp = New-Object System.Drawing.Bitmap($base)
$g = [System.Drawing.Graphics]::FromImage($bmp)
$g.InterpolationMode = [System.Drawing.Drawing2D.InterpolationMode]::HighQualityBicubic
$g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::HighQuality

# Figma design canvas size assumption: 500x500 (Figma default-ish for a frame)
# Target canvas: 40x40
# Scale = 40 / 500 = 0.08
$scale = 40.0 / 500.0

# Helper: place a vector PNG centered at (cx, cy) on the 40x40 canvas
function Place-Center($path, $cxFigma, $cyFigma) {
    $img = [System.Drawing.Image]::FromFile($path)
    $w = $img.Width
    $h = $img.Height
    # center position in target
    $cx = $cxFigma * $scale
    $cy = $cyFigma * $scale
    $x = [int][Math]::Round($cx - $w / 2.0)
    $y = [int][Math]::Round($cy - $h / 2.0)
    Write-Host ("Place {0} (w={1},h={2}) at ({3},{4}) [figma center=({5},{6})]" -f $path, $w, $h, $x, $y, $cxFigma, $cyFigma)
    $g.DrawImage($img, $x, $y, $w, $h)
    $img.Dispose()
}

# Vector 593: center (379, 456), size 0.08*6 (W*H) -> PNG already at native 3x8
Place-Center $v593 379 456

# Vector 594: center (395, 454), size 0.55*7.25 -> PNG at 3x10
Place-Center $v594 395 454

# Vector 595: HORIZONTAL line, center (382, 471), size 13.4*2.03 -> PNG at 16x5
# It's already horizontal in the asset, so place normally
Place-Center $v595 382 471

# Save
$bmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
Write-Host ("Saved: {0}" -f $outPath)

$g.Dispose()
$bmp.Dispose()
$base.Dispose()
