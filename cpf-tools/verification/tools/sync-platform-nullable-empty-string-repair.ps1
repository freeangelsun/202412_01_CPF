param(
    [string] $Root = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path,
    [switch] $Check
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

function Write-CpfGeneratedFile {
    param([string] $Path, [string] $Content)
    $normalized = $Content.Replace("`r`n", "`n").TrimEnd() + "`n"
    if (Test-Path -LiteralPath $Path -PathType Leaf) {
        $actual = [IO.File]::ReadAllText($Path, [Text.Encoding]::UTF8).Replace("`r`n", "`n")
        if ($actual -ceq $normalized) { return }
        throw "IMMUTABLE_MIGRATION_CONFLICT path=$Path"
    }
    if ($Check) {
        throw "Generated nullable repair is missing: $Path"
    }
    $parent = Split-Path -Parent $Path
    New-Item -ItemType Directory -Force -Path $parent | Out-Null
    [IO.File]::WriteAllText($Path, $normalized, [Text.UTF8Encoding]::new($false))
}

$contractPath = Join-Path $Root 'cpf-tools/db/metadata/platform-nullable-empty-string-repair.json'
$schemaPath = Join-Path $Root 'cpf-tools/db/canonical/platform-schema.json'
$contract = Get-Content -LiteralPath $contractPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 20
$schema = Get-Content -LiteralPath $schemaPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 100

$sourceSchemaVersion = [int]$contract.sourceSchemaVersion
$currentSchemaVersion = [int]$schema.schemaVersion
# This contract owns already-published V96/R96 bytes. Its sourceSchemaVersion is
# the immutable authoring identity, not a pointer that follows every later
# canonical schema revision. Reject invalid/future history while the table and
# column loop below validates that all repair targets still exist and retain
# their required current shape.
if ($sourceSchemaVersion -le 0 -or $sourceSchemaVersion -gt $currentSchemaVersion) {
    throw "Nullable repair sourceSchemaVersion is invalid: contract=$sourceSchemaVersion canonical=$currentSchemaVersion"
}
if ([int]$contract.version -le 0 -or [string]::IsNullOrWhiteSpace([string]$contract.description)) {
    throw 'Nullable repair migration identity is invalid.'
}

$columns = [Collections.Generic.List[object]]::new()
foreach ($item in @($contract.columns)) {
    $historicalTableName = [string]$item.table
    $table = @($schema.tables | Where-Object {
        $_.name -ceq $historicalTableName -or
            ($null -ne $_.PSObject.Properties['currentName'] -and
                [string]$_.currentName -ceq $historicalTableName)
    })
    if ($table.Count -ne 1) { throw "Nullable repair table is not canonical: $($item.table)" }
    if ([string]$table[0].logicalDatabase -cne [string]$contract.logicalDatabase) {
        throw "Nullable repair logical database drift: $($item.table)"
    }
    $column = @($table[0].columns | Where-Object { $_.name -ceq [string]$item.column })
    if ($column.Count -ne 1) { throw "Nullable repair column is not canonical: $($item.table).$($item.column)" }
    if (-not [bool]$column[0].nullable -or $null -ne $column[0].default) {
        throw "Nullable repair target must be nullable with no default: $($item.table).$($item.column)"
    }
    $columns.Add([pscustomobject]@{ table = [string]$item.table; column = [string]$item.column; type = [string]$column[0].type })
}
if ($columns.Count -eq 0 -or $columns.Count -ne @($contract.columns).Count) {
    throw 'Nullable repair column contract is empty or duplicated.'
}

$version = [int]$contract.version
$description = [string]$contract.description
$header = @(
    '-- GENERATED FILE. DO NOT EDIT VENDOR SQL DIRECTLY.',
    "-- Source: cpf-tools/db/metadata/platform-nullable-empty-string-repair.json + canonical schemaVersion $sourceSchemaVersion.",
    "-- Repair: $($contract.repairId); historical migrations remain immutable."
)

foreach ($vendor in @('mariadb', 'postgresql', 'oracle')) {
    $forward = [Collections.Generic.List[string]]::new()
    $rollback = [Collections.Generic.List[string]]::new()
    foreach ($line in $header) { $forward.Add($line); $rollback.Add($line) }
    $forward.Add('')
    $rollback.Add('')

    if ($vendor -eq 'mariadb') {
        foreach ($group in @($columns | Group-Object table)) {
            $modifiers = @($group.Group | ForEach-Object { "    MODIFY COLUMN $($_.column) $($_.type) NULL DEFAULT NULL" })
            $forward.Add("ALTER TABLE $($group.Name)`n" + ($modifiers -join ",`n") + ';')
            $forward.Add('')
            foreach ($item in $group.Group) { $rollback.Add("UPDATE $($item.table) SET $($item.column) = '' WHERE $($item.column) IS NULL;") }
            $legacy = @($group.Group | ForEach-Object { "    MODIFY COLUMN $($_.column) $($_.type) NOT NULL DEFAULT ''" })
            $rollback.Add("ALTER TABLE $($group.Name)`n" + ($legacy -join ",`n") + ';')
            $rollback.Add('')
        }
        $migrationPath = Join-Path $Root "cpf-tools/db/vendor/mariadb/migration/flyway/V${version}__${description}.sql"
        $rollbackPath = Join-Path $Root "cpf-tools/db/vendor/mariadb/rollback/R${version}__${description}.sql"
    } elseif ($vendor -eq 'postgresql') {
        foreach ($item in $columns) {
            $forward.Add("ALTER TABLE $($item.table) ALTER COLUMN $($item.column) DROP DEFAULT;")
            $forward.Add("ALTER TABLE $($item.table) ALTER COLUMN $($item.column) DROP NOT NULL;")
            $rollback.Add("UPDATE $($item.table) SET $($item.column) = '' WHERE $($item.column) IS NULL;")
            $rollback.Add("ALTER TABLE $($item.table) ALTER COLUMN $($item.column) SET DEFAULT '';")
            $rollback.Add("ALTER TABLE $($item.table) ALTER COLUMN $($item.column) SET NOT NULL;")
        }
        $migrationPath = Join-Path $Root "cpf-tools/db/vendor/postgresql/migration/flyway/$($contract.logicalDatabase)/V${version}__${description}.sql"
        $rollbackPath = Join-Path $Root "cpf-tools/db/vendor/postgresql/rollback/$($contract.logicalDatabase)/R${version}__${description}.sql"
    } else {
        foreach ($item in $columns) {
            $forward.Add("ALTER TABLE $($item.table) MODIFY ($($item.column) DEFAULT NULL);")
            $forward.Add("ALTER TABLE $($item.table) MODIFY ($($item.column) NULL);")
        }
        $rollback.Add('-- Oracle treats the empty string as NULL, so the historical NOT NULL DEFAULT empty-string state is unrepresentable.')
        $rollback.Add('-- Fail closed instead of coercing NULL business values to a fabricated sentinel.')
        $rollback.Add('BEGIN')
        $rollback.Add("  RAISE_APPLICATION_ERROR(-20096, 'R96 cannot restore Oracle empty-string NOT NULL semantics; restore the pre-V96 backup instead');")
        $rollback.Add('END;')
        $rollback.Add('/')
        $migrationPath = Join-Path $Root "cpf-tools/db/vendor/oracle/migration/flyway/$($contract.logicalDatabase)/V${version}__${description}.sql"
        $rollbackPath = Join-Path $Root "cpf-tools/db/vendor/oracle/rollback/$($contract.logicalDatabase)/R${version}__${description}.sql"
    }

    Write-CpfGeneratedFile $migrationPath ($forward -join "`n")
    Write-CpfGeneratedFile $rollbackPath ($rollback -join "`n")
}

$mode = if ($Check) { 'CHECK' } else { 'SYNC' }
Write-Host "[PASS] Platform nullable empty-string repair mode=$mode version=$version columns=$($columns.Count) vendors=3"
