param(
    [string]$DockerRoot = "C:\dev\Docker",
    [switch]$RequireStopped
)

$ErrorActionPreference = "Stop"
$cpfRoot = Join-Path $DockerRoot "CPF"
$envFile = Join-Path $cpfRoot "tool-images.env"
if (-not (Test-Path -LiteralPath $envFile -PathType Leaf)) { throw "Tool Image 환경파일이 없습니다." }

$dynamic = @{}
Get-Content $envFile | ForEach-Object {
    if ($_ -match "^(?<k>[A-Z0-9_]+)=(?<v>.+)$") {
        $dynamic[$Matches.k] = $Matches.v
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
    $dynamic["FULL_TOOLCHAIN_IMAGE"]
)
$legacyImages = @(
    "cpf-playwright:1.62.0-node22.16.0",
    "cpf-playwright-runner:1.62.0-node22.16.0",
    "cpf-validation-runner:java25-node22-pwsh7.6.4-playwright1.62.0"
)
$legacyPresent = @()
foreach ($legacyImage in $legacyImages) {
    docker image inspect $legacyImage *> $null
    if ($LASTEXITCODE -eq 0) { $legacyPresent += $legacyImage }
}

$missing = @()
foreach ($image in $requiredImages) {
    docker image inspect $image *> $null
    if ($LASTEXITCODE -ne 0) { $missing += $image }
}
if ($missing.Count -gt 0) { throw "누락 Image: $($missing -join ', ')" }

$expectedContainers = @(
    "cpf-mariadb",
    "cpf-postgresql",
    "cpf-oracle",
    "cpf-redis",
    "cpf-kafka",
    "cpf-toxiproxy",
    "cpf-otel-collector"
)
$containers = @(docker ps -a --format "{{.Names}}" | Where-Object { $expectedContainers -contains $_ })
$running = @(docker ps --format "{{.Names}}" | Where-Object { $expectedContainers -contains $_ })
$volumes = @(docker volume ls --format "{{.Name}}" | Where-Object {
    $_ -in @(
        "cpf-mariadb-data",
        "cpf-postgresql-data",
        "cpf-oracle-data",
        "cpf-redis-data",
        "cpf-kafka-data"
    )
})

Write-Host "Required Images: $($requiredImages.Count)/$($requiredImages.Count)"
Write-Host "Legacy Runner Images Preserved: $($legacyPresent.Count)/3"
Write-Host "Prepared Containers: $($containers.Count)/7"
Write-Host "Running Containers: $($running.Count)"
Write-Host "Prepared Volumes: $($volumes.Count)/5"

if ($containers.Count -ne 7) { throw "Container 수 불일치" }
if ($volumes.Count -ne 5) { throw "Volume 수 불일치" }
if ($RequireStopped -and $running.Count -ne 0) {
    throw "정지 상태가 아닙니다: $($running -join ', ')"
}

Write-Host "CPF Docker 개발·테스트 환경 상태 확인 완료" -ForegroundColor Green
