param(
    [string] $Root = (Resolve-Path (Join-Path $PSScriptRoot "../../..")).Path,
    [switch] $Check
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$Root = (Resolve-Path -LiteralPath $Root).Path
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$ContractPath = Join-Path $Root "cpf-tools/db/canonical/platform-non-table-objects.json"
$Contract = Get-Content -Raw -Encoding UTF8 -LiteralPath $ContractPath | ConvertFrom-Json -Depth 30
$PlatformSchemaPath = Join-Path $Root "cpf-tools/db/canonical/platform-schema.json"
$PlatformSchema = Get-Content -Raw -Encoding UTF8 -LiteralPath $PlatformSchemaPath | ConvertFrom-Json -Depth 100
$OfficialVendors = @("mariadb", "postgresql", "oracle")
$Marker = "spring-batch-6-sequences"
$Written = 0
$Checked = 0

function Normalize-Text([string] $Text) {
    return $Text.Replace("`r`n", "`n").Replace("`r", "`n").TrimEnd() + "`n"
}

function Set-GeneratedArtifact(
    [Parameter(Mandatory = $true)][string] $Path,
    [Parameter(Mandatory = $true)][string] $Expected,
    [switch] $ImmutableVersioned
) {
    $Expected = Normalize-Text $Expected
    if (Test-Path -LiteralPath $Path -PathType Leaf) {
        $Actual = Normalize-Text ([IO.File]::ReadAllText($Path, [Text.Encoding]::UTF8))
        if ($Actual -ceq $Expected) {
            if ($Check) { $script:Checked++ }
            return
        }
        if ($ImmutableVersioned) {
            throw "IMMUTABLE_MIGRATION_CONFLICT path=$Path"
        }
    }
    if ($Check) {
        if (-not (Test-Path -LiteralPath $Path -PathType Leaf)) {
            throw "Missing generated non-table DB artifact: $Path"
        }
        throw "Generated non-table DB artifact drift: $Path"
    }
    [IO.Directory]::CreateDirectory((Split-Path -Parent $Path)) | Out-Null
    [IO.File]::WriteAllText($Path, $Expected, $Utf8NoBom)
    $script:Written++
}

function Remove-ManagedBlock([string] $Text) {
    $begin = [regex]::Escape("-- CPF_CANONICAL_OBJECTS_BEGIN $Marker")
    $end = [regex]::Escape("-- CPF_CANONICAL_OBJECTS_END $Marker")
    return [regex]::Replace(
        $Text,
        "(?ms)^[ \t]*$begin[ \t]*\r?\n.*?^[ \t]*$end[ \t]*(?:\r?\n)?",
        ""
    )
}

function Add-ManagedBlock([string] $Text, [string] $Body) {
    $base = (Remove-ManagedBlock $Text).Replace("`r`n", "`n").Replace("`r", "`n").TrimEnd()
    $block = @"
-- CPF_CANONICAL_OBJECTS_BEGIN $Marker
$($Body.Trim())
-- CPF_CANONICAL_OBJECTS_END $Marker
"@
    return $base + "`n`n" + $block + "`n"
}

function Assert-Identifier([string] $Value, [string] $DisplayName) {
    if ($Value -cnotmatch "^[A-Za-z][A-Za-z0-9_]{1,62}$") {
        throw "Invalid ${DisplayName}: $Value"
    }
}

function Get-CurrentSequenceObjects(
    [object[]] $HistoricalObjects,
    [string] $CurrentLogicalDatabase,
    [string] $CurrentSourceFile
) {
    return @($HistoricalObjects | ForEach-Object {
        $historical = $_
        $matches = @($PlatformSchema.tables | Where-Object {
            [string] $_.currentName -ceq [string] $historical.idTable
        })
        if ($matches.Count -ne 1) {
            throw "Current Spring Batch table mapping must be exact: table=$($historical.idTable) count=$($matches.Count)"
        }
        $mapped = $matches[0]
        if ([string] $mapped.logicalDatabase -cne $CurrentLogicalDatabase) {
            throw "Current Spring Batch table must belong to ${CurrentLogicalDatabase}: table=$($historical.idTable) actual=$($mapped.logicalDatabase)"
        }
        $targetTable = [string] $mapped.targetTableName
        Assert-Identifier $targetTable "current Spring Batch table"
        [pscustomobject]@{
            kind = "sequence"
            module = "bat"
            logicalDatabase = $CurrentLogicalDatabase
            sourceFile = $CurrentSourceFile
            name = "${targetTable}_SEQ"
            idTable = $targetTable
            idColumn = [string] $historical.idColumn
            legacyNames = @()
        }
    })
}

function Get-SequenceSourceSql([string] $Vendor, [object[]] $Objects) {
    $definition = $Contract.vendorDefinition.$Vendor
    $lines = [Collections.Generic.List[string]]::new()
    $lines.Add("-- Generated from cpf-tools/db/canonical/platform-non-table-objects.json.")
    $lines.Add("-- Spring Batch 6.0.4 JobRepository sequence contract; do not edit vendor SQL directly.")
    foreach ($object in $Objects) {
        $name = [string] $object.name
        if ($Vendor -ceq "mariadb") {
            $lines.Add("CREATE SEQUENCE IF NOT EXISTS $name")
            $lines.Add("    START WITH $($definition.startWith) MINVALUE $($definition.minValue) MAXVALUE $($definition.maxValue)")
            $lines.Add("    INCREMENT BY $($definition.incrementBy) $($definition.cache) $($definition.cycle) ENGINE=$($definition.engine);")
        } elseif ($Vendor -ceq "postgresql") {
            $lines.Add(
                "CREATE SEQUENCE $name START WITH $($definition.startWith) " +
                "MINVALUE $($definition.minValue) MAXVALUE $($definition.maxValue) " +
                "INCREMENT BY $($definition.incrementBy) $($definition.cycle);"
            )
        } else {
            $lines.Add(
                "CREATE SEQUENCE $name START WITH $($definition.startWith) " +
                "MINVALUE $($definition.minValue) MAXVALUE $($definition.maxValue) " +
                "INCREMENT BY $($definition.incrementBy) $($definition.order) $($definition.cycle);"
            )
        }
        $lines.Add("")
    }
    return ($lines -join "`n").TrimEnd()
}

function Remove-LegacySequenceDdl([string] $Text, [string[]] $Names) {
    $result = Remove-ManagedBlock $Text
    foreach ($name in $Names) {
        $escaped = [regex]::Escape($name)
        $result = [regex]::Replace(
            $result,
            "(?ims)^[ \t]*CREATE[ \t]+SEQUENCE(?:[ \t]+IF[ \t]+NOT[ \t]+EXISTS)?[ \t]+$escaped\b.*?;[ \t]*(?:\r?\n)?",
            ""
        )
    }
    $result = [regex]::Replace(
        $result,
        "(?im)^[ \t]*-- Spring Batch 5\.2\.4[^\r\n]*\r?\n[ \t]*-- MariaDB SEQUENCE[^\r\n]*\r?\n",
        ""
    )
    return $result
}

function Get-VerifySql([string] $Vendor, [string[]] $ExpectedNames, [string[]] $LegacyNames) {
    $nameList = ($ExpectedNames | ForEach-Object { "'$($_.ToUpperInvariant())'" }) -join ", "
    $legacyList = ($LegacyNames | ForEach-Object { "'$($_.ToUpperInvariant())'" }) -join ", "
    $expectedCount = $ExpectedNames.Count
    $managedPrefix = $ExpectedNames[0].ToUpperInvariant()
    foreach ($expectedName in $ExpectedNames) {
        $upperName = $expectedName.ToUpperInvariant()
        while ($managedPrefix.Length -gt 0 -and -not $upperName.StartsWith($managedPrefix, [StringComparison]::Ordinal)) {
            $managedPrefix = $managedPrefix.Substring(0, $managedPrefix.Length - 1)
        }
    }
    if ([string]::IsNullOrWhiteSpace($managedPrefix)) {
        throw "Canonical non-table objects do not share a managed sequence namespace."
    }
    $managedPrefixLength = $managedPrefix.Length
    if ($Vendor -ceq "mariadb") {
        return @"
-- CPF_LOGICAL_DATABASE=$($Contract.canonicalPolicy.currentLogicalDatabase)
-- Fail-closed Spring Batch 6.0.4 sequence name/count verification.
SELECT 'bat_spring_batch_6_sequence_contract' AS check_name,
       IF(
           (SELECT COUNT(*)
              FROM information_schema.tables
             WHERE table_schema = DATABASE()
               AND table_type = 'SEQUENCE'
               AND LEFT(UPPER(table_name), $managedPrefixLength) = '$managedPrefix') = $expectedCount
           AND
           (SELECT COUNT(*)
              FROM information_schema.tables
             WHERE table_schema = DATABASE()
               AND table_type = 'SEQUENCE'
               AND UPPER(table_name) IN ($nameList)) = $expectedCount
           AND
           (SELECT COUNT(*)
              FROM information_schema.tables
             WHERE table_schema = DATABASE()
               AND UPPER(table_name) IN ($legacyList)) = 0,
           1, 0
       ) AS passed;
"@
    }
    if ($Vendor -ceq "postgresql") {
        return @"
-- CPF_LOGICAL_DATABASE=$($Contract.canonicalPolicy.currentLogicalDatabase)
-- Fail-closed Spring Batch 6.0.4 sequence name/count verification.
SELECT 'bat_spring_batch_6_sequence_contract' AS check_name,
       CASE WHEN
           (SELECT COUNT(*) FROM information_schema.sequences
             WHERE sequence_schema = current_schema()
               AND LEFT(UPPER(sequence_name), $managedPrefixLength) = '$managedPrefix') = $expectedCount
           AND
           (SELECT COUNT(*) FROM information_schema.sequences
             WHERE sequence_schema = current_schema()
               AND UPPER(sequence_name) IN ($nameList)) = $expectedCount
           AND
           (SELECT COUNT(*) FROM information_schema.sequences
             WHERE sequence_schema = current_schema()
               AND UPPER(sequence_name) IN ($legacyList)) = 0
       THEN 1 ELSE 0 END AS passed;
"@
    }
    return @"
-- CPF_LOGICAL_DATABASE=$($Contract.canonicalPolicy.currentLogicalDatabase)
-- Fail-closed Spring Batch 6.0.4 sequence name/count verification.
SELECT 'bat_spring_batch_6_sequence_contract' AS check_name,
       CASE WHEN
           (SELECT COUNT(*) FROM user_sequences
             WHERE SUBSTR(sequence_name, 1, $managedPrefixLength) = '$managedPrefix') = $expectedCount
           AND
           (SELECT COUNT(*) FROM user_sequences
             WHERE sequence_name IN ($nameList)) = $expectedCount
           AND
           (SELECT COUNT(*) FROM user_objects
             WHERE object_name IN ($legacyList)) = 0
       THEN 1 ELSE 0 END AS passed
FROM dual;
"@
}

function Get-MariaCaptureSql([string] $Name, [string] $IdColumn) {
    return @"
SET @cpf_object_kind = (
    SELECT UPPER(table_type)
      FROM information_schema.tables
     WHERE table_schema = DATABASE() AND UPPER(table_name) = UPPER('$Name')
     LIMIT 1
);
SET @cpf_observed_next = 0;
SET @cpf_capture_sql = CASE
    WHEN @cpf_object_kind = 'SEQUENCE'
        THEN 'SELECT NEXT VALUE FOR $Name INTO @cpf_observed_next'
    WHEN @cpf_object_kind = 'BASE TABLE'
        THEN 'SELECT COALESCE(MAX($IdColumn), 0) + 1 INTO @cpf_observed_next FROM $Name'
    ELSE 'SELECT 0 INTO @cpf_observed_next'
END;
PREPARE cpf_sequence_stmt FROM @cpf_capture_sql;
EXECUTE cpf_sequence_stmt;
DEALLOCATE PREPARE cpf_sequence_stmt;
SET @cpf_sequence_start = GREATEST(
    @cpf_sequence_start,
    @cpf_observed_next
);
"@
}

function Get-MariaDropSql([string] $Name) {
    return @"
SET @cpf_object_kind = (
    SELECT UPPER(table_type)
      FROM information_schema.tables
     WHERE table_schema = DATABASE() AND UPPER(table_name) = UPPER('$Name')
     LIMIT 1
);
SET @cpf_drop_sql = CASE
    WHEN @cpf_object_kind = 'SEQUENCE' THEN 'DROP SEQUENCE $Name'
    WHEN @cpf_object_kind = 'BASE TABLE' THEN 'DROP TABLE $Name'
    ELSE 'SELECT 1'
END;
PREPARE cpf_sequence_stmt FROM @cpf_drop_sql;
EXECUTE cpf_sequence_stmt;
DEALLOCATE PREPARE cpf_sequence_stmt;
"@
}

function Get-MariaCreateSql([string] $Name) {
    $definition = $Contract.vendorDefinition.mariadb
    return @"
SET @cpf_create_sql = CONCAT(
    'CREATE SEQUENCE $Name START WITH ',
    CAST(@cpf_sequence_start AS CHAR),
    ' MINVALUE $($definition.minValue) MAXVALUE $($definition.maxValue) INCREMENT BY $($definition.incrementBy) $($definition.cache) $($definition.cycle) ENGINE=$($definition.engine)'
);
PREPARE cpf_sequence_stmt FROM @cpf_create_sql;
EXECUTE cpf_sequence_stmt;
DEALLOCATE PREPARE cpf_sequence_stmt;
"@
}

function Get-MariaMigration([object[]] $Objects) {
    $body = [Collections.Generic.List[string]]::new()
    $body.Add("-- Generated from cpf-tools/db/canonical/platform-non-table-objects.json.")
    $body.Add("-- Spring Batch runtime must be stopped while sequence objects are replaced.")
    $body.Add("-- Existing sequence next values and persisted Spring Batch IDs are preserved as an exact monotonic lower bound.")
    $body.Add("USE batDB;")
    $body.Add("")
    foreach ($object in $Objects) {
        $name = [string] $object.name
        $idTable = [string] $object.idTable
        $idColumn = [string] $object.idColumn
        $start = [int64] $Contract.vendorDefinition.mariadb.startWith
        $body.Add("SET @cpf_sequence_start = GREATEST(")
        $body.Add("    $start,")
        $body.Add("    (SELECT COALESCE(MAX($idColumn), 0) + 1 FROM $idTable)")
        $body.Add(");")
        $body.Add((Get-MariaCaptureSql $name "ID").Trim())
        foreach ($legacy in @($object.legacyNames)) {
            $body.Add((Get-MariaCaptureSql ([string] $legacy) "ID").Trim())
        }
        $body.Add((Get-MariaDropSql $name).Trim())
        foreach ($legacy in @($object.legacyNames)) {
            $body.Add((Get-MariaDropSql ([string] $legacy)).Trim())
        }
        $body.Add((Get-MariaCreateSql $name).Trim())
        $body.Add("")
    }
    return ($body -join "`n").TrimEnd() + "`n"
}

function Get-MariaRollback([object] $JobInstanceObject) {
    $legacyName = [string] @($JobInstanceObject.legacyNames)[0]
    $idTable = [string] $JobInstanceObject.idTable
    $idColumn = [string] $JobInstanceObject.idColumn
    $name = [string] $JobInstanceObject.name
    $start = [int64] $Contract.vendorDefinition.mariadb.startWith
    return @"
-- Generated exact compatibility rollback for V$($Contract.migration.version).
-- Spring Batch runtime must be stopped while sequence objects are replaced.
USE batDB;

SET @cpf_sequence_start = GREATEST(
    $start,
    (SELECT COALESCE(MAX($idColumn), 0) + 1 FROM $idTable)
);
$(Get-MariaCaptureSql $name "ID")
$(Get-MariaCaptureSql $legacyName "ID")
$(Get-MariaDropSql $name)
$(Get-MariaDropSql $legacyName)
$(Get-MariaCreateSql $legacyName)
"@
}

function Get-PostgresqlMigration([object[]] $Objects) {
    $definition = $Contract.vendorDefinition.postgresql
    $body = [Collections.Generic.List[string]]::new()
    $body.Add("-- Generated from cpf-tools/db/canonical/platform-non-table-objects.json.")
    foreach ($object in $Objects) {
        $name = [string] $object.name
        $table = [string] $object.idTable
        $column = [string] $object.idColumn
        $body.Add(
            "CREATE SEQUENCE IF NOT EXISTS $name START WITH $($definition.startWith) " +
            "MINVALUE $($definition.minValue) MAXVALUE $($definition.maxValue) " +
            "INCREMENT BY $($definition.incrementBy) $($definition.cycle);"
        )
        $body.Add("SELECT setval(")
        $body.Add("    '$name',")
        $body.Add("    GREATEST(")
        $body.Add("        (SELECT COALESCE(MAX($column), 0) + 1 FROM $table),")
        $body.Add("        (SELECT CASE WHEN is_called THEN last_value + 1 ELSE last_value END FROM $name)")
        $body.Add("    ),")
        $body.Add("    false")
        $body.Add(");")
        $body.Add("")
    }
    return ($body -join "`n").TrimEnd() + "`n"
}

function Get-OracleMigration([object[]] $Objects) {
    $definition = $Contract.vendorDefinition.oracle
    $body = [Collections.Generic.List[string]]::new()
    $body.Add("-- Generated from cpf-tools/db/canonical/platform-non-table-objects.json.")
    foreach ($object in $Objects) {
        $name = [string] $object.name
        $table = [string] $object.idTable
        $column = [string] $object.idColumn
        $body.Add("DECLARE")
        $body.Add("    cpf_sequence_count NUMBER;")
        $body.Add("    cpf_sequence_start NUMBER;")
        $body.Add("BEGIN")
        $body.Add("    SELECT COUNT(*) INTO cpf_sequence_count")
        $body.Add("      FROM user_sequences WHERE sequence_name = '$name';")
        $body.Add("    IF cpf_sequence_count = 0 THEN")
        $body.Add(
            "        SELECT GREATEST(NVL(MAX($column), -1) + 1, $($definition.startWith)) " +
            "INTO cpf_sequence_start FROM $table;"
        )
        $body.Add("        EXECUTE IMMEDIATE")
        $body.Add("            'CREATE SEQUENCE $name START WITH ' || cpf_sequence_start ||")
        $body.Add(
            "            ' MINVALUE $($definition.minValue) MAXVALUE $($definition.maxValue) " +
            "INCREMENT BY $($definition.incrementBy) $($definition.order) $($definition.cycle)';"
        )
        $body.Add("    END IF;")
        $body.Add("END;")
        $body.Add("/")
        $body.Add("")
    }
    return ($body -join "`n").TrimEnd() + "`n"
}

function Get-DropRollback([string] $Vendor, [object[]] $Objects) {
    $body = [Collections.Generic.List[string]]::new()
    $body.Add("-- Generated rollback for V$($Contract.migration.version) Spring Batch 6.0.4 sequences.")
    foreach ($object in $Objects) {
        $name = [string] $object.name
        if ($Vendor -ceq "postgresql") {
            $body.Add("DROP SEQUENCE IF EXISTS $name;")
        } else {
            $body.Add(
                "BEGIN EXECUTE IMMEDIATE 'DROP SEQUENCE $name'; " +
                "EXCEPTION WHEN OTHERS THEN IF SQLCODE != -2289 THEN RAISE; END IF; END;"
            )
            $body.Add("/")
        }
    }
    return ($body -join "`n").TrimEnd() + "`n"
}

if ([int] $Contract.schemaVersion -ne 1 -or
        [string] $Contract.contract -cne "CPF_PLATFORM_NON_TABLE_OBJECTS") {
    throw "Invalid CPF Platform non-table object contract header."
}
$CurrentLogicalDatabase = [string] $Contract.canonicalPolicy.currentLogicalDatabase
$CurrentSourceFile = [string] $Contract.canonicalPolicy.currentSourceFile
Assert-Identifier $CurrentLogicalDatabase "current logical database"
if ($CurrentSourceFile -cnotmatch '^[A-Za-z0-9_.-]+\.sql$') {
    throw "Invalid current non-table source file: $CurrentSourceFile"
}
$contractVendors = @($Contract.canonicalPolicy.officialVendors | ForEach-Object { [string] $_ })
if (($contractVendors -join "`n") -cne ($OfficialVendors -join "`n")) {
    throw "Platform non-table object vendors must be exactly: $($OfficialVendors -join ',')"
}
$Objects = @($Contract.objects)
if ($Objects.Count -ne 3 -or @($Objects.name | Sort-Object -CaseSensitive -Unique).Count -ne 3) {
    throw "Spring Batch non-table object contract must contain exactly three unique sequences."
}
foreach ($object in $Objects) {
    if ([string] $object.kind -cne "sequence" -or
            [string] $object.logicalDatabase -cne "batDB" -or
            [string] $object.sourceFile -cne "35_bat_schema.sql") {
        throw "Invalid Spring Batch sequence ownership: $($object.name)"
    }
    Assert-Identifier ([string] $object.name) "sequence name"
    Assert-Identifier ([string] $object.idTable) "sequence id table"
    Assert-Identifier ([string] $object.idColumn) "sequence id column"
    foreach ($legacy in @($object.legacyNames)) {
        Assert-Identifier ([string] $legacy) "legacy sequence name"
    }
}
$ExpectedNames = @($Objects.name | ForEach-Object { [string] $_ })
$LegacyNames = @($Objects.legacyNames | ForEach-Object { [string] $_ } | Sort-Object -Unique)
if (($ExpectedNames | Sort-Object) -join "," -cne
        (@("BATCH_JOB_EXECUTION_SEQ", "BATCH_JOB_INSTANCE_SEQ", "BATCH_STEP_EXECUTION_SEQ") -join ",")) {
    throw "Spring Batch 6.0.4 sequence names do not match the official contract."
}
if (($LegacyNames -join ",") -cne "BATCH_JOB_SEQ") {
    throw "Unexpected Spring Batch legacy sequence contract: $($LegacyNames -join ',')"
}
$CurrentObjects = @(Get-CurrentSequenceObjects $Objects $CurrentLogicalDatabase $CurrentSourceFile)
$CurrentExpectedNames = @($CurrentObjects.name | ForEach-Object { [string] $_ })
if (($CurrentExpectedNames | Sort-Object) -join "," -cne
        (@("BAT_SB_JOB_EXECUTION_SEQ", "BAT_SB_JOB_INSTANCE_SEQ", "BAT_SB_STEP_EXECUTION_SEQ") -join ",")) {
    throw "Current Spring Batch sequence names do not match the canonical table mapping."
}

$RetiredCurrentNames = @($ExpectedNames + $LegacyNames | Sort-Object -Unique)
$AllManagedNames = @($CurrentExpectedNames + $RetiredCurrentNames | Sort-Object -Unique)
foreach ($vendor in $OfficialVendors) {
    $schemaPath = Join-Path $Root "cpf-tools/db/vendor/$vendor/source/$CurrentSourceFile"
    if (-not (Test-Path -LiteralPath $schemaPath -PathType Leaf)) {
        throw "Missing current platform vendor source schema: $schemaPath"
    }
    $schemaText = [IO.File]::ReadAllText($schemaPath, [Text.Encoding]::UTF8)
    $schemaBase = Remove-LegacySequenceDdl $schemaText $AllManagedNames
    $schemaExpected = Add-ManagedBlock $schemaBase (Get-SequenceSourceSql $vendor $CurrentObjects)
    Set-GeneratedArtifact $schemaPath $schemaExpected

    # Remove only the managed current projection from its retired split-database
    # location. Historical V73/R73 files below retain their original batDB owner.
    $retiredCurrentPath = Join-Path $Root "cpf-tools/db/vendor/$vendor/source/35_bat_schema.sql"
    if ($retiredCurrentPath -cne $schemaPath -and (Test-Path -LiteralPath $retiredCurrentPath -PathType Leaf)) {
        $retiredText = [IO.File]::ReadAllText($retiredCurrentPath, [Text.Encoding]::UTF8)
        $retiredExpected = Remove-LegacySequenceDdl $retiredText $AllManagedNames
        Set-GeneratedArtifact $retiredCurrentPath $retiredExpected
    }

    $verifyRelative = if ($vendor -ceq "mariadb") {
        "cpf-tools/db/vendor/mariadb/source/99_smoke_check.sql"
    } else {
        "cpf-tools/db/vendor/$vendor/source/00_verify.sql"
    }
    $verifyPath = Join-Path $Root $verifyRelative
    if (-not (Test-Path -LiteralPath $verifyPath -PathType Leaf)) {
        throw "Missing vendor verify source: $verifyPath"
    }
    $verifyText = [IO.File]::ReadAllText($verifyPath, [Text.Encoding]::UTF8)
    $verifyExpected = Add-ManagedBlock $verifyText (Get-VerifySql $vendor $CurrentExpectedNames $RetiredCurrentNames)
    Set-GeneratedArtifact $verifyPath $verifyExpected
}

$version = [int] $Contract.migration.version
$description = [string] $Contract.migration.description
$jobInstanceObject = @($Objects | Where-Object { $_.name -ceq "BATCH_JOB_INSTANCE_SEQ" })[0]
Set-GeneratedArtifact `
    (Join-Path $Root "cpf-tools/db/vendor/mariadb/source/migration/flyway/V${version}__${description}.sql") `
    (Get-MariaMigration $Objects) `
    -ImmutableVersioned
Set-GeneratedArtifact `
    (Join-Path $Root "cpf-tools/db/vendor/mariadb/source/migration/rollback/R${version}__${description}.sql") `
    (Get-MariaRollback $jobInstanceObject) `
    -ImmutableVersioned
Set-GeneratedArtifact `
    (Join-Path $Root "cpf-tools/db/vendor/postgresql/migration/flyway/batDB/V${version}__${description}.sql") `
    (Get-PostgresqlMigration $Objects) `
    -ImmutableVersioned
Set-GeneratedArtifact `
    (Join-Path $Root "cpf-tools/db/vendor/postgresql/rollback/batDB/R${version}__${description}.sql") `
    (Get-DropRollback "postgresql" $Objects) `
    -ImmutableVersioned
Set-GeneratedArtifact `
    (Join-Path $Root "cpf-tools/db/vendor/oracle/migration/flyway/batDB/V${version}__${description}.sql") `
    (Get-OracleMigration $Objects) `
    -ImmutableVersioned
Set-GeneratedArtifact `
    (Join-Path $Root "cpf-tools/db/vendor/oracle/rollback/batDB/R${version}__${description}.sql") `
    (Get-DropRollback "oracle" $Objects) `
    -ImmutableVersioned

[pscustomobject]@{
    status = "PASS"
    mode = if ($Check) { "CHECK" } else { "SYNC" }
    contract = [string] $Contract.contract
    springBatchVersion = [string] $Contract.upstreamReference.version
    objects = $Objects.Count
    vendors = $OfficialVendors.Count
    written = $Written
    checked = $Checked
} | ConvertTo-Json -Depth 4
