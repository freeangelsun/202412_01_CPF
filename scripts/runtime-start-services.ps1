param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string[]] $Modules = @("ACC", "MBR", "EXS", "ADM"),
    [string] $ResultDir = "",
    [int] $StartupTimeoutSeconds = 150,
    [switch] $BuildBeforeRun,
    [switch] $NoExitOnFailure
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "runtime-common.ps1")

# ACC_SERVER_PORT, MBR_SERVER_PORT, ADM_SERVER_PORT, EXS_SERVER_PORT, BAT_SERVER_PORT 값은
# runtime-common.ps1의 모듈 맵에서 읽어 각 서비스 port override로 사용한다.
$Root = Get-CpfRuntimeRoot -Root $Root
$ResultDir = Get-CpfRuntimeResultDir -Root $Root -ResultDir $ResultDir
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$resultPath = Join-Path $ResultDir "runtime-start-services-result.json"
$statePath = Join-Path $ResultDir "runtime-services.json"
$selectedModules = Resolve-CpfRuntimeModules -Modules $Modules
$started = @()
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = Get-CpfRuntimeStatusText "Partial"
    modules = @()
    stateFile = Get-CpfRelativePath -Root $Root -Path $statePath
}

function Save-StartResult {
    $result.finishedAt = (Get-Date).ToString("o")
    Write-CpfRuntimeJson -Path $resultPath -Value $result
}

function Invoke-BootJarBuildIfNeeded {
    param([object] $Module)

    $jar = Find-CpfRuntimeBootJar -Root $Root -Module $Module
    if ($jar -ne $null -and -not $BuildBeforeRun) {
        return [ordered]@{
            status = "SKIPPED"
            reason = "use existing bootJar"
            jar = Get-CpfRelativePath -Root $Root -Path $jar
        }
    }

    $task = ":" + $Module.moduleLower + ":bootJar"
    $buildOutput = Join-Path $ResultDir ("runtime-" + $Module.moduleLower + "-bootjar.out.log")
    $buildError = Join-Path $ResultDir ("runtime-" + $Module.moduleLower + "-bootjar.err.log")
    $build = Start-Process `
        -FilePath (Join-Path $Root "gradlew.bat") `
        -ArgumentList @($task, "--offline", "--no-daemon", "--console=plain") `
        -WorkingDirectory $Root `
        -RedirectStandardOutput $buildOutput `
        -RedirectStandardError $buildError `
        -WindowStyle Hidden `
        -Wait `
        -PassThru

    $jar = Find-CpfRuntimeBootJar -Root $Root -Module $Module
    return [ordered]@{
        status = $(if ($build.ExitCode -eq 0 -and $jar -ne $null) { "BUILT" } else { "FAILED" })
        exitCode = $build.ExitCode
        jar = $(if ($jar -ne $null) { Get-CpfRelativePath -Root $Root -Path $jar } else { $null })
        outputLog = Get-CpfRelativePath -Root $Root -Path $buildOutput
        errorLog = Get-CpfRelativePath -Root $Root -Path $buildError
    }
}

