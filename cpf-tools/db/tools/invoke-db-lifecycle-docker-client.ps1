[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string] $RequestPath
)

if ($PSVersionTable.PSVersion.Major -lt 7) {
    throw 'CPF Docker DB lifecycle adapter requires pwsh 7 or later.'
}
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

function Assert-CpfAdapterScalar {
    param([string] $Value, [string] $Name)
    if ([string]::IsNullOrWhiteSpace($Value) -or $Value -match '[\x00-\x1F\x7F]') {
        throw "$Name is required and must not contain control characters."
    }
    return $Value
}

function Invoke-CpfAdapterMigration {
    param(
        [ValidateSet('upgrade', 'rollback')][string] $Direction,
        [string] $ExpectedPlanSha256,
        [string] $ResultPath
    )
    if ($ExpectedPlanSha256 -notmatch '^[0-9a-fA-F]{64}$') {
        throw "Docker adapter requires a reviewed $Direction plan hash."
    }
    $arguments = @{
        Root = $root
        ProfilePath = $profilePath
        Direction = $Direction
        MigrationVersion = @($versions)
        Modules = @($modules)
        ResultPath = $ResultPath
        Apply = $true
        ConfirmApply = $true
        ConfirmApplicationsStopped = $true
        ConfirmRollbackReady = $true
        ExpectedPlanSha256 = $ExpectedPlanSha256
        BackupManifestPath = @($backupManifestPaths)
        Operator = $operator
        Reason = $reason
        ApprovalReference = $approvalReference
        VerifierOwnedDisposable = [bool]$verifierOwnedDisposable
        VerifierRunId = $verifierRunId
    }
    & $migrationTool @arguments
}

$requestAbsolute = (Resolve-Path -LiteralPath $RequestPath).Path
$request = Get-Content -LiteralPath $requestAbsolute -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 100
if ([int]$request.schemaVersion -ne 1) { throw 'Unsupported Docker DB lifecycle request schemaVersion.' }
$action = [string]$request.action
if ($action -notin @('FreshInstall', 'Upgrade', 'RollbackReapply')) {
    throw "Unsupported Docker DB lifecycle action: $action"
}
$root = (Resolve-Path -LiteralPath ([string]$request.root)).Path
$profilePath = (Resolve-Path -LiteralPath ([string]$request.profilePath)).Path
$resultRoot = [IO.Path]::GetFullPath([string]$request.resultRoot)
[IO.Directory]::CreateDirectory($resultRoot) | Out-Null
$initializeTool = Join-Path $root 'cpf-tools/db/tools/initialize-cpf-database.ps1'
$migrationTool = Join-Path $root 'cpf-tools/db/tools/invoke-platform-database-migration.ps1'
foreach ($tool in @($initializeTool, $migrationTool)) {
    if (-not (Test-Path -LiteralPath $tool -PathType Leaf)) { throw "Official DB lifecycle tool is missing: $tool" }
}

$versions = @($request.migrationVersions | ForEach-Object { [int]$_ })
$modules = @($request.modules | ForEach-Object { [string]$_ })
$backupManifestPaths = @($request.backupManifestPaths | ForEach-Object { [string]$_ })
$operator = [string]$request.operator
$reason = [string]$request.reason
$approvalReference = [string]$request.approvalReference
$verifierOwnedDisposable = $false
$verifierProperty = $request.PSObject.Properties['verifierOwnedDisposable']
if($null -ne $verifierProperty){$verifierOwnedDisposable=[bool]$verifierProperty.Value}
$verifierRunId = ''
$runProperty = $request.PSObject.Properties['verifierRunId']
if($null -ne $runProperty){$verifierRunId=[string]$runProperty.Value}
[void](Assert-CpfAdapterScalar $operator 'Operator')
[void](Assert-CpfAdapterScalar $reason 'Reason')
[void](Assert-CpfAdapterScalar $approvalReference 'ApprovalReference')

switch ($action) {
    'FreshInstall' {
        & $initializeTool `
            -Root $root `
            -ProfilePath $profilePath `
            -ResultDir (Join-Path $resultRoot 'fresh-install') `
            -All `
            -SeedMode product `
            -RequireRun
    }
    'Upgrade' {
        Invoke-CpfAdapterMigration `
            -Direction upgrade `
            -ExpectedPlanSha256 ([string]$request.expectedUpgradePlanSha256) `
            -ResultPath (Join-Path $resultRoot 'upgrade.json')
    }
    'RollbackReapply' {
        Invoke-CpfAdapterMigration `
            -Direction rollback `
            -ExpectedPlanSha256 ([string]$request.expectedRollbackPlanSha256) `
            -ResultPath (Join-Path $resultRoot 'rollback.json')
        Invoke-CpfAdapterMigration `
            -Direction upgrade `
            -ExpectedPlanSha256 ([string]$request.expectedUpgradePlanSha256) `
            -ResultPath (Join-Path $resultRoot 'reapply.json')
    }
}
