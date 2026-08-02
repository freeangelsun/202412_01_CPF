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
    "implementation platform('com.cpf:cpf-platform-bom:",
    "implementation 'com.cpf.core:cpf-core:",
    "implementation 'com.cpf.common:cpf-common:",
    "implementation 'org.springframework:spring-web'",
    "exclude group: 'org.springframework', module: 'spring-web'",
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
    '@Profile("!prod & !production & !stage & !staging & (local | test | edu)")'
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
$vendorManifestPath = Join-Path $Root "cpf-tools/db/vendor-pack-manifest.json"
if (-not (Test-Path $contractPath -PathType Leaf)) {
    throw "Central Generated Domain contract missing: $contractPath"
}
if (-not (Test-Path $metadataSchemaPath -PathType Leaf)) {
    throw "Generated Domain metadata schema missing: $metadataSchemaPath"
}
if (-not (Test-Path $vendorManifestPath -PathType Leaf)) {
    throw "CPF DB Vendor manifest missing: $vendorManifestPath"
}
$contract = Get-Content $contractPath -Raw -Encoding UTF8 | ConvertFrom-Json
$metadataSchema = Get-Content $metadataSchemaPath -Raw -Encoding UTF8 | ConvertFrom-Json
$vendorManifest = Get-Content $vendorManifestPath -Raw -Encoding UTF8 | ConvertFrom-Json

$contractVendors = @($contract.supportedVendors | Sort-Object -Unique)
$schemaVendors = @($metadataSchema.properties.databaseVendor.enum | Sort-Object -Unique)
$manifestVendors = @($vendorManifest.supportedVendors | Sort-Object -Unique)
$officialVendors = @($vendorManifest.officialVendors | Sort-Object -Unique)
if ($contractVendors.Count -eq 0 -or
        @(Compare-Object $contractVendors $schemaVendors).Count -ne 0 -or
        @(Compare-Object $contractVendors $manifestVendors).Count -ne 0 -or
        @(Compare-Object $contractVendors $officialVendors).Count -ne 0) {
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
        [object] $ContractNode,
        [object] $SchemaNode,
        [string] $ContractProperty,
        [string] $SchemaProperty,
        [int] $ExpectedCount,
        [string] $Label
    )
    $contractValues = @($ContractNode.$ContractProperty | Sort-Object -Unique)
    $schemaValues = @(
        $SchemaNode.properties.$SchemaProperty.items.enum |
            Sort-Object -Unique
    )
    if ($contractValues.Count -ne $ExpectedCount -or
            @(Compare-Object $contractValues $schemaValues).Count -ne 0) {
        throw "$Label $ContractProperty contract/schema mismatch."
    }
    return $contractValues
}

$minimalSchema = $metadataSchema.properties.minimalTransactionContract
$ledgerSchema = $metadataSchema.properties.idempotencyLedgerContract
$contractColumns = Assert-ContractSchemaList `
        $contract.verifyContract $minimalSchema "requiredColumns" "requiredColumns" 14 `
        "Minimal Transaction"
$contractKeys = Assert-ContractSchemaList `
        $contract.verifyContract $minimalSchema "requiredKeys" "requiredKeys" 2 `
        "Minimal Transaction"
$contractIndexes = Assert-ContractSchemaList `
        $contract.verifyContract $minimalSchema "requiredIndexes" "requiredIndexes" 3 `
        "Minimal Transaction"
$contractChecks = Assert-ContractSchemaList `
        $contract.verifyContract $minimalSchema "requiredChecks" "requiredChecks" 2 `
        "Minimal Transaction"
$contractOperations = Assert-ContractSchemaList `
        $contract.verifyContract $minimalSchema "requiredOperations" "operations" 22 `
        "Minimal Transaction"
$ledgerColumns = Assert-ContractSchemaList `
        $contract.idempotencyLedgerContract $ledgerSchema "requiredColumns" "requiredColumns" 8 `
        "Idempotency Ledger"
$ledgerKeys = Assert-ContractSchemaList `
        $contract.idempotencyLedgerContract $ledgerSchema "requiredKeys" "requiredKeys" 2 `
        "Idempotency Ledger"
