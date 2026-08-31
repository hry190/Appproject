Get-ChildItem 'C:\Users\cai\Downloads' | Where-Object { $_.Name -like '*头*' -or $_.Name -like '*avatar*' -or $_.Name -like '*portrait*' } | Select-Object Name, Length
Write-Host "---"
# Also list any recently downloaded PNGs
Get-ChildItem 'C:\Users\cai\Downloads' -Filter '*.png' | Sort-Object LastWriteTime -Descending | Select-Object -First 15 Name, LastWriteTime
