param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string[]] $Modules = @(),
    [string] $ResultDir = "",
    [int] $StartupTimeoutSeconds = 150,
    [int] $HttpTimeoutSeconds = 3,
    [string] $DbVendor = $env:CPF_DB_VENDOR,
    [string] $DbResourceRoot = $env:CPF_DB_RESOURCE_ROOT,
    [string] $DatabaseProfilePath = "",
    [switch] $BuildBeforeRun,
    [switch] $NoExitOnFailure
)

# PowerShell 7과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"

# Child process가 새 Windows process로 분리되어도 UTF-8 계약을 잃지 않도록 고정합니다.
$CpfUtf8ChildJavaOptions = '-Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8'
if ([string]::IsNullOrWhiteSpace($env:JAVA_TOOL_OPTIONS)) {
    $env:JAVA_TOOL_OPTIONS = $CpfUtf8ChildJavaOptions
} elseif ($env:JAVA_TOOL_OPTIONS -notmatch '(?:^|\s)-Dfile\.encoding=UTF-8(?:\s|$)') {
    $env:JAVA_TOOL_OPTIONS = ($env:JAVA_TOOL_OPTIONS.Trim() + ' ' + $CpfUtf8ChildJavaOptions)
}
$env:PYTHONUTF8 = '1'
$env:PYTHONIOENCODING = 'utf-8'
$env:PGCLIENTENCODING = 'UTF8'
$env:NLS_LANG = '.AL32UTF8'
. (Join-Path $PSScriptRoot "runtime-common.ps1")

# 각 모듈은 runtime-common.ps1에 정의된 포트 환경변수로 기동한다.
# BuildBeforeRun이 요청되면 빌드 실패 시 기존 jar로 대체하지 않고 실패로 기록한다.
$Root = Get-CpfRuntimeRoot -Root $Root
$ResultDir = Get-CpfRuntimeResultDir -Root $Root -ResultDir $ResultDir
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$selectedModules = @(Resolve-CpfRuntimeModules -Modules $Modules -Root $Root)
. (Join-Path $Root "cpf-tools\db\tools\database-profile-common.ps1")

