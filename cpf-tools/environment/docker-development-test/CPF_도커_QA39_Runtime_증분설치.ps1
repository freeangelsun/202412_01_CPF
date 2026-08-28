[CmdletBinding()]
param(
    [string]$DockerRoot='C:\dev\Docker',
    [string]$RepoRoot='C:\dev\projects\jck\202412_01_CPF',
    [switch]$IncludeIbmMq,
    [switch]$SkipPull
)
$ErrorActionPreference='Stop';Set-StrictMode -Version Latest
[Console]::OutputEncoding=[Text.UTF8Encoding]::new($false);$OutputEncoding=[Text.UTF8Encoding]::new($false)
function Invoke-DockerChecked{param([string[]]$Arguments);& docker @Arguments;if($LASTEXITCODE-ne0){throw "Docker 단계 실패(exit=$LASTEXITCODE)"}}
function New-Secret{param([string]$Path);if(Test-Path $Path -PathType Leaf){if((Get-Item $Path).Length-le0){throw "비어 있는 Secret: $Path"};return};$b=New-Object byte[] 32;[Security.Cryptography.RandomNumberGenerator]::Fill($b);[IO.File]::WriteAllText($Path,([Convert]::ToHexString($b).ToLowerInvariant()+"`n"),[Text.UTF8Encoding]::new($false))}
$sourceRoot=Split-Path -Parent $MyInvocation.MyCommand.Path;$cpfRoot=Join-Path $DockerRoot 'CPF';$secretRoot=Join-Path $DockerRoot 'Secrets'
if(-not(Test-Path $RepoRoot -PathType Container)){throw "Repository가 없습니다: $RepoRoot"};docker version *> $null;if($LASTEXITCODE-ne0){throw 'Docker Desktop이 실행 중이 아닙니다.'};New-Item -ItemType Directory -Force -Path $cpfRoot,$secretRoot|Out-Null
foreach($name in @('compose.qa39-runtime.yml','CPF_도커_QA39_Runtime_증분설치.ps1','verify-qa39-runtime.ps1','start-qa39-runtime.ps1','stop-qa39-runtime.ps1','cleanup-qa39-runtime.ps1','run-qa39-runtime-validation.ps1','run-qa39-runtime-fault-smoke.ps1','repair-qa39-runtime-r3.ps1','CPF_QA39_DOCKER_RUNTIME_MANIFEST.json')){$src=Join-Path $sourceRoot $name;if(-not(Test-Path $src -PathType Leaf)){throw "Source 누락: $src"};Copy-Item $src (Join-Path $cpfRoot $name) -Force}
$tcpSrc=Join-Path $sourceRoot 'fixtures\tcp';$tcpDst=Join-Path $cpfRoot 'fixtures\tcp';New-Item -ItemType Directory -Force -Path (Split-Path $tcpDst -Parent)|Out-Null;if(Test-Path $tcpDst){Remove-Item $tcpDst -Recurse -Force};Copy-Item $tcpSrc $tcpDst -Recurse -Force
foreach($name in @('rabbitmq-password.txt','artemis-password.txt')){New-Secret (Join-Path $secretRoot $name)};if($IncludeIbmMq){New-Secret (Join-Path $secretRoot 'mqAdminPassword');New-Secret (Join-Path $secretRoot 'mqAppPassword')}
$provider=[ordered]@{RABBITMQ_IMAGE='rabbitmq:4.1.3-management';ARTEMIS_IMAGE='apache/activemq-artemis:2.41.0-alpine';IBM_MQ_IMAGE='icr.io/ibm-messaging/mq:9.4.3.0-r1';PYTHON_FIXTURE_IMAGE='python:3.13-alpine';MAILPIT_IMAGE='axllent/mailpit:v1.27.8'}
[IO.File]::WriteAllLines((Join-Path $cpfRoot 'qa39-provider-images.env'),@($provider.Keys|ForEach-Object{"$_=$($provider[$_])"}),[Text.UTF8Encoding]::new($false))
if(-not$SkipPull){foreach($k in @('RABBITMQ_IMAGE','ARTEMIS_IMAGE','PYTHON_FIXTURE_IMAGE','MAILPIT_IMAGE')){Invoke-DockerChecked @('pull',$provider[$k])};if($IncludeIbmMq){Invoke-DockerChecked @('pull',$provider.IBM_MQ_IMAGE)}}
$runtimeEnv=Join-Path $secretRoot 'cpf-runtime.env';$toolEnv=Join-Path $cpfRoot 'tool-images.env';$providerEnv=Join-Path $cpfRoot 'qa39-provider-images.env';foreach($p in @($runtimeEnv,$toolEnv,$providerEnv)){if(-not(Test-Path $p -PathType Leaf)){throw "환경파일 누락: $p"}}
$compose=@('compose','--project-name','cpf','--env-file',$runtimeEnv,'--env-file',$toolEnv,'--env-file',$providerEnv,'-f',(Join-Path $cpfRoot 'compose.integration.yml'),'-f',(Join-Path $cpfRoot 'compose.tooling.yml'),'-f',(Join-Path $cpfRoot 'compose.qa39-runtime.yml'))
$profile=if($IncludeIbmMq){@('--profile','ibm-mq')}else{@()};Invoke-DockerChecked ($compose+$profile+@('config','--quiet'))
$services=@('rabbitmq','artemis','tcp-simulator','mailpit');if($IncludeIbmMq){$services+='ibm-mq'};Invoke-DockerChecked ($compose+$profile+@('create','--force-recreate')+$services)
$names=@('cpf-rabbitmq','cpf-artemis','cpf-tcp-simulator','cpf-mailpit');if($IncludeIbmMq){$names+='cpf-ibm-mq'};foreach($name in $names){docker update --restart=no $name *> $null;if($LASTEXITCODE-ne0){throw "Restart Policy 설정 실패: $name"}}
& (Join-Path $cpfRoot 'verify-qa39-runtime.ps1') -DockerRoot $DockerRoot -RepoRoot $RepoRoot -RequireStopped -IncludeIbmMq:$IncludeIbmMq
if($LASTEXITCODE-ne0){throw "QA39 Runtime 설치 검증 실패(exit=$LASTEXITCODE)"};Write-Host 'CPF QA39 Runtime 증분 설치 완료 / Created-Stopped / restart=no' -ForegroundColor Green
