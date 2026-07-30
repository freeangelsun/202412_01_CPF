param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [switch] $Quiet
)

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$Root = (Resolve-Path -LiteralPath $Root).Path
$syncScript = Join-Path $Root "cpf-tools\scripts\sync-platform-runtime-query-packs.ps1"
& $syncScript -Root $Root -Check | Out-Null

$contractPath = Join-Path $Root "cpf-tools\db\metadata\platform-runtime-query-contract.json"
$contract = Get-Content -Raw -Encoding UTF8 -LiteralPath $contractPath | ConvertFrom-Json
$vendors = @($contract.vendors | ForEach-Object { [string] $_ })
$failures = [System.Collections.Generic.List[string]]::new()
$inlineInventory = [System.Collections.Generic.List[object]]::new()
$inlineSqlPattern = @'
(?isx)(?:"""|")\s*(?:
    SELECT\s+(?:DISTINCT\s+)?[\w(*]
  | INSERT\s+INTO\b
  | UPDATE\s+[A-Za-z_(]
  | DELETE\s+FROM\b
  | MERGE\s+INTO\b
  | WITH\s+(?:RECURSIVE\s+)?[A-Za-z_][A-Za-z0-9_]*\s+AS\s*\(
)
'@
$vendorOnlyInlinePatterns = @(
    [pscustomobject]@{ name = "mysql-upsert"; pattern = '(?i)\bON\s+DUPLICATE\s+KEY\b' },
    [pscustomobject]@{ name = "mysql-last-insert-id"; pattern = '(?i)\bLAST_INSERT_ID\s*\(' },
    [pscustomobject]@{ name = "mysql-database-function"; pattern = '(?i)\bDATABASE\s*\(\s*\)' },
    [pscustomobject]@{ name = "vendor-information-schema"; pattern = '(?i)\binformation_schema\b' },
    [pscustomobject]@{ name = "limit-pagination"; pattern = '(?i)\bLIMIT\s+(?:\?|:[A-Za-z_][A-Za-z0-9_]*|\d+)' },
    [pscustomobject]@{ name = "mysql-insert-ignore"; pattern = '(?i)\bINSERT\s+IGNORE\b' },
    [pscustomobject]@{ name = "mysql-ifnull"; pattern = '(?i)\bIFNULL\s*\(' },
    [pscustomobject]@{ name = "mysql-date-format"; pattern = '(?i)\bDATE_FORMAT\s*\(' },
    [pscustomobject]@{ name = "mysql-group-concat"; pattern = '(?i)\bGROUP_CONCAT\s*\(' },
    [pscustomobject]@{ name = "mysql-substring-index"; pattern = '(?i)\bSUBSTRING_INDEX\s*\(' },
    [pscustomobject]@{ name = "mysql-timestampadd"; pattern = '(?i)\bTIMESTAMPADD\s*\(' },
    [pscustomobject]@{ name = "mysql-pow"; pattern = '(?i)\bPOW\s*\(' },
    [pscustomobject]@{ name = "postgres-upsert"; pattern = '(?i)\bON\s+CONFLICT\b' },
    [pscustomobject]@{ name = "postgres-cast"; pattern = '(?i)::[A-Za-z_][A-Za-z0-9_]*' },
    [pscustomobject]@{ name = "returning-clause"; pattern = '(?i)\bRETURNING\b' },
    [pscustomobject]@{ name = "oracle-nvl"; pattern = '(?i)\bNVL\s*\(' },
    [pscustomobject]@{ name = "oracle-sysdate"; pattern = '(?i)\bSYSDATE\b' },
    [pscustomobject]@{ name = "oracle-rownum"; pattern = '(?i)\bROWNUM\b' },
    [pscustomobject]@{ name = "sqlserver-top"; pattern = '(?i)\bTOP\s*(?:\(|\d)' },
    [pscustomobject]@{ name = "sqlserver-getdate"; pattern = '(?i)\bGETDATE\s*\(' },
    [pscustomobject]@{ name = "sqlserver-isnull"; pattern = '(?i)\bISNULL\s*\(' },
    [pscustomobject]@{ name = "mysql-quoted-identifier"; pattern = '`[A-Za-z_][A-Za-z0-9_]*`' }
)

function Add-Failure {
    param([Parameter(Mandatory = $true)][string] $Message)
    $failures.Add($Message)
}

function Get-InlineSqlFragments {
    param([Parameter(Mandatory = $true)][string] $Text)
    $fragments = [System.Collections.Generic.List[object]]::new()
    foreach ($match in [regex]::Matches($Text, $inlineSqlPattern)) {
        $start = $match.Index
        $remaining = $Text.Length - $start
        $maxLength = [Math]::Min($remaining, 12000)
        $window = $Text.Substring($start, $maxLength)
        $terminator = $window.IndexOf(');')
        if ($terminator -ge 0) {
            $window = $window.Substring(0, $terminator + 2)
        } elseif ($window.Length -eq 12000) {
            # Unbounded fragments are suspicious. Keep the bounded fragment and fail below when needed.
            $window = $window.Substring(0, 12000)
        }
        $line = 1 + ([regex]::Matches($Text.Substring(0, $start), "`n")).Count
        $fragments.Add([pscustomobject]@{ line = $line; text = $window }) | Out-Null
    }
    return @($fragments)
}

function Get-ScriptAstErrors {
    param([Parameter(Mandatory = $true)][string] $Path)
    $tokens = $null
    $errors = $null
    [void] [System.Management.Automation.Language.Parser]::ParseFile(
        $Path,
        [ref] $tokens,
        [ref] $errors)
    return @($errors)
}

function Read-XmlWithoutExternalResolution {
    param([Parameter(Mandatory = $true)][string] $Path)
    $settings = [System.Xml.XmlReaderSettings]::new()
    $settings.DtdProcessing = [System.Xml.DtdProcessing]::Ignore
    $settings.XmlResolver = $null
    $reader = [System.Xml.XmlReader]::Create($Path, $settings)
    try {
        $document = [System.Xml.XmlDocument]::new()
        $document.XmlResolver = $null
        $document.Load($reader)
        return $document
    } finally {
        $reader.Dispose()
    }
}

foreach ($scriptName in @(
    "sync-platform-runtime-query-packs.ps1",
    "check-platform-runtime-query-packs.ps1",
    "smoke-platform-runtime-query-packs-mariadb.ps1",
    "smoke-platform-runtime-query-packs-official-db.ps1",
    "check-query-contract-integrity.ps1"
)) {
    $scriptPath = Join-Path $Root "cpf-tools\scripts\$scriptName"
    if (-not (Test-Path -LiteralPath $scriptPath -PathType Leaf)) {
        Add-Failure "Managed Platform Runtime script is missing: $scriptName"
        continue
    }
    foreach ($parseError in Get-ScriptAstErrors -Path $scriptPath) {
        Add-Failure (
            "PowerShell AST error: script=$scriptName " +
            "line=$($parseError.Extent.StartLineNumber) message=$($parseError.Message)"
        )
    }
}

foreach ($module in @($contract.modules)) {
    $moduleCode = [string] $module.module
    $ownerArtifact = [string] $module.ownerArtifact
    $inlineSqlPolicy = [string] $module.inlineSqlPolicy
    if ($inlineSqlPolicy -cne "PORTABLE_ONLY") {
        Add-Failure (
            "Platform Runtime inline SQL policy must be PORTABLE_ONLY: " +
            "module=$moduleCode actual=$inlineSqlPolicy"
        )
    }
    $contractKeys = @(
        $module.statements |
            ForEach-Object { [string] $_.key } |
            Sort-Object -CaseSensitive
    )
    $activeKeys = @(
        $module.statements |
            Where-Object { [string] $_.usage -ceq "ACTIVE" } |
            ForEach-Object { [string] $_.key } |
            Sort-Object -CaseSensitive
    )
    $sourceKeys = [System.Collections.Generic.List[string]]::new()

    foreach ($scope in @($module.sourceScopes)) {
        $sourceRoot = Join-Path $Root ([string] $scope -replace "/", "\")
        if (-not (Test-Path -LiteralPath $sourceRoot -PathType Container)) {
            Add-Failure "Platform Runtime source scope is missing: module=$moduleCode scope=$scope"
            continue
        }
        foreach ($file in Get-ChildItem -LiteralPath $sourceRoot -Recurse -File -Filter "*.java") {
            $text = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
            foreach ($match in [regex]::Matches(
                $text,
                '\.required\(\s*"(?<key>[a-z][a-z0-9-]{1,63})"\s*\)')) {
                $sourceKeys.Add($match.Groups["key"].Value)
            }
            $inlineFragments = @(Get-InlineSqlFragments -Text $text)
            if ($inlineFragments.Count -gt 0) {
                $relative = [System.IO.Path]::GetRelativePath($Root, $file.FullName).Replace("\", "/")
                $inlineInventory.Add([pscustomobject][ordered]@{
                    module = $moduleCode
                    path = $relative
                    statements = $inlineFragments.Count
                    policy = $inlineSqlPolicy
                })
                foreach ($fragment in $inlineFragments) {
                    foreach ($vendorPattern in $vendorOnlyInlinePatterns) {
                        if ([string]$fragment.text -match [string]$vendorPattern.pattern) {
                            Add-Failure (
                                "Vendor-only inline SQL is forbidden: " +
                                "module=$moduleCode path=$relative line=$($fragment.line) token=$($vendorPattern.name)"
                            )
                        }
                    }
                }
            }
        }
    }
    $actualSourceKeys = @($sourceKeys | Sort-Object -CaseSensitive -Unique)
    $missingActiveSourceKeys = @($activeKeys | Where-Object { $actualSourceKeys -cnotcontains $_ })
    if ($missingActiveSourceKeys.Count -gt 0) {
        Add-Failure (
            "ACTIVE catalog key has no source consumer: module=$moduleCode " +
            "missing=$($missingActiveSourceKeys -join ',')"
        )
    }

    foreach ($statement in @($module.statements | Where-Object {
        [string] $_.usage -ceq "ACTIVE"
    })) {
        $consumer = [string] $statement.consumer
        $consumerFiles = @(
            foreach ($scope in @($module.sourceScopes)) {
                $sourceRoot = Join-Path $Root ([string] $scope -replace "/", "\")
                if (Test-Path -LiteralPath $sourceRoot -PathType Container) {
                    Get-ChildItem -LiteralPath $sourceRoot -Recurse -File -Filter "$consumer.java"
                }
            }
        )
        if ($consumerFiles.Count -eq 0) {
            Add-Failure (
                "Active Platform Runtime consumer is missing: " +
                "module=$moduleCode key=$($statement.key) consumer=$consumer"
            )
            continue
        }
        $consumerUsesKey = $false
        foreach ($consumerFile in $consumerFiles) {
            $consumerText = [System.IO.File]::ReadAllText(
                $consumerFile.FullName,
                [System.Text.Encoding]::UTF8)
            if ($consumerText.Contains('required("' + [string] $statement.key + '")')) {
                $consumerUsesKey = $true
                break
            }
        }
        if (-not $consumerUsesKey) {
            Add-Failure (
                "Active Platform Runtime consumer metadata drift: " +
                "module=$moduleCode key=$($statement.key) consumer=$consumer"
            )
        }
    }

    foreach ($vendor in $vendors) {
        $packPath = Join-Path $Root "cpf-tools\db\vendor\$vendor\pack.json"
        $pack = Get-Content -Raw -Encoding UTF8 -LiteralPath $packPath | ConvertFrom-Json
        $descriptorProperty = $pack.runtimeModules.PSObject.Properties[$moduleCode]
        if ($null -eq $descriptorProperty -or
                [string] $descriptorProperty.Value.ownerArtifact -cne $ownerArtifact) {
            Add-Failure (
                "Platform Runtime Pack ownership is missing: " +
                "module=$moduleCode vendor=$vendor owner=$ownerArtifact"
            )
        }

        $repositoryRoot = Join-Path $Root "cpf-tools\db\vendor\$vendor\runtime\$moduleCode\repository"
        $actualKeys = @(
            Get-ChildItem -LiteralPath $repositoryRoot -File -Filter "*.sql" |
                ForEach-Object { $_.BaseName } |
                Sort-Object -CaseSensitive
        )
        $missingContractKeys = @($contractKeys | Where-Object { $actualKeys -cnotcontains $_ })
        if ($missingContractKeys.Count -gt 0) {
            Add-Failure "Platform Runtime contract SQL missing: module=$moduleCode vendor=$vendor keys=$($missingContractKeys -join ',')"
        }
        foreach ($key in $contractKeys) {
            $sqlPath = Join-Path $repositoryRoot "$key.sql"
            if (-not (Test-Path -LiteralPath $sqlPath -PathType Leaf)) {
                continue
            }
            $sql = [System.IO.File]::ReadAllText($sqlPath, [System.Text.Encoding]::UTF8)
            if ($sql -match "@[A-Z0-9_]+@") {
                Add-Failure "Unresolved Runtime token: module=$moduleCode vendor=$vendor key=$key"
            }
            foreach ($forbidden in @($contract.forbiddenRuntimeIdentifiers)) {
                if ($sql -match "(?i)\b$([regex]::Escape([string] $forbidden))\b") {
                    Add-Failure (
                        "Legacy transaction identifier in Runtime SQL: " +
                        "module=$moduleCode vendor=$vendor key=$key identifier=$forbidden"
                    )
                }
            }
            if ($vendor -ne "mariadb" -and
                    $sql -match "(?i)\bON\s+DUPLICATE\s+KEY\b") {
                Add-Failure "MySQL-family UPSERT leaked: module=$moduleCode vendor=$vendor key=$key"
            }
            if ($vendor -eq "mariadb" -and
                    $sql -match "(?i)\b(?:ON\s+CONFLICT|MERGE\s+INTO)\b") {
                Add-Failure "Non-MySQL UPSERT leaked: module=$moduleCode vendor=$vendor key=$key"
            }
            if ($vendor -eq "oracle" -and
                    $sql -match "(?i)\bLIMIT\s+(?:\d+|\?|:[A-Za-z])") {
                Add-Failure "LIMIT syntax leaked: module=$moduleCode vendor=$vendor key=$key"
            }
            if ($vendor -eq "oracle" -and $sql -match "(?i)\bTOP\s*(?:\(|\d)") {
                Add-Failure "SQL Server TOP syntax leaked: module=$moduleCode vendor=$vendor key=$key"
            }
            if ($vendor -ne "postgresql" -and $sql -match "(?i)\bON\s+CONFLICT\b") {
                Add-Failure "PostgreSQL UPSERT leaked: module=$moduleCode vendor=$vendor key=$key"
            }
        }
    }

    $templateRoot = Join-Path $Root ([string] $module.templateRoot -replace "/", "\")
    foreach ($template in Get-ChildItem -LiteralPath $templateRoot -Recurse -File) {
        $templateText = [System.IO.File]::ReadAllText(
            $template.FullName,
            [System.Text.Encoding]::UTF8)
        foreach ($forbidden in @($contract.forbiddenRuntimeIdentifiers)) {
            if ($templateText -match "(?i)\b$([regex]::Escape([string] $forbidden))\b") {
                $relative = [System.IO.Path]::GetRelativePath(
                    $Root,
                    $template.FullName).Replace("\", "/")
                Add-Failure (
                    "Legacy transaction identifier in canonical template: " +
                    "module=$moduleCode path=$relative identifier=$forbidden"
                )
            }
        }
    }
}

$segmentMapperPath = Join-Path $Root (
    "cpf-tools\db\vendor\mariadb\runtime\cpf\mybatis\logging\" +
    "TransactionSegmentMapper.xml")
$segmentRecordPath = Join-Path $Root (
    "cpf-core\src\main\java\com\cpf\core\common\logging\segment\" +
    "TransactionSegmentRecord.java")
$canonicalDdlPath = Join-Path $Root "cpf-tools\db\vendor\mariadb\source\00_empty_install.sql"
try {
    $mapperDocument = Read-XmlWithoutExternalResolution -Path $segmentMapperPath
    $insertNode = $mapperDocument.SelectSingleNode(
        "/mapper/insert[@id='insertSegment']")
    if ($null -eq $insertNode) {
        Add-Failure "TransactionSegmentMapper.insertSegment is missing."
    } else {
        $insertSql = [string] $insertNode.InnerText
        if ($insertSql -notmatch "(?i)\btransaction_id\b" -or
                $insertSql -notmatch '#\{transactionId\}') {
            Add-Failure "TransactionSegmentMapper does not persist canonical transaction_id/transactionId."
        }
        $insertColumnMatch = [regex]::Match(
            $insertSql,
            "(?is)INSERT\s+INTO\s+cpf_transaction_segment\s*\((?<columns>.*?)\)\s*VALUES")
        if (-not $insertColumnMatch.Success) {
            Add-Failure "TransactionSegmentMapper insert column list cannot be parsed."
        } else {
            $insertColumns = @(
                $insertColumnMatch.Groups["columns"].Value.Split(",") |
                    ForEach-Object { $_.Trim().ToLowerInvariant() } |
                    Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
            )
            $ddl = [System.IO.File]::ReadAllText($canonicalDdlPath, [System.Text.Encoding]::UTF8)
            $tableMatch = [regex]::Match(
                $ddl,
                "(?is)CREATE\s+TABLE\s+IF\s+NOT\s+EXISTS\s+cpf_transaction_segment\s*" +
                "\((?<body>.*?)\)\s*ENGINE\s*=")
            if (-not $tableMatch.Success) {
                Add-Failure "Canonical cpf_transaction_segment DDL cannot be parsed."
            } else {
                $ddlColumns = @(
                    [regex]::Matches(
                        $tableMatch.Groups["body"].Value,
                        "(?m)^\s*(?<column>[a-z][a-z0-9_]*)\s+[A-Z]") |
                        ForEach-Object { $_.Groups["column"].Value.ToLowerInvariant() } |
                        Sort-Object -Unique
                )
                foreach ($column in $insertColumns) {
                    if ($column -notin $ddlColumns) {
                        Add-Failure "TransactionSegmentMapper column is absent from canonical DDL: $column"
                    }
                }
            }
        }
    }

    $mapperText = [System.IO.File]::ReadAllText(
        $segmentMapperPath,
        [System.Text.Encoding]::UTF8)
    $recordText = [System.IO.File]::ReadAllText(
        $segmentRecordPath,
        [System.Text.Encoding]::UTF8)
    $recordProperties = @(
        [regex]::Matches($recordText, "\bget(?<name>[A-Z][A-Za-z0-9_]*)\s*\(") |
            ForEach-Object {
                $name = $_.Groups["name"].Value
                $name.Substring(0, 1).ToLowerInvariant() + $name.Substring(1)
            } |
            Sort-Object -Unique
    )
    $mapperProperties = @(
        [regex]::Matches($mapperText, '#\{(?<name>[A-Za-z][A-Za-z0-9_]*)\}') |
            ForEach-Object { $_.Groups["name"].Value } |
            Sort-Object -Unique
    )
    foreach ($property in $mapperProperties) {
        if ($property -notin $recordProperties) {
            Add-Failure "TransactionSegmentMapper references missing record property: $property"
        }
    }
} catch {
    Add-Failure "TransactionSegmentMapper static validation failed: $($_.Exception.Message)"
}

$integrityScript = Join-Path $Root "cpf-tools\scripts\check-query-contract-integrity.ps1"
try { & $integrityScript -Root $Root } catch { Add-Failure ("Query Contract integrity gate failed: " + $_.Exception.Message) }

$inlineStatementCount = 0
foreach ($inlineItem in $inlineInventory) {
    $inlineStatementCount += [int] $inlineItem.statements
}
$result = [ordered]@{
    status = if ($failures.Count -eq 0) { "PASS" } else { "FAIL" }
    contract = [string] $contract.contract
    modules = @($contract.modules | ForEach-Object {
        [ordered]@{
            module = [string] $_.module
            status = [string] $_.status
            statements = @($_.statements).Count
            activeStatements = @($_.statements | Where-Object {
                [string] $_.usage -ceq "ACTIVE"
            }).Count
            stagedStatements = @($_.statements | Where-Object {
                [string] $_.usage -ceq "STAGED"
            }).Count
        }
    })
    vendors = $vendors.Count
    generatedRepositoryFiles = (
        $contract.modules |
            ForEach-Object { @($_.statements).Count * $vendors.Count } |
            Measure-Object -Sum
    ).Sum
    remainingInlineSqlPolicy = "PORTABLE_ONLY"
    remainingInlineSqlStatements = [int] $inlineStatementCount
    remainingInlineSqlFiles = @($inlineInventory)
    failures = @($failures)
}
if (-not $Quiet) {
    $result | ConvertTo-Json -Depth 10
}
if ($failures.Count -gt 0) {
    throw (
        "CPF/BZA Platform Runtime Query Pack validation failed " +
        "($($failures.Count)): $($failures -join ' | ')"
    )
}
