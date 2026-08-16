param(
    [string]$DockerRoot = "C:\dev\Docker",
    [string]$RepoRoot = "C:\dev\projects\jck\202412_01_CPF",
    [string]$AdminPassword = ""
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Write-Utf8NoBom {
    param([Parameter(Mandatory)][string]$Path, [Parameter(Mandatory)][string]$Content)
    $parent = Split-Path -Parent $Path
    if ($parent) { New-Item -ItemType Directory -Force -Path $parent | Out-Null }
    [System.IO.File]::WriteAllText($Path, $Content, [System.Text.UTF8Encoding]::new($false))
}

function Read-RequiredPassword {
    param([Parameter(Mandatory)][string]$Prompt)
    $secure = Read-Host -Prompt $Prompt -AsSecureString
    $pointer = [System.Runtime.InteropServices.Marshal]::SecureStringToBSTR($secure)
    try {
        $plain = [System.Runtime.InteropServices.Marshal]::PtrToStringBSTR($pointer)
        if ([string]::IsNullOrWhiteSpace($plain)) { throw "비밀번호를 비워 둘 수 없습니다." }
        return $plain
    } finally {
        [System.Runtime.InteropServices.Marshal]::ZeroFreeBSTR($pointer)
    }
}

function Invoke-Docker {
    param([Parameter(Mandatory)][string[]]$Arguments)
    & docker @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "docker $($Arguments -join ' ') 실패(exit=$LASTEXITCODE)"
    }
}

function Test-Image {
    param([Parameter(Mandatory)][string]$Reference)
    & docker image inspect $Reference *> $null
    return $LASTEXITCODE -eq 0
}

function Pull-FirstAvailable {
    param(
        [Parameter(Mandatory)][string]$Name,
        [Parameter(Mandatory)][string[]]$Candidates
    )
    foreach ($candidate in $Candidates) {
        Write-Host "[$Name] Image 준비: $candidate" -ForegroundColor Cyan
        $pullOutput = & docker pull $candidate 2>&1
        $pullExitCode = $LASTEXITCODE
        $pullOutput | Out-Host
        if ($pullExitCode -eq 0) {
            return $candidate
        }
        Write-Warning "Image 준비 실패, 다음 후보를 확인합니다: $candidate"
    }
    throw "$Name Image를 준비하지 못했습니다: $($Candidates -join ', ')"
}

function Get-EnvValue {
    param([Parameter(Mandatory)][string]$Path, [Parameter(Mandatory)][string]$Name)
    $line = Get-Content -LiteralPath $Path -Encoding UTF8 |
        Where-Object { $_ -match "^\s*$([regex]::Escape($Name))=" } |
        Select-Object -First 1
    if ($null -eq $line) { return "" }
    return $line.Substring($line.IndexOf("=") + 1)
}

docker version *> $null
if ($LASTEXITCODE -ne 0) { throw "Docker Desktop이 실행 중이 아닙니다." }

$osType = (& docker info --format "{{.OSType}}").Trim()
$architecture = (& docker info --format "{{.Architecture}}").Trim()
if ($osType -ne "linux") { throw "Linux Container Backend가 필요합니다. OSType=$osType" }
if ($architecture -notin @("x86_64", "amd64")) {
    throw "현재 전체 Toolchain Image는 linux/amd64 기준입니다. Architecture=$architecture"
}

$sourceRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$cpfRoot = Join-Path $DockerRoot "CPF"
$secretRoot = Join-Path $DockerRoot "Secrets"
$envPath = Join-Path $secretRoot "cpf-runtime.env"
$redisSecretPath = Join-Path $secretRoot "redis-password.txt"

New-Item -ItemType Directory -Force -Path $cpfRoot, $secretRoot | Out-Null

if (-not (Test-Path -LiteralPath $envPath -PathType Leaf)) {
    if ([string]::IsNullOrWhiteSpace($AdminPassword)) {
        $AdminPassword = Read-RequiredPassword -Prompt "CPF 로컬 관리자 공통 비밀번호"
    }
    if ($AdminPassword.Contains("`r") -or $AdminPassword.Contains("`n")) {
        throw "관리자 비밀번호에 줄바꿈을 사용할 수 없습니다."
    }
    Write-Utf8NoBom -Path $envPath -Content "CPF_ADMIN_PASSWORD=$AdminPassword`n"
}
if (-not (Test-Path -LiteralPath $redisSecretPath -PathType Leaf)) {
    $password = if (-not [string]::IsNullOrWhiteSpace($AdminPassword)) {
        $AdminPassword
    } else {
        Get-EnvValue -Path $envPath -Name "CPF_ADMIN_PASSWORD"
    }
    if ([string]::IsNullOrWhiteSpace($password)) {
        throw "Redis Secret을 생성할 관리자 비밀번호가 없습니다."
    }
    Write-Utf8NoBom -Path $redisSecretPath -Content "$password`n"
}

