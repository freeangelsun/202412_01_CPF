param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $AdmBaseUrl = "http://localhost:8090",
    [int] $StartupTimeoutSeconds = 120,
    [int] $ShutdownTimeoutSeconds = 90,
    [string] $LogDir = "",
    [string] $AdmUsername = "admin",
    [string] $AdmPassword = $env:CPF_ADM_SMOKE_PASSWORD,
    [switch] $RequireBrowserClick
)

$ErrorActionPreference = "Stop"
    $result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    admBaseUrl = $AdmBaseUrl
    process = [ordered]@{}
    health = [ordered]@{}
    openapi = [ordered]@{}
    admOperationApi = [ordered]@{}
    batchApi = [ordered]@{}
    transactionMetaApi = [ordered]@{}
    staticUi = [ordered]@{}
    browserClick = [ordered]@{}
    cleanup = [ordered]@{}
}
$smokeSequence = 0

if ([string]::IsNullOrWhiteSpace($LogDir)) {
    $LogDir = Join-Path $Root "build/runtime-smoke"
}
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
$stdoutLog = Join-Path $LogDir "adm-bootRun.out.log"
$stderrLog = Join-Path $LogDir "adm-bootRun.err.log"
$resultPath = Join-Path $LogDir "adm-runtime-smoke-result.json"

function Save-SmokeResult {
    $result.finishedAt = (Get-Date).ToString("o")
    $result | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $resultPath -Encoding UTF8
}

function ConvertFrom-Utf8JsonResponse {
    param([object] $Response)
    $content = $Response.Content
    if ($null -ne $Response.RawContentStream) {
        $stream = $Response.RawContentStream
        if ($stream.CanSeek) {
            $stream.Position = 0
        }
        $reader = [System.IO.StreamReader]::new($stream, [System.Text.Encoding]::UTF8, $true, 1024, $true)
        try {
            $content = $reader.ReadToEnd()
        } finally {
            $reader.Dispose()
        }
    }
    if ([string]::IsNullOrWhiteSpace($content)) {
        return $null
    }
    return $content | ConvertFrom-Json
}

function Invoke-SmokeJson {
    param(
        [string] $Method,
        [string] $Uri,
        [hashtable] $Headers = @{},
        [object] $Body = $null,
        [int] $TimeoutSec = 15
    )

    $mergedHeaders = New-SmokeHeaders
    foreach ($key in $Headers.Keys) {
        $mergedHeaders[$key] = $Headers[$key]
    }

    $invokeParams = @{
        Method = $Method
        Uri = $Uri
        TimeoutSec = $TimeoutSec
        Headers = $mergedHeaders
        UseBasicParsing = $true
    }
    if ($null -ne $Body) {
        $invokeParams.ContentType = "application/json;charset=UTF-8"
        $invokeParams.Body = [System.Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Depth 20))
    }
    ConvertFrom-Utf8JsonResponse (Invoke-WebRequest @invokeParams)
}

function New-SmokeHeaders {
    $script:smokeSequence++
    $timestamp = Get-Date -Format "yyyyMMddHHmmssfff"
    $sequence = $script:smokeSequence.ToString("0000000")
    return @{
        "X-Transaction-Id" = "$timestamp" + "ADM" + "smoke01" + $sequence
        "X-Trace-Id" = [guid]::NewGuid().ToString("N")
        "X-Request-Type" = "SMOKE"
        "X-Original-Channel-Code" = "ADM"
        "X-Channel-Code" = "ADM"
        "X-Client-Version" = "runtime-smoke"
        "X-Caller-Service" = "cpf-smoke"
    }
}

function Test-HealthReady {
    try {
        $health = Invoke-SmokeJson -Method Get -Uri "$AdmBaseUrl/adm/api/health" -TimeoutSec 5
        return $health
    } catch {
        return $null
    }
}

function Stop-ProcessTree {
    param([int] $ProcessId)

    $children = @(Get-CimInstance Win32_Process -Filter "ParentProcessId=$ProcessId" -ErrorAction SilentlyContinue)
    foreach ($child in $children) {
        Stop-ProcessTree -ProcessId ([int] $child.ProcessId)
    }
    Stop-Process -Id $ProcessId -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 1
    if (Get-Process -Id $ProcessId -ErrorAction SilentlyContinue) {
        & "$env:WINDIR\System32\taskkill.exe" /PID $ProcessId /T /F | Out-Null
    }
}

function Resolve-AdmPort {
    $uri = [Uri] $AdmBaseUrl
    if ($uri.IsDefaultPort) {
        if ($uri.Scheme -eq "https") {
            return 443
        }
        return 80
    }
    return $uri.Port
}

function Stop-AdmPortOwner {
    $port = Resolve-AdmPort
    $owners = @(Get-NetTCPConnection -LocalPort $port -State Listen -ErrorAction SilentlyContinue |
        Select-Object -ExpandProperty OwningProcess -Unique)
    foreach ($owner in $owners) {
        if ($owner -and $owner -gt 0) {
            Stop-ProcessTree -ProcessId ([int] $owner)
        }
    }
}

