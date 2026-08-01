param([switch]$ConfirmReset)

$ErrorActionPreference = "Stop"
if (-not $ConfirmReset) {
    throw "데이터 초기화는 -ConfirmReset을 명시해야 실행됩니다."
}

$containerNames = @("cpf-mariadb","cpf-postgresql","cpf-oracle","cpf-redis","cpf-kafka")
$volumeNames = @("cpf-mariadb-data","cpf-postgresql-data","cpf-oracle-data","cpf-redis-data","cpf-kafka-data")

$existingContainers = @(docker ps -a --format "{{.Names}}")
foreach ($name in $containerNames) {
    if ($existingContainers -contains $name) {
        docker rm -f $name | Out-Host
        if ($LASTEXITCODE -ne 0) { throw "Container 삭제 실패: $name" }
    }
}

$existingVolumes = @(docker volume ls --format "{{.Name}}")
foreach ($name in $volumeNames) {
    if ($existingVolumes -contains $name) {
        docker volume rm $name | Out-Host
        if ($LASTEXITCODE -ne 0) { throw "Volume 삭제 실패: $name" }
    }
}

$networkExists = @(docker network ls --format "{{.Name}}" | Where-Object { $_ -eq "cpf_default" })
if ($networkExists.Count -gt 0) {
    $attached = [int](docker network inspect cpf_default --format '{{len .Containers}}')
    if ($attached -ne 0) {
        throw "cpf_default Network에 Container가 연결되어 있어 삭제하지 않습니다."
    }
    docker network rm cpf_default | Out-Host
    if ($LASTEXITCODE -ne 0) { throw "Network 삭제 실패: cpf_default" }
}

Write-Host "CPF 테스트 데이터 초기화 완료" -ForegroundColor Green
Write-Host "보존: 모든 Docker Image, Compose, Dockerfile, Script, Secret, Repository Source" -ForegroundColor Green
