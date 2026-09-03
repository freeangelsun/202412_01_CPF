param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [Alias("ReferenceBaseUrl")]
    [string] $EducationBaseUrl = "http://localhost:8099",
    [string] $ResultDir = "",
    [string] $LogBasePath = "",
    [int] $TimeoutSec = 20,
    # CPF System6 계약상 X-System-Code / X-Target-System-Code 는 **수신 Runtime 의 System Code**
    # 와 같아야 한다(CpfHttpInboundContextAdapter.validateReceiverSystem). 단독 EDU 실행에서는
    # EDU 지만, 1-WAS 통합 Runtime 은 자신을 LOCAL 로 선언한다. 대상 Runtime 을 호출자가 알려준다.
    [string] $SystemCode = "EDU",
    [switch] $RequireRuntime,

    # --------------------------------------------------------------------------------------
    # Probe 대상 설정 — 자주 바뀌는 값은 여기 한 곳에 모은다.
    #
    # 이 검증기가 확인하는 것은 "표준 File Log 22개 필드"이지 특정 업무의 결과가 아니다.
    # 따라서 대상 Runtime 에 **실제로 존재하는** @CpfOnlineTransaction 이면 무엇이든 된다.
    # (LoggingAspect 는 @CpfOnlineTransaction 에만 걸리므로, 표준 Controller 호출로는
    #  transactions/ File Log 자체가 생성되지 않는다.)
    #
    # 기본값 : 단독 EDU Runtime(8099)
    # 1-WAS  : EDU 는 Local Module Catalog(CpfLocalRuntimeModules)에 없다. 호출자가
    #          MBW_AUTH_LOGIN 처럼 조립 안에 있는 거래를 지정한다.
    # --------------------------------------------------------------------------------------
    [string] $ProbePath = "/edu/online/member-processing",
    [string] $ProbeOperationId = "EDU_LOCAL_MEMBER_PROCESS",
    [string] $ProbeBody = '"runtime-log-probe"',
    # File Log 디렉터리의 모듈 세그먼트(<env>/<module>/<instance>/transactions/...). 1-WAS 는
    # 앱별이 아니라 Runtime 자신의 모듈 코드 하나로 남긴다(CpfLogPathPolicy.instanceRoot).
    [string] $ProbeModuleCode = "EDU",
    [int[]] $ProbeDiagnosticPorts = @(8099),
    # 로그인 실패처럼 업무 결과가 2xx 가 아니어도 표준 로그 필드는 동일하게 남는다.
    # 자격증명 없이 표준 필드만 검증할 때 켠다.
    [switch] $AllowNonSuccessProbe,
    # Idempotency-Key 처럼 대상 거래가 요구하는 추가 Header 를 "이름=값;이름=값" 으로 전달한다.
    # 값이 비어 있으면 새 GUID 를 채운다(매 실행 고유 Idempotency-Key 용).
    [string] $ProbeExtraHeaders = ''
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
$SmokeStartedUtc = [DateTime]::UtcNow

