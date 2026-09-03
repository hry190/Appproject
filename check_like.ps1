Add-Type -AssemblyName System.Drawing
$files = @(
    'e:\app\Appproject\android\app\src\main\res\drawable\img_yanwuchang_video_comment1_like.png',
    'e:\app\Appproject\android\app\src\main\res\drawable\img_yanwuchang_video_comment1_50.png'
)
foreach ($f in $files) {
    $img = [System.Drawing.Image]::FromFile($f)
    Write-Host ("{0} = {1}x{2}" -f $f, $img.Width, $img.Height)
    $img.Dispose()
}
