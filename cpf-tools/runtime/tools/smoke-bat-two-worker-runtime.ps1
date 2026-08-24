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
    [string] $KafkaContainer = 'cpf-kafka',
    [int] $ControlPlanePort = 8279,
    [int] $CenterCutPort = 8280,
    [int] $WorkerOnePort = 8282,
    [int] $WorkerTwoPort = 8283
)

$ErrorActionPreference = 'Stop'
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$processes = @{}
$processStreams = @{}
$createdTopics = [Collections.Generic.List[string]]::new()
$failure = $null
$httpClient = $null

if ($DatabaseName -notmatch '^[A-Za-z][A-Za-z0-9_$]{0,63}$') { throw 'DatabaseName is not a safe MariaDB identifier' }
if ($DbVendor.ToLowerInvariant() -ne 'mariadb') { throw 'The MariaDB two-worker smoke requires DbVendor=mariadb' }
if ([string]::IsNullOrWhiteSpace($DbResourceRoot)) { $DbResourceRoot = Join-Path $Root 'cpf-tools/db/vendor/mariadb' }
elseif (-not [IO.Path]::IsPathRooted($DbResourceRoot)) { $DbResourceRoot = Join-Path $Root $DbResourceRoot }
$DbResourceRoot = [IO.Path]::GetFullPath($DbResourceRoot)
if (-not (Test-Path -LiteralPath (Join-Path $DbResourceRoot 'pack.json') -PathType Leaf)) {
    throw "Canonical MariaDB Vendor Pack is missing: $DbResourceRoot"
}
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path ([IO.Path]::GetTempPath()) 'cpf-bat-two-worker-runtime'
} elseif (-not [IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
$ResultDir = [IO.Path]::GetFullPath($ResultDir)
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$runtimeDir = Join-Path $ResultDir 'processes'
New-Item -ItemType Directory -Force -Path $runtimeDir | Out-Null
$resultPath = Join-Path $ResultDir 'bat-two-worker-runtime.sanitized.json'

$runId = ([Guid]::NewGuid().ToString('N')).Substring(0, 12)
$requestTopic = "cpf.batch.smoke.$runId.requests"
$replyTopicPrefix = "cpf.batch.smoke.$runId.replies"
$managerId = "bat-smoke-manager-$runId"
$consumerGroup = "cpf-batch-smoke-$runId"
$managerProducer = "bat-smoke-manager-$runId"
$workerOneProducer = "bat-smoke-worker-1-$runId"
$workerTwoProducer = "bat-smoke-worker-2-$runId"
$allowedProducers = "$managerProducer,$workerOneProducer,$workerTwoProducer"
$diagnosticToken = [Convert]::ToHexString([Security.Cryptography.RandomNumberGenerator]::GetBytes(32))
$fencingBase = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$result = [ordered]@{
    startedAt = (Get-Date).ToString('o')
    endedAt = $null
    status = 'FAILED'
    exitCode = 1
    runId = $runId
    resultPath = $resultPath
    clientAdapter = $null
    kafka = [ordered]@{}
    controlPlane = [ordered]@{}
    registration = [ordered]@{}
    distribution = [ordered]@{}
    durableMetadata = [ordered]@{}
    drain = [ordered]@{}
    crashUnknown = [ordered]@{}
    mismatch = [ordered]@{}
    logs = [ordered]@{}
}

function Write-Step([string] $Name, [string] $State) {
    Write-Host ("[{0}] {1} {2}" -f (Get-Date).ToString('o'), $State, $Name)
}

function Sanitize([string] $Text) {
    if ($null -eq $Text) { return '' }
    $safe = $Text -replace '(?i)(password|token|secret|authorization|cookie|session(?:id)?)\s*[=:]\s*[^,;\s]+', '$1=<masked>'
    $safe = ($safe -replace '[\r\n\t]+', ' ').Trim()
    if ($safe.Length -gt 2000) { return $safe.Substring(0, 2000) }
    return $safe
}

function Resolve-Client {
    if ($ClientAdapter -ne 'Docker') {
        if ($ClientPath -and (Test-Path -LiteralPath $ClientPath)) { return [pscustomobject]@{ mode='Host'; path=$ClientPath } }
        $client = Get-Command mariadb -ErrorAction SilentlyContinue
        if ($client) { return [pscustomobject]@{ mode='Host'; path=$client.Source } }
        if ($ClientAdapter -eq 'Host') { throw 'MariaDB host CLI not found' }
    }
    $docker = Get-Command docker -ErrorAction SilentlyContinue
    if ($docker) {
        $running = (& $docker.Source inspect --format '{{.State.Running}}' $MariaDbContainer 2>$null | Out-String).Trim()
        if ($LASTEXITCODE -eq 0 -and $running -eq 'true') { return [pscustomobject]@{ mode='Docker'; path=$docker.Source } }
    }
    throw 'MariaDB client unavailable: install host mariadb CLI or run cpf-mariadb container'
}

function Resolve-KafkaDocker {
    $docker = Get-Command docker -ErrorAction SilentlyContinue
    if (-not $docker) { throw 'Docker CLI is required to use the already-installed Kafka container' }
    $running = (& $docker.Source inspect --format '{{.State.Running}}' $KafkaContainer 2>$null | Out-String).Trim()
    if ($LASTEXITCODE -ne 0 -or $running -ne 'true') {
        throw "Installed Kafka container is not running: $KafkaContainer"
    }
    return $docker.Source
}

function Invoke-Sql([string] $Sql) {
    $psi = [Diagnostics.ProcessStartInfo]::new()
    $psi.UseShellExecute = $false
    $psi.RedirectStandardInput = $true
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    if ($script:MariaClient.mode -eq 'Docker') {
        $psi.FileName = $script:MariaClient.path
        foreach ($argument in @('exec','-i',$MariaDbContainer,'sh','-lc',"MYSQL_PWD=`"`$MARIADB_ROOT_PASSWORD`" mariadb --protocol=tcp --host=127.0.0.1 --port=3306 --user=$Username --ssl=0 --batch --skip-column-names --default-character-set=utf8mb4")) {
            [void] $psi.ArgumentList.Add($argument)
        }
    } else {
        $psi.FileName = $script:MariaClient.path
        foreach ($argument in @('--protocol=tcp',"--host=$HostName","--port=$Port","--user=$Username",'--ssl=0','--batch','--skip-column-names','--default-character-set=utf8mb4')) {
            [void] $psi.ArgumentList.Add($argument)
        }
        $psi.Environment['MYSQL_PWD'] = $Password
        $psi.Environment['MARIADB_PWD'] = $Password
    }
    $process = [Diagnostics.Process]::Start($psi)
    $process.StandardInput.Write($Sql)
    $process.StandardInput.Write("`n")
    $process.StandardInput.Close()
    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit()
    if ($process.ExitCode -ne 0) { throw "MariaDB query failed: $(Sanitize $stderr)" }
    return $stdout.Trim()
}

function Invoke-KafkaTopic([string[]] $Arguments) {
    $output = & $script:DockerPath exec $KafkaContainer /opt/kafka/bin/kafka-topics.sh --bootstrap-server localhost:9092 @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) { throw "Kafka topic command failed: $(Sanitize ($output -join ' '))" }
    return ($output -join "`n")
}

function New-SmokeTopic([string] $Topic, [int] $Partitions) {
    [void] (Invoke-KafkaTopic @('--create','--if-not-exists','--topic',$Topic,'--partitions',"$Partitions",'--replication-factor','1'))
    $createdTopics.Add($Topic)
}

function Wait-Until([scriptblock] $Condition, [int] $Seconds, [string] $Message) {
    $until = (Get-Date).AddSeconds($Seconds)
    while ((Get-Date) -lt $until) {
        if (& $Condition) { return }
        Start-Sleep -Milliseconds 300
    }
    throw $Message
}

function Save-WorkerStreams([string] $ProcessName) {
    $stream = $processStreams[$ProcessName]
    if (-not $stream -or $stream.saved) { return }
    $process = $processes[$ProcessName]
    if ($process -and -not $process.HasExited) { return }
    $stdout = $stream.stdoutTask.GetAwaiter().GetResult()
    $stderr = $stream.stderrTask.GetAwaiter().GetResult()
    [IO.File]::WriteAllText($stream.stdoutPath, $stdout, $Utf8NoBom)
    [IO.File]::WriteAllText($stream.stderrPath, $stderr, $Utf8NoBom)
    $stream.saved = $true
    $result.logs[$ProcessName] = [ordered]@{ stdout=$stream.stdoutPath; stderr=$stream.stderrPath; exitCode=$process.ExitCode }
}

function Assert-WorkersRunning {
    foreach ($processName in @($processes.Keys)) {
        $process = $processes[$processName]
        if ($process.HasExited) {
            Save-WorkerStreams $processName
            $stream = $processStreams[$processName]
            throw "Runtime $processName exited before verification ExitCode=$($process.ExitCode) stdout=$($stream.stdoutPath) stderr=$($stream.stderrPath)"
        }
    }
}

function Test-HttpOk([string] $Uri) {
    try {
        $response = Invoke-WebRequest -Method Get -Uri $Uri -TimeoutSec 3 -SkipHttpErrorCheck
        return $response.StatusCode -eq 200
    } catch { return $false }
}

function Resolve-BootJar([string] $ModulePath, [string] $Description) {
    $lib = Join-Path $Root "$ModulePath/build/libs"
    $jar = Get-ChildItem -LiteralPath $lib -File -Filter '*.jar' -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notmatch 'plain|sources|javadoc' } |
        Sort-Object LastWriteTimeUtc -Descending |
        Select-Object -First 1
    if (-not $jar) { throw "$Description bootJar missing: $lib" }
    return $jar
}

