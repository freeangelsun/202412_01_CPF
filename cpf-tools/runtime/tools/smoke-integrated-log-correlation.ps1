[CmdletBinding()]
param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $BaseUrl = 'http://127.0.0.1:8080',
    [string] $LogBasePath = '',
    [string] $RuntimeLogRoot = '',
    [string] $FileLogResultPath = '',
    [string] $LogPolicyResultPath = '',
    [string] $AdmUsername = 'admin',
    [string] $ResultPath = '',
    [int] $TimeoutSec = 20,
    # System6 계약상 X-System-Code 는 수신 Runtime 의 System Code 와 같아야 한다.
    # 1-WAS 는 System 이 아니라 topology 다. 호출자가 대상 canonical Identity 를 알려준다.
    [string] $SystemCode = 'EDU',
    [int] $PollSeconds = 15,

    # --------------------------------------------------------------------------------------
    # Probe 대상 설정 — 자주 바뀌는 값은 여기 한 곳에 모은다.
    # 상관관계 검증에 필요한 것은 "같은 transactionId/traceId 가 File/DB/ADM 에 함께 보이는가"
    # 이지 특정 업무의 성공 여부가 아니다. 대상 Runtime 에 실제로 존재하는
    # @CpfOnlineTransaction 이면 된다(LoggingAspect 는 이 annotation 에만 걸린다).
    #
    # 기본값 : 단독 EDU Runtime
    # 1-WAS  : EDU 는 Local Module Catalog(CpfLocalRuntimeModules)에 없다. 호출자가
    #          MBW_AUTH_LOGIN 처럼 조립 안에 있는 거래를 지정한다.
    # --------------------------------------------------------------------------------------
    [string] $ProbePath = '/edu/online/member-processing',
    [string] $ProbeOperationId = 'EDU_LOCAL_MEMBER_PROCESS',
    # 그대로 전송할 JSON 본문. 기본값은 EDU 가 받는 JSON 문자열이다.
    [string] $ProbeBody = '"runtime-log-correlation"',
    # 대상 거래가 요구하는 추가 Header 를 "이름=값;이름=값" 으로 전달한다(값이 비면 새 GUID).
    [string] $ProbeExtraHeaders = '',
    # 자격증명 없이 상관관계만 검증할 때 업무 결과 4xx/5xx 를 허용한다.
    [switch] $AllowNonSuccessProbe
)

