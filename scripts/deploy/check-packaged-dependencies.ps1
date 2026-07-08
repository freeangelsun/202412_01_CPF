param(
    [Parameter(Mandatory = $true)]
    [string] $Module,
    [string] $Root = "",
    [string] $ResultDir = ""
)

$ErrorActionPreference = "Stop"

$scriptDir = if ([string]::IsNullOrWhiteSpace($PSScriptRoot)) {
    Split-Path -Parent $MyInvocation.MyCommand.Path
} else {
    $PSScriptRoot
}

if ([string]::IsNullOrWhiteSpace($Root)) {
    $Root = (Resolve-Path (Join-Path $scriptDir "..\..")).Path
}

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/runtime-smoke"
}

$moduleLower = $Module.ToLowerInvariant()
$moduleUpper = $Module.ToUpperInvariant()
$failures = New-Object System.Collections.Generic.List[string]

function Add-Failure {
    param([string] $Message)
    $failures.Add($Message)
}

$moduleDir = Join-Path $Root $moduleLower
$buildFile = Join-Path $moduleDir "build.gradle"
if (-not (Test-Path -LiteralPath $buildFile)) {
    Add-Failure "module build.gradle missing: $moduleLower"
}

$hasPfwDependency = $false
$hasCmnDependency = $false
if (Test-Path -LiteralPath $buildFile) {
    $buildText = [System.IO.File]::ReadAllText($buildFile, [System.Text.Encoding]::UTF8)
    $hasPfwDependency = $buildText -match "project\(':pfw'\)"
    $hasCmnDependency = $buildText -match "project\(':cmn'\)"
    if (-not $hasPfwDependency) {
        Add-Failure "$moduleUpper build.gradle does not depend on :pfw."
    }
}

$bootJar = $null
$libsDir = Join-Path $moduleDir "build/libs"
if (Test-Path -LiteralPath $libsDir) {
    $bootJar = Get-ChildItem -LiteralPath $libsDir -File -Filter "*.jar" |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
}

$jarChecked = $false
$jarContainsPfw = $false
$jarContainsCmn = $false
if ($bootJar -ne $null) {
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $jarChecked = $true
    $zip = [System.IO.Compression.ZipFile]::OpenRead($bootJar.FullName)
    try {
        $entryNames = @($zip.Entries | ForEach-Object { $_.FullName })
        $jarContainsPfw = $entryNames | Where-Object { $_ -match "^BOOT-INF/lib/pfw-.*\.jar$" } | Select-Object -First 1
        $jarContainsCmn = $entryNames | Where-Object { $_ -match "^BOOT-INF/lib/cmn-.*\.jar$" } | Select-Object -First 1
        if (-not $jarContainsPfw) {
            Add-Failure "$moduleUpper bootJar does not contain PFW jar."
        }
        if ($hasCmnDependency -and -not $jarContainsCmn) {
            Add-Failure "$moduleUpper bootJar declares CMN dependency but does not contain CMN jar."
        }
    } finally {
        $zip.Dispose()
    }
}

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "packaged-dependencies-$moduleLower.sanitized.json"
$result = [pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    module = $moduleUpper
    status = $(if ($failures.Count -eq 0) { "DONE" } else { "FAILED" })
    staticDependency = [pscustomobject]@{
        pfw = $hasPfwDependency
        cmn = $hasCmnDependency
    }
    bootJar = [pscustomobject]@{
        checked = $jarChecked
        path = $(if ($bootJar -eq $null) { $null } else { $bootJar.FullName.Substring($Root.Length + 1).Replace("\", "/") })
        containsPfw = [bool] $jarContainsPfw
        containsCmn = [bool] $jarContainsCmn
    }
    failures = @($failures.ToArray())
}
$json = $result | ConvertTo-Json -Depth 8
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($resultPath, $json, $utf8NoBom)

Write-Host "Packaged dependency check module=$moduleUpper status=$($result.status) evidence=$resultPath"
if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Host "FAIL $_" }
    exit 1
}
