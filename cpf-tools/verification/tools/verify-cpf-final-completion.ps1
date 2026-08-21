[CmdletBinding()]
param(
    [string] $RepoRoot = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [ValidateSet('local','dev','test','stg','prod')]
    [string] $ResourceProfile = 'local',
    [string] $OutputRoot = '',
    [string] $BaselineSourceZipSha256 = $env:CPF_BASELINE_SOURCE_ZIP_SHA256,
    [switch] $IncludePerformanceLoad,
    [switch] $AllowDestructiveDbRollback
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
if ($PSVersionTable.PSVersion.Major -lt 7) {
    throw 'CPF Canonical Final Gate는 pwsh 7 이상이 필요합니다. Windows PowerShell 5.1 wrapper는 pwsh 7로 위임해야 합니다.'
}
$RepoRoot = (Resolve-Path -LiteralPath $RepoRoot).Path
$canonical = Join-Path $RepoRoot 'cpf-tools\verification\tools\run-cpf-local-full-validation.ps1'
if (-not (Test-Path -LiteralPath $canonical -PathType Leaf)) {
    throw "Canonical FullLocal Gate가 없습니다: $canonical"
}
if ($BaselineSourceZipSha256 -notmatch '^[0-9a-fA-F]{64}$') {
    throw 'BaselineSourceZipSha256은 사용자 Local Working Tree ZIP의 64자리 SHA-256이어야 합니다.'
}
$args = @(
    '-NoProfile','-File',$canonical,
    '-RepoRoot',$RepoRoot,
    '-ResourceProfile',$ResourceProfile,
    '-FullLocal','-StrictExit',
    '-BaselineSourceZipSha256',$BaselineSourceZipSha256
)
if (-not [string]::IsNullOrWhiteSpace($OutputRoot)) { $args += @('-OutputRoot',$OutputRoot) }
if ($IncludePerformanceLoad) { $args += '-IncludePerformanceLoad' }
if ($AllowDestructiveDbRollback) { $args += '-AllowDestructiveDbRollback' }
& pwsh @args
if ($LASTEXITCODE -ne 0) { throw "CPF Canonical Final Gate failed (exit=$LASTEXITCODE)" }
