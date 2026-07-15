param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "specs/evidence/20260714_02")
)

$ErrorActionPreference = "Stop"

$failures = New-Object System.Collections.Generic.List[string]

function Add-Failure {
    param([string] $Message)
    $failures.Add($Message)
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

function New-UnicodeText {
    param([int[]] $CodePoints)
    return -join ($CodePoints | ForEach-Object { [char] $_ })
}

function ConvertTo-PlainText {
    param([string] $Html)
    $withoutTags = [System.Text.RegularExpressions.Regex]::Replace($Html, "<[^>]+>", "")
    return [System.Net.WebUtility]::HtmlDecode($withoutTags).Trim()
}

function Get-AttributeValue {
    param(
        [string] $Tag,
        [string] $Name
    )

    $pattern = "\b" + [System.Text.RegularExpressions.Regex]::Escape($Name) + "\s*=\s*[""']([^""']+)[""']"
    $match = [System.Text.RegularExpressions.Regex]::Match(
        $Tag,
        $pattern,
        [System.Text.RegularExpressions.RegexOptions]::IgnoreCase
    )
    if ($match.Success) {
        return [System.Net.WebUtility]::HtmlDecode($match.Groups[1].Value)
    }
    return $null
}

$script:StatusDone = New-UnicodeText @(0xC644, 0xB8CC)
$script:StatusPartial = New-UnicodeText @(0xBD80, 0xBD84, 0x20, 0xAD6C, 0xD604)
$script:StatusNotImplemented = New-UnicodeText @(0xBBF8, 0xAD6C, 0xD604)
$script:StatusNotVerified = New-UnicodeText @(0xBBF8, 0xAC80, 0xC99D)
$script:StatusFailed = New-UnicodeText @(0xC2E4, 0xD328)
$script:StatusNeedsReview = New-UnicodeText @(0xC7AC, 0xD655, 0xC778, 0x20, 0xD544, 0xC694)
$script:NoEvidenceText = New-UnicodeText @(0xC5C6, 0xC74C)
$script:AllowedStatuses = @(
    $script:StatusDone,
    $script:StatusPartial,
    $script:StatusNotImplemented,
    $script:StatusNotVerified,
    $script:StatusFailed,
    $script:StatusNeedsReview
)

Write-JsonEvidence "report-matrix-evidence-consistency.sanitized.json" ([pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    status = $script:StatusNotVerified
    note = "self evidence file initialized before consistency scanning"
})

$script:RequiredCheckIds = @(
    "edu-mapper-db-slice",
    "mariadb-full-install",
    "adm-runtime",
    "adm-permission-runtime",
    "openapi-runtime",
    "adm-browser-click",
    "standard-header-e2e",
    "complex-transaction-trace",
    "transaction-segment-log",
    "adm-transaction-group-list",
    "adm-transaction-timeline",
    "cmn-fixed-length-engine",
    "composite-runtime-smoke",
    "adm-transaction-group-runtime",
    "redis-kafka-mq-broker",
    "broker-real-integration",
    "file-log-standard",
    "trace-boost-runtime",
    "bat-trace-boost-runtime",
    "runtime-start-services",
    "packaged-runtime-resources",
    "runtime-status-diagnostics",
    "runtime-closure",
    "adm-operation-console-runtime",
    "adm-log-policy-ui-static",
    "bat-log-bean-runtime",
    "exs-timeout-retry-runtime",
    "cmn-fixed-length-advanced",
    "create-domain-smoke",
    "pfw-service-call-engine",
    "adm-service-registry-runtime",
    "architecture-ownership-scan",
    "spring-event-usage-scan",
    "pfw-broker-capability",
    "pfw-file-transfer-capability",
    "pfw-security-credential-capability",
    "pfw-runtime-control-capability",
    "pfw-admin-status-capability",
    "profile-loading-standard",
    "packaged-dependencies-check",
    "deploy-dry-run-standard",
    "garbage-file-cleanup",
    "empty-directory-scan",
    "deploy-env-standard",
    "deploy-inventory-standard",
    "gradle-deploy-task-standard",
    "datasource-mode-standard",
    "local-port-duplicate-scan",
    "edu-module-deploy-alias-scan",
    "bat-edu-package",
    "bat-job-log-policy",
    "sample-coverage-matrix",
    "sample-placeholder-scan",
    "evidence-path-existence-check",
    "create-domain-profile-template",
    "runtime-smoke-summary",
    "check-report-matrix-evidence-consistency",
    "quality-gate",
    "check-docx-standard",
    "check-feature-evidence",
    "check-utf8"
)

function Test-AllowedStatus {
    param(
        [string] $Status,
        [string] $Context
    )

    if ($script:AllowedStatuses -notcontains $Status) {
        Add-Failure "unknown status [$Status]: $Context"
    }
}

function Get-MarkdownCheckStatusMap {
    param(
        [string] $Path,
        [string] $Name
    )

    $map = @{}
    if (-not (Test-Path -LiteralPath $Path)) {
        Add-Failure "$Name file missing: $Path"
        return $map
    }

    $text = [System.IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8)
    if ([string]::IsNullOrWhiteSpace($text)) {
        Add-Failure "$Name file is empty: $Path"
        return $map
    }

    foreach ($line in ($text -split "\r?\n")) {
        $trimmed = $line.Trim()
        if (-not $trimmed.StartsWith("|")) {
            continue
        }
        if ($trimmed -match '^\|\s*-+') {
            continue
        }
        $cells = @($trimmed.Trim("|") -split "\|" | ForEach-Object { $_.Trim() })
        if ($cells.Count -lt 3) {
            continue
        }
        $checkId = $cells[0]
        $status = $cells[1]
        if ($script:RequiredCheckIds -contains $checkId) {
            Test-AllowedStatus $status "${Name}:$checkId"
            $map[$checkId] = $status
        }
    }
    return $map
}

function Get-MatrixCheckStatusMap {
    param([string] $Path)

    $map = @{}
    if (-not (Test-Path -LiteralPath $Path)) {
        Add-Failure "feature matrix missing: $Path"
        return $map
    }

    try {
        $payload = [System.IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8) | ConvertFrom-Json
    } catch {
        Add-Failure "feature matrix json parse failed: $Path"
        return $map
    }
    foreach ($item in @($payload.items)) {
        $checkId = [string] $item.checkId
        $status = [string] $item.status
        if ([string]::IsNullOrWhiteSpace($checkId)) {
            Add-Failure "feature matrix check id is blank"
            continue
        }
        Test-AllowedStatus $status "feature-matrix:$checkId"
        $map[$checkId] = $status
    }
    return $map
}

function Get-EvidencePaths {
    param([string] $EvidenceCell)

    $paths = New-Object System.Collections.Generic.List[string]
    $matches = [System.Text.RegularExpressions.Regex]::Matches($EvidenceCell, '\x60([^\x60]+)\x60')
    foreach ($match in $matches) {
        $value = $match.Groups[1].Value.Trim()
        if ($value -eq $script:NoEvidenceText -or $value -eq "N/A" -or $value -eq $script:StatusNotVerified) {
            continue
        }
        if ($value -match '^(scripts/|scripts\\)') {
            continue
        }
        if ($value -match '^[A-Za-z]+:') {
            continue
        }
        $paths.Add($value)
    }
    return $paths
}

function Test-EvidenceFile {
    param(
        [string] $RelativePath,
        [string] $CheckId
    )

    $fullPath = Join-Path $Root $RelativePath
    if (-not (Test-Path -LiteralPath $fullPath)) {
        Add-Failure "evidence file missing [$CheckId]: $RelativePath"
        return
    }

    $item = Get-Item -LiteralPath $fullPath
    if ($item.Length -le 0) {
        Add-Failure "evidence file is empty [$CheckId]: $RelativePath"
        return
    }

    $stream = $null
    $reader = $null
    try {
        $stream = [System.IO.File]::Open(
            $fullPath,
            [System.IO.FileMode]::Open,
            [System.IO.FileAccess]::Read,
            [System.IO.FileShare]::ReadWrite
        )
        $reader = New-Object System.IO.StreamReader($stream, [System.Text.Encoding]::UTF8)
        $text = $reader.ReadToEnd()
    } catch {
        Add-Failure "evidence file read failed [$CheckId]: $RelativePath :: $($_.Exception.Message)"
        return
    } finally {
        if ($null -ne $reader) {
            $reader.Dispose()
        } elseif ($null -ne $stream) {
            $stream.Dispose()
        }
    }
    if ($RelativePath -match '\.json$') {
        try {
            $null = $text | ConvertFrom-Json
        } catch {
            Add-Failure "evidence json parse failed [$CheckId]: $RelativePath :: $($_.Exception.Message)"
        }
    }

    $sensitivePatterns = @(
        'Bearer\s+[A-Za-z0-9_\-\.=]+',
        '"Authorization"\s*:\s*"(?!\*\*\*)[^"]+"',
        '"X-Api-Key"\s*:\s*"(?!\*\*\*)[^"]+"',
        '"password"\s*:\s*"(?!\*\*\*)[^"]+"',
        '"secret"\s*:\s*"(?!\*\*\*)[^"]+"',
        '"credential"\s*:\s*"(?!\*\*\*)[^"]+"',
        '"signature"\s*:\s*"(?!\*\*\*)[^"]+"'
    )
    foreach ($pattern in $sensitivePatterns) {
        if ($text -match $pattern) {
            Add-Failure "evidence sensitive token pattern found [$CheckId]: $RelativePath"
            break
        }
    }
}

$reportPath = Join-Path $Root "CPF_STABILIZATION_REPORT.md"
$evidenceIndexPath = Join-Path $Root "CPF_EVIDENCE_INDEX.md"
$featureMatrixFileName = (New-UnicodeText @(0xAE30, 0xB2A5, 0x5F, 0xAD6C, 0xD604, 0x5F, 0xB9E4, 0xD2B8, 0xB9AD, 0xC2A4)) + ".json"
$matrixPath = Join-Path (Join-Path $Root "specs") $featureMatrixFileName

$reportMap = Get-MarkdownCheckStatusMap $reportPath "stabilization-report"
$evidenceMap = Get-MarkdownCheckStatusMap $evidenceIndexPath "evidence-index"
$matrixMap = Get-MatrixCheckStatusMap $matrixPath

$evidencePathMap = @{}
if (Test-Path -LiteralPath $evidenceIndexPath) {
    $evidenceText = [System.IO.File]::ReadAllText($evidenceIndexPath, [System.Text.Encoding]::UTF8)
    foreach ($line in ($evidenceText -split "\r?\n")) {
        $trimmed = $line.Trim()
        if (-not $trimmed.StartsWith("|")) {
            continue
        }
        if ($trimmed -match '^\|\s*-+') {
            continue
        }
        $cells = @($trimmed.Trim("|") -split "\|" | ForEach-Object { $_.Trim() })
        if ($cells.Count -lt 3) {
            continue
        }
        $checkId = $cells[0]
        if ($script:RequiredCheckIds -contains $checkId) {
            $evidencePathMap[$checkId] = @(Get-EvidencePaths $cells[2])
        }
    }
}

foreach ($checkId in $script:RequiredCheckIds) {
    if (-not $reportMap.ContainsKey($checkId)) {
        Add-Failure "report check id missing: $checkId"
        continue
    }
    if (-not $matrixMap.ContainsKey($checkId)) {
        Add-Failure "feature matrix check id missing: $checkId"
        continue
    }
    if (-not $evidenceMap.ContainsKey($checkId)) {
        Add-Failure "evidence index check id missing: $checkId"
        continue
    }

    if ($reportMap[$checkId] -ne $matrixMap[$checkId]) {
        Add-Failure "report/matrix status mismatch [$checkId]: report=[$($reportMap[$checkId])], matrix=[$($matrixMap[$checkId])]"
    }
    if ($reportMap[$checkId] -ne $evidenceMap[$checkId]) {
        Add-Failure "report/evidence status mismatch [$checkId]: report=[$($reportMap[$checkId])], evidence=[$($evidenceMap[$checkId])]"
    }

    $paths = @()
    if ($evidencePathMap.ContainsKey($checkId)) {
        $paths = @($evidencePathMap[$checkId])
    }

    if ($reportMap[$checkId] -eq $script:StatusDone -and $paths.Count -eq 0) {
        Add-Failure "done check has no evidence file [$checkId]"
    }

    foreach ($relativePath in $paths) {
        Test-EvidenceFile $relativePath $checkId
    }
}

Write-JsonEvidence "report-matrix-evidence-consistency.sanitized.json" ([pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    status = $(if ($failures.Count -eq 0) { $script:StatusDone } else { $script:StatusFailed })
    requiredCheckCount = $script:RequiredCheckIds.Count
    failureCount = $failures.Count
    failures = $failures
})

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Host $_ }
    exit 1
}

Write-Host "Report, feature matrix, and evidence index consistency check passed."
