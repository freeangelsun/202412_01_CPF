[CmdletBinding()]
param(
    [string] $RepoRoot = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [ValidateSet('local','dev','test','stg','prod')]
    [string] $ResourceProfile = 'local',
    [string] $OutputRoot = '',
    [string] $JavaHome = '',
    [string] $DockerRoot = 'C:\dev\Docker',
    [string] $DockerSecretFile = '',
    [switch] $IncludeDbRuntime,
    [switch] $IncludeRuntimeClosure,
    [switch] $IncludeBrowserE2E,
    [switch] $FullLocal,
    [switch] $IncludePerformanceLoad,
    [switch] $AllowDestructiveDbRollback,
    [switch] $SkipFrontend,
    [switch] $SkipOneWas,
    [switch] $SkipDocker,
    [switch] $KeepDockerStarted,
    [switch] $StrictExit
)

# 목적:
#  - 프로젝트 루트에서 한 번 실행하면 독립 검증을 끝까지 수행한다.
#  - 한 단계 FAIL이어도 다음 단계로 계속한다.
#  - 집/노트북 기본은 저메모리/순차 실행이며 Docker all-up을 하지 않는다.
#  - 결과/로그/요약/ZIP은 기본적으로 $HOME\Downloads 아래에 남긴다.
#  - destructive DB rollback, 분산 Runtime, Browser E2E는 명시적 opt-in만 허용한다.
#  - Git 상태를 조회하지 않는다. 전달 Manifest에 포함된 관리 파일 hash가 바뀌면 마지막 Managed State Gate가 FAIL한다.

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$RepoRoot = (Resolve-Path -LiteralPath $RepoRoot).Path
Set-Location $RepoRoot
if ([string]::IsNullOrWhiteSpace($OutputRoot)) { $OutputRoot = Join-Path $HOME 'Downloads' }
[IO.Directory]::CreateDirectory($OutputRoot) | Out-Null
if ([string]::IsNullOrWhiteSpace($DockerSecretFile)) { $DockerSecretFile = Join-Path $DockerRoot 'Secrets\cpf-runtime.env' }

$stamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$finalResultDir = Join-Path $OutputRoot "CPF_LOCAL_VALIDATION_$stamp"
$scratchRoot = Join-Path ([IO.Path]::GetTempPath()) ("CPF_LOCAL_VALIDATION_{0}_{1}" -f $stamp,$PID)
$resultDir = $scratchRoot
$logDir = Join-Path $resultDir 'logs'
$evidenceDir = Join-Path $resultDir 'evidence'
function Ensure-CpfResultDirectories {
    [IO.Directory]::CreateDirectory($script:resultDir) | Out-Null
    [IO.Directory]::CreateDirectory($script:logDir) | Out-Null
    [IO.Directory]::CreateDirectory($script:evidenceDir) | Out-Null
}
Ensure-CpfResultDirectories
$summary = [Collections.Generic.List[object]]::new()
$seq = 0
$utf8 = [Text.UTF8Encoding]::new($false)
$env:PYTHONDONTWRITEBYTECODE = '1'
$env:PYTHONUTF8 = '1'
$env:PYTHONIOENCODING = 'utf-8'
$env:CPF_RESOURCE_PROFILE = $ResourceProfile

# FullLocal은 사용자가 로컬 검증을 여러 번 반복하지 않도록 비파괴 검증 범위를 최대화합니다.
# destructive DB rollback과 장시간 HTTP load/soak는 별도 명시적 opt-in으로 유지합니다.
if($FullLocal){
    $IncludeDbRuntime=$true
    $IncludeRuntimeClosure=$true
    $IncludeBrowserE2E=$true
}

function Find-CpfCommand([string] $Name) {
    $cmd = Get-Command $Name -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($null -eq $cmd) { return $null }
    return [string] $cmd.Source
}

$hostPython = Find-CpfCommand 'python'
$python = $hostPython
$pwsh = Find-CpfCommand 'pwsh'
if (-not $pwsh) { $pwsh = Find-CpfCommand 'powershell' }
$node = Find-CpfCommand 'node'
$npm = Find-CpfCommand 'npm.cmd'
if (-not $npm) { $npm = Find-CpfCommand 'npm' }
$java = Find-CpfCommand 'java'
$docker = Find-CpfCommand 'docker'
$gradle = Join-Path $RepoRoot 'gradlew.bat'
$resourceHelper = Join-Path $RepoRoot 'cpf-tools\runtime\tools\cpf-resource-policy.ps1'
$dockerCpfRoot = Join-Path $DockerRoot 'CPF'
$dockerEnvTool = Join-Path $dockerCpfRoot 'cpf-env.ps1'
$sourceStateTool = Join-Path $RepoRoot 'cpf-tools\verification\tools\cpf-source-state.py'

