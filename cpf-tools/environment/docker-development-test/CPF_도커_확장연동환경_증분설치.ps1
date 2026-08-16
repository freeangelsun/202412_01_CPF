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
        if ($pullExitCode -eq 0) { return $candidate }
        Write-Warning "Image 준비 실패, 다음 후보를 확인합니다: $candidate"
    }
    throw "$Name Image를 준비하지 못했습니다: $($Candidates -join ', ')"
}

function New-RandomSecret {
    param([int]$Length = 48)
    $alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789-_.~"
    $bytes = New-Object byte[] ($Length * 2)
    [System.Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
    $builder = [System.Text.StringBuilder]::new()
    foreach ($value in $bytes) {
        [void]$builder.Append($alphabet[$value % $alphabet.Length])
        if ($builder.Length -ge $Length) { break }
    }
    return $builder.ToString()
}

function Ensure-SecretFile {
    param([Parameter(Mandatory)][string]$Path)
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
        Write-Utf8NoBom -Path $Path -Content "$(New-RandomSecret)`n"
    }
    $value = (Get-Content -LiteralPath $Path -Raw -Encoding UTF8).Trim()
    if ([string]::IsNullOrWhiteSpace($value)) { throw "비어 있는 Secret 파일: $Path" }
}

function Read-EnvMap {
    param([Parameter(Mandatory)][string]$Path)
    $map = [ordered]@{}
    if (Test-Path -LiteralPath $Path -PathType Leaf) {
        foreach ($line in Get-Content -LiteralPath $Path -Encoding UTF8) {
            if ($line -match '^(?<key>[A-Za-z_][A-Za-z0-9_]*)=(?<value>.*)$') {
                $map[$Matches.key] = $Matches.value
            }
        }
    }
    return $map
}

function Write-EnvMap {
    param([Parameter(Mandatory)][string]$Path, [Parameter(Mandatory)]$Map)
    $lines = foreach ($key in $Map.Keys) { "$key=$($Map[$key])" }
    Write-Utf8NoBom -Path $Path -Content (($lines -join "`n") + "`n")
}

function Copy-FileRequired {
    param([Parameter(Mandatory)][string]$Source, [Parameter(Mandatory)][string]$Destination)
    if (-not (Test-Path -LiteralPath $Source -PathType Leaf)) { throw "설치 파일이 없습니다: $Source" }
    Copy-Item -LiteralPath $Source -Destination $Destination -Force
}

$sourceRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
$cpfRoot = Join-Path $DockerRoot "CPF"
$secretRoot = Join-Path $DockerRoot "Secrets"
$envPath = Join-Path $secretRoot "cpf-runtime.env"
$toolEnvPath = Join-Path $cpfRoot "tool-images.env"

New-Item -ItemType Directory -Force -Path $cpfRoot, $secretRoot | Out-Null
if (-not (Test-Path -LiteralPath $RepoRoot -PathType Container)) { throw "Repository가 없습니다: $RepoRoot" }

docker version *> $null
if ($LASTEXITCODE -ne 0) { throw "Docker Desktop이 실행 중이 아닙니다." }
if ((& docker info --format "{{.OSType}}").Trim() -ne "linux") { throw "Linux Container Backend가 필요합니다." }
if ((& docker info --format "{{.Architecture}}").Trim() -notin @("x86_64", "amd64")) { throw "linux/amd64 환경이 필요합니다." }

$cpfContainers = @(
    "cpf-mariadb", "cpf-postgresql", "cpf-oracle", "cpf-redis", "cpf-kafka",
    "cpf-wiremock", "cpf-sftp", "cpf-vault", "cpf-keycloak", "cpf-toxiproxy", "cpf-otel-collector"
)
$runningCpf = @(docker ps --format "{{.Names}}" | Where-Object { $_ -in $cpfContainers })
if ($runningCpf.Count -gt 0) {
    throw "증분 설치 중에는 CPF Container가 정지 상태여야 합니다. 실행 중: $($runningCpf -join ', ')"
}
$requiredBaseFiles = @("compose.yml", "compose.redis.yml", "compose.kafka.yml", "compose.tooling.yml", "tool-images.env")
foreach ($name in $requiredBaseFiles) {
    if (-not (Test-Path -LiteralPath (Join-Path $cpfRoot $name) -PathType Leaf)) {
        throw "기존 Base 환경 파일이 없습니다. 새 PC 또는 Base 미구성 환경에서는 전체 설치 Script를 사용하세요: $name"
    }
}

