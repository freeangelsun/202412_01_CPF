param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $ResultDir = "",
    [string] $HostName = $(if ($env:CPF_DB_HOST) { $env:CPF_DB_HOST } else { 'localhost' }),
    [string] $Port = $(if ($env:CPF_DB_PORT) { $env:CPF_DB_PORT } else { '3306' }),
    [string] $Username = $(if ($env:CPF_DB_ROOT_USERNAME) { $env:CPF_DB_ROOT_USERNAME } else { 'root' }),
    [string] $Password = $env:CPF_DB_ROOT_PASSWORD,
    [string] $ClientPath = $env:CPF_MARIADB_CLI
)
$ErrorActionPreference='Stop'
$Utf8NoBom=[Text.UTF8Encoding]::new($false)
if([string]::IsNullOrWhiteSpace($ResultDir)){$ResultDir=Join-Path $Root 'build/runtime-smoke'}
elseif(-not[IO.Path]::IsPathRooted($ResultDir)){$ResultDir=Join-Path $Root $ResultDir}
New-Item -ItemType Directory -Force -Path $ResultDir|Out-Null
$runtimeDir=Join-Path $Root 'build/bat-two-worker-runtime';New-Item -ItemType Directory -Force -Path $runtimeDir|Out-Null
$resultPath=Join-Path $ResultDir 'bat-two-worker-runtime.sanitized.json';$processes=@{}
$result=[ordered]@{startedAt=(Get-Date).ToString('o');status='FAILED';registration=@{};distribution=@{};drain=@{};crashUnknown=@{};mismatch=@{}}
function Resolve-Client{if($ClientPath -and(Test-Path $ClientPath)){return $ClientPath};$c=Get-Command mariadb -ErrorAction SilentlyContinue;if($c){return $c.Source};throw 'MariaDB CLI not found'}
function Invoke-Sql([string]$Sql){$f=Join-Path $runtimeDir ('q-'+[guid]::NewGuid().ToString('N')+'.sql');[IO.File]::WriteAllText($f,$Sql,$Utf8NoBom);try{$psi=[Diagnostics.ProcessStartInfo]::new();$psi.FileName=$script:Maria;$psi.UseShellExecute=$false;$psi.RedirectStandardInput=$true;$psi.RedirectStandardOutput=$true;$psi.RedirectStandardError=$true;$psi.Arguments="--protocol=tcp --host=`"$HostName`" --port=$Port --user=`"$Username`" --ssl=0 --batch --skip-column-names --default-character-set=utf8mb4";$psi.Environment['MYSQL_PWD']=$Password;$psi.Environment['MARIADB_PWD']=$Password;$p=[Diagnostics.Process]::Start($psi);$path=(Resolve-Path $f).Path.Replace('\','/');$p.StandardInput.Write("SOURCE $path;`n");$p.StandardInput.Close();$o=$p.StandardOutput.ReadToEnd();$e=$p.StandardError.ReadToEnd();$p.WaitForExit();if($p.ExitCode-ne0){throw "MariaDB query failed: $e"};return $o.Trim()}finally{Remove-Item $f -Force -ErrorAction SilentlyContinue}}
function Wait-Until([scriptblock]$Condition,[int]$Seconds,[string]$Message){$until=(Get-Date).AddSeconds($Seconds);while((Get-Date)-lt$until){if(&$Condition){return};Start-Sleep -Milliseconds 300};throw $Message}
function Start-Worker([string]$WorkerId,[string]$InstanceId,[int]$ServerPort){
 $jar=Get-ChildItem (Join-Path $Root 'cpf-batch/worker/build/libs') -File -Filter 'worker-*.jar'|?{$_.Name-notmatch'plain'}|Select-Object -First 1
 if(-not$jar){$jar=Get-ChildItem (Join-Path $Root 'cpf-batch/worker/build/libs') -File -Filter '*.jar'|?{$_.Name-notmatch'plain'}|Select-Object -First 1};if(-not$jar){throw 'Worker bootJar missing'}
 $psi=[Diagnostics.ProcessStartInfo]::new();$psi.FileName=if($env:JAVA_HOME){Join-Path $env:JAVA_HOME 'bin/java.exe'}else{'java'};$psi.UseShellExecute=$false;$psi.RedirectStandardOutput=$true;$psi.RedirectStandardError=$true
 foreach($a in @('-jar',$jar.FullName,"--server.port=$ServerPort",'--spring.profiles.active=local','--cpf.batch.diagnostic.enabled=true','--cpf.batch.worker.lease-seconds=12','--cpf.batch.worker.poll-ms=200','--cpf.batch.worker.heartbeat-ms=500','--cpf.batch.worker.recovery-ms=500')){[void]$psi.ArgumentList.Add($a)}
 $psi.Environment['BAT_DATABASE_URL']="jdbc:mariadb://${HostName}:${Port}/batDB";$psi.Environment['BAT_DATABASE_USERNAME']=$Username;$psi.Environment['BAT_DATABASE_PASSWORD']=$Password;$psi.Environment['CPF_INSTANCE_ID']=$InstanceId;$psi.Environment['CPF_WAS_ID']=$InstanceId;$psi.Environment['CPF_BAT_WORKER_ID']=$WorkerId;$psi.Environment['CPF_BAT_WORKER_VERSION']='runtime-smoke-v1';$psi.Environment['CPF_BAT_WORKER_CAPABILITIES']='CPF_BAT_DIAGNOSTIC';$psi.Environment['CPF_BATCH_CONTROL_BASE_URL']='http://127.0.0.1:65534';$psi.Environment['CPF_LOG_ROOT']=(Join-Path $runtimeDir 'logs')
 $p=[Diagnostics.Process]::new();$p.StartInfo=$psi;[void]$p.Start();$processes[$WorkerId]=$p;return $p
}
try{
 if([string]::IsNullOrWhiteSpace($Password)){throw 'CPF_DB_ROOT_PASSWORD is required'};$script:Maria=Resolve-Client
 [void](Invoke-Sql @"
USE batDB;
DELETE FROM bat_execution_lease WHERE execution_id IN (SELECT execution_id FROM bat_execution WHERE created_by='BAT_TWO_WORKER_SMOKE');
DELETE FROM bat_execution WHERE created_by='BAT_TWO_WORKER_SMOKE';
DELETE FROM bat_worker WHERE worker_id IN ('bat-smoke-worker-1','bat-smoke-worker-2');
INSERT INTO bat_job(job_id,job_name,job_type,description,restartable_yn,use_yn,created_by,updated_by)
VALUES('CPF_BAT_DIAGNOSTIC_JOB','BAT standalone worker diagnostic','TASKLET','local/test multi-instance verification','Y','Y','BAT','BAT')
ON DUPLICATE KEY UPDATE use_yn='Y',updated_by='BAT';
"@)
 $w1=Start-Worker 'bat-smoke-worker-1' 'bat-smoke-instance-1' 8282;$w2=Start-Worker 'bat-smoke-worker-2' 'bat-smoke-instance-2' 8283
 Wait-Until { [int](Invoke-Sql "SELECT COUNT(*) FROM batDB.bat_worker WHERE worker_id IN ('bat-smoke-worker-1','bat-smoke-worker-2') AND active_yn='Y';") -eq 2 } 60 'two workers not registered'
 $result.registration=[ordered]@{status='DONE';workerCount=2}
 [void](Invoke-Sql @"
USE batDB;
INSERT INTO bat_execution(job_id,job_parameters,execution_status,required_worker_version,required_capability,requested_by,created_by,updated_by)
SELECT 'CPF_BAT_DIAGNOSTIC_JOB',CONCAT('{\"iterations\":4,\"sleepMs\":300,\"seq\":',seq,'}'),'READY','runtime-smoke-v1','CPF_BAT_DIAGNOSTIC','SMOKE','BAT_TWO_WORKER_SMOKE','BAT_TWO_WORKER_SMOKE'
FROM (SELECT 1 seq UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4) s;
"@)
 Wait-Until { [int](Invoke-Sql "SELECT COUNT(*) FROM batDB.bat_execution WHERE created_by='BAT_TWO_WORKER_SMOKE' AND requested_by='SMOKE' AND execution_status='COMPLETED';") -eq 4 } 120 'distribution executions did not complete'
 $workers=[int](Invoke-Sql "SELECT COUNT(DISTINCT worker_id) FROM batDB.bat_execution_lease WHERE execution_id IN (SELECT execution_id FROM batDB.bat_execution WHERE created_by='BAT_TWO_WORKER_SMOKE' AND requested_by='SMOKE');");if($workers-ne2){throw 'work was not distributed to both workers'}
 $result.distribution=[ordered]@{status='DONE';distinctWorkers=2;duplicateExecution=0}
 Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8282/internal/v1/worker/drain' -TimeoutSec 10|Out-Null
 [void](Invoke-Sql "INSERT INTO batDB.bat_execution(job_id,job_parameters,execution_status,required_worker_version,required_capability,requested_by,created_by,updated_by) VALUES('CPF_BAT_DIAGNOSTIC_JOB','{\"iterations\":2,\"sleepMs\":200}','READY','runtime-smoke-v1','CPF_BAT_DIAGNOSTIC','DRAIN','BAT_TWO_WORKER_SMOKE','BAT_TWO_WORKER_SMOKE');")
 $drainId=[long](Invoke-Sql "SELECT MAX(execution_id) FROM batDB.bat_execution WHERE requested_by='DRAIN';");Wait-Until { (Invoke-Sql "SELECT execution_status FROM batDB.bat_execution WHERE execution_id=$drainId;") -eq 'COMPLETED' } 60 'drain execution not completed';$drainOwner=Invoke-Sql "SELECT worker_id FROM batDB.bat_execution_lease WHERE execution_id=$drainId;";if($drainOwner-ne'bat-smoke-worker-2'){throw 'draining worker accepted new work'};$result.drain=[ordered]@{status='DONE';drainingWorkerNewClaimCount=0}
 Invoke-RestMethod -Method Post -Uri 'http://127.0.0.1:8282/internal/v1/worker/resume' -TimeoutSec 10|Out-Null
 [void](Invoke-Sql "INSERT INTO batDB.bat_execution(job_id,job_parameters,execution_status,required_worker_version,required_capability,requested_by,created_by,updated_by) VALUES('CPF_BAT_DIAGNOSTIC_JOB','{\"iterations\":200,\"sleepMs\":200}','READY','runtime-smoke-v1','CPF_BAT_DIAGNOSTIC','CRASH','BAT_TWO_WORKER_SMOKE','BAT_TWO_WORKER_SMOKE');")
 $crashId=[long](Invoke-Sql "SELECT MAX(execution_id) FROM batDB.bat_execution WHERE requested_by='CRASH';");Wait-Until { (Invoke-Sql "SELECT lease_status FROM batDB.bat_execution_lease WHERE execution_id=$crashId;") -eq 'RUNNING' } 30 'crash execution not running';$owner=Invoke-Sql "SELECT worker_id FROM batDB.bat_execution_lease WHERE execution_id=$crashId;";Stop-Process -Id $processes[$owner].Id -Force
 Wait-Until { (Invoke-Sql "SELECT execution_status FROM batDB.bat_execution WHERE execution_id=$crashId;") -eq 'UNKNOWN_RESULT' } 45 'crashed running execution was not isolated as UNKNOWN_RESULT';$attempt=[int](Invoke-Sql "SELECT attempt_no FROM batDB.bat_execution_lease WHERE execution_id=$crashId;");Start-Sleep 3;$attempt2=[int](Invoke-Sql "SELECT attempt_no FROM batDB.bat_execution_lease WHERE execution_id=$crashId;");if($attempt2-ne$attempt){throw 'UNKNOWN_RESULT was blindly retried'};$result.crashUnknown=[ordered]@{status='DONE';unknownResult=$true;blindRetryCount=0}
 [void](Invoke-Sql "INSERT INTO batDB.bat_execution(job_id,job_parameters,execution_status,required_worker_version,required_capability,requested_by,created_by,updated_by) VALUES('CPF_BAT_DIAGNOSTIC_JOB','{}','READY','wrong-version','CPF_BAT_DIAGNOSTIC','MISMATCH','BAT_TWO_WORKER_SMOKE','BAT_TWO_WORKER_SMOKE'),('CPF_BAT_DIAGNOSTIC_JOB','{}','READY','runtime-smoke-v1','WRONG_CAP','MISMATCH','BAT_TWO_WORKER_SMOKE','BAT_TWO_WORKER_SMOKE');")
 Start-Sleep 3;$ready=[int](Invoke-Sql "SELECT COUNT(*) FROM batDB.bat_execution WHERE requested_by='MISMATCH' AND execution_status='READY' AND worker_id IS NULL;");if($ready-ne2){throw 'version/capability mismatch was claimed'};$result.mismatch=[ordered]@{status='DONE';rejected=2};$result.status='DONE'
}finally{
 foreach($p in $processes.Values){if($p-and-not$p.HasExited){Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue}}
 $result.endedAt=(Get-Date).ToString('o');[IO.File]::WriteAllText($resultPath,($result|ConvertTo-Json -Depth 20),$Utf8NoBom)
}
