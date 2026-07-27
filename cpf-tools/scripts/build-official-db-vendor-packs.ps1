param(
    [string]$Root = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path
)
$ErrorActionPreference = "Stop"
$official = @("mariadb", "postgresql", "oracle")
$planPath = Join-Path $Root "cpf-tools/config/database-source-plan.json"
$plan = Get-Content -LiteralPath $planPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 30

function Read-Source([string]$vendor, [string]$name) {
    $path = Join-Path $Root "cpf-tools/db/vendor/$vendor/source/$name"
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Vendor source missing: $path" }
    return Get-Content -LiteralPath $path -Raw -Encoding UTF8
}
function Write-Utf8([string]$path, [string]$text) {
    $dir = Split-Path -Parent $path
    New-Item -ItemType Directory -Path $dir -Force | Out-Null
    [IO.File]::WriteAllText($path, ($text.TrimEnd()+"`n"), [Text.UTF8Encoding]::new($false))
}
function Build-Bundle([string]$vendor, [object[]]$names, [string]$target) {
    $body = "-- CPF generated lifecycle bundle; vendor=$vendor`n-- Source plan: cpf-tools/config/database-source-plan.json`n"
    foreach ($name in $names) {
        $body += "`n-- ===== BEGIN $name =====`n" + (Read-Source $vendor $name) + "`n-- ===== END $name =====`n"
    }
    Write-Utf8 $target $body
}
function Get-LogicalSections([string]$text) {
    $pattern = '(?im)^\s*--\s*CPF_LOGICAL_DATABASE=([A-Za-z][A-Za-z0-9_$#]*)\s*$'
    $matches = [regex]::Matches($text, $pattern)
    $result = @()
    for ($i=0; $i -lt $matches.Count; $i++) {
        $start = $matches[$i].Index
        $end = if ($i + 1 -lt $matches.Count) { $matches[$i+1].Index } else { $text.Length }
        $result += [pscustomobject]@{ logicalDatabase=$matches[$i].Groups[1].Value; text=$text.Substring($start, $end-$start) }
    }
    return $result
}
function Assert-Portable([string]$vendor, [string]$text, [string]$source) {
    if ($vendor -ne 'mariadb' -and $text -match '(?im)^\s*USE\s+') { throw "$vendor source has MariaDB USE directive: $source" }
    if ($vendor -ne 'mariadb' -and $text -match '(?i)\b(cpfDB|cmnDB|admDB|bzaDB|batDB|mbrDB|accDB|refDB)\.') { throw "$vendor source has logical database qualifier: $source" }
    if ($vendor -eq 'postgresql' -and $text -match '(?i)\b(AUTO_INCREMENT|ON\s+DUPLICATE\s+KEY|IFNULL\s*\(|DATE_ADD\s*\()') { throw "PostgreSQL source has non-native SQL: $source" }
    if ($vendor -eq 'oracle' -and $text -match '(?i)\b(AUTO_INCREMENT|ON\s+DUPLICATE\s+KEY|IFNULL\s*\(|DATE_ADD\s*\(|LIMIT\s+\d+)') { throw "Oracle source has non-native SQL: $source" }
}

$maria = $plan.mariadb
foreach ($vendor in @('postgresql','oracle')) {
    $root = Join-Path $Root "cpf-tools/db/vendor/$vendor"
    foreach ($name in @($maria.emptyInstallFiles + $maria.productSeedFiles + $maria.optionalSampleSeedFiles + $maria.testSeedFiles)) {
        Assert-Portable $vendor (Read-Source $vendor $name) $name
    }
    Build-Bundle $vendor $maria.emptyInstallFiles (Join-Path $root 'install/00_empty_install.sql')
    Build-Bundle $vendor $maria.productSeedFiles (Join-Path $root 'seed/00_product_seed.sql')
    Build-Bundle $vendor $maria.optionalSampleSeedFiles (Join-Path $root 'seed/00_optional_sample_seed.sql')
    Build-Bundle $vendor $maria.testSeedFiles (Join-Path $root 'seed/00_test_seed.sql')

    # New official vendors start from the current CPF baseline. Each physical DB has its own Flyway history.
    $allSchema = ($maria.emptyInstallFiles | ForEach-Object { Read-Source $vendor $_ }) -join "`n"
    $sections = Get-LogicalSections $allSchema
    foreach ($group in ($sections | Group-Object logicalDatabase)) {
        $db = $group.Name
        $body = ($group.Group | ForEach-Object text) -join "`n"
        $migration = Join-Path $root "migration/flyway/$db/V63__cpf_vendor_baseline.sql"
        Write-Utf8 $migration ("-- CPF $vendor initial baseline for $db`n" + $body)
        $tableNames = [regex]::Matches($body, '(?im)^\s*CREATE\s+TABLE\s+([A-Za-z][A-Za-z0-9_$#]*)') | ForEach-Object { $_.Groups[1].Value }
        [array]::Reverse($tableNames)
        $drop = if ($vendor -eq 'postgresql') {
            ($tableNames | ForEach-Object { "DROP TABLE IF EXISTS $_ CASCADE;" }) -join "`n"
        } else {
            ($tableNames | ForEach-Object { "BEGIN EXECUTE IMMEDIATE 'DROP TABLE $_ CASCADE CONSTRAINTS PURGE'; EXCEPTION WHEN OTHERS THEN IF SQLCODE != -942 THEN RAISE; END IF; END;`n/" }) -join "`n"
        }
        Write-Utf8 (Join-Path $root "rollback/$db/R63__cpf_vendor_baseline.sql") ("-- Exact rollback of CPF $vendor initial baseline for $db`n" + $drop)
    }
}
Write-Host "Official DB vendor lifecycle bundles generated for PostgreSQL and Oracle."
