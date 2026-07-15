param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "build/runtime-smoke")
)

$ErrorActionPreference = "Stop"

$profiles = @("local", "dev", "stg", "prod")
$modules = @("mbr", "adm", "bat", "bza", "xyz")
$failures = New-Object System.Collections.Generic.List[object]
$checks = New-Object System.Collections.Generic.List[object]

function Add-Check {
    param([string] $Name, [string] $Status, [string] $Detail)
    $checks.Add([pscustomobject]@{
        name = $Name
        status = $Status
        detail = $Detail
    })
}

function Add-Failure {
    param([string] $Name, [string] $Detail)
    $failures.Add([pscustomobject]@{
        name = $Name
        detail = $Detail
    })
    Add-Check $Name "FAILED" $Detail
}

function Read-Text {
    param([string] $Path)
    return [System.IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8)
}

function Test-File {
    param([string] $RelativePath, [string] $Name)
    $path = Join-Path $Root $RelativePath
    if (-not (Test-Path -LiteralPath $path)) {
        Add-Failure $Name "Missing file: $RelativePath"
        return $false
    }
    Add-Check $Name "DONE" $RelativePath
    return $true
}

foreach ($profile in $profiles) {
    Test-File "pfw/src/main/resources/application-pfw-$profile.yml" "PFW_PROFILE_$($profile.ToUpperInvariant())" | Out-Null
    Test-File "cmn/src/main/resources/application-cmn-$profile.yml" "CMN_PROFILE_$($profile.ToUpperInvariant())" | Out-Null
}
Test-File "pfw/src/main/resources/application-pfw.yml" "PFW_PROFILE_BASE" | Out-Null
Test-File "cmn/src/main/resources/application-cmn.yml" "CMN_PROFILE_BASE" | Out-Null

$commonFiles = @(
    "pfw/src/main/resources/application-pfw.yml",
    "cmn/src/main/resources/application-cmn.yml"
) + ($profiles | ForEach-Object {
    "pfw/src/main/resources/application-pfw-$_.yml"
}) + ($profiles | ForEach-Object {
    "cmn/src/main/resources/application-cmn-$_.yml"
})

foreach ($relativePath in $commonFiles) {
    $path = Join-Path $Root $relativePath
    if (-not (Test-Path -LiteralPath $path)) {
        continue
    }
    $text = Read-Text $path
    if ($text -match "spring:\s*\r?\n\s*application:" -or $text -match "server:\s*\r?\n\s*port:" -or $text -match "profiles:\s*\r?\n\s*active:") {
        Add-Failure "COMMON_CONFIG_DOES_NOT_OWN_RUNTIME_$relativePath" "PFW/CMN config must not force application name, server port, or active profile."
    }
}

foreach ($module in $modules) {
    $moduleUpper = $module.ToUpperInvariant()
    $resourceRoot = "$module/src/main/resources"
    $applicationPath = Join-Path $Root "$resourceRoot/application.yml"
    if (-not (Test-File "$resourceRoot/application.yml" "APPLICATION_YML_$moduleUpper")) {
        continue
    }
    $applicationText = Read-Text $applicationPath
    $requiredImports = @(
        "application-pfw.yml",
        'application-pfw-${spring.profiles.active:local}.yml',
        "application-cmn.yml",
        'application-cmn-${spring.profiles.active:local}.yml',
        "application-$module.yml",
        "application-$module-" + '${spring.profiles.active:local}' + ".yml"
    )
    foreach ($import in $requiredImports) {
        if ($applicationText -notlike "*$import*") {
            Add-Failure "CONFIG_IMPORT_$moduleUpper" "Missing config import [$import] in $resourceRoot/application.yml"
        }
    }

    Test-File "$resourceRoot/application-$module.yml" "MODULE_PROFILE_BASE_$moduleUpper" | Out-Null
    foreach ($profile in $profiles) {
        Test-File "$resourceRoot/application-$module-$profile.yml" "MODULE_PROFILE_$($moduleUpper)_$($profile.ToUpperInvariant())" | Out-Null
    }

    $moduleFiles = @(Get-ChildItem -LiteralPath (Join-Path $Root $resourceRoot) -File -Filter "application-$module*.yml" -ErrorAction SilentlyContinue)
    $joinedText = ($moduleFiles | ForEach-Object { Read-Text $_.FullName }) -join "`n"
    if ($joinedText -notmatch "\$\{$($moduleUpper)_MODULE_ID:" -or $joinedText -notmatch "\$\{$($moduleUpper)_SERVER_PORT") {
        Add-Failure "MODULE_PREFIX_RUNTIME_$moduleUpper" "Module profile must expose $($moduleUpper)_MODULE_ID and $($moduleUpper)_SERVER_PORT placeholders."
    } else {
        Add-Check "MODULE_PREFIX_RUNTIME_$moduleUpper" "DONE" "$moduleUpper runtime placeholders found."
    }

    if ($joinedText -match "(?i)(private-key|access-token|refresh-token|client-secret)\s*:\s*[^`r`n\$\{]") {
        Add-Failure "MODULE_SECRET_LITERAL_$moduleUpper" "Module profile has a secret-like literal value."
    }
}

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "profile-loading-result.sanitized.json"
$result = [pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    status = $(if ($failures.Count -eq 0) { "DONE" } else { "FAILED" })
    failureCount = $failures.Count
    checkedModules = $modules
    checkedProfiles = $profiles
    failures = @($failures.ToArray())
    checks = @($checks.ToArray())
}
$json = $result | ConvertTo-Json -Depth 8
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($resultPath, $json, $utf8NoBom)

Write-Host "Profile loading check status=$($result.status) failures=$($failures.Count) evidence=$resultPath"
if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Host "FAIL [$($_.name)] $($_.detail)" }
    exit 1
}
