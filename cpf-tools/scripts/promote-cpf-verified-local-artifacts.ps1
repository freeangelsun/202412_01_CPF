<#
.SYNOPSIS
검증된 CPF staging Maven Set을 Shared Local Repository로 Manifest Barrier 방식 승격합니다.
.DESCRIPTION
staging 검증 → publisher lock → 기존 Version backup → Manifest-backed Version directory 교체 → PROMOTED manifest 공개 → 재검증 순서로 동작합니다.
실패하면 기존 Version/Manifest를 복원합니다. 일반 사용자는 직접 호출하지 않고 Gradle publishCpfVerifiedLocalPlatformArtifacts를 사용합니다.
.PARAMETER Root
CPF Repository Root.
.PARAMETER StagingRepository
검증 대상 격리 Maven staging Repository.
.PARAMETER LocalRepository
승격 대상 Shared Local Maven Repository.
.EXAMPLE
pwsh -File .\cpf-tools\scripts\promote-cpf-verified-local-artifacts.ps1 -StagingRepository .\build\cpf-artifact-staging\repository
#>
param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [Parameter(Mandatory = $true)]
    [string] $StagingRepository,
    [string] $LocalRepository = ""
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

if ([string]::IsNullOrWhiteSpace($LocalRepository)) {
    $LocalRepository = if (-not [string]::IsNullOrWhiteSpace($env:CPF_LOCAL_ARTIFACT_REPOSITORY)) {
        $env:CPF_LOCAL_ARTIFACT_REPOSITORY
    } else {
        Join-Path $HOME '.cpf/repository'
    }
}
$StagingRepository = [IO.Path]::GetFullPath($StagingRepository)
$LocalRepository = [IO.Path]::GetFullPath($LocalRepository)
if ($StagingRepository -eq $LocalRepository) { throw 'StagingRepository and LocalRepository must be different.' }
if (-not (Test-Path -LiteralPath $StagingRepository -PathType Container)) { throw "Staging repository not found: $StagingRepository" }

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

$platform = Read-Properties (Join-Path $Root 'gradle/cpf-platform.properties')
$version = [string]$platform['platformVersion']
if ([string]::IsNullOrWhiteSpace($version)) { throw 'platformVersion is missing.' }
$verify = Join-Path $Root 'cpf-tools/scripts/verify-local-artifact-propagation.ps1'
if (-not (Test-Path -LiteralPath $verify -PathType Leaf)) { throw "Artifact verifier not found: $verify" }

$stagingManifest = Join-Path $StagingRepository "_cpf/manifests/$version.json"
& pwsh -NoProfile -File $verify -Root $Root -LocalRepository $StagingRepository -WriteManifest -ManifestPath $stagingManifest | Out-Host
if ($LASTEXITCODE -ne 0) { throw "Staging artifact verification failed: $LASTEXITCODE" }

$stagingManifestObject = Get-Content -LiteralPath $stagingManifest -Raw -Encoding UTF8 | ConvertFrom-Json
if ([string]$stagingManifestObject.platformVersion -ne $version) { throw 'Staging manifest version mismatch.' }

