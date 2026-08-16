param(
    [string]$DockerRoot = "C:\dev\Docker",
    [string]$RepoRoot = "C:\dev\projects\jck\202412_01_CPF",
    [string]$SourceIdentity = "",
    [switch]$IncludeIbmMq
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Invoke-Docker {
    param([Parameter(Mandatory)][string[]]$Arguments)
    & docker @Arguments
    if ($LASTEXITCODE -ne 0) { throw "docker $($Arguments -join ' ') 실패(exit=$LASTEXITCODE)" }
}

function Get-ContainerState {
    param([Parameter(Mandatory)][string]$Name)
    $raw = docker inspect $Name
    if ($LASTEXITCODE -ne 0) { throw "Container inspect 실패: $Name" }
    return ($raw | ConvertFrom-Json)[0]
}

function Wait-ContainerRunning {
    param([Parameter(Mandatory)][string]$Name, [int]$TimeoutSeconds = 240)
    Write-Host "[기동 대기] $Name" -ForegroundColor Cyan
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        $state = Get-ContainerState -Name $Name
        if ([bool]$state.State.Running) { Write-Host "[실행 확인] $Name" -ForegroundColor Green; return }
        if ([string]$state.State.Status -eq "exited") {
            docker logs --tail 120 $Name | Out-Host
            throw "Container가 종료됐습니다: $Name exit=$($state.State.ExitCode)"
        }
        Start-Sleep -Seconds 2
    } while ((Get-Date) -lt $deadline)
    docker logs --tail 120 $Name | Out-Host
    throw "Container 실행 Timeout: $Name"
}

function Test-TcpOnce {
    param([string]$HostName, [int]$Port, [int]$TimeoutMilliseconds = 1500)
    $client = [Net.Sockets.TcpClient]::new()
    try {
        $task = $client.ConnectAsync($HostName, $Port)
        if (-not $task.Wait($TimeoutMilliseconds)) { return $false }
        return $client.Connected
    } catch { return $false } finally { $client.Dispose() }
}

function Wait-Tcp {
    param([string]$Name, [string]$HostName, [int]$Port, [int]$TimeoutSeconds = 240)
    Write-Host "[TCP 대기] $Name $HostName`:$Port" -ForegroundColor Cyan
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        if (Test-TcpOnce -HostName $HostName -Port $Port) { Write-Host "[TCP 확인] $Name" -ForegroundColor Green; return }
        Start-Sleep -Seconds 3
    } while ((Get-Date) -lt $deadline)
    throw "TCP 준비 Timeout: $Name $HostName`:$Port"
}

function Wait-Http {
    param([string]$Name, [string]$Uri, [int]$TimeoutSeconds = 240)
    Write-Host "[HTTP 대기] $Name $Uri" -ForegroundColor Cyan
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        try {
            $response = Invoke-WebRequest -UseBasicParsing -Uri $Uri -TimeoutSec 5
            if ([int]$response.StatusCode -ge 200 -and [int]$response.StatusCode -lt 400) { Write-Host "[HTTP 확인] $Name status=$($response.StatusCode)" -ForegroundColor Green; return }
        } catch { }
        Start-Sleep -Seconds 3
    } while ((Get-Date) -lt $deadline)
    throw "HTTP 준비 Timeout: $Name $Uri"
}

function Wait-DockerProbe {
    param([string]$Name, [scriptblock]$Probe, [int]$TimeoutSeconds = 240)
    Write-Host "[서비스 대기] $Name" -ForegroundColor Cyan
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    do {
        & $Probe *> $null
        if ($LASTEXITCODE -eq 0) { Write-Host "[서비스 확인] $Name" -ForegroundColor Green; return }
        Start-Sleep -Seconds 3
    } while ((Get-Date) -lt $deadline)
    throw "서비스 준비 Timeout: $Name"
}

