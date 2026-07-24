param(
    [string] $Root = "",
    [Parameter(Mandatory = $true)]
    [string] $ResultDir
)

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"

$Root = if ([string]::IsNullOrWhiteSpace($Root)) {
    (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
} else {
    (Resolve-Path -LiteralPath $Root).Path
}
if (-not [System.IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
$ResultDir = [System.IO.Path]::GetFullPath($ResultDir)
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

$targetPath = Join-Path $Root "CPF_FINAL_TARGET_REQUIREMENTS.md"
$ledgerPath = Join-Path $Root "cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md"
$reviewPath = Join-Path $Root "cpf-docs/work/review/20260724_02/CPF_MASTER_REQUIREMENT_AND_SOURCE_REVIEW.md"
foreach ($required in @($targetPath, $ledgerPath, $reviewPath)) {
    if (-not (Test-Path -LiteralPath $required -PathType Leaf)) {
        throw "Requirement traceability input is missing: $required"
    }
}

function Read-Utf8([string] $Path) {
    return [System.IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8)
}

function Get-DeclaredCanonicalCount([string] $Text, [string] $Name) {
    $match = [regex]::Match($Text, 'Canonical(?: Requirement)? Count(?:는|:)?\s*\*{0,2}(?<count>\d+)', 'IgnoreCase')
    if (-not $match.Success) {
        $match = [regex]::Match($Text, 'Canonical Count:\s*\*{0,2}(?<count>\d+)')
    }
    if (-not $match.Success) {
        throw "$Name 문서에서 Canonical Requirement Count를 찾을 수 없습니다."
    }
    return [int] $match.Groups['count'].Value
}

function Get-CanonicalIds([string] $TargetText) {
    $sectionMatch = [regex]::Match(
        $TargetText,
        '(?s)## 22\. Requirement Catalog(?<body>.*?)(?:\r?\n## 23\.|\z)')
    if (-not $sectionMatch.Success) {
        throw "Final Target의 Requirement Catalog section을 찾을 수 없습니다."
    }

    $result = [System.Collections.Generic.List[string]]::new()
    $seen = @{}
    foreach ($match in [regex]::Matches(
            $sectionMatch.Groups['body'].Value,
            '`(?<id>[A-Z][A-Z0-9]+(?:-[A-Z0-9]+)+)`')) {
        $id = $match.Groups['id'].Value
        if (-not $seen.ContainsKey($id)) {
            $seen[$id] = $true
            $result.Add($id) | Out-Null
        }
    }
    return @($result)
}

function Get-ReviewStatusMap([string] $ReviewText) {
    $allowed = @("완료", "부분 구현", "미구현", "미검증", "실패", "재확인 필요")
    $map = @{}
    foreach ($line in ($ReviewText -split "\r?\n")) {
        if ($line -match '^\|\s*`(?<id>[A-Z][A-Z0-9]+(?:-[A-Z0-9]+)+)`\s*\|\s*(?<status>[^|]+?)\s*\|\s*(?<memo>.*?)\s*\|$') {
            $id = $Matches.id
            $status = $Matches.status.Trim()
            if ($allowed -notcontains $status) {
                throw "허용되지 않은 Requirement 상태입니다. id=$id status=$status"
            }
            if ($map.ContainsKey($id)) {
                throw "Review에 Requirement ID가 중복됩니다. id=$id"
            }
            $map[$id] = [ordered]@{
                status = $status
                memo = $Matches.memo.Trim()
            }
        }
    }
    return $map
}

$targetText = Read-Utf8 $targetPath
$ledgerText = Read-Utf8 $ledgerPath
$reviewText = Read-Utf8 $reviewPath

$canonicalIds = @(Get-CanonicalIds $targetText)
$targetDeclaredCount = Get-DeclaredCanonicalCount $targetText "Final Target"
$ledgerDeclaredCount = Get-DeclaredCanonicalCount $ledgerText "Continuity Ledger"

if ($canonicalIds.Count -ne $targetDeclaredCount) {
    throw "Final Target Canonical Count 불일치: declared=$targetDeclaredCount actual=$($canonicalIds.Count)"
}
if ($ledgerDeclaredCount -ne $targetDeclaredCount) {
    throw "Final Target/Ledger Canonical Count 불일치: target=$targetDeclaredCount ledger=$ledgerDeclaredCount"
}

$reviewMap = Get-ReviewStatusMap $reviewText
$rows = [System.Collections.Generic.List[object]]::new()
$missing = [System.Collections.Generic.List[string]]::new()
$statusCounts = [ordered]@{
    "완료" = 0
    "부분 구현" = 0
    "미구현" = 0
    "미검증" = 0
    "실패" = 0
    "재확인 필요" = 0
}

foreach ($id in $canonicalIds) {
    if (-not $reviewMap.ContainsKey($id)) {
        $missing.Add($id) | Out-Null
        continue
    }
    $entry = $reviewMap[$id]
    $statusCounts[$entry.status] = [int] $statusCounts[$entry.status] + 1
    $rows.Add([ordered]@{
        requirementId = $id
        status = $entry.status
        reviewMemo = $entry.memo
        runtimeVerified = $false
        evidence = @()
        note = "Evidence는 실제 실행 후 별도 연결하며 정적 Review를 Runtime 완료로 승격하지 않습니다."
    }) | Out-Null
}

$extra = @($reviewMap.Keys | Where-Object { $canonicalIds -notcontains $_ } | Sort-Object)
if ($missing.Count -gt 0 -or $extra.Count -gt 0) {
    throw "Requirement Review coverage 불일치: missing=$($missing -join ',') extra=$($extra -join ',')"
}

$result = [ordered]@{
    generatedAt = [DateTimeOffset]::Now.ToString("o")
    baselineCommit = "22b1874e67547372b51a4bcd21f47aea6fcb5c25"
    canonicalRequirementCount = $canonicalIds.Count
    legacyAliasCount = 8
    statusCounts = $statusCounts
    runtimeEvidenceInherited = $false
    requirements = @($rows)
}
$outputPath = Join-Path $ResultDir "requirement-traceability.sanitized.json"
[System.IO.File]::WriteAllText(
    $outputPath,
    ($result | ConvertTo-Json -Depth 12),
    $Utf8NoBom)

Write-Host "Requirement traceability exported: canonical=$($canonicalIds.Count) path=$outputPath"
