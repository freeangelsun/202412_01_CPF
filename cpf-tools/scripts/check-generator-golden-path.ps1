param([string] $Root = "")
$ErrorActionPreference = "Stop"
if ([string]::IsNullOrWhiteSpace($Root)) { $Root = (Resolve-Path "$PSScriptRoot\..\..").Path }

$generator = Join-Path $Root "cpf-tools/generator/create-domain.ps1"
$launcher = Join-Path $Root "cpf-tools/scripts/create-domain.ps1"
if (-not (Test-Path $generator -PathType Leaf)) { throw "Canonical generator missing: $generator" }
$text = Get-Content $generator -Raw -Encoding UTF8
foreach ($required in @(
    "[switch] `$DryRun",
    "module directory already exists",
    "SystemCode가 기존 manifest와 중복",
    "API route 또는 package가 기존 source와 충돌",
    "generator-ownership.json",
    "create-domain-result.json",
    "implementation project(':cpf-common')",
    "published-artifact",
    "implementation platform('com.cpf:cpf-bom:",
    "implementation 'com.cpf.core:cpf-core:",
    "implementation 'com.cpf.common:cpf-common:",
    "cpf-db/vendor",
    "central-domain-template-contract.json",
    '$supportedDatabaseVendorsJson',
    'CpfDataSources.resolve(environment, "cpf.$module.datasource")',
    "DataSourceIsolationTest",
    'withProperty("cpf.datasource.url"',
    'withProperty("cpf.$module.datasource.url"',
    "new groovy.json.JsonSlurper().parse(cpfDomainMetadataFile)",
    "runtimeOnly cpfJdbcDriverByVendor[cpfDbVendor]",
    "runtimeOnly cpfFlywayDatabaseByVendor[cpfDbVendor]",
    "@Profile({""local"", ""test"", ""edu""})"
)) {
    if (-not $text.Contains($required)) { throw "Golden Path generator contract missing: $required" }
}
if ($text -match '(?m)^\s*version\s*=') {
    throw "Generated module must inherit root platformVersion; independent version assignment is forbidden."
}
foreach ($forbiddenGeneratorResidue in @(
        '$applyOrder =',
        '$settingsPatch =',
        '$cpfSeed =',
        '$admSeed =',
        '$bzaSeed =',
        '$smokeSql =',
        'ON DUPLICATE KEY UPDATE')) {
    if ($text.Contains($forbiddenGeneratorResidue)) {
        throw "Canonical generator contains unused/vendor-specific candidate residue: $forbiddenGeneratorResidue"
    }
}
if (Test-Path $launcher -PathType Leaf) {
    $launcherText = Get-Content $launcher -Raw -Encoding UTF8
    if ((Get-Item $launcher).Length -gt 4096 -or -not ($launcherText.Contains("generator") -and $launcherText.Contains("create-domain.ps1"))) {
        throw "Compatibility launcher contains a second generator implementation."
    }
}

$contractPath = Join-Path $Root "cpf-tools/generator/contracts/central-domain-template-contract.json"
$metadataSchemaPath = Join-Path $Root "cpf-tools/generator/contracts/domain-metadata.schema.json"
if (-not (Test-Path $contractPath -PathType Leaf)) {
    throw "Central Generated Domain contract missing: $contractPath"
}
if (-not (Test-Path $metadataSchemaPath -PathType Leaf)) {
    throw "Generated Domain metadata schema missing: $metadataSchemaPath"
}
$contract = Get-Content $contractPath -Raw -Encoding UTF8 | ConvertFrom-Json
$metadataSchema = Get-Content $metadataSchemaPath -Raw -Encoding UTF8 | ConvertFrom-Json

