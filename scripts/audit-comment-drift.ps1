<#
.SYNOPSIS
    审计 Kotlin 源码里 inline 注释的几何数值(X=?, Y=?, W=?, H=?)与代码实际值是否一致;
    可选 -Fix 把注释对齐到代码。

.DESCRIPTION
    判据来自项目约定 docs/ONBOARDING.md §5.4:
        "✅ 改元素 offset/size 时,同步 inline 注释里的 // 元素(X=?, Y=?, W=?, H=?)"
    这条约定容易被漏做,于是注释会慢慢漂移(用户真机上调过 offset/size,注释没跟上),
    进而误导后续排查。本脚本负责发现这种漂移。

    ⚠️ 方向问题(重要):
    本脚本把**代码值当作真值**,即"注释向代码看齐"。这只在两种情况下成立:
      ① 用户明确说"代码里的数值是我调过的正确值"
      ② 已用 git 历史确认过代码是对的
    反例:2026-09-16 §21o 的 Volume3Part7,那里是**代码错、注释对**(y 被一个无关提交
    从 491 误改成 891)。那种情况要先查历史定性,不能直接跑 -Fix。
    所以:**默认只审计(只读),-Fix 需要显式指定**。

.EXAMPLE
    # 只审计(默认,不写任何文件)
    ./scripts/audit-comment-drift.ps1

.EXAMPLE
    # 对齐注释(会就地修改文件;建议先确保工作区已提交,便于 git diff 复核)
    ./scripts/audit-comment-drift.ps1 -Fix

.EXAMPLE
    # 供 CI/钩子用:有漂移就返回非 0
    ./scripts/audit-comment-drift.ps1 -FailOnDrift

.NOTES
    实现上踩过的 3 个坑(改动本脚本时请勿踩回去,详见 SESSION-LOG-2026-09-16 §21p):
      1. .NET 正则里**具名组的编号在无名组之后**。形如 (?<k>[XYWH])\s*=\s*(数字) 的表达式,
         Groups[1] 是数字、Groups[2] 才是键名 —— 别按书写顺序猜。
      2. **歧义护栏要按"任一键重复"判,不能只查某个键**。汇总式注释例如
         "Y=381+198=579,在书框 Y=88-872 范围内" 里 Y 出现两次;取值时"后者覆盖前者"
         会把参照物(书框)的 Y 当成元素的 Y → 假阳性(实测曾把 10 处虚报成 125 处)。
      3. **前瞻窗口要覆盖项目真实的代码形态**。本项目是
         "注释 / Box( / modifier / .align / .offset / .size" 共 5~6 行,
         窗口只给 4 行的话 .size 落在窗口外 → W/H 永远不会被比较(实测少报 38 处)。
         同时用"下一条注释行"作为语义边界,比固定行数更稳。
      另外:写文件必须**逐字节保真**。用 ReadAllLines + Join 会顺手干掉 UTF-8 BOM
      和文件末尾换行,让 diff 混入与本次目的无关的噪声。本脚本用
      "读原始字节 → 整文本 IndexOf 定点替换 → 同编码写回字节"。
#>
[CmdletBinding()]
param(
    # 扫描根目录(相对仓库根;默认只扫 UI 层)
    [string]$Root = "android/app/src/main/java/com/jueqiao/jianghu/ui",

    # 就地修改文件,把注释数值对齐到代码。默认只审计不写盘。
    [switch]$Fix,

    # 发现漂移时以退出码 1 结束(给 CI / pre-commit 用)
    [switch]$FailOnDrift
)

$ErrorActionPreference = "Stop"

$repoRoot = Split-Path $PSScriptRoot -Parent
$scanRoot = if ([System.IO.Path]::IsPathRooted($Root)) { $Root } else { Join-Path $repoRoot $Root }
if (-not (Test-Path $scanRoot)) { throw "扫描根不存在: $scanRoot" }

$numPat   = '(-?\d+(?:\.\d+)?)'
$tupleRe  = [regex]"(?<k>[XYWH])\s*=\s*$numPat"
$offsetRe = [regex]"\.offset\(\s*x\s*=\s*$numPat\s*f?\.dp\s*,\s*y\s*=\s*$numPat\s*f?\.dp\s*\)"
$sizeRe   = [regex]"\.size\(\s*width\s*=\s*$numPat\s*f?\.dp\s*,\s*height\s*=\s*$numPat\s*f?\.dp\s*\)"
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)

# 注释里习惯写整数;代码可能是浮点。整数值不要输出成 "380.0"
function Format-GeometryValue([double]$v) {
    if ($v -eq [Math]::Floor($v)) { "{0}" -f [int]$v } else { "{0}" -f $v }
}

$files = Get-ChildItem -Path $scanRoot -Recurse -Filter *.kt -File
$pairedCount = 0
$skippedCount = 0
$driftLines = 0
$driftFiles = 0
$report = New-Object System.Collections.Generic.List[object]

