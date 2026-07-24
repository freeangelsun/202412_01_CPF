param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "build/quality-gate")
)

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"

$Root = (Resolve-Path -LiteralPath $Root).Path
if (-not [System.IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
$ResultDir = [System.IO.Path]::GetFullPath($ResultDir)
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

$targetPath = Join-Path $Root "CPF_FINAL_TARGET_REQUIREMENTS.md"
$ledgerPath = Join-Path $Root "cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md"
$reviewPath = Join-Path $Root "cpf-docs/work/review/20260724_02/CPF_MASTER_REQUIREMENT_AND_SOURCE_REVIEW.md"
$evidenceIndexPath = Join-Path $Root "CPF_EVIDENCE_INDEX.md"
$failures = [System.Collections.Generic.List[string]]::new()

foreach ($required in @($targetPath, $ledgerPath, $reviewPath, $evidenceIndexPath)) {
    if (-not (Test-Path -LiteralPath $required -PathType Leaf)) {
        $failures.Add("정합성 입력 파일이 없습니다: $required") | Out-Null
    }
}
if ($failures.Count -gt 0) {
    throw ($failures -join [Environment]::NewLine)
}

$targetText = [System.IO.File]::ReadAllText($targetPath, [System.Text.Encoding]::UTF8)
$ledgerText = [System.IO.File]::ReadAllText($ledgerPath, [System.Text.Encoding]::UTF8)
$reviewText = [System.IO.File]::ReadAllText($reviewPath, [System.Text.Encoding]::UTF8)
$evidenceText = [System.IO.File]::ReadAllText($evidenceIndexPath, [System.Text.Encoding]::UTF8)

$section = [regex]::Match($targetText, '(?s)## 22\. Requirement Catalog(?<body>.*?)(?:\r?\n## 23\.|\z)')
if (-not $section.Success) {
    $failures.Add("Final Target Requirement Catalog section이 없습니다.") | Out-Null
}

$canonical = [System.Collections.Generic.List[string]]::new()
$seen = @{}
if ($section.Success) {
    foreach ($m in [regex]::Matches($section.Groups['body'].Value, '`(?<id>[A-Z][A-Z0-9]+(?:-[A-Z0-9]+)+)`')) {
        $id = $m.Groups['id'].Value
        if (-not $seen.ContainsKey($id)) {
            $seen[$id] = $true
            $canonical.Add($id) | Out-Null
        }
    }
}

$targetCountMatch = [regex]::Match($targetText, 'Canonical Requirement Count:\s*\*{0,2}(?<count>\d+)')
$ledgerCountMatch = [regex]::Match($ledgerText, 'Canonical Count:\s*\*{0,2}(?<count>\d+)')
if (-not $targetCountMatch.Success -or -not $ledgerCountMatch.Success) {
    $failures.Add("Canonical Count 선언을 찾을 수 없습니다.") | Out-Null
} else {
    $targetCount = [int] $targetCountMatch.Groups['count'].Value
    $ledgerCount = [int] $ledgerCountMatch.Groups['count'].Value
    if ($canonical.Count -ne $targetCount) {
        $failures.Add("Final Target Count 불일치 declared=$targetCount actual=$($canonical.Count)") | Out-Null
    }
    if ($ledgerCount -ne $targetCount) {
        $failures.Add("Final Target/Ledger Count 불일치 target=$targetCount ledger=$ledgerCount") | Out-Null
    }
}

$allowedStatuses = @("완료", "부분 구현", "미구현", "미검증", "실패", "재확인 필요")
$reviewMap = @{}
foreach ($line in ($reviewText -split "\r?\n")) {
    if ($line -match '^\|\s*`(?<id>[A-Z][A-Z0-9]+(?:-[A-Z0-9]+)+)`\s*\|\s*(?<status>[^|]+?)\s*\|') {
        $id = $Matches.id
        $status = $Matches.status.Trim()
        if ($allowedStatuses -notcontains $status) {
            $failures.Add("허용되지 않은 상태: $id=$status") | Out-Null
        }
        if ($reviewMap.ContainsKey($id)) {
            $failures.Add("Review Requirement 중복: $id") | Out-Null
        } else {
            $reviewMap[$id] = $status
        }
    }
}

foreach ($id in $canonical) {
    if (-not $reviewMap.ContainsKey($id)) {
        $failures.Add("Review Requirement 누락: $id") | Out-Null
    }
}
foreach ($id in $reviewMap.Keys) {
    if ($canonical -notcontains $id) {
        $failures.Add("Review에 비정본 Requirement가 있습니다: $id") | Out-Null
    }
}

$doneIds = @($canonical | Where-Object { $reviewMap[$_] -eq "완료" })
foreach ($id in $doneIds) {
    if ($evidenceText -notmatch ('`?' + [regex]::Escape($id) + '`?')) {
        $failures.Add("완료 Requirement가 Evidence Index에 연결되지 않았습니다: $id") | Out-Null
    }
}
$result = [ordered]@{
    generatedAt = [DateTimeOffset]::Now.ToString("o")
    status = if ($failures.Count -eq 0) { "완료" } else { "실패" }
    canonicalRequirementCount = $canonical.Count
    reviewRequirementCount = $reviewMap.Count
    completedRequirementCount = $doneIds.Count
    failureCount = $failures.Count
    failures = @($failures)
}
$output = Join-Path $ResultDir "report-matrix-evidence-consistency.sanitized.json"
[System.IO.File]::WriteAllText($output, ($result | ConvertTo-Json -Depth 10), $Utf8NoBom)

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Host "FAIL $_" }
    exit 1
}
Write-Host "Requirement/Review/Evidence consistency passed. canonical=$($canonical.Count)"
