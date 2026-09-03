Add-Type -AssemblyName System.Drawing
$files = @(
    "$env:USERPROFILE\Downloads\未标题-1 69 (3).png",
    'e:\app\Appproject\android\app\src\main\res\drawable\img_yanwuchang_video_comment_expanded_bg.png'
)
foreach ($f in $files) {
    if (Test-Path -LiteralPath $f) {
        $img = [System.Drawing.Image]::FromFile($f)
        Write-Host ("{0} = {1}x{2}" -f $f, $img.Width, $img.Height)
        $img.Dispose()
    } else {
        Write-Host ("MISSING: {0}" -f $f)
    }
}
