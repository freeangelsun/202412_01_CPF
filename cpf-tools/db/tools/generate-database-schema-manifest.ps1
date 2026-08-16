param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $OutputPath = ""
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

if ([string]::IsNullOrWhiteSpace($OutputPath)) {
    $OutputPath = Join-Path $Root "cpf-tools/db/generated/database-schema-manifest.json"
}
$sourcePlanPath = Join-Path $Root "cpf-tools/db/config/database-source-plan.json"
$sourcePlan = Get-Content -LiteralPath $sourcePlanPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 20
$mariaPlan = $sourcePlan.mariadb
if ([string]::IsNullOrWhiteSpace([string]$mariaPlan.sourceRoot)) {
    throw "database-source-plan.json에 mariadb.sourceRoot가 없습니다."
}
$sourceRoot = Join-Path $Root ([string]$mariaPlan.sourceRoot)

$tables = New-Object System.Collections.Generic.List[object]
$currentDb = ""

function Split-CpfSqlDefinitions {
    param([string] $Body)

    $definitions = [System.Collections.Generic.List[string]]::new()
    $current = [System.Text.StringBuilder]::new()
    $depth = 0
    [char] $quote = [char]0
    for ($index = 0; $index -lt $Body.Length; $index++) {
        $character = $Body[$index]
        if ($quote -ne [char]0) {
            [void] $current.Append($character)
            if ($character -eq $quote) {
                if ($index + 1 -lt $Body.Length -and $Body[$index + 1] -eq $quote) {
                    [void] $current.Append($Body[++$index])
                } elseif ($index -eq 0 -or $Body[$index - 1] -ne '\') {
                    $quote = [char]0
                }
            }
            continue
        }
        if ($character -in @("'", '"', '`')) {
            $quote = $character
            [void] $current.Append($character)
            continue
        }
        if ($character -eq '(') {
            $depth++
            [void] $current.Append($character)
            continue
        }
        if ($character -eq ')') {
            $depth--
            if ($depth -lt 0) { throw "Table DDL 괄호 깊이가 올바르지 않습니다." }
            [void] $current.Append($character)
            continue
        }
        if ($character -eq ',' -and $depth -eq 0) {
            $definition = $current.ToString().Trim()
            if (-not [string]::IsNullOrWhiteSpace($definition)) {
                $definitions.Add($definition)
            }
            [void] $current.Clear()
            continue
        }
        [void] $current.Append($character)
    }
    if ($quote -ne [char]0 -or $depth -ne 0) {
        throw "Table DDL 문자열 또는 괄호가 닫히지 않았습니다."
    }
    $last = $current.ToString().Trim()
    if (-not [string]::IsNullOrWhiteSpace($last)) {
        $definitions.Add($last)
    }
    return $definitions.ToArray()
}

function Get-CpfLogicalDatabaseAtIndex {
    param(
        [string] $Sql,
        [int] $Index
    )
    $database = ""
    foreach ($useMatch in [regex]::Matches(
            $Sql,
            '(?im)\bUSE\s+`?([A-Za-z][A-Za-z0-9_$#]*)`?\s*;')) {
        if ($useMatch.Index -gt $Index) { break }
        $database = $useMatch.Groups[1].Value
    }
    return $database
}

function ConvertTo-CpfIndexColumns {
    param([string] $ColumnText)
    return @(
        $ColumnText -split ',' | ForEach-Object {
            $clean = $_.Trim()
            $clean = [regex]::Replace($clean, '\s+(ASC|DESC)\b', '', 'IgnoreCase')
            $clean = [regex]::Replace($clean, '\(\s*\d+\s*\)$', '')
            $clean.Trim('`', ' ')
        }
    )
}

