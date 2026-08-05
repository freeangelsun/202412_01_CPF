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
$sourceSha=(& git -C $root rev-parse HEAD 2>&1|Select-Object -First 1).ToString().Trim().ToLowerInvariant()
if($LASTEXITCODE -ne 0 -or $sourceSha -notmatch '^[0-9a-f]{40}$'){throw 'Exact Git source SHA is required.'}
if((@(& git -C $root status --porcelain=v1 --untracked-files=all)).Count -gt 0){throw 'Clean working tree is required for runtime evidence approval.'}
$policy=Join-Path $root 'cpf-tools/db/cpf-db-performance-policy.json'
$verifier=Join-Path $root 'cpf-tools/scripts/verify-cpf-db-performance-evidence.py'
if(-not (Test-Path -LiteralPath $EvidencePath -PathType Leaf)){ throw "Performance evidence not found: $EvidencePath" }
$evidence=Get-Content -LiteralPath $EvidencePath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 100
if([string]$evidence.sourceSha -ne $sourceSha){throw "Evidence sourceSha mismatch. expected=$sourceSha actual=$($evidence.sourceSha)"}
if([string]$evidence.resultSha -ne $sourceSha){throw "Evidence resultSha mismatch. expected=$sourceSha actual=$($evidence.resultSha)"}
if([string]$evidence.vendor -ne $Vendor){ throw "Evidence vendor mismatch. expected=$Vendor actual=$($evidence.vendor)" }
if([string]::IsNullOrWhiteSpace($OutputPath)){$OutputPath=Join-Path $root ("build/reports/cpf-db/performance-$Vendor.sanitized.json")}elseif(-not[IO.Path]::IsPathRooted($OutputPath)){$OutputPath=Join-Path $root $OutputPath}
$OutputPath=[IO.Path]::GetFullPath($OutputPath);New-Item -ItemType Directory -Force -Path (Split-Path -Parent $OutputPath)|Out-Null
$arguments=@($verifier,'--policy',$policy,'--evidence',$EvidencePath,'--expected-evidence-sha256',$ExpectedEvidenceSha256,'--output',$OutputPath)
& python @arguments
if($LASTEXITCODE -ne 0){ throw "DB performance gate failed with exit code $LASTEXITCODE." }
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
