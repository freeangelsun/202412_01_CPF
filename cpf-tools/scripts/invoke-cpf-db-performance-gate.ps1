[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][ValidateSet('mariadb','postgresql','oracle')][string]$Vendor,
    [Parameter(Mandatory = $true)][string]$EvidencePath,
    [Parameter(Mandatory = $true)][ValidatePattern('^[0-9a-fA-F]{64}$')][string]$ExpectedEvidenceSha256,
    [Parameter(Mandatory = $true)][ValidateNotNullOrEmpty()][string]$Operator,
    [Parameter(Mandatory = $true)][ValidateNotNullOrEmpty()][string]$Reason,
    [Parameter(Mandatory = $true)][ValidateNotNullOrEmpty()][string]$ApprovedBy,
    [Parameter(Mandatory = $true)][switch]$ConfirmRepresentativeData,
    [Parameter(Mandatory = $true)][switch]$ConfirmSanitizedEvidence,
    [string]$OutputPath
)
Set-StrictMode -Version Latest
$ErrorActionPreference='Stop'
if($Operator -eq $ApprovedBy){ throw 'DB performance review requires independent approval.' }
if($Reason.Trim().Length -lt 10){ throw 'Reason must contain at least 10 characters.' }
if(-not $ConfirmRepresentativeData -or -not $ConfirmSanitizedEvidence){ throw 'Representative data and sanitized evidence confirmations are required.' }
$root=(Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$policy=Join-Path $root 'cpf-tools/db/cpf-db-performance-policy.json'
$verifier=Join-Path $root 'cpf-tools/scripts/verify-cpf-db-performance-evidence.py'
if(-not (Test-Path -LiteralPath $EvidencePath -PathType Leaf)){ throw "Performance evidence not found: $EvidencePath" }
$evidence=Get-Content -LiteralPath $EvidencePath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 100
if([string]$evidence.vendor -ne $Vendor){ throw "Evidence vendor mismatch. expected=$Vendor actual=$($evidence.vendor)" }
$arguments=@($verifier,'--policy',$policy,'--evidence',$EvidencePath,'--expected-evidence-sha256',$ExpectedEvidenceSha256)
if($OutputPath){ $arguments += @('--output',$OutputPath) }
& python @arguments
if($LASTEXITCODE -ne 0){ throw "DB performance gate failed with exit code $LASTEXITCODE." }
