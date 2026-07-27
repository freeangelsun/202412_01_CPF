<#
.SYNOPSIS
  ADM/BZA Data Safety 정적 Gate를 실행합니다.
.DESCRIPTION
  제품 DB fail-closed, 운영자/관리자 상태 모델, PII masked/raw 경계, BZA Query Contract,
  MariaDB V61 Canonical/Migration/Rollback/Bundle parity를 빠르게 검증합니다.
  DEV_ONLY/CI_RELEASE Gate이며 Runtime 제품 배포물에는 포함하지 않습니다.
.PARAMETER RootPath
  CPF Repository root. 생략 시 script 위치 기준 ../.. 를 사용합니다.
.EXAMPLE
  pwsh -NoProfile -File .\cpf-tools\scripts\check-admin-data-safety.ps1
.EXAMPLE
  pwsh -NoProfile -File .\cpf-tools\scripts\check-admin-data-safety.ps1 -RootPath C:\dev\202412_01_CPF
.NOTES
  이 Gate는 정적 검증입니다. 실제 MariaDB upgrade/rollback/reapply, Browser, Multi-instance 검증을 대체하지 않습니다.
#>
param(
    [string]$RootPath = ""
)
$ErrorActionPreference = 'Stop'
if ([string]::IsNullOrWhiteSpace($RootPath)) {
    $RootPath = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
}
function Require-File([string]$relative) {
    $path = Join-Path $RootPath $relative
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "필수 파일이 없습니다: $relative" }
    return $path
}
function Require-Contains([string]$relative,[string]$pattern,[string]$message) {
    $text = Get-Content -LiteralPath (Require-File $relative) -Raw -Encoding UTF8
    if ($text -notmatch $pattern) { throw "$message ($relative)" }
}
function Require-NotContains([string]$relative,[string]$pattern,[string]$message) {
    $text = Get-Content -LiteralPath (Require-File $relative) -Raw -Encoding UTF8
    if ($text -match $pattern) { throw "$message ($relative)" }
}

$admService = 'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmOperatorService.java'
$admPolicy = 'cpf-admin/src/main/java/com/cpf/admin/config/AdmPersistencePolicy.java'
$bzaEmployee = 'cpf-biz-admin/src/main/java/com/cpf/bizadmin/backoffice/service/BzaBackofficeService.java'
$bzaAudit = 'cpf-biz-admin/src/main/java/com/cpf/bizadmin/audit/service/BzaBusinessAuditService.java'
$v61Source = 'cpf-tools/db/vendor/mariadb/source/migration/flyway/V61__admin_data_safety_status.sql'
$v61Mirror = 'cpf-tools/db/vendor/mariadb/migration/flyway/V61__admin_data_safety_status.sql'

Require-Contains $admPolicy 'DATABASE' 'ADM 제품 기본 영속성 모드가 DATABASE가 아닙니다.'
Require-Contains 'cpf-admin/src/main/resources/application-adm-prod.yml' 'mode:\s*DATABASE' 'ADM prod가 DATABASE fail-closed가 아닙니다.'
Require-Contains $admService '@Transactional\(transactionManager = "admTransactionManager"\)' 'ADM 변경 메서드 Transaction 경계가 없습니다.'
Require-Contains $admService 'PENDING_ACTIVATION' 'ADM 신규 운영자 PENDING_ACTIVATION 정책이 없습니다.'
Require-Contains $admService 'CREATE_OPERATION_ID' 'ADM 생성 idempotency operationId 저장 계약이 없습니다.'
Require-Contains $admService 'useMemoryFallbackOrThrow' 'ADM DB 오류 fail-closed/fallback 분기점이 없습니다.'
Require-Contains $admService 'CpfSensitiveData\.maskPhone' 'ADM 기본 연락처 Projection이 마스킹되지 않았습니다.'
Require-Contains $bzaEmployee 'EMPLOYED' 'BZA EMPLOYED 안전 기본값이 없습니다.'
Require-Contains $bzaEmployee 'BzaEmploymentStatus\.parse' 'BZA 재직상태 Catalog 검증이 없습니다.'
Require-Contains $bzaEmployee 'CpfSensitiveData\.maskEmail' 'BZA 기본 이메일 Projection이 마스킹되지 않았습니다.'
Require-Contains $bzaEmployee 'EMPLOYEE_PII_RAW_VIEW' 'BZA 원문 조회 감사 이벤트가 없습니다.'
Require-Contains $bzaAudit '\[MASKED\]' 'BZA 감사 Snapshot PII 마스킹이 없습니다.'
Require-Contains $bzaAudit '\[REDACTED\]' 'BZA 감사 Snapshot Secret 제거가 없습니다.'

$moduleRoots = @('cpf-admin/src/main/java','cpf-biz-admin/src/main/java')
foreach ($moduleRoot in $moduleRoots) {
    $dir = Join-Path $RootPath $moduleRoot
    $bad = Get-ChildItem -LiteralPath $dir -Recurse -Filter '*.java' | Select-String -Pattern 'import\s+com\.cpf\.core\.common\.'
    if ($bad) { throw "ADM/BZA가 cpf-core internal package를 직접 참조합니다: $($bad[0].Path):$($bad[0].LineNumber)" }
}

# BZA Vendor SQL은 Java literal이 아니라 중앙 Vendor Query Contract에 있어야 합니다.
$bzaJava = Join-Path $RootPath 'cpf-biz-admin/src/main/java'
$sqlPattern = '(?is)(?:"{3}\s*(SELECT|INSERT|UPDATE|DELETE|WITH)\b|"\s*(SELECT|INSERT\s+INTO|UPDATE\s+[A-Za-z_]|DELETE\s+FROM|WITH\s+RECURSIVE)\b)'
foreach ($java in Get-ChildItem -LiteralPath $bzaJava -Recurse -Filter '*.java') {
    $content = Get-Content -LiteralPath $java.FullName -Raw -Encoding UTF8
    if ([regex]::IsMatch($content, $sqlPattern)) {
        throw "BZA Java inline SQL이 남아 있습니다: $($java.FullName)"
    }
}

