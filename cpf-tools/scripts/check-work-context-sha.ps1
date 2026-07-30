param(
    [string] $ExpectedSha = $env:GITHUB_SHA,
    [switch] $AllowOverlayBaseSha,
    [switch] $RequireCurrentEvidence
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$root = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
if ([string]::IsNullOrWhiteSpace($ExpectedSha)) {
    $ExpectedSha = (& git -C $root rev-parse HEAD).Trim()
}
if ($ExpectedSha -notmatch '^[0-9a-fA-F]{40}$') {
    throw "ExpectedSha must be a full 40-character Git SHA: $ExpectedSha"
}
$ExpectedSha = $ExpectedSha.ToLowerInvariant()

$activeRoots = @(
    'cpf-docs/work/current',
    'cpf-docs/work/handover',
    'cpf-docs/work/state'
) | ForEach-Object { Join-Path $root $_ } | Where-Object { Test-Path -LiteralPath $_ -PathType Container }
$currentEvidenceRoot = Join-Path $root 'cpf-docs/evidence/current'

# Historical evidence may legitimately describe an older commit. Only active work documents and
# evidence/current are required to describe the current exact source SHA.
$claimPattern = '(?im)(exact[_ ]?sha|validated[_ ]?sha|verification[_ ]?sha|source[_ ]?sha|검증 SHA|검수 SHA|Evidence SHA)\s*[:=]\s*`?([0-9a-f]{40})`?'
$basePattern = '(?im)(overlay[_ ]?base[_ ]?sha|개발 기준 SHA|Overlay 기준 SHA|시작 SHA)\s*[:=]\s*`?([0-9a-f]{40})`?'
$errors = [System.Collections.Generic.List[string]]::new()

foreach ($dir in $activeRoots) {
    Get-ChildItem -LiteralPath $dir -Recurse -File -Include *.md,*.json,*.csv | ForEach-Object {
        $text = Get-Content -LiteralPath $_.FullName -Raw -Encoding UTF8
        foreach ($match in [regex]::Matches($text, $claimPattern)) {
            $claimed = $match.Groups[2].Value.ToLowerInvariant()
            if ($claimed -ne $ExpectedSha) {
                $errors.Add("active exact-SHA mismatch: $($_.FullName) claimed=$claimed expected=$ExpectedSha") | Out-Null
            }
        }
        if (-not $AllowOverlayBaseSha) {
            foreach ($match in [regex]::Matches($text, $basePattern)) {
                $claimed = $match.Groups[2].Value.ToLowerInvariant()
                if ($claimed -ne $ExpectedSha) {
                    $errors.Add("active base-SHA mismatch: $($_.FullName) claimed=$claimed expected=$ExpectedSha") | Out-Null
                }
            }
        }
    }
}

$currentEvidenceFiles = @()
if (Test-Path -LiteralPath $currentEvidenceRoot -PathType Container) {
    $currentEvidenceFiles = @(Get-ChildItem -LiteralPath $currentEvidenceRoot -File -Filter '*.json')
}
if ($RequireCurrentEvidence -and $currentEvidenceFiles.Count -eq 0) {
    $errors.Add("current exact-SHA evidence is required: $currentEvidenceRoot") | Out-Null
}
foreach ($file in $currentEvidenceFiles) {
    try {
        $evidence = Get-Content -LiteralPath $file.FullName -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 100
    } catch {
        $errors.Add("current evidence JSON parse failed: $($file.FullName) :: $($_.Exception.Message)") | Out-Null
        continue
    }
    $sourceSha = ([string]$evidence.sourceSha).Trim().ToLowerInvariant()
    if ($sourceSha -ne $ExpectedSha) {
        $errors.Add("current evidence sourceSha mismatch: $($file.FullName) sourceSha=$sourceSha expected=$ExpectedSha") | Out-Null
    }
    if ([string]::IsNullOrWhiteSpace([string]$evidence.command)) {
        $errors.Add("current evidence command missing: $($file.FullName)") | Out-Null
    }
    if ($null -eq $evidence.exitCode) {
        $errors.Add("current evidence exitCode missing: $($file.FullName)") | Out-Null
    }
    if ([string]::IsNullOrWhiteSpace([string]$evidence.startedAt) -or
        [string]::IsNullOrWhiteSpace([string]$evidence.finishedAt)) {
        $errors.Add("current evidence start/finish time missing: $($file.FullName)") | Out-Null
    }
    if ([string]::IsNullOrWhiteSpace([string]$evidence.status) -or
        [string]$evidence.status -notin @('완료','실패','미검증')) {
        $errors.Add("current evidence status invalid: $($file.FullName)") | Out-Null
    }
    if ($null -eq $evidence.sensitiveDataRemoved -or -not [bool]$evidence.sensitiveDataRemoved) {
        $errors.Add("current evidence sensitiveDataRemoved must be true: $($file.FullName)") | Out-Null
    }
}

if ($errors.Count -gt 0) {
    $errors | ForEach-Object { Write-Error $_ }
    exit 1
}
Write-Host "[PASS] Active work/current evidence SHA is consistent with $ExpectedSha (currentEvidence=$($currentEvidenceFiles.Count), requireCurrent=$RequireCurrentEvidence)"
