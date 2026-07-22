param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "build/quality-gate")
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"

$modules = @("cpf-member", "cpf-admin", "cpf-batch", "cpf-biz-admin", "cpf-reference", "cpf-account", "cpf-external", "cpf-gateway")
$moduleCodes = @("MBR", "ADM", "BAT", "BZA", "REF", "ACC", "EXS", "GWY")
$profiles = @("local", "dev", "stg", "prod")
$prefixByModule = @{
    "cpf-member" = "MBR"
    "cpf-admin" = "ADM"
    "cpf-batch" = "BAT"
    "cpf-biz-admin" = "BZA"
    "cpf-reference" = "REF"
    "cpf-account" = "ACC"
    "cpf-external" = "EXS"
    "cpf-gateway" = "GWY"
}
$expectedLocalPorts = @{
    MBR = 8081
    ADM = 8090
    BZA = 8091
    BAT = 8093
    REF = 8099
    ACC = 8082
    EXS = 8094
    GWY = 8070
}
$schemaByModule = @{
    MBR = "mbrDB"
    ADM = "admDB"
    BZA = "bzaDB"
    BAT = "cpfDB"
    REF = "refDB"
    ACC = "accDB"
    EXS = "exsDB"
    GWY = "cpfDB"
}
$usernameByModule = @{
    MBR = "cpf_mbr_app"
    ADM = "cpf_adm_app"
    BZA = "cpf_bza_app"
    BAT = "cpf_app"
    REF = "cpf_ref_app"
    ACC = "cpf_acc_app"
    EXS = "cpf_exs_app"
    GWY = "cpf_app"
}
$jndiByModule = @{
    MBR = "java:comp/env/jdbc/cpfMemberDataSource"
    ADM = "java:comp/env/jdbc/cpfAdminDataSource"
    BZA = "java:comp/env/jdbc/cpfBizAdminDataSource"
    BAT = "java:comp/env/jdbc/cpfBatchDataSource"
    REF = "java:comp/env/jdbc/cpfReferenceDataSource"
    ACC = "java:comp/env/jdbc/cpfAccountDataSource"
    EXS = "java:comp/env/jdbc/cpfExternalDataSource"
    GWY = "java:comp/env/jdbc/cpfGatewayDataSource"
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
    $arguments = @("ls-files", "--cached", "--others", "--exclude-standard", "--") + @($PathSpecs)
    $output = @(& git -c "safe.directory=*" -c "core.quotepath=false" -C $Root @arguments 2>&1 |
            ForEach-Object { $_.ToString() })
    $exitCode = $LASTEXITCODE
    if ($exitCode -ne 0) {
        throw "git ls-files failed: $($PathSpecs -join ' '), output=$($output -join ' ')"
    }
    return @($output |
        Where-Object { $null -ne $_ -and (Test-Path -LiteralPath (Join-Path $Root $_) -PathType Leaf) } |
        Sort-Object -Unique)
}

# 요청서에 명시된 파일 목록은 raw txt를 흩어 놓지 않고 하나의 정제 JSON으로 보관합니다.
Write-JsonEvidence "runtime-config-inventory.sanitized.json" ([pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    status = "DONE"
    gitFiles = Get-GitFiles @()
    configFiles = Get-GitFiles @("*application*.yml", "*application*.yaml", "*application*.properties")
    deployConfigFiles = Get-GitFiles @("deploy/env/*", "deploy/inventory/*")
    deployScriptFilesAfterCleanup = Get-GitFiles @("scripts/deploy/*")
    evidenceFiles = Get-GitFiles @("cpf-docs/evidence/**")
})

