param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $BundleRoot = ''
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Root = (Resolve-Path -LiteralPath $Root).Path
$failures = [Collections.Generic.List[string]]::new()
function Fail([string]$Message) { $failures.Add($Message) }

$builder = Join-Path $Root 'cpf-tools/build/tools/build-cpf-offline-artifact-bundle.ps1'
if (-not (Test-Path -LiteralPath $builder -PathType Leaf)) { Fail 'Offline bundle builder is missing.' }
else {
    $text = Get-Content -LiteralPath $builder -Raw -Encoding UTF8
    foreach ($required in @(
        'db/vendor-pack',
        'select-db-vendor-resources.ps1',
        '-RequireExecutable',
        'supportedVendors',
        'cpf.db.resource-root',
        'db-vendor-packs.sanitized.json',
        'check-offline-db-resource-pack.ps1'
    )) {
        if (-not $text.Contains($required)) { Fail "Offline bundle builder contract is missing: $required" }
    }
}

$manifestPath = Join-Path $Root 'cpf-tools/db/vendor-pack-manifest.json'
if (-not (Test-Path -LiteralPath $manifestPath -PathType Leaf)) { Fail 'DB vendor-pack-manifest.json is missing.' }
else {
    $manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
    $official = @($manifest.officialVendors)
    if (($official -join ',') -ne 'mariadb,postgresql,oracle') { Fail "Official DB vendors must be mariadb,postgresql,oracle. actual=$($official -join ',')" }

    if (-not [string]::IsNullOrWhiteSpace($BundleRoot)) {
        $bundle = (Resolve-Path -LiteralPath $BundleRoot).Path
        $bundleManifest = Join-Path $bundle 'metadata/db-vendor-pack-manifest.json'
        $bundlePackMetadata = Join-Path $bundle 'metadata/db-vendor-packs.sanitized.json'
        $bundleHashes = Join-Path $bundle 'SHA256SUMS.txt'
        if (-not (Test-Path -LiteralPath $bundleManifest -PathType Leaf)) { Fail 'Bundle DB vendor manifest is missing.' }
        if (-not (Test-Path -LiteralPath $bundlePackMetadata -PathType Leaf)) { Fail 'Bundle DB vendor pack metadata is missing.' }
        if (-not (Test-Path -LiteralPath $bundleHashes -PathType Leaf)) { Fail 'Bundle SHA256SUMS.txt is missing.' }
        if (Test-Path -LiteralPath $bundleManifest -PathType Leaf) {
            if ((Get-FileHash -LiteralPath $bundleManifest -Algorithm SHA256).Hash -ne (Get-FileHash -LiteralPath $manifestPath -Algorithm SHA256).Hash) {
                Fail 'Bundle DB vendor manifest drifted from repository canonical manifest.'
            }
        }
        foreach ($vendor in @($manifest.supportedVendors)) {
            $vendorInfo = $manifest.vendors.$vendor
            if ($null -eq $vendorInfo) { Fail "Supported vendor metadata missing: $vendor"; continue }
            $sourceRoot = Join-Path $Root (([string]$vendorInfo.vendorRoot).Replace('/', [IO.Path]::DirectorySeparatorChar))
            $targetRoot = Join-Path $bundle "db/vendor-pack/$vendor"
            if (-not (Test-Path -LiteralPath $targetRoot -PathType Container)) { Fail "Bundle vendor pack missing: $vendor"; continue }
            $sourceFiles = @(Get-ChildItem -LiteralPath $sourceRoot -Recurse -File)
            $targetFiles = @(Get-ChildItem -LiteralPath $targetRoot -Recurse -File)
            if ($sourceFiles.Count -ne $targetFiles.Count) { Fail "Bundle vendor pack file count mismatch: vendor=$vendor source=$($sourceFiles.Count) target=$($targetFiles.Count)" }
            foreach ($sourceFile in $sourceFiles) {
                $relative = [IO.Path]::GetRelativePath($sourceRoot, $sourceFile.FullName)
                $targetFile = Join-Path $targetRoot $relative
                if (-not (Test-Path -LiteralPath $targetFile -PathType Leaf)) { Fail "Bundle vendor pack file missing: vendor=$vendor file=$relative"; continue }
                if ((Get-FileHash -LiteralPath $sourceFile.FullName -Algorithm SHA256).Hash -ne (Get-FileHash -LiteralPath $targetFile -Algorithm SHA256).Hash) {
                    Fail "Bundle vendor pack checksum mismatch: vendor=$vendor file=$relative"
                }
            }
        }
    }
}

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Error $_ }
    exit 1
}
if ([string]::IsNullOrWhiteSpace($BundleRoot)) {
    Write-Host 'CPF offline DB resource pack gate PASS_STATIC_ONLY.'
} else {
    Write-Host 'CPF offline DB resource pack gate PASS: bundle pack files and SHA match canonical source.'
}
exit 0