# MariaDB는 현재 구현된 GA lifecycle이므로 모든 정적 Query Key가 실제 파일을 가져야 합니다.
$queryRoot = Join-Path $RootPath 'cpf-tools/db/vendor/mariadb/runtime/bza/repository'
$known = @{}
Get-ChildItem -LiteralPath $queryRoot -Filter '*.sql' | ForEach-Object { $known[$_.BaseName] = $true }
$missing = New-Object System.Collections.Generic.List[string]
Get-ChildItem -LiteralPath $bzaJava -Recurse -Filter '*.java' | ForEach-Object {
    $javaFile = $_.FullName
    $content = Get-Content -LiteralPath $javaFile -Raw -Encoding UTF8
    [regex]::Matches($content,'sql\.required\("([^"]+)"\)') | ForEach-Object {
        $key = $_.Groups[1].Value
        if (-not $known.ContainsKey($key)) { $missing.Add("$key <- $javaFile") }
    }
}
if ($missing.Count -gt 0) { throw "BZA Query Contract resource 누락: $($missing -join '; ')" }

$sourceHash = (Get-FileHash -LiteralPath (Require-File $v61Source) -Algorithm SHA256).Hash
$mirrorHash = (Get-FileHash -LiteralPath (Require-File $v61Mirror) -Algorithm SHA256).Hash
if ($sourceHash -ne $mirrorHash) { throw 'V61 source/migration mirror hash가 다릅니다.' }
Require-Contains $v61Source 'ACCOUNT_STATUS' 'V61 ADM/BZA account status migration이 없습니다.'
Require-Contains $v61Source 'EMPLOYED' 'V61 legacy employee status 정규화가 없습니다.'
Require-Contains 'cpf-tools/db/vendor/mariadb/source/30_adm_schema.sql' 'PENDING_ACTIVATION' 'ADM fresh schema safe default가 없습니다.'
Require-Contains 'cpf-tools/db/vendor/mariadb/source/40_business_modules_schema.sql' 'EMPLOYED' 'BZA fresh schema employment default가 없습니다.'

Require-Contains 'cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmOperatorController.java' '@PostMapping\("/\{operatorId\}/contacts/raw"\)' 'ADM PII raw 조회는 query-string GET이 아니라 audited POST여야 합니다.'
Require-Contains 'cpf-biz-admin/src/main/java/com/cpf/bizadmin/backoffice/controller/BzaBackofficeController.java' '@PostMapping\("/employees/\{employeeNo\}/contacts/raw"\)' 'BZA PII raw 조회는 query-string GET이 아니라 audited POST여야 합니다.'
Require-NotContains 'cpf-admin/frontend/src/app/accessMethods.ts' 'contacts/raw\?reason=' 'ADM PII 조회 사유가 URL query string에 남아 있습니다.'
Require-NotContains 'cpf-biz-admin/frontend/src/components/CrudTable.vue' 'contacts/raw\?reason=|reason=\$\{encodeURIComponent' 'BZA PII 조회 사유가 URL query string에 남아 있습니다.'
Require-Contains 'cpf-tools/db/vendor/mariadb/source/60_adm_seed_data.sql' "OPERATOR_PII_RAW'.*'POST'.*/contacts/raw" 'ADM PII raw Permission Seed HTTP method가 POST와 일치하지 않습니다.'

$v61RollbackSource = 'cpf-tools/db/vendor/mariadb/source/migration/rollback/V61__admin_data_safety_status_rollback.sql'
$v61RollbackMirror = 'cpf-tools/db/vendor/mariadb/rollback/V61__admin_data_safety_status_rollback.sql'
$rollbackSourceHash = (Get-FileHash -LiteralPath (Require-File $v61RollbackSource) -Algorithm SHA256).Hash
$rollbackMirrorHash = (Get-FileHash -LiteralPath (Require-File $v61RollbackMirror) -Algorithm SHA256).Hash
if ($rollbackSourceHash -ne $rollbackMirrorHash) { throw 'V61 rollback source/lifecycle mirror hash가 다릅니다.' }
Require-NotContains $v61RollbackSource 'role_code\s+VARCHAR\(50\)\s+NOT NULL' 'V61 safe rollback이 Role 미부여 계정에 가짜 Role/실패를 강제할 수 있습니다.'

foreach ($pair in @(
    @('cpf-tools/db/vendor/mariadb/source/00_empty_install.sql','cpf-tools/db/vendor/mariadb/install/00_empty_install.sql'),
    @('cpf-tools/db/vendor/mariadb/source/00_product_seed.sql','cpf-tools/db/vendor/mariadb/seed/00_product_seed.sql'),
    @('cpf-tools/db/vendor/mariadb/source/00_verify.sql','cpf-tools/db/vendor/mariadb/verify/00_verify.sql')
)) {
    $left=(Get-FileHash -LiteralPath (Require-File $pair[0]) -Algorithm SHA256).Hash
    $right=(Get-FileHash -LiteralPath (Require-File $pair[1]) -Algorithm SHA256).Hash
    if($left -ne $right){ throw "MariaDB generated lifecycle parity 불일치: $($pair[0]) <> $($pair[1])" }
}
Require-Contains 'cpf-tools/db/vendor/mariadb/source/99_smoke_check.sql' 'VERIFY V61 status catalog constraints' 'V61 Fresh/Upgrade Verify contract가 없습니다.'

Write-Host '[PASS] CPF ADM/BZA data-safety gate'
