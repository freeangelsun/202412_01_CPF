[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)][string] $PackageRoot,
    [string] $ProjectRoot = (Get-Location).Path,
    [string] $ExpectedBaseSha = '9594c8d5d9b1127a4e2694d0ec2f4add9475fc7e',
    [switch] $AllowDirty
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
if ($ExpectedBaseSha -notmatch '^[0-9a-fA-F]{40}$') { throw 'ExpectedBaseSha must be a full SHA.' }
$PackageRoot = (Resolve-Path -LiteralPath $PackageRoot).Path
$ProjectRoot = (Resolve-Path -LiteralPath $ProjectRoot).Path
if (-not (Test-Path -LiteralPath (Join-Path $ProjectRoot '.git'))) { throw "ProjectRoot is not a Git worktree: $ProjectRoot" }
$head = (& git -C $ProjectRoot rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0 -or $head -ne $ExpectedBaseSha) {
    throw "QA31 overlay base mismatch. expected=$ExpectedBaseSha actual=$head. Fetch/review latest master before applying."
}
if (-not $AllowDirty -and @(& git -C $ProjectRoot status --porcelain).Count -ne 0) {
    throw 'Working tree is dirty. Commit/stash unrelated changes or pass -AllowDirty only after manual conflict review.'
}
$deleteManifest = Join-Path $PackageRoot 'cpf-docs/work/manifest/CPF_20260730_QA31_DELETE_MANIFEST.txt'
if (-not (Test-Path -LiteralPath $deleteManifest -PathType Leaf)) {
    throw "QA31 delete manifest is missing: $deleteManifest"
}
$excluded = @(
    '(^|/)README[^/]*$', '^cpf-docs/guides/', '^cpf-tools/README\.md$',
    '^cpf-docs/assets/readme/', '^cpf-docs/work/overlay/20260730-readme-guides/'
)
Get-ChildItem -LiteralPath $PackageRoot -Recurse -File | ForEach-Object {
    $relative = [IO.Path]::GetRelativePath($PackageRoot, $_.FullName).Replace('\','/')
    if ($relative.StartsWith('../') -or $relative.Contains('/../')) { throw "Unsafe package path: $relative" }
    if ($excluded | Where-Object { $relative -match $_ }) { throw "README/Guide excluded file is present in package: $relative" }
    $destination = Join-Path $ProjectRoot $relative
    New-Item -ItemType Directory -Path (Split-Path $destination -Parent) -Force | Out-Null
    Copy-Item -LiteralPath $_.FullName -Destination $destination -Force
}
Get-Content -LiteralPath $deleteManifest -Encoding UTF8 |
    Where-Object { $_ -and -not $_.Trim().StartsWith('#') } |
    ForEach-Object {
        $relative = $_.Trim().Replace('\','/')
        if ($relative.StartsWith('/') -or $relative.StartsWith('../') -or $relative.Contains('/../')) {
            throw "Unsafe delete manifest path: $relative"
        }
        $target = Join-Path $ProjectRoot $relative
        Remove-Item -LiteralPath $target -Force -ErrorAction SilentlyContinue
    }
& git -C $ProjectRoot diff --check
if ($LASTEXITCODE -ne 0) { throw 'git diff --check failed after applying QA31 overlay.' }
Write-Host "CPF QA31 development overlay applied on base=$head. Review git diff before commit/push."
