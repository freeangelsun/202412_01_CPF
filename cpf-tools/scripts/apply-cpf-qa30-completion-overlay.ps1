param(
    [Parameter(Mandatory = $true)][string] $ProjectRoot,
    [string] $OverlayRoot = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path,
    [string] $ExpectedBaseSha = '0c502b917cd2185cf1ff097c5beac3e5aefb00ac'
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

if ($PSVersionTable.PSVersion.Major -lt 7) { throw 'pwsh 7 이상이 필요합니다.' }
$ProjectRoot = (Resolve-Path -LiteralPath $ProjectRoot).Path
$OverlayRoot = (Resolve-Path -LiteralPath $OverlayRoot).Path
if (-not (Test-Path -LiteralPath (Join-Path $ProjectRoot '.git') -PathType Container)) {
    throw "CPF Git Repository Root가 아닙니다: $ProjectRoot"
}
$manifestPath = Join-Path $OverlayRoot 'cpf-docs/evidence/20260730_qa30/overlay-manifest.json'
if (-not (Test-Path -LiteralPath $manifestPath -PathType Leaf)) {
    throw "Overlay Manifest가 없습니다: $manifestPath"
}
$manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 30
$checksumPath = Join-Path $OverlayRoot 'cpf-docs/evidence/20260730_qa30/SHA256SUMS.txt'
if (-not (Test-Path -LiteralPath $checksumPath -PathType Leaf)) {
    throw "Overlay SHA256 Manifest가 없습니다: $checksumPath"
}
$overlayPrefix = $OverlayRoot.TrimEnd([IO.Path]::DirectorySeparatorChar, [IO.Path]::AltDirectorySeparatorChar) + [IO.Path]::DirectorySeparatorChar
$listed = [Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
foreach ($line in Get-Content -LiteralPath $checksumPath -Encoding UTF8) {
    if ([string]::IsNullOrWhiteSpace($line) -or $line.TrimStart().StartsWith('#')) { continue }
    if ($line -notmatch '^([0-9a-fA-F]{64})\s{2}(.+)$') {
        throw "잘못된 SHA256SUMS 행: $line"
    }
    $expectedHash = $Matches[1].ToLowerInvariant()
    $relative = $Matches[2].Replace('/', [IO.Path]::DirectorySeparatorChar)
    $candidate = [IO.Path]::GetFullPath((Join-Path $OverlayRoot $relative))
    if (-not $candidate.StartsWith($overlayPrefix, [StringComparison]::OrdinalIgnoreCase)) {
        throw "SHA256SUMS 경로가 Overlay Root를 벗어납니다: $relative"
    }
    if (-not (Test-Path -LiteralPath $candidate -PathType Leaf)) {
        throw "SHA256SUMS 대상 파일이 없습니다: $relative"
    }
    if (-not $listed.Add($relative.Replace([IO.Path]::DirectorySeparatorChar, '/'))) {
        throw "SHA256SUMS 중복 경로: $relative"
    }
    $actualHash = (Get-FileHash -LiteralPath $candidate -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($actualHash -cne $expectedHash) {
        throw "Overlay 파일 Hash 불일치: $relative expected=$expectedHash actual=$actualHash"
    }
}
$actualFiles = Get-ChildItem -LiteralPath $OverlayRoot -Recurse -Force -File |
    Where-Object { $_.FullName -cne $checksumPath } |
    ForEach-Object { $_.FullName.Substring($overlayPrefix.Length).Replace([IO.Path]::DirectorySeparatorChar, '/') }
$missingHash = @($actualFiles | Where-Object { -not $listed.Contains($_) })
$orphanHash = @($listed | Where-Object { $_ -notin $actualFiles })
if ($missingHash.Count -gt 0 -or $orphanHash.Count -gt 0) {
    throw "Overlay SHA256 Manifest 범위 불일치: missing=$($missingHash -join ',') orphan=$($orphanHash -join ',')"
}
if ([string]$manifest.basisSha -cne $ExpectedBaseSha) {
    throw "Overlay 기준 SHA 불일치: manifest=$($manifest.basisSha) expected=$ExpectedBaseSha"
}
$headSha = (& git -C $ProjectRoot rev-parse HEAD).Trim().ToLowerInvariant()
if ($LASTEXITCODE -ne 0) { throw 'git rev-parse HEAD 실패' }
if ($headSha -cne $ExpectedBaseSha.ToLowerInvariant()) {
    throw "Overlay는 정확한 기준 SHA에만 적용합니다: expected=$ExpectedBaseSha actual=$headSha"
}
$dirty = @(& git -C $ProjectRoot status --porcelain=v1)
if ($LASTEXITCODE -ne 0) { throw 'git status 확인 실패' }
if ($dirty.Count -gt 0) {
    throw "Overlay 적용 전 Working Tree가 Clean 상태여야 합니다: $($dirty -join '; ')"
}

Get-ChildItem -LiteralPath $OverlayRoot -Force | ForEach-Object {
    Copy-Item -LiteralPath $_.FullName -Destination $ProjectRoot -Recurse -Force
}

& pwsh -NoProfile -ExecutionPolicy Bypass -File `
    (Join-Path $ProjectRoot 'cpf-tools/scripts/cleanup-qa30-obsolete-gateway-model.ps1') `
    -Root $ProjectRoot
if ($LASTEXITCODE -ne 0) { throw '구형 Gateway 모델 Cleanup 실패' }

& git -C $ProjectRoot diff --check
if ($LASTEXITCODE -ne 0) { throw 'git diff --check 실패' }

$changed = @(& git -C $ProjectRoot status --short)
if ($LASTEXITCODE -ne 0) { throw 'git status --short 실패' }
Write-Host "CPF QA30 Completion Overlay 적용 PASS. basisSha=$ExpectedBaseSha changed=$($changed.Count)"
$changed | ForEach-Object { Write-Host $_ }
Write-Host 'Commit·Push는 생성하지 않았습니다.'
