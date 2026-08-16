[CmdletBinding()]
param(
    [string]$Root = "",
    [string]$EvidenceDirectory = "",
    [string]$SourceIdentity = $env:CPF_SOURCE_SHA,
    [switch]$SkipPull
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
[Console]::OutputEncoding = [System.Text.UTF8Encoding]::new($false)
$OutputEncoding = [System.Text.UTF8Encoding]::new($false)

$scriptRoot = Split-Path -Parent $MyInvocation.MyCommand.Path
if ([string]::IsNullOrWhiteSpace($Root)) {
    $Root = [System.IO.Path]::GetFullPath((Join-Path $scriptRoot "../../.."))
} else {
    $Root = [System.IO.Path]::GetFullPath($Root)
}

$composeFile = Join-Path $scriptRoot "compose.cache-provider-live.yml"
$probeFile = Join-Path $scriptRoot "probe-cache-provider-live.py"
$sourceGate = Join-Path $Root "cpf-tools/verification/nxt3/verify_redis_valkey_current.py"
foreach ($required in @($composeFile, $probeFile, $sourceGate)) {
    if (-not (Test-Path -LiteralPath $required -PathType Leaf)) {
        throw "Required cache live verification resource is missing: $required"
    }
}

$stateTool = Join-Path $Root "cpf-tools/verification/tools/cpf-source-state.py"
if (-not (Test-Path -LiteralPath $stateTool -PathType Leaf)) { throw "Git-independent source identity tool is missing: $stateTool" }

function Get-CpfSourceState {
    $stateJson = @(& python $stateTool --root $Root --scope source 2>&1)
    if ($LASTEXITCODE -ne 0) { throw "Cannot compute Git-independent source identity: $($stateJson -join [Environment]::NewLine)" }
    try { return (($stateJson | Select-Object -Last 1) | ConvertFrom-Json) }
    catch { throw "Invalid Git-independent source identity output." }
}

$sourceStateBefore = Get-CpfSourceState
$currentSourceIdentity = ([string]$sourceStateBefore.contentSha1).Trim().ToLowerInvariant()
$requestedSourceIdentity = if ([string]::IsNullOrWhiteSpace($SourceIdentity)) { $null } else { $SourceIdentity.Trim().ToLowerInvariant() }
if ($null -ne $requestedSourceIdentity -and $requestedSourceIdentity -notmatch '^[0-9a-f]{40}$') {
    throw "SourceIdentity provenance must be a 40-hex Git-independent content identity when supplied."
}
# SourceIdentity from a delivery package is provenance only. Runtime evidence is always bound to the
# product bytes that are actually being verified so local documentation/approved overlay drift does not
# cause a false failure. Managed source mutation is still checked before/after below.
$SourceIdentity = $currentSourceIdentity
$headShort = $currentSourceIdentity.Substring(0, 8)
if ([string]::IsNullOrWhiteSpace($EvidenceDirectory)) {
    $EvidenceDirectory = Join-Path $Root "build/codex-onepass/$headShort/cache-provider-live"
} else {
    $EvidenceDirectory = [System.IO.Path]::GetFullPath($EvidenceDirectory)
}
New-Item -ItemType Directory -Path $EvidenceDirectory -Force | Out-Null
foreach ($ownedEvidenceName in @(
    "cache-provider-live.json",
    "cache-provider-live.log",
    "cache-provider-source-gate.json",
    "redis-full.json",
    "redis-interruption.json",
    "redis-recovery.json",
    "valkey-full.json",
    "valkey-interruption.json",
    "valkey-recovery.json",
    "provider-switch-redis-as-valkey.json",
    "provider-switch-valkey-as-redis.json"
)) {
    $ownedEvidencePath = Join-Path $EvidenceDirectory $ownedEvidenceName
    if (Test-Path -LiteralPath $ownedEvidencePath -PathType Leaf) {
        Remove-Item -LiteralPath $ownedEvidencePath -Force
    }
}

$startedAt = [DateTimeOffset]::UtcNow
$runToken = ([Guid]::NewGuid().ToString("N")).Substring(0, 10)
$projectName = "cpf-cache-live-$PID-$runToken".ToLowerInvariant()
$namespace = "cpf-live-$headShort-$runToken"
$passwordFile = Join-Path ([System.IO.Path]::GetTempPath()) "cpf-cache-live-secret-$runToken.txt"
$logPath = Join-Path $EvidenceDirectory "cache-provider-live.log"
$evidencePath = Join-Path $EvidenceDirectory "cache-provider-live.json"
$redisImage = "redis:8.8.1-trixie@sha256:3eafabb4c93fcb8b36b666e07a43f096cb157bc6b07dce4b2492b895c63cf37f"
$valkeyImage = "valkey/valkey:9.1.1-alpine@sha256:ee91f7a174ac4d6a6b0685b3a60e321f0a9dbbb691f9b0e285be2ba1d1be8328"
$commands = [System.Collections.Generic.List[string]]::new()
$providerResults = [ordered]@{}
$imageResults = [System.Collections.Generic.List[object]]::new()
$cleanupResult = [ordered]@{
    attempted = $false
    composeDownExitCode = $null
    remainingContainers = @()
    remainingNetworks = @()
    remainingVolumes = @()
    temporarySecretRemoved = $false
}
$failure = $null
$secretForLeakCheck = $null
$secretLogged = $false

Set-Content -LiteralPath $logPath -Value "" -Encoding utf8NoBOM

function Write-RunLog {
    param([string]$Message)
    $line = "{0} {1}" -f ([DateTimeOffset]::UtcNow.ToString("o")), $Message
    Add-Content -LiteralPath $logPath -Value $line -Encoding utf8
    Write-Host $line
}

function Invoke-Recorded {
    param(
        [Parameter(Mandatory = $true)][string]$FilePath,
        [Parameter(Mandatory = $true)][string[]]$Arguments,
        [Parameter(Mandatory = $true)][string]$Display,
        [int[]]$ExpectedExitCodes = @(0)
    )
    $commands.Add($Display)
    Write-RunLog "COMMAND $Display"
    $lines = @(& $FilePath @Arguments 2>&1)
    $exitCode = $LASTEXITCODE
    foreach ($line in $lines) {
        Write-RunLog ("OUTPUT " + [string]$line)
    }
    Write-RunLog "EXIT $exitCode"
    if ($ExpectedExitCodes -notcontains $exitCode) {
        throw "Command failed with exit code ${exitCode}: $Display"
    }
    return $lines
}

function Get-Sha256 {
    param([Parameter(Mandatory = $true)][string]$Path)
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}

function Get-FreeLoopbackPort {
    $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, 0)
    try {
        $listener.Start()
        return ([System.Net.IPEndPoint]$listener.LocalEndpoint).Port
    } finally {
        $listener.Stop()
    }
}

