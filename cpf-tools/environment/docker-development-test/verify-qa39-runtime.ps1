param(
    [string]$DockerRoot = "C:\dev\Docker",
    [string]$RepoRoot = "C:\dev\projects\jck\202412_01_CPF",
    [string]$SourceIdentity = "",
    [string]$RuntimeDefinitionRoot = "",
    [string]$EvidenceDirectory = "",
    [switch]$IncludeIbmMq,
    [switch]$RequireStopped,
    [switch]$RequireRunning
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Read-EnvMap {
    param([Parameter(Mandatory)][string]$Path)
    $map = @{}
    foreach ($line in Get-Content -LiteralPath $Path -Encoding UTF8) {
        if ($line -match '^(?<key>[A-Za-z_][A-Za-z0-9_]*)=(?<value>.*)$') { $map[$Matches.key] = $Matches.value }
    }
    return $map
}

function Test-Http {
    param([Parameter(Mandatory)][string]$Name, [Parameter(Mandatory)][string]$Uri)
    try {
        $response = Invoke-WebRequest -UseBasicParsing -Uri $Uri -TimeoutSec 10
        if ([int]$response.StatusCode -lt 200 -or [int]$response.StatusCode -ge 400) { throw "status=$($response.StatusCode)" }
    } catch { throw "$Name HTTP 검증 실패: $Uri / $($_.Exception.Message)" }
}

function Test-Tcp {
    param([Parameter(Mandatory)][string]$Name, [string]$HostName = "127.0.0.1", [Parameter(Mandatory)][int]$Port)
    $client = [Net.Sockets.TcpClient]::new()
    try {
        $task = $client.ConnectAsync($HostName, $Port)
        if (-not $task.Wait(3000) -or -not $client.Connected) { throw "연결 실패" }
    } catch { throw "$Name TCP 검증 실패: $HostName`:$Port / $($_.Exception.Message)" } finally { $client.Dispose() }
}

function Invoke-Checked {
    param([Parameter(Mandatory)][scriptblock]$Command, [Parameter(Mandatory)][string]$Name)
    & $Command
    if ($LASTEXITCODE -ne 0) { throw "$Name 실패(exit=$LASTEXITCODE)" }
}

$cpfRoot = Join-Path $DockerRoot "CPF"
$secretRoot = Join-Path $DockerRoot "Secrets"
$runtimeEnvPath = Join-Path $secretRoot "cpf-runtime.env"
$providerEnvPath = Join-Path $cpfRoot "qa39-provider-images.env"
$runtimeDefinitionRoot = if ([string]::IsNullOrWhiteSpace($RuntimeDefinitionRoot)) { $PSScriptRoot } else { [IO.Path]::GetFullPath($RuntimeDefinitionRoot) }
$evidenceRoot = if ([string]::IsNullOrWhiteSpace($EvidenceDirectory)) { Join-Path $cpfRoot "output\qa39-runtime" } else { [IO.Path]::GetFullPath($EvidenceDirectory) }
New-Item -ItemType Directory -Path $evidenceRoot -Force | Out-Null
foreach ($path in @($runtimeEnvPath, $providerEnvPath)) { if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "환경파일 누락: $path" } }

$provider = Read-EnvMap -Path $providerEnvPath
$requiredImageKeys = @("RABBITMQ_IMAGE", "ARTEMIS_IMAGE", "MAILPIT_IMAGE", "PYTHON_FIXTURE_IMAGE")
if ($IncludeIbmMq) { $requiredImageKeys += "IBM_MQ_IMAGE" }
$imageEvidence = @()
foreach ($key in $requiredImageKeys) {
    if (-not $provider.ContainsKey($key) -or [string]::IsNullOrWhiteSpace($provider[$key])) { throw "Image 환경값 누락: $key" }
    $image = [string]$provider[$key]
    $inspect = docker image inspect $image | ConvertFrom-Json
    if ($LASTEXITCODE -ne 0) { throw "Image 누락: $image" }
    $imageEvidence += [pscustomobject]@{ key=$key; image=$image; id=[string]$inspect[0].Id; digests=@($inspect[0].RepoDigests) }
}

