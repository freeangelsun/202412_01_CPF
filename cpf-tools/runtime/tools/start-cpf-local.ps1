param(
    [string] $RepoRoot = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [ValidateSet('integrated','minimal','standard','full','integration')]
    [string] $Mode = 'integrated',
    [ValidateSet('local','dev','test','stg','prod')]
    [string] $ResourceProfile = 'local',
    [int] $WebPort = 8080,
    [int] $BatchControlPlanePort = 8090,
    [int] $BatchSchedulerPort = 8091,
    [int] $BatchWorkerPort = 8092,
    [int] $CenterCutPort = 8093,
    [int] $AgentPort = 8094,
    [string] $WebXms,
    [string] $WebXmx,
    [string] $BatchXms,
    [string] $BatchXmx,
    [int] $HealthTimeoutSeconds = 60,
    [switch] $SkipBuild,
    [switch] $WebOnly,
    [switch] $BatchOnly,
    [switch] $EnableBackoffice,
    [switch] $EnableCenterCut,
    [switch] $EnableAgent
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

# integrated는 개발자 기본값입니다. Generated Domain까지 한 JVM/한 HTTP Port에 조립합니다.
if ($Mode -eq 'integrated') { $WebOnly = $true }

if ($WebOnly -and $BatchOnly) {
    throw '-WebOnly와 -BatchOnly는 동시에 사용할 수 없습니다.'
}
if ($HealthTimeoutSeconds -lt 5 -or $HealthTimeoutSeconds -gt 600) {
    throw 'HealthTimeoutSeconds는 5~600초 범위여야 합니다.'
}

$RepoRoot = (Resolve-Path -LiteralPath $RepoRoot).Path
. (Join-Path $PSScriptRoot 'cpf-resource-policy.ps1')

$webExplicit = @{}
$batchExplicit = @{}
if ($PSBoundParameters.ContainsKey('WebXms')) { $webExplicit['runtime.web.xms'] = $WebXms }
if ($PSBoundParameters.ContainsKey('WebXmx')) { $webExplicit['runtime.web.xmx'] = $WebXmx }
if ($PSBoundParameters.ContainsKey('BatchXms')) { $batchExplicit['runtime.batch.xms'] = $BatchXms }
if ($PSBoundParameters.ContainsKey('BatchXmx')) { $batchExplicit['runtime.batch.xmx'] = $BatchXmx }
if ($PSBoundParameters.ContainsKey('WebPort')) { $webExplicit['runtime.web.port'] = [string]$WebPort }

$webPolicy = Resolve-CpfResourcePolicy -RepoRoot $RepoRoot -Profile $ResourceProfile `
    -ModuleDir (Join-Path $RepoRoot 'cpf-tools\runtime\cpf-local-runtime') -Explicit $webExplicit
$batchPolicy = Resolve-CpfResourcePolicy -RepoRoot $RepoRoot -Profile $ResourceProfile `
    -ModuleDir (Join-Path $RepoRoot 'cpf-tools\runtime\cpf-local-batch-runtime') -Explicit $batchExplicit

$WebXms = [string]$webPolicy.Values['runtime.web.xms']
$WebXmx = [string]$webPolicy.Values['runtime.web.xmx']
$webModuleFile = Join-Path $RepoRoot 'cpf-tools\runtime\cpf-local-runtime\cpf-resource.properties'
$webModuleHasXmx = $false
if (Test-Path -LiteralPath $webModuleFile -PathType Leaf) {
    $webModuleHasXmx = (Import-CpfPropertyFile $webModuleFile).ContainsKey('runtime.web.xmx') -or
                       (Import-CpfPropertyFile $webModuleFile).ContainsKey('runtime.xmx')
}
if (-not $webModuleHasXmx -and -not $PSBoundParameters.ContainsKey('WebXmx')) {
    $modeKey = "runtime.web.mode.$Mode.xmx"
    if ($webPolicy.Values.ContainsKey($modeKey)) { $WebXmx = [string]$webPolicy.Values[$modeKey] }
}
$BatchXms = [string]$batchPolicy.Values['runtime.batch.xms']
$BatchXmx = [string]$batchPolicy.Values['runtime.batch.xmx']
if (-not $PSBoundParameters.ContainsKey('WebPort')) { $WebPort = 8080 }
$BindAddress = '127.0.0.1'
$MaxMetaspace = [string]$webPolicy.Values['runtime.jvm.maxMetaspace']
$GradleMaxWorkers = [int]$webPolicy.Values['gradle.maxWorkers']
$GradleXms = [string]$webPolicy.Values['gradle.jvm.xms']
$GradleXmx = [string]$webPolicy.Values['gradle.jvm.xmx']
$MaxDirectMemory = [string]$webPolicy.Values['runtime.jvm.maxDirectMemory']
$ReservedCodeCache = [string]$webPolicy.Values['runtime.jvm.reservedCodeCache']
$ThreadStack = [string]$webPolicy.Values['runtime.jvm.threadStack']

Write-Host "CPF resource profile=$ResourceProfile web=$WebXms/$WebXmx batch=$BatchXms/$BatchXmx policy=$($webPolicy.PolicyRoot)"
$gradle = if ($IsWindows) { Join-Path $RepoRoot 'gradlew.bat' } else { Join-Path $RepoRoot 'gradlew' }
$runtimeRoot = Join-Path $RepoRoot 'build\cpf-local-runtime'
$logRoot = Join-Path $runtimeRoot 'logs'
$registryPath = Join-Path $runtimeRoot 'process-registry.json'
New-Item -ItemType Directory -Path $logRoot -Force | Out-Null

if (Test-Path -LiteralPath $registryPath) {
    throw "기존 local runtime registry가 남아 있습니다. 먼저 stop-cpf-local.ps1을 실행하세요: $registryPath"
}

$batchRoles = [ordered]@{
    'control-plane' = $true
    'scheduler' = $Mode -ne 'minimal'
    'worker' = $true
    'center-cut' = ($Mode -in @('full','integration')) -or $EnableCenterCut.IsPresent
    'agent' = ($Mode -in @('full','integration')) -or $EnableAgent.IsPresent
}
$batchPorts = [ordered]@{
    'control-plane' = $BatchControlPlanePort
    'scheduler' = $BatchSchedulerPort
    'worker' = $BatchWorkerPort
    'center-cut' = $CenterCutPort
    'agent' = $AgentPort
}

$ports = @()
if (-not $BatchOnly) { $ports += $WebPort }
if (-not $WebOnly) {
    foreach ($role in $batchRoles.Keys) {
        if ($batchRoles[$role]) { $ports += $batchPorts[$role] }
    }
}
if (@($ports | Group-Object | Where-Object Count -gt 1).Count -gt 0) {
    throw '활성 Local Runtime Port가 중복되었습니다.'
}
foreach ($port in $ports) {
    if ($port -lt 1 -or $port -gt 65535) { throw "유효하지 않은 Port입니다: $port" }
    # 기동 후 bind 오류를 기다리지 않고 시작 전에 loopback Port 점유를 차단합니다.
    $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Loopback, $port)
    try { $listener.Start() } catch { throw "Local Runtime Port가 이미 사용 중입니다: $port" } finally { try { $listener.Stop() } catch {} }
}

