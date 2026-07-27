<#
.SYNOPSIS
Nexus/Artifactory가 없는 환경용 CPF Offline 제품 Bundle을 생성합니다.
.DESCRIPTION
PROMOTED Local Artifact Manifest를 검증한 뒤 Maven Artifact와 실행 가능한 DB Runtime Vendor Pack을
하나의 ZIP으로 묶습니다. Runtime SQL은 개별 JAR에 복제하지 않고 외부 Vendor Pack을 정본으로 유지합니다.
.PARAMETER Root
CPF Repository Root.
.PARAMETER LocalRepository
검증된 PROMOTED CPF Maven Repository.
.PARAMETER OutputRoot
Offline Bundle 출력 Root. 기본 build/cpf-offline.
.PARAMETER SkipDbVendorPacks
특수한 Maven-only 배포 검증에만 사용합니다. 제품 Offline Bundle에서는 사용하지 않습니다.
#>
param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $LocalRepository = "",
    [string] $OutputRoot = "build/cpf-offline",
    [switch] $SkipDbVendorPacks
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Root = (Resolve-Path -LiteralPath $Root).Path
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
$dbTarget = Join-Path $bundleDir 'db/vendor-pack'
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

$dbPackRows = [Collections.Generic.List[object]]::new()
if (-not $SkipDbVendorPacks) {
    $dbManifestPath = Join-Path $Root 'cpf-tools/db/vendor-pack-manifest.json'
    if (-not (Test-Path -LiteralPath $dbManifestPath -PathType Leaf)) { throw "DB Vendor Pack manifest is missing: $dbManifestPath" }
    $dbManifest = Get-Content -LiteralPath $dbManifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
    $supportedVendors = @($dbManifest.supportedVendors)
    if ($supportedVendors.Count -eq 0) { throw 'No executable DB vendor is registered in supportedVendors.' }
    New-Item -ItemType Directory -Force -Path $dbTarget | Out-Null
    Copy-Item -LiteralPath $dbManifestPath -Destination (Join-Path $metadataTarget 'db-vendor-pack-manifest.json') -Force
    $selector = Join-Path $Root 'cpf-tools/scripts/select-db-vendor-resources.ps1'
    foreach ($vendor in $supportedVendors) {
        $selectionRoot = Join-Path $OutputRoot (".db-pack-selection-{0}-{1}" -f $vendor, [Guid]::NewGuid().ToString('N'))
        try {
            $selectionOutput = @(& pwsh -NoProfile -ExecutionPolicy Bypass -File $selector -Root $Root -Vendor ([string]$vendor) -ResultDir $selectionRoot -RequireExecutable -AssembleOverlay)
            if ($LASTEXITCODE -ne 0) { throw "DB Vendor Pack selection failed. vendor=$vendor exit=$LASTEXITCODE" }
            $resultPath = Join-Path $selectionRoot 'active-db-resources.sanitized.json'
            if (-not (Test-Path -LiteralPath $resultPath -PathType Leaf)) { throw "DB selection result is missing: $resultPath" }
            $selection = Get-Content -LiteralPath $resultPath -Raw -Encoding UTF8 | ConvertFrom-Json
            if (-not [bool]$selection.executable) { throw "DB Vendor Pack is not executable: $vendor" }
            $externalPackRoot = [string]$selection.overlay.externalPackRoot
            if ([string]::IsNullOrWhiteSpace($externalPackRoot) -or -not (Test-Path -LiteralPath $externalPackRoot -PathType Container)) {
                throw "Selected external DB Vendor Pack is missing: vendor=$vendor path=$externalPackRoot"
            }
            $vendorTarget = Join-Path $dbTarget ([string]$vendor)
            Copy-Item -LiteralPath $externalPackRoot -Destination $vendorTarget -Recurse -Force
            $files = @(Get-ChildItem -LiteralPath $vendorTarget -Recurse -File)
            if ($files.Count -eq 0) { throw "Offline DB Vendor Pack is empty: $vendorTarget" }
            $dbPackRows.Add([pscustomobject]@{
                vendor = [string]$vendor
                source = [string]$selection.vendorRoot
                target = "db/vendor-pack/$vendor"
                runtimeFileCount = [int]$selection.runtimeFileCount
                totalFileCount = $files.Count
            })
        } finally {
            if (Test-Path -LiteralPath $selectionRoot) { Remove-Item -LiteralPath $selectionRoot -Recurse -Force }
        }
    }
}
$dbPackMetadata = [ordered]@{
    generatedAt = (Get-Date).ToUniversalTime().ToString('o')
    platformVersion = $version
    sourceCommit = [string]$manifestObject.sourceCommit
    packs = @($dbPackRows)
}
[IO.File]::WriteAllText(
    (Join-Path $metadataTarget 'db-vendor-packs.sanitized.json'),
    ($dbPackMetadata | ConvertTo-Json -Depth 10) + [Environment]::NewLine,
    [Text.UTF8Encoding]::new($false)
)

$readmeContent = @"
CPF Offline Product Bundle
Platform Version: $version
Source Commit: $($manifestObject.sourceCommit)

Maven usage:
  set CPF_ARTIFACT_MODE=OFFLINE
  set CPF_OFFLINE_ARTIFACT_REPOSITORY=<extracted-path>\repository

DB Runtime Query Pack usage:
  set CPF_DB_VENDOR=<mariadb|postgresql|oracle>
  set JAVA_TOOL_OPTIONS=-Dcpf.db.vendor=<vendor> -Dcpf.db.resource-root=<extracted-path>\db\vendor-pack\<vendor>

Only Vendor Packs that pass the repository readiness contract are bundled. Missing/unready vendors fail closed.
Generated Domain/standalone builds must use the repository directory above. Do not copy individual CPF JARs or SQL files manually.
"@
Set-Content -LiteralPath (Join-Path $bundleDir 'README.txt') -Value $readmeContent -Encoding UTF8

$hashRows = Get-ChildItem -LiteralPath $bundleDir -Recurse -File | Sort-Object FullName | ForEach-Object {
    $relative = $_.FullName.Substring($bundleDir.Length + 1).Replace('\\','/')
    "$((Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant())  $relative"
}
[IO.File]::WriteAllLines((Join-Path $bundleDir 'SHA256SUMS.txt'), $hashRows, [Text.UTF8Encoding]::new($false))

$packGate = Join-Path $Root 'cpf-tools/scripts/check-offline-db-resource-pack.ps1'
if (-not (Test-Path -LiteralPath $packGate -PathType Leaf)) { throw "Offline DB Resource Pack gate is missing: $packGate" }
& pwsh -NoProfile -ExecutionPolicy Bypass -File $packGate -Root $Root -BundleRoot $bundleDir | Out-Host
if ($LASTEXITCODE -ne 0) { throw "Offline DB Resource Pack verification failed: $LASTEXITCODE" }

Compress-Archive -LiteralPath $bundleDir -DestinationPath $zipPath -CompressionLevel Optimal
Write-Host "CPF offline product bundle created: $zipPath"