$ledgerIndexes = Assert-ContractSchemaList `
        $contract.idempotencyLedgerContract $ledgerSchema "requiredIndexes" "requiredIndexes" 2 `
        "Idempotency Ledger"
$ledgerChecks = Assert-ContractSchemaList `
        $contract.idempotencyLedgerContract $ledgerSchema "requiredChecks" "requiredChecks" 2 `
        "Idempotency Ledger"
foreach ($requiredMetadataField in @(
        "templateContractVersion",
        "capabilities",
        "physicalTableContract",
        "minimalTransactionContract",
        "idempotencyLedgerContract")) {
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
        "tableRole",
        "logicalTable",
        "requiredColumns",
        "transactionIdWidth",
        "requiredKeys",
        "requiredIndexes",
        "requiredChecks",
        "operations")) {
    if ($requiredMinimalField -notin @($metadataSchema.properties.minimalTransactionContract.required)) {
        throw "Generated Domain minimalTransactionContract must require $requiredMinimalField."
    }
}
foreach ($requiredLedgerField in @(
        "model",
        "tableRole",
        "logicalTable",
        "requiredColumns",
        "transactionIdWidth",
        "requiredKeys",
        "requiredIndexes",
        "requiredChecks",
        "replayPolicy",
        "logicalDeleteReplayRequired")) {
    if ($requiredLedgerField -notin @($ledgerSchema.required)) {
        throw "Generated Domain idempotencyLedgerContract must require $requiredLedgerField."
    }
}
if ([int]$contract.physicalTableContract.totalTables -ne 2 -or
        [int]$contract.physicalTableContract.businessTableCount -ne 1 -or
        [int]$contract.physicalTableContract.supportLedgerCount -ne 1 -or
        [bool]$contract.physicalTableContract.additionalTablesAllowed -or
        [int]$metadataSchema.properties.physicalTableContract.properties.totalTables.const -ne 2 -or
        [int]$metadataSchema.properties.physicalTableContract.properties.businessTableCount.const -ne 1 -or
        [int]$metadataSchema.properties.physicalTableContract.properties.supportLedgerCount.const -ne 1 -or
        [bool]$metadataSchema.properties.physicalTableContract.properties.additionalTablesAllowed.const) {
    throw "Generated Domain physical table contract must be one business table plus one support ledger."
}
if ([string]$contract.verifyContract.tableRole -ne "business-sample" -or
        [int]$contract.verifyContract.transactionIdWidth -ne 34 -or
        [string]$contract.idempotencyLedgerContract.tableRole -ne "non-business-support-ledger" -or
        [int]$contract.idempotencyLedgerContract.transactionIdWidth -ne 34 -or
        [string]$contract.idempotencyLedgerContract.replayPolicy -ne
                "same-key-and-request-hash-replay-different-hash-conflict" -or
        -not [bool]$contract.idempotencyLedgerContract.logicalDeleteReplayRequired) {
    throw "Generated Domain Sample/Ledger role or replay contract mismatch."
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
$expectedRuntimeStatements = @(
    "search",
    "count",
    "findBySampleKey",
    "findById",
    "findIdempotency",
    "findForUpdate",
    "cursorSlice",
    "insert",
    "insertIdempotency",
    "updateWithVersion",
    "logicalDeleteWithVersion"
) | Sort-Object -Unique
if ($requiredStatements.Count -ne 11 -or
        @(Compare-Object $expectedRuntimeStatements $requiredStatements).Count -ne 0) {
    throw "Generated Domain Runtime statement contract must contain the exact 11-statement set."
}
$verifyCheckMarkers = @{
    mariadb = "information_schema.table_constraints"
    postgresql = "pg_constraint"
    oracle = "all_constraints"
}
$sampleIndexMarkers = @{
    mariadb = @(
        "ix_@CPF_TABLE_PREFIX@_sample_item_idem",
        "ix_@CPF_TABLE_PREFIX@_sample_item_status",
        "ix_@CPF_TABLE_PREFIX@_sample_item_tx"
    )
    postgresql = @(
        "ix_@CPF_TABLE_PREFIX@_sample_item_idem",
        "ix_@CPF_TABLE_PREFIX@_sample_item_status",
        "ix_@CPF_TABLE_PREFIX@_sample_item_tx"
    )
    oracle = @(
        "ix_@CPF_TABLE_PREFIX@_sample_idem",
        "ix_@CPF_TABLE_PREFIX@_sample_status",
        "ix_@CPF_TABLE_PREFIX@_sample_tx"
    )
}
function Get-CreateTableColumnNames {
    param([string] $CreateTableSegment)
    return @($CreateTableSegment -split '\r?\n' |
            ForEach-Object {
                if ($_ -match '(?i)^\s*[`"]?([a-z][a-z0-9_]*)[`"]?\s+(?:BIGINT|VARCHAR2?|CHAR|DATETIME|TIMESTAMP|NUMBER)\b') {
                    $Matches[1].ToLowerInvariant()
                }
            } |
            Where-Object { -not [string]::IsNullOrWhiteSpace($_) } |
            Sort-Object -Unique)
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
    $sampleTableToken = [string]$contract.verifyContract.requiredTable
    $ledgerTableToken = [string]$contract.idempotencyLedgerContract.requiredTable
    $createTableMatches = [regex]::Matches($installText, '(?im)\bCREATE\s+TABLE\b')
    $sampleCreateMatches = [regex]::Matches(
            $installText,
            "(?im)\bCREATE\s+TABLE(?:\s+IF\s+NOT\s+EXISTS)?[^\r\n]*$([regex]::Escape($sampleTableToken))(?!_idem)\b")
    $ledgerCreateMatches = [regex]::Matches(
            $installText,
            "(?im)\bCREATE\s+TABLE(?:\s+IF\s+NOT\s+EXISTS)?[^\r\n]*$([regex]::Escape($ledgerTableToken))\b")
    if ($createTableMatches.Count -ne 2 -or
            $sampleCreateMatches.Count -ne 1 -or
            $ledgerCreateMatches.Count -ne 1) {
        throw "Generated Domain must create exactly one business Sample table and one support ledger: $vendor"
    }
    if (-not $installText.Contains($sampleTableToken) -or
            -not $installText.Contains($ledgerTableToken) -or
            -not $installText.Contains("@CPF_SCHEMA_NAME@")) {
        throw "Generated Domain physical schema/table token contract missing: $vendor"
    }
    if ($ledgerCreateMatches[0].Index -le $sampleCreateMatches[0].Index) {
        throw "Generated Domain support ledger must be created after its business Sample parent: $vendor"
    }
    $sampleSegment = $installText.Substring(
            $sampleCreateMatches[0].Index,
            $ledgerCreateMatches[0].Index - $sampleCreateMatches[0].Index)
    $ledgerSegment = $installText.Substring($ledgerCreateMatches[0].Index)
    $sampleColumns = Get-CreateTableColumnNames $sampleSegment
    $actualLedgerColumns = Get-CreateTableColumnNames $ledgerSegment
    if ($sampleColumns.Count -ne 14 -or
            @(Compare-Object $contractColumns $sampleColumns).Count -ne 0) {
        throw "Generated Domain business Sample column contract mismatch: $vendor"
    }
    if ($actualLedgerColumns.Count -ne 8 -or
            @(Compare-Object $ledgerColumns $actualLedgerColumns).Count -ne 0) {
        throw "Generated Domain idempotency ledger column contract mismatch: $vendor"
    }
    $missingSampleIndexes = @($sampleIndexMarkers[$vendor] |
            Where-Object { -not $sampleSegment.Contains([string]$_) })
    if ([regex]::Matches($sampleSegment, '(?i)\bPRIMARY\s+KEY\b').Count -ne 1 -or
            [regex]::Matches($sampleSegment, '(?i)\bUNIQUE\b').Count -ne 1 -or
            $sampleSegment -match '(?im)^\s*(?:UNIQUE[^\r\n]*idempotency_key|idempotency_key[^\r\n]*\bUNIQUE\b)' -or
            $missingSampleIndexes.Count -gt 0 -or
            [regex]::Matches($sampleSegment, '(?i)\bCHECK\s*\(').Count -ne 2 -or
            $sampleSegment -notmatch "(?i)status_code\s+IN\s*\(\s*'ACTIVE'\s*,\s*'INACTIVE'\s*\)" -or
            $sampleSegment -notmatch "(?i)deleted_yn\s+IN\s*\(\s*'Y'\s*,\s*'N'\s*\)") {
        throw "Generated Domain business Sample key/index/check contract mismatch: $vendor"
    }
    if ([regex]::Matches($ledgerSegment, '(?i)\bPRIMARY\s+KEY\b').Count -ne 1 -or
            [regex]::Matches($ledgerSegment, '(?i)\b(?:FOREIGN\s+KEY|REFERENCES)\b').Count -lt 1 -or
            -not $ledgerSegment.Contains("ix_@CPF_TABLE_PREFIX@_sample_idem_item") -or
            -not $ledgerSegment.Contains("ix_@CPF_TABLE_PREFIX@_sample_idem_tx") -or
            [regex]::Matches($ledgerSegment, '(?i)\bCHECK\s*\(').Count -ne 2 -or
            $ledgerSegment -notmatch "(?i)operation_code\s+IN\s*\(\s*'CREATE'\s*,\s*'UPDATE'\s*,\s*'DELETE'\s*\)" -or
            $ledgerSegment -notmatch "(?i)deleted_yn\s+IN\s*\(\s*'Y'\s*,\s*'N'\s*\)") {
        throw "Generated Domain idempotency ledger key/index/check contract mismatch: $vendor"
    }

    $rollbackPath = Join-Path $templateRoot "rollback/R1__remove___DOMAIN___domain.sql.template"
    $rollbackText = Get-Content -LiteralPath $rollbackPath -Raw -Encoding UTF8
    if ([regex]::Matches($rollbackText, '(?im)\bDROP\s+TABLE\b').Count -ne 2 -or
            -not $rollbackText.Contains($sampleTableToken) -or
            -not $rollbackText.Contains($ledgerTableToken)) {
        throw "Generated Domain rollback must remove both and only the Sample/Ledger tables: $vendor"
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
            -not $mapperText.Contains($sampleTableToken) -or
            -not $mapperText.Contains($ledgerTableToken)) {
        throw "Generated Domain Runtime query is not schema/table metadata-driven: $vendor"
    }

    $verifyPath = Join-Path $templateRoot "verify/90_verify.sql.template"
    $verifyText = Get-Content -LiteralPath $verifyPath -Raw -Encoding UTF8
    foreach ($column in $contractColumns) {
        if (-not $verifyText.Contains([string]$column)) {
            throw "Generated Domain Sample Verify column contract missing: vendor=$vendor column=$column"
        }
    }
    foreach ($column in $ledgerColumns) {
        if (-not $verifyText.Contains([string]$column)) {
            throw "Generated Domain Ledger Verify column contract missing: vendor=$vendor column=$column"
        }
    }
    if (-not $verifyText.Contains([string]$verifyCheckMarkers[$vendor]) -or
            -not $verifyText.Contains("generated_domain_sample_verify") -or
            -not $verifyText.Contains("generated_domain_idempotency_verify") -or
            -not $verifyText.Contains($sampleTableToken) -or
            -not $verifyText.Contains($ledgerTableToken) -or
            $verifyText -notmatch '(?i)(?:character_maximum_length|char_length)\s*=\s*34') {
        throw "Generated Domain Verify does not fail-closed validate Sample and Ledger independently: $vendor"
    }

    $seedPath = Join-Path $templateRoot "seed/20_product_seed.sql.template"
    $seedText = Get-Content -LiteralPath $seedPath -Raw -Encoding UTF8
    if ($seedText -match '(?is)\bINSERT\s+INTO\s+.*@CPF_TABLE_PREFIX@_sample_item') {
        throw "Product Seed must not create Generated Domain Sample/Ledger rows: $vendor"
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
    if ($vendor -in @("mariadb", "postgresql") -and
            -not $principalText.Contains("@CPF_DATABASE_NAME@")) {
        throw "Generated Domain principal template does not select the generated database: $vendor"
    }
    if ($vendor -eq "postgresql" -and
            -not $principalText.Contains("@CPF_SCHEMA_NAME@")) {
        throw "Generated Domain principal template does not provision the selected schema: $vendor"
    }
}
Write-Host "CPF Generator Golden Path static gate passed."
