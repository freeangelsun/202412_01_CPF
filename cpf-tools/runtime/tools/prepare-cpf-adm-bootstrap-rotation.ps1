param(
    [string] $AdmBaseUrl = "http://127.0.0.1:8080",
    [string] $AdmUsername = "admin",
    [string] $ChannelCode = "ADM",
    [int] $TimeoutSec = 20,
    [int] $ReadyTimeoutSeconds = 120
)

# ADM Bootstrap 계정의 강제 비밀번호 변경을 **Runtime 당 한 번만** 수행하는 준비 단계다.
#
# 증상 근거: 각 Runtime 검증기(smoke)가 저마다 "passwordChangeRequired 면 회전한다"를 수행했다.
# 먼저 실행된 검증기가 비밀번호를 바꾸면, 뒤이어 실행된 검증기는 원래 비밀번호로 로그인해
# 400(운영자 인증 실패)으로 죽는다. 실패 지점이 검증 대상과 무관한 곳이라 원인 추적도 어렵다.
# 회전은 Runtime 준비의 책임이지 개별 검증기의 책임이 아니다.
#
# 비밀값은 자식 환경변수로만 받는다(명령줄/로그에 남기지 않는다).
#   CPF_ADM_BOOTSTRAP_SMOKE_PASSWORD : 기동 시 부여된 Bootstrap 비밀번호
#   CPF_ADM_SMOKE_PASSWORD           : 회전 후 모든 검증기가 사용할 비밀번호
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
[Console]::OutputEncoding = [Text.UTF8Encoding]::new($false)
$OutputEncoding = [Text.UTF8Encoding]::new($false)

$bootstrapPassword = $env:CPF_ADM_BOOTSTRAP_SMOKE_PASSWORD
$targetPassword = $env:CPF_ADM_SMOKE_PASSWORD
if ([string]::IsNullOrWhiteSpace($bootstrapPassword)) {
    throw 'CPF_ADM_BOOTSTRAP_SMOKE_PASSWORD 환경변수가 필요합니다.'
}
if ([string]::IsNullOrWhiteSpace($targetPassword)) {
    throw 'CPF_ADM_SMOKE_PASSWORD 환경변수가 필요합니다.'
}
if ($bootstrapPassword -eq $targetPassword) {
    throw 'Bootstrap 비밀번호와 회전 목표 비밀번호가 같습니다. 강제 변경 계약을 검증할 수 없습니다.'
}

$script:sequence = 0
$script:webSession = $null

function Get-IssuerCode([string] $Code) {
    # 거래ID issuer 는 최초 신뢰 거래 기동점의 canonical ChannelCode 다(Harness 30.7).
    # 축약/패딩으로 만들지 않는다.
    if ([string]::IsNullOrWhiteSpace($Code)) {
        throw 'CPF transactionId issuer requires the canonical ChannelCode of the initiating trusted channel.'
    }
    $trimmed = $Code.Trim().ToUpperInvariant()
    if ($trimmed -notmatch '^[A-Z0-9]{3}$') {
        throw "CPF transactionId issuer must be a 3-character canonical ChannelCode; truncation is not allowed. value=$Code"
    }
    return $trimmed
}

function New-AdmHeaders([string] $OperationId) {
    # ADM 은 Platform Control Plane 이라 Business SystemCode 를 가지지 않는다.
    # System 계열 Header 를 싣지 않고 정본 ChannelCode 계약만 사용한다(Harness 30.10/30.11/30.16.1).
    $script:sequence++
    $channel = Get-IssuerCode $ChannelCode
    $timestamp = Get-Date -Format 'yyyyMMddHHmmssfff'
    return @{
        'X-Transaction-Id' = "$timestamp$channel" + 'admrot1' + $script:sequence.ToString('0000000')
        'X-Trace-Id' = [guid]::NewGuid().ToString('N')
        'X-Request-Type' = 'SMOKE'
        'X-Client-Version' = 'runtime-prep'
        'X-Caller-Service' = 'cpf-runtime-prep'
        'X-Original-Channel' = $channel
        'X-Current-Channel' = $channel
        'X-Caller-Channel' = $channel
        'X-Target-Channel' = $channel
        'X-Target-Operation-Id' = $OperationId
    }
}

function Get-CsrfCookie {
    if ($null -eq $script:webSession) { return $null }
    return $script:webSession.Cookies.GetCookies($AdmBaseUrl) |
        Where-Object { $_.Name -eq 'XSRF-TOKEN' } | Select-Object -First 1
}

function Add-SessionParams([hashtable] $InvokeParams, [hashtable] $Headers) {
    if ($null -ne $script:webSession) {
        $InvokeParams.WebSession = $script:webSession
        # Cookie 전달을 PowerShell Cookie jar 동작에만 맡기지 않는다. 브라우저와 같은 형태로 명시한다.
        $pairs = @($script:webSession.Cookies.GetCookies($AdmBaseUrl) | ForEach-Object { "$($_.Name)=$($_.Value)" })
        if ($pairs.Count -gt 0) { $Headers['Cookie'] = ($pairs -join '; ') }
    }
    $cookie = Get-CsrfCookie
    if ($null -ne $cookie) { $Headers['X-XSRF-TOKEN'] = [string] $cookie.Value }
    # CpfTrustedOriginFilter 는 /adm/ 상태 변경 요청에 Origin/Referer 를 요구한다.
    $uri = [Uri] $AdmBaseUrl
    $Headers['Origin'] = "$($uri.Scheme)://$($uri.Authority)"
}