$contractVendors = @($contract.supportedVendors | Sort-Object -Unique)
$schemaVendors = @($metadataSchema.properties.databaseVendor.enum | Sort-Object -Unique)
if ($contractVendors.Count -ne 5 -or
        @(Compare-Object $contractVendors $schemaVendors).Count -ne 0) {
    throw "Generated Domain supported Vendor contract/schema mismatch."
}
$buildRuntimeVendors = @(
    $contract.buildRuntimeContract.vendors.PSObject.Properties.Name |
        Sort-Object -Unique
)
if ([string]$contract.buildRuntimeContract.selectionProperty -ne "cpfDbVendor" -or
        -not [bool]$contract.buildRuntimeContract.selectedVendorOnly -or
        @(Compare-Object $contractVendors $buildRuntimeVendors).Count -ne 0) {
    throw "Generated Domain selected-Vendor Build Runtime contract mismatch."
}
foreach ($contractVendor in $contractVendors) {
    $dependencyEntry = $contract.buildRuntimeContract.vendors.$contractVendor
    if ([string]::IsNullOrWhiteSpace([string]$dependencyEntry.jdbcDriver) -or
            [string]::IsNullOrWhiteSpace([string]$dependencyEntry.flywayDatabase)) {
        throw "Generated Domain Build Runtime dependency metadata missing: $contractVendor"
    }
}
if ($text -match "(?m)^\s*runtimeOnly\s+['""][^'""]*(?:jdbc|mariadb|mysql|postgresql|ojdbc|mssql)[^'""]*['""]") {
    throw "Canonical generator must select one JDBC driver through the Vendor dependency catalog."
}

function Assert-ContractSchemaList {
    param(
        [string] $ContractProperty,
        [string] $SchemaProperty,
        [int] $ExpectedCount
    )
    $contractValues = @($contract.verifyContract.$ContractProperty | Sort-Object -Unique)
    $schemaValues = @(
        $metadataSchema.properties.minimalTransactionContract.properties.$SchemaProperty.items.enum |
            Sort-Object -Unique
    )
    if ($contractValues.Count -ne $ExpectedCount -or
            @(Compare-Object $contractValues $schemaValues).Count -ne 0) {
        throw "Minimal Transaction $ContractProperty contract/schema mismatch."
    }
    return $contractValues
}

$contractColumns = Assert-ContractSchemaList "requiredColumns" "requiredColumns" 14
$contractKeys = Assert-ContractSchemaList "requiredKeys" "requiredKeys" 3
$contractIndexes = Assert-ContractSchemaList "requiredIndexes" "requiredIndexes" 2
$contractChecks = Assert-ContractSchemaList "requiredChecks" "requiredChecks" 2
$contractOperations = Assert-ContractSchemaList "requiredOperations" "operations" 22
foreach ($requiredMetadataField in @(
        "templateContractVersion",
        "capabilities",
        "minimalTransactionContract")) {
    if ($requiredMetadataField -notin @($metadataSchema.required)) {
        throw "Generated Domain metadata must require $requiredMetadataField."
    }
}
$supportedCapabilities = @($contract.capabilityContract.supported | Sort-Object -Unique)
$defaultCapabilities = @($contract.capabilityContract.defaultEnabled | Sort-Object -Unique)
if ($supportedCapabilities.Count -ne 11 -or
        @("online", "database", "local-call" | Where-Object {
                $_ -notin $defaultCapabilities
            }).Count -gt 0 -or
        -not [bool]$contract.capabilityContract.normalizedParityRequired) {
    throw "Generated Domain capability metadata contract가 유효하지 않습니다."
}
$schemaCapabilityKeys = @(
    $metadataSchema.properties.capabilities.properties.PSObject.Properties.Name |
        Sort-Object -Unique
)
$requiredSchemaCapabilityKeys = @(
    $metadataSchema.properties.capabilities.required |
        Sort-Object -Unique
)
if ($schemaCapabilityKeys.Count -ne 11 -or
        @(Compare-Object $schemaCapabilityKeys $requiredSchemaCapabilityKeys).Count -ne 0) {
    throw "Generated Domain capability schema가 모든 선택 결과를 필수 boolean으로 선언해야 합니다."
}
foreach ($requiredMinimalField in @(
        "model",
        "logicalTable",
        "requiredColumns",
        "requiredKeys",
        "requiredIndexes",
        "requiredChecks",
        "operations")) {
    if ($requiredMinimalField -notin @($metadataSchema.properties.minimalTransactionContract.required)) {
        throw "Generated Domain minimalTransactionContract must require $requiredMinimalField."
    }
}
$databaseVendorRequired = @(
    $metadataSchema.allOf |
        Where-Object { $_.if.properties.databaseEnabled.const -eq $true } |
        ForEach-Object { $_.then.required }
) -contains "databaseVendor"
if (-not $databaseVendorRequired) {
    throw "Database-enabled Generated Domain metadata must require databaseVendor."
}