try {
    foreach ($module in $selectedModules) {
        $moduleResult = [ordered]@{
            module = $module.module
            port = $module.port
            status = Get-CpfRuntimeStatusText "NotVerified"
            alreadyListening = $false
            startedByHarness = $false
        }

        if (Test-CpfRuntimeTcpPort -Port $module.port) {
            $moduleResult.status = Get-CpfRuntimeStatusText "Done"
            $moduleResult.alreadyListening = $true
            $moduleResult.reason = "port already listening, reuse existing runtime"
            $result.modules += $moduleResult
            Save-StartResult
            continue
        }

        $buildResult = Invoke-BootJarBuildIfNeeded -Module $module
        $moduleResult.bootJarBuild = $buildResult
        $jarPath = Find-CpfRuntimeBootJar -Root $Root -Module $module
        if ($jarPath -eq $null) {
            $moduleResult.status = Get-CpfRuntimeStatusText "Failed"
            $moduleResult.failureClassification = "implementation"
            $moduleResult.error = "bootJar file was not found."
            $result.modules += $moduleResult
            Save-StartResult
            continue
        }

        $stdout = Join-Path $ResultDir ("runtime-" + $module.moduleLower + ".out.log")
        $stderr = Join-Path $ResultDir ("runtime-" + $module.moduleLower + ".err.log")
        if (Test-Path -LiteralPath $stdout) { Remove-Item -LiteralPath $stdout -Force }
        if (Test-Path -LiteralPath $stderr) { Remove-Item -LiteralPath $stderr -Force }

        $previousPort = [Environment]::GetEnvironmentVariable($module.portEnv, "Process")
        $previousWasId = [Environment]::GetEnvironmentVariable("WAS_ID", "Process")
        $previousServerInstance = [Environment]::GetEnvironmentVariable("SERVER_INSTANCE_ID", "Process")
        $previousModuleId = [Environment]::GetEnvironmentVariable("CPF_MODULE_ID", "Process")
        $previousLogBase = [Environment]::GetEnvironmentVariable("CPF_LOGGING_FILE_BASE_PATH", "Process")
        try {
            [Environment]::SetEnvironmentVariable($module.portEnv, [string] $module.port, "Process")
            [Environment]::SetEnvironmentVariable("WAS_ID", ($module.moduleLower + "AP01"), "Process")
            [Environment]::SetEnvironmentVariable("SERVER_INSTANCE_ID", ($module.moduleLower + "-local-01"), "Process")
            [Environment]::SetEnvironmentVariable("CPF_MODULE_ID", $module.module, "Process")
            [Environment]::SetEnvironmentVariable("CPF_LOGGING_FILE_BASE_PATH", "logs", "Process")

            $process = Start-Process `
                -FilePath "java" `
                -ArgumentList @("-jar", $jarPath) `
                -WorkingDirectory $Root `
                -RedirectStandardOutput $stdout `
                -RedirectStandardError $stderr `
                -WindowStyle Hidden `
                -PassThru
        } finally {
            [Environment]::SetEnvironmentVariable($module.portEnv, $previousPort, "Process")
            [Environment]::SetEnvironmentVariable("WAS_ID", $previousWasId, "Process")
            [Environment]::SetEnvironmentVariable("SERVER_INSTANCE_ID", $previousServerInstance, "Process")
            [Environment]::SetEnvironmentVariable("CPF_MODULE_ID", $previousModuleId, "Process")
            [Environment]::SetEnvironmentVariable("CPF_LOGGING_FILE_BASE_PATH", $previousLogBase, "Process")
        }

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
                $moduleResult.status = Get-CpfRuntimeStatusText "Failed"
                $moduleResult.failureClassification = "implementation"
                $moduleResult.exitCode = $process.ExitCode
                $moduleResult.stdoutTail = Get-CpfRuntimeTailPreview -Path $stdout -LineCount 40
                $moduleResult.stderrTail = Get-CpfRuntimeTailPreview -Path $stderr -LineCount 40
                break
            }
            if (Test-CpfRuntimeTcpPort -Port $module.port) {
                $moduleResult.status = Get-CpfRuntimeStatusText "Done"
                $moduleResult.startedByHarness = $true
                $moduleResult.pid = $process.Id
                $moduleResult.stdout = Get-CpfRelativePath -Root $Root -Path $stdout
                $moduleResult.stderr = Get-CpfRelativePath -Root $Root -Path $stderr
                break
            }
            Start-Sleep -Seconds 2
        }

        if ($moduleResult.status -eq (Get-CpfRuntimeStatusText "NotVerified")) {
            $moduleResult.status = Get-CpfRuntimeStatusText "Failed"
            $moduleResult.failureClassification = "environment"
            $moduleResult.error = "runtime port did not open before startup timeout."
            $moduleResult.stdoutTail = Get-CpfRuntimeTailPreview -Path $stdout -LineCount 40
            $moduleResult.stderrTail = Get-CpfRuntimeTailPreview -Path $stderr -LineCount 40
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

    $failed = @($result.modules | Where-Object { $_.status -ne (Get-CpfRuntimeStatusText "Done") })
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
