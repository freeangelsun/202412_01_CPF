param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $ProfilePath = "",
    [string] $ResultDir = "",
    [string] $AdminUsername = $env:CPF_DB_ROOT_USERNAME,
    [string] $AdminPassword = $env:CPF_DB_ROOT_PASSWORD,
    [switch] $VerifyOnly,
    [switch] $Apply,
    [string] $Confirmation = ""
)

if ($PSVersionTable.PSVersion.Major -lt 7) {
    throw "CPF Platform Migration Smoke는 pwsh 7 이상이 필요합니다."
}

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$RequiredConfirmation = "VERIFY_V58_UPGRADE_ROLLBACK_REAPPLY"
$Root = (Resolve-Path -LiteralPath $Root).Path

if ($VerifyOnly -and $Apply) {
    throw "-VerifyOnly와 -Apply는 동시에 사용할 수 없습니다."
}
if (-not $VerifyOnly -and (-not $Apply -or $Confirmation -cne $RequiredConfirmation)) {
    throw "실제 Upgrade/Rollback/Re-upgrade 검증에는 -Apply -Confirmation $RequiredConfirmation 이 필요합니다."
}
if ([string]::IsNullOrWhiteSpace($AdminPassword)) {
    throw "CPF_DB_ROOT_PASSWORD 또는 -AdminPassword가 필요합니다."
}
if ([string]::IsNullOrWhiteSpace($ProfilePath)) {
    $ProfilePath = Join-Path $Root "cpf-tools/db/config/database-install.default.json"
}
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/db-migration"
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$profile = Get-Content -LiteralPath $ProfilePath -Raw -Encoding UTF8 | ConvertFrom-Json
$core = $profile.modules.core
if ([string]$core.vendor -ne "mariadb") {
    throw "이 Smoke는 MariaDB Platform Pack만 실제 실행합니다: vendor=$($core.vendor)"
}
if ([string]::IsNullOrWhiteSpace($AdminUsername)) {
    $AdminUsername = [string]$core.admin.username
}

$clientPath = [string]$core.clientPath
if (-not (Test-Path -LiteralPath $clientPath -PathType Leaf)) {
    throw "MariaDB Client가 없습니다: $clientPath"
}
$hostName = [string]$core.host
$port = [int]$core.port
$sslModeProperty = $core.PSObject.Properties["sslMode"]
$sslMode = if ($null -ne $sslModeProperty) {
    ([string]$sslModeProperty.Value).Trim().ToLowerInvariant()
} else {
    "preferred"
}
$sslCaProperty = $core.PSObject.Properties["sslCaPath"]
$sslCaPath = if ($null -ne $sslCaProperty) { [string]$sslCaProperty.Value } else { "" }

$metadataPath = Join-Path $Root "cpf-tools/db/metadata/platform-schema-comment-migration-v58.json"
$metadata = Get-Content -LiteralPath $metadataPath -Raw -Encoding UTF8 | ConvertFrom-Json
if ([string]$metadata.migrationVersion -ne "V58") {
    throw "지원하지 않는 Platform Comment Migration metadata입니다: $($metadata.migrationVersion)"
}
$migrationPath = Join-Path $Root "cpf-tools/db/vendor/mariadb/migration/flyway/V58__platform_schema_comments.sql"
$rollbackPath = Join-Path $Root "cpf-tools/db/vendor/mariadb/rollback/V58__platform_schema_comments_rollback.sql"
foreach ($path in @($migrationPath, $rollbackPath)) {
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Platform Migration artifact가 없습니다: $path"
    }
}

function Assert-Identifier {
    param([string] $Value, [string] $DisplayName)
    if ($Value -notmatch '^[A-Za-z][A-Za-z0-9_]{0,63}$') {
        throw "$DisplayName 식별자 규칙 위반: $Value"
    }
}

