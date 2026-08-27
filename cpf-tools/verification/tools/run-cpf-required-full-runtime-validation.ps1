[CmdletBinding()]
param(
    [string]$RepoRoot=(Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path,
    [string]$DockerRoot='C:\dev\Docker',
    [string]$DockerSecretFile='',
    [string]$OutputRoot=(Join-Path $HOME 'Downloads'),
    [string]$VsCodeProblemsJson=$env:CPF_VSCODE_PROBLEMS_JSON
)
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
if($PSVersionTable.PSVersion.Major -lt 7){throw 'PowerShell 7 이상(pwsh)이 필요합니다.'}
try{[Console]::InputEncoding=[Text.UTF8Encoding]::new($false);[Console]::OutputEncoding=[Text.UTF8Encoding]::new($false);$global:OutputEncoding=[Text.UTF8Encoding]::new($false)}catch{}
$env:PYTHONUTF8='1';$env:PYTHONIOENCODING='utf-8'
$RepoRoot=(Resolve-Path -LiteralPath $RepoRoot).Path
if([string]::IsNullOrWhiteSpace($DockerSecretFile)){$DockerSecretFile=Join-Path $DockerRoot 'Secrets\cpf-runtime.env'}
$stamp=Get-Date -Format 'yyyyMMdd_HHmmss';$log=Join-Path $OutputRoot "CPF_REQUIRED_FULL_RUNTIME_$stamp.log";New-Item -ItemType Directory -Force -Path $OutputRoot|Out-Null
function Require-Command([string]$Name){$c=Get-Command $Name -ErrorAction SilentlyContinue|Select-Object -First 1;if(-not$c){throw "필수 명령이 없습니다: $Name"};return $c.Source}
$java=Require-Command 'java';$python=Require-Command 'python';$docker=Require-Command 'docker';$node=Require-Command 'node';$npmCmd=(Get-Command npm.cmd -ErrorAction SilentlyContinue|Select-Object -First 1);if(-not$npmCmd){$npmCmd=Get-Command npm -ErrorAction Stop|Select-Object -First 1};$npm=[string]$npmCmd.Source
$javaText=(& $java -version 2>&1|Out-String);if($javaText -notmatch '(?m)version\s+"25\.|openjdk\s+version\s+"25\.'){throw "Java 25가 필요합니다. actual=$($javaText.Trim())"}
$nodeText=(& $node --version|Out-String).Trim();if($nodeText -notmatch '^v(?<major>\d+)\.(?<minor>\d+)'){throw "Node version parse failed: $nodeText"};if([int]$Matches.major -lt 22 -or ([int]$Matches.major -eq 22 -and [int]$Matches.minor -lt 18) -or [int]$Matches.major -ge 25){throw "Node >=22.18.0 <25가 필요합니다. actual=$nodeText"};$npmText=(& $npm --version|Out-String).Trim();if($npmText-ne'10.9.2'){throw "npm 10.9.2가 필요합니다. actual=$npmText"}
& $docker info *> $null;if($LASTEXITCODE -ne 0){throw 'Docker daemon이 실행 중이어야 합니다.'};if(-not(Test-Path -LiteralPath $DockerSecretFile -PathType Leaf)){throw "Docker Secret env가 없습니다: $DockerSecretFile"}
$identityJson=& $python (Join-Path $RepoRoot 'cpf-tools/verification/tools/cpf-source-state.py') --root $RepoRoot --scope source 2>&1|Out-String;$identity=$identityJson|ConvertFrom-Json;$sourceIdentity=[string]$identity.contentSha256
Write-Host "[CPF][FULL-RUNTIME] SourceIdentity=$sourceIdentity files=$($identity.fileCount)"
$runner=Join-Path $RepoRoot 'cpf-tools/verification/tools/run-cpf-local-full-validation.ps1'
$baseArgs=@('-NoProfile','-File',$runner,'-RepoRoot',$RepoRoot,'-OutputRoot',$OutputRoot,'-DockerRoot',$DockerRoot,'-DockerSecretFile',$DockerSecretFile,'-FullLocal','-IncludePerformanceLoad','-AllowDestructiveDbRollback','-StrictExit')
function Invoke-RequiredPass([string]$Label){$started=Get-Date;Write-Host "[CPF][FULL-RUNTIME][$Label] START=$($started.ToString('o'))";& pwsh @baseArgs 2>&1|Tee-Object -FilePath $log -Append;$rc=$LASTEXITCODE;$ended=Get-Date;Write-Host "[CPF][FULL-RUNTIME][$Label] END=$($ended.ToString('o')) EXIT_CODE=$rc";if($rc-ne0){throw "CPF REQUIRED FULL RUNTIME $Label FAIL exit=$rc log=$log"};$afterJson=& $python (Join-Path $RepoRoot 'cpf-tools/verification/tools/cpf-source-state.py') --root $RepoRoot --scope source 2>&1|Out-String;$after=$afterJson|ConvertFrom-Json;if([string]$after.contentSha256-ne$sourceIdentity){throw "Source drift after $Label expected=$sourceIdentity actual=$($after.contentSha256)"}}
Invoke-RequiredPass 'PRIMARY'
if([string]::IsNullOrWhiteSpace($VsCodeProblemsJson)){throw 'Fresh VS Code Problems JSON이 필요합니다. CPF_VSCODE_PROBLEMS_JSON 또는 -VsCodeProblemsJson으로 export 경로를 지정하세요.'}
$vscodeOut=Join-Path $OutputRoot "CPF_VSCODE_PROBLEMS_VERIFY_$stamp.json";& $python (Join-Path $RepoRoot 'cpf-tools/verification/tools/verify-cpf-vscode-problems.py') --input $VsCodeProblemsJson --output $vscodeOut 2>&1|Tee-Object -FilePath $log -Append;if($LASTEXITCODE-ne0){throw "VS Code Fresh Import Error/Warning gate failed: $vscodeOut"}
Invoke-RequiredPass 'FRESH_REPLAY'
Write-Host "[CPF][FULL-RUNTIME][PASS] SourceIdentity=$sourceIdentity PRIMARY+VSCODE_0_0+FRESH_REPLAY completed. LOG=$log" -ForegroundColor Green
