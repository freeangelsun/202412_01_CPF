param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "build/quality-gate")
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"

$failures = New-Object System.Collections.Generic.List[string]
$evidenceRows = New-Object System.Collections.Generic.List[object]
$currentEvidenceIds = @{}
$currentStartCommits = @{}
$legacyUntrackedRows = New-Object System.Collections.Generic.List[object]
$currentUntrackedRows = New-Object System.Collections.Generic.List[object]

function New-UnicodeText {
    param([int[]] $CodePoints)
    return -join ($CodePoints | ForEach-Object { [char] $_ })
}

$statusDone = New-UnicodeText @(0xC644, 0xB8CC)
$statusNotVerified = New-UnicodeText @(0xBBF8, 0xAC80, 0xC99D)
$statusFailed = New-UnicodeText @(0xC2E4, 0xD328)

function Add-Failure {
    param([string] $Message)
    $failures.Add($Message) | Out-Null
}

function Write-JsonEvidence {
    param(
        [string] $FileName,
        [object] $Value
    )
    New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
    $path = Join-Path $ResultDir $FileName
    $json = $Value | ConvertTo-Json -Depth 12
    [System.IO.File]::WriteAllText($path, $json, [System.Text.UTF8Encoding]::new($false))
}

function Add-EvidencePath {
    param(
        [string] $SourceFile,
        [string] $RelativePath
    )
    $normalized = $RelativePath.Replace("\", "/").Trim()
    if ($normalized -notmatch '^cpf-docs/evidence/') {
        return
    }
    $evidenceRows.Add([pscustomobject]@{
        sourceFile = $SourceFile
        evidencePath = $normalized
    }) | Out-Null
}

function Read-Utf8Text {
    param([string] $Path)
    return [System.IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8)
}

Write-JsonEvidence "evidence-path-existence-check.sanitized.json" ([pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    status = $statusNotVerified
    note = "self evidence file initialized before scanning"
})

$sourceFiles = @(
    "CPF_STABILIZATION_REPORT.md",
    "CPF_EVIDENCE_INDEX.md",
    "CPF_GAP_MATRIX.md",
    "specs/generated/sample-coverage-matrix.md",
    "specs/generated/feature-implementation-matrix.md"
)

foreach ($sourceFile in $sourceFiles) {
    $path = Join-Path $Root $sourceFile
    if (-not (Test-Path -LiteralPath $path)) {
        Add-Failure ("evidence source file missing: {0}" -f $sourceFile)
        continue
    }
    $text = Read-Utf8Text $path
    foreach ($match in [System.Text.RegularExpressions.Regex]::Matches($text, '\x60([^`]+)\x60')) {
        Add-EvidencePath $sourceFile $match.Groups[1].Value
    }
}

$uniqueRows = @($evidenceRows | Sort-Object evidencePath, sourceFile -Unique)
$missingRows = New-Object System.Collections.Generic.List[object]
$currentResultRelative = $ResultDir.Substring($Root.Length).TrimStart("\", "/").Replace("\", "/")
$headCommit = (& git -C $Root rev-parse HEAD).Trim()
foreach ($row in $uniqueRows) {
    $fullPath = Join-Path $Root ($row.evidencePath.Replace("/", "\"))
    if (-not (Test-Path -LiteralPath $fullPath)) {
        $missingRows.Add($row) | Out-Null
        Add-Failure ("evidence file missing: {0} referenced by {1}" -f $row.evidencePath, $row.sourceFile)
        continue
    }
    $item = Get-Item -LiteralPath $fullPath
    if ($item.Length -le 0) {
        $missingRows.Add($row) | Out-Null
        Add-Failure ("evidence file is empty: {0} referenced by {1}" -f $row.evidencePath, $row.sourceFile)
        continue
    }

    $trackedPaths = @(& git -C $Root ls-files -- $row.evidencePath)
    $tracked = $trackedPaths -contains $row.evidencePath
    $isCurrent = $row.evidencePath.StartsWith($currentResultRelative + "/", [System.StringComparison]::OrdinalIgnoreCase)
    if ($isCurrent -and -not $tracked) {
        $untrackedPaths = @(& git -C $Root ls-files --others --exclude-standard -- $row.evidencePath)
        if ($untrackedPaths -contains $row.evidencePath) {
            $currentUntrackedRows.Add($row) | Out-Null
        } else {
            Add-Failure ("current evidence is ignored or absent from the commit manifest: {0}" -f $row.evidencePath)
        }
    } elseif (-not $isCurrent -and -not $tracked) {
        $legacyUntrackedRows.Add($row) | Out-Null
    }
    $isRawCurrentLog = $isCurrent `
        -and $row.evidencePath.EndsWith(".log", [System.StringComparison]::OrdinalIgnoreCase) `
        -and -not $row.evidencePath.EndsWith(".sanitized.log", [System.StringComparison]::OrdinalIgnoreCase)
    if ($isRawCurrentLog) {
        Add-Failure ("current raw log cannot be used as evidence: {0}" -f $row.evidencePath)
    }

    if ($isCurrent -and $row.evidencePath.EndsWith(".sanitized.log", [System.StringComparison]::OrdinalIgnoreCase)) {
        $content = Read-Utf8Text $fullPath
        $commitMatch = [regex]::Match($content, '(?m)^START_COMMIT=([^\r\n]+)$')
        $idMatch = [regex]::Match($content, '(?m)^EVIDENCE_ID=([^\r\n]+)$')
        if (-not $commitMatch.Success -or $commitMatch.Groups[1].Value -notmatch '^[0-9a-fA-F]{40}$') {
            Add-Failure ("current evidence start commit is missing or invalid: {0}" -f $row.evidencePath)
        } else {
            $evidenceStartCommit = $commitMatch.Groups[1].Value.ToLowerInvariant()
            $currentStartCommits[$evidenceStartCommit] = $true
            & git -C $Root merge-base --is-ancestor $evidenceStartCommit $headCommit 2>$null
            if ($LASTEXITCODE -ne 0) {
                Add-Failure ("current evidence start commit is not an ancestor of HEAD: {0}" -f $row.evidencePath)
            }
        }
        if (-not $idMatch.Success) {
            Add-Failure ("current evidence id is missing: {0}" -f $row.evidencePath)
        } elseif ($currentEvidenceIds.ContainsKey($idMatch.Groups[1].Value) `
                -and $currentEvidenceIds[$idMatch.Groups[1].Value] -ne $row.evidencePath) {
            Add-Failure ("duplicate current evidence id: {0}" -f $idMatch.Groups[1].Value)
        } else {
            $currentEvidenceIds[$idMatch.Groups[1].Value] = $row.evidencePath
        }
    }
}

if ($currentStartCommits.Count -gt 1) {
    Add-Failure ("current sanitized logs use different start commits: {0}" -f (($currentStartCommits.Keys | Sort-Object) -join ", "))
}

Write-JsonEvidence "evidence-path-existence-check.sanitized.json" ([pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    status = $(if ($failures.Count -eq 0) { $statusDone } else { $statusFailed })
    checkedCount = $uniqueRows.Count
    missingCount = $missingRows.Count
    currentEvidenceIdCount = $currentEvidenceIds.Count
    currentStartCommits = @($currentStartCommits.Keys | Sort-Object)
    currentUntrackedCount = $currentUntrackedRows.Count
    legacyUntrackedCount = $legacyUntrackedRows.Count
    checked = $uniqueRows
    missing = $missingRows
    currentUntracked = $currentUntrackedRows
    legacyUntracked = $legacyUntrackedRows
    failures = $failures
})

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Error $_ }
    exit 1
}

Write-Host ("evidence path existence check passed: {0}" -f $ResultDir)
