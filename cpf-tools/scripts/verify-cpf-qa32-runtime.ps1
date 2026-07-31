param(
    [string]$Root = '.',
    [string]$EvidenceDir = 'cpf-docs/evidence/current/runtime',
    [switch]$SkipExternalTools,
    [switch]$AllowDirty
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$script:Results = [System.Collections.Generic.List[object]]::new()
$script:OverallExitCode = 0
$script:FailureMessage = $null
$script:InitialSha = $null
$script:StartedAt = (Get-Date).ToUniversalTime().ToString('o')
$script:RawDir = $null
$script:ResolvedEvidenceDir = $null

function Get-CpfExitCode {
    if ($null -eq $LASTEXITCODE) { return 0 }
    return [int]$LASTEXITCODE
}

function ConvertTo-CpfSafeText {
    param([AllowEmptyString()][string]$Text)
    if ($null -eq $Text) { return '' }
    $safe = $Text
    $safe = $safe -replace '(?i)(authorization\s*[:=]\s*bearer\s+)[^\s"'']+', '$1***'
    $safe = $safe -replace '(?i)((?:access|refresh)?token|password|secret|cookie|session(?:id)?|private[_-]?key)\s*[:=]\s*[^\s,;"'']+', '$1=***'
    $safe = $safe -replace '(?i)(jdbc:[^\s]+?://[^:/\s]+:)[^@/\s]+@', '$1***@'
    return $safe
}

function Write-CpfSanitizedLog {
    param(
        [Parameter(Mandatory)][string]$RawPath,
        [Parameter(Mandatory)][string]$TargetPath
    )
    $content = if (Test-Path $RawPath) { Get-Content -Raw -LiteralPath $RawPath } else { '' }
    $safe = ConvertTo-CpfSafeText $content
    [System.IO.File]::WriteAllText($TargetPath, $safe, [System.Text.UTF8Encoding]::new($false))
}

function Invoke-CpfNativeStep {
    param(
        [Parameter(Mandatory)][string]$Name,
        [Parameter(Mandatory)][string]$FilePath,
        [string[]]$ArgumentList = @(),
        [string]$WorkingDirectory = '.',
        [hashtable]$Environment = @{}
    )

    $started = (Get-Date).ToUniversalTime()
    $rawOut = Join-Path $script:RawDir "$Name.stdout.log"
    $rawErr = Join-Path $script:RawDir "$Name.stderr.log"
    $safeOut = Join-Path $script:ResolvedEvidenceDir "$Name.stdout.sanitized.log"
    $safeErr = Join-Path $script:ResolvedEvidenceDir "$Name.stderr.sanitized.log"
    $commandDisplay = (@($FilePath) + $ArgumentList) -join ' '
    $exitCode = 9009
    $errorText = $null

    try {
        $startInfo = [System.Diagnostics.ProcessStartInfo]::new()
        $startInfo.FileName = $FilePath
        $startInfo.WorkingDirectory = (Resolve-Path $WorkingDirectory).Path
        $startInfo.UseShellExecute = $false
        $startInfo.RedirectStandardOutput = $true
        $startInfo.RedirectStandardError = $true
        foreach ($argument in $ArgumentList) { [void]$startInfo.ArgumentList.Add($argument) }
        foreach ($key in $Environment.Keys) { $startInfo.Environment[$key] = [string]$Environment[$key] }

        $process = [System.Diagnostics.Process]::new()
        $process.StartInfo = $startInfo
        if (-not $process.Start()) { throw "process start returned false: $FilePath" }
        $stdoutTask = $process.StandardOutput.ReadToEndAsync()
        $stderrTask = $process.StandardError.ReadToEndAsync()
        $process.WaitForExit()
        $stdout = $stdoutTask.GetAwaiter().GetResult()
        $stderr = $stderrTask.GetAwaiter().GetResult()
        [System.IO.File]::WriteAllText($rawOut, $stdout, [System.Text.UTF8Encoding]::new($false))
        [System.IO.File]::WriteAllText($rawErr, $stderr, [System.Text.UTF8Encoding]::new($false))
        $exitCode = $process.ExitCode
    }
    catch {
        $errorText = $_.Exception.Message
        [System.IO.File]::WriteAllText($rawErr, $errorText, [System.Text.UTF8Encoding]::new($false))
        $exitCode = 9009
    }
    finally {
        Write-CpfSanitizedLog -RawPath $rawOut -TargetPath $safeOut
        Write-CpfSanitizedLog -RawPath $rawErr -TargetPath $safeErr
    }

    $finished = (Get-Date).ToUniversalTime()
    $result = [ordered]@{
        name = $Name
        command = $commandDisplay
        workingDirectory = (Resolve-Path $WorkingDirectory).Path
        startedAt = $started.ToString('o')
        finishedAt = $finished.ToString('o')
        durationMillis = [long]($finished - $started).TotalMilliseconds
        exitCode = $exitCode
        stdoutPath = (Resolve-Path $safeOut).Path
        stderrPath = (Resolve-Path $safeErr).Path
        stdoutSha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $safeOut).Hash.ToLowerInvariant()
        stderrSha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $safeErr).Hash.ToLowerInvariant()
        sanitized = $true
        error = ConvertTo-CpfSafeText $errorText
    }
    $script:Results.Add($result)

    if ($exitCode -ne 0) {
        throw "$Name failed (exit=$exitCode)"
    }
}

