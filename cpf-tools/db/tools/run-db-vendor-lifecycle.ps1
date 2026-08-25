[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidateSet('mariadb', 'postgresql', 'oracle')]
    [string] $Vendor,
    [Parameter(Mandatory = $true)]
    [ValidateSet('FreshInstall', 'Upgrade', 'RollbackReapply')]
    [string] $Mode,
    [string] $Root = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path,
    [string] $ProfilePath = '',
    [string[]] $Modules = @(),
    [int[]] $MigrationVersion = @(),
    [int] $FromVersion = -1,
    [int] $ToVersion = -1,
    [ValidateSet('Static', 'Auto', 'Host', 'Docker')]
    [string] $ClientAdapter = 'Auto',
    [string] $DockerImage = 'cpf-full-development-test-runner:java25-node22-pwsh7.6.4-playwright1.62.0-integration1',
    [string] $DockerNetwork = 'cpf_default',
    [string] $LogDir = 'build/cpf-db-lifecycle',
    [string] $LifecyclePlanPath = '',
    [string] $ResultPath = '',
    [switch] $ConfirmExecute,
    [switch] $ConfirmApplicationsStopped,
    [switch] $ConfirmRollbackReady,
    [switch] $ConfirmPreCurrentFixture,
    [switch] $ConfirmCurrentMigrationApplied,
    [string] $ExpectedPlanSha256 = '',
    [string] $ExpectedRollbackPlanSha256 = '',
    [string] $ExpectedLifecyclePlanSha256 = '',
    [string[]] $BackupManifestPath = @(),
    [string] $Operator = '',
    [string] $Reason = '',
    [string] $ApprovalReference = '',
    [switch] $VerifierOwnedDisposable,
    [string] $VerifierRunId = ''
)

if ($PSVersionTable.PSVersion.Major -lt 7) {
    throw 'CPF DB vendor lifecycle harness requires pwsh 7 or later.'
}
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$rootPath = (Resolve-Path -LiteralPath $Root).Path
$stamp = Get-Date -Format 'yyyyMMdd_HHmmss_fff'

function Resolve-CpfOutputPath {
    param([string] $Path, [string] $DefaultPath)
    $candidate = if ([string]::IsNullOrWhiteSpace($Path)) { $DefaultPath } else { $Path }
    if (-not [IO.Path]::IsPathRooted($candidate)) { $candidate = Join-Path $rootPath $candidate }
    return [IO.Path]::GetFullPath($candidate)
}

$logRoot = Resolve-CpfOutputPath $LogDir (Join-Path $rootPath 'build/cpf-db-lifecycle')
[IO.Directory]::CreateDirectory($logRoot) | Out-Null
$planAbsolute = Resolve-CpfOutputPath $LifecyclePlanPath (Join-Path $logRoot "${Vendor}_${Mode}_${stamp}.plan.json")
$resultAbsolute = Resolve-CpfOutputPath $ResultPath (Join-Path $logRoot "${Vendor}_${Mode}_${stamp}.result.json")

function Get-CpfFileSha256 {
    param([Parameter(Mandatory = $true)][string] $Path)
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}