function New-UnicodeText { param([int[]] $CodePoints) return -join ($CodePoints | ForEach-Object { [char] $_ }) }
$StatusDone = New-UnicodeText @(0xC644, 0xB8CC)
$StatusPartial = New-UnicodeText @(0xBD80, 0xBD84, 0x20, 0xAD6C, 0xD604)
$StatusNotVerified = New-UnicodeText @(0xBBF8, 0xAC80, 0xC99D)
$StatusFailed = New-UnicodeText @(0xC2E4, 0xD328)

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/runtime-smoke"
} elseif (-not [IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
if ([string]::IsNullOrWhiteSpace($LogBasePath)) {
    $LogBasePath = Join-Path $Root "logs"
} elseif (-not [IO.Path]::IsPathRooted($LogBasePath)) {
    $LogBasePath = Join-Path $Root $LogBasePath
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
. (Join-Path $Root "cpf-tools/runtime/tools/runtime-diagnostics.ps1")
$resultPath = Join-Path $ResultDir "file-log-standard-result.json"
$grepPath = Join-Path $ResultDir "file-log-grep-summary.log"

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = $StatusPartial
    logBasePath = $LogBasePath
    runtimeProbe = [ordered]@{ status = $StatusNotVerified }
    transactionId = $null
    files = @()
    requiredFields = @("timestamp", "level", "logType", "eventType", "moduleCode", "transactionId", "traceId", "executionId", "segmentId", "originalSystemCode", "systemCode", "callerSystemCode", "targetSystemCode", "operationId", "serverId", "instanceId", "hostName", "hostIp", "processId", "profile", "appVersion", "buildVersion")
}

function Save-Result {
    $result.finishedAt = (Get-Date).ToString("o")
    [System.IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 30), $Utf8NoBom)
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
    $timestamp = Get-Date -Format "yyyyMMddHHmmssfff"
    $traceId = [guid]::NewGuid().ToString("N")
    $spanId = [guid]::NewGuid().ToString("N").Substring(0,16)
    $headers = @{
        # X-Original-System-Code 는 X-Transaction-Id 에 박힌 issuer(3자리)와 같아야 한다.
        "X-Transaction-Id" = "$timestamp" + (Get-CpfIssuerCode $SystemCode) + "flog001" + "0000001"
        "X-Original-System-Code" = (Get-CpfIssuerCode $SystemCode)
        "X-System-Code" = $SystemCode
        "X-Caller-System-Code" = $SystemCode
        "X-Target-System-Code" = $SystemCode
        "X-Target-Operation-Id" = $ProbeOperationId
        "traceparent" = "00-$traceId-$spanId-01"
        "X-Correlation-Id" = "file-log-$timestamp"
        "X-Request-Type" = "RUNTIME_VALIDATION"
        "X-Client-Id" = "cpf-file-log-runtime"
        "X-Client-Version" = "1.0.0"
        "X-User-Id" = "runtime-validation"
    }
    # 대상 거래가 요구하는 추가 Header(Idempotency-Key 등)를 덮어쓴다.
    foreach ($entry in ($ProbeExtraHeaders -split ';')) {
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

function Read-CpfLiveLogText {
    param([string] $Path)
    # 이 검증기는 1-WAS 가 살아 있는 동안 그 Runtime 이 지금도 쓰고 있는 File Log 를 읽는다.
    # Windows 에서 File Log Owner(CpfFileLogWriter)는 rolling 파일 핸들을 연 채 유지하는데
    # [IO.File]::ReadAllText/ReadAllLines/ReadLines 는 FileShare.Read 로만 열기 때문에
    # 쓰기 핸들이 살아 있으면 "다른 프로세스가 사용 중" 으로 던진다.
    # (같은 결함으로 Batch Two-Worker 검증이 업무 단정을 모두 통과한 뒤 이 지점에서만 실패했다.)
    # 파일 부재/권한 오류는 그대로 예외로 남긴다 — '잠김' 만 허용하고 증적 부재는 숨기지 않는다.
    $stream = $null; $reader = $null
    try {
        $stream = [IO.FileStream]::new($Path, [IO.FileMode]::Open, [IO.FileAccess]::Read,
            ([IO.FileShare]::ReadWrite -bor [IO.FileShare]::Delete))
        $reader = [IO.StreamReader]::new($stream, [Text.UTF8Encoding]::new($false), $true)
        return $reader.ReadToEnd()
    } finally {
        if ($null -ne $reader) { $reader.Dispose() } elseif ($null -ne $stream) { $stream.Dispose() }
    }
}

function Read-CpfLiveLogLines {
    param([string] $Path)
    return @((Read-CpfLiveLogText -Path $Path) -split "`r`n|`n|`r")
}

function Read-LastLine {
    param([string] $Path)
    if (-not (Test-Path -LiteralPath $Path)) { return $null }
    $lines = @(Read-CpfLiveLogLines -Path $Path)
    if ($lines.Count -eq 0) { return $null }
    return $lines[$lines.Count - 1]
}

function Test-EndpointListening {
    param([string] $BaseUrl)

    try {
        $uri = [System.Uri] $BaseUrl
        $port = if ($uri.Port -gt 0) { $uri.Port } elseif ($uri.Scheme -eq "https") { 443 } else { 80 }
        $client = [System.Net.Sockets.TcpClient]::new()
        try {
            $task = $client.ConnectAsync("127.0.0.1", $port)
            return $task.Wait(700) -and $client.Connected
        } finally {
            $client.Dispose()
        }
    } catch {
        return $false
    }
}

function Test-LogFile {
    param(
        [string] $Module,
        [string] $LogType,
        [string] $TransactionId,
        [bool] $Required
    )
    $moduleLower = $Module.ToLowerInvariant()
    $candidates = @(Get-ChildItem -LiteralPath $LogBasePath -Recurse -File -ErrorAction SilentlyContinue | Where-Object {
        if ($_.LastWriteTimeUtc -lt $SmokeStartedUtc.AddSeconds(-2)) { return $false }
        $relative = $_.FullName.Substring($LogBasePath.Length).TrimStart('\', '/').Replace('\', '/')
        if ($LogType -eq 'transaction') {
            return $relative -match "^[^/]+/$moduleLower/[^/]+/transactions/[0-9]{8}/.+\.log$"
        }
        if ($LogType -eq 'batch') {
            return $relative -match "^[^/]+/$moduleLower/jobs/[0-9]{8}/[^/]+/.+\.log$"
        }
        # 파일명은 CpfLogPathPolicy.generalLogPath 가 cpf-<owner>-<type>-<instance>.<date>.log 로 만든다.
        # owner 자체에 '-' 가 들어갈 수 있으므로(local-runtime) owner 를 그대로 대조한다.
        return $relative -match "^[^/]+/$moduleLower/[^/]+/.+/cpf-$moduleLower-$LogType-.+\.log$"
    })
    $path = $null
    if (-not [string]::IsNullOrWhiteSpace($TransactionId)) {
        $path = $candidates | Where-Object {
            (Read-CpfLiveLogText -Path $_.FullName).Contains($TransactionId)
        } | Select-Object -First 1
    }
    if ($null -eq $path) {
        $path = $candidates | Sort-Object LastWriteTime -Descending | Select-Object -First 1
    }
    $item = [ordered]@{
        moduleCode = $Module.ToUpperInvariant()
        logType = $LogType
        path = $(if ($null -ne $path) { $path.FullName.Substring($Root.Length).TrimStart('\', '/') } else { $null })
        candidateCount = $candidates.Count
        required = $Required
        exists = $null -ne $path
        containsTransactionId = $false
        requiredFieldsPresent = @()
        missingFields = @()
        status = $StatusNotVerified
    }
    if (-not $item.exists) {
        $item.status = $(if ($Required) { $StatusFailed } else { $StatusNotVerified })
        return $item
    }
    $content = Read-CpfLiveLogText -Path $path.FullName
    $item.containsTransactionId = -not [string]::IsNullOrWhiteSpace($TransactionId) -and $content.Contains($TransactionId)
    $lastLine = @(Read-CpfLiveLogLines -Path $path.FullName | Where-Object {
        [string]::IsNullOrWhiteSpace($TransactionId) -or $_.Contains($TransactionId)
    } | Select-Object -Last 1)
    $lastLine = if ($lastLine.Count -gt 0) { $lastLine[0] } else { Read-LastLine -Path $path.FullName }
    $json = $null
    if (-not [string]::IsNullOrWhiteSpace($lastLine)) {
        try { $json = $lastLine | ConvertFrom-Json } catch { $json = $null }
    }
    foreach ($field in $result.requiredFields) {
        if ($null -ne $json -and $null -ne $json.$field) {
            $item.requiredFieldsPresent += $field
        } else {
            $item.missingFields += $field
        }
    }
    if ($item.containsTransactionId -and -not [string]::IsNullOrWhiteSpace($lastLine)) {
        $evidenceName = "file-log-$($Module.ToLowerInvariant())-$LogType-transaction.ndjson"
        $evidencePath = Join-Path $ResultDir $evidenceName
        [IO.File]::WriteAllText($evidencePath, $lastLine + [Environment]::NewLine, $Utf8NoBom)
        $item.transactionEvidence = $evidenceName
    }
    if (($item.containsTransactionId -or -not $Required) -and $item.missingFields.Count -eq 0) {
        $item.status = $StatusDone
    } elseif (-not $Required) {
        $item.status = $StatusNotVerified
    } else {
        $item.status = $StatusFailed
    }
    return $item
}

try {
    try {
        if (-not (Test-EndpointListening -BaseUrl $EducationBaseUrl)) {
            throw "target runtime port is not listening. baseUrl=$EducationBaseUrl"
        }
        # 보낸 거래ID 를 그대로 보관한다. CPF Runtime 은 X-Transaction-Id 를 그대로 승계하므로
        # 업무 응답이 2xx 가 아니어도 File Log 를 찾을 추적 키는 확정된다.
        $probeHeaders = New-SmokeHeaders
        $sentTransactionId = [string] $probeHeaders['X-Transaction-Id']
        $probeArgs = @{
            Method = 'Post'
            Uri = "$EducationBaseUrl$ProbePath"
            Headers = $probeHeaders
            ContentType = 'application/json'
            Body = $ProbeBody
            TimeoutSec = $TimeoutSec
            UseBasicParsing = $true
        }
        # 자격증명 없이 표준 필드만 검증할 때는 4xx/5xx 를 예외로 만들지 않는다.
        if ($AllowNonSuccessProbe) { $probeArgs['SkipHttpErrorCheck'] = $true }
        $response = Invoke-WebRequest @probeArgs
        $result.runtimeProbe.status = $StatusDone
        $result.runtimeProbe.httpStatus = [int] $response.StatusCode
        $result.runtimeProbe.operationId = $ProbeOperationId
        $result.runtimeProbe.path = $ProbePath
        # 실패 원인을 다음 실행까지 미루지 않는다. 응답 본문/오류 Header 를 증적에 그대로 남긴다.
        $result.runtimeProbe.responseBody = [string] $response.Content
        $result.runtimeProbe.messageCode = [string] $response.Headers['X-Message-Code']
        # 업무 거절(4xx)은 표준 로그 필드 검증에 지장이 없지만, 5xx 는 거래가 정상 수행되지
        # 않았다는 뜻이므로 표준 File Log 증적으로 인정하지 않는다.
        if ([int] $response.StatusCode -ge 500) {
            throw "probe returned a server error. status=$([int]$response.StatusCode) body=$([string]$response.Content)"
        }
        $body = $null
        try { $body = $response.Content | ConvertFrom-Json } catch { $body = $null }
        $transactionProperty = if ($null -ne $body) { $body.PSObject.Properties['transactionId'] } else { $null }
        $result.transactionId = if ($null -ne $transactionProperty -and
                -not [string]::IsNullOrWhiteSpace([string] $transactionProperty.Value)) {
            [string] $transactionProperty.Value
        } elseif (-not [string]::IsNullOrWhiteSpace($sentTransactionId)) {
            $sentTransactionId
        } else {
            throw '거래 응답과 요청 Header 어디에도 transactionId가 없습니다.'
        }
        Start-Sleep -Seconds 2
    } catch {
        $result.runtimeProbe.status = $StatusNotVerified
        $result.runtimeProbe.error = $_.Exception.Message
        $result.runtimeProbe.diagnostics = New-CpfRuntimeDiagnostic -Root $Root -Module $ProbeModuleCode -Ports $ProbeDiagnosticPorts -ErrorMessage $_.Exception.Message
        if ($RequireRuntime) { throw }
    }

    $tx = [string] $result.transactionId
    $runtimeVerified = $result.runtimeProbe.status -eq $StatusDone
    $result.files += Test-LogFile -Module $ProbeModuleCode -LogType "transaction" -TransactionId $tx -Required $runtimeVerified
    $result.files += Test-LogFile -Module $ProbeModuleCode -LogType "integration" -TransactionId $tx -Required $false
    $result.files += Test-LogFile -Module "BAT" -LogType "batch" -TransactionId "" -Required $false

    $grepLines = New-Object System.Collections.Generic.List[string]
    $grepLines.Add("transactionId=$tx") | Out-Null
    foreach ($file in $result.files) {
        $grepLines.Add("$($file.status) $($file.path) containsTransactionId=$($file.containsTransactionId) missingFields=$($file.missingFields -join ',')") | Out-Null
    }
    [System.IO.File]::WriteAllLines($grepPath, $grepLines, $Utf8NoBom)

    $requiredFailures = @($result.files | Where-Object { $_.required -and $_.status -ne $StatusDone })
    $result.grepSummaryPath = $grepPath.Substring($Root.Length).TrimStart('\', '/')
    $result.status = $(if (-not $runtimeVerified) { $StatusNotVerified } elseif ($requiredFailures.Count -eq 0) { $StatusDone } else { $StatusFailed })
    Save-Result
    if ($result.status -eq $StatusFailed -and $RequireRuntime) {
        throw "file log standard smoke failed. result=$resultPath"
    }
    Write-Host "File log standard smoke finished. status=$($result.status) result=$resultPath"
} catch {
    $result.status = $StatusFailed
    $result.error = $_.Exception.Message
    $diagnosticsProperty = $result.runtimeProbe.PSObject.Properties["diagnostics"]
    if ($null -eq $diagnosticsProperty -or $null -eq $diagnosticsProperty.Value) {
        $result.runtimeProbe.diagnostics = New-CpfRuntimeDiagnostic -Root $Root -Module $ProbeModuleCode -Ports $ProbeDiagnosticPorts -ErrorMessage $_.Exception.Message
    }
    Save-Result
    throw
}
