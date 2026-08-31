param(
    [Parameter(Mandatory=$true)][string]$Root,
    [Parameter(Mandatory=$true)][string]$ResultDir,
    [ValidateSet('Host','Docker')][string]$ClientAdapter='Docker',
    [string]$MariaDbContainer='cpf-mariadb',
    [string]$DatabaseName='cpfDB',
    [string]$DbVendor='mariadb',
    [string]$DbResourceRoot=$env:CPF_DB_RESOURCE_ROOT,
    [string]$DbUser='cpf_app',
    [string]$DbPassword=$env:CPF_CORE_DB_RUNTIME_PASSWORD,
    [string]$DbRootPassword=$env:CPF_DB_ROOT_PASSWORD,
    [int]$ControlPlanePort=8180,
    [int]$SchedulerPort=8181,
    [int]$Worker1Port=8182,
    [int]$CenterCutPort=8183,
    [int]$AgentPort=8184,
    [int]$Worker2Port=8282,
    [int]$MemberPort=8285,
    [int]$LeaseSeconds=10,
    [int]$TimeoutSeconds=180
)
# Full Runtime child-process UTF-8 contract. Keep the emitted byte stream UTF-8 even when pwsh is redirected.
$CpfUtf8ConsoleEncoding = [Text.UTF8Encoding]::new($false)
try {
    [Console]::InputEncoding = $CpfUtf8ConsoleEncoding
    [Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
    $OutputEncoding = $CpfUtf8ConsoleEncoding
    $global:OutputEncoding = $CpfUtf8ConsoleEncoding
} catch { }
$env:PYTHONUTF8 = '1'
$env:PYTHONIOENCODING = 'utf-8'

$ErrorActionPreference='Stop'

# Child process가 새 Windows process로 분리되어도 UTF-8 계약을 잃지 않도록 고정합니다.
$CpfUtf8ChildJavaOptions = '-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8'
if ([string]::IsNullOrWhiteSpace($env:JAVA_TOOL_OPTIONS)) {
    $env:JAVA_TOOL_OPTIONS = $CpfUtf8ChildJavaOptions
} elseif ($env:JAVA_TOOL_OPTIONS -notmatch '(?:^|\s)-Dfile\.encoding=UTF-8(?:\s|$)') {
    $env:JAVA_TOOL_OPTIONS = ($env:JAVA_TOOL_OPTIONS.Trim() + ' ' + $CpfUtf8ChildJavaOptions)
}
$env:PYTHONUTF8 = '1'
$env:PYTHONIOENCODING = 'utf-8'
$env:PGCLIENTENCODING = 'UTF8'
$env:NLS_LANG = '.AL32UTF8'
$ProgressPreference='SilentlyContinue'
Set-StrictMode -Version Latest
$root=(Resolve-Path -LiteralPath $Root).Path
if ([string]::IsNullOrWhiteSpace($DbPassword)) { $DbPassword = $env:CPF_DB_APP_PASSWORD }
if ([string]::IsNullOrWhiteSpace($DbPassword)) { throw 'CPF DB runtime password is required via CPF_CORE_DB_RUNTIME_PASSWORD or CPF_DB_APP_PASSWORD.' }
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
if ([string]::IsNullOrWhiteSpace($DbResourceRoot)) { $DbResourceRoot = Join-Path $Root ("cpf-tools\db\vendor\" + $DbVendor) }
$DbResourceRoot = [IO.Path]::GetFullPath($DbResourceRoot)
if (-not (Test-Path -LiteralPath (Join-Path $DbResourceRoot 'pack.json') -PathType Leaf)) {
    throw "중앙 DB Vendor Pack을 찾을 수 없습니다. vendor=$DbVendor root=$DbResourceRoot"
}
$script:DbResourceRootResolved = $DbResourceRoot
$started=Get-Date
$runId=Get-Date -Format 'yyyyMMddHHmmssfff'
$processes=@()
$result=[ordered]@{status='RUNNING';runId=$runId;startedAt=$started.ToString('o');kafkaUsed=$false;roles=[ordered]@{};checks=@();logs=@()}

function Step([string]$name,[string]$status,[string]$detail='') {
    $stamp=(Get-Date).ToString('HH:mm:ss')
    Write-Host "[$stamp] [$status] $name $detail"
    $result.checks += [ordered]@{name=$name;status=$status;detail=$detail;at=(Get-Date).ToString('o')}
}
function Require([bool]$condition,[string]$message) { if(-not $condition){throw $message} }
function Json([object]$value){ $value | ConvertTo-Json -Depth 12 -Compress }
function Resolve-Java {
    $java=Get-Command java -ErrorAction SilentlyContinue
    if(-not $java){throw 'java command not found'}
    $version=& $java.Source -version 2>&1 | Out-String
    if($version -notmatch 'version "25[\.]'){throw "Java 25 is required: $version"}
    return $java.Source
}
function Resolve-Docker {
    $d=Get-Command docker -ErrorAction SilentlyContinue
    if(-not $d){throw 'Docker CLI is required for MariaDB runtime qualification'}
    return $d.Source
}
function Resolve-Jar([string[]]$patterns,[string]$label){
    foreach($pattern in $patterns){
        $candidates=@(Get-ChildItem -Path (Join-Path $root $pattern) -File -ErrorAction SilentlyContinue | Where-Object {$_.Name -notmatch '-plain\.jar$'} | Sort-Object LastWriteTime -Descending)
        if($candidates.Count -gt 0){return $candidates[0].FullName}
    }
    throw "Boot JAR not found for $label. Build the full Java25 package first."
}
function Invoke-Sql([string]$sql,[switch]$RootUser){
    $docker=Resolve-Docker
    $password=if($RootUser){$DbRootPassword}else{$DbPassword}
    $user=if($RootUser){'root'}else{$DbUser}
    if([string]::IsNullOrWhiteSpace($password)){throw "DB password unavailable for $user"}
    $args=@('exec','-i',$MariaDbContainer,'mariadb',"-u$user","-p$password",'-N','-B','-e',$sql)
    $out=& $docker @args 2>&1
    if($LASTEXITCODE -ne 0){throw "SQL failed: $($out -join ' ')"}
    return (($out -join "`n").Trim())
}
function Wait-Http([string]$uri,[int]$seconds=$TimeoutSeconds){
    $until=(Get-Date).AddSeconds($seconds)
    do {
        try { $r=Invoke-WebRequest -UseBasicParsing -TimeoutSec 4 -Uri $uri; if($r.StatusCode -ge 200 -and $r.StatusCode -lt 500){return $r} } catch {}
        Start-Sleep -Milliseconds 700
    } while((Get-Date) -lt $until)
    throw "HTTP readiness timeout: $uri"
}
function Start-Role([string]$name,[string]$jar,[int]$port,[hashtable]$extra){
    $log=Join-Path $ResultDir "$name.log"
    $err=Join-Path $ResultDir "$name.err.log"
    $instance="bat-$name-$runId"
    # instanceId는 JVM 시스템 속성으로만 읽히므로(-D), Spring 인자만으로는 적용되지 않는다.
    $args=@("-Dcpf.runtime.instance-id=$instance",'-jar',$jar,"--server.port=$port",'--spring.batch.job.enabled=false',
      '--cpf.data.persistence.jdbc.role-datasources.cpf-platform-db.enabled=true',
      "--cpf.data.persistence.jdbc.role-datasources.cpf-platform-db.url=jdbc:mariadb://127.0.0.1:3306/$DatabaseName",
      "--cpf.data.persistence.jdbc.role-datasources.cpf-platform-db.username=$DbUser",
      "--cpf.data.persistence.jdbc.role-datasources.cpf-platform-db.password=$DbPassword",
      '--cpf.data.persistence.jdbc.role-datasources.cpf-platform-db.driver-class-name=org.mariadb.jdbc.Driver',
      "--cpf.runtime.instance-id=$instance","--cpf.was-id=$instance","--cpf.db.vendor=$DbVendor",
      "--cpf.db.resource-root=$script:DbResourceRootResolved",
      "--cpf.batch.control.base-url=http://127.0.0.1:$ControlPlanePort")
    foreach($k in $extra.Keys){$args += "--$k=$($extra[$k])"}
    $p=Start-Process -FilePath $script:Java -ArgumentList $args -PassThru -RedirectStandardOutput $log -RedirectStandardError $err
    $script:processes += [pscustomobject]@{Name=$name;Process=$p;Log=$log;Err=$err;Port=$port;Instance=$instance}
    $result.roles[$name]=[ordered]@{pid=$p.Id;port=$port;instanceId=$instance;jar=$jar;log=$log;errorLog=$err}
    Wait-Http "http://127.0.0.1:$port/actuator/health" | Out-Null
    Step "$name health" 'PASS' "pid=$($p.Id) port=$port"
    return $p
}
function BatHeaders([bool]$approved=$false){
    $h=@{'X-Cpf-Bat-Caller-Service'='ADM';'X-Cpf-Bat-Caller-Instance-Id'="harness-$runId";'X-Cpf-Bat-Operator-Id'='cpf-harness-approver'}
    if($approved){$h['X-Cpf-Bat-Approval-Request-Id']="APR-$runId";$h['X-Cpf-Bat-Approval-Requester-Id']='cpf-harness-requester'}
    return $h
}
function Invoke-Json([string]$method,[string]$uri,[object]$body=$null,[bool]$approved=$false){
    $params=@{Method=$method;Uri=$uri;Headers=(BatHeaders $approved);UseBasicParsing=$true;TimeoutSec=30}
    if($null -ne $body){$params.ContentType='application/json';$params.Body=(Json $body)}
    return Invoke-RestMethod @params
}
function Snapshot-Claims {
    $sql="SELECT CONCAT(center_cut_item_id,'|',runner_id,'|',claim_status,'|',fencing_token,'|',attempt_no,'|',takeover_count) FROM ${DatabaseName}.BAT_CENTER_CUT_CLAIM ORDER BY center_cut_item_id;"
    return @(Invoke-Sql $sql)
}
function RuntimeCount([string]$pattern){
    $v=Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.BAT_RUNTIME_INSTANCE WHERE instance_id LIKE '$pattern';"
    return [int]$v
}

try {
    $script:Java=Resolve-Java
    $script:Docker=Resolve-Docker
    $running=(& $script:Docker inspect --format '{{.State.Running}}' $MariaDbContainer 2>$null | Out-String).Trim()
    Require ($running -eq 'true') "MariaDB container is not running: $MariaDbContainer"
    Step 'prerequisite Java25/MariaDB' 'PASS'

    # Fail closed when the retired Kafka Remote Execution DB surface survived migration.
    $remoteTable=[int](Invoke-Sql "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DatabaseName' AND table_name='BAT_REMOTE_MESSAGE_LEDGER';")
    Require ($remoteTable -eq 0) 'BAT_REMOTE_MESSAGE_LEDGER still exists; V140/current schema removal is incomplete'
    Step 'retired remote ledger absent' 'PASS'

    $jars=[ordered]@{
      control=(Resolve-Jar @('cpf-batch/control-plane/build/libs/*.jar') 'control-plane')
      scheduler=(Resolve-Jar @('cpf-batch/scheduler/build/libs/*.jar') 'scheduler')
      worker=(Resolve-Jar @('cpf-batch/worker/build/libs/*.jar') 'worker')
      centercut=(Resolve-Jar @('cpf-batch/center-cut/build/libs/*.jar') 'center-cut')
      agent=(Resolve-Jar @('cpf-batch/agent/build/libs/*.jar') 'agent')
      member=(Resolve-Jar @('cpf-member/online/build/libs/*.jar') 'member')
    }

    Start-Role 'control-plane' $jars.control $ControlPlanePort @{} | Out-Null
    Start-Role 'scheduler' $jars.scheduler $SchedulerPort @{} | Out-Null
    Start-Role 'worker-1' $jars.worker $Worker1Port @{'cpf.batch.worker.center-cut.lease-seconds'=$LeaseSeconds;'cpf.batch.worker.center-cut.heartbeat-millis'=1000} | Out-Null
    Start-Role 'center-cut' $jars.centercut $CenterCutPort @{} | Out-Null
    Start-Role 'agent' $jars.agent $AgentPort @{} | Out-Null
    Start-Role 'worker-2' $jars.worker $Worker2Port @{'cpf.batch.worker.center-cut.lease-seconds'=$LeaseSeconds;'cpf.batch.worker.center-cut.heartbeat-millis'=1000} | Out-Null
    Start-Role 'member' $jars.member $MemberPort @{} | Out-Null
    Step 'five Batch runtimes + second worker + MBR' 'PASS'

    $workerRows=RuntimeCount "bat-worker-%-$runId"
    Require ($workerRows -ge 2) "Expected >=2 worker registry rows, actual=$workerRows"
    Step 'multi-instance worker registry' 'PASS' "workers=$workerRows"

    # Drain / resume is a general Worker lifecycle and must stay Kafka-independent.
    Invoke-WebRequest -UseBasicParsing -Method Post -Headers (BatHeaders) -Uri "http://127.0.0.1:$Worker1Port/internal/v1/worker/drain" | Out-Null
    Start-Sleep -Seconds 2
    $drainState=Invoke-RestMethod -Headers (BatHeaders) -Uri "http://127.0.0.1:$Worker1Port/actuator/health"
    Step 'worker drain' 'PASS'
    Invoke-WebRequest -UseBasicParsing -Method Post -Headers (BatHeaders) -Uri "http://127.0.0.1:$Worker1Port/internal/v1/worker/resume" | Out-Null
    Step 'worker resume' 'PASS'

    # Register MBR remote HTTP endpoint through the normal service registry, not a Batch transport.
    $serviceId="MBR-SERVICE-$runId"; $endpointCode='ping'; $instanceId="mbr-$runId"
    # The exact service-registry DML is installation-specific; use canonical runtime tables and fail if schema changed.
    Invoke-Sql "INSERT INTO ${DatabaseName}.OPS_SERVICE(service_id,service_name,system_code,service_status,created_by,updated_by) VALUES('$serviceId','MBR Harness','MBR','ACTIVE','HARNESS','HARNESS') ON DUPLICATE KEY UPDATE service_status='ACTIVE',updated_by='HARNESS';" | Out-Null
    Invoke-Sql "INSERT INTO ${DatabaseName}.OPS_SERVICE_ENDPOINT(service_id,endpoint_code,operation_id,protocol_type,endpoint_status,created_by,updated_by) VALUES('$serviceId','$endpointCode','ping','HTTP','ACTIVE','HARNESS','HARNESS') ON DUPLICATE KEY UPDATE endpoint_status='ACTIVE',updated_by='HARNESS';" | Out-Null
    Invoke-Sql "INSERT INTO ${DatabaseName}.OPS_SERVICE_INSTANCE(service_id,instance_id,base_url,instance_status,health_status,created_by,updated_by) VALUES('$serviceId','$instanceId','http://127.0.0.1:$MemberPort','ACTIVE','UP','HARNESS','HARNESS') ON DUPLICATE KEY UPDATE base_url=VALUES(base_url),instance_status='ACTIVE',health_status='UP',updated_by='HARNESS';" | Out-Null
    Step 'MBR normal service registry endpoint' 'PASS' "serviceId=$serviceId"

    $businessKey="BK-$runId"
    $parameters=[ordered]@{systemCode='MBR';operationId='ping';targets=@([ordered]@{businessKey=$businessKey;request=[ordered]@{message='cpf-batch-kafka-free'}})}
    $create=[ordered]@{centerCutJobId='CPF_BAT_CENTER_CUT_JOB';idempotencyKey="CC-$runId";parameters=$parameters;parameterSchemaVersion=1;tpsLimit=10;concurrencyLimit=2;requestedBy='cpf-harness-requester';reason='Kafka-free Center-Cut Domain Invocation qualification';transactionId=$null;parentSegmentId=$null}
    $execution=Invoke-Json 'POST' "http://127.0.0.1:$ControlPlanePort/api/v1/batch/center-cut/executions" $create $false
    $executionId=[string]($execution.center_cut_execution_id ?? $execution.centerCutExecutionId ?? $execution.executionId)
    Require (-not [string]::IsNullOrWhiteSpace($executionId)) "Center-Cut create response lacks execution id: $(Json $execution)"
    Step 'Center-Cut execution create' 'PASS' "executionId=$executionId"

    # Allow provider/worker path to materialize and process DB work item via official Domain Invocation.
    $deadline=(Get-Date).AddSeconds($TimeoutSeconds); $status=''
    do {
        Start-Sleep -Seconds 1
        $row=Invoke-Sql "SELECT CONCAT(execution_state,'|',processed_count,'|',success_count,'|',failure_count,'|',unknown_count) FROM ${DatabaseName}.BAT_CENTER_CUT_EXECUTION WHERE center_cut_execution_id='$executionId';"
        $status=$row
        if($row -match 'COMPLETED|SUCCESS'){break}
    } while((Get-Date) -lt $deadline)
    $itemCount=[int](Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.BAT_CENTER_CUT_ITEM WHERE center_cut_execution_id='$executionId';")
    Require ($itemCount -ge 1) "Center-Cut provider did not materialize DB work item: $executionId"
    $claimRows=Snapshot-Claims
    Step 'DB work item/claim/fencing path' 'PASS' "items=$itemCount claims=$($claimRows.Count) state=$status"

    # Kill a worker only after the DB claim path exists. Any in-flight loss must become UNKNOWN, never blind retry.
    $worker1=($processes | Where-Object Name -eq 'worker-1').Process
    Stop-Process -Id $worker1.Id -Force
    Step 'worker process kill' 'PASS' "pid=$($worker1.Id)"
    Start-Sleep -Seconds ($LeaseSeconds + 3)
    $beforeUnknown=[int](Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.BAT_CENTER_CUT_ITEM WHERE center_cut_execution_id='$executionId' AND item_status='UNKNOWN_RESULT';")
    $beforeFencing=[long](Invoke-Sql "SELECT COALESCE(MAX(fencing_token),0) FROM ${DatabaseName}.BAT_CENTER_CUT_CLAIM c JOIN ${DatabaseName}.BAT_CENTER_CUT_ITEM i ON i.center_cut_item_id=c.center_cut_item_id WHERE i.center_cut_execution_id='$executionId';")
    Start-Sleep -Seconds 2
    $afterUnknown=[int](Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.BAT_CENTER_CUT_ITEM WHERE center_cut_execution_id='$executionId' AND item_status='UNKNOWN_RESULT';")
    Require ($afterUnknown -eq $beforeUnknown) 'UNKNOWN_RESULT was blindly retried without explicit reconciliation'
    Step 'no blind retry for UNKNOWN' 'PASS' "unknown=$afterUnknown"

    if($afterUnknown -gt 0){
        $approved=[ordered]@{requestedBy='cpf-harness-requester';approvedBy='cpf-harness-approver';reason='Runtime qualification explicit UNKNOWN reconciliation'}
        [void](Invoke-Json 'POST' "http://127.0.0.1:$ControlPlanePort/api/v1/batch/center-cut/executions/$executionId/reconcile-unknown" $approved $true)
        Start-Sleep -Seconds 3
        $afterFencing=[long](Invoke-Sql "SELECT COALESCE(MAX(fencing_token),0) FROM ${DatabaseName}.BAT_CENTER_CUT_CLAIM c JOIN ${DatabaseName}.BAT_CENTER_CUT_ITEM i ON i.center_cut_item_id=c.center_cut_item_id WHERE i.center_cut_execution_id='$executionId';")
        Require ($afterFencing -gt $beforeFencing) "Reconciled takeover did not advance fencing token: before=$beforeFencing after=$afterFencing"
        Step 'explicit UNKNOWN reconcile + fencing takeover' 'PASS' "before=$beforeFencing after=$afterFencing"
    } else {
        Step 'explicit UNKNOWN reconcile + fencing takeover' 'NOT_TRIGGERED' 'No in-flight UNKNOWN was produced by the selected kill point; harness did not fabricate one.'
    }

    # Strong static runtime assertion: no Batch Kafka provider or retired ledger has been recreated.
    $remoteTableAfter=[int](Invoke-Sql "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='$DatabaseName' AND table_name='BAT_REMOTE_MESSAGE_LEDGER';")
    Require ($remoteTableAfter -eq 0) 'Retired Batch Remote Kafka ledger reappeared during runtime'
    Step 'post-runtime remote Kafka surface absence' 'PASS'

    $result.status='PASS'
} catch {
    $result.status='FAIL'; $result.error=$_.Exception.Message
    Step 'runtime qualification' 'FAIL' $_.Exception.Message
} finally {
    foreach($entry in @($processes | Sort-Object {$_.Name -eq 'worker-1'})){
        try { if(-not $entry.Process.HasExited){Stop-Process -Id $entry.Process.Id -Force -ErrorAction SilentlyContinue} } catch {}
    }
    $result.completedAt=(Get-Date).ToString('o');$result.durationSeconds=[math]::Round(((Get-Date)-$started).TotalSeconds,2)
    $out=Join-Path $ResultDir 'batch-kafka-free-runtime-result.json'
    $result | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $out -Encoding utf8
    Write-Host "RESULT=$out"
    Write-Host "STATUS=$($result.status)"
}
if($result.status -ne 'PASS'){exit 1}
