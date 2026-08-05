[CmdletBinding()]
param(
    [Parameter(Mandatory)][ValidateSet('mariadb','postgresql','oracle')][string]$Vendor,
    [Parameter(Mandatory)][string]$EvidencePath,
    [Parameter(Mandatory)][ValidatePattern('^[0-9a-fA-F]{64}$')][string]$ExpectedEvidenceSha256,
    [Parameter(Mandatory)][ValidateNotNullOrEmpty()][string]$Operator,
    [Parameter(Mandatory)][ValidateNotNullOrEmpty()][string]$ApprovedBy,
    [Parameter(Mandatory)][ValidateNotNullOrEmpty()][string]$ApprovalReference,
    [Parameter(Mandatory)][ValidateNotNullOrEmpty()][string]$Reason,
    [Parameter(Mandatory)][switch]$ConfirmSanitizedEvidence,
    [Parameter(Mandatory)][string]$OutputPath
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
if ($Operator.Trim().Equals($ApprovedBy.Trim(), [System.StringComparison]::OrdinalIgnoreCase)) {
    throw 'DB operability gate requires independent operator and approver.'
}
if ($Reason.Trim().Length -lt 10) { throw 'Reason must contain at least 10 characters.' }
if ($ApprovalReference.Trim().Length -lt 3) { throw 'ApprovalReference is required.' }
if (-not $ConfirmSanitizedEvidence) { throw 'ConfirmSanitizedEvidence is required.' }
if (-not (Test-Path -LiteralPath $EvidencePath -PathType Leaf)) { throw "Evidence not found: $EvidencePath" }
$actualHash = (Get-FileHash -LiteralPath $EvidencePath -Algorithm SHA256).Hash.ToLowerInvariant()
if ($actualHash -ne $ExpectedEvidenceSha256.ToLowerInvariant()) {
    throw "Evidence SHA-256 mismatch. expected=$ExpectedEvidenceSha256 actual=$actualHash"
}
$evidence = Get-Content -LiteralPath $EvidencePath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 100
if ([string]$evidence.vendor -ne $Vendor) { throw "Evidence vendor mismatch. expected=$Vendor actual=$($evidence.vendor)" }
if ([string]$evidence.operator -ne $Operator) { throw 'Evidence operator mismatch.' }
if ([string]$evidence.approvedBy -ne $ApprovedBy) { throw 'Evidence approver mismatch.' }
if ([string]$evidence.approvalReference -ne $ApprovalReference) { throw 'Evidence approvalReference mismatch.' }
if ([string]$evidence.reason -ne $Reason) { throw 'Evidence reason mismatch.' }
$root = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$python = Get-Command python -ErrorAction SilentlyContinue
if (-not $python) { $python = Get-Command python3 -ErrorAction SilentlyContinue }
if (-not $python) { throw 'Python interpreter was not found.' }
$contract = Join-Path $root 'cpf-tools/db/cpf-db-operability-contract.json'
$normalizer = Join-Path $root 'cpf-tools/scripts/normalize-cpf-db-operation-evidence.py'
& $python.Source $normalizer '--contract' $contract '--evidence' $EvidencePath '--output' $OutputPath
if ($LASTEXITCODE -ne 0) { throw "DB operability gate failed with exit code $LASTEXITCODE." }
if (-not (Test-Path -LiteralPath $OutputPath -PathType Leaf)) { throw 'Normalized evidence was not produced.' }
