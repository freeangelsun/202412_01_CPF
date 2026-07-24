param(
    [Parameter(Mandatory = $true)] [string] $DomainName,
    [Parameter(Mandatory = $true)] [string] $SystemCode,
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [ValidateSet('mariadb','mysql','postgresql','oracle','sqlserver')] [string] $DatabaseVendor = 'mariadb',
    [string] $DatabaseHost = $(if ($env:CPF_DOMAIN_DB_HOST) { $env:CPF_DOMAIN_DB_HOST } else { '127.0.0.1' }),
    [int] $DatabasePort = 0,
    [string] $DatabaseName = '',
    [string] $DatabaseUsername = $env:CPF_DOMAIN_DB_USERNAME,
    [string] $DatabasePassword = $env:CPF_DOMAIN_DB_PASSWORD,
    [string] $AdminUsername = $env:CPF_DOMAIN_DB_ADMIN_USERNAME,
    [string] $AdminPassword = $env:CPF_DOMAIN_DB_ADMIN_PASSWORD,
    [string] $DatabaseClientPath = '',
    [string] $ServiceBaseUrl = '',
    [string] $TransactionId = '',
    [switch] $Apply,
    [switch] $ProvisionDatabase,
    [switch] $SkipBuild,
    [switch] $RunHttpCrud,
    [switch] $RoundTrip,
    [switch] $ConfirmGeneratedSourceRemoval
)

$ErrorActionPreference = 'Stop'
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
$domain = $DomainName.Trim().ToLowerInvariant()
$code = $SystemCode.Trim().ToUpperInvariant()
if ($domain -notmatch '^[a-z][a-z0-9]{1,29}$') { throw 'DomainName 형식이 올바르지 않습니다.' }
if ($code -notmatch '^[A-Z][A-Z0-9]{2}$') { throw 'SystemCode는 3자리 대문자/숫자입니다.' }
if ($RoundTrip -and -not $ConfirmGeneratedSourceRemoval) { throw 'RoundTrip은 생성된 Source를 제거합니다. -ConfirmGeneratedSourceRemoval을 함께 지정하세요.' }
if ($ProvisionDatabase -and -not $Apply) { throw 'DB bootstrap은 실제 생성(-Apply) 시에만 수행합니다.' }

$create = Join-Path $PSScriptRoot 'create-domain.ps1'
$verify = Join-Path $PSScriptRoot 'verify-domain.ps1'
$remove = Join-Path $PSScriptRoot 'remove-domain.ps1'
$dbInit = Join-Path $PSScriptRoot 'initialize-domain-database.ps1'
foreach ($script in @($create,$verify,$remove,$dbInit)) { if (-not (Test-Path -LiteralPath $script -PathType Leaf)) { throw "필수 Script가 없습니다: $script" } }

$resultDir = Join-Path $Root "build/reports/generated-domain-lifecycle/$domain"
New-Item -ItemType Directory -Force -Path $resultDir | Out-Null
$resultPath = Join-Path $resultDir 'generated-domain-lifecycle.sanitized.json'
$result = [ordered]@{
    startedAt=[DateTimeOffset]::Now.ToString('o'); status='미검증'; domainName=$domain; systemCode=$code
    apply=[bool]$Apply; provisionDatabase=[bool]$ProvisionDatabase; databaseVendor=$DatabaseVendor
    steps=[ordered]@{}; httpCrud=[ordered]@{executed=$false}; databaseDropped=$false
}
function Save-Result { $result.finishedAt=[DateTimeOffset]::Now.ToString('o'); [IO.File]::WriteAllText($resultPath,($result|ConvertTo-Json -Depth 30)+[Environment]::NewLine,$Utf8NoBom) }
function Invoke-Step([string]$Name,[scriptblock]$Action) {
    $started=[DateTimeOffset]::Now
    try { & $Action; $result.steps[$Name]=[ordered]@{status='완료';startedAt=$started.ToString('o');finishedAt=[DateTimeOffset]::Now.ToString('o')} }
    catch { $result.steps[$Name]=[ordered]@{status='실패';error=$_.Exception.Message;startedAt=$started.ToString('o');finishedAt=[DateTimeOffset]::Now.ToString('o')}; throw }
}
function New-TransactionId {
    if (-not [string]::IsNullOrWhiteSpace($TransactionId)) { return $TransactionId }
    $time=(Get-Date).ToString('yyyyMMddHHmmssfff')
    return "${time}${code}00000010000001"
}
function Invoke-HttpCrud {
    if ([string]::IsNullOrWhiteSpace($ServiceBaseUrl)) { throw '-RunHttpCrud에는 -ServiceBaseUrl이 필요합니다.' }
    $tx=New-TransactionId
    if ($tx.Length -ne 34) { throw "transactionId는 34자리여야 합니다: $tx" }
    $headers=@{'X-Transaction-Id'=$tx;'X-Idempotency-Key'="${code}-SMOKE-001";'Content-Type'='application/json'}
    $base=$ServiceBaseUrl.TrimEnd('/')+"/api/v1/$domain/reference/sample-items"
    $body=@{sampleKey="${code}_SMOKE_001";itemName='Generated Domain Smoke';statusCode='ACTIVE';expectedVersion=0}|ConvertTo-Json
    $created=Invoke-RestMethod -Method Post -Uri $base -Headers $headers -Body $body
    $found=Invoke-RestMethod -Method Get -Uri "$base/${code}_SMOKE_001" -Headers @{'X-Transaction-Id'=$tx}
    $itemId=[long]$found.sampleItemId
    $version=[long]$found.versionNo
    $headers['X-Idempotency-Key']="${code}-SMOKE-002"
    $update=@{sampleKey="${code}_SMOKE_001";itemName='Generated Domain Smoke Updated';statusCode='ACTIVE';expectedVersion=$version}|ConvertTo-Json
    $updated=Invoke-RestMethod -Method Post -Uri "$base/$itemId/update" -Headers $headers -Body $update
    $delete=@{expectedVersion=[long]$updated.versionNo}|ConvertTo-Json
    [void](Invoke-RestMethod -Method Post -Uri "$base/$itemId/delete" -Headers $headers -Body $delete)
    $result.httpCrud=[ordered]@{executed=$true;status='완료';transactionId=$tx;sampleKey="${code}_SMOKE_001";sampleItemId=$itemId}
}

