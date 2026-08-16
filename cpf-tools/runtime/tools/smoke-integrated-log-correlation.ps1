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
    [int] $PollSeconds = 15
)

# Runtime-only closure for SPECIAL-09/10. A single transaction must be visible in
# structured FileLog, DB log and ADM observability with the same transactionId/traceId.
# Credentials are read from process environment only and are never emitted to stdout/evidence.
$ErrorActionPreference = 'Stop'
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
if ([string]::IsNullOrWhiteSpace($LogBasePath)) { $LogBasePath = Join-Path $Root 'logs' }
if ([string]::IsNullOrWhiteSpace($RuntimeLogRoot)) { $RuntimeLogRoot = Join-Path $Root 'build\cpf-local-runtime\logs' }
if ([string]::IsNullOrWhiteSpace($ResultPath)) { $ResultPath = Join-Path $Root 'build\runtime-smoke\integrated-log-correlation-result.json' }
[IO.Directory]::CreateDirectory((Split-Path -Parent $ResultPath)) | Out-Null

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
function Invoke-CpfJsonGet([string]$Uri,[hashtable]$Headers) {
    return Invoke-RestMethod -Method Get -Uri $Uri -Headers $Headers -TimeoutSec $TimeoutSec
}
function Find-TextFiles([string[]]$Roots) {
    $files = New-Object Collections.Generic.List[IO.FileInfo]
    foreach ($candidate in $Roots) {
        if ([string]::IsNullOrWhiteSpace($candidate) -or -not (Test-Path -LiteralPath $candidate)) { continue }
        if (Test-Path -LiteralPath $candidate -PathType Leaf) { [void]$files.Add((Get-Item -LiteralPath $candidate)); continue }
        Get-ChildItem -LiteralPath $candidate -Recurse -File -ErrorAction SilentlyContinue | ForEach-Object {
            if ($_.Length -le 50MB) { [void]$files.Add($_) }
        }
    }
    return @($files)
}
function Find-CorrelationInFiles([IO.FileInfo[]]$Files,[string]$TransactionId,[string]$TraceId) {
    $matches = New-Object Collections.Generic.List[object]
    foreach ($file in $Files) {
        try {
            $content = [IO.File]::ReadAllText($file.FullName,[Text.Encoding]::UTF8)
            if ($content.Contains($TransactionId) -and $content.Contains($TraceId)) {
                [void]$matches.Add([ordered]@{ path=$file.FullName.Substring($Root.Length).TrimStart('\\','/'); sizeBytes=$file.Length })
            }
        } catch { }
    }
    return @($matches)
}
function Test-RawSecretLeak([IO.FileInfo[]]$Files,[string[]]$Secrets) {
    $findings = New-Object Collections.Generic.List[object]
    foreach ($secret in $Secrets) {
        if ([string]::IsNullOrWhiteSpace($secret) -or $secret.Length -lt 6) { continue }
        foreach ($file in $Files) {
            try {
                $content = [IO.File]::ReadAllText($file.FullName,[Text.Encoding]::UTF8)
                if ($content.Contains($secret)) {
                    [void]$findings.Add([ordered]@{ path=$file.FullName.Substring($Root.Length).TrimStart('\\','/'); secretType='raw-sensitive-value' })
                }
            } catch { }
        }
    }
    return @($findings)
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
    upstreamEvidence=[ordered]@{}
}

$admPassword=[Environment]::GetEnvironmentVariable('CPF_ADM_SMOKE_PASSWORD','Process')
if ([string]::IsNullOrWhiteSpace($admPassword)) { $admPassword=[Environment]::GetEnvironmentVariable('CPF_ADMIN_PASSWORD','Process') }
if ([string]::IsNullOrWhiteSpace($admPassword)) { throw 'CPF_ADM_SMOKE_PASSWORD / CPF_ADMIN_PASSWORD is required in process environment.' }

