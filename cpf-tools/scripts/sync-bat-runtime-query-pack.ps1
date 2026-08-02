param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [switch] $Check
)

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"

$Root = (Resolve-Path -LiteralPath $Root).Path
$contractPath = Join-Path $Root "cpf-tools\db\metadata\bat-runtime-query-contract.json"
$contract = Get-Content -Raw -Encoding UTF8 -LiteralPath $contractPath | ConvertFrom-Json
$templateRoot = Join-Path $Root ($contract.authoringTemplateRoot -replace "/", "\")
$vendors = @("mariadb", "postgresql", "oracle")
$utf8NoBom = [System.Text.UTF8Encoding]::new($false)

$vendorTokens = @{
    mariadb = @{
        "@NOW@" = "CURRENT_TIMESTAMP"
        "@NOW3@" = "CURRENT_TIMESTAMP(3)"
        "@NOW6@" = "CURRENT_TIMESTAMP(6)"
        "@LIMIT_100@" = "LIMIT 100"
        "@LIMIT_20@" = "LIMIT 20"
        "@LIMIT_500@" = "LIMIT 500"
        "@LIMIT_PARAM@" = "LIMIT ?"
        "@NOW6_MINUS_30_SECONDS@" = "DATE_SUB(CURRENT_TIMESTAMP(6), INTERVAL 30 SECOND)"
        "@NOW3_MINUS_SECONDS_PARAM@" = "DATE_SUB(CURRENT_TIMESTAMP(3), INTERVAL ? SECOND)"
        "@NOW6_PLUS_60_SECONDS@" = "DATE_ADD(CURRENT_TIMESTAMP(6), INTERVAL 60 SECOND)"
    }
    postgresql = @{
        "@NOW@" = "CURRENT_TIMESTAMP"
        "@NOW3@" = "CURRENT_TIMESTAMP(3)"
        "@NOW6@" = "CURRENT_TIMESTAMP(6)"
        "@LIMIT_100@" = "LIMIT 100"
        "@LIMIT_20@" = "LIMIT 20"
        "@LIMIT_500@" = "LIMIT 500"
        "@LIMIT_PARAM@" = "LIMIT ?"
        "@NOW6_MINUS_30_SECONDS@" = "CURRENT_TIMESTAMP(6) - INTERVAL '30 seconds'"
        "@NOW3_MINUS_SECONDS_PARAM@" = "CURRENT_TIMESTAMP(3) - (? * INTERVAL '1 second')"
        "@NOW6_PLUS_60_SECONDS@" = "CURRENT_TIMESTAMP(6) + INTERVAL '60 seconds'"
    }
    oracle = @{
        "@NOW@" = "CURRENT_TIMESTAMP"
        "@NOW3@" = "CURRENT_TIMESTAMP(3)"
        "@NOW6@" = "CURRENT_TIMESTAMP(6)"
        "@LIMIT_100@" = "FETCH FIRST 100 ROWS ONLY"
        "@LIMIT_20@" = "FETCH FIRST 20 ROWS ONLY"
        "@LIMIT_500@" = "FETCH FIRST 500 ROWS ONLY"
        "@LIMIT_PARAM@" = "FETCH FIRST ? ROWS ONLY"
        "@NOW6_MINUS_30_SECONDS@" = "CURRENT_TIMESTAMP(6) - INTERVAL '30' SECOND"
        "@NOW3_MINUS_SECONDS_PARAM@" = "CURRENT_TIMESTAMP(3) - NUMTODSINTERVAL(?, 'SECOND')"
        "@NOW6_PLUS_60_SECONDS@" = "CURRENT_TIMESTAMP(6) + INTERVAL '60' SECOND"
    }
}

function Assert-SafeStatementKey {
    param([string] $Key)
    if ($Key -cnotmatch "^[a-z][a-z0-9-]{1,63}$") {
        throw "Invalid BAT Runtime Query statement key: $Key"
    }
}

function Get-RenderedSql {
    param(
        [string] $Vendor,
        [object] $Statement
    )

    $key = [string] $Statement.key
    $overridePath = Join-Path $templateRoot "vendor\$Vendor\repository\$key.sql.template"
    $commonPath = Join-Path $templateRoot "repository\$key.sql.template"
    $sourcePath = if (Test-Path -LiteralPath $overridePath -PathType Leaf) {
        $overridePath
    } else {
        $commonPath
    }
    if (-not (Test-Path -LiteralPath $sourcePath -PathType Leaf)) {
        throw "Missing BAT Runtime Query authoring template: vendor=$Vendor key=$key"
    }
    $text = [System.IO.File]::ReadAllText($sourcePath, [System.Text.Encoding]::UTF8)
    foreach ($token in $vendorTokens[$Vendor].Keys) {
        $text = $text.Replace($token, $vendorTokens[$Vendor][$token])
    }
    $text = $text.Trim() + "`n"
    if ($text -match "@[A-Z0-9_]+@") {
        throw "Unresolved BAT Runtime Query token: vendor=$Vendor key=$key token=$($Matches[0])"
    }
    $parameterCount = ([regex]::Matches($text, "\?")).Count
    if ($parameterCount -ne @($Statement.parameters).Count) {
        throw (
            "BAT Runtime Query parameter contract mismatch: vendor=$Vendor key=$key " +
            "expected=$(@($Statement.parameters).Count) actual=$parameterCount"
        )
    }
    return $text
}

if ([string] $contract.module -cne "bat" -or
        [string] $contract.ownerArtifact -cne "cpf-batch" -or
        [string] $contract.parameterStyle -cne "JDBC_POSITIONAL") {
    throw "Invalid BAT Runtime Query contract header."
}

$statements = @($contract.statements)
if ($statements.Count -eq 0) {
    throw "BAT Runtime Query statement contract is empty."
}
$keys = @($statements | ForEach-Object { [string] $_.key })
foreach ($key in $keys) {
    Assert-SafeStatementKey $key
}
if (@($keys | Sort-Object -Unique).Count -ne $keys.Count) {
    throw "Duplicate BAT Runtime Query statement key."
}

$commonTemplates = @(
    Get-ChildItem -LiteralPath (Join-Path $templateRoot "repository") -File -Filter "*.sql.template"
)
$commonKeys = @($commonTemplates | ForEach-Object { $_.BaseName -replace "\.sql$", "" })
$missingCommon = @($keys | Where-Object { $_ -notin $commonKeys })
$unexpectedCommon = @($commonKeys | Where-Object { $_ -notin $keys })
if ($missingCommon.Count -gt 0 -or $unexpectedCommon.Count -gt 0) {
    throw (
        "BAT Runtime Query common template/contract mismatch: " +
        "missing=$($missingCommon -join ',') unexpected=$($unexpectedCommon -join ',')"
    )
}

foreach ($overrideProperty in $contract.vendorOverrides.PSObject.Properties) {
    $key = [string] $overrideProperty.Name
    if ($key -notin $keys) {
        throw "BAT Runtime Query override references an unregistered key: $key"
    }
    foreach ($vendor in @($overrideProperty.Value)) {
        if ($vendor -notin $vendors) {
            throw "Invalid BAT Runtime Query override vendor: key=$key vendor=$vendor"
        }
        $overridePath = Join-Path $templateRoot "vendor\$vendor\repository\$key.sql.template"
        if (-not (Test-Path -LiteralPath $overridePath -PathType Leaf)) {
            throw "Missing BAT Runtime Query vendor override: key=$key vendor=$vendor"
        }
    }
}

$written = 0
$pruned = 0
foreach ($vendor in $vendors) {
    $targetRoot = Join-Path $Root "cpf-tools\db\vendor\$vendor\runtime\bat\repository"
    if (-not $Check) {
        [System.IO.Directory]::CreateDirectory($targetRoot) | Out-Null
    }
    foreach ($statement in $statements) {
        $key = [string] $statement.key
        $targetPath = Join-Path $targetRoot "$key.sql"
        $expected = Get-RenderedSql $vendor $statement
        if ($Check) {
            if (-not (Test-Path -LiteralPath $targetPath -PathType Leaf)) {
                throw "Missing generated BAT Runtime Query SQL: vendor=$vendor key=$key"
            }
            $actual = [System.IO.File]::ReadAllText($targetPath, [System.Text.Encoding]::UTF8)
            if ($actual -cne $expected) {
                throw "BAT Runtime Query generated SQL drift: vendor=$vendor key=$key"
            }
        } else {
            [System.IO.File]::WriteAllText($targetPath, $expected, $utf8NoBom)
            $written++
        }
    }
    if (Test-Path -LiteralPath $targetRoot -PathType Container) {
        $actualKeys = @(
            Get-ChildItem -LiteralPath $targetRoot -File -Filter "*.sql" |
                ForEach-Object { $_.BaseName }
        )
        $unexpected = @($actualKeys | Where-Object { $_ -notin $keys })
        if ($unexpected.Count -gt 0) {
            if ($Check) {
                throw "Unregistered BAT Runtime Query SQL exists: vendor=$vendor keys=$($unexpected -join ',')"
            }
            foreach ($key in $unexpected) {
                Assert-SafeStatementKey $key
                $stalePath = Join-Path $targetRoot "$key.sql"
                Remove-Item -LiteralPath $stalePath -Force
                $pruned++
            }
        }
    }
}

[ordered]@{
    status = "PASS"
    mode = if ($Check) { "CHECK" } else { "SYNC" }
    statements = $statements.Count
    vendors = $vendors.Count
    renderedFiles = if ($Check) { $statements.Count * $vendors.Count } else { $written }
    prunedFiles = if ($Check) { 0 } else { $pruned }
} | ConvertTo-Json
