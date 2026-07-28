param(
    [string] $RepoRoot = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [ValidateSet('minimal','standard','full','integration')]
    [string] $Mode = 'standard',
    [int] $WebPort = 8080,
    [int] $BatchControlPort = 8090,
    [int] $BatchSchedulerPort = 8091,
    [int] $BatchWorkerPort = 8092,
    [int] $CenterCutPort = 8093,
    [int] $HostAgentPort = 8094,
    [string] $WebXms = '256m',
    [string] $WebXmx = '768m',
    [string] $BatchXms = '256m',
    [string] $BatchXmx = '1024m',
    [switch] $SkipBuild,
    [switch] $WebOnly,
    [switch] $BatchOnly,
    [switch] $EnableBizAdmin,
    [switch] $EnableCenterCut,
    [switch] $EnableHostAgent
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

if ($WebOnly -and $BatchOnly) {
    throw '-WebOnly와 -BatchOnly는 동시에 사용할 수 없습니다.'
}
$RepoRoot = (Resolve-Path -LiteralPath $RepoRoot).Path
$gradle = if ($IsWindows) { Join-Path $RepoRoot 'gradlew.bat' } else { Join-Path $RepoRoot 'gradlew' }
$runtimeRoot = Join-Path $RepoRoot 'build\cpf-local-runtime'
$logRoot = Join-Path $runtimeRoot 'logs'
$registryPath = Join-Path $runtimeRoot 'process-registry.json'
New-Item -ItemType Directory -Path $logRoot -Force | Out-Null

if (Test-Path -LiteralPath $registryPath) {
    throw "기존 local runtime registry가 남아 있습니다. 먼저 stop-cpf-local.ps1을 실행하세요: $registryPath"
}

$ports = @($WebPort,$BatchControlPort,$BatchSchedulerPort,$BatchWorkerPort)
if ($EnableCenterCut) { $ports += $CenterCutPort }
if ($EnableHostAgent) { $ports += $HostAgentPort }
if (($ports | Group-Object | Where-Object Count -gt 1).Count -gt 0) {
    throw 'Local Runtime Port가 중복되었습니다.'
}
foreach ($port in $ports) {
    if ($port -lt 1 -or $port -gt 65535) { throw "유효하지 않은 Port입니다: $port" }
}

if (-not $SkipBuild) {
    $tasks = @()
    if (-not $BatchOnly) { $tasks += ':cpf-local-runtime:bootJar' }
    if (-not $WebOnly) { $tasks += ':cpf-local-batch-runtime:bootJar' }
    & $gradle @tasks --no-daemon
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
    [string] $Xms,
    [string] $Xmx,
    [string[]] $AdditionalArguments
) {
    $stdout = Join-Path $logRoot "$Role.out.log"
    $stderr = Join-Path $logRoot "$Role.err.log"
    $arguments = @(
        "-Xms$Xms", "-Xmx$Xmx",
        '-Dfile.encoding=UTF-8',
        '-jar', $Jar,
        "--spring.profiles.active=local,local-$Mode",
        "--server.port=$Port",
        '--server.address=127.0.0.1',
        '--cpf.environment=local'
    ) + $AdditionalArguments
    $process = Start-Process -FilePath 'java' -ArgumentList $arguments -PassThru `
        -RedirectStandardOutput $stdout -RedirectStandardError $stderr
    return [ordered]@{
        role = $Role
        pid = $process.Id
        port = $Port
        mode = $Mode
        jar = $Jar
        startedAt = (Get-Date).ToUniversalTime().ToString('o')
        stdout = $stdout
        stderr = $stderr
    }
}

$registry = @()
try {
    if (-not $BatchOnly) {
        $webJar = Resolve-BootJar (Join-Path $RepoRoot 'cpf-local-runtime') 'local-web'
        $enableBza = $EnableBizAdmin -or $Mode -in @('full','integration')
        $registry += Start-CpfProcess 'LOCAL_WEB' $webJar $WebPort $WebXms $WebXmx @(
            '--cpf.local.runtime.enabled=true',
            "--cpf.local.modules.biz-admin=$($enableBza.ToString().ToLowerInvariant())"
        )
    }
    if (-not $WebOnly) {
        $batchJar = Resolve-BootJar (Join-Path $RepoRoot 'cpf-local-batch-runtime') 'local-batch'
        $registry += Start-CpfProcess 'LOCAL_BATCH' $batchJar $BatchControlPort $BatchXms $BatchXmx @(
            '--cpf.local.batch.enabled=true',
            "--cpf.local.batch.modules.center-cut=$($EnableCenterCut.IsPresent.ToString().ToLowerInvariant())",
            "--cpf.local.batch.modules.host-agent=$($EnableHostAgent.IsPresent.ToString().ToLowerInvariant())",
            "--cpf.local.batch.ports.control-server=$BatchControlPort",
            "--cpf.local.batch.ports.scheduler=$BatchSchedulerPort",
            "--cpf.local.batch.ports.worker=$BatchWorkerPort",
            "--cpf.local.batch.ports.center-cut=$CenterCutPort",
            "--cpf.local.batch.ports.host-agent=$HostAgentPort"
        )
    }

    Start-Sleep -Seconds 4
    foreach ($entry in $registry) {
        $process = Get-Process -Id $entry.pid -ErrorAction SilentlyContinue
        if ($null -eq $process) {
            throw "$($entry.role) process가 시작 직후 종료됐습니다. stderr=$($entry.stderr)"
        }
    }
    $registry | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath $registryPath -Encoding UTF8
    Write-Host "CPF local runtime started. registry=$registryPath"
    $registry | Format-Table role,pid,port,mode,jar
} catch {
    foreach ($entry in $registry) {
        Stop-Process -Id $entry.pid -Force -ErrorAction SilentlyContinue
    }
    throw
}
