param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $AppBaseUrl = "http://localhost:8099",
    [string] $TargetUrl = "",
    [int] $MockDownstreamPort = 19080,
    [string] $ResultDir = "",
    [int] $TimeoutSec = 15,
    [string] $DbHost = $env:CPF_DB_HOST,
    [string] $DbPort = $env:CPF_DB_PORT,
    [string] $DbUsername = $env:CPF_DB_ROOT_USERNAME,
    [string] $DbPassword = $env:CPF_DB_ROOT_PASSWORD,
    [string] $ClientPath = $env:CPF_MARIADB_CLI,
    [switch] $SkipMockDownstream,
    [switch] $SkipLogLookup,
    [switch] $RequireRuntime
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"

function New-UnicodeText {
    param([int[]] $CodePoints)

    return -join ($CodePoints | ForEach-Object { [char] $_ })
}

function Read-CpfLiveLogText {
    param([string] $Path)
    # 이 검증기는 mock downstream 이 살아 있는 동안 그 프로세스가 쓴 capture 파일을 읽는다.
    # Windows 에서 쓰기 핸들이 살아 있으면 [IO.File]::ReadAllText/ReadAllLines/ReadLines 는
    # FileShare.Read 로만 열기 때문에 "다른 프로세스가 사용 중" 으로 던진다.
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

$StatusDone = New-UnicodeText @(0xC644, 0xB8CC)
$StatusPartial = New-UnicodeText @(0xBD80, 0xBD84, 0x20, 0xAD6C, 0xD604)
$StatusNotImplemented = New-UnicodeText @(0xBBF8, 0xAD6C, 0xD604)
$StatusNotVerified = New-UnicodeText @(0xBBF8, 0xAC80, 0xC99D)
$StatusFailed = New-UnicodeText @(0xC2E4, 0xD328)

$TransactionTimestamp = (Get-Date).ToString("yyyyMMddHHmmssfff")
$TransactionId = "$TransactionTimestamp" + "REF" + "local01" + "0000001"
$TraceId = "TRACE-STANDARD-HEADER-E2E"

if ([string]::IsNullOrWhiteSpace($DbHost)) {
    $DbHost = "localhost"
}
if ([string]::IsNullOrWhiteSpace($DbPort)) {
    $DbPort = "3306"
}
if ([string]::IsNullOrWhiteSpace($DbUsername)) {
    $DbUsername = "root"
}
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/runtime-smoke"
}

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$ResultDir = (Resolve-Path -LiteralPath $ResultDir).Path
$resultPath = Join-Path $ResultDir "standard-header-e2e-result.json"
$mockCapturePath = Join-Path $ResultDir "standard-header-e2e-downstream.json"
$mockUrl = "http://127.0.0.1:$MockDownstreamPort/cpf-standard-header-e2e"
if ([string]::IsNullOrWhiteSpace($TargetUrl)) {
    $encodedMockUrl = [System.Uri]::EscapeDataString($mockUrl)
    $TargetUrl = "$AppBaseUrl/api/reference/headers/propagation?menuId=STANDARD_HEADER_E2E&execUser=runtime-smoke&mockUrl=$encodedMockUrl"
}

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = $StatusPartial
    targetUrl = $TargetUrl
    resultPath = $resultPath
    scope = "standard header Runtime E2E"
    plannedFlow = @(
        "actual API call",
        "standard header receive",
        "external ingress protected-header rejection / framework context generation",
        "custom extension header receive",
        "TransactionContext creation",
        "transaction log and header snapshot save",
        "outbound call",
        "mock downstream receive check",
        "log lookup",
        "sensitive raw header suppression check"
    )
    headerScenario = [ordered]@{
        externalIngress = @(
            "X-Request-Type",
            "X-Client-Id",
            "X-Client-Version"
        )
        protectedInternal = @(
            "X-Transaction-Id",
            "X-Original-System-Code",
            "X-System-Code",
            "X-Caller-System-Code",
            "X-Target-System-Code",
            "X-Target-Operation-Id"
        )
        allowedExtension = @("X-Cpf-Ext-1", "X-Cpf-Ext-Campaign-Id", "X-Cpf-Ext-Partner-Code")
        blockedExtension = @("X-Cpf-Ext-Token", "X-Cpf-Ext-Api-Key", "X-Cpf-Ext-Authorization")
        sensitive = @("Authorization", "X-Api-Key")
    }
    probes = [ordered]@{}
    mockDownstream = [ordered]@{
        status = $StatusNotVerified
        url = $mockUrl
        capturePath = $mockCapturePath
    }
    logLookup = [ordered]@{
        status = $StatusNotVerified
        transactionId = $TransactionId
        traceId = $TraceId
    }
    admLookup = [ordered]@{
        status = $StatusNotVerified
        reason = "This smoke validates the same log evidence directly from cpfDB. ADM runtime API can be added as a separate smoke."
    }
    sensitiveRawRecorded = $false
}