[void][System.IO.File]::ReadAllBytes($envPath)
[void][System.IO.File]::ReadAllBytes($redisSecretPath)

$baseRuntimeFiles = @(
    "compose.yml",
    "compose.redis.yml",
    "compose.kafka.yml",
    "cpf-env.ps1",
    "reset-test-data.ps1",
    "verify-clean-prepared.ps1"
)
foreach ($name in $baseRuntimeFiles) {
    $source = Join-Path $sourceRoot $name
    $destination = Join-Path $cpfRoot $name
    if (-not (Test-Path -LiteralPath $source -PathType Leaf)) {
        throw "설치 묶음 파일이 없습니다: $source"
    }
    if (-not (Test-Path -LiteralPath $destination -PathType Leaf)) {
        Copy-Item -LiteralPath $source -Destination $destination
    }
}

$ownedFiles = @(
    "Dockerfile.full-toolchain",
    "compose.tooling.yml",
    "otel-collector-config.yml",
    "toxiproxy.json",
    "cpf-tooling.ps1",
    "run-trivy.ps1",
    "run-ort.ps1",
    "run-full-toolchain.ps1",
    "verify-complete-environment.ps1",
    "verify-clean-prepared.ps1",
    "CPF_도커_확장연동환경_증분설치.ps1",
    "compose.integration.yml",
    "Dockerfile.sftp-fixture",
    "sftp-entrypoint.sh",
    "initialize-integration-fixtures.ps1"
)
foreach ($name in $ownedFiles) {
    $source = Join-Path $sourceRoot $name
    if (-not (Test-Path -LiteralPath $source -PathType Leaf)) {
        throw "설치 묶음 파일이 없습니다: $source"
    }
    Copy-Item -LiteralPath $source -Destination (Join-Path $cpfRoot $name) -Force
}

foreach ($dir in @(
    (Join-Path $cpfRoot "output\otel"),
    (Join-Path $cpfRoot "output\trivy"),
    (Join-Path $cpfRoot "output\ort"),
    (Join-Path $cpfRoot "cache\trivy")
)) {
    New-Item -ItemType Directory -Force -Path $dir | Out-Null
}

$baseImages = @(
    "mariadb:12.3.2",
    "postgres:18.4-trixie",
    "container-registry.oracle.com/database/free:26ai-free-23.26.2.0.0",
    "redis:8.8.1-trixie",
    "apache/kafka:4.3.1",
    "eclipse-temurin:25.0.3_9-jdk",
    "node:22.16.0-bookworm",
    "mcr.microsoft.com/playwright:v1.62.0-noble"
)
foreach ($image in $baseImages) {
    if (-not (Test-Image $image)) {
        Invoke-Docker @("pull", $image)
    }
}

$toxiproxyImage = Pull-FirstAvailable -Name "Toxiproxy" -Candidates @(
    "ghcr.io/shopify/toxiproxy:2.12.0",
    "ghcr.io/shopify/toxiproxy:latest"
)
$otelImage = Pull-FirstAvailable -Name "OpenTelemetry Collector" -Candidates @(
    "otel/opentelemetry-collector-contrib:0.157.0"
)
$trivyImage = Pull-FirstAvailable -Name "Trivy" -Candidates @(
    "aquasec/trivy:0.70.0"
)
$ortImage = Pull-FirstAvailable -Name "OSS Review Toolkit" -Candidates @(
    "ghcr.io/oss-review-toolkit/ort:87.3.0",
    "ghcr.io/oss-review-toolkit/ort:latest"
)

$fullRunnerImage = "cpf-full-development-test-runner:java25-node22-pwsh7.6.4-playwright1.62.0-integration1"
Invoke-Docker @(
    "build",
    "--pull=false",
    "--file", (Join-Path $cpfRoot "Dockerfile.full-toolchain"),
    "--tag", $fullRunnerImage,
    $cpfRoot
)

$toolEnv = @"
TOXIPROXY_IMAGE=$toxiproxyImage
OTEL_COLLECTOR_IMAGE=$otelImage
TRIVY_IMAGE=$trivyImage
ORT_IMAGE=$ortImage
FULL_TOOLCHAIN_IMAGE=$fullRunnerImage
"@
Write-Utf8NoBom -Path (Join-Path $cpfRoot "tool-images.env") -Content $toolEnv

$toolCheck = @(
    "set -e",
    "java -version",
    "node --version",
    "npm --version",
    "pwsh -NoLogo -NoProfile -Command '`$PSVersionTable.PSVersion.ToString()'",
    "python3 --version",
    "git --version",
    "mariadb --version",
    "psql --version",
    "sqlplus -V",
    "docker --version",
    "docker compose version",
    "jq --version",
    "openssl version",
    "ssh -V",
    "sshpass -V"
) -join "; "
Invoke-Docker @("run", "--rm", $fullRunnerImage, "bash", "-lc", $toolCheck)
Invoke-Docker @("run", "--rm", $trivyImage, "--version")
Invoke-Docker @("run", "--rm", $ortImage, "--version")
Invoke-Docker @("run", "--rm", $otelImage, "--version")

