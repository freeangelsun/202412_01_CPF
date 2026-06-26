param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $AdmBaseUrl = "http://localhost:8090",
    [string] $AdmUsername = "admin",
    [string] $AdmPassword = $env:CPF_ADM_SMOKE_PASSWORD,
    [string] $TargetTransactionId = "ADM01TRN0010",
    [int] $StartupTimeoutSeconds = 120,
    [int] $ShutdownTimeoutSeconds = 90,
    [string] $LogDir = ""
)

$ErrorActionPreference = "Stop"
$sequence = 0
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    admBaseUrl = $AdmBaseUrl
    targetTransactionId = $TargetTransactionId
    process = [ordered]@{}
    login = [ordered]@{}
    baseline = [ordered]@{}
    dbLogDisabled = [ordered]@{}
    dbLogEnabled = [ordered]@{}
    cleanup = [ordered]@{}
}

if ([string]::IsNullOrWhiteSpace($LogDir)) {
    $LogDir = Join-Path $Root "build/runtime-smoke"
}
New-Item -ItemType Directory -Force -Path $LogDir | Out-Null
$stdoutLog = Join-Path $LogDir "adm-log-policy-smoke.out.log"
$stderrLog = Join-Path $LogDir "adm-log-policy-smoke.err.log"
$resultPath = Join-Path $LogDir "log-policy-runtime-smoke-result.json"