$expectedContainers = @("cpf-rabbitmq", "cpf-artemis", "cpf-tcp-simulator", "cpf-mailpit", "cpf-wiremock", "cpf-toxiproxy", "cpf-otel-collector")
if ($IncludeIbmMq) { $expectedContainers += "cpf-ibm-mq" }
$existing = @(docker ps -a --format "{{.Names}}")
$missingContainers = @($expectedContainers | Where-Object { $existing -notcontains $_ })
if ($missingContainers.Count -gt 0) { throw "Container 누락: $($missingContainers -join ', ')" }
$runningNames = @(docker ps --format "{{.Names}}")
$running = @($expectedContainers | Where-Object { $runningNames -contains $_ })
if ($RequireStopped -and $running.Count -gt 0) { throw "정지 상태가 아닙니다: $($running -join ', ')" }
if ($RequireRunning -and $running.Count -ne $expectedContainers.Count) { throw "실행 상태가 아닙니다: $((@($expectedContainers | Where-Object { $runningNames -notcontains $_ })) -join ', ')" }

$containerEvidence = @()
$restartMismatch = @()
foreach ($name in $expectedContainers) {
    $inspect = docker inspect $name | ConvertFrom-Json
    if ($LASTEXITCODE -ne 0) { throw "Container inspect 실패: $name" }
    $policy = [string]$inspect[0].HostConfig.RestartPolicy.Name
    if ($policy -ne "no") { $restartMismatch += "$name=$policy" }
    $health = if ($inspect[0].State.PSObject.Properties.Name -contains "Health") { [string]$inspect[0].State.Health.Status } else { "none" }
    $containerEvidence += [pscustomobject]@{ name=$name; running=[bool]$inspect[0].State.Running; status=[string]$inspect[0].State.Status; health=$health; restartPolicy=$policy; image=[string]$inspect[0].Config.Image }
}
if ($restartMismatch.Count -gt 0) { throw "Restart Policy 불일치: $($restartMismatch -join ', ')" }

$expectedVolumes = @("cpf-rabbitmq-data", "cpf-artemis-data", "cpf-mailpit-data")
if ($IncludeIbmMq) { $expectedVolumes += "cpf-ibm-mq-data" }
$volumeNames = @(docker volume ls --format "{{.Name}}")
$missingVolumes = @($expectedVolumes | Where-Object { $volumeNames -notcontains $_ })
if ($missingVolumes.Count -gt 0) { throw "Volume 누락: $($missingVolumes -join ', ')" }

$requiredSecrets = @((Join-Path $secretRoot "rabbitmq-password.txt"), (Join-Path $secretRoot "artemis-password.txt"))
if ($IncludeIbmMq) { $requiredSecrets += (Join-Path $secretRoot "mqAdminPassword"); $requiredSecrets += (Join-Path $secretRoot "mqAppPassword") }
foreach ($path in $requiredSecrets) { if (-not (Test-Path -LiteralPath $path -PathType Leaf) -or (Get-Item -LiteralPath $path).Length -le 0) { throw "Secret 누락 또는 비어 있음: $path" } }
$secretValues = @($requiredSecrets | ForEach-Object { (Get-Content -LiteralPath $_ -Raw -Encoding UTF8).Trim() })
if (@($secretValues | Select-Object -Unique).Count -ne 1) { throw "QA39 Provider Secret 값이 서로 다릅니다." }

