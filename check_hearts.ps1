Add-Type -AssemblyName System.Drawing
$files = @(
    (Join-Path $env:USERPROFILE 'Downloads\Like (喜欢) (1).png'),
    (Join-Path $env:USERPROFILE 'Downloads\Like (喜欢) (2).png'),
    'e:\app\Appproject\android\app\src\main\res\drawable\img_yanwuchang_video_comment_like_vector.png'
)
foreach ($f in $files) {
    $exists = [System.IO.File]::Exists($f)
    Write-Host ("[exists={0}] {1}" -f $exists, $f)
    if ($exists) {
        $img = [System.Drawing.Image]::FromFile($f)
        Write-Host ("    {0}x{1}" -f $img.Width, $img.Height)
        $img.Dispose()
    }
}