function Add-CommonEnvironment([Diagnostics.ProcessStartInfo] $Psi, [string] $InstanceId) {
    $Psi.Environment['CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CPF_PLATFORM_DB_ENABLED'] = 'true'
    $Psi.Environment['CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CPF_PLATFORM_DB_URL'] = "jdbc:mariadb://${HostName}:${Port}/${DatabaseName}"
    $Psi.Environment['CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CPF_PLATFORM_DB_USERNAME'] = $RuntimeUsername
    $Psi.Environment['CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CPF_PLATFORM_DB_PASSWORD'] = $RuntimePassword
    $Psi.Environment['CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CPF_PLATFORM_DB_DRIVER_CLASS_NAME'] = 'org.mariadb.jdbc.Driver'
    $Psi.Environment['CPF_RUNTIME_INSTANCE_ID'] = $InstanceId
    $Psi.Environment['CPF_WAS_ID'] = $InstanceId
    $Psi.Environment['CPF_BATCH_CONTROL_BASE_URL'] = "http://127.0.0.1:$ControlPlanePort"
    $Psi.Environment['CPF_LOG_ROOT'] = (Join-Path $runtimeDir 'logs')
    $Psi.Environment['CPF_DOMAIN_PERSISTENCE_PROVIDER'] = 'jdbc'
    $Psi.Environment['CPF_DB_VENDOR'] = $DbVendor.ToLowerInvariant()
    $Psi.Environment['CPF_DB_RESOURCE_ROOT'] = $DbResourceRoot
}