function Save-SmokeResult {
    $result.finishedAt = (Get-Date).ToString("o")
    $result | ConvertTo-Json -Depth 30 | Set-Content -LiteralPath $resultPath -Encoding UTF8
}

function Join-ProcessArguments {
    param([string[]] $Arguments)

    return ($Arguments | ForEach-Object {
        if ($_ -match '[\s"]') {
            '"' + ($_.Replace('\', '\\').Replace('"', '\"')) + '"'
        } else {
            $_
        }
    }) -join " "
}

function ConvertTo-SafeMessage {
    param([string] $Message)

    if ([string]::IsNullOrWhiteSpace($Message)) {
        return $Message
    }
    if (-not [string]::IsNullOrWhiteSpace($DbPassword)) {
        return $Message.Replace($DbPassword, "****")
    }
    return $Message
}

function Find-MariaDbClient {
    if (-not [string]::IsNullOrWhiteSpace($ClientPath)) {
        if (Test-Path -LiteralPath $ClientPath) {
            return (Resolve-Path -LiteralPath $ClientPath).Path
        }
        $command = Get-Command $ClientPath -ErrorAction SilentlyContinue
        if ($null -ne $command) {
            return $command.Source
        }
    }

    foreach ($name in @("mariadb", "mysql")) {
        $command = Get-Command $name -ErrorAction SilentlyContinue
        if ($null -ne $command) {
            return $command.Source
        }
    }

    foreach ($installRoot in @("C:\Program Files", "C:\Program Files (x86)")) {
        if (-not (Test-Path -LiteralPath $installRoot)) {
            continue
        }
        foreach ($productFilter in @("MariaDB*", "MySQL*")) {
            $productDirs = @(Get-ChildItem -LiteralPath $installRoot -Directory -Filter $productFilter -ErrorAction SilentlyContinue | Sort-Object FullName)
            foreach ($productDir in $productDirs) {
                foreach ($clientName in @("mariadb.exe", "mysql.exe")) {
                    $candidate = Join-Path $productDir.FullName "bin\$clientName"
                    if (Test-Path -LiteralPath $candidate) {
                        return (Resolve-Path -LiteralPath $candidate).Path
                    }
                }
            }
        }
    }

    return $null
}

function Invoke-MariaDbQuery {
    param([string] $SqlText)

    $client = Find-MariaDbClient
    if ([string]::IsNullOrWhiteSpace($client)) {
        throw "MariaDB CLI was not found. Configure PATH or CPF_MARIADB_CLI."
    }
    if ([string]::IsNullOrWhiteSpace($DbPassword)) {
        throw "CPF_DB_ROOT_PASSWORD or -DbPassword was not provided."
    }

    $arguments = @(
        "--protocol=tcp",
        "-h",
        $DbHost,
        "-P",
        $DbPort,
        "-u",
        $DbUsername,
        "--ssl=0",
        "--default-character-set=utf8mb4",
        "--batch",
        "--raw",
        "--skip-column-names",
        "--execute=$SqlText"
    )

    $psi = [System.Diagnostics.ProcessStartInfo]::new()
    $psi.FileName = $client
    $psi.Arguments = Join-ProcessArguments $arguments
    $psi.UseShellExecute = $false
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.StandardOutputEncoding = [System.Text.Encoding]::UTF8
    $psi.StandardErrorEncoding = [System.Text.Encoding]::UTF8
    $psi.EnvironmentVariables["MYSQL_PWD"] = $DbPassword
    $psi.EnvironmentVariables["MARIADB_PWD"] = $DbPassword
    if (-not [string]::IsNullOrWhiteSpace($DbPassword) -and $psi.Arguments.Contains($DbPassword)) {
        throw "MariaDB process arguments contain a raw DB password."
    }

    $process = [System.Diagnostics.Process]::new()
    $process.StartInfo = $psi
    [void] $process.Start()
    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit()

    if ($process.ExitCode -ne 0) {
        throw "MariaDB query failed. exitCode=$($process.ExitCode) error=$(ConvertTo-SafeMessage $stderr)"
    }
    return $stdout
}

function New-StandardHeaders {
    param([switch] $IncludeBlockedExtension)

    $headers = [ordered]@{
        "X-Request-Type" = "ONLINE"
        "X-Client-Id" = "cpf-smoke-client"
        "X-Client-Version" = "1.0.0"
        "X-Country-Code" = "KR"
        "X-User-Id" = "smoke-user"
        "X-Customer-No" = "CUST-SMOKE"
        "X-Member-No" = "MBR-SMOKE"
        "X-Cpf-Ext-1" = "reserved-one"
        "X-Cpf-Ext-Campaign-Id" = "CMP-SMOKE"
        "X-Cpf-Ext-Partner-Code" = "PARTNER-SMOKE"
        "Authorization" = ("Bearer " + "STANDARD_HEADER_E2E_" + "REDACTED")
        "X-Api-Key" = ("STANDARD_HEADER_E2E_" + "REDACTED_KEY")
    }

    if ($IncludeBlockedExtension) {
        $headers["X-Cpf-Ext-Token"] = ("STANDARD_HEADER_E2E_" + "BLOCKED_REDACTED")
    }
    return $headers
}

function ConvertTo-SafeHeaders {
    param([System.Collections.IDictionary] $Headers)

    $safe = [ordered]@{}
    foreach ($key in $Headers.Keys) {
        if ($key -in @("Authorization", "X-Api-Key") -or $key -like "X-Cpf-Ext-*Token*" -or $key -like "X-Cpf-Ext-*Api-Key*" -or $key -like "X-Cpf-Ext-*Authorization*") {
            $safe[$key] = "****"
        } else {
            $safe[$key] = $Headers[$key]
        }
    }
    return $safe
}

function Invoke-Probe {
    param(
        [string] $Name,
        [System.Collections.IDictionary] $Headers,
        [int[]] $ExpectedStatusRange,
        [string] $Uri = $TargetUrl
    )

    $probe = [ordered]@{
        status = $StatusNotVerified
        requestHeaders = ConvertTo-SafeHeaders $Headers
        expectedStatus = $ExpectedStatusRange -join ","
    }

    try {
        $response = Invoke-WebRequest -Method Get -Uri $Uri -Headers $Headers -TimeoutSec $TimeoutSec -UseBasicParsing
        $statusCode = [int] $response.StatusCode
        $probe.statusCode = $statusCode
        $probe.body = $response.Content
        if ($ExpectedStatusRange -contains $statusCode) {
            $probe.status = $StatusDone
        } else {
            $probe.status = $StatusFailed
            $probe.error = "Unexpected status code. actual=$statusCode"
        }
    } catch {
        $statusCode = 0
        if ($_.Exception.Response -and $_.Exception.Response.StatusCode) {
            $statusCode = [int] $_.Exception.Response.StatusCode
        }
        $probe.statusCode = $statusCode
        if ($ExpectedStatusRange -contains $statusCode) {
            $probe.status = $StatusDone
        } else {
            $probe.status = $StatusNotVerified
            $probe.error = $_.Exception.Message
        }
    }

    $result.probes[$Name] = $probe
    return $probe
}

function Start-MockDownstream {
    param(
        [int] $Port,
        [string] $CapturePath
    )

    Remove-Item -LiteralPath $CapturePath -ErrorAction SilentlyContinue
    return Start-Job -ArgumentList $Port, $CapturePath -ScriptBlock {
        param($Port, $CapturePath)

        $listener = $null
        $client = $null
        try {
            $listener = [System.Net.Sockets.TcpListener]::new([System.Net.IPAddress]::Parse("127.0.0.1"), $Port)
            $listener.Start()
            $client = $listener.AcceptTcpClient()
            $stream = $client.GetStream()
            $reader = [System.IO.StreamReader]::new($stream, [System.Text.Encoding]::ASCII)
            $requestLine = $reader.ReadLine()
            $headers = [ordered]@{}
            while ($true) {
                $line = $reader.ReadLine()
                if ($null -eq $line -or $line.Length -eq 0) {
                    break
                }
                $index = $line.IndexOf(":")
                if ($index -gt 0) {
                    $headers[$line.Substring(0, $index).Trim()] = $line.Substring($index + 1).Trim()
                }
            }

            $capture = [ordered]@{
                receivedAt = (Get-Date).ToString("o")
                requestLine = $requestLine
                headers = $headers
            }
            $capture | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $CapturePath -Encoding UTF8

            $body = '{"received":true}'
            $bodyBytes = [System.Text.Encoding]::UTF8.GetBytes($body)
            $responseHeader = "HTTP/1.1 200 OK`r`nContent-Type: application/json`r`nContent-Length: $($bodyBytes.Length)`r`nConnection: close`r`n`r`n"
            $responseBytes = [System.Text.Encoding]::ASCII.GetBytes($responseHeader)
            $stream.Write($responseBytes, 0, $responseBytes.Length)
            $stream.Write($bodyBytes, 0, $bodyBytes.Length)
            $stream.Flush()
        } finally {
            if ($client -ne $null) {
                $client.Close()
            }
            if ($listener -ne $null) {
                $listener.Stop()
            }
        }
    }
}

function Get-HeaderValue {
    param(
        [object] $Headers,
        [string] $Name
    )

    foreach ($property in $Headers.PSObject.Properties) {
        if ($property.Name.Equals($Name, [System.StringComparison]::OrdinalIgnoreCase)) {
            return [string] $property.Value
        }
    }
    return $null
}

function Test-MockDownstreamCapture {
    if (-not (Test-Path -LiteralPath $mockCapturePath)) {
        $result.mockDownstream.status = $StatusNotVerified
        $result.mockDownstream.reason = "mock downstream capture file was not created."
        return
    }

    $captureText = Read-CpfLiveLogText -Path $mockCapturePath
    $capture = $captureText | ConvertFrom-Json
    $result.mockDownstream.capture = $capture
    $missing = @()
    foreach ($name in @(
            "X-Cpf-Ext-1",
            "X-Cpf-Ext-Campaign-Id",
            "X-Cpf-Ext-Partner-Code")) {
        if (-not (Get-HeaderValue $capture.headers $name)) {
            $missing += $name
        }
    }

    $forbidden = @()
    foreach ($name in @(
            "Authorization", "X-Api-Key", "X-Cpf-Ext-Token",
            "X-Transaction-Id", "X-Original-System-Code", "X-System-Code",
            "X-Caller-System-Code", "X-Target-System-Code", "X-Target-Operation-Id")) {
        if (Get-HeaderValue $capture.headers $name) {
            $forbidden += $name
        }
    }

    $result.mockDownstream.missingHeaders = $missing
    $result.mockDownstream.forbiddenHeaders = $forbidden
    if ($missing.Count -eq 0 -and $forbidden.Count -eq 0) {
        $result.mockDownstream.status = $StatusDone
    } else {
        $result.mockDownstream.status = $StatusFailed
    }
}

function Test-LogLookup {
    if ($SkipLogLookup) {
        $result.logLookup.status = $StatusNotVerified
        $result.logLookup.reason = "log lookup skipped by option."
        return
    }

    try {
        $logIdxOutput = Invoke-MariaDbQuery "SELECT LOG_IDX FROM cpfDB.cpf_transaction_log WHERE TRANSACTION_ID='$TransactionId' AND TRACE_ID='$TraceId' ORDER BY LOG_IDX DESC LIMIT 1;"
        $logIdx = (($logIdxOutput -split "`r?`n") | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } | Select-Object -First 1)
        if ([string]::IsNullOrWhiteSpace($logIdx)) {
            $result.logLookup.status = $StatusNotVerified
            $result.logLookup.reason = "transaction log was not found."
            return
        }

        $detailOutput = Invoke-MariaDbQuery "SELECT DETAIL_KEY, DETAIL_VALUE FROM cpfDB.cpf_transaction_log_detail WHERE LOG_IDX=$logIdx ORDER BY DETAIL_KEY;"
        $details = [ordered]@{}
        foreach ($line in ($detailOutput -split "`r?`n")) {
            if ([string]::IsNullOrWhiteSpace($line)) {
                continue
            }
            $parts = $line -split "`t", 2
            if ($parts.Count -eq 2) {
                $details[$parts[0]] = $parts[1]
            }
        }

        $requiredDetails = @("inboundHeaders", "resolvedHeaders", "outboundHeaders", "responseHeaders")
        $missingDetails = @($requiredDetails | Where-Object { -not $details.Contains($_) })
        $combinedDetailText = ($details.Values -join "`n")
        $containsExtension = $combinedDetailText.IndexOf(
                "X-Cpf-Ext-Campaign-Id",
                [System.StringComparison]::OrdinalIgnoreCase) -ge 0
        $containsSensitiveRaw = $combinedDetailText.Contains("STANDARD_HEADER_E2E_REDACTED") -or
                $combinedDetailText.Contains("STANDARD_HEADER_E2E_REDACTED_KEY") -or
                $combinedDetailText.Contains("STANDARD_HEADER_E2E_BLOCKED_REDACTED")

        $result.logLookup.logIdx = $logIdx
        $result.logLookup.detailKeys = @($details.Keys)
        $result.logLookup.missingDetails = $missingDetails
        $result.logLookup.containsAllowedExtensionHeader = $containsExtension
        $result.logLookup.containsSensitiveRaw = $containsSensitiveRaw
        if ($missingDetails.Count -eq 0 -and $containsExtension -and -not $containsSensitiveRaw) {
            $result.logLookup.status = $StatusDone
            $result.admLookup.status = $StatusDone
            $result.admLookup.reason = "cpfDB log lookup verified the evidence required for ADM log detail."
        } else {
            $result.logLookup.status = $StatusFailed
        }
        if ($containsSensitiveRaw) {
            $result.sensitiveRawRecorded = $true
        }
    } catch {
        $result.logLookup.status = $StatusNotVerified
        $result.logLookup.reason = ConvertTo-SafeMessage $_.Exception.Message
    }
}

$mockJob = $null
try {
    $allowedHeaders = New-StandardHeaders
    $blockedHeaders = New-StandardHeaders -IncludeBlockedExtension

    if (-not $SkipMockDownstream) {
        $mockJob = Start-MockDownstream -Port $MockDownstreamPort -CapturePath $mockCapturePath
        Start-Sleep -Milliseconds 500
    } else {
        $result.mockDownstream.status = $StatusNotVerified
        $result.mockDownstream.reason = "mock downstream skipped by option."
    }

    $allowedProbe = Invoke-Probe -Name "allowedStandardAndExtensionHeaders" -Headers $allowedHeaders -ExpectedStatusRange @(200, 201, 202, 204)
    if ($mockJob -ne $null) {
        [void] (Wait-Job -Job $mockJob -Timeout 10)
        Receive-Job -Job $mockJob -ErrorAction SilentlyContinue | Out-Null
        Remove-Job -Job $mockJob -Force -ErrorAction SilentlyContinue
        $mockJob = $null
        Test-MockDownstreamCapture
    }

    $blockedUrl = "$AppBaseUrl/api/reference/headers/propagation?menuId=STANDARD_HEADER_E2E_BLOCKED&execUser=runtime-smoke&mockUrl=$([System.Uri]::EscapeDataString($mockUrl))"
    $blockedProbe = Invoke-Probe -Name "blockedExtensionHeaderRejected" -Headers $blockedHeaders -ExpectedStatusRange @(400, 401, 403, 422) -Uri $blockedUrl

    Start-Sleep -Milliseconds 500
    Test-LogLookup

    if ($allowedProbe.status -eq $StatusDone -and
            $blockedProbe.status -eq $StatusDone -and
            $result.mockDownstream.status -eq $StatusDone -and
            $result.logLookup.status -eq $StatusDone -and
            -not $result.sensitiveRawRecorded) {
        $result.status = $StatusDone
        $result.reason = "Runtime E2E completed: API call, blocked header rejection, downstream propagation, log lookup, and sensitive raw suppression were verified."
    } else {
        $result.status = $StatusPartial
        $result.reason = "Runtime E2E was executed but one or more verification steps remain incomplete."
    }

    Save-SmokeResult
    Write-Host "Standard header E2E smoke finished. status=$($result.status) result=$resultPath"
    if ($RequireRuntime -and $result.status -ne $StatusDone) {
        throw "standard-header runtime E2E did not complete. result=$resultPath"
    }
} catch {
    $result.status = $StatusFailed
    $result.error = ConvertTo-SafeMessage $_.Exception.Message
    Save-SmokeResult
    Write-Error $result.error
    exit 1
} finally {
    if ($mockJob -ne $null) {
        Remove-Job -Job $mockJob -Force -ErrorAction SilentlyContinue
    }
}
