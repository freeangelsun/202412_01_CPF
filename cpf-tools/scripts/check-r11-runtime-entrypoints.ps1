[CmdletBinding()]
param([string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path)
$ErrorActionPreference='Stop'
$failures=[System.Collections.Generic.List[string]]::new()
function Fail([string]$m){$failures.Add($m)|Out-Null;Write-Host "[FAIL] $m" -ForegroundColor Red}
function Pass([string]$m){Write-Host "[PASS] $m" -ForegroundColor Green}
function Require([string]$rel){if(!(Test-Path (Join-Path $Root $rel))){Fail "Missing: $rel"}else{Pass $rel}}

# 역할별 독립 실행 Entry Point와 Control/Runtime 핵심 Surface
@(
 'cpf-batch/control-server/src/main/java/com/cpf/batch/control/BatchControlServerApplication.java',
 'cpf-batch/control-server/src/main/java/com/cpf/batch/control/RuntimeControlController.java',
 'cpf-batch/scheduler/src/main/java/com/cpf/batch/scheduler/BatchSchedulerApplication.java',
 'cpf-batch/scheduler/src/main/java/com/cpf/batch/scheduler/SchedulerCoordinator.java',
 'cpf-batch/scheduler/src/main/java/com/cpf/batch/scheduler/SchedulerDispatchService.java',
 'cpf-batch/worker/src/main/java/com/cpf/batch/worker/BatchWorkerApplication.java',
 'cpf-batch/worker/src/main/java/com/cpf/batch/worker/WorkerRuntime.java',
 'cpf-batch/worker/src/main/java/com/cpf/batch/worker/JobPackDispatcher.java',
 'cpf-batch/center-cut-runner/src/main/java/com/cpf/batch/centercut/runner/CenterCutRunnerApplication.java',
 'cpf-batch/center-cut-runner/src/main/java/com/cpf/batch/centercut/runner/CenterCutRuntime.java',
 'cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/BatchHostAgentApplication.java',
 'cpf-batch/runtime-common/src/main/java/com/cpf/batch/runtime/RuntimeCommonConfiguration.java',
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

$legacyBatchSource = Join-Path $Root 'cpf-batch/src'
if (Test-Path $legacyBatchSource) {
    $legacyFiles = @(Get-ChildItem -LiteralPath $legacyBatchSource -Recurse -File -ErrorAction SilentlyContinue)
    if ($legacyFiles.Count -gt 0) { Fail "Legacy executable cpf-batch/src still contains $($legacyFiles.Count) files" }
}

$entryPoints = @(
    [ordered]@{ path = 'cpf-batch/control-server/src/main/java/com/cpf/batch/control/BatchControlServerApplication.java'; class = 'BatchControlServerApplication' },
    [ordered]@{ path = 'cpf-batch/scheduler/src/main/java/com/cpf/batch/scheduler/BatchSchedulerApplication.java'; class = 'BatchSchedulerApplication' },
    [ordered]@{ path = 'cpf-batch/worker/src/main/java/com/cpf/batch/worker/BatchWorkerApplication.java'; class = 'BatchWorkerApplication' },
    [ordered]@{ path = 'cpf-batch/center-cut-runner/src/main/java/com/cpf/batch/centercut/runner/CenterCutRunnerApplication.java'; class = 'CenterCutRunnerApplication' },
    [ordered]@{ path = 'cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/BatchHostAgentApplication.java'; class = 'BatchHostAgentApplication' }
)
foreach ($entryPoint in $entryPoints) {
    $path = Join-Path $Root $entryPoint.path
    if (-not (Test-Path $path -PathType Leaf)) { continue }
    $text = Get-Content $path -Raw
    if ($text -notmatch ('SpringApplication\.run\(' + [regex]::Escape($entryPoint.class) + '\.class')) {
        Fail "BAT role SpringApplication entrypoint missing: $($entryPoint.path)"
    } else {
        Pass "BAT role entrypoint: $($entryPoint.class)"
    }
}
$runtimeConfiguration = Join-Path $Root 'cpf-batch/runtime-common/src/main/java/com/cpf/batch/runtime/RuntimeCommonConfiguration.java'
if (Test-Path $runtimeConfiguration -PathType Leaf) {
    $runtimeText = Get-Content $runtimeConfiguration -Raw
    if ($runtimeText -notmatch '@EnableScheduling') { Fail 'BAT shared runtime scheduling activation missing' }
    else { Pass 'BAT shared runtime scheduling enabled' }
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
