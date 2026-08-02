param(
    [Parameter(Mandatory = $true)][string]$ProjectRoot,
    [string]$OverlayRoot = (Resolve-Path "$PSScriptRoot/../..").Path
)
$ErrorActionPreference = 'Stop'
$ProjectRoot = (Resolve-Path $ProjectRoot).Path
$OverlayRoot = (Resolve-Path $OverlayRoot).Path
Set-Location $ProjectRoot

$changeManifest = Join-Path $OverlayRoot 'cpf-docs/work/manifest/CPF_QA38_CHANGE_MANIFEST.csv'
$deleteManifest = Join-Path $OverlayRoot 'cpf-docs/work/manifest/CPF_QA38_DELETE_MANIFEST.txt'
$emptyDirectoryManifest = Join-Path $OverlayRoot 'cpf-docs/work/manifest/CPF_QA38_EMPTY_DIRECTORY_MANIFEST.txt'
foreach ($required in @($changeManifest, $deleteManifest, $emptyDirectoryManifest)) {
    if (-not (Test-Path -LiteralPath $required -PathType Leaf)) { throw "필수 Manifest가 없습니다: $required" }
}

$protected = @(
    'cpf-docs/deliverables',
    'cpf-docs/guides',
    'cpf-docs/environment/docker',
    'cpf-tools/environment/docker-development-test'
)
$overlayPaths = @(Import-Csv -LiteralPath $changeManifest | ForEach-Object {
    ([string]$_.path).Trim().Replace('\','/')
} | Where-Object { $_ })
$deletePaths = @(Get-Content -LiteralPath $deleteManifest -Encoding UTF8 | ForEach-Object {
    ([string]$_).Trim().Replace('\','/')
} | Where-Object { $_ })

foreach ($path in @($overlayPaths + $deletePaths | Sort-Object -Unique)) {
    foreach ($prefix in $protected) {
        if ($path.Equals($prefix, [StringComparison]::OrdinalIgnoreCase) -or $path.StartsWith("$prefix/", [StringComparison]::OrdinalIgnoreCase)) {
            throw "보호 경로가 Overlay 또는 Delete Manifest에 포함됐습니다: $path"
        }
    }
}

foreach ($relative in $overlayPaths) {
    $source = Join-Path $OverlayRoot $relative
    if (-not (Test-Path -LiteralPath $source -PathType Leaf)) { throw "Overlay Source가 없습니다: $relative" }
    $destination = Join-Path $ProjectRoot $relative
    $parent = Split-Path -Parent $destination
    if ($parent -and -not (Test-Path -LiteralPath $parent)) {
        New-Item -ItemType Directory -Path $parent -Force | Out-Null
    }
    Copy-Item -LiteralPath $source -Destination $destination -Force
}

foreach ($relative in $deletePaths) {
    $target = Join-Path $ProjectRoot $relative
    if (Test-Path -LiteralPath $target -PathType Leaf) {
        Remove-Item -LiteralPath $target -Force
    }
}

$emptyCandidates = @(Get-Content -LiteralPath $emptyDirectoryManifest -Encoding UTF8 | ForEach-Object {
    ([string]$_).Trim()
} | Where-Object { $_ } | Sort-Object { ($_ -split '[\\/]').Count } -Descending)
foreach ($relative in $emptyCandidates) {
    $target = Join-Path $ProjectRoot $relative
    if (Test-Path -LiteralPath $target -PathType Container) {
        if (@(Get-ChildItem -LiteralPath $target -Force).Count -eq 0) {
            Remove-Item -LiteralPath $target -Force
        }
    }
}

git diff --check
if ($LASTEXITCODE -ne 0) { throw 'git diff --check 실패' }
Write-Host "QA38 R4 Overlay 적용 완료: 덮어쓰기 $($overlayPaths.Count)개 / 삭제 대상 $($deletePaths.Count)개"
git -c core.quotepath=false status --short --branch
