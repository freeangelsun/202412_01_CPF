param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $OutputPath = ""
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

if ([string]::IsNullOrWhiteSpace($OutputPath)) {
    $OutputPath = Join-Path $Root "cpf-tools/db/generated/database-schema-manifest.json"
}
$sourcePlanPath = Join-Path $Root "cpf-tools/config/database-source-plan.json"
$sourcePlan = Get-Content -LiteralPath $sourcePlanPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 20
$mariaPlan = $sourcePlan.mariadb
if ([string]::IsNullOrWhiteSpace([string]$mariaPlan.sourceRoot)) {
    throw "database-source-plan.json에 mariadb.sourceRoot가 없습니다."
}
$sourceRoot = Join-Path $Root ([string]$mariaPlan.sourceRoot)

$tables = New-Object System.Collections.Generic.List[object]
$currentDb = ""

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

        foreach ($rawLine in ($body -split '\r?\n')) {
            $line = $rawLine.Trim().TrimEnd(',')
            if ([string]::IsNullOrWhiteSpace($line)) { continue }

            $columnMatch = [regex]::Match(
                $line,
                '^`?([A-Za-z][A-Za-z0-9_]*)`?\s+(BIGINT|INT|INTEGER|SMALLINT|TINYINT|MEDIUMINT|VARCHAR|CHAR|DATE|DATETIME|TIMESTAMP|LONGTEXT|MEDIUMTEXT|TEXT|DECIMAL|NUMERIC|JSON|BLOB|LONGBLOB|DOUBLE|FLOAT|BOOLEAN|VARBINARY|BINARY)\b',
                [System.Text.RegularExpressions.RegexOptions]::IgnoreCase
            )
            if ($columnMatch.Success) {
                $columns.Add($columnMatch.Groups[1].Value)
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
}

# FK referenced table/column은 전체 Table parse가 끝난 뒤 2차 검증한다.
# 선언 순서가 뒤에 있는 Table도 허용하되, 존재하지 않는 참조는 DB 실행 전에 실패시킨다.
$tableLookup = @{}
foreach ($table in $tables) {
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

if (@($tables | Where-Object { $_.logicalDatabase -eq "exsDB" -or $_.tableName -like "exs_*" }).Count -gt 0) {
    throw "EXS는 Generated Domain이므로 Platform canonical schema에 exsDB/exs_* Object가 존재할 수 없습니다."
}

$manifest = [ordered]@{
    schemaVersion = 2
    generatedBy = "cpf-tools/scripts/generate-database-schema-manifest.ps1"
    sourcePlan = "cpf-tools/config/database-source-plan.json"
    generatedBusinessDomainPolicy = "NO_FIXED_BUSINESS_DOMAIN_SCHEMA"
    tableCount = $tables.Count
    tables = @($tables | Sort-Object logicalDatabase, tableName)
}

$parent = Split-Path -Parent $OutputPath
New-Item -ItemType Directory -Force -Path $parent | Out-Null
[IO.File]::WriteAllText($OutputPath, ($manifest | ConvertTo-Json -Depth 50) + "`n", $Utf8NoBom)
Write-Host "Database schema manifest generated. tables=$($tables.Count) path=$OutputPath"
