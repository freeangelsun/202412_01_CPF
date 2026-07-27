<#
.SYNOPSIS
  ADM/BZA Data Safety 정적 Gate를 실행합니다.
.DESCRIPTION
  제품 DB fail-closed 구조, 운영자/관리자 상태 모델, Session revoke 결과불명 복구,
  PII masked/raw 경계, BZA Query Contract, MariaDB V61~V63 Canonical/Migration/Rollback parity를 검증합니다.
  이 스크립트는 구조적 회귀를 빠르게 차단하는 STATIC_ONLY Gate이며 Runtime 행동 검증을 대체하지 않습니다.
.PARAMETER RootPath
  CPF Repository root. 생략 시 script 위치 기준 ../.. 를 사용합니다.
#>
param([Alias("Root")][string]$RootPath = "")
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
if ([string]::IsNullOrWhiteSpace($RootPath)) {
    $RootPath = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
}
function Require-File([string]$relative) {
    $path = Join-Path $RootPath $relative
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "필수 파일이 없습니다: $relative" }
    return $path
}
function Read-Text([string]$relative) {
    return Get-Content -LiteralPath (Require-File $relative) -Raw -Encoding UTF8
}
function Require-Contains([string]$relative,[string]$pattern,[string]$message) {
    if ((Read-Text $relative) -notmatch $pattern) { throw "$message ($relative)" }
}
function Require-NotContains([string]$relative,[string]$pattern,[string]$message) {
    if ((Read-Text $relative) -match $pattern) { throw "$message ($relative)" }
}
function Require-SameHash([string]$left,[string]$right,[string]$message) {
    $leftHash = (Get-FileHash -LiteralPath (Require-File $left) -Algorithm SHA256).Hash
    $rightHash = (Get-FileHash -LiteralPath (Require-File $right) -Algorithm SHA256).Hash
    if ($leftHash -ne $rightHash) { throw "$message ($left <> $right)" }
}

$admService = 'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmOperatorService.java'
$admSession = 'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmSessionService.java'
$admPermission = 'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmPermissionService.java'
$admPolicy = 'cpf-admin/src/main/java/com/cpf/admin/config/AdmPersistencePolicy.java'
$bzaEmployee = 'cpf-biz-admin/src/main/java/com/cpf/bizadmin/backoffice/service/BzaBackofficeService.java'
$bzaAudit = 'cpf-biz-admin/src/main/java/com/cpf/bizadmin/audit/service/BzaBusinessAuditService.java'
$admAuditDelivery = 'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmAuditDeliveryService.java'
$admFrontend = 'cpf-admin/frontend/src/app/methods/accessMethods.ts'
$bzaFrontend = 'cpf-biz-admin/frontend/src/components/CrudTable.vue'

# 제품 영속성 및 fail-closed 구조.
Require-Contains $admPolicy 'DATABASE' 'ADM 제품 기본 영속성 모드가 DATABASE가 아닙니다.'
Require-Contains 'cpf-admin/src/main/resources/application-adm-prod.yml' 'mode:\s*DATABASE' 'ADM prod가 DATABASE fail-closed가 아닙니다.'
Require-Contains $admSession 'persistencePolicy\.memoryEnabled\(\)' 'Session MEMORY 모드가 명시적 정책으로 제한되지 않았습니다.'
Require-Contains $admSession 'throw unavailable\(' 'Session DB 장애가 infra unavailable로 승격되지 않습니다.'
Require-Contains $admSession 'recordRevocationUnknown' 'Session revoke 결과불명 복구 기록이 없습니다.'
Require-Contains $admSession 'retryPendingRevocation' 'Session revoke 결과불명 재처리 경로가 없습니다.'
Require-Contains $admPermission 'readFailure\(' 'Permission DB read fail-closed 분기점이 없습니다.'
Require-Contains $admPermission 'throw unavailable\(' 'Permission DB 장애가 infra unavailable로 승격되지 않습니다.'
Require-Contains $admService 'useMemoryFallbackOrThrow' 'ADM 메뉴/운영자 DB 오류 fail-closed 분기점이 없습니다.'

