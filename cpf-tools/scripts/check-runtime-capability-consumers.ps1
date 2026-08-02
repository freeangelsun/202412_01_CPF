param([string]$Root=(Resolve-Path "$PSScriptRoot\..\..").Path)
$ErrorActionPreference='Stop'; Set-StrictMode -Version Latest
$Root=(Resolve-Path -LiteralPath $Root).Path
function Require-File([string]$rel){$p=Join-Path $Root $rel;if(-not(Test-Path -LiteralPath $p -PathType Leaf)){throw "required file missing: $rel"};$p}
function Require-Dir([string]$rel){$p=Join-Path $Root $rel;if(-not(Test-Path -LiteralPath $p -PathType Container)){throw "required directory missing: $rel"};$p}
function Require-Markers([string]$rel,[string[]]$markers){$p=Require-File $rel;$t=Get-Content -Raw -Encoding UTF8 $p;foreach($m in $markers){if($t -notmatch [regex]::Escape($m)){throw "capability marker missing: $rel :: $m"}}}

# 정본 Catalog와 Control Plane lifecycle
Require-Markers 'cpf-core\src\main\java\com\cpf\core\api\runtimecontrol\CpfRuntimeCapabilityCatalog.java' @(
 'COMMON_CODE','MESSAGE_RESPONSE_CODE','CONFIG_PARAMETER_FEATURE_FLAG','CACHE','ONLINE_TRANSACTION','SERVICE_CALL','GATEWAY','LOG_TRACE','BATCH_RUNTIME','SECURITY_RUNTIME_POLICY','EXTERNAL_INTEGRATION','INSTANCE_MANAGEMENT','INSTANCE_LOG_DOWNLOAD','OBSERVABILITY_NOTIFICATION')
Require-Markers 'cpf-core\src\main\java\com\cpf\core\common\runtimecontrol\CpfRuntimeControlPlaneService.java' @('expectedVersion','requestHash','preview','rollback')
Require-Markers 'cpf-core\src\main\java\com\cpf\core\common\runtimecontrol\CpfRuntimeControlPlaneRepository.java' @(
 'scheduleRetryOrPoison','next_attempt_at','POISONED')
Require-Markers 'cpf-core\src\main\java\com\cpf\core\common\runtimecontrol\CpfRuntimeControlAgent.java' @('ack','actualVersion','fencing')
Require-Markers 'cpf-core\src\main\java\com\cpf\core\common\runtimecontrol\CpfRuntimeControlReconciler.java' @('reconcile')
Require-Dir 'cpf-admin\frontend\src\features\runtime-control'|Out-Null
Require-Dir 'cpf-admin\frontend\src\features\approvals'|Out-Null
Require-Dir 'cpf-admin\frontend\src\features\break-glass'|Out-Null
Require-Dir 'cpf-admin\frontend\src\features\audit-logs'|Out-Null

# 1~4 CMN 실제 Cache Consumer
Require-Markers 'cpf-common\src\main\java\com\cpf\common\config\CmnRuntimeControlAutoConfiguration.java' @(
 'cmnCommonCodeRuntimeApplier','cmnMessageRuntimeApplier','cmnResponseCodeRuntimeApplier','cmnRuntimeConfigApplier','CmnCacheRuntimeApplier')
foreach($d in @('codes','messages','response-codes','configs','cache')){Require-Dir "cpf-admin\frontend\src\features\$d"|Out-Null}
Require-Markers 'cpf-common\src\main\java\com\cpf\common\ref\service\CacheRefreshEventPublisher.java' @('publishRequired','durable','memoryRetryQueue')
Require-Markers 'cpf-common\src\main\java\com\cpf\common\ref\service\CacheRefreshEventListener.java' @('findCheckpoint','findEventsAfter','updateCheckpoint','refreshAll')

# 5~8 거래/Service/Gateway/로그 실제 Runtime Consumer
Require-Markers 'cpf-core\src\main\java\com\cpf\core\config\CpfRuntimeControlAutoConfiguration.java' @(
 'cpfServiceRouteRuntimeApplier','cpfCircuitRuntimeApplier','cpfMaintenanceRuntimeApplier','cpfChannelPolicyRuntimeApplier','cpfTraceSamplingRuntimeApplier','cpfDynamicLogLevelRuntimeApplier','cpfMaskingPolicyRuntimeApplier')
