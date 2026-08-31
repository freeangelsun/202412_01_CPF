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

if ([int] $Contract.schemaVersion -ne 2 -or
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
            [string] $object.logicalDatabase -cne $CurrentLogicalDatabase -or
            [string] $object.sourceFile -cne $CurrentSourceFile) {
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

[pscustomobject]@{
    status = "PASS"
    mode = if ($Check) { "CHECK" } else { "SYNC" }
    contract = [string] $Contract.contract
    objects = $Objects.Count
    vendors = $OfficialVendors.Count
    written = $Written
    checked = $Checked
} | ConvertTo-Json -Depth 4