if (-not $SkipBuild) {
    $tasks = @()
    if (-not $BatchOnly) { $tasks += ':runtime:local:bootJar' }
    if (-not $WebOnly) { $tasks += ':runtime:local-batch:bootJar' }
    $gradleArgs = @("-PcpfResourceProfile=$ResourceProfile",'-PcpfIncludeGeneratedDomains=true',"-Dorg.gradle.jvmargs=-Xms$GradleXms -Xmx$GradleXmx","--max-workers=$GradleMaxWorkers",'--no-parallel','--no-daemon') + $tasks
    & $gradle @gradleArgs
    if ($LASTEXITCODE -ne 0) {
        throw "CPF local runtime build failed. exit=$LASTEXITCODE"
    }
}

function Resolve-BootJar([string] $ProjectDir, [string] $Classifier) {
    $libDir = Join-Path $ProjectDir 'build\libs'
    $candidate = Get-ChildItem -LiteralPath $libDir -Filter "*-$Classifier.jar" -File |
        Where-Object { $_.Name -notmatch '-plain\.jar$' } |
        Sort-Object LastWriteTimeUtc -Descending |
        Select-Object -First 1
    if ($null -eq $candidate) {
        throw "BootJar를 찾을 수 없습니다. project=$ProjectDir classifier=$Classifier"
    }
    return $candidate.FullName
}

