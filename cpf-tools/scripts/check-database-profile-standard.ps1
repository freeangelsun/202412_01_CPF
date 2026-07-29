param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $ProfilePath = ""
)
$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "database-profile-common.ps1")
$supportedVendors = @(Get-CpfSupportedDatabaseVendors)
if ([string]::IsNullOrWhiteSpace($ProfilePath)) {
    $ProfilePath = Join-Path $Root "cpf-tools/config/database-install.default.json"
}
$profile = Get-CpfDatabaseProfile $ProfilePath
if (@(Compare-Object `
            @($profile.supportedVendors | Sort-Object -Unique) `
            @($supportedVendors | Sort-Object -Unique)).Count -ne 0) {
    throw "Default DB Profile supportedVendors가 중앙 Vendor manifest와 일치하지 않습니다."
}
$keys = @($profile.modules.PSObject.Properties | ForEach-Object { [string]$_.Name })
if ($keys.Count -eq 0) {
    throw "Default DB Profile modules가 비어 있습니다."
}
$targets = @()
foreach ($key in $keys) {
    $target = ConvertTo-CpfModuleProfile $profile $key -SkipSecretResolution
    $targets += $target
    Write-Host "$key domainName=$($target.domainName) systemCode=$($target.systemCode) moduleName=$($target.moduleName) vendor=$($target.vendor) host=$($target.host):$($target.port) db=$($target.databaseName) enabled=$($target.enabled)"
    if ([string]$target.sslMode -ne "disabled") {
        throw "Local development DB Profile은 재현 가능한 명시적 sslMode=disabled여야 합니다: module=$key sslMode=$($target.sslMode)"
    }
}

$productionProfilePath = Join-Path $Root "cpf-tools/config/database-install.prod.template.json"
if (-not (Test-Path -LiteralPath $productionProfilePath -PathType Leaf)) {
    throw "Production DB Profile template이 없습니다."
}
$productionProfile = Get-Content -LiteralPath $productionProfilePath -Raw -Encoding UTF8 |
    ConvertFrom-Json -Depth 50
