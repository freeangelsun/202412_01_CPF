param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = "",
    [string] $AdmBaseUrl = "http://127.0.0.1:8090",
    [string] $AdmUsername = "admin",
    [string] $AdmPassword = $env:CPF_ADM_SMOKE_PASSWORD,
    [switch] $RequireRuntime
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "runtime-common.ps1")
. (Join-Path $PSScriptRoot "runtime-diagnostics.ps1")

$Root = Get-CpfRuntimeRoot -Root $Root
$ResultDir = Get-CpfRuntimeResultDir -Root $Root -ResultDir $ResultDir
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$resultPath = Join-Path $ResultDir "adm-operation-console-runtime-result.json"
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = Get-CpfRuntimeStatusText "Partial"
    baseUrl = $AdmBaseUrl
    checkedEndpoints = @()
}

function Save-AdmOperationResult {
    $result.finishedAt = (Get-Date).ToString("o")
    Write-CpfRuntimeJson -Path $resultPath -Value $result
}

function Invoke-AdmJson {
    param(
        [string] $Method,
        [string] $Path,
        [hashtable] $Headers = @{},
        [object] $Body = $null
    )

    $params = @{
        Uri = "$AdmBaseUrl$Path"
        Method = $Method
        TimeoutSec = 10
        Headers = $Headers
        UseBasicParsing = $true
    }
    if ($Body -ne $null) {
        $params.ContentType = "application/json;charset=UTF-8"
        $params.Body = [System.Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Depth 20))
    }
    $response = Invoke-WebRequest @params
    if ([string]::IsNullOrWhiteSpace($response.Content)) {
        return $null
    }
    return $response.Content | ConvertFrom-Json
}

try {
    $portListening = Test-CpfRuntimeTcpPort -Port 8090
    $result.portListening = $portListening
    if (-not $portListening) {
        $result.status = $(if ($RequireRuntime) { Get-CpfRuntimeStatusText "Failed" } else { Get-CpfRuntimeStatusText "NotVerified" })
        $result.failureClassification = "environment"
        $result.diagnostics = New-CpfRuntimeDiagnostic -Root $Root -Module "ADM" -Ports @(8090) -ErrorMessage "ADM runtime port is not listening." -ResultDir $ResultDir
        Save-AdmOperationResult
        if ($RequireRuntime) {
            exit 1
        }
        return
    }

    if ([string]::IsNullOrWhiteSpace($AdmPassword)) {
        $AdmPassword = "Adm!n12345"
    }

    $health = Invoke-AdmJson -Method Get -Path "/adm/api/health"
    $result.health = $health
    $login = Invoke-AdmJson -Method Post -Path "/adm/api/auth/login" -Body @{
        operatorId = $AdmUsername
        password = $AdmPassword
    }
    if ([string]::IsNullOrWhiteSpace([string] $login.accessToken)) {
        throw "ADM login response does not contain accessToken."
    }
    $headers = @{ Authorization = "Bearer $($login.accessToken)" }

    $endpoints = @(
        "/adm/api/transaction-groups?limit=5",
        "/adm/api/log-policies?limit=5",
        "/adm/api/log-policies/runtime-state?limit=5",
        "/adm/api/log-policies/history?limit=5",
        "/adm/api/batch/jobs",
        "/adm/api/batch/schedules",
        "/adm/api/batch/relations",
        "/adm/api/batch/execution-targets?limit=5",
        "/adm/api/batch/workers",
        "/adm/api/batch/locks",
        "/adm/api/cache/summary",
        "/adm/api/messages",
        "/adm/api/codes",
        "/adm/api/configs"
    )
    $checked = New-Object System.Collections.Generic.List[object]
    foreach ($endpoint in $endpoints) {
        $body = Invoke-AdmJson -Method Get -Path $endpoint -Headers $headers
        $checked.Add([ordered]@{
            method = "GET"
            path = $endpoint
            itemCount = $(if ($body -is [System.Array]) { $body.Count } elseif ($body -ne $null -and $body.items -ne $null) { @($body.items).Count } else { $null })
        })
    }

    $result.checkedEndpoints = @($checked)
    $result.status = Get-CpfRuntimeStatusText "Done"
    Save-AdmOperationResult
} catch {
    $result.status = Get-CpfRuntimeStatusText "Failed"
    $result.failureClassification = Get-CpfRuntimeFailureClassification -Message $_.Exception.Message
    $result.error = $_.Exception.Message
    $result.diagnostics = New-CpfRuntimeDiagnostic -Root $Root -Module "ADM" -Ports @(8090) -ErrorMessage $_.Exception.Message -ResultDir $ResultDir
    Save-AdmOperationResult
    throw
}

Write-Host "ADM operation console runtime smoke finished. status=$($result.status) result=$resultPath"