function Start-CpfProcess(
    [string] $Role,
    [string] $Jar,
    [int] $Port,
    [string] $Profiles,
    [string] $Xms,
    [string] $Xmx,
    [string[]] $HealthUrls,
    [string[]] $AdditionalArguments
) {
    $stdout = Join-Path $logRoot "$Role.out.log"
    $stderr = Join-Path $logRoot "$Role.err.log"
    $arguments = @(
        "-Xms$Xms", "-Xmx$Xmx",
        "-XX:MaxMetaspaceSize=$MaxMetaspace",
        "-XX:MaxDirectMemorySize=$MaxDirectMemory",
        "-XX:ReservedCodeCacheSize=$ReservedCodeCache",
        "-Xss$ThreadStack",
        '-Dfile.encoding=UTF-8',
        '-jar', $Jar,
        "--spring.profiles.active=$Profiles",
        "--server.port=$Port",
        "--server.address=$BindAddress",
        '--cpf.environment=local'
    ) + $AdditionalArguments
    $process = Start-Process -FilePath 'java' -ArgumentList $arguments -PassThru `
        -RedirectStandardOutput $stdout -RedirectStandardError $stderr
    return [ordered]@{
        role = $Role
        pid = $process.Id
        port = $Port
        mode = $Mode
        profiles = $Profiles
        jar = $Jar
        startedAt = (Get-Date).ToUniversalTime().ToString('o')
        healthUrls = @($HealthUrls)
        stdout = $stdout
        stderr = $stderr
    }
}

function Wait-CpfHealth([object[]] $Entries) {
    $deadline = (Get-Date).AddSeconds($HealthTimeoutSeconds)
    $pending = [System.Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
    foreach ($entry in $Entries) {
        foreach ($url in @($entry.healthUrls)) { [void]$pending.Add([string]$url) }
    }
    while ($pending.Count -gt 0 -and (Get-Date) -lt $deadline) {
        foreach ($url in @($pending)) {
            try {
                $response = Invoke-RestMethod -Uri $url -Method Get -TimeoutSec 3
                if ($null -ne $response -and [string]$response.status -eq 'UP') {
                    [void]$pending.Remove($url)
                    Write-Host "[UP] $url"
                }
            } catch {
                # 시작 중일 수 있으므로 deadline까지 재시도합니다.
            }
        }
        if ($pending.Count -gt 0) { Start-Sleep -Seconds 2 }
    }
    if ($pending.Count -gt 0) {
        throw "Local Runtime health timeout. unavailable=$(@($pending) -join ', ')"
    }
}

$registry = @()
try {
    if (-not $BatchOnly) {
        $webJar = Resolve-BootJar (Join-Path $RepoRoot 'cpf-tools\runtime\cpf-local-runtime') 'local-web'
        $enableBackoffice = $EnableBackoffice -or $Mode -in @('integrated','full','integration')
        $registry += Start-CpfProcess 'LOCAL_WEB' $webJar $WebPort "local,local-$Mode" $WebXms $WebXmx `
            @("http://127.0.0.1:$WebPort/actuator/health") @(
                '--cpf.local.runtime.enabled=true',
                '--cpf.local.modules.domains.enabled=true',
                '--cpf.local.modules.domains.auto-discover=true',
                "--cpf.local.modules.gateway=$((($Mode -in @('full','integration'))).ToString().ToLowerInvariant())",
                "--cpf.local.modules.backoffice=$($enableBackoffice.ToString().ToLowerInvariant())"
            )
    }
    if (-not $WebOnly) {
        $batchJar = Resolve-BootJar (Join-Path $RepoRoot 'cpf-tools\runtime\cpf-local-batch-runtime') 'local-batch'
        $healthUrls = @()
        $roleArguments = @('--cpf.local.batch.enabled=true')
        foreach ($role in $batchRoles.Keys) {
            $enabled = [bool]$batchRoles[$role]
            $roleArguments += "--cpf.local.batch.modules.$role=$($enabled.ToString().ToLowerInvariant())"
            $roleArguments += "--cpf.local.batch.ports.$role=$($batchPorts[$role])"
            if ($enabled) { $healthUrls += "http://127.0.0.1:$($batchPorts[$role])/actuator/health" }
        }
        $registry += Start-CpfProcess 'LOCAL_BATCH' $batchJar $BatchControlPlanePort "local,local-batch-$Mode" `
            $BatchXms $BatchXmx $healthUrls $roleArguments
    }

    foreach ($entry in $registry) {
        $process = Get-Process -Id $entry.pid -ErrorAction SilentlyContinue
        if ($null -eq $process) {
            throw "$($entry.role) process가 시작 직후 종료됐습니다. stderr=$($entry.stderr)"
        }
    }
    Wait-CpfHealth $registry
    $registry | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath $registryPath -Encoding UTF8
    Write-Host "CPF local runtime started. registry=$registryPath"
    $registry | Format-Table role,pid,port,mode,profiles,jar
} catch {
    foreach ($entry in $registry) {
        Stop-Process -Id $entry.pid -Force -ErrorAction SilentlyContinue
    }
    throw
}
