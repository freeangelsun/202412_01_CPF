[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [string]$RepositoryRoot,

    [Parameter(Mandatory = $true)]
    [string]$OverlayRoot,

    [Parameter(Mandatory = $true)]
    [string]$SourceZipPath,

    [Parameter(Mandatory = $true)]
    [string]$CommitMessage,

    [Parameter(Mandatory = $true)]
    [switch]$ApproveAllCurrentChanges
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Get-NormalizedRelativePath {
    param([Parameter(Mandatory = $true)][string]$PathText)
    $value = $PathText.Replace('\', '/').Trim()
    if ([string]::IsNullOrWhiteSpace($value) -or
        [System.IO.Path]::IsPathRooted($value) -or
        $value -match '(^|/)\.\.(/|$)') {
        throw "Unsafe repository-relative path: $PathText"
    }
    return $value
}

function Invoke-Git {
    param(
        [Parameter(Mandatory = $true)][string[]]$Arguments,
        [switch]$AllowFailure
    )
    $output = @(& git -C $script:Repository @Arguments 2>&1)
    $exitCode = $LASTEXITCODE
    if (-not $AllowFailure -and $exitCode -ne 0) {
        throw "git $($Arguments -join ' ') failed ($exitCode): $($output -join [Environment]::NewLine)"
    }
    return [pscustomobject]@{
        ExitCode = $exitCode
        Output = $output
        Text = ($output -join [Environment]::NewLine).Trim()
    }
}

function Get-RelativePathFromRoot {
    param(
        [Parameter(Mandatory = $true)][string]$Root,
        [Parameter(Mandatory = $true)][string]$FullPath
    )
    return [System.IO.Path]::GetRelativePath($Root, $FullPath).Replace('\', '/')
}

if (-not $ApproveAllCurrentChanges) {
    throw 'This script commits all current repository changes. Pass -ApproveAllCurrentChanges explicitly.'
}

$script:Repository = (Resolve-Path -LiteralPath $RepositoryRoot).Path
$overlay = (Resolve-Path -LiteralPath $OverlayRoot).Path
$zipPath = (Resolve-Path -LiteralPath $SourceZipPath).Path

if (-not (Test-Path -LiteralPath (Join-Path $script:Repository '.git'))) {
    throw "Not a Git repository root: $script:Repository"
}

$manifestRelative = 'cpf-docs/work/repository-consolidation/20260802/PACKAGE_MANIFEST.json'
$hashRelative = 'cpf-docs/work/repository-consolidation/20260802/FILES.sha256'
$deleteRelative = 'cpf-docs/work/repository-consolidation/20260802/DELETE_MANIFEST.txt'

$manifestPath = Join-Path $overlay $manifestRelative.Replace('/', [System.IO.Path]::DirectorySeparatorChar)
$hashPath = Join-Path $overlay $hashRelative.Replace('/', [System.IO.Path]::DirectorySeparatorChar)
$deletePath = Join-Path $overlay $deleteRelative.Replace('/', [System.IO.Path]::DirectorySeparatorChar)

foreach ($required in @($manifestPath, $hashPath, $deletePath)) {
    if (-not (Test-Path -LiteralPath $required -PathType Leaf)) {
        throw "Required package file is missing: $required"
    }
}

$manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
$baselineSha = [string]$manifest.baseline.sha
if ($baselineSha -notmatch '^[0-9a-f]{40}$') {
    throw "Invalid baseline SHA in package manifest: $baselineSha"
}

$branch = (Invoke-Git -Arguments @('rev-parse', '--abbrev-ref', 'HEAD')).Text
if ($branch -ne 'master') {
    throw "This package may only be applied on master. Current branch: $branch"
}

$remoteUrl = (Invoke-Git -Arguments @('remote', 'get-url', 'origin')).Text
if ($remoteUrl -notmatch 'freeangelsun/202412_01_CPF(?:\.git)?$') {
    throw "Unexpected origin remote: $remoteUrl"
}

Invoke-Git -Arguments @('fetch', 'origin', 'master') | Out-Null

$baselineAncestor = Invoke-Git -Arguments @('merge-base', '--is-ancestor', $baselineSha, 'HEAD') -AllowFailure
if ($baselineAncestor.ExitCode -ne 0) {
    throw "Package baseline $baselineSha is not an ancestor of local HEAD."
}

$remoteAncestor = Invoke-Git -Arguments @('merge-base', '--is-ancestor', 'origin/master', 'HEAD') -AllowFailure
if ($remoteAncestor.ExitCode -ne 0) {
    throw 'origin/master contains commits not present in local HEAD. Reconcile normally before applying; no automatic pull/reset is performed.'
}

$hashEntries = New-Object System.Collections.Generic.List[object]
$allowedRelativePaths = New-Object 'System.Collections.Generic.HashSet[string]' ([System.StringComparer]::OrdinalIgnoreCase)

foreach ($line in Get-Content -LiteralPath $hashPath -Encoding UTF8) {
    if ([string]::IsNullOrWhiteSpace($line)) { continue }
    if ($line -notmatch '^([0-9a-fA-F]{64})  (.+)$') {
        throw "Invalid hash line: $line"
    }
    $expectedHash = $Matches[1].ToLowerInvariant()
    $relative = Get-NormalizedRelativePath -PathText $Matches[2]
    $source = Join-Path $overlay $relative.Replace('/', [System.IO.Path]::DirectorySeparatorChar)
    if (-not (Test-Path -LiteralPath $source -PathType Leaf)) {
        throw "Hashed package file is missing: $relative"
    }
    $actualHash = (Get-FileHash -LiteralPath $source -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($actualHash -ne $expectedHash) {
        throw "Package hash mismatch: $relative"
    }
    [void]$allowedRelativePaths.Add($relative)
    $hashEntries.Add([pscustomobject]@{
        Relative = $relative
        Source = $source
        Hash = $expectedHash
    })
}

$hashRelative = Get-NormalizedRelativePath -PathText $hashRelative
[void]$allowedRelativePaths.Add($hashRelative)
$hashEntries.Add([pscustomobject]@{
    Relative = $hashRelative
    Source = $hashPath
    Hash = (Get-FileHash -LiteralPath $hashPath -Algorithm SHA256).Hash.ToLowerInvariant()
})

$extraPackageFiles = @()
foreach ($file in Get-ChildItem -LiteralPath $overlay -File -Recurse) {
    $relative = Get-RelativePathFromRoot -Root $overlay -FullPath $file.FullName
    if (-not $allowedRelativePaths.Contains($relative)) {
        $extraPackageFiles += $relative
    }
}
if ($extraPackageFiles.Count -gt 0) {
    throw "Package contains unhashed files: $($extraPackageFiles -join ', ')"
}

$deleteCandidates = New-Object System.Collections.Generic.List[string]
foreach ($line in Get-Content -LiteralPath $deletePath -Encoding UTF8) {
    $value = $line.Trim()
    if ([string]::IsNullOrWhiteSpace($value) -or $value.StartsWith('#')) { continue }
    $relative = Get-NormalizedRelativePath -PathText $value
    if ($allowedRelativePaths.Contains($relative)) {
        throw "Delete manifest overlaps overlay file: $relative"
    }
    $deleteCandidates.Add($relative)
}

$conflicts = New-Object System.Collections.Generic.List[string]
foreach ($entry in $hashEntries) {
    $relative = $entry.Relative
    $target = Join-Path $script:Repository $relative.Replace('/', [System.IO.Path]::DirectorySeparatorChar)
    $status = Invoke-Git -Arguments @('status', '--porcelain=v1', '--', $relative)
    if (-not [string]::IsNullOrWhiteSpace($status.Text)) {
        if ((Test-Path -LiteralPath $target -PathType Leaf) -and
            ((Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash.ToLowerInvariant() -eq $entry.Hash)) {
            continue
        }
        $conflicts.Add($relative)
    }
}

$missingDeleteCandidates = New-Object System.Collections.Generic.List[string]
foreach ($relative in $deleteCandidates) {
    $target = Join-Path $script:Repository $relative.Replace('/', [System.IO.Path]::DirectorySeparatorChar)
    if (-not (Test-Path -LiteralPath $target -PathType Leaf)) {
        # Another approved shell may already have deleted the exact candidate.
        # Missing candidates are idempotent cleanup success and must not block apply/commit/push.
        $missingDeleteCandidates.Add($relative)
        continue
    }

    $status = Invoke-Git -Arguments @('status', '--porcelain=v1', '--', $relative)
    if (-not [string]::IsNullOrWhiteSpace($status.Text)) {
        # Only an existing candidate with local edits is a conflict.
        $conflicts.Add($relative)
    }
}

if ($conflicts.Count -gt 0) {
    $uniqueConflicts = $conflicts | Sort-Object -Unique
    throw "Codex/current work conflicts with overlay or deletion targets. Nothing was changed.`n$($uniqueConflicts -join [Environment]::NewLine)"
}

$timestamp = Get-Date -Format 'yyyyMMdd_HHmmss'
$backupRoot = Join-Path $env:TEMP "cpf-final-overlay-backup-$timestamp"
New-Item -ItemType Directory -Path $backupRoot -Force | Out-Null

$backupPaths = @($hashEntries.Relative) + @($deleteCandidates)
foreach ($relative in ($backupPaths | Sort-Object -Unique)) {
    $target = Join-Path $script:Repository $relative.Replace('/', [System.IO.Path]::DirectorySeparatorChar)
    if (Test-Path -LiteralPath $target -PathType Leaf) {
        $backupTarget = Join-Path $backupRoot $relative.Replace('/', [System.IO.Path]::DirectorySeparatorChar)
        New-Item -ItemType Directory -Path (Split-Path -Parent $backupTarget) -Force | Out-Null
        Copy-Item -LiteralPath $target -Destination $backupTarget -Force
    }
}

foreach ($entry in $hashEntries) {
    $target = Join-Path $script:Repository $entry.Relative.Replace('/', [System.IO.Path]::DirectorySeparatorChar)
    New-Item -ItemType Directory -Path (Split-Path -Parent $target) -Force | Out-Null
    Copy-Item -LiteralPath $entry.Source -Destination $target -Force
}

$deletedCount = 0
$skippedMissingCount = 0
foreach ($relative in $deleteCandidates) {
    $target = Join-Path $script:Repository $relative.Replace('/', [System.IO.Path]::DirectorySeparatorChar)
    if (Test-Path -LiteralPath $target -PathType Leaf) {
        Remove-Item -LiteralPath $target -Force
        $deletedCount++
        Write-Host "[DELETE] $relative"
    } else {
        $skippedMissingCount++
        Write-Host "[SKIP_MISSING] $relative"
    }
}

Invoke-Git -Arguments @('diff', '--check') | Out-Null
Invoke-Git -Arguments @('diff', '--cached', '--check') | Out-Null

$changedPaths = New-Object 'System.Collections.Generic.HashSet[string]' ([System.StringComparer]::OrdinalIgnoreCase)
foreach ($args in @(
    @('diff', '--name-only'),
    @('diff', '--cached', '--name-only'),
    @('ls-files', '--others', '--exclude-standard')
)) {
    $result = Invoke-Git -Arguments $args
    foreach ($item in $result.Output) {
        if (-not [string]::IsNullOrWhiteSpace([string]$item)) {
            [void]$changedPaths.Add(([string]$item).Replace('\', '/').Trim())
        }
    }
}

$zipInsideRepository = $null
try {
    $zipRelativeCandidate = Get-RelativePathFromRoot -Root $script:Repository -FullPath $zipPath
    if ($zipRelativeCandidate -notmatch '(^|/)\.\.(/|$)' -and
        -not [System.IO.Path]::IsPathRooted($zipRelativeCandidate)) {
        $zipInsideRepository = $zipRelativeCandidate
        [void]$changedPaths.Remove($zipInsideRepository)
    }
} catch {
    $zipInsideRepository = $null
}

$suspicious = New-Object System.Collections.Generic.List[string]
$oversized = New-Object System.Collections.Generic.List[string]
foreach ($relative in $changedPaths) {
    if ($relative -match '(?i)(^|/)(\.env(?:\.|$)|id_rsa(?:\.|$)|credentials?\.json$|.*\.(?:pem|p12|pfx|key)$)') {
        $suspicious.Add($relative)
    }
    $full = Join-Path $script:Repository $relative.Replace('/', [System.IO.Path]::DirectorySeparatorChar)
    if ((Test-Path -LiteralPath $full -PathType Leaf) -and ((Get-Item -LiteralPath $full).Length -gt 90MB)) {
        $oversized.Add($relative)
    }
}
if ($suspicious.Count -gt 0) {
    throw "Potential secret files detected; no commit was made: $($suspicious -join ', ')"
}
if ($oversized.Count -gt 0) {
    throw "Files larger than 90MB detected; no commit was made: $($oversized -join ', ')"
}

if ($null -ne $zipInsideRepository) {
    Invoke-Git -Arguments @('add', '-A', '--', '.', ":(exclude)$zipInsideRepository") | Out-Null
} else {
    Invoke-Git -Arguments @('add', '-A', '--', '.') | Out-Null
}

Invoke-Git -Arguments @('diff', '--cached', '--check') | Out-Null
$hasStagedChanges = Invoke-Git -Arguments @('diff', '--cached', '--quiet') -AllowFailure
if ($hasStagedChanges.ExitCode -eq 0) {
    throw 'There are no staged changes to commit.'
}
if ($hasStagedChanges.ExitCode -ne 1) {
    throw 'Unable to determine staged change state.'
}

Invoke-Git -Arguments @('commit', '-m', $CommitMessage) | Out-Null
$newHead = (Invoke-Git -Arguments @('rev-parse', 'HEAD')).Text

try {
    Invoke-Git -Arguments @('push', 'origin', 'master') | Out-Null
} catch {
    Write-Warning "Commit succeeded but push failed. Local commit: $newHead"
    Write-Warning "Exact-path backup: $backupRoot"
    throw
}

Write-Host "CPF final overlay applied, committed, and pushed."
Write-Host "Commit: $newHead"
Write-Host "Backup: $backupRoot"
Write-Host "Delete candidates removed now: $deletedCount"
Write-Host "Delete candidates already missing and skipped: $skippedMissingCount"
if ($null -ne $zipInsideRepository) {
    Write-Host "Source ZIP was intentionally excluded from Git staging: $zipInsideRepository"
}
