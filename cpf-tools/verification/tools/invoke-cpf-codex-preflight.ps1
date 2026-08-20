[CmdletBinding()]
param(
    [string]$RepoRoot = "",
    [string]$DockerRoot = "C:\dev\Docker\CPF",
    [string]$ExpectedHead = "",
    [string]$OutputPath = "",
    [switch]$RequireCleanWorktree,
    [switch]$RequireTrackedBuildSources
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

function Invoke-Native {
    param(
        [Parameter(Mandatory = $true)][string]$FilePath,
        [string[]]$Arguments = @()
    )

    $command = Get-Command -Name $FilePath -ErrorAction SilentlyContinue
    if ($null -eq $command) {
        return [pscustomobject]@{
            command   = $FilePath
            arguments = $Arguments
            found     = $false
            exitCode  = 127
            output    = "Command not found: $FilePath"
        }
    }

    $output = & $FilePath @Arguments 2>&1
    $exitCode = $LASTEXITCODE
    return [pscustomobject]@{
        command   = $FilePath
        arguments = $Arguments
        found     = $true
        exitCode  = $exitCode
        output    = (($output | ForEach-Object { "$_" }) -join [Environment]::NewLine).Trim()
    }
}

function Get-FirstLine {
    param([object]$Result)
    if ($null -eq $Result -or [string]::IsNullOrWhiteSpace($Result.output)) {
        return ""
    }
    return (($Result.output -split "\r?\n")[0]).Trim()
}

function Get-Sha256Text {
    param([AllowEmptyString()][string]$Value)

    $bytes = [Text.Encoding]::UTF8.GetBytes($Value)
    $sha = [Security.Cryptography.SHA256]::Create()
    try {
        return ([BitConverter]::ToString($sha.ComputeHash($bytes))).Replace("-", "").ToLowerInvariant()
    } finally {
        $sha.Dispose()
    }
}

function Get-WorktreeFingerprint {
    param(
        [Parameter(Mandatory = $true)][string]$RepositoryRoot,
        [Parameter(Mandatory = $true)][string]$GitHead
    )

    $checkpointPath = "cpf-docs/work/current/CODEX_FINAL_VALIDATION_RESULT.md"
    $diffResult = Invoke-Native -FilePath "git" -Arguments @(
        "-C", $RepositoryRoot,
        "diff", "--no-ext-diff", "--no-textconv", "--binary", "--full-index", "HEAD", "--", ".",
        ":(exclude,top)$checkpointPath"
    )
    if ($diffResult.exitCode -ne 0) {
        throw "Unable to hash the tracked worktree diff: $($diffResult.output)"
    }

    $untrackedResult = Invoke-Native -FilePath "git" -Arguments @(
        "-C", $RepositoryRoot,
        "ls-files", "--others", "--exclude-standard"
    )
    if ($untrackedResult.exitCode -ne 0) {
        throw "Unable to enumerate untracked worktree files: $($untrackedResult.output)"
    }

    $parts = [Collections.Generic.List[string]]::new()
    $parts.Add("cpf-worktree-fingerprint-v2")
    $parts.Add("head=$GitHead")
    $parts.Add("tracked-diff-begin")
    if (-not [string]::IsNullOrWhiteSpace($diffResult.output)) {
        foreach ($line in ($diffResult.output -split "\r?\n")) {
            $parts.Add([string]$line)
        }
    }
    $parts.Add("tracked-diff-end")
    $parts.Add("untracked-begin")
    if (-not [string]::IsNullOrWhiteSpace($untrackedResult.output)) {
        foreach ($relativePath in ($untrackedResult.output -split "\r?\n")) {
            if ([string]::IsNullOrWhiteSpace($relativePath)) {
                continue
            }
            $objectId = Invoke-Native -FilePath "git" -Arguments @(
                "-C", $RepositoryRoot,
                "hash-object", "--no-filters", "--", [string]$relativePath
            )
            if ($objectId.exitCode -ne 0) {
                throw "Unable to hash untracked file '$relativePath': $($objectId.output)"
            }
            $parts.Add("$relativePath`t$($objectId.output.Trim().ToLowerInvariant())")
        }
    }
    $parts.Add("untracked-end")

    return Get-Sha256Text -Value ($parts -join "`n")
}

if ([string]::IsNullOrWhiteSpace($RepoRoot)) {
    $RepoRoot = [IO.Path]::GetFullPath((Join-Path $PSScriptRoot "..\..\.."))
} else {
    $RepoRoot = [IO.Path]::GetFullPath($RepoRoot)
}

if (-not (Test-Path -LiteralPath $RepoRoot -PathType Container)) {
    throw "Repository root not found: $RepoRoot"
}

Set-Location -LiteralPath $RepoRoot

$requiredDocuments = @(
    "cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md",
    "cpf-docs/work/current/CPF_REQUIREMENT_MASTER.csv",
    "cpf-docs/work/REQUIREMENT_STATUS.csv",
    "cpf-docs/work/QA_FINDING_REVALIDATION.csv",
    "cpf-docs/deliverables/TEST_AND_EVIDENCE.md",
    "cpf-docs/deliverables/OPEN_ISSUES.md",
    "cpf-docs/work/REVIEW_INDEX.md",
    "cpf-docs/work/current/CPF_CODEX_REVALIDATION_SCOPE.md",
    "cpf-docs/work/current/CODEX_FINAL_VALIDATION_RESULT.md"
)

$restoredBuildSources = @(
    "cpf-tools/build/gradle-plugin/build.gradle",
    "cpf-tools/build/gradle-plugin/settings.gradle",
    "cpf-tools/build/gradle-plugin/src/main/java/com/cpf/gradle/CpfPlatformConventionPlugin.java",
    "cpf-tools/build/gradle-plugin/src/test/java/com/cpf/gradle/CpfPlatformConventionPluginTest.java",
    "cpf-tools/build/platform-bom/build.gradle",
    "cpf-tools/build/platform-bom/settings.gradle"
)

$missingDocuments = @(
    $requiredDocuments | Where-Object { -not (Test-Path -LiteralPath (Join-Path $RepoRoot $_) -PathType Leaf) }
)

$missingBuildSources = @(
    $restoredBuildSources | Where-Object { -not (Test-Path -LiteralPath (Join-Path $RepoRoot $_) -PathType Leaf) }
)

$untrackedBuildSources = @()
foreach ($path in $restoredBuildSources) {
    $tracked = Invoke-Native -FilePath "git" -Arguments @("ls-files", "--error-unmatch", "--", $path)
    if ($tracked.exitCode -ne 0) {
        $untrackedBuildSources += $path
    }
}

$headResult = Invoke-Native -FilePath "git" -Arguments @("rev-parse", "HEAD")
$remoteResult = Invoke-Native -FilePath "git" -Arguments @("rev-parse", "origin/master")
$branchResult = Invoke-Native -FilePath "git" -Arguments @("-c", "core.quotepath=false", "status", "--short", "--branch")
$porcelainResult = Invoke-Native -FilePath "git" -Arguments @("-c", "core.quotepath=false", "status", "--porcelain=v1", "--untracked-files=all")
$stagedResult = Invoke-Native -FilePath "git" -Arguments @("-c", "core.quotepath=false", "diff", "--cached", "--name-status")
$unstagedResult = Invoke-Native -FilePath "git" -Arguments @("-c", "core.quotepath=false", "diff", "--name-status")
$stashResult = Invoke-Native -FilePath "git" -Arguments @("stash", "list")

$head = Get-FirstLine $headResult
$remote = Get-FirstLine $remoteResult
$isClean = [string]::IsNullOrWhiteSpace($porcelainResult.output)
$headMatchesRemote = ($headResult.exitCode -eq 0 -and $remoteResult.exitCode -eq 0 -and $head -eq $remote)
$headMatchesExpected = ([string]::IsNullOrWhiteSpace($ExpectedHead) -or $head -eq $ExpectedHead)
$worktreeFingerprint = if ($headResult.exitCode -eq 0) {
    Get-WorktreeFingerprint -RepositoryRoot $RepoRoot -GitHead $head
} else {
    ""
}

$dockerScripts = @(
    (Join-Path $DockerRoot "cpf-env.ps1"),
    (Join-Path $DockerRoot "cpf-tooling.ps1"),
    (Join-Path $DockerRoot "run-full-toolchain.ps1"),
    (Join-Path $DockerRoot "run-trivy.ps1"),
    (Join-Path $DockerRoot "run-ort.ps1")
)
$missingDockerScripts = @($dockerScripts | Where-Object { -not (Test-Path -LiteralPath $_ -PathType Leaf) })

$toolChecks = [ordered]@{}
$toolChecks.git = Invoke-Native -FilePath "git" -Arguments @("--version")
$toolChecks.pwsh = Invoke-Native -FilePath "pwsh" -Arguments @("--version")
$toolChecks.java = Invoke-Native -FilePath "java" -Arguments @("-version")
$toolChecks.node = Invoke-Native -FilePath "node" -Arguments @("--version")
$toolChecks.npm = Invoke-Native -FilePath "npm" -Arguments @("--version")
$toolChecks.python = Invoke-Native -FilePath "python" -Arguments @("--version")
$toolChecks.docker = Invoke-Native -FilePath "docker" -Arguments @("--version")
$toolChecks.trivy = Invoke-Native -FilePath "trivy" -Arguments @("--version")

$dockerInfo = Invoke-Native -FilePath "docker" -Arguments @("info", "--format", "{{.ServerVersion}}")
$dockerEngineReady = ($dockerInfo.found -and $dockerInfo.exitCode -eq 0)

$containerList = $null
$containerInspect = $null
$volumeList = $null
$networkList = $null
$restartPolicyViolations = @()

if ($dockerEngineReady) {
    $containerList = Invoke-Native -FilePath "docker" -Arguments @(
        "ps", "-a", "--filter", "name=cpf-",
        "--format", "{{.Names}}|{{.Status}}|{{.Ports}}"
    )
    $volumeList = Invoke-Native -FilePath "docker" -Arguments @(
        "volume", "ls", "--filter", "name=cpf-", "--format", "{{.Name}}"
    )
    $networkList = Invoke-Native -FilePath "docker" -Arguments @(
        "network", "ls", "--filter", "name=cpf", "--format", "{{.Name}}"
    )

    $expectedContainers = @(
        "cpf-mariadb",
        "cpf-postgresql",
        "cpf-oracle",
        "cpf-redis",
        "cpf-kafka",
        "cpf-toxiproxy",
        "cpf-otel-collector"
    )

    $inspectRows = @()
    foreach ($name in $expectedContainers) {
        $inspect = Invoke-Native -FilePath "docker" -Arguments @(
            "inspect", $name,
            "--format", "{{.Name}}|{{.HostConfig.RestartPolicy.Name}}|{{.State.Status}}"
        )
        $inspectRows += [pscustomobject]@{
            name = $name
            exitCode = $inspect.exitCode
            output = $inspect.output
        }

        if ($inspect.exitCode -eq 0) {
            $parts = $inspect.output -split "\|"
            if ($parts.Count -ge 2 -and $parts[1].Trim() -ne "no") {
                $restartPolicyViolations += $name
            }
        }
    }
    $containerInspect = $inspectRows
}

$sourceFailures = [Collections.Generic.List[string]]::new()
if ($headResult.exitCode -ne 0 -or $remoteResult.exitCode -ne 0) {
    $sourceFailures.Add("Unable to resolve HEAD and origin/master.")
} elseif (-not $headMatchesRemote) {
    $sourceFailures.Add("HEAD must exactly match origin/master.")
}
if (-not $headMatchesExpected) {
    $sourceFailures.Add("HEAD does not match ExpectedHead.")
}
if ($missingDocuments.Count -gt 0) {
    $sourceFailures.Add("Required V2 canonical repository documents are missing.")
}
if ($missingBuildSources.Count -gt 0) {
    $sourceFailures.Add("Required Build Owner Source files are missing.")
}
if ($RequireCleanWorktree.IsPresent -and -not $isClean) {
    $sourceFailures.Add("RequireCleanWorktree was specified but the worktree is dirty.")
}
if ($RequireTrackedBuildSources.IsPresent -and $untrackedBuildSources.Count -gt 0) {
    $sourceFailures.Add("RequireTrackedBuildSources was specified but Build Owner Source files are untracked.")
}

$sourceReady = (
    $headResult.exitCode -eq 0 -and
    $remoteResult.exitCode -eq 0 -and
    $headMatchesRemote -and
    $headMatchesExpected -and
    $missingDocuments.Count -eq 0 -and
    $missingBuildSources.Count -eq 0 -and
    (-not $RequireCleanWorktree.IsPresent -or $isClean) -and
    (-not $RequireTrackedBuildSources.IsPresent -or $untrackedBuildSources.Count -eq 0)
)

$environmentReady = (
    $dockerEngineReady -and
    $missingDockerScripts.Count -eq 0 -and
    $restartPolicyViolations.Count -eq 0
)

if ([string]::IsNullOrWhiteSpace($OutputPath)) {
    $safeHead = if ([string]::IsNullOrWhiteSpace($head)) { "unknown" } else { $head.Substring(0, [Math]::Min(12, $head.Length)) }
    $timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
    $OutputPath = Join-Path $env:TEMP "cpf-codex-preflight-$safeHead-$timestamp.json"
}

$result = [ordered]@{
    schemaVersion = 2
    generatedAt = (Get-Date).ToString("o")
    repoRoot = $RepoRoot
    dockerRoot = $DockerRoot
    expectedHead = $ExpectedHead
    sourceReady = $sourceReady
    environmentReady = $environmentReady
    sourcePolicy = [ordered]@{
        requireCleanWorktree = $RequireCleanWorktree.IsPresent
        requireTrackedBuildSources = $RequireTrackedBuildSources.IsPresent
        failures = $sourceFailures
    }
    git = [ordered]@{
        head = $head
        originMaster = $remote
        headMatchesOriginMaster = $headMatchesRemote
        headMatchesExpected = $headMatchesExpected
        clean = $isClean
        worktreeFingerprint = $worktreeFingerprint
        porcelainSha256 = Get-Sha256Text -Value $porcelainResult.output
        branchStatus = $branchResult.output
        porcelain = $porcelainResult.output
        staged = $stagedResult.output
        unstaged = $unstagedResult.output
        stash = $stashResult.output
    }
    repositoryContracts = [ordered]@{
        requiredDocuments = $requiredDocuments
        restoredBuildSources = $restoredBuildSources
        missingDocuments = $missingDocuments
        missingRestoredBuildSources = $missingBuildSources
        untrackedRestoredBuildSources = $untrackedBuildSources
    }
    docker = [ordered]@{
        engineReady = $dockerEngineReady
        serverVersion = $dockerInfo.output
        missingScripts = $missingDockerScripts
        restartPolicyViolations = $restartPolicyViolations
        containers = if ($null -eq $containerList) { "" } else { $containerList.output }
        inspections = $containerInspect
        volumes = if ($null -eq $volumeList) { "" } else { $volumeList.output }
        networks = if ($null -eq $networkList) { "" } else { $networkList.output }
    }
    tools = $toolChecks
    outputPath = $OutputPath
}

$outputDirectory = Split-Path -Parent $OutputPath
if (-not (Test-Path -LiteralPath $outputDirectory -PathType Container)) {
    New-Item -ItemType Directory -Path $outputDirectory -Force | Out-Null
}

$result | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath $OutputPath -Encoding utf8NoBOM

Write-Host "CPF Codex Preflight"
Write-Host "  HEAD              : $head"
Write-Host "  origin/master     : $remote"
Write-Host "  Expected HEAD     : $ExpectedHead"
Write-Host "  Clean Tree        : $isClean"
Write-Host "  Clean Required    : $($RequireCleanWorktree.IsPresent)"
Write-Host "  Tracked Required  : $($RequireTrackedBuildSources.IsPresent)"
Write-Host "  Worktree SHA-256  : $worktreeFingerprint"
Write-Host "  Source Ready      : $sourceReady"
Write-Host "  Docker Ready      : $environmentReady"
Write-Host "  Result JSON       : $OutputPath"

if (-not $headMatchesExpected -and -not [string]::IsNullOrWhiteSpace($ExpectedHead)) {
    Write-Warning "HEAD differs from the document baseline. Use actual HEAD if HEAD == origin/master."
}

if (-not $sourceReady) {
    [Console]::Error.WriteLine("Source preflight failed. Do not start expensive validation stages.")
    exit 2
}

if (-not $environmentReady) {
    Write-Warning "Docker/tooling preflight is not fully ready. Static and Java stages may continue; runtime stages remain Environment Blocker until resolved."
}

exit 0
