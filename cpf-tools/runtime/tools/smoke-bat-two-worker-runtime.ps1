param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $ResultDir = "",
    [string] $HostName = $(if ($env:CPF_DB_HOST) { $env:CPF_DB_HOST } else { 'localhost' }),
    [string] $Port = $(if ($env:CPF_DB_PORT) { $env:CPF_DB_PORT } else { '3306' }),
    [string] $Username = $(if ($env:CPF_DB_ROOT_USERNAME) { $env:CPF_DB_ROOT_USERNAME } else { 'root' }),
    [string] $Password = $(if ($env:CPF_DB_ROOT_PASSWORD) { $env:CPF_DB_ROOT_PASSWORD } elseif ($env:CPF_ADMIN_PASSWORD) { $env:CPF_ADMIN_PASSWORD } else { '' }),
    [string] $RuntimeUsername = $(if ($env:CPF_DB_RUNTIME_USERNAME) { $env:CPF_DB_RUNTIME_USERNAME } else { $Username }),
    [string] $RuntimePassword = $(if ($env:CPF_DB_RUNTIME_PASSWORD) { $env:CPF_DB_RUNTIME_PASSWORD } else { $Password }),
    [string] $DatabaseName = $(if ($env:CPF_DB_DATABASE) { $env:CPF_DB_DATABASE } else { 'cpfDB' }),
    [string] $DbVendor = $(if ($env:CPF_DB_VENDOR) { $env:CPF_DB_VENDOR } else { 'mariadb' }),
    [string] $DbResourceRoot = $env:CPF_DB_RESOURCE_ROOT,
    [string] $ClientPath = $env:CPF_MARIADB_CLI,
    [ValidateSet('Auto','Host','Docker')]
    [string] $ClientAdapter = 'Auto',
    [string] $MariaDbContainer = 'cpf-mariadb',
    [int] $ControlPlanePort = 8279
)
$ErrorActionPreference='Stop'
$Utf8NoBom=[Text.UTF8Encoding]::new($false)
if($DatabaseName -notmatch '^[A-Za-z][A-Za-z0-9_$]{0,63}$'){throw 'DatabaseName is not a safe MariaDB identifier'}
if($DbVendor.ToLowerInvariant()-ne'mariadb'){throw 'The MariaDB two-worker smoke requires DbVendor=mariadb'}
if([string]::IsNullOrWhiteSpace($DbResourceRoot)){$DbResourceRoot=Join-Path $Root 'cpf-tools/db/vendor/mariadb'}
elseif(-not[IO.Path]::IsPathRooted($DbResourceRoot)){$DbResourceRoot=Join-Path $Root $DbResourceRoot}
$DbResourceRoot=[IO.Path]::GetFullPath($DbResourceRoot)
if(-not(Test-Path -LiteralPath (Join-Path $DbResourceRoot 'pack.json') -PathType Leaf)){throw "Canonical MariaDB Vendor Pack is missing: $DbResourceRoot"}
if([string]::IsNullOrWhiteSpace($ResultDir)){$ResultDir=Join-Path $Root 'cpf-docs/work/evidence/generated/runtime/batch-two-worker'}
elseif(-not[IO.Path]::IsPathRooted($ResultDir)){$ResultDir=Join-Path $Root $ResultDir}
New-Item -ItemType Directory -Force -Path $ResultDir|Out-Null
$runtimeDir=Join-Path $ResultDir 'processes';New-Item -ItemType Directory -Force -Path $runtimeDir|Out-Null
$resultPath=Join-Path $ResultDir 'bat-two-worker-runtime.sanitized.json';$processes=@{};$processStreams=@{}
$result=[ordered]@{startedAt=(Get-Date).ToString('o');status='FAILED';clientAdapter=$null;controlPlane=@{};registration=@{};distribution=@{};drain=@{};crashUnknown=@{};mismatch=@{}}
function Resolve-Client {
 if($ClientAdapter -ne 'Docker'){
  if($ClientPath -and(Test-Path $ClientPath)){return [pscustomobject]@{mode='Host';path=$ClientPath}}
  $c=Get-Command mariadb -ErrorAction SilentlyContinue
  if($c){return [pscustomobject]@{mode='Host';path=$c.Source}}
  if($ClientAdapter -eq 'Host'){throw 'MariaDB host CLI not found'}
 }
 $docker=Get-Command docker -ErrorAction SilentlyContinue
 if($docker){
  $running=(& $docker.Source inspect --format '{{.State.Running}}' $MariaDbContainer 2>$null|Out-String).Trim()
  if($LASTEXITCODE-eq0-and$running-eq'true'){return [pscustomobject]@{mode='Docker';path=$docker.Source}}
 }
 throw 'MariaDB client unavailable: install host mariadb CLI or run cpf-mariadb container'
}
function Invoke-Sql([string]$Sql){
 $psi=[Diagnostics.ProcessStartInfo]::new();$psi.UseShellExecute=$false;$psi.RedirectStandardInput=$true;$psi.RedirectStandardOutput=$true;$psi.RedirectStandardError=$true
 if($script:MariaClient.mode-eq'Docker'){
  $psi.FileName=$script:MariaClient.path
  foreach($a in @('exec','-i',$MariaDbContainer,'sh','-lc',"MYSQL_PWD=`"`$MARIADB_ROOT_PASSWORD`" mariadb --protocol=tcp --host=127.0.0.1 --port=3306 --user=$Username --ssl=0 --batch --skip-column-names --default-character-set=utf8mb4")){[void]$psi.ArgumentList.Add($a)}
 }else{
  $psi.FileName=$script:MariaClient.path
  foreach($a in @("--protocol=tcp","--host=$HostName","--port=$Port","--user=$Username",'--ssl=0','--batch','--skip-column-names','--default-character-set=utf8mb4')){[void]$psi.ArgumentList.Add($a)}
  $psi.Environment['MYSQL_PWD']=$Password;$psi.Environment['MARIADB_PWD']=$Password
 }
 $p=[Diagnostics.Process]::Start($psi);$p.StandardInput.Write($Sql);$p.StandardInput.Write("`n");$p.StandardInput.Close();$o=$p.StandardOutput.ReadToEnd();$e=$p.StandardError.ReadToEnd();$p.WaitForExit();if($p.ExitCode-ne0){throw "MariaDB query failed: $e"};return $o.Trim()
}
function Wait-Until([scriptblock]$Condition,[int]$Seconds,[string]$Message){$until=(Get-Date).AddSeconds($Seconds);while((Get-Date)-lt$until){if(&$Condition){return};Start-Sleep -Milliseconds 300};throw $Message}
function Save-WorkerStreams([string]$WorkerId){
 $stream=$processStreams[$WorkerId];if(-not$stream-or$stream.saved){return}
 $process=$processes[$WorkerId];if($process-and-not$process.HasExited){return}
 $stdout=$stream.stdoutTask.GetAwaiter().GetResult();$stderr=$stream.stderrTask.GetAwaiter().GetResult()
 [IO.File]::WriteAllText($stream.stdoutPath,$stdout,$Utf8NoBom);[IO.File]::WriteAllText($stream.stderrPath,$stderr,$Utf8NoBom);$stream.saved=$true
}
function Assert-WorkersRunning {
 foreach($workerId in @($processes.Keys)){
  $process=$processes[$workerId]
  if($process.HasExited){Save-WorkerStreams $workerId;$stream=$processStreams[$workerId];throw "Worker $workerId exited before registration ExitCode=$($process.ExitCode) stdout=$($stream.stdoutPath) stderr=$($stream.stderrPath)"}
 }
}
function Test-HttpOk([string]$Uri){
 try{$response=Invoke-WebRequest -Method Get -Uri $Uri -TimeoutSec 3 -SkipHttpErrorCheck;return $response.StatusCode-eq200}catch{return $false}
}
function Start-ControlPlane {
 $jar=Get-ChildItem (Join-Path $Root 'cpf-batch/control-plane/build/libs') -File -Filter 'cpf-batch-control-plane-*.jar'|?{$_.Name-notmatch'plain'}|Select-Object -First 1
 if(-not$jar){throw 'Control Plane bootJar missing'}
 $processId='bat-smoke-control-plane';$psi=[Diagnostics.ProcessStartInfo]::new();$psi.FileName=if($env:JAVA_HOME){Join-Path $env:JAVA_HOME 'bin/java.exe'}else{'java'};$psi.UseShellExecute=$false;$psi.RedirectStandardOutput=$true;$psi.RedirectStandardError=$true
 $crashDir=Join-Path $runtimeDir $processId;New-Item -ItemType Directory -Force -Path $crashDir|Out-Null
 foreach($a in @('-Xms128m','-Xmx512m','-XX:+HeapDumpOnOutOfMemoryError',"-XX:HeapDumpPath=$crashDir",("-XX:ErrorFile="+(Join-Path $crashDir 'hs_err_pid%p.log')),'-jar',$jar.FullName,"--server.port=$ControlPlanePort",'--spring.profiles.active=local')){[void]$psi.ArgumentList.Add($a)}
 $psi.Environment['CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CPF_PLATFORM_DB_ENABLED']='true';$psi.Environment['CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CPF_PLATFORM_DB_URL']="jdbc:mariadb://${HostName}:${Port}/${DatabaseName}";$psi.Environment['CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CPF_PLATFORM_DB_USERNAME']=$RuntimeUsername;$psi.Environment['CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CPF_PLATFORM_DB_PASSWORD']=$RuntimePassword;$psi.Environment['CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CPF_PLATFORM_DB_DRIVER_CLASS_NAME']='org.mariadb.jdbc.Driver';$psi.Environment['CPF_RUNTIME_INSTANCE_ID']='bat-smoke-control-instance';$psi.Environment['CPF_WAS_ID']='bat-smoke-control-instance';$psi.Environment['CPF_BATCH_CONTROL_BASE_URL']="http://127.0.0.1:$ControlPlanePort";$psi.Environment['CPF_LOG_ROOT']=(Join-Path $runtimeDir 'logs');$psi.Environment['CPF_DOMAIN_PERSISTENCE_PROVIDER']='jdbc';$psi.Environment['CPF_DB_VENDOR']=$DbVendor.ToLowerInvariant();$psi.Environment['CPF_DB_RESOURCE_ROOT']=$DbResourceRoot
 $p=[Diagnostics.Process]::new();$p.StartInfo=$psi;[void]$p.Start();$processes[$processId]=$p;$processStreams[$processId]=[pscustomobject]@{stdoutPath=(Join-Path $crashDir 'control-plane.stdout.log');stderrPath=(Join-Path $crashDir 'control-plane.stderr.log');stdoutTask=$p.StandardOutput.ReadToEndAsync();stderrTask=$p.StandardError.ReadToEndAsync();saved=$false};return $p
}
function Start-Worker([string]$WorkerId,[string]$InstanceId,[int]$ServerPort){
 $jar=Get-ChildItem (Join-Path $Root 'cpf-batch/worker/build/libs') -File -Filter 'worker-*.jar'|?{$_.Name-notmatch'plain'}|Select-Object -First 1
 if(-not$jar){$jar=Get-ChildItem (Join-Path $Root 'cpf-batch/worker/build/libs') -File -Filter '*.jar'|?{$_.Name-notmatch'plain'}|Select-Object -First 1};if(-not$jar){throw 'Worker bootJar missing'}
 $psi=[Diagnostics.ProcessStartInfo]::new();$psi.FileName=if($env:JAVA_HOME){Join-Path $env:JAVA_HOME 'bin/java.exe'}else{'java'};$psi.UseShellExecute=$false;$psi.RedirectStandardOutput=$true;$psi.RedirectStandardError=$true
 $workerCrashDir=Join-Path $runtimeDir $WorkerId;New-Item -ItemType Directory -Force -Path $workerCrashDir|Out-Null
 foreach($a in @('-Xms128m','-Xmx512m','-XX:+HeapDumpOnOutOfMemoryError',"-XX:HeapDumpPath=$workerCrashDir",("-XX:ErrorFile="+(Join-Path $workerCrashDir 'hs_err_pid%p.log')),'-jar',$jar.FullName,"--server.port=$ServerPort",'--spring.profiles.active=local','--cpf.batch.diagnostic.enabled=true','--cpf.batch.worker.lease-seconds=12','--cpf.batch.worker.poll-ms=200','--cpf.batch.worker.heartbeat-ms=500','--cpf.batch.worker.recovery-ms=500')){[void]$psi.ArgumentList.Add($a)}
 $psi.Environment['CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CPF_PLATFORM_DB_ENABLED']='true';$psi.Environment['CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CPF_PLATFORM_DB_URL']="jdbc:mariadb://${HostName}:${Port}/${DatabaseName}";$psi.Environment['CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CPF_PLATFORM_DB_USERNAME']=$RuntimeUsername;$psi.Environment['CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CPF_PLATFORM_DB_PASSWORD']=$RuntimePassword;$psi.Environment['CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CPF_PLATFORM_DB_DRIVER_CLASS_NAME']='org.mariadb.jdbc.Driver';$psi.Environment['CPF_RUNTIME_INSTANCE_ID']=$InstanceId;$psi.Environment['CPF_WAS_ID']=$InstanceId;$psi.Environment['CPF_BAT_WORKER_ID']=$WorkerId;$psi.Environment['CPF_BAT_WORKER_VERSION']='runtime-smoke-v1';$psi.Environment['CPF_BAT_WORKER_CAPABILITIES']='CPF_BAT_DIAGNOSTIC';$psi.Environment['CPF_BATCH_CONTROL_BASE_URL']="http://127.0.0.1:$ControlPlanePort";$psi.Environment['CPF_LOG_ROOT']=(Join-Path $runtimeDir 'logs')
 $psi.Environment['CPF_DOMAIN_PERSISTENCE_PROVIDER']='jdbc';$psi.Environment['CPF_DB_VENDOR']=$DbVendor.ToLowerInvariant();$psi.Environment['CPF_DB_RESOURCE_ROOT']=$DbResourceRoot
 $p=[Diagnostics.Process]::new();$p.StartInfo=$psi;[void]$p.Start();$processes[$WorkerId]=$p
 $processStreams[$WorkerId]=[pscustomobject]@{stdoutPath=(Join-Path $workerCrashDir 'worker.stdout.log');stderrPath=(Join-Path $workerCrashDir 'worker.stderr.log');stdoutTask=$p.StandardOutput.ReadToEndAsync();stderrTask=$p.StandardError.ReadToEndAsync();saved=$false}
 return $p
}
try{
 if([string]::IsNullOrWhiteSpace($Password)){throw 'CPF_DB_ROOT_PASSWORD or CPF_ADMIN_PASSWORD is required'}
 if([string]::IsNullOrWhiteSpace($RuntimeUsername)-or[string]::IsNullOrWhiteSpace($RuntimePassword)){throw 'Batch Worker runtime database credentials are required'}
 $script:MariaClient=Resolve-Client;$result.clientAdapter=$script:MariaClient.mode
 [void](Invoke-Sql @"
USE $DatabaseName;
DELETE FROM BAT_EXECUTION_LEASE WHERE execution_id IN (SELECT execution_id FROM BAT_EXECUTION WHERE created_by='BAT_TWO_WORKER_SMOKE');
DELETE FROM BAT_EXECUTION WHERE created_by='BAT_TWO_WORKER_SMOKE';
DELETE FROM BAT_WORKER WHERE worker_id IN ('bat-smoke-worker-1','bat-smoke-worker-2');
INSERT INTO BAT_JOB(job_id,job_name,job_type,description,restartable_yn,use_yn,created_by,updated_by)
VALUES('CPF_BAT_DIAGNOSTIC_JOB','BAT standalone worker diagnostic','TASKLET','local/test multi-instance verification','Y','Y','BAT','BAT')
ON DUPLICATE KEY UPDATE use_yn='Y',updated_by='BAT';
"@)
 $control=Start-ControlPlane
 Wait-Until { Assert-WorkersRunning;Test-HttpOk "http://127.0.0.1:$ControlPlanePort/actuator/health/liveness" } 90 'Control Plane did not become live'
 $result.controlPlane=[ordered]@{status='DONE';port=$ControlPlanePort;liveness=200}
 $w1=Start-Worker 'bat-smoke-worker-1' 'bat-smoke-instance-1' 8282;$w2=Start-Worker 'bat-smoke-worker-2' 'bat-smoke-instance-2' 8283
 Wait-Until { Assert-WorkersRunning;[int](Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.BAT_WORKER WHERE worker_id IN ('bat-smoke-worker-1','bat-smoke-worker-2') AND active_yn='Y';") -eq 2 } 60 'two workers not registered'
 $jobPackCount=[int](Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.BAT_JOB_PACK WHERE job_pack_id='CPF-BAT-DIAGNOSTIC';");$jobCount=[int](Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.BAT_JOB_PACK_JOB WHERE job_pack_id='CPF-BAT-DIAGNOSTIC' AND job_id='CPF_BAT_DIAGNOSTIC_JOB';");if($jobPackCount-ne1-or$jobCount-ne1){throw 'Workers did not register the diagnostic Job Pack through Control Plane'}
 $result.registration=[ordered]@{status='DONE';workerCount=2;jobPackCount=$jobPackCount;jobCount=$jobCount}
 [void](Invoke-Sql @"
USE $DatabaseName;
INSERT INTO BAT_EXECUTION(job_id,job_parameters,execution_status,required_worker_version,required_capability,requested_by,created_by,updated_by)
SELECT 'CPF_BAT_DIAGNOSTIC_JOB',CONCAT('{\"iterations\":4,\"sleepMs\":300,\"seq\":',seq,'}'),'READY','runtime-smoke-v1','CPF_BAT_DIAGNOSTIC','SMOKE','BAT_TWO_WORKER_SMOKE','BAT_TWO_WORKER_SMOKE'
FROM (SELECT 1 seq UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4) s;
"@)
 Wait-Until { [int](Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.BAT_EXECUTION WHERE created_by='BAT_TWO_WORKER_SMOKE' AND requested_by='SMOKE' AND execution_status='COMPLETED';") -eq 4 } 120 'distribution executions did not complete'
 $workers=[int](Invoke-Sql "SELECT COUNT(DISTINCT worker_id) FROM ${DatabaseName}.BAT_EXECUTION_LEASE WHERE execution_id IN (SELECT execution_id FROM ${DatabaseName}.BAT_EXECUTION WHERE created_by='BAT_TWO_WORKER_SMOKE' AND requested_by='SMOKE');");if($workers-ne2){throw 'work was not distributed to both workers'}
 $result.distribution=[ordered]@{status='DONE';distinctWorkers=2;duplicateExecution=0}
 Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8282/internal/v1/worker/drain' -TimeoutSec 10|Out-Null
 [void](Invoke-Sql "INSERT INTO ${DatabaseName}.BAT_EXECUTION(job_id,job_parameters,execution_status,required_worker_version,required_capability,requested_by,created_by,updated_by) VALUES('CPF_BAT_DIAGNOSTIC_JOB','{\"iterations\":2,\"sleepMs\":200}','READY','runtime-smoke-v1','CPF_BAT_DIAGNOSTIC','DRAIN','BAT_TWO_WORKER_SMOKE','BAT_TWO_WORKER_SMOKE');")
 $drainId=[long](Invoke-Sql "SELECT MAX(execution_id) FROM ${DatabaseName}.BAT_EXECUTION WHERE requested_by='DRAIN';");Wait-Until { (Invoke-Sql "SELECT execution_status FROM ${DatabaseName}.BAT_EXECUTION WHERE execution_id=$drainId;") -eq 'COMPLETED' } 60 'drain execution not completed';$drainOwner=Invoke-Sql "SELECT worker_id FROM ${DatabaseName}.BAT_EXECUTION_LEASE WHERE execution_id=$drainId;";if($drainOwner-ne'bat-smoke-worker-2'){throw 'draining worker accepted new work'};$result.drain=[ordered]@{status='DONE';drainingWorkerNewClaimCount=0}
 Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8282/internal/v1/worker/resume' -TimeoutSec 10|Out-Null
 [void](Invoke-Sql "INSERT INTO ${DatabaseName}.BAT_EXECUTION(job_id,job_parameters,execution_status,required_worker_version,required_capability,requested_by,created_by,updated_by) VALUES('CPF_BAT_DIAGNOSTIC_JOB','{\"iterations\":200,\"sleepMs\":200}','READY','runtime-smoke-v1','CPF_BAT_DIAGNOSTIC','CRASH','BAT_TWO_WORKER_SMOKE','BAT_TWO_WORKER_SMOKE');")
 $crashId=[long](Invoke-Sql "SELECT MAX(execution_id) FROM ${DatabaseName}.BAT_EXECUTION WHERE requested_by='CRASH';");Wait-Until { (Invoke-Sql "SELECT lease_status FROM ${DatabaseName}.BAT_EXECUTION_LEASE WHERE execution_id=$crashId;") -eq 'RUNNING' } 30 'crash execution not running';$owner=Invoke-Sql "SELECT worker_id FROM ${DatabaseName}.BAT_EXECUTION_LEASE WHERE execution_id=$crashId;";Stop-Process -Id $processes[$owner].Id -Force
 Wait-Until { (Invoke-Sql "SELECT execution_status FROM ${DatabaseName}.BAT_EXECUTION WHERE execution_id=$crashId;") -eq 'UNKNOWN_RESULT' } 45 'crashed running execution was not isolated as UNKNOWN_RESULT';$attempt=[int](Invoke-Sql "SELECT attempt_no FROM ${DatabaseName}.BAT_EXECUTION_LEASE WHERE execution_id=$crashId;");Start-Sleep 3;$attempt2=[int](Invoke-Sql "SELECT attempt_no FROM ${DatabaseName}.BAT_EXECUTION_LEASE WHERE execution_id=$crashId;");if($attempt2-ne$attempt){throw 'UNKNOWN_RESULT was blindly retried'};$result.crashUnknown=[ordered]@{status='DONE';unknownResult=$true;blindRetryCount=0}
 [void](Invoke-Sql "INSERT INTO ${DatabaseName}.BAT_EXECUTION(job_id,job_parameters,execution_status,required_worker_version,required_capability,requested_by,created_by,updated_by) VALUES('CPF_BAT_DIAGNOSTIC_JOB','{}','READY','wrong-version','CPF_BAT_DIAGNOSTIC','MISMATCH','BAT_TWO_WORKER_SMOKE','BAT_TWO_WORKER_SMOKE'),('CPF_BAT_DIAGNOSTIC_JOB','{}','READY','runtime-smoke-v1','WRONG_CAP','MISMATCH','BAT_TWO_WORKER_SMOKE','BAT_TWO_WORKER_SMOKE');")
 Start-Sleep 3;$ready=[int](Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.BAT_EXECUTION WHERE requested_by='MISMATCH' AND execution_status='READY' AND worker_id IS NULL;");if($ready-ne2){throw 'version/capability mismatch was claimed'};$result.mismatch=[ordered]@{status='DONE';rejected=2};$result.status='DONE'
}finally{
 foreach($workerId in @($processes.Keys)){$p=$processes[$workerId];if($p-and-not$p.HasExited){Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue};if($p){[void]$p.WaitForExit(10000);Save-WorkerStreams $workerId}}
 $result.endedAt=(Get-Date).ToString('o');[IO.File]::WriteAllText($resultPath,($result|ConvertTo-Json -Depth 20),$Utf8NoBom)
}