function Test-BrowserAutomationAvailable {
    $commands = @("node", "npx", "chrome", "chrome.exe", "msedge", "msedge.exe", "chromium", "chromium.exe")
    foreach ($command in $commands) {
        if (Get-Command $command -ErrorAction SilentlyContinue) {
            return $command
        }
    }
    return $null
}

function Resolve-GradleExecutable {
    $cachedGradle = Get-ChildItem -LiteralPath "$env:USERPROFILE\.gradle\wrapper\dists\gradle-8.11.1-bin" `
        -Recurse `
        -Filter "gradle.bat" `
        -ErrorAction SilentlyContinue |
        Select-Object -First 1
    if ($null -ne $cachedGradle) {
        return $cachedGradle.FullName
    }
    return Join-Path $Root "gradlew.bat"
}

function Resolve-AdmBootJar {
    $libsDir = Join-Path $Root "adm/build/libs"
    if (-not (Test-Path -LiteralPath $libsDir)) {
        return $null
    }
    $jar = Get-ChildItem -LiteralPath $libsDir -File -Filter "*.jar" -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notlike "*plain*" } |
        Sort-Object LastWriteTime -Descending |
        Select-Object -First 1
    if ($null -eq $jar) {
        return $null
    }
    return $jar.FullName
}

$startedProcess = $null
$startedByScript = $false

try {
    $initialHealth = Test-HealthReady
    if ($null -ne $initialHealth) {
        $result.process.status = "REUSED"
        $result.process.message = "Reused an already running ADM application."
    } else {
        if (Test-Path -LiteralPath $stdoutLog) { Remove-Item -LiteralPath $stdoutLog -Force }
        if (Test-Path -LiteralPath $stderrLog) { Remove-Item -LiteralPath $stderrLog -Force }

        $bootJar = Resolve-AdmBootJar
        if ($null -ne $bootJar) {
            $startedProcess = Start-Process `
                -FilePath "java.exe" `
                -ArgumentList @("-jar", $bootJar) `
                -WorkingDirectory $Root `
                -RedirectStandardOutput $stdoutLog `
                -RedirectStandardError $stderrLog `
                -WindowStyle Hidden `
                -PassThru
            $result.process.launchMode = "BOOT_JAR"
            $result.process.bootJar = $bootJar
        } else {
            $gradle = Resolve-GradleExecutable
            if (-not (Test-Path -LiteralPath $gradle)) {
                throw "Gradle executable was not found: $gradle"
            }
            $startedProcess = Start-Process `
                -FilePath $gradle `
                -ArgumentList @("--offline", ":adm:bootRun") `
                -WorkingDirectory $Root `
                -RedirectStandardOutput $stdoutLog `
                -RedirectStandardError $stderrLog `
                -WindowStyle Hidden `
                -PassThru
            $result.process.launchMode = "GRADLE_BOOT_RUN"
            $result.process.gradle = $gradle
        }
        $startedByScript = $true
        $result.process.status = "STARTED"
        $result.process.pid = $startedProcess.Id
        $result.process.stdoutLog = $stdoutLog
        $result.process.stderrLog = $stderrLog
    }

    $deadline = (Get-Date).AddSeconds($StartupTimeoutSeconds)
    $health = $null
    while ((Get-Date) -lt $deadline) {
        $health = Test-HealthReady
        if ($null -ne $health -and $health.status -eq "UP") {
            break
        }
        if ($startedByScript -and $startedProcess.HasExited) {
            throw "ADM application exited before health readiness. Check logs: $stdoutLog, $stderrLog"
        }
        Start-Sleep -Seconds 2
    }

    if ($null -eq $health) {
        throw "ADM health endpoint did not respond before timeout. Check logs: $stdoutLog, $stderrLog"
    }
    if ($health.status -ne "UP") {
        throw "ADM health status is not UP. status=$($health.status)"
    }
    $result.health.status = "PASSED"
    $result.health.response = $health

    & (Join-Path $Root "scripts/smoke-openapi.ps1") -AdmBaseUrl $AdmBaseUrl -SkipMbr -SkipXyz -SkipBizAdm -SkipExs
    $result.openapi.status = "PASSED"
    $result.openapi.path = "$AdmBaseUrl/v3/api-docs"

    if ([string]::IsNullOrWhiteSpace($AdmPassword)) {
        $AdmPassword = "Adm!n12345"
    }
    $login = Invoke-SmokeJson -Method Post -Uri "$AdmBaseUrl/adm/api/auth/login" -Body @{
        operatorId = $AdmUsername
        password = $AdmPassword
    }
    if ([string]::IsNullOrWhiteSpace($login.accessToken)) {
        throw "ADM login response does not contain accessToken."
    }
    $headers = @{ Authorization = "Bearer $($login.accessToken)" }
    $admOperationEndpoints = @(
        "/adm/api/operators",
        "/adm/api/operators/roles",
        "/adm/api/operators/menus",
        "/adm/api/permissions/roles",
        "/adm/api/permissions/menus",
        "/adm/api/permissions/buttons",
        "/adm/api/permissions/menu-matrix",
        "/adm/api/permissions/button-matrix",
        "/adm/api/permissions/api-permissions",
        "/adm/api/permissions/api-matrix",
        "/adm/api/audit-logs?limit=5",
        "/adm/api/cache/summary",
        "/adm/api/messages",
        "/adm/api/codes",
        "/adm/api/configs"
    )
    $checkedAdmOperationEndpoints = New-Object System.Collections.Generic.List[string]
    foreach ($endpoint in $admOperationEndpoints) {
        Invoke-SmokeJson -Method Get -Uri "$AdmBaseUrl$endpoint" -Headers $headers | Out-Null
        $checkedAdmOperationEndpoints.Add($endpoint)
    }
    $result.admOperationApi.status = "PASSED"
    $result.admOperationApi.checkedEndpoints = $checkedAdmOperationEndpoints

    $batchEndpoints = @(
        "/adm/api/batch/jobs",
        "/adm/api/batch/jobs/CPF_EDU_TASKLET_JOB",
        "/adm/api/batch/schedules",
        "/adm/api/batch/instances",
        "/adm/api/batch/executions?limit=20",
        "/adm/api/batch/steps?limit=20",
        "/adm/api/batch/workers",
        "/adm/api/batch/locks",
        "/adm/api/batch/ghost-candidates",
        "/adm/api/batch/operations?limit=20"
    )
    $checkedEndpoints = New-Object System.Collections.Generic.List[string]
    foreach ($endpoint in $batchEndpoints) {
        Invoke-SmokeJson -Method Get -Uri "$AdmBaseUrl$endpoint" -Headers $headers | Out-Null
        $checkedEndpoints.Add($endpoint)
    }
    $result.batchApi.status = "PASSED"
    $result.batchApi.checkedEndpoints = $checkedEndpoints

    $transactionMetaResultPath = Join-Path $LogDir "transaction-meta-runtime-smoke-result.json"
    & (Join-Path $Root "scripts/smoke-transaction-meta-runtime.ps1") `
        -AdmBaseUrl $AdmBaseUrl `
        -AccessToken $login.accessToken `
        -ResultPath $transactionMetaResultPath
    $result.transactionMetaApi.status = "PASSED"
    $result.transactionMetaApi.endpoint = "/adm/api/transactions/scan"
    $result.transactionMetaApi.resultPath = $transactionMetaResultPath

    & (Join-Path $Root "scripts/smoke-adm-ui.ps1") -Root $Root
    $result.staticUi.status = "PASSED"

    $browserCommand = Test-BrowserAutomationAvailable
    if ($null -eq $browserCommand) {
        $result.browserClick.status = "SKIPPED"
        $result.browserClick.reason = "Browser automation runtime was not found in PATH."
        if ($RequireBrowserClick) {
            throw $result.browserClick.reason
        }
    } else {
        $result.browserClick.status = "SKIPPED"
        $result.browserClick.reason = "A browser command was detected, but no Playwright or Selenium click scenario exists in this repository."
        $result.browserClick.detectedCommand = $browserCommand
        if ($RequireBrowserClick) {
            throw $result.browserClick.reason
        }
    }
} catch {
    $result.error = $_.Exception.Message
    Save-SmokeResult
    throw
} finally {
    if ($startedByScript -and $null -ne $startedProcess) {
        Stop-AdmPortOwner
        Stop-ProcessTree -ProcessId $startedProcess.Id
        Wait-Process -Id $startedProcess.Id -Timeout $ShutdownTimeoutSeconds -ErrorAction SilentlyContinue
        $deadline = (Get-Date).AddSeconds($ShutdownTimeoutSeconds)
        while ((Get-Date) -lt $deadline) {
            Start-Sleep -Seconds 1
            $healthAfterStop = Test-HealthReady
            if ($null -eq $healthAfterStop) {
                $result.cleanup.status = "PASSED"
                $result.cleanup.message = "The ADM process started by this script was stopped and the health endpoint no longer responds."
                break
            }
        }
        if (-not $result.cleanup.status) {
            if (-not (Get-Process -Id $startedProcess.Id -ErrorAction SilentlyContinue)) {
                $result.cleanup.status = "PASSED"
                $result.cleanup.message = "The ADM process started by this script no longer exists."
            } else {
                $result.cleanup.status = "WARNING"
                $result.cleanup.message = "ADM health endpoint still responds after stop request. Check whether another process is using the same port."
            }
        }
    } elseif (-not $result.cleanup.status) {
        $result.cleanup.status = "SKIPPED"
        $result.cleanup.message = "The script reused an existing ADM application and did not stop it."
    }
    Save-SmokeResult
}

Write-Host "ADM runtime smoke completed. Result: $resultPath"
