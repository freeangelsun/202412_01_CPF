param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $MatrixPath = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "specs/sample-coverage-matrix.md"),
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "specs/evidence/20260713_02")
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
    "XYZ-EDU-TLM-001","XYZ-EDU-TLM-002","XYZ-EDU-TLM-003","XYZ-EDU-TLM-004",
    "XYZ-EDU-MSG-001","XYZ-EDU-MSG-002","XYZ-EDU-MSG-003","XYZ-EDU-MSG-004","XYZ-EDU-MSG-005",
    "XYZ-EDU-FILE-001","XYZ-EDU-FILE-002","XYZ-EDU-FILE-003","XYZ-EDU-FILE-004","XYZ-EDU-FILE-005","XYZ-EDU-FILE-006","XYZ-EDU-FILE-007","XYZ-EDU-FILE-008",
    "XYZ-EDU-ARCHIVE-001","XYZ-EDU-ARCHIVE-002","XYZ-EDU-ARCHIVE-003","XYZ-EDU-ARCHIVE-004",
    "XYZ-EDU-OPER-001","XYZ-EDU-OPER-002",
    "BAT-EDU-JOB-001","BAT-EDU-JOB-002","BAT-EDU-JOB-003","BAT-EDU-JOB-004","BAT-EDU-JOB-005","BAT-EDU-JOB-006","BAT-EDU-JOB-007","BAT-EDU-JOB-008","BAT-EDU-JOB-009",
    "BAT-EDU-TRX-001","BAT-EDU-TRX-002","BAT-EDU-TRX-003","BAT-EDU-TRX-004",
    "BAT-EDU-CALL-001","BAT-EDU-CALL-002","BAT-EDU-CALL-003","BAT-EDU-CALL-004","BAT-EDU-CALL-005","BAT-EDU-CALL-006",
    "BAT-EDU-CENTER-001","BAT-EDU-CENTER-002","BAT-EDU-CENTER-003","BAT-EDU-CENTER-004","BAT-EDU-CENTER-005","BAT-EDU-CENTER-006","BAT-EDU-CENTER-007",
    "BAT-EDU-LOG-001","BAT-EDU-LOG-002","BAT-EDU-LOG-003","BAT-EDU-LOG-004","BAT-EDU-LOG-005",
    "BAT-EDU-IDEMP-001","BAT-EDU-IDEMP-002","BAT-EDU-UNKNOWN-001","BAT-EDU-RECON-001","BAT-EDU-RECON-002",
    "BAT-EDU-ARCHIVE-001","BAT-EDU-ARCHIVE-002","BAT-EDU-ARCHIVE-003",
    "CMN-EDU-FIXED-001","CMN-EDU-FIXED-002","CMN-EDU-FIXED-003","CMN-EDU-FIXED-004","CMN-EDU-FIXED-005","CMN-EDU-FIXED-006","CMN-EDU-FIXED-007","CMN-EDU-FIXED-008","CMN-EDU-FIXED-009","CMN-EDU-FIXED-010","CMN-EDU-FIXED-011",
    "CMN-EDU-MSG-001","CMN-EDU-CODE-001","CMN-EDU-VALID-001","CMN-EDU-CONV-001","CMN-EDU-FILE-001","CMN-EDU-FILE-002","CMN-EDU-FIXTURE-001","CMN-EDU-MASK-001",
    "EXS-EDU-FIXED-001","EXS-EDU-FIXED-002","EXS-EDU-FIXED-003","EXS-EDU-FIXED-004","EXS-EDU-ENDPOINT-001","EXS-EDU-ENDPOINT-002","EXS-EDU-AUTH-001","EXS-EDU-AUTH-002","EXS-EDU-AUTH-003","EXS-EDU-CALL-001",
    "EXS-EDU-RETRY-001","EXS-EDU-RETRY-002","EXS-EDU-RETRY-003","EXS-EDU-RETRY-004","EXS-EDU-UNKNOWN-001","EXS-EDU-RECON-001","EXS-EDU-LEDGER-001","EXS-EDU-IDEMP-001","EXS-EDU-FILE-001","EXS-EDU-FILE-002",
    "EXS-EDU-ARCHIVE-001","EXS-EDU-ARCHIVE-002",
    "PFW-EDU-CALL-001","PFW-EDU-CALL-002","PFW-EDU-CALL-003","PFW-EDU-CALL-004","PFW-EDU-CALL-005","PFW-EDU-CALL-006","PFW-EDU-CALL-007","PFW-EDU-CALL-008","PFW-EDU-CALL-009","PFW-EDU-CALL-010",
    "PFW-EDU-BROKER-001","PFW-EDU-BROKER-002","PFW-EDU-BROKER-003","PFW-EDU-BROKER-004","PFW-EDU-BROKER-005","PFW-EDU-BROKER-006","PFW-EDU-BROKER-007","PFW-EDU-BROKER-008","PFW-EDU-BROKER-009",
    "PFW-EDU-FILE-001","PFW-EDU-FILE-002","PFW-EDU-FILE-003","PFW-EDU-FILE-004","PFW-EDU-FILE-005","PFW-EDU-FILE-006","PFW-EDU-FILE-007",
    "PFW-EDU-IDEMP-001","PFW-EDU-IDEMP-002","PFW-EDU-RECON-001","PFW-EDU-RECON-002",
    "PFW-EDU-ARCHIVE-001","PFW-EDU-ARCHIVE-002","PFW-EDU-ARCHIVE-003","PFW-EDU-ARCHIVE-004","PFW-EDU-ARCHIVE-005","PFW-EDU-ARCHIVE-006","PFW-EDU-ARCHIVE-007","PFW-EDU-ARCHIVE-008","PFW-EDU-ARCHIVE-009","PFW-EDU-ARCHIVE-010","PFW-EDU-ARCHIVE-011","PFW-EDU-ARCHIVE-012","PFW-EDU-ARCHIVE-013","PFW-EDU-ARCHIVE-014","PFW-EDU-ARCHIVE-015","PFW-EDU-ARCHIVE-016",
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
            if (-not (Test-ReasonText $row.evidencePath) -and -not (Test-Path -LiteralPath (Join-RootPath $row.evidencePath))) {
                Add-Failure ("sample evidencePath missing: {0} :: {1}" -f $row.sampleId, $row.evidencePath)
            }
        }

        $catalogOnlyRows = @($rows | Where-Object {
                $_.sourcePath -match 'EducationCoverageCatalog\.java$' -or $_.testPath -match 'EducationCoverageCatalogTest\.java$'
            })
        foreach ($row in $catalogOnlyRows) {
            if ($row.status -eq $statusDone) {
                Add-Failure ("catalog-only sample cannot be completed: {0}" -f $row.sampleId)
            }
            if ($row.validationLevel -eq "unit-test" -and $row.testPath -match 'EducationCoverageCatalogTest\.java$' -and $row.status -eq $statusDone) {
                Add-Failure ("CoverageCatalogTest cannot be functional unit evidence: {0}" -f $row.sampleId)
            }
        }

        $duplicateSourceFindings = New-Object System.Collections.Generic.List[object]
        $actualSourceRows = @($rows | Where-Object {
                -not (Test-ReasonText $_.sourcePath) -and $_.sourcePath -notmatch 'EducationCoverageCatalog\.java$'
            })
        foreach ($group in @($actualSourceRows | Group-Object sourcePath | Where-Object { $_.Count -gt 5 })) {
            $duplicateSourceFindings.Add([pscustomobject]@{
                sourcePath = $group.Name
                sampleIds = @($group.Group | ForEach-Object { $_.sampleId })
                count = $group.Count
            }) | Out-Null
            Add-Failure ("one sample source represents too many sampleIds: {0} :: {1}" -f $group.Name, $group.Count)
        }

        $actualSampleFiles = @(
            Get-ChildItem -LiteralPath $Root -Recurse -File -Filter "*EducationSample.java" |
                Where-Object {
                    $_.FullName -notmatch '\\build\\' -and
                    $_.FullName -notmatch 'EducationCoverageCatalog\.java$'
                } |
                ForEach-Object { $_.FullName.Substring($Root.Length).TrimStart('\') -replace '\\', '/' }
        )
        $actualSampleTestFiles = @(
            Get-ChildItem -LiteralPath $Root -Recurse -File -Filter "*EducationSampleTest.java" |
                Where-Object {
                    $_.FullName -notmatch '\\build\\' -and
                    $_.FullName -notmatch 'EducationCoverageCatalogTest\.java$'
                } |
                ForEach-Object { $_.FullName.Substring($Root.Length).TrimStart('\') -replace '\\', '/' }
        )
        $batSamples = @($actualSampleFiles | Where-Object { $_ -like 'bat/src/main/java/cpf/bat/edu/*' })
        $batTests = @($actualSampleTestFiles | Where-Object { $_ -like 'bat/src/test/java/cpf/bat/edu/*' })
        $xyzSamples = @($actualSampleFiles | Where-Object { $_ -like 'xyz/src/main/java/cpf/xyz/edu/*' })
        $xyzTests = @($actualSampleTestFiles | Where-Object { $_ -like 'xyz/src/test/java/cpf/xyz/edu/*' })
        $cmnSamples = @($actualSampleFiles | Where-Object { $_ -like 'cmn/src/main/java/cpf/cmn/edu/*' })
        $cmnTests = @($actualSampleTestFiles | Where-Object { $_ -like 'cmn/src/test/java/cpf/cmn/edu/*' })
        $exsSamples = @($actualSampleFiles | Where-Object { $_ -like 'exs/src/main/java/cpf/exs/edu/*' })
        $exsTests = @($actualSampleTestFiles | Where-Object { $_ -like 'exs/src/test/java/cpf/exs/edu/*' })
        $pfwSamples = @($actualSampleFiles | Where-Object { $_ -like 'pfw/src/main/java/cpf/pfw/common/*' })
        $pfwTests = @($actualSampleTestFiles | Where-Object { $_ -like 'pfw/src/test/java/cpf/pfw/common/*' })
        $admBizSamples = @($actualSampleFiles | Where-Object { $_ -like 'adm/src/main/java/cpf/adm/edu/*' -or $_ -like 'bizadm/src/main/java/cpf/bizadm/edu/*' })
        $admBizTests = @($actualSampleTestFiles | Where-Object { $_ -like 'adm/src/test/java/cpf/adm/edu/*' -or $_ -like 'bizadm/src/test/java/cpf/bizadm/edu/*' })

        if ($batSamples.Count -lt 15) {
            Add-Failure ("BAT edu actual sample class count is below minimum: {0}" -f $batSamples.Count)
        }
        if ($batTests.Count -lt 15) {
            Add-Failure ("BAT edu actual test class count is below minimum: {0}" -f $batTests.Count)
        }
        if ($xyzSamples.Count -lt 20) {
            Add-Failure ("XYZ edu actual sample class count is below minimum: {0}" -f $xyzSamples.Count)
        }
        if ($xyzTests.Count -lt 20) {
            Add-Failure ("XYZ edu actual test class count is below minimum: {0}" -f $xyzTests.Count)
        }
        if ($cmnSamples.Count -lt 5 -or $cmnTests.Count -lt 5) {
            Add-Failure ("CMN edu fixed/helper sample coverage is below minimum.")
        }
        if ($exsSamples.Count -lt 5 -or $exsTests.Count -lt 5) {
            Add-Failure ("EXS edu adapter sample coverage is below minimum.")
        }
        if ($pfwSamples.Count -lt 5 -or $pfwTests.Count -lt 5) {
            Add-Failure ("PFW capability sample coverage is below minimum.")
        }
        if ($admBizSamples.Count -lt 5 -or $admBizTests.Count -lt 5) {
            Add-Failure ("ADM/BIZADM operation/auth sample coverage is below minimum.")
        }

        $requiredPackageFragments = @(
            'bat/src/main/java/cpf/bat/edu/job/',
            'bat/src/main/java/cpf/bat/edu/chunk/',
            'bat/src/main/java/cpf/bat/edu/servicecall/',
            'bat/src/main/java/cpf/bat/edu/archive/',
            'xyz/src/main/java/cpf/xyz/edu/servicecall/',
            'xyz/src/main/java/cpf/xyz/edu/header/',
            'xyz/src/main/java/cpf/xyz/edu/messaging/',
            'xyz/src/main/java/cpf/xyz/edu/archive/',
            'cmn/src/main/java/cpf/cmn/edu/fixedlength/',
            'exs/src/main/java/cpf/exs/edu/fixedlength/',
            'pfw/src/main/java/cpf/pfw/common/archive/'
        )
        $missingPackageFragments = @($requiredPackageFragments | Where-Object {
                $fragment = $_
                -not ($actualSampleFiles | Where-Object { $_.StartsWith($fragment) } | Select-Object -First 1)
            })
        foreach ($fragment in $missingPackageFragments) {
            Add-Failure ("required edu package has no actual sample: {0}" -f $fragment)
        }

        $archiveFiles = @(
            'pfw/src/main/java/cpf/pfw/common/archive/CpfArchiveService.java',
            'pfw/src/main/java/cpf/pfw/common/archive/LocalCpfArchiveService.java',
            'pfw/src/test/java/cpf/pfw/common/archive/CpfArchiveServiceTest.java',
            'pfw/src/test/java/cpf/pfw/common/archive/CpfZipSlipGuardTest.java',
            'pfw/src/test/java/cpf/pfw/common/archive/CpfArchiveChecksumTest.java',
            'xyz/src/main/java/cpf/xyz/edu/archive/XyzArchiveEducationSample.java',
            'bat/src/main/java/cpf/bat/edu/archive/BatArchiveCompressionEducationSample.java',
            'exs/src/main/java/cpf/exs/edu/archive/ExsArchiveEducationSample.java'
        )
        $missingArchiveFiles = @($archiveFiles | Where-Object { -not (Test-Path -LiteralPath (Join-RootPath $_)) })
        foreach ($file in $missingArchiveFiles) {
            Add-Failure ("archive/compression coverage file missing: {0}" -f $file)
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
        Write-JsonEvidence "sample-catalog-only-scan.sanitized.json" ([pscustomobject]@{
            generatedAt = (Get-Date).ToString("o")
            status = $(if (@($catalogOnlyRows | Where-Object { $_.status -eq $statusDone }).Count -eq 0) { $statusDone } else { $statusFailed })
            catalogOnlyCount = $catalogOnlyRows.Count
            completedCatalogOnly = @($catalogOnlyRows | Where-Object { $_.status -eq $statusDone } | ForEach-Object { $_.sampleId })
        })
        Write-JsonEvidence "sample-source-duplication-scan.sanitized.json" ([pscustomobject]@{
            generatedAt = (Get-Date).ToString("o")
            status = $(if ($duplicateSourceFindings.Count -eq 0) { $statusDone } else { $statusFailed })
            findings = $duplicateSourceFindings
        })
        Write-JsonEvidence "sample-actual-implementation-scan.sanitized.json" ([pscustomobject]@{
            generatedAt = (Get-Date).ToString("o")
            status = $(if ($actualSampleFiles.Count -gt 0 -and $actualSampleTestFiles.Count -gt 0) { $statusDone } else { $statusFailed })
            sampleCount = $actualSampleFiles.Count
            testCount = $actualSampleTestFiles.Count
            samples = $actualSampleFiles
            tests = $actualSampleTestFiles
        })
        Write-JsonEvidence "sample-package-structure-scan.sanitized.json" ([pscustomobject]@{
            generatedAt = (Get-Date).ToString("o")
            status = $(if ($missingPackageFragments.Count -eq 0) { $statusDone } else { $statusFailed })
            missingPackageFragments = $missingPackageFragments
        })
        Write-JsonEvidence "bat-edu-actual-sample-scan.sanitized.json" ([pscustomobject]@{
            generatedAt = (Get-Date).ToString("o")
            status = $(if ($batSamples.Count -ge 15 -and $batTests.Count -ge 15) { $statusDone } else { $statusFailed })
            sampleCount = $batSamples.Count
            testCount = $batTests.Count
        })
        Write-JsonEvidence "xyz-edu-actual-sample-scan.sanitized.json" ([pscustomobject]@{
            generatedAt = (Get-Date).ToString("o")
            status = $(if ($xyzSamples.Count -ge 20 -and $xyzTests.Count -ge 20) { $statusDone } else { $statusFailed })
            sampleCount = $xyzSamples.Count
            testCount = $xyzTests.Count
        })
        Write-JsonEvidence "archive-compression-coverage.sanitized.json" ([pscustomobject]@{
            generatedAt = (Get-Date).ToString("o")
            status = $(if ($missingArchiveFiles.Count -eq 0) { $statusDone } else { $statusFailed })
            requiredFiles = $archiveFiles
            missingFiles = $missingArchiveFiles
            tarStatus = $statusPartial
            tarReason = "TAR support remains partial because the Java standard library has no built-in TAR implementation."
        })
        Write-JsonEvidence "sample-coverage-result.sanitized.json" ([pscustomobject]@{
            generatedAt = (Get-Date).ToString("o")
            status = $(if ($failures.Count -eq 0) { $statusDone } else { $statusFailed })
            requiredSampleCount = $requiredSampleIds.Count
            matrixSampleCount = $rows.Count
            statusSummary = $statusSummary
            moduleSummary = $moduleSummary
            actualSampleCount = $actualSampleFiles.Count
            actualSampleTestCount = $actualSampleTestFiles.Count
            failures = $failures
        })
    }
}

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Error $_ }
    exit 1
}

Write-Host ("sample coverage check passed: {0}" -f $MatrixPath)
