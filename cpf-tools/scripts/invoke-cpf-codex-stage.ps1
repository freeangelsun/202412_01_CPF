[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][ValidatePattern("^[A-Za-z0-9_.-]+$")][string]$StageId,
    [Parameter(Mandatory = $true)][string]$Command,
    [string]$WorkingDirectory = "C:\dev\projects\jck\202412_01_CPF",
    [string]$LedgerRoot = "C:\dev\Docker\CPF\output\codex\qa37",
    [ValidateSet("unspecified", "mariadb", "postgresql", "oracle")][string]$DatabaseVendor = "unspecified",
    [string]$EnvironmentFingerprint = "",
    [string]$ArtifactPath = "",
    [switch]$RequireArtifact,
    [switch]$RequireExplicitEnvironmentFingerprint,
    [switch]$AllowRerun
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$ledgerColumns = @(
    "stageId",
    "status",
    "startedAt",
    "endedAt",
    "durationSeconds",
    "exitCode",
    "gitHead",
    "gitHeadAfter",
    "workingDirectory",
    "commandHash",
    "worktreeFingerprint",
    "worktreeFingerprintAfter",
    "sourceSelfDirty",
    "databaseVendor",
    "environmentFingerprint",
    "environmentFingerprintExplicit",
    "explicitEnvironmentRequired",
    "logPath",
    "logSha256",
    "artifactRequired",
    "artifactPath",
    "artifactKind",
    "artifactSha256"
)

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

function Get-NormalizedFullPath {
    param(
        [Parameter(Mandatory = $true)][string]$Path,
        [string]$BasePath = ""
    )

    if (-not [IO.Path]::IsPathRooted($Path)) {
        if ([string]::IsNullOrWhiteSpace($BasePath)) {
            $Path = Join-Path (Get-Location).Path $Path
        } else {
            $Path = Join-Path $BasePath $Path
        }
    }
    return [IO.Path]::GetFullPath($Path).TrimEnd([IO.Path]::DirectorySeparatorChar, [IO.Path]::AltDirectorySeparatorChar)
}

function Invoke-CpfGit {
    param(
        [Parameter(Mandatory = $true)][string]$RepositoryRoot,
        [Parameter(Mandatory = $true)][string[]]$Arguments
    )

    $output = @(& git -C $RepositoryRoot @Arguments 2>&1)
    if ($LASTEXITCODE -ne 0) {
        $detail = ($output | ForEach-Object { [string]$_ }) -join "`n"
        throw "Git command failed (exit $LASTEXITCODE): git $($Arguments -join ' ')`n$detail"
    }
    return $output | ForEach-Object { [string]$_ }
}

function Get-GitHead {
    param([Parameter(Mandatory = $true)][string]$RepositoryRoot)

    $lines = @(Invoke-CpfGit -RepositoryRoot $RepositoryRoot -Arguments @("rev-parse", "HEAD"))
    $head = ($lines -join "`n").Trim()
    if ($head -notmatch "^[0-9a-fA-F]{40,64}$") {
        throw "Unable to determine a valid Git HEAD for: $RepositoryRoot"
    }
    return $head.ToLowerInvariant()
}

function Get-WorktreeFingerprint {
    param(
        [Parameter(Mandatory = $true)][string]$RepositoryRoot,
        [Parameter(Mandatory = $true)][string]$GitHead
    )

    # HEAD-relative binary diff captures tracked/staged source changes. Untracked,
    # non-ignored files are represented by both their relative path and Git blob
    # hash so a content-only change also invalidates a previous PASS.
    $diff = @(
        Invoke-CpfGit -RepositoryRoot $RepositoryRoot -Arguments @(
            "diff", "--no-ext-diff", "--no-textconv", "--binary", "--full-index", "HEAD", "--"
        )
    )
    $untracked = @(
        Invoke-CpfGit -RepositoryRoot $RepositoryRoot -Arguments @(
            "ls-files", "--others", "--exclude-standard"
        )
    )

    $parts = [Collections.Generic.List[string]]::new()
    $parts.Add("cpf-worktree-fingerprint-v1")
    $parts.Add("head=$GitHead")
    $parts.Add("tracked-diff-begin")
    foreach ($line in $diff) {
        $parts.Add([string]$line)
    }
    $parts.Add("tracked-diff-end")
    $parts.Add("untracked-begin")
    foreach ($relativePath in $untracked) {
        $objectId = @(
            Invoke-CpfGit -RepositoryRoot $RepositoryRoot -Arguments @(
                "hash-object", "--no-filters", "--", [string]$relativePath
            )
        )
        $parts.Add("$relativePath`t$(($objectId -join '').Trim().ToLowerInvariant())")
    }
    $parts.Add("untracked-end")

    return Get-Sha256Text -Value ($parts -join "`n")
}