$baseCompose = Join-Path $cpfRoot "compose.yml"
$redisCompose = Join-Path $cpfRoot "compose.redis.yml"
$kafkaCompose = Join-Path $cpfRoot "compose.kafka.yml"
$toolCompose = Join-Path $cpfRoot "compose.tooling.yml"
foreach ($file in @($baseCompose, $redisCompose, $kafkaCompose, $toolCompose)) {
    if (-not (Test-Path -LiteralPath $file -PathType Leaf)) {
        throw "Compose 파일이 없습니다: $file"
    }
}

$runningNames = @(docker ps --format "{{.Names}}" | Where-Object {
    $_ -in @(
        "cpf-mariadb", "cpf-postgresql", "cpf-oracle", "cpf-redis", "cpf-kafka",
        "cpf-toxiproxy", "cpf-otel-collector"
    )
})
if ($runningNames.Count -gt 0) {
    throw "설치 중에는 CPF Service가 정지 상태여야 합니다. 실행 중: $($runningNames -join ', ')"
}

$composeArgs = @(
    "compose",
    "--env-file", $envPath,
    "--env-file", (Join-Path $cpfRoot "tool-images.env"),
    "-f", $baseCompose,
    "-f", $redisCompose,
    "-f", $kafkaCompose,
    "-f", $toolCompose
)

Invoke-Docker ($composeArgs + @("config", "--quiet"))
Invoke-Docker ($composeArgs + @("create"))

$requiredImages = @(
    $baseImages
    $toxiproxyImage
    $otelImage
    $trivyImage
    $ortImage
    $fullRunnerImage
)
$legacyImages = @(
    "cpf-playwright:1.62.0-node22.16.0",
    "cpf-playwright-runner:1.62.0-node22.16.0",
    "cpf-validation-runner:java25-node22-pwsh7.6.4-playwright1.62.0"
)
$allImages = @($requiredImages)
foreach ($legacyImage in $legacyImages) {
    if (Test-Image $legacyImage) {
        $allImages += $legacyImage
    }
}

$lock = foreach ($image in $allImages) {
    $inspect = docker image inspect $image | ConvertFrom-Json
    [pscustomobject]@{
        image = $image
        required = $requiredImages -contains $image
        imageId = [string]$inspect[0].Id
        repoTags = @($inspect[0].RepoTags)
        repoDigests = @($inspect[0].RepoDigests)
    }
}
Write-Utf8NoBom -Path (Join-Path $cpfRoot "image-lock-complete.json") -Content (
    $lock | ConvertTo-Json -Depth 8
)

$integrationInstaller = Join-Path $sourceRoot "CPF_도커_확장연동환경_증분설치.ps1"
if (-not (Test-Path -LiteralPath $integrationInstaller -PathType Leaf)) { throw "확장 연동 설치 Script가 없습니다: $integrationInstaller" }
$integrationArgs = @(
    "-NoProfile", "-File", $integrationInstaller,
    "-DockerRoot", $DockerRoot, "-RepoRoot", $RepoRoot
)
# 관리자 비밀번호는 이미 Repository 밖 환경파일에 저장되어 있으므로 자식 Process 인자로 전달하지 않는다.
& pwsh @integrationArgs
if ($LASTEXITCODE -ne 0) { throw "확장 연동 환경 설치 실패(exit=$LASTEXITCODE)" }

& pwsh -NoProfile -File (Join-Path $cpfRoot "verify-complete-environment.ps1") -RequireStopped
if ($LASTEXITCODE -ne 0) { throw "전체 환경 상태 확인 실패(exit=$LASTEXITCODE)" }

Write-Host ""
Write-Host "CPF Docker 개발·테스트 환경 전체 구성 완료" -ForegroundColor Green
Write-Host "Base: Oracle, PostgreSQL, MariaDB, Redis, Kafka"
Write-Host "확장 연동: WireMock, SFTP, Vault, Keycloak"
Write-Host "Tool: Toxiproxy, OpenTelemetry Collector, Trivy, OSS Review Toolkit"
Write-Host "통합 Toolchain: Java 25, Node 22, PowerShell 7.6.4, Playwright 1.62.0, Python 3, Git, DB Client, Docker CLI, OpenSSH Client"
Write-Host "필수 Image: 18개, 기존 Runner Image는 있으면 보존"
Write-Host "Container: 11개 Created/Stopped"
Write-Host "Running: 0"
Write-Host "CPF 업무 Schema·Data·Seed·Kafka Topic: 생성하지 않음"
Write-Host "Runtime Root: $cpfRoot"
