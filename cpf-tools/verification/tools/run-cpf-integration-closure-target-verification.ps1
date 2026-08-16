$ErrorActionPreference='Stop'
$root=(Resolve-Path (Join-Path $PSScriptRoot '..\..\..')).Path
Set-Location $root
$java=(java -version 2>&1 | Out-String)
if($java -notmatch 'version "25'){ throw "Java 25 is required. Actual: $java" }
.\gradlew.bat --no-daemon --stacktrace tasks --all
.\gradlew.bat --no-daemon --stacktrace compileJava compileTestJava check
python cpf-tools/verification/tools/verify-cpf-integration-closure.py $root
Push-Location cpf-admin\frontend
npm ci
npm run build
npx playwright test --project=chromium --project=firefox --project=webkit
Pop-Location
