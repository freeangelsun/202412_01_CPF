[CmdletBinding()]
param(
  [switch]$RunBuild,
  [string[]]$Modules=@('cpf-core','cpf-common','cpf-reference','cpf-batch','cpf-gateway')
)
$ErrorActionPreference='Stop'
$here=$PSScriptRoot
$failures = [System.Collections.Generic.List[string]]::new()

function Invoke-CpfGate {
  param([Parameter(Mandatory=$true)][string]$Path,[Parameter(Mandatory=$true)][string]$Name)
  if(!(Test-Path $Path)){ return }
  try {
    $global:LASTEXITCODE = 0
    & $Path
    $exitCode = $global:LASTEXITCODE
    if($exitCode -ne 0){
      throw "$Name exited with code $exitCode"
    }
  } catch {
    $message = $_.Exception.Message
    $failures.Add("[$Name] $message")
    Write-Host "[FAIL-COLLECTED] $Name" -ForegroundColor Red
    Write-Host $message -ForegroundColor Red
  }
}

$gates = @(
  @{ Name='R11 public boundary'; File='check-r11-public-boundary.ps1' },
  @{ Name='R11 common capabilities'; File='check-r11-common-capabilities.ps1' },
  @{ Name='R11 runtime entrypoints'; File='check-r11-runtime-entrypoints.ps1' },
  @{ Name='R11 ADM/BZA UX security'; File='check-r11-admin-ux-security.ps1' },
  @{ Name='Repository hygiene'; File='check-repository-hygiene.ps1' },
  @{ Name='R10 cleanup'; File='check-r10-cleanup.ps1' },
  @{ Name='R10 product standard'; File='check-r10-product-standard.ps1' },
  @{ Name='Source documentation'; File='check-source-documentation-standard.ps1' },
  @{ Name='Frontend route targets'; File='check-frontend-route-targets.ps1' }
)

foreach($gate in $gates){
  Invoke-CpfGate -Path (Join-Path $here $gate.File) -Name $gate.Name
}

if($RunBuild){
  try {
    & (Join-Path $here 'build-module-set.ps1') -Modules $Modules -Goal test -NoDaemon
  } catch {
    $failures.Add("[Build] $($_.Exception.Message)")
    Write-Host '[FAIL-COLLECTED] Build' -ForegroundColor Red
    Write-Host $_.Exception.Message -ForegroundColor Red
  }
}

if($failures.Count){
  Write-Host "`n===== CPF R11 COLLECTED FAILURES ($($failures.Count)) =====" -ForegroundColor Red
  $failures | ForEach-Object { Write-Host " - $_" -ForegroundColor Red }
  throw "R11 source/product gates failed: $($failures.Count) gate(s). Review the collected failures above together."
}

Write-Host '[PASS] R11 source/product gate orchestration completed.' -ForegroundColor Green
