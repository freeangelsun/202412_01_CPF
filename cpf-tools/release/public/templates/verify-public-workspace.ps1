$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
function Invoke-Checked([string]$Label,[scriptblock]$Action) {
  Write-Host "[CPF][PUBLIC] $Label"
  & $Action
  if ($LASTEXITCODE -ne 0) { throw "$Label failed with exit code $LASTEXITCODE" }
}
Invoke-Checked 'Member Online/Batch build and tests' { & (Join-Path $Root 'gradlew.bat') -p (Join-Path $Root 'cpf-member') clean build --no-daemon }
if (Test-Path (Join-Path $Root 'cpf-biz-channel/build.gradle')) {
  Invoke-Checked 'BZA Channel build and tests' { & (Join-Path $Root 'gradlew.bat') -p (Join-Path $Root 'cpf-biz-channel') clean test build --no-daemon }
}
if (Test-Path (Join-Path $Root 'cpf-biz-frontend/package.json')) {
  Push-Location (Join-Path $Root 'cpf-biz-frontend')
  try {
    Invoke-Checked 'BZA Frontend clean install' { npm ci --ignore-scripts }
    Invoke-Checked 'BZA Frontend verify' { npm run verify }
  } finally { Pop-Location }
}
Write-Host '[CPF][PUBLIC] WORKSPACE_VERIFY=PASS'
