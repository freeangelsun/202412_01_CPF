param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [switch] $Check
)

$ErrorActionPreference = "Stop"
$utf8NoBom = [System.Text.UTF8Encoding]::new($false)

$metadataRelativePath = "cpf-tools/db/metadata/platform-schema-comment-migration-v58.json"
$metadataPath = Join-Path $Root $metadataRelativePath
$metadata = Get-Content -LiteralPath $metadataPath -Raw -Encoding UTF8 | ConvertFrom-Json
if ($metadata.schemaVersion -ne 1 -or $metadata.migrationVersion -ne "V58") {
    throw "Unsupported schema-comment migration metadata: $metadataRelativePath"
}

$tableDefinitions = @{}
foreach ($relativePath in @($metadata.sourceFiles)) {
    $lines = Get-Content -LiteralPath (Join-Path $Root $relativePath) -Encoding UTF8
    for ($index = 0; $index -lt $lines.Count; $index++) {
        if ($lines[$index] -notmatch "(?i)^CREATE\s+TABLE\s+IF\s+NOT\s+EXISTS\s+(?<table>[A-Za-z0-9_]+)\s*\(") {
            continue
        }

        $tableName = $Matches.table.ToLowerInvariant()
        $columnDefinitions = @{}
        $tableComment = $null
        for ($cursor = $index + 1; $cursor -lt $lines.Count; $cursor++) {
            $trimmed = $lines[$cursor].Trim()
            if ($trimmed -match "^\).*?COMMENT\s*=\s*'(?<comment>[^']+)'\s*;") {
                $tableComment = $Matches.comment
                $index = $cursor
                break
            }
            if ($trimmed -match "^\)\s*.*;") {
                $index = $cursor
                break
            }
            if ($trimmed -match "^(?<column>[A-Za-z_][A-Za-z0-9_]*)\s+(BIGINT|INT|SMALLINT|TINYINT|VARCHAR|CHAR|TEXT|LONGTEXT|DATETIME|TIMESTAMP|DATE|TIME|DECIMAL|NUMERIC|BOOLEAN|BLOB|JSON)\b") {
                $columnDefinitions[$Matches.column.ToLowerInvariant()] = $trimmed.TrimEnd(",")
            }
        }
        $tableDefinitions[$tableName] = [pscustomobject]@{
            Columns = $columnDefinitions
            TableComment = $tableComment
        }
    }
}

$forward = [System.Collections.Generic.List[string]]::new()
$rollback = [System.Collections.Generic.List[string]]::new()
$forward.Add("-- Generated from canonical MariaDB schema and $metadataRelativePath.")
$forward.Add("-- Do not edit this migration directly; update canonical schema/metadata and regenerate.")
$forward.Add("SET @cpf_v58_previous_foreign_key_checks := @@FOREIGN_KEY_CHECKS;")
$forward.Add("SET FOREIGN_KEY_CHECKS = 0;")
$rollback.Add("-- Generated rollback for canonical MariaDB schema comments introduced by V58.")
$rollback.Add("-- Do not edit this rollback directly; update canonical schema/metadata and regenerate.")
$rollback.Add("SET @cpf_v58_previous_foreign_key_checks := @@FOREIGN_KEY_CHECKS;")
$rollback.Add("SET FOREIGN_KEY_CHECKS = 0;")

foreach ($change in @($metadata.changes)) {
    $schemaName = [string] $change.schema
    $tableName = ([string] $change.table).ToLowerInvariant()
    if ($schemaName -notmatch "^[A-Za-z][A-Za-z0-9_]*$" -or $tableName -notmatch "^[a-z][a-z0-9_]*$") {
        throw "Unsafe schema/table identifier in schema-comment metadata: $schemaName.$tableName"
    }
    if (-not $tableDefinitions.ContainsKey($tableName)) {
        throw "Canonical table not found for schema-comment migration: $tableName"
    }

    $definition = $tableDefinitions[$tableName]
    if ($change.tableCommentAdded) {
        if ([string]::IsNullOrWhiteSpace([string] $definition.TableComment)) {
            throw "Canonical table comment missing: $tableName"
        }
        $escapedTableComment = ([string] $definition.TableComment).Replace("'", "''")
        $forward.Add("ALTER TABLE $schemaName.$tableName COMMENT = '$escapedTableComment';")
        $rollback.Add("ALTER TABLE $schemaName.$tableName COMMENT = '';")
    }

    foreach ($columnValue in @($change.columns)) {
        $columnName = ([string] $columnValue).ToLowerInvariant()
        if ($columnName -notmatch "^[a-z][a-z0-9_]*$" -or -not $definition.Columns.ContainsKey($columnName)) {
            throw "Canonical column not found for schema-comment migration: $tableName.$columnName"
        }
        $columnDefinition = [string] $definition.Columns[$columnName]
        if ($columnDefinition -notmatch "(?i)\sCOMMENT\s+'[^']+'") {
            throw "Canonical column comment missing: $tableName.$columnName"
        }
        # MODIFY COLUMN must preserve the physical column contract without attempting
        # to recreate inline key declarations that are already owned by table indexes.
        $columnDefinition = [regex]::Replace($columnDefinition, "(?i)\s+PRIMARY\s+KEY\b", "")
        $columnDefinition = [regex]::Replace($columnDefinition, "(?i)\s+UNIQUE\b", "")
        $rollbackDefinition = [regex]::Replace($columnDefinition, "(?i)\sCOMMENT\s+'[^']+'", "")
        $forward.Add("ALTER TABLE $schemaName.$tableName MODIFY COLUMN $columnDefinition;")
        $rollback.Add("ALTER TABLE $schemaName.$tableName MODIFY COLUMN $rollbackDefinition;")
    }
}

$forward.Add("SET FOREIGN_KEY_CHECKS = @cpf_v58_previous_foreign_key_checks;")
$rollback.Add("SET FOREIGN_KEY_CHECKS = @cpf_v58_previous_foreign_key_checks;")
$forward.Add("")
$rollback.Add("")
$forwardText = $forward -join "`n"
$rollbackText = $rollback -join "`n"
$forwardRelativePath = "cpf-tools/db/vendor/mariadb/source/migration/flyway/V58__platform_schema_comments.sql"
$rollbackRelativePath = "cpf-tools/db/vendor/mariadb/source/migration/rollback/V58__platform_schema_comments_rollback.sql"

foreach ($artifact in @(
    @{ Path = $forwardRelativePath; Content = $forwardText },
    @{ Path = $rollbackRelativePath; Content = $rollbackText }
)) {
    $path = Join-Path $Root $artifact.Path
    if ($Check) {
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
            throw "Generated schema-comment artifact missing: $($artifact.Path)"
        }
        $actual = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8).Replace("`r`n", "`n")
        if ($actual -cne $artifact.Content) {
            throw "Generated schema-comment artifact drift: $($artifact.Path)"
        }
        continue
    }
    [System.IO.Directory]::CreateDirectory([System.IO.Path]::GetDirectoryName($path)) | Out-Null
    [System.IO.File]::WriteAllText($path, $artifact.Content, $utf8NoBom)
}

if ($Check) {
    Write-Host "Platform schema-comment migration check passed."
} else {
    Write-Host "Platform schema-comment migration generated."
}
