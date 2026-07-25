[CmdletBinding()]
param([string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path)
$ErrorActionPreference='Stop'
$failures=[System.Collections.Generic.List[string]]::new()
function Fail([string]$m){$failures.Add($m)|Out-Null;Write-Host "[FAIL] $m" -ForegroundColor Red}
function Pass([string]$m){Write-Host "[PASS] $m" -ForegroundColor Green}
function Require([string]$rel){if(!(Test-Path (Join-Path $Root $rel))){Fail "Missing: $rel"}else{Pass $rel}}

# 독립 실행 Entry Point와 Control/Runtime 핵심 Surface
@(
 'cpf-batch/src/main/java/com/cpf/batch/BatApplication.java',
 'cpf-batch/src/main/java/com/cpf/batch/runtime/centercut/BatCenterCutRunner.java',
 'cpf-batch/src/main/java/com/cpf/batch/runtime/centercut/BatCenterCutService.java',
 'cpf-batch/src/main/java/com/cpf/batch/runtime/centercut/BatRemoteCenterCutHandler.java',
 'cpf-batch/src/main/java/com/cpf/batch/runtime/centercut/BatHttpCenterCutRemoteTransport.java',
 'cpf-batch/src/main/java/com/cpf/batch/scheduler/BatBatchScheduler.java',
 'cpf-batch/src/main/java/com/cpf/batch/scheduler/BatBatchScheduleService.java',
 'cpf-batch/src/main/java/com/cpf/batch/scheduler/BatBatchExecutionTargetService.java',
 'cpf-batch/src/main/java/com/cpf/batch/operation/BatOperationFacade.java',
 'cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmBatchController.java',
 'cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmCenterCutController.java',
 'cpf-gateway/src/main/java/com/cpf/gateway/CpfGatewayApplication.java',
 'cpf-gateway/src/main/java/com/cpf/gateway/service/CpfGatewayProxyService.java'
) | ForEach-Object { Require $_ }


$admLegacyBat = @(
 'cpf-admin/src/main/java/com/cpf/admin/config/AdmBatchRepositoryConfig.java',
 'cpf-admin/src/main/java/com/cpf/admin/opr/service/CpfBatchScheduleService.java',
 'cpf-admin/src/main/java/com/cpf/admin/opr/service/CpfBatchExecutionTargetService.java',
 'cpf-admin/src/main/java/com/cpf/admin/opr/service/CpfBatchScheduler.java'
) | Where-Object { Test-Path (Join-Path $Root $_) }
if($admLegacyBat){ Fail "ADM still contains BAT runtime ownership: $($admLegacyBat -join ', ')" }
else { Pass 'BAT scheduler/runtime ownership is isolated in cpf-batch' }

$batApp=Join-Path $Root 'cpf-batch/src/main/java/com/cpf/batch/BatApplication.java'
if(Test-Path $batApp){
 $txt=Get-Content $batApp -Raw
 if($txt -notmatch 'SpringApplication\.run\(BatApplication\.class'){Fail 'BAT standalone SpringApplication entrypoint missing'} else {Pass 'BAT standalone entrypoint'}
 if($txt -notmatch '@EnableScheduling'){Fail 'BAT scheduler activation missing'} else {Pass 'BAT scheduling enabled'}
}

$gateway=Join-Path $Root 'cpf-gateway/src/main/java/com/cpf/gateway/service/CpfGatewayProxyService.java'
if(Test-Path $gateway){
 $txt=Get-Content $gateway -Raw
 foreach($pattern in @('CpfGatewayRouteSnapshot','authorization','hop','X-Cpf')){
   if($txt -notmatch [regex]::Escape($pattern)){ Write-Host "[INFO] Gateway source does not contain literal '$pattern'; manual/static review may be required." -ForegroundColor Yellow }
 }
 # Gateway가 업무 Domain 내부 endpoint를 무차별 생성하는 구조인지 최소 Gate
 if($txt -match 'com\.cpf\.(member|account|reference)\.'){Fail 'Gateway directly depends on a business Domain implementation'}
 else {Pass 'Gateway has no obvious direct business-domain implementation dependency'}
}

if($failures.Count){throw "CPF R11 runtime-entrypoint gate failed ($($failures.Count))."}
Pass 'CPF R11 runtime-entrypoint gate completed'
