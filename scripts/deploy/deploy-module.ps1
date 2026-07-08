param(
    [Parameter(Mandatory = $true)]
    [string] $Module,
    [Parameter(Mandatory = $true)]
    [ValidateSet("local", "dev", "stg", "prod")]
    [string] $Env,
    [switch] $DryRun,
    [switch] $BuildBeforeDeploy,
    [switch] $RunQualityGate,
    [switch] $SkipArtifactRequired,
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
$startedAt = Get-Date
$failures = New-Object System.Collections.Generic.List[string]

function Add-Failure {
    param([string] $Message)
    $failures.Add($Message)
}

$envFile = Join-Path $Root "deploy/env/$Env-$moduleLower.env"
$inventoryFile = Join-Path $Root "deploy/inventory/$Env-services.json"
if (-not (Test-Path -LiteralPath $envFile)) {
    Add-Failure "env file missing: deploy/env/$Env-$moduleLower.env"
}
if (-not (Test-Path -LiteralPath $inventoryFile)) {
    Add-Failure "inventory file missing: deploy/inventory/$Env-services.json"
}

$targetService = $null
if (Test-Path -LiteralPath $inventoryFile) {
    $inventory = [System.IO.File]::ReadAllText($inventoryFile, [System.Text.Encoding]::UTF8) | ConvertFrom-Json
    $targetService = @($inventory.services | Where-Object { $_.module -eq $moduleUpper } | Select-Object -First 1)
    if ($targetService.Count -eq 0) {
        Add-Failure "module target missing in inventory: $moduleUpper / $Env"
        $targetService = $null
    } else {
        $targetService = $targetService[0]
    }
}

if ($RunQualityGate) {
    & (Join-Path $Root "gradlew.bat") qualityGate --offline --no-daemon --console=plain
    if ($LASTEXITCODE -ne 0) {
        Add-Failure "qualityGate failed before deploy."
    }
}

if ($BuildBeforeDeploy) {
    & (Join-Path $Root "gradlew.bat") ":${moduleLower}:bootJar" --offline --no-daemon --console=plain
    if ($LASTEXITCODE -ne 0) {
        Add-Failure "bootJar build failed: $moduleUpper"
    }
}

$artifact = $null
$libsDir = Join-Path $Root "$moduleLower/build/libs"
if (Test-Path -LiteralPath $libsDir) {
    $artifact = Get-ChildItem -LiteralPath $libsDir -File -Filter "*.jar" |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
}
if ($artifact -eq $null) {
    if (-not $SkipArtifactRequired) {
        Add-Failure "bootJar artifact not found for $moduleUpper."
    }
}

$checksum = $null
if ($artifact -ne $null) {
    $checksum = (Get-FileHash -Algorithm SHA256 -LiteralPath $artifact.FullName).Hash
}

if (-not $DryRun) {
    Add-Failure "real remote deploy was not executed by this safety script. Use DryRun until inventory and approval are ready."
}

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$packagingLog = Join-Path $ResultDir "check-packaged-dependencies-$moduleLower.log"
$packagingResultPath = Join-Path $ResultDir "packaged-dependencies-$moduleLower.sanitized.json"
& (Join-Path $scriptDir "check-packaged-dependencies.ps1") -Module $moduleUpper -Root $Root -ResultDir $ResultDir *> $packagingLog
if (-not (Test-Path -LiteralPath $packagingResultPath)) {
    Add-Failure "packaged dependency check failed."
} else {
    $packagingResult = [System.IO.File]::ReadAllText($packagingResultPath, [System.Text.Encoding]::UTF8) | ConvertFrom-Json
    if ($packagingResult.status -ne "DONE") {
        Add-Failure "packaged dependency check failed."
    }
}

$resultPath = Join-Path $ResultDir "deploy-$moduleLower-$Env-dry-run.sanitized.json"
$result = [pscustomobject]@{
    startedAt = $startedAt.ToString("o")
    finishedAt = (Get-Date).ToString("o")
    module = $moduleUpper
    env = $Env
    dryRun = [bool] $DryRun
    buildBeforeDeploy = [bool] $BuildBeforeDeploy
    skipArtifactRequired = [bool] $SkipArtifactRequired
    realRemoteExecuted = $false
    remoteResult = $(if ($DryRun) { "DRY_RUN_ONLY" } else { "NOT_EXECUTED" })
    rollbackExecuted = $false
    status = $(if ($failures.Count -eq 0) { "DONE" } else { "FAILED" })
    artifact = [pscustomobject]@{
        path = $(if ($artifact -eq $null) { $null } else { $artifact.FullName.Substring($Root.Length + 1).Replace("\", "/") })
        sha256 = $checksum
    }
    target = [pscustomobject]@{
        envFile = "deploy/env/$Env-$moduleLower.env"
        inventory = "deploy/inventory/$Env-services.json"
        hostAlias = $(if ($targetService -eq $null) { "$moduleLower-$Env" } else { $targetService.hostAlias })
        deployBase = $(if ($targetService -eq $null) { $null } else { $targetService.deployBase })
        serviceName = $(if ($targetService -eq $null) { $null } else { $targetService.serviceName })
        healthUrl = $(if ($targetService -eq $null) { $null } else { $targetService.healthUrl })
        rollbackEnabled = $(if ($targetService -eq $null) { $false } else { [bool] $targetService.rollbackEnabled })
    }
    remoteActions = @("validate-env", "build-bootJar", "checksum", "package-dependency-check")
    failures = @($failures.ToArray())
}
$json = $result | ConvertTo-Json -Depth 8
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($resultPath, $json, $utf8NoBom)

Write-Host "Deploy dry-run module=$moduleUpper env=$Env status=$($result.status) evidence=$resultPath"
if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Host "FAIL $_" }
    exit 1
}
