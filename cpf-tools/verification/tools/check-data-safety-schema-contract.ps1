param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path
)

$ErrorActionPreference = "Stop"

function Require-File {
    param([string] $Path)
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        throw "Required file is missing: $Path"
    }
    return (Resolve-Path -LiteralPath $Path).Path
}

function Require-Contains {
    param([string] $Path, [string[]] $Patterns)
    $text = Get-Content -LiteralPath $Path -Raw -Encoding UTF8
    foreach ($pattern in $Patterns) {
        if ($text -notmatch $pattern) {
            throw "Schema contract is missing pattern '$pattern': $Path"
        }
    }
}

function Require-ManifestHash {
    param([string] $Path, [string] $ManifestPath)
    $name = [IO.Path]::GetFileName($Path)
    $entry = @(
        Get-Content -LiteralPath $ManifestPath -Encoding UTF8 |
            Where-Object { $_ -match "^([0-9a-fA-F]{64})\s+\*?$([regex]::Escape($name))$" }
    )
    if ($entry.Count -ne 1) {
        throw "Immutable manifest entry must exist exactly once: file=$Path manifest=$ManifestPath"
    }
    $null = $entry[0] -match '^([0-9a-fA-F]{64})'
    $expected = $Matches[1].ToLowerInvariant()
    $actual = (Get-FileHash -Algorithm SHA256 -LiteralPath $Path).Hash.ToLowerInvariant()
    if ($actual -cne $expected) {
        throw "Immutable manifest hash drift: $Path"
    }
}