if ([string]::IsNullOrWhiteSpace($DbVendor)) { $DbVendor = "mariadb" }
$DbVendor = Assert-CpfSupportedDatabaseVendor $DbVendor
if ([string]::IsNullOrWhiteSpace($DbResourceRoot)) {
    # 이 Script는 Repository Local Runtime Harness이므로 중앙 Source Pack을 명시적으로 선택합니다.
    # 제품 배포에서는 배포 Bundle의 외부 Pack 경로를 CPF_DB_RESOURCE_ROOT로 주입해야 합니다.
    $DbResourceRoot = Join-Path $Root ("cpf-tools\db\vendor\" + $DbVendor)
}
$DbResourceRoot = [System.IO.Path]::GetFullPath($DbResourceRoot)
$packManifest = Join-Path $DbResourceRoot "pack.json"
if (-not (Test-Path -LiteralPath $packManifest -PathType Leaf)) {
    throw "중앙 DB Vendor Pack을 찾을 수 없습니다. vendor=$DbVendor root=$DbResourceRoot"
}

if ([string]::IsNullOrWhiteSpace($DatabaseProfilePath)) {
    $DatabaseProfilePath = Join-Path $Root "cpf-tools\db\config\database-install.default.json"
} elseif (-not [System.IO.Path]::IsPathRooted($DatabaseProfilePath)) {
    $DatabaseProfilePath = Join-Path $Root $DatabaseProfilePath
}
$DatabaseProfilePath = [System.IO.Path]::GetFullPath($DatabaseProfilePath)
$databaseProfile = Get-CpfDatabaseProfile -Path $DatabaseProfilePath
$allowRuntimeDevDefault =
    ([string] $databaseProfile.environment).ToLowerInvariant() -in @("development", "dev", "local") -and
    [bool] $databaseProfile.policy.allowInlineDevDefaults
$databaseTargets = [ordered]@{}
foreach ($moduleKey in $databaseProfile.modules.PSObject.Properties.Name) {
    $target = ConvertTo-CpfModuleProfile `
        -Profile $databaseProfile `
        -ModuleKey $moduleKey `
        -SkipSecretResolution
    if (-not $target.enabled) {
        continue
    }
    if ([string] $target.vendor -cne $DbVendor) {
        throw (
            "Runtime DB Profile과 선택 Vendor가 다릅니다. " +
            "module=$moduleKey profileVendor=$($target.vendor) selectedVendor=$DbVendor"
        )
    }
    $target.runtimePassword = Resolve-CpfProfileSecret `
        -SecretSpec $databaseProfile.modules.$moduleKey.runtime.password `
        -DisplayName "$moduleKey.runtime.password" `
        -AllowDevDefault $allowRuntimeDevDefault
    $databaseTargets[$moduleKey] = $target
}

function Get-CpfRuntimeJdbcUrl {
    param([object] $Target)

    $hostName = [string] $Target.host
    $port = [int] $Target.port
    $databaseName = [string] $Target.databaseName
    switch ([string] $Target.vendor) {
        "mariadb" { return "jdbc:mariadb://${hostName}:${port}/${databaseName}" }
        "postgresql" { return "jdbc:postgresql://${hostName}:${port}/${databaseName}" }
        "oracle" { return "jdbc:oracle:thin:@//${hostName}:${port}/${databaseName}" }
        default { throw "지원하지 않는 Runtime DB Vendor입니다: $($Target.vendor)" }
    }
}

function Get-CpfRuntimeDriverClassName {
    param([string] $Vendor)

    switch ($Vendor) {
        "mariadb" { return "org.mariadb.jdbc.Driver" }
        "postgresql" { return "org.postgresql.Driver" }
        "oracle" { return "oracle.jdbc.OracleDriver" }
        default { throw "지원하지 않는 Runtime DB Vendor입니다: $Vendor" }
    }
}

$databaseEnvironment = [ordered]@{}
function Add-CpfRuntimeDatabaseEnvironment {
    param(
        [string] $Prefix,
        [object] $Target
    )

    $url = Get-CpfRuntimeJdbcUrl -Target $Target
    $driver = Get-CpfRuntimeDriverClassName -Vendor ([string] $Target.vendor)
    $databaseEnvironment["${Prefix}_DB_VENDOR"] = [string] $Target.vendor
    $databaseEnvironment["${Prefix}_DB_URL"] = $url
    $databaseEnvironment["${Prefix}_DB_NAME"] = [string] $Target.databaseName
    $databaseEnvironment["${Prefix}_DB_USERNAME"] = [string] $Target.runtimeUsername
    $databaseEnvironment["${Prefix}_DB_PASSWORD"] = [string] $Target.runtimePassword
    $databaseEnvironment["${Prefix}_DB_DRIVER"] = $driver
    $databaseEnvironment["${Prefix}_DB_DRIVER_CLASS_NAME"] = $driver
    $databaseEnvironment["${Prefix}_DATASOURCE_VENDOR"] = [string] $Target.vendor
    $databaseEnvironment["${Prefix}_DATASOURCE_URL"] = $url
    $databaseEnvironment["${Prefix}_DATASOURCE_USERNAME"] = [string] $Target.runtimeUsername
    $databaseEnvironment["${Prefix}_DATASOURCE_PASSWORD"] = [string] $Target.runtimePassword
    $databaseEnvironment["${Prefix}_DATASOURCE_DRIVER"] = $driver
    $databaseEnvironment["${Prefix}_DATASOURCE_DRIVER_CLASS_NAME"] = $driver
    $databaseEnvironment["${Prefix}_DATABASE_VENDOR"] = [string] $Target.vendor
    $databaseEnvironment["${Prefix}_DATABASE_URL"] = $url
    $databaseEnvironment["${Prefix}_DATABASE_USERNAME"] = [string] $Target.runtimeUsername
    $databaseEnvironment["${Prefix}_DATABASE_PASSWORD"] = [string] $Target.runtimePassword
    $databaseEnvironment["${Prefix}_DATABASE_DRIVER"] = $driver
    $databaseEnvironment["${Prefix}_DATABASE_DRIVER_CLASS_NAME"] = $driver
}

foreach ($moduleKey in $databaseTargets.Keys) {
    $target = $databaseTargets[$moduleKey]
    if ($moduleKey -eq "common") {
        Add-CpfRuntimeDatabaseEnvironment -Prefix "CMN_SAMPLE" -Target $target
    } else {
        Add-CpfRuntimeDatabaseEnvironment -Prefix ([string] $target.systemCode) -Target $target
    }
}
if ($databaseTargets.Contains("core")) {
    $coreTarget = $databaseTargets["core"]
    # CMN code/message/config는 cpfDB 소유이고 GWY도 CPF 공개 metadata만 읽습니다.
    Add-CpfRuntimeDatabaseEnvironment -Prefix "CMN" -Target $coreTarget
    Add-CpfRuntimeDatabaseEnvironment -Prefix "GWY" -Target $coreTarget
    $databaseEnvironment["CPF_DB_HOST"] = [string] $coreTarget.host
    $databaseEnvironment["CPF_DB_PORT"] = [string] $coreTarget.port
}

$generatedDatabaseBindings = @()
foreach ($generatedModule in @($selectedModules | Where-Object {
            [bool] $_.generatedDomain -and [bool] $_.databaseEnabled
        })) {
    if ([string] $generatedModule.databaseRole -cne "CUSTOMER_BUSINESS_DB") {
        throw "Generated Domain DB Role이 canonical 계약과 다릅니다: module=$($generatedModule.module) role=$($generatedModule.databaseRole)"
    }

    # Generated Project에는 DB Vendor/Endpoint/Secret Profile을 영구 생성하지 않습니다.
    # 배포 소유 CUSTOMER_BUSINESS_DB binding을 임의 SystemCode 기반 환경변수로 해석합니다.
    $prefix = [string] $generatedModule.module
    $bindingNames = [ordered]@{
        url = "${prefix}_DATASOURCE_URL"
        username = "${prefix}_DATASOURCE_USERNAME"
        password = "${prefix}_DATASOURCE_PASSWORD"
        driver = "${prefix}_DATASOURCE_DRIVER"
    }
    $bindingValues = [ordered]@{}
    foreach ($bindingName in $bindingNames.Keys) {
        $environmentName = [string] $bindingNames[$bindingName]
        $value = [Environment]::GetEnvironmentVariable($environmentName, "Process")
        if ([string]::IsNullOrWhiteSpace($value)) {
            throw "Generated Domain CUSTOMER_BUSINESS_DB 환경 Binding이 없습니다: module=$prefix env=$environmentName"
        }
        $bindingValues[$bindingName] = $value
    }
    if ([string] $bindingValues.url -notmatch '^jdbc:(mariadb|postgresql|oracle):') {
        throw "Generated Domain Datasource URL의 Vendor를 판별할 수 없습니다: module=$prefix env=$($bindingNames.url)"
    }
    $bindingVendor = $Matches[1].ToLowerInvariant()
    if ($bindingVendor -cne $DbVendor) {
        throw "Generated Domain Datasource Vendor가 선택 Vendor와 다릅니다: module=$prefix bindingVendor=$bindingVendor selectedVendor=$DbVendor"
    }
    $generatedDatabaseBindings += [ordered]@{
        module = $prefix
        databaseRole = [string] $generatedModule.databaseRole
        vendor = $bindingVendor
        source = "deployment-environment"
        environmentNames = @($bindingNames.Values)
    }
}
$databaseEnvironment["CPF_MBW_DB_ENABLED"] = "true"

$resultPath = Join-Path $ResultDir "runtime-start-services-result.json"
$statePath = Join-Path $ResultDir "runtime-services.json"
$started = @()
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = Get-CpfRuntimeStatusText "Partial"
    modules = @()
    dbVendor = $DbVendor
    dbResourceRoot = $DbResourceRoot
    databaseProfile = Get-CpfRelativePath -Root $Root -Path $DatabaseProfilePath
    generatedDatabaseBindings = @($generatedDatabaseBindings)
    stateFile = Get-CpfRelativePath -Root $Root -Path $statePath
}