$toolMap = Read-EnvMap -Path $toolEnvPath
foreach ($key in @("TOXIPROXY_IMAGE", "OTEL_COLLECTOR_IMAGE", "TRIVY_IMAGE", "ORT_IMAGE", "FULL_TOOLCHAIN_IMAGE")) {
    if (-not $toolMap.Contains($key) -or [string]::IsNullOrWhiteSpace([string]$toolMap[$key])) {
        throw "기존 Base Tool Image 환경값이 없습니다. 전체 설치 Script로 Base 환경을 구성하세요: $key"
    }
}

$envMap = Read-EnvMap -Path $envPath
if (-not $envMap.Contains("CPF_ADMIN_PASSWORD")) {
    if ([string]::IsNullOrWhiteSpace($AdminPassword)) {
        $AdminPassword = Read-RequiredPassword -Prompt "CPF 로컬 관리자 공통 비밀번호"
    }
    $envMap["CPF_ADMIN_PASSWORD"] = $AdminPassword
}
$defaults = [ordered]@{
    CPF_SFTP_USER = "cpf-sftp"
    CPF_KEYCLOAK_ADMIN_USER = "cpf-admin"
    CPF_KEYCLOAK_TEST_USER = "cpf-reviewer"
    CPF_KEYCLOAK_REALM = "cpf-test"
    CPF_KEYCLOAK_PUBLIC_CLIENT = "cpf-admin-local"
    CPF_KEYCLOAK_SERVICE_CLIENT = "cpf-service-local"
}
foreach ($entry in $defaults.GetEnumerator()) {
    if (-not $envMap.Contains($entry.Key)) { $envMap[$entry.Key] = $entry.Value }
}
Write-EnvMap -Path $envPath -Map $envMap

$secretFiles = [ordered]@{
    sftp = Join-Path $secretRoot "sftp-password.txt"
    vault = Join-Path $secretRoot "vault-token.txt"
    keycloakAdmin = Join-Path $secretRoot "keycloak-admin-password.txt"
    keycloakTest = Join-Path $secretRoot "keycloak-test-password.txt"
    keycloakClient = Join-Path $secretRoot "keycloak-service-client-secret.txt"
}
foreach ($path in $secretFiles.Values) { Ensure-SecretFile -Path $path }

$runtimeFiles = @(
    "compose.integration.yml",
    "compose.tooling.yml",
    "Dockerfile.sftp-fixture",
    "Dockerfile.full-toolchain",
    "sftp-entrypoint.sh",
    "cpf-env.ps1",
    "cpf-tooling.ps1",
    "toxiproxy.json",
    "initialize-integration-fixtures.ps1",
    "verify-complete-environment.ps1",
    "verify-clean-prepared.ps1"
)
foreach ($name in $runtimeFiles) {
    Copy-FileRequired -Source (Join-Path $sourceRoot $name) -Destination (Join-Path $cpfRoot $name)
}

foreach ($fixtureName in @("wiremock", "keycloak")) {
    $source = Join-Path $sourceRoot "fixtures\$fixtureName"
    if (-not (Test-Path -LiteralPath $source -PathType Container)) { throw "Fixture 디렉터리가 없습니다: $source" }
    $destination = Join-Path $cpfRoot "fixtures\$fixtureName"
    New-Item -ItemType Directory -Force -Path $destination | Out-Null
    Copy-Item -Path (Join-Path $source "*") -Destination $destination -Recurse -Force
}
New-Item -ItemType Directory -Force -Path (Join-Path $cpfRoot "output\integration") | Out-Null


$wiremockImage = Pull-FirstAvailable -Name "WireMock" -Candidates @(
    "wiremock/wiremock:3.13.2"
)
$vaultImage = Pull-FirstAvailable -Name "Vault" -Candidates @(
    "hashicorp/vault:1.21.4",
    "hashicorp/vault:1.21"
)
$keycloakImage = Pull-FirstAvailable -Name "Keycloak" -Candidates @(
    "quay.io/keycloak/keycloak:26.6.1"
)
$alpineImage = Pull-FirstAvailable -Name "Alpine" -Candidates @(
    "alpine:3.23.5",
    "alpine:3.23"
)

