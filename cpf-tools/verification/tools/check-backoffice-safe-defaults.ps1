param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path
)
$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$targets = [ordered]@{
    service = "cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/backoffice/service/BzaBackofficeService.java"
    schema = "cpf-tools/db/vendor/mariadb/source/40_business_modules_schema.sql"
    migration = "cpf-tools/db/vendor/mariadb/source/migration/flyway/V60__bza_employee_safe_defaults.sql"
    rollback = "cpf-tools/db/vendor/mariadb/source/migration/rollback/V60__bza_employee_safe_defaults_rollback.sql"
}

function Require-Pattern([string] $RelativePath, [string] $Pattern) {
    $path = Join-Path $Root $RelativePath
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Backoffice/MBW safe default 필수 파일이 없습니다: $path"
    }
    $content = Get-Content -LiteralPath $path -Raw -Encoding UTF8
    if ($content -notmatch $Pattern) {
        throw "Backoffice/MBW safe default parity 실패: file=$RelativePath pattern=$Pattern"
    }
}

Require-Pattern $targets.service 'defaultText\(request\.employmentStatus\(\),"EMPLOYED"\)'
Require-Pattern $targets.schema "employment_status VARCHAR\(30\) NOT NULL DEFAULT 'EMPLOYED'"
Require-Pattern $targets.migration "SET DEFAULT 'EMPLOYED'"
Require-Pattern $targets.rollback "SET DEFAULT 'ACTIVE'"

Write-Host "[PASS] Backoffice/MBW employee safe default source/canonical/migration parity"
