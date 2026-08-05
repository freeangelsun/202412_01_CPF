param(
    [Parameter(Mandatory = $true)]
    [string] $DomainPath,
    [string] $Root = "",
    [switch] $Apply
)

$ErrorActionPreference = 'Stop'
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
if ([string]::IsNullOrWhiteSpace($Root)) {
    $Root = (Resolve-Path "$PSScriptRoot\..\..").Path
} else {
    $Root = (Resolve-Path -LiteralPath $Root).Path
}
$domainRoot = if ([IO.Path]::IsPathRooted($DomainPath)) {
    [IO.Path]::GetFullPath($DomainPath)
} else {
    [IO.Path]::GetFullPath((Join-Path $Root $DomainPath))
}
$manifestPath = Join-Path $domainRoot 'manifest/domain-manifest.json'
$ownershipPath = Join-Path $domainRoot 'manifest/generator-ownership.json'
if (-not (Test-Path -LiteralPath $manifestPath -PathType Leaf)) {
    throw "Generated Domain manifest가 없습니다: $manifestPath"
}
if (-not (Test-Path -LiteralPath $ownershipPath -PathType Leaf)) {
    throw "Generator ownership manifest가 없습니다: $ownershipPath"
}
$manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
$ownership = Get-Content -LiteralPath $ownershipPath -Raw -Encoding UTF8 | ConvertFrom-Json
$projectName = [string]$manifest.projectName
if ([string]::IsNullOrWhiteSpace($projectName) -or
        -not [IO.Path]::GetFullPath((Join-Path $Root $projectName)).Equals($domainRoot, [StringComparison]::OrdinalIgnoreCase)) {
    throw "DomainPath와 manifest.projectName이 일치하지 않습니다."
}

