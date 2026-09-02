param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $ResultDir = "",
    [string] $DatabaseProfilePath = "",
    [string] $DbVendor = $env:CPF_DB_VENDOR,
    [string] $DbResourceRoot = $env:CPF_DB_RESOURCE_ROOT,
    [int] $StartupTimeoutSeconds = 180,
    [switch] $BuildBeforeRun
)

# 이 파일명은 기존 자동화 호환을 위해 유지합니다. 검증 대상은 고정 업무 Domain이 아니라
# Platform Gateway OpenAPI와 BAT Control Server readiness 계약입니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
. (Join-Path $PSScriptRoot "runtime-common.ps1")

$Root = Get-CpfRuntimeRoot -Root $Root
$ResultDir = Get-CpfRuntimeResultDir -Root $Root -ResultDir $ResultDir
$runtimeDir = Join-Path $ResultDir "gateway-bat-services"
$resultPath = Join-Path $ResultDir "gateway-bat-runtime-result.sanitized.json"
New-Item -ItemType Directory -Force -Path $runtimeDir | Out-Null

$statusDone = Get-CpfRuntimeStatusText "Done"
$statusFailed = Get-CpfRuntimeStatusText "Failed"
$statusNotVerified = Get-CpfRuntimeStatusText "NotVerified"
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = $statusNotVerified
    contract = "platform-gateway-batch-readiness-v2"
    fixedBusinessDomainRequired = $false
    modules = @("GWY", "BAT")
    services = [ordered]@{}
    gatewayOpenApi = [ordered]@{ status = $statusNotVerified }
    batchReadiness = [ordered]@{ status = $statusNotVerified }
    cleanup = [ordered]@{ status = $statusNotVerified }
}

function Save-Result {
    $result.finishedAt = (Get-Date).ToString("o")
    Write-CpfRuntimeJson -Path $resultPath -Value $result
}

function Get-CpfOptionalRuntimeProperty {
    param([object] $Value,[string] $Name,[object] $Default = $null)
    if ($null -eq $Value) { return $Default }
    $property = $Value.PSObject.Properties[$Name]
    if ($null -eq $property -or $null -eq $property.Value) { return $Default }
    return $property.Value
}

# 신규 설치 상태에서는 GW_BINDING 에 행이 없다. Gateway Route 는 CPF 가 배포하는 seed 가 아니라
# 운영자가 등록하는 운영 데이터이며, product/sample seed 어디에도 행이 없다. Gateway 는 이런
# 상태를 위해 Default Deny 기동 모드를 제공한다(cpf.gateway.allow-empty-routes).
# 이 스테이지의 검증 대상은 Gateway OpenAPI 와 BAT Control Server readiness 이지 Route 서빙이
# 아니므로, 가짜 Route/ACK 를 만들어 넣지 않고 신규 설치와 같은 모드로 기동한다.
$env:CPF_GATEWAY_ALLOW_EMPTY_ROUTES = 'true'

# Runtime Agent 는 기동 시 자신의 serviceId/endpointCode 가 중앙 Registry(OPS_SERVICE /
# OPS_SERVICE_ENDPOINT)에 등록되어 있어야만 진행한다. 이 등록은 CPF 가 배포하는 seed 가 아니라
# 운영자가 만드는 운영 데이터이므로, Verifier 가 자기 시나리오의 전제를 직접 만든다.
# smoke-bat-two-worker-runtime.ps1 의 Generated Domain 사전 등록과 같은 방식이다.
function Invoke-CpfGatewaySql([string] $Sql) {
    $docker = (Get-Command docker -ErrorAction SilentlyContinue)
    if (-not $docker) { throw 'Docker CLI is required to register the Gateway runtime prerequisite.' }
    $arguments = @('exec', '-i', $script:GatewayDbContainer, 'mariadb',
        "-u$script:GatewayDbUser", "-p$script:GatewayDbPassword", '-N', '-B', '-e', $Sql)
    $out = & $docker.Source @arguments 2>&1
    if ($LASTEXITCODE -ne 0) { throw "Gateway prerequisite SQL failed: $($out -join ' ')" }
    return (($out -join "`n").Trim())
}

