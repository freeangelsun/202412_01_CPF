param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

function ConvertFrom-UnicodeEscape {
    param([string] $Value)

    return [System.Text.RegularExpressions.Regex]::Unescape($Value)
}

function Escape-MarkdownCell {
    param([string] $Value)

    if ($null -eq $Value) {
        return ""
    }
    return ($Value -replace '\|', '\|') -replace "`r?`n", "<br>"
}

function Get-EvidenceLinks {
    param([string] $Evidence)

    $tick = [char] 0x60
    if ([string]::IsNullOrWhiteSpace($Evidence) -or $Evidence -eq "없음") {
        return "$tick$($Evidence)$tick"
    }
    $paths = @($Evidence -split ',' | ForEach-Object { $_.Trim() } | Where-Object { $_ })
    return (($paths | ForEach-Object { "$tick$_$tick" }) -join ", ")
}

function Set-MarkedSection {
    param(
        [string] $Path,
        [string] $BeginMarker,
        [string] $EndMarker,
        [string[]] $Lines
    )

    $text = [System.IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8)
    $pattern = [System.Text.RegularExpressions.Regex]::Escape($BeginMarker) +
            '[\s\S]*?' + [System.Text.RegularExpressions.Regex]::Escape($EndMarker)
    if (-not [System.Text.RegularExpressions.Regex]::IsMatch($text, $pattern)) {
        throw "동기화 구간을 찾지 못했습니다. path=$Path marker=$BeginMarker"
    }
    $replacement = $BeginMarker + "`n" + ($Lines -join "`n") + "`n" + $EndMarker
    $updated = [System.Text.RegularExpressions.Regex]::Replace($text, $pattern, $replacement, 1)
    [System.IO.File]::WriteAllText($Path, $updated, $Utf8NoBom)
}

$matrixBaseName = ConvertFrom-UnicodeEscape '\uAE30\uB2A5_\uAD6C\uD604_\uB9E4\uD2B8\uB9AD\uC2A4'
$matrixJsonPath = Join-Path $Root "specs/$matrixBaseName.json"
$matrixMarkdownPath = Join-Path $Root "specs/$matrixBaseName.md"
$evidenceIndexPath = Join-Path $Root "CPF_EVIDENCE_INDEX.md"
$gapPath = Join-Path $Root "CPF_GAP_MATRIX.md"
$reportPath = Join-Path $Root "CPF_STABILIZATION_REPORT.md"

$payload = [System.IO.File]::ReadAllText($matrixJsonPath, [System.Text.Encoding]::UTF8) | ConvertFrom-Json
$items = @($payload.items)
$allowed = @($payload.allowedStatuses)
$duplicates = @($items | Group-Object checkId | Where-Object Count -gt 1)
if ($items.Count -ne 61 -or $duplicates.Count -gt 0) {
    throw "기능 검증 ledger는 중복 없는 61개 check ID여야 합니다. count=$($items.Count)"
}
foreach ($item in $items) {
    if ($allowed -notcontains [string] $item.status) {
        throw "허용되지 않은 상태값입니다. checkId=$($item.checkId) status=$($item.status)"
    }
}

$matrixLines = New-Object System.Collections.Generic.List[string]
$matrixLines.Add("# CPF 기능 구현 검증 매트릭스") | Out-Null
$matrixLines.Add("") | Out-Null
$matrixLines.Add("> 이 파일은 ``specs/$matrixBaseName.json``에서 생성되는 기계 검증 정본입니다.") | Out-Null
$matrixLines.Add("") | Out-Null
$matrixLines.Add("| check id | 상태 | 증적 | 비고 |") | Out-Null
$matrixLines.Add("|---|---|---|---|") | Out-Null
foreach ($item in $items) {
    $matrixLines.Add("| $(Escape-MarkdownCell $item.checkId) | $(Escape-MarkdownCell $item.status) | $(Escape-MarkdownCell $item.evidence) | $(Escape-MarkdownCell $item.note) |") | Out-Null
}
[System.IO.File]::WriteAllText($matrixMarkdownPath, (($matrixLines -join "`n") + "`n"), $Utf8NoBom)

$evidenceLines = New-Object System.Collections.Generic.List[string]
$evidenceLines.Add("# CPF 증적 인덱스") | Out-Null
$evidenceLines.Add("") | Out-Null
$evidenceLines.Add("생성 시각: $([DateTimeOffset]::Now.ToString('yyyy-MM-dd HH:mm:ss zzz'))") | Out-Null
$evidenceLines.Add("") | Out-Null
$evidenceLines.Add("기준 증적 디렉터리: ``specs/evidence/20260714_02``") | Out-Null
$evidenceLines.Add("") | Out-Null
$evidenceLines.Add("| check id | 상태 | 증적 | 확인 기준 | 비고 |") | Out-Null
$evidenceLines.Add("|---|---|---|---|---|") | Out-Null
foreach ($item in $items) {
    $evidenceLines.Add("| $(Escape-MarkdownCell $item.checkId) | $(Escape-MarkdownCell $item.status) | $(Get-EvidenceLinks $item.evidence) | report/matrix/evidence 정합성 | $(Escape-MarkdownCell $item.note) |") | Out-Null
}
[System.IO.File]::WriteAllText($evidenceIndexPath, (($evidenceLines -join "`n") + "`n"), $Utf8NoBom)

$reportLedger = New-Object System.Collections.Generic.List[string]
$reportLedger.Add("| check id | 상태 | 핵심 증적 | 판정 |") | Out-Null
$reportLedger.Add("|---|---|---|---|") | Out-Null
foreach ($item in $items) {
    $reportLedger.Add("| $(Escape-MarkdownCell $item.checkId) | $(Escape-MarkdownCell $item.status) | $(Get-EvidenceLinks $item.evidence) | $(Escape-MarkdownCell $item.note) |") | Out-Null
}
Set-MarkedSection -Path $reportPath -BeginMarker '<!-- CPF_LEDGER_BEGIN -->' -EndMarker '<!-- CPF_LEDGER_END -->' -Lines $reportLedger

$gapLines = New-Object System.Collections.Generic.List[string]
$gapLines.Add("# CPF GAP 매트릭스") | Out-Null
$gapLines.Add("") | Out-Null
$gapLines.Add("생성 시각: $([DateTimeOffset]::Now.ToString('yyyy-MM-dd HH:mm:ss zzz'))") | Out-Null
$gapLines.Add("") | Out-Null
$gapLines.Add("``완료``가 아닌 항목만 표시하며, 외부 환경 선행조건은 완료로 승격하지 않습니다.") | Out-Null
$gapLines.Add("") | Out-Null
$gapLines.Add("| check id | 상태 | 현재 증적 | 남은 GAP |") | Out-Null
$gapLines.Add("|---|---|---|---|") | Out-Null
foreach ($item in @($items | Where-Object { $_.status -ne "완료" })) {
    $gapLines.Add("| $(Escape-MarkdownCell $item.checkId) | $(Escape-MarkdownCell $item.status) | $(Get-EvidenceLinks $item.evidence) | $(Escape-MarkdownCell $item.note) |") | Out-Null
}
[System.IO.File]::WriteAllText($gapPath, (($gapLines -join "`n") + "`n"), $Utf8NoBom)

Write-Host "검증 ledger 동기화 완료: items=$($items.Count)"
