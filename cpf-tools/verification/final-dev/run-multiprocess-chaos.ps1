[CmdletBinding()]
param(
  [Parameter(Mandatory=$true)][ValidatePattern('^[0-9a-fA-F]{40}$')][string]$ExpectedHead,
  [string]$RepoRoot='',
  [string]$EvidenceDir='build/evidence/r6-release/multiprocess'
)
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
if([string]::IsNullOrWhiteSpace($RepoRoot)){
  $RepoRoot=(& git -C $PSScriptRoot rev-parse --show-toplevel).Trim()
  if($LASTEXITCODE -ne 0){throw 'Repository root resolution failed'}
}
$root=(Resolve-Path $RepoRoot).Path
$head=(& git -C $root rev-parse HEAD).Trim().ToLowerInvariant()
if($LASTEXITCODE -ne 0){throw 'git rev-parse HEAD failed'}
if($head -ne $ExpectedHead.ToLowerInvariant()){throw "ExpectedHead mismatch expected=$ExpectedHead actual=$head"}
$dirty=(& git -C $root status --porcelain=v1 --untracked-files=all | Out-String).Trim()
if($dirty){throw 'Multi-process chaos qualification requires clean exact-SHA tree'}
$evidencePath=if([IO.Path]::IsPathRooted($EvidenceDir)){$EvidenceDir}else{Join-Path $root $EvidenceDir}
New-Item -ItemType Directory -Force -Path $evidencePath | Out-Null
$runner=$env:CPF_MULTIPROCESS_CHAOS_RUNNER
if([string]::IsNullOrWhiteSpace($runner) -or -not(Test-Path -LiteralPath $runner -PathType Leaf)){
  throw 'CPF_MULTIPROCESS_CHAOS_RUNNER must point to the approved broker/network/split-WAS/process-kill harness.'
}
foreach($name in @('CPF_BROKER_URL','CPF_CHAOS_DB_URL','CPF_CHAOS_DB_USER','CPF_CHAOS_DB_PASSWORD')){
  if([string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($name))){throw "Missing $name"}
}
$required=@('broker-redelivery','broker-outage','network-partition','multi-instance-claim','split-was','process-kill','db-outage','unknown-reconcile')
$summary=Join-Path $evidencePath 'chaos-summary.json'
$log=Join-Path $evidencePath 'chaos.log'
& $runner --repo-root $root --baseline-sha $head --evidence-dir $evidencePath --summary-json $summary --scenarios ($required -join ',') 2>&1 | Tee-Object -FilePath $log
if($LASTEXITCODE -ne 0){throw "Multi-process chaos harness failed: $LASTEXITCODE"}
if(-not(Test-Path -LiteralPath $summary -PathType Leaf)){throw 'chaos-summary.json was not produced by the approved harness'}
$data=Get-Content -LiteralPath $summary -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 30
if([string]$data.sourceSha -ne $head){throw "chaos summary sourceSha mismatch: $($data.sourceSha)"}
if([string]$data.status -ne 'PASS'){throw "chaos summary status is not PASS: $($data.status)"}
$rows=@($data.scenarios)
foreach($scenario in $required){
  $row=@($rows | Where-Object { [string]$_.id -eq $scenario })
  if($row.Count -ne 1 -or [string]$row[0].status -ne 'PASS'){throw "required chaos scenario not PASS: $scenario"}
}
if((& git -C $root rev-parse HEAD).Trim().ToLowerInvariant() -ne $head){throw 'Source SHA changed during chaos qualification'}
if((& git -C $root status --porcelain=v1 --untracked-files=all | Out-String).Trim()){throw 'Repository changed during chaos qualification'}
Write-Host "[CPF][R6I][CHAOS][PASS] sourceSha=$head scenarios=$($required.Count) evidence=$summary"