function Get-EffectiveEnvironmentFingerprint {
    param([AllowEmptyString()][string]$ExplicitFingerprint)

    if (-not [string]::IsNullOrWhiteSpace($ExplicitFingerprint)) {
        # Store only a one-way digest. The caller-provided value may be an opaque
        # environment identifier and must never be copied into the ledger or log.
        return Get-Sha256Text -Value "cpf-explicit-environment-v1`n$ExplicitFingerprint"
    }

    $dockerContext = "unavailable"
    $dockerServer = "unavailable"
    $dockerHost = "unavailable"
    try {
        $contextOutput = @(& docker context show 2>$null)
        if ($LASTEXITCODE -eq 0 -and $contextOutput.Count -gt 0) {
            $dockerContext = ($contextOutput -join "`n").Trim()
        }
        $serverOutput = @(& docker version --format "{{.Server.Version}}|{{.Server.Os}}|{{.Server.Arch}}" 2>$null)
        if ($LASTEXITCODE -eq 0 -and $serverOutput.Count -gt 0) {
            $dockerServer = ($serverOutput -join "`n").Trim()
        }
        $hostOutput = @(& docker info --format "{{.ID}}|{{.Name}}|{{.OSType}}|{{.Architecture}}" 2>$null)
        if ($LASTEXITCODE -eq 0 -and $hostOutput.Count -gt 0) {
            $dockerHost = ($hostOutput -join "`n").Trim()
        }
    } catch {
        $dockerContext = "unavailable"
        $dockerServer = "unavailable"
        $dockerHost = "unavailable"
    }

    $descriptor = @(
        "cpf-default-environment-v1",
        "machine=$([Environment]::MachineName)",
        "os=$([Environment]::OSVersion.VersionString)",
        "processArchitecture=$([Runtime.InteropServices.RuntimeInformation]::ProcessArchitecture)",
        "pwsh=$($PSVersionTable.PSVersion)",
        "dockerContext=$dockerContext",
        "dockerServer=$dockerServer",
        "dockerHost=$dockerHost"
    ) -join "`n"
    return Get-Sha256Text -Value $descriptor
}

