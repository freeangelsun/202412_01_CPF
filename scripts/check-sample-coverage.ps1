param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $MatrixPath = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "specs/sample-coverage-matrix.md"),
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "specs/evidence/20260708_04")
)

$ErrorActionPreference = "Stop"

function New-UnicodeText {
    param([int[]] $CodePoints)
    return -join ($CodePoints | ForEach-Object { [char] $_ })
}

$statusDone = New-UnicodeText @(0xC644, 0xB8CC)
$statusPartial = New-UnicodeText @(0xBD80, 0xBD84, 0x20, 0xAD6C, 0xD604)
$statusNotImplemented = New-UnicodeText @(0xBBF8, 0xAD6C, 0xD604)
$statusNotVerified = New-UnicodeText @(0xBBF8, 0xAC80, 0xC99D)
$statusFailed = New-UnicodeText @(0xC2E4, 0xD328)
$statusNeedsReview = New-UnicodeText @(0xC7AC, 0xD655, 0xC778, 0x20, 0xD544, 0xC694)
$noEvidenceText = New-UnicodeText @(0xC5C6, 0xC74C)
$allowedStatuses = @($statusDone, $statusPartial, $statusNotImplemented, $statusNotVerified, $statusFailed, $statusNeedsReview)
$allowedValidationLevels = @(
    "source-compile",
    "unit-test",
    "contract-test",
    "fixture-test",
    "source-smoke",
    "profile-smoke",
    "runtime-smoke",
    "external-runtime-required"
)
$requiredColumns = @(
    "sampleId",
    "module",
    "package",
    "featureArea",
    "sampleName",
    "sourcePath",
    "testPath",
    "evidencePath",
    "validationLevel",
    "runtimeRequired",
    "runtimeExecuted",
    "status",
    "notes"
)
$requiredSampleIds = @(
    "XYZ-EDU-CRUD-001","XYZ-EDU-CRUD-002","XYZ-EDU-CRUD-003","XYZ-EDU-CRUD-004","XYZ-EDU-CRUD-005","XYZ-EDU-CRUD-006",
    "XYZ-EDU-LIST-001","XYZ-EDU-LIST-002","XYZ-EDU-LIST-003","XYZ-EDU-LIST-004","XYZ-EDU-LIST-005","XYZ-EDU-LIST-006","XYZ-EDU-LIST-007",
    "XYZ-EDU-PAGE-001","XYZ-EDU-PAGE-002","XYZ-EDU-PAGE-003","XYZ-EDU-PAGE-004",
    "XYZ-EDU-DETAIL-001","XYZ-EDU-DETAIL-002","XYZ-EDU-DETAIL-003","XYZ-EDU-DETAIL-004",
    "XYZ-EDU-TRX-001","XYZ-EDU-TRX-002","XYZ-EDU-TRX-003","XYZ-EDU-TRX-004","XYZ-EDU-TRX-005","XYZ-EDU-TRX-006","XYZ-EDU-TRX-007",
    "XYZ-EDU-CALL-001","XYZ-EDU-CALL-002","XYZ-EDU-CALL-003","XYZ-EDU-CALL-004","XYZ-EDU-CALL-005","XYZ-EDU-CALL-006","XYZ-EDU-CALL-007","XYZ-EDU-CALL-008","XYZ-EDU-CALL-009","XYZ-EDU-CALL-010",
    "XYZ-EDU-HEADER-001","XYZ-EDU-HEADER-002","XYZ-EDU-HEADER-003","XYZ-EDU-HEADER-004",
    "XYZ-EDU-IDEMP-001","XYZ-EDU-IDEMP-002","XYZ-EDU-IDEMP-003",
    "XYZ-EDU-FAIL-001","XYZ-EDU-FAIL-002","XYZ-EDU-FAIL-003","XYZ-EDU-FAIL-004",
    "XYZ-EDU-SEC-001","XYZ-EDU-SEC-002","XYZ-EDU-SEC-003","XYZ-EDU-SEC-004","XYZ-EDU-SEC-005",
    "XYZ-EDU-AUDIT-001","XYZ-EDU-AUDIT-002","XYZ-EDU-AUDIT-003",
    "XYZ-EDU-VALID-001","XYZ-EDU-VALID-002","XYZ-EDU-VALID-003","XYZ-EDU-VALID-004",
    "XYZ-EDU-OPER-001","XYZ-EDU-OPER-002",
    "BAT-EDU-JOB-001","BAT-EDU-JOB-002","BAT-EDU-JOB-003","BAT-EDU-JOB-004","BAT-EDU-JOB-005","BAT-EDU-JOB-006","BAT-EDU-JOB-007","BAT-EDU-JOB-008","BAT-EDU-JOB-009",
    "BAT-EDU-TRX-001","BAT-EDU-TRX-002","BAT-EDU-TRX-003","BAT-EDU-TRX-004",
    "BAT-EDU-CALL-001","BAT-EDU-CALL-002","BAT-EDU-CALL-003","BAT-EDU-CALL-004","BAT-EDU-CALL-005","BAT-EDU-CALL-006",
    "BAT-EDU-CENTER-001","BAT-EDU-CENTER-002","BAT-EDU-CENTER-003","BAT-EDU-CENTER-004","BAT-EDU-CENTER-005","BAT-EDU-CENTER-006","BAT-EDU-CENTER-007",
    "BAT-EDU-LOG-001","BAT-EDU-LOG-002","BAT-EDU-LOG-003","BAT-EDU-LOG-004","BAT-EDU-LOG-005",
    "BAT-EDU-IDEMP-001","BAT-EDU-IDEMP-002","BAT-EDU-UNKNOWN-001","BAT-EDU-RECON-001","BAT-EDU-RECON-002",
    "CMN-EDU-FIXED-001","CMN-EDU-FIXED-002","CMN-EDU-FIXED-003","CMN-EDU-FIXED-004","CMN-EDU-FIXED-005","CMN-EDU-FIXED-006","CMN-EDU-FIXED-007","CMN-EDU-FIXED-008","CMN-EDU-FIXED-009","CMN-EDU-FIXED-010","CMN-EDU-FIXED-011",
    "CMN-EDU-MSG-001","CMN-EDU-CODE-001","CMN-EDU-VALID-001","CMN-EDU-CONV-001","CMN-EDU-FILE-001","CMN-EDU-FILE-002","CMN-EDU-FIXTURE-001","CMN-EDU-MASK-001",
    "EXS-EDU-FIXED-001","EXS-EDU-FIXED-002","EXS-EDU-FIXED-003","EXS-EDU-FIXED-004","EXS-EDU-ENDPOINT-001","EXS-EDU-ENDPOINT-002","EXS-EDU-AUTH-001","EXS-EDU-AUTH-002","EXS-EDU-AUTH-003","EXS-EDU-CALL-001",
    "EXS-EDU-RETRY-001","EXS-EDU-RETRY-002","EXS-EDU-RETRY-003","EXS-EDU-RETRY-004","EXS-EDU-UNKNOWN-001","EXS-EDU-RECON-001","EXS-EDU-LEDGER-001","EXS-EDU-IDEMP-001","EXS-EDU-FILE-001","EXS-EDU-FILE-002",
    "PFW-EDU-CALL-001","PFW-EDU-CALL-002","PFW-EDU-CALL-003","PFW-EDU-CALL-004","PFW-EDU-CALL-005","PFW-EDU-CALL-006","PFW-EDU-CALL-007","PFW-EDU-CALL-008","PFW-EDU-CALL-009","PFW-EDU-CALL-010",
    "PFW-EDU-BROKER-001","PFW-EDU-BROKER-002","PFW-EDU-BROKER-003","PFW-EDU-BROKER-004","PFW-EDU-BROKER-005","PFW-EDU-BROKER-006","PFW-EDU-BROKER-007","PFW-EDU-BROKER-008",
    "PFW-EDU-FILE-001","PFW-EDU-FILE-002","PFW-EDU-FILE-003","PFW-EDU-FILE-004","PFW-EDU-FILE-005","PFW-EDU-FILE-006",
    "PFW-EDU-SEC-001","PFW-EDU-SEC-002","PFW-EDU-SEC-003","PFW-EDU-SEC-004","PFW-EDU-SEC-005",
    "PFW-EDU-RUNTIME-001","PFW-EDU-RUNTIME-002","PFW-EDU-RUNTIME-003","PFW-EDU-RUNTIME-004","PFW-EDU-RUNTIME-005","PFW-EDU-RUNTIME-006","PFW-EDU-ADMIN-001","PFW-EDU-AUDIT-001","PFW-EDU-MASK-001",
    "ADM-EDU-OPR-001","ADM-EDU-OPR-002","ADM-EDU-OPR-003","ADM-EDU-OPR-004","ADM-EDU-OPR-005","ADM-EDU-OPR-006","ADM-EDU-OPR-007","ADM-EDU-OPR-008","ADM-EDU-OPR-009","ADM-EDU-OPR-010",
    "BIZADM-EDU-AUTH-001","BIZADM-EDU-AUTH-002","BIZADM-EDU-AUTH-003","BIZADM-EDU-AUDIT-001","BIZADM-EDU-DOWNLOAD-001","BIZADM-EDU-MASK-001"
)

