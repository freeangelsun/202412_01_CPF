[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string] $DomainName,
    [Parameter(Mandatory = $true)][string] $SystemCode,
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $PackageName = '',
    [string] $TablePrefix = '',
    [ValidateSet('none', 'jdbc', 'mybatis', 'jpa')][string] $Persistence = 'mybatis',
    [ValidateSet('mariadb', 'postgresql', 'oracle')][string] $DatabaseVendor = 'mariadb',
    [string] $DatabaseHost = $(if ($env:CPF_DOMAIN_DB_HOST) { $env:CPF_DOMAIN_DB_HOST } else { '127.0.0.1' }),
    [int] $DatabasePort = 0,
    [string] $DatabaseName = '',
    [string] $DatabaseUsername = $env:CPF_DOMAIN_DB_USERNAME,
    [string] $DatabasePassword = $env:CPF_DOMAIN_DB_PASSWORD,
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

# Full Runtime child-process UTF-8 contract. Keep the emitted byte stream UTF-8 even when pwsh is redirected.
$CpfUtf8ConsoleEncoding = [Text.UTF8Encoding]::new($false)
try {
    [Console]::InputEncoding = $CpfUtf8ConsoleEncoding
    [Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
    $OutputEncoding = $CpfUtf8ConsoleEncoding
    $global:OutputEncoding = $CpfUtf8ConsoleEncoding
} catch { }
$env:PYTHONUTF8 = '1'
$env:PYTHONIOENCODING = 'utf-8'

$ErrorActionPreference = 'Stop'
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
. (Join-Path $Root 'cpf-tools/generator/tools/generated-domain-common.ps1')
$domain = $DomainName.Trim().ToLowerInvariant()
$code = $SystemCode.Trim().ToUpperInvariant()
if ($domain -notmatch '^[a-z][a-z0-9-]{1,49}$') { throw 'DomainName 형식이 올바르지 않습니다.' }
if ($code -notmatch '^[A-Z][A-Z0-9]{2}$') { throw 'SystemCode는 정확히 3자리 대문자/숫자입니다.' }
if ($RoundTrip -and -not $ConfirmGeneratedSourceRemoval) {
    throw 'RoundTrip은 생성된 Source를 제거합니다. -ConfirmGeneratedSourceRemoval을 함께 지정하세요.'
}
if ($ProvisionDatabase -and -not $Apply) { throw 'DB bootstrap은 -Apply와 함께 수행합니다.' }
$resolvedPackageName = if ([string]::IsNullOrWhiteSpace($PackageName)) { "$domain" } else { $PackageName.Trim() }
$resolvedTablePrefix = if ([string]::IsNullOrWhiteSpace($TablePrefix)) { $code } else { $TablePrefix.Trim().ToUpperInvariant() }
if ($resolvedPackageName -notmatch '^[a-z][a-z0-9_]*(?:\.[a-z][a-z0-9_]*)*$' -or
        $resolvedPackageName -eq 'com.cpf' -or $resolvedPackageName.StartsWith('com.cpf.')) {
    throw "Generated Customer Domain PackageName 형식/소유권이 올바르지 않습니다: $resolvedPackageName"
}
if ($resolvedTablePrefix -notmatch '^[A-Z][A-Z0-9_]{1,19}$') {
    throw "TablePrefix 형식이 올바르지 않습니다: $resolvedTablePrefix"
}
if ($Persistence -ne 'mybatis') {
    throw '현재 canonical Sample Transaction lifecycle은 persistence=mybatis를 사용합니다.'
}

# Canonical owner paths are intentionally explicit for repository ownership gates.
$create = Join-Path $Root 'cpf-tools/generator/tools/create-domain.ps1'
$verify = Join-Path $Root 'cpf-tools/generator/verification/verify-domain.ps1'
$remove = Join-Path $Root 'cpf-tools/generator/tools/remove-domain.ps1'
$dbInit = Join-Path $Root 'cpf-tools/generator/tools/initialize-domain-database.ps1'
foreach ($script in @($create, $verify, $remove, $dbInit)) {
    if (-not (Test-Path -LiteralPath $script -PathType Leaf)) { throw "필수 Script가 없습니다: $script" }
}

$sandbox = Join-Path $Root "cpf-docs/work/evidence/generated/domain-generator/lifecycle-$domain"
$definitionDir = Join-Path $sandbox 'definition'
$definitionPath = Join-Path $definitionDir 'cpf-domain.yaml'
$project = Join-Path $sandbox "cpf-$domain"
$transient = Join-Path $Root "cpf-docs/work/evidence/generated/domain-generator/verification/cpf-$domain"
$resultDir = Join-Path $Root "cpf-docs/work/evidence/generated/domain-generator/reports/generated-domain-lifecycle/$domain"
New-Item -ItemType Directory -Force -Path $resultDir | Out-Null
$resultPath = Join-Path $resultDir 'generated-domain-lifecycle.sanitized.json'

function Assert-SafeGeneratedPath([string] $Path) {
    $resolved = [IO.Path]::GetFullPath($Path)
    $allowed = [IO.Path]::GetFullPath((Join-Path $Root 'cpf-docs/work/evidence/generated/domain-generator')).TrimEnd('\', '/') + [IO.Path]::DirectorySeparatorChar
    if (-not $resolved.StartsWith($allowed, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Generated lifecycle sandbox가 허용 경로 밖입니다: $resolved"
    }
}
Assert-SafeGeneratedPath $sandbox
if (Test-Path -LiteralPath $sandbox) { Remove-Item -LiteralPath $sandbox -Recurse -Force }
New-Item -ItemType Directory -Force -Path $definitionDir | Out-Null
$definition = @"
domain:
  name: $domain
  systemCode: $code
  packageName: $resolvedPackageName
database:
  role: CUSTOMER_BUSINESS_DB
  tablePrefix: $resolvedTablePrefix
preset: standard-enterprise
modules:
  online: true
features:
  persistence: mybatis
  httpClient: true
  resilience: true
  cache: none
  messaging: none
runtime:
  localOnlinePort: 18680
generation:
  sampleTransaction: true
"@
[IO.File]::WriteAllText($definitionPath, $definition.Replace("`r`n", "`n"), $Utf8NoBom)

$result = [ordered]@{
    startedAt = [DateTimeOffset]::Now.ToString('o')
    status = '미검증'
    domainName = $domain
    systemCode = $code
    definitionPath = $definitionPath
    generatedProjectMetadata = 'ABSENT'
    apply = [bool]$Apply
    provisionDatabase = [bool]$ProvisionDatabase
    databaseVendor = $DatabaseVendor
    steps = [ordered]@{}
    httpCrud = [ordered]@{ executed = $false }
    databaseDropped = $false
}
function Save-Result {
    $result.finishedAt = [DateTimeOffset]::Now.ToString('o')
    [IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 100) + [Environment]::NewLine, $Utf8NoBom)
}
function Invoke-Step([string] $Name, [scriptblock] $Action) {
    $started = [DateTimeOffset]::Now
    try {
        $value = & $Action
        $result.steps[$Name] = [ordered]@{
            status = '완료'; startedAt = $started.ToString('o')
            finishedAt = [DateTimeOffset]::Now.ToString('o'); result = $value
        }
    } catch {
        $result.steps[$Name] = [ordered]@{
            status = '실패'; error = $_.Exception.Message; startedAt = $started.ToString('o')
            finishedAt = [DateTimeOffset]::Now.ToString('o')
        }
        throw
    }
}
function New-TransactionId {
    if (-not [string]::IsNullOrWhiteSpace($TransactionId)) { return $TransactionId }
    return "$(Get-Date -Format 'yyyyMMddHHmmssfff')${code}00000010000001"
}
function Invoke-HttpCrud {
    if ([string]::IsNullOrWhiteSpace($ServiceBaseUrl)) { throw '-RunHttpCrud에는 -ServiceBaseUrl이 필요합니다.' }
    $tx = New-TransactionId
    if ($tx.Length -ne 34) { throw "transactionId는 34자리여야 합니다: $tx" }
    $headers = @{'X-Transaction-Id'=$tx; 'Idempotency-Key'="${code}-SMOKE-001"; 'Content-Type'='application/json'}
    $base = $ServiceBaseUrl.TrimEnd('/') + "/api/v1/$domain/reference/sample-items"
    $created = Invoke-RestMethod -Method Post -Uri $base -Headers $headers -Body (@{
        sampleKey="${code}_SMOKE_001"; itemName='Generated Domain Smoke'; idempotencyKey="${code}-SMOKE-001"
    } | ConvertTo-Json)
    $itemId = [long]$created.sampleItemId
    $found = Invoke-RestMethod -Method Get -Uri "$base/$itemId" -Headers @{'X-Transaction-Id'=$tx}
    $headers['Idempotency-Key'] = "${code}-SMOKE-002"
    $updated = Invoke-RestMethod -Method Put -Uri "$base/$itemId" -Headers $headers -Body (@{
        itemName='Generated Domain Smoke Updated'; statusCode='ACTIVE'
        idempotencyKey="${code}-SMOKE-002"; expectedVersion=[long]$found.versionNo
    } | ConvertTo-Json)
    $headers['Idempotency-Key'] = "${code}-SMOKE-003"
    [void](Invoke-RestMethod -Method Delete -Uri "$base/$itemId" -Headers $headers -Body (@{
        idempotencyKey="${code}-SMOKE-003"; expectedVersion=[long]$updated.versionNo
    } | ConvertTo-Json))
    $result.httpCrud = [ordered]@{ executed=$true; status='완료'; transactionId=$tx; sampleItemId=$itemId }
}
function Get-SourceSnapshot([string] $Path) {
    return @(Get-ChildItem -LiteralPath $Path -Recurse -File | ForEach-Object {
        $relative = $_.FullName.Substring($Path.Length + 1).Replace('\', '/')
        if ($relative -match '(^|/)build/' -or $relative -match '(^|/)\.gradle/') { return }
        "$((Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant())  $relative"
    } | Sort-Object)
}

try {
    Invoke-Step 'createDryRun' {
        Invoke-CpfCanonicalCli -Root $Root -Arguments @('domain', 'dry-run', '--file', $definitionPath, '--output', $project)
    }
    if (-not $Apply) {
        Save-Result
        Write-Host "Dry-run 완료. 실제 lifecycle은 -Apply로 실행하세요. result=$resultPath"
        return
    }
    Invoke-Step 'create' {
        Invoke-CpfCanonicalCli -Root $Root -Arguments @('domain', 'generate', '--file', $definitionPath, '--output', $project)
    }
    if ($ProvisionDatabase) {
        Invoke-Step 'bootstrapDatabase' {
            & $dbInit -Root $Root -DomainName $domain -SystemCode $code -DefinitionPath $definitionPath `
                -DatabaseVendor $DatabaseVendor -DatabaseHost $DatabaseHost -DatabasePort $DatabasePort `
                -DatabaseName $DatabaseName -DatabaseUsername $DatabaseUsername -DatabasePassword $DatabasePassword `
                -ClientPath $DatabaseClientPath -Operation bootstrap -Apply
        }
    }
    Invoke-Step 'verifyGeneratedSource' {
        & $verify -Root $Root -DomainName $domain -SystemCode $code -DefinitionPath $definitionPath `
            -OutputDir $project -DatabaseVendor $DatabaseVendor -SkipBuild:$SkipBuild
    }
    if ($ProvisionDatabase) {
        Invoke-Step 'verifyDatabase' {
            & $dbInit -Root $Root -DomainName $domain -SystemCode $code -DefinitionPath $definitionPath `
                -DatabaseVendor $DatabaseVendor -DatabaseHost $DatabaseHost -DatabasePort $DatabasePort `
                -DatabaseName $DatabaseName -DatabaseUsername $DatabaseUsername -DatabasePassword $DatabasePassword `
                -ClientPath $DatabaseClientPath -Operation verify -Apply
        }
    }
    if ($RunHttpCrud) { Invoke-Step 'httpCrud' { Invoke-HttpCrud } }

    if ($RoundTrip) {
        [string[]]$firstSnapshotRows = @(Get-SourceSnapshot $project)
        if ($firstSnapshotRows.Count -eq 0) { throw 'Round-trip 첫 Source snapshot이 비어 있습니다.' }
        Invoke-Step 'removeDryRun' {
            & $remove -Root $Root -DomainName $domain -SystemCode $code -DefinitionPath $definitionPath -OutputDir $project -DryRun
        }
        Invoke-Step 'removeGeneratedSource' {
            & $remove -Root $Root -DomainName $domain -SystemCode $code -DefinitionPath $definitionPath -OutputDir $project `
                -ApprovedDisposableLifecycle
        }
        Invoke-Step 'restore' {
            Invoke-CpfCanonicalCli -Root $Root -Arguments @('domain', 'restore', '--file', $definitionPath, '--output', $project)
        }
        Invoke-Step 'verifyRestoredSource' {
            & $verify -Root $Root -DomainName $domain -SystemCode $code -DefinitionPath $definitionPath `
                -OutputDir $project -SkipBuild:$SkipBuild
        }
        [string[]]$secondSnapshotRows = @(Get-SourceSnapshot $project)
        if ($secondSnapshotRows.Count -eq 0) { throw 'Round-trip 복원 Source snapshot이 비어 있습니다.' }
        $difference = @(Compare-Object -ReferenceObject $firstSnapshotRows -DifferenceObject $secondSnapshotRows)
        if ($difference.Count -gt 0) { throw "Generator round-trip 결정성 위반: diff=$($difference.Count)" }
        $result.steps.roundTripParity = [ordered]@{ status='완료'; differenceCount=0 }
    }
    $result.status = '완료'
} catch {
    $result.status = '실패'
    $result.error = $_.Exception.Message
    Save-Result
    throw
} finally {
    Save-Result
    if (Test-Path -LiteralPath $sandbox) {
        Assert-SafeGeneratedPath $sandbox
        Remove-Item -LiteralPath $sandbox -Recurse -Force
    }
    if (Test-Path -LiteralPath $transient) {
        Assert-SafeGeneratedPath $transient
        Remove-Item -LiteralPath $transient -Recurse -Force
    }
}
Write-Host "Generated Domain lifecycle PASS. result=$resultPath"
Write-Host '주의: 이 Script는 Domain DB를 자동 DROP하지 않습니다. DB 삭제는 사용자 승인 절차로 별도 수행합니다.'
