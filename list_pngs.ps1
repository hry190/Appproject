Add-Type -AssemblyName System.Drawing
$dir = 'C:\Users\cai\Downloads'
$all = Get-ChildItem $dir -Filter '*.png' | Sort-Object LastWriteTime
# Print first chars of names
$all | ForEach-Object {
    $n = $_.Name
    if ($n.Length -ge 1) { $c1 = [int]$n[0] } else { $c1 = -1 }
    if ($n.Length -ge 2) { $c2 = [int]$n[1] } else { $c2 = -1 }
    Write-Host ("  {0}  c1={1:X4} c2={2:X4}  date={3}" -f $n, $c1, $c2, $_.LastWriteTime)
}
