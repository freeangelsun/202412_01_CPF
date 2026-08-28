[CmdletBinding()]
param(
    [string]$Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string]$EvidenceDirectory = "",
    [string]$SourceIdentity = $env:CPF_SOURCE_SHA,
    [switch]$RestartBrokerIfOwned
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
if ([string]::IsNullOrWhiteSpace($EvidenceDirectory)) {
    $EvidenceDirectory = Join-Path $Root 'build/runtime-smoke/kafka-reliability-live'
} elseif (-not [IO.Path]::IsPathRooted($EvidenceDirectory)) {
    $EvidenceDirectory = Join-Path $Root $EvidenceDirectory
}
New-Item -ItemType Directory -Path $EvidenceDirectory -Force | Out-Null

if ([string]::IsNullOrWhiteSpace($SourceIdentity)) {
    $stateTool = Join-Path $Root 'cpf-tools/verification/tools/cpf-source-state.py'
    $stateJson = @(& python $stateTool --root $Root --scope source 2>&1)
    if ($LASTEXITCODE -ne 0) { throw "Cannot compute Git-independent source identity: $($stateJson -join [Environment]::NewLine)" }
    $SourceIdentity = [string](($stateJson | Select-Object -Last 1) | ConvertFrom-Json).contentSha1
}
$SourceIdentity = $SourceIdentity.Trim().ToLowerInvariant()
if ($SourceIdentity -notmatch '^[0-9a-f]{40}$') { throw 'SourceIdentity must be a 40-hex Git-independent content identity.' }

$container = 'cpf-kafka'
$topic = ('cpf-local-validation-' + ([Guid]::NewGuid().ToString('N')).Substring(0, 12)).ToLowerInvariant()
$payload1 = "before-restart-$topic"
$payload2 = "after-restart-$topic"
$startedAt = [DateTimeOffset]::UtcNow
$resultPath = Join-Path $EvidenceDirectory 'kafka-reliability-live.json'
$logPath = Join-Path $EvidenceDirectory 'kafka-reliability-live.log'
$events = [Collections.Generic.List[object]]::new()

function Write-RunLog([string]$Text) {
    $line = "{0} {1}" -f ([DateTimeOffset]::UtcNow.ToString('o')), $Text
    Add-Content -LiteralPath $logPath -Value $line -Encoding UTF8
    Write-Host $line
}

function Invoke-Docker([string[]]$Arguments) {
    Write-RunLog ("docker " + ($Arguments -join ' '))
    $output = @(& docker @Arguments 2>&1)
    $rc = $LASTEXITCODE
    foreach ($line in $output) { Write-RunLog ([string]$line) }
    if ($rc -ne 0) { throw "docker command failed rc=$rc args=$($Arguments -join ' ')" }
    return @($output)
}

function Invoke-DockerInput([string[]]$Arguments, [string]$InputText) {
    Write-RunLog ("docker " + ($Arguments -join ' ') + ' <stdin>')
    $psi = [Diagnostics.ProcessStartInfo]::new()
    $psi.FileName = 'docker'
    $psi.UseShellExecute = $false
    $psi.RedirectStandardInput = $true
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.StandardOutputEncoding = [Text.Encoding]::UTF8
    $psi.StandardErrorEncoding = [Text.Encoding]::UTF8
    foreach ($arg in $Arguments) { [void]$psi.ArgumentList.Add($arg) }
    $process = [Diagnostics.Process]::Start($psi)
    $process.StandardInput.WriteLine($InputText)
    $process.StandardInput.Close()
    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit()
    if ($stdout) { Write-RunLog $stdout.TrimEnd() }
    if ($stderr) { Write-RunLog $stderr.TrimEnd() }
    if ($process.ExitCode -ne 0) { throw "docker stdin command failed rc=$($process.ExitCode) args=$($Arguments -join ' ')" }
    return $stdout
}

