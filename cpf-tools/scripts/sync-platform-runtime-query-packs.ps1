param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [switch] $Check
)

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$Root = (Resolve-Path -LiteralPath $Root).Path
$contractPath = Join-Path $Root "cpf-tools\db\metadata\platform-runtime-query-contract.json"
$contract = Get-Content -Raw -Encoding UTF8 -LiteralPath $contractPath | ConvertFrom-Json
$utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$expectedVendors = @("mariadb", "mysql", "postgresql", "oracle", "sqlserver")

$vendorTokens = @{
    mariadb = @{
        "@NOW@" = "CURRENT_TIMESTAMP"
        "@NOW3@" = "CURRENT_TIMESTAMP(3)"
        "@NOW6@" = "CURRENT_TIMESTAMP(6)"
        "@LIMIT_POSITIONAL@" = "LIMIT ?"
        "@LIMIT_NAMED@" = "LIMIT :limit"
        "@COALESCE_CREATED_BY@" = "IFNULL(#{createdBy}, 'CPF')"
        "@COALESCE_UPDATED_BY@" = "IFNULL(#{updatedBy}, 'CPF')"
    }
    mysql = @{
        "@NOW@" = "CURRENT_TIMESTAMP"
        "@NOW3@" = "CURRENT_TIMESTAMP(3)"
        "@NOW6@" = "CURRENT_TIMESTAMP(6)"
        "@LIMIT_POSITIONAL@" = "LIMIT ?"
        "@LIMIT_NAMED@" = "LIMIT :limit"
        "@COALESCE_CREATED_BY@" = "IFNULL(#{createdBy}, 'CPF')"
        "@COALESCE_UPDATED_BY@" = "IFNULL(#{updatedBy}, 'CPF')"
    }
    postgresql = @{
        "@NOW@" = "CURRENT_TIMESTAMP"
        "@NOW3@" = "CURRENT_TIMESTAMP(3)"
        "@NOW6@" = "CURRENT_TIMESTAMP(6)"
        "@LIMIT_POSITIONAL@" = "LIMIT ?"
        "@LIMIT_NAMED@" = "LIMIT :limit"
        "@COALESCE_CREATED_BY@" = "COALESCE(#{createdBy}, 'CPF')"
        "@COALESCE_UPDATED_BY@" = "COALESCE(#{updatedBy}, 'CPF')"
    }
    oracle = @{
        "@NOW@" = "CURRENT_TIMESTAMP"
        "@NOW3@" = "CURRENT_TIMESTAMP(3)"
        "@NOW6@" = "CURRENT_TIMESTAMP(6)"
        "@LIMIT_POSITIONAL@" = "OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY"
        "@LIMIT_NAMED@" = "OFFSET 0 ROWS FETCH NEXT :limit ROWS ONLY"
        "@COALESCE_CREATED_BY@" = "COALESCE(#{createdBy}, 'CPF')"
        "@COALESCE_UPDATED_BY@" = "COALESCE(#{updatedBy}, 'CPF')"
    }
    sqlserver = @{
        "@NOW@" = "CURRENT_TIMESTAMP"
        "@NOW3@" = "SYSUTCDATETIME()"
        "@NOW6@" = "SYSUTCDATETIME()"
        "@LIMIT_POSITIONAL@" = "OFFSET 0 ROWS FETCH NEXT ? ROWS ONLY"
        "@LIMIT_NAMED@" = "OFFSET 0 ROWS FETCH NEXT :limit ROWS ONLY"
        "@COALESCE_CREATED_BY@" = "COALESCE(#{createdBy}, 'CPF')"
        "@COALESCE_UPDATED_BY@" = "COALESCE(#{updatedBy}, 'CPF')"
    }
}

function Assert-SafeName {
    param(
        [Parameter(Mandatory = $true)][string] $Value,
        [Parameter(Mandatory = $true)][string] $DisplayName
    )
    if ($Value -cnotmatch "^[a-z][a-z0-9-]{1,63}$") {
        throw "Invalid ${DisplayName}: $Value"
    }
}