function Require-PublishedHash {
    param([string] $Path)
    $inventoryPath = Require-File (Join-Path $Root "cpf-docs/deliverables/SHA256SUMS.txt")
    $relative = [IO.Path]::GetRelativePath($Root, $Path).Replace('\', '/')
    $entry = @(
        Get-Content -LiteralPath $inventoryPath -Encoding UTF8 |
            Where-Object { $_ -match "^([0-9a-fA-F]{64})\s+$([regex]::Escape($relative))$" }
    )
    if ($entry.Count -ne 1) {
        throw "Published immutable hash must exist exactly once: $relative"
    }
    $null = $entry[0] -match '^([0-9a-fA-F]{64})'
    $expected = $Matches[1].ToLowerInvariant()
    $actual = (Get-FileHash -Algorithm SHA256 -LiteralPath $Path).Hash.ToLowerInvariant()
    if ($actual -cne $expected) {
        throw "Published immutable hash drift: $relative"
    }
}

$dbRoot = Join-Path $Root "cpf-tools/db/vendor/mariadb"
$admSchema = Require-File (Join-Path $dbRoot "source/10_cpf_schema.sql")
$backofficeSchema = Require-File (Join-Path $dbRoot "source/40_business_modules_schema.sql")

Require-Contains $admSchema @(
    'ACCOUNT_STATUS\s+VARCHAR\(30\)\s+NOT NULL\s+DEFAULT\s+''PENDING_ACTIVATION''',
    'VERSION_NO\s+BIGINT\s+NOT NULL\s+DEFAULT\s+0',
    'CREATE_OPERATION_ID\s+VARCHAR\(100\)',
    'uk_adm_operator_create_operation',
    'ck_adm_operator_status',
    'DISPLAY_NAME\s+VARCHAR\(100\)'
)

Require-Contains $backofficeSchema @(
    'account_status\s+VARCHAR\(30\)\s+NOT NULL\s+DEFAULT\s+''PENDING_ACTIVATION''',
    'create_operation_id\s+VARCHAR\(100\)',
    'uk_mbw_admin_user_create_operation',
    'CREATE TABLE IF NOT EXISTS\s+MBW_LOGIN_OPERATION',
    'operation_status\s+VARCHAR\(20\)\s+NOT NULL\s+DEFAULT\s+''PROCESSING''',
    'login_operation_id\s+VARCHAR\(100\)',
    'ix_mbw_refresh_token_login_operation',
    "ck_mbw_employee_status CHECK \(employment_status IN \('EMPLOYED','ON_LEAVE','SECONDMENT','DISPATCHED','RETIRED','TERMINATED'\)\)"
)

$versions = @('V61__admin_data_safety_status.sql', 'V62__bza_admin_create_idempotency.sql', 'V63__bza_login_atomic_operation.sql')
$sourceMigrationManifest = Require-File (Join-Path $dbRoot "source/migration/flyway/checksums.sha256")
$lifecycleMigrationManifest = Require-File (Join-Path $dbRoot "migration/flyway/checksums.sha256")
foreach ($name in $versions) {
    $sourceArchive = Require-File (Join-Path $dbRoot "source/migration/flyway/$name")
    $lifecycleHistory = Require-File (Join-Path $dbRoot "migration/flyway/$name")
    Require-ManifestHash $sourceArchive $sourceMigrationManifest
    Require-ManifestHash $lifecycleHistory $lifecycleMigrationManifest
}

$rollbackVersions = @('V61__admin_data_safety_status_rollback.sql', 'V62__bza_admin_create_idempotency_rollback.sql', 'V63__bza_login_atomic_operation_rollback.sql')
$sourceRollbackManifest = Require-File (Join-Path $dbRoot "source/migration/rollback/checksums.sha256")
foreach ($name in $rollbackVersions) {
    $sourceArchive = Require-File (Join-Path $dbRoot "source/migration/rollback/$name")
    $lifecycleHistory = Require-File (Join-Path $dbRoot "rollback/$name")
    Require-ManifestHash $sourceArchive $sourceRollbackManifest
    Require-PublishedHash $sourceArchive
    Require-PublishedHash $lifecycleHistory
}

$sourceV61 = Require-File (Join-Path $dbRoot "source/migration/flyway/V61__admin_data_safety_status.sql")
Require-Contains $sourceV61 @(
    'ADD COLUMN IF NOT EXISTS\s+ACCOUNT_STATUS',
    'ADD COLUMN IF NOT EXISTS\s+account_status',
    'ADD UNIQUE INDEX IF NOT EXISTS\s+uk_adm_operator_create_operation',
    'DROP CONSTRAINT IF EXISTS\s+ck_adm_operator_status',
    'DROP CONSTRAINT IF EXISTS\s+ck_mbw_admin_user_status',
    'UPDATE mbw_employee SET employment_status = ''EMPLOYED'' WHERE employment_status = ''ACTIVE'''
)
$lifecycleV61 = Require-File (Join-Path $dbRoot "migration/flyway/V61__admin_data_safety_status.sql")
Require-Contains $lifecycleV61 @(
    'ADD UNIQUE INDEX IF NOT EXISTS\s+uk_adm_operator_create_operation',
    'DROP CONSTRAINT IF EXISTS\s+ck_bza_admin_user_status',
    'UPDATE bza_employee SET employment_status = ''EMPLOYED'' WHERE employment_status = ''ACTIVE'''
)

foreach ($rollbackPath in @(
    (Require-File (Join-Path $dbRoot "source/migration/rollback/V61__admin_data_safety_status_rollback.sql")),
    (Require-File (Join-Path $dbRoot "rollback/V61__admin_data_safety_status_rollback.sql"))
)) {
    Require-Contains $rollbackPath @(
        'exact rollback to the V60-compatible schema',
        'V60.*Binary.*DB rollback',
        "employment_status IN \('ACTIVE','EMPLOYED','ON_LEAVE','SECONDMENT','DISPATCHED','RETIRED','TERMINATED'\)",
        'MODIFY COLUMN role_code VARCHAR\(50\) NOT NULL',
        'DROP COLUMN IF EXISTS account_status',
        'DROP COLUMN IF EXISTS ACCOUNT_STATUS'
    )
}

Write-Host "CPF data-safety schema contract: PASS_STATIC_ONLY"
Write-Host "Current MBW Fresh schema, independent V61-V63 immutable histories, exact rollback, and checksum evidence are aligned."