function Get-CpfRelativePath {
    param([Parameter(Mandatory = $true)][string] $Path)
    $relative = [IO.Path]::GetRelativePath($rootPath, [IO.Path]::GetFullPath($Path)).Replace('\', '/')
    if ($relative -eq '..' -or $relative.StartsWith('../', [StringComparison]::Ordinal)) {
        return "external/$([IO.Path]::GetFileName($Path))"
    }
    return $relative
}

function Resolve-CpfRepositoryPath {
    param(
        [Parameter(Mandatory = $true)][string] $RelativePath,
        [Parameter(Mandatory = $true)][string] $Label,
        [ValidateSet('Leaf', 'Container')][string] $PathType = 'Leaf'
    )
    if ([string]::IsNullOrWhiteSpace($RelativePath) -or [IO.Path]::IsPathRooted($RelativePath)) {
        throw "$Label must be a repository-relative path: $RelativePath"
    }
    $absolute = [IO.Path]::GetFullPath((Join-Path $rootPath $RelativePath))
    $prefix = $rootPath.TrimEnd([IO.Path]::DirectorySeparatorChar, [IO.Path]::AltDirectorySeparatorChar) + [IO.Path]::DirectorySeparatorChar
    if (-not $absolute.StartsWith($prefix, [StringComparison]::OrdinalIgnoreCase)) {
        throw "$Label resolves outside the repository: $RelativePath"
    }
    if (-not (Test-Path -LiteralPath $absolute -PathType $PathType)) {
        throw "$Label is missing: $RelativePath"
    }
    return $absolute
}

function Read-CpfJsonFile {
    param([string] $Path, [string] $Label)
    try {
        return Get-Content -LiteralPath $Path -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 100
    } catch {
        throw "$Label is invalid JSON: $($_.Exception.Message)"
    }
}

function Write-CpfJsonAtomic {
    param($Value, [string] $Path)
    $directory = Split-Path -Parent $Path
    if (-not [string]::IsNullOrWhiteSpace($directory)) { [IO.Directory]::CreateDirectory($directory) | Out-Null }
    $temporary = "$Path.$([Guid]::NewGuid().ToString('N')).tmp"
    try {
        [IO.File]::WriteAllText($temporary, ($Value | ConvertTo-Json -Depth 100) + "`n", $Utf8NoBom)
        [IO.File]::Move($temporary, $Path, $true)
    } finally {
        if (Test-Path -LiteralPath $temporary -PathType Leaf) {
            Remove-Item -LiteralPath $temporary -Force -ErrorAction SilentlyContinue
        }
    }
}

function Assert-CpfRequiredScalar {
    param([string] $Value, [string] $Name)
    if ([string]::IsNullOrWhiteSpace($Value) -or $Value -match '[\x00-\x1F\x7F]') {
        throw "$Name is required and must not contain control characters."
    }
    return $Value
}

function Get-CpfLifecycleRoot {
    param([string] $Pattern, [string] $Label)
    $relative = if ($Pattern.Contains('{logicalDatabase}')) {
        $Pattern.Substring(0, $Pattern.IndexOf('{logicalDatabase}', [StringComparison]::Ordinal)).TrimEnd('/', '\')
    } else {
        $Pattern.TrimEnd('/', '\')
    }
    return Resolve-CpfRepositoryPath $relative $Label Container
}

function Get-CpfMigrationCatalog {
    param([Parameter(Mandatory = $true)][string] $MigrationRoot)
    $items = [Collections.Generic.List[object]]::new()
    $declaredPaths = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
    $manifests = @(Get-ChildItem -LiteralPath $MigrationRoot -Recurse -File -Filter 'checksums.sha256' | Sort-Object FullName)
    if ($manifests.Count -eq 0) { throw "No checksum manifests were found below $(Get-CpfRelativePath $MigrationRoot)." }
    foreach ($checksumManifest in $manifests) {
        $declaredNames = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
        $declaredVersions = [Collections.Generic.HashSet[int]]::new()
        foreach ($line in Get-Content -LiteralPath $checksumManifest.FullName -Encoding UTF8) {
            if ([string]::IsNullOrWhiteSpace($line)) { continue }
            if ($line -notmatch '^(?<sha>[0-9a-fA-F]{64})\s+\*?(?<name>V(?<version>[0-9]+)__.+\.sql)$') {
                throw "Invalid checksum manifest entry: manifest=$(Get-CpfRelativePath $checksumManifest.FullName)"
            }
            $name = [string]$Matches.name
            $version = [int]$Matches.version
            if (-not $declaredNames.Add($name) -or -not $declaredVersions.Add($version)) {
                throw "Duplicate migration file/version in checksum manifest: manifest=$(Get-CpfRelativePath $checksumManifest.FullName) version=$version"
            }
            $migrationPath = Join-Path $checksumManifest.Directory.FullName $name
            if (-not (Test-Path -LiteralPath $migrationPath -PathType Leaf)) {
                throw "Checksum manifest points to a missing migration: $(Get-CpfRelativePath $migrationPath)"
            }
            $migrationPath = [IO.Path]::GetFullPath($migrationPath)
            if (-not $declaredPaths.Add($migrationPath)) {
                throw "Migration is declared by more than one checksum manifest: $(Get-CpfRelativePath $migrationPath)"
            }
            $expectedSha = ([string]$Matches.sha).ToLowerInvariant()
            $actualSha = Get-CpfFileSha256 $migrationPath
            if ($actualSha -ne $expectedSha) { throw "Migration checksum mismatch: $(Get-CpfRelativePath $migrationPath)" }
            $packRelative = [IO.Path]::GetRelativePath($MigrationRoot, $checksumManifest.Directory.FullName).Replace('\', '/')
            $items.Add([pscustomobject]@{
                    pack = if ($packRelative -eq '.') { 'root' } else { $packRelative }
                    version = $version
                    path = Get-CpfRelativePath $migrationPath
                    checksumSha256 = $actualSha
                })
        }
        $diskNames = @(Get-ChildItem -LiteralPath $checksumManifest.Directory.FullName -File -Filter 'V*.sql' |
                Where-Object { $_.Name -match '^V[0-9]+__.+\.sql$' } | ForEach-Object { $_.Name })
        $missingDeclarations = @($diskNames | Where-Object { -not $declaredNames.Contains($_) })
        if ($missingDeclarations.Count -gt 0) {
            throw "Migrations are missing from checksum manifest $(Get-CpfRelativePath $checksumManifest.FullName): $($missingDeclarations -join ', ')"
        }
    }
    $undeclaredMigrations = @(
        Get-ChildItem -LiteralPath $MigrationRoot -Recurse -File -Filter 'V*.sql' |
            Where-Object {
                $_.Name -match '^V[0-9]+__.+\.sql$' -and
                -not $declaredPaths.Contains([IO.Path]::GetFullPath($_.FullName))
            } |
            ForEach-Object { Get-CpfRelativePath $_.FullName }
    )
    if ($undeclaredMigrations.Count -gt 0) {
        throw "Migrations below the official root are not declared by a checksum manifest: $($undeclaredMigrations -join ', ')"
    }
    if ($items.Count -eq 0) { throw "The migration catalog is empty below $(Get-CpfRelativePath $MigrationRoot)." }
    return @($items.ToArray() | Sort-Object version, pack, path)
}

function Get-CpfRollbackArtifact {
    param($Migration, [string] $RollbackRoot)
    $directory = if ($Migration.pack -eq 'root') { $RollbackRoot } else { Join-Path $RollbackRoot ([string]$Migration.pack) }
    if (-not (Test-Path -LiteralPath $directory -PathType Container)) {
        throw "Rollback pack is missing for migration $($Migration.path): $(Get-CpfRelativePath $directory)"
    }
    $version = [int]$Migration.version
    $matches = @(Get-ChildItem -LiteralPath $directory -File | Where-Object {
            $_.Name -match ("^(?:R|U){0}__.+\.sql$" -f $version) -or
            $_.Name -match ("^V{0}__.+_rollback\.sql$" -f $version)
        })
    if ($matches.Count -ne 1) {
        throw "Current-edge migration requires exactly one directory-owned rollback artifact: migration=$($Migration.path) count=$($matches.Count)"
    }
    return [pscustomobject]@{
        path = Get-CpfRelativePath $matches[0].FullName
        checksumSha256 = Get-CpfFileSha256 $matches[0].FullName
    }
}

function Get-CpfBackupManifestPlan {
    param([string[]] $Paths)
    $items = [Collections.Generic.List[object]]::new()
    foreach ($requested in @($Paths)) {
        $absolute = if ([IO.Path]::IsPathRooted($requested)) {
            [IO.Path]::GetFullPath($requested)
        } else {
            [IO.Path]::GetFullPath((Join-Path $rootPath $requested))
        }
        if (-not (Test-Path -LiteralPath $absolute -PathType Leaf)) { throw "Backup manifest is missing: $requested" }
        $items.Add([pscustomobject]@{ path = Get-CpfRelativePath $absolute; sha256 = Get-CpfFileSha256 $absolute })
    }
    return @($items.ToArray())
}

function Test-CpfHostClientAdapter {
    param([object[]] $Targets, [string] $ManifestClient)
    $missing = [Collections.Generic.List[string]]::new()
    foreach ($target in $Targets) {
        $configured = [string]$target.clientPath
        if (-not [string]::IsNullOrWhiteSpace($configured) -and (Test-Path -LiteralPath $configured -PathType Leaf)) { continue }
        if ($null -eq (Get-Command $ManifestClient -ErrorAction SilentlyContinue)) { $missing.Add([string]$target.moduleKey) }
    }
    return [pscustomobject]@{
        ready = $missing.Count -eq 0
        mode = 'Host'
        client = $ManifestClient
        missingModules = @($missing.ToArray())
    }
}

function Test-CpfDockerClientAdapter {
    param([string] $Image, [string] $ManifestClient)
    if ($ManifestClient -notmatch '^[a-z0-9._-]+$') { throw "Vendor manifest client command is invalid: $ManifestClient" }
    if ($null -eq (Get-Command docker -ErrorAction SilentlyContinue)) {
        return [pscustomobject]@{ ready = $false; mode = 'Docker'; client = $ManifestClient; image = $Image; imageId = ''; reason = 'DOCKER_CLI_MISSING' }
    }
    $imageId = @(& docker image inspect $Image --format '{{.Id}}' 2>$null)
    if ($LASTEXITCODE -ne 0 -or $imageId.Count -ne 1 -or [string]::IsNullOrWhiteSpace([string]$imageId[0])) {
        return [pscustomobject]@{ ready = $false; mode = 'Docker'; client = $ManifestClient; image = $Image; imageId = ''; reason = 'DOCKER_IMAGE_MISSING' }
    }
    # Capability-only probe: the container has no network, receives no profile and cannot reach a DB.
    & docker run --rm --network none $Image sh -lc "command -v $ManifestClient >/dev/null" *> $null
    if ($LASTEXITCODE -ne 0) {
        return [pscustomobject]@{ ready = $false; mode = 'Docker'; client = $ManifestClient; image = $Image; imageId = ([string]$imageId[0]); reason = 'DOCKER_CLIENT_MISSING' }
    }
    return [pscustomobject]@{
        ready = $true
        mode = 'Docker'
        client = $ManifestClient
        image = $Image
        imageId = ([string]$imageId[0]).ToLowerInvariant()
        probeNetwork = 'none'
    }
}

function Invoke-CpfMigrationDryRun {
    param(
        [ValidateSet('upgrade', 'rollback')][string] $Direction,
        [int[]] $Versions,
        [string] $OutputPath
    )
    $arguments = @{
        Root = $rootPath
        ProfilePath = $profileAbsolute
        Direction = $Direction
        MigrationVersion = @($Versions)
        Modules = @($selectedModuleKeys)
        ResultPath = $OutputPath
        DryRun = $true
    }
    & $migrationTool @arguments
    $child = Read-CpfJsonFile $OutputPath "Official $Direction migration plan result"
    if ([string]$child.status -cne '미검증' -or [string]$child.planSha256 -notmatch '^[0-9a-f]{64}$') {
        throw "Official $Direction migration planner did not produce a reviewed dry-run hash."
    }
    return $child
}

function Invoke-CpfMigrationHost {
    param(
        [ValidateSet('upgrade', 'rollback')][string] $Direction,
        [int[]] $Versions,
        [string] $ExpectedHash,
        [string] $OutputPath
    )
    $arguments = @{
        Root = $rootPath
        ProfilePath = $profileAbsolute
        Direction = $Direction
        MigrationVersion = @($Versions)
        Modules = @($selectedModuleKeys)
        ResultPath = $OutputPath
        Apply = $true
        ConfirmApply = $true
        ConfirmApplicationsStopped = $true
        ConfirmRollbackReady = $true
        ExpectedPlanSha256 = $ExpectedHash
        BackupManifestPath = @($BackupManifestPath)
        Operator = $Operator
        Reason = $Reason
        ApprovalReference = $ApprovalReference
        VerifierOwnedDisposable = [bool]$VerifierOwnedDisposable
        VerifierRunId = $VerifierRunId
    }
    & $migrationTool @arguments
}

function Get-CpfProfileEnvironmentNames {
    param($Profile)
    $names = [Collections.Generic.HashSet[string]]::new([StringComparer]::Ordinal)
    foreach ($moduleProperty in @($Profile.modules.PSObject.Properties)) {
        foreach ($role in @('admin', 'migration', 'runtime')) {
            $password = $moduleProperty.Value.$role.password
            if ($null -eq $password) { continue }
            foreach ($inlineName in @('value', 'devDefault')) {
                $inline = $password.PSObject.Properties[$inlineName]
                if ($null -ne $inline -and -not [string]::IsNullOrWhiteSpace([string]$inline.Value)) {
                    throw "Docker DB lifecycle profiles must use environment references, not inline secret values: module=$($moduleProperty.Name) role=$role"
                }
            }
            foreach ($propertyName in @('env', 'fallbackEnv')) {
                $property = $password.PSObject.Properties[$propertyName]
                if ($null -eq $property -or [string]::IsNullOrWhiteSpace([string]$property.Value)) { continue }
                $name = [string]$property.Value
                if ($name -notmatch '^[A-Za-z_][A-Za-z0-9_]*$') { throw "Invalid environment reference in DB profile: $name" }
                [void]$names.Add($name)
            }
        }
    }
    return @($names | Sort-Object)
}

function Invoke-CpfDockerLifecycle {
    param([string] $ExecutionRoot)
    $loopbackTargets = @($selectedTargets | Where-Object { ([string]$_.host).Trim().ToLowerInvariant() -in @('127.0.0.1', 'localhost', '::1') })
    if ($loopbackTargets.Count -gt 0) {
        throw 'Docker execution requires a dedicated container-addressed DB profile; loopback hosts are forbidden.'
    }
    [IO.Directory]::CreateDirectory($ExecutionRoot) | Out-Null
    $dockerProfile = $profileRaw | ConvertTo-Json -Depth 100 | ConvertFrom-Json -Depth 100
    foreach ($moduleProperty in @($dockerProfile.modules.PSObject.Properties)) {
        if (([string]$moduleProperty.Value.vendor).Trim().ToLowerInvariant() -eq $Vendor) {
            $moduleProperty.Value.clientPath = $manifestClient
        }
    }
    Write-CpfJsonAtomic $dockerProfile (Join-Path $ExecutionRoot 'profile.json')

    $backupContainerPaths = [Collections.Generic.List[string]]::new()
    $dockerArguments = [Collections.Generic.List[string]]::new()
    foreach ($argument in @(
            'run', '--rm', '--network', $DockerNetwork,
            '--mount', "type=bind,source=$rootPath,target=/workspace/cpf,readonly",
            '--mount', "type=bind,source=$ExecutionRoot,target=/workspace/result",
            '--workdir', '/workspace/cpf'
        )) { $dockerArguments.Add($argument) }

    $backupIndex = 0
    foreach ($requested in @($BackupManifestPath)) {
        $absolute = if ([IO.Path]::IsPathRooted($requested)) { [IO.Path]::GetFullPath($requested) } else { [IO.Path]::GetFullPath((Join-Path $rootPath $requested)) }
        $parent = Split-Path -Parent $absolute
        $target = "/workspace/backup/$backupIndex"
        $dockerArguments.Add('--mount')
        $dockerArguments.Add("type=bind,source=$parent,target=$target,readonly")
        $backupContainerPaths.Add("$target/$([IO.Path]::GetFileName($absolute))")
        $backupIndex++
    }
    foreach ($name in @(Get-CpfProfileEnvironmentNames $profileRaw)) {
        $dockerArguments.Add('--env')
        $dockerArguments.Add($name)
    }

    $request = [ordered]@{
        schemaVersion = 1
        action = $Mode
        root = '/workspace/cpf'
        profilePath = '/workspace/result/profile.json'
        resultRoot = '/workspace/result/output'
        migrationVersions = @($selectedVersions)
        modules = @($selectedModuleKeys)
        expectedUpgradePlanSha256 = $ExpectedPlanSha256.ToLowerInvariant()
        expectedRollbackPlanSha256 = $ExpectedRollbackPlanSha256.ToLowerInvariant()
        backupManifestPaths = @($backupContainerPaths.ToArray())
        operator = $Operator
        reason = $Reason
        approvalReference = $ApprovalReference
        verifierOwnedDisposable = [bool]$VerifierOwnedDisposable
        verifierRunId = $VerifierRunId
    }
    Write-CpfJsonAtomic $request (Join-Path $ExecutionRoot 'request.json')
    foreach ($argument in @(
            $DockerImage, 'pwsh', '-NoProfile', '-File',
            '/workspace/cpf/cpf-tools/db/tools/invoke-db-lifecycle-docker-client.ps1',
            '-RequestPath', '/workspace/result/request.json'
        )) { $dockerArguments.Add($argument) }
    $result.status = 'RUNNING'
    $result.executionStarted = $true
    $script:executionStarted = $true
    & docker @dockerArguments
    if ($LASTEXITCODE -ne 0) { throw "Docker DB lifecycle adapter failed: exit=$LASTEXITCODE" }
}

$result = [ordered]@{
    schemaVersion = 2
    tool = 'run-db-vendor-lifecycle.ps1'
    vendor = $Vendor
    mode = $Mode
    status = 'PLANNING'
    executionStarted = $false
    reconcileRequired = $false
    lifecyclePlan = Get-CpfRelativePath $planAbsolute
    lifecyclePlanSha256 = ''
    profileSha256 = ''
    adapter = $null
    startedAt = [DateTimeOffset]::UtcNow.ToString('o')
    finishedAt = $null
    approvalReference = ''
    error = ''
    sanitized = $true
    verifierOwnedDisposable = [bool]$VerifierOwnedDisposable
    verifierRunId = if($VerifierOwnedDisposable){$VerifierRunId}else{''}
}
$executionStarted = $false

try {
    $manifestPath = Resolve-CpfRepositoryPath 'cpf-tools/db/vendor-pack-manifest.json' 'Vendor pack manifest'
    $contractPath = Resolve-CpfRepositoryPath 'cpf-tools/db/cpf-db-lifecycle-contract.json' 'DB lifecycle contract'
    $intentCatalogPath = Resolve-CpfRepositoryPath 'cpf-tools/db/canonical/migration-intent-catalog.json' 'Migration intent catalog'
    $scenarioCatalogPath = Resolve-CpfRepositoryPath ([string](Read-CpfJsonFile $manifestPath 'Vendor pack manifest').lifecycleScenarioContract) 'Lifecycle scenario catalog'
    $manifest = Read-CpfJsonFile $manifestPath 'Vendor pack manifest'
    $contract = Read-CpfJsonFile $contractPath 'DB lifecycle contract'
    [void](Read-CpfJsonFile $intentCatalogPath 'Migration intent catalog')
    [void](Read-CpfJsonFile $scenarioCatalogPath 'Lifecycle scenario catalog')

    if ($Vendor -notin @($manifest.officialVendors) -or $Vendor -notin @($manifest.supportedVendors)) {
        throw "Vendor is not an official supported DB pack: $Vendor"
    }
    if ($Vendor -notin @($contract.officialVendors)) { throw "Vendor is not declared by the DB lifecycle contract: $Vendor" }
    if ([string]$contract.migrationDiscoveryPolicy.mode -cne 'ALL_CHECKSUM_MANIFEST_MIGRATIONS' -or
            -not [bool]$contract.migrationDiscoveryPolicy.hardCodedVersionAllowlistForbidden -or
            -not [bool]$contract.migrationDiscoveryPolicy.recursiveLogicalDatabasePacks) {
        throw 'DB lifecycle migration discovery policy is not manifest-driven/fail-closed.'
    }

    $entry = $manifest.vendors.$Vendor
    $vendorContract = $contract.vendorContracts.$Vendor
    if ($null -eq $entry -or $null -eq $entry.lifecycle -or $null -eq $vendorContract) {
        throw "Vendor lifecycle declaration is missing: $Vendor"
    }
    if ([string]$entry.lifecycle.migration -cne [string]$vendorContract.migrationRoot -or
            [string]$entry.lifecycle.rollback -cne [string]$vendorContract.rollbackRoot) {
        throw "Vendor lifecycle manifest/contract path drift: $Vendor"
    }
    $manifestClient = [string]$entry.client
    if ($manifestClient -notmatch '^[a-z0-9._-]+$') { throw "Vendor manifest client command is invalid: $manifestClient" }

    $freshArtifacts = [Collections.Generic.List[object]]::new()
    foreach ($declaration in @(
            [pscustomobject]@{ role = 'Provision'; key = 'provision' },
            [pscustomobject]@{ role = 'EmptyInstall'; key = 'emptyInstall' },
            [pscustomobject]@{ role = 'ProductSeed'; key = 'productSeed' },
            [pscustomobject]@{ role = 'Verify'; key = 'verify' }
        )) {
        $relative = [string]$entry.lifecycle.($declaration.key)
        $absolute = Resolve-CpfRepositoryPath $relative "$Vendor lifecycle $($declaration.role)"
        $freshArtifacts.Add([pscustomobject]@{
                role = $declaration.role
                path = Get-CpfRelativePath $absolute
                sha256 = Get-CpfFileSha256 $absolute
            })
    }

    if ([string]::IsNullOrWhiteSpace($ProfilePath)) { $ProfilePath = 'cpf-tools/db/config/database-install.default.json' }
    $profileAbsolute = if ([IO.Path]::IsPathRooted($ProfilePath)) {
        [IO.Path]::GetFullPath($ProfilePath)
    } else {
        [IO.Path]::GetFullPath((Join-Path $rootPath $ProfilePath))
    }
    if (-not (Test-Path -LiteralPath $profileAbsolute -PathType Leaf)) { throw "DB profile is missing: $ProfilePath" }
    $profileSha256 = Get-CpfFileSha256 $profileAbsolute
    $result.profileSha256 = $profileSha256

    . (Join-Path $PSScriptRoot 'database-profile-common.ps1')
    $profileRaw = Get-CpfDatabaseProfile $profileAbsolute
    $moduleOrder = @($profileRaw.modules.PSObject.Properties | ForEach-Object { [string]$_.Name })
    $staticProfiles = @{}
    foreach ($moduleKey in $moduleOrder) {
        $staticProfiles[$moduleKey] = ConvertTo-CpfModuleProfile $profileRaw $moduleKey -SkipSecretResolution
    }
    $selectedModuleKeys = @($moduleOrder | Where-Object {
            $staticProfiles[$_].enabled -and $staticProfiles[$_].databaseLifecycle -eq 'platform-pack'
        })
    $Modules = @($Modules | ForEach-Object { $_ -split ',' } | ForEach-Object { $_.Trim() } | Where-Object { $_ })
    if ($Modules.Count -gt 0) {
        if ($Mode -eq 'FreshInstall') { throw 'FreshInstall is a full enabled-platform lifecycle and does not accept -Modules.' }
        $unknownModules = @($Modules | Where-Object { $_ -notin $moduleOrder })
        if ($unknownModules.Count -gt 0) { throw "DB profile modules are unknown: $($unknownModules -join ', ')" }
        $selectedModuleKeys = @($selectedModuleKeys | Where-Object { $_ -in $Modules })
    }
    if ($selectedModuleKeys.Count -eq 0) { throw 'No enabled platform-pack modules were selected.' }
    $selectedTargets = @($selectedModuleKeys | ForEach-Object { $staticProfiles[$_] })
    if($VerifierOwnedDisposable){
        $profileEnvironment=([string]$profileRaw.environment).Trim().ToLowerInvariant()
        if($profileEnvironment -notin @('development','dev','local','test')){throw "Verifier-owned disposable lifecycle is forbidden for environment=$profileEnvironment"}
        if($VerifierRunId -notmatch '^[a-f0-9]{8,24}$'){throw 'Verifier-owned disposable lifecycle requires a lowercase hex -VerifierRunId (8..24 chars).'}
        $allowedHosts=@('mariadb','cpf-mariadb','postgresql','cpf-postgresql','oracle','cpf-oracle')
        foreach($target in $selectedTargets){
            $targetHost=([string]$target.host).Trim().ToLowerInvariant()
            if($targetHost -notin $allowedHosts){throw "Verifier-owned disposable lifecycle forbids host=$targetHost"}
            if($Vendor -in @('mariadb','postgresql')){
                $expectedPrefix="cpf_verify_${VerifierRunId}_"
                if(-not ([string]$target.databaseName).ToLowerInvariant().StartsWith($expectedPrefix)){throw "Verifier-owned database must start with $expectedPrefix"}
            }elseif($Vendor -eq 'oracle'){
                $expectedPrefix="cpfv_${VerifierRunId}_"
                if(-not ([string]$target.schemaName).ToLowerInvariant().StartsWith($expectedPrefix)){throw "Verifier-owned Oracle schema must start with $expectedPrefix"}
            }
        }
    }
    $selectedVendors = @($selectedTargets.vendor | Sort-Object -Unique)
    if ($selectedVendors.Count -ne 1 -or $selectedVendors[0] -cne $Vendor) {
        throw "DB profile vendor does not match the requested lifecycle vendor: requested=$Vendor profile=$($selectedVendors -join ',')"
    }

    $migrationRoot = Get-CpfLifecycleRoot ([string]$entry.lifecycle.migration) "$Vendor migration root"
    $rollbackRoot = Get-CpfLifecycleRoot ([string]$entry.lifecycle.rollback) "$Vendor rollback root"
    $migrations = @(Get-CpfMigrationCatalog $migrationRoot)
    # currentVersion/availableVersions는 반드시 선택된 Module/logical DB 범위로만 계산해야 한다.
    # Vendor 전체 checksum manifest의 전역 최댓값을 그대로 쓰면, core-only 등 부분 Module 선택 시
    # 활성화되지 않은 logical DB(예: mbwDB 전용 Migration)가 "현재 버전"으로 잘못 선택된다.
    # PostgreSQL/Oracle은 pack(= 논리DB 전용 하위 디렉터리) 이름으로, MariaDB flat root 파일은
    # 실제 라우팅 지시자인 inline `USE <logicalDatabase>;` 절 내용으로 동일한 원칙(실제 라우팅 대상이
    # 선택 범위 안에 있는가)을 적용한다. 세 Vendor 모두 이 한 필터를 거친다 — Vendor별 특례 분기 없음.
    $enabledLogicalDatabases = @($selectedTargets.logicalDatabase | Sort-Object -Unique)
    $migrations = @($migrations | Where-Object {
            $item = $_
            if ($item.pack -ne 'root') {
                return $enabledLogicalDatabases -contains $item.pack
            }
            if ($Vendor -ne 'mariadb') { return $true }
            $sqlText = Get-Content -LiteralPath (Join-Path $rootPath $item.path) -Raw -Encoding UTF8
            $referencedDatabases = @([regex]::Matches($sqlText, '(?im)^\s*USE\s+([A-Za-z0-9_]+)\s*;') |
                    ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique)
            if ($referencedDatabases.Count -eq 0) { return $true }
            return (@($referencedDatabases | Where-Object { $enabledLogicalDatabases -notcontains $_ })).Count -eq 0
        })
    $availableVersions = @($migrations.version | Sort-Object -Unique)
    $currentVersion = [int]($availableVersions | Measure-Object -Maximum).Maximum

    if ($MigrationVersion.Count -gt 0 -and ($FromVersion -ge 0 -or $ToVersion -ge 0)) {
        throw '-MigrationVersion and -FromVersion/-ToVersion cannot be combined.'
    }
    if (($FromVersion -ge 0) -xor ($ToVersion -ge 0)) { throw '-FromVersion and -ToVersion must be provided together.' }
    if ($MigrationVersion.Count -gt 0) {
        $selectedVersions = @($MigrationVersion | Sort-Object -Unique)
        if ($selectedVersions.Count -ne $MigrationVersion.Count -or @($selectedVersions | Where-Object { $_ -le 0 }).Count -gt 0) {
            throw '-MigrationVersion values must be unique positive integers.'
        }
        $selectionMode = 'EXPLICIT_VERSION'
    } elseif ($FromVersion -ge 0) {
        if ($FromVersion -ge $ToVersion) { throw 'Current-edge range requires FromVersion < ToVersion.' }
        $selectedVersions = @($availableVersions | Where-Object { $_ -gt $FromVersion -and $_ -le $ToVersion })
        $selectionMode = 'EXPLICIT_RANGE'
    } else {
        $selectedVersions = @($currentVersion)
        $selectionMode = 'DISCOVERED_CURRENT_EDGE'
    }
    $missingVersions = @($selectedVersions | Where-Object { $_ -notin $availableVersions })
    if ($missingVersions.Count -gt 0 -or $selectedVersions.Count -eq 0) {
        throw "Selected migration versions are absent from checksum manifests: $($missingVersions -join ', ')"
    }
    $selectedMigrations = @($migrations | Where-Object { $_.version -in $selectedVersions })
    foreach ($migration in $selectedMigrations) {
        $rollback = Get-CpfRollbackArtifact $migration $rollbackRoot
        Add-Member -InputObject $migration -NotePropertyName rollbackPath -NotePropertyValue $rollback.path
        Add-Member -InputObject $migration -NotePropertyName rollbackSha256 -NotePropertyValue $rollback.checksumSha256
    }

    $initializeTool = Resolve-CpfRepositoryPath 'cpf-tools/db/tools/initialize-cpf-database.ps1' 'Official DB initializer'
    $migrationTool = Resolve-CpfRepositoryPath 'cpf-tools/db/tools/invoke-platform-database-migration.ps1' 'Official platform migration tool'
    [void](Resolve-CpfRepositoryPath 'cpf-tools/db/tools/invoke-db-lifecycle-docker-client.ps1' 'Official Docker lifecycle adapter')

    $upgradePlan = $null
    $rollbackPlan = $null
    if ($Mode -ne 'FreshInstall') {
        if ($Mode -eq 'RollbackReapply') {
            $rollbackDryRunPath = Join-Path $logRoot "${Vendor}_${Mode}_${stamp}.rollback-dry-run.json"
            $rollbackPlan = Invoke-CpfMigrationDryRun rollback @($selectedVersions | Sort-Object -Descending) $rollbackDryRunPath
        }
        $upgradeDryRunPath = Join-Path $logRoot "${Vendor}_${Mode}_${stamp}.upgrade-dry-run.json"
        $upgradePlan = Invoke-CpfMigrationDryRun upgrade @($selectedVersions | Sort-Object) $upgradeDryRunPath
    }

    $adapterPlan = switch ($ClientAdapter) {
        'Static' { [pscustomobject]@{ ready = $false; mode = 'Static'; client = $manifestClient; reason = 'PLAN_ONLY' } }
        'Host' { Test-CpfHostClientAdapter $selectedTargets $manifestClient }
        'Docker' { Test-CpfDockerClientAdapter $DockerImage $manifestClient }
        default {
            $hostAdapter = Test-CpfHostClientAdapter $selectedTargets $manifestClient
            if ($hostAdapter.ready) { $hostAdapter } else { Test-CpfDockerClientAdapter $DockerImage $manifestClient }
        }
    }
    $result.adapter = $adapterPlan
    $backupManifests = @(Get-CpfBackupManifestPlan $BackupManifestPath)
    $authority = [ordered]@{
        vendorPackManifest = [ordered]@{ path = Get-CpfRelativePath $manifestPath; sha256 = Get-CpfFileSha256 $manifestPath }
        lifecycleContract = [ordered]@{ path = Get-CpfRelativePath $contractPath; sha256 = Get-CpfFileSha256 $contractPath }
        migrationIntentCatalog = [ordered]@{ path = Get-CpfRelativePath $intentCatalogPath; sha256 = Get-CpfFileSha256 $intentCatalogPath }
        lifecycleScenarioCatalog = [ordered]@{ path = Get-CpfRelativePath $scenarioCatalogPath; sha256 = Get-CpfFileSha256 $scenarioCatalogPath }
    }
    $stages = if ($Mode -eq 'FreshInstall') {
        @($freshArtifacts | ForEach-Object {
                [ordered]@{
                    stage = $_.role
                    consumer = Get-CpfRelativePath $initializeTool
                    artifact = $_.path
                    artifactSha256 = $_.sha256
                }
            })
    } elseif ($Mode -eq 'Upgrade') {
        @([ordered]@{
                stage = 'Upgrade'
                consumer = Get-CpfRelativePath $migrationTool
                direction = 'upgrade'
                versions = @($selectedVersions | Sort-Object)
                planSha256 = [string]$upgradePlan.planSha256
            })
    } else {
        @(
            [ordered]@{
                stage = 'Rollback'
                consumer = Get-CpfRelativePath $migrationTool
                direction = 'rollback'
                versions = @($selectedVersions | Sort-Object -Descending)
                planSha256 = [string]$rollbackPlan.planSha256
            },
            [ordered]@{
                stage = 'Reapply'
                consumer = Get-CpfRelativePath $migrationTool
                direction = 'upgrade'
                versions = @($selectedVersions | Sort-Object)
                planSha256 = [string]$upgradePlan.planSha256
            }
        )
    }
    $sourceStateRequirement = @{
        FreshInstall = 'EMPTY_DATABASES'
        Upgrade = 'PRE_CURRENT_EDGE_FIXTURE'
        RollbackReapply = 'CURRENT_EDGE_APPLIED'
    }[$Mode]
    $lifecyclePlan = [ordered]@{
        schemaVersion = 2
        contract = 'CPF_DB_VENDOR_LIFECYCLE_PLAN'
        vendor = $Vendor
        mode = $Mode
        profile = [ordered]@{
            name = [IO.Path]::GetFileName($profileAbsolute)
            profileSha256 = $profileSha256
            modules = @($selectedModuleKeys)
        }
        authority = $authority
        discovery = [ordered]@{
            policy = 'ALL_CHECKSUM_MANIFEST_MIGRATIONS'
            executionCoverage = if ($selectionMode -eq 'DISCOVERED_CURRENT_EDGE') { 'CURRENT_EDGE_ONLY' } else { 'EXPLICIT_SELECTION' }
            fullHistoricalLifecycleEvidence = $false
            migrationRoot = Get-CpfRelativePath $migrationRoot
            rollbackRoot = Get-CpfRelativePath $rollbackRoot
            discoveredMigrationCount = $migrations.Count
            availableVersions = @($availableVersions)
            currentVersion = $currentVersion
            selectionMode = $selectionMode
            selectedVersions = @($selectedVersions)
            selectedMigrations = @($selectedMigrations | ForEach-Object {
                    [ordered]@{
                        pack = $_.pack
                        version = $_.version
                        path = $_.path
                        checksumSha256 = $_.checksumSha256
                        rollbackPath = $_.rollbackPath
                        rollbackSha256 = $_.rollbackSha256
                    }
                })
        }
        sourceStateRequirement = $sourceStateRequirement
        freshInstallCurrentSnapshotWarning = 'FreshInstall already contains the current schema edge; do not run current-edge Upgrade against that same database. Use the catalogued pre-current fixture.'
        stages = @($stages)
        clientAdapter = $adapterPlan
        backupManifests = @($backupManifests)
        mutationAuthorized = $false
        sanitized = $true
    }

    Write-CpfJsonAtomic $lifecyclePlan $planAbsolute
    $lifecyclePlanSha256 = (Get-FileHash -LiteralPath $planAbsolute -Algorithm SHA256).Hash.ToLowerInvariant()
    $result.lifecyclePlanSha256 = $lifecyclePlanSha256
    $result.status = 'PLANNED'

    if ($ConfirmExecute) {
        if ($ExpectedLifecyclePlanSha256 -notmatch '^[0-9a-fA-F]{64}$' -or
                $ExpectedLifecyclePlanSha256.ToLowerInvariant() -ne $lifecyclePlanSha256) {
            throw "Execution requires the reviewed -ExpectedLifecyclePlanSha256. current=$lifecyclePlanSha256"
        }
        [void](Assert-CpfRequiredScalar $Operator 'Operator')
        [void](Assert-CpfRequiredScalar $Reason 'Reason')
        [void](Assert-CpfRequiredScalar $ApprovalReference 'ApprovalReference')
        if (-not $ConfirmApplicationsStopped -or -not $ConfirmRollbackReady) {
            throw 'Execution requires -ConfirmApplicationsStopped and -ConfirmRollbackReady.'
        }
        if ($Mode -eq 'Upgrade' -and -not $ConfirmPreCurrentFixture) {
            throw 'Upgrade requires -ConfirmPreCurrentFixture; current FreshInstall is not a valid V-current source state.'
        }
        if ($Mode -eq 'RollbackReapply' -and -not $ConfirmCurrentMigrationApplied) {
            throw 'RollbackReapply requires -ConfirmCurrentMigrationApplied.'
        }
        if ($Mode -ne 'FreshInstall') {
            if (-not $VerifierOwnedDisposable -and $BackupManifestPath.Count -eq 0) { throw 'Migration execution requires checksum-verified -BackupManifestPath values.' }
            if ($VerifierOwnedDisposable) {
                if ($VerifierRunId -notmatch '^[a-f0-9]{8,24}$') { throw 'Verifier-owned disposable lifecycle requires -VerifierRunId.' }
                if ($Operator -cne 'CPF_FULLLOCAL' -or $Reason -cne 'cpf-full-local-isolated-db-lifecycle' -or $ApprovalReference -cne ("CPF-VERIFY-" + $VerifierRunId)) {
                    throw 'Verifier-owned disposable lifecycle requires canonical FullLocal operator/reason/approval.'
                }
            }
            if ($ExpectedPlanSha256 -notmatch '^[0-9a-fA-F]{64}$' -or
                    $ExpectedPlanSha256.ToLowerInvariant() -ne [string]$upgradePlan.planSha256) {
                throw "Execution requires the reviewed -ExpectedPlanSha256. current=$($upgradePlan.planSha256)"
            }
            if ($Mode -eq 'RollbackReapply' -and
                    ($ExpectedRollbackPlanSha256 -notmatch '^[0-9a-fA-F]{64}$' -or
                    $ExpectedRollbackPlanSha256.ToLowerInvariant() -ne [string]$rollbackPlan.planSha256)) {
                throw "Execution requires the reviewed -ExpectedRollbackPlanSha256. current=$($rollbackPlan.planSha256)"
            }
        }
        if (-not [bool]$adapterPlan.ready -or [string]$adapterPlan.mode -eq 'Static') {
            throw "Selected DB client adapter is not execution-ready: mode=$($adapterPlan.mode)"
        }

        $result.approvalReference = $ApprovalReference
        if ([string]$adapterPlan.mode -eq 'Docker') {
            if ($DockerNetwork -notmatch '^[A-Za-z0-9][A-Za-z0-9_.-]*$') { throw "Docker network name is invalid: $DockerNetwork" }
            Invoke-CpfDockerLifecycle (Join-Path $logRoot "${Vendor}_${Mode}_${stamp}.docker")
        } else {
            $result.status = 'RUNNING'
            $result.executionStarted = $true
            $executionStarted = $true
            if ($Mode -eq 'FreshInstall') {
                $freshResultRoot = Join-Path $logRoot "${Vendor}_${Mode}_${stamp}.fresh"
                & $initializeTool -Root $rootPath -ProfilePath $profileAbsolute -ResultDir $freshResultRoot -All -SeedMode product -RequireRun
            } elseif ($Mode -eq 'Upgrade') {
                Invoke-CpfMigrationHost upgrade @($selectedVersions | Sort-Object) $ExpectedPlanSha256 (Join-Path $logRoot "${Vendor}_${Mode}_${stamp}.upgrade.json")
            } else {
                Invoke-CpfMigrationHost rollback @($selectedVersions | Sort-Object -Descending) $ExpectedRollbackPlanSha256 (Join-Path $logRoot "${Vendor}_${Mode}_${stamp}.rollback.json")
                Invoke-CpfMigrationHost upgrade @($selectedVersions | Sort-Object) $ExpectedPlanSha256 (Join-Path $logRoot "${Vendor}_${Mode}_${stamp}.reapply.json")
            }
        }
        $result.status = 'SUCCEEDED'
    }
} catch {
    $result.status=if($executionStarted){'UNKNOWN'}else{'FAILED'}
    $result.reconcileRequired=$executionStarted
    $result.error = $_.Exception.Message
    throw
} finally {
    $result.finishedAt = [DateTimeOffset]::UtcNow.ToString('o')
    Write-CpfJsonAtomic $result $resultAbsolute
    Write-Host "CPF DB lifecycle result: $resultAbsolute"
}

if (-not $ConfirmExecute) {
    Write-Host "CPF DB lifecycle plan PASS. No database was changed. lifecyclePlanSha256=$($result.lifecyclePlanSha256)"
} else {
    Write-Host "CPF DB lifecycle execution PASS. lifecyclePlanSha256=$($result.lifecyclePlanSha256)"
}