function Save-Result {
    $result.finishedAt = (Get-Date).ToString("o")
    $result | ConvertTo-Json -Depth 30 | Set-Content -LiteralPath $resultPath -Encoding UTF8
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

function New-SmokeHeaders {
    $script:sequence++
    $timestamp = Get-Date -Format "yyyyMMddHHmmssfff"
    return @{
        "X-Transaction-Id" = "$timestamp" + "ADM" + "lgpol01" + $script:sequence.ToString("0000000")
        "X-Trace-Id" = [guid]::NewGuid().ToString("N")
        "X-Request-Type" = "SMOKE"
        "X-Original-Channel-Code" = "ADM"
        "X-Channel-Code" = "ADM"
        "X-Client-Version" = "runtime-smoke"
        "X-Caller-Service" = "cpf-smoke"
    }
}

function Invoke-SmokeJson {
    param(
        [string] $Method,
        [string] $Uri,
        [hashtable] $Headers = @{},
        [object] $Body = $null,
        [int] $TimeoutSec = 20
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

function Test-HealthReady {
    try {
        return Invoke-SmokeJson -Method Get -Uri "$AdmBaseUrl/adm/api/health" -TimeoutSec 5
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
        if ($uri.Scheme -eq "https") { return 443 }
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

function Get-Value {
    param(
        [object] $Object,
        [string[]] $Names
    )
    foreach ($name in $Names) {
        if ($null -ne $Object.$name) {
            return $Object.$name
        }
    }
    return $null
}

function Get-LatestLogIndex {
    param([hashtable] $Headers)
    $logs = Invoke-SmokeJson `
        -Method Get `
        -Uri "$AdmBaseUrl/adm/api/logs?businessTransactionId=$TargetTransactionId&limit=1" `
        -Headers $Headers
    if ($logs.available -eq $false) {
        throw "ADM log API is not available. message=$($logs.message)"
    }
    $items = @($logs.items)
    if ($items.Count -eq 0) {
        return 0L
    }
    $value = Get-Value -Object ($items | Select-Object -First 1) -Names @("LOG_IDX", "logIdx", "log_idx")
    if ($null -eq $value) {
        return 0L
    }
    return [long] $value
}

function Invoke-TargetTransaction {
    param([hashtable] $Headers)
    Invoke-SmokeJson `
        -Method Get `
        -Uri "$AdmBaseUrl/adm/api/transactions?activeYn=Y&limit=1" `
        -Headers $Headers | Out-Null
}

function Refresh-TargetPolicy {
    param([hashtable] $Headers)
    $encodedReason = [uri]::EscapeDataString("runtime-smoke-policy-refresh")
    Invoke-SmokeJson `
        -Method Post `
        -Uri "$AdmBaseUrl/adm/api/log-policies/cache/refresh?targetType=ONLINE_TRANSACTION&targetId=$TargetTransactionId&reason=$encodedReason" `
        -Headers $Headers | Out-Null
}

function Create-DbLogOffOverride {
    param([hashtable] $Headers)
    $start = (Get-Date).AddMinutes(-5).ToString("yyyy-MM-ddTHH:mm:ss")
    $end = (Get-Date).AddMinutes(15).ToString("yyyy-MM-ddTHH:mm:ss")
    Invoke-SmokeJson `
        -Method Post `
        -Uri "$AdmBaseUrl/adm/api/log-policies/overrides" `
        -Headers $Headers `
        -Body @{
            targetType = "ONLINE_TRANSACTION"
            targetId = $TargetTransactionId
            logLevel = "INFO"
            dbLogEnabledYn = "N"
            fileLogEnabledYn = "Y"
            requestBodyLogYn = "N"
            responseBodyLogYn = "N"
            errorStackLogYn = "N"
            effectiveStartAt = $start
            effectiveEndAt = $end
            approvedBy = "runtime-smoke"
            requestUser = "runtime-smoke"
            reason = "runtime-smoke-db-log-off"
        }
}

function Disable-Override {
    param(
        [hashtable] $Headers,
        [long] $OverrideId
    )
    $encodedReason = [uri]::EscapeDataString("runtime-smoke-cleanup")
    Invoke-SmokeJson `
        -Method Patch `
        -Uri "$AdmBaseUrl/adm/api/log-policies/overrides/$OverrideId/disable?reason=$encodedReason" `
        -Headers $Headers | Out-Null
}

$startedProcess = $null
$startedByScript = $false
$overrideId = $null

try {
    $initialHealth = Test-HealthReady
    if ($null -ne $initialHealth) {
        $result.process.status = "REUSED"
        $result.process.message = "Reused an already running ADM application."
    } else {
        if (Test-Path -LiteralPath $stdoutLog) { Remove-Item -LiteralPath $stdoutLog -Force }
        if (Test-Path -LiteralPath $stderrLog) { Remove-Item -LiteralPath $stderrLog -Force }
        $bootJar = Resolve-AdmBootJar
        if ($null -eq $bootJar) {
            throw "ADM boot jar was not found. Run :adm:bootJar first."
        }
        $startedProcess = Start-Process `
            -FilePath "java.exe" `
            -ArgumentList @("-jar", $bootJar) `
            -WorkingDirectory $Root `
            -RedirectStandardOutput $stdoutLog `
            -RedirectStandardError $stderrLog `
            -WindowStyle Hidden `
            -PassThru
        $startedByScript = $true
        $result.process.status = "STARTED"
        $result.process.pid = $startedProcess.Id
        $result.process.bootJar = $bootJar
        $result.process.stdoutLog = $stdoutLog
        $result.process.stderrLog = $stderrLog
    }

    $deadline = (Get-Date).AddSeconds($StartupTimeoutSeconds)
    $health = $null
    while ((Get-Date) -lt $deadline) {
        $health = Test-HealthReady
        if ($null -ne $health -and $health.status -eq "UP") { break }
        if ($startedByScript -and $startedProcess.HasExited) {
            throw "ADM application exited before health readiness. Check logs: $stdoutLog, $stderrLog"
        }
        Start-Sleep -Seconds 2
    }
    if ($null -eq $health -or $health.status -ne "UP") {
        throw "ADM health endpoint did not become UP before timeout."
    }

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
    $result.login.status = "PASSED"
    $headers = @{ Authorization = "Bearer $($login.accessToken)" }

    Refresh-TargetPolicy -Headers $headers
    $baselineLogIdx = Get-LatestLogIndex -Headers $headers
    $result.baseline.latestLogIdx = $baselineLogIdx

    $override = Create-DbLogOffOverride -Headers $headers
    $overrideIdValue = Get-Value -Object $override -Names @("override_id", "overrideId", "OVERRIDE_ID")
    if ($null -eq $overrideIdValue) {
        throw "Log policy override response does not contain override id."
    }
    $overrideId = [long] $overrideIdValue
    Refresh-TargetPolicy -Headers $headers
    Invoke-TargetTransaction -Headers $headers
    Start-Sleep -Seconds 3
    $afterDisabledLogIdx = Get-LatestLogIndex -Headers $headers
    if ($afterDisabledLogIdx -ne $baselineLogIdx) {
        throw "dbLogEnabled=N did not suppress DB log. before=$baselineLogIdx after=$afterDisabledLogIdx"
    }
    $result.dbLogDisabled.status = "PASSED"
    $result.dbLogDisabled.overrideId = $overrideId
    $result.dbLogDisabled.beforeLogIdx = $baselineLogIdx
    $result.dbLogDisabled.afterLogIdx = $afterDisabledLogIdx

    Disable-Override -Headers $headers -OverrideId $overrideId
    $overrideId = $null
    Refresh-TargetPolicy -Headers $headers
    Invoke-TargetTransaction -Headers $headers
    $enabledDeadline = (Get-Date).AddSeconds(15)
    $afterEnabledLogIdx = $afterDisabledLogIdx
    while ((Get-Date) -lt $enabledDeadline) {
        Start-Sleep -Seconds 2
        $afterEnabledLogIdx = Get-LatestLogIndex -Headers $headers
        if ($afterEnabledLogIdx -gt $afterDisabledLogIdx) {
            break
        }
    }
    if ($afterEnabledLogIdx -le $afterDisabledLogIdx) {
        throw "dbLogEnabled=Y/default did not create DB log. before=$afterDisabledLogIdx after=$afterEnabledLogIdx"
    }
    $result.dbLogEnabled.status = "PASSED"
    $result.dbLogEnabled.beforeLogIdx = $afterDisabledLogIdx
    $result.dbLogEnabled.afterLogIdx = $afterEnabledLogIdx
} catch {
    $result.error = $_.Exception.Message
    Save-Result
    throw
} finally {
    if ($null -ne $overrideId) {
        try {
            $headers = @{ Authorization = "Bearer $($login.accessToken)" }
            Disable-Override -Headers $headers -OverrideId $overrideId
            Refresh-TargetPolicy -Headers $headers
            $result.cleanup.override = "DISABLED"
        } catch {
            $result.cleanup.override = "FAILED: $($_.Exception.Message)"
        }
    }
    if ($startedByScript -and $null -ne $startedProcess) {
        Stop-AdmPortOwner
        Stop-ProcessTree -ProcessId $startedProcess.Id
        Wait-Process -Id $startedProcess.Id -Timeout $ShutdownTimeoutSeconds -ErrorAction SilentlyContinue
        $result.cleanup.process = "STOPPED"
    } elseif (-not $result.cleanup.process) {
        $result.cleanup.process = "SKIPPED_REUSED"
    }
    Save-Result
}

Write-Host "Log policy runtime smoke completed. Result: $resultPath"
