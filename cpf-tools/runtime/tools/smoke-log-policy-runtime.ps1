param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $AdmBaseUrl = "http://localhost:8090",
    [string] $AdmUsername = "admin",
    # ADM 은 Platform Control Plane 이라 SystemCode 가 없다(Harness 30.10).
    # 이 값은 ADM 운영 ChannelCode 이며 거래ID issuer 의 source 다(정본: OPS_CHANNEL_REGISTRY 의 ADM).
    # 기존 호출자의 -SystemCode 는 호환을 위해 alias 로만 수용한다. ADM SystemCode 로 해석하지 않는다.
    [Alias('SystemCode')]
    [string] $ChannelCode = "ADM",
    [string] $AdmPassword = $env:CPF_ADM_SMOKE_PASSWORD,
    # 검증 대상 업무 거래. DB Log Policy 는 @CpfOnlineTransaction 이 붙은 **업무 거래**에만
    # 적용된다. ADM 은 Platform Control Plane 이라 업무 거래 로그를 만들지 않으므로
    # ADM API 를 대상 거래로 쓰면 DB 로그가 영원히 생기지 않는다(존재하지 않는 거래ID로는
    # 정책 on/off 차이를 관측할 수 없다).
    [string] $TargetTransactionId = "MBW_AUTH_LOGIN",
    [string] $TargetProbeMethod = "Post",
    [string] $TargetProbePath = "/api/v1/backoffice/auth/login",
    [string] $TargetProbeBody = '{"loginId":"cpf-log-policy-probe","password":"cpf-log-policy-probe"}',
    # 대상 업무 거래를 소유한 Business SystemCode(1-WAS 조립에서는 MBW).
    [string] $TargetSystemCode = "MBW",
    [string] $TargetProbeExtraHeaders = "Idempotency-Key=",
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
    # 거래ID issuer 의 source 는 **최초 신뢰 거래 기동점의 canonical ChannelCode** 다(Harness 30.7).
    # 축약/패딩으로 issuer 를 만들지 않는다. 정본 ChannelCode 자체가 3자리 규격을 만족해야 한다.
    # (이전 구현은 LOCAL -> LOC 처럼 SystemCode 를 잘라 issuer 를 만들었고, 그 전제였던
    #  LOCAL SystemCode 자체가 존재하지 않는다.)
    if ([string]::IsNullOrWhiteSpace($Code)) {
        throw 'CPF transactionId issuer requires the canonical ChannelCode of the initiating trusted channel.'
    }
    $trimmed = $Code.Trim().ToUpperInvariant()
    if ($trimmed.Length -ne 3 -or $trimmed -notmatch '^[A-Z0-9]{3}$') {
        throw "CPF transactionId issuer must be a 3-character canonical ChannelCode; truncation is not allowed. value=$Code"
    }
    return $trimmed
}

function New-SmokeHeaders {
    $script:sequence++
    $timestamp = Get-Date -Format "yyyyMMddHHmmssfff"
    # 이 검증기는 ADM(Platform Control Plane) 운영 API 를 호출한다. ADM 은 Business SystemCode 를
    # 가지지 않으므로 System 계열 Header 를 보내지 않고 **정본 운영 ChannelCode 계약**을 쓴다
    # (Harness 30.10 / 30.11 / 30.16.1). 거래ID issuer 도 그 ChannelCode 다(Harness 30.7).
    # Business System 헤더를 ADM 경로에 실어 통과시키는 것은 금지된 False Green 이다(Harness 30.14).
    $channel = $ChannelCode.Trim().ToUpperInvariant()
    return @{
        "X-Transaction-Id" = "$timestamp" + (Get-CpfIssuerCode $channel) + "lgpol01" + $script:sequence.ToString("0000000")
        "X-Trace-Id" = [guid]::NewGuid().ToString("N")
        "X-Request-Type" = "SMOKE"
        "X-Client-Version" = "runtime-smoke"
        "X-Caller-Service" = "cpf-smoke"
        "X-Original-Channel" = $channel
        "X-Current-Channel" = $channel
        "X-Caller-Channel" = $channel
        "X-Target-Channel" = $channel
        # X-Target-Operation-Id 는 External CPF protocol 호출의 필수 Header 다
        # (CpfHttpInboundContextAdapter.requireExternal / CpfHttpHeaderCatalog.required).
        # 값은 호출 대상 Controller 의 정본 operationId 여야 한다. 검증기가 아무 거래 ID나
        # 실어 보내면 Runtime 이 실제로 그 계약을 지키는지 검증하지 못한다(False Green).
        # 기본값은 이 검증기가 가장 먼저 호출하는 readiness 거래다.
        "X-Target-Operation-Id" = "getAdmReadiness"
    }
}

