param(
  [string]$RepoRoot=(Get-Location).Path,
  [string]$EvidenceDir="cpf-docs/work/v9i/fdr/r1/evidence/runtime/FDEV-006"
)
$ErrorActionPreference='Stop'
Set-Location $RepoRoot
$EvidencePath=Join-Path $RepoRoot $EvidenceDir
New-Item -ItemType Directory -Force $EvidencePath | Out-Null
$Runner=$env:CPF_MULTIPROCESS_CHAOS_RUNNER
if([string]::IsNullOrWhiteSpace($Runner) -or -not (Test-Path -LiteralPath $Runner)){throw 'CPF_MULTIPROCESS_CHAOS_RUNNER must point to the approved broker/split-WAS/process-kill harness.'}
foreach($Name in @('CPF_BROKER_URL','CPF_CHAOS_DB_URL','CPF_CHAOS_DB_USER','CPF_CHAOS_DB_PASSWORD')){if([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($Name))){throw "Missing $Name"}}
& $Runner --repo-root $RepoRoot --baseline-sha 2929163b3bb40159e22e1f57e79b6cd070abf7ad --evidence-dir $EvidencePath --scenarios broker-redelivery,multi-instance-claim,split-was,process-kill,unknown-reconcile 2>&1 | Tee-Object (Join-Path $EvidencePath 'chaos.log')
if($LASTEXITCODE-ne 0){throw "Multi-process chaos gate failed: $LASTEXITCODE"}