function New-MariaStartInfo {
    param([bool] $RedirectInput)

    $psi = [System.Diagnostics.ProcessStartInfo]::new()
    $psi.FileName = $clientPath
    $psi.UseShellExecute = $false
    $psi.RedirectStandardInput = $RedirectInput
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.StandardOutputEncoding = [System.Text.Encoding]::UTF8
    $psi.StandardErrorEncoding = [System.Text.Encoding]::UTF8
    if ($RedirectInput) {
        $psi.StandardInputEncoding = [System.Text.Encoding]::UTF8
    }
    foreach ($argument in @(
        "--protocol=tcp",
        "--host=$hostName",
        "--port=$port",
        "--user=$AdminUsername",
        "--default-character-set=utf8mb4",
        "--batch",
        "--skip-column-names"
    )) {
        [void]$psi.ArgumentList.Add($argument)
    }
    switch ($sslMode) {
        "disabled" {
            [void]$psi.ArgumentList.Add("--ssl=0")
        }
        "preferred" {
            # MariaDB Client의 preferred 기본 negotiation을 사용합니다.
        }
        "required" {
            [void]$psi.ArgumentList.Add("--ssl=1")
            [void]$psi.ArgumentList.Add("--ssl-verify-server-cert=0")
        }
        "verify-full" {
            if ([string]::IsNullOrWhiteSpace($sslCaPath) -or
                -not (Test-Path -LiteralPath $sslCaPath -PathType Leaf)) {
                throw "sslMode=verify-full에는 유효한 sslCaPath가 필요합니다."
            }
            [void]$psi.ArgumentList.Add("--ssl=1")
            [void]$psi.ArgumentList.Add("--ssl-verify-server-cert")
            [void]$psi.ArgumentList.Add("--ssl-ca=$sslCaPath")
        }
        default {
            throw "지원하지 않는 sslMode입니다: $sslMode"
        }
    }
    $psi.Environment["MYSQL_PWD"] = $AdminPassword
    $psi.Environment["MARIADB_PWD"] = $AdminPassword
    return $psi
}

function Invoke-MariaText {
    param([string] $SqlText)

    $process = [System.Diagnostics.Process]::new()
    $process.StartInfo = New-MariaStartInfo $true
    try {
        if (-not $process.Start()) {
            throw "MariaDB Client process를 시작하지 못했습니다."
        }
        $process.StandardInput.Write($SqlText)
        $process.StandardInput.Close()
        $stdout = $process.StandardOutput.ReadToEnd()
        $stderr = $process.StandardError.ReadToEnd()
        $process.WaitForExit()
        if ($process.ExitCode -ne 0) {
            throw "MariaDB SQL 실행 실패 exitCode=$($process.ExitCode): $($stderr.Trim())"
        }
        return $stdout.TrimEnd()
    } finally {
        $process.Dispose()
    }
}

function Get-Sha256 {
    param([string] $Text)
    $sha = [System.Security.Cryptography.SHA256]::Create()
    try {
        return [Convert]::ToHexString(
            $sha.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($Text))
        ).ToLowerInvariant()
    } finally {
        $sha.Dispose()
    }
}

$columnClauses = [System.Collections.Generic.List[string]]::new()
$tableClauses = [System.Collections.Generic.List[string]]::new()
$definitionTableClauses = [System.Collections.Generic.List[string]]::new()
$referentialClauses = [System.Collections.Generic.List[string]]::new()
$expectedColumnCount = 0
$expectedTableCount = 0
foreach ($change in $metadata.changes) {
    $schemaName = [string]$change.schema
    $tableName = [string]$change.table
    Assert-Identifier $schemaName "schema"
    Assert-Identifier $tableName "table"
    $escapedColumns = [System.Collections.Generic.List[string]]::new()
    foreach ($column in $change.columns) {
        $columnName = [string]$column
        Assert-Identifier $columnName "column"
        $escapedColumns.Add("'$columnName'")
        $expectedColumnCount++
    }
    $columnClauses.Add(
        "(TABLE_SCHEMA='$schemaName' AND TABLE_NAME='$tableName' AND COLUMN_NAME IN ($($escapedColumns -join ',')))"
    )
    $definitionTableClauses.Add("(TABLE_SCHEMA='$schemaName' AND TABLE_NAME='$tableName')")
    $referentialClauses.Add("(CONSTRAINT_SCHEMA='$schemaName' AND TABLE_NAME='$tableName')")
    if ([bool]$change.tableCommentAdded) {
        $tableClauses.Add("(TABLE_SCHEMA='$schemaName' AND TABLE_NAME='$tableName')")
        $expectedTableCount++
    }
}
if ($expectedColumnCount -le 0 -or $expectedTableCount -le 0) {
    throw "V58 metadata 대상이 비어 있습니다."
}