# ADM/BZA 운영자 상태/멱등/원문 접근 경계.
Require-Contains $admService '@Transactional\(transactionManager = "admTransactionManager"\)' 'ADM 변경 메서드 Transaction 경계가 없습니다.'
Require-Contains $admService 'PENDING_ACTIVATION' 'ADM 신규 운영자 PENDING_ACTIVATION 정책이 없습니다.'
Require-Contains $admService 'CREATE_OPERATION_ID' 'ADM 생성 idempotency operationId 저장 계약이 없습니다.'
Require-Contains $admService 'CpfSensitiveData\.maskPhone' 'ADM 기본 연락처 Projection이 마스킹되지 않았습니다.'
Require-Contains $bzaEmployee 'EMPLOYED' 'BZA EMPLOYED 안전 기본값이 없습니다.'
Require-Contains $bzaEmployee 'BzaEmploymentStatus\.parse' 'BZA 재직상태 Catalog 검증이 없습니다.'
Require-Contains $bzaEmployee 'CpfSensitiveData\.maskEmail' 'BZA 기본 이메일 Projection이 마스킹되지 않았습니다.'
Require-Contains $bzaEmployee 'EMPLOYEE_PII_RAW_VIEW' 'BZA 원문 조회 감사 이벤트가 없습니다.'
Require-Contains $bzaAudit '\[MASKED\]' 'BZA 감사 Snapshot PII 마스킹이 없습니다.'
Require-Contains $bzaAudit '\[REDACTED\]' 'BZA 감사 Snapshot Secret 제거가 없습니다.'

# ADM durable audit relay는 3 Vendor 공통 SQL과 공통 PII Sanitizer를 사용해야 합니다.
Require-Contains $admAuditDelivery 'CpfSensitiveData\.sanitizeAuditReason' 'ADM 감사 사유가 공통 PII/Secret Sanitizer를 사용하지 않습니다.'
Require-Contains $admAuditDelivery 'CpfSensitiveData\.sanitizeAuditText' 'ADM 감사 Snapshot/Error가 공통 Sanitizer를 사용하지 않습니다.'
Require-Contains $admAuditDelivery "OPERATION_STATUS='UNKNOWN'" 'ADM 감사 결과불명 UNKNOWN 승격 경로가 없습니다.'
Require-Contains $admAuditDelivery 'setMaxRows\(' 'ADM 감사 relay/list가 Vendor-neutral JDBC 조회 제한을 사용하지 않습니다.'
Require-NotContains $admAuditDelivery 'LIMIT\s+\?|DATE_ADD\s*\(|TIMESTAMPADD\s*\(|POW\s*\(|CURRENT_TIMESTAMP\s*\(3\)|CONCAT\s*\(' 'ADM 감사 relay에 Vendor 전용 SQL이 남아 있습니다.'

# 외부 Module은 cpf-core internal package를 직접 참조하면 안 됩니다.
$moduleRoots = @('cpf-admin/src/main/java','cpf-biz-admin/src/main/java','cpf-gateway/src/main/java','cpf-batch')
foreach ($moduleRoot in $moduleRoots) {
    $dir = Join-Path $RootPath $moduleRoot
    if (-not (Test-Path -LiteralPath $dir -PathType Container)) { continue }
    $bad = Get-ChildItem -LiteralPath $dir -Recurse -Filter '*.java' | Select-String -Pattern 'import\s+com\.cpf\.core\.common\.'
    if ($bad) { throw "외부 Module이 cpf-core internal package를 직접 참조합니다: $($bad[0].Path):$($bad[0].LineNumber)" }
}

# BZA Java inline SQL 금지.
$bzaJava = Join-Path $RootPath 'cpf-biz-admin/src/main/java'
$sqlPattern = '(?is)(?:"{3}\s*(SELECT|INSERT|UPDATE|DELETE|WITH)\b|"\s*(SELECT|INSERT\s+INTO|UPDATE\s+[A-Za-z_]|DELETE\s+FROM|WITH\s+RECURSIVE)\b)'
foreach ($java in Get-ChildItem -LiteralPath $bzaJava -Recurse -Filter '*.java') {
    $content = Get-Content -LiteralPath $java.FullName -Raw -Encoding UTF8
    if ([regex]::IsMatch($content, $sqlPattern)) { throw "BZA Java inline SQL이 남아 있습니다: $($java.FullName)" }
}

# Raw PII는 audited POST + body reason 이어야 하며 Browser prompt/query-string에 남기지 않습니다.
Require-Contains 'cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmOperatorController.java' '@PostMapping\("/\{operatorId\}/contacts/raw"\)' 'ADM PII raw 조회가 audited POST가 아닙니다.'
Require-Contains 'cpf-biz-admin/src/main/java/com/cpf/bizadmin/backoffice/controller/BzaBackofficeController.java' '@PostMapping\("/employees/\{employeeNo\}/contacts/raw"\)' 'BZA PII raw 조회가 audited POST가 아닙니다.'
Require-NotContains $admFrontend 'window\.prompt|contacts/raw\?reason=' 'ADM Raw PII가 prompt 또는 URL query reason을 사용합니다.'
Require-NotContains $bzaFrontend 'contacts/raw\?reason=|reason=\$\{encodeURIComponent' 'BZA PII 조회 사유가 URL query string에 남아 있습니다.'
Require-Contains 'cpf-tools/db/vendor/mariadb/source/60_adm_seed_data.sql' "OPERATOR_PII_RAW'.*'POST'.*/contacts/raw" 'ADM PII raw Permission Seed HTTP method가 POST와 일치하지 않습니다.'

