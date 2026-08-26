param(
    [ValidateSet("status","prepare","up","restart","stop","logs")]
    [string]$Action = "status",
    [ValidateSet(
        "mariadb","postgresql","oracle","redis","kafka","infra",
        "wiremock","sftp","vault","identity","external",
        "batch-mariadb","batch-postgresql","batch-oracle",
        "integration-mariadb","integration-postgresql","integration-oracle","all"
    )]
    [string]$Target = "infra",
    [string]$SecretFile = "C:\dev\Docker\Secrets\cpf-runtime.env"
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$toolEnv = Join-Path $root "tool-images.env"
$composeFiles = @(
    (Join-Path $root "compose.yml"),
    (Join-Path $root "compose.redis.yml"),
    (Join-Path $root "compose.kafka.yml"),
    (Join-Path $root "compose.integration.yml")
)
foreach ($file in $composeFiles) {
    if (-not (Test-Path -LiteralPath $file -PathType Leaf)) { throw "Compose 파일이 없습니다: $file" }
}
if (-not (Test-Path -LiteralPath $SecretFile -PathType Leaf)) { throw "Secret 파일이 없습니다: $SecretFile" }
if (-not (Test-Path -LiteralPath $toolEnv -PathType Leaf)) { throw "Tool Image 환경파일이 없습니다: $toolEnv" }

$compose = @("compose", "--env-file", $SecretFile, "--env-file", $toolEnv)
foreach ($file in $composeFiles) { $compose += @("-f", $file) }

$external = @("wiremock", "sftp", "vault", "keycloak")
$groups = @{
    mariadb = @("mariadb")
    postgresql = @("postgresql")
    oracle = @("oracle")
    redis = @("redis")
    kafka = @("kafka")
    infra = @("redis", "kafka")
    wiremock = @("wiremock")
    sftp = @("sftp")
    vault = @("vault")
    identity = @("keycloak")
    external = $external
    "batch-mariadb" = @("mariadb")
    "batch-postgresql" = @("postgresql")
    "batch-oracle" = @("oracle")
    "integration-mariadb" = @("mariadb", "redis", "kafka") + $external
    "integration-postgresql" = @("postgresql", "redis", "kafka") + $external
    "integration-oracle" = @("oracle", "redis", "kafka") + $external
    all = @("mariadb", "postgresql", "oracle", "redis", "kafka") + $external
}

function Invoke-Compose([string[]]$Arguments) {
    & docker @compose @Arguments
    if ($LASTEXITCODE -ne 0) { throw "docker compose 실패(exit=$LASTEXITCODE)" }
}

$selected = @($groups[$Target])
switch ($Action) {
    "status" { Invoke-Compose @("ps", "-a") }
    "prepare" { Invoke-Compose @("create"); Invoke-Compose @("ps", "-a") }
    "up" { Invoke-Compose (@("up", "-d") + $selected); Invoke-Compose @("ps", "-a") }
    "restart" {
        Invoke-Compose (@("stop") + $selected)
        Invoke-Compose (@("up", "-d") + $selected)
        Invoke-Compose @("ps", "-a")
    }
    "stop" { Invoke-Compose (@("stop") + $selected); Invoke-Compose @("ps", "-a") }
    "logs" { Invoke-Compose (@("logs", "--tail", "300") + $selected) }
}
