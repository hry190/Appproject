Add-Type -AssemblyName System.Drawing
$files = @(
    'C:\Users\cai\Downloads\Ellipse 31.png',
    'C:\Users\cai\Downloads\Vector 593.png',
    'C:\Users\cai\Downloads\Vector 594.png',
    'C:\Users\cai\Downloads\Vector 595.png',
    'C:\Users\cai\Downloads\头像.png',
    'e:\app\Appproject\android\app\src\main\res\drawable\ellipse_30.png',
    'e:\app\Appproject\android\app\src\main\res\drawable\vector_593.png',
    'e:\app\Appproject\android\app\src\main\res\drawable\vector_594.png',
    'e:\app\Appproject\android\app\src\main\res\drawable\vector_595.png'
)
foreach ($f in $files) {
    if (Test-Path $f) {
        $img = [System.Drawing.Image]::FromFile($f)
        Write-Host ("{0} = {1}x{2}" -f $f, $img.Width, $img.Height)
        $img.Dispose()
    } else {
        Write-Host ("MISSING: {0}" -f $f)
    }
}
