[CmdletBinding()]
param(
    [Parameter(Mandatory)][ValidateSet('mariadb','postgresql','oracle')][string]$Vendor,
    [Parameter(Mandatory)][string]$EvidencePath,
    [Parameter(Mandatory)][ValidatePattern('^[0-9a-fA-F]{64}$')][string]$ExpectedEvidenceSha256,
    [Parameter(Mandatory)][ValidateNotNullOrEmpty()][string]$Operator,
    [Parameter(Mandatory)][ValidateNotNullOrEmpty()][string]$Reason,
    [Parameter(Mandatory)][ValidateNotNullOrEmpty()][string]$ApprovedBy,
    [Parameter(Mandatory)][switch]$ConfirmSanitizedEvidence,
    [string]$OutputPath
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
if ($Operator -eq $ApprovedBy) { throw 'DataSource runtime gate requires independent approval.' }
if ($Reason.Trim().Length -lt 10) { throw 'Reason must contain at least 10 characters.' }
if (-not $ConfirmSanitizedEvidence) { throw 'Sanitized evidence confirmation is required.' }
if (-not (Test-Path -LiteralPath $EvidencePath -PathType Leaf)) { throw "Evidence not found: $EvidencePath" }
$root = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$evidence = Get-Content -LiteralPath $EvidencePath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 100
if ([string]$evidence.vendor -ne $Vendor) { throw "Evidence vendor mismatch. expected=$Vendor actual=$($evidence.vendor)" }
$python = Get-Command python -ErrorAction SilentlyContinue
if (-not $python) { $python = Get-Command python3 -ErrorAction SilentlyContinue }
if (-not $python) { throw 'Python interpreter was not found.' }
$args = @(
    'cpf-tools/scripts/verify-cpf-datasource-runtime-evidence.py',
    '--policy', 'cpf-tools/db/cpf-datasource-runtime-policy.json',
    '--evidence', $EvidencePath,
    '--expected-evidence-sha256', $ExpectedEvidenceSha256
)
$args[0] = Join-Path $root $args[0]
$args[2] = Join-Path $root $args[2]
if ($OutputPath) { $args += @('--output', $OutputPath) }
& $python.Source @args
if ($LASTEXITCODE -ne 0) { throw "DataSource runtime gate failed with exit code $LASTEXITCODE." }
