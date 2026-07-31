param(
    [Parameter(Mandatory=$false)][string]$Root = "."
)
$ErrorActionPreference = "Stop"
$rootPath = (Resolve-Path -LiteralPath $Root).Path
$manifest = Join-Path $rootPath "cpf-docs/work/manifest/CPF_GUIDE_REBUILD_DELETE_MANIFEST.txt"
if (-not (Test-Path -LiteralPath $manifest)) { throw "삭제 Manifest가 없습니다: $manifest" }
$deleted = 0
Get-Content -LiteralPath $manifest -Encoding UTF8 |
    Where-Object { $_ -and -not $_.Trim().StartsWith('#') } |
    ForEach-Object {
        $relative = $_.Trim().Replace('/', [IO.Path]::DirectorySeparatorChar)
        if ([IO.Path]::IsPathRooted($relative) -or $relative.Contains('..')) { throw "안전하지 않은 경로: $_" }
        $target = Join-Path $rootPath $relative
        if (Test-Path -LiteralPath $target) {
            Remove-Item -LiteralPath $target -Force
            $deleted++
        }
    }
Write-Host "기존 Guide 삭제: $deleted 파일"
& (Join-Path $rootPath "cpf-tools/scripts/verify-cpf-guide-system.ps1") -Root $rootPath -RequireLegacyRemoved
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
$docLink = Join-Path $rootPath "cpf-tools/scripts/check-document-links.ps1"
if (Test-Path -LiteralPath $docLink) {
    & $docLink
    if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
}
git -C $rootPath diff --check
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
git -C $rootPath status --short