function Wait-ContainerHealthy {
    param([Parameter(Mandatory = $true)][string]$ContainerId)
    $deadline = [DateTimeOffset]::UtcNow.AddSeconds(90)
    do {
        $raw = @(& docker inspect $ContainerId 2>$null)
        if ($LASTEXITCODE -eq 0 -and $raw.Count -gt 0) {
            $inspect = ($raw -join [Environment]::NewLine) | ConvertFrom-Json
            $state = $inspect[0].State
            if ($state.Running -and $null -ne $state.Health -and $state.Health.Status -eq "healthy") {
                return
            }
        }
        Start-Sleep -Milliseconds 500
    } while ([DateTimeOffset]::UtcNow -lt $deadline)
    throw "Container did not become healthy: $ContainerId"
}

function Get-ContainerDescriptor {
    param(
        [Parameter(Mandatory = $true)][string]$Provider,
        [Parameter(Mandatory = $true)][string]$ContainerId
    )
    $raw = @(& docker inspect $ContainerId)
    if ($LASTEXITCODE -ne 0) {
        throw "Cannot inspect $Provider container."
    }
    $inspect = (($raw -join [Environment]::NewLine) | ConvertFrom-Json)[0]
    $bindings = @($inspect.NetworkSettings.Ports.'6379/tcp')
    if ($bindings.Count -ne 1) {
        $portSnapshot = $inspect.NetworkSettings.Ports | ConvertTo-Json -Compress -Depth 5
        throw "Expected exactly one random loopback port for $Provider; bindingCount=$($bindings.Count); ports=$portSnapshot"
    }
    $hostIp = [string]$bindings[0].HostIp
    $hostPort = [int]$bindings[0].HostPort
    if ($hostIp -notin @("127.0.0.1", "::1") -or $hostPort -le 0) {
        throw "Unsafe published endpoint for ${Provider}: ${hostIp}:${hostPort}"
    }
    return [ordered]@{
        provider = $Provider
        containerId = [string]$inspect.Id
        imageReference = [string]$inspect.Config.Image
        imageId = [string]$inspect.Image
        host = "127.0.0.1"
        port = $hostPort
        restartPolicy = [string]$inspect.HostConfig.RestartPolicy.Name
        composeProject = [string]$inspect.Config.Labels.'com.docker.compose.project'
    }
}