function Get-ArtifactEvidence {
    param([Parameter(Mandatory = $true)][string]$Path)

    if (Test-Path -LiteralPath $Path -PathType Leaf) {
        return [pscustomobject]@{
            Kind = "file"
            Sha256 = (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
        }
    }
    if (Test-Path -LiteralPath $Path -PathType Container) {
        $manifest = [Collections.Generic.List[string]]::new()
        $manifest.Add("cpf-artifact-directory-manifest-v1")
        $files = @(
            Get-ChildItem -LiteralPath $Path -File -Recurse -Force |
            Sort-Object { [IO.Path]::GetRelativePath($Path, $_.FullName).Replace("\", "/") } -CaseSensitive
        )
        foreach ($file in $files) {
            $relativePath = [IO.Path]::GetRelativePath($Path, $file.FullName).Replace("\", "/")
            $fileHash = (Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
            $manifest.Add("$($relativePath.Length):$relativePath`t$fileHash")
        }
        return [pscustomobject]@{
            Kind = "directory"
            Sha256 = Get-Sha256Text -Value ($manifest -join "`n")
        }
    }
    return $null
}

function Test-PreviousPassReusable {
    param(
        [Parameter(Mandatory = $true)][psobject]$Row,
        [Parameter(Mandatory = $true)][string]$CurrentGitHead,
        [Parameter(Mandatory = $true)][string]$CurrentCommandHash,
        [Parameter(Mandatory = $true)][string]$CurrentWorktreeFingerprint,
        [Parameter(Mandatory = $true)][string]$CurrentDatabaseVendor,
        [Parameter(Mandatory = $true)][string]$CurrentEnvironmentFingerprint,
        [Parameter(Mandatory = $true)][bool]$CurrentEnvironmentFingerprintExplicit,
        [Parameter(Mandatory = $true)][bool]$CurrentExplicitEnvironmentRequired,
        [Parameter(Mandatory = $true)][string]$CurrentWorkingDirectory,
        [Parameter(Mandatory = $true)][bool]$CurrentArtifactRequired,
        [AllowEmptyString()][string]$CurrentArtifactPath
    )

    foreach ($column in $ledgerColumns) {
        if ($null -eq $Row.PSObject.Properties[$column]) {
            return $false
        }
    }
    if ([string]$Row.status -cne "PASS" -or [string]$Row.exitCode -ne "0") {
        return $false
    }
    if ([string]$Row.gitHead -cne $CurrentGitHead -or [string]$Row.gitHeadAfter -cne $CurrentGitHead) {
        return $false
    }
    if ([string]$Row.commandHash -cne $CurrentCommandHash) {
        return $false
    }
    if (
        [string]$Row.worktreeFingerprint -cne $CurrentWorktreeFingerprint -or
        [string]$Row.worktreeFingerprintAfter -cne $CurrentWorktreeFingerprint -or
        [string]$Row.sourceSelfDirty -cne "false"
    ) {
        return $false
    }
    if (
        [string]$Row.databaseVendor -cne $CurrentDatabaseVendor -or
        [string]$Row.environmentFingerprint -cne $CurrentEnvironmentFingerprint -or
        [string]$Row.environmentFingerprintExplicit -cne $CurrentEnvironmentFingerprintExplicit.ToString().ToLowerInvariant() -or
        [string]$Row.explicitEnvironmentRequired -cne $CurrentExplicitEnvironmentRequired.ToString().ToLowerInvariant()
    ) {
        return $false
    }
    if ($CurrentExplicitEnvironmentRequired -and -not $CurrentEnvironmentFingerprintExplicit) {
        return $false
    }

    try {
        $rowWorkingDirectory = Get-NormalizedFullPath -Path ([string]$Row.workingDirectory)
    } catch {
        return $false
    }
    if ($rowWorkingDirectory -ne $CurrentWorkingDirectory) {
        return $false
    }

    $rowLogPath = [string]$Row.logPath
    $rowLogHash = ([string]$Row.logSha256).ToLowerInvariant()
    if (
        [string]::IsNullOrWhiteSpace($rowLogPath) -or
        $rowLogHash -notmatch "^[0-9a-f]{64}$" -or
        -not (Test-Path -LiteralPath $rowLogPath -PathType Leaf)
    ) {
        return $false
    }
    if ((Get-FileHash -LiteralPath $rowLogPath -Algorithm SHA256).Hash.ToLowerInvariant() -cne $rowLogHash) {
        return $false
    }

    $rowArtifactPath = [string]$Row.artifactPath
    $rowArtifactKind = [string]$Row.artifactKind
    $rowArtifactHash = ([string]$Row.artifactSha256).ToLowerInvariant()
    if (-not $CurrentArtifactRequired) {
        return (
            [string]$Row.artifactRequired -ceq "false" -and
            [string]::IsNullOrWhiteSpace($rowArtifactPath) -and
            [string]::IsNullOrWhiteSpace($rowArtifactKind) -and
            [string]::IsNullOrWhiteSpace($rowArtifactHash)
        )
    }
    if (
        [string]$Row.artifactRequired -cne "true" -or
        [string]::IsNullOrWhiteSpace($CurrentArtifactPath) -or
        [string]::IsNullOrWhiteSpace($rowArtifactPath) -or
        $rowArtifactKind -notin @("file", "directory") -or
        $rowArtifactHash -notmatch "^[0-9a-f]{64}$" -or
        -not (Test-Path -LiteralPath $CurrentArtifactPath)
    ) {
        return $false
    }
    try {
        $normalizedRowArtifactPath = Get-NormalizedFullPath -Path $rowArtifactPath
    } catch {
        return $false
    }
    if ($normalizedRowArtifactPath -ne $CurrentArtifactPath) {
        return $false
    }
    $currentArtifactEvidence = Get-ArtifactEvidence -Path $CurrentArtifactPath
    if ($null -eq $currentArtifactEvidence) {
        return $false
    }
    return (
        [string]$currentArtifactEvidence.Kind -ceq $rowArtifactKind -and
        [string]$currentArtifactEvidence.Sha256 -ceq $rowArtifactHash
    )
}

function Convert-ToCanonicalLedgerRow {
    param(
        [Parameter(Mandatory = $true)][psobject]$InputRow,
        [Parameter(Mandatory = $true)][string[]]$Columns
    )

    $values = [ordered]@{}
    foreach ($column in $Columns) {
        $property = $InputRow.PSObject.Properties[$column]
        $values[$column] = if ($null -eq $property) { "" } else { $property.Value }
    }
    return [pscustomobject]$values
}

$workingDirectoryFull = Get-NormalizedFullPath -Path $WorkingDirectory
if (-not (Test-Path -LiteralPath $workingDirectoryFull -PathType Container)) {
    throw "Working directory not found: $workingDirectoryFull"
}

$repositoryRootOutput = @(
    Invoke-CpfGit -RepositoryRoot $workingDirectoryFull -Arguments @("rev-parse", "--show-toplevel")
)
$repositoryRoot = Get-NormalizedFullPath -Path (($repositoryRootOutput -join "`n").Trim())
$gitHead = Get-GitHead -RepositoryRoot $repositoryRoot
$commandHash = Get-Sha256Text -Value $Command
$worktreeFingerprint = Get-WorktreeFingerprint -RepositoryRoot $repositoryRoot -GitHead $gitHead
$stageIdNormalized = $StageId.ToUpperInvariant()
$qa37ExpectedDatabaseVendors = @{
    "04_DB_MARIA" = "mariadb"
    "05_DB_POSTGRES" = "postgresql"
    "06_DB_ORACLE" = "oracle"
}
$qa37DockerSensitiveStageIds = @(
    "07_RUNTIME",
    "08_FAULT",
    "09_OTEL",
    "12_BROWSER",
    "13_SUPPLY"
)
$expectedDatabaseVendor = if ($qa37ExpectedDatabaseVendors.ContainsKey($stageIdNormalized)) {
    [string]$qa37ExpectedDatabaseVendors[$stageIdNormalized]
} else {
    ""
}
$fixedStageRequiresExplicitEnvironment = (
    -not [string]::IsNullOrWhiteSpace($expectedDatabaseVendor) -or
    $qa37DockerSensitiveStageIds -contains $stageIdNormalized
)
$databaseVendorNormalized = $DatabaseVendor.ToLowerInvariant()
$environmentFingerprintExplicit = -not [string]::IsNullOrWhiteSpace($EnvironmentFingerprint)
$explicitEnvironmentRequired = (
    $RequireExplicitEnvironmentFingerprint.IsPresent -or
    $databaseVendorNormalized -cne "unspecified" -or
    $fixedStageRequiresExplicitEnvironment
)
$environmentFingerprintHash = Get-EffectiveEnvironmentFingerprint -ExplicitFingerprint $EnvironmentFingerprint
$artifactPathFull = if ([string]::IsNullOrWhiteSpace($ArtifactPath)) {
    ""
} else {
    Get-NormalizedFullPath -Path $ArtifactPath -BasePath $workingDirectoryFull
}
$artifactRequiredEffective = $RequireArtifact.IsPresent -or -not [string]::IsNullOrWhiteSpace($artifactPathFull)

$preflightFailures = [Collections.Generic.List[string]]::new()
if (
    -not [string]::IsNullOrWhiteSpace($expectedDatabaseVendor) -and
    $databaseVendorNormalized -cne $expectedDatabaseVendor
) {
    $preflightFailures.Add(
        "QA37 stage '$stageIdNormalized' requires DatabaseVendor '$expectedDatabaseVendor'; received '$databaseVendorNormalized'."
    )
}
if ($explicitEnvironmentRequired -and -not $environmentFingerprintExplicit) {
    $preflightFailures.Add("This DB/runtime stage requires an explicit EnvironmentFingerprint.")
}
if ($RequireArtifact.IsPresent -and [string]::IsNullOrWhiteSpace($artifactPathFull)) {
    $preflightFailures.Add("RequireArtifact was specified but ArtifactPath is empty.")
}

$ledgerRootFull = Get-NormalizedFullPath -Path $LedgerRoot
New-Item -ItemType Directory -Path $ledgerRootFull -Force | Out-Null
$logRoot = Join-Path $ledgerRootFull "logs"
New-Item -ItemType Directory -Path $logRoot -Force | Out-Null

$ledgerPath = Join-Path $ledgerRootFull "execution-ledger.csv"
$previous = @()
if (Test-Path -LiteralPath $ledgerPath -PathType Leaf) {
    $previous = @(Import-Csv -LiteralPath $ledgerPath)
}
$effectiveLedgerColumns = [Collections.Generic.List[string]]::new()
foreach ($column in $ledgerColumns) {
    $effectiveLedgerColumns.Add($column)
}
foreach ($previousRow in $previous) {
    foreach ($property in $previousRow.PSObject.Properties) {
        if (-not $effectiveLedgerColumns.Contains($property.Name)) {
            $effectiveLedgerColumns.Add($property.Name)
        }
    }
}

$lastStageResult = @(
    $previous |
    Where-Object { $_.stageId -eq $StageId } |
    Select-Object -Last 1
)

if (
    $lastStageResult.Count -gt 0 -and
    (Test-PreviousPassReusable `
        -Row $lastStageResult[0] `
        -CurrentGitHead $gitHead `
        -CurrentCommandHash $commandHash `
        -CurrentWorktreeFingerprint $worktreeFingerprint `
        -CurrentDatabaseVendor $databaseVendorNormalized `
        -CurrentEnvironmentFingerprint $environmentFingerprintHash `
        -CurrentEnvironmentFingerprintExplicit $environmentFingerprintExplicit `
        -CurrentExplicitEnvironmentRequired $explicitEnvironmentRequired `
        -CurrentWorkingDirectory $workingDirectoryFull `
        -CurrentArtifactRequired $artifactRequiredEffective `
        -CurrentArtifactPath $artifactPathFull)
) {
    Write-Host "SKIP: Stage '$StageId' has a fully matching, evidence-valid PASS."
    Write-Host "Ledger: $ledgerPath"
    exit 0
}

if ($lastStageResult.Count -gt 0 -and [string]$lastStageResult[0].status -eq "FAIL" -and -not $AllowRerun) {
    throw "Stage '$StageId' previously failed. Fix the root cause and rerun with -AllowRerun."
}

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss_fff"
$logPath = Join-Path $logRoot "$StageId-$timestamp-$PID.log"
$startedAt = Get-Date
$commandExitCode = 1

Write-Host "START: $StageId"
Write-Host "WORKDIR: $workingDirectoryFull"
Write-Host "LOG: $logPath"

if ($preflightFailures.Count -eq 0) {
    Push-Location -LiteralPath $workingDirectoryFull
    try {
        $PSNativeCommandUseErrorActionPreference = $false
        & pwsh -NoProfile -Command $Command 2>&1 |
            Tee-Object -FilePath $logPath
        $commandExitCode = if ($null -eq $LASTEXITCODE) { 1 } else { [int]$LASTEXITCODE }
    } catch {
        $commandExitCode = 1
        $_ | Out-String | Add-Content -LiteralPath $logPath -Encoding utf8
    } finally {
        Pop-Location
    }
} else {
    $commandExitCode = 87
    New-Item -ItemType File -Path $logPath -Force | Out-Null
}

if (-not (Test-Path -LiteralPath $logPath -PathType Leaf)) {
    New-Item -ItemType File -Path $logPath -Force | Out-Null
}

$validationFailures = [Collections.Generic.List[string]]::new()
foreach ($preflightFailure in $preflightFailures) {
    $validationFailures.Add($preflightFailure)
}
$gitHeadAfter = ""
$worktreeFingerprintAfter = ""
try {
    $gitHeadAfter = Get-GitHead -RepositoryRoot $repositoryRoot
    $worktreeFingerprintAfter = Get-WorktreeFingerprint -RepositoryRoot $repositoryRoot -GitHead $gitHeadAfter
} catch {
    $validationFailures.Add("Unable to compute the post-command Git source fingerprint.")
}

$sourceSelfDirty = (
    $gitHeadAfter -cne $gitHead -or
    $worktreeFingerprintAfter -cne $worktreeFingerprint
)
if ($sourceSelfDirty) {
    $validationFailures.Add("The command changed Git HEAD or non-ignored worktree source content.")
}

$artifactHash = ""
$artifactKind = ""
if ($artifactRequiredEffective -and -not [string]::IsNullOrWhiteSpace($artifactPathFull)) {
    $artifactEvidence = Get-ArtifactEvidence -Path $artifactPathFull
    if ($null -eq $artifactEvidence) {
        $validationFailures.Add("Required artifact file or directory is missing: $artifactPathFull")
    } else {
        $artifactKind = [string]$artifactEvidence.Kind
        $artifactHash = [string]$artifactEvidence.Sha256
    }
}

if ($validationFailures.Count -gt 0) {
    foreach ($failure in $validationFailures) {
        $message = "CPF_STAGE_VALIDATION_FAILURE: $failure"
        Add-Content -LiteralPath $logPath -Value $message -Encoding utf8
        Write-Warning $message
    }
    if ($commandExitCode -eq 0) {
        $commandExitCode = 86
    }
}

$endedAt = Get-Date
$logHash = (Get-FileHash -LiteralPath $logPath -Algorithm SHA256).Hash.ToLowerInvariant()
$status = if ($commandExitCode -eq 0) { "PASS" } else { "FAIL" }

$row = [pscustomobject][ordered]@{
    stageId = $StageId
    status = $status
    startedAt = $startedAt.ToString("o")
    endedAt = $endedAt.ToString("o")
    durationSeconds = [Math]::Round(($endedAt - $startedAt).TotalSeconds, 3)
    exitCode = $commandExitCode
    gitHead = $gitHead
    gitHeadAfter = $gitHeadAfter
    workingDirectory = $workingDirectoryFull
    commandHash = $commandHash
    worktreeFingerprint = $worktreeFingerprint
    worktreeFingerprintAfter = $worktreeFingerprintAfter
    sourceSelfDirty = $sourceSelfDirty.ToString().ToLowerInvariant()
    databaseVendor = $databaseVendorNormalized
    environmentFingerprint = $environmentFingerprintHash
    environmentFingerprintExplicit = $environmentFingerprintExplicit.ToString().ToLowerInvariant()
    explicitEnvironmentRequired = $explicitEnvironmentRequired.ToString().ToLowerInvariant()
    logPath = $logPath
    logSha256 = $logHash
    artifactRequired = $artifactRequiredEffective.ToString().ToLowerInvariant()
    artifactPath = $artifactPathFull
    artifactKind = $artifactKind
    artifactSha256 = $artifactHash
}

$canonicalRows = [Collections.Generic.List[object]]::new()
foreach ($previousRow in $previous) {
    $canonicalRows.Add((Convert-ToCanonicalLedgerRow -InputRow $previousRow -Columns $effectiveLedgerColumns))
}
$canonicalRows.Add((Convert-ToCanonicalLedgerRow -InputRow $row -Columns $effectiveLedgerColumns))

$ledgerTempPath = "$ledgerPath.tmp-$PID"
try {
    $canonicalRows | Export-Csv -LiteralPath $ledgerTempPath -NoTypeInformation -Encoding utf8
    Move-Item -LiteralPath $ledgerTempPath -Destination $ledgerPath -Force
} finally {
    if (Test-Path -LiteralPath $ledgerTempPath -PathType Leaf) {
        Remove-Item -LiteralPath $ledgerTempPath -Force
    }
}

Write-Host "END: $StageId status=$status exit=$commandExitCode"
Write-Host "LEDGER: $ledgerPath"
Write-Host "LOG SHA-256: $logHash"

exit $commandExitCode