$allowedTokens = @($contract.tokens | Sort-Object -Unique)
$requiredStatements = @($contract.runtimeContract.requiredStatements | Sort-Object -Unique)
if ($requiredStatements.Count -ne 8) {
    throw "Generated Domain Runtime statement contract must contain exactly 8 statements."
}
$verifyCheckMarkers = @{
    mariadb = "information_schema.table_constraints"
    mysql = "information_schema.table_constraints"
    postgresql = "pg_constraint"
    oracle = "all_constraints"
    sqlserver = "sys.check_constraints"
}
$forbiddenFixedDomainPattern = '(?<![A-Z0-9])(MBR|ACC|REF|EXS|PAY|INS)(?![A-Z0-9])'
if ($text -cmatch $forbiddenFixedDomainPattern) {
    throw "Canonical generator contains a fixed example Domain/SystemCode."
}

foreach ($vendor in $contractVendors) {
    $templateRoot = Join-Path $Root "cpf-tools/db/vendor/$vendor/domain-template"
    $packPath = Join-Path $Root "cpf-tools/db/vendor/$vendor/pack.json"
    if (-not (Test-Path -LiteralPath $templateRoot -PathType Container)) {
        throw "Generated Domain Vendor template root missing: $vendor"
    }
    if (-not (Test-Path -LiteralPath $packPath -PathType Leaf)) {
        throw "Generated Domain Vendor pack metadata missing: $vendor"
    }
    $pack = Get-Content -LiteralPath $packPath -Raw -Encoding UTF8 | ConvertFrom-Json
    $packTokens = @($pack.generatedDomainContract.tokens | Sort-Object -Unique)
    if (@(Compare-Object $allowedTokens $packTokens).Count -ne 0) {
        throw "Generated Domain token contract/pack mismatch: $vendor"
    }

    foreach ($relativeTemplate in @($contract.requiredTemplates)) {
        $templatePath = Join-Path $templateRoot ([string]$relativeTemplate)
        if (-not (Test-Path -LiteralPath $templatePath -PathType Leaf)) {
            throw "Generated Domain required template missing: $vendor/$relativeTemplate"
        }
        $templateText = Get-Content -LiteralPath $templatePath -Raw -Encoding UTF8
        $unknownTokens = @([regex]::Matches($templateText, '@(CPF_[A-Z_]+)@') |
                ForEach-Object { $_.Groups[1].Value } |
                Sort-Object -Unique |
                Where-Object { $_ -notin $allowedTokens })
        if ($unknownTokens.Count -gt 0) {
            throw "Unknown Generated Domain template token: vendor=$vendor tokens=$($unknownTokens -join ', ')"
        }
        if ($templateText -cmatch $forbiddenFixedDomainPattern) {
            throw "Generated Domain Vendor template contains a fixed Domain/SystemCode: $vendor/$relativeTemplate"
        }
    }

    $installPath = Join-Path $templateRoot "install/10_empty_install.sql.template"
    $migrationPath = Join-Path $templateRoot "migration/V1____DOMAIN___domain.sql.template"
    $installText = Get-Content -LiteralPath $installPath -Raw -Encoding UTF8
    $migrationText = Get-Content -LiteralPath $migrationPath -Raw -Encoding UTF8
    $normalizedInstall = $installText -replace '\r\n?', "`n"
    $normalizedMigration = $migrationText -replace '\r\n?', "`n"
    if ($normalizedInstall.Trim() -cne $normalizedMigration.Trim()) {
        throw "Generated Domain fresh install/V1 migration drift: $vendor"
    }
    if ([regex]::Matches($installText, '(?im)\bCREATE\s+TABLE\b').Count -ne 1) {
        throw "Generated Domain must create exactly one Sample table: $vendor"
    }
    if (-not $installText.Contains([string]$contract.verifyContract.requiredTable) -or
            -not $installText.Contains("@CPF_SCHEMA_NAME@")) {
        throw "Generated Domain physical schema/table token contract missing: $vendor"
    }
    foreach ($column in $contractColumns) {
        if ($installText -notmatch "(?im)^\s*$([regex]::Escape([string]$column))\s+") {
            throw "Generated Domain install column missing: vendor=$vendor column=$column"
        }
    }
    if ([regex]::Matches($installText, '(?i)\bCHECK\s*\(').Count -ne $contractChecks.Count -or
            $installText -notmatch "(?i)status_code\s+IN\s*\(\s*'ACTIVE'\s*,\s*'INACTIVE'\s*\)" -or
            $installText -notmatch "(?i)deleted_yn\s+IN\s*\(\s*'Y'\s*,\s*'N'\s*\)") {
        throw "Generated Domain Check Constraint contract mismatch: $vendor"
    }

    $mapperPath = Join-Path $templateRoot "runtime/mybatis/__MAPPER__.xml.template"
    $mapperText = Get-Content -LiteralPath $mapperPath -Raw -Encoding UTF8
    $mapperStatements = @([regex]::Matches(
            $mapperText,
            '<(?:select|insert|update|delete)\s+id="([^"]+)"') |
            ForEach-Object { $_.Groups[1].Value } |
            Sort-Object -Unique)
    if (@(Compare-Object $requiredStatements $mapperStatements).Count -ne 0) {
        throw "Generated Domain Runtime statement contract mismatch: $vendor"
    }
    if (-not $mapperText.Contains("@CPF_SCHEMA_NAME@") -or
            -not $mapperText.Contains([string]$contract.verifyContract.requiredTable)) {
        throw "Generated Domain Runtime query is not schema/table metadata-driven: $vendor"
    }

    $verifyPath = Join-Path $templateRoot "verify/90_verify.sql.template"
    $verifyText = Get-Content -LiteralPath $verifyPath -Raw -Encoding UTF8
    foreach ($column in $contractColumns) {
        if (-not $verifyText.Contains([string]$column)) {
            throw "Generated Domain Verify column contract missing: vendor=$vendor column=$column"
        }
    }
    if (-not $verifyText.Contains([string]$verifyCheckMarkers[$vendor])) {
        throw "Generated Domain Verify does not validate Check Constraints: $vendor"
    }

    $seedPath = Join-Path $templateRoot "seed/20_product_seed.sql.template"
    $seedText = Get-Content -LiteralPath $seedPath -Raw -Encoding UTF8
    if ($seedText -match '(?is)\bINSERT\s+INTO\s+.*@CPF_TABLE_PREFIX@_sample_item') {
        throw "Product Seed must not create Generated Domain business sample rows: $vendor"
    }

    $principalPath = Join-Path $templateRoot "provision/02_principals.sql.template"
    $principalText = Get-Content -LiteralPath $principalPath -Raw -Encoding UTF8
    foreach ($requiredPrincipalToken in @(
            "@CPF_MIGRATION_USERNAME@",
            "@CPF_RUNTIME_USERNAME@")) {
        if (-not $principalText.Contains($requiredPrincipalToken)) {
            throw "Generated Domain principal template token missing: vendor=$vendor token=$requiredPrincipalToken"
        }
    }
    if ($principalText -notmatch "@CPF_(?:MIGRATION|RUNTIME)_PASSWORD_(?:HEX|SQL_LITERAL)@") {
        throw "Generated Domain principal template does not use secret injection tokens: $vendor"
    }
    if ($vendor -in @("mariadb", "mysql", "postgresql") -and
            -not $principalText.Contains("@CPF_DATABASE_NAME@")) {
        throw "Generated Domain principal template does not select the generated database: $vendor"
    }
    if ($vendor -in @("postgresql", "sqlserver") -and
            -not $principalText.Contains("@CPF_SCHEMA_NAME@")) {
        throw "Generated Domain principal template does not provision the selected schema: $vendor"
    }
}
Write-Host "CPF Generator Golden Path static gate passed."