foreach ($fileName in @($mariaPlan.emptyInstallFiles)) {
    $path = Join-Path $sourceRoot $fileName
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Canonical schema source 누락: $path"
    }
    $text = Get-Content -LiteralPath $path -Raw -Encoding UTF8

    $tokenRegex = [regex]'(?is)(USE\s+`?([A-Za-z][A-Za-z0-9_$#]*)`?\s*;)|(CREATE\s+TABLE\s+IF\s+NOT\s+EXISTS\s+`?([A-Za-z][A-Za-z0-9_]*)`?\s*\((.*?)\)\s*ENGINE=)'
    foreach ($match in $tokenRegex.Matches($text)) {
        if ($match.Groups[2].Success) {
            $currentDb = $match.Groups[2].Value
            continue
        }
        if (-not $match.Groups[4].Success) { continue }
        if ([string]::IsNullOrWhiteSpace($currentDb)) {
            throw "CREATE TABLE 앞에 USE logical DB가 없습니다. file=$fileName table=$($match.Groups[4].Value)"
        }

        $tableName = $match.Groups[4].Value
        $body = $match.Groups[5].Value
        $columns = New-Object System.Collections.Generic.List[string]
        $indexes = New-Object System.Collections.Generic.List[object]
        $foreignKeys = New-Object System.Collections.Generic.List[object]

        foreach ($rawLine in (Split-CpfSqlDefinitions $body)) {
            $line = [regex]::Replace($rawLine.Trim(), '\s+', ' ')
            if ([string]::IsNullOrWhiteSpace($line)) { continue }

            $columnMatch = [regex]::Match(
                $line,
                '^`?([A-Za-z][A-Za-z0-9_]*)`?\s+(BIGINT|INT|INTEGER|SMALLINT|TINYINT|MEDIUMINT|VARCHAR|CHAR|DATE|DATETIME|TIMESTAMP|TIME|LONGTEXT|MEDIUMTEXT|TEXT|DECIMAL|NUMERIC|JSON|BLOB|LONGBLOB|DOUBLE|FLOAT|BOOLEAN|VARBINARY|BINARY)\b',
                [System.Text.RegularExpressions.RegexOptions]::IgnoreCase
            )
            if ($columnMatch.Success) {
                $columnName = $columnMatch.Groups[1].Value
                $columns.Add($columnName)
                if ($line -match '(?i)\bUNIQUE\b') {
                    $indexes.Add([pscustomobject]@{
                        name = $columnName
                        unique = $true
                        columns = @($columnName)
                    })
                }
                continue
            }

            $indexMatch = [regex]::Match(
                $line,
                '^(UNIQUE\s+)?(?:KEY|INDEX)\s+`?([A-Za-z][A-Za-z0-9_]*)`?\s*\((.+)\)$',
                [System.Text.RegularExpressions.RegexOptions]::IgnoreCase
            )
            if ($indexMatch.Success) {
                $columnTokens = New-Object System.Collections.Generic.List[string]
                foreach ($token in ($indexMatch.Groups[3].Value -split ',')) {
                    $clean = $token.Trim()
                    $clean = [regex]::Replace($clean, '\s+(ASC|DESC)\b', '', 'IgnoreCase')
                    $clean = [regex]::Replace($clean, '\(\s*\d+\s*\)$', '')
                    $clean = $clean.Trim('`', ' ')
                    $columnTokens.Add($clean)
                }
                $indexes.Add([pscustomobject]@{
                    name = $indexMatch.Groups[2].Value
                    unique = $indexMatch.Groups[1].Success
                    columns = @($columnTokens.ToArray())
                })
                continue
            }

            $constraintUniqueMatch = [regex]::Match(
                $line,
                '^CONSTRAINT\s+`?([A-Za-z][A-Za-z0-9_]*)`?\s+UNIQUE\s*\((.+)\)$',
                [System.Text.RegularExpressions.RegexOptions]::IgnoreCase
            )
            if ($constraintUniqueMatch.Success) {
                $columnTokens = New-Object System.Collections.Generic.List[string]
                foreach ($token in ($constraintUniqueMatch.Groups[2].Value -split ',')) {
                    $clean = $token.Trim()
                    $clean = [regex]::Replace($clean, '\s+(ASC|DESC)\b', '', 'IgnoreCase')
                    $clean = [regex]::Replace($clean, '\(\s*\d+\s*\)$', '')
                    $clean = $clean.Trim('`', ' ')
                    $columnTokens.Add($clean)
                }
                $indexes.Add([pscustomobject]@{
                    name = $constraintUniqueMatch.Groups[1].Value
                    unique = $true
                    columns = @($columnTokens.ToArray())
                })
                continue
            }

        }

        # FK는 실 DDL에서 CONSTRAINT / FOREIGN KEY / REFERENCES가 여러 줄로 분리될 수 있으므로
        # line parser가 아니라 Table body 전체를 대상으로 수집한다.
        $fkRegex = [regex]::new(
            'CONSTRAINT\s+`?([A-Za-z][A-Za-z0-9_]*)`?\s+FOREIGN\s+KEY\s*\(([^)]+)\)\s+REFERENCES\s+`?([A-Za-z][A-Za-z0-9_]*)`?\s*\(([^)]+)\)',
            [System.Text.RegularExpressions.RegexOptions]::IgnoreCase -bor [System.Text.RegularExpressions.RegexOptions]::Singleline
        )
        foreach ($fkMatch in $fkRegex.Matches($body)) {
            $local = @($fkMatch.Groups[2].Value -split ',' | ForEach-Object { $_.Trim().Trim('`') })
            $referenced = @($fkMatch.Groups[4].Value -split ',' | ForEach-Object { $_.Trim().Trim('`') })
            $foreignKeys.Add([pscustomobject]@{
                name = $fkMatch.Groups[1].Value
                columns = $local
                referencedTable = $fkMatch.Groups[3].Value
                referencedColumns = $referenced
            })
        }

        $columnArray = @($columns.ToArray())
        $columnLookup = @($columnArray | ForEach-Object { $_.ToLowerInvariant() })
        foreach ($index in $indexes) {
            $missing = @($index.columns | Where-Object { $_.ToLowerInvariant() -notin $columnLookup })
            if ($missing.Count -gt 0) {
                throw "Index가 존재하지 않는 Column을 참조합니다. file=$fileName db=$currentDb table=$tableName index=$($index.name) missing=$($missing -join ',')"
            }
        }
        foreach ($fk in $foreignKeys) {
            $missing = @($fk.columns | Where-Object { $_.ToLowerInvariant() -notin $columnLookup })
            if ($missing.Count -gt 0) {
                throw "FK가 존재하지 않는 local Column을 참조합니다. file=$fileName db=$currentDb table=$tableName fk=$($fk.name) missing=$($missing -join ',')"
            }
        }

        $tables.Add([pscustomobject]@{
            vendor = "mariadb"
            logicalDatabase = $currentDb
            sourceFile = $fileName
            tableName = $tableName
            columns = $columnArray
            indexes = @($indexes.ToArray())
            foreignKeys = @($foreignKeys.ToArray())
        })
    }

    # Fresh Install source의 ALTER는 최종 Schema 계약의 일부다. CREATE만 읽으면
    # 실제 설치 Column/Index/FK를 drift로 오판하므로 문장 순서대로 Metadata에 반영한다.
    $alterStatementRegex = [regex]::new(
        'ALTER\s+TABLE\s+`?([A-Za-z][A-Za-z0-9_]*)`?\s+.*?;',
        [System.Text.RegularExpressions.RegexOptions]::IgnoreCase -bor
            [System.Text.RegularExpressions.RegexOptions]::Singleline
    )
    foreach ($alterMatch in $alterStatementRegex.Matches($text)) {
        $database = Get-CpfLogicalDatabaseAtIndex $text $alterMatch.Index
        $tableName = $alterMatch.Groups[1].Value
        $table = @($tables | Where-Object {
            $_.logicalDatabase -eq $database -and $_.tableName -eq $tableName
        } | Select-Object -First 1)
        if ($table.Count -ne 1) {
            throw "ALTER TABLE Metadata 대상이 없습니다. file=$fileName db=$database table=$tableName"
        }
        $table = $table[0]
        $statement = [regex]::Replace($alterMatch.Value.Trim(), '\s+', ' ')

        $addColumn = [regex]::Match(
            $statement,
            '^ALTER TABLE `?[A-Za-z][A-Za-z0-9_]*`? ADD COLUMN (?:IF NOT EXISTS )?`?([A-Za-z][A-Za-z0-9_]*)`? (BIGINT|INT|INTEGER|SMALLINT|TINYINT|MEDIUMINT|VARCHAR|CHAR|DATE|DATETIME|TIMESTAMP|TIME|LONGTEXT|MEDIUMTEXT|TEXT|DECIMAL|NUMERIC|JSON|BLOB|LONGBLOB|DOUBLE|FLOAT|BOOLEAN|VARBINARY|BINARY)\b',
            [System.Text.RegularExpressions.RegexOptions]::IgnoreCase
        )
        if ($addColumn.Success) {
            $columnName = $addColumn.Groups[1].Value
            if ($columnName -notin @($table.columns)) {
                $columns = [System.Collections.Generic.List[string]]::new()
                foreach ($existingColumn in @($table.columns)) {
                    $columns.Add([string]$existingColumn)
                }
                $afterMatch = [regex]::Match(
                    $statement,
                    '\s+AFTER\s+`?([A-Za-z][A-Za-z0-9_]*)`?\s*;$',
                    [System.Text.RegularExpressions.RegexOptions]::IgnoreCase
                )
                if ($statement -match '(?i)\s+FIRST\s*;$') {
                    $columns.Insert(0, $columnName)
                } elseif ($afterMatch.Success) {
                    $afterColumn = $afterMatch.Groups[1].Value
                    $afterIndex = -1
                    for ($i = 0; $i -lt $columns.Count; $i++) {
                        if ($columns[$i].Equals($afterColumn, [System.StringComparison]::OrdinalIgnoreCase)) {
                            $afterIndex = $i
                            break
                        }
                    }
                    if ($afterIndex -lt 0) {
                        throw "ALTER TABLE AFTER 대상 Column이 없습니다. file=$fileName db=$database table=$tableName column=$columnName after=$afterColumn"
                    }
                    $columns.Insert($afterIndex + 1, $columnName)
                } else {
                    $columns.Add($columnName)
                }
                $table.columns = @($columns.ToArray())
            }
            continue
        }

        $addIndex = [regex]::Match(
            $statement,
            '^ALTER TABLE `?[A-Za-z][A-Za-z0-9_]*`? ADD (UNIQUE )?(?:INDEX|KEY) (?:IF NOT EXISTS )?`?([A-Za-z][A-Za-z0-9_]*)`?\s*\(([^)]+)\)',
            [System.Text.RegularExpressions.RegexOptions]::IgnoreCase
        )
        if ($addIndex.Success) {
            $indexName = $addIndex.Groups[2].Value
            $table.indexes = @($table.indexes | Where-Object { $_.name -ne $indexName }) + [pscustomobject]@{
                name = $indexName
                unique = $addIndex.Groups[1].Success
                columns = @(ConvertTo-CpfIndexColumns $addIndex.Groups[3].Value)
            }
            continue
        }

        $addForeignKey = [regex]::Match(
            $statement,
            '^ALTER TABLE `?[A-Za-z][A-Za-z0-9_]*`? ADD CONSTRAINT `?([A-Za-z][A-Za-z0-9_]*)`? FOREIGN KEY\s*\(([^)]+)\)\s+REFERENCES\s+`?([A-Za-z][A-Za-z0-9_]*)`?\s*\(([^)]+)\)',
            [System.Text.RegularExpressions.RegexOptions]::IgnoreCase
        )
        if ($addForeignKey.Success) {
            $foreignKeyName = $addForeignKey.Groups[1].Value
            $table.foreignKeys = @($table.foreignKeys | Where-Object { $_.name -ne $foreignKeyName }) + [pscustomobject]@{
                name = $foreignKeyName
                columns = @($addForeignKey.Groups[2].Value -split ',' | ForEach-Object { $_.Trim().Trim('`') })
                referencedTable = $addForeignKey.Groups[3].Value
                referencedColumns = @($addForeignKey.Groups[4].Value -split ',' | ForEach-Object { $_.Trim().Trim('`') })
            }
            continue
        }

        $dropIndex = [regex]::Match(
            $statement,
            '^ALTER TABLE `?[A-Za-z][A-Za-z0-9_]*`? DROP INDEX (?:IF EXISTS )?`?([A-Za-z][A-Za-z0-9_]*)`?',
            [System.Text.RegularExpressions.RegexOptions]::IgnoreCase
        )
        if ($dropIndex.Success) {
            $indexName = $dropIndex.Groups[1].Value
            $table.indexes = @($table.indexes | Where-Object { $_.name -ne $indexName })
            continue
        }
    }
}

