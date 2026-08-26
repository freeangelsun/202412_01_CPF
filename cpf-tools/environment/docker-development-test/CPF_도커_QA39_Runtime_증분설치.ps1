[CmdletBinding()]
param(
    [string]$DockerRoot = 'C:\dev\Docker',
    [string]$RepoRoot = 'C:\dev\projects\jck\202412_01_CPF',
    [switch]$IncludeIbmMq
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$sourceRoot = $PSScriptRoot
$cpfRoot = Join-Path $DockerRoot 'CPF'
$secretRoot = Join-Path $DockerRoot 'Secrets'
foreach ($path in @($cpfRoot,$secretRoot)) { if (-not (Test-Path -LiteralPath $path -PathType Container)) { throw "선행 Docker 경로가 없습니다: $path" } }
$runtimeEnv = Join-Path $secretRoot 'cpf-runtime.env'
$toolEnv = Join-Path $cpfRoot 'tool-images.env'
foreach ($path in @($runtimeEnv,$toolEnv)) { if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "선행 환경 파일 누락: $path" } }
$files = @('compose.qa39-runtime.yml','CPF_QA39_DOCKER_RUNTIME_MANIFEST.json','start-qa39-runtime.ps1','stop-qa39-runtime.ps1','cleanup-qa39-runtime.ps1','verify-qa39-runtime.ps1','run-qa39-runtime-validation.ps1','run-qa39-runtime-fault-smoke.ps1','repair-qa39-runtime-r2.ps1','repair-qa39-runtime-r3.ps1','CPF_도커_QA39_Runtime_증분설치.ps1')
foreach ($name in $files) {
    $source = Join-Path $sourceRoot $name
    if (-not (Test-Path -LiteralPath $source -PathType Leaf)) { throw "QA39 Runtime Source 누락: $source" }
    Copy-Item -LiteralPath $source -Destination (Join-Path $cpfRoot $name) -Force
}
$providerEnv = Join-Path $cpfRoot 'qa39-provider-images.env'
if (-not (Test-Path -LiteralPath $providerEnv -PathType Leaf)) {
    [IO.File]::WriteAllLines($providerEnv, @(
        'RABBITMQ_IMAGE=rabbitmq:4.1.8-management',
        'ARTEMIS_IMAGE=apache/artemis:2.55.0',
        'IBM_MQ_IMAGE=icr.io/ibm-messaging/mq:9.4.5.1-r1',
        'PYTHON_FIXTURE_IMAGE=python:3.13.14-alpine3.24',
        'MAILPIT_IMAGE=axllent/mailpit:v1.30.0'
    ), [Text.UTF8Encoding]::new($false))
}
# QA39 secrets are never echoed and are never overwritten. Existing operator-owned values win.
$secretNames = @('rabbitmq-password.txt','artemis-password.txt','mqAdminPassword','mqAppPassword')
foreach ($name in $secretNames) {
    $path = Join-Path $secretRoot $name
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        $bytes = New-Object byte[] 32; [Security.Cryptography.RandomNumberGenerator]::Fill($bytes)
        [IO.File]::WriteAllText($path, [Convert]::ToBase64String($bytes), [Text.UTF8Encoding]::new($false))
    }
}
$runtimeText = [IO.File]::ReadAllText($runtimeEnv,[Text.UTF8Encoding]::new($false))
$defaults = [ordered]@{CPF_RABBITMQ_USER='cpf';CPF_RABBITMQ_VHOST='/';CPF_ARTEMIS_USER='cpf';CPF_IBM_MQ_QMGR='QM1'}
$append = @()
foreach ($key in $defaults.Keys) { if ($runtimeText -notmatch "(?m)^$([regex]::Escape($key))=") { $append += "$key=$($defaults[$key])" } }
if ($append.Count -gt 0) { [IO.File]::AppendAllText($runtimeEnv, (($append -join "`n") + "`n"), [Text.UTF8Encoding]::new($false)) }
$compose = @('compose','--project-name','cpf','--env-file',$runtimeEnv,'--env-file',$toolEnv,'--env-file',$providerEnv,
    '-f',(Join-Path $cpfRoot 'compose.yml'),'-f',(Join-Path $cpfRoot 'compose.redis.yml'),'-f',(Join-Path $cpfRoot 'compose.kafka.yml'),
    '-f',(Join-Path $cpfRoot 'compose.integration.yml'),'-f',(Join-Path $cpfRoot 'compose.tooling.yml'),'-f',(Join-Path $cpfRoot 'compose.qa39-runtime.yml'))
$profile = if ($IncludeIbmMq) { @('--profile','ibm-mq') } else { @() }
& docker @compose @profile 'config' '--quiet'; if ($LASTEXITCODE -ne 0) { throw 'QA39 Compose config 검증 실패' }
$services = @('rabbitmq','artemis','tcp-simulator','mailpit','wiremock','toxiproxy','otel-collector'); if ($IncludeIbmMq) { $services += 'ibm-mq' }
& docker @compose @profile 'create' '--force-recreate' @services; if ($LASTEXITCODE -ne 0) { throw 'QA39 Container prepare 실패' }
$containers = @('cpf-rabbitmq','cpf-artemis','cpf-tcp-simulator','cpf-mailpit','cpf-wiremock','cpf-toxiproxy','cpf-otel-collector'); if ($IncludeIbmMq) { $containers += 'cpf-ibm-mq' }
foreach ($name in $containers) { & docker update --restart=no $name *> $null; if ($LASTEXITCODE -ne 0) { throw "restart=no 설정 실패: $name" } }
$verifyArgs = @('-NoProfile','-File',(Join-Path $cpfRoot 'verify-qa39-runtime.ps1'),'-DockerRoot',$DockerRoot,'-RepoRoot',$RepoRoot,'-RequireStopped'); if ($IncludeIbmMq) { $verifyArgs += '-IncludeIbmMq' }
& pwsh @verifyArgs; if ($LASTEXITCODE -ne 0) { throw 'QA39 정지 상태 검증 실패' }
Write-Host 'CPF QA39 Runtime 증분 설치 완료 / Container Created-Stopped' -ForegroundColor Green
