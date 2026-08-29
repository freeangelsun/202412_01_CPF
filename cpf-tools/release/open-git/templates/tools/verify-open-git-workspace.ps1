$ErrorActionPreference='Stop'
$Root=(Resolve-Path (Join-Path $PSScriptRoot '..')).Path
foreach($required in @('cpf-education','bin')){if(!(Test-Path -LiteralPath (Join-Path $Root $required))){throw "[CPF][OPEN-GIT][FAIL] required path missing: $required"}}
$count=0
Get-ChildItem -LiteralPath $Root -Directory -Filter 'cpf-*' | ForEach-Object {
  $contract=Join-Path $_.FullName 'gradle.properties'; if(!(Test-Path -LiteralPath $contract -PathType Leaf)){return}
  $props=@{}; Get-Content -LiteralPath $contract -Encoding UTF8 | ForEach-Object { $line=$_.Trim(); if($line -and !$line.StartsWith('#') -and $line.Contains('=')){ $k,$v=$line.Split('=',2); $props[$k.Trim()]=$v.Trim() } }
  if($props['cpf.domain.contractVersion'] -ne '1'){return}
  if($_.Name -ne ('cpf-'+$props['cpf.domain.name'])){throw "[CPF][OPEN-GIT][FAIL] domain root/name mismatch: $($_.FullName)"}
  foreach($forbidden in @('cpf-domain.yaml','cpf-generator.lock.json')){if(Test-Path -LiteralPath (Join-Path $_.FullName $forbidden)){throw "[CPF][OPEN-GIT][FAIL] generator metadata leaked: $($_.Name)/$forbidden"}}
  $count++
}
$Frontend=Join-Path $Root 'cpf-backoffice-web\frontend'
if(Test-Path -LiteralPath (Join-Path $Frontend 'package.json')){
  $npm=Get-Command npm.cmd,npm -ErrorAction SilentlyContinue | Select-Object -First 1; if(!$npm){throw '[CPF][OPEN-GIT][FAIL] npm missing for selected Backoffice frontend'}
  Push-Location $Frontend; try{& $npm.Source ci --ignore-scripts; if($LASTEXITCODE-ne0){throw 'npm ci failed'};& $npm.Source run verify;if($LASTEXITCODE-ne0){throw 'npm verify failed'}}finally{Pop-Location}
}
$state=if($count-eq0){'NOT_SELECTED'}else{'SELECTED'}; Write-Host "CPF_OPEN_GIT_WORKSPACE=PASS DOMAIN_COUNT=$count DOMAIN_STATE=$state"