function Add-CpfResult(
    [string]$Name,
    [string]$Status,
    [Nullable[int]]$ExitCode,
    [double]$Seconds,
    [string]$Log,
    [string]$Note=''
) {
    $script:summary.Add([pscustomobject]@{
        seq=$script:seq
        name=$Name
        status=$Status
        exitCode=$ExitCode
        seconds=[math]::Round($Seconds,3)
        log=if($Log){[IO.Path]::GetRelativePath($script:resultDir,$Log).Replace('\','/')}else{''}
        note=$Note
    })
}

function Add-CpfTextResult([string]$Name,[string]$Status,[string]$Text,[string]$Note='') {
    Ensure-CpfResultDirectories
    $script:seq++
    $safe = $Name -replace '[^A-Za-z0-9._-]','_'
    $log = Join-Path $script:logDir ('{0:D2}_{1}.log' -f $script:seq,$safe)
    [IO.File]::WriteAllText($log,$Text + "`n",$script:utf8)
    $color = if($Status -eq 'PASS'){'Green'}elseif($Status -eq 'SKIP_ENV'){'Yellow'}else{'Red'}
    Write-Host ("[{0:D2}] {1} -> {2}" -f $script:seq,$Name,$Status) -ForegroundColor $color
    Add-CpfResult $Name $Status $null 0 $log $Note
}

function Skip-CpfStage([string]$Name,[string]$Reason) {
    Add-CpfTextResult $Name 'SKIP_ENV' "STATUS=SKIP_ENV`nREASON=$Reason" $Reason
}

function Invoke-CpfStage {
    param(
        [Parameter(Mandatory=$true)][string]$Name,
        [Parameter(Mandatory=$true)][string]$Executable,
        [string[]]$Arguments=@(),
        [string]$WorkingDirectory=$script:RepoRoot,
        [hashtable]$Environment=@{}
    )
    if ([string]::IsNullOrWhiteSpace($Executable) -or -not (Test-Path -LiteralPath $Executable -PathType Leaf)) {
        Skip-CpfStage $Name 'required executable missing'
        return
    }
    Ensure-CpfResultDirectories
    $script:seq++
    $safe = $Name -replace '[^A-Za-z0-9._-]','_'
    $log = Join-Path $script:logDir ('{0:D2}_{1}.log' -f $script:seq,$safe)
    $started = Get-Date
    [IO.File]::WriteAllText($log,"START=$($started.ToString('o'))`nWD=$WorkingDirectory`nCMD=$Executable $($Arguments -join ' ')`n",$script:utf8)
    Write-Host ("[{0:D2}] {1} ..." -f $script:seq,$Name) -ForegroundColor Cyan
    $previousEnv=@{}
    foreach($key in $Environment.Keys){
        $previousEnv[$key]=[Environment]::GetEnvironmentVariable([string]$key,'Process')
        [Environment]::SetEnvironmentVariable([string]$key,[string]$Environment[$key],'Process')
    }
    $rc=1
    $old=$ErrorActionPreference
    try {
        $ErrorActionPreference='Continue'
        Push-Location $WorkingDirectory
        try {
            & $Executable @Arguments 2>&1 | ForEach-Object { Add-Content -LiteralPath $log -Value $_.ToString() -Encoding UTF8 }
            $rc=if($null -eq $LASTEXITCODE){0}else{[int]$LASTEXITCODE}
        } finally { Pop-Location }
    } catch {
        Add-Content -LiteralPath $log -Value $_.Exception.ToString() -Encoding UTF8
        $rc=1
    } finally {
        foreach($key in $previousEnv.Keys){[Environment]::SetEnvironmentVariable([string]$key,$previousEnv[$key],'Process')}
        $ErrorActionPreference=$old
    }
    $seconds=((Get-Date)-$started).TotalSeconds
    $status=if($rc -eq 0){'PASS'}else{'FAIL'}
    Ensure-CpfResultDirectories
    if(-not(Test-Path -LiteralPath $log -PathType Leaf)){[IO.File]::WriteAllText($log,"RECOVERED_STAGE_LOG=true`n",$script:utf8)}
    Add-Content -LiteralPath $log -Value "`nEXIT_CODE=$rc`nEND=$((Get-Date).ToString('o'))" -Encoding UTF8
    Write-Host ("[{0:D2}] {1} -> {2} rc={3} {4:N1}s" -f $script:seq,$Name,$status,$rc,$seconds) -ForegroundColor $(if($rc -eq 0){'Green'}else{'Red'})
    Add-CpfResult $Name $status $rc $seconds $log
}

function Test-CpfDockerReady {
    if(-not $docker){return $false}
    & $docker info *> $null
    return $LASTEXITCODE -eq 0
}

function Test-CpfContainerRunning([string]$Name) {
    if(-not $docker){return $false}
    $value=(& $docker inspect --format '{{.State.Running}}' $Name 2>$null | Out-String).Trim()
    return $LASTEXITCODE -eq 0 -and $value -eq 'true'
}

function Wait-CpfContainerReady([string]$Name,[int]$TimeoutSeconds=300) {
    $deadline=(Get-Date).AddSeconds($TimeoutSeconds)
    do {
        $state=(& $docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' $Name 2>$null | Out-String).Trim()
        if($LASTEXITCODE -eq 0 -and $state -in @('healthy','running')){return $true}
        if($state -eq 'unhealthy'){return $false}
        Start-Sleep -Seconds 3
    } while((Get-Date) -lt $deadline)
    return $false
}

function Import-CpfEnvFile([string]$Path) {
    $previous=@{}
    if(-not(Test-Path -LiteralPath $Path -PathType Leaf)){return $previous}
    foreach($line in Get-Content -LiteralPath $Path -Encoding UTF8){
        if($line -match '^(?<k>[A-Za-z_][A-Za-z0-9_]*)=(?<v>.*)$'){
            $key=[string]$Matches.k
            $previous[$key]=[Environment]::GetEnvironmentVariable($key,'Process')
            [Environment]::SetEnvironmentVariable($key,[string]$Matches.v,'Process')
        }
    }
    return $previous
}

function Restore-CpfEnvironment([hashtable]$Previous) {
    foreach($key in $Previous.Keys){[Environment]::SetEnvironmentVariable([string]$key,$Previous[$key],'Process')}
}

$dockerTargetContainers=@{
    mariadb='cpf-mariadb'
    postgresql='cpf-postgresql'
    oracle='cpf-oracle'
    redis='cpf-redis'
    kafka='cpf-kafka'
}

function Start-CpfDockerTarget([string]$Target) {
    if($SkipDocker){return [pscustomobject]@{ready=$false;started=$false;reason='SkipDocker requested'}}
    if(-not(Test-CpfDockerReady)){return [pscustomobject]@{ready=$false;started=$false;reason='Docker Desktop/daemon is not ready'}}
    if(-not(Test-Path -LiteralPath $dockerEnvTool -PathType Leaf)){return [pscustomobject]@{ready=$false;started=$false;reason="Docker helper missing: $dockerEnvTool"}}
    if(-not(Test-Path -LiteralPath $DockerSecretFile -PathType Leaf)){return [pscustomobject]@{ready=$false;started=$false;reason="Docker secret env missing: $DockerSecretFile"}}
    $container=[string]$dockerTargetContainers[$Target]
    if([string]::IsNullOrWhiteSpace($container)){return [pscustomobject]@{ready=$false;started=$false;reason="unsupported Docker target=$Target"}}
    if(Test-CpfContainerRunning $container){
        Add-CpfTextResult "DOCKER_${Target}_START" 'PASS' "STATE=ALREADY_RUNNING`nCONTAINER=$container" 'existing container is preserved'
        return [pscustomobject]@{ready=$true;started=$false;reason='already running'}
    }
    Invoke-CpfStage "DOCKER_${Target}_START" $pwsh @('-NoProfile','-File',$dockerEnvTool,'-Action','up','-Target',$Target,'-SecretFile',$DockerSecretFile)
    $stageOk=$summary[$summary.Count-1].status -eq 'PASS'
    $timeout=if($Target -eq 'oracle'){600}else{240}
    $ready=$stageOk -and (Wait-CpfContainerReady $container $timeout)
    if($ready){Add-CpfTextResult "DOCKER_${Target}_HEALTH" 'PASS' "CONTAINER=$container`nREADY=true"}
    else{Add-CpfTextResult "DOCKER_${Target}_HEALTH" 'FAIL' "CONTAINER=$container`nREADY=false" 'container did not become ready'}
    $reason=if($ready){'started by validation'}else{'start/health failed'}
    return [pscustomobject]@{ready=$ready;started=$true;reason=$reason}
}

function Stop-CpfDockerTargetIfOwned([string]$Target,$State) {
    if($null -eq $State -or -not $State.started -or $KeepDockerStarted){return}
    Invoke-CpfStage "DOCKER_${Target}_STOP" $pwsh @('-NoProfile','-File',$dockerEnvTool,'-Action','stop','-Target',$Target,'-SecretFile',$DockerSecretFile)
}



function Resolve-CpfJava25Home {
    $candidates=[Collections.Generic.List[string]]::new()
    if(-not [string]::IsNullOrWhiteSpace($JavaHome)){$candidates.Add($JavaHome)}
    if(-not [string]::IsNullOrWhiteSpace($env:CPF_JAVA25_HOME)){$candidates.Add($env:CPF_JAVA25_HOME)}
    if(-not [string]::IsNullOrWhiteSpace($env:JAVA_HOME)){$candidates.Add($env:JAVA_HOME)}
    if($IsWindows){
        $candidates.Add('C:\dev\java\jdk-25.0.3.9-hotspot')
        foreach($base in @('C:\dev\java','C:\Program Files\Eclipse Adoptium','C:\Program Files\Java')){
            if(Test-Path -LiteralPath $base -PathType Container){
                foreach($dir in Get-ChildItem -LiteralPath $base -Directory -Filter 'jdk-25*' -ErrorAction SilentlyContinue | Sort-Object Name -Descending){
                    $candidates.Add($dir.FullName)
                }
            }
        }
    }
    foreach($candidate in @($candidates | Select-Object -Unique)){
        if([string]::IsNullOrWhiteSpace($candidate)){continue}
        $javaExe=Join-Path $candidate $(if($IsWindows){'bin\java.exe'}else{'bin/java'})
        if(-not(Test-Path -LiteralPath $javaExe -PathType Leaf)){continue}
        $versionText=(& $javaExe -version 2>&1 | Out-String)
        if($versionText -match '(?im)(?:openjdk|java) version "25(?:\.|")'){
            return (Resolve-Path -LiteralPath $candidate).Path
        }
    }
    return $null
}

function Initialize-CpfJava25 {
    $resolved=Resolve-CpfJava25Home
    if([string]::IsNullOrWhiteSpace($resolved)){
        Add-CpfTextResult 'JAVA25_ENV' 'FAIL' "JAVA25_HOME=NOT_FOUND`nREQUIRED_MAJOR=25" 'Java 25 is required for CPF Gradle/runtime validation'
        return $false
    }
    $env:JAVA_HOME=$resolved
    $bin=Join-Path $resolved 'bin'
    $separator=[IO.Path]::PathSeparator
    $env:PATH=$bin+$separator+($env:PATH -split [regex]::Escape([string]$separator) | Where-Object {$_ -ne $bin} | Join-String -Separator $separator)
    $version=(& (Join-Path $bin $(if($IsWindows){'java.exe'}else{'java'})) -version 2>&1 | Out-String).Trim()
    Add-CpfTextResult 'JAVA25_ENV' 'PASS' "JAVA_HOME=$resolved`n$version" 'CPF Gradle/runtime is pinned to Java 25 for this validation process'
    return $true
}

function Initialize-CpfPythonEnvironment {
    if(-not $hostPython){
        Add-CpfTextResult 'PYTHON_ENV' 'SKIP_ENV' 'python missing' 'Python 3.13 validation environment unavailable'
        return $null
    }
    $versionText=(& $hostPython --version 2>&1 | Out-String).Trim()
    if($versionText -notmatch '^Python 3\.13(?:\.|$)'){
        Add-CpfTextResult 'PYTHON_ENV' 'FAIL' "EXPECTED=Python 3.13`nACTUAL=$versionText" 'CPF local validation requires Python 3.13'
        return $null
    }
    $venvRoot=Join-Path ([IO.Path]::GetTempPath()) 'cpf-local-validation-python313-env'
    $venvPython=Join-Path $venvRoot $(if($IsWindows){'Scripts\python.exe'}else{'bin/python'})
    $bootstrapLog=Join-Path $script:logDir 'PYTHON_BOOTSTRAP.log'
    if(-not(Test-Path -LiteralPath $venvPython -PathType Leaf)){
        [IO.Directory]::CreateDirectory((Split-Path -Parent $venvRoot)) | Out-Null
        $venvOutput=@(& $hostPython -m venv $venvRoot 2>&1)
        $venvRc=$LASTEXITCODE
        if($venvOutput.Count -gt 0){
            $venvOutput | ForEach-Object { Add-Content -LiteralPath $bootstrapLog -Value $_.ToString() -Encoding UTF8; Write-Host $_.ToString() }
        }
        if($venvRc -ne 0){
            Add-CpfTextResult 'PYTHON_ENV' 'FAIL' "venv creation failed: $venvRoot" 'unable to create TEMP-local validation Python environment'
            return $null
        }
    }
    $importOutput=@(& $venvPython -c 'import pytest, cryptography' 2>&1)
    $importRc=$LASTEXITCODE
    if($importOutput.Count -gt 0){
        $importOutput | ForEach-Object { Add-Content -LiteralPath $bootstrapLog -Value $_.ToString() -Encoding UTF8 }
    }
    if($importRc -ne 0){
        $requirements=Join-Path $RepoRoot 'cpf-tools\verification\local-validation-requirements.txt'
        if(-not(Test-Path -LiteralPath $requirements -PathType Leaf)){
            Add-CpfTextResult 'PYTHON_ENV' 'FAIL' "requirements missing: $requirements" 'TEMP-local Python dependencies cannot be bootstrapped'
            return $null
        }
        $pipOutput=@(& $venvPython -m pip install --disable-pip-version-check --no-input -r $requirements 2>&1)
        $pipRc=$LASTEXITCODE
        if($pipOutput.Count -gt 0){
            $pipOutput | ForEach-Object { Add-Content -LiteralPath $bootstrapLog -Value $_.ToString() -Encoding UTF8; Write-Host $_.ToString() }
        }
        if($pipRc -ne 0){
            Add-CpfTextResult 'PYTHON_ENV' 'FAIL' "dependency bootstrap failed`nrequirements=$requirements`nbootstrapLog=$bootstrapLog" 'network/package environment prevented project-local Python bootstrap'
            return $null
        }
    }
    $verifyOutput=@(& $venvPython -c 'import pytest, cryptography' 2>&1)
    $verifyRc=$LASTEXITCODE
    if($verifyOutput.Count -gt 0){
        $verifyOutput | ForEach-Object { Add-Content -LiteralPath $bootstrapLog -Value $_.ToString() -Encoding UTF8 }
    }
    if($verifyRc -ne 0){
        Add-CpfTextResult 'PYTHON_ENV' 'FAIL' "pytest/cryptography import failed after bootstrap`nbootstrapLog=$bootstrapLog"
        return $null
    }
    Add-CpfTextResult 'PYTHON_ENV' 'PASS' "PYTHON=$venvPython`n$versionText`nDEPENDENCIES=pytest,cryptography`nBOOTSTRAP_LOG=$bootstrapLog" 'TEMP-local reusable Python validation environment outside repository managed/source state'
    return [string]$venvPython
}

function Get-CpfBaselineSha {
    $baseSha = Join-Path $script:RepoRoot 'cpf-docs\work\BASE_SHA.txt'
    if(Test-Path -LiteralPath $baseSha -PathType Leaf){
        $value=(Get-Content -LiteralPath $baseSha -Raw -Encoding UTF8).Trim()
        if($value -match '^[0-9a-fA-F]{40}$'){return $value.ToLowerInvariant()}
    }
    $manifestPath=Join-Path $script:RepoRoot 'cpf-docs\work\PACKAGE_MANIFEST.json'
    if(Test-Path -LiteralPath $manifestPath -PathType Leaf){
        try{
            $manifest=Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
            foreach($candidate in @($manifest.baselineSha,$manifest.basis_sha)){
                if($candidate -and [string]$candidate -match '^[0-9a-fA-F]{40}$'){return ([string]$candidate).ToLowerInvariant()}
            }
        }catch{}
    }
    return 'UNKNOWN_BASELINE_SHA'
}

function Get-CpfTreeState([string]$Scope,[string]$InventoryOutput='') {
    if(-not $python -or -not(Test-Path -LiteralPath $sourceStateTool -PathType Leaf)){
        return [pscustomobject]@{scope=$Scope;contentSha1='';contentSha256='';fileCount=0;totalBytes=0;error='source-state tool unavailable'}
    }
    $args=@($sourceStateTool,'--root',$script:RepoRoot,'--scope',$Scope)
    if(-not [string]::IsNullOrWhiteSpace($InventoryOutput)){$args+=@('--inventory-output',$InventoryOutput)}
    $output=@(& $python @args 2>&1)
    $rc=$LASTEXITCODE
    if($rc -ne 0){
        return [pscustomobject]@{scope=$Scope;contentSha1='';contentSha256='';fileCount=0;totalBytes=0;error=($output -join "`n")}
    }
    try{return (($output | Select-Object -Last 1) | ConvertFrom-Json)}
    catch{return [pscustomobject]@{scope=$Scope;contentSha1='';contentSha256='';fileCount=0;totalBytes=0;error='invalid source-state JSON'}}
}

function Get-CpfNodeOptions([string]$ModuleDir) {
    if(-not(Test-Path -LiteralPath $resourceHelper -PathType Leaf)){return '--max-old-space-size=750'}
    . $resourceHelper
    $resolved=Resolve-CpfResourcePolicy -RepoRoot $RepoRoot -Profile $ResourceProfile -ModuleDir (Join-Path $RepoRoot $ModuleDir)
    $mb=[string]$resolved.Values['frontend.node.maxOldSpace.mb']
    if([string]::IsNullOrWhiteSpace($mb)){$mb='750'}
    return "--max-old-space-size=$mb"
}

function New-CpfFrontendSandbox([string]$FrontendRelative,[string]$Name) {
    $source=Join-Path $RepoRoot $FrontendRelative
    if(-not(Test-Path -LiteralPath $source -PathType Container)){return $null}
    $sandboxRepo=Join-Path ([IO.Path]::GetTempPath()) ("cpf-local-validation-$stamp\repo")
    $moduleDir=if($Name -eq 'ADM'){'cpf-admin'}else{'cpf-biz-admin'}
    $sandboxModule=Join-Path $sandboxRepo $moduleDir
    $target=Join-Path $sandboxModule 'frontend'
    [IO.Directory]::CreateDirectory($target) | Out-Null
    # Frontend 검증은 TEMP의 repository-shaped sandbox에서 수행한다.
    # Backend contract consumer와 BZA permission manifest가 ../.. 상대경로를 사용하므로 필요한 read-only Source도 함께 복사한다.
    foreach($item in Get-ChildItem -LiteralPath $source -Force){
        if($item.Name -in @('node_modules','dist','.vite','playwright-report','test-results')){continue}
        Copy-Item -LiteralPath $item.FullName -Destination $target -Recurse -Force
    }
    $backendSource=Join-Path $RepoRoot "$moduleDir\src"
    if(Test-Path -LiteralPath $backendSource -PathType Container){Copy-Item -LiteralPath $backendSource -Destination $sandboxModule -Recurse -Force}
    if($Name -eq 'BZA'){
        $permissionSource=Join-Path $RepoRoot 'cpf-tools\db\metadata\bza-permission-manifest.json'
        if(Test-Path -LiteralPath $permissionSource -PathType Leaf){
            $permissionTarget=Join-Path $sandboxRepo 'cpf-tools\db\metadata'
            [IO.Directory]::CreateDirectory($permissionTarget)|Out-Null
            Copy-Item -LiteralPath $permissionSource -Destination $permissionTarget -Force
        }
    }
    return $target
}

$java25Ready=Initialize-CpfJava25
$python=Initialize-CpfPythonEnvironment
$baselineSha=Get-CpfBaselineSha
$sourceStateBefore=Get-CpfTreeState 'source' (Join-Path $evidenceDir 'source-state-before.json')
$managedStateBefore=Get-CpfTreeState 'managed' (Join-Path $evidenceDir 'managed-state-before.json')
$sourceIdentity=[string]$sourceStateBefore.contentSha1
$sourceContentSha256=[string]$sourceStateBefore.contentSha256
$sourceStateReady=($sourceIdentity -match '^[0-9a-f]{40}$' -and $sourceContentSha256 -match '^[0-9a-f]{64}$')
$managedStateReady=([string]$managedStateBefore.contentSha256 -match '^[0-9a-f]{64}$')
$env:CPF_SOURCE_SHA=if($sourceStateReady){$sourceIdentity}else{''}
$env:CPF_SOURCE_IDENTITY=if($sourceStateReady){"sha256:$sourceContentSha256"}else{''}
$env:CPF_BASELINE_SHA=if($baselineSha -match '^[0-9a-f]{40}$'){$baselineSha}else{''}
Add-CpfTextResult 'SOURCE_IDENTITY' $(if($sourceStateReady){'PASS'}else{'FAIL'}) "BASELINE_SHA=$baselineSha`nRESULT_CONTENT_SHA1=$sourceIdentity`nRESULT_CONTENT_SHA256=$sourceContentSha256`nSOURCE_FILES=$($sourceStateBefore.fileCount)`nSOURCE_BYTES=$($sourceStateBefore.totalBytes)" 'baseline provenance is separated from Git-independent result content identity'
Add-CpfTextResult 'MANAGED_STATE_BEFORE' $(if($managedStateReady){'PASS'}else{'FAIL'}) "MANAGED_FILES=$($managedStateBefore.fileCount)`nMANAGED_BYTES=$($managedStateBefore.totalBytes)`nSTATE_SHA256=$($managedStateBefore.contentSha256)" 'Git-independent full managed-tree snapshot; generated outputs excluded, cpf-tools/build included'

$dockerReady=Test-CpfDockerReady
$environment=[ordered]@{
    generatedAt=(Get-Date).ToString('o')
    repoRoot=$RepoRoot
    baselineSha=$baselineSha
    resultContentSha1=$sourceIdentity
    resultContentSha256=$sourceContentSha256
    resultSourceIdentity=$env:CPF_SOURCE_IDENTITY
    resourceProfile=$ResourceProfile
    downloads=$OutputRoot
    powershell=$PSVersionTable.PSVersion.ToString()
    java=if($java25Ready){(& (Join-Path $env:JAVA_HOME $(if($IsWindows){'bin\java.exe'}else{'bin/java'})) -version 2>&1 | Out-String).Trim()}else{'JAVA25_MISSING'}
    python=if($python){(& $python --version 2>&1 | Out-String).Trim()}else{'MISSING'}
    node=if($node){(& $node --version 2>&1 | Out-String).Trim()}else{'MISSING'}
    npm=if($npm){(& $npm --version 2>&1 | Out-String).Trim()}else{'MISSING'}
    docker=if($docker){(& $docker --version 2>&1 | Out-String).Trim()}else{'MISSING'}
    dockerDaemonReady=$dockerReady
    dockerAllContainersPrestartRequired=$false
    fullLocal=[bool]$FullLocal
    destructiveDbRollback=[bool]$AllowDestructiveDbRollback
    performanceLoad=[bool]$IncludePerformanceLoad
}
[IO.File]::WriteAllText((Join-Path $evidenceDir 'environment.json'),($environment|ConvertTo-Json -Depth 10)+"`n",$utf8)

# 1. 저비용/정적 Gate + Python 전체 test tree를 독립 실행한다.
#    한 tree 실패가 다른 tree 실행을 막지 않으며, 전체 collection도 별도 확인한다.
if($python){
    Invoke-CpfStage 'RESOURCE_POLICY' $python @('.\cpf-tools\verification\verify-cpf-resource-policy.py','--root','.')
    Invoke-CpfStage 'NXT3_22' $python @('.\cpf-tools\verification\nxt3\run_nxt3_final_all.py','--root','.', '--evidence',(Join-Path $evidenceDir 'nxt3.json'),'--log',(Join-Path $evidenceDir 'nxt3.log'))
    Invoke-CpfStage 'EVIDENCE_INTEGRITY' $python @('.\cpf-tools\verification\tools\verify-cpf-development-evidence-integrity.py','--root','.', '--review-dir','cpf-docs/work','--source-head',$sourceIdentity,'--expected-sha',$sourceIdentity,'--expected-requirements','31','--expected-findings','25')
    $inventory=Join-Path $evidenceDir 'inventory'
    Invoke-CpfStage 'ARCH_INVENTORY_GENERATE' $python @('.\cpf-tools\governance\tools\generate-cpf-project-inventory.py','--root','.', '--output-dir',$inventory)
    Invoke-CpfStage 'ARCH_INVENTORY_VERIFY' $python @('.\cpf-tools\governance\tools\verify-cpf-project-inventory.py','--inventory-dir',$inventory,'--policy','.\cpf-tools\governance\cpf-product-surface-policy.json','--waivers','.\cpf-tools\governance\cpf-project-inventory-waivers.csv','--release')

    Invoke-CpfStage 'PYTEST_COLLECT_ALL' $python @('-m','pytest','--collect-only','.\cpf-tools','-q','--import-mode=importlib','-p','no:cacheprovider')
    $pythonTrees=@(
        @{name='TESTING_TOOLS';path='.\cpf-tools\testing\tools\tests'},
        @{name='DB_VERIFICATION_TESTS';path='.\cpf-tools\db\verification\tests'},
        @{name='DB_TESTS';path='.\cpf-tools\db\tests'},
        @{name='RUNTIME_TOOL_TESTS';path='.\cpf-tools\runtime\tools\tests'},
        @{name='SECURITY_TOOL_TESTS';path='.\cpf-tools\security\tools\tests'},
        @{name='RELEASE_TOOL_TESTS';path='.\cpf-tools\release\tools\tests'},
        @{name='GENERATOR_TOOL_TESTS';path='.\cpf-tools\generator\verification\tests'},
        @{name='DOCKER_DEV_TESTS';path='.\cpf-tools\environment\docker-development-test\tests'},
        @{name='OPENAPI_TOOL_TESTS';path='.\cpf-tools\verification\openapi\tests'},
        @{name='VERIFICATION_TOOL_TESTS';path='.\cpf-tools\verification\tests'},
        @{name='AUDIT_RUNTIME_TOOL_TESTS';path='.\cpf-tools\verification\java21\audit-runtime\tests'},
        @{name='SUPPLY_CHAIN_TOOL_TESTS';path='.\cpf-tools\supply-chain\tools\tests'}
    )
    foreach($tree in $pythonTrees){
        Invoke-CpfStage $tree.name $python @('-m','pytest',$tree.path,'-q','--import-mode=importlib','-p','no:cacheprovider')
    }

    # 고가치 독립 계약 Gate. 전체 pytest와 별개로 결과를 명시적으로 남긴다.
    Invoke-CpfStage 'GRADLE_LOGICAL_TREE' $python @('.\cpf-tools\verification\verify-cpf-gradle-logical-tree.py','--root','.', '--json-output',(Join-Path $evidenceDir 'gradle-logical-tree.json'))
    Invoke-CpfStage 'WINDOWS_PATH_COMPATIBILITY' $python @('.\cpf-tools\verification\verify_windows_path_compatibility.py','--root','.','--target-root-text',$RepoRoot)
    Invoke-CpfStage 'SECURITY_CONTROLLER_PERMISSION' $python @('.\cpf-tools\security\tools\verify-cpf-controller-permission-contract.py','--root','.','--strict','--report',(Join-Path $evidenceDir 'security-controller-permission.json'))
    Invoke-CpfStage 'GATEWAY_STATIC_CLOSURE' $python @('.\cpf-tools\verification\verify_gateway_closure.py')
    Invoke-CpfStage 'TRANSACTION_HEADER_STANDARD' $python @('.\cpf-tools\verification\tools\verify-cpf-transaction-id-standard.py','--root','.', '--json-output',(Join-Path $evidenceDir 'transaction-header-standard.json'))
    Invoke-CpfStage 'FIXED_LENGTH_CLOSURE' $python @('.\cpf-tools\verification\tools\verify-cpf-fixed-length-closure.py','--root','.')
    Invoke-CpfStage 'EVENT_DLQ_APPROVAL_OWNER' $python @('.\cpf-tools\verification\tools\verify-event-dlq-contract.py','.')
    Invoke-CpfStage 'APPROVAL_STATE_MACHINE' $python @('.\cpf-tools\verification\tools\verify-cpf-approval-state-machine.py','--root','.', '--json-output',(Join-Path $evidenceDir 'approval-state-machine.json'))
    Invoke-CpfStage 'DANGEROUS_ACTION_APPROVAL' $python @('.\cpf-tools\verification\tools\verify-cpf-adm-dangerous-action-approval-boundary.py')
    Invoke-CpfStage 'OPERATOR_TRUST_BOUNDARY' $python @('.\cpf-tools\verification\tools\verify-cpf-operator-trust-boundary.py','--root','.', '--json-output',(Join-Path $evidenceDir 'operator-trust-boundary.json'))
    Invoke-CpfStage 'INTERNAL_SERVICE_IDENTITY' $python @('.\cpf-tools\verification\tools\verify-cpf-internal-service-identity-binding.py','--root','.', '--json-output',(Join-Path $evidenceDir 'internal-service-identity.json'))
    Invoke-CpfStage 'THREAT_MODELS' $python @('.\cpf-tools\verification\tools\verify-cpf-threat-models.py','--repo-root','.', '--manifest-dir','.\cpf-tools\verification\security\threat-models','--output-json',(Join-Path $evidenceDir 'threat-models.json'))
    Invoke-CpfStage 'SECURITY_SESSION_OIDC' $python @('.\cpf-tools\verification\verify_security_session_oidc.py','--root','.')
    Invoke-CpfStage 'GENERATOR_FULL_CONTRACT' $python @('.\cpf-tools\verification\verify_generator_full_contract.py','--root','.')
    Invoke-CpfStage 'CACHE_CORRECTNESS' $python @('.\cpf-tools\verification\verify_cache_correctness.py','--root','.')
    Invoke-CpfStage 'CACHE_DURABLE_LIFECYCLE' $python @('.\cpf-tools\verification\tools\verify-cpf-cache-durable-lifecycle.py','--repo-root','.', '--report-json',(Join-Path $evidenceDir 'cache-durable-lifecycle.json'))
    Invoke-CpfStage 'CONTEXT_ARCH_RUNTIME' $python @('.\cpf-tools\verification\run_context_architecture_runtime_tests.py')
    Invoke-CpfStage 'CONTEXT_RUNTIME_LIFECYCLE' $python @('.\cpf-tools\verification\run_context_runtime_lifecycle_tests.py')
    Invoke-CpfStage 'INTEGRATION_CONTEXT_RUNTIME' $python @('.\cpf-tools\verification\run_integration_context_runtime_tests.py')
    Invoke-CpfStage 'MESSAGE_CONTEXT_RUNTIME' $python @('.\cpf-tools\verification\run_message_context_runtime_tests.py')
    Invoke-CpfStage 'BATCH_CONTEXT_RUNTIME' $python @('.\cpf-tools\verification\run_batch_context_runtime_tests.py')
    Invoke-CpfStage 'SECURITY_CONTEXT_RUNTIME' $python @('.\cpf-tools\verification\run_security_context_runtime_tests.py')
    Invoke-CpfStage 'BATCH_UNKNOWN_RECONCILIATION' $python @('.\cpf-tools\verification\tools\verify-cpf-batch-unknown-reconciliation.py','--root','.')
    Invoke-CpfStage 'BATCH_APPROVAL_TRUST_BOUNDARY' $python @('.\cpf-tools\verification\tools\verify-cpf-batch-approval-trust-boundary.py','--root','.', '--json-output',(Join-Path $evidenceDir 'batch-approval-trust-boundary.json'))
    Invoke-CpfStage 'BATCH_EXECUTION_FENCING' $python @('.\cpf-tools\verification\tools\verify-cpf-batch-execution-fencing.py','--root','.', '--json-output',(Join-Path $evidenceDir 'batch-execution-fencing.json'))
    Invoke-CpfStage 'BATCH_GHOST_SAFETY' $python @('.\cpf-tools\verification\tools\verify-cpf-batch-ghost-safety.py','--root','.')
    Invoke-CpfStage 'BATCH_FAIL_CLOSED' $python @('.\cpf-tools\verification\tools\verify-cpf-batch-fail-closed.py','--root','.')
    Invoke-CpfStage 'PUBLICATION_STARTER_CLOSURE' $python @('.\cpf-tools\release\tools\verify-cpf-publication-starter-closure.py','--root','.', '--require-physical','--json-output',(Join-Path $evidenceDir 'publication-starter-closure.json'))


    # Codex/QA 고가치 정적 검수. pytest aggregate에 묻히지 않고 SUMMARY에 독립 결과를 남긴다.
    # 이 묶음은 현재 Source에서 독립 실행 PASS가 확인된 canonical verifier만 포함한다.
    Invoke-CpfStage 'CODEX_COMMON_PRODUCT_SERVICE_DX' $python @('.\cpf-tools\verification\verify_common_product_service_dx.py')
    Invoke-CpfStage 'CODEX_TRANSACTION_ID_CONTRACT' $python @('.\cpf-tools\verification\verify_transaction_id_contract.py')
    Invoke-CpfStage 'CODEX_TXID_ALL_CHANNEL' $python @('.\cpf-tools\verification\verify_txid_all_channel.py','--root','.')
    Invoke-CpfStage 'CODEX_CACHE_DB3_LIFECYCLE' $python @('.\cpf-tools\verification\verify_cache_db3_lifecycle.py','--root','.')
    Invoke-CpfStage 'CODEX_FRONTEND_GOLDEN_PATH' $python @('.\cpf-tools\verification\verify_frontend_golden_path.py','--root','.')
    Invoke-CpfStage 'CODEX_INTEGRATION_CLOSURE_CONTRACT' $python @('.\cpf-tools\verification\verify_integration_closure_contract.py','--root','.')
    Invoke-CpfStage 'CODEX_BUSINESS_FRAMEWORK_CROSSCUT' $python @('.\cpf-tools\verification\verify_business_framework_crosscut.py','--root','.', '--evidence',(Join-Path $evidenceDir 'business-framework-crosscut.json'))
    Invoke-CpfStage 'CODEX_EVENT_SCHEMA_CAPABILITY' $python @('.\cpf-tools\verification\verify_event_schema_capability.py','--root','.')
    Invoke-CpfStage 'CODEX_OBJECT_STORAGE_CAPABILITY' $python @('.\cpf-tools\verification\verify_object_storage_capability.py','--root','.')
    Invoke-CpfStage 'CODEX_ADMIN_DEPENDENCY_BOUNDARY' $python @('.\cpf-tools\verification\verify_admin_dependency_boundaries.py','--root','.')
    Invoke-CpfStage 'CODEX_GRADLE_DEPENDENCY_CLOSURE' $python @('.\cpf-tools\verification\verify_gradle_project_dependency_closure.py','--root','.')
    Invoke-CpfStage 'CODEX_OWNER_BOUNDARIES' $python @('.\cpf-tools\verification\verify_owner_boundaries.py')
    Invoke-CpfStage 'CODEX_NO_PARTIAL_IMPLEMENTATION' $python @('.\cpf-tools\verification\verify_no_partial_implementation.py','--root','.')
    Invoke-CpfStage 'CODEX_STARTER_CATALOG' $python @('.\cpf-tools\verification\verify_starter_catalog.py','--root','.')
    Invoke-CpfStage 'CODEX_RETIRED_STARTER_DEPENDENCIES' $python @('.\cpf-tools\verification\verify_retired_starter_dependencies.py','--root','.')
    Invoke-CpfStage 'CODEX_COMMON_VALIDATION_OWNER' $python @('.\cpf-tools\verification\verify_common_validation_owner.py','--root','.')
    Invoke-CpfStage 'CODEX_DOMAIN_EXCEPTION_ENFORCEMENT' $python @('.\cpf-tools\verification\verify_domain_exception_enforcement.py')
    Invoke-CpfStage 'CODEX_LOGGING_DX' $python @('.\cpf-tools\verification\verify_logging_dx.py')
    Invoke-CpfStage 'CODEX_TESTKIT_CONTRACT' $python @('.\cpf-tools\verification\verify_testkit_contract.py')
    Invoke-CpfStage 'CODEX_ZERO_FOOTPRINT' $python @('.\cpf-tools\verification\verify_nxt_zero_footprint.py')
    Invoke-CpfStage 'CODEX_SPRING_ROUTE_UNIQUENESS' $python @('.\cpf-tools\verification\tools\verify-cpf-spring-request-mapping-uniqueness.py','--root','.', '--json-output',(Join-Path $evidenceDir 'spring-route-uniqueness.json'))
    Invoke-CpfStage 'CODEX_ASYNCAPI_LIFECYCLE' $python @('.\cpf-tools\verification\tools\verify-cpf-asyncapi-lifecycle.py','--repo-root','.', '--catalog','.\cpf-tools\contracts\asyncapi\cpf-asyncapi-catalog.json','--output-json',(Join-Path $evidenceDir 'asyncapi-lifecycle.json'))
    Invoke-CpfStage 'CODEX_AUDIT_FAIL_CLOSED' $python @('.\cpf-tools\verification\tools\verify-cpf-audit-fail-closed.py','--root','.', '--json-output',(Join-Path $evidenceDir 'audit-fail-closed.json'))
    Invoke-CpfStage 'CODEX_TELEMETRY_LIFECYCLE' $python @('.\cpf-tools\verification\tools\verify-cpf-telemetry-lifecycle.py','--root','.')
    Invoke-CpfStage 'CODEX_NETWORK_POLICY_CONSUMERS' $python @('.\cpf-tools\verification\tools\verify-cpf-network-policy-consumers.py','--root','.', '--json-output',(Join-Path $evidenceDir 'network-policy-consumers.json'))
    Invoke-CpfStage 'CODEX_NOTIFICATION_INCIDENT_LIFECYCLE' $python @('.\cpf-tools\verification\tools\verify-cpf-notification-incident-lifecycle.py','--root','.')
    Invoke-CpfStage 'CODEX_FRONTEND_CONSUMER_CLOSURE' $python @('.\cpf-tools\verification\tools\verify-cpf-frontend-consumer-closure.py','--root','.', '--json-output',(Join-Path $evidenceDir 'frontend-consumer-closure.json'))

    # Exhaustive execution-scope audit는 source만으로 생성할 수 없는 QA audit/work-package 입력을 요구한다.
    # 두 입력이 모두 주어질 때만 실제 검증하며, 하나라도 없으면 미실행을 명시해 false PASS를 막는다.
    $executionAuditCsv=[Environment]::GetEnvironmentVariable('CPF_EXECUTION_AUDIT_CSV','Process')
    $executionWorkPackageCsv=[Environment]::GetEnvironmentVariable('CPF_EXECUTION_WORK_PACKAGE_CSV','Process')
    if(-not [string]::IsNullOrWhiteSpace($executionAuditCsv) -and -not [string]::IsNullOrWhiteSpace($executionWorkPackageCsv) -and (Test-Path -LiteralPath $executionAuditCsv -PathType Leaf) -and (Test-Path -LiteralPath $executionWorkPackageCsv -PathType Leaf)){
        Invoke-CpfStage 'CODEX_EXECUTION_SCOPE_EXHAUSTIVE' $python @('.\cpf-tools\verification\tools\verify-cpf-execution-scope-exhaustive.py','--root','.', '--expected-sha',$sourceIdentity,'--source-head',$sourceIdentity,'--audit-csv',$executionAuditCsv,'--work-package-csv',$executionWorkPackageCsv,'--json-output',(Join-Path $evidenceDir 'execution-scope-exhaustive.json'))
    }else{
        Skip-CpfStage 'CODEX_EXECUTION_SCOPE_EXHAUSTIVE' 'CPF_EXECUTION_AUDIT_CSV / CPF_EXECUTION_WORK_PACKAGE_CSV inputs not provided'
    }
    foreach($topology in @('single-node','split-online','split-batch','full-distributed')){
        $stage='DEPLOYMENT_TOPOLOGY_'+$topology.ToUpperInvariant().Replace('-','_')
        Invoke-CpfStage $stage $python @('.\deploy\tools\prepare-distribution.py','--root','.', '--env','local','--topology',$topology,'--output',(Join-Path $evidenceDir "deployment-plan\$topology"),'--plan-only')
    }

    # Performance contract/profile correctness is always checked here without pretending that live probes ran.
    # Actual command/HTTP workload closure is a separate stage and requires explicit live probe endpoints.
    if($FullLocal){
        $perfProfile=Join-Path $RepoRoot 'cpf-tools\testing\performance\cpf-performance-profile.json'
        foreach($workload in @('broker-backpressure','batch-reconcile','resource-budget')){
            Invoke-CpfStage ("PERFORMANCE_"+$workload.ToUpperInvariant().Replace('-','_')+"_CONTRACT") $python @('.\cpf-tools\testing\tools\run-cpf-performance-contract.py','--profile',$perfProfile,'--workload',$workload,'--dry-run','--output-json',(Join-Path $evidenceDir ("performance-$workload-contract.json")))
        }
    }
}else{Skip-CpfStage 'PYTHON_SUITE' 'python missing'}

# 2. Java/Gradle. Frontend는 이 단계에서 제외하고 순차 실행해 노트북 OOM을 피한다.
if($java25Ready -and (Test-Path -LiteralPath $gradle -PathType Leaf)){
    $gradleBase=@("-PcpfResourceProfile=$ResourceProfile",'-PcpfSkipFrontendBuild=true','--no-daemon','--no-parallel','--stacktrace')
    Invoke-CpfStage 'GRADLE_PROJECTS' $gradle (@('projects')+$gradleBase)
    Invoke-CpfStage 'GRADLE_HELP' $gradle (@('help')+$gradleBase)
    Invoke-CpfStage 'GRADLE_CPF_HELP' $gradle (@('cpfHelp')+$gradleBase)
    Invoke-CpfStage 'GRADLE_CPF_MODULES' $gradle (@('cpfModules')+$gradleBase)
    Invoke-CpfStage 'GRADLE_FULL_BUILD_QUALITY' $gradle (@('clean','cpfBuild','qualityGate','--continue')+$gradleBase)
    Invoke-CpfStage 'GRADLE_ALL_JAVA_TESTS' $gradle (@('cpfTest','--continue')+$gradleBase)
    Invoke-CpfStage 'GRADLE_QA34_INTEGRATION' $gradle (@('qa34IntegrationTest','--continue')+$gradleBase)
    Invoke-CpfStage 'GRADLE_PUBLICATION' $gradle (@('publicationGate','publishToMavenLocal','--continue')+$gradleBase)
    if($python){
        Invoke-CpfStage 'DEPLOYMENT_FULL_DISTRIBUTED_ARTIFACT_PACK' $python @('.\deploy\tools\prepare-distribution.py','--root','.', '--env','local','--topology','full-distributed','--output',(Join-Path $evidenceDir 'deployment-artifact\full-distributed'))
    }
}else{Skip-CpfStage 'GRADLE_JAVA_TEST_QUALITY' $(if(-not $java25Ready){'Java 25 unavailable'}else{'gradlew.bat missing'})}

# 3. ADM/BZA Frontend를 Java와 겹치지 않게 순차 실행한다.
$frontendSandboxes=@{}
if(-not $SkipFrontend -and $npm){
    foreach($frontend in @('cpf-admin\frontend','cpf-biz-admin\frontend')){
        $name=if($frontend.StartsWith('cpf-admin')){'ADM'}else{'BZA'}
        $moduleDir=if($name -eq 'ADM'){'cpf-admin'}else{'cpf-biz-admin'}
        $nodeOptions=Get-CpfNodeOptions $moduleDir
        $frontendSandbox=New-CpfFrontendSandbox $frontend $name
        if($null -eq $frontendSandbox){
            Add-CpfTextResult "${name}_FRONTEND_SANDBOX" 'FAIL' "source frontend missing: $frontend"
            continue
        }
        Add-CpfTextResult "${name}_FRONTEND_SANDBOX" 'PASS' "SOURCE=$frontend`nSANDBOX=$frontendSandbox" 'generated/source frontend remains read-only during npm verification'
        $frontendSandboxes[$name]=$frontendSandbox
        Invoke-CpfStage "${name}_NPM_CI" $npm @('ci','--ignore-scripts') $frontendSandbox @{NODE_OPTIONS=$nodeOptions;CPF_SOURCE_SHA=$env:CPF_SOURCE_SHA}
        Invoke-CpfStage "${name}_FRONTEND_VERIFY" $npm @('run','verify') $frontendSandbox @{NODE_OPTIONS=$nodeOptions;CPF_SOURCE_SHA=$env:CPF_SOURCE_SHA}
        Invoke-CpfStage "${name}_OPENAPI_SOURCE_VALIDATE" $npm @('run','validate:openapi:source') $frontendSandbox @{NODE_OPTIONS=$nodeOptions;CPF_SOURCE_SHA=$env:CPF_SOURCE_SHA}
    }
}elseif($SkipFrontend){Skip-CpfStage 'FRONTEND' 'SkipFrontend requested'}else{Skip-CpfStage 'FRONTEND' 'npm missing'}

# Frontend 산출물이 준비된 뒤 Java assemble/SBOM만 다시 수행한다. Frontend를 재실행하지 않는다.
if($java25Ready -and (Test-Path -LiteralPath $gradle -PathType Leaf)){
    $gradleBase=@("-PcpfResourceProfile=$ResourceProfile",'-PcpfSkipFrontendBuild=true','--no-daemon','--no-parallel','--stacktrace')
    Invoke-CpfStage 'GRADLE_ASSEMBLE_AFTER_FRONTEND' $gradle (@('assemble','--continue')+$gradleBase)
    Invoke-CpfStage 'GRADLE_SBOM' $gradle (@('cyclonedxBom','--continue')+$gradleBase)
}

# 4. Windows/PowerShell Generator 및 DB static.
if($pwsh){
    Invoke-CpfStage 'LOCAL_TOPOLOGY_CONTRACT' $pwsh @('-NoProfile','-File','.\cpf-tools\runtime\tools\check-local-runtime-topology.ps1','-Root',$RepoRoot)
    Invoke-CpfStage 'GENERATOR_LIFECYCLE' $pwsh @('-NoProfile','-File','.\cpf-tools\generator\verification\smoke-generated-domain-lifecycle.ps1','-Root',$RepoRoot,'-DomainName','localqa','-SystemCode','LQA','-Apply','-RoundTrip','-ConfirmGeneratedSourceRemoval')
    foreach($dbScript in @('check-db-vendor-pack-parity.ps1','check-canonical-db-lifecycle-contract.ps1','check-migration-checksums.ps1','check-sql-standard.ps1','check-sql-canonical.ps1')){
        Invoke-CpfStage ("DB_"+[IO.Path]::GetFileNameWithoutExtension($dbScript).ToUpperInvariant()) $pwsh @('-NoProfile','-File',(Join-Path $RepoRoot "cpf-tools\db\verification\$dbScript"))
    }
}else{Skip-CpfStage 'POWERSHELL_SUITE' 'pwsh/powershell missing'}

# 5. DB3 live: opt-in. 세 Vendor를 동시에 띄우지 않고 하나씩 올려서 검증 후 내린다.
if($IncludeDbRuntime){
    if(-not $pwsh){Skip-CpfStage 'DB3_RUNTIME_MATRIX' 'pwsh missing'}
    elseif($SkipDocker){Skip-CpfStage 'DB3_RUNTIME_MATRIX' 'SkipDocker requested'}
    elseif(-not $dockerReady){Skip-CpfStage 'DB3_RUNTIME_MATRIX' 'Docker Desktop/daemon is not ready'}
    else{
        $dbEnvBefore=Import-CpfEnvFile $DockerSecretFile
        try{
            foreach($vendor in @('mariadb','postgresql','oracle')){
                $dockerState=Start-CpfDockerTarget $vendor
                try{
                    if($dockerState.ready){
                        $args=@('-NoProfile','-File','.\cpf-tools\db\verification\invoke-cpf-db-runtime-matrix.ps1','-Root',$RepoRoot,'-SourceSha',$sourceIdentity,'-Vendor',$vendor,'-ClientAdapter','Auto','-RequireRuntime','-EvidenceRoot',(Join-Path $evidenceDir "db3-runtime\$vendor"))
                        if($AllowDestructiveDbRollback){$args+='-AllowDestructiveRollback'}
                        Invoke-CpfStage ("DB3_RUNTIME_"+$vendor.ToUpperInvariant()) $pwsh $args
                    }else{Skip-CpfStage ("DB3_RUNTIME_"+$vendor.ToUpperInvariant()) $dockerState.reason}
                }finally{Stop-CpfDockerTargetIfOwned $vendor $dockerState}
            }
        }finally{Restore-CpfEnvironment $dbEnvBefore}
    }
}else{Skip-CpfStage 'DB3_RUNTIME_MATRIX' 'IncludeDbRuntime not requested'}

# 6. Runtime closure. 각 외부 자원은 순차 기동하고 검증기가 직접 시작한 컨테이너만 중지/재시작한다.
if($IncludeRuntimeClosure){
    if($pwsh -and $dockerReady -and -not $SkipDocker){
        Invoke-CpfStage 'CACHE_PROVIDER_LIVE' $pwsh @('-NoProfile','-File','.\cpf-tools\environment\docker-development-test\run-cache-provider-live.ps1','-Root',$RepoRoot,'-EvidenceDirectory',(Join-Path $evidenceDir 'cache-provider-live'),'-SourceIdentity',$sourceIdentity)
        $qa39=Join-Path $RepoRoot 'cpf-tools\environment\docker-development-test\run-qa39-runtime-validation.ps1'
        $qa39Start=Join-Path $dockerCpfRoot 'start-qa39-runtime.ps1'
        $qa39Stop=Join-Path $dockerCpfRoot 'stop-qa39-runtime.ps1'
        if((Test-Path -LiteralPath $qa39 -PathType Leaf) -and (Test-Path -LiteralPath $qa39Start -PathType Leaf) -and (Test-Path -LiteralPath $qa39Stop -PathType Leaf)){
            Invoke-CpfStage 'QA39_RUNTIME_FAULT_SMOKE' $pwsh @('-NoProfile','-File',$qa39,'-DockerRoot',$DockerRoot,'-RepoRoot',$RepoRoot,'-SourceIdentity',$sourceIdentity)
        }else{Skip-CpfStage 'QA39_RUNTIME_FAULT_SMOKE' 'QA39 installed Docker runtime helpers are missing'}

        $kafkaState=$null
        try{
            $kafkaState=Start-CpfDockerTarget 'kafka'
            if($kafkaState.ready){
                $kafkaArgs=@('-NoProfile','-File','.\cpf-tools\runtime\tools\smoke-kafka-reliability.ps1','-Root',$RepoRoot,'-ResultDir',(Join-Path $evidenceDir 'kafka-reliability'),'-SourceIdentity',$sourceIdentity)
                if($kafkaState.started){$kafkaArgs+='-RestartOwnedContainer'}
                Invoke-CpfStage 'MESSAGING_KAFKA_RELIABILITY' $pwsh $kafkaArgs
            }else{Skip-CpfStage 'MESSAGING_KAFKA_RELIABILITY' $kafkaState.reason}
        }finally{Stop-CpfDockerTargetIfOwned 'kafka' $kafkaState}

        $batchDbEnv=Import-CpfEnvFile $DockerSecretFile
        $batchDbState=$null
        try{
            $batchDbState=Start-CpfDockerTarget 'mariadb'
            if(-not $batchDbState.ready){
                Skip-CpfStage 'BATCH_TWO_WORKER_CRASH_UNKNOWN' $batchDbState.reason
            }else{
                $workerJar=Get-ChildItem -LiteralPath (Join-Path $RepoRoot 'cpf-batch\worker\build\libs') -File -Filter '*.jar' -ErrorAction SilentlyContinue | Where-Object {$_.Name -notmatch 'plain'} | Select-Object -First 1
                $adminPassword=[Environment]::GetEnvironmentVariable('CPF_ADMIN_PASSWORD','Process')
                if(-not $workerJar){Skip-CpfStage 'BATCH_TWO_WORKER_CRASH_UNKNOWN' 'Batch worker bootJar unavailable after Gradle build'}
                elseif([string]::IsNullOrWhiteSpace($adminPassword)){Add-CpfTextResult 'BATCH_TWO_WORKER_CRASH_UNKNOWN' 'FAIL' 'CPF_ADMIN_PASSWORD missing from Docker secret env' 'Batch two-worker runtime requires local MariaDB credentials'}
                else{
                    Invoke-CpfStage 'BATCH_TWO_WORKER_CRASH_UNKNOWN' $pwsh @('-NoProfile','-File','.\cpf-tools\runtime\tools\smoke-bat-two-worker-runtime.ps1','-Root',$RepoRoot,'-ResultDir',(Join-Path $evidenceDir 'batch-two-worker'),'-ClientAdapter','Docker','-MariaDbContainer','cpf-mariadb') $RepoRoot @{CPF_DB_ROOT_PASSWORD=$adminPassword;CPF_ADMIN_PASSWORD=$adminPassword}
                    Invoke-CpfStage 'GATEWAY_BATCH_RUNTIME' $pwsh @('-NoProfile','-File','.\cpf-tools\runtime\tools\smoke-gateway-bat-runtime.ps1','-Root',$RepoRoot,'-ResultDir',(Join-Path $evidenceDir 'gateway-batch-runtime'),'-DbVendor','mariadb')
                }
            }
        }finally{
            Stop-CpfDockerTargetIfOwned 'mariadb' $batchDbState
            Restore-CpfEnvironment $batchDbEnv
        }
    }else{Skip-CpfStage 'RUNTIME_DOCKER_CLOSURE' 'IncludeRuntimeClosure requires pwsh + ready Docker and no SkipDocker'}
}else{Skip-CpfStage 'RUNTIME_DOCKER_CLOSURE' 'IncludeRuntimeClosure not requested'}

# 7. 기본 로컬 Runtime은 1 WAS만. Batch/다중 WAS는 기본으로 띄우지 않는다.
if(-not $SkipOneWas -and $pwsh){
    Invoke-CpfStage 'LOCAL_ONE_WAS_START' $pwsh @('-NoProfile','-File','.\cpf-tools\runtime\tools\start-cpf-local.ps1','-RepoRoot',$RepoRoot,'-Mode','integrated','-ResourceProfile',$ResourceProfile,'-WebOnly')
    $oneWasReady=($summary[$summary.Count-1].status -eq 'PASS')
    if($oneWasReady){
        $integratedLogRoot=Join-Path $evidenceDir 'integrated-logging'
        $fileLogEvidence=Join-Path $integratedLogRoot 'file'
        $policyLogEvidence=Join-Path $integratedLogRoot 'policy'
        [IO.Directory]::CreateDirectory($fileLogEvidence)|Out-Null
        [IO.Directory]::CreateDirectory($policyLogEvidence)|Out-Null
        Invoke-CpfStage 'LOCAL_FILE_LOG_STANDARD' $pwsh @('-NoProfile','-File','.\cpf-tools\runtime\tools\smoke-file-log-standard-runtime.ps1','-Root',$RepoRoot,'-EducationBaseUrl','http://127.0.0.1:8080','-ResultDir',$fileLogEvidence,'-LogBasePath',(Join-Path $RepoRoot 'logs'),'-RequireRuntime')
        $localSecretPrevious=@{}
        if(Test-Path -LiteralPath $DockerSecretFile -PathType Leaf){$localSecretPrevious=Import-CpfEnvFile $DockerSecretFile}
        try{
            $admPassword=[Environment]::GetEnvironmentVariable('CPF_ADMIN_PASSWORD','Process')
            if([string]::IsNullOrWhiteSpace($admPassword)){$admPassword=[Environment]::GetEnvironmentVariable('CPF_ADM_SMOKE_PASSWORD','Process')}
            if([string]::IsNullOrWhiteSpace($admPassword)){
                Skip-CpfStage 'LOCAL_DB_LOG_POLICY_RUNTIME' 'CPF_ADMIN_PASSWORD / CPF_ADM_SMOKE_PASSWORD not available; password is never placed on command line'
                Skip-CpfStage 'LOCAL_INTEGRATED_LOG_CORRELATION' 'ADM local credential unavailable'
            }else{
                $secretEnv=@{CPF_ADM_SMOKE_PASSWORD=$admPassword;CPF_ADMIN_PASSWORD=$admPassword}
                Invoke-CpfStage 'LOCAL_DB_LOG_POLICY_RUNTIME' $pwsh @('-NoProfile','-File','.\cpf-tools\runtime\tools\smoke-log-policy-runtime.ps1','-Root',$RepoRoot,'-AdmBaseUrl','http://127.0.0.1:8080','-AdmUsername','admin','-LogDir',$policyLogEvidence) $RepoRoot $secretEnv
                Invoke-CpfStage 'LOCAL_INTEGRATED_LOG_CORRELATION' $pwsh @('-NoProfile','-File','.\cpf-tools\runtime\tools\smoke-integrated-log-correlation.ps1','-Root',$RepoRoot,'-BaseUrl','http://127.0.0.1:8080','-LogBasePath',(Join-Path $RepoRoot 'logs'),'-RuntimeLogRoot',(Join-Path $RepoRoot 'build\cpf-local-runtime\logs'),'-FileLogResultPath',(Join-Path $fileLogEvidence 'file-log-standard-result.json'),'-LogPolicyResultPath',(Join-Path $policyLogEvidence 'log-policy-runtime-smoke-result.json'),'-AdmUsername','admin','-ResultPath',(Join-Path $integratedLogRoot 'integrated-log-correlation-result.json')) $RepoRoot $secretEnv
            }
        }finally{Restore-CpfEnvironment $localSecretPrevious}
    }else{
        Add-CpfTextResult 'LOCAL_FILE_LOG_STANDARD' 'NOT_EXECUTED' 'LOCAL_ONE_WAS_START failed; integrated logging requires a live transaction' 'upstream runtime start failed'
        Add-CpfTextResult 'LOCAL_DB_LOG_POLICY_RUNTIME' 'NOT_EXECUTED' 'LOCAL_ONE_WAS_START failed; DB log policy runtime was not executed' 'upstream runtime start failed'
        Add-CpfTextResult 'LOCAL_INTEGRATED_LOG_CORRELATION' 'NOT_EXECUTED' 'LOCAL_ONE_WAS_START failed; FileLog/DB/ADM correlation was not executed' 'upstream runtime start failed'
    }
    if($node -and $python -and $oneWasReady){
        Invoke-CpfStage 'ADM_RUNTIME_OPENAPI_RELEASE' $pwsh @('-NoProfile','-File','.\cpf-tools\contracts\openapi\verify-cpf-runtime-openapi-release.ps1','-Module','ADM','-BaseUrl','http://127.0.0.1:8080','-Root',$RepoRoot,'-EvidenceDirectory',(Join-Path $evidenceDir 'runtime-openapi\adm'),'-SourceIdentity',$sourceIdentity)
        Invoke-CpfStage 'BZA_RUNTIME_OPENAPI_RELEASE' $pwsh @('-NoProfile','-File','.\cpf-tools\contracts\openapi\verify-cpf-runtime-openapi-release.ps1','-Module','BZA','-BaseUrl','http://127.0.0.1:8080','-Root',$RepoRoot,'-EvidenceDirectory',(Join-Path $evidenceDir 'runtime-openapi\bza'),'-SourceIdentity',$sourceIdentity)
    }elseif(-not $oneWasReady){
        Add-CpfTextResult 'ADM_RUNTIME_OPENAPI_RELEASE' 'NOT_EXECUTED' 'LOCAL_ONE_WAS_START failed; ADM runtime OpenAPI parity was not executed' 'upstream runtime start failed'
        Add-CpfTextResult 'BZA_RUNTIME_OPENAPI_RELEASE' 'NOT_EXECUTED' 'LOCAL_ONE_WAS_START failed; BZA runtime OpenAPI parity was not executed' 'upstream runtime start failed'
    }else{
        Skip-CpfStage 'RUNTIME_OPENAPI_RELEASE' 'node/python unavailable'
    }
    if($IncludeBrowserE2E -and $oneWasReady){
        Invoke-CpfStage 'ADM_BROWSER_E2E_SMOKE' $pwsh @('-NoProfile','-File','.\cpf-tools\verification\tools\smoke-adm-ui.ps1','-Root',$RepoRoot,'-AdmBaseUrl','http://127.0.0.1:8080','-LogDir',(Join-Path $evidenceDir 'browser'),'-BrowserClick')
        Invoke-CpfStage 'BZA_BROWSER_STATIC' $pwsh @('-NoProfile','-File','.\cpf-tools\verification\tools\smoke-bza-ui.ps1','-Root',$RepoRoot,'-ResultDir',(Join-Path $evidenceDir 'browser'))
        if($npm -and $frontendSandboxes.ContainsKey('ADM')){
            $admSandbox=[string]$frontendSandboxes['ADM']
            Invoke-CpfStage 'ADM_PLAYWRIGHT_E2E' $npm @('run','test:e2e') $admSandbox @{NODE_OPTIONS=(Get-CpfNodeOptions 'cpf-admin');CPF_SOURCE_SHA=$env:CPF_SOURCE_SHA;CPF_ADM_BASE_URL='http://127.0.0.1:8080'}
            Invoke-CpfStage 'ADM_PLAYWRIGHT_A11Y' $npm @('run','test:a11y') $admSandbox @{NODE_OPTIONS=(Get-CpfNodeOptions 'cpf-admin');CPF_SOURCE_SHA=$env:CPF_SOURCE_SHA;CPF_ADM_BASE_URL='http://127.0.0.1:8080'}
        }else{Skip-CpfStage 'ADM_PLAYWRIGHT_E2E' 'ADM frontend sandbox/npm unavailable'}
        if($npm -and $frontendSandboxes.ContainsKey('BZA')){
            $bzaSandbox=[string]$frontendSandboxes['BZA']
            Invoke-CpfStage 'BZA_PLAYWRIGHT_E2E' $npm @('run','test:e2e') $bzaSandbox @{NODE_OPTIONS=(Get-CpfNodeOptions 'cpf-biz-admin');CPF_SOURCE_SHA=$env:CPF_SOURCE_SHA;CPF_BZA_BASE_URL='http://127.0.0.1:8080'}
            Invoke-CpfStage 'BZA_PLAYWRIGHT_A11Y' $npm @('run','test:a11y') $bzaSandbox @{NODE_OPTIONS=(Get-CpfNodeOptions 'cpf-biz-admin');CPF_SOURCE_SHA=$env:CPF_SOURCE_SHA;CPF_BZA_BASE_URL='http://127.0.0.1:8080'}
        }else{Skip-CpfStage 'BZA_PLAYWRIGHT_E2E' 'BZA frontend sandbox/npm unavailable'}
    }elseif(-not $IncludeBrowserE2E){Skip-CpfStage 'BROWSER_E2E' 'IncludeBrowserE2E not requested'}
    else{Add-CpfTextResult 'BROWSER_E2E' 'NOT_EXECUTED' 'LOCAL_ONE_WAS_START failed; Browser E2E was not executed' 'upstream runtime start failed'}
    if($FullLocal -and $python -and $oneWasReady){
        $perfProfile=Join-Path $RepoRoot 'cpf-tools\testing\performance\cpf-performance-profile.json'
        $livePerf=@(
            @{id='broker-backpressure'; env='CPF_PERF_BROKER_PROBE_URL'},
            @{id='batch-reconcile'; env='CPF_PERF_BATCH_PROBE_URL'},
            @{id='resource-budget'; env='CPF_PERF_RESOURCE_PROBE_URL'}
        )
        foreach($item in $livePerf){
            $probe=[Environment]::GetEnvironmentVariable([string]$item.env,'Process')
            $stage="PERFORMANCE_"+([string]$item.id).ToUpperInvariant().Replace('-','_')+"_LIVE"
            if([string]::IsNullOrWhiteSpace($probe)){Skip-CpfStage $stage ("live product probe is not configured: "+$item.env)}
            else{Invoke-CpfStage $stage $python @('.\cpf-tools\testing\tools\run-cpf-performance-contract.py','--profile',$perfProfile,'--workload',[string]$item.id,'--output-json',(Join-Path $evidenceDir ("performance-"+$item.id+"-live.json"))) $RepoRoot @{CPF_EXPECTED_HEAD=$sourceIdentity}}
        }
        if($IncludePerformanceLoad){
            $admHealth=[Environment]::GetEnvironmentVariable('CPF_PERF_ADM_HEALTH_URL','Process')
            if([string]::IsNullOrWhiteSpace($admHealth)){Skip-CpfStage 'PERFORMANCE_ADM_API_LOAD_LIVE' 'CPF_PERF_ADM_HEALTH_URL is not configured'}
            else{
                foreach($workload in @('adm-api-load','adm-api-soak')){Invoke-CpfStage ("PERFORMANCE_"+$workload.ToUpperInvariant().Replace('-','_')+"_LIVE") $python @('.\cpf-tools\testing\tools\run-cpf-performance-contract.py','--profile',$perfProfile,'--workload',$workload,'--output-json',(Join-Path $evidenceDir ("performance-$workload-live.json")))}
            }
        }
    }elseif($FullLocal -and -not $oneWasReady){
        Add-CpfTextResult 'PERFORMANCE_LIVE' 'NOT_EXECUTED' 'LOCAL_ONE_WAS_START failed; live performance probes were not executed' 'upstream runtime start failed'
    }
    Invoke-CpfStage 'LOCAL_ONE_WAS_STOP' $pwsh @('-NoProfile','-File','.\cpf-tools\runtime\tools\stop-cpf-local.ps1','-RepoRoot',$RepoRoot)
}elseif($SkipOneWas){Skip-CpfStage 'LOCAL_ONE_WAS_RUNTIME' 'SkipOneWas requested'}else{Skip-CpfStage 'LOCAL_ONE_WAS_RUNTIME' 'pwsh/powershell missing'}

if($python){Invoke-CpfStage 'SUPPLY_CHAIN' $python @('.\cpf-tools\supply-chain\tools\verify-cpf-supply-chain.py','--root','.')}

$sourceStateAfter=Get-CpfTreeState 'source' (Join-Path $evidenceDir 'source-state-after.json')
$sourceStable=$sourceStateReady -and ([string]$sourceStateAfter.contentSha256 -eq $sourceContentSha256)
Add-CpfTextResult 'SOURCE_STATE_AFTER' $(if($sourceStable){'PASS'}else{'FAIL'}) "AFTER_SHA256=$($sourceStateAfter.contentSha256)`nBEFORE_SHA256=$sourceContentSha256`nSOURCE_FILES=$($sourceStateAfter.fileCount)" $(if($sourceStable){'validation did not change product source bytes'}else{'validation changed product source bytes'})
$managedStateAfter=Get-CpfTreeState 'managed' (Join-Path $evidenceDir 'managed-state-after.json')
$stable=$managedStateReady -and ([string]$managedStateBefore.contentSha256 -eq [string]$managedStateAfter.contentSha256)
Add-CpfTextResult 'MANAGED_STATE_AFTER' $(if($stable){'PASS'}else{'FAIL'}) "AFTER_SHA256=$($managedStateAfter.contentSha256)`nBEFORE_SHA256=$($managedStateBefore.contentSha256)`nMANAGED_FILES=$($managedStateAfter.fileCount)" $(if($stable){'validation did not change managed repository files'}else{'validation changed managed repository files'})

$summaryCsv=Join-Path $resultDir 'SUMMARY.csv'
$summaryJson=Join-Path $resultDir 'SUMMARY.json'
$summaryTxt=Join-Path $resultDir 'SUMMARY.txt'
$summary | Export-Csv -LiteralPath $summaryCsv -NoTypeInformation -Encoding UTF8
[IO.File]::WriteAllText($summaryJson,($summary|ConvertTo-Json -Depth 20)+"`n",$utf8)
$pass=@($summary|Where-Object status -eq 'PASS').Count
$fail=@($summary|Where-Object status -eq 'FAIL').Count
$skip=@($summary|Where-Object status -eq 'SKIP_ENV').Count
$notExecuted=@($summary|Where-Object status -eq 'NOT_EXECUTED').Count
$lines=[Collections.Generic.List[string]]::new()
$lines.Add("CPF_LOCAL_VALIDATION=$stamp")
$lines.Add("RESOURCE_PROFILE=$ResourceProfile")
$lines.Add("FULL_LOCAL=$([bool]$FullLocal)")
$lines.Add("DESTRUCTIVE_DB_ROLLBACK=$([bool]$AllowDestructiveDbRollback)")
$lines.Add("BASELINE_SHA=$baselineSha")
$lines.Add("RESULT_CONTENT_SHA1=$sourceIdentity")
$lines.Add("RESULT_CONTENT_SHA256=$sourceContentSha256")
$lines.Add("RESULT_SOURCE_IDENTITY=$env:CPF_SOURCE_IDENTITY")
$lines.Add("PASS=$pass FAIL=$fail SKIP_ENV=$skip NOT_EXECUTED=$notExecuted TOTAL=$($summary.Count)")
$lines.Add("DOCKER_ALL_PRESTART_REQUIRED=false")
$lines.Add('')
foreach($row in $summary){$lines.Add("[$($row.status)] $($row.name) rc=$($row.exitCode) sec=$($row.seconds) note=$($row.note) log=$($row.log)")}
[IO.File]::WriteAllText($summaryTxt,($lines -join "`n")+"`n",$utf8)

Ensure-CpfResultDirectories
[IO.Directory]::CreateDirectory($finalResultDir)|Out-Null
Copy-Item -Path (Join-Path $resultDir '*') -Destination $finalResultDir -Recurse -Force
$zip=Join-Path $OutputRoot "CPF_LOCAL_VALIDATION_$stamp.zip"
if(Test-Path -LiteralPath $zip){Remove-Item -LiteralPath $zip -Force}
Compress-Archive -Path (Join-Path $finalResultDir '*') -DestinationPath $zip -CompressionLevel Optimal
$zipSha=(Get-FileHash -LiteralPath $zip -Algorithm SHA256).Hash.ToLowerInvariant()
[IO.File]::WriteAllText("$zip.sha256.txt","$zipSha  $([IO.Path]::GetFileName($zip))`n",$utf8)
Write-Host ''
Write-Host "CPF LOCAL VALIDATION COMPLETE: PASS=$pass FAIL=$fail SKIP_ENV=$skip NOT_EXECUTED=$notExecuted" -ForegroundColor Cyan
Write-Host "RESULT_DIR=$finalResultDir"
Write-Host "CPF_LOCAL_VALIDATION_ZIP=$zip"
Write-Host "ZIP_SHA256=$zipSha"
# Collect every stage and ZIP first; automation-safe mode reports failure only after evidence is preserved.
$strictExitEffective=[bool]$StrictExit -or [bool]$FullLocal
if($strictExitEffective -and $fail -gt 0){exit 1}
