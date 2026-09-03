param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $AdmBaseUrl = "http://localhost:8090",
    [string] $AdmUsername = "admin",
    # 단독 ADM 실행은 ADM, 1-WAS 통합 Runtime 은 자신을 LOCAL 로 선언한다. 대상 Runtime 의
    # System Code 를 호출자가 알려준다(System6 수신자 일치 계약).
    [string] $SystemCode = "ADM",
    [string] $AdmPassword = $env:CPF_ADM_SMOKE_PASSWORD,
    [string] $TargetTransactionId = "ADM01TRN0010",
    [int] $StartupTimeoutSeconds = 120,
    [int] $ShutdownTimeoutSeconds = 90,
    [string] $LogDir = "",
    [switch] $CheckRequestBodyPolicy,
    [switch] $CheckResponseBodyPolicy,
    [switch] $CheckErrorStackPolicy,
    [switch] $CheckOverrideFallback
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"

# Child process가 새 Windows process로 분리되어도 UTF-8 계약을 잃지 않도록 고정합니다.
$CpfUtf8ChildJavaOptions = '-Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8 -Dstderr.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8'
if ([string]::IsNullOrWhiteSpace($env:JAVA_TOOL_OPTIONS)) {
    $env:JAVA_TOOL_OPTIONS = $CpfUtf8ChildJavaOptions
} elseif ($env:JAVA_TOOL_OPTIONS -notmatch '(?:^|\s)-Dfile\.encoding=UTF-8(?:\s|$)') {
    $env:JAVA_TOOL_OPTIONS = ($env:JAVA_TOOL_OPTIONS.Trim() + ' ' + $CpfUtf8ChildJavaOptions)
}
$env:PYTHONUTF8 = '1'
$env:PYTHONIOENCODING = 'utf-8'
$env:PGCLIENTENCODING = 'UTF8'
$env:NLS_LANG = '.AL32UTF8'
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
    requestBodyPolicy = [ordered]@{}
    responseBodyPolicy = [ordered]@{}
    errorStackPolicy = [ordered]@{}
    overrideFallback = [ordered]@{}
    admObservability = [ordered]@{}
    cleanup = [ordered]@{}
}
$runAllPolicyChecks = -not ($CheckRequestBodyPolicy -or $CheckResponseBodyPolicy -or $CheckErrorStackPolicy -or $CheckOverrideFallback)

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

function Get-CpfIssuerCode([string] $Code) {
    # CPF 정본 거래ID 의 issuer 는 **3자리**다(CpfTransactionIds). Runtime System Code 가 3자리보다
    # 길면 CpfSystemCodes.normalize 가 앞 3자리를 issuer 로 쓴다(LOCAL -> LOC).
    # 그래서 X-System-Code/X-Target-System-Code(수신자 일치)와 X-Original-System-Code/거래ID issuer
    # 는 서로 다른 값일 수 있다. 둘을 같은 값으로 보내면 1-WAS 에서 반드시 거절된다.
    if ([string]::IsNullOrWhiteSpace($Code)) { return 'CPF' }
    $trimmed = $Code.Trim().ToUpperInvariant()
    if ($trimmed.Length -le 3) { return $trimmed }
    return $trimmed.Substring(0, 3)
}

function New-SmokeHeaders {
    $script:sequence++
    $timestamp = Get-Date -Format "yyyyMMddHHmmssfff"
    return @{
        # 거래ID 의 issuer(3자리)는 X-Original-System-Code 와 반드시 같아야 한다
        # (CpfHttpInboundContextAdapter, ORIGINAL_SYSTEM_CODE_MISMATCH). "ADM" 을 고정으로 박으면
        # 1-WAS(LOCAL -> LOC)에서 항상 ECPF900002 400 이 되어 health 조차 통과하지 못한다.
        "X-Transaction-Id" = "$timestamp" + (Get-CpfIssuerCode $SystemCode) + "lgpol01" + $script:sequence.ToString("0000000")
        "X-Trace-Id" = [guid]::NewGuid().ToString("N")
        "X-Request-Type" = "SMOKE"
        "X-Client-Version" = "runtime-smoke"
        "X-Caller-Service" = "cpf-smoke"
        # System6 계약상 X-System-Code / X-Target-System-Code 는 수신 Runtime 의 System Code 와
        # 같아야 한다(CpfHttpInboundContextAdapter.validateReceiverSystem). 이 헤더가 없으면
        # /adm/api/health 조차 거절되어 "이미 떠 있는 ADM" 을 인식하지 못하고, 스스로 ADM 을
        # 기동하려다 "ADM boot jar was not found" 로 실패한다.
        "X-Original-System-Code" = (Get-CpfIssuerCode $SystemCode)
        "X-System-Code" = $SystemCode
        "X-Caller-System-Code" = $SystemCode
        "X-Target-System-Code" = $SystemCode
        # X-Target-Operation-Id 는 External CPF protocol 호출의 필수 Header 다
        # (CpfHttpInboundContextAdapter.requireExternal / CpfHttpHeaderCatalog.required).
        # 없으면 GET /adm/api/health 가 ECPF900002 400 으로 거절되어 "이미 떠 있는 1-WAS" 를
        # 인식하지 못하고 ADM boot jar 를 다시 찾다가 실패한다. 호출별로 값을 넘길 수 있으며
        # 기본값은 이 검증기가 가장 먼저 호출하는 readiness 거래다.
        "X-Target-Operation-Id" = "getAdmReadiness"
    }
}

