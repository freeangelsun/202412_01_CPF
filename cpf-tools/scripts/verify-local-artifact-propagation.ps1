<#
.SYNOPSIS
CPF Maven Artifact Set의 좌표, Version, POM, BOM, Plugin Marker, Hash와 Source Identity를 검증합니다.
.DESCRIPTION
LOCAL_DEV staging/local Repository 또는 Offline Repository의 CPF Artifact Set을 검증합니다.
-WriteManifest는 VERIFIED_STAGING manifest를 생성하고, -RequireManifest는 PROMOTED manifest와 현재 Source fingerprint까지 대조합니다.
.PARAMETER Root
CPF Repository Root.
.PARAMETER LocalRepository
검증할 Maven Repository 경로. 미지정 시 CPF_LOCAL_ARTIFACT_REPOSITORY 또는 ~/.cpf/repository.
.PARAMETER WriteManifest
검증 결과를 ManifestPath에 VERIFIED_STAGING manifest로 기록합니다.
.PARAMETER ManifestPath
Manifest 파일 경로. 미지정 시 _cpf/manifests/<platformVersion>.json.
.PARAMETER RequireManifest
기존 PROMOTED manifest가 현재 Artifact Hash와 Source fingerprint에 정확히 일치해야 합니다.
.EXAMPLE
pwsh -File .\cpf-tools\scripts\verify-local-artifact-propagation.ps1 -LocalRepository C:\cpf-repo -RequireManifest
#>
param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $LocalRepository = "",
    [switch] $WriteManifest,
    [string] $ManifestPath = "",
    [switch] $RequireManifest
)
$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

if ([string]::IsNullOrWhiteSpace($LocalRepository)) {
    $LocalRepository = if (-not [string]::IsNullOrWhiteSpace($env:CPF_LOCAL_ARTIFACT_REPOSITORY)) {
        $env:CPF_LOCAL_ARTIFACT_REPOSITORY
    } else {
        Join-Path $HOME ".cpf/repository"
    }
}
$LocalRepository = [IO.Path]::GetFullPath($LocalRepository)

