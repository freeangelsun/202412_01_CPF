param([string]$Root=(Resolve-Path "$PSScriptRoot\..\..").Path)
$ErrorActionPreference="Stop"
$Root=(Resolve-Path -LiteralPath $Root).Path
function Fail([string]$m){throw "R10 Gate FAIL: $m"}
function RequireFile([string]$p){if(-not(Test-Path (Join-Path $Root $p) -PathType Leaf)){Fail "required R10 artifact 누락: $p"}}
function RequireContains([string]$p,[string]$pattern,[string]$message){RequireFile $p;$t=Get-Content(Join-Path $Root $p)-Raw;if($t -notmatch $pattern){Fail $message}}
function RequireNotContains([string]$p,[string]$pattern,[string]$message){RequireFile $p;$t=Get-Content(Join-Path $Root $p)-Raw;if($t -match $pattern){Fail $message}}

if(Test-Path (Join-Path $Root "cpf-external")){Fail "Baseline cpf-external 잔존"}
foreach($p in @(
 "cpf-core/src/main/java/com/cpf/core/common/batch",
 "cpf-core/src/main/java/com/cpf/core/config/CpfBatchAutoConfiguration.java",
 "cpf-core/src/main/java/com/cpf/core/config/CpfCenterCutAutoConfiguration.java",
 "docker-compose.local.yml",
 "cpf-tools/db/source"
)){if(Test-Path (Join-Path $Root $p)){Fail "obsolete artifact: $p"}}

foreach($p in @(
 "cpf-core/src/main/java/com/cpf/core/api/util/CpfStrings.java",
 "cpf-core/src/main/java/com/cpf/core/api/page/CpfPage.java",
 "cpf-core/src/main/java/com/cpf/core/api/page/CpfCursorCodec.java",
 "cpf-core/src/main/java/com/cpf/core/api/page/CpfHmacCursorCodec.java",
 "cpf-core/src/main/java/com/cpf/core/api/transaction/CpfTransactionIdGenerator.java",
 "cpf-common/src/main/java/com/cpf/common/calendar/CmnCalendarService.java",
 "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmBusinessCalendarController.java",
 "cpf-admin/frontend/src/features/business-calendar/BusinessCalendarPage.vue",
 "cpf-admin/frontend/src/features/logs/LogsPage.vue",
 "cpf-tools/scripts/sync-generated-domain-artifacts.ps1",
 "cpf-tools/scripts/verify-exs-generated-domain-lifecycle.ps1",
 "cpf-tools/scripts/check-work-context.ps1",
 "cpf-tools/scripts/check-frontend-route-targets.ps1",
 "cpf-docs/work/current/CPF_INTEGRATED_VERIFICATION_PLAN.md"
)){RequireFile $p}

RequireContains "cpf-tools/db/vendor/mariadb/source/20_cmn_schema.sql" 'cmn_business_calendar_day' "CMN canonical business calendar table 누락"
RequireNotContains "cpf-tools/db/vendor/mariadb/source/35_bat_schema.sql" 'bat_business_day_calendar' "BAT legacy business calendar table 잔존"
foreach($vendor in @('mariadb','mysql','postgresql','oracle','sqlserver')){
    RequireFile "cpf-tools/db/vendor/$vendor/sample/cmn-calendar/00_cmn_business_calendar.sql"
    RequireFile "cpf-tools/db/vendor/$vendor/sample/cmn-calendar/rollback.sql"
}

$java=Get-ChildItem $Root -Recurse -File -Filter *.java|Where-Object{$_.FullName -notmatch '[\\/](build|node_modules)[\\/]'}
$legacy=@($java|Select-String -Pattern 'com\.cpf\.core\.common\.batch')
if($legacy.Count){Fail "legacy Core Batch import $($legacy.Count)건"}

$routes=Get-Content (Join-Path $Root "cpf-admin/frontend/src/app/routes.ts") -Raw
if($routes -notmatch 'businessCalendar' -or $routes -notmatch 'features/logs/LogsPage'){Fail "ADM Calendar/Log route 누락"}
RequireNotContains "cpf-admin/frontend/src/features/core/methods.ts" 'searchMembers\s*\(' "삭제된 ADM Member 초기조회 잔존"
RequireNotContains "cpf-admin/frontend/src/features/core/methods.ts" 'saveBusinessDay\s*\(' "BAT 영업일 저장 frontend 잔존"