$script:admWebSession = $null
$script:admCsrfToken = $null

function Initialize-AdmCsrf {
    # ADM BFF Security Chain 은 /adm/** 에 CSRF 를 적용한다
    # (CookieCsrfTokenRepository + CpfCsrfCookieExposureFilter).
    # 공개 GET 으로 XSRF-TOKEN cookie 를 먼저 받지 않으면 로그인 POST 가 403 으로 거절된다.
    # 인증 자체도 HttpOnly Session Cookie 로 이어지므로 이후 호출은 같은 WebSession 을 쓴다.
    param([string] $BaseUrl)
    $probe = Invoke-WebRequest -Method Get -Uri "$BaseUrl/adm/api/health" -Headers (New-SmokeHeaders) `
        -TimeoutSec 10 -UseBasicParsing -SessionVariable session
    $null = $probe
    $script:admWebSession = $session
    $cookie = $session.Cookies.GetCookies($BaseUrl) | Where-Object { $_.Name -eq 'XSRF-TOKEN' } | Select-Object -First 1
    if ($null -eq $cookie) { throw 'ADM CSRF cookie(XSRF-TOKEN)가 발급되지 않았습니다.' }
    $script:admCsrfToken = [string] $cookie.Value
    return $script:admCsrfToken
}

function Get-AdmOrigin {
    param([string] $BaseUrl)
    $uri = [Uri] $BaseUrl
    return "$($uri.Scheme)://$($uri.Authority)"
}

function Add-AdmSessionParams {
    param([hashtable] $InvokeParams, [hashtable] $Headers)
    if ($null -ne $script:admWebSession) { $InvokeParams.WebSession = $script:admWebSession }
    if (-not [string]::IsNullOrWhiteSpace($script:admCsrfToken)) { $Headers['X-XSRF-TOKEN'] = $script:admCsrfToken }
    # CpfTrustedOriginFilter 는 /adm/ 의 상태 변경 요청에 Origin/Referer 를 요구하고, 없으면
    # "Untrusted request origin" 으로 403 을 낸다. Browser 와 같은 same-origin 값을 보낸다.
    $Headers['Origin'] = Get-AdmOrigin -BaseUrl $AdmBaseUrl
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
    Add-AdmSessionParams $invokeParams $mergedHeaders
    if ($null -ne $Body) {
        $invokeParams.ContentType = "application/json;charset=UTF-8"
        $invokeParams.Body = [System.Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Depth 20))
    }
    ConvertFrom-Utf8JsonResponse (Invoke-WebRequest @invokeParams)
}

function Invoke-SmokeJsonAllowHttpError {
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
    Add-AdmSessionParams $invokeParams $mergedHeaders
    if ($null -ne $Body) {
        $invokeParams.ContentType = "application/json;charset=UTF-8"
        $invokeParams.Body = [System.Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Depth 20))
    }
    try {
        $response = Invoke-WebRequest @invokeParams
        return [ordered]@{
            statusCode = [int] $response.StatusCode
            body = ConvertFrom-Utf8JsonResponse $response
        }
    } catch {
        $webResponse = $_.Exception.Response
        if ($null -eq $webResponse) {
            throw
        }
        $stream = $webResponse.GetResponseStream()
        $content = ""
        if ($null -ne $stream) {
            $reader = [System.IO.StreamReader]::new($stream, [System.Text.Encoding]::UTF8, $true)
            try {
                $content = $reader.ReadToEnd()
            } finally {
                $reader.Dispose()
                $stream.Dispose()
            }
        }
        $body = $null
        if (-not [string]::IsNullOrWhiteSpace($content)) {
            try {
                $body = $content | ConvertFrom-Json
            } catch {
                $body = $content
            }
        }
        return [ordered]@{
            statusCode = [int] $webResponse.StatusCode
            body = $body
        }
    }
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
    $libsDir = Join-Path $Root "cpf-admin/build/libs"
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

function Get-LatestLogItem {
    param(
        [hashtable] $Headers,
        [string] $BusinessTransactionId,
        [string] $LogType = ""
    )
    $query = "businessTransactionId=$BusinessTransactionId&limit=1"
    if (-not [string]::IsNullOrWhiteSpace($LogType)) {
        $query = "$query&logType=$LogType"
    }
    $logs = Invoke-SmokeJson `
        -Method Get `
        -Uri "$AdmBaseUrl/adm/api/logs?$query" `
        -Headers $Headers
    if ($logs.available -eq $false) {
        throw "ADM log API is not available. message=$($logs.message)"
    }
    $items = @($logs.items)
    if ($items.Count -eq 0) {
        return $null
    }
    return $items | Select-Object -First 1
}