$requiredFiles = @(
    "compose.qa39-runtime.yml", "compose.integration.yml", "compose.tooling.yml", "otel-collector-config.yml", "toxiproxy.json",
    "start-qa39-runtime.ps1", "verify-qa39-runtime.ps1", "run-qa39-runtime-fault-smoke.ps1", "run-qa39-runtime-validation.ps1",
    "stop-qa39-runtime.ps1", "cleanup-qa39-runtime.ps1", "repair-qa39-runtime-r3.ps1", "CPF_QA39_DOCKER_RUNTIME_MANIFEST.json",
    "fixtures\tcp\qa39-tcp-simulator.py", "fixtures\wiremock\mappings\qa39-sms-submit.json", "fixtures\wiremock\mappings\qa39-sms-status.json"
)
$missingFiles = @($requiredFiles | Where-Object { -not (Test-Path -LiteralPath (Join-Path $runtimeDefinitionRoot $_) -PathType Leaf) })
if ($missingFiles.Count -gt 0) { throw "Runtime 파일 누락: $($missingFiles -join ', ')" }

$smoke = [ordered]@{}
if ($RequireRunning) {
    Invoke-Checked -Name "RabbitMQ" -Command { docker exec cpf-rabbitmq rabbitmq-diagnostics -q check_running }
    $smoke.rabbitmq = "PASS"
    Test-Tcp -Name "Artemis JMS" -Port 61616
    $smoke.artemis = "PASS"
    Invoke-Checked -Name "TCP Simulator" -Command { docker exec cpf-tcp-simulator python /app/qa39-tcp-simulator.py --self-test }
    $smoke.tcpSimulator = "PASS"
    Test-Http -Name "Mailpit" -Uri "http://127.0.0.1:18025/"
    $smoke.mailpit = "PASS"
    Test-Http -Name "WireMock" -Uri "http://127.0.0.1:18080/__admin/health"
    $smoke.wiremock = "PASS"
    Test-Http -Name "Toxiproxy" -Uri "http://127.0.0.1:8474/proxies"
    $smoke.toxiproxy = "PASS"
    Test-Http -Name "OpenTelemetry Collector" -Uri "http://127.0.0.1:8888/metrics"
    $smoke.otelCollector = "PASS"
    if ($IncludeIbmMq) {
        $mqOutput = docker exec cpf-ibm-mq dspmq
        if ($LASTEXITCODE -ne 0 -or ($mqOutput -join "`n") -notmatch "STATUS\(Running\)") { throw "IBM MQ Queue Manager가 Running 상태가 아닙니다." }
        $smoke.ibmMq = "PASS"
    }
    $smsBody = '{"to":"+821012345678","templateId":"QA39","idempotencyKey":"qa39-docker-smoke"}'
    $smsResponse = Invoke-RestMethod -Method Post -Uri "http://127.0.0.1:18080/qa39/sms/messages" -ContentType "application/json" -Body $smsBody -TimeoutSec 10
    if ([string]::IsNullOrWhiteSpace([string]$smsResponse.providerMessageId) -or [string]$smsResponse.status -ne "ACCEPTED") { throw "WireMock SMS Submit 응답이 예상과 다릅니다." }
    $smoke.smsFixture = "PASS"
}

$head = $SourceIdentity
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$evidence = [pscustomobject]@{ generatedAt=(Get-Date).ToString("o"); repository=$RepoRoot; head=$head; includeIbmMq=[bool]$IncludeIbmMq; requireStopped=[bool]$RequireStopped; requireRunning=[bool]$RequireRunning; images=$imageEvidence; containers=$containerEvidence; volumes=$expectedVolumes; secretFiles=@($requiredSecrets | ForEach-Object { Split-Path -Leaf $_ }); smoke=$smoke }
$evidencePath = Join-Path $evidenceRoot "qa39-runtime-$timestamp.json"
[IO.File]::WriteAllText($evidencePath, ($evidence | ConvertTo-Json -Depth 10) + "`n", [Text.UTF8Encoding]::new($false))
Write-Host "Prepared Containers: $($expectedContainers.Count)/$($expectedContainers.Count)"
Write-Host "Running Containers: $($running.Count)"
Write-Host "Restart Policy no: $($expectedContainers.Count)/$($expectedContainers.Count)"
Write-Host "Evidence: $evidencePath"
Write-Host "CPF QA39 Provider Runtime 검증 완료" -ForegroundColor Green