$script:admWebSession = $null
$script:admCsrfToken = $null
$script:admCsrfInitialFingerprint = '(없음)'
$script:admCsrfCurrentFingerprint = '(없음)'

function Merge-OperationHeaders {
    # 호출자가 넘긴 Header 를 보존하면서 대상 Controller 의 정본 operationId 를 싣는다.
    param([hashtable] $Headers, [string] $OperationId)
    $merged = @{}
    if ($null -ne $Headers) { foreach ($key in $Headers.Keys) { $merged[$key] = $Headers[$key] } }
    $merged['X-Target-Operation-Id'] = $OperationId
    return $merged
}

function Get-SecretFingerprint {
    param([string] $Value)
    if ([string]::IsNullOrWhiteSpace($Value)) { return '(없음)' }
    $hash = [Security.Cryptography.SHA256]::HashData([Text.Encoding]::UTF8.GetBytes($Value))
    return [Convert]::ToHexString($hash).Substring(0, 12).ToLowerInvariant()
}

function Get-AdmCsrfCookie {
    # CookiePath=/adm 이므로 root URI가 아니라 실제 BFF path로 선택한다. Login 응답에서
    # 같은 이름의 Cookie가 회전될 수 있으므로 마지막(가장 최근) Cookie를 사용한다.
    if ($null -eq $script:admWebSession) { return $null }
    $uri = [Uri] ("{0}/adm/" -f $AdmBaseUrl.TrimEnd('/'))
    return @($script:admWebSession.Cookies.GetCookies($uri) |
        Where-Object { $_.Name -eq 'XSRF-TOKEN' } | Select-Object -Last 1)[0]
}

function Get-AdmCookieDiagnostics {
    if ($null -eq $script:admWebSession) { return '(세션없음)' }
    $uri = [Uri] ("{0}/adm/" -f $AdmBaseUrl.TrimEnd('/'))
    return (@($script:admWebSession.Cookies.GetCookies($uri) | ForEach-Object {
        "{0}(path={1},secure={2},httpOnly={3},valueLength={4})" -f $_.Name,$_.Path,$_.Secure,$_.HttpOnly,$_.Value.Length
    }) -join ',')
}

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
    $cookie = Get-AdmCsrfCookie
    if ($null -eq $cookie) { throw 'ADM CSRF cookie(XSRF-TOKEN)가 발급되지 않았습니다.' }
    $script:admCsrfToken = [string] $cookie.Value
    $script:admCsrfInitialFingerprint = Get-SecretFingerprint $script:admCsrfToken
    $script:admCsrfCurrentFingerprint = $script:admCsrfInitialFingerprint
    return $script:admCsrfToken
}

function Get-AdmOrigin {
    param([string] $BaseUrl)
    $uri = [Uri] $BaseUrl
    return "$($uri.Scheme)://$($uri.Authority)"
}