$cpfRoot = Join-Path $DockerRoot "CPF"
$secretRoot = Join-Path $DockerRoot "Secrets"
$runtimeEnv = Join-Path $secretRoot "cpf-runtime.env"
$toolEnv = Join-Path $cpfRoot "tool-images.env"
$providerEnv = Join-Path $cpfRoot "qa39-provider-images.env"
$compose = @(
    "compose", "--project-name", "cpf",
    "--env-file", $runtimeEnv,
    "--env-file", $toolEnv,
    "--env-file", $providerEnv,
    "-f", (Join-Path $cpfRoot "compose.yml"),
    "-f", (Join-Path $cpfRoot "compose.redis.yml"),
    "-f", (Join-Path $cpfRoot "compose.kafka.yml"),
    "-f", (Join-Path $cpfRoot "compose.integration.yml"),
    "-f", (Join-Path $cpfRoot "compose.tooling.yml"),
    "-f", (Join-Path $cpfRoot "compose.qa39-runtime.yml")
)
$profile = if ($IncludeIbmMq) { @("--profile", "ibm-mq") } else { @() }
$services = @("rabbitmq", "artemis", "tcp-simulator", "mailpit", "wiremock", "toxiproxy", "otel-collector")
if ($IncludeIbmMq) { $services += "ibm-mq" }

Invoke-Docker ($compose + $profile + @("up", "-d") + $services)
$containers = @("cpf-rabbitmq", "cpf-artemis", "cpf-tcp-simulator", "cpf-mailpit", "cpf-wiremock", "cpf-toxiproxy", "cpf-otel-collector")
if ($IncludeIbmMq) { $containers += "cpf-ibm-mq" }
foreach ($name in $containers) {
    docker update --restart=no $name *> $null
    if ($LASTEXITCODE -ne 0) { throw "Restart Policy 설정 실패: $name" }
    Wait-ContainerRunning -Name $name -TimeoutSeconds $(if ($name -eq "cpf-ibm-mq") { 420 } else { 120 })
}

Wait-DockerProbe -Name "RabbitMQ" -Probe { docker exec cpf-rabbitmq rabbitmq-diagnostics -q check_running } -TimeoutSeconds 180
Wait-Tcp -Name "Artemis JMS" -HostName "127.0.0.1" -Port 61616 -TimeoutSeconds 180
Wait-DockerProbe -Name "TCP Simulator" -Probe { docker exec cpf-tcp-simulator python /app/qa39-tcp-simulator.py --self-test } -TimeoutSeconds 120
Wait-Http -Name "Mailpit" -Uri "http://127.0.0.1:18025/" -TimeoutSeconds 120
Wait-Http -Name "WireMock" -Uri "http://127.0.0.1:18080/__admin/health" -TimeoutSeconds 120
Wait-Http -Name "Toxiproxy" -Uri "http://127.0.0.1:8474/proxies" -TimeoutSeconds 120
Wait-Http -Name "OpenTelemetry Collector" -Uri "http://127.0.0.1:8888/metrics" -TimeoutSeconds 120
if ($IncludeIbmMq) {
    Wait-DockerProbe -Name "IBM MQ" -Probe { docker exec cpf-ibm-mq sh -lc "dspmq | grep -q 'STATUS(Running)'" } -TimeoutSeconds 420
}

$verify = @("-NoProfile", "-File", (Join-Path $PSScriptRoot "verify-qa39-runtime.ps1"), "-DockerRoot", $DockerRoot, "-RepoRoot", $RepoRoot, "-SourceIdentity", $SourceIdentity, "-RequireRunning")
if ($IncludeIbmMq) { $verify += "-IncludeIbmMq" }
& pwsh @verify
if ($LASTEXITCODE -ne 0) { throw "QA39 Runtime 실행 검증 실패(exit=$LASTEXITCODE)" }
Write-Host "CPF QA39 Runtime 수동 기동 완료. 사용 후 stop-qa39-runtime.ps1을 실행해야 합니다." -ForegroundColor Green