# MariaDB Canonical source와 lifecycle mirror는 V61~V63 모두 동일해야 합니다.
foreach ($version in @('V61__admin_data_safety_status','V62__bza_admin_create_idempotency','V63__bza_login_atomic_operation')) {
    Require-SameHash "cpf-tools/db/vendor/mariadb/source/migration/flyway/$version.sql" "cpf-tools/db/vendor/mariadb/migration/flyway/$version.sql" "$version migration source/mirror hash가 다릅니다."
}
foreach ($version in @('V61__admin_data_safety_status_rollback','V62__bza_admin_create_idempotency_rollback','V63__bza_login_atomic_operation_rollback')) {
    Require-SameHash "cpf-tools/db/vendor/mariadb/source/migration/rollback/$version.sql" "cpf-tools/db/vendor/mariadb/rollback/$version.sql" "$version rollback source/mirror hash가 다릅니다."
}
Require-Contains 'cpf-tools/db/vendor/mariadb/source/migration/flyway/V61__admin_data_safety_status.sql' 'ACCOUNT_STATUS' 'V61 ADM/BZA account status migration이 없습니다.'
Require-Contains 'cpf-tools/db/vendor/mariadb/source/migration/flyway/V61__admin_data_safety_status.sql' 'EMPLOYED' 'V61 legacy employee status 정규화가 없습니다.'
Require-Contains 'cpf-tools/db/vendor/mariadb/source/30_adm_schema.sql' 'PENDING_ACTIVATION' 'ADM fresh schema safe default가 없습니다.'
Require-Contains 'cpf-tools/db/vendor/mariadb/source/40_business_modules_schema.sql' 'EMPLOYED' 'BZA fresh schema employment default가 없습니다.'

# V61 rollback 정책은 Exact Rollback이다. V60 호환 Binary를 먼저 배포하고 DB를 V60 schema로 되돌린다.
$v61Rollback = 'cpf-tools/db/vendor/mariadb/source/migration/rollback/V61__admin_data_safety_status_rollback.sql'
Require-Contains $v61Rollback 'V60 호환 Binary 배포' 'V61 Exact Rollback 운영 순서가 명시되지 않았습니다.'
Require-Contains $v61Rollback 'role_code\s+VARCHAR\(50\)\s+NOT NULL' 'V61 Exact Rollback이 V60 role_code NOT NULL 계약을 복원하지 않습니다.'
Require-Contains $v61Rollback "'ACTIVE'.*'EMPLOYED'.*'ON_LEAVE'.*'SECONDMENT'.*'DISPATCHED'.*'RETIRED'.*'TERMINATED'" 'V61 Exact Rollback이 V60 호환 employment status catalog를 복원하지 않습니다.'
Require-NotContains $v61Rollback 'UPDATE\s+bza_admin_user\s+SET\s+role_code\s*=' 'V61 rollback이 임의 Role을 주입합니다.'
Require-Contains 'cpf-docs/guide/CPF_V61_EXACT_ROLLBACK_RUNBOOK.md' 'V60' 'V61 Exact Rollback Runbook이 없습니다.'

# 기존 generated lifecycle parity.
foreach ($pair in @(
    @('cpf-tools/db/vendor/mariadb/source/00_empty_install.sql','cpf-tools/db/vendor/mariadb/install/00_empty_install.sql'),
    @('cpf-tools/db/vendor/mariadb/source/00_product_seed.sql','cpf-tools/db/vendor/mariadb/seed/00_product_seed.sql'),
    @('cpf-tools/db/vendor/mariadb/source/00_verify.sql','cpf-tools/db/vendor/mariadb/verify/00_verify.sql')
)) {
    Require-SameHash $pair[0] $pair[1] 'MariaDB generated lifecycle parity가 깨졌습니다.'
}
Require-Contains 'cpf-tools/db/vendor/mariadb/source/99_smoke_check.sql' 'VERIFY V61 status catalog constraints' 'V61 Fresh/Upgrade Verify contract가 없습니다.'

# 더 강한 양방향 Query Contract 및 공식 DB vendor readiness gate를 반드시 함께 실행한다.
& (Join-Path $PSScriptRoot 'check-query-contract-integrity.ps1') -Root $RootPath
& (Join-Path $PSScriptRoot 'check-official-db-vendor-readiness.ps1') -Root $RootPath

Write-Host '[PASS][STATIC_ONLY] CPF ADM/BZA data-safety structural gate'
Write-Host '[INFO] Runtime DB outage, upgrade/rollback/reapply, Browser, multi-instance 행동 검증은 별도 Evidence가 필요합니다.'
