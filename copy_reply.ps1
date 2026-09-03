$src = Join-Path $env:USERPROFILE 'Downloads\未标题-2-恢复的 12 (1).png'
$dst = 'e:\app\Appproject\android\app\src\main\res\drawable\img_yanwuchang_video_comment1_reply.png'
Write-Host ("Source exists: {0}" -f (Test-Path -LiteralPath $src))
Write-Host ("Source size: {0} bytes" -f (Get-Item -LiteralPath $src).Length)
Copy-Item -LiteralPath $src -Destination $dst -Force
Write-Host ("Dest exists: {0}" -f (Test-Path $dst))
Write-Host ("Dest size: {0} bytes" -f (Get-Item $dst).Length)
