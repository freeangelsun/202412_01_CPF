param(
    [Parameter(Mandatory = $true)][string]$PackageRoot,
    [Parameter(Mandatory = $true)][string]$RepositoryRoot,
    [switch]$SkipBaselineCheck
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$PackageRoot = (Resolve-Path -LiteralPath $PackageRoot).Path
$RepositoryRoot = (Resolve-Path -LiteralPath $RepositoryRoot).Path
if ($PackageRoot -eq $RepositoryRoot) { throw 'PackageRoot and RepositoryRoot must differ.' }

$expectedBaseline = '2daef3b7d2f82745d42d9b19804dde4bcac60edb'
$deleteManifest = 'cpf-tools/scripts/config/CPF_FINAL_COMPLETION_DELETE_PATHS.txt'
$required = @(
    'settings.gradle',
    'build.gradle',
    'cpf-tools/scripts/verify-cpf-final-completion.ps1',
    $deleteManifest
)
foreach ($item in $required) {
    if (-not (Test-Path -LiteralPath (Join-Path $PackageRoot $item))) { throw "Incomplete completion package: $item" }
}

if (-not $SkipBaselineCheck) {
    $git = Get-Command git -ErrorAction SilentlyContinue
    if ($null -eq $git) { throw 'git executable is required for baseline verification. Use -SkipBaselineCheck only after manual SHA verification.' }
    $head = (& $git.Source -C $RepositoryRoot rev-parse HEAD 2>$null).Trim()
    if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($head)) { throw 'Repository HEAD SHA를 확인할 수 없습니다.' }
    if ($head -ne $expectedBaseline) { throw "Completion package baseline mismatch. expected=$expectedBaseline actual=$head" }
}

# Replacement files are installed before any destructive cleanup.
Get-ChildItem -LiteralPath $PackageRoot -Recurse -File -Force |
    Where-Object { $_.FullName -ne (Join-Path $PackageRoot ($deleteManifest -replace '/', [IO.Path]::DirectorySeparatorChar)) } |
    ForEach-Object {
        $relative = $_.FullName.Substring($PackageRoot.Length).TrimStart([IO.Path]::DirectorySeparatorChar, [IO.Path]::AltDirectorySeparatorChar)
        $target = Join-Path $RepositoryRoot $relative
        $parent = Split-Path -Parent $target
        if ($parent) { New-Item -ItemType Directory -Force -Path $parent | Out-Null }
        Copy-Item -LiteralPath $_.FullName -Destination $target -Force
    }

$repositoryFull = [IO.Path]::GetFullPath($RepositoryRoot).TrimEnd([IO.Path]::DirectorySeparatorChar, [IO.Path]::AltDirectorySeparatorChar)
Get-Content -LiteralPath (Join-Path $PackageRoot ($deleteManifest -replace '/', [IO.Path]::DirectorySeparatorChar)) -Encoding UTF8 |
    Where-Object { $_ -and -not $_.Trim().StartsWith('#') } |
    ForEach-Object {
        $relative = $_.Trim()
        if ([IO.Path]::IsPathRooted($relative) -or $relative -match '(^|[\\/])\.\.([\\/]|$)') { throw "Unsafe delete path: $relative" }
        $target = [IO.Path]::GetFullPath((Join-Path $RepositoryRoot $relative))
        if (-not $target.StartsWith($repositoryFull + [IO.Path]::DirectorySeparatorChar, [StringComparison]::OrdinalIgnoreCase)) {
            throw "Delete path escaped repository: $relative"
        }
        if (Test-Path -LiteralPath $target) { Remove-Item -LiteralPath $target -Recurse -Force }
    }

Write-Host 'CPF final completion overlay applied. Commit/push/branch was NOT performed.'
Write-Host 'Run cpf-tools/scripts/verify-cpf-final-completion.ps1 before committing.'