$columnWhere = $columnClauses -join " OR "
$tableWhere = $tableClauses -join " OR "
$definitionTableWhere = $definitionTableClauses -join " OR "
$referentialWhere = $referentialClauses -join " OR "
$definitionSql = @"
SELECT TABLE_SCHEMA,TABLE_NAME,COLUMN_NAME,COLUMN_TYPE,IS_NULLABLE,
       COALESCE(COLUMN_DEFAULT,'<NULL>'),EXTRA,ORDINAL_POSITION
FROM information_schema.COLUMNS
WHERE $columnWhere
ORDER BY TABLE_SCHEMA,TABLE_NAME,ORDINAL_POSITION;
SELECT TABLE_SCHEMA,TABLE_NAME,INDEX_NAME,NON_UNIQUE,SEQ_IN_INDEX,COLUMN_NAME,COALESCE(SUB_PART,0)
FROM information_schema.STATISTICS
WHERE $definitionTableWhere
ORDER BY TABLE_SCHEMA,TABLE_NAME,INDEX_NAME,SEQ_IN_INDEX;
SELECT CONSTRAINT_SCHEMA,TABLE_NAME,CONSTRAINT_NAME,REFERENCED_TABLE_NAME
FROM information_schema.REFERENTIAL_CONSTRAINTS
WHERE $referentialWhere
ORDER BY CONSTRAINT_SCHEMA,TABLE_NAME,CONSTRAINT_NAME;
"@
$commentCountSql = @"
SELECT COUNT(*),SUM(CASE WHEN COLUMN_COMMENT <> '' THEN 1 ELSE 0 END)
FROM information_schema.COLUMNS
WHERE $columnWhere;
SELECT COUNT(*),SUM(CASE WHEN TABLE_COMMENT <> '' THEN 1 ELSE 0 END)
FROM information_schema.TABLES
WHERE $tableWhere;
"@

function Get-CommentState {
    $lines = @((Invoke-MariaText $commentCountSql) -split "`r?`n")
    if ($lines.Count -ne 2) {
        throw "Comment count query 결과가 예상과 다릅니다: $($lines -join ' | ')"
    }
    $columnParts = $lines[0] -split "`t"
    $tableParts = $lines[1] -split "`t"
    return [ordered]@{
        columnTargetCount = [int]$columnParts[0]
        columnCommentCount = [int]$columnParts[1]
        tableTargetCount = [int]$tableParts[0]
        tableCommentCount = [int]$tableParts[1]
    }
}

function Assert-State {
    param(
        [string] $Stage,
        $State,
        [int] $ExpectedCommentCount
    )
    $expectedTableCommentCount = if ($ExpectedCommentCount -eq 0) {
        0
    } else {
        $expectedTableCount
    }
    if ($State.columnTargetCount -ne $expectedColumnCount -or
        $State.tableTargetCount -ne $expectedTableCount -or
        $State.columnCommentCount -ne $ExpectedCommentCount -or
        $State.tableCommentCount -ne $expectedTableCommentCount) {
        throw "$Stage Comment 상태 불일치: $($State | ConvertTo-Json -Compress)"
    }
}