function Save-StartResult {
    $result.finishedAt = (Get-Date).ToString("o")
    Write-CpfRuntimeJson -Path $resultPath -Value $result
}

function New-ModuleStartResult {
    param([object] $Module)

    return [ordered]@{
        module = $Module.module
        port = $Module.port
        status = Get-CpfRuntimeStatusText "NotVerified"
        alreadyListening = $false
        startedByHarness = $false
        bootJarBuildRequired = [bool] $BuildBeforeRun
        bootJarBuildStatus = $(if ($BuildBeforeRun) { "PENDING" } else { "NOT_REQUESTED" })
        jarFallbackUsed = $false
        processStarted = $false
        portOpened = $false
        processStillAliveAfterProbe = $false
        health = $null
        healthCheckPassed = $false
        finalRuntimeUsable = $false
        failureClassification = $null
        failureRootCause = ""
    }
}

function Set-ModuleFailure {
    param(
        [object] $ModuleResult,
        [string] $Classification,
        [string] $RootCause,
        [object] $HttpProbe = $null
    )

    $ModuleResult.status = Get-CpfRuntimeStatusText "Failed"
    $ModuleResult.finalRuntimeUsable = $false
    $ModuleResult.failureClassification = $Classification
    $ModuleResult.failureRootCause = $RootCause
    if ($HttpProbe -ne $null) {
        $ModuleResult.health = $HttpProbe
    }
}