# DB 이름과 계정은 실행마다 새로 만들어지므로 어떤 값도 하드코딩하지 않는다. 이 스테이지가
# 이미 받는 Database Profile 에서 읽고, 비밀은 profile 이 가리키는 환경변수에서만 가져온다.
$script:GatewayDbContainer = if ($env:CPF_MARIADB_CONTAINER) { $env:CPF_MARIADB_CONTAINER } else { 'cpf-mariadb' }
# 이 스크립트는 Profile 을 파라미터로만 받는다. Runtime 기동 스크립트의 내부 변수에 의존하지
# 않도록 canonical loader 로 직접 읽는다.
. (Join-Path $Root 'cpf-tools/db/tools/database-profile-common.ps1')
$gatewayProfilePath = $DatabaseProfilePath
if ([string]::IsNullOrWhiteSpace($gatewayProfilePath)) {
    $gatewayProfilePath = Join-Path $Root 'cpf-tools\db\config\database-install.default.json'
} elseif (-not [System.IO.Path]::IsPathRooted($gatewayProfilePath)) {
    $gatewayProfilePath = Join-Path $Root $gatewayProfilePath
}
$gatewayDatabaseProfile = Get-CpfDatabaseProfile -Path ([System.IO.Path]::GetFullPath($gatewayProfilePath))
$gatewayCoreProfile = $gatewayDatabaseProfile.modules.core
if (-not $gatewayCoreProfile) { throw 'Database Profile 에 core module 이 없어 Gateway 전제를 등록할 수 없습니다.' }
$script:GatewayDbName = [string] $gatewayCoreProfile.databaseName
$script:GatewayDbUser = [string] $gatewayCoreProfile.runtime.username
$gatewayPasswordEnv = [string] $gatewayCoreProfile.runtime.password.env
if ([string]::IsNullOrWhiteSpace($gatewayPasswordEnv)) { throw 'Database Profile 의 runtime password 환경변수 참조가 비어 있습니다.' }
$script:GatewayDbPassword = [Environment]::GetEnvironmentVariable($gatewayPasswordEnv, 'Process')
if ([string]::IsNullOrWhiteSpace($script:GatewayDbPassword)) { throw "Gateway 전제 등록에 필요한 비밀이 없습니다: $gatewayPasswordEnv" }

$gatewayServiceId = 'GWY'
$gatewayEndpointCode = 'GWY_API'
Invoke-CpfGatewaySql "INSERT INTO $script:GatewayDbName.OPS_SERVICE(service_id,service_name,service_type,owner_module_code,description,use_yn,created_by,updated_by) VALUES('$gatewayServiceId','CPF Gateway','GATEWAY','GWY','Runtime qualification prerequisite seeded by the verifier','Y','SYSTEM','SYSTEM') ON DUPLICATE KEY UPDATE use_yn=VALUES(use_yn),updated_by=VALUES(updated_by);"
Invoke-CpfGatewaySql "INSERT INTO $script:GatewayDbName.OPS_SERVICE_ENDPOINT(endpoint_code,service_id,endpoint_name,endpoint_type,base_url,context_path,default_timeout_ms,default_retry_count,use_yn,created_by,updated_by) VALUES('$gatewayEndpointCode','$gatewayServiceId','CPF Gateway API','HTTP','http://127.0.0.1:8070','/',5000,0,'Y','SYSTEM','SYSTEM') ON DUPLICATE KEY UPDATE base_url=VALUES(base_url),use_yn=VALUES(use_yn),updated_by=VALUES(updated_by);"
$gatewayServiceRows = [int](Invoke-CpfGatewaySql "SELECT COUNT(*) FROM $script:GatewayDbName.OPS_SERVICE WHERE service_id='$gatewayServiceId' AND use_yn='Y';")
if ($gatewayServiceRows -ne 1) { throw "Gateway service registry prerequisite was not registered: $gatewayServiceId" }

