param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $XyzBaseUrl = "http://localhost:8099",
    [string] $ResultDir = "",
    [string] $LogBasePath = "",
    [int] $TimeoutSec = 20,
    [switch] $RequireRuntime
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path

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
. (Join-Path $Root "scripts/runtime-diagnostics.ps1")
$resultPath = Join-Path $ResultDir "file-log-standard-result.json"
$grepPath = Join-Path $ResultDir "file-log-grep-summary.log"

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = $StatusPartial
    logBasePath = $LogBasePath
    runtimeProbe = [ordered]@{ status = $StatusNotVerified }
    transactionGlobalId = $null
    files = @()
    requiredFields = @("timestamp", "level", "logType", "eventType", "moduleCode", "transactionGlobalId", "serverId", "instanceId", "hostName", "hostIp", "processId", "profile", "appVersion", "buildVersion")
}

function Save-Result {
    $result.finishedAt = (Get-Date).ToString("o")
    [System.IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 30), $Utf8NoBom)
}

function New-SmokeHeaders {
    $timestamp = Get-Date -Format "yyyyMMddHHmmssfff"
    return @{
        "X-Transaction-Id" = "$timestamp" + "XYZ" + "flog001" + "0000001"
        "X-Trace-Id" = [guid]::NewGuid().ToString("N")
        "X-Request-Type" = "SMOKE"
        "X-Original-Channel-Code" = "XYZ"
        "X-Channel-Code" = "XYZ"
        "X-Client-App-Id" = "cpf-file-log-smoke"
        "X-Client-Version" = "1.0.0"
        "X-User-Id" = "runtime-smoke"
    }
}

function Read-LastLine {
    param([string] $Path)
    if (-not (Test-Path -LiteralPath $Path)) { return $null }
    $lines = [System.IO.File]::ReadAllLines($Path, [System.Text.Encoding]::UTF8)
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
        [string] $TransactionGlobalId,
        [bool] $Required
    )
    $moduleLower = $Module.ToLowerInvariant()
    $candidates = @(Get-ChildItem -LiteralPath $LogBasePath -Recurse -File -ErrorAction SilentlyContinue | Where-Object {
        $relative = $_.FullName.Substring($LogBasePath.Length).TrimStart('\', '/').Replace('\', '/')
        if ($LogType -eq 'transaction') {
            return $relative -match "^[^/]+/$moduleLower/[^/]+/transactions/[0-9]{8}/.+\.log$"
        }
        if ($LogType -eq 'batch') {
            return $relative -match "^[^/]+/$moduleLower/jobs/[0-9]{8}/[^/]+/.+\.log$"
        }
        return $relative -match "^[^/]+/$moduleLower/[^/]+/.+/cpf-[^-]+-$LogType-.+\.log$"
    })
    $path = $null
    if (-not [string]::IsNullOrWhiteSpace($TransactionGlobalId)) {
        $path = $candidates | Where-Object {
            [IO.File]::ReadAllText($_.FullName, [Text.Encoding]::UTF8).Contains($TransactionGlobalId)
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
        containsTransactionGlobalId = $false
        requiredFieldsPresent = @()
        missingFields = @()
        status = $StatusNotVerified
    }
    if (-not $item.exists) {
        $item.status = $(if ($Required) { $StatusFailed } else { $StatusNotVerified })
        return $item
    }
    $content = [System.IO.File]::ReadAllText($path.FullName, [System.Text.Encoding]::UTF8)
    $item.containsTransactionGlobalId = -not [string]::IsNullOrWhiteSpace($TransactionGlobalId) -and $content.Contains($TransactionGlobalId)
    $lastLine = @([System.IO.File]::ReadAllLines($path.FullName, [System.Text.Encoding]::UTF8) | Where-Object {
        [string]::IsNullOrWhiteSpace($TransactionGlobalId) -or $_.Contains($TransactionGlobalId)
    } | Select-Object -Last 1)
    $lastLine = if ($lastLine.Count -gt 0) { $lastLine[0] } else { Read-LastLine -Path $path.FullName }
    $json = $null
    if (-not [string]::IsNullOrWhiteSpace($lastLine)) {
        try { $json = $lastLine | ConvertFrom-Json } catch { $json = $null }
    }
    foreach ($field in $result.requiredFields) {
        if ($json -ne $null -and $null -ne $json.$field) {
            $item.requiredFieldsPresent += $field
        } else {
            $item.missingFields += $field
        }
    }
    if (($item.containsTransactionGlobalId -or -not $Required) -and $item.missingFields.Count -eq 0) {
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
        if (-not (Test-EndpointListening -BaseUrl $XyzBaseUrl)) {
            throw "XYZ runtime port is not listening. baseUrl=$XyzBaseUrl"
        }
        $response = Invoke-WebRequest `
            -Method Get `
            -Uri "$XyzBaseUrl/api/xyz/reference/query/headers" `
            -Headers (New-SmokeHeaders) `
            -TimeoutSec $TimeoutSec `
            -UseBasicParsing
        $body = $response.Content | ConvertFrom-Json
        $result.runtimeProbe.status = $StatusDone
        $result.runtimeProbe.httpStatus = [int] $response.StatusCode
        $transactionProperty = $body.PSObject.Properties['transactionGlobalId']
        $legacyProperty = $body.PSObject.Properties['transactionId']
        $result.transactionGlobalId = if ($null -ne $transactionProperty) {
            [string] $transactionProperty.Value
        } elseif ($null -ne $legacyProperty) {
            [string] $legacyProperty.Value
        } else {
            throw '헤더 조회 응답에 transactionGlobalId 또는 transactionId가 없습니다.'
        }
        Start-Sleep -Seconds 2
    } catch {
        $result.runtimeProbe.status = $StatusNotVerified
        $result.runtimeProbe.error = $_.Exception.Message
        $result.runtimeProbe.diagnostics = New-CpfRuntimeDiagnostic -Root $Root -Module "XYZ" -Ports @(8099) -ErrorMessage $_.Exception.Message
        if ($RequireRuntime) { throw }
    }

    $tx = [string] $result.transactionGlobalId
    $runtimeVerified = $result.runtimeProbe.status -eq $StatusDone
    $result.files += Test-LogFile -Module "XYZ" -LogType "transaction" -TransactionGlobalId $tx -Required $runtimeVerified
    $result.files += Test-LogFile -Module "XYZ" -LogType "integration" -TransactionGlobalId $tx -Required $false
    $result.files += Test-LogFile -Module "BAT" -LogType "batch" -TransactionGlobalId "" -Required $false

    $grepLines = New-Object System.Collections.Generic.List[string]
    $grepLines.Add("transactionGlobalId=$tx") | Out-Null
    foreach ($file in $result.files) {
        $grepLines.Add("$($file.status) $($file.path) containsTransactionGlobalId=$($file.containsTransactionGlobalId) missingFields=$($file.missingFields -join ',')") | Out-Null
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
        $result.runtimeProbe.diagnostics = New-CpfRuntimeDiagnostic -Root $Root -Module "XYZ" -Ports @(8099) -ErrorMessage $_.Exception.Message
    }
    Save-Result
    throw
}