function Invoke-AdmJson([string] $Method, [string] $Uri, [string] $OperationId, [object] $Body = $null) {
    $headers = New-AdmHeaders $OperationId
    $params = @{
        Method = $Method
        Uri = $Uri
        Headers = $headers
        TimeoutSec = $TimeoutSec
        UseBasicParsing = $true
        ErrorAction = 'Stop'
    }
    Add-SessionParams $params $headers
    if ($null -ne $Body) {
        $params.ContentType = 'application/json;charset=UTF-8'
        $params.Body = [Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Depth 10))
    }
    try {
        $response = Invoke-WebRequest @params
    } catch {
        # 4xx 의 실제 원인(code/message)은 응답 본문에만 있다. 본문 없이는 다음 Runtime 주기까지
        # 원인 판별이 미뤄진다.
        $body = ''
        try { $body = [string] $_.ErrorDetails.Message } catch { }
        if ($body.Length -gt 600) { $body = $body.Substring(0, 600) + '...' }
        throw "CPF_ADM_ROTATION_HTTP_FAILURE method=$Method uri=$Uri operationId=$OperationId message=$($_.Exception.Message) body=$body"
    }
    $text = [Text.Encoding]::UTF8.GetString($response.RawContentStream.ToArray())
    if ([string]::IsNullOrWhiteSpace($text)) { return $null }
    return $text | ConvertFrom-Json
}

function Connect-Adm([string] $Password) {
    # 로그인 POST 전에 XSRF-TOKEN 을 선취한다. 없으면 permitAll 경로여도 CSRF 로 403 이 된다.
    $probeHeaders = New-AdmHeaders 'getAdmReadiness'
    $null = Invoke-WebRequest -Method Get -Uri "$AdmBaseUrl/adm/api/health" -Headers $probeHeaders `
        -TimeoutSec $TimeoutSec -UseBasicParsing -SessionVariable session -ErrorAction Stop
    $script:webSession = $session
    if ($null -eq (Get-CsrfCookie)) { throw 'ADM CSRF cookie(XSRF-TOKEN)가 발급되지 않았습니다.' }

    $null = Invoke-AdmJson 'Post' "$AdmBaseUrl/adm/api/auth/login" 'admAuthLogin' @{
        operatorId = $AdmUsername
        password = $Password
    }
    # BFF 계약상 인증은 HttpOnly Session Cookie 가 운반한다. Body 토큰은 노출되지 않는다.
    $sessionCookie = $script:webSession.Cookies.GetCookies($AdmBaseUrl) |
        Where-Object { $_.Name -ne 'XSRF-TOKEN' } | Select-Object -First 1
    if ($null -eq $sessionCookie) { throw 'ADM login did not establish a BFF session cookie.' }
}

function Wait-AdmReady {
    $deadline = (Get-Date).AddSeconds($ReadyTimeoutSeconds)
    while ((Get-Date) -lt $deadline) {
        try {
            $health = Invoke-RestMethod -Method Get -Uri "$AdmBaseUrl/adm/api/health" `
                -Headers (New-AdmHeaders 'getAdmReadiness') -TimeoutSec 10 -ErrorAction Stop
            if ($null -ne $health -and $health.status -eq 'UP') { return }
        } catch { }
        Start-Sleep -Seconds 2
    }
    throw "ADM health endpoint did not become UP within $ReadyTimeoutSeconds seconds."
}

Wait-AdmReady
Connect-Adm $bootstrapPassword

$me = Invoke-AdmJson 'Get' "$AdmBaseUrl/adm/api/auth/me" 'admAuthMe'
if (-not [bool] $me.passwordChangeRequired) {
    throw 'ADM Bootstrap 계정에 강제 비밀번호 변경이 요구되지 않습니다. Bootstrap 계약이 깨졌습니다.'
}

$null = Invoke-AdmJson 'Post' "$AdmBaseUrl/adm/api/operators/$AdmUsername/password" 'admOperatorChangePassword' @{
    currentPassword = $bootstrapPassword
    newPassword = $targetPassword
    newPasswordConfirm = $targetPassword
    reason = 'runtime-prep mandatory password rotation'
}

# 비밀번호 변경은 정본 계약상 해당 운영자의 모든 Session 을 revoke 한다
# (AdmOperatorService -> AdmSessionService.revokeOperatorSessions). 새 비밀번호로 다시 확립한다.
Connect-Adm $targetPassword
$rotated = Invoke-AdmJson 'Get' "$AdmBaseUrl/adm/api/auth/me" 'admAuthMe'
if ([bool] $rotated.passwordChangeRequired) {
    throw 'ADM 비밀번호 회전 후에도 강제 변경 상태가 남아 있습니다.'
}

Write-Output "CPF_ADM_BOOTSTRAP_ROTATION=PASS operatorId=$AdmUsername"
exit 0
