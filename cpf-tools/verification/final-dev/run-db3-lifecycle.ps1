[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[0-9a-fA-F]{40}$')]
    [string]$ExpectedHead,

    [string]$EvidenceDir,
    [string]$RunnerExecutable,
    [string[]]$RunnerPrefixArguments = @(),
    [string]$RunnerClass = 'com.cpf.tools.db.CpfDbLifecycleRunner',
    [string]$RunnerClasspath,
    [ValidateRange(1, 7200)]
    [int]$TimeoutSeconds = 900
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Get-RepositoryRoot {
    $root = (& git -C $PSScriptRoot rev-parse --show-toplevel 2>$null | Select-Object -First 1)
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($root)) {
        throw 'Repository Root를 git rev-parse --show-toplevel로 확인할 수 없습니다.'
    }
    return [IO.Path]::GetFullPath($root.Trim())
}

function Protect-Text {
    param([AllowNull()][string]$Text, [string[]]$Secrets)
    $safe = if ($null -eq $Text) { '' } else { $Text }
    foreach ($secret in $Secrets) {
        if (-not [string]::IsNullOrEmpty($secret)) {
            $variants = @(
                $secret,
                $secret.Replace('\', '\\').Replace('"', '\"').Replace("`r", '\r').Replace("`n", '\n').Replace("`t", '\t')
            ) | Sort-Object -Unique
            foreach ($variant in $variants) {
                if (-not [string]::IsNullOrEmpty($variant)) {
                    $safe = $safe.Replace($variant, '***REDACTED***')
                }
            }
        }
    }
    return $safe
}

function Assert-SafeJdbcUrl {
    param([Parameter(Mandatory=$true)][string]$Url)
    if ($Url -match '(?i)(password|passwd|pwd|secret|token|access[_-]?key)\s*=' -or
        $Url -match '(?i)jdbc:[^:]+://[^/@:]+:[^/@]+@') {
        throw 'JDBC URL에 credential 또는 secret을 포함할 수 없습니다.'
    }
}

function Invoke-LifecycleRunner {
    param(
        [Parameter(Mandatory = $true)][string]$Executable,
        [Parameter(Mandatory = $true)][string[]]$Arguments,
        [Parameter(Mandatory = $true)][string]$ConnectionJson,
        [Parameter(Mandatory = $true)][string[]]$Secrets,
        [Parameter(Mandatory = $true)][int]$TimeoutSeconds
    )

    $start = [Diagnostics.ProcessStartInfo]::new()
    $start.FileName = $Executable
    $start.UseShellExecute = $false
    $start.CreateNoWindow = $true
    $start.RedirectStandardInput = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    $start.Environment.Clear()
    foreach ($name in @('PATH','JAVA_HOME','SystemRoot','WINDIR','TEMP','TMP','LANG','LC_ALL')) {
        $value = [Environment]::GetEnvironmentVariable($name)
        if (-not [string]::IsNullOrWhiteSpace($value)) { $start.Environment[$name] = $value }
    }
    $start.Environment['CPF_DB_RUNNER_CHILD'] = 'true'
    foreach ($argument in $Arguments) { [void]$start.ArgumentList.Add($argument) }

    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $start
    $started = $false
    try {
        if (-not $process.Start()) { throw "DB Lifecycle Runner를 시작할 수 없습니다: $Executable" }
        $started = $true
        $stdoutTask = $process.StandardOutput.ReadToEndAsync()
        $stderrTask = $process.StandardError.ReadToEndAsync()
        $process.StandardInput.WriteLine($ConnectionJson)
        $process.StandardInput.Close()
        if (-not $process.WaitForExit($TimeoutSeconds * 1000)) {
            $process.Kill($true)
            [void]$process.WaitForExit(30000)
            return [pscustomobject]@{
                ExitCode = 124
                TimedOut = $true
                Stdout = Protect-Text -Text $stdoutTask.GetAwaiter().GetResult() -Secrets $Secrets
                Stderr = Protect-Text -Text ($stderrTask.GetAwaiter().GetResult() + "`nDB runner timeout after $TimeoutSeconds seconds") -Secrets $Secrets
            }
        }
        $stdout = $stdoutTask.GetAwaiter().GetResult()
        $stderr = $stderrTask.GetAwaiter().GetResult()
        [pscustomobject]@{
            ExitCode = $process.ExitCode
            TimedOut = $false
            Stdout = Protect-Text -Text $stdout -Secrets $Secrets
            Stderr = Protect-Text -Text $stderr -Secrets $Secrets
        }
    }
    finally {
        if ($started -and -not $process.HasExited) { $process.Kill($true) }
        $process.Dispose()
    }
}

$repoRoot = Get-RepositoryRoot
$actualHead = (& git -C $repoRoot rev-parse HEAD).Trim().ToLowerInvariant()
if ($LASTEXITCODE -ne 0) { throw '현재 Git HEAD를 확인할 수 없습니다.' }
if ($actualHead -ne $ExpectedHead.Trim().ToLowerInvariant()) {
    throw "ExpectedHead mismatch. expected=$ExpectedHead actual=$actualHead"
}

if ([string]::IsNullOrWhiteSpace($EvidenceDir)) {
    $EvidenceDir = Join-Path $repoRoot 'build/evidence/db3-lifecycle'
} elseif (-not [IO.Path]::IsPathRooted($EvidenceDir)) {
    $EvidenceDir = Join-Path $repoRoot $EvidenceDir
}
$EvidenceDir = [IO.Path]::GetFullPath($EvidenceDir)
New-Item -ItemType Directory -Path $EvidenceDir -Force | Out-Null

if ([string]::IsNullOrWhiteSpace($RunnerExecutable)) {
    $isWindowsPlatform = [Runtime.InteropServices.RuntimeInformation]::IsOSPlatform([Runtime.InteropServices.OSPlatform]::Windows)
    $javaRelativePath = if ($isWindowsPlatform) { 'bin/java.exe' } else { 'bin/java' }
    $RunnerExecutable = if ($env:JAVA_HOME) { Join-Path $env:JAVA_HOME $javaRelativePath } else { 'java' }
    if ([string]::IsNullOrWhiteSpace($RunnerClasspath)) {
        $RunnerClasspath = Join-Path $repoRoot 'cpf-tools/verification/final-dev/build/classes/java/main'
    }
    $RunnerPrefixArguments = @('-cp', $RunnerClasspath, $RunnerClass)
}

$vendors = [ordered]@{
    oracle = @{ Url = 'CPF_RUNTIME_ORACLE_JDBC_URL'; User = 'CPF_RUNTIME_ORACLE_USERNAME'; Password = 'CPF_RUNTIME_ORACLE_PASSWORD' }
    postgresql = @{ Url = 'CPF_RUNTIME_POSTGRESQL_JDBC_URL'; User = 'CPF_RUNTIME_POSTGRESQL_USERNAME'; Password = 'CPF_RUNTIME_POSTGRESQL_PASSWORD' }
    mariadb = @{ Url = 'CPF_RUNTIME_MARIADB_JDBC_URL'; User = 'CPF_RUNTIME_MARIADB_USERNAME'; Password = 'CPF_RUNTIME_MARIADB_PASSWORD' }
}

$missing = [Collections.Generic.List[string]]::new()
foreach ($vendor in $vendors.GetEnumerator()) {
    foreach ($field in @('Url', 'User', 'Password')) {
        $name = $vendor.Value[$field]
        if ([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($name))) { $missing.Add($name) }
    }
}
if ($missing.Count -gt 0) {
    throw "DB3 환경변수 Preflight 실패: $([string]::Join(', ', ($missing | Sort-Object -Unique)))"
}

$allSecrets = @($vendors.Values | ForEach-Object { [Environment]::GetEnvironmentVariable($_.Password) })
$results = [Collections.Generic.List[object]]::new()
$overallExit = 0
foreach ($vendor in $vendors.GetEnumerator()) {
    $name = $vendor.Key
    $url = [Environment]::GetEnvironmentVariable($vendor.Value.Url)
    $username = [Environment]::GetEnvironmentVariable($vendor.Value.User)
    $password = [Environment]::GetEnvironmentVariable($vendor.Value.Password)
    $auditPath = Join-Path $EvidenceDir "$name-audit.json"
    $stdoutPath = Join-Path $EvidenceDir "$name-stdout.log"
    $stderrPath = Join-Path $EvidenceDir "$name-stderr.log"

    Assert-SafeJdbcUrl -Url $url
    $connectionJson = [ordered]@{ url = $url; username = $username; password = $password } | ConvertTo-Json -Compress
    $arguments = @($RunnerPrefixArguments) + @(
        "--vendor=$name",
        '--connection-json-stdin',
        "--audit-output=$auditPath"
    )
    $startedAt = [DateTimeOffset]::UtcNow
    try {
        $run = Invoke-LifecycleRunner -Executable $RunnerExecutable -Arguments $arguments -ConnectionJson $connectionJson -Secrets $allSecrets -TimeoutSeconds $TimeoutSeconds
        [IO.File]::WriteAllText($stdoutPath, $run.Stdout, [Text.UTF8Encoding]::new($false))
        [IO.File]::WriteAllText($stderrPath, $run.Stderr, [Text.UTF8Encoding]::new($false))
        if ($run.ExitCode -ne 0 -and $overallExit -eq 0) { $overallExit = $run.ExitCode }
        $dbVersion = $null
        $lifecycleStatus = if ($run.TimedOut) { 'UNKNOWN_TIMEOUT' } elseif ($run.ExitCode -eq 0) { 'SUCCEEDED' } else { 'FAILED' }
        if (Test-Path -LiteralPath $auditPath) {
            try {
                $auditRaw = Get-Content -LiteralPath $auditPath -Raw
                $auditSafe = Protect-Text -Text $auditRaw -Secrets $allSecrets
                [IO.File]::WriteAllText($auditPath, $auditSafe, [Text.UTF8Encoding]::new($false))
                $audit = $auditSafe | ConvertFrom-Json
                $dbVersion = $audit.dbVersion
                if ($audit.lifecycleStatus) { $lifecycleStatus = $audit.lifecycleStatus }
            } catch {
                if ($overallExit -eq 0) { $overallExit = 70 }
                $lifecycleStatus = 'INVALID_AUDIT'
            }
        } elseif ($run.ExitCode -eq 0) {
            if ($overallExit -eq 0) { $overallExit = 71 }
            $lifecycleStatus = 'MISSING_AUDIT'
        }
        $results.Add([ordered]@{
            vendor = $name; exitCode = $run.ExitCode; dbVersion = $dbVersion; timedOut = $run.TimedOut
            lifecycleStatus = $lifecycleStatus; startedAt = $startedAt.ToString('O')
            finishedAt = [DateTimeOffset]::UtcNow.ToString('O')
            auditFile = [IO.Path]::GetFileName($auditPath)
            stdoutFile = [IO.Path]::GetFileName($stdoutPath)
            stderrFile = [IO.Path]::GetFileName($stderrPath)
        })
    }
    finally {
        $password = $null
    }
}

$summary = [ordered]@{
    protocolVersion = 'CPF-DB3-LIFECYCLE-1'
    expectedHead = $ExpectedHead.ToLowerInvariant()
    actualHead = $actualHead
    repositoryRootResolvedByGit = $true
    connectionTransport = 'JSON_STDIN'
    credentialInArguments = $false
    childEnvironmentPolicy = 'CLEAR_THEN_ALLOWLIST'
    vendors = $results
    exitCode = $overallExit
}
$summaryPath = Join-Path $EvidenceDir 'db3-lifecycle-summary.json'
$summaryJson = $summary | ConvertTo-Json -Depth 8
$summaryJson = Protect-Text -Text $summaryJson -Secrets $allSecrets
[IO.File]::WriteAllText($summaryPath, $summaryJson, [Text.UTF8Encoding]::new($false))
exit $overallExit