function Get-AdmCsrfToken {
    # 로그인 시 Session 이 회전하면(CpfBffCredentialResponseAdvice.changeSessionId) XSRF-TOKEN Cookie 도
    # 새로 발급된다. 캐시한 값을 계속 쓰면 그 다음 상태 변경 요청이 403 이 된다.
    # 그래서 매 요청마다 WebSession 의 현재 Cookie 를 읽는다.
    if ($null -eq $script:admWebSession) { return $script:admCsrfToken }
    $cookie = Get-AdmCsrfCookie
    if ($null -ne $cookie) {
        $script:admCsrfToken = [string] $cookie.Value
        $script:admCsrfCurrentFingerprint = Get-SecretFingerprint $script:admCsrfToken
    }
    return $script:admCsrfToken
}

function Add-AdmSessionParams {
    param([hashtable] $InvokeParams, [hashtable] $Headers)
    if ($null -ne $script:admWebSession) {
        $InvokeParams.WebSession = $script:admWebSession
        # Cookie 전달을 PowerShell Cookie jar 동작에만 맡기지 않는다. 실제로 jar 에는 XSRF-TOKEN 이
        # 있는데 서버는 받지 못해 CSRF_TOKEN_MISSING(= 서버가 토큰을 새로 생성) 으로 거절됐다.
        # 브라우저와 동일하게 Cookie Header 를 명시해 전송을 결정적으로 만든다.
        $cookiePairs = @($script:admWebSession.Cookies.GetCookies($AdmBaseUrl) |
            ForEach-Object { "$($_.Name)=$($_.Value)" })
        if ($cookiePairs.Count -gt 0) { $Headers['Cookie'] = ($cookiePairs -join '; ') }
    }
    $csrf = Get-AdmCsrfToken
    if (-not [string]::IsNullOrWhiteSpace($csrf)) { $Headers['X-XSRF-TOKEN'] = $csrf }
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
    try {
        ConvertFrom-Utf8JsonResponse (Invoke-WebRequest @invokeParams)
    } catch {
        # 403/401 은 원인이 CSRF / Origin / 권한 중 무엇인지 본문만으로는 구분되지 않는다.
        # 다음 Runtime 주기를 기다리지 않도록 실제로 보낸 보호 Header 상태를 함께 남긴다.
        $sent = @()
        foreach ($name in @('X-XSRF-TOKEN','Origin','X-Target-Operation-Id','X-System-Code')) {
            if ($mergedHeaders.ContainsKey($name)) {
                $value = [string] $mergedHeaders[$name]
                $shown = if ($name -eq 'X-XSRF-TOKEN' -and $value.Length -gt 8) { $value.Substring(0,8) + '...' } else { $value }
                $sent += "$name=$shown"
            } else { $sent += "$name=(없음)" }
        }
        $cookieNames = Get-AdmCookieDiagnostics
        $securityRejection = '(없음)'
        try {
            $response = $_.Exception.Response
            if ($null -ne $response) {
                $values = @($response.Headers.GetValues('X-CPF-Security-Rejection'))
                if ($values.Count -gt 0) { $securityRejection = [string]$values[0] }
            }
        } catch { }
        $sentFingerprint = Get-SecretFingerprint ([string]$mergedHeaders['X-XSRF-TOKEN'])
        # 4xx 의 실제 원인(code/message)은 응답 본문에만 있다. 본문을 남기지 않으면
        # 다음 Runtime 주기까지 원인 판별이 미뤄진다(400 CpfValidationException 을 그렇게 놓쳤다).
        $responseBody = ''
        try {
            $errorDetails = $_.ErrorDetails
            if ($null -ne $errorDetails -and -not [string]::IsNullOrWhiteSpace([string]$errorDetails.Message)) {
                $responseBody = [string] $errorDetails.Message
            }
        } catch { }
        if ($responseBody.Length -gt 600) { $responseBody = $responseBody.Substring(0,600) + '...' }
        throw "$($_.Exception.Message) | uri=$Uri securityRejection=$securityRejection csrfFingerprint(initial=$script:admCsrfInitialFingerprint,current=$script:admCsrfCurrentFingerprint,sent=$sentFingerprint) sentHeaders=[$($sent -join '; ')] cookies=[$cookieNames] body=$responseBody"
    }
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
        # PowerShell 7 의 Invoke-WebRequest 는 System.Net.Http.HttpResponseMessage 를 준다.
        # 구버전의 HttpWebResponse.GetResponseStream() 을 호출하면 InvalidOperation 으로 죽어
        # 검증 대상이 아니라 검증기 자신이 실패한다. 본문은 ErrorDetails 로 읽는다.
        $content = ""
        try { $content = [string] $_.ErrorDetails.Message } catch { }
        if ([string]::IsNullOrWhiteSpace($content) -and $webResponse.PSObject.Methods["GetResponseStream"]) {
            $stream = $webResponse.GetResponseStream()
            if ($null -ne $stream) {
                $reader = [System.IO.StreamReader]::new($stream, [System.Text.Encoding]::UTF8, $true)
                try {
                    $content = $reader.ReadToEnd()
                } finally {
                    $reader.Dispose()
                    $stream.Dispose()
                }
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
        -Headers (Merge-OperationHeaders $Headers 'admLogFindLogs')
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
    $detail = Invoke-SmokeJson -Method Get -Uri "$AdmBaseUrl/adm/api/logs/$LogIdx" -Headers (Merge-OperationHeaders $Headers 'admLogGetLogDetail')
    if ($detail.available -eq $false) {
        throw "ADM log detail API is not available. logIdx=$LogIdx message=$($detail.message)"
    }
    return $detail.item
}

function Get-LogDetailEntries {
    # ADM 상세 조회는 detail 목록을 formattedDetails(detailKey/raw) 로 돌려준다.
    # 예전 이름(details/DETAIL_KEY)만 보면 항상 빈 목록이 되어 "정책이 기록되지 않았다"고
    # 오판한다. 두 형태를 모두 받아들인다.
    param([object] $LogDetail)
    if ($null -eq $LogDetail) { return @() }
    $entries = @()
    foreach ($name in @("formattedDetails", "details")) {
        $property = $LogDetail.PSObject.Properties[$name]
        if ($null -ne $property -and $null -ne $property.Value) { $entries += @($property.Value) }
    }
    return $entries
}

function Get-DetailEntryKey {
    param([object] $Entry)
    return [string](Get-Value -Object $Entry -Names @("detailKey", "DETAIL_KEY", "detail_key"))
}

function Test-DetailKeyExists {
    param(
        [object] $LogDetail,
        [string] $DetailKey
    )
    foreach ($entry in (Get-LogDetailEntries -LogDetail $LogDetail)) {
        if ($DetailKey.Equals((Get-DetailEntryKey -Entry $entry), [System.StringComparison]::OrdinalIgnoreCase)) {
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
    foreach ($entry in (Get-LogDetailEntries -LogDetail $LogDetail)) {
        if ($DetailKey.Equals((Get-DetailEntryKey -Entry $entry), [System.StringComparison]::OrdinalIgnoreCase)) {
            return Get-Value -Object $entry -Names @("raw", "DETAIL_VALUE", "detailValue", "detail_value")
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
        # "값이 다르다"와 "키 자체가 없다"는 원인이 전혀 다르다. 실제 detail 키 목록을 함께 남긴다.
        $available = @(Get-LogDetailEntries -LogDetail $LogDetail | ForEach-Object {
            Get-DetailEntryKey -Entry $_
        } | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } | Sort-Object)
        throw "Unexpected detail value. key=$DetailKey expected=$ExpectedValue actual=$value availableKeys=[$($available -join ', ')]"
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
        -Headers (Merge-OperationHeaders $Headers 'admLogPolicyRefreshCache')
}

function New-TargetTransactionHeaders {
    # 대상은 업무 거래다. ADM 운영 Channel Header 가 아니라 **소유 Business System 의
    # System6 Header** 를 실어야 한다(Harness 30.16.1). ADM 경로에 Business System Header 를
    # 싣거나 그 반대로 하는 것은 금지된 False Green 이다.
    $timestamp = Get-Date -Format "yyyyMMddHHmmssfff"
    $issuer = Get-CpfIssuerCode $TargetSystemCode
    $script:sequence++
    $headers = @{
        "X-Transaction-Id" = "$timestamp$issuer" + "lgpolt" + $script:sequence.ToString("00000000")
        "X-Original-System-Code" = $issuer
        "X-System-Code" = $TargetSystemCode
        "X-Caller-System-Code" = $TargetSystemCode
        "X-Target-System-Code" = $TargetSystemCode
        "X-Target-Operation-Id" = $TargetTransactionId
        "X-Trace-Id" = [guid]::NewGuid().ToString("N")
        "X-Request-Type" = "RUNTIME_VALIDATION"
        "X-Client-Version" = "runtime-smoke"
    }
    foreach ($entry in ($TargetProbeExtraHeaders -split ';')) {
        if ([string]::IsNullOrWhiteSpace($entry)) { continue }
        $pair = $entry.Split('=', 2)
        $name = $pair[0].Trim()
        if ([string]::IsNullOrWhiteSpace($name)) { continue }
        $value = if ($pair.Count -eq 2) { $pair[1].Trim() } else { '' }
        if ([string]::IsNullOrWhiteSpace($value)) { $value = [guid]::NewGuid().ToString('N') }
        $headers[$name] = $value
    }
    return $headers
}

function Invoke-TargetTransaction {
    # 업무 결과(예: 401)는 검증에 지장이 없다. DB 로그가 남는지가 관측 대상이다.
    param([hashtable] $Headers)
    $null = $Headers
    $params = @{
        Method = $TargetProbeMethod
        Uri = "$AdmBaseUrl$TargetProbePath"
        Headers = (New-TargetTransactionHeaders)
        ContentType = "application/json;charset=UTF-8"
        TimeoutSec = 20
        UseBasicParsing = $true
        SkipHttpErrorCheck = $true
    }
    if (-not [string]::IsNullOrWhiteSpace($TargetProbeBody)) {
        $params.Body = [System.Text.Encoding]::UTF8.GetBytes($TargetProbeBody)
    }
    $response = Invoke-WebRequest @params
    if ([int] $response.StatusCode -ge 500) {
        throw "target business transaction returned a server error. status=$([int]$response.StatusCode) body=$([string]$response.Content)"
    }
}

function Refresh-TargetPolicy {
    # 정책 재평가 결과를 버리지 않는다. 이 값이 곧 Runtime 이 실제로 적용할 결정이며,
    # 결과를 버리면 "정책이 반영됐는가"와 "로그가 남았는가"를 구분할 수 없다.
    param([hashtable] $Headers)
    return Refresh-PolicyDecision -Headers $Headers -TargetType "ONLINE_TRANSACTION" -TargetId $TargetTransactionId
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

function Get-CaptureMode {
    # Y/N 플래그를 정본 CaptureMode 로 옮긴다. N 은 항상 NONE(수집 안 함)이다.
    param([string] $Flag, [string] $EnabledMode)
    if ([string]::IsNullOrWhiteSpace($Flag)) { return $null }
    if ($Flag.Trim().ToUpperInvariant() -eq "Y") { return $EnabledMode }
    return "NONE"
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
    # 유효구간은 Runtime 이 정책을 평가할 때 쓰는 시간 기준과 같아야 한다. CPF Runtime 은
    # LogPolicyCache/LoggingAspect 모두 Clock.systemUTC() 를 기본으로 쓰며 LocalDateTime 을
    # 그 zone 의 wall-clock 으로 저장·비교한다. 검증기가 로컬 시각을 보내면 UTC 와의
    # offset 만큼 구간이 미래로 밀려 override 가 절대 매칭되지 않는다(KST 는 +9시간).
    $start = [DateTime]::UtcNow.AddMinutes($StartOffsetMinutes).ToString("yyyy-MM-ddTHH:mm:ss")
    $end = [DateTime]::UtcNow.AddMinutes($EndOffsetMinutes).ToString("yyyy-MM-ddTHH:mm:ss")
    Invoke-SmokeJson `
        -Method Post `
        -Uri "$AdmBaseUrl/adm/api/log-policies/overrides" `
        -Headers (Merge-OperationHeaders $Headers 'admLogPolicyCreateOverride') `
        -Body @{
            targetType = "ONLINE_TRANSACTION"
            targetId = $TargetId
            logLevel = "INFO"
            dbLogEnabledYn = $DbLogEnabledYn
            fileLogEnabledYn = "Y"
            requestBodyLogYn = $RequestBodyLogYn
            responseBodyLogYn = $ResponseBodyLogYn
            errorStackLogYn = $ErrorStackLogYn
            # Runtime 의 정책 결정은 *_capture_mode 로 계산된다
            # (LogPolicyDecision.errorStackSave() 등은 captureMode != NONE 으로 판정하고,
            #  JdbcLogPolicyRepository 는 *_log_yn 컬럼을 읽지 않는다).
            # 검증기가 legacy *_log_yn 만 보내면 실제로는 아무 것도 바꾸지 못한 채
            # "정책을 걸었다"고 착각하게 된다. 정본 필드를 함께 보낸다.
            # BODY 영역이 허용하는 모드는 NONE/METADATA_ONLY/ALLOWLIST_FIELDS/MASKED_BODY/
            # ENCRYPTED_BODY 뿐이다(LogCaptureMode.validateFor). STACK 전용 FULL_MASKED 를
            # 쓰면 ADM 이 400 으로 거절한다. 본문을 남기는 모드는 MASKED_BODY 다.
            requestBodyCaptureMode = (Get-CaptureMode $RequestBodyLogYn "MASKED_BODY")
            responseBodyCaptureMode = (Get-CaptureMode $ResponseBodyLogYn "MASKED_BODY")
            errorStackCaptureMode = (Get-CaptureMode $ErrorStackLogYn "SUMMARY")
            effectiveStartAt = $start
            effectiveEndAt = $end
            # requestUser/approvedBy 는 보내지 않는다. 정본 계약상 요청자는 서버 Context
            # (adm.operatorId)로 확정된다. 본문에 운영자 식별자를 실으면
            # AdmVerifiedActorRequestBodyAdvice 가 "인증 주체와 일치"를 요구하고,
            # AdmLogPolicyService.createOverride 는 "요청자 != 승인자"를 요구하므로
            # 두 계약이 동시에 성립할 수 없어 어떤 값을 넣어도 400 이 된다.
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
        -Headers (Merge-OperationHeaders $Headers 'admLogPolicyDisableOverride') | Out-Null
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

    # ADM Bootstrap 계정의 강제 비밀번호 변경은 Runtime 준비 단계가 한 번만 수행한다
    # (prepare-cpf-adm-bootstrap-rotation.ps1). 검증기마다 회전하면 먼저 실행된 검증기가
    # 비밀번호를 바꿔 뒤 검증기가 검증 대상과 무관한 인증 실패로 죽는다.
    # 여기서는 회전이 실제로 적용됐는지만 확인한다.
    $currentSession = Invoke-SmokeJson -Method Get -Uri "$AdmBaseUrl/adm/api/auth/me" `
        -Headers @{ "X-Target-Operation-Id" = "admAuthMe" }
    if ([bool]$currentSession.passwordChangeRequired) {
        throw 'ADM 운영자에 강제 비밀번호 변경이 남아 있습니다. Runtime 준비 단계의 회전이 적용되지 않았습니다.'
    }
    # BFF 계약상 Browser 채널은 Authorization Header 를 보내면 안 된다.
    # CpfBffSessionBridgeFilter 가 Authorization 이 있으면
    # 400 "Browser Authorization header is prohibited" 로 잘라낸다.
    # 인증은 로그인으로 발급된 HttpOnly Session Cookie(= $script:admWebSession)가 운반한다.
    $headers = @{}

    $baselineDecision = Refresh-TargetPolicy -Headers $headers
    $result.baseline.resolvedSource = [string] $baselineDecision.resolvedSource
    $result.baseline.dbLogEnabledYn = [string] $baselineDecision.dbLogEnabledYn
    $baselineLogIdx = Get-LatestLogIndex -Headers $headers
    $result.baseline.latestLogIdx = $baselineLogIdx

    $override = Create-DbLogOffOverride -Headers $headers
    $overrideId = Get-OverrideIdFromResponse -Response $override
    $disabledDecision = Refresh-TargetPolicy -Headers $headers
    # 정책 반영 실패와 정책 미준수는 원인이 다르다. 먼저 "Runtime 이 무엇으로 결정했는가"를 확인한다.
    if ([string]$disabledDecision.dbLogEnabledYn -ne "N") {
        throw "log policy override was not applied to the runtime decision. targetId=$TargetTransactionId dbLogEnabledYn=$($disabledDecision.dbLogEnabledYn) resolvedSource=$($disabledDecision.resolvedSource) overrideId=$($disabledDecision.overrideId) policyId=$($disabledDecision.policyId)"
    }
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
    $null = Refresh-TargetPolicy -Headers $headers
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

    # 상세 정책(요청/응답 Body, Error Stack)도 업무 거래에서만 관측된다. ADM 은 Platform
    # Control Plane 이라 자신의 API 호출을 업무 거래 로그로 남기지 않으므로, ADM 거래ID 를
    # 대상으로 두면 영원히 로그를 기다리게 된다.
    $policyApiTransactionId = $TargetTransactionId
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

            # 무관한 대상의 override 가 이 대상의 결정에 새어 들어오지 않아야 한다.
            # 관측 대상 로그는 업무 거래를 직접 호출해서 만든다.
            Invoke-TargetTransaction -Headers $headers
            $bodyLogItem = Wait-NewLogItem -Headers $headers -BusinessTransactionId $policyApiTransactionId -AfterLogIdx $bodyBaseline
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
                -Headers (Merge-OperationHeaders $headers 'admLogPolicyCreateOverride') `
                -Body @{
                    targetType = "ONLINE_TRANSACTION"
                    targetId = "CPF_SMOKE_INVALID_TARGET"
                    logLevel = "INFO"
                    dbLogEnabledYn = "Y"
                    fileLogEnabledYn = "Y"
                    requestBodyLogYn = "N"
                    responseBodyLogYn = "N"
                    errorStackLogYn = "N"
                    effectiveStartAt = [DateTime]::UtcNow.AddMinutes(-5).ToString("yyyy-MM-ddTHH:mm:ss")
                    effectiveEndAt = $null
                    # 이 호출은 "잘못된 대상" 검증이 거절 사유여야 한다. 본문에 운영자
                    # 식별자를 실으면 actor/직무분리 검증이 먼저 걸려 의도한 검증이 아닌
                    # 이유로 통과하는 False Green 이 된다.
                    reason = "runtime-smoke-invalid-error-stack"
                }
            if ($errorResponse.statusCode -lt 400) {
                throw "Invalid override request did not return an HTTP error. statusCode=$($errorResponse.statusCode)"
            }

            # Error Stack 정책은 실패한 **업무 거래** 로그에서 관측한다. 위 ADM 호출은
            # "잘못된 override 요청을 ADM 이 거절하는가"를 검증할 뿐 업무 거래 로그를 만들지 않는다.
            Invoke-TargetTransaction -Headers $headers
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
            $null = Refresh-TargetPolicy -Headers $headers
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
