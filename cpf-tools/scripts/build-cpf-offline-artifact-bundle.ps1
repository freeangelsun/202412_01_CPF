<#
.SYNOPSIS
Nexus/Artifactory가 없는 환경용 CPF Offline Maven Bundle을 생성합니다.
.DESCRIPTION
PROMOTED Local Artifact Manifest를 먼저 검증하고 Manifest에 포함된 파일만 복사하여 checksum과 metadata를 포함한 ZIP을 만듭니다.
개별 CPF JAR 수동 복사를 대체하는 배포 방식입니다.
.PARAMETER Root
CPF Repository Root.
.PARAMETER LocalRepository
검증된 PROMOTED CPF Maven Repository.
.PARAMETER OutputRoot
Offline Bundle 출력 Root. 기본 build/cpf-offline.
.EXAMPLE
pwsh -File .\cpf-tools\scripts\build-cpf-offline-artifact-bundle.ps1 -LocalRepository C:\cpf-repo
#>
param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $LocalRepository = "",
    [string] $OutputRoot = "build/cpf-offline"
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
if ([string]::IsNullOrWhiteSpace($LocalRepository)) { $LocalRepository = Join-Path $HOME '.cpf/repository' }
$LocalRepository = [IO.Path]::GetFullPath($LocalRepository)
$OutputRoot = if ([IO.Path]::IsPathRooted($OutputRoot)) { [IO.Path]::GetFullPath($OutputRoot) } else { [IO.Path]::GetFullPath((Join-Path $Root $OutputRoot)) }
$versionLine = Get-Content -LiteralPath (Join-Path $Root 'gradle/cpf-platform.properties') -Encoding UTF8 | Where-Object { $_ -match '^\s*platformVersion\s*=' } | Select-Object -First 1
if ($null -eq $versionLine) { throw 'platformVersion is missing.' }
$version = ($versionLine -split '=',2)[1].Trim()
$manifest = Join-Path $LocalRepository "_cpf/manifests/$version.json"
if (-not (Test-Path -LiteralPath $manifest -PathType Leaf)) {
    throw "Verified CPF artifact manifest is missing. Run publishCpfVerifiedLocalPlatformArtifacts first: $manifest"
}
& pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $Root 'cpf-tools/scripts/verify-local-artifact-propagation.ps1') -Root $Root -LocalRepository $LocalRepository -RequireManifest | Out-Host
if ($LASTEXITCODE -ne 0) { throw "Local artifact verification failed: $LASTEXITCODE" }

$bundleName = "cpf-offline-artifacts-$version"
$bundleDir = Join-Path $OutputRoot $bundleName
$zipPath = Join-Path $OutputRoot "$bundleName.zip"
if (Test-Path -LiteralPath $bundleDir) { Remove-Item -LiteralPath $bundleDir -Recurse -Force }
if (Test-Path -LiteralPath $zipPath) { Remove-Item -LiteralPath $zipPath -Force }
$repoTarget = Join-Path $bundleDir 'repository'
$metadataTarget = Join-Path $bundleDir 'metadata'
New-Item -ItemType Directory -Force -Path $repoTarget,$metadataTarget | Out-Null

$manifestObject = Get-Content -LiteralPath $manifest -Raw -Encoding UTF8 | ConvertFrom-Json
foreach ($artifact in @($manifestObject.artifacts)) {
    $source = Join-Path $LocalRepository (([string]$artifact.path).Replace('/', [IO.Path]::DirectorySeparatorChar))
    if (-not (Test-Path -LiteralPath $source -PathType Leaf)) { throw "Manifest artifact disappeared: $source" }
    $destination = Join-Path $repoTarget (([string]$artifact.path).Replace('/', [IO.Path]::DirectorySeparatorChar))
    $parent = Split-Path -Parent $destination
    if (-not (Test-Path -LiteralPath $parent)) { New-Item -ItemType Directory -Force -Path $parent | Out-Null }
    Copy-Item -LiteralPath $source -Destination $destination -Force
}
$manifestDest = Join-Path $repoTarget "_cpf/manifests/$version.json"
New-Item -ItemType Directory -Force -Path (Split-Path -Parent $manifestDest) | Out-Null
Copy-Item -LiteralPath $manifest -Destination $manifestDest -Force
Copy-Item -LiteralPath (Join-Path $Root 'gradle/cpf-platform.properties') -Destination (Join-Path $metadataTarget 'cpf-platform.properties') -Force
Copy-Item -LiteralPath (Join-Path $Root 'gradle/cpf-stack.properties') -Destination (Join-Path $metadataTarget 'cpf-stack.properties') -Force
@"
CPF Offline Artifact Bundle
Platform Version: $version
Source Commit: $($manifestObject.sourceCommit)

Usage:
  set CPF_ARTIFACT_MODE=OFFLINE
  set CPF_OFFLINE_ARTIFACT_REPOSITORY=<extracted-path>\repository

Generated Domain/standalone builds must use the repository directory above. Do not copy individual CPF JARs manually.
"@ | Set-Content -LiteralPath (Join-Path $bundleDir 'README.txt') -Encoding UTF8

$hashRows = Get-ChildItem -LiteralPath $bundleDir -Recurse -File | Sort-Object FullName | ForEach-Object {
    $relative = $_.FullName.Substring($bundleDir.Length + 1).Replace('\\','/')
    "$((Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant())  $relative"
}
[IO.File]::WriteAllLines((Join-Path $bundleDir 'SHA256SUMS.txt'), $hashRows, [Text.UTF8Encoding]::new($false))
Compress-Archive -LiteralPath $bundleDir -DestinationPath $zipPath -CompressionLevel Optimal
Write-Host "CPF offline artifact bundle created: $zipPath"