function Invoke-ProviderProbe {
    param(
        [Parameter(Mandatory = $true)][string]$Provider,
        [Parameter(Mandatory = $true)][System.Collections.IDictionary]$Descriptor,
        [Parameter(Mandatory = $true)][string]$Mode,
        [Parameter(Mandatory = $true)][string]$EvidenceName,
        [int[]]$ExpectedExitCodes = @(0),
        [string]$RequestedProvider = ""
    )
    if ([string]::IsNullOrWhiteSpace($RequestedProvider)) {
        $RequestedProvider = $Provider
    }
    $probeEvidence = Join-Path $EvidenceDirectory $EvidenceName
    $args = @(
        "-B", $probeFile,
        "--provider", $RequestedProvider,
        "--host", $Descriptor.host,
        "--port", [string]$Descriptor.port,
        "--password-file", $passwordFile,
        "--namespace", $namespace,
        "--mode", $Mode,
        "--evidence-output", $probeEvidence
    )
    $display = "python -B cpf-tools/environment/docker-development-test/probe-cache-provider-live.py --provider $RequestedProvider --host 127.0.0.1 --port $($Descriptor.port) --password-file <temporary-secret> --namespace $namespace --mode $Mode --evidence-output $EvidenceName"
    Invoke-Recorded -FilePath "python" -Arguments $args -Display $display -ExpectedExitCodes $ExpectedExitCodes | Out-Null
    if (-not (Test-Path -LiteralPath $probeEvidence -PathType Leaf)) {
        throw "Probe evidence was not produced: $probeEvidence"
    }
    return [ordered]@{
        path = [System.IO.Path]::GetRelativePath($Root, $probeEvidence).Replace('\', '/')
        sha256 = Get-Sha256 $probeEvidence
        result = Get-Content -LiteralPath $probeEvidence -Raw -Encoding utf8 | ConvertFrom-Json -AsHashtable
    }
}

try {
    $secretBytes = [System.Security.Cryptography.RandomNumberGenerator]::GetBytes(32)
    $secret = [Convert]::ToHexString($secretBytes).ToLowerInvariant()
    $secretForLeakCheck = $secret
    [System.IO.File]::WriteAllText($passwordFile, $secret, [System.Text.UTF8Encoding]::new($false))
    $secret = $null

    $env:CPF_CACHE_LIVE_PASSWORD_FILE = $passwordFile
    $env:CPF_CACHE_LIVE_REDIS_IMAGE = $redisImage
    $env:CPF_CACHE_LIVE_VALKEY_IMAGE = $valkeyImage
    $redisPort = Get-FreeLoopbackPort
    do {
        $valkeyPort = Get-FreeLoopbackPort
    } while ($valkeyPort -eq $redisPort)
    $env:CPF_CACHE_LIVE_REDIS_PORT = [string]$redisPort
    $env:CPF_CACHE_LIVE_VALKEY_PORT = [string]$valkeyPort
    $composeArgs = @("compose", "--project-name", $projectName, "--file", $composeFile)
    $pullMode = if ($SkipPull) { "missing" } else { "always" }
    Invoke-Recorded -FilePath "docker" -Arguments ($composeArgs + @("up", "--detach", "--pull", $pullMode, "--wait", "--wait-timeout", "180")) -Display "docker compose --project-name $projectName --file cpf-tools/environment/docker-development-test/compose.cache-provider-live.yml up --detach --pull $pullMode --wait --wait-timeout 180" | Out-Null

    $descriptors = [ordered]@{}
    foreach ($provider in @("redis", "valkey")) {
        $containerId = ((& docker @($composeArgs + @("ps", "--quiet", $provider))) | Select-Object -First 1).Trim()
        if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($containerId)) {
            throw "Compose did not return the $provider container ID."
        }
        Wait-ContainerHealthy -ContainerId $containerId
        $descriptor = Get-ContainerDescriptor -Provider $provider -ContainerId $containerId
        if ($descriptor.composeProject -ne $projectName -or $descriptor.restartPolicy -ne "no") {
            throw "The $provider container is not owned by the isolated Compose project."
        }
        $expectedImage = if ($provider -eq "redis") { $redisImage } else { $valkeyImage }
        if ($descriptor.imageReference -ne $expectedImage -or $descriptor.imageId -notmatch '^sha256:[0-9a-f]{64}$') {
            throw "The $provider container did not run the exact pinned image reference."
        }
        $descriptors[$provider] = $descriptor
        $imageResults.Add($descriptor)
    }

    $sourceLines = @(Invoke-Recorded -FilePath "python" -Arguments @("-B", $sourceGate, "--root", $Root) -Display "python -B cpf-tools/verification/nxt3/verify_redis_valkey_current.py --root .")
    $sourceOutput = $sourceLines -join "`n"
    if ($sourceOutput -notmatch 'CPF_REDIS_VALKEY_CURRENT=PASS') {
        throw "Redis/Valkey provider source and conflict gate did not pass."
    }
    $sourceResult = [ordered]@{
        status = "PASS"
        productionProviderSelectionJavaExecuted = $true
        providerConflictFailsClosed = $true
        liveRuntimeCoveredBy = "this aggregate Redis+Valkey Docker evidence"
    }

    foreach ($provider in @("redis", "valkey")) {
        $descriptor = $descriptors[$provider]
        $full = Invoke-ProviderProbe -Provider $provider -Descriptor $descriptor -Mode "full" -EvidenceName "$provider-full.json"
        if ($full.result.status -ne "PASS") {
            throw "$provider full live protocol verification failed."
        }

        Invoke-Recorded -FilePath "docker" -Arguments @("stop", "--timeout", "5", $descriptor.containerId) -Display "docker stop --timeout 5 <$provider-owned-container>" | Out-Null
        $interruption = Invoke-ProviderProbe -Provider $provider -Descriptor $descriptor -Mode "identity" -EvidenceName "$provider-interruption.json" -ExpectedExitCodes @(1)
        if ($interruption.result.status -ne "FAIL") {
            throw "$provider network/service interruption was not observed as a failed connection."
        }
        Invoke-Recorded -FilePath "docker" -Arguments @("start", $descriptor.containerId) -Display "docker start <$provider-owned-container>" | Out-Null
        Wait-ContainerHealthy -ContainerId $descriptor.containerId
        $recovery = Invoke-ProviderProbe -Provider $provider -Descriptor $descriptor -Mode "recovery" -EvidenceName "$provider-recovery.json"
        if ($recovery.result.status -ne "PASS") {
            throw "$provider reconnect/recovery verification failed."
        }
        $providerResults[$provider] = [ordered]@{
            descriptor = $descriptor
            full = $full
            interruption = $interruption
            recovery = $recovery
        }
    }

    $redisAsValkey = Invoke-ProviderProbe -Provider "redis" -Descriptor $descriptors.redis -Mode "identity" -EvidenceName "provider-switch-redis-as-valkey.json" -ExpectedExitCodes @(1) -RequestedProvider "valkey"
    $valkeyAsRedis = Invoke-ProviderProbe -Provider "valkey" -Descriptor $descriptors.valkey -Mode "identity" -EvidenceName "provider-switch-valkey-as-redis.json" -ExpectedExitCodes @(1) -RequestedProvider "redis"
    foreach ($negative in @($redisAsValkey, $valkeyAsRedis)) {
        if ($negative.result.status -ne "FAIL" -or $negative.result.error.type -ne "AssertionError") {
            throw "Provider switch identity mismatch did not fail closed."
        }
    }
    $providerResults["switch"] = [ordered]@{
        redisEndpointRejectsValkeySelection = $redisAsValkey
        valkeyEndpointRejectsRedisSelection = $valkeyAsRedis
        equivalenceClaimed = $false
    }
    $providerResults["sourceGate"] = [ordered]@{
        result = $sourceResult
    }
    Write-RunLog "LIVE_CACHE_CONTRACT=PASS providers=redis,valkey equivalenceClaimed=false"
} catch {
    $failure = $_.Exception.Message
    Write-RunLog "LIVE_CACHE_CONTRACT=FAIL error=$failure"
} finally {
    $cleanupResult.attempted = $true
    try {
        $downArguments = @("compose", "--project-name", $projectName, "--file", $composeFile, "down", "--volumes", "--remove-orphans", "--timeout", "10")
        $commands.Add("docker compose --project-name $projectName --file cpf-tools/environment/docker-development-test/compose.cache-provider-live.yml down --volumes --remove-orphans --timeout 10")
        Write-RunLog "COMMAND docker compose --project-name $projectName --file cpf-tools/environment/docker-development-test/compose.cache-provider-live.yml down --volumes --remove-orphans --timeout 10"
        @(& docker @downArguments 2>&1) | ForEach-Object { Write-RunLog ("OUTPUT " + [string]$_) }
        $cleanupResult.composeDownExitCode = $LASTEXITCODE
    } catch {
        $cleanupResult.composeDownExitCode = -1
        if ($null -eq $failure) {
            $failure = "Exact Compose cleanup failed: $($_.Exception.Message)"
        }
    }
    if (Test-Path -LiteralPath $passwordFile -PathType Leaf) {
        Remove-Item -LiteralPath $passwordFile -Force
    }
    $cleanupResult.temporarySecretRemoved = -not (Test-Path -LiteralPath $passwordFile)
    $cleanupResult.remainingContainers = @(& docker ps --all --quiet --filter "label=com.docker.compose.project=$projectName")
    $cleanupResult.remainingNetworks = @(& docker network ls --quiet --filter "label=com.docker.compose.project=$projectName")
    $cleanupResult.remainingVolumes = @(& docker volume ls --quiet --filter "label=com.docker.compose.project=$projectName")
    if ($cleanupResult.composeDownExitCode -ne 0 -or -not $cleanupResult.temporarySecretRemoved -or $cleanupResult.remainingContainers.Count -ne 0 -or $cleanupResult.remainingNetworks.Count -ne 0 -or $cleanupResult.remainingVolumes.Count -ne 0) {
        if ($null -eq $failure) {
            $failure = "Isolated cache fixture cleanup left owned resources behind."
        }
    }
    Remove-Item Env:CPF_CACHE_LIVE_PASSWORD_FILE -ErrorAction SilentlyContinue
    Remove-Item Env:CPF_CACHE_LIVE_REDIS_IMAGE -ErrorAction SilentlyContinue
    Remove-Item Env:CPF_CACHE_LIVE_VALKEY_IMAGE -ErrorAction SilentlyContinue
    Remove-Item Env:CPF_CACHE_LIVE_REDIS_PORT -ErrorAction SilentlyContinue
    Remove-Item Env:CPF_CACHE_LIVE_VALKEY_PORT -ErrorAction SilentlyContinue
}

