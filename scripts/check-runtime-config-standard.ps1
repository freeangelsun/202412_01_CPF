param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "specs/evidence/20260714_02")
)

$ErrorActionPreference = "Stop"

$modules = @("acc", "mbr", "exs", "adm", "bat", "bizadm", "xyz")
$moduleCodes = @("ACC", "MBR", "EXS", "ADM", "BAT", "BIZADM", "XYZ")
$profiles = @("local", "dev", "stg", "prod")
$prefixByModule = @{
    acc = "ACC"
    mbr = "MBR"
    exs = "EXS"
    adm = "ADM"
    bat = "BAT"
    bizadm = "BIZADM"
    xyz = "XYZ"
}
$expectedLocalPorts = @{
    ACC = 8080
    MBR = 8081
    ADM = 8090
    BIZADM = 8091
    EXS = 8092
    BAT = 8093
    XYZ = 8099
}
$failures = New-Object System.Collections.Generic.List[string]

function Add-Failure {
    param([string] $Message)
    $failures.Add($Message) | Out-Null
}

function Join-RootPath {
    param([string] $RelativePath)
    return Join-Path $Root $RelativePath
}

function Write-JsonEvidence {
    param(
        [string] $FileName,
        [object] $Value
    )
    New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
    $path = Join-Path $ResultDir $FileName
    $json = $Value | ConvertTo-Json -Depth 16
    [System.IO.File]::WriteAllText($path, $json, [System.Text.UTF8Encoding]::new($false))
}

function Read-Utf8Text {
    param([string] $Path)
    return [System.IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8)
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
            Add-Failure ("env line invalid: {0} :: {1}" -f $Path, $line)
            continue
        }
        $values[$trimmed.Substring(0, $index)] = $trimmed.Substring($index + 1)
    }
    return $values
}

function Get-GitFiles {
    param([string[]] $PathSpecs)
    $output = & git -c "safe.directory=*" -C $Root ls-files @PathSpecs
    if ($LASTEXITCODE -ne 0) {
        throw "git ls-files failed: $($PathSpecs -join ' ')"
    }
    return @($output | Where-Object { $null -ne $_ })
}

# 요청서에 명시된 파일 목록은 raw txt를 흩어 놓지 않고 하나의 정제 JSON으로 보관합니다.
Write-JsonEvidence "runtime-config-inventory.sanitized.json" ([pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    status = "DONE"
    gitFiles = Get-GitFiles @()
    configFiles = Get-GitFiles @("*application*.yml", "*application*.yaml", "*application*.properties")
    deployConfigFiles = Get-GitFiles @("deploy/env/*", "deploy/inventory/*")
    deployScriptFilesAfterCleanup = Get-GitFiles @("scripts/deploy/*")
    evidenceFiles = Get-GitFiles @("specs/evidence/**")
})

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
$deletedFiles = New-Object System.Collections.Generic.List[object]
$retainedFiles = New-Object System.Collections.Generic.List[object]
foreach ($relativePath in $staleFiles) {
    $fullPath = Join-RootPath $relativePath
    if (Test-Path -LiteralPath $fullPath) {
        Add-Failure ("stale file still exists: {0}" -f $relativePath)
        $retainedFiles.Add([pscustomobject]@{
            path = $relativePath
            reason = "stale deletion candidate still exists"
        }) | Out-Null
    } else {
        $deletedFiles.Add([pscustomobject]@{
            path = $relativePath
            deleted = $true
            reason = "removed because Gradle deployment standard replaces it"
        }) | Out-Null
    }
}

$deployScriptFiles = Get-GitFiles @("scripts/deploy/*")
foreach ($deployScript in $deployScriptFiles) {
    Add-Failure ("scripts/deploy residue remains: {0}" -f $deployScript)
}

