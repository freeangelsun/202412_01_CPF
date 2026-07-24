param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $ProfilePath = ""
)
$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "database-profile-common.ps1")
if ([string]::IsNullOrWhiteSpace($ProfilePath)) {
    $ProfilePath = Join-Path $Root "cpf-tools/config/database-install.default.json"
}
$profile = Get-CpfDatabaseProfile $ProfilePath
$keys = @("core","common","admin","bizAdmin","batch","reference","member","account")
$targets = @()
foreach ($key in $keys) {
    $target = ConvertTo-CpfModuleProfile $profile $key
    $targets += $target
    Write-Host "$key domainName=$($target.domainName) systemCode=$($target.systemCode) moduleName=$($target.moduleName) vendor=$($target.vendor) host=$($target.host):$($target.port) db=$($target.databaseName) enabled=$($target.enabled)"
}

$domainDuplicates = @($targets | Group-Object domainName | Where-Object Count -gt 1)
if ($domainDuplicates.Count -gt 0) {
    throw "domainName 중복: $((($domainDuplicates | ForEach-Object Name) -join ', '))"
}

$codeDuplicates = @($targets | Group-Object systemCode | Where-Object Count -gt 1)
if ($codeDuplicates.Count -gt 0) {
    throw "systemCode 중복: $((($codeDuplicates | ForEach-Object Name) -join ', '))"
}

$moduleDuplicates = @($targets | Group-Object moduleName | Where-Object Count -gt 1)
if ($moduleDuplicates.Count -gt 0) {
    throw "moduleName 중복: $((($moduleDuplicates | ForEach-Object Name) -join ', '))"
}
foreach ($vendor in @("mariadb","mysql","postgresql","oracle","sqlserver")) {
    $rootPath = Join-Path $Root "cpf-tools/db/vendor/$vendor/domain-template"
    foreach ($rel in @(
        "provision/01_provision.sql.template",
        "install/10_empty_install.sql.template",
        "seed/20_product_seed.sql.template",
        "verify/90_verify.sql.template",
        "migration/V1____DOMAIN___domain.sql.template",
        "rollback/R1__remove___DOMAIN___domain.sql.template",
        "runtime/mybatis/__MAPPER__.xml.template"
    )) {
        $p = Join-Path $rootPath $rel
        if (-not (Test-Path -LiteralPath $p -PathType Leaf)) {
            throw "Generated Domain Vendor template 누락: $vendor/$rel"
        }
    }
}

$platformInstaller = Get-Content -LiteralPath (Join-Path $Root "cpf-tools/scripts/initialize-cpf-database.ps1") -Raw -Encoding UTF8
if ($platformInstaller -match 'CONCAT\(''CREATE USER IF NOT EXISTS \$migrationAccount IDENTIFIED') {
    throw "Platform Installer 동적 CREATE USER quoting 회귀를 감지했습니다."
}
if ($platformInstaller -notmatch "migrationAccountDynamic" -or
    $platformInstaller -notmatch "runtimeAccountDynamic") {
    throw "Platform Installer의 동적 account quote 보호 코드가 없습니다."
}

$domainInstaller = Get-Content -LiteralPath (Join-Path $Root "cpf-tools/scripts/initialize-domain-database.ps1") -Raw -Encoding UTF8
if ($domainInstaller -match 'CONCAT\("CREATE USER IF NOT EXISTS') {
    throw "Generated Domain Installer가 MariaDB ANSI_QUOTES에 의존하는 CREATE USER 문자열을 사용합니다."
}
if ($domainInstaller -notmatch "migAccountDynamic" -or
    $domainInstaller -notmatch "runAccountDynamic") {
    throw "Generated Domain Installer의 동적 account quote 보호 코드가 없습니다."
}


if ($platformInstaller -notmatch "function Test-MariaConnection" -or
    $platformInstaller -notmatch "admin connection preflight=PASS" -or
    $platformInstaller -notmatch '\$inputError') {
    throw "Platform Installer MariaDB connection/stdin error 보호 로직이 없습니다."
}

if ($domainInstaller -notmatch '\$inputError') {
    throw "Generated Domain Installer stdin error 보호 로직이 없습니다."
}


$productSeedPath = Join-Path $Root "cpf-tools/db/vendor/mariadb/seed/00_product_seed.sql"
$productSeed = Get-Content -LiteralPath $productSeedPath -Raw -Encoding UTF8
if ($productSeed -match '(?i)\b(?:cpf|cmn|adm|bza|bat|mbr|acc|ref|exs)DB\s*\.') {
    throw "Product Seed에 cross logical DB 참조가 있습니다. Owner별 Seed 분리가 필요합니다."
}
if ($productSeed -match "(?i)https?://(?:localhost|127\.0\.0\.1)" -or
    $productSeed -match "(?i)'(?:localhost|127\.0\.0\.1)'") {
    throw "Product Seed에 local endpoint/host fixture가 있습니다. Optional Seed로 이동해야 합니다."
}

