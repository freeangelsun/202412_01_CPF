param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "specs/evidence/20260708_03")
)

$ErrorActionPreference = "Stop"

$modules = @("acc", "mbr", "exs", "adm", "bat", "bizadm", "xyz")
$prefixByModule = @{
    acc = "ACC"
    mbr = "MBR"
    exs = "EXS"
    adm = "ADM"
    bat = "BAT"
    bizadm = "BIZADM"
    xyz = "XYZ"
}
$profiles = @("local", "dev", "stg", "prod")
$staleFiles = @(
    "acc/src/main/resources/application-local.yml",
    "acc/src/main/resources/application-dev.yml",
    "acc/src/main/resources/application-prod.yml",
    "acc/src/main/resources/application-test.yml",
    "cmn/src/main/resources/application-cmn-test.yml",
    "bat/src/main/resources/application-bat-worker.yml",
    "scripts/deploy/check-packaged-dependencies.ps1",
    "scripts/deploy/deploy-acc.ps1",
    "scripts/deploy/deploy-adm.ps1",
    "scripts/deploy/deploy-bat.ps1",
    "scripts/deploy/deploy-bizadm.ps1",
    "scripts/deploy/deploy-edu.ps1",
    "scripts/deploy/deploy-exs.ps1",
    "scripts/deploy/deploy-mbr.ps1",
    "scripts/deploy/deploy-module.ps1",
    "scripts/deploy/deploy-module.sh",
    "scripts/deploy/deploy-xyz.ps1",
    "scripts/deploy/remote-health-check.ps1",
    "scripts/deploy/remote-restart-module.ps1",
    "scripts/deploy/remote-start-module.ps1",
    "scripts/deploy/remote-stop-module.ps1",
    "scripts/deploy/rollback-module.ps1"
)

$failures = New-Object System.Collections.Generic.List[string]
$deletedFiles = New-Object System.Collections.Generic.List[object]
$retainedFiles = New-Object System.Collections.Generic.List[object]
$envChecks = New-Object System.Collections.Generic.List[object]
$inventoryChecks = New-Object System.Collections.Generic.List[object]

function Join-RootPath {
    param([string] $RelativePath)
    return Join-Path $Root $RelativePath
}

function Read-Utf8Text {
    param([string] $Path)
    return [System.IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8)
}

function Add-Failure {
    param([string] $Message)
    $failures.Add($Message) | Out-Null
}

function Write-JsonEvidence {
    param(
        [string] $FileName,
        [object] $Value
    )
    New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
    $path = Join-Path $ResultDir $FileName
    $json = $Value | ConvertTo-Json -Depth 12
    [System.IO.File]::WriteAllText($path, $json, [System.Text.UTF8Encoding]::new($false))
}

function Read-EnvFile {
    param([string] $Path)

    $values = [ordered]@{}
    foreach ($line in [System.IO.File]::ReadAllLines($Path, [System.Text.Encoding]::UTF8)) {
        $trimmed = $line.Trim()
        if ($trimmed.Length -eq 0 -or $trimmed.StartsWith("#")) {
            continue
        }
        $index = $trimmed.IndexOf("=")
        if ($index -le 0) {
            Add-Failure ("env  : {0} :: {1}" -f $Path, $line)
            continue
        }
        $values[$trimmed.Substring(0, $index)] = $trimmed.Substring($index + 1)
    }
    return $values
}

foreach ($relativePath in $staleFiles) {
    $fullPath = Join-RootPath $relativePath
    if (Test-Path -LiteralPath $fullPath) {
        Add-Failure ("    : {0}" -f $relativePath)
        $retainedFiles.Add([pscustomobject]@{
            path = $relativePath
            reason = "       ."
        }) | Out-Null
    } else {
        $deletedFiles.Add([pscustomobject]@{
            path = $relativePath
            deleted = $true
            reason = "Gradle       ."
        }) | Out-Null
    }
}

