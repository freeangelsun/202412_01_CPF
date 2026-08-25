[CmdletBinding()]
param(
    [string]$RepoRoot=(Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path,
    [string]$DockerRoot='C:\dev\Docker',
    [string]$DockerSecretFile='',
    [string]$OutputRoot=(Join-Path $HOME 'Downloads')
)
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
if($PSVersionTable.PSVersion.Major -lt 7){throw 'PowerShell 7 이상(pwsh)이 필요합니다.'}
$RepoRoot=(Resolve-Path -LiteralPath $RepoRoot).Path
if([string]::IsNullOrWhiteSpace($DockerSecretFile)){$DockerSecretFile=Join-Path $DockerRoot 'Secrets\cpf-runtime.env'}
$stamp=Get-Date -Format 'yyyyMMdd_HHmmss'
$log=Join-Path $OutputRoot "CPF_REQUIRED_FULL_RUNTIME_$stamp.log"
New-Item -ItemType Directory -Force -Path $OutputRoot|Out-Null
function Require-Command([string]$Name){$c=Get-Command $Name -ErrorAction SilentlyContinue|Select-Object -First 1;if(-not$c){throw "필수 명령이 없습니다: $Name"};return $c.Source}
$java=Require-Command 'java';$python=Require-Command 'python';$docker=Require-Command 'docker';$node=Require-Command 'node';$npm=(Get-Command npm.cmd -ErrorAction SilentlyContinue|Select-Object -First 1);if(-not$npm){$npm=Get-Command npm -ErrorAction Stop|Select-Object -First 1}
$javaText=(& $java -version 2>&1|Out-String);if($javaText -notmatch '(?m)version\s+"25\.|openjdk\s+version\s+"25\.'){throw "Java 25가 필요합니다. actual=$($javaText.Trim())"}
$nodeText=(& $node --version|Out-String).Trim();if($nodeText -notmatch '^v(?<major>\d+)\.(?<minor>\d+)'){throw "Node version parse failed: $nodeText"};if([int]$Matches.major -lt 22 -or ([int]$Matches.major -eq 22 -and [int]$Matches.minor -lt 18)){throw "Node 22.18+가 필요합니다. actual=$nodeText"}
& $docker info *> $null;if($LASTEXITCODE -ne 0){throw 'Docker daemon이 실행 중이어야 합니다.'}
if(-not(Test-Path -LiteralPath $DockerSecretFile -PathType Leaf)){throw "Docker Secret env가 없습니다: $DockerSecretFile"}
$identityJson=& $python (Join-Path $RepoRoot 'cpf-tools/verification/tools/cpf-source-state.py') 2>&1|Out-String
$identity=$identityJson|ConvertFrom-Json
Write-Host "[CPF][FULL-RUNTIME] SourceIdentity=$($identity.contentSha256) files=$($identity.fileCount)"
$started=Get-Date
$runner=Join-Path $RepoRoot 'cpf-tools/verification/tools/run-cpf-local-full-validation.ps1'
$args=@('-NoProfile','-File',$runner,'-RepoRoot',$RepoRoot,'-OutputRoot',$OutputRoot,'-DockerRoot',$DockerRoot,'-DockerSecretFile',$DockerSecretFile,'-FullLocal','-AllowDestructiveDbRollback','-StrictExit')
$rc=0
& pwsh @args 2>&1 | Tee-Object -FilePath $log
$rc=$LASTEXITCODE
$finished=Get-Date
Write-Host "[CPF][FULL-RUNTIME] START=$($started.ToString('o')) END=$($finished.ToString('o')) EXIT_CODE=$rc LOG=$log"
if($rc -ne 0){throw "CPF REQUIRED FULL RUNTIME FAIL exit=$rc log=$log"}
Write-Host '[CPF][FULL-RUNTIME][PASS] DB3 Fresh/Upgrade/Rollback-Reapply + Runtime Closure + Browser E2E + Side Effect gates completed.' -ForegroundColor Green