try {
    $startArgs = @{
        Root = $Root
        Modules = @("GWY", "BAT")
        ResultDir = $runtimeDir
        StartupTimeoutSeconds = $StartupTimeoutSeconds
        NoExitOnFailure = $true
        BuildBeforeRun = [bool] $BuildBeforeRun
    }
    if (-not [string]::IsNullOrWhiteSpace($DatabaseProfilePath)) {
        $startArgs.DatabaseProfilePath = $DatabaseProfilePath
    }
    if (-not [string]::IsNullOrWhiteSpace($DbVendor)) {
        $startArgs.DbVendor = $DbVendor
    }
    if (-not [string]::IsNullOrWhiteSpace($DbResourceRoot)) {
        $startArgs.DbResourceRoot = $DbResourceRoot
    }

    & (Join-Path $PSScriptRoot "runtime-start-services.ps1") @startArgs
    $startResultPath = Join-Path $runtimeDir "runtime-start-services-result.json"
    if (-not (Test-Path -LiteralPath $startResultPath -PathType Leaf)) {
        throw "Runtime start 결과가 생성되지 않았습니다: $startResultPath"
    }
    $startResult = Get-Content -LiteralPath $startResultPath -Raw -Encoding UTF8 | ConvertFrom-Json
    $result.services = [ordered]@{
        status = [string] $startResult.status
        modules = @($startResult.modules | ForEach-Object {
                [ordered]@{
                    module = [string] (Get-CpfOptionalRuntimeProperty $_ "module" "UNKNOWN")
                    status = [string] (Get-CpfOptionalRuntimeProperty $_ "status" $statusNotVerified)
                    port = [int] (Get-CpfOptionalRuntimeProperty $_ "port" 0)
                    healthCheckPassed = [bool] (Get-CpfOptionalRuntimeProperty $_ "healthCheckPassed" $false)
                    processStillAliveAfterProbe = [bool] (Get-CpfOptionalRuntimeProperty $_ "processStillAliveAfterProbe" $false)
                    finalRuntimeUsable = [bool] (Get-CpfOptionalRuntimeProperty $_ "finalRuntimeUsable" $false)
                    failureClassification = [string] (Get-CpfOptionalRuntimeProperty $_ "failureClassification" "UNKNOWN")
                    failureRootCause = [string] (Get-CpfOptionalRuntimeProperty $_ "failureRootCause" "")
                    bootJarBuildStatus = [string] (Get-CpfOptionalRuntimeProperty $_ "bootJarBuildStatus" $statusNotVerified)
                    stdout = [string] (Get-CpfOptionalRuntimeProperty $_ "stdout" "")
                    stderr = [string] (Get-CpfOptionalRuntimeProperty $_ "stderr" "")
                    stdoutTail = [string] (Get-CpfOptionalRuntimeProperty $_ "stdoutTail" "")
                    stderrTail = [string] (Get-CpfOptionalRuntimeProperty $_ "stderrTail" "")
                }
            })
    }
    $unusable = @($startResult.modules | Where-Object { [bool] (Get-CpfOptionalRuntimeProperty $_ "finalRuntimeUsable" $false) -ne $true })
    if ($unusable.Count -gt 0) {
        $failureDetails = @($unusable | ForEach-Object {
                $moduleName = [string] (Get-CpfOptionalRuntimeProperty $_ "module" "UNKNOWN")
                $classification = [string] (Get-CpfOptionalRuntimeProperty $_ "failureClassification" "UNKNOWN")
                $rootCause = [string] (Get-CpfOptionalRuntimeProperty $_ "failureRootCause" "")
                $stderrTail = ([string] (Get-CpfOptionalRuntimeProperty $_ "stderrTail" "") -replace "`r`n", ' ' -replace "`n", ' ').Trim()
                if ($stderrTail.Length -gt 600) { $stderrTail = $stderrTail.Substring($stderrTail.Length - 600) }
                "$moduleName classification=$classification rootCause=$rootCause stderrTail=$stderrTail"
            })
        throw ("Gateway/BAT runtime readiness failed: " + ($failureDetails -join ' || '))
    }

    $batchResult = $startResult.modules | Where-Object { $_.module -eq "BAT" } | Select-Object -First 1
    if ($null -eq $batchResult -or -not [bool] (Get-CpfOptionalRuntimeProperty $batchResult "healthCheckPassed" $false)) {
        throw "BAT Control Server readiness 검증 결과가 없습니다."
    }
    $result.batchReadiness = [ordered]@{
        status = $statusDone
        port = [int] (Get-CpfOptionalRuntimeProperty $batchResult "port" 0)
        health = Get-CpfOptionalRuntimeProperty $batchResult "health" $null
    }

    & (Join-Path $PSScriptRoot "..\..\verification\openapi\smoke-openapi.ps1") `
        -Root $Root `
        -Modules @("GWY") `
        -ResultDir $runtimeDir `
        -RequireRuntime
    if ($LASTEXITCODE -ne 0) {
        throw "Gateway OpenAPI runtime 검증이 실패했습니다."
    }
    $openApiResultPath = Join-Path $runtimeDir "openapi-runtime-result.sanitized.json"
    $openApiResult = Get-Content -LiteralPath $openApiResultPath -Raw -Encoding UTF8 | ConvertFrom-Json
    $result.gatewayOpenApi = [ordered]@{
        status = $(if ([string] $openApiResult.status -eq "PASSED") { $statusDone } else { $statusFailed })
        result = Get-CpfRelativePath -Root $Root -Path $openApiResultPath
    }
    if ($result.gatewayOpenApi.status -ne $statusDone) {
        throw "Gateway OpenAPI contract가 통과하지 못했습니다."
    }

    $result.status = $statusDone
} catch {
    $result.status = $statusFailed
    $result.errorType = $_.Exception.GetType().Name
    $result.error = $_.Exception.Message
    throw
} finally {
    try {
        & (Join-Path $PSScriptRoot "runtime-stop-services.ps1") `
            -Root $Root `
            -Modules @("GWY", "BAT") `
            -ResultDir $runtimeDir
        $stopResultPath = Join-Path $runtimeDir "runtime-stop-services-result.json"
        $stopResult = Get-Content -LiteralPath $stopResultPath -Raw -Encoding UTF8 | ConvertFrom-Json
        $result.cleanup = [ordered]@{
            status = [string] $stopResult.status
            modules = @($stopResult.modules | Select-Object module, status)
        }
    } catch {
        $result.cleanup = [ordered]@{
            status = $statusFailed
            error = $_.Exception.Message
        }
        if ($result.status -eq $statusDone) {
            $result.status = $statusFailed
        }
    }
    Save-Result
}

if ($result.status -ne $statusDone) {
    throw "Gateway/BAT platform runtime smoke가 완료 상태가 아닙니다. result=$resultPath"
}
Write-Host "Gateway/BAT platform runtime smoke completed. status=$($result.status) result=$resultPath"
