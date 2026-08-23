param(
    [Parameter(Mandatory = $true)][string]$RepoRoot,
    [string]$RunName = 'F136-FRESH-RUNTIME-RERUN5',
    [string]$InstanceId = 'f136-runtime-rerun5'
)

$ErrorActionPreference = 'Stop'
$root = (Resolve-Path -LiteralPath $RepoRoot).Path
$runRoot = Join-Path $root "cpf-docs/work/evidence/codex/current/runtime/$RunName"
$logs = Join-Path $runRoot 'logs'
New-Item -ItemType Directory -Force -Path $logs | Out-Null
$stdout = Join-Path $runRoot 'stdout.log'
$stderr = Join-Path $runRoot 'stderr.log'
$resultPath = Join-Path $runRoot 'result.json'
$jar = Join-Path $root 'cpf-docs/work/evidence/generated/domain-generator/cpf-f132consumer/online/build/libs/cpf-f132consumer-online-1.0.0-SNAPSHOT.jar'
$secretPath = Join-Path $root 'cpf-docs/work/evidence/generated/runtime-secrets/f132consumer-db-password.tmp'
$password = (Get-Content -Raw -LiteralPath $secretPath).Trim()
$pepperBytes = [byte[]]::new(32)
[Security.Cryptography.RandomNumberGenerator]::Fill($pepperBytes)
$pepper = [Convert]::ToBase64String($pepperBytes)

$childEnvironment = @{
    SPRING_PROFILES_ACTIVE = 'prod'
    SERVER_PORT = '18732'
    FCS_DATASOURCE_URL = 'jdbc:postgresql://127.0.0.1:15432/f132db'
    FCS_DATASOURCE_USERNAME = 'cpf_f132_runtime'
    FCS_DATASOURCE_PASSWORD = $password
    FCS_DATASOURCE_DRIVER = 'org.postgresql.Driver'
    CPF_PASSWORD_PEPPER = $pepper
    CPF_LOG_ROOT = $logs
    CPF_RUNTIME_INSTANCE_ID = $InstanceId
}
$arguments = @(
    '-Xms250m', '-Xmx500m',
    "-XX:ErrorFile=$runRoot\java-hs_err_pid%p.log",
    '-XX:+HeapDumpOnOutOfMemoryError',
    "-XX:HeapDumpPath=$runRoot",
    '-jar', $jar
)
$process = Start-Process `
    -FilePath 'C:\dev\java\jdk-25.0.3.9-hotspot\bin\java.exe' `
    -ArgumentList $arguments `
    -WorkingDirectory $root `
    -RedirectStandardOutput $stdout `
    -RedirectStandardError $stderr `
    -Environment $childEnvironment `
    -PassThru `
    -WindowStyle Hidden

$pidValue = $process.Id
$healthStatus = 'NOT_REACHED'
$healthBody = ''
$deadline = (Get-Date).AddSeconds(60)
while ((Get-Date) -lt $deadline) {
    $process.Refresh()
    if ($process.HasExited) { break }
    try {
        $response = Invoke-WebRequest -Uri 'http://127.0.0.1:18732/actuator/health' -TimeoutSec 2 -UseBasicParsing
        $healthStatus = [string]$response.StatusCode
        $healthBody = $response.Content
        break
    } catch {
        Start-Sleep -Milliseconds 500
    }
}
$wasRunning = -not $process.HasExited
if ($wasRunning) {
    Stop-Process -Id $pidValue -Force
    $process.WaitForExit(10000) | Out-Null
}
$process.Refresh()
$combined = (Get-Content -Raw -LiteralPath $stdout -ErrorAction SilentlyContinue) + "`n" +
        (Get-Content -Raw -LiteralPath $stderr -ErrorAction SilentlyContinue)
$result = [ordered]@{
    executedAt = [DateTimeOffset]::Now.ToString('o')
    pid = $pidValue
    exitCode = if ($wasRunning) { 'STOPPED_AFTER_PROBE' } else { $process.ExitCode }
    healthStatus = $healthStatus
    healthBody = $healthBody
    driverMissing = $combined.Contains('Cannot load driver class')
    sessionAutoConfiguration = $combined.Contains('CpfServerSessionSecurityAutoConfiguration')
    credentialKeyMissing = $combined.Contains('CPF_BFF_CREDENTIAL_KEY is required')
    domainDataSourceCreated = $combined.Contains('cpfDomainDataSource')
    domainSqlSessionFactoryCreated = $combined.Contains('cpfDomainSqlSessionFactory')
    hikariStarted = $combined.Contains('HikariPool-1 - Start completed')
    operationCatalogRegistryUnavailable = $combined.Contains('CPF_OPERATION_CATALOG_REGISTRY_UNAVAILABLE')
    started = $combined.Contains('Started F132consumerOnlineApplication')
    loggingReady = $combined.Contains('CPF_LOGGING_READY')
    stdout = "cpf-docs/work/evidence/codex/current/runtime/$RunName/stdout.log"
    stderr = "cpf-docs/work/evidence/codex/current/runtime/$RunName/stderr.log"
}
$result | ConvertTo-Json -Depth 5 | Set-Content -LiteralPath $resultPath -Encoding utf8
$result | ConvertTo-Json -Depth 5
