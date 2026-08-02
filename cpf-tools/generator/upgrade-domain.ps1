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

    $oldOwned = @{}
    foreach ($entry in @($ownership.createdFiles)) {
        $oldOwned[[string]$entry.path] = [string]$entry.sha256
    }
    $userOwnedPrefixes = @('src/main/java/', 'src/test/java/', 'ui/')
    $userOwnedFiles = @('README.md', 'config/cpf-approved-exceptions.csv')
    $candidateFiles = @(Get-ChildItem -LiteralPath $tempRoot -Recurse -File)
    $operations = [System.Collections.Generic.List[object]]::new()
    $conflicts = [System.Collections.Generic.List[string]]::new()
    $preserved = [System.Collections.Generic.List[string]]::new()

    foreach ($candidate in $candidateFiles) {
        $relative = $candidate.FullName.Substring($tempRoot.Length).TrimStart('\','/').Replace('\','/')
        if ($relative -eq 'manifest/generator-ownership.json') { continue }
        $target = Join-Path $domainRoot $relative
        if (-not (Test-Path -LiteralPath $target -PathType Leaf)) {
            $operations.Add([ordered]@{ action='ADD'; path=$relative; source=$candidate.FullName; target=$target })
            continue
        }
        if ($relative -in $userOwnedFiles -or @($userOwnedPrefixes | Where-Object { $relative.StartsWith($_) }).Count -gt 0) {
            if ($relative -eq 'src/main/resources/META-INF/cpf/cpf-approved-exceptions.csv') {
                # Runtime copy는 승인 Registry 정본과 동일해야 하므로 managed file로 처리합니다.
            } else {
                $preserved.Add($relative)
                continue
            }
        }
        $currentSha = Get-Sha $target
        $ownedSha = [string]$oldOwned[$relative]
        if ([string]::IsNullOrWhiteSpace($ownedSha) -or $currentSha -ne $ownedSha) {
            $conflicts.Add("managed file drift: $relative")
            continue
        }
        $candidateSha = Get-Sha $candidate.FullName
        if ($currentSha -ne $candidateSha) {
            $operations.Add([ordered]@{ action='UPDATE'; path=$relative; source=$candidate.FullName; target=$target })
        }
    }

    $candidateRelativePaths = @($candidateFiles | ForEach-Object {
        $_.FullName.Substring($tempRoot.Length).TrimStart('\','/').Replace('\','/')
    })
    $obsolete = @($oldOwned.Keys | Where-Object {
        $_ -notin $candidateRelativePaths -and $_ -ne 'manifest/generator-ownership.json'
    } | Sort-Object)

    $report = [ordered]@{
        schemaVersion = '1.0'
        projectName = $projectName
        apply = [bool]$Apply
        generatedAt = (Get-Date).ToString('o')
        operations = @($operations | ForEach-Object { [ordered]@{ action=$_.action; path=$_.path } })
        preservedUserFiles = @($preserved | Sort-Object -Unique)
        managedConflicts = @($conflicts)
        obsoleteGeneratedPaths = $obsolete
        deletionPolicy = 'NOT_EXECUTED; exact paths require user approval and Delete Manifest.'
    }
    $reportPath = Join-Path $Root "build/reports/domain-upgrade/$projectName/upgrade-domain-result.json"
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $reportPath) | Out-Null
    [IO.File]::WriteAllText($reportPath, ($report | ConvertTo-Json -Depth 20), $Utf8NoBom)

    if ($conflicts.Count -gt 0) {
        throw "Generated Domain managed files have user drift. No file was changed. report=$reportPath"
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
    foreach ($candidateRelative in $candidateRelativePaths | Sort-Object -Unique) {
        if ($candidateRelative -eq 'manifest/generator-ownership.json') { continue }
        $target = Join-Path $domainRoot $candidateRelative
        if (Test-Path -LiteralPath $target -PathType Leaf) {
            $newOwnedFiles += [ordered]@{ path=$candidateRelative; sha256=(Get-Sha $target) }
        }
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
