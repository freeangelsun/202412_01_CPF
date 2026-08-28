$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
if ([string]::IsNullOrWhiteSpace($env:CPF_MAVEN_REPOSITORY_URL)) { throw 'CPF_MAVEN_REPOSITORY_URL is required.' }
if ([string]::IsNullOrWhiteSpace($env:CPF_VERSION)) { throw 'CPF_VERSION is required.' }
foreach ($required in @('cpf-member','cpf-external','cpf-backoffice','cpf-backoffice-web','cpf-education','bin')) {
    if (-not (Test-Path (Join-Path $Root $required))) { throw "CPF Open Git required path missing: $required" }
}
$forbidden = Get-ChildItem -LiteralPath $Root -Recurse -File -ErrorAction Stop | Where-Object { $_.Extension -in @('.jar','.war') -and $_.FullName -ne (Join-Path $Root 'gradle\wrapper\gradle-wrapper.jar') }
if ($forbidden) { throw ('CPF Open Git Source Workspace must not contain accumulated CPF/application JAR/WAR: ' + (($forbidden | Select-Object -First 20 -ExpandProperty FullName) -join ', ')) }
& (Join-Path $Root 'gradlew.bat') cpfVerify --no-daemon
if ($LASTEXITCODE -ne 0) { throw "CPF Open Git Gradle verify failed: exit=$LASTEXITCODE" }
$Frontend = Join-Path $Root 'cpf-backoffice-web/frontend'
if (Test-Path (Join-Path $Frontend 'package.json') -PathType Leaf) {
    if ($null -eq (Get-Command npm -ErrorAction SilentlyContinue)) { throw 'npm is required for Backoffice Web frontend verification.' }
    Push-Location $Frontend
    try {
        & npm ci --ignore-scripts
        if ($LASTEXITCODE -ne 0) { throw "npm ci failed: exit=$LASTEXITCODE" }
        & npm run verify
        if ($LASTEXITCODE -ne 0) { throw "npm verify failed: exit=$LASTEXITCODE" }
    } finally { Pop-Location }
}
Write-Host '[CPF][OPEN-GIT] WORKSPACE_VERIFY=PASS'