try {
    $stamp=Get-Date -Format 'yyyyMMddHHmmssfff'
    $transactionId="${stamp}EDUlogcor10000001"
    $traceId=[guid]::NewGuid().ToString('N')
    $result.transactionId=$transactionId
    $result.traceId=$traceId
    $requestHeaders=@{
        'X-Transaction-Id'=$transactionId; 'X-Trace-Id'=$traceId; 'X-Request-Type'='SMOKE';
        'X-Original-Channel-Code'='EDU'; 'X-Channel-Code'='EDU'; 'X-Client-App-Id'='cpf-integrated-log-smoke';
        'X-Client-Version'='1.0.0'; 'X-User-Id'='runtime-smoke'
    }
    $probe=Invoke-CpfJsonGet "$BaseUrl/api/education/query/headers" $requestHeaders
    $actualTx=[string](Get-SafeProperty $probe 'transactionId' '')
    $actualTrace=[string](Get-SafeProperty $probe 'traceId' $traceId)
    if ($actualTx -ne $transactionId) { throw "transaction header propagation mismatch. expected=$transactionId actual=$actualTx" }
    if (-not [string]::IsNullOrWhiteSpace($actualTrace) -and $actualTrace -ne $traceId) { throw 'trace header propagation mismatch.' }
    $result.transactionProbe.status='PASS'

    $loginBody=@{operatorId=$AdmUsername;password=$admPassword;otpCode=$null} | ConvertTo-Json -Compress
    $login=Invoke-RestMethod -Method Post -Uri "$BaseUrl/adm/api/auth/login" -ContentType 'application/json' -Body $loginBody -TimeoutSec $TimeoutSec
    $accessToken=[string](Get-SafeProperty $login 'accessToken' '')
    if ([string]::IsNullOrWhiteSpace($accessToken)) { throw 'ADM login did not return accessToken.' }
    $authHeaders=@{ Authorization="Bearer $accessToken" }

    $deadline=(Get-Date).AddSeconds([Math]::Max(1,$PollSeconds))
    $dbResponse=$null; $obs=$null; $timeline=$null; $recovery=$null
    do {
        Start-Sleep -Milliseconds 750
        try { $dbResponse=Invoke-CpfJsonGet "$BaseUrl/adm/api/logs?transactionId=$transactionId&traceId=$traceId&limit=50" $authHeaders } catch { $dbResponse=$null }
        try { $obs=Invoke-CpfJsonGet "$BaseUrl/adm/api/observability/transactions/$transactionId?limit=50" $authHeaders } catch { $obs=$null }
        try { $timeline=Invoke-CpfJsonGet "$BaseUrl/adm/api/transaction-groups/$transactionId/timeline" $authHeaders } catch { $timeline=$null }
        try { $recovery=Invoke-CpfJsonGet "$BaseUrl/adm/api/observability/file-log-recovery" $authHeaders } catch { $recovery=$null }
        $dbCount=Get-ArrayCount $dbResponse 'items'
        $obsCount=Get-ArrayCount $obs 'transactionLogs'
        $timelineCount=Get-ArrayCount $timeline 'items'
        if ($dbCount -gt 0 -and $obsCount -gt 0) { break }
    } while((Get-Date) -lt $deadline)

    $logFiles=Find-TextFiles @($LogBasePath)
    $fileMatches=Find-CorrelationInFiles $logFiles $transactionId $traceId
    $result.fileLog.matchCount=$fileMatches.Count
    $result.fileLog.files=$fileMatches
    $result.fileLog.status=if($fileMatches.Count -gt 0){'PASS'}else{'FAIL'}

    $dbItems=@(Get-SafeProperty $dbResponse 'items' @())
    $result.dbLog.itemCount=$dbItems.Count
    $dbCorrelated=@($dbItems | Where-Object {
        ([string](Get-SafeProperty $_ 'transactionId' (Get-SafeProperty $_ 'TRANSACTION_ID' ''))) -eq $transactionId -and
        ([string](Get-SafeProperty $_ 'traceId' (Get-SafeProperty $_ 'TRACE_ID' ''))) -eq $traceId
    })
    $result.dbLog.correlatedCount=$dbCorrelated.Count
    $result.dbLog.status=if($dbCorrelated.Count -gt 0){'PASS'}else{'FAIL'}

    $result.admTimeline.transactionLogCount=Get-ArrayCount $obs 'transactionLogs'
    $result.admTimeline.timelineSegmentCount=Get-ArrayCount $timeline 'items'
    $obsText=if($null -ne $obs){$obs|ConvertTo-Json -Depth 30 -Compress}else{''}
    $timelineText=if($null -ne $timeline){$timeline|ConvertTo-Json -Depth 30 -Compress}else{''}
    $result.admTimeline.traceLinked=($obsText.Contains($traceId) -or $timelineText.Contains($traceId))
    $result.admTimeline.status=if($result.admTimeline.transactionLogCount -gt 0 -and $result.admTimeline.traceLinked){'PASS'}else{'FAIL'}

    $recoveryObj=Get-SafeProperty $recovery 'recovery' $null
    $result.recovery.pending=Get-SafeProperty $recoveryObj 'pending' $null
    $result.recovery.quarantined=Get-SafeProperty $recoveryObj 'quarantined' $null
    $result.recovery.terminalLoss=Get-SafeProperty $recoveryObj 'terminalLoss' $null
    $result.recovery.alertState=Get-SafeProperty $recovery 'alertState' $null
    [long]$terminal=0
    [long]$quarantine=0
    if($null -ne $result.recovery.terminalLoss){$terminal=[long]$result.recovery.terminalLoss}
    if($null -ne $result.recovery.quarantined){$quarantine=[long]$result.recovery.quarantined}
    $result.recovery.status=if($null -eq $recovery){'FAIL'}elseif($terminal -eq 0 -and $quarantine -eq 0){'PASS'}else{'FAIL'}

    $fileEvidence=Read-JsonIfPresent $FileLogResultPath
    $policyEvidence=Read-JsonIfPresent $LogPolicyResultPath
    $result.upstreamEvidence.fileLogResultPresent=$null -ne $fileEvidence
    $result.upstreamEvidence.logPolicyResultPresent=$null -ne $policyEvidence
    $upstreamError=($null -ne (Get-SafeProperty $fileEvidence 'error' $null)) -or ($null -ne (Get-SafeProperty $policyEvidence 'error' $null))
    $result.upstreamEvidence.status=if($null -ne $fileEvidence -and $null -ne $policyEvidence -and -not $upstreamError){'PASS'}else{'FAIL'}

    $securityFiles=Find-TextFiles @($LogBasePath,$RuntimeLogRoot,$FileLogResultPath,$LogPolicyResultPath)
    $leaks=Test-RawSecretLeak $securityFiles @($admPassword,$accessToken)
    $result.security.rawSecretLeakCount=$leaks.Count
    $result.security.rawSecretLeakFiles=$leaks
    $fatalPatterns=@('OutOfMemoryError','StackOverflowError','FATAL EXCEPTION','Unhandled exception','TERMINAL_LOSS')
    $fatalFiles=New-Object Collections.Generic.List[string]
    foreach($file in (Find-TextFiles @($RuntimeLogRoot))){
        try {
            $text=[IO.File]::ReadAllText($file.FullName,[Text.Encoding]::UTF8)
            if($fatalPatterns | Where-Object {$text.Contains($_)}){[void]$fatalFiles.Add($file.FullName.Substring($Root.Length).TrimStart('\\','/'))}
        }catch{}
    }
    $result.security.fatalRuntimeMarkerCount=$fatalFiles.Count
    $result.security.fatalRuntimeFiles=@($fatalFiles)
    $result.security.status=if($leaks.Count -eq 0 -and $fatalFiles.Count -eq 0){'PASS'}else{'FAIL'}

    $checks=@($result.transactionProbe.status,$result.fileLog.status,$result.dbLog.status,$result.admTimeline.status,$result.recovery.status,$result.security.status,$result.upstreamEvidence.status)
    $result.status=if(@($checks|Where-Object {$_ -ne 'PASS'}).Count -eq 0){'PASS'}else{'FAIL'}
    Save-Result $result
    if($result.status -ne 'PASS'){throw "integrated log correlation failed. result=$ResultPath"}
    Write-Host "Integrated log correlation PASS. transactionId=$transactionId result=$ResultPath"
} catch {
    $result.status='FAIL'
    $result.error=$_.Exception.Message
    Save-Result $result
    throw
}