foreach ($profile in $profiles) {
    foreach ($module in $modules) {
        $prefix = $prefixByModule[$module]
        $relativePath = "deploy/env/$profile-$module.env"
        $path = Join-RootPath $relativePath
        if (-not (Test-Path -LiteralPath $path)) {
            Add-Failure (" env  : {0}" -f $relativePath)
            continue
        }

        $values = Read-EnvFile $path
        $requiredKeys = @(
            "SPRING_PROFILES_ACTIVE",
            ("{0}_MODULE_ID" -f $prefix),
            ("{0}_INSTANCE_ID" -f $prefix),
            ("{0}_WAS_ID" -f $prefix),
            ("{0}_SERVER_PORT" -f $prefix),
            ("{0}_LOG_BASE_PATH" -f $prefix),
            ("{0}_DATASOURCE_MODE" -f $prefix),
            ("{0}_DATASOURCE_URL" -f $prefix),
            ("{0}_DATASOURCE_USERNAME" -f $prefix),
            ("{0}_DATASOURCE_PASSWORD" -f $prefix),
            ("{0}_DATASOURCE_JNDI_NAME" -f $prefix)
        )

        $missingKeys = @()
        $emptyKeys = @()
        foreach ($key in $requiredKeys) {
            if (-not $values.Contains($key)) {
                $missingKeys += $key
            } elseif ([string]::IsNullOrWhiteSpace([string] $values[$key])) {
                $emptyKeys += $key
            }
        }

        $modeKey = "{0}_DATASOURCE_MODE" -f $prefix
        $passwordKey = "{0}_DATASOURCE_PASSWORD" -f $prefix
        $mode = [string] $values[$modeKey]
        $password = [string] $values[$passwordKey]
        if ($mode -and $mode.ToLowerInvariant() -notin @("url", "jndi")) {
            Add-Failure ("DATASOURCE_MODE  url  jndi : {0}" -f $relativePath)
        }
        if ($password -match "(?i)cpf_local_pw|password123|admin123|secret-change-me") {
            Add-Failure ("env      : {0}" -f $relativePath)
        }
        if ($missingKeys.Count -gt 0) {
            Add-Failure ("env   : {0} :: {1}" -f $relativePath, ($missingKeys -join ", "))
        }
        if ($emptyKeys.Count -gt 0) {
            Add-Failure ("env    : {0} :: {1}" -f $relativePath, ($emptyKeys -join ", "))
        }

        $envChecks.Add([pscustomobject]@{
            file = $relativePath
            keyCount = $values.Count
            missingKeys = $missingKeys
            emptyKeys = $emptyKeys
            datasourceMode = $mode
            status = $(if ($missingKeys.Count -eq 0 -and $emptyKeys.Count -eq 0) { "DONE" } else { "FAILED" })
        }) | Out-Null
    }
}

foreach ($profile in $profiles) {
    $inventoryName = if ($profile -eq "prod") { "prod-services.template.json" } else { "$profile-services.json" }
    $relativePath = "deploy/inventory/$inventoryName"
    $path = Join-RootPath $relativePath
    if (-not (Test-Path -LiteralPath $path)) {
        Add-Failure (" inventory  : {0}" -f $relativePath)
        continue
    }

    $inventory = Get-Content -LiteralPath $path -Raw -Encoding UTF8 | ConvertFrom-Json
    foreach ($module in $modules) {
        $moduleCode = $prefixByModule[$module]
        $service = @($inventory.services) | Where-Object { $_.module -eq $moduleCode } | Select-Object -First 1
        if ($null -eq $service) {
            Add-Failure ("inventory   : {0} :: {1}" -f $relativePath, $moduleCode)
            continue
        }
        foreach ($field in @("module", "hostAlias", "sshHostEnvKey", "sshUserEnvKey", "deployBase", "healthUrl", "serviceName", "portEnvKey", "profile", "runtimeMode", "approvalRequired", "rollbackEnabled")) {
            if (-not ($service.PSObject.Properties.Name -contains $field)) {
                Add-Failure ("inventory  : {0} :: {1} :: {2}" -f $relativePath, $moduleCode, $field)
            }
        }
        if (($service.PSObject.Properties.Name -contains "runtimeMode") -and $service.runtimeMode -notin @("embedded-bootjar", "external-was")) {
            Add-Failure ("runtimeMode  : {0} :: {1} :: {2}" -f $relativePath, $moduleCode, $service.runtimeMode)
        }
    }
    $inventoryChecks.Add([pscustomobject]@{
        file = $relativePath
        serviceCount = @($inventory.services).Count
        status = "CHECKED"
    }) | Out-Null
}

