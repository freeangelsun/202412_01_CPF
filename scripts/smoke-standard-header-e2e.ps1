param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $TargetUrl = "http://localhost:8080/acc/tran/success?menuId=STANDARD_HEADER_E2E&execUser=runtime-smoke",
    [string] $ResultDir = "",
    [int] $TimeoutSec = 15,
    [switch] $RequireRuntime
)

$ErrorActionPreference = "Stop"

function New-UnicodeText {
    param([int[]] $CodePoints)

    return -join ($CodePoints | ForEach-Object { [char] $_ })
}

$StatusDone = New-UnicodeText @(0xC644, 0xB8CC)
$StatusPartial = New-UnicodeText @(0xBD80, 0xBD84, 0x20, 0xAD6C, 0xD604)
$StatusNotImplemented = New-UnicodeText @(0xBBF8, 0xAD6C, 0xD604)
$StatusNotVerified = New-UnicodeText @(0xBBF8, 0xAC80, 0xC99D)
$StatusFailed = New-UnicodeText @(0xC2E4, 0xD328)

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/runtime-smoke"
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "standard-header-e2e-result.json"

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = $StatusPartial
    targetUrl = $TargetUrl
    resultPath = $resultPath
    scope = "standard header Runtime E2E entrypoint"
    plannedFlow = @(
        "actual API call",
        "standard header receive",
        "required header validation",
        "X-Cpf-Ext-* extension header receive",
        "TransactionContext creation",
        "transaction log and header snapshot save",
        "outbound call",
        "mock downstream receive check",
        "ADM log lookup",
        "sensitive raw header suppression check"
    )
    headerScenario = [ordered]@{
        required = @(
            "X-Transaction-Id",
            "X-Request-Type",
            "X-Original-Channel-Code",
            "X-Channel-Code",
            "X-Trace-Id",
            "X-Client-App-Id",
            "X-User-Id",
            "X-Customer-No",
            "X-Member-No"
        )
        allowedExtension = @("X-Cpf-Ext-1", "X-Cpf-Ext-Campaign-Id", "X-Cpf-Ext-Partner-Code")
        blockedExtension = @("X-Cpf-Ext-Token", "X-Cpf-Ext-Api-Key", "X-Cpf-Ext-Authorization")
        sensitive = @("Authorization", "X-Api-Key")
    }
    probes = [ordered]@{}
    mockDownstream = [ordered]@{
        status = $StatusNotImplemented
        reason = "This script prepares the E2E entrypoint and result format. Real outbound target integration is a later milestone."
    }
    admLookup = [ordered]@{
        status = $StatusNotVerified
        reason = "This script does not run ADM log lookup yet."
    }
    sensitiveRawRecorded = $false
}

function Save-SmokeResult {
    $result.finishedAt = (Get-Date).ToString("o")
    $result | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $resultPath -Encoding UTF8
}

function New-StandardHeaders {
    param([switch] $IncludeBlockedExtension)

    $headers = [ordered]@{
        "X-Transaction-Id" = "20260702103000000ACClocal010000001"
        "X-Request-Type" = "ONLINE"
        "X-Original-Channel-Code" = "MOBILE"
        "X-Channel-Code" = "ACC"
        "X-Trace-Id" = "TRACE-STANDARD-HEADER-E2E"
        "X-Parent-Span-Id" = "SPAN-PARENT-E2E"
        "X-Client-App-Id" = "cpf-smoke-client"
        "X-Client-Version" = "1.0.0"
        "X-Caller-Service" = "cpf-standard-header-smoke"
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
        [int[]] $ExpectedStatusRange
    )

    $probe = [ordered]@{
        status = $StatusNotVerified
        requestHeaders = ConvertTo-SafeHeaders $Headers
        expectedStatus = $ExpectedStatusRange -join ","
    }

    try {
        $response = Invoke-WebRequest -Method Get -Uri $TargetUrl -Headers $Headers -TimeoutSec $TimeoutSec -UseBasicParsing
        $statusCode = [int] $response.StatusCode
        $probe.statusCode = $statusCode
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

try {
    $allowedHeaders = New-StandardHeaders
    $blockedHeaders = New-StandardHeaders -IncludeBlockedExtension

    $allowedProbe = Invoke-Probe -Name "allowedStandardAndExtensionHeaders" -Headers $allowedHeaders -ExpectedStatusRange @(200, 201, 202, 204)
    $blockedProbe = Invoke-Probe -Name "blockedExtensionHeaderRejected" -Headers $blockedHeaders -ExpectedStatusRange @(400, 401, 403, 422)

    if ($allowedProbe.status -eq $StatusDone -and $blockedProbe.status -eq $StatusDone) {
        $result.status = $StatusPartial
        $result.reason = "API call and blocked header response were checked, but outbound mock downstream receive and ADM log lookup are not connected yet."
    } else {
        $result.status = $StatusPartial
        $result.reason = "E2E entrypoint and result JSON format were prepared. Target app may be down or some runtime probes remain unverified."
    }

    Save-SmokeResult
    Write-Host "Standard header E2E entry smoke finished. status=$($result.status) result=$resultPath"
    if ($RequireRuntime -and ($allowedProbe.status -ne $StatusDone -or $blockedProbe.status -ne $StatusDone)) {
        throw "standard-header runtime probes did not complete. result=$resultPath"
    }
} catch {
    $result.status = $StatusFailed
    $result.error = $_.Exception.Message
    Save-SmokeResult
    Write-Error $result.error
    exit 1
}
