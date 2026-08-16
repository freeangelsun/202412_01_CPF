param(
    [string]$DockerRoot = "C:\dev\Docker",
    [switch]$RequireStopped
)

$ErrorActionPreference = "Stop"
$cpfRoot = Join-Path $DockerRoot "CPF"
$secretRoot = Join-Path $DockerRoot "Secrets"
$toolEnvPath = Join-Path $cpfRoot "tool-images.env"
if (-not (Test-Path -LiteralPath $toolEnvPath -PathType Leaf)) { throw "Tool Image 환경파일이 없습니다: $toolEnvPath" }

$dynamic = @{}
Get-Content -LiteralPath $toolEnvPath -Encoding UTF8 | ForEach-Object {
    if ($_ -match "^(?<k>[A-Z0-9_]+)=(?<v>.+)$") { $dynamic[$Matches.k] = $Matches.v }
}
$requiredDynamicKeys = @(
    "TOXIPROXY_IMAGE", "OTEL_COLLECTOR_IMAGE", "TRIVY_IMAGE", "ORT_IMAGE", "FULL_TOOLCHAIN_IMAGE",
    "WIREMOCK_IMAGE", "VAULT_IMAGE", "KEYCLOAK_IMAGE", "ALPINE_IMAGE", "SFTP_FIXTURE_IMAGE"
)
foreach ($key in $requiredDynamicKeys) {
    if (-not $dynamic.ContainsKey($key) -or [string]::IsNullOrWhiteSpace($dynamic[$key])) {
        throw "Tool Image 환경값이 없습니다: $key"
    }
}

$requiredImages = @(
    "mariadb:12.3.2",
    "postgres:18.4-trixie",
    "container-registry.oracle.com/database/free:26ai-free-23.26.2.0.0",
    "redis:8.8.1-trixie",
    "apache/kafka:4.3.1",
    "eclipse-temurin:25.0.3_9-jdk",
    "node:22.16.0-bookworm",
    "mcr.microsoft.com/playwright:v1.62.0-noble",
    $dynamic["TOXIPROXY_IMAGE"],
    $dynamic["OTEL_COLLECTOR_IMAGE"],
    $dynamic["TRIVY_IMAGE"],
    $dynamic["ORT_IMAGE"],
    $dynamic["FULL_TOOLCHAIN_IMAGE"],
    $dynamic["WIREMOCK_IMAGE"],
    $dynamic["VAULT_IMAGE"],
    $dynamic["KEYCLOAK_IMAGE"],
    $dynamic["ALPINE_IMAGE"],
    $dynamic["SFTP_FIXTURE_IMAGE"]
)
$legacyImages = @(
    "cpf-playwright:1.62.0-node22.16.0",
    "cpf-playwright-runner:1.62.0-node22.16.0",
    "cpf-validation-runner:java25-node22-pwsh7.6.4-playwright1.62.0",
    "cpf-full-development-test-runner:java25-node22-pwsh7.6.4-playwright1.62.0"
)

$missingImages = @()
foreach ($image in $requiredImages) {
    docker image inspect $image *> $null
    if ($LASTEXITCODE -ne 0) { $missingImages += $image }
}
if ($missingImages.Count -gt 0) { throw "누락 Image: $($missingImages -join ', ')" }

$legacyPresent = @()
foreach ($image in $legacyImages) {
    docker image inspect $image *> $null
    if ($LASTEXITCODE -eq 0) { $legacyPresent += $image }
}

$expectedContainers = @(
    "cpf-mariadb",
    "cpf-postgresql",
    "cpf-oracle",
    "cpf-redis",
    "cpf-kafka",
    "cpf-wiremock",
    "cpf-sftp",
    "cpf-vault",
    "cpf-keycloak",
    "cpf-toxiproxy",
    "cpf-otel-collector"
)
$existingNames = @(docker ps -a --format "{{.Names}}")
$containers = @($expectedContainers | Where-Object { $existingNames -contains $_ })
$runningNames = @(docker ps --format "{{.Names}}")
$running = @($expectedContainers | Where-Object { $runningNames -contains $_ })
if ($containers.Count -ne $expectedContainers.Count) {
    $missingContainers = @($expectedContainers | Where-Object { $existingNames -notcontains $_ })
    throw "누락 Container: $($missingContainers -join ', ')"
}

