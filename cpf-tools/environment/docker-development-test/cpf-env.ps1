param(
    [ValidateSet("status","prepare","up","restart","stop","logs")]
    [string]$Action = "status",
    [ValidateSet("mariadb","postgresql","oracle","redis","kafka","infra","batch-mariadb","batch-postgresql","batch-oracle","all")]
    [string]$Target = "infra",
    [string]$SecretFile = "C:\dev\Docker\Secrets\cpf-runtime.env"
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$base = Join-Path $root "compose.yml"
$redis = Join-Path $root "compose.redis.yml"
$kafka = Join-Path $root "compose.kafka.yml"
$compose = @("compose","--env-file",$SecretFile,"-f",$base,"-f",$redis,"-f",$kafka)

$groups = @{
    mariadb = @("mariadb")
    postgresql = @("postgresql")
    oracle = @("oracle")
    redis = @("redis")
    kafka = @("kafka")
    infra = @("redis","kafka")
    "batch-mariadb" = @("mariadb","kafka")
    "batch-postgresql" = @("postgresql","kafka")
    "batch-oracle" = @("oracle","kafka")
    all = @("mariadb","postgresql","oracle","redis","kafka")
}

function Invoke-Compose([string[]]$Arguments) {
    & docker @compose @Arguments
    if ($LASTEXITCODE -ne 0) {
        throw "docker compose 실패(exit=$LASTEXITCODE)"
    }
}

if (!(Test-Path -LiteralPath $SecretFile)) {
    throw "Secret file이 없습니다: $SecretFile"
}

switch ($Action) {
    "status" { Invoke-Compose @("ps","-a") }
    "prepare" { Invoke-Compose @("create"); Invoke-Compose @("ps","-a") }
    "stop" { Invoke-Compose @("stop"); Invoke-Compose @("ps","-a") }
    "logs" { Invoke-Compose @("logs","--tail","200") }
    "restart" {
        Invoke-Compose @("stop")
        Invoke-Compose (@("up","-d") + $groups[$Target])
        Invoke-Compose @("ps","-a")
    }
    "up" {
        Invoke-Compose @("stop")
        Invoke-Compose (@("up","-d") + $groups[$Target])
        Invoke-Compose @("ps","-a")
    }
}