function Get-RelativePath {
    param(
        [Parameter(Mandatory = $true)][string] $BasePath,
        [Parameter(Mandatory = $true)][string] $Path
    )
    return [System.IO.Path]::GetRelativePath($BasePath, $Path).Replace("\", "/")
}

function Get-RenderedText {
    param(
        [Parameter(Mandatory = $true)][string] $TemplatePath,
        [Parameter(Mandatory = $true)][string] $Vendor,
        [Parameter(Mandatory = $true)][string] $DisplayName
    )
    if (-not (Test-Path -LiteralPath $TemplatePath -PathType Leaf)) {
        throw "Missing Platform Runtime authoring template: $DisplayName path=$TemplatePath"
    }
    $text = [System.IO.File]::ReadAllText($TemplatePath, [System.Text.Encoding]::UTF8)
    foreach ($token in $vendorTokens[$Vendor].Keys) {
        $text = $text.Replace($token, $vendorTokens[$Vendor][$token])
    }
    $text = $text.Replace("`r`n", "`n").Replace("`r", "`n").Trim() + "`n"
    if ($text -match "@[A-Z0-9_]+@") {
        throw "Unresolved Platform Runtime template token: $DisplayName token=$($Matches[0])"
    }
    return $text
}

function Assert-StatementParameters {
    param(
        [Parameter(Mandatory = $true)][object] $Module,
        [Parameter(Mandatory = $true)][object] $Statement,
        [Parameter(Mandatory = $true)][string] $Vendor,
        [Parameter(Mandatory = $true)][string] $Sql
    )
    $key = [string] $Statement.key
    $style = [string] $Module.parameterStyle
    if ($style -ceq "JDBC_POSITIONAL") {
        $actualCount = ([regex]::Matches($Sql, "\?")).Count
        $expectedCount = [int] $Statement.parameterCount
        if ($actualCount -ne $expectedCount) {
            throw (
                "Platform Runtime positional parameter mismatch: " +
                "module=$($Module.module) vendor=$Vendor key=$key " +
                "expected=$expectedCount actual=$actualCount"
            )
        }
    } elseif ($style -ceq "SPRING_NAMED") {
        $actualNames = @(
            [regex]::Matches($Sql, "(?<!:):([A-Za-z][A-Za-z0-9_]*)") |
                ForEach-Object { $_.Groups[1].Value } |
                Sort-Object -CaseSensitive -Unique
        )
        $expectedNames = @(
            @($Statement.parameters) |
                ForEach-Object { [string] $_ } |
                Sort-Object -CaseSensitive -Unique
        )
        if (($actualNames -join "`n") -cne ($expectedNames -join "`n")) {
            throw (
                "Platform Runtime named parameter mismatch: " +
                "module=$($Module.module) vendor=$Vendor key=$key " +
                "expected=$($expectedNames -join ',') actual=$($actualNames -join ',')"
            )
        }
    } else {
        throw "Unsupported Platform Runtime parameter style: module=$($Module.module) style=$style"
    }

    $formatCount = ([regex]::Matches($Sql, "%s")).Count
    $expectedFormatCount = if ($Statement.PSObject.Properties.Name -contains "formatTokens") {
        @($Statement.formatTokens).Count
    } else {
        0
    }
    if ($formatCount -ne $expectedFormatCount) {
        throw (
            "Platform Runtime format token mismatch: module=$($Module.module) " +
            "vendor=$Vendor key=$key expected=$expectedFormatCount actual=$formatCount"
        )
    }
}

if ([int] $contract.schemaVersion -ne 1 -or
        [string] $contract.contract -cne "CPF_BZA_CENTRAL_RUNTIME_QUERY_PACK") {
    throw "Invalid CPF/BZA Platform Runtime Query contract header."
}
$contractVendors = @($contract.vendors | ForEach-Object { [string] $_ })
if (($contractVendors -join "`n") -cne ($expectedVendors -join "`n")) {
    throw "Platform Runtime Query vendor contract must be exactly: $($expectedVendors -join ',')"
}

$modules = @($contract.modules)
if ($modules.Count -ne 2 -or
        "bza" -notin @($modules.module) -or
        "cpf" -notin @($modules.module)) {
    throw "Platform Runtime Query contract must contain exactly CPF and BZA modules."
}

$written = 0
$checked = 0
$managedMyBatisCount = 0
foreach ($module in $modules) {
    $moduleCode = [string] $module.module
    Assert-SafeName -Value $moduleCode -DisplayName "module code"
    $templateRoot = Join-Path $Root ([string] $module.templateRoot -replace "/", "\")
    $commonRoot = Join-Path $templateRoot "repository"
    $statements = @($module.statements)
    $keys = @($statements | ForEach-Object { [string] $_.key })
    foreach ($key in $keys) {
        Assert-SafeName -Value $key -DisplayName "$moduleCode statement key"
    }
    if ($statements.Count -eq 0 -or
            @($keys | Sort-Object -CaseSensitive -Unique).Count -ne $keys.Count) {
        throw "Platform Runtime Query statement contract is empty or duplicated: module=$moduleCode"
    }

    $commonKeys = @(
        Get-ChildItem -LiteralPath $commonRoot -File -Filter "*.sql.template" |
            ForEach-Object { $_.Name -replace "\.sql\.template$", "" }
    )
    $missingCommon = @($keys | Where-Object { $_ -notin $commonKeys })
    $unexpectedCommon = @($commonKeys | Where-Object { $_ -notin $keys })
    if ($missingCommon.Count -gt 0 -or $unexpectedCommon.Count -gt 0) {
        throw (
            "Platform Runtime common template/contract mismatch: module=$moduleCode " +
            "missing=$($missingCommon -join ',') unexpected=$($unexpectedCommon -join ',')"
        )
    }

    $vendorTemplateRoot = Join-Path $templateRoot "vendor"
    if (Test-Path -LiteralPath $vendorTemplateRoot -PathType Container) {
        foreach ($override in Get-ChildItem -LiteralPath $vendorTemplateRoot -Recurse -File -Filter "*.sql.template") {
            $relative = Get-RelativePath -BasePath $vendorTemplateRoot -Path $override.FullName
            $segments = $relative.Split("/")
            if ($segments.Count -ne 3 -or
                    $segments[0] -notin $expectedVendors -or
                    $segments[1] -cne "repository") {
                throw "Invalid Platform Runtime vendor override path: module=$moduleCode path=$relative"
            }
            $overrideKey = $segments[2] -replace "\.sql\.template$", ""
            if ($overrideKey -notin $keys) {
                throw "Unregistered Platform Runtime vendor override: module=$moduleCode path=$relative"
            }
        }
    }

    foreach ($vendor in $expectedVendors) {
        $targetRoot = Join-Path $Root "cpf-tools\db\vendor\$vendor\$($module.generatedPackPath -replace '/', '\')"
        if (-not $Check) {
            [System.IO.Directory]::CreateDirectory($targetRoot) | Out-Null
        }
        foreach ($statement in $statements) {
            $key = [string] $statement.key
            $overridePath = Join-Path $templateRoot "vendor\$vendor\repository\$key.sql.template"
            $commonPath = Join-Path $commonRoot "$key.sql.template"
            $templatePath = if (Test-Path -LiteralPath $overridePath -PathType Leaf) {
                $overridePath
            } else {
                $commonPath
            }
            $expected = Get-RenderedText `
                -TemplatePath $templatePath `
                -Vendor $vendor `
                -DisplayName "module=$moduleCode vendor=$vendor key=$key"
            Assert-StatementParameters `
                -Module $module `
                -Statement $statement `
                -Vendor $vendor `
                -Sql $expected
            $targetPath = Join-Path $targetRoot "$key.sql"
            if ($Check) {
                if (-not (Test-Path -LiteralPath $targetPath -PathType Leaf)) {
                    throw "Missing generated Platform Runtime SQL: module=$moduleCode vendor=$vendor key=$key"
                }
                $actual = [System.IO.File]::ReadAllText($targetPath, [System.Text.Encoding]::UTF8)
                if ($actual.Replace("`r`n", "`n").Replace("`r", "`n") -cne $expected) {
                    throw "Generated Platform Runtime SQL drift: module=$moduleCode vendor=$vendor key=$key"
                }
                $checked++
            } else {
                [System.IO.File]::WriteAllText($targetPath, $expected, $utf8NoBom)
                $written++
            }
        }
        $actualKeys = @(
            Get-ChildItem -LiteralPath $targetRoot -File -Filter "*.sql" |
                ForEach-Object { $_.BaseName }
        )
        $unexpectedKeys = @($actualKeys | Where-Object { $_ -notin $keys })
        $missingKeys = @($keys | Where-Object { $_ -notin $actualKeys })
        if ($unexpectedKeys.Count -gt 0 -or $missingKeys.Count -gt 0) {
            throw (
                "Generated Platform Runtime statement parity mismatch: " +
                "module=$moduleCode vendor=$vendor missing=$($missingKeys -join ',') " +
                "unexpected=$($unexpectedKeys -join ',')"
            )
        }
    }

    foreach ($artifact in @($module.managedMyBatis)) {
        Assert-SafeName -Value ([string] $artifact.key) -DisplayName "$moduleCode MyBatis artifact key"
        foreach ($vendor in @($artifact.vendors)) {
            $vendorName = [string] $vendor
            if ($vendorName -notin $expectedVendors) {
                throw "Invalid managed MyBatis vendor: module=$moduleCode vendor=$vendorName"
            }
            $templateRelativePath = ([string] $artifact.templateRelativePath).Replace(
                "{vendor}", $vendorName)
            $templatePath = Join-Path $templateRoot ($templateRelativePath -replace "/", "\")
            $expected = Get-RenderedText `
                -TemplatePath $templatePath `
                -Vendor $vendorName `
                -DisplayName "module=$moduleCode vendor=$vendorName artifact=$($artifact.key)"
            $targetPath = Join-Path $Root (
                "cpf-tools\db\vendor\$vendorName\runtime\$moduleCode\" +
                (([string] $artifact.relativePath) -replace "/", "\"))
            if ($Check) {
                if (-not (Test-Path -LiteralPath $targetPath -PathType Leaf)) {
                    throw (
                        "Missing managed Platform Runtime MyBatis resource: " +
                        "module=$moduleCode vendor=$vendorName artifact=$($artifact.key)"
                    )
                }
                $actual = [System.IO.File]::ReadAllText($targetPath, [System.Text.Encoding]::UTF8)
                if ($actual.Replace("`r`n", "`n").Replace("`r", "`n") -cne $expected) {
                    throw (
                        "Generated Platform Runtime MyBatis drift: " +
                        "module=$moduleCode vendor=$vendorName artifact=$($artifact.key)"
                    )
                }
                $checked++
            } else {
                [System.IO.Directory]::CreateDirectory((Split-Path -Parent $targetPath)) | Out-Null
                [System.IO.File]::WriteAllText($targetPath, $expected, $utf8NoBom)
                $written++
            }
            $managedMyBatisCount++
        }
    }
}

[ordered]@{
    status = "PASS"
    mode = if ($Check) { "CHECK" } else { "SYNC" }
    modules = @($modules.module)
    vendors = $expectedVendors.Count
    statements = ($modules | ForEach-Object { @($_.statements).Count } | Measure-Object -Sum).Sum
    repositoryFiles = ($modules | ForEach-Object {
        @($_.statements).Count * $expectedVendors.Count
    } | Measure-Object -Sum).Sum
    managedMyBatisFiles = $managedMyBatisCount
    processedFiles = if ($Check) { $checked } else { $written }
} | ConvertTo-Json -Depth 5