function Get-CpfVersion {
    param([string]$Name, [string]$FilePath, [string[]]$Arguments)
    $stdout = Join-Path $script:RawDir "version-$Name.stdout.log"
    $stderr = Join-Path $script:RawDir "version-$Name.stderr.log"
    try {
        $process = Start-Process -FilePath $FilePath -ArgumentList $Arguments -NoNewWindow -Wait -PassThru -RedirectStandardOutput $stdout -RedirectStandardError $stderr
        $text = ((Get-Content -Raw -ErrorAction SilentlyContinue $stdout) + "`n" + (Get-Content -Raw -ErrorAction SilentlyContinue $stderr)).Trim()
        return [ordered]@{ exitCode = $process.ExitCode; output = ConvertTo-CpfSafeText $text }
    }
    catch {
        return [ordered]@{ exitCode = 9009; output = ConvertTo-CpfSafeText $_.Exception.Message }
    }
}

$originalLocation = Get-Location
try {
    $resolvedRoot = (Resolve-Path $Root).Path
    Set-Location $resolvedRoot
    $script:ResolvedEvidenceDir = Join-Path $resolvedRoot $EvidenceDir
    New-Item -ItemType Directory -Force $script:ResolvedEvidenceDir | Out-Null
    $script:RawDir = Join-Path $resolvedRoot 'build/qa32-runtime-raw'
    Remove-Item -Recurse -Force -ErrorAction SilentlyContinue $script:RawDir
    New-Item -ItemType Directory -Force $script:RawDir | Out-Null

    $script:InitialSha = (& git rev-parse HEAD).Trim()
    if ((Get-CpfExitCode) -ne 0 -or $script:InitialSha -notmatch '^[0-9a-f]{40}$') {
        throw 'git SHA resolution failed'
    }
    $dirty = (& git status --porcelain=v1 | Out-String).Trim()
    if (-not $AllowDirty -and $dirty) {
        throw 'Release runtime verification requires a clean Working Tree.'
    }

    $versions = [ordered]@{
        java = Get-CpfVersion 'java' 'java' @('-version')
        gradle = Get-CpfVersion 'gradle' (Join-Path $resolvedRoot 'gradlew.bat') @('--version', '--no-daemon')
        node = Get-CpfVersion 'node' 'node' @('--version')
        npm = Get-CpfVersion 'npm' 'npm.cmd' @('--version')
        git = Get-CpfVersion 'git' 'git' @('--version')
    }
    if ($versions.java.exitCode -ne 0 -or $versions.java.output -notmatch '(?m)version\s+"25(?:\.|"|\s)') {
        throw 'Java 25 is required and must be the active java executable.'
    }

    Invoke-CpfNativeStep 'gradle-projects' (Join-Path $resolvedRoot 'gradlew.bat') @('projects', '--no-daemon', '--stacktrace')
    Invoke-CpfNativeStep 'gradle-help' (Join-Path $resolvedRoot 'gradlew.bat') @('help', '--no-daemon', '--stacktrace')
    Invoke-CpfNativeStep 'gradle-clean-test' (Join-Path $resolvedRoot 'gradlew.bat') @('clean', 'test', '--no-daemon', '--stacktrace')

    foreach ($frontend in @(
        @{ prefix = 'adm'; dir = 'cpf-admin/frontend' },
        @{ prefix = 'bza'; dir = 'cpf-biz-admin/frontend' }
    )) {
        $dir = Join-Path $resolvedRoot $frontend.dir
        $npmCache = Join-Path $resolvedRoot "build/npm-cache-$($frontend.prefix)"
        $env = @{ npm_config_cache = $npmCache }
        Invoke-CpfNativeStep "$($frontend.prefix)-npm-ci" 'npm.cmd' @('ci', '--no-audit', '--no-fund') $dir $env
        Invoke-CpfNativeStep "$($frontend.prefix)-typecheck" 'npm.cmd' @('run', 'typecheck') $dir $env
        Invoke-CpfNativeStep "$($frontend.prefix)-unit-test" 'npm.cmd' @('run', 'test') $dir $env
        Invoke-CpfNativeStep "$($frontend.prefix)-build" 'npm.cmd' @('run', 'build') $dir $env
        foreach ($browser in @('chromium', 'firefox', 'webkit')) {
            Invoke-CpfNativeStep "$($frontend.prefix)-playwright-$browser" 'npx.cmd' @('--no-install', 'playwright', 'test', "--project=$browser") $dir $env
        }
    }

    Invoke-CpfNativeStep 'qa32-integration' (Join-Path $resolvedRoot 'gradlew.bat') @(
        'qa32IntegrationTest',
        '-Pqa32Vendors=oracle,postgresql,mariadb',
        '-Pqa32Kafka=true',
        '--no-daemon',
        '--stacktrace'
    )

    if (-not $SkipExternalTools) {
        Invoke-CpfNativeStep 'supply-chain-final-artifact' 'pwsh' @(
            '-NoProfile',
            '-File',
            (Join-Path $resolvedRoot 'cpf-tools/scripts/generate-cpf-supply-chain-evidence.ps1'),
            '-Root',
            $resolvedRoot
        )
    }

    $finalSha = (& git rev-parse HEAD).Trim()
    if ((Get-CpfExitCode) -ne 0 -or $finalSha -ne $script:InitialSha) {
        throw "Source SHA changed during verification: start=$($script:InitialSha), end=$finalSha"
    }
    if (-not $AllowDirty -and ((& git status --porcelain=v1 | Out-String).Trim())) {
        throw 'Working Tree changed during release runtime verification.'
    }
}
catch {
    $script:OverallExitCode = 1
    $script:FailureMessage = ConvertTo-CpfSafeText $_.Exception.Message
}
finally {
    try {
        if ($script:ResolvedEvidenceDir) {
            $finishedAt = (Get-Date).ToUniversalTime().ToString('o')
            $finalSha = $null
            try { $finalSha = (& git rev-parse HEAD).Trim() } catch { $finalSha = $null }
            $versions = if (Get-Variable -Name versions -ErrorAction SilentlyContinue) { $versions } else { [ordered]@{} }
            $evidence = [ordered]@{
                schemaVersion = 2
                sourceSha = $script:InitialSha
                finalSourceSha = $finalSha
                startedAt = $script:StartedAt
                finishedAt = $finishedAt
                exitCode = $script:OverallExitCode
                status = if ($script:OverallExitCode -eq 0) { 'PASS' } else { 'FAIL' }
                failureMessage = $script:FailureMessage
                profile = 'QA32_RELEASE'
                environment = [ordered]@{
                    operatingSystem = [System.Environment]::OSVersion.VersionString
                    machineName = [System.Environment]::MachineName
                    userName = 'REDACTED'
                    topology = 'local-or-orchestrated-runtime; individual steps record actual commands'
                }
                versions = $versions
                results = $script:Results
                sanitized = $true
            }
            $evidencePath = Join-Path $script:ResolvedEvidenceDir 'qa32-runtime-evidence.sanitized.json'
            $json = $evidence | ConvertTo-Json -Depth 12
            [System.IO.File]::WriteAllText($evidencePath, $json + "`n", [System.Text.UTF8Encoding]::new($false))
            $evidenceHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $evidencePath).Hash.ToLowerInvariant()
            [System.IO.File]::WriteAllText("$evidencePath.sha256", "$evidenceHash  qa32-runtime-evidence.sanitized.json`n", [System.Text.UTF8Encoding]::new($false))
        }
    }
    finally {
        if ($script:RawDir) { Remove-Item -Recurse -Force -ErrorAction SilentlyContinue $script:RawDir }
        Set-Location $originalLocation
    }
}

exit $script:OverallExitCode