try {
    # 1. 생성 전 충돌/계획 검증
    Invoke-Step 'createDryRun' { & $create -Root $Root -DomainName $domain -SystemCode $code -DatabaseVendor $DatabaseVendor -DryRun | Out-Null }
    if (-not $Apply) { $result.status='미검증'; Save-Result; Write-Host "Dry-run 완료. 실제 lifecycle은 -Apply로 실행하세요. result=$resultPath"; return }

    $createArgs=@{Root=$Root;DomainName=$domain;SystemCode=$code;DatabaseVendor=$DatabaseVendor;Apply=$true}
    if ($ProvisionDatabase) {
        $createArgs.ProvisionDatabase=$true; $createArgs.DatabaseHost=$DatabaseHost; $createArgs.DatabasePort=$DatabasePort; $createArgs.DatabaseName=$DatabaseName
        $createArgs.DatabaseUsername=$DatabaseUsername; $createArgs.DatabasePassword=$DatabasePassword; $createArgs.AdminUsername=$AdminUsername; $createArgs.AdminPassword=$AdminPassword; $createArgs.DatabaseClientPath=$DatabaseClientPath
    }
    Invoke-Step 'create' { & $create @createArgs | Out-Null }
    Invoke-Step 'verifyGeneratedSource' { if ($SkipBuild) { & $verify -Root $Root -DomainName $domain -SystemCode $code -SkipBuild | Out-Null } else { & $verify -Root $Root -DomainName $domain -SystemCode $code | Out-Null } }
    if ($ProvisionDatabase) {
        Invoke-Step 'verifyDatabase' { & $dbInit -Root $Root -DomainName $domain -SystemCode $code -DatabaseVendor $DatabaseVendor -DatabaseHost $DatabaseHost -DatabasePort $DatabasePort -DatabaseName $DatabaseName -DatabaseUsername $DatabaseUsername -DatabasePassword $DatabasePassword -AdminUsername $AdminUsername -AdminPassword $AdminPassword -ClientPath $DatabaseClientPath -Operation verify -Apply | Out-Null }
    }
    if ($RunHttpCrud) { Invoke-Step 'httpCrud' { Invoke-HttpCrud } }

    if ($RoundTrip) {
        $project=Join-Path $Root "cpf-$domain"
        $firstSnapshot=Join-Path $resultDir 'roundtrip-first.sha256'
        Get-ChildItem -LiteralPath $project -Recurse -File | Where-Object { $_.FullName -notmatch '\\build\\|generator-ownership.json|domain-manifest.json' } | ForEach-Object {
            $rel=$_.FullName.Substring($project.Length+1).Replace('\','/'); "$((Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant())  $rel"
        } | Sort-Object | Set-Content -LiteralPath $firstSnapshot -Encoding utf8NoBOM
        Invoke-Step 'removeDryRun' { & $remove -Root $Root -DomainName $domain -SystemCode $code -DryRun | Out-Null }
        Invoke-Step 'removeGeneratedSource' { & $remove -Root $Root -DomainName $domain -SystemCode $code | Out-Null }
        # DB는 자동 DROP하지 않는다. 같은 schema를 재사용하면 install/seed가 idempotent여야 한다.
        Invoke-Step 'recreate' { & $create @createArgs | Out-Null }
        Invoke-Step 'verifyRecreatedSource' { if ($SkipBuild) { & $verify -Root $Root -DomainName $domain -SystemCode $code -SkipBuild | Out-Null } else { & $verify -Root $Root -DomainName $domain -SystemCode $code | Out-Null } }
        $secondSnapshot=Join-Path $resultDir 'roundtrip-second.sha256'
        Get-ChildItem -LiteralPath $project -Recurse -File | Where-Object { $_.FullName -notmatch '\\build\\|generator-ownership.json|domain-manifest.json' } | ForEach-Object {
            $rel=$_.FullName.Substring($project.Length+1).Replace('\','/'); "$((Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant())  $rel"
        } | Sort-Object | Set-Content -LiteralPath $secondSnapshot -Encoding utf8NoBOM
        $first=Get-Content -LiteralPath $firstSnapshot -Encoding UTF8
        $second=Get-Content -LiteralPath $secondSnapshot -Encoding UTF8
        $diff=@(Compare-Object $first $second)
        if ($diff.Count -gt 0) { throw "Generator round-trip 결정성 위반: diff=$($diff.Count)" }
        $result.steps['roundTripParity']=[ordered]@{status='완료';differenceCount=0}
    }
    $result.status='완료'
} catch {
    $result.status='실패'; $result.error=$_.Exception.Message; Save-Result; throw
}
Save-Result
Write-Host "Generated Domain lifecycle PASS. result=$resultPath"
Write-Host '주의: 이 Script는 Domain DB를 자동 DROP하지 않습니다. DB 삭제는 사용자 승인 절차로 별도 수행합니다.'
