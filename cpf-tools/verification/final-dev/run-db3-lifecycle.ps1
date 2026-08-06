param(
  [string]$RepoRoot=(Get-Location).Path,
  [string]$EvidenceDir="cpf-docs/work/v9i/fdr/r1/evidence/runtime/FDEV-005"
)
$ErrorActionPreference='Stop'
Set-Location $RepoRoot
$EvidencePath=Join-Path $RepoRoot $EvidenceDir
New-Item -ItemType Directory -Force $EvidencePath | Out-Null
$Baseline='2929163b3bb40159e22e1f57e79b6cd070abf7ad'
python cpf-tools/db/verify_migration_lifecycle.py --root $RepoRoot --source-sha $Baseline --report (Join-Path $EvidencePath 'static-lifecycle.json') 2>&1 | Tee-Object (Join-Path $EvidencePath 'static-lifecycle.log')
if($LASTEXITCODE-ne 0){throw "Static DB lifecycle gate failed: $LASTEXITCODE"}
$Runner=$env:CPF_DB_LIFECYCLE_RUNNER
if([string]::IsNullOrWhiteSpace($Runner) -or -not (Test-Path -LiteralPath $Runner)){throw 'CPF_DB_LIFECYCLE_RUNNER must point to the approved target-runtime DB lifecycle runner.'}
foreach($Vendor in @('ORACLE','POSTGRESQL','MARIADB')){
  $Url=[Environment]::GetEnvironmentVariable("CPF_${Vendor}_URL")
  $User=[Environment]::GetEnvironmentVariable("CPF_${Vendor}_USER")
  $Password=[Environment]::GetEnvironmentVariable("CPF_${Vendor}_PASSWORD")
  if([string]::IsNullOrWhiteSpace($Url)-or[string]::IsNullOrWhiteSpace($User)-or[string]::IsNullOrWhiteSpace($Password)){throw "Missing CPF_${Vendor}_URL/USER/PASSWORD"}
  & $Runner --vendor $Vendor.ToLowerInvariant() --repo-root $RepoRoot --url $Url --user $User --password-stdin --evidence-dir (Join-Path $EvidencePath $Vendor.ToLowerInvariant()) 2>&1 | Tee-Object (Join-Path $EvidencePath "$($Vendor.ToLowerInvariant()).log")
  if($LASTEXITCODE-ne 0){throw "$Vendor lifecycle failed: $LASTEXITCODE"}
}