function Wait-NewLogItem {
    param(
        [hashtable] $Headers,
        [string] $BusinessTransactionId,
        [long] $AfterLogIdx,
        [string] $LogType = "",
        [int] $TimeoutSeconds = 20
    )
    $deadline = (Get-Date).AddSeconds($TimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        Start-Sleep -Seconds 2
        $item = Get-LatestLogItem -Headers $Headers -BusinessTransactionId $BusinessTransactionId -LogType $LogType
        if ($null -ne $item) {
            $value = Get-Value -Object $item -Names @("LOG_IDX", "logIdx", "log_idx")
            if ($null -ne $value -and [long] $value -gt $AfterLogIdx) {
                return $item
            }
        }
    }
    throw "Timed out waiting for a new transaction log. businessTransactionId=$BusinessTransactionId afterLogIdx=$AfterLogIdx logType=$LogType"
}

function Get-LogDetail {
    param(
        [hashtable] $Headers,
        [long] $LogIdx
    )
    $detail = Invoke-SmokeJson -Method Get -Uri "$AdmBaseUrl/adm/api/logs/$LogIdx" -Headers $Headers
    if ($detail.available -eq $false) {
        throw "ADM log detail API is not available. logIdx=$LogIdx message=$($detail.message)"
    }
    return $detail.item
}

function Test-DetailKeyExists {
    param(
        [object] $LogDetail,
        [string] $DetailKey
    )
    $details = @($LogDetail.details)
    foreach ($detail in $details) {
        $key = Get-Value -Object $detail -Names @("DETAIL_KEY", "detailKey", "detail_key")
        if ($DetailKey.Equals([string] $key, [System.StringComparison]::OrdinalIgnoreCase)) {
            return $true
        }
    }
    return $false
}

function Get-DetailValue {
    param(
        [object] $LogDetail,
        [string] $DetailKey
    )
    $details = @($LogDetail.details)
    foreach ($detail in $details) {
        $key = Get-Value -Object $detail -Names @("DETAIL_KEY", "detailKey", "detail_key")
        if ($DetailKey.Equals([string] $key, [System.StringComparison]::OrdinalIgnoreCase)) {
            return Get-Value -Object $detail -Names @("DETAIL_VALUE", "detailValue", "detail_value")
        }
    }
    return $null
}

function Assert-DetailKeyMissing {
    param(
        [object] $LogDetail,
        [string] $DetailKey
    )
    if (Test-DetailKeyExists -LogDetail $LogDetail -DetailKey $DetailKey) {
        throw "$DetailKey was saved even though policy disabled it."
    }
}

function Assert-DetailValueEquals {
    param(
        [object] $LogDetail,
        [string] $DetailKey,
        [string] $ExpectedValue
    )
    $value = Get-DetailValue -LogDetail $LogDetail -DetailKey $DetailKey
    if ($null -eq $value -or -not $ExpectedValue.Equals([string] $value, [System.StringComparison]::OrdinalIgnoreCase)) {
        throw "Unexpected detail value. key=$DetailKey expected=$ExpectedValue actual=$value"
    }
}

function Assert-BlankSummaryValue {
    param(
        [object] $LogDetail,
        [string[]] $Names,
        [string] $Label
    )
    $value = Get-Value -Object $LogDetail.summary -Names $Names
    if ($null -ne $value -and -not [string]::IsNullOrWhiteSpace([string] $value)) {
        throw "$Label was saved even though policy disabled it."
    }
}