function Read-Properties([string] $Path) {
    $values = @{}
    foreach ($line in Get-Content -LiteralPath $Path -Encoding UTF8) {
        $trimmed = $line.Trim()
        if ([string]::IsNullOrWhiteSpace($trimmed) -or $trimmed.StartsWith('#')) { continue }
        $index = $trimmed.IndexOf('=')
        if ($index -gt 0) { $values[$trimmed.Substring(0,$index).Trim()] = $trimmed.Substring($index+1).Trim() }
    }
    return $values
}
function Sha256([string] $Path) { (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant() }
function Sha256-Text([string] $Text) {
    $sha = [Security.Cryptography.SHA256]::Create()
    try {
        $bytes = [Text.Encoding]::UTF8.GetBytes($Text)
        return ([Convert]::ToHexString($sha.ComputeHash($bytes))).ToLowerInvariant()
    } finally {
        $sha.Dispose()
    }
}
function Get-SourceIdentity([string] $RepositoryRoot) {
    $commit = 'UNKNOWN'
    $dirty = $false
    $fingerprint = 'UNKNOWN'
    try {
        $commit = (& git -C $RepositoryRoot rev-parse HEAD 2>$null).Trim()
        if ([string]::IsNullOrWhiteSpace($commit)) { return [ordered]@{ commit='UNKNOWN'; dirty=$false; fingerprint='UNKNOWN' } }
        $status = @(& git -C $RepositoryRoot status --porcelain=v1 --untracked-files=all 2>$null)
        $dirty = $status.Count -gt 0
        if (-not $dirty) {
            $fingerprint = $commit
        } else {
            $diff = @(& git -C $RepositoryRoot diff --binary HEAD -- . 2>$null)
            $untrackedRows = [System.Collections.Generic.List[string]]::new()
            foreach ($row in $status) {
                if ($row -notmatch '^\?\?\s+(.+)$') { continue }
                $relative = $Matches[1].Trim()
                $candidate = Join-Path $RepositoryRoot $relative
                if (Test-Path -LiteralPath $candidate -PathType Leaf) {
                    $untrackedRows.Add("$relative=$(Sha256 $candidate)")
                }
            }
            $material = @(
                "commit=$commit",
                'status:',
                ($status -join "`n"),
                'diff:',
                ($diff -join "`n"),
                'untracked:',
                ((@($untrackedRows) | Sort-Object) -join "`n")
            ) -join "`n"
            $fingerprint = Sha256-Text $material
        }
    } catch {
        $commit = 'UNKNOWN'
        $dirty = $false
        $fingerprint = 'UNKNOWN'
    }
    return [ordered]@{ commit=$commit; dirty=$dirty; fingerprint=$fingerprint }
}
function Require-File([string] $Relative) {
    $candidate = Join-Path $LocalRepository ($Relative.Replace('/', [IO.Path]::DirectorySeparatorChar))
    if (-not (Test-Path -LiteralPath $candidate -PathType Leaf)) { throw "Missing CPF artifact: $Relative" }
    return $candidate
}
function Assert-PomIdentity([string] $PomPath, [string] $GroupId, [string] $ArtifactId, [string] $Version) {
    [xml]$pom = Get-Content -LiteralPath $PomPath -Raw -Encoding UTF8
    $project = $pom.project
    $actualGroup = if ($project.groupId) { [string]$project.groupId } else { [string]$project.parent.groupId }
    $actualVersion = if ($project.version) { [string]$project.version } else { [string]$project.parent.version }
    if ($actualGroup -ne $GroupId -or [string]$project.artifactId -ne $ArtifactId -or $actualVersion -ne $Version) {
        throw "POM identity mismatch: $PomPath expected=$GroupId`:$ArtifactId`:$Version actual=$actualGroup`:$($project.artifactId)`:$actualVersion"
    }
}

$platformProperties = Join-Path $Root "gradle/cpf-platform.properties"
if (-not (Test-Path -LiteralPath $platformProperties -PathType Leaf)) { throw "CPF platform properties가 없습니다: $platformProperties" }
$platform = Read-Properties $platformProperties
$version = [string]$platform['platformVersion']
if ([string]::IsNullOrWhiteSpace($version)) { throw "platformVersion을 찾을 수 없습니다: $platformProperties" }

$artifactCatalogPath = Join-Path $Root 'cpf-tools/release/cpf-final-artifact-catalog.json'
if (-not (Test-Path -LiteralPath $artifactCatalogPath -PathType Leaf)) {
    throw "CPF final artifact catalog is missing: $artifactCatalogPath"
}
$artifactCatalog = Get-Content -LiteralPath $artifactCatalogPath -Raw -Encoding UTF8 |
    ConvertFrom-Json -Depth 30
$starterCatalogRows = @($artifactCatalog.artifacts |
    Where-Object { [string]$_.kind -eq 'starter' } |
    Sort-Object { [string]$_.ownerPath })
if ($starterCatalogRows.Count -eq 0) {
    throw 'CPF final artifact catalog has no Starter artifacts.'
}
$starterCatalogOwnerPaths = [System.Collections.Generic.List[string]]::new()
$starterCoordinates = @($starterCatalogRows | ForEach-Object {
    $ownerPath = ([string]$_.ownerPath).Replace('\', '/').TrimEnd('/')
    if ($ownerPath -notmatch '^cpf-starters/([^/]+)$') {
        throw "Starter artifact ownerPath is invalid: $ownerPath"
    }
    $expectedArtifactId = "cpf-starter-$($Matches[1])"
    if ([string]$_.artifactId -ne $expectedArtifactId) {
        throw "Starter artifact identity mismatch: owner=$ownerPath artifact=$($_.artifactId)"
    }
    if (-not (Test-Path -LiteralPath (Join-Path $Root "$ownerPath/build.gradle") -PathType Leaf)) {
        throw "Starter catalog owner has no Gradle project: $ownerPath"
    }
    $starterCatalogOwnerPaths.Add($ownerPath) | Out-Null
    @{ group='com.cpf.starter'; artifact=$expectedArtifactId; packaging='jar' }
})
if (@($starterCatalogOwnerPaths | Sort-Object -Unique).Count -ne $starterCatalogOwnerPaths.Count) {
    throw "Starter artifact catalog contains duplicate owner paths: $($starterCatalogOwnerPaths -join ', ')"
}
$physicalStarterOwnerPaths = @(Get-ChildItem -LiteralPath (Join-Path $Root 'cpf-starters') -Directory |
    Where-Object { Test-Path -LiteralPath (Join-Path $_.FullName 'build.gradle') -PathType Leaf } |
    Sort-Object Name |
    ForEach-Object { "cpf-starters/$($_.Name)" })
$starterPathDrift = @(Compare-Object -ReferenceObject @($starterCatalogOwnerPaths | Sort-Object) `
    -DifferenceObject $physicalStarterOwnerPaths)
if ($starterPathDrift.Count -gt 0) {
    throw "Starter catalog/physical project mismatch: $($starterPathDrift | ConvertTo-Json -Compress)"
}

$coordinates = @(
    @{ group='com.cpf.core'; artifact='cpf-core'; packaging='jar' },
    @{ group='com.cpf.common'; artifact='cpf-common'; packaging='jar' },
    @{ group='com.cpf.batch'; artifact='cpf-batch-contract'; packaging='jar' },
    @{ group='com.cpf.batch'; artifact='cpf-batch-runtime-common'; packaging='jar' },
    @{ group='com.cpf.batch'; artifact='cpf-batch-testkit'; packaging='jar' },
    @{ group='com.cpf.batch'; artifact='cpf-batch-control-server'; packaging='jar' },
    @{ group='com.cpf.batch'; artifact='cpf-batch-scheduler'; packaging='jar' },
    @{ group='com.cpf.batch'; artifact='cpf-batch-worker'; packaging='jar' },
    @{ group='com.cpf.batch'; artifact='cpf-center-cut-runner'; packaging='jar' },
    @{ group='com.cpf.batch'; artifact='cpf-batch-host-agent'; packaging='jar' }
) + $starterCoordinates + @(
    @{ group='com.cpf'; artifact='cpf-platform-bom'; packaging='pom' },
    @{ group='com.cpf.gradle'; artifact='cpf-gradle-plugin'; packaging='jar' }
)
$files = [System.Collections.Generic.List[object]]::new()
$fileIndex = @{}
function Add-ManifestFile([string] $Relative, [string] $Type) {
    if ($fileIndex.ContainsKey($Relative)) { return }
    $candidate = Require-File $Relative
    $record = [ordered]@{ path=$Relative; sha256=Sha256 $candidate; type=$Type }
    $files.Add($record)
    $fileIndex[$Relative] = $true
}
function Add-VersionDirectoryFiles([string] $BaseRelative, [string] $Artifact, [string] $Version) {
    $versionRelative = ($BaseRelative.Substring(0, $BaseRelative.LastIndexOf('/')))
    $versionDirectory = Join-Path $LocalRepository ($versionRelative.Replace('/', [IO.Path]::DirectorySeparatorChar))
    foreach ($candidate in @(Get-ChildItem -LiteralPath $versionDirectory -File | Sort-Object Name)) {
        if ($candidate.Name.StartsWith("$Artifact-") -and
                -not $candidate.Name.StartsWith("$Artifact-$Version")) {
            throw "Mixed artifact version file found in exact version directory: $($candidate.FullName)"
        }
        $relative = $candidate.FullName.Substring($LocalRepository.Length).TrimStart('\','/').Replace('\','/')
        Add-ManifestFile $relative 'repository-file'
    }
}
foreach ($coordinate in $coordinates) {
    $groupPath = $coordinate.group.Replace('.', '/')
    $base = "$groupPath/$($coordinate.artifact)/$version/$($coordinate.artifact)-$version"
    $pom = Require-File "$base.pom"
    Assert-PomIdentity $pom $coordinate.group $coordinate.artifact $version
    Add-ManifestFile "$base.pom" 'pom'
    if ($coordinate.packaging -eq 'jar') {
        $jar = Require-File "$base.jar"
        Add-ManifestFile "$base.jar" 'jar'
    }
    $module = Join-Path $LocalRepository (($base + '.module').Replace('/', [IO.Path]::DirectorySeparatorChar))
    if (Test-Path -LiteralPath $module -PathType Leaf) {
        $moduleJson = Get-Content -LiteralPath $module -Raw -Encoding UTF8 | ConvertFrom-Json
        if ([string]$moduleJson.component.group -ne $coordinate.group -or [string]$moduleJson.component.module -ne $coordinate.artifact -or [string]$moduleJson.component.version -ne $version) {
            throw "Gradle module metadata identity mismatch: $module"
        }
        Add-ManifestFile "$base.module" 'gradle-module'
    }
    Add-VersionDirectoryFiles $base $coordinate.artifact $version
}

$markerGroup = 'com.cpf.platform-conventions'
$markerArtifact = 'com.cpf.platform-conventions.gradle.plugin'
$markerBase = "$($markerGroup.Replace('.','/'))/$markerArtifact/$version/$markerArtifact-$version"
$markerPom = Require-File "$markerBase.pom"
Assert-PomIdentity $markerPom $markerGroup $markerArtifact $version
[xml]$markerXml = Get-Content -LiteralPath $markerPom -Raw -Encoding UTF8
$markerDependency = @($markerXml.project.dependencies.dependency) | Where-Object {
    [string]$_.groupId -eq 'com.cpf.gradle' -and [string]$_.artifactId -eq 'cpf-gradle-plugin' -and [string]$_.version -eq $version
}
if ($markerDependency.Count -ne 1) { throw 'CPF Gradle plugin marker does not point to the exact implementation version.' }
Add-ManifestFile "$markerBase.pom" 'plugin-marker'
Add-VersionDirectoryFiles $markerBase $markerArtifact $version

$bomPomPath = Join-Path $LocalRepository (("com/cpf/cpf-platform-bom/$version/cpf-platform-bom-$version.pom").Replace('/', [IO.Path]::DirectorySeparatorChar))
[xml]$bomXml = Get-Content -LiteralPath $bomPomPath -Raw -Encoding UTF8
$bomDependencies = @($bomXml.project.dependencyManagement.dependencies.dependency)
$requiredBomArtifacts = @(
    'cpf-core','cpf-common','cpf-batch-contract','cpf-batch-testkit'
) + @($starterCatalogRows | ForEach-Object { [string]$_.artifactId } | Sort-Object)
foreach ($required in $requiredBomArtifacts) {
    $matches = @($bomDependencies | Where-Object { [string]$_.artifactId -eq $required -and [string]$_.version -eq $version })
    if ($matches.Count -ne 1) { throw "CPF BOM exact-version constraint missing or duplicated: ${required}:$version" }
}

$sourceIdentity = Get-SourceIdentity $Root
$sourceSha = [string]$sourceIdentity.commit
$stack = Read-Properties (Join-Path $Root 'gradle/cpf-stack.properties')
$manifest = [ordered]@{
    schemaVersion = 2
    platformVersion = $version
    sourceCommit = $sourceSha
    sourceDirty = [bool]$sourceIdentity.dirty
    sourceFingerprint = [string]$sourceIdentity.fingerprint
    generatedAtUtc = [DateTime]::UtcNow.ToString('o')
    javaVersion = $stack['javaVersion']
    gradleVersion = $stack['gradleVersion']
    springBootVersion = $stack['springBootVersion']
    promotionState = 'VERIFIED_STAGING'
    artifacts = @($files)
}


if ($RequireManifest) {
    if ([string]::IsNullOrWhiteSpace($ManifestPath)) {
        $ManifestPath = Join-Path $LocalRepository "_cpf/manifests/$version.json"
    }
    if (-not (Test-Path -LiteralPath $ManifestPath -PathType Leaf)) { throw "CPF promoted manifest is missing: $ManifestPath" }
    $published = Get-Content -LiteralPath $ManifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
    if ([string]$published.platformVersion -ne $version) { throw 'CPF promoted manifest platformVersion mismatch.' }
    if ([string]$published.promotionState -ne 'PROMOTED') { throw "CPF manifest is not promoted: $($published.promotionState)" }
    if ($sourceSha -ne 'UNKNOWN' -and [string]$published.sourceCommit -ne $sourceSha) {
        throw "CPF manifest sourceCommit does not match current source. current=$sourceSha manifest=$($published.sourceCommit)"
    }
    if ([string]$sourceIdentity.fingerprint -ne 'UNKNOWN') {
        if ([string]::IsNullOrWhiteSpace([string]$published.sourceFingerprint)) {
            throw 'CPF promoted manifest has no sourceFingerprint. Republish with the current verifier.'
        }
        if ([string]$published.sourceFingerprint -ne [string]$sourceIdentity.fingerprint) {
            throw "CPF manifest sourceFingerprint does not match current source tree. current=$($sourceIdentity.fingerprint) manifest=$($published.sourceFingerprint)"
        }
    }
    $publishedArtifacts = @{}
    foreach ($item in @($published.artifacts)) { $publishedArtifacts[[string]$item.path] = [string]$item.sha256 }
    if ($publishedArtifacts.Count -ne $files.Count) { throw "CPF manifest artifact count mismatch. manifest=$($publishedArtifacts.Count) actual=$($files.Count)" }
    foreach ($item in $files) {
        $path = [string]$item.path
        if (-not $publishedArtifacts.ContainsKey($path) -or $publishedArtifacts[$path] -ne [string]$item.sha256) {
            throw "CPF promoted manifest hash mismatch: $path"
        }
    }
}
if ($WriteManifest) {
    if ([string]::IsNullOrWhiteSpace($ManifestPath)) {
        $ManifestPath = Join-Path $LocalRepository "_cpf/manifests/$version.json"
    }
    $parent = Split-Path -Parent $ManifestPath
    if (-not (Test-Path -LiteralPath $parent)) { New-Item -ItemType Directory -Force -Path $parent | Out-Null }
    [IO.File]::WriteAllText($ManifestPath, ($manifest | ConvertTo-Json -Depth 20), [Text.UTF8Encoding]::new($false))
}
$manifest | ConvertTo-Json -Depth 20
