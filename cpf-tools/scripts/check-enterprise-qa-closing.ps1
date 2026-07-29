param([string]$Root=(Resolve-Path "$PSScriptRoot\..\..").Path)
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
$Root=(Resolve-Path -LiteralPath $Root).Path

function Require-File([string]$rel){
    $p=Join-Path $Root $rel
    if(-not(Test-Path -LiteralPath $p -PathType Leaf)){throw "required file missing: $rel"}
    return $p
}
function Require-Text([string]$rel,[string[]]$need,[string[]]$forbid=@()){
    $p=Require-File $rel
    $t=Get-Content -Raw -Encoding UTF8 -LiteralPath $p
    foreach($x in $need){if($t -notmatch [regex]::Escape($x)){throw "required marker missing: $rel :: $x"}}
    foreach($x in $forbid){if($t -match [regex]::Escape($x)){throw "forbidden marker remains: $rel :: $x"}}
}

# Official DB policy: exactly MariaDB/PostgreSQL/Oracle.
Require-Text 'cpf-core\src\main\java\com\cpf\core\api\database\CpfDatabaseVendor.java' @('MARIADB','POSTGRESQL','ORACLE') @('MYSQL','SQLSERVER')
Require-Text 'cpf-core\src\main\java\com\cpf\core\common\database\CpfSqlResourceResolver.java' @('mariadb','postgresql','oracle') @('"mysql"','"sqlserver"')
$schema=Get-Content -Raw -Encoding UTF8 -LiteralPath (Require-File 'cpf-tools\db\canonical\platform-schema.json') | ConvertFrom-Json -Depth 100
$vendors=@($schema.canonicalPolicy.officialVendors)
if(($vendors -join ',') -ne 'mariadb,postgresql,oracle'){throw "official DB vendor policy drift: $($vendors -join ',')"}
foreach($t in @(
 'cpf_runtime_version','cpf_runtime_instance_group','cpf_runtime_group_member','cpf_runtime_instance_state',
 'cpf_control_operation','cpf_runtime_change','cpf_runtime_delivery','cpf_runtime_change_audit',
 'cpf_cache_refresh_checkpoint','cpf_notification_delivery_attempt')){
    if(-not($schema.tables.name -contains $t)){throw "canonical table missing: $t"}
}

# Runtime Control Plane contracts/owner/agent.
foreach($rel in @(
 'cpf-core\src\main\java\com\cpf\core\api\runtimecontrol\CpfRuntimeControlPlane.java',
 'cpf-core\src\main\java\com\cpf\core\api\runtimecontrol\CpfRuntimeAgentPort.java',
 'cpf-core\src\main\java\com\cpf\core\common\runtimecontrol\CpfRuntimeControlPlaneRepository.java',
 'cpf-core\src\main\java\com\cpf\core\common\runtimecontrol\CpfRuntimeControlAgent.java',
 'cpf-core\src\main\java\com\cpf\core\common\runtimecontrol\CpfRuntimeHttpControlPlaneClient.java',
 'cpf-admin\src\main\java\com\cpf\admin\opr\controller\AdmRuntimeControlController.java')){Require-File $rel|Out-Null}
Require-Text 'cpf-core\src\main\java\com\cpf\core\common\runtimecontrol\CpfRuntimeControlPlaneRepository.java' @(
 'request_hash','fencing_token','desired_version','actual_version','cpf_runtime_delivery')
Require-Text 'cpf-admin\src\main\java\com\cpf\admin\opr\controller\AdmRuntimeControlController.java' @(
 'com.cpf.core.api.runtimecontrol','CpfRuntimeCapabilityCatalog','/adm/api/runtime-control/capabilities') @(
 'com.cpf.core.common.runtimecontrol')
