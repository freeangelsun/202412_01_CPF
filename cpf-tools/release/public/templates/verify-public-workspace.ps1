$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
if ([string]::IsNullOrWhiteSpace($env:CPF_MAVEN_REPOSITORY_URL)) { throw 'CPF_MAVEN_REPOSITORY_URL is required.' }
if ([string]::IsNullOrWhiteSpace($env:CPF_VERSION)) { throw 'CPF_VERSION is required.' }
if (-not (Test-Path (Join-Path $Root 'settings.gradle') -PathType Leaf) -or -not (Test-Path (Join-Path $Root 'domains') -PathType Container)) {
    throw 'CPF Public Workspace root/domain catalog is missing.'
}
& (Join-Path $Root 'gradlew.bat') cpfVerify --no-daemon
if ($LASTEXITCODE -ne 0) { throw "CPF Public Workspace Gradle verify failed: exit=$LASTEXITCODE" }
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
Write-Host '[CPF][PUBLIC] WORKSPACE_VERIFY=PASS'
