param([string] $Root = "")
$ErrorActionPreference = "Stop"
if ([string]::IsNullOrWhiteSpace($Root)) { $Root = (Resolve-Path "$PSScriptRoot\..\..").Path }

$generator = Join-Path $Root "cpf-tools/generator/create-domain.ps1"
$launcher = Join-Path $Root "cpf-tools/scripts/create-domain.ps1"
if (-not (Test-Path $generator -PathType Leaf)) { throw "Canonical generator missing: $generator" }
$text = Get-Content $generator -Raw -Encoding UTF8
foreach ($required in @(
    "[switch] `$DryRun",
    "module directory already exists",
    "SystemCode가 기존 manifest와 중복",
    "API route 또는 package가 기존 source와 충돌",
    "generator-ownership.json",
    "create-domain-result.json",
    "implementation project(':cpf-common')",
    "@Profile({""local"", ""test"", ""edu""})"
)) {
    if (-not $text.Contains($required)) { throw "Golden Path generator contract missing: $required" }
}
if ($text -match '(?m)^\s*version\s*=') {
    throw "Generated module must inherit root platformVersion; independent version assignment is forbidden."
}
if (Test-Path $launcher -PathType Leaf) {
    $launcherText = Get-Content $launcher -Raw -Encoding UTF8
    if ((Get-Item $launcher).Length -gt 4096 -or -not ($launcherText.Contains("generator") -and $launcherText.Contains("create-domain.ps1"))) {
        throw "Compatibility launcher contains a second generator implementation."
    }
}
Write-Host "CPF Generator Golden Path static gate passed."