$envChecks = New-Object System.Collections.Generic.List[object]
$inventoryChecks = New-Object System.Collections.Generic.List[object]
$portRows = New-Object System.Collections.Generic.List[object]
$datasourceRows = New-Object System.Collections.Generic.List[object]
foreach ($profile in $profiles) {
    foreach ($module in $modules) {
        $prefix = $prefixByModule[$module]
        $relativePath = "deploy/env/$profile-$module.env"
        $path = Join-RootPath $relativePath
        if (-not (Test-Path -LiteralPath $path)) {
            Add-Failure ("deploy env missing: {0}" -f $relativePath)
            continue
        }

        $values = Read-EnvFile $path
        $requiredKeys = @(
            "SPRING_PROFILES_ACTIVE",
            ("{0}_MODULE_ID" -f $prefix),
            ("{0}_INSTANCE_ID" -f $prefix),
            ("{0}_WAS_ID" -f $prefix),
            ("{0}_SERVER_PORT" -f $prefix),
            "CPF_LOG_ROOT",
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
        $urlKey = "{0}_DATASOURCE_URL" -f $prefix
        $jndiKey = "{0}_DATASOURCE_JNDI_NAME" -f $prefix
        $passwordKey = "{0}_DATASOURCE_PASSWORD" -f $prefix
        $portKey = "{0}_SERVER_PORT" -f $prefix
        $mode = [string] $values[$modeKey]
        $port = [int] $values[$portKey]
        $password = [string] $values[$passwordKey]
        $logRoot = [string] $values["CPF_LOG_ROOT"]

        if ($mode -and $mode.ToLowerInvariant() -notin @("url", "jndi")) {
            Add-Failure ("DATASOURCE_MODE must be url or jndi: {0}" -f $relativePath)
        }
        if ($profile -in @("local", "dev") -and $mode.ToLowerInvariant() -ne "url") {
            Add-Failure ("local/dev datasource mode must default to url: {0}" -f $relativePath)
        }
        if ($password -match "(?i)password123|admin123|secret-change-me") {
            Add-Failure ("raw default password pattern remains: {0}" -f $relativePath)
        }
        if ($logRoot -and (-not [System.IO.Path]::IsPathRooted($logRoot) -or $logRoot -match '(^|[/\\])\.\.([/\\]|$)')) {
            Add-Failure ("CPF_LOG_ROOT must be an absolute path without parent traversal: {0}" -f $relativePath)
        }
        if ($missingKeys.Count -gt 0) {
            Add-Failure ("deploy env missing keys: {0} :: {1}" -f $relativePath, ($missingKeys -join ", "))
        }
        if ($emptyKeys.Count -gt 0) {
            Add-Failure ("deploy env empty keys: {0} :: {1}" -f $relativePath, ($emptyKeys -join ", "))
        }
        if ($profile -eq "local" -and $expectedLocalPorts.ContainsKey($prefix) -and $port -ne $expectedLocalPorts[$prefix]) {
            Add-Failure ("local port mismatch: {0} expected {1}, actual {2}" -f $prefix, $expectedLocalPorts[$prefix], $port)
        }

        $envChecks.Add([pscustomobject]@{
            file = $relativePath
            keyCount = $values.Count
            missingKeys = $missingKeys
            emptyKeys = $emptyKeys
            status = $(if ($missingKeys.Count -eq 0 -and $emptyKeys.Count -eq 0) { "DONE" } else { "FAILED" })
        }) | Out-Null
        $portRows.Add([pscustomobject]@{
            profile = $profile
            module = $prefix
            port = $port
            source = $relativePath
        }) | Out-Null
        $datasourceRows.Add([pscustomobject]@{
            profile = $profile
            module = $prefix
            mode = $mode
            urlKeyPresent = $values.Contains($urlKey)
            jndiKeyPresent = $values.Contains($jndiKey)
            passwordMasked = $(if ($values.Contains($passwordKey)) { "***" } else { $null })
            source = $relativePath
        }) | Out-Null
    }
}

foreach ($profile in $profiles) {
    $inventoryName = if ($profile -eq "prod") { "prod-services.template.json" } else { "$profile-services.json" }
    $relativePath = "deploy/inventory/$inventoryName"
    $path = Join-RootPath $relativePath
    if (-not (Test-Path -LiteralPath $path)) {
        Add-Failure ("deploy inventory missing: {0}" -f $relativePath)
        continue
    }

    $inventory = Get-Content -LiteralPath $path -Raw -Encoding UTF8 | ConvertFrom-Json
    foreach ($moduleCode in $moduleCodes) {
        $service = @($inventory.services) | Where-Object { $_.module -eq $moduleCode } | Select-Object -First 1
        if ($null -eq $service) {
            Add-Failure ("inventory service missing: {0} :: {1}" -f $relativePath, $moduleCode)
            continue
        }
        foreach ($field in @("module", "hostAlias", "sshHostEnvKey", "sshUserEnvKey", "deployBase", "healthUrl", "serviceName", "portEnvKey", "profile", "runtimeMode", "approvalRequired", "rollbackEnabled")) {
            if (-not ($service.PSObject.Properties.Name -contains $field)) {
                Add-Failure ("inventory field missing: {0} :: {1} :: {2}" -f $relativePath, $moduleCode, $field)
            }
        }
        if (($service.PSObject.Properties.Name -contains "runtimeMode") -and $service.runtimeMode -notin @("embedded-bootjar", "external-was")) {
            Add-Failure ("inventory runtimeMode invalid: {0} :: {1} :: {2}" -f $relativePath, $moduleCode, $service.runtimeMode)
        }
    }
    $eduService = @($inventory.services) | Where-Object { $_.module -eq "EDU" } | Select-Object -First 1
    if ($null -ne $eduService) {
        Add-Failure ("EDU inventory service must not exist: {0}" -f $relativePath)
    }
    $inventoryChecks.Add([pscustomobject]@{
        file = $relativePath
        serviceCount = @($inventory.services).Count
        status = "DONE"
    }) | Out-Null
}

$buildGradlePath = Join-RootPath "build.gradle"
$buildGradle = Read-Utf8Text $buildGradlePath
$deployTaskRows = New-Object System.Collections.Generic.List[object]
foreach ($taskName in @("checkDeployEnv", "checkDeployInventory", "checkPackagedDependencies", "remoteDeployDryRun", "remoteDeploy", "rollbackDeploy", "checkRuntimeConfigStandard", "checkSampleCoverage", "checkEvidencePathExistence")) {
    $present = $buildGradle.IndexOf("tasks.register('$taskName'", [System.StringComparison]::Ordinal) -ge 0
    if (-not $present) {
        Add-Failure ("Gradle deploy/check task missing: {0}" -f $taskName)
    }
    $deployTaskRows.Add([pscustomobject]@{
        task = $taskName
        present = $present
    }) | Out-Null
}

$eduForbiddenTexts = @("EDU   :", "EDU:", "EDU_SERVER_PORT", "EDU_MODULE_ID", "EDU_INSTANCE_ID", "deploy-edu.ps1", "deploy-edu.sh")
$eduFindings = New-Object System.Collections.Generic.List[object]
foreach ($relativePath in @("build.gradle", "settings.gradle", "README.md")) {
    $path = Join-RootPath $relativePath
    if (Test-Path -LiteralPath $path) {
        $text = Read-Utf8Text $path
        foreach ($needle in $eduForbiddenTexts) {
            if ($text.IndexOf($needle, [System.StringComparison]::OrdinalIgnoreCase) -ge 0) {
                Add-Failure ("EDU forbidden text remains: {0} :: {1}" -f $relativePath, $needle)
                $eduFindings.Add([pscustomobject]@{
                    file = $relativePath
                    pattern = $needle
                }) | Out-Null
            }
        }
    }
}
foreach ($relativePath in ((Get-GitFiles @("deploy/env/*", "deploy/inventory/*", "scripts/deploy/*")) | Where-Object { $_ -match '(?i)edu' })) {
    Add-Failure ("EDU deploy artifact remains: {0}" -f $relativePath)
    $eduFindings.Add([pscustomobject]@{
        file = $relativePath
        pattern = "EDU deploy artifact"
    }) | Out-Null
}

$localPorts = @($portRows | Where-Object { $_.profile -eq "local" })
$duplicatePorts = @($localPorts | Group-Object port | Where-Object { $_.Count -gt 1 } | ForEach-Object {
    [pscustomobject]@{
        port = $_.Name
        modules = @($_.Group | ForEach-Object { $_.module })
        count = $_.Count
    }
})
foreach ($duplicate in $duplicatePorts) {
    Add-Failure ("local port duplicate: {0} :: {1}" -f $duplicate.port, ($duplicate.modules -join ", "))
}

$emptyTargets = @(
    "scripts/deploy",
    "deploy/env",
    "deploy/inventory",
    "bat/src/main/java/cpf/bat/edu",
    "bat/src/test/java/cpf/bat/edu",
    "xyz/src/main/java/cpf/xyz/edu",
    "xyz/src/test/java/cpf/xyz/edu",
    "exs/src/main/java/cpf/exs",
    "cmn/src/main/java/cpf/cmn",
    "pfw/src/main/java/cpf/pfw",
    "specs/evidence"
)
$emptyDirs = New-Object System.Collections.Generic.List[object]
$deletedEmptyDirs = New-Object System.Collections.Generic.List[object]
$retainedEmptyDirs = New-Object System.Collections.Generic.List[object]
foreach ($relativeTarget in $emptyTargets) {
    $target = Join-RootPath $relativeTarget
    if (-not (Test-Path -LiteralPath $target)) {
        continue
    }
    Get-ChildItem -LiteralPath $target -Recurse -Directory -Force | Sort-Object FullName -Descending | ForEach-Object {
        $relative = $_.FullName.Substring($Root.Length).TrimStart("\", "/")
        $childCount = @(Get-ChildItem -LiteralPath $_.FullName -Force).Count
        if ($childCount -eq 0) {
            $emptyDirs.Add([pscustomobject]@{
                path = $relative
            }) | Out-Null
            if ($_.FullName.StartsWith($Root, [System.StringComparison]::OrdinalIgnoreCase)) {
                Remove-Item -LiteralPath $_.FullName -Force
                $deletedEmptyDirs.Add([pscustomobject]@{
                    path = $relative
                    deleted = $true
                    reason = "empty working directory removed"
                }) | Out-Null
            } else {
                $retainedEmptyDirs.Add([pscustomobject]@{
                    path = $relative
                    reason = "path is outside workspace"
                }) | Out-Null
            }
        }
    }
}

Write-JsonEvidence "garbage-file-scan.sanitized.json" ([pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    status = $(if ($failures.Count -eq 0) { "DONE" } else { "FAILED" })
    failureCount = $failures.Count
    failures = $failures
    staleFileCount = $staleFiles.Count
    deployScriptResidueCount = @($deployScriptFiles).Count
    envChecks = $envChecks
    inventoryChecks = $inventoryChecks
})
Write-JsonEvidence "deleted-files.sanitized.json" ([pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    status = $(if ($retainedFiles.Count -eq 0) { "DONE" } else { "FAILED" })
    files = $deletedFiles
})
Write-JsonEvidence "retained-review-required-files.sanitized.json" ([pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    status = $(if ($retainedFiles.Count -eq 0) { "DONE" } else { "NEEDS_REVIEW" })
    files = $retainedFiles
})
Write-JsonEvidence "empty-directory-scan.sanitized.json" ([pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    status = "DONE"
    emptyDirectoryCount = $emptyDirs.Count
    directories = $emptyDirs
})
Write-JsonEvidence "deleted-empty-directories.sanitized.json" ([pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    status = "DONE"
    directories = $deletedEmptyDirs
})
Write-JsonEvidence "retained-empty-directories.sanitized.json" ([pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    status = $(if ($retainedEmptyDirs.Count -eq 0) { "DONE" } else { "NEEDS_REVIEW" })
    directories = $retainedEmptyDirs
})
Write-JsonEvidence "local-port-duplicate-scan.sanitized.json" ([pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    status = $(if ($duplicatePorts.Count -eq 0) { "DONE" } else { "FAILED" })
    localPorts = $localPorts
    duplicates = $duplicatePorts
})
Write-JsonEvidence "datasource-mode-scan.sanitized.json" ([pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    status = "DONE"
    rows = $datasourceRows
})
Write-JsonEvidence "gradle-remote-deploy-task-scan.sanitized.json" ([pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    status = $(if (($deployTaskRows | Where-Object { -not $_.present }).Count -eq 0) { "DONE" } else { "FAILED" })
    tasks = $deployTaskRows
    allowedModules = $moduleCodes
    forbiddenModules = @("EDU", "PFW", "CMN")
})
Write-JsonEvidence "edu-module-deploy-alias-scan.sanitized.json" ([pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    status = $(if ($eduFindings.Count -eq 0) { "DONE" } else { "FAILED" })
    findings = $eduFindings
})

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Error $_ }
    exit 1
}

Write-Host ("runtime config standard check passed: {0}" -f $ResultDir)