function Refresh-PolicyDecision {
    param(
        [hashtable] $Headers,
        [string] $TargetType,
        [string] $TargetId,
        [string] $Reason = "runtime-smoke-policy-refresh"
    )
    $encodedReason = [uri]::EscapeDataString($Reason)
    Invoke-SmokeJson `
        -Method Post `
        -Uri "$AdmBaseUrl/adm/api/log-policies/cache/refresh?targetType=$TargetType&targetId=$TargetId&reason=$encodedReason" `
        -Headers $Headers
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
    Refresh-PolicyDecision -Headers $Headers -TargetType "ONLINE_TRANSACTION" -TargetId $TargetTransactionId | Out-Null
}

function Get-LogIndexFromItem {
    param([object] $Item)
    if ($null -eq $Item) {
        return 0L
    }
    $value = Get-Value -Object $Item -Names @("LOG_IDX", "logIdx", "log_idx")
    if ($null -eq $value) {
        return 0L
    }
    return [long] $value
}

function Get-OverrideIdFromResponse {
    param([object] $Response)
    $overrideIdValue = Get-Value -Object $Response -Names @("override_id", "overrideId", "OVERRIDE_ID")
    if ($null -eq $overrideIdValue) {
        throw "Log policy override response does not contain override id."
    }
    return [long] $overrideIdValue
}

function Create-LogPolicyOverride {
    param(
        [hashtable] $Headers,
        [string] $TargetId,
        [string] $Reason,
        [string] $DbLogEnabledYn = "Y",
        [string] $RequestBodyLogYn = "N",
        [string] $ResponseBodyLogYn = "N",
        [string] $ErrorStackLogYn = "Y",
        [int] $StartOffsetMinutes = -5,
        [int] $EndOffsetMinutes = 15
    )
    $start = (Get-Date).AddMinutes($StartOffsetMinutes).ToString("yyyy-MM-ddTHH:mm:ss")
    $end = (Get-Date).AddMinutes($EndOffsetMinutes).ToString("yyyy-MM-ddTHH:mm:ss")
    Invoke-SmokeJson `
        -Method Post `
        -Uri "$AdmBaseUrl/adm/api/log-policies/overrides" `
        -Headers $Headers `
        -Body @{
            targetType = "ONLINE_TRANSACTION"
            targetId = $TargetId
            logLevel = "INFO"
            dbLogEnabledYn = $DbLogEnabledYn
            fileLogEnabledYn = "Y"
            requestBodyLogYn = $RequestBodyLogYn
            responseBodyLogYn = $ResponseBodyLogYn
            errorStackLogYn = $ErrorStackLogYn
            effectiveStartAt = $start
            effectiveEndAt = $end
            approvedBy = "runtime-smoke"
            requestUser = "runtime-smoke"
            reason = $Reason
        }
}

function Create-DbLogOffOverride {
    param([hashtable] $Headers)
    Create-LogPolicyOverride `
        -Headers $Headers `
        -TargetId $TargetTransactionId `
        -Reason "runtime-smoke-db-log-off" `
        -DbLogEnabledYn "N" `
        -RequestBodyLogYn "N" `
        -ResponseBodyLogYn "N" `
        -ErrorStackLogYn "N"
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
$cleanupOverrideIds = New-Object System.Collections.Generic.List[long]

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
            throw "ADM boot jar was not found. Run :cpf-admin:bootJar first."
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
        throw "CPF_ADM_SMOKE_PASSWORD 환경변수 또는 -AdmPassword 인수가 필요합니다."
    }
    # 로그인 POST 전에 XSRF-TOKEN 을 선취한다. 없으면 permitAll 경로여도 CSRF 로 403 이 된다.
    Initialize-AdmCsrf -BaseUrl $AdmBaseUrl | Out-Null
    $login = Invoke-SmokeJson -Method Post -Uri "$AdmBaseUrl/adm/api/auth/login" -Body @{
        operatorId = $AdmUsername
        password = $AdmPassword
    }
    # BFF 계약상 내부 인증 토큰은 응답 Body 에 노출되지 않는다
    # (CpfBffCredentialResponseAdvice 가 accessToken/refreshToken 을 제거하고 Vault 에 저장한다).
    # 따라서 로그인 성공은 "Body 에 토큰이 있는가"가 아니라 "Session Cookie 가 발급됐는가"로 본다.
    if ($null -ne $login -and -not [string]::IsNullOrWhiteSpace([string]$login.accessToken)) {
        throw "ADM login response leaked an internal credential in the body."
    }
    $sessionCookie = $script:admWebSession.Cookies.GetCookies($AdmBaseUrl) |
        Where-Object { $_.Name -ne 'XSRF-TOKEN' } | Select-Object -First 1
    if ($null -eq $sessionCookie) { throw "ADM login did not establish a BFF session cookie." }
    $result.login.status = "PASSED"

    # ADM 최초 Bootstrap 계정은 비밀번호 변경이 강제된다.
    # 변경 전에는 self-service 경로 외 모든 ADM API 가
    # 403 "비밀번호를 먼저 변경해야 합니다."(AdmApiAuthFilter)로 막힌다.
    # 검증 대상 API 를 호출하기 전에 정본 self-service 경로로 1회 변경한다.
    $currentSession = Invoke-SmokeJson -Method Get -Uri "$AdmBaseUrl/adm/api/auth/me" `
        -Headers @{ "X-Target-Operation-Id" = "admAuthMe" }
    if ([bool]$currentSession.passwordChangeRequired) {
        $rotatedPassword = "$AdmPassword" + 'R7!'
        Invoke-SmokeJson -Method Post -Uri "$AdmBaseUrl/adm/api/operators/$AdmUsername/password" `
            -Headers @{ "X-Target-Operation-Id" = "admOperatorChangePassword" } `
            -Body @{
                currentPassword = $AdmPassword
                newPassword = $rotatedPassword
                newPasswordConfirm = $rotatedPassword
                reason = "runtime-smoke mandatory password rotation"
            } | Out-Null
        $script:AdmPassword = $rotatedPassword
        $result.login.passwordRotated = $true
    }
    # BFF 계약상 Browser 채널은 Authorization Header 를 보내면 안 된다.
    # CpfBffSessionBridgeFilter 가 Authorization 이 있으면
    # 400 "Browser Authorization header is prohibited" 로 잘라낸다.
    # 인증은 로그인으로 발급된 HttpOnly Session Cookie(= $script:admWebSession)가 운반한다.
    $headers = @{}

    Refresh-TargetPolicy -Headers $headers
    $baselineLogIdx = Get-LatestLogIndex -Headers $headers
    $result.baseline.latestLogIdx = $baselineLogIdx

    $override = Create-DbLogOffOverride -Headers $headers
    $overrideId = Get-OverrideIdFromResponse -Response $override
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

    $policyApiTransactionId = "ADM03LGP0014"
    if ($runAllPolicyChecks -or $CheckRequestBodyPolicy -or $CheckResponseBodyPolicy -or $CheckErrorStackPolicy) {
        $controlOverride = Create-LogPolicyOverride `
            -Headers $headers `
            -TargetId $policyApiTransactionId `
            -Reason "runtime-smoke-detail-control" `
            -DbLogEnabledYn "Y" `
            -RequestBodyLogYn "N" `
            -ResponseBodyLogYn "N" `
            -ErrorStackLogYn "N"
        $controlOverrideId = Get-OverrideIdFromResponse -Response $controlOverride
        $cleanupOverrideIds.Add($controlOverrideId) | Out-Null

        $controlDecision = Refresh-PolicyDecision -Headers $headers -TargetType "ONLINE_TRANSACTION" -TargetId $policyApiTransactionId -Reason "runtime-smoke-detail-control-refresh"
        if ($controlDecision.resolvedSource -ne "ADM_OVERRIDE" `
                -or $controlDecision.requestBodySaveYn -ne "N" `
                -or $controlDecision.responseBodySaveYn -ne "N" `
                -or $controlDecision.errorStackSaveYn -ne "N" `
                -or $controlDecision.dbLogEnabledYn -ne "Y") {
            throw "Detail control override was not resolved as expected. decision=$($controlDecision | ConvertTo-Json -Compress)"
        }

        if ($runAllPolicyChecks -or $CheckRequestBodyPolicy -or $CheckResponseBodyPolicy) {
            $bodyBaseline = Get-LogIndexFromItem (Get-LatestLogItem -Headers $headers -BusinessTransactionId $policyApiTransactionId)
            $bodyProbeTargetId = "CPF_SMOKE_DETAIL_TARGET_$((Get-Date).ToString('HHmmssfff'))"
            $bodyProbeOverride = Create-LogPolicyOverride `
                -Headers $headers `
                -TargetId $bodyProbeTargetId `
                -Reason "runtime-smoke-detail-probe" `
                -DbLogEnabledYn "Y" `
                -RequestBodyLogYn "N" `
                -ResponseBodyLogYn "N" `
                -ErrorStackLogYn "Y"
            $bodyProbeOverrideId = Get-OverrideIdFromResponse -Response $bodyProbeOverride
            $cleanupOverrideIds.Add($bodyProbeOverrideId) | Out-Null

            $bodyLogItem = Wait-NewLogItem -Headers $headers -BusinessTransactionId $policyApiTransactionId -AfterLogIdx $bodyBaseline -LogType "SUCCESS"
            $bodyLogIdx = Get-LogIndexFromItem $bodyLogItem
            $bodyLogDetail = Get-LogDetail -Headers $headers -LogIdx $bodyLogIdx
            Assert-DetailValueEquals -LogDetail $bodyLogDetail -DetailKey "logPolicy.resolvedSource" -ExpectedValue "ADM_OVERRIDE"
            Assert-DetailValueEquals -LogDetail $bodyLogDetail -DetailKey "logPolicy.requestBodySaveYn" -ExpectedValue "N"
            Assert-DetailValueEquals -LogDetail $bodyLogDetail -DetailKey "logPolicy.responseBodySaveYn" -ExpectedValue "N"
            Assert-BlankSummaryValue -LogDetail $bodyLogDetail -Names @("REQUEST_BODY", "requestBody", "request_body") -Label "request body"
            Assert-BlankSummaryValue -LogDetail $bodyLogDetail -Names @("RESPONSE", "response") -Label "response body"
            Assert-DetailKeyMissing -LogDetail $bodyLogDetail -DetailKey "requestBody"
            Assert-DetailKeyMissing -LogDetail $bodyLogDetail -DetailKey "response"

            $result.requestBodyPolicy.status = "PASSED"
            $result.requestBodyPolicy.logIdx = $bodyLogIdx
            $result.requestBodyPolicy.overrideId = $controlOverrideId
            $result.requestBodyPolicy.evidence = "summary.REQUEST_BODY and detail requestBody are empty"
            $result.responseBodyPolicy.status = "PASSED"
            $result.responseBodyPolicy.logIdx = $bodyLogIdx
            $result.responseBodyPolicy.overrideId = $controlOverrideId
            $result.responseBodyPolicy.evidence = "summary.RESPONSE and detail response are empty"
        }

        if ($runAllPolicyChecks -or $CheckErrorStackPolicy) {
            $errorBaseline = Get-LogIndexFromItem (Get-LatestLogItem -Headers $headers -BusinessTransactionId $policyApiTransactionId)
            $errorResponse = Invoke-SmokeJsonAllowHttpError `
                -Method Post `
                -Uri "$AdmBaseUrl/adm/api/log-policies/overrides" `
                -Headers $headers `
                -Body @{
                    targetType = "ONLINE_TRANSACTION"
                    targetId = "CPF_SMOKE_INVALID_TARGET"
                    logLevel = "INFO"
                    dbLogEnabledYn = "Y"
                    fileLogEnabledYn = "Y"
                    requestBodyLogYn = "N"
                    responseBodyLogYn = "N"
                    errorStackLogYn = "N"
                    effectiveStartAt = (Get-Date).AddMinutes(-5).ToString("yyyy-MM-ddTHH:mm:ss")
                    effectiveEndAt = $null
                    approvedBy = "runtime-smoke"
                    requestUser = "runtime-smoke"
                    reason = "runtime-smoke-invalid-error-stack"
                }
            if ($errorResponse.statusCode -lt 400) {
                throw "Invalid override request did not return an HTTP error. statusCode=$($errorResponse.statusCode)"
            }

            $errorLogItem = Wait-NewLogItem -Headers $headers -BusinessTransactionId $policyApiTransactionId -AfterLogIdx $errorBaseline -LogType "FAILURE"
            $errorLogIdx = Get-LogIndexFromItem $errorLogItem
            $errorLogDetail = Get-LogDetail -Headers $headers -LogIdx $errorLogIdx
            Assert-DetailValueEquals -LogDetail $errorLogDetail -DetailKey "logPolicy.resolvedSource" -ExpectedValue "ADM_OVERRIDE"
            Assert-DetailValueEquals -LogDetail $errorLogDetail -DetailKey "logPolicy.errorStackSaveYn" -ExpectedValue "N"
            Assert-BlankSummaryValue -LogDetail $errorLogDetail -Names @("INTERNAL_MESSAGE", "internalMessage", "internal_message") -Label "internal error stack"
            Assert-DetailKeyMissing -LogDetail $errorLogDetail -DetailKey "error.internalMessage"

            $result.errorStackPolicy.status = "PASSED"
            $result.errorStackPolicy.logIdx = $errorLogIdx
            $result.errorStackPolicy.httpStatus = $errorResponse.statusCode
            $result.errorStackPolicy.overrideId = $controlOverrideId
            $result.errorStackPolicy.evidence = "summary.INTERNAL_MESSAGE and detail error.internalMessage are empty"
        }
    }

    if ($runAllPolicyChecks -or $CheckOverrideFallback) {
        $fallbackTargetId = "CPF_SMOKE_FALLBACK_$((Get-Date).ToString('HHmmssfff'))"
        $activeOverride = Create-LogPolicyOverride `
            -Headers $headers `
            -TargetId $fallbackTargetId `
            -Reason "runtime-smoke-active-fallback" `
            -DbLogEnabledYn "N" `
            -RequestBodyLogYn "Y" `
            -ResponseBodyLogYn "Y" `
            -ErrorStackLogYn "N"
        $activeOverrideId = Get-OverrideIdFromResponse -Response $activeOverride
        $cleanupOverrideIds.Add($activeOverrideId) | Out-Null
        $activeDecision = Refresh-PolicyDecision -Headers $headers -TargetType "ONLINE_TRANSACTION" -TargetId $fallbackTargetId -Reason "runtime-smoke-active-fallback-refresh"
        if ($activeDecision.resolvedSource -ne "ADM_OVERRIDE" -or $activeDecision.overrideId -eq $null) {
            throw "Active override was not selected. decision=$($activeDecision | ConvertTo-Json -Compress)"
        }

        $futureOverride = Create-LogPolicyOverride `
            -Headers $headers `
            -TargetId $fallbackTargetId `
            -Reason "runtime-smoke-future-fallback" `
            -DbLogEnabledYn "N" `
            -RequestBodyLogYn "Y" `
            -ResponseBodyLogYn "Y" `
            -ErrorStackLogYn "N" `
            -StartOffsetMinutes 30 `
            -EndOffsetMinutes 60
        $futureOverrideId = Get-OverrideIdFromResponse -Response $futureOverride
        $cleanupOverrideIds.Add($futureOverrideId) | Out-Null

        Disable-Override -Headers $headers -OverrideId $activeOverrideId
        $dbFallbackDecision = Refresh-PolicyDecision -Headers $headers -TargetType "ONLINE_TRANSACTION" -TargetId $fallbackTargetId -Reason "runtime-smoke-db-policy-fallback"
        if ($dbFallbackDecision.resolvedSource -ne "DB_POLICY" -or $dbFallbackDecision.overrideId -ne $null) {
            throw "Future override should not be selected before its start time. decision=$($dbFallbackDecision | ConvertTo-Json -Compress)"
        }

        $cpfDefaultDecision = Refresh-PolicyDecision -Headers $headers -TargetType "MODULE" -TargetId "CPF_SMOKE_NO_POLICY" -Reason "runtime-smoke-cpf-default"
        if ($cpfDefaultDecision.resolvedSource -ne "CPF_DEFAULT") {
            throw "CPF default fallback was not selected. decision=$($cpfDefaultDecision | ConvertTo-Json -Compress)"
        }

        $result.overrideFallback.status = "PASSED"
        $result.overrideFallback.activeOverride = [ordered]@{
            status = "PASSED"
            targetId = $fallbackTargetId
            overrideId = $activeOverrideId
            resolvedSource = $activeDecision.resolvedSource
        }
        $result.overrideFallback.futureOverride = [ordered]@{
            status = "PASSED"
            targetId = $fallbackTargetId
            overrideId = $futureOverrideId
            fallbackSource = $dbFallbackDecision.resolvedSource
        }
        $result.overrideFallback.dbPolicy = [ordered]@{
            status = "PASSED"
            targetId = $fallbackTargetId
            resolvedSource = $dbFallbackDecision.resolvedSource
            policyId = $dbFallbackDecision.policyId
        }
        $result.overrideFallback.cpfDefault = [ordered]@{
            status = "PASSED"
            targetType = "MODULE"
            targetId = "CPF_SMOKE_NO_POLICY"
            resolvedSource = $cpfDefaultDecision.resolvedSource
        }
        $result.overrideFallback.applicationDefault = [ordered]@{
            status = "UNIT_TESTED"
            test = "LogPolicyCacheTest.applicationDefaultIsUsedWhenDbPolicyIsMissing"
        }
        $result.overrideFallback.expiredOverride = [ordered]@{
            status = "UNIT_TESTED"
            test = "LogPolicyCacheTest.expiredOverrideFallsBackToDbPolicy"
        }
    }

    $latestPolicyLog = Get-LatestLogItem -Headers $headers -BusinessTransactionId $policyApiTransactionId
    if ($null -ne $latestPolicyLog) {
        $latestPolicyLogIdx = Get-LogIndexFromItem $latestPolicyLog
        $policyLogDetail = Get-LogDetail -Headers $headers -LogIdx $latestPolicyLogIdx
        $summary = $policyLogDetail.summary
        $transactionIdForAlias = Get-Value -Object $summary -Names @("TRANSACTION_ID", "transactionId", "transaction_id")
        $traceIdForSearch = Get-Value -Object $summary -Names @("TRACE_ID", "traceId", "trace_id")

        $logsByBusiness = Invoke-SmokeJson `
            -Method Get `
            -Uri "$AdmBaseUrl/adm/api/logs?businessTransactionId=$policyApiTransactionId&limit=1" `
            -Headers $headers
        if ($logsByBusiness.available -eq $false -or @($logsByBusiness.items).Count -eq 0) {
            throw "ADM transaction log list did not return business transaction result."
        }

        $observabilityByBusiness = Invoke-SmokeJson `
            -Method Get `
            -Uri "$AdmBaseUrl/adm/api/observability/business-transactions/${policyApiTransactionId}?limit=5" `
            -Headers $headers
        if ($observabilityByBusiness.available -eq $false -or @($observabilityByBusiness.transactionLogs).Count -eq 0) {
            throw "ADM observability business transaction query did not return transaction logs."
        }

        $logsByGlobal = $null
        $observabilityByGlobal = $null
        if (-not [string]::IsNullOrWhiteSpace($transactionIdForAlias)) {
            $encodedTransactionId = [uri]::EscapeDataString([string] $transactionIdForAlias)
            $logsByGlobal = Invoke-SmokeJson `
                -Method Get `
                -Uri "$AdmBaseUrl/adm/api/logs?transactionId=$encodedTransactionId&limit=1" `
                -Headers $headers
            if ($logsByGlobal.available -eq $false -or @($logsByGlobal.items).Count -eq 0) {
                throw "ADM transactionId alias search did not return a result."
            }
            $observabilityByGlobal = Invoke-SmokeJson `
                -Method Get `
                -Uri "$AdmBaseUrl/adm/api/observability/transactions/${encodedTransactionId}?limit=5" `
                -Headers $headers
            if ($observabilityByGlobal.available -eq $false -or @($observabilityByGlobal.transactionLogs).Count -eq 0) {
                throw "ADM observability transactionId query did not return transaction logs."
            }
        }

        $logsByTrace = $null
        $observabilityByTrace = $null
        if (-not [string]::IsNullOrWhiteSpace($traceIdForSearch)) {
            $encodedTraceId = [uri]::EscapeDataString([string] $traceIdForSearch)
            $logsByTrace = Invoke-SmokeJson `
                -Method Get `
                -Uri "$AdmBaseUrl/adm/api/logs?traceId=$encodedTraceId&limit=1" `
                -Headers $headers
            if ($logsByTrace.available -eq $false -or @($logsByTrace.items).Count -eq 0) {
                throw "ADM traceId search did not return a result."
            }
            $observabilityByTrace = Invoke-SmokeJson `
                -Method Get `
                -Uri "$AdmBaseUrl/adm/api/observability/traces/${encodedTraceId}?limit=5" `
                -Headers $headers
            if ($observabilityByTrace.available -eq $false -or @($observabilityByTrace.transactionLogs).Count -eq 0) {
                throw "ADM observability traceId query did not return transaction logs."
            }
        }

        $errorLogs = Invoke-SmokeJson `
            -Method Get `
            -Uri "$AdmBaseUrl/adm/api/logs?businessTransactionId=$policyApiTransactionId&logType=FAILURE&limit=1" `
            -Headers $headers
        $auditLogs = Invoke-SmokeJsonAllowHttpError `
            -Method Get `
            -Uri "$AdmBaseUrl/adm/api/audit-logs?limit=1" `
            -Headers $headers
        $policyAuditLogs = Invoke-SmokeJsonAllowHttpError `
            -Method Get `
            -Uri "$AdmBaseUrl/adm/api/log-policy-audits?targetType=ONLINE_TRANSACTION&targetId=$policyApiTransactionId&limit=5" `
            -Headers $headers
        if ($policyAuditLogs.statusCode -ne 200 -or @($policyAuditLogs.body.items).Count -eq 0) {
            throw "ADM log policy audit query did not return policy audit rows. statusCode=$($policyAuditLogs.statusCode)"
        }
        $globalAliasStatus = "SKIPPED_NO_TRANSACTION_ID"
        if ($logsByGlobal -ne $null) {
            $globalAliasStatus = "PASSED"
        }
        $traceSearchStatus = "SKIPPED_NO_TRACE_ID"
        if ($logsByTrace -ne $null) {
            $traceSearchStatus = "PASSED"
        }
        $errorLogQueryStatus = "PASSED"
        if ($errorLogs.available -eq $false) {
            $errorLogQueryStatus = "FAILED"
        }
        $auditLogQueryStatus = "PARTIAL"
        if ($auditLogs.statusCode -eq 200) {
            $auditLogQueryStatus = "PASSED"
        }
        $policyAuditQueryStatus = "PASSED"

        $result.admObservability.transactionLogList = [ordered]@{
            status = "PASSED"
            businessTransactionId = $policyApiTransactionId
            count = @($logsByBusiness.items).Count
        }
        $result.admObservability.transactionLogDetail = [ordered]@{
            status = "PASSED"
            logIdx = $latestPolicyLogIdx
            hasFormattedDetails = @($policyLogDetail.formattedDetails).Count -gt 0
        }
        $result.admObservability.businessTransactionSearch = [ordered]@{
            status = "PASSED"
            businessTransactionId = $policyApiTransactionId
            count = @($logsByBusiness.items).Count
        }
        $result.admObservability.transactionIdAlias = [ordered]@{
            status = $globalAliasStatus
            transactionId = $transactionIdForAlias
        }
        $result.admObservability.traceSearch = [ordered]@{
            status = $traceSearchStatus
            traceId = $traceIdForSearch
        }
        $result.admObservability.observabilityByBusinessTransaction = [ordered]@{
            status = "PASSED"
            businessTransactionId = $policyApiTransactionId
            transactionLogCount = @($observabilityByBusiness.transactionLogs).Count
            failureLogCount = @($observabilityByBusiness.failureLogs).Count
            policyAuditCount = @($observabilityByBusiness.policyAuditLogs).Count
        }
        $result.admObservability.observabilityByTransaction = [ordered]@{
            status = $(if ($observabilityByGlobal -ne $null) { "PASSED" } else { "SKIPPED_NO_TRANSACTION_ID" })
            transactionId = $transactionIdForAlias
            transactionLogCount = $(if ($observabilityByGlobal -ne $null) { @($observabilityByGlobal.transactionLogs).Count } else { 0 })
        }
        $result.admObservability.observabilityByTrace = [ordered]@{
            status = $(if ($observabilityByTrace -ne $null) { "PASSED" } else { "SKIPPED_NO_TRACE_ID" })
            traceId = $traceIdForSearch
            transactionLogCount = $(if ($observabilityByTrace -ne $null) { @($observabilityByTrace.transactionLogs).Count } else { 0 })
        }
        $result.admObservability.errorLogQuery = [ordered]@{
            status = $errorLogQueryStatus
            failureCount = @($errorLogs.items).Count
        }
        $result.admObservability.auditLogQuery = [ordered]@{
            status = $auditLogQueryStatus
            httpStatus = $auditLogs.statusCode
            note = "ADM generic audit API state only. cpf_log_policy_audit is still policy-domain audit storage."
        }
        $result.admObservability.policyAuditQuery = [ordered]@{
            status = $policyAuditQueryStatus
            httpStatus = $policyAuditLogs.statusCode
            count = @($policyAuditLogs.body.items).Count
            source = "cpf_log_policy_audit"
        }
    }
} catch {
    $result.error = $_.Exception.Message
    Save-Result
    throw
} finally {
    $idsToCleanup = @()
    if ($null -ne $overrideId) {
        $idsToCleanup += [long] $overrideId
    }
    foreach ($cleanupId in $cleanupOverrideIds) {
        $idsToCleanup += [long] $cleanupId
    }
    $idsToCleanup = @($idsToCleanup | Sort-Object -Unique)
    if ($idsToCleanup.Count -gt 0 -and $null -ne $login -and -not [string]::IsNullOrWhiteSpace($login.accessToken)) {
        $cleanupResults = New-Object System.Collections.Generic.List[object]
        $headers = @{ Authorization = "Bearer $($login.accessToken)" }
        foreach ($cleanupId in $idsToCleanup) {
            try {
                Disable-Override -Headers $headers -OverrideId $cleanupId
                $cleanupResults.Add([ordered]@{ overrideId = $cleanupId; status = "DISABLED" }) | Out-Null
            } catch {
                $cleanupResults.Add([ordered]@{ overrideId = $cleanupId; status = "FAILED"; message = $_.Exception.Message }) | Out-Null
            }
        }
        try {
            Refresh-TargetPolicy -Headers $headers
        } catch {
            $result.cleanup.refreshTargetPolicy = "FAILED: $($_.Exception.Message)"
        }
        $result.cleanup.overrides = $cleanupResults
    } elseif ($idsToCleanup.Count -gt 0) {
        $result.cleanup.overrides = "SKIPPED_NO_LOGIN_TOKEN"
    } else {
        $result.cleanup.overrides = "NONE"
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
