param(
    [string]$DockerRoot = "C:\dev\Docker",
    [switch]$RemoveVolumes,
    [switch]$RemoveSecrets,
    [switch]$IncludeIbmMq
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$cpfRoot = Join-Path $DockerRoot "CPF"
$secretRoot = Join-Path $DockerRoot "Secrets"
$containers = @("cpf-rabbitmq", "cpf-artemis", "cpf-tcp-simulator", "cpf-mailpit")
if ($IncludeIbmMq) { $containers += "cpf-ibm-mq" }
$running = @(docker ps --format "{{.Names}}" | Where-Object { $_ -in $containers })
if ($running.Count -gt 0) { throw "먼저 Container를 중지하세요: $($running -join ', ')" }

foreach ($name in $containers) {
    docker container inspect $name *> $null
    if ($LASTEXITCODE -eq 0) {
        & docker container rm $name
        if ($LASTEXITCODE -ne 0) { throw "Container 삭제 실패: $name" }
    }
}

if ($RemoveVolumes) {
    $volumes = @("cpf-rabbitmq-data", "cpf-artemis-data", "cpf-mailpit-data")
    if ($IncludeIbmMq) { $volumes += "cpf-ibm-mq-data" }
    foreach ($name in $volumes) {
        docker volume inspect $name *> $null
        if ($LASTEXITCODE -eq 0) {
            & docker volume rm $name
            if ($LASTEXITCODE -ne 0) { throw "Volume 삭제 실패: $name" }
        }
    }
}

if ($RemoveSecrets) {
    $secretFiles = @(
        (Join-Path $secretRoot "rabbitmq-password.txt"),
        (Join-Path $secretRoot "artemis-password.txt")
    )
    if ($IncludeIbmMq) {
        $secretFiles += (Join-Path $secretRoot "mqAdminPassword")
        $secretFiles += (Join-Path $secretRoot "mqAppPassword")
    }
    foreach ($path in $secretFiles) {
        if (Test-Path -LiteralPath $path -PathType Leaf) { Remove-Item -LiteralPath $path -Force }
    }
}

Write-Host "CPF QA39 Runtime 정확한 Cleanup 완료" -ForegroundColor Green
