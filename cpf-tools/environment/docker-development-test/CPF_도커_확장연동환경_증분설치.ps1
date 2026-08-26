[CmdletBinding()]
param([string]$DockerRoot = 'C:\dev\Docker')
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$sourceRoot = $PSScriptRoot
$cpfRoot = Join-Path $DockerRoot 'CPF'
$secretRoot = Join-Path $DockerRoot 'Secrets'
$runtimeEnv = Join-Path $secretRoot 'cpf-runtime.env'
$toolEnv = Join-Path $cpfRoot 'tool-images.env'
foreach ($path in @($cpfRoot,$secretRoot)) { if (-not (Test-Path -LiteralPath $path -PathType Container)) { throw "선행 전체 설치 경로가 없습니다: $path" } }
foreach ($path in @($runtimeEnv,$toolEnv)) { if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "선행 환경 파일이 없습니다: $path" } }
$files = @('compose.integration.yml','compose.tooling.yml','initialize-integration-fixtures.ps1','Dockerfile.sftp-fixture','sftp-entrypoint.sh','toxiproxy.json','otel-collector-config.yml','CPF_도커_확장연동환경_증분설치.ps1')
foreach ($name in $files) {
    $source = Join-Path $sourceRoot $name
    if (-not (Test-Path -LiteralPath $source -PathType Leaf)) { throw "확장 설치 Source 누락: $source" }
    Copy-Item -LiteralPath $source -Destination (Join-Path $cpfRoot $name) -Force
}
$compose = @('compose','--project-name','cpf','--env-file',$runtimeEnv,'--env-file',$toolEnv,
    '-f',(Join-Path $cpfRoot 'compose.yml'),'-f',(Join-Path $cpfRoot 'compose.redis.yml'),'-f',(Join-Path $cpfRoot 'compose.kafka.yml'),
    '-f',(Join-Path $cpfRoot 'compose.integration.yml'),'-f',(Join-Path $cpfRoot 'compose.tooling.yml'))
& docker @compose 'config' '--quiet'; if ($LASTEXITCODE -ne 0) { throw '확장 Compose config 검증 실패' }
& docker @compose 'create' 'wiremock' 'sftp' 'vault' 'keycloak' 'toxiproxy' 'otel-collector'; if ($LASTEXITCODE -ne 0) { throw '확장 Container prepare 실패' }
foreach ($name in @('cpf-wiremock','cpf-sftp','cpf-vault','cpf-keycloak','cpf-toxiproxy','cpf-otel-collector')) { & docker update --restart=no $name *> $null; if ($LASTEXITCODE -ne 0) { throw "restart=no 설정 실패: $name" } }
Write-Host 'CPF Docker 확장 연동 환경 증분 설치 완료 / Container Created-Stopped' -ForegroundColor Green
