param(
    [string] $Root = (Resolve-Path (Join-Path $PSScriptRoot "../../..")).Path
)
$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$officialVendors = @("mariadb", "postgresql", "oracle")
$failures = [System.Collections.Generic.List[string]]::new()

function Add-Failure([string] $Message) { $script:failures.Add($Message) }
function Normalize-Set([object[]] $Values) {
    return @($Values | ForEach-Object { [string] $_ } | Where-Object { $_ } | Sort-Object -CaseSensitive -Unique)
}
function Compare-Set([object[]] $Left, [object[]] $Right) {
    return ((Normalize-Set $Left) -join "`n") -ceq ((Normalize-Set $Right) -join "`n")
}
function Read-Text([string] $Path) {
    return [System.IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8)
}
function Strip-SqlNoise([string] $Sql) {
    $text = [regex]::Replace($Sql, '(?s)/\*.*?\*/', ' ')
    $text = [regex]::Replace($text, '(?m)--[^\r\n]*$', ' ')
    $text = [regex]::Replace($text, "'(?:''|[^'])*'", "''")
    return $text
}
function Get-NamedParameters([string] $Sql) {
    $clean = Strip-SqlNoise $Sql
    return Normalize-Set @([regex]::Matches($clean, '(?<!:):(?<p>[A-Za-z][A-Za-z0-9_]*)') |
        ForEach-Object { $_.Groups['p'].Value })
}
function Get-PositionalParameterCount([string] $Sql) {
    $clean = Strip-SqlNoise $Sql
    return ([regex]::Matches($clean, '\?')).Count
}
function Get-ResultAliases([string] $Sql) {
    $clean = Strip-SqlNoise $Sql
    # Query packs keep projection aliases stable across vendors. Comparing explicit AS aliases catches DTO/result drift.
    return Normalize-Set @([regex]::Matches($clean, '(?i)\bAS\s+(?:"(?<qa>[A-Za-z][A-Za-z0-9_]*)"|(?<a>[A-Za-z][A-Za-z0-9_]*))') |
        ForEach-Object { if ($_.Groups['qa'].Success) { $_.Groups['qa'].Value } else { $_.Groups['a'].Value } })
}
function Test-RowQuery([string] $Sql) {
    return (Strip-SqlNoise $Sql) -match '(?is)^\s*(?:SELECT|WITH)\b'
}
function Get-SourceKeys([string[]] $Scopes) {
    $keys = [System.Collections.Generic.List[string]]::new()
    foreach ($scope in $Scopes) {
        $sourceRoot = Join-Path $Root ($scope -replace '/', [System.IO.Path]::DirectorySeparatorChar)
        if (-not (Test-Path -LiteralPath $sourceRoot -PathType Container)) {
            Add-Failure "Query source scope missing: $scope"
            continue
        }
        foreach ($file in Get-ChildItem -LiteralPath $sourceRoot -Recurse -File -Filter '*.java') {
            $text = Read-Text $file.FullName
            foreach ($m in [regex]::Matches($text, '\.required\(\s*"(?<key>[a-z][a-z0-9-]{1,127})"\s*\)')) {
                $keys.Add($m.Groups['key'].Value)
            }
        }
    }
    return Normalize-Set $keys
}
function Get-SourceResources([string[]] $Scopes) {
    $resources = [System.Collections.Generic.List[string]]::new()
    foreach ($scope in $Scopes) {
        $sourceRoot = Join-Path $Root ($scope -replace '/', [System.IO.Path]::DirectorySeparatorChar)
        if (-not (Test-Path -LiteralPath $sourceRoot -PathType Container)) {
            Add-Failure "Query source scope missing: $scope"
            continue
        }
        foreach ($file in Get-ChildItem -LiteralPath $sourceRoot -Recurse -File -Filter '*.java') {
            $text = Read-Text $file.FullName
            foreach ($m in [regex]::Matches(
                    $text,
                    'CpfCommonSqlResourceLoader\.load\(\s*"(?<resource>(?:[a-z0-9][a-z0-9_.-]*/)*[a-z0-9][a-z0-9_.-]*\.sql)"\s*\)')) {
                $resources.Add($m.Groups['resource'].Value)
            }
        }
    }
    return Normalize-Set $resources
}
function Build-StatementIndex([object[]] $Statements) {
    $index = @{}
    foreach ($s in $Statements) { $index[[string]$s.key] = $s }
    return $index
}
function Get-StatementUsage([object] $Statement) {
    if ($null -eq $Statement) { return $null }
    $usageProperty = $Statement.PSObject.Properties['usage']
    # BAT predates explicit ACTIVE/STAGED lifecycle metadata. A registered
    # legacy statement remains valid without inferring source lifecycle state.
    if ($null -eq $usageProperty) { return 'LEGACY' }
    return [string] $usageProperty.Value
}
function Test-Module(
    [string] $ModuleCode,
    [string] $ParameterStyle,
    [string] $GeneratedPackPath,
    [string[]] $SourceScopes,
    [object[]] $Statements
) {
    $statementIndex = Build-StatementIndex $Statements
    $hasLifecycleMetadata = @(
        $Statements | Where-Object { $null -ne $_.PSObject.Properties['usage'] }
    ).Count -gt 0
    $resourceStatements = @(
        $Statements | Where-Object { $null -ne $_.PSObject.Properties['resource'] }
    )
    if ($resourceStatements.Count -gt 0) {
        if ($resourceStatements.Count -ne $Statements.Count) {
            Add-Failure "Query module mixes repository keys and portable resources: module=$ModuleCode"
            return
        }
        $sourceResources = Get-SourceResources $SourceScopes
        $resourceIndex = @{}
        foreach ($statement in $resourceStatements) {
            $resourceIndex[[string]$statement.resource] = $statement
        }
        $targetRoot = Join-Path $Root ($GeneratedPackPath -replace '/', [System.IO.Path]::DirectorySeparatorChar)
        if (-not (Test-Path -LiteralPath $targetRoot -PathType Container)) {
            Add-Failure "Portable query target missing: module=$ModuleCode path=$GeneratedPackPath"
            return
        }
        $actualResources = Normalize-Set @(
            Get-ChildItem -LiteralPath $targetRoot -Recurse -File -Filter '*.sql' |
                ForEach-Object {
                    [System.IO.Path]::GetRelativePath($targetRoot, $_.FullName).Replace('\', '/')
                }
        )
        foreach ($resource in $sourceResources) {
            if (-not $resourceIndex.ContainsKey($resource)) {
                Add-Failure "Source portable query has no contract resource: module=$ModuleCode resource=$resource"
            }
            if ($actualResources -cnotcontains $resource) {
                Add-Failure "Source portable query has no generated resource: module=$ModuleCode resource=$resource"
            }
        }
        foreach ($resource in $actualResources) {
            if (-not $resourceIndex.ContainsKey($resource)) {
                Add-Failure "Orphan portable query resource: module=$ModuleCode resource=$resource"
            }
        }
        foreach ($statement in $resourceStatements) {
            $resource = [string]$statement.resource
            if ((Get-StatementUsage $statement) -ceq 'ACTIVE' -and $sourceResources -cnotcontains $resource) {
                Add-Failure "ACTIVE portable query has no Source consumer: module=$ModuleCode resource=$resource consumer=$($statement.consumer)"
            }
            $targetPath = Join-Path $targetRoot ($resource -replace '/', [System.IO.Path]::DirectorySeparatorChar)
            if (-not (Test-Path -LiteralPath $targetPath -PathType Leaf)) { continue }
            $sql = Read-Text $targetPath
            if ($sql -match '@[A-Z0-9_]+@') {
                Add-Failure "Unresolved portable SQL token: module=$ModuleCode resource=$resource"
            }
            $actualParameterCount = Get-PositionalParameterCount $sql
            if ($ParameterStyle -ceq 'JDBC_POSITIONAL' -and
                    $actualParameterCount -ne [int]$statement.parameterCount) {
                Add-Failure "Portable positional parameter mismatch: module=$ModuleCode resource=$resource contract=$($statement.parameterCount) sql=$actualParameterCount"
            }
        }
        return
    }

    $sourceKeys = Get-SourceKeys $SourceScopes
    $vendorKeys = @{}
    $vendorSql = @{}

    foreach ($vendor in $officialVendors) {
        $repoRoot = Join-Path $Root "cpf-tools/db/vendor/$vendor/$GeneratedPackPath"
        if (-not (Test-Path -LiteralPath $repoRoot -PathType Container)) {
            Add-Failure "Runtime query repository missing: module=$ModuleCode vendor=$vendor"
            continue
        }
        $files = @(Get-ChildItem -LiteralPath $repoRoot -File -Filter '*.sql')
        $vendorKeys[$vendor] = Normalize-Set @($files | ForEach-Object { $_.BaseName })
        $map = @{}
        foreach ($file in $files) { $map[$file.BaseName] = Read-Text $file.FullName }
        $vendorSql[$vendor] = $map
    }

    if ($vendorKeys.Count -eq $officialVendors.Count) {
        $baseKeys = $vendorKeys['mariadb']
        foreach ($vendor in @('postgresql','oracle')) {
            if (-not (Compare-Set $baseKeys $vendorKeys[$vendor])) {
                Add-Failure "Vendor query-key parity mismatch: module=$ModuleCode mariadb<>$vendor"
            }
        }
    }

    $allSqlKeys = Normalize-Set @($vendorKeys.Values | ForEach-Object { $_ })
    foreach ($key in $sourceKeys) {
        foreach ($vendor in $officialVendors) {
            if (-not $vendorSql.ContainsKey($vendor) -or -not $vendorSql[$vendor].ContainsKey($key)) {
                Add-Failure "Source query has no SQL resource: module=$ModuleCode vendor=$vendor key=$key"
            }
        }
    }
    foreach ($key in $allSqlKeys) {
        if ($sourceKeys -ccontains $key) { continue }
        $statement = $statementIndex[$key]
        $usage = Get-StatementUsage $statement
        if ($null -eq $statement -or $usage -notin @('STAGED', 'LEGACY')) {
            Add-Failure "Orphan SQL resource is not explicitly STAGED: module=$ModuleCode key=$key"
        }
    }

    foreach ($statement in $Statements) {
        $key = [string]$statement.key
        if ((Get-StatementUsage $statement) -ceq 'ACTIVE' -and -not ($sourceKeys -ccontains $key)) {
            Add-Failure "ACTIVE contract query has no Source consumer: module=$ModuleCode key=$key consumer=$($statement.consumer)"
        }
    }

    foreach ($key in $allSqlKeys) {
        if (@($officialVendors | ForEach-Object { $vendorSql.ContainsKey($_) -and $vendorSql[$_].ContainsKey($key) } | Where-Object { -not $_ }).Count -ne 0) {
            continue
        }
        $maria = $vendorSql['mariadb'][$key]
        $pg = $vendorSql['postgresql'][$key]
        $ora = $vendorSql['oracle'][$key]
        foreach ($pair in @(@('postgresql',$pg), @('oracle',$ora))) {
            $vendor = $pair[0]; $sql = [string]$pair[1]
            if ($sql -match '@[A-Z0-9_]+@') { Add-Failure "Unresolved SQL token: module=$ModuleCode vendor=$vendor key=$key" }
            if ($vendor -ne 'postgresql' -and $sql -match '(?i)\bON\s+CONFLICT\b') { Add-Failure "PostgreSQL syntax leaked: module=$ModuleCode vendor=$vendor key=$key" }
            if ($vendor -eq 'oracle' -and $sql -match '(?i)\bLIMIT\s+(?:\d+|\?|:[A-Za-z])') { Add-Failure "LIMIT leaked to Oracle: module=$ModuleCode key=$key" }
            if ($sql -match '(?i)\bON\s+DUPLICATE\s+KEY\b') { Add-Failure "MariaDB UPSERT leaked: module=$ModuleCode vendor=$vendor key=$key" }
        }

        if ($ParameterStyle -ceq 'SPRING_NAMED') {
            $expected = Get-NamedParameters $maria
            if (-not (Compare-Set $expected (Get-NamedParameters $pg))) { Add-Failure "Named parameter parity mismatch: module=$ModuleCode key=$key vendor=postgresql" }
            if (-not (Compare-Set $expected (Get-NamedParameters $ora))) { Add-Failure "Named parameter parity mismatch: module=$ModuleCode key=$key vendor=oracle" }
            $contractStatement = $statementIndex[$key]
            if ($null -ne $contractStatement -and $null -ne $contractStatement.PSObject.Properties['parameters']) {
                if (-not (Compare-Set $expected @($contractStatement.parameters))) {
                    Add-Failure "Named parameter contract mismatch: module=$ModuleCode key=$key contract=$(@($contractStatement.parameters) -join ',') sql=$($expected -join ',')"
                }
            }
        } else {
            $expected = Get-PositionalParameterCount $maria
            if ($expected -ne (Get-PositionalParameterCount $pg)) { Add-Failure "Positional parameter parity mismatch: module=$ModuleCode key=$key vendor=postgresql" }
            if ($expected -ne (Get-PositionalParameterCount $ora)) { Add-Failure "Positional parameter parity mismatch: module=$ModuleCode key=$key vendor=oracle" }
            $contractStatement = $statementIndex[$key]
            if ($null -ne $contractStatement -and $null -ne $contractStatement.PSObject.Properties['parameterCount']) {
                if ($expected -ne [int]$contractStatement.parameterCount) {
                    Add-Failure "Positional parameter contract mismatch: module=$ModuleCode key=$key contract=$($contractStatement.parameterCount) sql=$expected"
                }
            }
        }

        $contractStatement = $statementIndex[$key]
        $resultFieldsProperty = if ($null -ne $contractStatement) {
            $contractStatement.PSObject.Properties['resultFields']
        } else {
            $null
        }
        if ($hasLifecycleMetadata -and
                ((Test-RowQuery $maria) -or $null -ne $resultFieldsProperty)) {
            $aliases = Get-ResultAliases $maria
            if (-not (Compare-Set $aliases (Get-ResultAliases $pg))) { Add-Failure "Result alias parity mismatch: module=$ModuleCode key=$key vendor=postgresql" }
            if (-not (Compare-Set $aliases (Get-ResultAliases $ora))) { Add-Failure "Result alias parity mismatch: module=$ModuleCode key=$key vendor=oracle" }
            if ($null -ne $resultFieldsProperty -and
                    -not (Compare-Set $aliases @($contractStatement.resultFields))) {
                    Add-Failure "Result alias contract mismatch: module=$ModuleCode key=$key"
            }
        }
    }
}

$platformPath = Join-Path $Root 'cpf-tools/db/metadata/platform-runtime-query-contract.json'
$batPath = Join-Path $Root 'cpf-tools/db/metadata/bat-runtime-query-contract.json'
$platform = Get-Content -Raw -Encoding UTF8 -LiteralPath $platformPath | ConvertFrom-Json
$bat = Get-Content -Raw -Encoding UTF8 -LiteralPath $batPath | ConvertFrom-Json
if (-not (Compare-Set @($platform.vendors) $officialVendors)) {
    Add-Failure "Official query vendors must be exactly: $($officialVendors -join ',')"
}
foreach ($module in @($platform.modules)) {
    Test-Module -ModuleCode ([string]$module.module) -ParameterStyle ([string]$module.parameterStyle) `
        -GeneratedPackPath ([string]$module.generatedPackPath) `
        -SourceScopes @($module.sourceScopes) -Statements @($module.statements)
}

function Test-BatCanonicalPhysicalIdentifiers {
    $schemaPath = Join-Path $Root 'cpf-tools/db/canonical/platform-schema.json'
    $schema = Get-Content -Raw -Encoding UTF8 -LiteralPath $schemaPath | ConvertFrom-Json
    $legacyMappings = @(
        $schema.tables | Where-Object {
            [string]$_.module -ceq 'bat' -and
            $null -ne $_.PSObject.Properties['currentName'] -and
            $null -ne $_.PSObject.Properties['targetTableName'] -and
            -not [string]::IsNullOrWhiteSpace([string]$_.currentName) -and
            ([string]$_.currentName -cne [string]$_.targetTableName)
        } | ForEach-Object {
            [pscustomobject]@{
                legacy = [string]$_.currentName
                target = [string]$_.targetTableName
            }
        }
    )
    $roots = [System.Collections.Generic.List[string]]::new()
    $roots.Add((Join-Path $Root 'cpf-tools/db/runtime-template/bat'))
    foreach ($vendor in $officialVendors) {
        $roots.Add((Join-Path $Root "cpf-tools/db/vendor/$vendor/runtime/bat"))
    }
    foreach ($queryRoot in $roots) {
        if (-not (Test-Path -LiteralPath $queryRoot -PathType Container)) {
            Add-Failure "BAT physical query root missing: $queryRoot"
            continue
        }
        foreach ($file in Get-ChildItem -LiteralPath $queryRoot -Recurse -File |
                Where-Object { $_.Name -match '\.sql(?:\.template)?$' }) {
            $text = Strip-SqlNoise (Read-Text $file.FullName)
            foreach ($mapping in $legacyMappings) {
                $pattern = '(?<![A-Za-z0-9_])' + [regex]::Escape($mapping.legacy) + '(?![A-Za-z0-9_])'
                if ([regex]::IsMatch($text, $pattern)) {
                    $relative = [System.IO.Path]::GetRelativePath($Root, $file.FullName).Replace('\', '/')
                    Add-Failure "Legacy BAT physical identifier: path=$relative legacy=$($mapping.legacy) canonical=$($mapping.target)"
                }
            }
        }
    }
}
Test-Module -ModuleCode 'bat' -ParameterStyle ([string]$bat.parameterStyle) `
    -GeneratedPackPath ([string]$bat.generatedPackPath) `
    -SourceScopes @($bat.migrationScope) -Statements @($bat.statements)
Test-BatCanonicalPhysicalIdentifiers

if ($failures.Count -gt 0) {
    # Emit the complete independent inventory before failing. Write-Error is
    # terminating under ErrorActionPreference=Stop and previously hid every
    # finding after the first one.
    $failures | ForEach-Object { [Console]::Error.WriteLine("QUERY_CONTRACT_FAILURE: $_") }
    throw "CPF Query Contract integrity gate failed: $($failures.Count) issue(s)."
}
Write-Host "CPF Query Contract integrity gate passed. vendors=$($officialVendors -join ',')"
