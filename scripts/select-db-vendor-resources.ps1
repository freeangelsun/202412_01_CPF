param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [ValidateSet("mariadb", "mysql", "postgresql", "oracle", "sqlserver")]
    [string] $Vendor = $env:CPF_DB_VENDOR,
    [string] $ResultDir = "",
    [switch] $RequireExecutable,
    [switch] $AssembleOverlay,
    [switch] $SkipParityCheck
)

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($Vendor)) { $Vendor = "mariadb" }
$Vendor = $Vendor.ToLowerInvariant()
$Root = (Resolve-Path -LiteralPath $Root).Path
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build\db-selection\$Vendor"
}

function Test-PathWithin {
    param(
        [string] $Candidate,
        [string] $Parent
    )

    $candidateFull = [System.IO.Path]::GetFullPath($Candidate).TrimEnd(
        [System.IO.Path]::DirectorySeparatorChar,
        [System.IO.Path]::AltDirectorySeparatorChar
    )
    $parentFull = [System.IO.Path]::GetFullPath($Parent).TrimEnd(
        [System.IO.Path]::DirectorySeparatorChar,
        [System.IO.Path]::AltDirectorySeparatorChar
    )
    return $candidateFull.Equals($parentFull, [System.StringComparison]::OrdinalIgnoreCase) -or
        $candidateFull.StartsWith(
            $parentFull + [System.IO.Path]::DirectorySeparatorChar,
            [System.StringComparison]::OrdinalIgnoreCase
        )
}

function Resolve-RepositoryPath {
    param(
        [string] $RelativePath,
        [switch] $AllowMissing
    )

    if ([System.IO.Path]::IsPathRooted($RelativePath)) {
        throw "Vendor Pack manifest에는 Repository 상대경로만 허용됩니다: $RelativePath"
    }
    $absolutePath = [System.IO.Path]::GetFullPath(
        (Join-Path $Root ($RelativePath -replace "/", [System.IO.Path]::DirectorySeparatorChar))
    )
    if (-not (Test-PathWithin $absolutePath $Root)) {
        throw "Vendor Pack 경로가 Repository 밖으로 이탈합니다: $RelativePath"
    }
    if (-not $AllowMissing -and -not (Test-Path -LiteralPath $absolutePath)) {
        throw "Vendor Pack resource가 없습니다: $RelativePath"
    }
    return $absolutePath
}

function Copy-ExactFile {
    param(
        [string] $Source,
        [string] $Target
    )

    $targetDirectory = Split-Path -Parent $Target
    New-Item -ItemType Directory -Force -Path $targetDirectory | Out-Null
    [System.IO.File]::Copy($Source, $Target, $false)
}

$resultFullPath = [System.IO.Path]::GetFullPath($ResultDir)
if ((Test-PathWithin $resultFullPath $Root) -and
        -not (Test-PathWithin $resultFullPath (Join-Path $Root "build"))) {
    throw "선택 결과는 Git Source Tree가 아닌 Repository build/ 아래 또는 Repository 외부에만 생성할 수 있습니다: $resultFullPath"
}

$canonicalManifestRelative = "cpf-tools/db/vendor-pack-manifest.json"
$compatibilityManifestRelative = "specs/sql/vendor-resource-manifest.json"
$canonicalManifestPath = Resolve-RepositoryPath $canonicalManifestRelative
$compatibilityManifestPath = Resolve-RepositoryPath $compatibilityManifestRelative
$canonicalManifestHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $canonicalManifestPath).Hash
$compatibilityManifestHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $compatibilityManifestPath).Hash
if ($canonicalManifestHash -ne $compatibilityManifestHash) {
    throw "중앙 Vendor Pack manifest와 specs 호환 manifest가 다릅니다."
}

$manifest = Get-Content -Raw -Encoding UTF8 -LiteralPath $canonicalManifestPath | ConvertFrom-Json
if ($Vendor -notin @($manifest.supportedVendors)) {
    throw "지원하지 않는 DB Vendor입니다: $Vendor"
}
$pack = $manifest.vendors.$Vendor
if ($null -eq $pack) {
    throw "DB Vendor resource pack 정의가 없습니다: $Vendor"
}

$vendorRoot = Resolve-RepositoryPath ([string] $pack.vendorRoot)
$packPath = Resolve-RepositoryPath ([string] $pack.pack)
if (-not (Test-PathWithin $packPath $vendorRoot)) {
    throw "pack.json이 선택 Vendor Root 밖에 있습니다: $($pack.pack)"
}
$packDescriptor = Get-Content -Raw -Encoding UTF8 -LiteralPath $packPath | ConvertFrom-Json
if ([string] $packDescriptor.vendor -cne $Vendor) {
    throw "pack.json Vendor가 선택 Vendor와 일치하지 않습니다: selected=$Vendor pack=$($packDescriptor.vendor)"
}

$lifecycle = [ordered]@{}
$missing = [System.Collections.Generic.List[string]]::new()
foreach ($property in $pack.lifecycle.PSObject.Properties) {
    $relativePath = [string] $property.Value
    $absolutePath = Resolve-RepositoryPath $relativePath -AllowMissing
    $exists = Test-Path -LiteralPath $absolutePath
    if (-not $exists) { $missing.Add($relativePath) }
    $lifecycle[$property.Name] = [ordered]@{
        relativePath = $relativePath
        absolutePath = $absolutePath
        exists = $exists
    }
}

