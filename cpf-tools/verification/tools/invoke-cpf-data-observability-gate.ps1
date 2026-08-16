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
if ($Operator -eq $ApprovedBy) { throw 'Data observability gate requires independent approval.' }
if ($Reason.Trim().Length -lt 10) { throw 'Reason must contain at least 10 characters.' }
if (-not $ConfirmSanitizedEvidence) { throw 'Sanitized evidence confirmation is required.' }
if (-not (Test-Path -LiteralPath $EvidencePath -PathType Leaf)) { throw "Evidence not found: $EvidencePath" }
$root = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
$sourceSha=(& git -C $root rev-parse HEAD 2>&1|Select-Object -First 1).ToString().Trim().ToLowerInvariant()
if($LASTEXITCODE -ne 0 -or $sourceSha -notmatch '^[0-9a-f]{40}$'){throw 'Exact Git source SHA is required.'}
if((@(& git -C $root status --porcelain=v1 --untracked-files=all)).Count -gt 0){throw 'Clean working tree is required for runtime evidence approval.'}
$evidence = Get-Content -LiteralPath $EvidencePath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 100
if([string]$evidence.sourceSha -ne $sourceSha){throw "Evidence sourceSha mismatch. expected=$sourceSha actual=$($evidence.sourceSha)"}
if ([string]$evidence.vendor -ne $Vendor) { throw "Evidence vendor mismatch. expected=$Vendor actual=$($evidence.vendor)" }
$python = Get-Command python -ErrorAction SilentlyContinue
if (-not $python) { $python = Get-Command python3 -ErrorAction SilentlyContinue }
if (-not $python) { throw 'Python interpreter was not found.' }
$args = @(
    'cpf-tools/verification/tools/verify-cpf-data-observability-evidence.py',
    '--policy', 'cpf-tools/db/cpf-data-observability-policy.json',
    '--evidence', $EvidencePath,
    '--expected-evidence-sha256', $ExpectedEvidenceSha256
)
$args[0] = Join-Path $root $args[0]
$args[2] = Join-Path $root $args[2]
if([string]::IsNullOrWhiteSpace($OutputPath)){$OutputPath=Join-Path $root ("build/reports/cpf-db/data-observability-$Vendor.sanitized.json")}elseif(-not[IO.Path]::IsPathRooted($OutputPath)){$OutputPath=Join-Path $root $OutputPath}
$OutputPath=[IO.Path]::GetFullPath($OutputPath);New-Item -ItemType Directory -Force -Path (Split-Path -Parent $OutputPath)|Out-Null
$args += @('--output', $OutputPath)
& $python.Source @args
if ($LASTEXITCODE -ne 0) { throw "Data observability gate failed with exit code $LASTEXITCODE." }
$approval=Get-Content -LiteralPath $OutputPath -Raw -Encoding UTF8|ConvertFrom-Json -Depth 100
$approval|Add-Member -NotePropertyName sourceSha -NotePropertyValue $sourceSha -Force
$approval|Add-Member -NotePropertyName operator -NotePropertyValue $Operator -Force
$approval|Add-Member -NotePropertyName approvedBy -NotePropertyValue $ApprovedBy -Force
$approval|Add-Member -NotePropertyName reason -NotePropertyValue $Reason -Force
$approval|Add-Member -NotePropertyName approvalTimestamp -NotePropertyValue ([DateTimeOffset]::UtcNow.ToString('o')) -Force
$approval|Add-Member -NotePropertyName inputEvidenceSha256 -NotePropertyValue ((Get-FileHash -LiteralPath $EvidencePath -Algorithm SHA256).Hash.ToLowerInvariant()) -Force
$approval|Add-Member -NotePropertyName sanitized -NotePropertyValue $true -Force
[IO.File]::WriteAllText($OutputPath,($approval|ConvertTo-Json -Depth 100)+"`n",[Text.UTF8Encoding]::new($false))
Write-Host "CPF DB evidence approval PASS. sourceSha=$sourceSha output=$OutputPath"