$startedAt = (Get-Date).ToString("o")
if ($VerifyOnly) {
    $verifyState = Get-CommentState
    Assert-State "verifyOnly" $verifyState $expectedColumnCount
    $verifyForeignKeyChecks = (Invoke-MariaText "SELECT @@FOREIGN_KEY_CHECKS;").Trim()
    if ($verifyForeignKeyChecks -ne "1") {
        throw "Fresh Install Verify에서 FOREIGN_KEY_CHECKS가 1이 아닙니다: $verifyForeignKeyChecks"
    }
    $verifyResult = [ordered]@{
        startedAt = $startedAt
        completedAt = (Get-Date).ToString("o")
        status = "완료"
        mode = "verify-only"
        vendor = "mariadb"
        host = $hostName
        port = $port
        expectedColumnComments = $expectedColumnCount
        expectedTableComments = $expectedTableCount
        state = $verifyState
        definitionHash = Get-Sha256 (Invoke-MariaText $definitionSql)
        foreignKeyChecks = [int]$verifyForeignKeyChecks
    }
    $verifyResultPath = Join-Path $ResultDir "platform-schema-comment-fresh-verify.sanitized.json"
    [System.IO.File]::WriteAllText(
        $verifyResultPath,
        ($verifyResult | ConvertTo-Json -Depth 10) + [Environment]::NewLine,
        $Utf8NoBom
    )
    Write-Host "Platform V58 Fresh Install comment verify passed."
    Write-Host "Sanitized result: $verifyResultPath"
    return
}

$baselineDefinition = Invoke-MariaText $definitionSql
$baselineDefinitionHash = Get-Sha256 $baselineDefinition
$baselineState = Get-CommentState
Assert-State "baseline" $baselineState 0

Invoke-MariaText ([System.IO.File]::ReadAllText($migrationPath, [System.Text.Encoding]::UTF8)) | Out-Null
$upgradeDefinitionHash = Get-Sha256 (Invoke-MariaText $definitionSql)
$upgradeState = Get-CommentState
Assert-State "upgrade" $upgradeState $expectedColumnCount
if ($upgradeDefinitionHash -ne $baselineDefinitionHash) {
    throw "V58 Upgrade가 Comment 외 Column/Index/FK 정의를 변경했습니다."
}

Invoke-MariaText ([System.IO.File]::ReadAllText($rollbackPath, [System.Text.Encoding]::UTF8)) | Out-Null
$rollbackDefinitionHash = Get-Sha256 (Invoke-MariaText $definitionSql)
$rollbackState = Get-CommentState
Assert-State "rollback" $rollbackState 0
if ($rollbackDefinitionHash -ne $baselineDefinitionHash) {
    throw "V58 Rollback이 원래 Column/Index/FK 정의를 복원하지 못했습니다."
}

Invoke-MariaText ([System.IO.File]::ReadAllText($migrationPath, [System.Text.Encoding]::UTF8)) | Out-Null
$reUpgradeDefinitionHash = Get-Sha256 (Invoke-MariaText $definitionSql)
$reUpgradeState = Get-CommentState
Assert-State "reUpgrade" $reUpgradeState $expectedColumnCount
if ($reUpgradeDefinitionHash -ne $baselineDefinitionHash) {
    throw "V58 Re-upgrade가 Comment 외 Column/Index/FK 정의를 변경했습니다."
}

$foreignKeyChecks = (Invoke-MariaText "SELECT @@FOREIGN_KEY_CHECKS;").Trim()
if ($foreignKeyChecks -ne "1") {
    throw "V58 lifecycle 후 FOREIGN_KEY_CHECKS가 복원되지 않았습니다: $foreignKeyChecks"
}

$result = [ordered]@{
    startedAt = $startedAt
    completedAt = (Get-Date).ToString("o")
    status = "완료"
    vendor = "mariadb"
    host = $hostName
    port = $port
    migration = "V58__platform_schema_comments.sql"
    rollback = "V58__platform_schema_comments_rollback.sql"
    expectedColumnComments = $expectedColumnCount
    expectedTableComments = $expectedTableCount
    baseline = $baselineState
    upgrade = $upgradeState
    rollbackState = $rollbackState
    reUpgrade = $reUpgradeState
    definitionHash = $baselineDefinitionHash
    foreignKeyChecks = [int]$foreignKeyChecks
}
$resultPath = Join-Path $ResultDir "platform-schema-comment-migration-result.sanitized.json"
[System.IO.File]::WriteAllText(
    $resultPath,
    ($result | ConvertTo-Json -Depth 10) + [Environment]::NewLine,
    $Utf8NoBom
)
Write-Host "Platform V58 Upgrade/Rollback/Re-upgrade smoke passed."
Write-Host "Sanitized result: $resultPath"
