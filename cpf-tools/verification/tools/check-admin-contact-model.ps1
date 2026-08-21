param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path
)
$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$checks = [ordered]@{
    admSchema = "cpf-tools/db/vendor/mariadb/source/30_adm_schema.sql"
    bzaSchema = "cpf-tools/db/vendor/mariadb/source/40_business_modules_schema.sql"
    migration = "cpf-tools/db/vendor/mariadb/source/migration/flyway/V59__admin_contact_model.sql"
    rollback = "cpf-tools/db/vendor/mariadb/source/migration/rollback/V59__admin_contact_model_rollback.sql"
    admService = "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmOperatorService.java"
    bzaRepository = "cpf-backoffice/online/src/main/java/com/cpf/backoffice/online/backoffice/repository/BzaBackofficeRepository.java"
    bzaFindEmployeesTemplate = "cpf-tools/db/runtime-template/bza/repository/backoffice-repository-find-employees-01.sql.template"
    bzaFindEmployeeTemplate = "cpf-tools/db/runtime-template/bza/repository/backoffice-repository-find-employee-01.sql.template"
    bzaRawContactTemplate = "cpf-tools/db/runtime-template/bza/repository/backoffice-repository-find-employee-raw-contact-01.sql.template"
    admUi = "cpf-admin/frontend/src/features/operators/OperatorsPage.vue"
    bzaUi = "cpf-backoffice-web/frontend/src/features/employees/components/EmployeeChangeForm.vue"
}

foreach ($entry in $checks.GetEnumerator()) {
    $path = Join-Path $Root $entry.Value
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Admin contact model 필수 파일이 없습니다: $($entry.Key)=$path"
    }
}

function Require-Text([string] $RelativePath, [string[]] $Patterns) {
    $path = Join-Path $Root $RelativePath
    $content = Get-Content -LiteralPath $path -Raw -Encoding UTF8
    foreach ($pattern in $Patterns) {
        if ($content -notmatch $pattern) {
            throw "Admin contact model parity 실패: file=$RelativePath pattern=$pattern"
        }
    }
}

$admSchemaContent = Get-Content -LiteralPath (Join-Path $Root $checks.admSchema) -Raw -Encoding UTF8
$identityMatch = [regex]::Match($admSchemaContent, '(?s)CREATE TABLE IF NOT EXISTS adm_operator \((.*?)\) ENGINE=InnoDB')
if (-not $identityMatch.Success) {
    throw "adm_operator Identity 정의를 찾을 수 없습니다."
}
if ($identityMatch.Groups[1].Value -match 'MOBILE_NO|OFFICE_PHONE_NO') {
    throw "ADM 연락처는 인증 Identity adm_operator가 아니라 adm_operator_profile이 소유해야 합니다."
}
$profileMatch = [regex]::Match($admSchemaContent, '(?s)CREATE TABLE IF NOT EXISTS adm_operator_profile \((.*?)\) ENGINE=InnoDB')
if (-not $profileMatch.Success -or $profileMatch.Groups[1].Value -notmatch 'MOBILE_NO' -or $profileMatch.Groups[1].Value -notmatch 'OFFICE_PHONE_NO') {
    throw "ADM 연락처 Profile 컬럼이 adm_operator_profile에 없습니다."
}

Require-Text $checks.bzaSchema @("mobile_no", "office_phone_no")
Require-Text $checks.migration @("ALTER TABLE adm_operator_profile", "MOBILE_NO", "OFFICE_PHONE_NO", "USE bzaDB", "office_phone_no")
Require-Text $checks.rollback @("ALTER TABLE adm_operator_profile", "DROP COLUMN IF EXISTS OFFICE_PHONE_NO", "DROP COLUMN IF EXISTS office_phone_no")
Require-Text $checks.admService @("LEFT JOIN adm_operator_profile", "upsertOperatorContactProfile", "MOBILE_NO", "OFFICE_PHONE_NO")
Require-Text $checks.bzaRepository @(
    "CpfVendorSqlCatalog",
    'sqlCatalogProvider\.forModule\("bza"\)',
    'sql\.required\("backoffice-repository-find-employees-01"\)',
    'sql\.required\("backoffice-repository-find-employee-01"\)',
    'sql\.required\("backoffice-repository-find-employee-raw-contact-01"\)'
)
foreach ($queryTemplate in @(
    $checks.bzaFindEmployeesTemplate,
    $checks.bzaFindEmployeeTemplate,
    $checks.bzaRawContactTemplate
)) {
    Require-Text $queryTemplate @("mobile_no AS mobileNo", "office_phone_no AS officePhoneNo")
}
foreach ($vendor in @("mariadb", "postgresql", "oracle")) {
    foreach ($queryKey in @(
        "backoffice-repository-find-employees-01",
        "backoffice-repository-find-employee-01",
        "backoffice-repository-find-employee-raw-contact-01"
    )) {
        $renderedQuery = "cpf-tools/db/vendor/$vendor/runtime/bza/repository/$queryKey.sql"
        if (-not (Test-Path -LiteralPath (Join-Path $Root $renderedQuery) -PathType Leaf)) {
            throw "Admin contact model DB3 runtime query가 없습니다: $renderedQuery"
        }
        Require-Text $renderedQuery @("mobile_no AS mobileNo", "office_phone_no AS officePhoneNo")
    }
}
Require-Text $checks.admUi @("연락처\(휴대폰\)", "내부 전화번호")
Require-Text $checks.bzaUi @("연락처\(휴대폰\)", "내부 전화번호")

Write-Host "[PASS] ADM/MBW contact model identity/profile ownership + API/UI/migration static parity"