$sftpImage = "cpf-sftp-fixture:alpine3.23"
if (-not (Test-Image $sftpImage)) {
    Invoke-Docker @(
        "build",
        "--pull=false",
        "--build-arg", "ALPINE_IMAGE=$alpineImage",
        "--file", (Join-Path $cpfRoot "Dockerfile.sftp-fixture"),
        "--tag", $sftpImage,
        $cpfRoot
    )
}

$fullRunnerImage = "cpf-full-development-test-runner:java25-node22-pwsh7.6.4-playwright1.62.0-integration1"
if (-not (Test-Image $fullRunnerImage)) {
    Invoke-Docker @(
        "build",
        "--pull=false",
        "--file", (Join-Path $cpfRoot "Dockerfile.full-toolchain"),
        "--tag", $fullRunnerImage,
        $cpfRoot
    )
}

$toolMap["WIREMOCK_IMAGE"] = $wiremockImage
$toolMap["VAULT_IMAGE"] = $vaultImage
$toolMap["KEYCLOAK_IMAGE"] = $keycloakImage
$toolMap["ALPINE_IMAGE"] = $alpineImage
$toolMap["SFTP_FIXTURE_IMAGE"] = $sftpImage
$toolMap["FULL_TOOLCHAIN_IMAGE"] = $fullRunnerImage
Write-EnvMap -Path $toolEnvPath -Map $toolMap

$compose = @(
    "compose",
    "--env-file", $envPath,
    "--env-file", $toolEnvPath,
    "-f", (Join-Path $cpfRoot "compose.yml"),
    "-f", (Join-Path $cpfRoot "compose.redis.yml"),
    "-f", (Join-Path $cpfRoot "compose.kafka.yml"),
    "-f", (Join-Path $cpfRoot "compose.integration.yml"),
    "-f", (Join-Path $cpfRoot "compose.tooling.yml")
)
Invoke-Docker ($compose + @("config", "--quiet"))
Invoke-Docker ($compose + @("create", "wiremock", "sftp", "vault", "keycloak"))

$integrationImages = @($wiremockImage, $vaultImage, $keycloakImage, $alpineImage, $sftpImage, $fullRunnerImage)
$lockPath = Join-Path $cpfRoot "image-lock-complete.json"
$existingLock = @()
if (Test-Path -LiteralPath $lockPath -PathType Leaf) {
    try { $existingLock = @(Get-Content -LiteralPath $lockPath -Raw -Encoding UTF8 | ConvertFrom-Json) } catch { $existingLock = @() }
}
$lockByImage = [ordered]@{}
foreach ($item in $existingLock) { if ($item.image) { $lockByImage[[string]$item.image] = $item } }
foreach ($image in $integrationImages) {
    $inspect = docker image inspect $image | ConvertFrom-Json
    $lockByImage[$image] = [pscustomobject]@{
        image = $image
        required = $true
        imageId = [string]$inspect[0].Id
        repoTags = @($inspect[0].RepoTags)
        repoDigests = @($inspect[0].RepoDigests)
    }
}
$previousRunner = "cpf-full-development-test-runner:java25-node22-pwsh7.6.4-playwright1.62.0"
if ($lockByImage.Contains($previousRunner)) { $lockByImage[$previousRunner].required = $false }
Write-Utf8NoBom -Path $lockPath -Content (($lockByImage.Values | ConvertTo-Json -Depth 8) + "`n")

& pwsh -NoProfile -File (Join-Path $cpfRoot "verify-complete-environment.ps1") -RequireStopped
if ($LASTEXITCODE -ne 0) { throw "확장 환경 상태 확인 실패(exit=$LASTEXITCODE)" }

Write-Host ""
Write-Host "CPF Docker 확장 연동 환경 증분 설치 완료" -ForegroundColor Green
Write-Host "추가 Container: cpf-wiremock, cpf-sftp, cpf-vault, cpf-keycloak"
Write-Host "모든 추가 Container는 Created/Stopped 상태입니다."
Write-Host "Secret 값은 출력하지 않았으며 $secretRoot 아래에만 저장했습니다."
Write-Host "초기 Fixture 구성: initialize-integration-fixtures.ps1"
