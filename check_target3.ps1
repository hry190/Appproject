Add-Type -AssemblyName System.Drawing
$dir = 'C:\Users\cai\Downloads'
Get-ChildItem $dir -Filter '*.png' | Where-Object { $_.Name -like '头像*' -and $_.Name -notlike '*(*' } | ForEach-Object {
    $img = [System.Drawing.Image]::FromFile($_.FullName)
    Write-Host ("{0} = {1}x{2}" -f $_.FullName, $img.Width, $img.Height)
    $img.Dispose()
}
