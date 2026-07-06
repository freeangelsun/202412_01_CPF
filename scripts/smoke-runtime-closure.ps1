param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = "",
    [int] $StartupTimeoutSeconds = 180,
    [switch] $StartServices,
    [switch] $SkipServiceStart
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

function New-UnicodeText {
    param([int[]] $CodePoints)
    return -join ($CodePoints | ForEach-Object { [char] $_ })
}

$StatusDone = New-UnicodeText @(0xC644, 0xB8CC)
$StatusPartial = New-UnicodeText @(0xBD80, 0xBD84, 0x20, 0xAD6C, 0xD604)
$StatusNotVerified = New-UnicodeText @(0xBBF8, 0xAC80, 0xC99D)
$StatusFailed = New-UnicodeText @(0xC2E4, 0xD328)

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/runtime-smoke"
}
if (-not $StartServices) {
    $SkipServiceStart = $true
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
. (Join-Path $Root "scripts/runtime-diagnostics.ps1")

$resultPath = Join-Path $ResultDir "runtime-closure-result.json"
$serviceLog = Join-Path $ResultDir "run-local-services-closure.out.log"
$serviceErrLog = Join-Path $ResultDir "run-local-services-closure.err.log"
$gradleUserHome = if ([string]::IsNullOrWhiteSpace($env:GRADLE_USER_HOME)) {
    Join-Path $env:USERPROFILE ".gradle"
} else {
    $env:GRADLE_USER_HOME
}
$services = @(
    [ordered]@{ module = "ACC"; port = 8080 },
    [ordered]@{ module = "MBR"; port = 8081 },
    [ordered]@{ module = "ADM"; port = 8090 },
    [ordered]@{ module = "EXS"; port = 8092 }
)

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = $StatusPartial
    serviceStart = [ordered]@{
        status = $StatusNotVerified
        logPath = $serviceLog.Substring($Root.Length).TrimStart('\', '/')
        errorLogPath = $serviceErrLog.Substring($Root.Length).TrimStart('\', '/')
        ports = @()
    }
    fileLogStandard = [ordered]@{ status = $StatusNotVerified }
    traceBoost = [ordered]@{ status = $StatusNotVerified }
    batTraceBoost = [ordered]@{ status = $StatusNotVerified }
}

function Save-Result {
    $result.finishedAt = (Get-Date).ToString("o")
    [System.IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 40), $Utf8NoBom)
}

function Add-ServiceLogTail {
    if (Test-Path -LiteralPath $serviceLog) {
        $result.serviceStart.logTail = @(Get-Content -LiteralPath $serviceLog -Encoding UTF8 -Tail 80 -ErrorAction SilentlyContinue)
    }
    if (Test-Path -LiteralPath $serviceErrLog) {
        $result.serviceStart.errorLogTail = @(Get-Content -LiteralPath $serviceErrLog -Encoding UTF8 -Tail 80 -ErrorAction SilentlyContinue)
    }
}

function Test-ServicePorts {
    $states = @()
    foreach ($service in $services) {
        $client = [System.Net.Sockets.TcpClient]::new()
        $listening = $false
        try {
            $task = $client.ConnectAsync("127.0.0.1", $service.port)
            $listening = $task.Wait(700) -and $client.Connected
        } catch {
            $listening = $false
        } finally {
            $client.Dispose()
        }
        $states += [ordered]@{
            module = $service.module
            port = $service.port
            listening = $listening
            owningProcess = $null
        }
    }
    $result.serviceStart.ports = $states
    return @($states | Where-Object { -not $_.listening }).Count -eq 0
}

function Read-SmokeResult {
    param(
        [string] $Name,
        [string] $FileName
    )

    $path = Join-Path $ResultDir $FileName
    if (-not (Test-Path -LiteralPath $path)) {
        return [ordered]@{
            status = $StatusNotVerified
            name = $Name
            resultPath = $path.Substring($Root.Length).TrimStart('\', '/')
            reason = "smoke result file not found"
        }
    }
    try {
        $json = Get-Content -LiteralPath $path -Encoding UTF8 -Raw | ConvertFrom-Json
        return [ordered]@{
            status = [string] $json.status
            name = $Name
            resultPath = $path.Substring($Root.Length).TrimStart('\', '/')
            finishedAt = $json.finishedAt
            error = $json.error
        }
    } catch {
        return [ordered]@{
            status = $StatusFailed
            name = $Name
            resultPath = $path.Substring($Root.Length).TrimStart('\', '/')
            error = $_.Exception.Message
        }
    }
}

$serviceProcess = $null
$startedAt = Get-Date
try {
    if (-not $SkipServiceStart) {
        if (Test-Path -LiteralPath $serviceLog) {
            Remove-Item -LiteralPath $serviceLog -Force
        }
        if (Test-Path -LiteralPath $serviceErrLog) {
            Remove-Item -LiteralPath $serviceErrLog -Force
        }
        $env:GRADLE_USER_HOME = $gradleUserHome
        $serviceProcess = Start-Process `
            -FilePath (Join-Path $Root "gradlew.bat") `
            -ArgumentList @("runLocalServices", "-PcpfRunServices=MBR,ACC,ADM,EXS", "--offline", "--no-daemon", "--console=plain") `
            -WorkingDirectory $Root `
            -RedirectStandardOutput $serviceLog `
            -RedirectStandardError $serviceErrLog `
            -WindowStyle Hidden `
            -PassThru
        $result.serviceStart.processId = $serviceProcess.Id
        $result.serviceStart.gradleUserHome = $gradleUserHome
    }

    if ($SkipServiceStart) {
        $result.serviceStart.status = $StatusNotVerified
        $result.serviceStart.reason = "service start skipped"
    } else {
        $deadline = (Get-Date).AddSeconds($StartupTimeoutSeconds)
        while ((Get-Date) -lt $deadline) {
            if (Test-ServicePorts) {
                $result.serviceStart.status = $StatusDone
                break
            }
            if ($serviceProcess -ne $null) {
                $serviceProcess.Refresh()
                $result.serviceStart.processExited = $serviceProcess.HasExited
                if ($serviceProcess.HasExited) {
                    $result.serviceStart.exitCode = $serviceProcess.ExitCode
                    $result.serviceStart.status = $StatusFailed
                    break
                }
            }
            Start-Sleep -Seconds 3
        }
    }

    if (-not $SkipServiceStart -and $result.serviceStart.status -ne $StatusDone) {
        Test-ServicePorts | Out-Null
        $result.serviceStart.status = $(if ($result.serviceStart.status -eq $StatusFailed) { $StatusFailed } else { $StatusNotVerified })
        $result.serviceStart.diagnostics = New-CpfRuntimeDiagnostic -Root $Root -Module "ACC" -Ports @(8080, 8081, 8090, 8092) -ErrorMessage "runLocalServices did not expose required ports."
        Add-ServiceLogTail
    }

    $result.fileLogStandard = Read-SmokeResult -Name "file-log-standard" -FileName "file-log-standard-result.json"
    $result.traceBoost = Read-SmokeResult -Name "trace-boost" -FileName "trace-boost-runtime-result.json"
    $result.batTraceBoost = Read-SmokeResult -Name "bat-trace-boost" -FileName "bat-trace-boost-runtime-result.json"

    $failed = @(
        $result.serviceStart,
        $result.fileLogStandard,
        $result.traceBoost,
        $result.batTraceBoost
    ) | Where-Object { $_.status -eq $StatusFailed -or $_.status -eq $StatusNotVerified }
    $result.status = $(if ($failed.Count -eq 0) { $StatusDone } else { $StatusFailed })
    Save-Result
    if ($result.status -eq $StatusFailed) {
        exit 1
    }
} catch {
    $result.status = $StatusFailed
    $result.error = $_.Exception.Message
    Save-Result
    throw
} finally {
    if ($serviceProcess -ne $null) {
        $serviceProcess.Refresh()
        if (-not $serviceProcess.HasExited) {
            Stop-Process -Id $serviceProcess.Id -Force -ErrorAction SilentlyContinue
        }
    }
}

Write-Host "runtime closure smoke finished. status=$($result.status) result=$resultPath"