foreach($rel in @(
 'cpf-core\src\main\java\com\cpf\core\api\runtimecontrol\CpfRuntimeFenceException.java',
 'cpf-core\src\main\java\com\cpf\core\api\runtimecontrol\CpfRuntimeRateLimitException.java',
 'cpf-core\src\main\java\com\cpf\core\api\runtimecontrol\CpfRuntimeVersionConflictException.java',
 'cpf-core\src\main\java\com\cpf\core\api\runtimecontrol\CpfRuntimeCapabilityCatalog.java')){Require-File $rel|Out-Null}

& pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'check-runtime-control-public-boundary.ps1') -Root $Root
if($LASTEXITCODE -ne 0){throw "runtime control public boundary gate failed: $LASTEXITCODE"}

# ADM Runtime Control 14 Capability는 Catalog뿐 아니라 실제 Consumer와 ADM surface가 있어야 한다.
& pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'check-runtime-capability-consumers.ps1') -Root $Root
if($LASTEXITCODE -ne 0){throw "runtime capability consumer gate failed: $LASTEXITCODE"}

# Notification trust boundary and official DB portability.
Require-Text 'cpf-admin\src\main\java\com\cpf\admin\opr\controller\AdmNotificationController.java' @(
 'com.cpf.core.api.execution.CpfOnlineTransaction','adm.operatorId','UNAUTHORIZED','FORBIDDEN') @(
 'com.cpf.core.common.','defaultValue = "ADM"')
Require-Text 'cpf-admin\src\main\java\com\cpf\admin\opr\service\AdmNotificationService.java' @(
 'com.cpf.core.api.error.CpfValidationException','com.cpf.core.api.logging.CpfTransactionContext',
 'GeneratedKeyHolder','new String[] {"rule_id"}','setMaxRows','notificationOutboxService.enqueueTest','findDeliveryAttempts') @(
 'com.cpf.core.common.','ON DUPLICATE KEY','LAST_INSERT_ID','LIMIT ?','CURRENT_TIMESTAMP(3)')

& pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'check-notification-portable-sql.ps1') -Root $Root
if($LASTEXITCODE -ne 0){throw "notification portable SQL and durable recovery gate failed: $LASTEXITCODE"}

# Local Web single-JVM and separate local Batch process.
foreach($rel in @(
 'cpf-tools\runtime\cpf-local-runtime\build.gradle',
 'cpf-tools\runtime\cpf-local-runtime\src\main\java\com\cpf\local\runtime\CpfLocalRuntimeApplication.java',
 'cpf-tools\runtime\cpf-local-batch-runtime\build.gradle',
 'cpf-tools\runtime\cpf-local-batch-runtime\src\main\java\com\cpf\local\batch\CpfLocalBatchRuntimeApplication.java',
 'cpf-tools\scripts\start-cpf-local.ps1','cpf-tools\scripts\stop-cpf-local.ps1')){Require-File $rel|Out-Null}

& pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'check-local-runtime-topology.ps1') -Root $Root
if($LASTEXITCODE -ne 0){throw "local runtime topology gate failed: $LASTEXITCODE"}

# Gateway trust boundary.
Require-Text 'cpf-gateway\src\main\java\com\cpf\gateway\controller\CpfGatewayController.java' @(
 'com.cpf.core.api.header.CpfHeaderNames','URI와 header의 표준 실행 ID가 일치하지 않습니다.') @(
 'com.cpf.core.common.header','validateHeader')
Require-Text 'cpf-gateway\src\main\java\com\cpf\gateway\service\CpfGatewayProxyService.java' @(
 'CpfGatewayAuthenticationPort','requestSignatureVerified','PASSTHROUGH','NEVER_FORWARD','targetUri','validateEndpointPath') @(
 'hasAuthentication','com.cpf.core.common.')
Require-Text 'cpf-gateway\src\main\java\com\cpf\gateway\route\CpfGatewayRouteSnapshot.java' @('cpf.gateway.allow-empty-routes')