# Runtime-only closure for SPECIAL-09/10. A single transaction must be visible in
# structured FileLog, DB log and ADM observability with the same transactionId/traceId.
# Credentials are read from process environment only and are never emitted to stdout/evidence.
# Full Runtime child-process UTF-8 contract. Keep the emitted byte stream UTF-8 even when pwsh is redirected.
$CpfUtf8ConsoleEncoding = [Text.UTF8Encoding]::new($false)
try {
    [Console]::InputEncoding = $CpfUtf8ConsoleEncoding
    [Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
    $OutputEncoding = $CpfUtf8ConsoleEncoding
    $global:OutputEncoding = $CpfUtf8ConsoleEncoding
} catch { }
$env:PYTHONUTF8 = '1'
$env:PYTHONIOENCODING = 'utf-8'

$ErrorActionPreference = 'Stop'
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
try {
    [Console]::InputEncoding = $Utf8NoBom
    [Console]::OutputEncoding = $Utf8NoBom
    $OutputEncoding = $Utf8NoBom
    $global:OutputEncoding = $Utf8NoBom
} catch { }
$env:PYTHONUTF8 = '1'
$env:PYTHONIOENCODING = 'utf-8'
$Root = (Resolve-Path -LiteralPath $Root).Path
if ([string]::IsNullOrWhiteSpace($LogBasePath)) { $LogBasePath = Join-Path $Root 'logs' }
if ([string]::IsNullOrWhiteSpace($RuntimeLogRoot)) { $RuntimeLogRoot = Join-Path $Root 'build\cpf-local-runtime\logs' }
if ([string]::IsNullOrWhiteSpace($ResultPath)) { $ResultPath = Join-Path $Root 'build\runtime-smoke\integrated-log-correlation-result.json' }
[IO.Directory]::CreateDirectory((Split-Path -Parent $ResultPath)) | Out-Null
$ValidationStartedUtc = [DateTime]::UtcNow
$EvidenceRoot = Split-Path -Parent $ResultPath

function Get-SafeProperty([object]$Object,[string]$Name,[object]$Default=$null) {
    if ($null -eq $Object) { return $Default }
    $p = $Object.PSObject.Properties[$Name]
    if ($null -eq $p) { return $Default }
    return $p.Value
}
function Get-ArrayCount([object]$Object,[string]$Name) {
    $value = Get-SafeProperty $Object $Name @()
    if ($null -eq $value) { return 0 }
    return @($value).Count
}
function Read-JsonIfPresent([string]$Path) {
    if ([string]::IsNullOrWhiteSpace($Path) -or -not (Test-Path -LiteralPath $Path -PathType Leaf)) { return $null }
    try { return (Get-Content -LiteralPath $Path -Raw -Encoding UTF8 | ConvertFrom-Json) } catch { return $null }
}
function Get-CpfStableHttpFailure([string]$Method,[string]$Uri,[Exception]$Exception) {
    $status='NA'
    try {
        if($null -ne $Exception.Response -and $null -ne $Exception.Response.StatusCode){$status=[string][int]$Exception.Response.StatusCode}
    } catch { }
    $type=$Exception.GetType().FullName
    $innerType=if($null -ne $Exception.InnerException){$Exception.InnerException.GetType().FullName}else{'NA'}
    return "CPF_HTTP_FAILURE method=$Method uri=$Uri status=$status type=$type innerType=$innerType"
}
function Invoke-CpfJsonGet([string]$Uri,[hashtable]$Headers) {
    try {
        $params=@{Method='Get';Uri=$Uri;Headers=$Headers;TimeoutSec=$TimeoutSec;ErrorAction='Stop'}
        if ($null -ne $script:admWebSession) { $params.WebSession=$script:admWebSession }
        return Invoke-RestMethod @params
    } catch {
        throw [InvalidOperationException]::new((Get-CpfStableHttpFailure 'GET' $Uri $_.Exception),$_.Exception)
    }
}
function Invoke-CpfJsonPost([string]$Uri,[hashtable]$Headers,[object]$Body) {
    try {
        return Invoke-RestMethod -Method Post -Uri $Uri -Headers $Headers -ContentType 'application/json' -Body ($Body | ConvertTo-Json -Compress) -TimeoutSec $TimeoutSec -ErrorAction Stop
    } catch {
        throw [InvalidOperationException]::new((Get-CpfStableHttpFailure 'POST' $Uri $_.Exception),$_.Exception)
    }
}
$script:admHeaderSequence=0
function New-CpfAdmHeaders([string]$OperationId) {
    # ADM Route 도 External CPF protocol 계약을 그대로 요구한다. Authorization 만 보내면
    # X-Transaction-Id / X-Target-Operation-Id 부재로 ECPF900002 400 이 되어 관측 API 응답이
    # 전부 비고, 상관관계 단정이 "증적 없음" 으로 실패한다.
    # 조회용 거래ID 는 검증 대상 거래ID 와 반드시 달라야 한다(교차 오염 방지).
    $script:admHeaderSequence++
    $stamp=Get-Date -Format 'yyyyMMddHHmmssfff'
    $issuer=Get-CpfIssuerCode $SystemCode
    return @{
        'X-Transaction-Id'="$stamp$issuer" + 'logadm1' + $script:admHeaderSequence.ToString('0000000')
        'X-Original-System-Code'=$issuer
        'X-System-Code'=$SystemCode
        'X-Caller-System-Code'=$SystemCode
        'X-Target-System-Code'=$SystemCode
        'X-Target-Operation-Id'=$OperationId
        'X-Request-Type'='RUNTIME_VALIDATION'
        'X-Client-Version'='1.0.0'
        'X-User-Id'='runtime-validation'
        # CpfTrustedOriginFilter 는 /adm/ 의 상태 변경 요청에 Origin/Referer 를 요구한다.
        # 없으면 "Untrusted request origin" 403 이다. Browser 와 같은 same-origin 값을 보낸다.
        'Origin'=("{0}://{1}" -f ([Uri]$BaseUrl).Scheme,([Uri]$BaseUrl).Authority)
    }
}
$script:admWebSession=$null
$script:admCsrfToken=$null
function Initialize-CpfAdmCsrf([string]$BaseUrl) {
    # ADM BFF Security Chain 은 /adm/** 에 CSRF 를 적용한다
    # (CookieCsrfTokenRepository + CpfCsrfCookieExposureFilter). 공개 GET 으로 XSRF-TOKEN cookie 를
    # 먼저 받지 않으면 permitAll 경로인 로그인 POST 조차 403 으로 거절된다.
    # 인증은 HttpOnly Session Cookie 로 이어지므로 이후 조회도 같은 WebSession 을 쓴다.
    $probe=Invoke-WebRequest -Method Get -Uri "$BaseUrl/adm/api/health" -Headers (New-CpfAdmHeaders 'getAdmHealth') `
        -TimeoutSec $TimeoutSec -UseBasicParsing -SessionVariable session
    $null=$probe
    $script:admWebSession=$session
    $cookie=$session.Cookies.GetCookies($BaseUrl) | Where-Object { $_.Name -eq 'XSRF-TOKEN' } | Select-Object -First 1
    if ($null -eq $cookie) { throw 'ADM CSRF cookie(XSRF-TOKEN)가 발급되지 않았습니다.' }
    $script:admCsrfToken=[string]$cookie.Value
}
function Get-CpfAdmCsrfToken([string]$BaseUrl) {
    # 로그인 시 Session 회전으로 XSRF-TOKEN Cookie 가 새로 발급된다. 캐시 값을 계속 쓰면
    # 그 다음 상태 변경 요청이 403 이 되므로 매번 현재 Cookie 를 읽는다.
    if ($null -eq $script:admWebSession) { return $script:admCsrfToken }
    $cookie=$script:admWebSession.Cookies.GetCookies($BaseUrl) |
        Where-Object { $_.Name -eq 'XSRF-TOKEN' } | Select-Object -First 1
    if ($null -ne $cookie) { $script:admCsrfToken=[string]$cookie.Value }
    return $script:admCsrfToken
}
function Invoke-CpfRawJsonPost([string]$Uri,[hashtable]$Headers,[string]$RawBody,[bool]$AllowNonSuccess) {
    # 이미 JSON 인 본문을 그대로 보낸다. AllowNonSuccess 이면 4xx/5xx 도 예외로 만들지 않고
    # CPF 표준 오류 본문을 그대로 돌려준다 — 오류 본문에도 transactionId 가 실려 있으므로
    # 상관관계 추적 키는 동일하게 확정된다.
    try {
        $params=@{Method='Post';Uri=$Uri;Headers=$Headers;ContentType='application/json';Body=$RawBody
            TimeoutSec=$TimeoutSec;UseBasicParsing=$true;ErrorAction='Stop'}
        if ($AllowNonSuccess) { $params.SkipHttpErrorCheck=$true }
        # ADM Route 는 HttpOnly Session Cookie 로 인증한다. Session 이 있으면 함께 보낸다.
        if ($null -ne $script:admWebSession) { $params.WebSession=$script:admWebSession }
        $response = Invoke-WebRequest @params
        if (-not $AllowNonSuccess -and [int]$response.StatusCode -ge 400) {
            throw "unexpected status=$([int]$response.StatusCode)"
        }
        try { return $response.Content | ConvertFrom-Json } catch { return $null }
    } catch {
        throw [InvalidOperationException]::new((Get-CpfStableHttpFailure 'POST' $Uri $_.Exception),$_.Exception)
    }
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

function Find-TextFiles([string[]]$Roots) {
    $files = New-Object Collections.Generic.List[IO.FileInfo]
    foreach ($candidate in $Roots) {
        if ([string]::IsNullOrWhiteSpace($candidate) -or -not (Test-Path -LiteralPath $candidate)) { continue }
        if (Test-Path -LiteralPath $candidate -PathType Leaf) { [void]$files.Add((Get-Item -LiteralPath $candidate)); continue }
        Get-ChildItem -LiteralPath $candidate -Recurse -File -ErrorAction SilentlyContinue | ForEach-Object {
            if ($_.Length -le 10MB -and $_.LastWriteTimeUtc -ge $ValidationStartedUtc.AddSeconds(-5)) { [void]$files.Add($_) }
        }
    }
    return @($files)
}
function Read-CpfLiveLogText([string]$Path) {
    # 이 검증기는 1-WAS 가 살아 있는 동안 그 Runtime 이 지금도 쓰고 있는 File Log 를 읽는다.
    # Windows 에서 File Log Owner(CpfFileLogWriter)는 rolling 파일 핸들을 연 채 유지하는데
    # [IO.File]::ReadAllText/ReadAllLines/ReadLines 는 FileShare.Read 로만 열기 때문에
    # 쓰기 핸들이 살아 있으면 "다른 프로세스가 사용 중" 으로 던진다.
    # 이 스크립트는 각 읽기를 try/catch 로 감싸므로 그 예외가 삼켜져 '상관관계 없음' 이라는
    # 잘못된 FAIL 로 보고된다. 실제 결함이 아닌 파일 잠김이 원인이므로 공유 모드를 명시한다.
    # 파일 부재/권한 오류는 그대로 예외로 남긴다 — '잠김' 만 허용하고 증적 부재는 숨기지 않는다.
    $stream=$null;$reader=$null
    try {
        $stream=[IO.FileStream]::new($Path,[IO.FileMode]::Open,[IO.FileAccess]::Read,
            ([IO.FileShare]::ReadWrite -bor [IO.FileShare]::Delete))
        $reader=[IO.StreamReader]::new($stream,[Text.UTF8Encoding]::new($false),$true)
        return $reader.ReadToEnd()
    } finally {
        if($null -ne $reader){$reader.Dispose()} elseif($null -ne $stream){$stream.Dispose()}
    }
}
function Read-CpfLiveLogLines([string]$Path) {
    return @((Read-CpfLiveLogText $Path) -split "`r`n|`n|`r")
}
function Find-CorrelationInFiles([IO.FileInfo[]]$Files,[string]$TransactionId,[string]$TraceId) {
    # 1) $matches 는 PowerShell 자동변수다(-match 연산자 결과 Hashtable). 재사용하지 않는다.
    # 2) 반환은 @($list) 가 아니라 .ToArray() 를 쓴다. PowerShell 7.6 에서
    #    List[object] 에 array subexpression @() 를 적용하면
    #    "Argument types do not match"(System.ArgumentException)로 던진다(List[string]은 정상).
    #    실제로 상관관계 검증이 이 지점에서 끊겼다.
    $correlated = New-Object Collections.Generic.List[object]
    foreach ($file in $Files) {
        try {
            $content = Read-CpfLiveLogText $file.FullName
            if ($content.Contains($TransactionId) -and $content.Contains($TraceId)) {
                $matchedLines=@(Read-CpfLiveLogLines $file.FullName | Where-Object { $_.Contains($TransactionId) -and $_.Contains($TraceId) } | Select-Object -First 50)
                $relativePath=if($file.FullName.StartsWith($Root,[StringComparison]::OrdinalIgnoreCase)){$file.FullName.Substring($Root.Length).TrimStart('\','/')}else{$file.Name}
                [void]$correlated.Add([ordered]@{ path=$file.FullName; relativePath=$relativePath; sizeBytes=$file.Length; lines=$matchedLines })
            }
        } catch { }
    }
    return ,$correlated.ToArray()
}
function Test-RawSecretLeak([IO.FileInfo[]]$Files,[string[]]$Secrets) {
    $findings = New-Object Collections.Generic.List[object]
    foreach ($secret in $Secrets) {
        if ([string]::IsNullOrWhiteSpace($secret) -or $secret.Length -lt 6) { continue }
        foreach ($file in $Files) {
            try {
                $content = Read-CpfLiveLogText $file.FullName
                if ($content.Contains($secret)) {
                    [void]$findings.Add([ordered]@{ path=$file.FullName.Substring($Root.Length).TrimStart('\\','/'); secretType='raw-sensitive-value' })
                }
            } catch { }
        }
    }
    return ,$findings.ToArray()
}
function Test-ContainsSecret([string]$Text,[string[]]$Secrets) {
    foreach($secret in $Secrets){
        if(-not [string]::IsNullOrWhiteSpace($secret) -and $secret.Length -ge 6 -and $Text.Contains($secret)){ return $true }
    }
    return $false
}
function Write-SafeJsonEvidence([string]$Name,[object]$Value,[string[]]$Secrets) {
    $path=Join-Path $EvidenceRoot $Name
    $text=($Value | ConvertTo-Json -Depth 40) + "`n"
    if(Test-ContainsSecret $text $Secrets){ return [ordered]@{name=$Name;written=$false;reason='RAW_SECRET_DETECTED'} }
    [IO.File]::WriteAllText($path,$text,$Utf8NoBom)
    return [ordered]@{name=$Name;written=$true;sha256=(Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant();sizeBytes=(Get-Item -LiteralPath $path).Length}
}
function Write-SafeTextEvidence([string]$Name,[string[]]$Lines,[string[]]$Secrets) {
    $path=Join-Path $EvidenceRoot $Name
    $text=(@($Lines) -join [Environment]::NewLine) + [Environment]::NewLine
    if(Test-ContainsSecret $text $Secrets){ return [ordered]@{name=$Name;written=$false;reason='RAW_SECRET_DETECTED'} }
    [IO.File]::WriteAllText($path,$text,$Utf8NoBom)
    return [ordered]@{name=$Name;written=$true;sha256=(Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant();sizeBytes=(Get-Item -LiteralPath $path).Length}
}

function Save-Result([hashtable]$Result) {
    $Result.finishedAt = (Get-Date).ToString('o')
    [IO.File]::WriteAllText($ResultPath,($Result | ConvertTo-Json -Depth 40)+"`n",$Utf8NoBom)
}

$result = [ordered]@{
    startedAt=(Get-Date).ToString('o')
    status='FAIL'
    transactionId=$null
    traceId=$null
    transactionProbe=[ordered]@{ status='NOT_EXECUTED' }
    fileLog=[ordered]@{ status='NOT_EXECUTED'; matchCount=0; files=@() }
    dbLog=[ordered]@{ status='NOT_EXECUTED'; itemCount=0 }
    admTimeline=[ordered]@{ status='NOT_EXECUTED'; transactionLogCount=0; timelineSegmentCount=0 }
    recovery=[ordered]@{ status='NOT_EXECUTED'; pending=$null; quarantined=$null; terminalLoss=$null; alertState=$null }
    security=[ordered]@{ status='NOT_EXECUTED'; rawSecretLeakCount=0; fatalRuntimeMarkerCount=0 }
    fileLogDbCorrelation=[ordered]@{ status='NOT_EXECUTED'; fileLogMatches=0; dbLogMatches=0 }
    fileLogRecovery=[ordered]@{ status='NOT_EXECUTED'; pending=$null; quarantined=$null; terminalLoss=$null; writeFailureCount=$null }
    processRuntimeLog=[ordered]@{ status='NOT_EXECUTED'; fatalRuntimeMarkerCount=0 }
    secretLeakScan=[ordered]@{ status='NOT_EXECUTED'; rawSecretLeakCount=0 }
    upstreamEvidence=[ordered]@{}
    evidenceFiles=@()
}

# AdmPassword is process-environment only; it is never accepted as a command-line parameter.
$admPassword=[Environment]::GetEnvironmentVariable('CPF_ADM_SMOKE_PASSWORD','Process')
if ([string]::IsNullOrWhiteSpace($admPassword)) { $admPassword=[Environment]::GetEnvironmentVariable('CPF_ADMIN_PASSWORD','Process') }
if ([string]::IsNullOrWhiteSpace($admPassword)) { throw 'CPF_ADM_SMOKE_PASSWORD / CPF_ADMIN_PASSWORD is required in process environment.' }
$approvalProofKey=[Environment]::GetEnvironmentVariable('CPF_ADM_APPROVAL_PROOF_KEY_BASE64','Process')

try {
    $stamp=Get-Date -Format 'yyyyMMddHHmmssfff'
    # X-Original-System-Code 는 transactionId 에 박힌 issuer 와 같아야 하므로 대상 Runtime
    # System Code 를 issuer 로 쓴다(단독 EDU 실행에서는 종전과 동일).
    $transactionId="${stamp}$(Get-CpfIssuerCode $SystemCode)logcor10000001"
    $traceId=[guid]::NewGuid().ToString('N')
    $result.transactionId=$transactionId
    $result.traceId=$traceId
    $spanId=[guid]::NewGuid().ToString('N').Substring(0,16)
    $requestHeaders=@{
        'X-Transaction-Id'=$transactionId;
        'X-Original-System-Code'=(Get-CpfIssuerCode $SystemCode); 'X-System-Code'=$SystemCode; 'X-Caller-System-Code'=$SystemCode; 'X-Target-System-Code'=$SystemCode;
        'X-Target-Operation-Id'=$ProbeOperationId;
        'X-Trace-Id'=$traceId; 'traceparent'="00-$traceId-$spanId-01"; 'X-Correlation-Id'="integrated-log-$stamp"; 'X-Request-Type'='RUNTIME_VALIDATION';
        'X-Client-Version'='1.0.0'; 'X-User-Id'='runtime-validation'
    }
    # 대상 거래가 요구하는 추가 Header(Idempotency-Key 등)를 덮어쓴다.
    foreach ($entry in ($ProbeExtraHeaders -split ';')) {
        if ([string]::IsNullOrWhiteSpace($entry)) { continue }
        $pair=$entry.Split('=',2); $name=$pair[0].Trim()
        if ([string]::IsNullOrWhiteSpace($name)) { continue }
        $value=if ($pair.Count -eq 2) { $pair[1].Trim() } else { '' }
        if ([string]::IsNullOrWhiteSpace($value)) { $value=[guid]::NewGuid().ToString('N') }
        $requestHeaders[$name]=$value
    }
    $probe=Invoke-CpfRawJsonPost "$BaseUrl$ProbePath" $requestHeaders $ProbeBody ([bool]$AllowNonSuccessProbe)
    $actualTx=[string](Get-SafeProperty $probe 'transactionId' '')
    $actualTrace=[string](Get-SafeProperty $probe 'traceId' $traceId)
    if ($actualTx -ne $transactionId) { throw "transaction header propagation mismatch. expected=$transactionId actual=$actualTx" }
    if (-not [string]::IsNullOrWhiteSpace($actualTrace) -and $actualTrace -ne $traceId) { throw 'trace header propagation mismatch.' }
    $result.transactionProbe.status='PASS'

    $loginBody=@{operatorId=$AdmUsername;password=$admPassword;otpCode=$null} | ConvertTo-Json -Compress
    try {
        Initialize-CpfAdmCsrf $BaseUrl
        $loginHeaders=New-CpfAdmHeaders 'admAuthLogin'
        $loginHeaders['X-XSRF-TOKEN']=$script:admCsrfToken
        $login=Invoke-RestMethod -Method Post -Uri "$BaseUrl/adm/api/auth/login" -Headers $loginHeaders -WebSession $script:admWebSession -ContentType 'application/json' -Body $loginBody -TimeoutSec $TimeoutSec -ErrorAction Stop
    } catch {
        throw [InvalidOperationException]::new((Get-CpfStableHttpFailure 'POST' "$BaseUrl/adm/api/auth/login" $_.Exception),$_.Exception)
    }
    # BFF 계약상 내부 인증 토큰은 응답 Body 에 노출되지 않는다
    # (CpfBffCredentialResponseAdvice). 로그인 성공은 Session Cookie 발급으로 판정한다.
    $accessToken=[string](Get-SafeProperty $login 'accessToken' '')
    if (-not [string]::IsNullOrWhiteSpace($accessToken)) { throw 'ADM login leaked an internal credential in the body.' }
    $sessionCookie=$script:admWebSession.Cookies.GetCookies($BaseUrl) |
        Where-Object { $_.Name -ne 'XSRF-TOKEN' } | Select-Object -First 1
    if ($null -eq $sessionCookie) { throw 'ADM login did not establish a BFF session cookie.' }

    # ADM 최초 Bootstrap 계정은 비밀번호 변경이 강제된다. 변경 전에는 self-service 경로 외
    # 모든 ADM API 가 403 "비밀번호를 먼저 변경해야 합니다."(AdmApiAuthFilter)로 막히므로,
    # 관측 API 조회 전에 정본 self-service 경로로 1회 변경한다.
    $me=Invoke-CpfJsonGet "$BaseUrl/adm/api/auth/me" (New-CpfAdmHeaders 'admAuthMe')
    if ([bool](Get-SafeProperty $me 'passwordChangeRequired' $false)) {
        $rotatedPassword="$admPassword" + 'R7!'
        $rotateHeaders=New-CpfAdmHeaders 'admOperatorChangePassword'
        $rotateHeaders['X-XSRF-TOKEN']=Get-CpfAdmCsrfToken $BaseUrl
        $rotateBody=@{currentPassword=$admPassword;newPassword=$rotatedPassword;newPasswordConfirm=$rotatedPassword;reason='runtime-smoke mandatory password rotation'} | ConvertTo-Json -Compress
        Invoke-CpfRawJsonPost "$BaseUrl/adm/api/operators/$AdmUsername/password" $rotateHeaders $rotateBody $false | Out-Null
        $admPassword=$rotatedPassword
    }
    # 조회 호출도 System6 Header 를 갖춰야 한다. 호출마다 새 거래ID 를 쓰도록 함수로 만든다.
    function New-AdmAuthHeaders([string]$OperationId) {
        # BFF 계약상 Browser 채널은 Authorization Header 를 보내면 안 된다.
        # CpfBffSessionBridgeFilter 가 Authorization 이 있으면
        # 400 "Browser Authorization header is prohibited" 로 잘라낸다.
        # 인증은 로그인으로 발급된 HttpOnly Session Cookie($script:admWebSession)가 운반한다.
        return New-CpfAdmHeaders $OperationId
    }

    $deadline=(Get-Date).AddSeconds([Math]::Max(1,$PollSeconds))
    $dbResponse=$null; $obs=$null; $timeline=$null; $recovery=$null
    do {
        Start-Sleep -Milliseconds 750
        try { $dbResponse=Invoke-CpfJsonGet "$BaseUrl/adm/api/logs?transactionId=$transactionId&traceId=$traceId&limit=50" (New-AdmAuthHeaders 'admLogSearch') } catch { $dbResponse=$null }
        try { $obs=Invoke-CpfJsonGet "$BaseUrl/adm/api/observability/transactions/$transactionId?limit=50" (New-AdmAuthHeaders 'admObservabilityTransaction') } catch { $obs=$null }
        try { $timeline=Invoke-CpfJsonGet "$BaseUrl/adm/api/transaction-groups/$transactionId/timeline" (New-AdmAuthHeaders 'admTransactionGroupTimeline') } catch { $timeline=$null }
        try { $recovery=Invoke-CpfJsonGet "$BaseUrl/adm/api/observability/file-log-recovery" (New-AdmAuthHeaders 'admObservabilityFileLogRecovery') } catch { $recovery=$null }
        $dbCount=Get-ArrayCount $dbResponse 'items'
        $obsCount=Get-ArrayCount $obs 'transactionLogs'
        $timelineCount=Get-ArrayCount $timeline 'items'
        if ($dbCount -gt 0 -and $obsCount -gt 0) { break }
    } while((Get-Date) -lt $deadline)

    $logFiles=Find-TextFiles @($LogBasePath)
    $fileMatches=Find-CorrelationInFiles $logFiles $transactionId $traceId
    $result.fileLog.matchCount=$fileMatches.Count
    $fileEvidenceLines=@($fileMatches | ForEach-Object { @($_.lines) } | Select-Object -First 100)
    $fileEvidence=Write-SafeTextEvidence 'file-log-transaction.ndjson' $fileEvidenceLines @($admPassword,$accessToken)
    $result.evidenceFiles+=,$fileEvidence
    $result.fileLog.files=@($fileMatches | ForEach-Object { [ordered]@{relativePath=$_.relativePath;sizeBytes=$_.sizeBytes;matchingLineCount=@($_.lines).Count} })
    $result.fileLog.status=if($fileMatches.Count -gt 0 -and [bool]$fileEvidence.written){'PASS'}else{'FAIL'}

    $dbItems=@(Get-SafeProperty $dbResponse 'items' @())
    $result.dbLog.itemCount=$dbItems.Count
    $dbCorrelated=@($dbItems | Where-Object {
        ([string](Get-SafeProperty $_ 'transactionId' (Get-SafeProperty $_ 'TRANSACTION_ID' ''))) -eq $transactionId -and
        ([string](Get-SafeProperty $_ 'traceId' (Get-SafeProperty $_ 'TRACE_ID' ''))) -eq $traceId
    })
    $result.dbLog.correlatedCount=$dbCorrelated.Count
    $result.dbLog.status=if($dbCorrelated.Count -gt 0){'PASS'}else{'FAIL'}
    $result.fileLogDbCorrelation.fileLogMatches=$result.fileLog.matchCount
    $result.fileLogDbCorrelation.dbLogMatches=$result.dbLog.correlatedCount
    $result.fileLogDbCorrelation.status=if($result.fileLog.status -eq 'PASS' -and $result.dbLog.status -eq 'PASS'){'PASS'}else{'FAIL'}

    $result.admTimeline.transactionLogCount=Get-ArrayCount $obs 'transactionLogs'
    $result.admTimeline.timelineSegmentCount=Get-ArrayCount $timeline 'items'
    $obsText=if($null -ne $obs){$obs|ConvertTo-Json -Depth 30 -Compress}else{''}
    $timelineText=if($null -ne $timeline){$timeline|ConvertTo-Json -Depth 30 -Compress}else{''}
    $result.admTimeline.traceLinked=($obsText.Contains($traceId) -or $timelineText.Contains($traceId))
    $result.admTimeline.status=if($result.admTimeline.transactionLogCount -gt 0 -and $result.admTimeline.traceLinked){'PASS'}else{'FAIL'}

    $result.evidenceFiles+=,(Write-SafeJsonEvidence 'db-log-transaction.json' ([ordered]@{transactionId=$transactionId;traceId=$traceId;items=$dbCorrelated}) @($admPassword,$accessToken))
    $result.evidenceFiles+=,(Write-SafeJsonEvidence 'adm-observability-transaction.json' $obs @($admPassword,$accessToken))
    $result.evidenceFiles+=,(Write-SafeJsonEvidence 'adm-timeline-transaction.json' $timeline @($admPassword,$accessToken))

    $recoveryObj=Get-SafeProperty $recovery 'recovery' $null
    $result.recovery.pending=Get-SafeProperty $recoveryObj 'pending' $null
    $result.recovery.quarantined=Get-SafeProperty $recoveryObj 'quarantined' $null
    $result.recovery.terminalLoss=Get-SafeProperty $recoveryObj 'terminalLoss' $null
    $result.recovery.alertState=Get-SafeProperty $recovery 'alertState' $null
    $writeObj=Get-SafeProperty $recovery 'write' $null
    $writeFailureCount=Get-SafeProperty $writeObj 'writeFailureCount' $null
    $result.fileLogRecovery.pending=$result.recovery.pending
    $result.fileLogRecovery.quarantined=$result.recovery.quarantined
    $result.fileLogRecovery.terminalLoss=$result.recovery.terminalLoss
    $result.fileLogRecovery.writeFailureCount=$writeFailureCount
    [long]$terminal=0
    [long]$quarantine=0
    if($null -ne $result.recovery.terminalLoss){$terminal=[long]$result.recovery.terminalLoss}
    if($null -ne $result.recovery.quarantined){$quarantine=[long]$result.recovery.quarantined}
    $result.recovery.status=if($null -eq $recovery){'FAIL'}elseif($terminal -eq 0 -and $quarantine -eq 0){'PASS'}else{'FAIL'}
    $result.fileLogRecovery.status=$result.recovery.status

    $fileEvidence=Read-JsonIfPresent $FileLogResultPath
    $policyEvidence=Read-JsonIfPresent $LogPolicyResultPath
    $result.upstreamEvidence.fileLogResultPresent=$null -ne $fileEvidence
    $result.upstreamEvidence.logPolicyResultPresent=$null -ne $policyEvidence
    $upstreamError=($null -ne (Get-SafeProperty $fileEvidence 'error' $null)) -or ($null -ne (Get-SafeProperty $policyEvidence 'error' $null))
    $result.upstreamEvidence.status=if($null -ne $fileEvidence -and $null -ne $policyEvidence -and -not $upstreamError){'PASS'}else{'FAIL'}

    $securityFiles=Find-TextFiles @($LogBasePath,$RuntimeLogRoot,$FileLogResultPath,$LogPolicyResultPath)
    $leaks=Test-RawSecretLeak $securityFiles @($admPassword,$accessToken,$approvalProofKey)
    $result.security.rawSecretLeakCount=$leaks.Count
    $result.security.rawSecretLeakFiles=$leaks
    $fatalPatterns=@('APPLICATION FAILED TO START','OutOfMemoryError','BeanCreationException','StackOverflowError','FATAL EXCEPTION','Unhandled exception','TERMINAL_LOSS')
    $fatalFiles=New-Object Collections.Generic.List[string]
    foreach($file in (Find-TextFiles @($RuntimeLogRoot))){
        try {
            $text=Read-CpfLiveLogText $file.FullName
            if($fatalPatterns | Where-Object {$text.Contains($_)}){[void]$fatalFiles.Add($file.FullName.Substring($Root.Length).TrimStart('\\','/'))}
        }catch{}
    }
    $result.security.fatalRuntimeMarkerCount=$fatalFiles.Count
    $result.security.fatalRuntimeFiles=@($fatalFiles)
    $result.security.status=if($leaks.Count -eq 0 -and $fatalFiles.Count -eq 0){'PASS'}else{'FAIL'}
    $result.secretLeakScan.rawSecretLeakCount=$leaks.Count
    $result.secretLeakScan.status=if($leaks.Count -eq 0){'PASS'}else{'FAIL'}
    $result.processRuntimeLog.fatalRuntimeMarkerCount=$fatalFiles.Count
    $result.processRuntimeLog.status=if($fatalFiles.Count -eq 0){'PASS'}else{'FAIL'}
    if($leaks.Count -gt 0){$result.secretLeakScan.message='Raw credential/token found'}

    $result.evidenceFiles+=,(Write-SafeJsonEvidence 'masking-scan.json' ([ordered]@{transactionId=$transactionId;traceId=$traceId;rawSecretLeakCount=$leaks.Count;fatalRuntimeMarkerCount=$fatalFiles.Count;status=$result.security.status}) @())
    $result.evidenceFiles+=,(Write-SafeJsonEvidence 'correlation-matrix.json' ([ordered]@{transactionId=$transactionId;traceId=$traceId;fileLogMatches=$result.fileLog.matchCount;dbLogMatches=$result.dbLog.correlatedCount;admTransactionLogs=$result.admTimeline.transactionLogCount;admTimelineSegments=$result.admTimeline.timelineSegmentCount;recoveryStatus=$result.recovery.status;securityStatus=$result.security.status}) @())

    $requiredEvidenceMissing=@($result.evidenceFiles | Where-Object { -not [bool]$_.written }).Count
    $checks=@($result.transactionProbe.status,$result.fileLog.status,$result.dbLog.status,$result.admTimeline.status,$result.recovery.status,$result.security.status,$result.fileLogDbCorrelation.status,$result.fileLogRecovery.status,$result.processRuntimeLog.status,$result.secretLeakScan.status,$result.upstreamEvidence.status)
    if($requiredEvidenceMissing -gt 0){$checks+='FAIL'}
    $result.status=if(@($checks|Where-Object {$_ -ne 'PASS'}).Count -eq 0){'PASS'}else{'FAIL'}
    Save-Result $result
    if($result.status -ne 'PASS'){throw "integrated log correlation failed. result=$ResultPath"}
    Write-Host "Integrated log correlation PASS. transactionId=$transactionId result=$ResultPath"
} catch {
    $result.status='FAIL'
    $result.error=$_.Exception.Message
    # 원인을 다음 Runtime 주기로 미루지 않는다. 어느 지점에서 끊겼는지 증적에 남긴다.
    $result.errorType=$_.Exception.GetType().FullName
    $result.errorAt="$($_.InvocationInfo.ScriptName):$($_.InvocationInfo.ScriptLineNumber)"
    $result.errorLine=[string]$_.InvocationInfo.Line
    $result.errorStack=[string]$_.ScriptStackTrace
    Save-Result $result
    # Do not re-emit a localized PowerShell ErrorRecord. Emit a stable UTF-8/ASCII summary so
    # redirected Full Runtime logs cannot turn an OS-localized message into mojibake.
    throw [InvalidOperationException]::new("CPF_INTEGRATED_LOG_CORRELATION_FAIL result=$ResultPath type=$($_.Exception.GetType().FullName)",$_.Exception)
}
