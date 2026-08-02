param(
    [string]$DockerRoot = "C:\dev\Docker",
    [string]$RepoRoot = "C:\dev\projects\jck\202412_01_CPF",
    [switch]$IncludeIbmMq
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
function Invoke-Docker { param([string[]]$Arguments); & docker @Arguments; if ($LASTEXITCODE -ne 0) { throw "docker $($Arguments -join ' ') 실패(exit=$LASTEXITCODE)" } }

if (-not (Test-Path -LiteralPath $RepoRoot -PathType Container)) { throw "Repository가 없습니다: $RepoRoot" }
docker version *> $null
if ($LASTEXITCODE -ne 0) { throw "Docker Desktop이 실행 중이 아닙니다." }
$sourceRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$cpfRoot = Join-Path $DockerRoot "CPF"
$secretRoot = Join-Path $DockerRoot "Secrets"
$copyNames = @(
    "compose.qa39-runtime.yml", "compose.integration.yml", "otel-collector-config.yml",
    "CPF_도커_QA39_Runtime_증분설치.ps1", "repair-qa39-runtime-r3.ps1",
    "start-qa39-runtime.ps1", "verify-qa39-runtime.ps1", "run-qa39-runtime-validation.ps1",
    "CPF_QA39_DOCKER_RUNTIME_MANIFEST.json"
)
foreach ($name in $copyNames) {
    $src = Join-Path $sourceRoot $name
    if (-not (Test-Path -LiteralPath $src -PathType Leaf)) { throw "수정 파일 누락: $src" }
    Copy-Item -LiteralPath $src -Destination (Join-Path $cpfRoot $name) -Force
}

$containers = @("cpf-rabbitmq", "cpf-artemis", "cpf-tcp-simulator", "cpf-mailpit", "cpf-wiremock", "cpf-toxiproxy", "cpf-otel-collector")
if ($IncludeIbmMq) { $containers += "cpf-ibm-mq" }
$running = @(docker ps --format "{{.Names}}" | Where-Object { $_ -in $containers })
if ($running.Count -gt 0) { Invoke-Docker (@("stop") + $running) }
foreach ($name in $containers) { docker container inspect $name *> $null; if ($LASTEXITCODE -eq 0) { docker update --restart=no $name *> $null } }

$runtimeEnv = Join-Path $secretRoot "cpf-runtime.env"
$toolEnv = Join-Path $cpfRoot "tool-images.env"
$providerEnv = Join-Path $cpfRoot "qa39-provider-images.env"
foreach ($path in @($runtimeEnv, $toolEnv, $providerEnv)) { if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "환경파일 누락: $path" } }
$compose = @(
    "compose", "--project-name", "cpf",
    "--env-file", $runtimeEnv, "--env-file", $toolEnv, "--env-file", $providerEnv,
    "-f", (Join-Path $cpfRoot "compose.yml"),
    "-f", (Join-Path $cpfRoot "compose.redis.yml"),
    "-f", (Join-Path $cpfRoot "compose.kafka.yml"),
    "-f", (Join-Path $cpfRoot "compose.integration.yml"),
    "-f", (Join-Path $cpfRoot "compose.tooling.yml"),
    "-f", (Join-Path $cpfRoot "compose.qa39-runtime.yml")
)
$profile = if ($IncludeIbmMq) { @("--profile", "ibm-mq") } else { @() }
Invoke-Docker ($compose + $profile + @("config", "--quiet"))
$services = @("rabbitmq", "artemis", "tcp-simulator", "mailpit", "wiremock", "toxiproxy", "otel-collector")
if ($IncludeIbmMq) { $services += "ibm-mq" }
Invoke-Docker ($compose + $profile + @("create", "--force-recreate") + $services)
foreach ($name in $containers) { docker update --restart=no $name *> $null; if ($LASTEXITCODE -ne 0) { throw "Restart Policy 설정 실패: $name" } }
$verify = @("-NoProfile", "-ExecutionPolicy", "Bypass", "-File", (Join-Path $cpfRoot "verify-qa39-runtime.ps1"), "-DockerRoot", $DockerRoot, "-RepoRoot", $RepoRoot, "-RequireStopped")
if ($IncludeIbmMq) { $verify += "-IncludeIbmMq" }
& pwsh @verify
if ($LASTEXITCODE -ne 0) { throw "R3 정지 상태 검증 실패(exit=$LASTEXITCODE)" }
Write-Host "R3 Health·OTel 설정 적용 완료 / Container 정지 / Restart Policy no" -ForegroundColor Green
