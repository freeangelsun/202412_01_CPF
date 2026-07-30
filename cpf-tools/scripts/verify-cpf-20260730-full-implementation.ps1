param(
    [string] $RepoRoot = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [switch] $SkipDatabaseSync,
    [switch] $SkipFrontend,
    [switch] $SkipRuntime,
    [switch] $RunDatabaseLifecycle,
    [string[]] $DatabaseProfilePath = @(),
    [switch] $RecordEvidence
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
if ($PSVersionTable.PSVersion.Major -lt 7) { throw 'pwsh 7 이상이 필요합니다.' }
$RepoRoot = (Resolve-Path -LiteralPath $RepoRoot).Path
$head = (& git -C $RepoRoot rev-parse HEAD).Trim().ToLowerInvariant()
$branch = (& git -C $RepoRoot branch --show-current).Trim()
if ($branch -ne 'master') { throw "master Branch에서 실행해야 합니다. current=$branch" }
if ($head -notmatch '^[0-9a-f]{40}$') { throw "HEAD SHA를 확인할 수 없습니다: $head" }

$dirty = @(& git -C $RepoRoot status --porcelain)
if ($RecordEvidence -and $dirty.Count -gt 0) {
    throw 'Exact-SHA Evidence는 Working Tree Clean 상태에서만 기록합니다. 먼저 변경을 검토하고 사용자가 Commit한 뒤 -RecordEvidence를 사용하십시오.'
}

$gradle = if ($IsWindows) { Join-Path $RepoRoot 'gradlew.bat' } else { Join-Path $RepoRoot 'gradlew' }
$pwsh = (Get-Command pwsh -ErrorAction Stop).Source
$evidenceRunner = Join-Path $RepoRoot 'cpf-tools/scripts/invoke-cpf-evidence-command.ps1'

function Invoke-CpfStep {
    param(
        [Parameter(Mandatory=$true)][string] $Name,
        [Parameter(Mandatory=$true)][string[]] $RequirementIds,
        [Parameter(Mandatory=$true)][string] $Executable,
        [string[]] $Arguments = @(),
        [Parameter(Mandatory=$true)][string] $SanitizedCommand,
        [string] $Profile = 'default'
    )
    Write-Host "==> $Name"
    if ($RecordEvidence) {
        & $pwsh -NoProfile -ExecutionPolicy Bypass -File $evidenceRunner `
            -Name $Name -RequirementIds $RequirementIds -Executable $Executable `
            -ArgumentList $Arguments -SanitizedCommand $SanitizedCommand `
            -Profile $Profile -Root $RepoRoot
    } else {
        Push-Location $RepoRoot
        try { & $Executable @Arguments } finally { Pop-Location }
    }
    if ($LASTEXITCODE -ne 0) { throw "$Name failed (exit=$LASTEXITCODE)" }
}

Push-Location $RepoRoot
try {
    Write-Host "CPF verification start: branch=$branch head=$head dirty=$($dirty.Count)"

    Invoke-CpfStep -Name 'CPF 20260730 overlay structure' `
        -RequirementIds @('WP00-R002','WP15-R001','WP16-R020') `
        -Executable $pwsh -Arguments @('-NoProfile','-ExecutionPolicy','Bypass','-File','cpf-tools/scripts/verify-cpf-20260730-overlay-structure.ps1','-Root',$RepoRoot) `
        -SanitizedCommand 'pwsh -File cpf-tools/scripts/verify-cpf-20260730-overlay-structure.ps1 -Root <repo>' `
        -Profile 'overlay-structure'

    if (-not $SkipDatabaseSync) {
        foreach ($script in @(
            'cpf-tools/scripts/sync-database-artifacts.ps1',
            'cpf-tools/scripts/build-all-install-sql.ps1'
        )) {
            if (-not (Test-Path -LiteralPath $script -PathType Leaf)) { throw "DB canonical script가 없습니다: $script" }
            Invoke-CpfStep -Name "DB canonical: $script" -RequirementIds @('WP15-R001','WP15-R002','WP15-R003') `
                -Executable $pwsh -Arguments @('-NoProfile','-ExecutionPolicy','Bypass','-File',$script,'-Root',$RepoRoot) `
                -SanitizedCommand "pwsh -File $script -Root <repo>" -Profile 'canonical-db'
        }
    }

    Invoke-CpfStep -Name 'Low-cost source and ownership gates' `
        -RequirementIds @('WP00-R008','WP01-R003','WP01-R010','WP02-R011','WP15-R020') `
        -Executable $gradle -Arguments @('verifyCpfFinalSourceGates','checkRuntimeQueryContracts','checkSqlCanonical','--no-daemon') `
        -SanitizedCommand './gradlew verifyCpfFinalSourceGates checkRuntimeQueryContracts checkSqlCanonical --no-daemon'

    Invoke-CpfStep -Name 'Full Java clean test assemble' `
        -RequirementIds @('WP01-R005','WP01-R006','WP01-R007','WP16-R001') `
        -Executable $gradle -Arguments @('clean','test','assemble','--no-daemon') `
        -SanitizedCommand './gradlew clean test assemble --no-daemon'

    if (-not $SkipFrontend) {
        Invoke-CpfStep -Name 'ADM/BZA frontend verification' `
            -RequirementIds @('WP01-R008','WP01-R009','WP03-R019','WP16-R005') `
            -Executable $gradle -Arguments @(':cpf-admin:frontendVerify',':cpf-biz-admin:frontendVerify','--no-daemon') `
            -SanitizedCommand './gradlew :cpf-admin:frontendVerify :cpf-biz-admin:frontendVerify --no-daemon'
    }

    Invoke-CpfStep -Name 'Full quality gate' `
        -RequirementIds @('WP01-R010','WP01-R016','WP01-R017','WP16-R020') `
        -Executable $gradle -Arguments @('qualityGate','--no-daemon') `
        -SanitizedCommand './gradlew qualityGate --no-daemon'

    $finalArgs = @('-NoProfile','-ExecutionPolicy','Bypass','-File','cpf-tools/scripts/verify-cpf-final-completion.ps1','-RepoRoot',$RepoRoot,'-ExpectedSourceSha',$head)
    if ($SkipFrontend) { $finalArgs += '-SkipFrontend' }
    if ($SkipRuntime) { $finalArgs += '-SkipRuntime' }
    if ($RunDatabaseLifecycle) {
        $finalArgs += '-RunDatabaseLifecycle'
        foreach ($profile in $DatabaseProfilePath) { $finalArgs += @('-DatabaseProfilePath',$profile) }
    }
    Invoke-CpfStep -Name 'CPF final completion gate' `
        -RequirementIds @('WP16-R021','WP16-R022','WP17-R001') `
        -Executable $pwsh -Arguments $finalArgs `
        -SanitizedCommand 'pwsh -File cpf-tools/scripts/verify-cpf-final-completion.ps1 -RepoRoot <repo> -ExpectedSourceSha <head>' `
        -Profile 'final-completion'

    Write-Host "CPF verification PASS: head=$head"
} finally {
    Pop-Location
}