function Invoke-BootJarBuildIfNeeded {
    param([object] $Module)

    $existingJar = Find-CpfRuntimeBootJar -Root $Root -Module $Module
    if (-not $BuildBeforeRun) {
        return [ordered]@{
            required = $false
            status = $(if ($existingJar -ne $null) { "SKIPPED" } else { "FAILED" })
            reason = $(if ($existingJar -ne $null) { "use existing bootJar" } else { "existing bootJar was not found and build was not requested" })
            jar = $(if ($existingJar -ne $null) { Get-CpfRelativePath -Root $Root -Path $existingJar } else { $null })
        }
    }

    $task = ":" + $Module.projectName + ":bootJar"
    $buildOutput = Join-Path $ResultDir ("runtime-" + $Module.moduleLower + "-bootjar.out.log")
    $buildError = Join-Path $ResultDir ("runtime-" + $Module.moduleLower + "-bootjar.err.log")
    $build = Start-Process `
        -FilePath (Join-Path $Root "gradlew.bat") `
        -ArgumentList @($task, "--offline", "--no-daemon", "--max-workers=1", "--console=plain") `
        -WorkingDirectory $Root `
        -RedirectStandardOutput $buildOutput `
        -RedirectStandardError $buildError `
        -WindowStyle Hidden `
        -Wait `
        -PassThru

    $jar = Find-CpfRuntimeBootJar -Root $Root -Module $Module
    return [ordered]@{
        required = $true
        status = $(if ($build.ExitCode -eq 0 -and $jar -ne $null) { "BUILT" } else { "FAILED" })
        exitCode = $build.ExitCode
        jar = $(if ($jar -ne $null) { Get-CpfRelativePath -Root $Root -Path $jar } else { $null })
        outputLog = Get-CpfRelativePath -Root $Root -Path $buildOutput
        errorLog = Get-CpfRelativePath -Root $Root -Path $buildError
    }
}

