[CmdletBinding()]
param(
    [string]$DockerRoot = 'C:\dev\Docker',
    [string]$RepoRoot = 'C:\dev\projects\jck\202412_01_CPF',
    [switch]$SkipPull
)
$ErrorActionPreference='Stop'; Set-StrictMode -Version Latest
[Console]::OutputEncoding=[Text.UTF8Encoding]::new($false);$OutputEncoding=[Text.UTF8Encoding]::new($false)
function Invoke-DockerChecked { param([string[]]$Arguments); & docker @Arguments; if($LASTEXITCODE-ne0){throw "Docker 단계 실패(exit=$LASTEXITCODE)"} }
function New-Secret { param([string]$Path); if(Test-Path -LiteralPath $Path -PathType Leaf){if((Get-Item $Path).Length-le0){throw "비어 있는 Secret: $Path"};return};$b=New-Object byte[] 32;[Security.Cryptography.RandomNumberGenerator]::Fill($b);[IO.File]::WriteAllText($Path,([Convert]::ToHexString($b).ToLowerInvariant()+"`n"),[Text.UTF8Encoding]::new($false)) }
$sourceRoot=Split-Path -Parent $MyInvocation.MyCommand.Path;$cpfRoot=Join-Path $DockerRoot 'CPF';$secretRoot=Join-Path $DockerRoot 'Secrets'
if(-not(Test-Path -LiteralPath $RepoRoot -PathType Container)){throw "Repository가 없습니다: $RepoRoot"};docker version *> $null;if($LASTEXITCODE-ne0){throw 'Docker Desktop이 실행 중이 아닙니다.'}
New-Item -ItemType Directory -Force -Path $cpfRoot,$secretRoot|Out-Null
foreach($name in @('compose.integration.yml','Dockerfile.sftp-fixture','sftp-entrypoint.sh','initialize-integration-fixtures.ps1')){$src=Join-Path $sourceRoot $name;if(-not(Test-Path $src -PathType Leaf)){throw "Source 누락: $src"};Copy-Item $src (Join-Path $cpfRoot $name) -Force}
$srcFixtures=Join-Path $sourceRoot 'fixtures';$dstFixtures=Join-Path $cpfRoot 'fixtures';if(Test-Path $dstFixtures){Remove-Item $dstFixtures -Recurse -Force};Copy-Item $srcFixtures $dstFixtures -Recurse -Force
foreach($name in @('sftp-password.txt','vault-token.txt','keycloak-admin-password.txt','keycloak-test-password.txt','keycloak-service-client-secret.txt')){New-Secret (Join-Path $secretRoot $name)}
$runtimeEnv=Join-Path $secretRoot 'cpf-runtime.env';$toolEnv=Join-Path $cpfRoot 'tool-images.env';if(-not(Test-Path $runtimeEnv -PathType Leaf)){throw "Runtime env 누락: $runtimeEnv"};if(-not(Test-Path $toolEnv -PathType Leaf)){throw "Tool env 누락: $toolEnv"}
$tool=@{};Get-Content $toolEnv -Encoding UTF8|ForEach-Object{if($_-match '^([A-Z0-9_]+)=(.+)$'){$tool[$Matches[1]]=$Matches[2]}}
foreach($k in @('WIREMOCK_IMAGE','VAULT_IMAGE','KEYCLOAK_IMAGE','SFTP_FIXTURE_IMAGE')){if(-not$tool.ContainsKey($k)){throw "Tool image 누락: $k"}}
if(-not$SkipPull){foreach($k in @('WIREMOCK_IMAGE','VAULT_IMAGE','KEYCLOAK_IMAGE')){Invoke-DockerChecked @('pull',$tool[$k])}}
Invoke-DockerChecked @('build','-t',$tool['SFTP_FIXTURE_IMAGE'],'-f',(Join-Path $cpfRoot 'Dockerfile.sftp-fixture'),$cpfRoot)
$compose=@('compose','--project-name','cpf','--env-file',$runtimeEnv,'--env-file',$toolEnv,'-f',(Join-Path $cpfRoot 'compose.integration.yml'))
Invoke-DockerChecked ($compose+@('config','--quiet'));Invoke-DockerChecked ($compose+@('create','--force-recreate','wiremock','sftp','vault','keycloak'))
foreach($name in @('cpf-wiremock','cpf-sftp','cpf-vault','cpf-keycloak')){docker update --restart=no $name *> $null;if($LASTEXITCODE-ne0){throw "Restart Policy 설정 실패: $name"}}
Write-Host 'CPF Docker 확장 연동 환경 설치 완료 / Created-Stopped / restart=no' -ForegroundColor Green