function Convert-ToYn([object] $Value) { if ([bool]$Value) { 'Y' } else { 'N' } }
function Get-Sha([string] $Path) {
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) { return $null }
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}
function Get-SafeOwnedRelativePath([string] $Value) {
    if ([string]::IsNullOrWhiteSpace($Value) -or $Value -ne $Value.Trim()) {
        throw "Generator ownership manifest contains an invalid relative path."
    }
    if ($Value.Contains('\') -or [IO.Path]::IsPathRooted($Value) -or
            $Value.StartsWith('/') -or $Value.EndsWith('/') -or $Value.Contains('//') -or
            $Value.Contains(':') -or $Value.Contains([char]0)) {
        throw "Generator ownership manifest contains a non-canonical or rooted path: $Value"
    }
    $segments = @($Value.Split('/'))
    if ($segments.Count -eq 0 -or @($segments | Where-Object {
                [string]::IsNullOrWhiteSpace($_) -or $_ -eq '.' -or $_ -eq '..' -or
                $_ -ne $_.Trim() -or $_.EndsWith('.')
            }).Count -gt 0) {
        throw "Generator ownership manifest contains an unsafe path segment: $Value"
    }
    $fullPath = [IO.Path]::GetFullPath((Join-Path $domainRoot $Value))
    $separator = [string][IO.Path]::DirectorySeparatorChar
    $domainPrefix = $domainRoot.TrimEnd([char[]]@([IO.Path]::DirectorySeparatorChar, [IO.Path]::AltDirectorySeparatorChar)) + $separator
    if (-not $fullPath.StartsWith($domainPrefix, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Generator ownership path escapes the generated domain root: $Value"
    }
    return $Value
}
function Get-OwnedTargetPath([string] $RelativePath) {
    $safeRelativePath = Get-SafeOwnedRelativePath $RelativePath
    return [IO.Path]::GetFullPath((Join-Path $domainRoot $safeRelativePath))
}
function Assert-Sha256([string] $Value, [string] $RelativePath) {
    if ([string]::IsNullOrWhiteSpace($Value) -or $Value -notmatch '^[0-9A-Fa-f]{64}$') {
        throw "Generator ownership manifest contains an invalid SHA-256 for path: $RelativePath"
    }
}

$oldOwned = @{}
foreach ($entry in @($ownership.createdFiles)) {
    $entryPath = Get-SafeOwnedRelativePath ([string]$entry.path)
    $entrySha = [string]$entry.sha256
    Assert-Sha256 $entrySha $entryPath
    if ($oldOwned.ContainsKey($entryPath)) {
        throw "Generator ownership manifest contains a duplicate path: $entryPath"
    }
    $oldOwned[$entryPath] = $entrySha.ToLowerInvariant()
}

$providerPairs = @($manifest.providerBindings.PSObject.Properties | ForEach-Object {
    "$($_.Name)=$([string]$_.Value)"
})
$capabilities = @($manifest.resolvedCapabilityGroups | ForEach-Object { [string]$_ })
$exceptionRegistry = Join-Path $domainRoot 'config/cpf-approved-exceptions.csv'
if (-not (Test-Path -LiteralPath $exceptionRegistry -PathType Leaf)) {
    throw "승인 예외 Registry가 없습니다: $exceptionRegistry"
}

$tempRoot = Join-Path $Root "build/domain-upgrade/$projectName/$([guid]::NewGuid().ToString('N'))"
New-Item -ItemType Directory -Force -Path $tempRoot | Out-Null
try {
    $generator = Join-Path $Root 'cpf-tools/generator/create-domain.ps1'
    & pwsh -NoProfile -ExecutionPolicy Bypass -File $generator `
        -Root $Root `
        -DomainName ([string]$manifest.domainName) `
        -SystemCode ([string]$manifest.systemCode) `
        -ModuleName ([string]$manifest.moduleName) `
        -DomainIdCode ([string]$manifest.domainIdCode) `
        -PackageName ([string]$manifest.packageName) `
        -SchemaName ([string]$manifest.schemaName) `
        -TablePrefix ([string]$manifest.tablePrefix) `
        -Port ([int]$manifest.port) `
        -Online (Convert-ToYn $manifest.onlineEnabled) `
        -Database (Convert-ToYn $manifest.databaseEnabled) `
        -DatabaseVendor ([string]$manifest.databaseVendor) `
        -DependencyModel ([string]$manifest.dependencyModel) `
        -PlatformVersion ([string]$manifest.platformVersion) `
        -CapabilityProfile ([string]$manifest.capabilityProfile) `
        -Capabilities ($capabilities -join ',') `
        -ProviderBindings ($providerPairs -join ',') `
        -Batch (Convert-ToYn $manifest.batchEnabled) `
        -CenterCut (Convert-ToYn $manifest.centerCutEnabled) `
        -External (Convert-ToYn $manifest.externalEnabled) `
        -Messaging (Convert-ToYn $manifest.messagingEnabled) `
        -File (Convert-ToYn $manifest.fileEnabled) `
        -SecurityAudit (Convert-ToYn $manifest.securityAuditEnabled) `
        -Ui (Convert-ToYn $manifest.uiEnabled) `
        -BzaMenu (Convert-ToYn $manifest.bzaMenuEnabled) `
        -ProductionProfile (Convert-ToYn $manifest.productionProfileEnabled) `
        -ApprovedExceptionRegistry $exceptionRegistry `
        -TargetEnvironment ([string]$manifest.targetEnvironment) `
        -UpgradeSourceDomainPath $domainRoot `
        -OutputDir $tempRoot `
        -AllowReserved
    if ($LASTEXITCODE -ne 0) {
        throw "Generated Domain upgrade candidate 생성이 실패했습니다. exitCode=$LASTEXITCODE"
    }

    # Only explicitly declared customer-owned files are preserved. Java/UI files emitted by
    # create-domain.ps1 remain generator-managed when they are present in createdFiles. Treating
    # whole source trees as user-owned prevents template/security fixes from ever reaching an
    # existing generated domain and makes upgrade results depend on the first generator version.
    $userOwnedFiles = @('README.md', 'config/cpf-approved-exceptions.csv')
    $candidateFiles = @(Get-ChildItem -LiteralPath $tempRoot -Recurse -File)
    $candidateRelativePaths = @($candidateFiles | ForEach-Object {
        $_.FullName.Substring($tempRoot.Length).TrimStart('\','/').Replace('\','/')
    } | Sort-Object -Unique)
    $candidatePathSet = @{}
    foreach ($candidateRelativePath in $candidateRelativePaths) {
        $candidatePathSet[$candidateRelativePath] = $true
    }

    $operations = [System.Collections.Generic.List[object]]::new()
    $conflicts = [System.Collections.Generic.List[string]]::new()
    $preserved = [System.Collections.Generic.List[string]]::new()
    $managedCandidatePaths = [System.Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)

    foreach ($candidate in $candidateFiles) {
        $relative = $candidate.FullName.Substring($tempRoot.Length).TrimStart('\','/').Replace('\','/')
        if ($relative -eq 'manifest/generator-ownership.json') { continue }
        $target = Get-OwnedTargetPath $relative

        if ($relative -in $userOwnedFiles) {
            if (Test-Path -LiteralPath $target -PathType Leaf) {
                $preserved.Add($relative)
                continue
            }
            $operations.Add([ordered]@{ action='ADD_USER_OWNED_DEFAULT'; path=$relative; source=$candidate.FullName; target=$target })
            continue
        }

        $ownedSha = if ($oldOwned.ContainsKey($relative)) { [string]$oldOwned[$relative] } else { $null }
        if (-not (Test-Path -LiteralPath $target -PathType Leaf)) {
            if (-not [string]::IsNullOrWhiteSpace($ownedSha)) {
                $conflicts.Add("managed file missing: $relative")
                continue
            }
            $operations.Add([ordered]@{ action='ADD'; path=$relative; source=$candidate.FullName; target=$target })
            [void]$managedCandidatePaths.Add($relative)
            continue
        }

        if ([string]::IsNullOrWhiteSpace($ownedSha)) {
            $conflicts.Add("unmanaged target collision: $relative")
            continue
        }

        $currentSha = Get-Sha $target
        if ($currentSha -ne $ownedSha) {
            $conflicts.Add("managed file drift: $relative")
            continue
        }

        [void]$managedCandidatePaths.Add($relative)
        $candidateSha = Get-Sha $candidate.FullName
        if ($currentSha -ne $candidateSha) {
            $operations.Add([ordered]@{ action='UPDATE'; path=$relative; source=$candidate.FullName; target=$target })
        }
    }

    $obsolete = [System.Collections.Generic.List[string]]::new()
    $retainedObsoletePaths = [System.Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
    foreach ($ownedPath in @($oldOwned.Keys | Sort-Object)) {
        if ($ownedPath -eq 'manifest/generator-ownership.json' -or $candidatePathSet.ContainsKey($ownedPath)) {
            continue
        }
        $obsolete.Add($ownedPath)
        $target = Get-OwnedTargetPath $ownedPath
        if (-not (Test-Path -LiteralPath $target -PathType Leaf)) {
            continue
        }
        $currentSha = Get-Sha $target
        if ($currentSha -ne [string]$oldOwned[$ownedPath]) {
            $conflicts.Add("obsolete managed file drift: $ownedPath")
            continue
        }
        # Obsolete generated files are not auto-deleted. Retain ownership until an approved exact
        # delete manifest removes them, otherwise later removal could mistake them for user files.
        [void]$retainedObsoletePaths.Add($ownedPath)
    }

    $report = [ordered]@{
        schemaVersion = '1.1'
        projectName = $projectName
        apply = [bool]$Apply
        generatedAt = (Get-Date).ToString('o')
        operations = @($operations | ForEach-Object { [ordered]@{ action=$_.action; path=$_.path } })
        preservedUserFiles = @($preserved | Sort-Object -Unique)
        managedConflicts = @($conflicts | Sort-Object -Unique)
        obsoleteGeneratedPaths = @($obsolete)
        retainedObsoleteOwnership = @($retainedObsoletePaths | Sort-Object)
        deletionPolicy = 'NOT_EXECUTED; exact paths require user approval and Delete Manifest.'
    }
    $reportPath = Join-Path $Root "build/reports/domain-upgrade/$projectName/upgrade-domain-result.json"
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $reportPath) | Out-Null
    [IO.File]::WriteAllText($reportPath, ($report | ConvertTo-Json -Depth 20), $Utf8NoBom)

    if ($conflicts.Count -gt 0) {
        throw "Generated Domain managed files have drift or ownership collisions. No file was changed. report=$reportPath"
    }
    if (-not $Apply) {
        $report | ConvertTo-Json -Depth 20
        return
    }

    foreach ($operation in $operations) {
        New-Item -ItemType Directory -Force -Path (Split-Path -Parent $operation.target) | Out-Null
        Copy-Item -LiteralPath $operation.source -Destination $operation.target -Force
    }

    $newOwnedFiles = @()
    foreach ($candidateRelative in @($managedCandidatePaths | Sort-Object)) {
        $target = Get-OwnedTargetPath $candidateRelative
        if (-not (Test-Path -LiteralPath $target -PathType Leaf)) {
            throw "Managed candidate file is missing after upgrade apply: $candidateRelative"
        }
        $newOwnedFiles += [ordered]@{ path=$candidateRelative; sha256=(Get-Sha $target) }
    }
    foreach ($obsoleteRelative in @($retainedObsoletePaths | Sort-Object)) {
        $target = Get-OwnedTargetPath $obsoleteRelative
        $newOwnedFiles += [ordered]@{ path=$obsoleteRelative; sha256=(Get-Sha $target) }
    }
    $ownership.generatorVersion = '3.2'
    $ownership.standardInheritancePolicyVersion = '1.0'
    $ownership.upgradedAt = (Get-Date).ToString('o')
    $ownership.createdFiles = $newOwnedFiles
    $ownership.preservedUserFiles = @($preserved | Sort-Object -Unique)
    $ownership.obsoleteGeneratedPaths = $obsolete
    [IO.File]::WriteAllText($ownershipPath, ($ownership | ConvertTo-Json -Depth 30), $Utf8NoBom)
    Write-Host "Generated Domain upgrade applied. project=$projectName, updated=$($operations.Count), preserved=$($preserved.Count), obsolete=$($obsolete.Count)"
} finally {
    Remove-Item -LiteralPath $tempRoot -Recurse -Force -ErrorAction SilentlyContinue
}
