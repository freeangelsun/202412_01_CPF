param([string]$Root=(Resolve-Path "$PSScriptRoot\..\..").Path)
$ErrorActionPreference='Stop'; Set-StrictMode -Version Latest
$Root=(Resolve-Path -LiteralPath $Root).Path
function Require-File([string]$rel){$p=Join-Path $Root $rel;if(-not(Test-Path -LiteralPath $p -PathType Leaf)){throw "required file missing: $rel"};return $p}
function Require-Text([string]$rel,[string[]]$need,[string[]]$forbid=@()){$p=Require-File $rel;$t=Get-Content -Raw -Encoding UTF8 -LiteralPath $p;foreach($x in $need){if($t -notmatch [regex]::Escape($x)){throw "required marker missing: $rel :: $x"}};foreach($x in $forbid){if($t -match [regex]::Escape($x)){throw "forbidden marker remains: $rel :: $x"}}}

# Official DB policy: exactly MariaDB/PostgreSQL/Oracle.
Require-Text 'cpf-core\src\main\java\com\cpf\core\api\database\CpfDatabaseVendor.java' @('MARIADB','POSTGRESQL','ORACLE') @('MYSQL','SQLSERVER')
Require-Text 'cpf-core\src\main\java\com\cpf\core\common\database\CpfSqlResourceResolver.java' @('mariadb','postgresql','oracle') @('"mysql"','"sqlserver"')
$schema=Get-Content -Raw -Encoding UTF8 -LiteralPath (Require-File 'cpf-tools\db\canonical\platform-schema.json') | ConvertFrom-Json
$vendors=@($schema.canonicalPolicy.officialVendors)
if(($vendors -join ',') -ne 'mariadb,postgresql,oracle'){throw "official DB vendor policy drift: $($vendors -join ',')"}
foreach($t in @('cpf_runtime_version','cpf_runtime_instance_group','cpf_runtime_group_member','cpf_runtime_instance_state','cpf_control_operation','cpf_runtime_change','cpf_runtime_delivery','cpf_runtime_change_audit','cpf_cache_refresh_checkpoint')){if(-not($schema.tables.name -contains $t)){throw "canonical table missing: $t"}}

# Runtime Control Plane contracts/owner/agent.
foreach($rel in @(
 'cpf-core\src\main\java\com\cpf\core\api\runtimecontrol\CpfRuntimeControlPlane.java',
 'cpf-core\src\main\java\com\cpf\core\api\runtimecontrol\CpfRuntimeAgentPort.java',
 'cpf-core\src\main\java\com\cpf\core\common\runtimecontrol\CpfRuntimeControlPlaneRepository.java',
 'cpf-core\src\main\java\com\cpf\core\common\runtimecontrol\CpfRuntimeControlAgent.java',
 'cpf-core\src\main\java\com\cpf\core\common\runtimecontrol\CpfRuntimeHttpControlPlaneClient.java',
 'cpf-admin\src\main\java\com\cpf\admin\opr\controller\AdmRuntimeControlController.java')){Require-File $rel|Out-Null}
Require-Text 'cpf-core\src\main\java\com\cpf\core\common\runtimecontrol\CpfRuntimeControlPlaneRepository.java' @('request_hash','fencing_token','desired_version','actual_version','cpf_runtime_delivery')

# Gateway trust boundary.
Require-Text 'cpf-gateway\src\main\java\com\cpf\gateway\controller\CpfGatewayController.java' @('com.cpf.core.api.header.CpfHeaderNames','URI와 header의 표준 실행 ID가 일치하지 않습니다.') @('com.cpf.core.common.header','validateHeader')
Require-Text 'cpf-gateway\src\main\java\com\cpf\gateway\service\CpfGatewayProxyService.java' @('CpfGatewayAuthenticationPort','auditReasonRequired','NEVER_FORWARD','targetUri') @('hasAuthentication')
Require-Text 'cpf-gateway\src\main\java\com\cpf\gateway\route\CpfGatewayRouteSnapshot.java' @('cpf.gateway.allow-empty-routes')

# Registry routing/control.
Require-Text 'cpf-core\src\main\java\com\cpf\core\common\servicecall\CpfHealthAwareInstanceSelector.java' @('priority','weight','maintenance','drain')
Require-Text 'cpf-core\src\main\java\com\cpf\core\common\servicecall\CpfServiceRegistryRepository.java' @('saveService','saveEndpoint','saveInstance','row_version','DRAINING') @('DATABASE()','ON DUPLICATE KEY','LIMIT ?','CURRENT_TIMESTAMP(3)')

# Durable cache delivery + checkpoint replay.
Require-Text 'cpf-common\src\main\java\com\cpf\common\ref\service\CacheRefreshEventPublisher.java' @('publishRequired') @('drop oldest','ArrayBlockingQueue','LinkedBlockingQueue')
Require-Text 'cpf-common\src\main\java\com\cpf\common\ref\service\CacheRefreshEventListener.java' @('checkpoint','findEventsAfter')
Require-File 'cpf-tools\db\runtime-template\cmn\mybatis\ref\CacheRefreshEventMapper.xml.template'|Out-Null

# BZA login exact replay / fingerprint.
Require-Text 'cpf-biz-admin\src\main\java\com\cpf\bizadmin\auth\service\BzaAuthService.java' @('requestHash','aesGcmEncrypt','aesGcmDecrypt') @('revokeRefreshTokensByLoginOperationId')
Require-Text 'cpf-biz-admin\src\main\java\com\cpf\bizadmin\auth\service\BzaLoginTransactionService.java' @('requestHash','encryptedAccessToken','encryptedRefreshToken') @('revokeRefreshTokensByLoginOperationId')
Require-Text 'cpf-tools\db\metadata\platform-runtime-query-contract.json' @('requestHash','encryptedAccessToken','encryptedRefreshToken')

# Official DB lifecycle artifacts added for this change set.
foreach($rel in @(
 'cpf-tools\db\vendor\mariadb\migration\flyway\V64__runtime_control_plane.sql','cpf-tools\db\vendor\mariadb\migration\flyway\V65__bza_login_idempotent_result.sql',
 'cpf-tools\db\vendor\mariadb\rollback\R64__runtime_control_plane.sql','cpf-tools\db\vendor\mariadb\rollback\R65__bza_login_idempotent_result.sql',
 'cpf-tools\db\vendor\postgresql\migration\flyway\cpfDB\V64__runtime_control_plane.sql','cpf-tools\db\vendor\postgresql\migration\flyway\bzaDB\V64__bza_login_idempotent_result.sql',
 'cpf-tools\db\vendor\oracle\migration\flyway\cpfDB\V64__runtime_control_plane.sql','cpf-tools\db\vendor\oracle\migration\flyway\bzaDB\V64__bza_login_idempotent_result.sql')){Require-File $rel|Out-Null}
Write-Host '[PASS] CPF 20260728 enterprise QA closing static gate'