function Start-Runtime(
        [string] $ProcessName,
        [string] $ModulePath,
        [string] $Description,
        [string] $InstanceId,
        [int] $ServerPort,
        [string] $RemoteRole,
        [string] $ProducerId) {
    $jar = Resolve-BootJar $ModulePath $Description
    $processDir = Join-Path $runtimeDir $ProcessName
    New-Item -ItemType Directory -Force -Path $processDir | Out-Null
    $psi = [Diagnostics.ProcessStartInfo]::new()
    $psi.FileName = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME 'bin/java.exe' } else { 'java' }
    $psi.UseShellExecute = $false
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $arguments = @(
        '-Xms128m', '-Xmx512m', '-XX:+HeapDumpOnOutOfMemoryError',
        "-XX:HeapDumpPath=$processDir", "-XX:ErrorFile=$(Join-Path $processDir 'hs_err_pid%p.log')",
        '-jar', $jar.FullName, "--server.port=$ServerPort", '--spring.profiles.active=local',
        '--spring.batch.job.enabled=false', '--spring.kafka.bootstrap-servers=127.0.0.1:9092',
        '--spring.kafka.consumer.auto-offset-reset=earliest', '--spring.kafka.listener.missing-topics-fatal=true'
    )
    if ($RemoteRole -eq 'DISABLED') {
        $arguments += '--cpf.batch.remote.transport=disabled'
    } else {
        $arguments += @(
            '--cpf.batch.remote.transport=kafka', "--cpf.batch.remote.kafka.role=$RemoteRole",
            '--cpf.batch.diagnostic.enabled=true',
            "--cpf.batch.remote.kafka.request-topic=$requestTopic",
            "--cpf.batch.remote.kafka.reply-topic-prefix=$replyTopicPrefix",
            "--cpf.batch.remote.kafka.consumer-group=$consumerGroup",
            "--cpf.batch.remote.kafka.manager-instance-id=$managerId",
            "--cpf.batch.remote.kafka.producer-id=$ProducerId",
            "--cpf.batch.remote.kafka.allowed-producer-ids=$allowedProducers",
            '--cpf.batch.remote.kafka.send-timeout=PT15S', '--cpf.batch.remote.kafka.message-ttl=PT45S',
            '--cpf.batch.remote.kafka.retry-backoff-millis=500', '--cpf.batch.remote.kafka.max-delivery-attempts=3',
            '--cpf.batch.execution.remote-poll-interval-ms=100', '--cpf.batch.execution.remote-timeout-ms=20000'
        )
    }
    foreach ($argument in $arguments) { [void] $psi.ArgumentList.Add($argument) }
    Add-CommonEnvironment $psi $InstanceId
    if ($RemoteRole -eq 'MANAGER') { $psi.Environment['CPF_BAT_DIAGNOSTIC_TOKEN'] = $diagnosticToken }
    if ($RemoteRole -eq 'WORKER') {
        $psi.Environment['CPF_BAT_WORKER_ID'] = $ProcessName
        $psi.Environment['CPF_BAT_WORKER_VERSION'] = 'runtime-smoke-v2'
        $psi.Environment['CPF_BAT_WORKER_CAPABILITIES'] = 'CPF_BAT_DIAGNOSTIC'
    }
    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $psi
    [void] $process.Start()
    $processes[$ProcessName] = $process
    $processStreams[$ProcessName] = [pscustomobject]@{
        stdoutPath = Join-Path $processDir "$ProcessName.stdout.log"
        stderrPath = Join-Path $processDir "$ProcessName.stderr.log"
        stdoutTask = $process.StandardOutput.ReadToEndAsync()
        stderrTask = $process.StandardError.ReadToEndAsync()
        saved = $false
    }
    return $process
}