$allText = Get-ChildItem $Root -Recurse -File -Include *.java,*.ts,*.vue |
    Where-Object{$_.FullName -notmatch '[\\/](build|node_modules)[\\/]'} |
    Select-String -Pattern '/adm/api/batch/calendar|bat_business_day_calendar' -ErrorAction SilentlyContinue
if(@($allText).Count){Fail "BAT 영업일 legacy 참조 $(@($allText).Count)건"}

RequireContains "cpf-core/src/main/java/com/cpf/core/common/logging/TransactionIdGenerator.java" 'SEQUENCE_DIGITS\s*=\s*7|sequenceDigits.*7' "transactionId 34자리/7자리 sequence 고정 근거 누락"
RequireContains "cpf-core/src/main/java/com/cpf/core/api/transaction/CpfTransactionIds.java" '34자리' "transactionId canonical validator 누락"
RequireContains "cpf-core/src/main/java/com/cpf/core/common/logging/file/CpfLogPathPolicy.java" 'transactions' "transactionId별 File Log 경로 정책 누락"
RequireContains "cpf-core/src/main/java/com/cpf/core/common/logging/file/CpfLogPathPolicy.java" 'instance' "instance 기준 File Log 경로 정책 누락"
foreach($column in @('TRANSACTION_ID','MODULE_ID','WAS_ID','SERVER_INSTANCE_ID')){
    RequireContains "cpf-tools/db/vendor/mariadb/source/10_cpf_schema.sql" $column "CPF DB Log 운영 추적 컬럼 누락: $column"
}
RequireContains "cpf-tools/db/vendor/mariadb/source/10_cpf_schema.sql" 'cpf_security_token_audit_log[\s\S]*TRANSACTION_ID CHAR\(34\)' "Security audit transactionId 34자리 표준 누락"
RequireContains "cpf-tools/db/vendor/mariadb/source/10_cpf_schema.sql" 'cpf_saga_execution[\s\S]*transaction_id CHAR\(34\)' "Saga transactionId 34자리 표준 누락"
RequireFile "cpf-tools/db/vendor/mariadb/source/migration/flyway/V50__cpf_transaction_id_width_standard.sql"
RequireFile "cpf-tools/scripts/generate-migration-checksums.ps1"
foreach($column in @('spring_batch_job_instance_id','worker_id','server_instance_id','transaction_id')){
    RequireContains "cpf-tools/db/vendor/mariadb/source/35_bat_schema.sql" $column "BAT 실행 추적 컬럼 누락: $column"
}

RequireContains "cpf-batch/src/main/java/com/cpf/batch/runtime/BatBatchJobLogPath.java" 'serverInstanceId' "BAT Job 파일로그 경로에 serverInstanceId 축 누락"
RequireContains "cpf-batch/src/main/java/com/cpf/batch/runtime/BatBatchJobLogPath.java" 'ServerInstanceIdentity\.current' "BAT Job 파일로그 legacy overload가 현재 serverInstance를 보존하지 않음"
RequireContains "cpf-tools/scripts/sync-database-artifacts.ps1" 'generate-migration-checksums\.ps1' "DB artifact sync가 migration checksum 자동 생성 단계를 호출하지 않음"

$finalTarget=Get-Content (Join-Path $Root "cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md") -Raw
foreach($keyword in @("Generated Domain artifact parity","통합 검증 계획","가짜 구현","한글 JavaDoc","영업일")){
  if($finalTarget -notmatch [regex]::Escape($keyword)){Fail "Final Target R10 정책 누락: $keyword"}
}
RequireContains "cpf-tools/generator/create-domain.ps1" 'CpfPageRequest' "Generator가 CPF PageRequest 표준을 사용하지 않음"
RequireContains "cpf-tools/generator/create-domain.ps1" 'CpfSlice' "Generator가 CPF Slice 표준을 사용하지 않음"
RequireNotContains "cpf-tools/generator/create-domain.ps1" '\$\{FeatureClassPrefix\}Slice\.java' "Generator가 Domain 전용 Slice DTO를 계속 생성함"
RequireContains "cpf-tools/scripts/sync-generated-domain-artifacts.ps1" "action = 'DELETE'" "Generator에서 제거된 파일의 안전 삭제 동기화 누락"
Write-Host "R10 product standard PASS."
