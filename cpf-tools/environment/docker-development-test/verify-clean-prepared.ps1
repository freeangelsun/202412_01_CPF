$ErrorActionPreference = "Stop"

$requiredImages = @(
    "mariadb:12.3.2",
    "postgres:18.4-trixie",
    "container-registry.oracle.com/database/free:26ai-free-23.26.2.0.0",
    "redis:8.8.1-trixie",
    "apache/kafka:4.3.1"
)

$missing = @()
foreach ($image in $requiredImages) {
    docker image inspect $image *> $null
    if ($LASTEXITCODE -ne 0) { $missing += $image }
}
if ($missing.Count -gt 0) { throw "누락 Image: $($missing -join ', ')" }

$expectedContainers = @("cpf-mariadb","cpf-postgresql","cpf-oracle","cpf-redis","cpf-kafka")
$containers = @(docker ps -a --format "{{.Names}}" | Where-Object { $expectedContainers -contains $_ })
$running = @(docker ps --format "{{.Names}}" | Where-Object { $expectedContainers -contains $_ })
$volumes = @(docker volume ls --format "{{.Name}}" | Where-Object {
    $_ -in @("cpf-mariadb-data","cpf-postgresql-data","cpf-oracle-data","cpf-redis-data","cpf-kafka-data")
})

Write-Host "Base Images: 5/5"
Write-Host "Prepared Containers: $($containers.Count)/5"
Write-Host "Running Containers: $($running.Count)"
Write-Host "Prepared Volumes: $($volumes.Count)/5"

if ($containers.Count -ne 5 -or $running.Count -ne 0 -or $volumes.Count -ne 5) {
    throw "Clean Prepared 상태 불일치"
}
Write-Host "CPF Clean Prepared 상태 확인" -ForegroundColor Green