if (@(Compare-Object `
            @($productionProfile.supportedVendors | Sort-Object -Unique) `
            @($supportedVendors | Sort-Object -Unique)).Count -ne 0) {
    throw "Production DB Profile supportedVendors가 중앙 Vendor manifest와 일치하지 않습니다."
}
$productionKeys = @($productionProfile.modules.PSObject.Properties | ForEach-Object { [string]$_.Name })
if (@(Compare-Object $keys $productionKeys).Count -ne 0) {
    throw "Default/Production DB Profile module metadata가 일치하지 않습니다."
}
foreach ($key in $keys) {
    $productionModule = $productionProfile.modules.$key
    if ($null -eq $productionModule -or [string]$productionModule.sslMode -ne "verify-full") {
        throw "Production DB Profile은 sslMode=verify-full이어야 합니다: module=$key"
    }
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

# Current Platform Seed/Provision is derived only from the install profile.
# Historical migration paths are intentionally outside this current-state gate.
$platformSystemCodes = @($targets.systemCode | ForEach-Object {
        ([string] $_).ToUpperInvariant()
    } | Sort-Object -Unique)
$platformLogicalDatabases = @($targets.logicalDatabase | ForEach-Object {
        [string] $_
    } | Sort-Object -Unique)
$canonicalSeedPath = Join-Path $Root "cpf-tools/db/canonical/seed-model.json"
$canonicalSeed = Get-Content -LiteralPath $canonicalSeedPath -Raw -Encoding UTF8 |
    ConvertFrom-Json -Depth 100
$canonicalSeedText = @($canonicalSeed.statements | ForEach-Object {
        if ($_.PSObject.Properties["source"]) { [string] $_.source }
        if ($_.PSObject.Properties["expression"]) { [string] $_.expression }
    }) -join "`n"
$seedModuleCodes = @(
    [regex]::Matches($canonicalSeedText, "'MODULE'\s*,\s*'([A-Z]{3})'") |
        ForEach-Object { $_.Groups[1].Value } |
        Sort-Object -Unique
)
if (@(Compare-Object $platformSystemCodes $seedModuleCodes).Count -ne 0) {
    throw "Canonical Product Seed MODULE code가 Platform Profile과 다릅니다: profile=$($platformSystemCodes -join ',') seed=$($seedModuleCodes -join ',')"
}
$seedOwnedCodes = @(
    [regex]::Matches($canonicalSeedText, "'(?:M|[SEW])([A-Z]{3})\d{4,}'") |
        ForEach-Object { $_.Groups[1].Value } |
        Sort-Object -Unique
)
$nonPlatformSeedCodes = @($seedOwnedCodes | Where-Object {
        $_ -notin $platformSystemCodes
    })
if ($nonPlatformSeedCodes.Count -gt 0) {
    throw "Canonical Product Seed에 Generated/비Platform SystemCode 메시지·응답이 고정되어 있습니다: $($nonPlatformSeedCodes -join ',')"
}

$metadataCatalog = Get-Content -LiteralPath (
    Join-Path $Root "cpf-tools/db/metadata/default-metadata-catalog.json"
) -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 30
$metadataModuleCodes = @($metadataCatalog.codeGroups.MODULE | ForEach-Object {
        ([string] $_).ToUpperInvariant()
    } | Sort-Object -Unique)
if (@(Compare-Object $platformSystemCodes $metadataModuleCodes).Count -ne 0) {
    throw "Default Metadata MODULE code가 Platform Profile과 다릅니다."
}

$provisionContracts = [ordered]@{
    mariadb = [pscustomobject]@{
        path = "cpf-tools/db/vendor/mariadb/source/01_create_databases.sql"
        pattern = "(?im)^\s*CREATE\s+DATABASE\s+IF\s+NOT\s+EXISTS\s+([A-Za-z][A-Za-z0-9_$#]*)"
    }
    postgresql = [pscustomobject]@{
        path = "cpf-tools/db/vendor/postgresql/source/00_provision.sql"
        pattern = "(?im)^\s*--\s*CPF_LOGICAL_DATABASE=([A-Za-z][A-Za-z0-9_$#]*)\s*$"
    }
    oracle = [pscustomobject]@{
        path = "cpf-tools/db/vendor/oracle/source/00_provision.sql"
        pattern = "(?im)^\s*--\s*CPF_LOGICAL_DATABASE=([A-Za-z][A-Za-z0-9_$#]*)\s*$"
    }
}
foreach ($vendorProperty in $provisionContracts.GetEnumerator()) {
    $contract = $vendorProperty.Value
    $provisionText = Get-Content -LiteralPath (Join-Path $Root $contract.path) -Raw -Encoding UTF8
    $provisionDatabases = @(
        [regex]::Matches($provisionText, [string] $contract.pattern) |
            ForEach-Object { $_.Groups[1].Value } |
            Sort-Object -Unique
    )
    if (@(Compare-Object $platformLogicalDatabases $provisionDatabases).Count -ne 0) {
        throw "Vendor Provision DB/Schema가 Platform Profile과 다릅니다: vendor=$($vendorProperty.Key) expected=$($platformLogicalDatabases -join ',') actual=$($provisionDatabases -join ',')"
    }
}

foreach ($vendor in $supportedVendors) {
    $rootPath = Join-Path $Root "cpf-tools/db/vendor/$vendor/domain-template"
    foreach ($rel in @(
        "provision/01_provision.sql.template",
        "provision/02_principals.sql.template",
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
if ($domainInstaller -match '(?i)\bCREATE\s+USER\b') {
    throw "Generated Domain Installer에 Vendor별 principal SQL이 하드코딩되어 있습니다."
}
foreach ($requiredDomainInstallerToken in @(
        'provision/02_principals.sql.template',
        'function Render-DomainTemplate',
        'secretBearing',
        'GetTempFileName')) {
    if (-not $domainInstaller.Contains($requiredDomainInstallerToken)) {
        throw "Generated Domain Installer principal/secret 보호 계약이 없습니다: $requiredDomainInstallerToken"
    }
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


$createDomainPath = Join-Path $Root "cpf-tools/generator/create-domain.ps1"
$createDomain = Get-Content -LiteralPath $createDomainPath -Raw -Encoding UTF8
$createDomainParamEnd = [regex]::Match($createDomain, "(?m)^\)\s*$")
if (-not $createDomainParamEnd.Success) {
    throw "Canonical create-domain.ps1 param block 종료를 찾을 수 없습니다."
}
$createDomainParamHeader = $createDomain.Substring(
    0,
    $createDomainParamEnd.Index + $createDomainParamEnd.Length
)
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

foreach ($forbiddenPlatformDomainLiteral in @(
        '"member"',
        '"account"',
        'mbrDB',
        'accDB')) {
    if ($platformInstaller.Contains($forbiddenPlatformDomainLiteral)) {
        throw "Platform Installer에 Generated Domain 목록/DB가 하드코딩되어 있습니다: $forbiddenPlatformDomainLiteral"
    }
}
if (-not $platformInstaller.Contains('$profile.modules.PSObject.Properties') -or
        -not $platformInstaller.Contains('databaseLifecycle')) {
    throw "Platform Installer가 Profile metadata 기반 module discovery/lifecycle 분리를 사용하지 않습니다."
}

$generatedTransitions = @($targets | Where-Object {
        $_.databaseLifecycle -eq "generated-domain"
    })
foreach ($transition in $generatedTransitions) {
    if ($transition.enabled -or -not $transition.transitional) {
        throw "Generated Domain transition entry는 Platform Pack에서 disabled/transitional이어야 합니다: $($transition.moduleKey)"
    }
}

$generatedBatchInstaller = Join-Path $Root "cpf-tools/scripts/initialize-generated-domain-databases.ps1"
$unifiedInstaller = Join-Path $Root "cpf-tools/scripts/initialize-databases.ps1"
$fixedExternalFiles = @(
    (Join-Path $Root "cpf-tools/db/vendor/mariadb/source/45_external_schema.sql"),
    (Join-Path $Root "cpf-tools/db/vendor/mariadb/source/57_external_seed_data.sql")
)
foreach ($fixedExternalPath in $fixedExternalFiles) {
    if (Test-Path -LiteralPath $fixedExternalPath) {
        throw "EXS fixed-domain SQL residue가 남아 있습니다: $fixedExternalPath"
    }
}

# cpf-external 이름 자체는 금지 대상이 아니다. 사용자가 external/EXS Domain을 필요로 하면
# Golden Generator가 동일 project name을 생성할 수 있다. fixed residue와 generated output은
# Generator ownership manifest로 구분한다.
$generatedExternalDir = Join-Path $Root "cpf-external"
$generatedExternalValid = $false
if (Test-Path -LiteralPath $generatedExternalDir -PathType Container) {
    $externalManifestPath = Join-Path $generatedExternalDir "manifest/domain-manifest.json"
    $externalOwnershipPath = Join-Path $generatedExternalDir "manifest/generator-ownership.json"
    if (-not (Test-Path -LiteralPath $externalManifestPath -PathType Leaf) -or
        -not (Test-Path -LiteralPath $externalOwnershipPath -PathType Leaf)) {
        throw "cpf-external이 존재하지만 Generated Domain manifest/ownership이 없습니다. fixed module residue로 판정합니다."
    }
    $externalManifest = Get-Content -LiteralPath $externalManifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
    $generatedExternalValid = [string]$externalManifest.domainType -eq "GENERATED_DOMAIN" -and
        [string]$externalManifest.domainName -eq "external" -and
        [string]$externalManifest.systemCode -eq "EXS" -and
        [string]$externalManifest.projectName -eq "cpf-external"
    if (-not $generatedExternalValid) {
        throw "cpf-external이 Golden Generated Domain external/EXS 계약과 다릅니다."
    }
}

$settingsPath = Join-Path $Root "settings.gradle"
if (Test-Path -LiteralPath $settingsPath) {
    $settingsText = Get-Content -LiteralPath $settingsPath -Raw -Encoding UTF8
    $settingsHasExternal = $settingsText -match '(?m)^\s*include\s+[''"]cpf-external[''"]'
    if ($settingsHasExternal -and -not $generatedExternalValid) {
        throw "settings.gradle에 cpf-external 등록이 있지만 유효한 Generated Domain external/EXS가 없습니다."
    }
    if ($generatedExternalValid -and -not $settingsHasExternal) {
        throw "Generated external/EXS가 존재하지만 settings.gradle include가 없습니다."
    }
}

$schemaDriftScript = Join-Path $PSScriptRoot "check-database-schema-drift.ps1"
& pwsh -NoProfile -ExecutionPolicy Bypass -File $schemaDriftScript -Root $Root
if ($LASTEXITCODE -ne 0) {
    throw "DB schema artifact drift gate 실패 exitCode=$LASTEXITCODE"
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
if ($resolver -notmatch "CpfVendorResourceRoot\.requiredDirectory" -or
        $resolver -notmatch "Runtime Mapper Pack이 비어 있습니다" -or
        $resolver -notmatch "지원하지 않는 CPF DB Vendor") {
    throw "Vendor Runtime SQL fail-closed 계약이 없습니다."
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

$legacyVendorSourceRoot = Join-Path $Root "cpf-tools/db/source"
if (Test-Path -LiteralPath $legacyVendorSourceRoot -PathType Container) {
    $legacyEntries = @(Get-ChildItem -LiteralPath $legacyVendorSourceRoot -Force -ErrorAction SilentlyContinue)
    if ($legacyEntries.Count -gt 0) {
        throw "Legacy standalone DB vendor source tree가 남아 있습니다: $legacyVendorSourceRoot"
    }
}

$coveragePath = Join-Path $Root "cpf-tools/config/database-vendor-coverage.json"
if (-not (Test-Path -LiteralPath $coveragePath -PathType Leaf)) {
    throw "DB Vendor coverage manifest가 없습니다."
}
$coverage = Get-Content -LiteralPath $coveragePath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 20
$coverageVendors = @($coverage.platform.PSObject.Properties.Name | Sort-Object -Unique)
$generatedCoverageVendors = @($coverage.generatedDomain.PSObject.Properties.Name | Sort-Object -Unique)
if (@(Compare-Object `
            @($supportedVendors | Sort-Object -Unique) `
            $coverageVendors).Count -ne 0 -or
        @(Compare-Object `
            @($supportedVendors | Sort-Object -Unique) `
            $generatedCoverageVendors).Count -ne 0) {
    throw "DB Vendor coverage 목록이 중앙 Vendor manifest와 일치하지 않습니다."
}
foreach ($vendor in $supportedVendors) {
    $expectedSourceRoot = "cpf-tools/db/vendor/$vendor/source"
    $coverageProperty = $coverage.platform.PSObject.Properties[$vendor]
    $coverageEntry = if ($null -eq $coverageProperty) { $null } else { $coverageProperty.Value }
    if ($null -eq $coverageEntry -or [string]$coverageEntry.sourceRoot -ne $expectedSourceRoot) {
        throw "DB Vendor source ownership manifest 불일치: vendor=$vendor expected=$expectedSourceRoot"
    }
    $sourceRootPath = Join-Path $Root $expectedSourceRoot
    if (-not (Test-Path -LiteralPath $sourceRootPath -PathType Container)) {
        throw "DB Vendor source ownership directory 누락: $expectedSourceRoot"
    }
    if ([string]$coverageEntry.ddl -ne "implemented" -or
            [string]$coverageEntry.runtimeSql -ne "implemented" -or
            [string]$coverageEntry.sourceStatus -ne "implemented" -or
            -not (Test-Path -LiteralPath (Join-Path $sourceRootPath "10_cpf_schema.sql") -PathType Leaf)) {
        throw "공식 DB Vendor Platform source/runtime coverage가 구현 상태가 아닙니다: vendor=$vendor"
    }
    $generatedCoverageProperty = $coverage.generatedDomain.PSObject.Properties[$vendor]
    $generatedCoverageEntry = if ($null -eq $generatedCoverageProperty) {
        $null
    } else {
        $generatedCoverageProperty.Value
    }
    if ($null -eq $generatedCoverageEntry -or
            [string]$generatedCoverageEntry.ddlTemplate -ne "implemented" -or
            [string]$generatedCoverageEntry.mybatisTemplate -ne "implemented") {
        throw "공식 DB Vendor Generated Domain coverage가 구현 상태가 아닙니다: vendor=$vendor"
    }
}

Write-Host "CPF database profile/vendor-template check passed."