$optionalSeedPath = Join-Path $Root "cpf-tools/db/vendor/mariadb/seed/00_optional_sample_seed.sql"
if (-not (Test-Path -LiteralPath $optionalSeedPath -PathType Leaf)) {
    throw "Optional Sample Seed bundle이 없습니다."
}
$testSeedPath = Join-Path $Root "cpf-tools/db/vendor/mariadb/seed/00_test_seed.sql"
if (-not (Test-Path -LiteralPath $testSeedPath -PathType Leaf)) {
    throw "Test Seed bundle이 없습니다."
}


$createDomainPath = Join-Path $Root "cpf-tools/scripts/create-domain.ps1"
$createDomain = Get-Content -LiteralPath $createDomainPath -Raw -Encoding UTF8
$createDomainParamHeader = $createDomain.Substring(0, $createDomain.IndexOf("`n)"))
foreach ($parameterName in @(
    "DatabaseHost", "DatabasePort", "DatabaseName", "DatabaseClientPath"
)) {
    $matches = [regex]::Matches($createDomainParamHeader, "\`$$parameterName\b")
    if ($matches.Count -ne 1) {
        throw "create-domain.ps1 parameter 중복/누락: $parameterName count=$($matches.Count)"
    }
}

foreach ($requiredToken in @(
    '[string[]] $DomainName',
    '[string[]] $SystemCode',
    '[string[]] $ModuleName',
    '[string] $SeedMode'
)) {
    if (-not $platformInstaller.Contains($requiredToken)) {
        throw "Platform DB 선택 설치 기능 누락: $requiredToken"
    }
}

$generatedBatchInstaller = Join-Path $Root "cpf-tools/scripts/initialize-generated-domain-databases.ps1"
$unifiedInstaller = Join-Path $Root "cpf-tools/scripts/initialize-databases.ps1"
$fixedExternalFiles = @(
    (Join-Path $Root "cpf-tools/db/source/mariadb/45_external_schema.sql"),
    (Join-Path $Root "cpf-tools/db/source/mariadb/57_external_seed_data.sql"),
    (Join-Path $Root "cpf-external")
)
foreach ($fixedExternalPath in $fixedExternalFiles) {
    if (Test-Path -LiteralPath $fixedExternalPath) {
        throw "EXS fixed-domain residue가 남아 있습니다: $fixedExternalPath"
    }
}
$settingsPath = Join-Path $Root "settings.gradle"
if (Test-Path -LiteralPath $settingsPath) {
    $settingsText = Get-Content -LiteralPath $settingsPath -Raw -Encoding UTF8
    if ($settingsText -match "cpf-external") {
        throw "settings.gradle에 fixed cpf-external 등록이 남아 있습니다."
    }
}

& (Join-Path $PSScriptRoot "check-database-schema-drift.ps1") -Root $Root
if ($LASTEXITCODE -ne 0) {
    throw "DB schema artifact drift gate 실패"
}

foreach ($requiredScript in @($generatedBatchInstaller, $unifiedInstaller)) {
    if (-not (Test-Path -LiteralPath $requiredScript -PathType Leaf)) {
        throw "DB 전체/개별 설치 orchestration Script 누락: $requiredScript"
    }
}

$resolverPath = Join-Path $Root "cpf-core/src/main/java/com/cpf/core/common/database/CpfSqlResourceResolver.java"
if (-not (Test-Path -LiteralPath $resolverPath -PathType Leaf)) {
    throw "Vendor Runtime SQL resolver 누락: $resolverPath"
}
$resolver = Get-Content -LiteralPath $resolverPath -Raw -Encoding UTF8
if ($resolver -notmatch "mybatis/vendor/" -or $resolver -match "fallback") {
    # source comment contains fallback, so only ensure the strict error contract exists.
    if ($resolver -notmatch "Runtime Mapper가 없습니다") {
        throw "Vendor Runtime SQL fail-closed 계약이 없습니다."
    }
}

# Product Seed는 반복 실행 가능한 기준정보만 허용한다.
$insertStatements = [regex]::Matches($productSeed, '(?is)\bINSERT\s+(?:IGNORE\s+)?INTO\b.*?;')
foreach ($match in $insertStatements) {
    $statement = $match.Value.ToUpperInvariant()
    $idempotent = $statement.Contains("ON DUPLICATE KEY UPDATE") -or
        $statement.Contains("INSERT IGNORE") -or
        $statement.Contains("WHERE NOT EXISTS")
    if (-not $idempotent) {
        throw "Product Seed에 비멱등 INSERT가 있습니다. 기존 고객 데이터를 덮어쓰지 않는 명시 정책이 필요합니다."
    }
}

$coveragePath = Join-Path $Root "cpf-tools/config/database-vendor-coverage.json"
if (-not (Test-Path -LiteralPath $coveragePath -PathType Leaf)) {
    throw "DB Vendor coverage manifest가 없습니다."
}

Write-Host "CPF database profile/vendor-template check passed."