function Invoke-Diagnostic([string] $Key, [long] $Fence, [int] $Partitions, [long] $SleepMs) {
    $body = [ordered]@{ idempotencyKey=$Key; fencingToken=$Fence; partitions=$Partitions; sleepMs=$SleepMs } | ConvertTo-Json -Compress
    $response = Invoke-WebRequest -Method Post -Uri "http://127.0.0.1:$ControlPlanePort/internal/v1/batch/remote-diagnostic/executions" `
        -Headers @{ 'X-CPF-Batch-Diagnostic-Token'=$diagnosticToken } -ContentType 'application/json' -Body $body -TimeoutSec 90 -SkipHttpErrorCheck
    if ($response.StatusCode -ne 202) { throw "Diagnostic launch failed with HTTP $($response.StatusCode): $(Sanitize $response.Content)" }
    return $response.Content | ConvertFrom-Json
}

function Get-WorkerRequestCount([string] $ProducerId) {
    return [int] (Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.BAT_REMOTE_MESSAGE_LEDGER WHERE direction_cd='REQUEST' AND owner_id LIKE '${ProducerId}:%';")
}

try {
    if ([string]::IsNullOrWhiteSpace($Password)) { throw 'CPF_DB_ROOT_PASSWORD or CPF_ADMIN_PASSWORD is required' }
    if ([string]::IsNullOrWhiteSpace($RuntimeUsername) -or [string]::IsNullOrWhiteSpace($RuntimePassword)) { throw 'Batch Worker runtime database credentials are required' }
    $script:MariaClient = Resolve-Client
    $script:DockerPath = Resolve-KafkaDocker
    $result.clientAdapter = $script:MariaClient.mode
    Write-Step 'installed MariaDB/Kafka adapters' 'DONE'

    New-SmokeTopic $requestTopic 2
    New-SmokeTopic "$requestTopic.DLT" 2
    New-SmokeTopic "$replyTopicPrefix.$managerId" 1
    New-SmokeTopic "$replyTopicPrefix.$managerId.DLT" 1
    $result.kafka = [ordered]@{ status='DONE'; container=$KafkaContainer; createdTopicCount=$createdTopics.Count; installedAssetOnly=$true }
    Write-Step 'isolated Kafka topics' 'DONE'

    [void] (Start-Runtime 'bat-smoke-control-plane' 'cpf-batch/control-plane' 'Control Plane' 'bat-smoke-control-instance' $ControlPlanePort 'MANAGER' $managerProducer)
    Wait-Until { Assert-WorkersRunning; Test-HttpOk "http://127.0.0.1:$ControlPlanePort/actuator/health/liveness" } 120 'Control Plane did not become live'
    $result.controlPlane = [ordered]@{ status='DONE'; port=$ControlPlanePort; liveness=200 }

    [void] (Start-Runtime 'bat-smoke-center-cut' 'cpf-batch/center-cut' 'Center Cut' $managerId $CenterCutPort 'DISABLED' $managerProducer)
    [void] (Start-Runtime 'bat-smoke-worker-1' 'cpf-batch/worker' 'Worker' 'bat-smoke-instance-1' $WorkerOnePort 'WORKER' $workerOneProducer)
    [void] (Start-Runtime 'bat-smoke-worker-2' 'cpf-batch/worker' 'Worker' 'bat-smoke-instance-2' $WorkerTwoPort 'WORKER' $workerTwoProducer)
    Wait-Until {
        Assert-WorkersRunning
        (Test-HttpOk "http://127.0.0.1:$CenterCutPort/actuator/health/liveness") -and
        (Test-HttpOk "http://127.0.0.1:$WorkerOnePort/actuator/health/liveness") -and
        (Test-HttpOk "http://127.0.0.1:$WorkerTwoPort/actuator/health/liveness")
    } 150 'Center Cut and two Workers did not become live'
    Wait-Until {
        Assert-WorkersRunning
        [int] (Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.BAT_WORKER WHERE instance_id IN ('bat-smoke-instance-1','bat-smoke-instance-2') AND active_yn='Y';") -eq 2
    } 90 'two canonical Worker instances not registered'
    $jobPackCount = [int] (Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.BAT_JOB_PACK WHERE job_pack_id='CPF-BAT-DIAGNOSTIC';")
    $jobCount = [int] (Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.BAT_JOB_PACK_JOB WHERE job_pack_id='CPF-BAT-DIAGNOSTIC' AND job_id='CPF_BAT_DIAGNOSTIC_JOB';")
    if ($jobPackCount -ne 1 -or $jobCount -ne 1) { throw 'Workers did not register the diagnostic Job Pack through Control Plane' }
    $result.registration = [ordered]@{ status='DONE'; workerCount=2; jobPackCount=$jobPackCount; jobCount=$jobCount }
    Write-Step 'Control Plane, Center Cut and two Worker registration' 'DONE'

    $normal = Invoke-Diagnostic "normal-$runId" ($fencingBase + 1) 6 600
    $normalExecutionId = [long] $normal.jobExecutionId
    $normalStatus = Invoke-Sql "SELECT STATUS FROM ${DatabaseName}.BAT_SB_JOB_EXECUTION WHERE JOB_EXECUTION_ID=$normalExecutionId;"
    $workerStepNameRegex = '^cpfRemotePartitionWorkerStep:partition-[0-9]+$'
    $workerSteps = [int] (Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.BAT_SB_STEP_EXECUTION WHERE JOB_EXECUTION_ID=$normalExecutionId AND STEP_NAME REGEXP '$workerStepNameRegex' AND STATUS='COMPLETED';")
    $distinctWorkers = [int] (Invoke-Sql "SELECT COUNT(DISTINCT SUBSTRING_INDEX(owner_id, ':', 1)) FROM ${DatabaseName}.BAT_REMOTE_MESSAGE_LEDGER WHERE direction_cd='REQUEST' AND owner_id LIKE 'bat-smoke-worker-%-${runId}:%';")
    if ($normalStatus -ne 'COMPLETED' -or $workerSteps -ne 6 -or $distinctWorkers -ne 2) { throw "Actual distributed execution mismatch status=$normalStatus workerSteps=$workerSteps distinctWorkers=$distinctWorkers" }
    $jobInstanceCount = [int] (Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.BAT_SB_JOB_INSTANCE i JOIN ${DatabaseName}.BAT_SB_JOB_EXECUTION e ON e.JOB_INSTANCE_ID=i.JOB_INSTANCE_ID WHERE e.JOB_EXECUTION_ID=$normalExecutionId;")
    $stepContextCount = [int] (Invoke-Sql "SELECT COUNT(*) FROM ${DatabaseName}.BAT_SB_STEP_EXECUTION_CONTEXT c JOIN ${DatabaseName}.BAT_SB_STEP_EXECUTION s ON s.STEP_EXECUTION_ID=c.STEP_EXECUTION_ID WHERE s.JOB_EXECUTION_ID=$normalExecutionId AND s.STEP_NAME REGEXP '$workerStepNameRegex';")
    if ($jobInstanceCount -ne 1 -or $stepContextCount -ne 6) { throw 'Spring Batch durable metadata is incomplete' }
    $result.distribution = [ordered]@{ status='DONE'; jobExecutionId=$normalExecutionId; partitions=6; distinctWorkers=2; duplicateExecution=0 }
    $result.durableMetadata = [ordered]@{ status='DONE'; BAT_SB_JOB_INSTANCE=$jobInstanceCount; BAT_SB_JOB_EXECUTION=1; BAT_SB_STEP_EXECUTION=$workerSteps; BAT_SB_STEP_EXECUTION_CONTEXT=$stepContextCount }
    Write-Step 'actual Kafka remote partition and BAT_SB durable metadata' 'DONE'

    $workerOneBeforeDrain = Get-WorkerRequestCount $workerOneProducer
    $workerTwoBeforeDrain = Get-WorkerRequestCount $workerTwoProducer
    [void] (Invoke-WebRequest -Method Post -Uri "http://127.0.0.1:$WorkerOnePort/internal/v1/worker/drain" -TimeoutSec 10 -SkipHttpErrorCheck)
    Wait-Until { (Invoke-Sql "SELECT control_status FROM ${DatabaseName}.BAT_WORKER WHERE instance_id='bat-smoke-instance-1';") -eq 'DRAINING' } 20 'Worker 1 did not enter DRAINING'
    $drainExecution = Invoke-Diagnostic "drain-$runId" ($fencingBase + 2) 4 300
    $workerOneAfterDrain = Get-WorkerRequestCount $workerOneProducer
    $workerTwoAfterDrain = Get-WorkerRequestCount $workerTwoProducer
    if ($workerOneAfterDrain -ne $workerOneBeforeDrain -or $workerTwoAfterDrain -le $workerTwoBeforeDrain) { throw 'Draining Worker accepted new work or remaining Worker did not take over' }
    $result.drain = [ordered]@{ status='DONE'; jobExecutionId=[long]$drainExecution.jobExecutionId; drainingWorkerNewClaimCount=0; remainingWorkerClaimed=$true }

    [void] (Invoke-WebRequest -Method Post -Uri "http://127.0.0.1:$WorkerOnePort/internal/v1/worker/resume" -TimeoutSec 10 -SkipHttpErrorCheck)
    Wait-Until { (Invoke-Sql "SELECT control_status FROM ${DatabaseName}.BAT_WORKER WHERE instance_id='bat-smoke-instance-1';") -eq 'RUNNING' } 20 'Worker 1 did not resume'
    $resumeExecution = Invoke-Diagnostic "resume-$runId" ($fencingBase + 3) 6 300
    $workerOneAfterResume = Get-WorkerRequestCount $workerOneProducer
    if ($workerOneAfterResume -le $workerOneAfterDrain) { throw 'Resumed Worker did not receive new work' }
    $result.drain.resumeJobExecutionId = [long] $resumeExecution.jobExecutionId
    $result.drain.resumedWorkerClaimed = $true
    Write-Step 'physical drain, takeover and resume' 'DONE'

    $invalid = Invoke-WebRequest -Method Post -Uri "http://127.0.0.1:$ControlPlanePort/internal/v1/batch/remote-diagnostic/executions" `
        -Headers @{ 'X-CPF-Batch-Diagnostic-Token'='invalid-diagnostic-token-value-0000' } -ContentType 'application/json' `
        -Body (@{idempotencyKey="denied-$runId";fencingToken=($fencingBase+4);partitions=2;sleepMs=0}|ConvertTo-Json -Compress) -TimeoutSec 10 -SkipHttpErrorCheck
    if ($invalid.StatusCode -lt 400) { throw 'Invalid diagnostic token was accepted' }
    $duplicate = Invoke-Diagnostic "resume-$runId" ($fencingBase + 3) 6 300
    if ([long]$duplicate.jobExecutionId -ne [long]$resumeExecution.jobExecutionId) { throw 'Idempotent replay created another JobExecution' }
    $conflictRejected = $false
    try { [void] (Invoke-Diagnostic "resume-$runId" ($fencingBase + 3) 4 300) } catch { $conflictRejected = $true }
    if (-not $conflictRejected) { throw 'Conflicting duplicate launch was accepted' }
    $result.mismatch = [ordered]@{ status='DONE'; invalidTokenRejected=$true; idempotentReplayStable=$true; conflictingReplayRejected=$true }
    Write-Step 'negative token and idempotency paths' 'DONE'

    $crashKey = "crash-$runId"
    $crashBody = @{idempotencyKey=$crashKey;fencingToken=($fencingBase+4);partitions=2;sleepMs=30000}|ConvertTo-Json -Compress
    $httpClient = [Net.Http.HttpClient]::new()
    $request = [Net.Http.HttpRequestMessage]::new([Net.Http.HttpMethod]::Post, "http://127.0.0.1:$ControlPlanePort/internal/v1/batch/remote-diagnostic/executions")
    [void] $request.Headers.Add('X-CPF-Batch-Diagnostic-Token', $diagnosticToken)
    $request.Content = [Net.Http.StringContent]::new($crashBody, [Text.Encoding]::UTF8, 'application/json')
    $crashHttpTask = $httpClient.SendAsync($request)
    Wait-Until {
        $runningInstance = Invoke-Sql "SELECT instance_id FROM ${DatabaseName}.BAT_WORKER WHERE instance_id IN ('bat-smoke-instance-1','bat-smoke-instance-2') AND current_execution_id IS NOT NULL ORDER BY instance_id LIMIT 1;"
        -not [string]::IsNullOrWhiteSpace($runningInstance)
    } 20 'No Worker exposed the in-flight crash execution'
    $crashOwnerInstance = Invoke-Sql "SELECT instance_id FROM ${DatabaseName}.BAT_WORKER WHERE instance_id IN ('bat-smoke-instance-1','bat-smoke-instance-2') AND current_execution_id IS NOT NULL ORDER BY instance_id LIMIT 1;"
    $crashProcessName = if ($crashOwnerInstance -eq 'bat-smoke-instance-1') { 'bat-smoke-worker-1' } else { 'bat-smoke-worker-2' }
    Stop-Process -Id $processes[$crashProcessName].Id -Force
    [void] $processes[$crashProcessName].WaitForExit(10000)
    Save-WorkerStreams $crashProcessName
    [void] $crashHttpTask.GetAwaiter().GetResult()
    Wait-Until { (Invoke-Sql "SELECT control_status FROM ${DatabaseName}.BAT_EXECUTION_CONTROL WHERE idempotency_key='$crashKey';") -eq 'UNKNOWN_RESULT' } 45 'Crashed in-flight execution was not isolated as UNKNOWN_RESULT'
    $crashAttempts = [int] (Invoke-Sql "SELECT COALESCE(MAX(attempt_no),0) FROM ${DatabaseName}.BAT_REMOTE_MESSAGE_LEDGER WHERE direction_cd='REQUEST' AND owner_id LIKE '${crashProcessName}-${runId}:%';")
    Start-Sleep -Seconds 3
    $crashAttemptsAfter = [int] (Invoke-Sql "SELECT COALESCE(MAX(attempt_no),0) FROM ${DatabaseName}.BAT_REMOTE_MESSAGE_LEDGER WHERE direction_cd='REQUEST' AND owner_id LIKE '${crashProcessName}-${runId}:%';")
    if ($crashAttemptsAfter -ne $crashAttempts) { throw 'UNKNOWN_RESULT was blindly retried' }
    $result.crashUnknown = [ordered]@{ status='DONE'; ownerInstance=$crashOwnerInstance; unknownResult=$true; blindRetryCount=0 }
    Write-Step 'crash isolation and no blind retry' 'DONE'

    $result.status = 'DONE'
    $result.exitCode = 0
} catch {
    $failure = $_
    $result.error = Sanitize $_.Exception.Message
} finally {
    foreach ($processName in @($processes.Keys)) {
        $process = $processes[$processName]
        if ($process -and -not $process.HasExited) { Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue }
        if ($process) { [void] $process.WaitForExit(10000); Save-WorkerStreams $processName }
    }
    foreach ($topic in @($createdTopics)) {
        try { [void] (Invoke-KafkaTopic @('--delete','--if-exists','--topic',$topic)) } catch { $result.kafka.cleanupError = Sanitize $_.Exception.Message }
    }
    if ($httpClient) { $httpClient.Dispose() }
    $result.kafka.cleanedTopicCount = $createdTopics.Count
    $result.endedAt = (Get-Date).ToString('o')
    [IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 20), $Utf8NoBom)
    Write-Host ("[{0}] END status={1} exitCode={2} result={3}" -f $result.endedAt, $result.status, $result.exitCode, $resultPath)
}
if ($failure) { throw "Batch two-Worker Runtime verification failed: $($result.error). Logs: $runtimeDir Result: $resultPath" }
