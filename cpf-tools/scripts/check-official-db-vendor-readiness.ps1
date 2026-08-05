param([string] $Root = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$official = @('mariadb','postgresql','oracle')
$manifestPath = Join-Path $Root 'cpf-tools/db/vendor-pack-manifest.json'
$manifest = Get-Content -Raw -Encoding UTF8 -LiteralPath $manifestPath | ConvertFrom-Json
$profilePath = Join-Path $Root 'cpf-tools/config/database-install.default.json'
$profile = Get-Content -Raw -Encoding UTF8 -LiteralPath $profilePath | ConvertFrom-Json
$logicalDatabases = @(
    $profile.modules.PSObject.Properties |
        Where-Object { [bool]$_.Value.enabled } |
        ForEach-Object { [string]$_.Value.logicalDatabase } |
        Sort-Object -Unique
)
if ($logicalDatabases.Count -eq 0) {
    throw "Enabled platform database가 없습니다: $profilePath"
}
$qualifiedDatabasePattern = '(?i)\b(?:' +
    (($logicalDatabases | ForEach-Object { [regex]::Escape($_) }) -join '|') +
    ')\.'
$failures = [System.Collections.Generic.List[string]]::new()

if ((@($manifest.officialVendors) -join ',') -cne ($official -join ',')) {
    $failures.Add("officialVendors must be exactly: $($official -join ',')")
}
if (@($manifest.vendors.PSObject.Properties.Name | Where-Object { $_ -notin $official }).Count -gt 0) {
    $failures.Add('Unsupported DB vendor metadata remains in vendor-pack-manifest.json')
}

$requiredLifecycle = @('provision','emptyInstall','productSeed','optionalSampleSeed','testSeed','migration','verify','rollback')
foreach ($vendor in $official) {
    $entry = $manifest.vendors.$vendor
    if ($null -eq $entry) { $failures.Add("Official vendor entry missing: $vendor"); continue }
    $isSelectable = @($manifest.supportedVendors) -contains $vendor
    $ready = $true
    foreach ($name in $requiredLifecycle) {
        $prop = $entry.lifecycle.PSObject.Properties[$name]
        if ($null -eq $prop) { $ready = $false; continue }
        $relative = [string]$prop.Value
        if ($relative -match '\{logicalDatabase\}') {
            $rootRelative = $relative -replace '/\{logicalDatabase\}.*$',''
            $path = Join-Path $Root ($rootRelative -replace '/', [System.IO.Path]::DirectorySeparatorChar)
            if (-not (Test-Path -LiteralPath $path -PathType Container)) { $ready = $false }
            else {
                foreach ($db in $logicalDatabases) {
                    $dbDir = Join-Path $path $db
                    if (-not (Test-Path -LiteralPath $dbDir -PathType Container) -or @(Get-ChildItem -LiteralPath $dbDir -File -Filter '*.sql').Count -eq 0) { $ready = $false }
                }
            }
        } else {
            $path = Join-Path $Root ($relative -replace '/', [System.IO.Path]::DirectorySeparatorChar)
            if ($name -in @('migration','rollback')) {
                if (-not (Test-Path -LiteralPath $path -PathType Container) -or @(Get-ChildItem -LiteralPath $path -Recurse -File | Where-Object { $_.Name -ne '.gitkeep' }).Count -eq 0) { $ready = $false }
            } else {
                if (-not (Test-Path -LiteralPath $path -PathType Leaf) -or (Get-Item $path).Length -le 1) { $ready = $false }
            }
        }
    }
    if ($isSelectable -and -not $ready) {
        $failures.Add("Selectable vendor has incomplete lifecycle: $vendor")
    }
    if (-not $isSelectable -and $ready) {
        $failures.Add("Lifecycle-ready official vendor is not promoted to supportedVendors: $vendor")
    }
}
foreach ($vendor in @('postgresql','oracle')) {
    $vendorRoot = Join-Path $Root "cpf-tools/db/vendor/$vendor"
    foreach ($sql in Get-ChildItem -LiteralPath $vendorRoot -Recurse -File -Filter '*.sql') {
        $body = Get-Content -LiteralPath $sql.FullName -Raw -Encoding UTF8
        $structuralBody = [regex]::Replace($body, "'(?:''|[^'])*'", "''")
        $structuralBody = [regex]::Replace($structuralBody, '(?m)--.*$', '')
        if ($structuralBody -match '(?im)^\s*USE\s+') { $failures.Add("$vendor contains USE directive: $($sql.FullName)") }
        if ($structuralBody -match $qualifiedDatabasePattern -or
                $structuralBody -match '(?i)\b[a-z][a-z0-9_]*DB\.') {
            $failures.Add("$vendor contains logical DB qualifier: $($sql.FullName)")
        }
    }
}
if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Error $_ }
    throw "Official DB vendor readiness failed: $($failures.Count) issue(s)."
}
Write-Host "Official DB vendor readiness passed. official=$($official -join ',') selectable=$(@($manifest.supportedVendors) -join ',')"

