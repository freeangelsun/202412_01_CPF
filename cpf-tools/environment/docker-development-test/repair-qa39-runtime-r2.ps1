param(
    [string]$DockerRoot = "C:\dev\Docker",
    [string]$RepoRoot = "C:\dev\projects\jck\202412_01_CPF",
    [string]$CommonPassword = "",
    [switch]$IncludeIbmMq,
    [switch]$ResetProviderData
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Write-Utf8NoBom { param([string]$Path,[string]$Content); $parent=Split-Path -Parent $Path; if($parent){New-Item -ItemType Directory -Path $parent -Force|Out-Null}; [IO.File]::WriteAllText($Path,$Content,[Text.UTF8Encoding]::new($false)) }
function Invoke-Docker { param([string[]]$Arguments); & docker @Arguments; if($LASTEXITCODE -ne 0){throw "docker $($Arguments -join ' ') 실패(exit=$LASTEXITCODE)"} }
function Read-Password { if(-not [string]::IsNullOrWhiteSpace($CommonPassword)){return $CommonPassword}; $s=Read-Host -Prompt "CPF 로컬 Docker 공통 비밀번호" -AsSecureString; $p=[Runtime.InteropServices.Marshal]::SecureStringToBSTR($s); try{return [Runtime.InteropServices.Marshal]::PtrToStringBSTR($p)}finally{[Runtime.InteropServices.Marshal]::ZeroFreeBSTR($p)} }

if(-not(Test-Path -LiteralPath $RepoRoot -PathType Container)){throw "Repository가 없습니다: $RepoRoot"}
docker version *> $null; if($LASTEXITCODE -ne 0){throw "Docker Desktop이 실행 중이 아닙니다."}
$password=Read-Password; if([string]::IsNullOrWhiteSpace($password) -or $password.Contains("`r") -or $password.Contains("`n")){throw "공통 비밀번호가 유효하지 않습니다."}
$sourceRoot=Split-Path -Parent $MyInvocation.MyCommand.Path; $cpfRoot=Join-Path $DockerRoot "CPF"; $secretRoot=Join-Path $DockerRoot "Secrets"
$copyNames=@("compose.qa39-runtime.yml","CPF_도커_QA39_Runtime_증분설치.ps1","repair-qa39-runtime-r2.ps1","start-qa39-runtime.ps1","verify-qa39-runtime.ps1","run-qa39-runtime-fault-smoke.ps1","run-qa39-runtime-validation.ps1","stop-qa39-runtime.ps1","cleanup-qa39-runtime.ps1","CPF_QA39_DOCKER_RUNTIME_MANIFEST.json")
foreach($name in $copyNames){$src=Join-Path $sourceRoot $name;if(-not(Test-Path -LiteralPath $src -PathType Leaf)){throw "수정 파일 누락: $src"};Copy-Item -LiteralPath $src -Destination (Join-Path $cpfRoot $name) -Force}
$qaContainers=@("cpf-rabbitmq","cpf-artemis","cpf-tcp-simulator","cpf-mailpit","cpf-ibm-mq");$support=@("cpf-wiremock","cpf-toxiproxy","cpf-otel-collector")
$running=@(docker ps --format "{{.Names}}"|Where-Object{$_ -in @($qaContainers+$support)});if($running.Count -gt 0){Invoke-Docker (@("stop")+$running)}
foreach($name in @($qaContainers+$support)){docker container inspect $name *> $null;if($LASTEXITCODE -eq 0){docker update --restart=no $name *> $null;if($LASTEXITCODE -ne 0){throw "Restart Policy 설정 실패: $name"}}}
foreach($name in @("rabbitmq-password.txt","artemis-password.txt","mqAdminPassword","mqAppPassword")){Write-Utf8NoBom -Path (Join-Path $secretRoot $name) -Content "$password`n"}
if($ResetProviderData){foreach($name in $qaContainers){docker container inspect $name *> $null;if($LASTEXITCODE -eq 0){Invoke-Docker @("container","rm",$name)}};$volumes=@("cpf-rabbitmq-data","cpf-artemis-data","cpf-mailpit-data");if($IncludeIbmMq){$volumes+="cpf-ibm-mq-data"};foreach($name in $volumes){docker volume inspect $name *> $null;if($LASTEXITCODE -eq 0){Invoke-Docker @("volume","rm",$name)}}}
$runtimeEnv=Join-Path $secretRoot "cpf-runtime.env";$toolEnv=Join-Path $cpfRoot "tool-images.env";$providerEnv=Join-Path $cpfRoot "qa39-provider-images.env";foreach($p in @($runtimeEnv,$toolEnv,$providerEnv)){if(-not(Test-Path -LiteralPath $p -PathType Leaf)){throw "환경파일 누락: $p"}}
$compose=@("compose","--project-name","cpf","--env-file",$runtimeEnv,"--env-file",$toolEnv,"--env-file",$providerEnv,"-f",(Join-Path $cpfRoot "compose.yml"),"-f",(Join-Path $cpfRoot "compose.redis.yml"),"-f",(Join-Path $cpfRoot "compose.kafka.yml"),"-f",(Join-Path $cpfRoot "compose.integration.yml"),"-f",(Join-Path $cpfRoot "compose.tooling.yml"),"-f",(Join-Path $cpfRoot "compose.qa39-runtime.yml"));$profile=if($IncludeIbmMq){@("--profile","ibm-mq")}else{@()}
Invoke-Docker ($compose+$profile+@("config","--quiet"));Invoke-Docker ($compose+$profile+@("create","--force-recreate","rabbitmq","artemis","tcp-simulator","mailpit"));if($IncludeIbmMq){Invoke-Docker ($compose+$profile+@("create","--force-recreate","ibm-mq"))};Invoke-Docker ($compose+$profile+@("create","--force-recreate","toxiproxy"))
foreach($name in @($qaContainers+$support)){docker container inspect $name *> $null;if($LASTEXITCODE -eq 0){docker update --restart=no $name *> $null}}
$verify=@("-NoProfile","-ExecutionPolicy","Bypass","-File",(Join-Path $cpfRoot "verify-qa39-runtime.ps1"),"-DockerRoot",$DockerRoot,"-RepoRoot",$RepoRoot,"-RequireStopped");if($IncludeIbmMq){$verify+="-IncludeIbmMq"};& pwsh @verify;if($LASTEXITCODE -ne 0){throw "수정 후 정지 상태 검증 실패(exit=$LASTEXITCODE)"}
Write-Host "Artemis 수정·Provider 재생성 완료 / 모든 QA39 Container 정지 / Restart Policy no" -ForegroundColor Green
