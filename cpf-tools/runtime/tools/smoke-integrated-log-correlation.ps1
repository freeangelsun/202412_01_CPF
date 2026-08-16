[CmdletBinding()]
param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $BaseUrl = 'http://127.0.0.1:8080',
    [string] $LogBasePath = '',
    [string] $RuntimeLogRoot = '',
    [string] $FileLogResultPath = '',
    [string] $LogPolicyResultPath = '',
    [string] $AdmUsername = 'admin',
    [string] $AdmPassword = $env:CPF_ADM_SMOKE_PASSWORD,
    [string] $ResultPath = '',
    [int] $TimeoutSec = 20
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
if ([string]::IsNullOrWhiteSpace($LogBasePath)) { $LogBasePath = Join-Path $Root 'logs' }
if ([string]::IsNullOrWhiteSpace($RuntimeLogRoot)) { $RuntimeLogRoot = Join-Path $Root 'build\cpf-local-runtime\logs' }
if ([string]::IsNullOrWhiteSpace($FileLogResultPath)) { $FileLogResultPath = Join-Path $Root 'build\runtime-smoke\file-log-standard-result.json' }
if ([string]::IsNullOrWhiteSpace($LogPolicyResultPath)) { $LogPolicyResultPath = Join-Path $Root 'build\runtime-smoke\log-policy-runtime-smoke-result.json' }
if ([string]::IsNullOrWhiteSpace($ResultPath)) { $ResultPath = Join-Path $Root 'build\runtime-smoke\integrated-log-correlation-result.json' }
[IO.Directory]::CreateDirectory((Split-Path -Parent $ResultPath)) | Out-Null

$result = [ordered]@{
    startedAt = (Get-Date).ToUniversalTime().ToString('o')
    status = 'FAIL'
    transactionId = $null
    traceId = $null
    fileLogDbCorrelation = [ordered]@{}
    fileLogRecovery = [ordered]@{}
    processRuntimeLog = [ordered]@{}
    secretLeakScan = [ordered]@{}
}

function Save-Result {
    $result.finishedAt = (Get-Date).ToUniversalTime().ToString('o')
    [IO.File]::WriteAllText($ResultPath, ($result | ConvertTo-Json -Depth 30) + "`n", $Utf8NoBom)
}

function Read-JsonFile([string] $Path) {
    if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) { throw "required result file missing: $Path" }
    return Get-Content -LiteralPath $Path -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 50
}

function Invoke-Json {
    param([string]$Method,[string]$Uri,[hashtable]$Headers=@{},[object]$Body=$null)
    $parameters = @{
        Method=$Method; Uri=$Uri; Headers=$Headers; TimeoutSec=$TimeoutSec; UseBasicParsing=$true
    }
    if ($null -ne $Body) {
        $parameters.ContentType='application/json;charset=UTF-8'
        $parameters.Body=[Text.Encoding]::UTF8.GetBytes(($Body | ConvertTo-Json -Depth 20))
    }
    $response = Invoke-WebRequest @parameters
    if ([string]::IsNullOrWhiteSpace($response.Content)) { return $null }
    return $response.Content | ConvertFrom-Json -Depth 50
}

function Assert-Passed([object]$Object,[string]$Path) {
    if ($null -eq $Object -or [string]$Object.status -ne 'PASSED') {
        throw "runtime logging prerequisite did not PASS: $Path"
    }
}

function Find-TransactionEvents([string]$TransactionId) {
    $events = [Collections.Generic.List[object]]::new()
    if (-not (Test-Path -LiteralPath $LogBasePath -PathType Container)) { return @() }
    foreach ($file in Get-ChildItem -LiteralPath $LogBasePath -Recurse -File -Filter '*.log' -ErrorAction SilentlyContinue) {
        foreach ($line in Get-Content -LiteralPath $file.FullName -Encoding UTF8 -ErrorAction SilentlyContinue) {
            if ([string]::IsNullOrWhiteSpace($line) -or -not $line.Contains($TransactionId)) { continue }
            try {
                $json = $line | ConvertFrom-Json -Depth 30
                if ([string]$json.transactionId -eq $TransactionId) {
                    $events.Add([pscustomobject]@{ file=$file.FullName; event=$json }) | Out-Null
                }
            } catch {
                # Non-JSON process/application lines are not structured FileLog evidence.
            }
        }
    }
    return @($events)
}

