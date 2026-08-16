param(
    [string]$RepoRoot = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [int]$ControlPlaneCount = 2,
    [int]$SchedulerCount = 2,
    [int]$WorkerCount = 2,
    [int]$CenterCutCount = 2,
    [int]$AgentCount = 1
)
$ErrorActionPreference='Stop'
$runRoot=Join-Path $RepoRoot 'build\bat-local-runtime';New-Item -ItemType Directory -Force -Path $runRoot|Out-Null
function Invoke-Gradle([string[]]$Args){if($IsWindows -or(Test-Path (Join-Path $RepoRoot 'gradlew.bat'))){& (Join-Path $RepoRoot 'gradlew.bat') @Args}else{& (Join-Path $RepoRoot 'gradlew') @Args};if($LASTEXITCODE-ne0){throw "Gradle failed: $($Args -join ' ')"}}
function Wait-Health([string]$Url,[int]$Seconds=60){$until=(Get-Date).AddSeconds($Seconds);while((Get-Date)-lt$until){try{$r=Invoke-RestMethod -Uri $Url -TimeoutSec 3;if(($r.status -eq 'UP')-or($r.status -eq 'up')){return}}catch{};Start-Sleep -Milliseconds 500};throw "Readiness timeout: $Url"}
$services=@(
 @{Module='control-plane';Artifact='cpf-batch-control-plane';Role='CONTROL_PLANE';BasePort=8180;Count=$ControlPlaneCount},
 @{Module='scheduler';Artifact='cpf-batch-scheduler';Role='SCHEDULER';BasePort=8190;Count=$SchedulerCount},
 @{Module='worker';Artifact='cpf-batch-worker';Role='WORKER';BasePort=8200;Count=$WorkerCount},
 @{Module='center-cut';Artifact='cpf-batch-center-cut';Role='CENTER_CUT';BasePort=8210;Count=$CenterCutCount},
 @{Module='agent';Artifact='cpf-batch-agent';Role='AGENT';BasePort=8220;Count=$AgentCount}
)
Push-Location $RepoRoot
try{
 Invoke-Gradle @(':runtime:batch:verifyStandaloneArtifacts','--no-daemon')
 $sha=(git rev-parse HEAD).Trim();$registry=@()
 foreach($s in $services){for($i=1;$i-le$s.Count;$i++){
  $port=$s.BasePort+$i-1;$id="$($s.Artifact)-local-{0:d2}"-f$i
  $jar=Get-ChildItem (Join-Path $RepoRoot "cpf-batch\$($s.Module)\build\libs") -File -Filter "$($s.Artifact)-*.jar"|Where-Object{$_.Name-notmatch'plain'}|Select-Object -First 1
  if(-not$jar){throw "Jar not found: $($s.Artifact)"}
  $psi=[Diagnostics.ProcessStartInfo]::new();$psi.FileName=if($env:JAVA_HOME){Join-Path $env:JAVA_HOME 'bin\java.exe'}else{'java'};$psi.UseShellExecute=$false
  [void]$psi.ArgumentList.Add('-jar');[void]$psi.ArgumentList.Add($jar.FullName)
  $psi.Environment['CPF_INSTANCE_ID']=$id;$psi.Environment['CPF_PORT']="$port";$psi.Environment['CPF_WAS_ID']="BAT-$($s.Role)-$i";$psi.Environment['CPF_GIT_SHA']=$sha;$psi.Environment['SPRING_PROFILES_ACTIVE']='local';$psi.Environment['CPF_LOG_ROOT']=(Join-Path $runRoot 'logs');$psi.Environment['CPF_BATCH_CONTROL_BASE_URL']='http://127.0.0.1:8180'
  $psi.RedirectStandardOutput=$true;$psi.RedirectStandardError=$true;$p=[Diagnostics.Process]::new();$p.StartInfo=$psi;[void]$p.Start()
  $registry+=[pscustomobject]@{instanceId=$id;role=$s.Role;pid=$p.Id;port=$port;health="http://127.0.0.1:$port/actuator/health/readiness";jar=$jar.FullName}
 }}
 $registry|ConvertTo-Json -Depth 6|Set-Content -Encoding UTF8 (Join-Path $runRoot 'process-registry.json')
 foreach($r in $registry){Wait-Health $r.health}
 $registry|Format-Table -AutoSize
} catch {
 if(Test-Path (Join-Path $runRoot 'process-registry.json')){& (Join-Path $PSScriptRoot 'stop-bat-local-distributed.ps1') -RepoRoot $RepoRoot}
 throw
} finally {Pop-Location}