$transactionRoot = Join-Path ([IO.Path]::GetTempPath()) ("cpf-artifact-promote-$version-" + [guid]::NewGuid().ToString('N'))
$backupRoot = Join-Path $transactionRoot 'backup'
New-Item -ItemType Directory -Force -Path $backupRoot | Out-Null
$lockDir = Join-Path $LocalRepository '_cpf'
New-Item -ItemType Directory -Force -Path $lockDir | Out-Null
$lockPath = Join-Path $lockDir 'publish.lock'
$lockStream = $null
$promoted = [System.Collections.Generic.List[object]]::new()
$manifestBackup = $null
$targetManifest = Join-Path $LocalRepository "_cpf/manifests/$version.json"
try {
    # Publisher끼리만 직렬화합니다. Consumer는 PROMOTED manifest가 없으면 시작하지 않는 fail-closed 규칙을 사용합니다.
    $lockStream = [IO.File]::Open($lockPath, [IO.FileMode]::OpenOrCreate, [IO.FileAccess]::ReadWrite, [IO.FileShare]::None)

    if (Test-Path -LiteralPath $targetManifest -PathType Leaf) {
        $manifestBackup = Join-Path $backupRoot 'previous-manifest.json'
        Copy-Item -LiteralPath $targetManifest -Destination $manifestBackup -Force
        Remove-Item -LiteralPath $targetManifest -Force
    }

    # Manifest에 포함된 coordinate/version 디렉터리만 promotion합니다.
    # staging에 Manifest 밖의 동일-version Artifact가 있으면 부분 검증 노출을 막기 위해 즉시 실패합니다.
    $manifestVersionDirectoryMap = @{}
    foreach ($artifact in @($stagingManifestObject.artifacts)) {
        $relativeFile = ([string]$artifact.path).Replace('/', [IO.Path]::DirectorySeparatorChar)
        $relativeDirectory = Split-Path -Parent $relativeFile
        if ([string]::IsNullOrWhiteSpace($relativeDirectory) -or (Split-Path -Leaf $relativeDirectory) -ne $version) {
            throw "Manifest artifact is outside exact version directory: $($artifact.path)"
        }
        $manifestVersionDirectoryMap[$relativeDirectory] = $true
    }
    $manifestVersionDirectories = @($manifestVersionDirectoryMap.Keys | Sort-Object)
    if ($manifestVersionDirectories.Count -eq 0) { throw 'No manifest-backed CPF version directories found.' }

    $stagedVersionDirectories = @(Get-ChildItem -LiteralPath (Join-Path $StagingRepository 'com/cpf') -Recurse -Directory -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -eq $version } |
        ForEach-Object { $_.FullName.Substring($StagingRepository.Length + 1) } |
        Sort-Object -Unique)
    $unexpected = @($stagedVersionDirectories | Where-Object { -not $manifestVersionDirectoryMap.ContainsKey($_) })
    if ($unexpected.Count -gt 0) {
        throw "Unverified staged CPF version directories exist: $($unexpected -join ', ')"
    }
    $missing = @($manifestVersionDirectories | Where-Object {
        -not (Test-Path -LiteralPath (Join-Path $StagingRepository $_) -PathType Container)
    })
    if ($missing.Count -gt 0) { throw "Manifest-backed staged directories are missing: $($missing -join ', ')" }

    foreach ($relative in $manifestVersionDirectories) {
        $sourceVersionDir = Get-Item -LiteralPath (Join-Path $StagingRepository $relative)
        $targetVersionDir = Join-Path $LocalRepository $relative
        $targetParent = Split-Path -Parent $targetVersionDir
        if (-not (Test-Path -LiteralPath $targetParent)) { New-Item -ItemType Directory -Force -Path $targetParent | Out-Null }
        $backup = $null
        if (Test-Path -LiteralPath $targetVersionDir) {
            $safeName = [Convert]::ToHexString([Text.Encoding]::UTF8.GetBytes($relative)).ToLowerInvariant()
            $backup = Join-Path $backupRoot $safeName
            Move-Item -LiteralPath $targetVersionDir -Destination $backup -Force
        }
        Move-Item -LiteralPath $sourceVersionDir.FullName -Destination $targetVersionDir -Force
        $promoted.Add([ordered]@{ target=$targetVersionDir; backup=$backup })
    }

    $promotedManifest = [ordered]@{}
    foreach ($property in $stagingManifestObject.PSObject.Properties) { $promotedManifest[$property.Name] = $property.Value }
    $promotedManifest['promotionState'] = 'PROMOTED'
    $promotedManifest['promotedAtUtc'] = [DateTime]::UtcNow.ToString('o')
    $manifestParent = Split-Path -Parent $targetManifest
    if (-not (Test-Path -LiteralPath $manifestParent)) { New-Item -ItemType Directory -Force -Path $manifestParent | Out-Null }
    [IO.File]::WriteAllText($targetManifest, ($promotedManifest | ConvertTo-Json -Depth 30), [Text.UTF8Encoding]::new($false))

    & pwsh -NoProfile -File $verify -Root $Root -LocalRepository $LocalRepository -RequireManifest | Out-Host
    if ($LASTEXITCODE -ne 0) { throw "Promoted repository verification failed: $LASTEXITCODE" }
    Write-Host "CPF local artifact promotion complete. version=$version repository=$LocalRepository"
} catch {
    if (Test-Path -LiteralPath $targetManifest) { Remove-Item -LiteralPath $targetManifest -Force -ErrorAction SilentlyContinue }
    for ($i=$promoted.Count-1; $i -ge 0; $i--) {
        $entry=$promoted[$i]
        if (Test-Path -LiteralPath $entry.target) { Remove-Item -LiteralPath $entry.target -Recurse -Force }
        if ($null -ne $entry.backup -and (Test-Path -LiteralPath $entry.backup)) {
            $parent=Split-Path -Parent $entry.target
            if (-not (Test-Path -LiteralPath $parent)) { New-Item -ItemType Directory -Force -Path $parent | Out-Null }
            Move-Item -LiteralPath $entry.backup -Destination $entry.target -Force
        }
    }
    if ($null -ne $manifestBackup -and (Test-Path -LiteralPath $manifestBackup)) {
        $manifestParent = Split-Path -Parent $targetManifest
        if (-not (Test-Path -LiteralPath $manifestParent)) { New-Item -ItemType Directory -Force -Path $manifestParent | Out-Null }
        Copy-Item -LiteralPath $manifestBackup -Destination $targetManifest -Force
    }
    throw
} finally {
    if ($null -ne $lockStream) { $lockStream.Dispose() }
    if (Test-Path -LiteralPath $transactionRoot) { Remove-Item -LiteralPath $transactionRoot -Recurse -Force -ErrorAction SilentlyContinue }
}