$failures = New-Object System.Collections.Generic.List[string]

function Add-Failure {
    param([string] $Message)
    $failures.Add($Message) | Out-Null
}

function Join-RootPath {
    param([string] $RelativePath)
    return Join-Path $Root ($RelativePath -replace '/', '\')
}

function Write-JsonEvidence {
    param(
        [string] $FileName,
        [object] $Value
    )
    New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
    $path = Join-Path $ResultDir $FileName
    $json = $Value | ConvertTo-Json -Depth 16
    [System.IO.File]::WriteAllText($path, $json, [System.Text.UTF8Encoding]::new($false))
}

function Test-ReasonText {
    param([string] $Value)
    return $Value -eq $noEvidenceText -or
        $Value -eq $statusNotImplemented -or
        $Value -eq $statusNotVerified -or
        $Value -match '^(N/A|external-runtime-required|reason:)'
}

if (-not (Test-Path -LiteralPath $MatrixPath)) {
    Add-Failure ("sample coverage matrix missing: {0}" -f $MatrixPath)
} else {
    $lines = [System.IO.File]::ReadAllLines($MatrixPath, [System.Text.Encoding]::UTF8)
    $tableLines = @($lines | Where-Object { $_.Trim().StartsWith("|") })
    if ($tableLines.Count -lt 3) {
        Add-Failure "sample coverage matrix table is missing or empty"
    } else {
        $header = @($tableLines[0].Trim().Trim("|") -split "\|" | ForEach-Object { $_.Trim() })
        for ($i = 0; $i -lt $requiredColumns.Count; $i++) {
            if ($header.Count -le $i -or $header[$i] -ne $requiredColumns[$i]) {
                Add-Failure ("sample matrix column mismatch at {0}: expected {1}" -f $i, $requiredColumns[$i])
            }
        }

        $rows = New-Object System.Collections.Generic.List[object]
        foreach ($line in $tableLines | Select-Object -Skip 2) {
            $cells = @($line.Trim().Trim("|") -split "\|" | ForEach-Object { $_.Trim() })
            if ($cells.Count -lt $requiredColumns.Count) {
                Add-Failure ("sample matrix row has too few columns: {0}" -f $line)
                continue
            }
            $row = [ordered]@{}
            for ($i = 0; $i -lt $requiredColumns.Count; $i++) {
                $row[$requiredColumns[$i]] = $cells[$i]
            }
            $rows.Add([pscustomobject]$row) | Out-Null
        }

        $ids = @($rows | ForEach-Object { $_.sampleId })
        foreach ($requiredId in $requiredSampleIds) {
            if ($ids -notcontains $requiredId) {
                Add-Failure ("required sampleId missing: {0}" -f $requiredId)
            }
        }
        $duplicateIds = @($ids | Group-Object | Where-Object { $_.Count -gt 1 })
        foreach ($duplicate in $duplicateIds) {
            Add-Failure ("duplicate sampleId: {0}" -f $duplicate.Name)
        }

        foreach ($row in $rows) {
            if ($allowedStatuses -notcontains $row.status) {
                Add-Failure ("invalid sample status: {0} :: {1}" -f $row.sampleId, $row.status)
            }
            if ($allowedValidationLevels -notcontains $row.validationLevel) {
                Add-Failure ("invalid validation level: {0} :: {1}" -f $row.sampleId, $row.validationLevel)
            }
            if ($row.runtimeRequired -notin @("true", "false")) {
                Add-Failure ("runtimeRequired must be true or false: {0}" -f $row.sampleId)
            }
            if ($row.runtimeExecuted -notin @("true", "false")) {
                Add-Failure ("runtimeExecuted must be true or false: {0}" -f $row.sampleId)
            }
            if ($row.runtimeRequired -eq "true" -and $row.runtimeExecuted -eq "true" -and $row.validationLevel -eq "external-runtime-required") {
                Add-Failure ("external runtime sample cannot be executed without real runtime evidence: {0}" -f $row.sampleId)
            }
            if ([string]::IsNullOrWhiteSpace($row.sourcePath)) {
                Add-Failure ("sample sourcePath is blank: {0}" -f $row.sampleId)
            } elseif (-not (Test-Path -LiteralPath (Join-RootPath $row.sourcePath))) {
                Add-Failure ("sample sourcePath missing: {0} :: {1}" -f $row.sampleId, $row.sourcePath)
            }
            if (-not (Test-ReasonText $row.testPath) -and -not (Test-Path -LiteralPath (Join-RootPath $row.testPath))) {
                Add-Failure ("sample testPath missing: {0} :: {1}" -f $row.sampleId, $row.testPath)
            }
            if ($row.status -eq $statusDone -and (Test-ReasonText $row.evidencePath)) {
                Add-Failure ("completed sample must have evidence path: {0}" -f $row.sampleId)
            }
        }

        $statusSummary = $rows | Group-Object status | ForEach-Object {
            [pscustomobject]@{
                status = $_.Name
                count = $_.Count
            }
        }
        $moduleSummary = $rows | Group-Object module | ForEach-Object {
            [pscustomobject]@{
                module = $_.Name
                count = $_.Count
            }
        }
        $placeholderFindings = New-Object System.Collections.Generic.List[object]
        foreach ($sourcePath in @($rows | Select-Object -ExpandProperty sourcePath -Unique)) {
            if (Test-ReasonText $sourcePath) {
                continue
            }
            $fullPath = Join-RootPath $sourcePath
            if (-not (Test-Path -LiteralPath $fullPath)) {
                continue
            }
            $text = [System.IO.File]::ReadAllText($fullPath, [System.Text.Encoding]::UTF8)
            foreach ($pattern in @("placeholder-only", "TODO sample", "throw new UnsupportedOperationException")) {
                if ($text.IndexOf($pattern, [System.StringComparison]::OrdinalIgnoreCase) -ge 0) {
                    $placeholderFindings.Add([pscustomobject]@{
                        sourcePath = $sourcePath
                        pattern = $pattern
                    }) | Out-Null
                }
            }
        }
        foreach ($finding in $placeholderFindings) {
            Add-Failure ("placeholder-only sample source detected: {0} :: {1}" -f $finding.sourcePath, $finding.pattern)
        }

        Write-JsonEvidence "sample-placeholder-scan.sanitized.json" ([pscustomobject]@{
            generatedAt = (Get-Date).ToString("o")
            status = $(if ($placeholderFindings.Count -eq 0) { $statusDone } else { $statusFailed })
            findings = $placeholderFindings
        })
        Write-JsonEvidence "sample-coverage-result.sanitized.json" ([pscustomobject]@{
            generatedAt = (Get-Date).ToString("o")
            status = $(if ($failures.Count -eq 0) { $statusDone } else { $statusFailed })
            requiredSampleCount = $requiredSampleIds.Count
            matrixSampleCount = $rows.Count
            statusSummary = $statusSummary
            moduleSummary = $moduleSummary
            failures = $failures
        })
    }
}

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Error $_ }
    exit 1
}

Write-Host ("sample coverage check passed: {0}" -f $MatrixPath)