$staleFiles = @(
    "cpf-common/src/main/resources/application-cmn-test.yml",
    "scripts/deploy/check-packaged-dependencies.ps1",
    "scripts/deploy/deploy-adm.ps1",
    "scripts/deploy/deploy-bat.ps1",
    "scripts/deploy/deploy-bza.ps1",
    "scripts/deploy/deploy-edu.ps1",
    "scripts/deploy/deploy-mbr.ps1",
    "scripts/deploy/deploy-module.ps1",
    "scripts/deploy/deploy-module.sh",
    "scripts/deploy/deploy-ref.ps1",
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
    $profileModules = if ($profile -eq "prod") {
        @($modules | Where-Object { $_ -ne "cpf-reference" })
    } else {
        $modules
    }
    foreach ($module in $profileModules) {
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
            "CPF_DB_MODE",
            "CPF_DB_URL",
            "CPF_DB_USERNAME",
            "CPF_DB_PASSWORD",
            "CPF_DB_JNDI_NAME",
            ("{0}_DATASOURCE_MODE" -f $prefix),
            ("{0}_DATASOURCE_URL" -f $prefix),
            ("{0}_DATASOURCE_USERNAME" -f $prefix),
            ("{0}_DATASOURCE_PASSWORD" -f $prefix),
            ("{0}_DATASOURCE_JNDI_NAME" -f $prefix)
        )
        if ($prefix -eq "MBR") {
            $requiredKeys += @(
                "CPF_MBR_JWT_SECRET",
                "CPF_MBR_ACCESS_TOKEN_TTL_SECONDS",
                "CPF_MBR_REFRESH_TOKEN_TTL_SECONDS"
            )
        }
        if ($prefix -eq "BZA") {
            $requiredKeys += @(
                "CPF_BZA_JWT_SECRET",
                "CPF_BZA_ACCESS_TOKEN_TTL_SECONDS",
                "CPF_BZA_REFRESH_TOKEN_TTL_SECONDS"
            )
        }

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
        $moduleIdKey = "{0}_MODULE_ID" -f $prefix
        $mode = [string] $values[$modeKey]
        $port = [int] $values[$portKey]
        $password = [string] $values[$passwordKey]
        $logRoot = [string] $values["CPF_LOG_ROOT"]

        if ($values.Contains($moduleIdKey) -and [string]$values[$moduleIdKey] -ne $prefix) {
            Add-Failure ("module ID mismatch: {0} expected {1}, actual {2}" -f $relativePath, $prefix, $values[$moduleIdKey])
        }
        if ($profile -in @("local", "dev")) {
            $expectedSchema = $schemaByModule[$prefix]
            $expectedUsername = $usernameByModule[$prefix]
            $usernameKey = "{0}_DATASOURCE_USERNAME" -f $prefix
            if ($values.Contains($urlKey) -and
                    [string]$values[$urlKey] -notmatch ("(?i)(?:/|databaseName=){0}(?:[;?]|$)" -f [regex]::Escape($expectedSchema))) {
                Add-Failure ("module datasource schema mismatch: {0} expected {1}" -f $relativePath, $expectedSchema)
            }
            if ($values.Contains($usernameKey) -and [string]$values[$usernameKey] -ne $expectedUsername) {
                Add-Failure ("module datasource username mismatch: {0} expected {1}" -f $relativePath, $expectedUsername)
            }
            if ($values.Contains("CPF_DB_URL") -and [string]$values["CPF_DB_URL"] -notmatch '(?i)(?:/|databaseName=)cpfDB(?:[;?]|$)') {
                Add-Failure ("cpf-core datasource must target cpfDB: {0}" -f $relativePath)
            }
            if ($values.Contains("CPF_DB_USERNAME") -and [string]$values["CPF_DB_USERNAME"] -ne "cpf_app") {
                Add-Failure ("cpf-core datasource username must be cpf_app: {0}" -f $relativePath)
            }
        }
        if ($values.Contains($jndiKey) -and [string]$values[$jndiKey] -ne $jndiByModule[$prefix]) {
            Add-Failure ("module datasource JNDI mismatch: {0} expected {1}" -f $relativePath, $jndiByModule[$prefix])
        }
        if ($values.Contains("CPF_DB_JNDI_NAME") -and [string]$values["CPF_DB_JNDI_NAME"] -ne "java:comp/env/jdbc/cpfCoreDataSource") {
            Add-Failure ("cpf-core datasource JNDI mismatch: {0}" -f $relativePath)
        }

        if ($mode -and $mode.ToLowerInvariant() -notin @("url", "jndi")) {
            Add-Failure ("DATASOURCE_MODE must be url or jndi: {0}" -f $relativePath)
        }
        $cpfMode = [string] $values["CPF_DB_MODE"]
        if ($cpfMode -and $cpfMode.ToLowerInvariant() -notin @("url", "jndi")) {
            Add-Failure ("CPF_DB_MODE must be url or jndi: {0}" -f $relativePath)
        }
        if ($profile -in @("local", "dev") -and $mode.ToLowerInvariant() -ne "url") {
            Add-Failure ("local/dev datasource mode must default to url: {0}" -f $relativePath)
        }
        if ($profile -in @("local", "dev") -and $cpfMode.ToLowerInvariant() -ne "url") {
            Add-Failure ("local/dev cpf-core datasource mode must default to url: {0}" -f $relativePath)
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
    $expectedModuleCodes = if ($profile -eq "prod") {
        @($moduleCodes | Where-Object { $_ -ne "REF" })
    } else {
        $moduleCodes
    }
    foreach ($moduleCode in $expectedModuleCodes) {
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
        if (($service.PSObject.Properties.Name -contains "runtimeMode") -and $service.runtimeMode -notin @("embedded-bootjar", "external-was", "external-tomcat-war")) {
            Add-Failure ("inventory runtimeMode invalid: {0} :: {1} :: {2}" -f $relativePath, $moduleCode, $service.runtimeMode)
        }
    }
    $eduService = @($inventory.services) | Where-Object { $_.module -eq "EDU" } | Select-Object -First 1
    if ($null -ne $eduService) {
        Add-Failure ("EDU inventory service must not exist: {0}" -f $relativePath)
    }
    if ($profile -eq "prod") {
        $refService = @($inventory.services) | Where-Object { $_.module -eq "REF" } | Select-Object -First 1
        if ($null -ne $refService) {
            Add-Failure ("REF reference service must not be included in the default production inventory: {0}" -f $relativePath)
        }
    }
    $inventoryChecks.Add([pscustomobject]@{
        file = $relativePath
        serviceCount = @($inventory.services).Count
        status = "DONE"
    }) | Out-Null
}

if (Test-Path -LiteralPath (Join-RootPath "deploy/env/prod-cpf-reference.env")) {
    Add-Failure "REF reference production env must not exist in the default deployment set."
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
    "cpf-batch/src/main/java/com/cpf/batch/edu",
    "cpf-batch/src/test/java/com/cpf/batch/edu",
    "cpf-reference/src/main/java/com/cpf/reference",
    "cpf-reference/src/test/java/com/cpf/reference",
    "cpf-common/src/main/java/com/cpf/common",
    "cpf-core/src/main/java/com/cpf/core",
    "cpf-docs/evidence"
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
    forbiddenModules = @("EDU", "CPF", "CMN")
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
