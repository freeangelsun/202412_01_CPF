param([string] $Root = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Root = (Resolve-Path -LiteralPath $Root).Path

function Require-File([string] $RelativePath) {
    $path = Join-Path $Root $RelativePath
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "필수 Overlay 파일이 없습니다: $RelativePath" }
    return $path
}

$canonicalPath = Require-File 'cpf-tools/db/canonical/platform-schema.json'
$canonical = Get-Content -LiteralPath $canonicalPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 60
if ([int]$canonical.tableCount -ne 173 -or @($canonical.tables).Count -ne 173) {
    throw "Canonical Table 수가 173이 아닙니다. declared=$($canonical.tableCount) actual=$(@($canonical.tables).Count)"
}
$names = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
foreach ($table in @($canonical.tables)) {
    if (-not $names.Add([string]$table.name)) { throw "Canonical 중복 Table: $($table.name)" }
}

$requiredNewTables = @(
    'cpf_gateway_server_group','cpf_gateway_server_group_member','cpf_gateway_binding',
    'cpf_gateway_apply_status','cpf_gateway_connection_test','cpf_gateway_transaction',
    'cpf_gateway_attempt','cpf_runtime_policy_event','cpf_runtime_policy_delivery',
    'bat_job_definition_version','bat_job_parameter_definition','bat_job_dependency','bat_job_definition_audit'
)
foreach ($tableName in $requiredNewTables) {
    if (-not $names.Contains($tableName)) { throw "Canonical 신규 Table 누락: $tableName" }
}

foreach ($vendor in @('mariadb','postgresql','oracle')) {
    $installPath = Require-File "cpf-tools/db/vendor/$vendor/install/00_empty_install.sql"
    $installText = [IO.File]::ReadAllText($installPath, [Text.Encoding]::UTF8)
    $createdTables = @([regex]::Matches($installText, '(?im)^CREATE\s+TABLE(?:\s+IF\s+NOT\s+EXISTS)?\s+([A-Za-z0-9_]+)'))
    $installNames = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
    foreach ($match in $createdTables) { [void]$installNames.Add($match.Groups[1].Value) }
    if ($installNames.Count -ne 173) { throw "$vendor Clean Install Table 수가 173이 아닙니다: $($installNames.Count)" }
    foreach ($tableName in $requiredNewTables) {
        if (-not $installNames.Contains($tableName)) { throw "$vendor Clean Install 신규 Table 누락: $tableName" }
    }
    if ($vendor -ne 'mariadb' -and $installText -match '(?i)duplicate_key\(255\)') {
        throw "MariaDB Index Prefix 문법이 $vendor DDL에 유출되었습니다."
    }
}

$matrixPath = Require-File 'cpf-docs/quality/CPF_20260730_INTEGRATED_REQUIREMENT_SCENARIO_MATRIX.csv'
$matrix = @(Import-Csv -LiteralPath $matrixPath -Encoding UTF8)
if ($matrix.Count -ne 495) { throw "통합 Matrix가 495행이 아닙니다: $($matrix.Count)" }
if (@($matrix | Where-Object kind -eq 'Requirement').Count -ne 405) { throw 'Requirement 수가 405가 아닙니다.' }
if (@($matrix | Where-Object kind -eq 'Scenario').Count -ne 90) { throw 'Scenario 수가 90이 아닙니다.' }
$developmentOpen = @($matrix | Where-Object development_status -ne '완료')
if ($developmentOpen.Count -gt 0) { throw "개발 상태 미폐쇄 행이 있습니다: $($developmentOpen.Count)" }

$currentRoot = Join-Path $Root 'cpf-docs/work/current'
$currentFiles = @(Get-ChildItem -LiteralPath $currentRoot -File)
if ($currentFiles.Count -ne 1 -or $currentFiles[0].Name -ne 'CPF_CURRENT_WORK_REQUEST.md') {
    throw "Current Request 정본은 한 개여야 합니다: $($currentFiles.Name -join ',')"
}

foreach ($path in @(
    'README.md',
    'cpf-tools/scripts/apply-cpf-20260730-final-overlay.ps1',
    'cpf-tools/scripts/verify-cpf-20260730-full-implementation.ps1',
    'cpf-docs/work/handover/CPF_20260730_CHATGPT_DIRECT_IMPLEMENTATION_FINAL_HANDOVER.md',
    'cpf-docs/work/requests/CPF_CODEX_20260730_EXACT_SHA_FINAL_VALIDATION_REQUEST.md',
    'cpf-tools/generator/contracts/domain-metadata.schema.json'
)) { [void](Require-File $path) }


$manifestPath = Require-File 'cpf-docs/work/overlay/20260730/CPF_OVERLAY_MANIFEST.json'
[void](Require-File 'cpf-docs/work/overlay/20260730/CPF_OVERLAY_SHA256SUMS.txt')
$manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 30
$manifestRows = @($manifest.files)
if ([int]$manifest.fileCount -ne $manifestRows.Count -or $manifestRows.Count -lt 100) {
    throw "Overlay Manifest File 수가 비정상입니다. declared=$($manifest.fileCount) actual=$($manifestRows.Count)"
}
foreach ($row in $manifestRows) {
    $relative = [string]$row.path
    if ([string]::IsNullOrWhiteSpace($relative) -or [IO.Path]::IsPathRooted($relative) -or $relative -match '(^|/|\\)\.\.($|/|\\)') {
        throw "Overlay Manifest 경로가 안전하지 않습니다: $relative"
    }
    $target = Require-File $relative
    $actualBytes = (Get-Item -LiteralPath $target).Length
    if ([long]$row.bytes -ne $actualBytes) { throw "Overlay File 크기 불일치: $relative" }
    $actualHash = (Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($actualHash -ne ([string]$row.sha256).ToLowerInvariant()) { throw "Overlay File SHA-256 불일치: $relative" }
}

foreach ($staleRootFile in @('CPF_20260730_OVERLAY_APPLY_README.md','CPF_OVERLAY_MANIFEST.json','CPF_OVERLAY_SHA256SUMS.txt')) {
    if (Test-Path -LiteralPath (Join-Path $Root $staleRootFile)) { throw "Repository Root Stale 파일이 남았습니다: $staleRootFile" }
}

Write-Host 'CPF 20260730 overlay structure PASS. canonicalTables=173 matrix=495 vendors=3'