function Wait-KafkaReady([int]$TimeoutSeconds = 180) {
    $deadline = [DateTimeOffset]::UtcNow.AddSeconds($TimeoutSeconds)
    do {
        $state = @(& docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' $container 2>$null)
        if ($LASTEXITCODE -eq 0 -and (($state -join '').Trim() -in @('healthy','running'))) { return }
        Start-Sleep -Seconds 2
    } while ([DateTimeOffset]::UtcNow -lt $deadline)
    throw 'Kafka container did not become ready.'
}

function Consume-All() {
    $lines = Invoke-Docker @('exec',$container,'/opt/kafka/bin/kafka-console-consumer.sh','--bootstrap-server','localhost:19092','--topic',$topic,'--from-beginning','--timeout-ms','10000')
    return @($lines | ForEach-Object { [string]$_ } | Where-Object { -not [string]::IsNullOrWhiteSpace($_) })
}

Set-Content -LiteralPath $logPath -Value '' -Encoding utf8NoBOM
$failure = $null
try {
    $running = (@(& docker inspect --format '{{.State.Running}}' $container 2>$null) -join '').Trim()
    if ($LASTEXITCODE -ne 0 -or $running -ne 'true') { throw 'cpf-kafka container is not running.' }
    Wait-KafkaReady
    [void](Invoke-Docker @('exec',$container,'/opt/kafka/bin/kafka-topics.sh','--bootstrap-server','localhost:19092','--create','--if-not-exists','--topic',$topic,'--partitions','1','--replication-factor','1'))
    [void](Invoke-DockerInput @('exec','-i',$container,'/opt/kafka/bin/kafka-console-producer.sh','--bootstrap-server','localhost:19092','--topic',$topic) $payload1)
    $initial = Consume-All
    if ($initial -notcontains $payload1) { throw 'Kafka baseline produce/consume failed.' }
    $events.Add([ordered]@{step='baseline-produce-consume';status='PASS'}) | Out-Null

    $restartPerformed = $false
    if ($RestartBrokerIfOwned) {
        [void](Invoke-Docker @('restart',$container))
        Wait-KafkaReady
        $restartPerformed = $true
        $afterRestart = Consume-All
        if ($afterRestart -notcontains $payload1) { throw 'Kafka persisted message was not readable after broker restart.' }
        $events.Add([ordered]@{step='restart-persistence';status='PASS'}) | Out-Null
    }

    [void](Invoke-DockerInput @('exec','-i',$container,'/opt/kafka/bin/kafka-console-producer.sh','--bootstrap-server','localhost:19092','--topic',$topic) $payload2)
    $final = Consume-All
    if ($final -notcontains $payload1 -or $final -notcontains $payload2) { throw 'Kafka post-restart produce/consume failed.' }
    $events.Add([ordered]@{step='post-restart-produce-consume';status='PASS'}) | Out-Null

    $result = [ordered]@{
        schemaVersion = 1
        requirementId = 'CPF-RUNTIME-MESSAGING-KAFKA-LIVE'
        status = 'PASS'
        sourceIdentity = $SourceIdentity
        identityPolicy = 'GIT_INDEPENDENT_CONTENT_SHA1'
        startedAt = $startedAt.ToString('o')
        finishedAt = [DateTimeOffset]::UtcNow.ToString('o')
        container = $container
        topic = $topic
        restartPerformed = $restartPerformed
        events = @($events)
        sanitized = $true
    }
    [IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 10) + "`n", $Utf8NoBom)
    Write-Host "CPF_KAFKA_RELIABILITY_LIVE=PASS evidence=$resultPath"
} catch {
    $failure = $_.Exception.Message
    $result = [ordered]@{
        schemaVersion = 1
        requirementId = 'CPF-RUNTIME-MESSAGING-KAFKA-LIVE'
        status = 'FAIL'
        sourceIdentity = $SourceIdentity
        identityPolicy = 'GIT_INDEPENDENT_CONTENT_SHA1'
        startedAt = $startedAt.ToString('o')
        finishedAt = [DateTimeOffset]::UtcNow.ToString('o')
        container = $container
        topic = $topic
        error = $failure
        events = @($events)
        sanitized = $true
    }
    [IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 10) + "`n", $Utf8NoBom)
    throw
} finally {
    try { [void](Invoke-Docker @('exec',$container,'/opt/kafka/bin/kafka-topics.sh','--bootstrap-server','localhost:19092','--delete','--topic',$topic)) } catch { Write-RunLog "topic cleanup warning: $($_.Exception.Message)" }
}