$runtimeRoot = Resolve-RepositoryPath ([string] $pack.runtimeRoot)
$domainTemplateRoot = Resolve-RepositoryPath ([string] $pack.domainTemplateRoot) -AllowMissing
$runtimeFiles = @(
    Get-ChildItem -LiteralPath $runtimeRoot -Recurse -File -ErrorAction SilentlyContinue |
        Where-Object { $_.Extension -in @(".sql", ".xml") }
)
$domainTemplateFiles = @(
    Get-ChildItem -LiteralPath $domainTemplateRoot -Recurse -File -ErrorAction SilentlyContinue |
        Where-Object { $_.Name.EndsWith(".template", [System.StringComparison]::Ordinal) }
)

$parityStatus = "미검증"
if (-not $SkipParityCheck) {
    $parityScript = Join-Path $Root "scripts\check-db-vendor-pack-parity.ps1"
    if (-not (Test-Path -LiteralPath $parityScript -PathType Leaf)) {
        throw "중앙 Vendor Pack parity gate가 없습니다: $parityScript"
    }
    & $parityScript -Root $Root -Vendor $Vendor -Quiet
    $parityStatus = "완료"
}

$overlay = $null
if ($AssembleOverlay) {
    $overlayId = "{0}-{1}-{2}" -f (
        (Get-Date).ToUniversalTime().ToString("yyyyMMddTHHmmssfffZ"),
        $PID,
        ([Guid]::NewGuid().ToString("N").Substring(0, 8))
    )
    $overlayRoot = Join-Path $resultFullPath "overlays\$Vendor\$overlayId"
    if (Test-Path -LiteralPath $overlayRoot) {
        throw "격리 Overlay 대상이 이미 존재합니다: $overlayRoot"
    }

    $externalPackRoot = Join-Path $overlayRoot "vendor-pack\$Vendor"
    foreach ($sourceFile in Get-ChildItem -LiteralPath $vendorRoot -Recurse -File) {
        $relativePath = [System.IO.Path]::GetRelativePath($vendorRoot, $sourceFile.FullName)
        Copy-ExactFile $sourceFile.FullName (Join-Path $externalPackRoot $relativePath)
    }

    $classpathRoot = Join-Path $overlayRoot "classpath"
    foreach ($sourceFile in $runtimeFiles) {
        $runtimeRelative = [System.IO.Path]::GetRelativePath($runtimeRoot, $sourceFile.FullName)
        $segments = $runtimeRelative -split "[\\/]"
        if ($segments.Count -lt 3) {
            throw "중앙 Runtime resource 경로가 계약과 다릅니다: $runtimeRelative"
        }
        $module = $segments[0]
        $kind = $segments[1]
        $tail = ($segments[2..($segments.Count - 1)] -join [System.IO.Path]::DirectorySeparatorChar)
        if ($kind -eq "repository" -and $sourceFile.Extension -eq ".sql") {
            $target = Join-Path $classpathRoot "sql\vendor\$Vendor\$module\$tail"
        } elseif ($kind -eq "mybatis" -and $sourceFile.Extension -eq ".xml") {
            $target = Join-Path $classpathRoot "mybatis\vendor\$Vendor\mapper\$module\$tail"
        } else {
            throw "중앙 Runtime resource kind/확장자 계약 위반: $runtimeRelative"
        }
        Copy-ExactFile $sourceFile.FullName $target
    }

    $overlayMarker = [ordered]@{
        generatedBy = "scripts/select-db-vendor-resources.ps1"
        generatedAt = (Get-Date).ToString("o")
        vendor = $Vendor
        sourceVendorRoot = $vendorRoot
        externalPackRoot = $externalPackRoot
        classpathRoot = $classpathRoot
        runtimeFileCount = $runtimeFiles.Count
    }
    [System.IO.File]::WriteAllText(
        (Join-Path $overlayRoot "overlay.sanitized.json"),
        ($overlayMarker | ConvertTo-Json -Depth 10) + [Environment]::NewLine,
        [System.Text.UTF8Encoding]::new($false)
    )
    $overlay = $overlayMarker
}

$result = [ordered]@{
    selectedAt = (Get-Date).ToString("o")
    selectionProperty = [string] $manifest.selectionProperty
    vendor = $Vendor
    status = [string] $pack.status
    client = [string] $pack.client
    vendorRoot = $vendorRoot
    pack = $packPath
    lifecycle = $lifecycle
    runtimeRoot = $runtimeRoot
    runtimeFileCount = $runtimeFiles.Count
    domainTemplateRoot = $domainTemplateRoot
    domainTemplateFileCount = $domainTemplateFiles.Count
    runtimeResourceContracts = $manifest.runtimeResourceContracts
    missingResources = @($missing)
    parity = $parityStatus
    executable = ($missing.Count -eq 0 -and [string] $pack.status -ne "미구현")
    overlay = $overlay
}

New-Item -ItemType Directory -Force -Path $resultFullPath | Out-Null
$resultPath = Join-Path $resultFullPath "active-db-resources.sanitized.json"
[System.IO.File]::WriteAllText(
    $resultPath,
    ($result | ConvertTo-Json -Depth 20) + [Environment]::NewLine,
    [System.Text.UTF8Encoding]::new($false)
)

Write-Host "CPF DB Vendor: $Vendor"
Write-Host "Central Vendor Root: $vendorRoot"
Write-Host "Resource pack status: $($pack.status)"
Write-Host "Runtime resources: $($runtimeFiles.Count)"
Write-Host "Domain templates: $($domainTemplateFiles.Count)"
Write-Host "Resource selection result: $resultPath"
if ($missing.Count -gt 0) {
    Write-Warning "Missing Vendor lifecycle resources: $($missing -join ', ')"
}
if ($RequireExecutable -and -not $result.executable) {
    throw "선택한 DB Vendor pack은 초기화 실행 가능한 상태가 아닙니다. vendor=$Vendor status=$($pack.status)"
}

$result
