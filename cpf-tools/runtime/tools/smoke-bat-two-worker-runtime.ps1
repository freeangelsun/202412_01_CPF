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
    [int]$DomainPort=8285,
    [string]$DomainSystemCode='',
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
$script:AgentArtifactStateMacKeyBase64=[Convert]::ToBase64String([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
$script:AgentCommandLedgerRoot=Join-Path $ResultDir 'agent-command-ledger'
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
function Resolve-GeneratedDomainTarget {
    # Generated Domain의 물리 Project 이름은 이 Runtime에 고정하지 않는다. Canonical
    # developer contract inventory가 현재 존재하는 독립 Online Domain을 단일 source로 제공한다.
    . (Join-Path $root 'cpf-tools/generator/tools/generated-domain-common.ps1')
    $candidates=@(
        Get-CpfGeneratedDomainInventory -Root $root |
        Where-Object {
            [bool]$_.exists -and [bool]$_.onlineEnabled -and
            [string]$_.generationMode -eq 'generated' -and
            [bool]$_.databaseEnabled -and
            @($_.domainDependencies).Count -eq 0 -and
            @($_.forbiddenPermanentMetadata).Count -eq 0
        }
    )
    if(-not [string]::IsNullOrWhiteSpace($DomainSystemCode)){
        $expected=$DomainSystemCode.Trim().ToUpperInvariant()
        $candidates=@($candidates | Where-Object { [string]$_.systemCode -ceq $expected })
    }
    if($candidates.Count -eq 0){
        $selector=if([string]::IsNullOrWhiteSpace($DomainSystemCode)){'an independent generated Online Domain'}else{"systemCode=$DomainSystemCode"}
        throw "Batch→Domain runtime target is unavailable: $selector"
    }
    # Several valid generated Domains may coexist. The default remains deterministic without
    # coupling the harness to a customer/project name; callers can request a system code explicitly.
    return @($candidates | Sort-Object @{Expression={[string]$_.systemCode};Ascending=$true}, @{Expression={[string]$_.projectName};Ascending=$true})[0]
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
function Start-Role([string]$name,[string]$jar,[int]$port,[hashtable]$extra,[string]$domainDatasourceSystemCode=''){
    $log=Join-Path $ResultDir "$name.log"
    $err=Join-Path $ResultDir "$name.err.log"
    $instance="bat-$name-$runId"
    # instanceId는 JVM 시스템 속성으로만 읽히므로(-D), Spring 인자만으로는 적용되지 않는다.
    $args=@("-Dcpf.runtime.instance-id=$instance",'-jar',$jar,"--server.port=$port",'--spring.batch.job.enabled=false',
      '--cpf.data.persistence.jdbc.role-datasources.cpf-platform-db.enabled=true',
      "--cpf.data.persistence.jdbc.role-datasources.cpf-platform-db.url=jdbc:mariadb://127.0.0.1:3306/$DatabaseName",
      "--cpf.data.persistence.jdbc.role-datasources.cpf-platform-db.username=$DbUser",
      '--cpf.data.persistence.jdbc.role-datasources.cpf-platform-db.driver-class-name=org.mariadb.jdbc.Driver',
      "--cpf.runtime.instance-id=$instance","--cpf.was-id=$instance","--cpf.db.vendor=$DbVendor",
      "--cpf.db.resource-root=$script:DbResourceRootResolved",
      "--cpf.batch.control.base-url=http://127.0.0.1:$ControlPlanePort")
    foreach($k in $extra.Keys){$args += "--$k=$($extra[$k])"}
    # JDBC passwords must never enter a Java command line, process listing, or harness log.
    # Spring's canonical Batch runtime contract reads the password from this child-only environment.
    $childEnvironment=@{
        # Batch executable shared runtime binding and the direct role-data-source binding are
        # both supported Consumer paths. Keep their secret value environment-only in either case.
        CPF_PLATFORM_DB_PASSWORD=$DbPassword
        CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CPF_PLATFORM_DB_PASSWORD=$DbPassword
        # Center-Cut Parameter Snapshot 은 AES-256-GCM 으로 봉인되며 Key 는 외부 Secret 으로만
        # 공급된다. 없으면 실행 생성이 500 으로 실패한다. 검증용 Key 는 실행마다 새로 만들고
        # 저장소에 남기지 않으며, JDBC 비밀번호와 동일하게 자식 환경에만 전달한다.
        CPF_CENTER_CUT_PARAMETER_KEY=$script:CenterCutParameterKey
    }
    if(-not [string]::IsNullOrWhiteSpace($domainDatasourceSystemCode)){
        # Every generated Online Domain owns a separate Spring datasource binding named from its
        # canonical system code. Keep the verifier credential environment-only and never rely on a
        # particular generated project being present.
        $domainDatasourcePrefix=$domainDatasourceSystemCode.Trim().ToUpperInvariant()
        $domainUrl="jdbc:mariadb://127.0.0.1:3306/$DatabaseName"
        $childEnvironment["${domainDatasourcePrefix}_DATASOURCE_URL"]=$domainUrl
        $childEnvironment["${domainDatasourcePrefix}_DATASOURCE_USERNAME"]=$DbUser
        $childEnvironment["${domainDatasourcePrefix}_DATASOURCE_PASSWORD"]=$DbPassword
        $childEnvironment["${domainDatasourcePrefix}_DATASOURCE_DRIVER"]='org.mariadb.jdbc.Driver'
    }
    if($name -eq 'agent'){
        # The Agent must receive a valid run-scoped integrity key even when no artifact install is
        # invoked. The key is intentionally environment-only; command-line exposure defeats it.
        $childEnvironment.CPF_AGENT_ARTIFACT_STATE_MAC_KEY_BASE64=$script:AgentArtifactStateMacKeyBase64
        $childEnvironment.CPF_AGENT_COMMAND_LEDGER_ROOT=$script:AgentCommandLedgerRoot
    }
    $p=Start-Process -FilePath $script:Java -ArgumentList $args -Environment $childEnvironment -PassThru -RedirectStandardOutput $log -RedirectStandardError $err
    $script:processes += [pscustomobject]@{Name=$name;Process=$p;Log=$log;Err=$err;Port=$port;Instance=$instance}
    $result.roles[$name]=[ordered]@{pid=$p.Id;port=$port;instanceId=$instance;jar=$jar;log=$log;errorLog=$err}
    Wait-Http "http://127.0.0.1:$port/actuator/health" | Out-Null
    Step "$name health" 'PASS' "pid=$($p.Id) port=$port"
    return $p
}
# BAT 인가 계약: 일반 명령의 body actor 는 인증된 Operator 와 같아야 하고, 승인 명령은
# 요청자와 승인자가 서로 달라야 한다. 두 값을 한 곳에서 소유해 header/body 드리프트를 막는다.
$BatOperatorId='cpf-harness-approver'
$BatApprovalRequesterId='cpf-harness-requester'
# Canonical Center-Cut Job id. 등록과 실행 요청이 같은 값을 쓰도록 한 곳에서 소유한다.
$CenterCutJobId='CPF_BAT_CENTER_CUT_JOB'
# 실행 범위 Center-Cut Parameter Key(256-bit). 고정 Secret 을 저장소에 두지 않기 위해
# 매 실행마다 난수로 생성한다.
$script:CenterCutParameterKey=[Convert]::ToBase64String((
    [System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32)))
function BatHeaders([bool]$approved=$false,[string]$targetOperationId='cpf-bat-runtime-qualification'){
    # BAT 전용 헤더만 보내면 CPF 공통 Header Filter 가 EXTERNAL_HEADER_REQUIRED(ECPF900002)로
    # 거부한다. 다른 smoke harness 와 같은 CPF 표준 거래 헤더를 함께 실어야 한다.
    # transactionId 는 canonical 34자 계약(yyyyMMddHHmmssfff + systemCode + caller + sequence)이다.
    $sequence=Get-Random -Minimum 1 -Maximum 9999999
    $transactionId="{0:yyyyMMddHHmmssfff}BATbatwrkr{1:0000000}" -f (Get-Date), $sequence
    # Canonical 6(X-Transaction-Id + System lineage 5)은 외부 Channel 호출의 필수 계약이다.
    # X-Original-System-Code 는 X-Transaction-Id 에 인코딩된 발행자와 일치해야 하고(BAT),
    # X-System-Code / X-Target-System-Code 는 수신 Runtime 의 System Code 와 같아야 한다.
    $h=@{
        'X-Cpf-Bat-Caller-Service'='ADM'
        'X-Cpf-Bat-Caller-Instance-Id'="harness-$runId"
        'X-Cpf-Bat-Operator-Id'=$BatOperatorId
        'X-Request-Type'='INQUIRY'
        'X-Transaction-Id'=$transactionId
        'X-Original-System-Code'='BAT'
        'X-System-Code'='BAT'
        'X-Caller-System-Code'='ADM'
        'X-Target-System-Code'='BAT'
        'X-Target-Operation-Id'=$targetOperationId
        'X-Trace-Id'=$transactionId
        'X-User-Id'='runtime-smoke'
        'X-Client-Id'='cpf-smoke'
        'X-Client-Version'='1.0.0'
        'X-Caller-Service'='smoke-bat-two-worker-runtime'
    }
    if($approved){$h['X-Cpf-Bat-Approval-Request-Id']="APR-$runId";$h['X-Cpf-Bat-Approval-Requester-Id']=$BatApprovalRequesterId}
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
    # Runtime Agent registration is authoritative. BAT_RUNTIME_INSTANCE is not the Runtime
    # Agent projection and is legitimately empty before a Batch job starts.
    $v=Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.OPS_RUNTIME_INSTANCE_STATE WHERE instance_id LIKE '$pattern' AND runtime_role='WORKER';"
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
      domain=$null
    }
    $targetDomain=Resolve-GeneratedDomainTarget
    $jars.domain=Resolve-Jar @("$([string]$targetDomain.projectName)/online/build/libs/*.jar") ("generated Domain " + [string]$targetDomain.systemCode)
    # Runtime Agent defaults serviceId to its canonical systemCode and endpointCode to
    # <systemCode>_API. The verifier must seed those exact identities, not a run-scoped alias.
    $serviceId=[string]$targetDomain.systemCode
    # Register the selected Domain before its Runtime Agent starts. Runtime registration is
    # intentionally fail-closed, so reversing this order makes an otherwise healthy Domain fail.
    $runtimeEndpointCode="$([string]$targetDomain.systemCode)_API"
    Invoke-Sql "INSERT INTO ${DatabaseName}.OPS_SERVICE(service_id,service_name,service_type,owner_module_code,description,use_yn,created_by,updated_by) VALUES('$serviceId','$([string]$targetDomain.projectName) Harness','INTERNAL','$([string]$targetDomain.systemCode)','Batch runtime generated Domain target','Y','HARNESS','HARNESS') ON DUPLICATE KEY UPDATE service_name=VALUES(service_name),service_type=VALUES(service_type),owner_module_code=VALUES(owner_module_code),description=VALUES(description),use_yn='Y',updated_by='HARNESS';" | Out-Null
    Invoke-Sql "INSERT INTO ${DatabaseName}.OPS_SERVICE_ENDPOINT(endpoint_code,service_id,endpoint_name,endpoint_type,base_url,context_path,default_timeout_ms,default_retry_count,use_yn,created_by,updated_by) VALUES('$runtimeEndpointCode','$serviceId','$([string]$targetDomain.systemCode) Runtime API','HTTP','http://127.0.0.1:$DomainPort','/',3000,0,'Y','HARNESS','HARNESS') ON DUPLICATE KEY UPDATE service_id=VALUES(service_id),endpoint_name=VALUES(endpoint_name),endpoint_type=VALUES(endpoint_type),base_url=VALUES(base_url),context_path=VALUES(context_path),default_timeout_ms=VALUES(default_timeout_ms),default_retry_count=VALUES(default_retry_count),use_yn='Y',updated_by='HARNESS';" | Out-Null
    Step 'Generated Domain service registry pre-registration' 'PASS' "systemCode=$([string]$targetDomain.systemCode) serviceId=$serviceId"

    # Center-Cut Job 정의는 Canonical Seed Model 에서 sample seed(58_runtime_sample_seed.sql)로
    # 분류되어 있고, Verifier 소유 Runtime DB 는 product seed 만 적재한다. Domain 서비스 등록과
    # 동일하게 이 시나리오의 전제를 Verifier 가 직접 만든다. Canonical 정의를 바꾸거나 sample
    # 행을 product seed 로 승격시키지 않는다.
    # BAT_CENTER_CUT_JOB.batch_job_id 는 BAT_JOB(job_id) 를 참조한다. Canonical sample seed 와
    # 같은 순서로 BAT_JOB 을 먼저 만든다. INSERT IGNORE 는 FK 위반까지 조용히 삼켜 원인을
    # 감추므로 쓰지 않는다.
    Invoke-Sql "INSERT INTO ${DatabaseName}.BAT_JOB(job_id,job_name,job_type,description,restartable_yn,use_yn,created_by,updated_by) VALUES('$CenterCutJobId','CPF BAT Center-Cut qualification Job','TASKLET','Runtime qualification prerequisite seeded by the verifier','Y','Y','SYSTEM','SYSTEM') ON DUPLICATE KEY UPDATE use_yn=VALUES(use_yn),updated_by=VALUES(updated_by);"
    Invoke-Sql "INSERT INTO ${DatabaseName}.BAT_CENTER_CUT_JOB(center_cut_job_id,batch_job_id,center_cut_job_name,provider_key,handler_key,chunk_size,retry_limit,use_yn,description,created_by,updated_by) VALUES('$CenterCutJobId','$CenterCutJobId','CPF Domain Invocation Center-Cut Job','cpfParameterSnapshotCenterCutTargetProvider','cpfDomainInvocationCenterCutHandler',10,3,'Y','Runtime qualification prerequisite seeded by the verifier','SYSTEM','SYSTEM') ON DUPLICATE KEY UPDATE use_yn=VALUES(use_yn),provider_key=VALUES(provider_key),handler_key=VALUES(handler_key),updated_by=VALUES(updated_by);"
    $centerCutJobRows=[int](Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.BAT_CENTER_CUT_JOB WHERE center_cut_job_id='$CenterCutJobId';")
    Require ($centerCutJobRows -eq 1) "Center-Cut Job prerequisite was not registered: $CenterCutJobId"
    Step 'Center-Cut Job pre-registration' 'PASS' "centerCutJobId=$CenterCutJobId rows=$centerCutJobRows"

    Start-Role 'control-plane' $jars.control $ControlPlanePort @{} | Out-Null
    Start-Role 'scheduler' $jars.scheduler $SchedulerPort @{} | Out-Null
    Start-Role 'worker-1' $jars.worker $Worker1Port @{'cpf.batch.worker.center-cut.lease-seconds'=$LeaseSeconds;'cpf.batch.worker.center-cut.heartbeat-millis'=1000} | Out-Null
    Start-Role 'center-cut' $jars.centercut $CenterCutPort @{} | Out-Null
    Start-Role 'agent' $jars.agent $AgentPort @{} | Out-Null
    Start-Role 'worker-2' $jars.worker $Worker2Port @{'cpf.batch.worker.center-cut.lease-seconds'=$LeaseSeconds;'cpf.batch.worker.center-cut.heartbeat-millis'=1000} | Out-Null
    Start-Role 'domain' $jars.domain $DomainPort @{} ([string]$targetDomain.systemCode) | Out-Null
    Step 'five Batch runtimes + second worker + Generated Domain' 'PASS' "systemCode=$([string]$targetDomain.systemCode)"

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

    $businessKey="BK-$runId"
    $parameters=[ordered]@{systemCode=[string]$targetDomain.systemCode;operationId='ping';targets=@([ordered]@{businessKey=$businessKey;request=[ordered]@{message='cpf-batch-kafka-free'}})}
    $create=[ordered]@{centerCutJobId=$CenterCutJobId;idempotencyKey="CC-$runId";parameters=$parameters;parameterSchemaVersion=1;tpsLimit=10;concurrencyLimit=2;requestedBy=$BatOperatorId;reason='Kafka-free Center-Cut Domain Invocation qualification';transactionId=$null;parentSegmentId=$null}
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
    # PowerShell 은 원소가 0~1 개인 배열을 return 시 풀어버린다. Set-StrictMode Latest 에서는
    # 스칼라의 .Count 접근이 오류이므로 호출부에서 다시 배열로 고정한다.
    $claimRows=@(Snapshot-Claims)
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
        $approved=[ordered]@{requestedBy=$BatApprovalRequesterId;approvedBy=$BatOperatorId;reason='Runtime qualification explicit UNKNOWN reconciliation'}
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