Require-File 'cpf-gateway\src\main\java\com\cpf\gateway\runtime\CpfGatewayRuntimeApplier.java'|Out-Null
Require-File 'cpf-gateway\src\main\java\com\cpf\gateway\runtime\CpfGatewayRouteRuntimeApplier.java'|Out-Null
foreach($d in @('transactions','transaction-groups','service-registry','channel-policy','log-level','log-policies','logs')){Require-Dir "cpf-admin\frontend\src\features\$d"|Out-Null}

# 9 Batch Runtime은 BAT Owner와 실제 ADM 화면/API를 모두 요구
foreach($rel in @(
 'cpf-batch\scheduler\src\main\java\com\cpf\batch\scheduler\BatchSchedulerApplication.java',
 'cpf-batch\worker\src\main\java\com\cpf\batch\worker\BatchWorkerApplication.java',
 'cpf-batch\center-cut-runner\src\main\java\com\cpf\batch\centercut\runner\CenterCutRunnerApplication.java',
 'cpf-batch\host-agent\src\main\java\com\cpf\batch\agent\BatchHostAgentApplication.java')){Require-File $rel|Out-Null}
foreach($d in @('batch-runtime-control','batch-scheduler','batch-worker-pools','batch-center-cut','batch-agents','batch-recovery','batch-leases')){Require-Dir "cpf-admin\frontend\src\features\$d"|Out-Null}

# 10 Security
Require-Markers 'cpf-core\src\main\java\com\cpf\core\config\CpfRuntimeControlAutoConfiguration.java' @(
 'cpfPasswordPolicyRuntimeApplier','cpfCertificateRuntimeApplier','cpfSecretReferenceRuntimeApplier','cpfJwtKeyRuntimeApplier','cpfEncryptionKeyRuntimeApplier')
foreach($d in @('password','permissions','secrets','security')){Require-Dir "cpf-admin\frontend\src\features\$d"|Out-Null}

# 11 외부연계/Messaging/File
Require-Markers 'cpf-core\src\main\java\com\cpf\core\config\CpfRuntimeControlAutoConfiguration.java' @(
 'cpfBrokerConsumerRuntimeApplier','cpfBrokerRetryDlqRuntimeApplier','cpfFilePolicyRuntimeApplier','cpfSftpTransferRuntimeApplier','cpfWebhookCallbackRuntimeApplier','cpfExternalInstitutionRuntimeApplier')
Require-File 'cpf-tools\contracts\external-provider-capability-matrix.json'|Out-Null

# 12 Instance 정식 관리 객체
foreach($table in @('cpf_runtime_instance_group','cpf_runtime_group_member','cpf_runtime_instance_state')){
 $schema=Get-Content -Raw -Encoding UTF8 (Require-File 'cpf-tools\db\canonical\platform-schema.json')|ConvertFrom-Json -Depth 100
 if($schema.tables.name -notcontains $table){throw "instance canonical table missing: $table"}
}
foreach($d in @('topology','service-registry','batch-instances')){Require-Dir "cpf-admin\frontend\src\features\$d"|Out-Null}

# 13 Log download는 Agent/API/ADM UI 동시 연결
Require-File 'cpf-admin\src\main\java\com\cpf\admin\opr\service\AdmDownloadService.java'|Out-Null
Require-Dir 'cpf-admin\frontend\src\features\remote-logs'|Out-Null
Require-Dir 'cpf-admin\frontend\src\features\downloads'|Out-Null
Require-Markers 'cpf-batch\host-agent\src\main\java\com\cpf\batch\agent\AgentController.java' @(
 '@GetMapping("/services/{id}/logs")','StreamingResponseBody','Files.newInputStream',
 'LinkOption.NOFOLLOW_LINKS','logs.delete(archive)','COLLECT_LOGS')

# 14 Notification Outbox/Worker/Provider
foreach($rel in @(
 'cpf-admin\src\main\java\com\cpf\admin\opr\service\AdmNotificationOutboxService.java',
 'cpf-admin\src\main\java\com\cpf\admin\opr\service\AdmNotificationOutboxWorker.java',
 'cpf-admin\src\main\java\com\cpf\admin\opr\service\NotificationSender.java',
 'cpf-admin\src\main\java\com\cpf\admin\opr\service\MockNotificationSender.java')){Require-File $rel|Out-Null}
Require-Markers 'cpf-admin\src\main\java\com\cpf\admin\opr\service\AdmNotificationOutboxService.java' @(
 'operation_id','request_hash','lease_owner','lease_until','version','UNKNOWN_RESULT','RETRY','CANCELLED')
Require-Dir 'cpf-admin\frontend\src\features\notifications'|Out-Null

Write-Host '[PASS] 14 ADM runtime capabilities have source consumer and ADM surface gates.'
