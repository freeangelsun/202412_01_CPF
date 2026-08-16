param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [switch] $Check
)

$ErrorActionPreference = "Stop"
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$metadataRelativePath = "cpf-tools/db/metadata/platform-schema-comment-migration-v58.json"
$metadataPath = Join-Path $Root $metadataRelativePath
$metadata = Get-Content -LiteralPath $metadataPath -Raw -Encoding UTF8 | ConvertFrom-Json
if ($metadata.schemaVersion -ne 1 -or $metadata.migrationVersion -ne "V58") {
    throw "Unsupported schema-comment migration metadata: $metadataRelativePath"
}
if (-not $Check) {
    throw "V58 is an immutable Historical Migration. Update neither the migration nor its rollback; use -Check to verify the preserved artifact."
}

$forwardRelativePath = "cpf-tools/db/vendor/mariadb/source/migration/flyway/V58__platform_schema_comments.sql"
$rollbackRelativePath = "cpf-tools/db/vendor/mariadb/source/migration/rollback/V58__platform_schema_comments_rollback.sql"
$forwardPath = Join-Path $Root $forwardRelativePath
$rollbackPath = Join-Path $Root $rollbackRelativePath
foreach ($path in @($forwardPath, $rollbackPath)) {
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Historical schema-comment artifact missing: $path"
    }
}

$expectedForwardHash = ([string] $metadata.artifactHashes.forwardSha256).ToUpperInvariant()
$expectedRollbackHash = ([string] $metadata.artifactHashes.rollbackSha256).ToUpperInvariant()
if ($expectedForwardHash -notmatch "^[0-9A-F]{64}$" -or $expectedRollbackHash -notmatch "^[0-9A-F]{64}$") {
    throw "Historical schema-comment artifact SHA-256 metadata is invalid."
}
$actualForwardHash = (Get-FileHash -LiteralPath $forwardPath -Algorithm SHA256).Hash.ToUpperInvariant()
$actualRollbackHash = (Get-FileHash -LiteralPath $rollbackPath -Algorithm SHA256).Hash.ToUpperInvariant()
if ($actualForwardHash -cne $expectedForwardHash) {
    throw "Historical schema-comment forward migration changed: expected=$expectedForwardHash actual=$actualForwardHash"
}
if ($actualRollbackHash -cne $expectedRollbackHash) {
    throw "Historical schema-comment rollback changed: expected=$expectedRollbackHash actual=$actualRollbackHash"
}

$forwardText = [System.IO.File]::ReadAllText($forwardPath, [System.Text.Encoding]::UTF8)
$rollbackText = [System.IO.File]::ReadAllText($rollbackPath, [System.Text.Encoding]::UTF8)
foreach ($change in @($metadata.changes)) {
    $schemaName = [string] $change.schema
    $tableName = [string] $change.table
    if ($schemaName -notmatch "^[A-Za-z][A-Za-z0-9_]*$" -or $tableName -notmatch "^[a-z][a-z0-9_]*$") {
        throw "Unsafe schema/table identifier in schema-comment metadata: $schemaName.$tableName"
    }
    $qualifiedTable = [regex]::Escape("$schemaName.$tableName")
    if ($change.tableCommentAdded -and
        $forwardText -notmatch "(?im)^ALTER\s+TABLE\s+$qualifiedTable\s+COMMENT\s*=\s*'[^']*'\s*;") {
        throw "Historical table-comment statement missing: $schemaName.$tableName"
    }
    foreach ($columnValue in @($change.columns)) {
        $columnName = [string] $columnValue
        if ($columnName -notmatch "^[a-z][a-z0-9_]*$") {
            throw "Unsafe column identifier in schema-comment metadata: $schemaName.$tableName.$columnName"
        }
        $escapedColumn = [regex]::Escape($columnName)
        if ($forwardText -notmatch "(?im)^ALTER\s+TABLE\s+$qualifiedTable\s+MODIFY\s+COLUMN\s+$escapedColumn\b[^\r\n;]*\sCOMMENT\s+'[^']+'\s*;") {
            throw "Historical column-comment statement missing: $schemaName.$tableName.$columnName"
        }
        if ($rollbackText -notmatch "(?im)^ALTER\s+TABLE\s+$qualifiedTable\s+MODIFY\s+COLUMN\s+$escapedColumn\b[^\r\n;]*;") {
            throw "Historical column-comment rollback missing: $schemaName.$tableName.$columnName"
        }
    }
}

Write-Host "Historical V58 schema-comment migration integrity check passed."
