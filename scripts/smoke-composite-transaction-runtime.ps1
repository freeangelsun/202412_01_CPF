param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $AccBaseUrl = "http://localhost:8080",
    [string] $ResultDir = "",
    [int] $MemberId = 1,
    [int] $TimeoutSec = 20
)

$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/runtime-smoke"
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "composite-transaction-runtime-result.json"

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    accBaseUrl = $AccBaseUrl
    memberId = $MemberId
    probes = [ordered]@{}
}

function Save-SmokeResult {
    $result.finishedAt = (Get-Date).ToString("o")
    $result | ConvertTo-Json -Depth 30 | Set-Content -LiteralPath $resultPath -Encoding UTF8
}

function New-CpfSmokeHeaders {
    return @{
        "X-Request-Type" = "ONLINE"
        "X-Original-Channel-Code" = "SMOKE"
        "X-Channel-Code" = "ACC"
        "X-Client-App-Id" = "cpf-composite-smoke"
        "X-Client-Version" = "1.0.0"
        "X-User-Id" = "runtime-smoke"
        "X-Customer-No" = "CUST-SMOKE-001"
        "X-Member-No" = "MBR-SMOKE-001"
    }
}

function Invoke-Json {
    param([string] $Method, [string] $Uri)
    $response = Invoke-WebRequest -Method $Method -Uri $Uri -Headers (New-CpfSmokeHeaders) -TimeoutSec $TimeoutSec -UseBasicParsing
    if ([string]::IsNullOrWhiteSpace($response.Content)) {
        return $null
    }
    return $response.Content | ConvertFrom-Json
}

function Assert-Condition {
    param([bool] $Condition, [string] $Message)
    if (-not $Condition) {
        throw $Message
    }
}

try {
    $uri = "$AccBaseUrl/acc/edu/composite/member-then-external?memberId=$MemberId"
    $body = Invoke-Json -Method Post -Uri $uri
    $result.probes.accComposite = [ordered]@{
        uri = $uri
        transactionGlobalId = $body.transactionGlobalId
        segmentCount = $body.segmentCount
        moduleFlowText = $body.moduleFlowText
        overallStatus = $body.overallStatus
    }

    Assert-Condition (-not [string]::IsNullOrWhiteSpace($body.transactionGlobalId)) "transactionGlobalId is empty"
    Assert-Condition ($body.segmentCount -ge 3) "segmentCount must be at least 3"
    Assert-Condition ($body.moduleFlowText -match "ACC") "moduleFlowText must contain ACC"
    Assert-Condition ($body.moduleFlowText -match "MBR") "moduleFlowText must contain MBR"
    Assert-Condition ($body.moduleFlowText -match "EXS") "moduleFlowText must contain EXS"
    Assert-Condition ($body.overallStatus -eq "SUCCESS") "overallStatus must be SUCCESS"

    $segmentIds = @($body.segmentIds)
    Assert-Condition ($segmentIds.Count -ge 3) "segmentIds must contain at least 3 ids"
    Assert-Condition (($segmentIds | Select-Object -Unique).Count -eq $segmentIds.Count) "transactionSegmentId values must be unique"

    $result.status = "완료"
    $result.transactionGlobalId = $body.transactionGlobalId
    Save-SmokeResult
    Write-Host "Composite transaction runtime smoke passed. Result: $resultPath"
} catch {
    $result.status = "실패"
    $result.error = $_.Exception.Message
    Save-SmokeResult
    throw
}