# V9 S04: execute the fail-closed migration lifecycle verifier through the
# existing Gradle-owned DB readiness consumer. The verifier discovers every
# checksum-managed logical database pack; no migration version allowlist is
# accepted here.
$pythonCommand = Get-Command python -ErrorAction SilentlyContinue
if ($null -eq $pythonCommand) {
    $pythonCommand = Get-Command python3 -ErrorAction SilentlyContinue
}
if ($null -eq $pythonCommand) {
    throw 'Python 3 executable is required for CPF DB migration lifecycle verification.'
}
$sourceSha = (& git -C $Root rev-parse HEAD 2>&1 | Select-Object -First 1).ToString().Trim().ToLowerInvariant()
if ($LASTEXITCODE -ne 0 -or $sourceSha -notmatch '^[0-9a-f]{40}$') {
    throw "Exact Git source SHA is unavailable for DB lifecycle evidence: $sourceSha"
}
$reportDirectory = Join-Path $Root 'build/reports/cpf-db'
New-Item -ItemType Directory -Force -Path $reportDirectory | Out-Null
$reportPath = Join-Path $reportDirectory 'migration-lifecycle.json'
& $pythonCommand.Source (Join-Path $Root 'cpf-tools/db/verify_migration_lifecycle.py') `
    --root $Root `
    --source-sha $sourceSha `
    --report $reportPath
if ($LASTEXITCODE -ne 0) {
    throw "CPF DB migration lifecycle verification failed with exit code $LASTEXITCODE. report=$reportPath"
}
Write-Host "CPF DB migration lifecycle verification passed. report=$reportPath sourceSha=$sourceSha"

# Execute the existing CPF DB static contracts from the same Gradle consumer so
# DB-INSTALL/OWNERSHIP/MULTI-VENDOR/SQL/PERF/MULTI/LINEAGE/RETENTION cannot pass
# merely because their JSON or SQL files exist.
function Invoke-CpfPythonDbGate {
    param(
        [Parameter(Mandatory)][string]$ScriptRelativePath,
        [Parameter(Mandatory)][string[]]$Arguments
    )
    $scriptPath = Join-Path $Root $ScriptRelativePath
    if (-not (Test-Path -LiteralPath $scriptPath -PathType Leaf)) {
        throw "CPF DB Python gate is missing: $ScriptRelativePath"
    }
    & $pythonCommand.Source $scriptPath @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "CPF DB Python gate failed: script=$ScriptRelativePath exit=$LASTEXITCODE"
    }
}

Invoke-CpfPythonDbGate 'cpf-tools/scripts/verify-cpf-db-vendor-manifest.py' @(
    '--root', $Root,
    '--json-output', (Join-Path $reportDirectory 'vendor-manifest.json')
)
Invoke-CpfPythonDbGate 'cpf-tools/scripts/verify-cpf-db-lifecycle-contract.py' @(
    '--root', $Root
)
Invoke-CpfPythonDbGate 'cpf-tools/scripts/verify-cpf-db-development-contract.py' @(
    '--root', $Root,
    '--json-output', (Join-Path $reportDirectory 'development-contract.json')
)
Invoke-CpfPythonDbGate 'cpf-tools/scripts/verify-cpf-db-schema-governance.py' @(
    '--root', $Root,
    '--json-output', (Join-Path $reportDirectory 'schema-governance.json')
)
Invoke-CpfPythonDbGate 'cpf-tools/scripts/verify-cpf-db-vendor-semantic-parity.py' @(
    '--root', $Root,
    '--json-output', (Join-Path $reportDirectory 'vendor-semantic-parity.json')
)
Write-Host "CPF DB composite static verification passed. reportDirectory=$reportDirectory"
