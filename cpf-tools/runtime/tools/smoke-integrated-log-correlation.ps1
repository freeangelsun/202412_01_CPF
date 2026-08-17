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
function Invoke-CpfJsonGet([string]$Uri,[hashtable]$Headers) {
    return Invoke-RestMethod -Method Get -Uri $Uri -Headers $Headers -TimeoutSec $TimeoutSec
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
function Find-CorrelationInFiles([IO.FileInfo[]]$Files,[string]$TransactionId,[string]$TraceId) {
    $matches = New-Object Collections.Generic.List[object]
    foreach ($file in $Files) {
        try {
            $content = [IO.File]::ReadAllText($file.FullName,[Text.Encoding]::UTF8)
            if ($content.Contains($TransactionId) -and $content.Contains($TraceId)) {
                $matchedLines=@([IO.File]::ReadLines($file.FullName,[Text.Encoding]::UTF8) | Where-Object { $_.Contains($TransactionId) -and $_.Contains($TraceId) } | Select-Object -First 50)
                $relativePath=if($file.FullName.StartsWith($Root,[StringComparison]::OrdinalIgnoreCase)){$file.FullName.Substring($Root.Length).TrimStart('\','/')}else{$file.Name}
                [void]$matches.Add([ordered]@{ path=$file.FullName; relativePath=$relativePath; sizeBytes=$file.Length; lines=$matchedLines })
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
    $transactionId="${stamp}EDUlogcor10000001"
    $traceId=[guid]::NewGuid().ToString('N')
    $result.transactionId=$transactionId
    $result.traceId=$traceId
    $requestHeaders=@{
        'X-Transaction-Id'=$transactionId; 'X-Trace-Id'=$traceId; 'X-Request-Type'='SMOKE';
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
            $text=[IO.File]::ReadAllText($file.FullName,[Text.Encoding]::UTF8)
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
    Save-Result $result
    throw
}
