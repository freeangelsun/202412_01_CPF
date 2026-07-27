param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path
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

function Require-SameHash {
    param([string] $Left, [string] $Right)
    $leftHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $Left).Hash
    $rightHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $Right).Hash
    if ($leftHash -ne $rightHash) {
        throw "Canonical/lifecycle drift: $Left != $Right"
    }
}

$dbRoot = Join-Path $Root "cpf-tools/db/vendor/mariadb"
$admSchema = Require-File (Join-Path $dbRoot "source/30_adm_schema.sql")
$bzaSchema = Require-File (Join-Path $dbRoot "source/40_business_modules_schema.sql")

Require-Contains $admSchema @(
    'ACCOUNT_STATUS\s+VARCHAR\(30\)\s+NOT NULL\s+DEFAULT\s+''PENDING_ACTIVATION''',
    'VERSION_NO\s+BIGINT\s+NOT NULL\s+DEFAULT\s+0',
    'CREATE_OPERATION_ID\s+VARCHAR\(100\)',
    'uk_adm_operator_create_operation',
    'ck_adm_operator_status',
    'DISPLAY_NAME\s+VARCHAR\(100\)'
)

Require-Contains $bzaSchema @(
    'account_status\s+VARCHAR\(30\)\s+NOT NULL\s+DEFAULT\s+''PENDING_ACTIVATION''',
    'create_operation_id\s+VARCHAR\(100\)',
    'uk_bza_admin_user_create_operation',
    'CREATE TABLE IF NOT EXISTS\s+bza_login_operation',
    'operation_status\s+VARCHAR\(20\)\s+NOT NULL\s+DEFAULT\s+''PROCESSING''',
    'login_operation_id\s+VARCHAR\(100\)',
    'ix_bza_refresh_token_login_operation',
    "ck_bza_employee_status CHECK \(employment_status IN \('EMPLOYED','ON_LEAVE','SECONDMENT','DISPATCHED','RETIRED','TERMINATED'\)\)"
)

$versions = @('V61__admin_data_safety_status.sql', 'V62__bza_admin_create_idempotency.sql', 'V63__bza_login_atomic_operation.sql')
foreach ($name in $versions) {
    $canonical = Require-File (Join-Path $dbRoot "source/migration/flyway/$name")
    $lifecycle = Require-File (Join-Path $dbRoot "migration/flyway/$name")
    Require-SameHash $canonical $lifecycle
}

$rollbackVersions = @('V61__admin_data_safety_status_rollback.sql', 'V62__bza_admin_create_idempotency_rollback.sql', 'V63__bza_login_atomic_operation_rollback.sql')
foreach ($name in $rollbackVersions) {
    $canonical = Require-File (Join-Path $dbRoot "source/migration/rollback/$name")
    $lifecycle = Require-File (Join-Path $dbRoot "rollback/$name")
    Require-SameHash $canonical $lifecycle
}

$v61 = Require-File (Join-Path $dbRoot "source/migration/flyway/V61__admin_data_safety_status.sql")
Require-Contains $v61 @(
    'ADD COLUMN IF NOT EXISTS\s+ACCOUNT_STATUS',
    'ADD COLUMN IF NOT EXISTS\s+account_status',
    'ADD UNIQUE INDEX IF NOT EXISTS\s+uk_adm_operator_create_operation',
    'DROP CONSTRAINT IF EXISTS\s+ck_adm_operator_status',
    'DROP CONSTRAINT IF EXISTS\s+ck_bza_admin_user_status',
    'UPDATE bza_employee SET employment_status = ''EMPLOYED'' WHERE employment_status = ''ACTIVE'''
)

$v61Rollback = Require-File (Join-Path $dbRoot "source/migration/rollback/V61__admin_data_safety_status_rollback.sql")
Require-Contains $v61Rollback @(
    'exact rollback to the V60-compatible schema',
    'V60.*Binary.*DB rollback',
    "employment_status IN \('ACTIVE','EMPLOYED','ON_LEAVE','SECONDMENT','DISPATCHED','RETIRED','TERMINATED'\)",
    'MODIFY COLUMN role_code VARCHAR\(50\) NOT NULL',
    'DROP COLUMN IF EXISTS account_status',
    'DROP COLUMN IF EXISTS ACCOUNT_STATUS'
)

$checksumCanonical = Require-File (Join-Path $dbRoot "source/migration/flyway/checksums.sha256")
$checksumLifecycle = Require-File (Join-Path $dbRoot "migration/flyway/checksums.sha256")
Require-SameHash $checksumCanonical $checksumLifecycle
$checksumText = Get-Content -LiteralPath $checksumCanonical -Raw -Encoding UTF8
foreach ($name in $versions) {
    if ($checksumText -notmatch [regex]::Escape("*$name")) {
        throw "Migration checksum entry is missing: $name"
    }
}

Write-Host "CPF data-safety schema contract: PASS_STATIC_ONLY"
Write-Host "Fresh schema, V61-V63 canonical/lifecycle mirrors, exact rollback, and checksum manifest are aligned."