# FK referenced table/column은 전체 Table parse가 끝난 뒤 2차 검증한다.
# 선언 순서가 뒤에 있는 Table도 허용하되, 존재하지 않는 참조는 DB 실행 전에 실패시킨다.
$tableLookup = @{}
foreach ($table in $tables) {
    $localColumnLookup = @($table.columns | ForEach-Object { ([string] $_).ToLowerInvariant() })
    foreach ($index in @($table.indexes)) {
        $missingLocalColumns = @($index.columns | Where-Object {
            ([string] $_).ToLowerInvariant() -notin $localColumnLookup
        })
        if ($missingLocalColumns.Count -gt 0) {
            throw "Index가 존재하지 않는 Column을 참조합니다. db=$($table.logicalDatabase) table=$($table.tableName) index=$($index.name) missing=$($missingLocalColumns -join ',')"
        }
    }
    foreach ($fk in @($table.foreignKeys)) {
        $missingLocalColumns = @($fk.columns | Where-Object {
            ([string] $_).ToLowerInvariant() -notin $localColumnLookup
        })
        if ($missingLocalColumns.Count -gt 0) {
            throw "FK가 존재하지 않는 local Column을 참조합니다. db=$($table.logicalDatabase) table=$($table.tableName) fk=$($fk.name) missing=$($missingLocalColumns -join ',')"
        }
    }
    $tableKey = (([string]$table.logicalDatabase).ToLowerInvariant() + "." + ([string]$table.tableName).ToLowerInvariant())
    $tableLookup[$tableKey] = $table
}
foreach ($table in $tables) {
    foreach ($fk in @($table.foreignKeys)) {
        $referencedKey = (([string]$table.logicalDatabase).ToLowerInvariant() + "." + ([string]$fk.referencedTable).ToLowerInvariant())
        if (-not $tableLookup.ContainsKey($referencedKey)) {
            throw "FK가 존재하지 않는 referenced Table을 참조합니다. db=$($table.logicalDatabase) table=$($table.tableName) fk=$($fk.name) referencedTable=$($fk.referencedTable)"
        }
        $referencedTable = $tableLookup[$referencedKey]
        $referencedColumnLookup = @($referencedTable.columns | ForEach-Object { ([string]$_).ToLowerInvariant() })
        $missingReferencedColumns = @($fk.referencedColumns | Where-Object { ([string]$_).ToLowerInvariant() -notin $referencedColumnLookup })
        if ($missingReferencedColumns.Count -gt 0) {
            throw "FK가 존재하지 않는 referenced Column을 참조합니다. db=$($table.logicalDatabase) table=$($table.tableName) fk=$($fk.name) referencedTable=$($fk.referencedTable) missing=$($missingReferencedColumns -join ',')"
        }
        if (@($fk.columns).Count -ne @($fk.referencedColumns).Count) {
            throw "FK local/referenced Column 개수가 다릅니다. db=$($table.logicalDatabase) table=$($table.tableName) fk=$($fk.name)"
        }
    }
}