# Registry routing/control.
Require-Text 'cpf-core\src\main\java\com\cpf\core\common\servicecall\CpfHealthAwareInstanceSelector.java' @(
 'priority','weight','preferredZone','preferredCell','rendezvousScore','maintenanceYn','drainYn')
Require-Text 'cpf-core\src\main\java\com\cpf\core\common\servicecall\CpfServiceRegistryRepository.java' @(
 'saveService','saveEndpoint','saveInstance','row_version','DRAINING') @(
 'DATABASE()','ON DUPLICATE KEY','LIMIT ?','CURRENT_TIMESTAMP(3)')
Require-File 'cpf-core\src\test\java\com\cpf\core\common\servicecall\CpfHealthAwareInstanceSelectorTest.java' | Out-Null

# Durable cache delivery + checkpoint replay.
Require-Text 'cpf-common\src\main\java\com\cpf\common\ref\service\CacheRefreshEventPublisher.java' @(
 'publishRequired','insertRequired','memoryRetryQueue') @(
 'drop oldest','ArrayBlockingQueue','LinkedBlockingQueue','ArrayDeque')
Require-Text 'cpf-common\src\main\java\com\cpf\common\ref\service\CacheRefreshEventListener.java' @(
 'findCheckpoint','refreshAll','findEventsAfter','updateCheckpoint')
foreach($rel in @(
 'cpf-common\src\test\java\com\cpf\common\ref\service\CacheRefreshEventPublisherTest.java',
 'cpf-common\src\test\java\com\cpf\common\ref\service\CacheRefreshEventListenerTest.java',
 'cpf-tools\db\runtime-template\cmn\mybatis\ref\CacheRefreshEventMapper.xml.template')){Require-File $rel|Out-Null}

# BZA login exact replay/fingerprint.
Require-Text 'cpf-biz-admin\src\main\java\com\cpf\bizadmin\auth\service\BzaAuthService.java' @(
 'requestHash','aesGcmEncrypt','aesGcmDecrypt') @('revokeRefreshTokensByLoginOperationId')
Require-Text 'cpf-biz-admin\src\main\java\com\cpf\bizadmin\auth\service\BzaLoginTransactionService.java' @(
 'requestHash','resultAccessTokenEnc','resultRefreshTokenEnc','resultExpiresAt') @(
 'revokeRefreshTokensByLoginOperationId')
Require-Text 'cpf-tools\db\metadata\platform-runtime-query-contract.json' @(
 'requestHash','resultAccessTokenEnc','resultRefreshTokenEnc')
Require-File 'cpf-biz-admin\src\test\java\com\cpf\bizadmin\auth\service\BzaLoginTransactionServiceTest.java' | Out-Null

# Text artifacts must not contain hidden control characters.
& pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'check-text-control-characters.ps1') -Root $Root
if($LASTEXITCODE -ne 0){throw "text control character gate failed: $LASTEXITCODE"}



# 기존 2,118건과 신규 QA 병합 원장의 Architecture·Generated Domain·UI·Hygiene를 검증합니다.
& pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'check-integrated-architecture-ui-hygiene.ps1') -Root $Root
if($LASTEXITCODE -ne 0){throw "integrated architecture/UI/hygiene gate failed: $LASTEXITCODE"}

# Migration history must be complete and tamper-evident.
& pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'check-migration-checksums.ps1') -Root $Root
if($LASTEXITCODE -ne 0){throw "migration checksum gate failed: $LASTEXITCODE"}

# Spring Batch framework-owned non-table objects are canonical artifacts, not
# ad-hoc schema additions.  Keep the official 6.x names in every vendor pack
# and reject stale BATCH_JOB_SEQ fresh-install regressions.
& pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'check-spring-batch-sequence-contract.ps1') -Root $Root
if($LASTEXITCODE -ne 0){throw "Spring Batch sequence contract gate failed: $LASTEXITCODE"}

Write-Host '[PASS] CPF enterprise QA closing static gate'