$restartMismatch = @()
foreach ($name in $expectedContainers) {
    $policy = (& docker inspect --format "{{.HostConfig.RestartPolicy.Name}}" $name).Trim()
    if ($LASTEXITCODE -ne 0 -or $policy -ne "no") { $restartMismatch += "$name=$policy" }
}
if ($restartMismatch.Count -gt 0) { throw "Restart Policy 불일치: $($restartMismatch -join ', ')" }

$expectedVolumes = @(
    "cpf-mariadb-data",
    "cpf-postgresql-data",
    "cpf-oracle-data",
    "cpf-redis-data",
    "cpf-kafka-data",
    "cpf-sftp-data",
    "cpf-keycloak-data"
)
$volumeNames = @(docker volume ls --format "{{.Name}}")
$volumes = @($expectedVolumes | Where-Object { $volumeNames -contains $_ })
if ($volumes.Count -ne $expectedVolumes.Count) {
    $missingVolumes = @($expectedVolumes | Where-Object { $volumeNames -notcontains $_ })
    throw "누락 Volume: $($missingVolumes -join ', ')"
}

$requiredSecretFiles = @(
    (Join-Path $secretRoot "cpf-runtime.env"),
    (Join-Path $secretRoot "redis-password.txt"),
    (Join-Path $secretRoot "sftp-password.txt"),
    (Join-Path $secretRoot "vault-token.txt"),
    (Join-Path $secretRoot "keycloak-admin-password.txt"),
    (Join-Path $secretRoot "keycloak-test-password.txt"),
    (Join-Path $secretRoot "keycloak-service-client-secret.txt")
)
$missingSecrets = @($requiredSecretFiles | Where-Object { -not (Test-Path -LiteralPath $_ -PathType Leaf) })
if ($missingSecrets.Count -gt 0) { throw "누락 Secret 파일: $($missingSecrets -join ', ')" }
foreach ($path in $requiredSecretFiles) {
    if ((Get-Item -LiteralPath $path).Length -le 0) { throw "비어 있는 Secret 파일: $path" }
}

$requiredRuntimeFiles = @(
    "compose.yml", "compose.redis.yml", "compose.kafka.yml", "compose.integration.yml", "compose.tooling.yml",
    "cpf-env.ps1", "cpf-tooling.ps1", "initialize-integration-fixtures.ps1",
    "Dockerfile.full-toolchain", "Dockerfile.sftp-fixture", "sftp-entrypoint.sh",
    "toxiproxy.json", "otel-collector-config.yml"
)
$missingRuntimeFiles = @($requiredRuntimeFiles | Where-Object { -not (Test-Path -LiteralPath (Join-Path $cpfRoot $_) -PathType Leaf) })
if ($missingRuntimeFiles.Count -gt 0) { throw "누락 Runtime 파일: $($missingRuntimeFiles -join ', ')" }

Write-Host "Required Images: $($requiredImages.Count)/$($requiredImages.Count)"
Write-Host "Legacy Runner Images Preserved: $($legacyPresent.Count)/$($legacyImages.Count)"
Write-Host "Prepared Containers: $($containers.Count)/$($expectedContainers.Count)"
Write-Host "Running Containers: $($running.Count)"
Write-Host "Prepared Volumes: $($volumes.Count)/$($expectedVolumes.Count)"
Write-Host "Secret Files: $($requiredSecretFiles.Count)/$($requiredSecretFiles.Count)"
Write-Host "Restart Policy no: $($expectedContainers.Count)/$($expectedContainers.Count)"

if ($RequireStopped -and $running.Count -ne 0) {
    throw "정지 상태가 아닙니다: $($running -join ', ')"
}

Write-Host "CPF Docker 개발·테스트 전체 환경 상태 확인 완료" -ForegroundColor Green
