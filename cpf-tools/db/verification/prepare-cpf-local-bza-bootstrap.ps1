[CmdletBinding()]
param(
 [Parameter(Mandatory=$true)][string]$VerifierRunId,
 [Parameter(Mandatory=$true)][string]$RuntimeDbResultPath,
 [Parameter(Mandatory=$true)][string]$SecretDirectory,
 [Parameter(Mandatory=$true)][string]$ResultPath,
 [string]$LoginId='backoffice-full-local',
 [string]$OperatorName='Backoffice FullLocal Operator',
 [string]$RoleCode='MBW_MANAGER',
 [string]$EnvironmentCode='local',
 [string]$ActiveProfiles='local,local-integrated'
)
$ErrorActionPreference='Stop';Set-StrictMode -Version Latest
$delegate=Join-Path $PSScriptRoot 'prepare-cpf-local-backoffice-bootstrap.ps1'
if(-not(Test-Path -LiteralPath $delegate -PathType Leaf)){throw "Canonical Backoffice bootstrap verifier missing: $delegate"}
& powershell -NoProfile -ExecutionPolicy Bypass -File $delegate -VerifierRunId $VerifierRunId -RuntimeDbResultPath $RuntimeDbResultPath -SecretDirectory $SecretDirectory -ResultPath $ResultPath -LoginId $LoginId -OperatorName $OperatorName -RoleCode $RoleCode -EnvironmentCode $EnvironmentCode -ActiveProfiles $ActiveProfiles
if($LASTEXITCODE -ne 0){throw "Canonical Backoffice bootstrap verifier failed exit=$LASTEXITCODE"}
Write-Host "Legacy BZA bootstrap entry delegated to current Backoffice/MBW verifier"