function Test-AlreadyListeningModule {
    param(
        [object] $Module,
        [object] $ModuleResult
    )

    $ModuleResult.alreadyListening = $true
    $ModuleResult.portOpened = $true
    $httpProbe = Invoke-CpfRuntimeHttpProbe -Port $Module.port -Path $Module.healthPath -TimeoutSeconds $HttpTimeoutSeconds
    $ModuleResult.health = $httpProbe
    $ModuleResult.healthCheckPassed = [bool] $httpProbe.success

    $pidInfo = Get-CpfRuntimePidInfo -ResultDir $ResultDir -Module $Module
    $ModuleResult.pid = $pidInfo.pid
    $ModuleResult.processName = $pidInfo.processName
    $ModuleResult.processStillAliveAfterProbe = [bool] $pidInfo.processAlive
    $ModuleResult.finalRuntimeUsable = ($ModuleResult.healthCheckPassed -and $ModuleResult.processStillAliveAfterProbe)

    if ($ModuleResult.finalRuntimeUsable) {
        $ModuleResult.status = Get-CpfRuntimeStatusText "Done"
        return
    }

    $ModuleResult.status = Get-CpfRuntimeStatusText "NeedsReview"
    $ModuleResult.failureClassification = "environment"
    $ModuleResult.failureRootCause = "port is already listening but the harness cannot prove both healthCheckPassed=true and processAlive=true."
}

