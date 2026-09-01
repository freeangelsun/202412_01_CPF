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
# Fresh Consumer 는 checkout 만으로 Gradle 을 실행할 수 있어야 한다. 경로 존재만 확인하면
# gradlew 가 fail-closed 로 요구하는 resource policy 누락 같은 실행 결함을 놓친다.
foreach($resource in @('gradle/cpf-runtime/common.properties','gradle/cpf-runtime/local.properties')){
  if(!(Test-Path -LiteralPath (Join-Path $Root $resource) -PathType Leaf)){
    throw "[CPF][OPEN-GIT][FAIL] required gradle resource policy missing: $resource"
  }
}
$IsolatedGradleHome=Join-Path ([IO.Path]::GetTempPath()) ("cpf-open-git-consumer-" + [guid]::NewGuid().ToString('N').Substring(0,10))
New-Item -ItemType Directory -Force -Path $IsolatedGradleHome | Out-Null
try {
  $previousGradleHome=$env:GRADLE_USER_HOME
  $env:GRADLE_USER_HOME=$IsolatedGradleHome
  & (Join-Path $Root 'gradlew.bat') --no-daemon --console=plain -q projects
  if($LASTEXITCODE -ne 0){ throw '[CPF][OPEN-GIT][FAIL] isolated Gradle consumer run failed' }
  Write-Host 'CPF_OPEN_GIT_ISOLATED_GRADLE=PASS'
} finally {
  $env:GRADLE_USER_HOME=$previousGradleHome
  Remove-Item -Recurse -Force -LiteralPath $IsolatedGradleHome -ErrorAction SilentlyContinue
}
$state=if($count-eq0){'NOT_SELECTED'}else{'SELECTED'}; Write-Host "CPF_OPEN_GIT_WORKSPACE=PASS DOMAIN_COUNT=$count DOMAIN_STATE=$state"
