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
try{[Console]::InputEncoding=[Text.UTF8Encoding]::new($false);[Console]::OutputEncoding=[Text.UTF8Encoding]::new($false);$OutputEncoding=[Text.UTF8Encoding]::new($false);$global:OutputEncoding=$OutputEncoding}catch{}
$env:PYTHONUTF8='1';$env:PYTHONIOENCODING='utf-8'
$RepoRoot=(Resolve-Path -LiteralPath $RepoRoot).Path
if([string]::IsNullOrWhiteSpace($DockerSecretFile)){$DockerSecretFile=Join-Path $DockerRoot 'Secrets\cpf-runtime.env'}
$stamp=Get-Date -Format 'yyyyMMdd_HHmmss';$log=Join-Path $OutputRoot "CPF_REQUIRED_FULL_RUNTIME_$stamp.log";New-Item -ItemType Directory -Force -Path $OutputRoot|Out-Null
function Require-Command([string]$Name){$c=Get-Command $Name -ErrorAction SilentlyContinue|Select-Object -First 1;if(-not$c){throw "필수 명령이 없습니다: $Name"};return $c.Source}
$java=Require-Command 'java';$javac=Require-Command 'javac';$python=Require-Command 'python';$docker=Require-Command 'docker';$node=Require-Command 'node';$npmCmd=(Get-Command npm.cmd -ErrorAction SilentlyContinue|Select-Object -First 1);if(-not$npmCmd){$npmCmd=Get-Command npm -ErrorAction Stop|Select-Object -First 1};$npm=[string]$npmCmd.Source
$toolchainPolicyPath=Join-Path $RepoRoot 'cpf-tools\verification\contracts\cpf-toolchain-compatibility.json';if(-not(Test-Path -LiteralPath $toolchainPolicyPath -PathType Leaf)){throw "Toolchain compatibility policy missing: $toolchainPolicyPath"};$toolchainPolicy=Get-Content -LiteralPath $toolchainPolicyPath -Raw -Encoding UTF8|ConvertFrom-Json;if([string]$toolchainPolicy.policy-ne'CAPABILITY_FIRST'){throw "Unsupported toolchain policy: $($toolchainPolicy.policy)"}
$javaText=(& $java --version 2>&1|Out-String).Trim();$javacText=(& $javac --version 2>&1|Out-String).Trim()
$javaProbeRoot=Join-Path ([IO.Path]::GetTempPath()) ("cpf-java25-capability-{0}-{1}" -f $PID,[guid]::NewGuid().ToString('N'))
try {
    [IO.Directory]::CreateDirectory($javaProbeRoot)|Out-Null
    $javaProbeSource=Join-Path $javaProbeRoot 'CpfJava25CapabilityProbe.java'
    [IO.File]::WriteAllText($javaProbeSource,'public final class CpfJava25CapabilityProbe { public static void main(String[] args) { System.out.print("CPF_JAVA25_CAPABILITY=PASS"); } }',[Text.UTF8Encoding]::new($false))
    & $javac --release 25 -d $javaProbeRoot $javaProbeSource *> $null
    if($LASTEXITCODE-ne0){throw "설치된 JDK가 CPF Java 25 target 컴파일 capability(javac --release 25)를 제공하지 않습니다. java=$javaText javac=$javacText"}
    $probeOut=(& $java -cp $javaProbeRoot CpfJava25CapabilityProbe 2>&1|Out-String).Trim()
    if($LASTEXITCODE-ne0 -or $probeOut-ne'CPF_JAVA25_CAPABILITY=PASS'){throw "설치된 Java Runtime이 Java 25 target class 실행 capability를 제공하지 않습니다. java=$javaText javac=$javacText result=$probeOut"}
} finally { if(Test-Path -LiteralPath $javaProbeRoot){Remove-Item -LiteralPath $javaProbeRoot -Recurse -Force -ErrorAction SilentlyContinue} }
$nodeText=(& $node --version|Out-String).Trim();if($nodeText -notmatch '^v(?<major>\d+)\.(?<minor>\d+)\.(?<patch>\d+)'){throw "Node version parse failed: $nodeText"};$actualNode=[version]("$($Matches.major).$($Matches.minor).$($Matches.patch)");$nodeFloor=[version][string]$toolchainPolicy.tools.node.compatibilityFloor;if($actualNode-lt$nodeFloor){Write-Warning "Node $nodeText는 dependency engine 안내선 $nodeFloor보다 낮지만 버전만으로 차단하지 않고 실제 capability/npm lifecycle을 실행합니다."};& $node --input-type=module -e 'if(typeof fetch!=="function")process.exit(3); await Promise.resolve();' *> $null;if($LASTEXITCODE-ne0){throw "설치된 Node가 CPF Frontend에 필요한 ESM/fetch capability를 제공하지 않습니다. actual=$nodeText"}
$npmText=(& $npm --version|Out-String).Trim();if($npmText -notmatch '^(?<major>\d+)(?:\.|$)'){throw "npm version parse failed: $npmText"};$npmFloor=[int]$toolchainPolicy.tools.npm.compatibilityFloorMajor;if([int]$Matches.major-lt$npmFloor){Write-Warning "npm $npmText는 권장 major $npmFloor보다 낮지만 버전만으로 차단하지 않고 npm ci capability를 실행합니다."};& $npm ci --help *> $null;if($LASTEXITCODE-ne0){throw "설치된 npm이 npm ci capability를 제공하지 않습니다. actual=$npmText"};Write-Host "[CPF][사전조건] policy=CAPABILITY_FIRST node=$nodeText npm=$npmText pwsh=$($PSVersionTable.PSVersion)"
& $docker info *> $null;if($LASTEXITCODE -ne 0){throw 'Docker daemon이 실행 중이어야 합니다.'};if(-not(Test-Path -LiteralPath $DockerSecretFile -PathType Leaf)){throw "Docker Secret env가 없습니다: $DockerSecretFile"}
$identityJson=& $python (Join-Path $RepoRoot 'cpf-tools/verification/tools/cpf-source-state.py') --root $RepoRoot --scope source 2>&1|Out-String;$identity=$identityJson|ConvertFrom-Json;$sourceIdentity=[string]$identity.contentSha256
Write-Host "[CPF][FULL-RUNTIME] SourceIdentity=$sourceIdentity files=$($identity.fileCount)"
$runner=Join-Path $RepoRoot 'cpf-tools/verification/tools/run-cpf-local-full-validation.ps1'
$runnerArgs=@{
    RepoRoot=$RepoRoot
    OutputRoot=$OutputRoot
    DockerRoot=$DockerRoot
    DockerSecretFile=$DockerSecretFile
    FullLocal=$true
    IncludePerformanceLoad=$true
    AllowDestructiveDbRollback=$true
    StrictExit=$true
}
$javaUtf8Options='-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8'
if([string]::IsNullOrWhiteSpace($env:JAVA_TOOL_OPTIONS)){$env:JAVA_TOOL_OPTIONS=$javaUtf8Options}elseif($env:JAVA_TOOL_OPTIONS -notmatch '(?:^|\s)-Dfile\.encoding='){$env:JAVA_TOOL_OPTIONS=($env:JAVA_TOOL_OPTIONS.Trim()+' '+$javaUtf8Options)}
function Assert-NoMojibake([string]$Path,[string]$Label){$text=Get-Content -LiteralPath $Path -Raw -Encoding UTF8;foreach($literal in @([char]0xFFFD,'占쏙옙','?ㅽ뙣','?꾨즺','?먮뒗','?덉뒿','?덈떎','湲곕낯','?낅Т','?〓떒')){if($text.Contains([string]$literal)){throw "UTF-8 mojibake detected after $Label marker=$literal log=$Path"}}}
function Invoke-RequiredPass([string]$Label){
    $started=Get-Date
    Write-Host "[CPF][FULL-RUNTIME][$Label] START=$($started.ToString('o'))"
    $failure=$null
    try {
        # Invoke the FullLocal script in the current pwsh 7 process. This removes the extra native stdout
        # decode boundary that previously turned UTF-8 Korean text into CP949 mojibake before Tee-Object.
        & $runner @runnerArgs *>&1 | Tee-Object -FilePath $log -Append | Out-Host
    } catch {
        $failure=$_
        ($_ | Out-String) | Tee-Object -FilePath $log -Append | Out-Host
    }
    $ended=Get-Date
    Write-Host "[CPF][FULL-RUNTIME][$Label] END=$($ended.ToString('o')) RESULT=$(if($null-eq$failure){'PASS'}else{'FAIL'})"
    Assert-NoMojibake $log $Label
    if($null-ne$failure){throw "CPF REQUIRED FULL RUNTIME $Label FAIL log=$log cause=$($failure.Exception.Message)"}
    $afterJson=& $python (Join-Path $RepoRoot 'cpf-tools/verification/tools/cpf-source-state.py') --root $RepoRoot --scope source 2>&1|Out-String
    $after=$afterJson|ConvertFrom-Json
    if([string]$after.contentSha256-ne$sourceIdentity){throw "Source drift after $Label expected=$sourceIdentity actual=$($after.contentSha256)"}
}
Invoke-RequiredPass 'PRIMARY'
if([string]::IsNullOrWhiteSpace($VsCodeProblemsJson)){throw 'Fresh VS Code Problems JSON이 필요합니다. CPF_VSCODE_PROBLEMS_JSON 또는 -VsCodeProblemsJson으로 export 경로를 지정하세요.'}
$vscodeOut=Join-Path $OutputRoot "CPF_VSCODE_PROBLEMS_VERIFY_$stamp.json";& $python (Join-Path $RepoRoot 'cpf-tools/verification/tools/verify-cpf-vscode-problems.py') --input $VsCodeProblemsJson --output $vscodeOut 2>&1|Tee-Object -FilePath $log -Append;if($LASTEXITCODE-ne0){throw "VS Code Fresh Import Error/Warning gate failed: $vscodeOut"}
Invoke-RequiredPass 'FRESH_REPLAY'
Write-Host "[CPF][FULL-RUNTIME][PASS] SourceIdentity=$sourceIdentity PRIMARY+VSCODE_0_0+FRESH_REPLAY completed. LOG=$log" -ForegroundColor Green
