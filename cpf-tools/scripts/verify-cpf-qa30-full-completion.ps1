param(
    [string] $RepoRoot = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [Parameter(Mandatory = $true)][string] $MariaDbProfilePath,
    [Parameter(Mandatory = $true)][string] $PostgreSqlProfilePath,
    [Parameter(Mandatory = $true)][string] $OracleProfilePath,
    [string] $ExpectedSourceSha = ''
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

if ($PSVersionTable.PSVersion.Major -lt 7) { throw 'pwsh 7 이상이 필요합니다.' }
$RepoRoot = (Resolve-Path -LiteralPath $RepoRoot).Path
foreach ($profile in @($MariaDbProfilePath,$PostgreSqlProfilePath,$OracleProfilePath)) {
    if (-not (Test-Path -LiteralPath $profile -PathType Leaf)) { throw "DB Profile이 없습니다: $profile" }
}
if ([string]::IsNullOrWhiteSpace($ExpectedSourceSha)) {
    $ExpectedSourceSha = (& git -C $RepoRoot rev-parse HEAD).Trim()
}
if ($ExpectedSourceSha -notmatch '^[0-9a-fA-F]{40}$') { throw "정확한 40자리 SHA가 필요합니다: $ExpectedSourceSha" }

& pwsh -NoProfile -File (Join-Path $RepoRoot 'cpf-tools/scripts/verify-cpf-final-completion.ps1') `
    -RepoRoot $RepoRoot `
    -RunDatabaseLifecycle `
    -DatabaseProfilePath @($MariaDbProfilePath,$PostgreSqlProfilePath,$OracleProfilePath) `
    -RunGitHubGovernance `
    -ExpectedSourceSha $ExpectedSourceSha `
    -RequireFullCompletion
if ($LASTEXITCODE -ne 0) { throw "CPF QA30 전체 완료 Gate 실패: exit=$LASTEXITCODE" }
