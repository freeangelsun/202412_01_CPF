[CmdletBinding()]
param([string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path)

$ErrorActionPreference = 'Stop'
$Root = (Resolve-Path -LiteralPath $Root).Path
. (Join-Path $Root 'cpf-tools/generator/tools/generated-domain-common.ps1')
$enginePath = Join-Path $Root 'cpf-tools/generator/engine/cpf_domain_generator.py'
$schemaPath = Join-Path $Root 'cpf-tools/generator/contracts/cpf-domain.schema.json'
$contractPath = Join-Path $Root 'cpf-tools/generator/contracts/central-domain-template-contract.json'
$vendorManifestPath = Join-Path $Root 'cpf-tools/db/vendor-pack-manifest.json'
$canonicalSchemaPath = Join-Path $Root 'cpf-tools/db/canonical/generated-domain-schema.json'
$rendererPath = Join-Path $Root 'cpf-tools/db/render_generated_domain_template.py'
foreach ($required in @($enginePath, $schemaPath, $contractPath, $vendorManifestPath, $canonicalSchemaPath, $rendererPath)) {
    if (-not (Test-Path -LiteralPath $required -PathType Leaf)) { throw "Golden Path 정본이 없습니다: $required" }
}
$engine = Get-Content -LiteralPath $enginePath -Raw -Encoding UTF8
$schema = Get-Content -LiteralPath $schemaPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 100
$contract = Get-Content -LiteralPath $contractPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 100
$vendorManifest = Get-Content -LiteralPath $vendorManifestPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 100
$canonicalSchema = Get-Content -LiteralPath $canonicalSchemaPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 100

foreach ($requiredKey in @('domain', 'database', 'preset', 'modules', 'generation')) {
    if ($requiredKey -notin @($schema.required)) { throw "cpf-domain.schema.json 필수 key 누락: $requiredKey" }
}
if ([string]$contract.selection.metadataSource -ne 'gradle.properties' -or
        [string]$contract.selection.generatedProjectMetadata -ne 'ABSENT' -or
        [bool]$contract.selection.sourceTreeMutation -or
        [bool]$contract.selection.moduleLocalVendorPack -or
        [string]$contract.businessDatabaseContract.physicalDatabaseProvisioning -ne 'EXTERNAL_DEPLOYMENT_OWNER' -or
        [bool]$contract.businessDatabaseContract.generatedDomainCreatesPhysicalDatabase -or
        [bool]$contract.businessDatabaseContract.generatedDomainCreatesSchema -or
        [string]$contract.businessDatabaseContract.tableQualification -ne 'CONNECTION_DEFAULT_SCHEMA') {
    throw 'Generated Domain metadata-free/DB ownership 계약이 canonical 정책과 다릅니다.'
}
foreach ($forbiddenEngineToken in @(
    'manifest/domain-manifest.json',
    'manifest/generator-ownership.json',
    'cpf-domain-manifest.json'
)) {
    if ($engine.Contains($forbiddenEngineToken)) {
        throw "Canonical Engine이 Generated Project 영구 metadata를 참조합니다: $forbiddenEngineToken"
    }
}

$contractVendors = @($contract.supportedVendors | ForEach-Object { ([string]$_).ToLowerInvariant() } | Sort-Object -Unique)
$manifestVendors = @($vendorManifest.supportedVendors | ForEach-Object { ([string]$_).ToLowerInvariant() } | Sort-Object -Unique)
if ($contractVendors.Count -ne 3 -or @(Compare-Object $contractVendors $manifestVendors).Count -ne 0) {
    throw 'Generated Domain 공식 Vendor 계약이 중앙 Vendor manifest와 다릅니다.'
}
$expectedResources = @(
    'install/10_empty_install.sql.template',
    'seed/20_product_seed.sql.template',
    'migration/V1____DOMAIN___domain.sql.template',
    'rollback/R1__remove___DOMAIN___domain.sql.template',
    'verify/90_verify.sql.template'
) | Sort-Object
if (@(Compare-Object $expectedResources @($contract.requiredTemplates | Sort-Object)).Count -ne 0) {
    throw 'Generated Domain resource set은 Install/Seed/Migration/Rollback/Verify 정확히 5개여야 합니다.'
}
if ([int]$contract.physicalTableContract.totalTables -ne 2 -or
        [int]$contract.physicalTableContract.businessTableCount -ne 1 -or
        [int]$contract.physicalTableContract.supportLedgerCount -ne 1 -or
        [bool]$contract.physicalTableContract.additionalTablesAllowed) {
    throw 'Generated Domain은 business sample 1개와 idempotency ledger 1개만 허용합니다.'
}
if (@($canonicalSchema.tables).Count -ne 2 -or
        [string]$canonicalSchema.businessDatabaseRole -ne 'CUSTOMER_BUSINESS_DB') {
    throw 'Canonical Generated Domain schema의 DB role/물리 Table 수가 올바르지 않습니다.'
}

$allowedTokens = @($contract.tokens | Sort-Object -Unique)
$expectedStatements = @(
    'search', 'count', 'findBySampleKey', 'findById', 'findIdempotency', 'findForUpdate',
    'cursorSlice', 'insert', 'insertIdempotency', 'updateWithVersion', 'logicalDeleteWithVersion'
) | Sort-Object
if (@(Compare-Object $expectedStatements @($contract.runtimeContract.requiredStatements | Sort-Object)).Count -ne 0) {
    throw 'Generated Domain Runtime Mapper statement 계약이 정확한 11개 집합이 아닙니다.'
}
foreach ($vendor in $contractVendors) {
    $templateRoot = Join-Path $Root "cpf-tools/db/generated/domain-template/$vendor"
    foreach ($relative in $expectedResources) {
        $path = Join-Path $templateRoot $relative
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Generated Domain template 누락: $vendor/$relative" }
        $text = Get-Content -LiteralPath $path -Raw -Encoding UTF8
        $unknown = @([regex]::Matches($text, '@(CPF_[A-Z_]+)@') | ForEach-Object { $_.Groups[1].Value } |
            Sort-Object -Unique | Where-Object { $_ -notin $allowedTokens })
        if ($unknown.Count -gt 0) { throw "Generated Domain template 미등록 token: vendor=$vendor tokens=$($unknown -join ',')" }
        if ($text -match '@CPF_SCHEMA_NAME@' -or $text -match '(?i)\bCREATE\s+(?:DATABASE|SCHEMA|USER|ROLE)\b') {
            throw "Generated Domain resource가 connection-default schema/external provisioning 계약을 위반합니다: $vendor/$relative"
        }
    }
    $install = Get-Content -LiteralPath (Join-Path $templateRoot 'install/10_empty_install.sql.template') -Raw -Encoding UTF8
    if ([regex]::Matches($install, '(?im)\bCREATE\s+TABLE\b').Count -ne 2 -or
            -not $install.Contains('@CPF_TABLE_PREFIX@_sample_item') -or
            -not $install.Contains('@CPF_TABLE_PREFIX@_sample_item_idem')) {
        throw "Generated Domain canonical table 구조가 정확히 2개가 아닙니다: $vendor"
    }
    $runtimeMapper = Join-Path $Root "cpf-starters/data/persistence/src/main/resources/cpf-generated-domain-dialect/$vendor/mybatis/__MAPPER__.xml.template"
    if (-not (Test-Path -LiteralPath $runtimeMapper -PathType Leaf)) { throw "Runtime dialect mapper 누락: $vendor" }
    $mapperText = Get-Content -LiteralPath $runtimeMapper -Raw -Encoding UTF8
    $statements = @([regex]::Matches($mapperText, '<(?:select|insert|update|delete)\s+id="([^"]+)"') |
        ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique)
    if (@(Compare-Object $expectedStatements $statements).Count -ne 0 -or $mapperText -match '@CPF_SCHEMA_NAME@') {
        throw "Runtime dialect mapper statement/schema 계약 위반: $vendor"
    }
}

$python = @(Get-CpfPythonCommand)
$arguments = @()
if ($python.Count -gt 1) { $arguments += $python[1..($python.Count - 1)] }
$arguments += @($rendererPath, '--root', $Root, '--check')
& $python[0] @arguments
if ($LASTEXITCODE -ne 0) { throw 'Canonical Generated Domain DB renderer drift 검증 실패' }
$genericity = Invoke-CpfCanonicalCli -Root $Root -Arguments @('verify', 'generator')
if ([string]$genericity.status -ne 'PASS') { throw 'Canonical Generator genericity 검증 실패' }
Write-Host 'CPF Generator Golden Path static gate passed.'
