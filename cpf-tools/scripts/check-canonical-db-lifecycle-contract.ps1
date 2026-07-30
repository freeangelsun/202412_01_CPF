param(
    [string] $Root = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

$schemaPath = Join-Path $Root 'cpf-tools/db/canonical/platform-schema.json'
if (-not (Test-Path -LiteralPath $schemaPath -PathType Leaf)) {
    throw "Canonical schema not found: $schemaPath"
}
$schema = Get-Content -LiteralPath $schemaPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 60
$officialVendors = @('mariadb', 'postgresql', 'oracle')
$fileByDb = @{
    cpfDB = '10_cpf_schema.sql'
    cmnDB = '20_cmn_schema.sql'
    admDB = '30_adm_schema.sql'
    batDB = '35_bat_schema.sql'
    bzaDB = '40_business_modules_schema.sql'
    refDB = '40_business_modules_schema.sql'
}

function Get-TableOrder([object[]] $Tables, [string] $LogicalDatabase) {
    $byName = @{}
    $dependencies = @{}
    foreach ($table in $Tables) {
        $name = [string] $table.name
        if ($byName.ContainsKey($name)) {
            throw "Duplicate table: $name"
        }
        $byName[$name] = $table
        $dependencies[$name] = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
    }
    foreach ($table in $Tables) {
        $child = [string] $table.name
        foreach ($foreignKey in @($table.foreignKeys)) {
            $parent = [string] $foreignKey.refTable
            if ($parent -eq $child) { continue }
            if (-not $byName.ContainsKey($parent)) {
                throw "Missing/cross-DB FK parent: logicalDatabase=$LogicalDatabase child=$child parent=$parent"
            }
            [void] $dependencies[$child].Add($parent)
        }
    }
    $remaining = @{}
    foreach ($name in $byName.Keys) { $remaining[$name] = $byName[$name] }
    $ordered = [Collections.Generic.List[object]]::new()
    while ($remaining.Count -gt 0) {
        $ready = [Collections.Generic.List[object]]::new()
        foreach ($name in @($remaining.Keys)) {
            $blocked = $false
            foreach ($parent in $dependencies[$name]) {
                if ($remaining.ContainsKey($parent)) {
                    $blocked = $true
                    break
                }
            }
            if (-not $blocked) { $ready.Add($remaining[$name]) }
        }
        if ($ready.Count -eq 0) {
            throw "Foreign-key cycle: logicalDatabase=$LogicalDatabase tables=$(@($remaining.Keys | Sort-Object) -join ',')"
        }
        foreach ($table in @($ready | Sort-Object name)) {
            $ordered.Add($table)
            $remaining.Remove([string] $table.name)
        }
    }
    return @($ordered)
}

$tableByName = @{}
foreach ($table in @($schema.tables)) {
    $tableName = [string] $table.name
    if ($tableByName.ContainsKey($tableName)) {
        throw "Duplicate canonical table: $tableName"
    }
    $tableByName[$tableName] = $table
    foreach ($column in @($table.columns)) {
        if ([string] $column.default -eq "''") {
            throw "Non-portable empty-string default remains: $tableName.$($column.name)"
        }
        if ([bool] $column.autoIncrement -and [string] $column.type -notmatch '^(?i:BIGINT|INT|TINYINT)$') {
            throw "Non-integer autoIncrement: $tableName.$($column.name)"
        }
    }
}
foreach ($table in @($schema.tables)) {
    foreach ($foreignKey in @($table.foreignKeys)) {
        $parent = [string] $foreignKey.refTable
        if (-not $tableByName.ContainsKey($parent)) {
            throw "Missing FK parent: child=$($table.name) parent=$parent"
        }
        if ([string] $table.logicalDatabase -ne [string] $tableByName[$parent].logicalDatabase) {
            throw "Cross logical-database FK: child=$($table.name) parent=$parent"
        }
    }
}

$auditTable = $tableByName['bat_job_definition_audit']
if ($null -eq $auditTable) { throw 'bat_job_definition_audit missing' }
$auditId = @($auditTable.columns | Where-Object { $_.name -eq 'audit_id' })
if ($auditId.Count -ne 1 -or -not [bool] $auditId[0].autoIncrement) {
    throw 'bat_job_definition_audit.audit_id must be canonical autoIncrement identity'
}

foreach ($logicalDatabase in @($schema.tables | ForEach-Object { [string] $_.logicalDatabase } | Sort-Object -Unique)) {
    $expected = @(Get-TableOrder @($schema.tables | Where-Object { [string] $_.logicalDatabase -eq $logicalDatabase }) $logicalDatabase)
    foreach ($vendor in $officialVendors) {
        if (-not $fileByDb.ContainsKey($logicalDatabase)) {
            throw "No generated source mapping for logicalDatabase=$logicalDatabase"
        }
        $sourcePath = Join-Path $Root "cpf-tools/db/vendor/$vendor/source/$($fileByDb[$logicalDatabase])"
        if (-not (Test-Path -LiteralPath $sourcePath -PathType Leaf)) {
            throw "Generated source missing: $sourcePath"
        }
        $sql = Get-Content -LiteralPath $sourcePath -Raw -Encoding UTF8
        $positions = @{}
        foreach ($table in $expected) {
            $name = [string] $table.name
            $match = [regex]::Match($sql, "(?im)^\s*CREATE\s+TABLE(?:\s+IF\s+NOT\s+EXISTS)?\s+" + [regex]::Escape($name) + "\s*\(")
            if (-not $match.Success) {
                throw "Generated CREATE TABLE missing: vendor=$vendor logicalDatabase=$logicalDatabase table=$name source=$sourcePath"
            }
            $positions[$name] = $match.Index
        }
        foreach ($table in $expected) {
            $child = [string] $table.name
            $childStart = [int] $positions[$child]
            $childEnd = $sql.IndexOf(';', $childStart)
            if ($childEnd -lt 0) { throw "CREATE TABLE terminator missing: vendor=$vendor table=$child source=$sourcePath" }
            $childSql = $sql.Substring($childStart, $childEnd - $childStart + 1)
            foreach ($foreignKey in @($table.foreignKeys)) {
                $parent = [string] $foreignKey.refTable
                if ($parent -eq $child) { continue }
                $constraintName = [string] $foreignKey.name
                $inlineConstraint = $childSql -match ("(?i)\bCONSTRAINT\s+" + [regex]::Escape($constraintName) + "\b")
                if ($inlineConstraint) {
                    if ($positions[$parent] -ge $positions[$child]) {
                        throw "Inline FK child appears before parent: vendor=$vendor child=$child parent=$parent source=$sourcePath"
                    }
                    continue
                }
                $deferredPattern = "(?is)\bALTER\s+TABLE\s+" + [regex]::Escape($child) + ".*?\bADD\s+CONSTRAINT\s+" + [regex]::Escape($constraintName) + "\b"
                if ($sql -notmatch $deferredPattern) {
                    throw "Canonical FK is neither inline nor deferred: vendor=$vendor child=$child constraint=$constraintName source=$sourcePath"
                }
            }
        }
        if ($vendor -eq 'oracle' -and $sql -match "(?im)\bNOT\s+NULL\s+DEFAULT\s+''") {
            throw "Oracle empty-string NOT NULL default remains: $sourcePath"
        }
    }
}

foreach ($vendor in $officialVendors) {
    $sourcePath = Join-Path $Root "cpf-tools/db/vendor/$vendor/source/35_bat_schema.sql"
    $sql = Get-Content -LiteralPath $sourcePath -Raw -Encoding UTF8
    $identityPattern = switch ($vendor) {
        'mariadb' { '(?im)^\s*audit_id\s+BIGINT\s+(?:NOT\s+NULL\s+AUTO_INCREMENT|AUTO_INCREMENT\s+NOT\s+NULL)\b' }
        'postgresql' { '(?im)^\s*audit_id\s+BIGINT\s+GENERATED\s+BY\s+DEFAULT\s+AS\s+IDENTITY\s+NOT\s+NULL\b' }
        'oracle' { '(?im)^\s*audit_id\s+NUMBER\(19\)\s+GENERATED\s+BY\s+DEFAULT\s+ON\s+NULL\s+AS\s+IDENTITY\s+NOT\s+NULL\b' }
    }
    if ($sql -notmatch $identityPattern) {
        throw "Vendor identity projection is missing: vendor=$vendor source=$sourcePath"
    }
}

Write-Host 'CPF canonical DB lifecycle contract PASS'
