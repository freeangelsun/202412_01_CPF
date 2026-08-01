[CmdletBinding()]
param(
    [string]$RepoRoot = "C:\dev\projects\jck\202412_01_CPF",
    [string]$DockerRoot = "C:\dev\Docker\CPF",
    [string]$ExpectedHead = "",
    [string]$OutputPath = ""
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

if (-not (Test-Path -LiteralPath $RepoRoot -PathType Container)) {
    throw "Repository root not found: $RepoRoot"
}

Set-Location -LiteralPath $RepoRoot

$requiredDocuments = @(
    "cpf-docs/guides/docker/README.md",
    "cpf-docs/guides/docker/CPF_도커_개발테스트환경_안내.md",
    "cpf-docs/guides/docker/CPF_도커_연동및사용가이드.md",
    "cpf-docs/architecture/CPF_도커_개발테스트환경_구성명세.md",
    "cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md",
    "cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md",
    "cpf-docs/work/codex/qa37/CODEX_START_HERE.md",
    "cpf-docs/work/codex/qa37/CPF_CODEX_QA37_FINAL_INDEPENDENT_VERIFICATION_REQUEST.md"
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

$sourceReady = (
    $headResult.exitCode -eq 0 -and
    $remoteResult.exitCode -eq 0 -and
    $headMatchesRemote -and
    $isClean -and
    $missingDocuments.Count -eq 0 -and
    $missingBuildSources.Count -eq 0 -and
    $untrackedBuildSources.Count -eq 0
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
    schemaVersion = 1
    generatedAt = (Get-Date).ToString("o")
    repoRoot = $RepoRoot
    dockerRoot = $DockerRoot
    expectedHead = $ExpectedHead
    sourceReady = $sourceReady
    environmentReady = $environmentReady
    git = [ordered]@{
        head = $head
        originMaster = $remote
        headMatchesOriginMaster = $headMatchesRemote
        headMatchesExpected = $headMatchesExpected
        clean = $isClean
        branchStatus = $branchResult.output
        porcelain = $porcelainResult.output
        staged = $stagedResult.output
        unstaged = $unstagedResult.output
        stash = $stashResult.output
    }
    repositoryContracts = [ordered]@{
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
Write-Host "  Source Ready      : $sourceReady"
Write-Host "  Docker Ready      : $environmentReady"
Write-Host "  Result JSON       : $OutputPath"

if (-not $headMatchesExpected -and -not [string]::IsNullOrWhiteSpace($ExpectedHead)) {
    Write-Warning "HEAD differs from the document baseline. Use actual HEAD if HEAD == origin/master."
}

if (-not $sourceReady) {
    Write-Error "Source preflight failed. Do not start expensive validation stages."
    exit 2
}

if (-not $environmentReady) {
    Write-Warning "Docker/tooling preflight is not fully ready. Static and Java stages may continue; runtime stages remain Environment Blocker until resolved."
}

exit 0
