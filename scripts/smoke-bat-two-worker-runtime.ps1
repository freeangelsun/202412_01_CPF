param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = "",
    [string] $HostName = $(if ($env:CPF_DB_HOST) { $env:CPF_DB_HOST } else { 'localhost' }),
    [string] $Port = $(if ($env:CPF_DB_PORT) { $env:CPF_DB_PORT } else { '3306' }),
    [string] $Username = $(if ($env:CPF_DB_ROOT_USERNAME) { $env:CPF_DB_ROOT_USERNAME } else { 'root' }),
    [string] $Password = $env:CPF_DB_ROOT_PASSWORD,
    [string] $ClientPath = $env:CPF_MARIADB_CLI
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = 'Stop'
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root 'build/runtime-smoke'
} elseif (-not [IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$runtimeDir = Join-Path $Root 'build/bat-two-worker-runtime'
$resultPath = Join-Path $ResultDir 'bat-two-worker-runtime.sanitized.json'
$processes = [System.Collections.Generic.List[Diagnostics.Process]]::new()
$result = [ordered]@{
    startedAt = (Get-Date).ToString('o')
    status = 'FAILED'
    workerRegistration = [ordered]@{}
    distribution = [ordered]@{}
    drain = [ordered]@{}
    crashTakeover = [ordered]@{}
    mismatch = [ordered]@{}
}

function Resolve-Client {
    if ($ClientPath -and (Test-Path -LiteralPath $ClientPath -PathType Leaf)) { return $ClientPath }
    $command = Get-Command mariadb -ErrorAction SilentlyContinue
    if ($command) { return $command.Source }
    throw 'MariaDB CLI를 찾지 못했습니다.'
}

function Invoke-Sql([string] $Sql) {
    $sqlPath = Join-Path $runtimeDir ('query-' + [guid]::NewGuid().ToString('N') + '.sql')
    [IO.File]::WriteAllText($sqlPath, $Sql, $Utf8NoBom)
    try {
        $start = [Diagnostics.ProcessStartInfo]::new()
        $start.FileName = $script:MariaClient
        $start.UseShellExecute = $false
        $start.RedirectStandardInput = $true
        $start.RedirectStandardOutput = $true
        $start.RedirectStandardError = $true
        try {
            $start.StandardInputEncoding = [Text.Encoding]::UTF8
        } catch {
            # Windows PowerShell 5.1 호환 환경에서는 시스템 기본 인코딩을 사용합니다.
        }
        $start.StandardOutputEncoding = [Text.Encoding]::UTF8
        $start.StandardErrorEncoding = [Text.Encoding]::UTF8
        $start.Arguments = "--protocol=tcp --host=`"$HostName`" --port=$Port --user=`"$Username`" " +
                '--ssl=0 --batch --skip-column-names --default-character-set=utf8mb4'
        $start.Environment['MYSQL_PWD'] = $Password
        $start.Environment['MARIADB_PWD'] = $Password
        $process = [Diagnostics.Process]::Start($start)
        # Windows PowerShell 5.1의 stdin writer는 UTF-8 인코딩을 지정할 수 없으므로
        # ASCII SOURCE 명령만 전달하고 SQL 본문은 client가 UTF-8 파일로 직접 읽게 합니다.
        $sourcePath = (Resolve-Path -LiteralPath $sqlPath).Path.Replace('\', '/')
        $process.StandardInput.Write("SOURCE $sourcePath;`n")
        $process.StandardInput.Close()
        $stdout = $process.StandardOutput.ReadToEnd()
        $stderr = $process.StandardError.ReadToEnd()
        $process.WaitForExit()
        if ($process.ExitCode -ne 0) { throw "MariaDB query 실패: $stderr" }
        return $stdout.Trim()
    } finally {
        Remove-Item -LiteralPath $sqlPath -Force -ErrorAction SilentlyContinue
    }
}

function Wait-Until([scriptblock] $Condition, [int] $TimeoutSeconds, [string] $Message) {
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        if (& $Condition) { return }
        Start-Sleep -Milliseconds 300
    }
    throw $Message
}

function Start-Worker([string] $WorkerId, [string] $InstanceId, [int] $Index) {
    $jar = Get-ChildItem -LiteralPath (Join-Path $Root 'cpf-batch/build/libs') -File -Filter '*.jar' |
        Where-Object { $_.Name -notlike '*-plain.jar' } | Select-Object -First 1
    if ($null -eq $jar) { throw 'BAT bootJar를 찾지 못했습니다.' }
    $stdout = Join-Path $runtimeDir "worker-$Index.stdout.log"
    $stderr = Join-Path $runtimeDir "worker-$Index.stderr.log"
    $old = @{
        CPF_DB_URL = $env:CPF_DB_URL; CPF_DB_USERNAME = $env:CPF_DB_USERNAME; CPF_DB_PASSWORD = $env:CPF_DB_PASSWORD
        BAT_DATASOURCE_URL = $env:BAT_DATASOURCE_URL; BAT_DATASOURCE_USERNAME = $env:BAT_DATASOURCE_USERNAME
        BAT_DATASOURCE_PASSWORD = $env:BAT_DATASOURCE_PASSWORD
        CPF_INSTANCE_ID = $env:CPF_INSTANCE_ID; CPF_BAT_WORKER_ID = $env:CPF_BAT_WORKER_ID
        CPF_BAT_WORKER_VERSION = $env:CPF_BAT_WORKER_VERSION; CPF_BAT_WORKER_CAPABILITIES = $env:CPF_BAT_WORKER_CAPABILITIES
        CPF_BAT_WORKER_ENABLED = $env:CPF_BAT_WORKER_ENABLED; CPF_BAT_WORKER_LEASE_SECONDS = $env:CPF_BAT_WORKER_LEASE_SECONDS
        CPF_BAT_WORKER_HEARTBEAT_INTERVAL_MS = $env:CPF_BAT_WORKER_HEARTBEAT_INTERVAL_MS
        CPF_BAT_WORKER_POLL_INTERVAL_MS = $env:CPF_BAT_WORKER_POLL_INTERVAL_MS
        CPF_BAT_WORKER_MAX_CONCURRENCY = $env:CPF_BAT_WORKER_MAX_CONCURRENCY
        CPF_BAT_WORKER_QUEUE_CAPACITY = $env:CPF_BAT_WORKER_QUEUE_CAPACITY
        CPF_BAT_SCHEDULER_POOL_SIZE = $env:CPF_BAT_SCHEDULER_POOL_SIZE
        CPF_BAT_SMOKE_HEARTBEAT_ITERATIONS = $env:CPF_BAT_SMOKE_HEARTBEAT_ITERATIONS
        CPF_BAT_SMOKE_HEARTBEAT_SLEEP_MS = $env:CPF_BAT_SMOKE_HEARTBEAT_SLEEP_MS
    }
    try {
        $env:CPF_DB_URL = "jdbc:mariadb://${HostName}:${Port}/cpfDB"
        $env:CPF_DB_USERNAME = $Username
        $env:CPF_DB_PASSWORD = $Password
        $env:BAT_DATASOURCE_URL = "jdbc:mariadb://${HostName}:${Port}/batDB"
        $env:BAT_DATASOURCE_USERNAME = $Username
        $env:BAT_DATASOURCE_PASSWORD = $Password
        $env:CPF_INSTANCE_ID = $InstanceId
        $env:CPF_BAT_WORKER_ID = $WorkerId
        $env:CPF_BAT_WORKER_VERSION = 'runtime-smoke-v1'
        $env:CPF_BAT_WORKER_CAPABILITIES = 'CPF_BAT_HEARTBEAT_JOB'
        $env:CPF_BAT_WORKER_ENABLED = 'true'
        $env:CPF_BAT_WORKER_LEASE_SECONDS = '12'
        $env:CPF_BAT_WORKER_HEARTBEAT_INTERVAL_MS = '500'
        $env:CPF_BAT_WORKER_POLL_INTERVAL_MS = '200'
        $env:CPF_BAT_WORKER_MAX_CONCURRENCY = '1'
        $env:CPF_BAT_WORKER_QUEUE_CAPACITY = '1'
        $env:CPF_BAT_SCHEDULER_POOL_SIZE = '4'
        $env:CPF_BAT_SMOKE_HEARTBEAT_ITERATIONS = '4'
        $env:CPF_BAT_SMOKE_HEARTBEAT_SLEEP_MS = '500'
        $process = Start-Process -FilePath (Join-Path $env:JAVA_HOME 'bin/java.exe') `
            -ArgumentList @('-jar', $jar.FullName, '--spring.profiles.active=local,bat-worker', '--server.port=0') `
            -PassThru -WindowStyle Hidden -RedirectStandardOutput $stdout -RedirectStandardError $stderr
        $processes.Add($process) | Out-Null
        return $process
    } finally {
        foreach ($key in $old.Keys) {
            [Environment]::SetEnvironmentVariable($key, $old[$key], 'Process')
        }
    }
}

try {
    if ([string]::IsNullOrWhiteSpace($Password)) { throw 'DB password가 없어 2-worker runtime을 검증할 수 없습니다.' }
    if (Test-Path -LiteralPath $runtimeDir) { Remove-Item -LiteralPath $runtimeDir -Recurse -Force }
    New-Item -ItemType Directory -Force -Path $runtimeDir | Out-Null
    $script:MariaClient = Resolve-Client

    [void](Invoke-Sql @"
USE batDB;
DELETE FROM bat_execution_lease WHERE execution_id IN (
    SELECT execution_id FROM bat_execution WHERE created_by = 'BAT_TWO_WORKER_SMOKE'
);
DELETE FROM bat_execution WHERE created_by = 'BAT_TWO_WORKER_SMOKE';
DELETE FROM bat_worker WHERE worker_id IN ('bat-smoke-worker-1', 'bat-smoke-worker-2');
INSERT INTO bat_job (job_id, job_name, job_type, description, restartable_yn, use_yn, created_by, updated_by)
VALUES ('CPF_BAT_HEARTBEAT_JOB', 'BAT 2-worker heartbeat 검증 Job', 'TASKLET', '2-worker claim과 takeover 검증', 'Y', 'Y', 'BAT', 'BAT')
ON DUPLICATE KEY UPDATE job_name=VALUES(job_name), use_yn='Y', updated_by='BAT', updated_at=CURRENT_TIMESTAMP;
"@)

    $first = Start-Worker 'bat-smoke-worker-1' 'bat-smoke-instance-1' 1
    $second = Start-Worker 'bat-smoke-worker-2' 'bat-smoke-instance-2' 2
    Wait-Until { [int](Invoke-Sql "SELECT COUNT(*) FROM batDB.bat_worker WHERE worker_id IN ('bat-smoke-worker-1','bat-smoke-worker-2') AND active_yn='Y';") -eq 2 } 60 'worker 2개가 등록되지 않았습니다.'
    $result.workerRegistration = [ordered]@{ status = 'DONE'; workerCount = 2; distinctProcessCount = 2; distinctInstanceCount = 2 }

    [void](Invoke-Sql @"
USE batDB;
INSERT INTO bat_execution (
    job_id, job_parameters, execution_status, required_worker_version, required_capability,
    requested_by, created_by, updated_by
)
SELECT 'CPF_BAT_HEARTBEAT_JOB', CONCAT('{"case":"distribution","sequence":', seq, '}'), 'READY',
       'runtime-smoke-v1', 'CPF_BAT_HEARTBEAT_JOB', 'BAT_TWO_WORKER_SMOKE', 'BAT_TWO_WORKER_SMOKE', 'BAT_TWO_WORKER_SMOKE'
FROM (SELECT 1 seq UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4) value_source;
"@)
    Wait-Until { [int](Invoke-Sql "SELECT COUNT(*) FROM batDB.bat_execution WHERE created_by='BAT_TWO_WORKER_SMOKE' AND execution_status='COMPLETED';") -ge 4 } 90 '분산 실행 4건이 완료되지 않았습니다.'
    $distribution = (Invoke-Sql "SELECT COUNT(DISTINCT worker_id), COUNT(*), COUNT(DISTINCT execution_id) FROM batDB.bat_execution_lease WHERE execution_id IN (SELECT execution_id FROM batDB.bat_execution WHERE created_by='BAT_TWO_WORKER_SMOKE');").Split("`t")
    if ([int]$distribution[0] -ne 2 -or [int]$distribution[1] -ne [int]$distribution[2]) { throw '2-worker 분산 또는 중복 claim 검증에 실패했습니다.' }
    $duplicateSpringExecutionCount = [int](Invoke-Sql @"
SELECT COUNT(*)
FROM (
    SELECT p.PARAMETER_VALUE
    FROM batDB.BATCH_JOB_EXECUTION_PARAMS p
    WHERE p.PARAMETER_NAME = 'cpfCpfExecutionId'
      AND CAST(p.PARAMETER_VALUE AS UNSIGNED) IN (
          SELECT execution_id
          FROM batDB.bat_execution
          WHERE created_by = 'BAT_TWO_WORKER_SMOKE'
            AND job_parameters LIKE '%distribution%'
      )
    GROUP BY p.PARAMETER_VALUE
    HAVING COUNT(*) > 1
) duplicated;
"@)
    if ($duplicateSpringExecutionCount -ne 0) { throw '정상 분산 단계에서 동일 CPF execution의 Spring Batch 중복 실행이 발생했습니다.' }
    $result.distribution = [ordered]@{
        status = 'DONE'
        distinctWorkerCount = [int]$distribution[0]
        leaseCount = [int]$distribution[1]
        duplicateClaimCount = 0
        duplicateSpringExecutionCount = 0
    }

    $worker1Before = [int](Invoke-Sql "SELECT COUNT(*) FROM batDB.bat_execution_lease WHERE worker_id='bat-smoke-worker-1';")
    [void](Invoke-Sql "UPDATE batDB.bat_worker SET control_status='DRAINING', updated_by='BAT_TWO_WORKER_SMOKE' WHERE worker_id='bat-smoke-worker-1';")
    Start-Sleep -Seconds 2
    [void](Invoke-Sql @"
INSERT INTO batDB.bat_execution (
    job_id, job_parameters, execution_status, required_worker_version, required_capability,
    requested_by, created_by, updated_by
) VALUES ('CPF_BAT_HEARTBEAT_JOB', '{"case":"drain"}', 'READY', 'runtime-smoke-v1', 'CPF_BAT_HEARTBEAT_JOB',
          'BAT_TWO_WORKER_SMOKE', 'BAT_TWO_WORKER_SMOKE', 'BAT_TWO_WORKER_SMOKE');
"@)
    Wait-Until { [int](Invoke-Sql "SELECT COUNT(*) FROM batDB.bat_execution WHERE created_by='BAT_TWO_WORKER_SMOKE' AND execution_status='COMPLETED';") -ge 5 } 60 'drain 이후 실행이 완료되지 않았습니다.'
    $worker1After = [int](Invoke-Sql "SELECT COUNT(*) FROM batDB.bat_execution_lease WHERE worker_id='bat-smoke-worker-1';")
    if ($worker1After -ne $worker1Before) { throw 'DRAINING worker가 신규 실행을 claim했습니다.' }
    $result.drain = [ordered]@{ status = 'DONE'; drainingWorkerNewClaimCount = 0 }

    [void](Invoke-Sql "UPDATE batDB.bat_worker SET control_status='RUNNING' WHERE worker_id='bat-smoke-worker-1';")
    [void](Invoke-Sql @"
INSERT INTO batDB.bat_execution (
    job_id, job_parameters, execution_status, required_worker_version, required_capability,
    requested_by, created_by, updated_by
) VALUES ('CPF_BAT_HEARTBEAT_JOB', '{"case":"crash"}', 'READY', 'runtime-smoke-v1', 'CPF_BAT_HEARTBEAT_JOB',
          'BAT_TWO_WORKER_SMOKE_CRASH', 'BAT_TWO_WORKER_SMOKE', 'BAT_TWO_WORKER_SMOKE');
"@)
    $crashExecutionId = [long](Invoke-Sql "SELECT MAX(execution_id) FROM batDB.bat_execution WHERE requested_by='BAT_TWO_WORKER_SMOKE_CRASH';")
    Wait-Until { (Invoke-Sql "SELECT COUNT(*) FROM batDB.bat_execution_lease WHERE execution_id=$crashExecutionId AND lease_status='RUNNING';") -eq '1' } 30 'crash 대상 실행이 claim되지 않았습니다.'
    $crashOwner = Invoke-Sql "SELECT worker_id FROM batDB.bat_execution_lease WHERE execution_id=$crashExecutionId;"
    if ($crashOwner -eq 'bat-smoke-worker-1') { Stop-Process -Id $first.Id -Force } else { Stop-Process -Id $second.Id -Force }
    Wait-Until { (Invoke-Sql "SELECT execution_status FROM batDB.bat_execution WHERE execution_id=$crashExecutionId;") -eq 'COMPLETED' } 75 'lease 만료 후 surviving worker takeover가 완료되지 않았습니다.'
    $takeover = (Invoke-Sql "SELECT worker_id, takeover_count, attempt_no FROM batDB.bat_execution_lease WHERE execution_id=$crashExecutionId;").Split("`t")
    if ($takeover[0] -eq $crashOwner -or [int]$takeover[1] -lt 1 -or [int]$takeover[2] -lt 2) { throw 'crash lease takeover 메타가 올바르지 않습니다.' }
    $result.crashTakeover = [ordered]@{ status = 'DONE'; ownerChanged = $true; takeoverCount = [int]$takeover[1]; attemptNo = [int]$takeover[2] }

    [void](Invoke-Sql @"
INSERT INTO batDB.bat_execution (
    job_id, job_parameters, execution_status, required_worker_version, required_capability,
    requested_by, created_by, updated_by
) VALUES
('CPF_BAT_HEARTBEAT_JOB', '{"case":"version-mismatch"}', 'READY', 'not-installed-version', 'CPF_BAT_HEARTBEAT_JOB', 'BAT_TWO_WORKER_SMOKE_MISMATCH', 'BAT_TWO_WORKER_SMOKE', 'BAT_TWO_WORKER_SMOKE'),
('CPF_BAT_HEARTBEAT_JOB', '{"case":"capability-mismatch"}', 'READY', 'runtime-smoke-v1', 'NOT_SUPPORTED', 'BAT_TWO_WORKER_SMOKE_MISMATCH', 'BAT_TWO_WORKER_SMOKE', 'BAT_TWO_WORKER_SMOKE');
"@)
    Start-Sleep -Seconds 3
    $mismatchReady = [int](Invoke-Sql "SELECT COUNT(*) FROM batDB.bat_execution WHERE requested_by='BAT_TWO_WORKER_SMOKE_MISMATCH' AND execution_status='READY' AND worker_id IS NULL;")
    if ($mismatchReady -ne 2) { throw 'version/capability mismatch 실행이 claim되었습니다.' }
    $result.mismatch = [ordered]@{ status = 'DONE'; rejectedCount = 2 }
    $result.status = 'DONE'
} finally {
    foreach ($process in $processes) {
        if ($process -and -not $process.HasExited) {
            Stop-Process -Id $process.Id -Force -ErrorAction SilentlyContinue
        }
    }
    $result.endedAt = (Get-Date).ToString('o')
    [IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 20), $Utf8NoBom)
}

Write-Host 'BAT 2-worker claim·drain·crash·takeover runtime 검증 완료'
