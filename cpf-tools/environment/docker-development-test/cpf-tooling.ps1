param(
    [ValidateSet("status", "up", "stop", "logs", "reset-faults")]
    [string]$Action = "status",
    [ValidateSet(
        "fault-mariadb",
        "fault-postgresql",
        "fault-oracle",
        "fault-infra",
        "fault-external",
        "observability",
        "tools",
        "all"
    )]
    [string]$Target = "tools",
    [string]$SecretFile = "C:\dev\Docker\Secrets\cpf-runtime.env"
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$files = @(
    "compose.yml",
    "compose.redis.yml",
    "compose.kafka.yml",
    "compose.integration.yml",
    "compose.tooling.yml",
    "tool-images.env"
)
foreach ($name in $files) {
    if (-not (Test-Path -LiteralPath (Join-Path $root $name) -PathType Leaf)) {
        throw "필수 파일이 없습니다: $name"
    }
}
if (-not (Test-Path -LiteralPath $SecretFile -PathType Leaf)) { throw "Secret 파일이 없습니다: $SecretFile" }

$compose = @(
    "compose",
    "--env-file", $SecretFile,
    "--env-file", (Join-Path $root "tool-images.env"),
    "-f", (Join-Path $root "compose.yml"),
    "-f", (Join-Path $root "compose.redis.yml"),
    "-f", (Join-Path $root "compose.kafka.yml"),
    "-f", (Join-Path $root "compose.integration.yml"),
    "-f", (Join-Path $root "compose.tooling.yml")
)

$groups = @{
    "fault-mariadb" = @("mariadb", "kafka", "toxiproxy")
    "fault-postgresql" = @("postgresql", "kafka", "toxiproxy")
    "fault-oracle" = @("oracle", "kafka", "toxiproxy")
    "fault-infra" = @("redis", "kafka", "toxiproxy")
    "fault-external" = @("wiremock", "sftp", "vault", "keycloak", "toxiproxy")
    "observability" = @("otel-collector")
    "tools" = @("toxiproxy", "otel-collector")
    "all" = @(
        "mariadb", "postgresql", "oracle", "redis", "kafka",
        "wiremock", "sftp", "vault", "keycloak",
        "toxiproxy", "otel-collector"
    )
}

function Invoke-Compose {
    param([string[]]$Arguments)
    & docker @compose @Arguments
    if ($LASTEXITCODE -ne 0) { throw "docker compose 실패(exit=$LASTEXITCODE)" }
}

switch ($Action) {
    "status" { Invoke-Compose @("ps", "-a") }
    "up" { Invoke-Compose (@("up", "-d") + $groups[$Target]); Invoke-Compose @("ps", "-a") }
    "stop" { Invoke-Compose (@("stop") + $groups[$Target]); Invoke-Compose @("ps", "-a") }
    "logs" { Invoke-Compose (@("logs", "--tail", "300") + $groups[$Target]) }
    "reset-faults" {
        # Toxiproxy 2.9+ 는 브라우저형 User-Agent를 403 "User agent not allowed"로 거부한다.
        # PowerShell 기본 User-Agent가 "Mozilla/5.0 ... PowerShell/7.x" 이므로 명시 지정한다.
        Invoke-RestMethod -Method Post -Uri "http://127.0.0.1:8474/reset" -UserAgent 'CPF-Runtime-Verifier' | Out-Null
        Write-Host "Toxiproxy 장애 조건 초기화 완료" -ForegroundColor Green
    }
}