$profilePath = Join-Path $Root "cpf-tools/db/config/database-install.default.json"
$profile = Get-Content -LiteralPath $profilePath -Raw -Encoding UTF8 | ConvertFrom-Json
$expectedLogicalDatabases = @(
    $profile.modules.PSObject.Properties |
        Where-Object { [bool]$_.Value.enabled } |
        ForEach-Object { [string]$_.Value.logicalDatabase } |
        Sort-Object -Unique
)
$actualLogicalDatabases = @(
    $tables |
        ForEach-Object { [string]$_.logicalDatabase } |
        Sort-Object -Unique
)
$logicalDatabaseDifference = @(
    Compare-Object -ReferenceObject $expectedLogicalDatabases -DifferenceObject $actualLogicalDatabases
)
if ($logicalDatabaseDifference.Count -gt 0) {
    throw "Platform Schema logical DB 집합이 enabled Profile과 다릅니다: $($logicalDatabaseDifference | ConvertTo-Json -Compress)"
}

$manifest = [ordered]@{
    schemaVersion = 2
    generatedBy = "cpf-tools/db/tools/generate-database-schema-manifest.ps1"
    sourcePlan = "cpf-tools/db/config/database-source-plan.json"
    generatedBusinessDomainPolicy = "NO_FIXED_BUSINESS_DOMAIN_SCHEMA"
    tableCount = $tables.Count
    tables = @($tables | Sort-Object logicalDatabase, tableName)
}

$parent = Split-Path -Parent $OutputPath
New-Item -ItemType Directory -Force -Path $parent | Out-Null
[IO.File]::WriteAllText($OutputPath, ($manifest | ConvertTo-Json -Depth 50) + "`n", $Utf8NoBom)
Write-Host "Database schema manifest generated. tables=$($tables.Count) path=$OutputPath"
