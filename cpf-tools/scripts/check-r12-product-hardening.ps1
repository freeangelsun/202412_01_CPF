param([string]$Root=(Resolve-Path "$PSScriptRoot\..\..").Path)
$ErrorActionPreference='Stop';Set-StrictMode -Version Latest
function Read([string]$Rel){$p=Join-Path $Root $Rel;if(-not(Test-Path $p)){throw "missing: $Rel"};Get-Content -LiteralPath $p -Raw}
function Must([string]$Name,[bool]$Condition){if(-not $Condition){throw "[FAIL] $Name"};Write-Host "[PASS] $Name"}
$a=Read 'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmAuditDeliveryService.java';Must 'durable audit reservation' ($a.Contains('adm_audit_delivery') -and $a.Contains('PROPAGATION_REQUIRES_NEW'))
$b=Read 'cpf-batch/control-server/src/main/java/com/cpf/batch/control/compat/BatchOperationsCompatibilityService.java'
Must 'BAT query failure not empty' (-not$b.Contains('queryOrEmpty') -and $b.Contains('BAT resource not found'))
Must 'Ghost broad unlock removed' (-not [regex]::IsMatch($b,'DELETE FROM bat_lock[^"\r\n]*OR\s+\?\s+IS\s+NULL'))
Must 'Ghost BAT transaction' ($b.Contains('new TransactionTemplate') -and $b.Contains('return tx.execute'))
$bc=Read 'cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmBatchController.java';Must 'ADM Batch actor fail-closed' (-not $bc.Contains('request.requestUser()'))
$actor=Read 'cpf-admin/src/main/java/com/cpf/admin/opr/audit/AdmVerifiedActorRequestBodyAdvice.java';Must 'ADM legacy actor spoof guard' ($actor.Contains('requestUser') -and $actor.Contains('adm.operatorId'))
foreach($d in 'AdmBatchOperationRequest.java','AdmBatchJobRegisterRequest.java','AdmBatchLockReleaseRequest.java','AdmBatchGhostActionRequest.java'){Must "Batch DTO no requestUser $d" (-not(Read ("cpf-admin/src/main/java/com/cpf/admin/opr/dto/"+$d)).Contains('requestUser'))}
$g=Read 'cpf-tools/generator/create-domain.ps1';Must 'Generator cpf-common dependency' ($g.Contains("implementation project(':cpf-common')"));Must 'Generator no core.common' (-not$g.Contains('com.cpf.core.common.'));Must 'Generator memory production blocked' ($g.Contains('@Profile("!prod & !production & !stage & !staging & (local | test | edu)")') -and $g.Contains('if (-not $DatabaseEnabled -and -not $ExternalEnabled)'))
$launch=Read 'cpf-tools/scripts/create-domain.ps1';Must 'Single canonical generator' ($launch.Contains('generator/create-domain.ps1'))
$m=Read 'cpf-tools/scripts/verify-full-product.ps1';Must 'Full verify read-only' (-not$m.Contains("'cpf-tools/scripts/sync-database-artifacts.ps1'") -and -not$m.Contains("'cpf-tools/scripts/sync-generated-domain-artifacts.ps1'"));Must 'Full verify immutable checksum gate' ($m.Contains('check-migration-checksums.ps1'))
$mixPath=Join-Path $Root 'cpf-admin/frontend/src/app/admConsoleMixin.ts'
$mixClean=(-not(Test-Path -LiteralPath $mixPath))
if(-not$mixClean){$mix=Get-Content -LiteralPath $mixPath -Raw;$mixClean=-not($mix-match 'features/(access|observability|platform|reference)/methods')}
Must 'Deleted frontend feature imports absent' $mixClean
$cal=Read 'cpf-common/src/main/java/com/cpf/common/calendar/CmnJdbcCalendarStore.java';Must 'Calendar create race protected' ($cal.Contains('DuplicateKeyException') -and $cal.Contains('VERSION_CONFLICT'));Must 'Calendar actor aware' ($cal.Contains('created_by,updated_by'))
$old=Join-Path $Root 'cpf-tools/db/vendor/mariadb/migration/flyway/V6__bizadm_exs_transaction_identity.sql';Must 'Obsolete duplicate V6 removed' (-not(Test-Path $old))
Must 'Legacy cpf-tools/db/source absent' (-not(Test-Path(Join-Path $Root 'cpf-tools/db/source')))
Write-Host '[PASS] R12 product hardening gate'
