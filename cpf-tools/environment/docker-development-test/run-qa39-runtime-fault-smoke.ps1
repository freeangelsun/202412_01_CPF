param(
    [string]$DockerRoot = "C:\dev\Docker",
    [string]$RepoRoot = "C:\dev\projects\jck\202412_01_CPF",
    [string]$SourceIdentity = $env:CPF_SOURCE_SHA
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

function Invoke-CrlfPing {
    param([int]$Port = 19021, [int]$TimeoutMilliseconds = 2000)
    $client = [System.Net.Sockets.TcpClient]::new()
    try {
        $task = $client.ConnectAsync("127.0.0.1", $Port)
        if (-not $task.Wait($TimeoutMilliseconds)) { throw "connect timeout" }
        $stream = $client.GetStream()
        $stream.ReadTimeout = $TimeoutMilliseconds
        $stream.WriteTimeout = $TimeoutMilliseconds
        $request = [System.Text.Encoding]::ASCII.GetBytes("PING`r`n")
        $stream.Write($request, 0, $request.Length)
        $buffer = New-Object byte[] 64
        $count = $stream.Read($buffer, 0, $buffer.Length)
        if ($count -le 0) { throw "empty response" }
        return [System.Text.Encoding]::ASCII.GetString($buffer, 0, $count).Trim()
    } finally {
        $client.Dispose()
    }
}

$cpfRoot = Join-Path $DockerRoot "CPF"
$evidenceRoot = Join-Path $cpfRoot "output\qa39-runtime"
New-Item -ItemType Directory -Path $evidenceRoot -Force | Out-Null
$proxyUri = "http://127.0.0.1:8474/proxies/cpf_tcp_crlf"
$toxicUri = "$proxyUri/toxics"
$events = New-Object System.Collections.Generic.List[object]

$initial = Invoke-CrlfPing
if ($initial -ne "PONG") { throw "Fault 전 PING 실패: $initial" }
$events.Add([pscustomobject]@{ step = "baseline"; result = "PASS"; response = $initial })

try {
    Invoke-RestMethod -Method Post -Uri $proxyUri -ContentType "application/json" -Body '{"enabled":false}' -TimeoutSec 10 | Out-Null
    $blocked = $false
    try { [void](Invoke-CrlfPing -TimeoutMilliseconds 1000) } catch { $blocked = $true }
    if (-not $blocked) { throw "Proxy 비활성화 후 연결이 계속 성공했습니다." }
    $events.Add([pscustomobject]@{ step = "proxy-disabled"; result = "PASS" })
} finally {
    Invoke-RestMethod -Method Post -Uri $proxyUri -ContentType "application/json" -Body '{"enabled":true}' -TimeoutSec 10 | Out-Null
}

$recovered = Invoke-CrlfPing
if ($recovered -ne "PONG") { throw "Proxy 복구 후 PING 실패: $recovered" }
$events.Add([pscustomobject]@{ step = "proxy-recovered"; result = "PASS"; response = $recovered })

$toxicName = "qa39_latency"
try {
    $body = '{"name":"qa39_latency","type":"latency","stream":"downstream","toxicity":1.0,"attributes":{"latency":300,"jitter":0}}'
    Invoke-RestMethod -Method Post -Uri $toxicUri -ContentType "application/json" -Body $body -TimeoutSec 10 | Out-Null
    $watch = [System.Diagnostics.Stopwatch]::StartNew()
    $latencyResponse = Invoke-CrlfPing -TimeoutMilliseconds 3000
    $watch.Stop()
    if ($latencyResponse -ne "PONG" -or $watch.ElapsedMilliseconds -lt 250) {
        throw "Latency Toxic 결과 불일치: response=$latencyResponse elapsed=$($watch.ElapsedMilliseconds)"
    }
    $events.Add([pscustomobject]@{ step = "latency"; result = "PASS"; elapsedMilliseconds = $watch.ElapsedMilliseconds })
} finally {
    try { Invoke-RestMethod -Method Delete -Uri "$toxicUri/$toxicName" -TimeoutSec 10 | Out-Null } catch {}
}

$final = Invoke-CrlfPing
if ($final -ne "PONG") { throw "Toxic 제거 후 PING 실패: $final" }
$events.Add([pscustomobject]@{ step = "final-recovery"; result = "PASS"; response = $final })

if ([string]::IsNullOrWhiteSpace($SourceIdentity)) {
    $stateTool = Join-Path $RepoRoot "cpf-tools/verification/tools/cpf-source-state.py"
    if (Test-Path -LiteralPath $stateTool -PathType Leaf) {
        $stateJson = @(& python $stateTool --root $RepoRoot --scope source 2>&1)
        if ($LASTEXITCODE -eq 0) {
            try { $SourceIdentity = [string](($stateJson | Select-Object -Last 1) | ConvertFrom-Json).contentSha1 } catch {}
        }
    }
}
if ($SourceIdentity -notmatch '^[0-9a-fA-F]{40}$') { throw 'Git-independent SourceIdentity is required for QA39 runtime evidence.' }
$SourceIdentity = $SourceIdentity.ToLowerInvariant()
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$evidence = [pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    sourceIdentity = $SourceIdentity
    identityPolicy = "GIT_INDEPENDENT_CONTENT_SHA1"
    proxy = "cpf_tcp_crlf"
    events = $events
}
$path = Join-Path $evidenceRoot "qa39-runtime-fault-$timestamp.json"
[System.IO.File]::WriteAllText($path, ($evidence | ConvertTo-Json -Depth 8) + "`n", [System.Text.UTF8Encoding]::new($false))
Write-Host "Evidence: $path"
Write-Host "CPF QA39 Toxiproxy 장애·복구 Smoke 완료" -ForegroundColor Green