function Find-SecretLeak([string[]]$Secrets,[string[]]$Roots) {
    $hits = [Collections.Generic.List[object]]::new()
    foreach ($rootPath in $Roots) {
        if (-not (Test-Path -LiteralPath $rootPath -PathType Container)) { continue }
        foreach ($file in Get-ChildItem -LiteralPath $rootPath -Recurse -File -ErrorAction SilentlyContinue | Where-Object { $_.Extension -in @('.log','.txt','.out','.err') -or $_.Name -match '\.(out|err)\.log$' }) {
            $text = ''
            try { $text = [IO.File]::ReadAllText($file.FullName, [Text.Encoding]::UTF8) } catch { continue }
            foreach ($secret in $Secrets) {
                if (-not [string]::IsNullOrWhiteSpace($secret) -and $text.Contains($secret)) {
                    $hits.Add([pscustomobject]@{ file=$file.FullName; secretType='RAW_SECRET' }) | Out-Null
                }
            }
        }
    }
    return @($hits)
}

try {
    $fileResult = Read-JsonFile $FileLogResultPath
    if ([int]$fileResult.runtimeProbe.httpStatus -lt 200 -or [int]$fileResult.runtimeProbe.httpStatus -ge 300) {
        throw 'FileLog runtime probe did not return 2xx.'
    }
    $requiredFileFailures = @($fileResult.files | Where-Object { $_.required -and ((-not $_.exists) -or @($_.missingFields).Count -gt 0) })
    if ($requiredFileFailures.Count -gt 0) { throw 'FileLog standard runtime required fields are incomplete.' }

    $policyResult = Read-JsonFile $LogPolicyResultPath
    if ($null -ne $policyResult.PSObject.Properties['error'] -and -not [string]::IsNullOrWhiteSpace([string]$policyResult.error)) {
        throw "Log policy runtime reported error: $($policyResult.error)"
    }
    Assert-Passed $policyResult.dbLogDisabled 'dbLogDisabled'
    Assert-Passed $policyResult.dbLogEnabled 'dbLogEnabled'
    Assert-Passed $policyResult.requestBodyPolicy 'requestBodyPolicy'
    Assert-Passed $policyResult.responseBodyPolicy 'responseBodyPolicy'
    Assert-Passed $policyResult.errorStackPolicy 'errorStackPolicy'
    Assert-Passed $policyResult.admObservability.transactionLogList 'admObservability.transactionLogList'
    Assert-Passed $policyResult.admObservability.transactionLogDetail 'admObservability.transactionLogDetail'
    Assert-Passed $policyResult.admObservability.observabilityByBusinessTransaction 'admObservability.observabilityByBusinessTransaction'

    $transactionId = [string]$policyResult.admObservability.transactionIdAlias.transactionId
    $traceId = [string]$policyResult.admObservability.traceSearch.traceId
    if ([string]::IsNullOrWhiteSpace($transactionId)) { throw 'DB/ADM runtime result did not expose transactionId.' }
    $result.transactionId = $transactionId
    $result.traceId = $traceId

    $events = @(Find-TransactionEvents $transactionId)
    if ($events.Count -eq 0) { throw "FileLog does not contain DB transactionId=$transactionId" }
    $traceMatched = [string]::IsNullOrWhiteSpace($traceId) -or @($events | Where-Object { [string]$_.event.traceId -eq $traceId }).Count -gt 0
    if (-not $traceMatched) { throw "FileLog traceId does not match DB/ADM traceId=$traceId" }
    $result.fileLogDbCorrelation = [ordered]@{
        status='PASSED'; transactionId=$transactionId; traceId=$traceId; fileEventCount=$events.Count
        files=@($events | ForEach-Object { [IO.Path]::GetRelativePath($Root, $_.file).Replace('\','/') } | Sort-Object -Unique)
    }

    if ([string]::IsNullOrWhiteSpace($AdmPassword)) { throw 'CPF_ADM_SMOKE_PASSWORD/AdmPassword is required for file-log recovery runtime query.' }
    $login = Invoke-Json -Method Post -Uri "$BaseUrl/adm/api/auth/login" -Body @{ operatorId=$AdmUsername; password=$AdmPassword }
    $accessToken = [string]$login.accessToken
    if ([string]::IsNullOrWhiteSpace($accessToken)) { throw 'ADM login did not return accessToken.' }
    $headers = @{ Authorization="Bearer $accessToken"; 'X-Transaction-Id'=("{0}ADMlogcorr0000001" -f (Get-Date -Format 'yyyyMMddHHmmssfff')); 'X-Trace-Id'=[guid]::NewGuid().ToString('N'); 'X-Request-Type'='SMOKE'; 'X-Original-Channel-Code'='ADM'; 'X-Channel-Code'='ADM'; 'X-Client-Version'='runtime-smoke'; 'X-Caller-Service'='cpf-smoke' }
    $recovery = Invoke-Json -Method Get -Uri "$BaseUrl/adm/api/observability/file-log-recovery" -Headers $headers
    if ($recovery.available -ne $true) { throw 'FileLog runtime diagnostics are unavailable.' }
    $terminalLoss = [long]$recovery.recovery.terminalLoss
    $quarantined = [long]$recovery.recovery.quarantined
    $pending = [long]$recovery.recovery.pending
    $writeFailures = [long]$recovery.write.writeFailureCount
    if ($terminalLoss -ne 0 -or $quarantined -ne 0 -or $pending -ne 0 -or $writeFailures -ne 0) {
        throw "FileLog runtime is not clean: terminalLoss=$terminalLoss quarantined=$quarantined pending=$pending writeFailures=$writeFailures"
    }
    $result.fileLogRecovery = [ordered]@{ status='PASSED'; health=$recovery.health; alertState=$recovery.alertState; terminalLoss=$terminalLoss; quarantined=$quarantined; pending=$pending; writeFailureCount=$writeFailures }

    $stdout = Join-Path $RuntimeLogRoot 'LOCAL_WEB.out.log'
    $stderr = Join-Path $RuntimeLogRoot 'LOCAL_WEB.err.log'
    if (-not (Test-Path -LiteralPath $stdout -PathType Leaf) -or (Get-Item -LiteralPath $stdout).Length -le 0) {
        throw "Local runtime stdout log missing/empty: $stdout"
    }
    $fatalPatterns = @('APPLICATION FAILED TO START','OutOfMemoryError','BindException','BeanCreationException')
    $fatalHits = [Collections.Generic.List[string]]::new()
    foreach ($path in @($stdout,$stderr)) {
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { continue }
        $text=[IO.File]::ReadAllText($path,[Text.Encoding]::UTF8)
        foreach($pattern in $fatalPatterns){ if($text.Contains($pattern)){ $fatalHits.Add("$([IO.Path]::GetFileName($path)):$pattern") | Out-Null } }
    }
    if ($fatalHits.Count -gt 0) { throw "Fatal pattern found in local runtime process log: $($fatalHits -join ', ')" }
    $result.processRuntimeLog = [ordered]@{ status='PASSED'; stdout=[IO.Path]::GetRelativePath($Root,$stdout).Replace('\','/'); stderr=$(if(Test-Path $stderr){[IO.Path]::GetRelativePath($Root,$stderr).Replace('\','/')}else{$null}); fatalHits=@() }

    $proofKey=[Environment]::GetEnvironmentVariable('CPF_ADM_APPROVAL_PROOF_KEY_BASE64','Process')
    $leaks = @(Find-SecretLeak @($AdmPassword,$accessToken,$proofKey) @($LogBasePath,$RuntimeLogRoot))
    if ($leaks.Count -gt 0) { throw "Raw credential/token found in runtime logs: $($leaks.Count) file(s)" }
    $result.secretLeakScan = [ordered]@{ status='PASSED'; scannedRoots=@($LogBasePath,$RuntimeLogRoot); rawSecretHits=0 }

    $result.status='PASS'
    Save-Result
    Write-Host "[CPF][LOG][INTEGRATED][PASS] transactionId=$transactionId fileEvents=$($events.Count) result=$ResultPath"
    exit 0
} catch {
    $result.error=$_.Exception.Message
    Save-Result
    Write-Error $_.Exception.Message
    exit 1
}