try {
    foreach ($module in $selectedModules) {
        $moduleResult = New-ModuleStartResult -Module $module

        if (Test-CpfRuntimeTcpPort -Port $module.port) {
            Test-AlreadyListeningModule -Module $module -ModuleResult $moduleResult
            $result.modules += $moduleResult
            Save-StartResult
            continue
        }

        $buildResult = Invoke-BootJarBuildIfNeeded -Module $module
        $moduleResult.bootJarBuild = $buildResult
        $moduleResult.bootJarBuildStatus = [string] $buildResult.status
        if ($BuildBeforeRun -and $buildResult.status -ne "BUILT") {
            Set-ModuleFailure `
                -ModuleResult $moduleResult `
                -Classification "implementation" `
                -RootCause "bootJar build failed; existing jar fallback is disabled when BuildBeforeRun is required."
            $result.modules += $moduleResult
            Save-StartResult
            continue
        }

        $jarPath = Find-CpfRuntimeBootJar -Root $Root -Module $module
        if ($jarPath -eq $null) {
            Set-ModuleFailure `
                -ModuleResult $moduleResult `
                -Classification "implementation" `
                -RootCause "bootJar file was not found."
            $result.modules += $moduleResult
            Save-StartResult
            continue
        }

        $stdout = Join-Path $ResultDir ("runtime-" + $module.moduleLower + ".out.log")
        $stderr = Join-Path $ResultDir ("runtime-" + $module.moduleLower + ".err.log")
        if (Test-Path -LiteralPath $stdout) { Remove-Item -LiteralPath $stdout -Force }
        if (Test-Path -LiteralPath $stderr) { Remove-Item -LiteralPath $stderr -Force }

        $previousPort = [Environment]::GetEnvironmentVariable($module.portEnv, "Process")
        $previousCpfSystemCode = [Environment]::GetEnvironmentVariable("CPF_SYSTEM_CODE", "Process")
        $previousCpfInstanceId = [Environment]::GetEnvironmentVariable("CPF_RUNTIME_INSTANCE_ID", "Process")
        $previousCpfEnvironment = [Environment]::GetEnvironmentVariable("CPF_ENV", "Process")
        $previousLogRoot = [Environment]::GetEnvironmentVariable("CPF_LOG_ROOT", "Process")
        $previousActiveProfile = [Environment]::GetEnvironmentVariable("SPRING_PROFILES_ACTIVE", "Process")
        $previousDbVendor = [Environment]::GetEnvironmentVariable("CPF_DB_VENDOR", "Process")
        $previousDbResourceRoot = [Environment]::GetEnvironmentVariable("CPF_DB_RESOURCE_ROOT", "Process")
        $previousDatabaseEnvironment = [ordered]@{}
        try {
            [Environment]::SetEnvironmentVariable($module.portEnv, [string] $module.port, "Process")
            # Do not synthesize <SYSTEM>01. Explicit CPF_RUNTIME_INSTANCE_ID is preserved when supplied;
            # otherwise the Runtime resolves its canonical identity from the real Runtime hostname.
            $launchInstanceId = [Environment]::GetEnvironmentVariable("CPF_RUNTIME_INSTANCE_ID", "Process")
            if ([string]::IsNullOrWhiteSpace($launchInstanceId)) {
                [Environment]::SetEnvironmentVariable("CPF_RUNTIME_INSTANCE_ID", $null, "Process")
            }
            [Environment]::SetEnvironmentVariable("CPF_SYSTEM_CODE", $module.module, "Process")
            [Environment]::SetEnvironmentVariable("CPF_ENV", "local", "Process")
            [Environment]::SetEnvironmentVariable("CPF_LOG_ROOT", (Join-Path $Root "logs"), "Process")
            [Environment]::SetEnvironmentVariable("SPRING_PROFILES_ACTIVE", "local", "Process")
            [Environment]::SetEnvironmentVariable("CPF_DB_VENDOR", $DbVendor, "Process")
            [Environment]::SetEnvironmentVariable("CPF_DB_RESOURCE_ROOT", $DbResourceRoot, "Process")
            foreach ($environmentName in $databaseEnvironment.Keys) {
                $previousDatabaseEnvironment[$environmentName] =
                    [Environment]::GetEnvironmentVariable($environmentName, "Process")
                [Environment]::SetEnvironmentVariable(
                    $environmentName,
                    [string] $databaseEnvironment[$environmentName],
                    "Process")
            }

            $process = Start-Process `
                -FilePath "java" `
                -ArgumentList @("-jar", $jarPath, "--server.port=$($module.port)") `
                -WorkingDirectory $Root `
                -RedirectStandardOutput $stdout `
                -RedirectStandardError $stderr `
                -WindowStyle Hidden `
                -PassThru
        } finally {
            [Environment]::SetEnvironmentVariable($module.portEnv, $previousPort, "Process")
            [Environment]::SetEnvironmentVariable("CPF_SYSTEM_CODE", $previousCpfSystemCode, "Process")
            [Environment]::SetEnvironmentVariable("CPF_RUNTIME_INSTANCE_ID", $previousCpfInstanceId, "Process")
            [Environment]::SetEnvironmentVariable("CPF_ENV", $previousCpfEnvironment, "Process")
            [Environment]::SetEnvironmentVariable("CPF_LOG_ROOT", $previousLogRoot, "Process")
            [Environment]::SetEnvironmentVariable("SPRING_PROFILES_ACTIVE", $previousActiveProfile, "Process")
            [Environment]::SetEnvironmentVariable("CPF_DB_VENDOR", $previousDbVendor, "Process")
            [Environment]::SetEnvironmentVariable("CPF_DB_RESOURCE_ROOT", $previousDbResourceRoot, "Process")
            foreach ($environmentName in $previousDatabaseEnvironment.Keys) {
                [Environment]::SetEnvironmentVariable(
                    $environmentName,
                    $previousDatabaseEnvironment[$environmentName],
                    "Process")
            }
        }

        $moduleResult.processStarted = $true
        $moduleResult.pid = $process.Id
        $moduleResult.jar = Get-CpfRelativePath -Root $Root -Path $jarPath
        $moduleResult.stdout = Get-CpfRelativePath -Root $Root -Path $stdout
        $moduleResult.stderr = Get-CpfRelativePath -Root $Root -Path $stderr

        $pidPath = Join-Path $ResultDir ("runtime-" + $module.moduleLower + ".pid")
        [System.IO.File]::WriteAllText($pidPath, [string] $process.Id, $script:CpfRuntimeUtf8NoBom)
        $started += [ordered]@{
            module = $module.module
            port = $module.port
            pid = $process.Id
            jar = Get-CpfRelativePath -Root $Root -Path $jarPath
            stdout = Get-CpfRelativePath -Root $Root -Path $stdout
            stderr = Get-CpfRelativePath -Root $Root -Path $stderr
        }

        $deadline = (Get-Date).AddSeconds($StartupTimeoutSeconds)
        while ((Get-Date) -lt $deadline) {
            $process.Refresh()
            if ($process.HasExited) {
                $moduleResult.processStillAliveAfterProbe = $false
                $moduleResult.exitCode = $process.ExitCode
                $moduleResult.stdoutTail = Get-CpfRuntimeTailPreview -Path $stdout -LineCount 40
                $moduleResult.stderrTail = Get-CpfRuntimeTailPreview -Path $stderr -LineCount 40
                Set-ModuleFailure `
                    -ModuleResult $moduleResult `
                    -Classification "implementation" `
                    -RootCause "runtime process exited before health check passed."
                break
            }

            if (Test-CpfRuntimeTcpPort -Port $module.port) {
                $moduleResult.portOpened = $true
                $httpProbe = Invoke-CpfRuntimeHttpProbe -Port $module.port -Path $module.healthPath -TimeoutSeconds $HttpTimeoutSeconds
                $moduleResult.health = $httpProbe
                $moduleResult.healthCheckPassed = [bool] $httpProbe.success
                $process.Refresh()
                $moduleResult.processStillAliveAfterProbe = (-not $process.HasExited)
                if ($moduleResult.healthCheckPassed -and $moduleResult.processStillAliveAfterProbe) {
                    $moduleResult.status = Get-CpfRuntimeStatusText "Done"
                    $moduleResult.startedByHarness = $true
                    $moduleResult.finalRuntimeUsable = $true
                    break
                }
            }
            Start-Sleep -Seconds 2
        }

        if ($moduleResult.status -eq (Get-CpfRuntimeStatusText "NotVerified")) {
            $rootCause = "runtime port did not open before startup timeout."
            if ($moduleResult.portOpened -and -not $moduleResult.healthCheckPassed) {
                $rootCause = "runtime port opened but health check did not pass before startup timeout."
            } elseif ($moduleResult.portOpened -and -not $moduleResult.processStillAliveAfterProbe) {
                $rootCause = "runtime port opened but process was not alive after probe."
            }
            $moduleResult.stdoutTail = Get-CpfRuntimeTailPreview -Path $stdout -LineCount 40
            $moduleResult.stderrTail = Get-CpfRuntimeTailPreview -Path $stderr -LineCount 40
            Set-ModuleFailure `
                -ModuleResult $moduleResult `
                -Classification "environment" `
                -RootCause $rootCause `
                -HttpProbe $moduleResult.health
        }

        $result.modules += $moduleResult
        Save-StartResult
    }

    $state = [ordered]@{
        updatedAt = (Get-Date).ToString("o")
        root = $Root
        resultDir = $ResultDir
        started = @($started)
    }
    Write-CpfRuntimeJson -Path $statePath -Value $state

    $failed = @($result.modules | Where-Object { $_.finalRuntimeUsable -ne $true })
    $result.status = $(if ($failed.Count -eq 0) { Get-CpfRuntimeStatusText "Done" } else { Get-CpfRuntimeStatusText "Failed" })
    Save-StartResult
    if (-not $NoExitOnFailure -and $result.status -ne (Get-CpfRuntimeStatusText "Done")) {
        exit 1
    }
} catch {
    $result.status = Get-CpfRuntimeStatusText "Failed"
    $result.error = $_.Exception.Message
    Save-StartResult
    throw
}

Write-Host "runtime start finished. status=$($result.status) result=$resultPath"
