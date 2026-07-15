param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = "",
    [string] $BaseUrl = "http://127.0.0.1:8099"
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "runtime-common.ps1")

$Root = Get-CpfRuntimeRoot -Root $Root
$ResultDir = Get-CpfRuntimeResultDir -Root $Root -ResultDir $ResultDir
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
Add-Type -AssemblyName System.Net.Http

$resultPath = Join-Path $ResultDir "attachment-edu-runtime-result.json"
$result = [ordered]@{
    startedAt = [DateTimeOffset]::Now.ToString("o")
    status = Get-CpfRuntimeStatusText "Partial"
    baseUrl = $BaseUrl
}

function New-CpfAttachmentTransactionId {
    param([int] $Sequence)

    return "$(Get-Date -Format 'yyyyMMddHHmmssfff')XYZlocal01$($Sequence.ToString('0000000'))"
}

function Invoke-CpfAttachmentJson {
    param(
        [ValidateSet("POST")]
        [string] $Method,
        [string] $Uri,
        [string] $TransactionId,
        [string] $ExecutionId,
        [object] $Body
    )

    $client = [System.Net.Http.HttpClient]::new()
    $request = [System.Net.Http.HttpRequestMessage]::new(
        [System.Net.Http.HttpMethod]::$Method,
        $Uri)
    try {
        $headers = [ordered]@{
            "X-Transaction-Id" = $TransactionId
            "X-Cpf-Standard-Execution-Id" = $ExecutionId
            "X-Request-Type" = "INQUIRY"
            "X-Original-Channel-Code" = "EDU"
            "X-Channel-Code" = "EDU"
            "X-Client-App-Id" = "cpf-attachment-runtime-smoke"
            "X-Client-Version" = "1.0.0"
        }
        foreach ($header in $headers.GetEnumerator()) {
            [void] $request.Headers.TryAddWithoutValidation($header.Key, $header.Value)
        }
        $json = $Body | ConvertTo-Json -Depth 10 -Compress
        $request.Content = [System.Net.Http.StringContent]::new(
            $json,
            [System.Text.Encoding]::UTF8,
            "application/json")
        $response = $client.SendAsync($request).GetAwaiter().GetResult()
        $bytes = $response.Content.ReadAsByteArrayAsync().GetAwaiter().GetResult()
        $content = [System.Text.Encoding]::UTF8.GetString($bytes)
        if (-not $response.IsSuccessStatusCode) {
            throw "첨부 EDU API 호출 실패: status=$([int] $response.StatusCode), uri=$Uri, body=$content"
        }
        return [ordered]@{
            statusCode = [int] $response.StatusCode
            body = $content | ConvertFrom-Json
        }
    } finally {
        $request.Dispose()
        $client.Dispose()
    }
}

try {
    $store = Invoke-CpfAttachmentJson `
        -Method POST `
        -Uri "$BaseUrl/xyz/edu/attachments/text" `
        -TransactionId (New-CpfAttachmentTransactionId -Sequence 21) `
        -ExecutionId "OXYZ-EDU-17-0001" `
        -Body ([ordered]@{
            groupId = "EDU"
            fileName = "cpf-attachment-runtime.txt"
            text = "CPF 첨부 EDU 런타임 검증"
        })

    $verify = Invoke-CpfAttachmentJson `
        -Method POST `
        -Uri "$BaseUrl/xyz/edu/attachments/verify" `
        -TransactionId (New-CpfAttachmentTransactionId -Sequence 22) `
        -ExecutionId "OXYZ-EDU-17-0002" `
        -Body ([ordered]@{
            storageKey = $store.body.storageKey
            expectedChecksumSha256 = $store.body.checksumSha256
        })

    if (-not $verify.body.checksumMatched) {
        throw "첨부파일 재조회 checksum이 저장 결과와 일치하지 않습니다."
    }
    if ([long] $store.body.fileSize -ne [long] $verify.body.fileSize) {
        throw "첨부파일 재조회 크기가 저장 결과와 일치하지 않습니다."
    }

    $result.status = Get-CpfRuntimeStatusText "Done"
    $result.store = [ordered]@{
        statusCode = $store.statusCode
        storageKey = $store.body.storageKey
        originalFileName = $store.body.originalFileName
        contentType = $store.body.contentType
        fileSize = $store.body.fileSize
        checksumSha256 = $store.body.checksumSha256
    }
    $result.verify = [ordered]@{
        statusCode = $verify.statusCode
        fileSize = $verify.body.fileSize
        checksumSha256 = $verify.body.checksumSha256
        checksumMatched = [bool] $verify.body.checksumMatched
    }
} catch {
    $result.status = Get-CpfRuntimeStatusText "Failed"
    $result.failureClassification = "runtime"
    $result.error = $_.Exception.Message
    throw
} finally {
    $result.finishedAt = [DateTimeOffset]::Now.ToString("o")
    Write-CpfRuntimeJson -Path $resultPath -Value $result
}

Write-Host "첨부 EDU 런타임 스모크 통과: $resultPath"