foreach ($file in $files) {
    $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
    # 用 GetString 解码会把 BOM 保留成字符串首的 U+FEFF,写回时会原样再产出 BOM
    $text = [System.Text.Encoding]::UTF8.GetString($bytes)
    $lines = $text -split "`r`n"
    $edits = New-Object System.Collections.Generic.List[object]

    for ($i = 0; $i -lt $lines.Count; $i++) {
        $line = $lines[$i]
        if ($line -notmatch '^\s*(//|\*)') { continue }

        $matches = $tupleRe.Matches($line)
        if ($matches.Count -lt 2) { continue }

        # 坑 2:任一键重复 → 无法按位置配对,跳过(宁可漏报,不要假报)
        $dup = $matches | Group-Object { $_.Groups['k'].Value } | Where-Object { $_.Count -gt 1 }
        if ($dup) { $skippedCount++; continue }

        $comment = @{}
        foreach ($m in $matches) { $comment[$m.Groups['k'].Value] = [double]$m.Groups[1].Value }  # 坑 1:数字是组 1

        # 坑 3:窗口 9 行,遇到下一条注释即停
        $ox = $null; $oy = $null; $sw = $null; $sh = $null; $codeLineNo = -1
        for ($k = $i + 1; $k -lt [Math]::Min($i + 9, $lines.Count); $k++) {
            if ($lines[$k] -match '^\s*(//|\*)') { break }
            $mo = $offsetRe.Match($lines[$k])
            if ($mo.Success) {
                $ox = [double]$mo.Groups[1].Value; $oy = [double]$mo.Groups[2].Value
                if ($codeLineNo -lt 0) { $codeLineNo = $k }
            }
            $mz = $sizeRe.Match($lines[$k])
            if ($mz.Success) {
                $sw = [double]$mz.Groups[1].Value; $sh = [double]$mz.Groups[2].Value
                if ($codeLineNo -lt 0) { $codeLineNo = $k }
            }
            if ($null -ne $ox -and $null -ne $sw) { break }
        }
        if ($null -eq $ox -and $null -eq $sw) { continue }
        $pairedCount++

        $codeValue = @{}
        if ($null -ne $ox) { $codeValue['X'] = $ox }
        if ($null -ne $oy) { $codeValue['Y'] = $oy }
        if ($null -ne $sw) { $codeValue['W'] = $sw }
        if ($null -ne $sh) { $codeValue['H'] = $sh }

        $newLine = $line
        $notes = @()
        foreach ($key in @('X', 'Y', 'W', 'H')) {
            if (-not $comment.ContainsKey($key) -or -not $codeValue.ContainsKey($key)) { continue }
            if ($comment[$key] -eq $codeValue[$key]) { continue }
            $re = [regex]("(?<k>" + $key + ")(?<sep>\s*=\s*)(?<v>-?\d+(?:\.\d+)?)")
            $newLine = $re.Replace(
                $newLine,
                { param($m) $m.Groups['k'].Value + $m.Groups['sep'].Value + (Format-GeometryValue $codeValue[$key]) },
                1)
            $notes += ("{0} {1}->{2}" -f $key, (Format-GeometryValue $comment[$key]), (Format-GeometryValue $codeValue[$key]))
        }
        if ($notes.Count -eq 0) { continue }

        $edits.Add(@($line, $newLine))
        $driftLines++
        Write-Output ("{0} {1}:{2}  (代码行 {3})  {4}" -f `
            $(if ($Fix) { '[FIX]' } else { '[DRIFT]' }), $file.Name, ($i + 1), ($codeLineNo + 1), ($notes -join ', '))
        Write-Output ("        - {0}" -f $line.Trim())
        Write-Output ("        + {0}" -f $newLine.Trim())
        $report.Add([pscustomobject]@{
            File = $file.FullName.Substring($repoRoot.Length + 1)
            CommentLine = $i + 1
            CodeLine = $codeLineNo + 1
            Diff = ($notes -join ' | ')
        })
    }

    if ($edits.Count -gt 0) {
        $driftFiles++
        if ($Fix) {
            # 定点替换:不重建行、不规整换行、不动 BOM 与末尾换行
            $cursor = 0
            foreach ($e in $edits) {
                $idx = $text.IndexOf($e[0], $cursor)
                if ($idx -lt 0) { throw "$($file.Name): 找不到待替换行(文件可能已被外部改动): $($e[0])" }
                $text = $text.Remove($idx, $e[0].Length).Insert($idx, $e[1])
                $cursor = $idx + $e[1].Length
            }
            [System.IO.File]::WriteAllBytes($file.FullName, $utf8NoBom.GetBytes($text))
        }
    }
}

Write-Output ""
# 路径显示:在仓库内就显示相对路径;传了仓库外的绝对 -Root 时原样显示
$displayRoot = if ($scanRoot.StartsWith($repoRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
    $scanRoot.Substring($repoRoot.Length).TrimStart('\', '/')
} else { $scanRoot }
Write-Output ("扫描根        : {0}" -f $displayRoot)
Write-Output ("配对成功      : {0} 处注释 <-> 代码" -f $pairedCount)
Write-Output ("因歧义跳过    : {0} 行(同一行里同一个键出现多次)" -f $skippedCount)
Write-Output ("数值漂移      : {0} 行 / {1} 个文件{2}" -f $driftLines, $driftFiles, $(if ($Fix) { '(已对齐)' } else { '' }))

if (-not $Fix -and $driftLines -gt 0) {
    Write-Output ""
    Write-Output "提示:确认代码值是正确的一方(必要时查 git 历史),再跑 -Fix 对齐注释。"
}

if ($FailOnDrift -and $driftLines -gt 0) { exit 1 }