$buildGradlePath = Join-RootPath "build.gradle"
$buildGradle = Read-Utf8Text $buildGradlePath
foreach ($taskName in @("checkDeployEnv", "checkDeployInventory", "checkPackagedDependencies", "remoteDeployDryRun", "remoteDeploy", "rollbackDeploy", "checkRuntimeConfigStandard")) {
    $needle = "tasks.register('$taskName'"
    if ($buildGradle.IndexOf($needle, [System.StringComparison]::Ordinal) -lt 0) {
        Add-Failure ("Gradle   : {0}" -f $taskName)
    }
}
foreach ($forbiddenText in @("scripts/deploy/deploy-module.ps1", "scripts/deploy/check-packaged-dependencies.ps1")) {
    if ($buildGradle.IndexOf($forbiddenText, [System.StringComparison]::OrdinalIgnoreCase) -ge 0) {
        Add-Failure ("build.gradle      : {0}" -f $forbiddenText)
    }
}

$readmePath = Join-RootPath "README.md"
if (Test-Path -LiteralPath $readmePath) {
    $readme = Read-Utf8Text $readmePath
    if ($readme.IndexOf("scripts/deploy/deploy-module.ps1", [System.StringComparison]::OrdinalIgnoreCase) -ge 0) {
        Add-Failure "README  deploy-module.ps1   ."
    }
    if ($readme.IndexOf("remoteDeployDryRun", [System.StringComparison]::OrdinalIgnoreCase) -lt 0) {
        Add-Failure "README Gradle remoteDeployDryRun  ."
    }
}

$createDomainPath = Join-RootPath "scripts/create-domain.ps1"
if (Test-Path -LiteralPath $createDomainPath) {
    $createDomain = Read-Utf8Text $createDomainPath
    foreach ($requiredText in @('application-${module}-local.yml', 'deploy/env/local-${module}.env', 'deploy/inventory/local-services.json')) {
        if ($createDomain.IndexOf($requiredText, [System.StringComparison]::OrdinalIgnoreCase) -lt 0) {
            Add-Failure ("create-domain     : {0}" -f $requiredText)
        }
    }
} else {
    Add-Failure "create-domain  ."
}

foreach ($requiredPath in @(
    "bat/src/main/java/cpf/bat/edu/BatTaskletEducationSample.java",
    "bat/src/test/java/cpf/bat/edu/BatTaskletEducationSampleTest.java",
    "bat/src/main/java/cpf/bat/logging/BatJobLogPathPolicy.java",
    "bat/src/test/java/cpf/bat/logging/BatJobLogPathPolicyTest.java"
)) {
    if (-not (Test-Path -LiteralPath (Join-RootPath $requiredPath))) {
        Add-Failure ("BAT EDU     : {0}" -f $requiredPath)
    }
}

$summary = [pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    status = $(if ($failures.Count -eq 0) { "DONE" } else { "FAILED" })
    failureCount = $failures.Count
    failures = $failures
    envChecks = $envChecks
    inventoryChecks = $inventoryChecks
}

Write-JsonEvidence "garbage-file-scan.sanitized.json" $summary
Write-JsonEvidence "deleted-files.sanitized.json" ([pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    status = $(if ($retainedFiles.Count -eq 0) { "DONE" } else { "FAILED" })
    files = $deletedFiles
})
Write-JsonEvidence "retained-review-required-files.sanitized.json" ([pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    status = $(if ($retainedFiles.Count -eq 0) { "DONE" } else { "FAILED" })
    files = $retainedFiles
})

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Error $_ }
    exit 1
}

Write-Host ("runtime config standard check passed: {0}" -f $ResultDir)
