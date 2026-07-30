[CmdletBinding()]
param(
    [string] $Root = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$manifestPath = Join-Path $Root 'cpf-docs/quality/CPF_20260730_QA32_REQUEST_INTEGRITY.json'
if (-not (Test-Path -LiteralPath $manifestPath -PathType Leaf)) {
    throw "QA32 request integrity manifest is missing: $manifestPath"
}
$manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
if ($manifest.schemaVersion -ne 'CPF-QA32-REQUEST-INTEGRITY-1') {
    throw "Unsupported QA32 integrity schema: $($manifest.schemaVersion)"
}

$failures = @()
foreach ($entry in @($manifest.files)) {
    $relative = [string] $entry.path
    $expected = ([string] $entry.sha256).ToLowerInvariant()
    $target = Join-Path $Root ($relative -replace '/', [IO.Path]::DirectorySeparatorChar)
    if (-not (Test-Path -LiteralPath $target -PathType Leaf)) {
        $failures += "MISSING $relative"
        continue
    }
    $actual = (Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($actual -ne $expected) {
        $failures += "HASH_MISMATCH $relative expected=$expected actual=$actual"
    }
}

$requirements = Import-Csv -LiteralPath (Join-Path $Root 'cpf-docs/quality/CPF_20260730_QA32_REQUIREMENT_MATRIX.csv')
$defects = Import-Csv -LiteralPath (Join-Path $Root 'cpf-docs/quality/CPF_20260730_QA32_DEFECT_REGISTER.csv')
$scenarios = Import-Csv -LiteralPath (Join-Path $Root 'cpf-docs/quality/CPF_20260730_QA32_SCENARIO_MATRIX.csv')
$migrations = Import-Csv -LiteralPath (Join-Path $Root 'cpf-docs/quality/CPF_20260730_QA32_OSS_MIGRATION_MATRIX.csv')

if ($requirements.Count -ne 62) { $failures += "REQUIREMENT_COUNT expected=62 actual=$($requirements.Count)" }
if ($defects.Count -ne 60) { $failures += "DEFECT_COUNT expected=60 actual=$($defects.Count)" }
if ($scenarios.Count -ne 202) { $failures += "SCENARIO_COUNT expected=202 actual=$($scenarios.Count)" }
if ($migrations.Count -ne 23) { $failures += "MIGRATION_COUNT expected=23 actual=$($migrations.Count)" }

foreach ($group in @(
    @{name='requirement'; rows=$requirements; key='requirement_id'},
    @{name='defect'; rows=$defects; key='defect_id'},
    @{name='scenario'; rows=$scenarios; key='scenario_id'},
    @{name='migration'; rows=$migrations; key='change_id'})) {
    $ids = @($group.rows | ForEach-Object { [string] $_.($group.key) })
    $duplicates = @($ids | Group-Object | Where-Object Count -gt 1 | ForEach-Object Name)
    $blank = @($ids | Where-Object { [string]::IsNullOrWhiteSpace($_) })
    if ($duplicates.Count -gt 0) { $failures += "DUPLICATE_$($group.name.ToUpperInvariant())_ID $($duplicates -join ',')" }
    if ($blank.Count -gt 0) { $failures += "BLANK_$($group.name.ToUpperInvariant())_ID count=$($blank.Count)" }
}

$requirementIds = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
$requirements | ForEach-Object { [void] $requirementIds.Add([string] $_.requirement_id) }
foreach ($scenario in $scenarios) {
    if (-not $requirementIds.Contains([string] $scenario.requirement_id)) {
        $failures += "SCENARIO_UNKNOWN_REQUIREMENT scenario=$($scenario.scenario_id) requirement=$($scenario.requirement_id)"
    }
}

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Error $_ }
    throw "CPF QA32 request integrity verification failed. failures=$($failures.Count)"
}

Write-Host "CPF_QA32_REQUEST_INTEGRITY_PASS files=$(@($manifest.files).Count) requirements=$($requirements.Count) defects=$($defects.Count) scenarios=$($scenarios.Count) migrations=$($migrations.Count)"
