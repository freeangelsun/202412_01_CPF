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
$sourceRoot = Join-Path $Root "cpf-tools/db/source/mariadb"

$tables = New-Object System.Collections.Generic.List[object]
$currentDb = ""

foreach ($fileName in @($sourcePlan.mariadb.emptyInstallFiles)) {
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

            $fkMatch = [regex]::Match(
                $line,
                '^CONSTRAINT\s+`?([A-Za-z][A-Za-z0-9_]*)`?\s+FOREIGN\s+KEY\s*\(([^)]+)\)\s+REFERENCES\s+`?([A-Za-z][A-Za-z0-9_]*)`?\s*\(([^)]+)\)',
                [System.Text.RegularExpressions.RegexOptions]::IgnoreCase
            )
            if ($fkMatch.Success) {
                $local = @($fkMatch.Groups[2].Value -split ',' | ForEach-Object { $_.Trim('`', ' ') })
                $foreignKeys.Add([pscustomobject]@{
                    name = $fkMatch.Groups[1].Value
                    columns = $local
                    referencedTable = $fkMatch.Groups[3].Value
                    referencedColumns = @($fkMatch.Groups[4].Value -split ',' | ForEach-Object { $_.Trim('`', ' ') })
                })
            }
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

if (@($tables | Where-Object { $_.logicalDatabase -eq "exsDB" -or $_.tableName -like "exs_*" }).Count -gt 0) {
    throw "EXS는 Generated Domain이므로 Platform canonical schema에 exsDB/exs_* Object가 존재할 수 없습니다."
}

$manifest = [ordered]@{
    schemaVersion = 1
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