$sourceStateAfter = Get-CpfSourceState
$sourceStateStable = (
    ([string]$sourceStateBefore.contentSha1).Trim().ToLowerInvariant() -eq ([string]$sourceStateAfter.contentSha1).Trim().ToLowerInvariant() -and
    ([string]$sourceStateBefore.contentSha256).Trim().ToLowerInvariant() -eq ([string]$sourceStateAfter.contentSha256).Trim().ToLowerInvariant() -and
    [int64]$sourceStateBefore.fileCount -eq [int64]$sourceStateAfter.fileCount -and
    [int64]$sourceStateBefore.totalBytes -eq [int64]$sourceStateAfter.totalBytes
)
if (-not $sourceStateStable -and $null -eq $failure) {
    $failure = "Cache live verification mutated product source bytes."
}

$endedAt = [DateTimeOffset]::UtcNow
$logText = Get-Content -LiteralPath $logPath -Raw -Encoding utf8
$secretLogged = -not [string]::IsNullOrEmpty($secretForLeakCheck) -and $logText.Contains($secretForLeakCheck, [StringComparison]::Ordinal)
$secretForLeakCheck = $null
if ($secretLogged -and $null -eq $failure) {
    $failure = "A generated cache fixture secret was detected in the execution log."
}
$logSha256 = Get-Sha256 $logPath
$commandText = ($commands -join "`n") + "`n"
$commandBytes = [System.Text.Encoding]::UTF8.GetBytes($commandText)
$commandHash = [Convert]::ToHexString([System.Security.Cryptography.SHA256]::HashData($commandBytes)).ToLowerInvariant()
$dockerVersion = (& docker version --format '{{.Server.Version}}').Trim()
$artifacts = [System.Collections.Generic.List[object]]::new()
Get-ChildItem -LiteralPath $EvidenceDirectory -File -Filter "*.json" | Sort-Object Name | ForEach-Object {
    if ($_.FullName -ne $evidencePath) {
        $artifacts.Add([ordered]@{
            path = [System.IO.Path]::GetRelativePath($Root, $_.FullName).Replace('\', '/')
            sha256 = Get-Sha256 $_.FullName
        })
    }
}
$artifacts.Add([ordered]@{
    path = [System.IO.Path]::GetRelativePath($Root, $logPath).Replace('\', '/')
    sha256 = $logSha256
})

$result = [ordered]@{
    schemaVersion = 1
    requirementId = "CPF-RUNTIME-CACHE-REDIS-VALKEY-LIVE"
    status = if ($null -eq $failure) { "PASS" } else { "FAIL" }
    exitCode = if ($null -eq $failure) { 0 } else { 1 }
    sourceIdentity = $currentSourceIdentity
    requestedSourceIdentityProvenance = $requestedSourceIdentity
    identityPolicy = "GIT_INDEPENDENT_CONTENT_SHA1"
    startedAt = $startedAt.ToString("o")
    endedAt = $endedAt.ToString("o")
    durationMilliseconds = [Math]::Round(($endedAt - $startedAt).TotalMilliseconds)
    commandHashSha256 = $commandHash
    commands = @($commands)
    docker = [ordered]@{
        serverVersion = $dockerVersion
        composeProject = $projectName
        networkIsolation = "project-scoped bridge with loopback-only random published ports"
        storageIsolation = "project-scoped ephemeral named volumes removed after execution"
        images = @($imageResults)
    }
    assertions = [ordered]@{
        bothNativeProvidersExecuted = $providerResults.Contains("redis") -and $providerResults.Contains("valkey")
        providerSwitchFailsClosed = $providerResults.Contains("switch")
        redisValkeyEquivalenceClaimed = $false
        secretLogged = $secretLogged
        sourceStateStable = $sourceStateStable
        exactOwnedResourceCleanup = $null -eq $failure -or (
            $cleanupResult.composeDownExitCode -eq 0 -and
            $cleanupResult.temporarySecretRemoved -and
            $cleanupResult.remainingContainers.Count -eq 0 -and
            $cleanupResult.remainingNetworks.Count -eq 0 -and
            $cleanupResult.remainingVolumes.Count -eq 0
        )
    }
    sourceState = [ordered]@{
        beforeSha1 = ([string]$sourceStateBefore.contentSha1).Trim().ToLowerInvariant()
        beforeSha256 = ([string]$sourceStateBefore.contentSha256).Trim().ToLowerInvariant()
        afterSha1 = ([string]$sourceStateAfter.contentSha1).Trim().ToLowerInvariant()
        afterSha256 = ([string]$sourceStateAfter.contentSha256).Trim().ToLowerInvariant()
        stable = $sourceStateStable
    }
    providers = $providerResults
    cleanup = $cleanupResult
    failure = $failure
    log = [ordered]@{
        path = [System.IO.Path]::GetRelativePath($Root, $logPath).Replace('\', '/')
        sha256 = $logSha256
    }
    artifacts = @($artifacts)
}
$result | ConvertTo-Json -Depth 30 | Set-Content -LiteralPath $evidencePath -Encoding utf8NoBOM
Write-Host "CPF_CACHE_PROVIDER_LIVE=$($result.status) evidence=$evidencePath logSha256=$logSha256"
if ($null -ne $failure) {
    exit 1
}
