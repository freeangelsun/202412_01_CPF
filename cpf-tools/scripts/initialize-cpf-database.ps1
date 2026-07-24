param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $ProfilePath = "",
    [string] $ResultDir = "",
    [switch] $All,
    [string[]] $DomainName = @(),
    [string[]] $SystemCode = @(),
    [string[]] $ModuleName = @(),
    [ValidateSet("profile", "product", "none", "all")]
    [string] $SeedMode = "profile",
    [switch] $RequireRun
)

if ($PSVersionTable.PSVersion.Major -lt 7) {
    throw "CPF DB 초기화는 pwsh 7 이상이 필요합니다."
}

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path

. (Join-Path $PSScriptRoot "database-profile-common.ps1")

if ([string]::IsNullOrWhiteSpace($ProfilePath)) {
    $ProfilePath = Join-Path $Root "cpf-tools/config/database-install.default.json"
} elseif (-not [System.IO.Path]::IsPathRooted($ProfilePath)) {
    $ProfilePath = Join-Path $Root $ProfilePath
}
$ProfilePath = [System.IO.Path]::GetFullPath($ProfilePath)
$profile = Get-CpfDatabaseProfile $ProfilePath

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/db-install"
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$moduleOrder = @("core", "common", "admin", "bizAdmin", "batch", "reference", "member", "account")
$moduleProfiles = @{}
foreach ($key in $moduleOrder) {
    $moduleProfiles[$key] = ConvertTo-CpfModuleProfile $profile $key
}

$enabledKeys = @($moduleOrder | Where-Object { $moduleProfiles[$_].enabled })
foreach ($requiredKey in @($moduleOrder | Where-Object { $moduleProfiles[$_].required })) {
    if (-not $moduleProfiles[$requiredKey].enabled) {
        throw "필수 Module DB를 disabled로 설정할 수 없습니다: $requiredKey"
    }
}
if ($enabledKeys.Count -eq 0) { throw "설치할 Module DB가 하나도 없습니다." }

$hasSelector = $All -or $DomainName.Count -gt 0 -or $SystemCode.Count -gt 0 -or $ModuleName.Count -gt 0
if ($All -and ($DomainName.Count -gt 0 -or $SystemCode.Count -gt 0 -or $ModuleName.Count -gt 0)) {
    throw "-All과 DomainName/SystemCode/ModuleName 선택자는 동시에 사용할 수 없습니다."
}

if (-not $hasSelector -or $All) {
    $selectedKeys = @($enabledKeys)
} else {
    $selected = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)

    foreach ($name in $DomainName) {
        $matched = @($moduleOrder | Where-Object {
            $moduleProfiles[$_].domainName -eq $name
        })
        if ($matched.Count -eq 0) { throw "알 수 없는 DomainName입니다: $name" }
        foreach ($key in $matched) { [void]$selected.Add($key) }
    }

    foreach ($code in $SystemCode) {
        $normalized = $code.Trim().ToUpperInvariant()
        $matched = @($moduleOrder | Where-Object {
            $moduleProfiles[$_].systemCode -eq $normalized
        })
        if ($matched.Count -eq 0) { throw "알 수 없는 SystemCode입니다: $code" }
        foreach ($key in $matched) { [void]$selected.Add($key) }
    }

    foreach ($name in $ModuleName) {
        $matched = @($moduleOrder | Where-Object {
            $moduleProfiles[$_].moduleName -eq $name
        })
        if ($matched.Count -eq 0) { throw "알 수 없는 ModuleName입니다: $name" }
        foreach ($key in $matched) { [void]$selected.Add($key) }
    }

    $selectedKeys = @($moduleOrder | Where-Object { $selected.Contains($_) })
}

$disabledSelected = @($selectedKeys | Where-Object { -not $moduleProfiles[$_].enabled })
if ($disabledSelected.Count -gt 0) {
    throw "Profile에서 disabled인 Module은 설치할 수 없습니다: $($disabledSelected -join ', ')"
}
if ($selectedKeys.Count -eq 0) { throw "선택된 Module DB가 하나도 없습니다." }

Write-Host "CPF DB selected modules: $($selectedKeys -join ', ')"

# 현재 플랫폼 전체 Vendor Pack은 MariaDB만 실행 검증되어 있습니다.
# 다른 Vendor 값은 Profile/Generator 계약에는 허용하지만 절대로 MariaDB SQL로 fallback하지 않습니다.
$unsupportedExecution = @($selectedKeys | Where-Object { $moduleProfiles[$_].vendor -ne "mariadb" })
if ($unsupportedExecution.Count -gt 0) {
    $details = $unsupportedExecution | ForEach-Object {
        "$_=$($moduleProfiles[$_].vendor)"
    }
    throw "Platform DB Vendor Pack 실행 미구현/미검증입니다. MariaDB로 fallback하지 않습니다: $($details -join ', ')"
}

$logicalToKey = @{
    cpfDB = "core"
    cmnDB = "common"
    admDB = "admin"
    bzaDB = "bizAdmin"
    batDB = "batch"
    refDB = "reference"
    mbrDB = "member"
    accDB = "account"
}

$installFile = Join-Path $Root "cpf-tools/db/vendor/mariadb/install/00_empty_install.sql"
$productSeedFile = Join-Path $Root "cpf-tools/db/vendor/mariadb/seed/00_product_seed.sql"
$optionalSampleSeedFile = Join-Path $Root "cpf-tools/db/vendor/mariadb/seed/00_optional_sample_seed.sql"
$testSeedFile = Join-Path $Root "cpf-tools/db/vendor/mariadb/seed/00_test_seed.sql"
foreach ($requiredFile in @($installFile, $productSeedFile, $optionalSampleSeedFile, $testSeedFile)) {
    if (-not (Test-Path -LiteralPath $requiredFile -PathType Leaf)) {
        throw "MariaDB Vendor Pack 파일이 없습니다: $requiredFile"
    }
}

function Find-MariaClient {
    param($Target)
    if (-not [string]::IsNullOrWhiteSpace($Target.clientPath) -and
        (Test-Path -LiteralPath $Target.clientPath -PathType Leaf)) {
        return (Resolve-Path -LiteralPath $Target.clientPath).Path
    }
    foreach ($name in @("mariadb", "mysql")) {
        $cmd = Get-Command $name -ErrorAction SilentlyContinue
        if ($null -ne $cmd) { return $cmd.Source }
    }
    throw "MariaDB/MySQL CLI를 찾을 수 없습니다. module=$($Target.moduleKey)"
}

function Protect-CpfSecretText {
    param(
        [string] $Text,
        [string[]] $Secrets
    )
    if ($null -eq $Text) { return "" }
    $safe = $Text
    foreach ($secret in $Secrets) {
        if (-not [string]::IsNullOrWhiteSpace($secret)) {
            $safe = $safe.Replace($secret, "****")
        }
    }
    return $safe
}

function New-MariaProcessStartInfo {
    param(
        $Target,
        [string] $Username,
        [string] $Password,
        [string] $Database = "",
        [switch] $RedirectInput
    )

    $client = Find-MariaClient $Target
    $psi = [System.Diagnostics.ProcessStartInfo]::new()
    $psi.FileName = $client
    $psi.UseShellExecute = $false
    $psi.RedirectStandardInput = [bool]$RedirectInput
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.CreateNoWindow = $true
    if ($RedirectInput) {
        $psi.StandardInputEncoding = [System.Text.Encoding]::UTF8
    }
    $psi.StandardOutputEncoding = [System.Text.Encoding]::UTF8
    $psi.StandardErrorEncoding = [System.Text.Encoding]::UTF8

    foreach ($arg in @(
        "--protocol=TCP",
        "--host=$($Target.host)",
        "--port=$($Target.port)",
        "--user=$Username",
        "--connect-timeout=5",
        "--default-character-set=utf8mb4",
        "--batch",
        "--raw",
        "--skip-column-names"
    )) { [void]$psi.ArgumentList.Add($arg) }

    if (-not [string]::IsNullOrWhiteSpace($Database)) {
        [void]$psi.ArgumentList.Add("--database=$Database")
    }

    if (-not [string]::IsNullOrWhiteSpace($Password)) {
        $psi.Environment["MYSQL_PWD"] = $Password
        $psi.Environment["MARIADB_PWD"] = $Password
    }
    return $psi
}

function Test-MariaConnection {
    param(
        $Target,
        [string] $Username,
        [string] $Password
    )

    $psi = New-MariaProcessStartInfo `
        -Target $Target `
        -Username $Username `
        -Password $Password
    [void]$psi.ArgumentList.Add("--execute=SELECT 1;")

    $p = [System.Diagnostics.Process]::new()
    $p.StartInfo = $psi
    [void]$p.Start()
    $stdout = $p.StandardOutput.ReadToEnd()
    $stderr = $p.StandardError.ReadToEnd()
    $p.WaitForExit()

    if ($p.ExitCode -ne 0) {
        $safeError = Protect-CpfSecretText $stderr @($Password)
        throw "MariaDB 접속/인증 사전검증 실패 module=$($Target.moduleKey) host=$($Target.host):$($Target.port) user=$Username exit=$($p.ExitCode) error=$safeError"
    }
}

function Invoke-MariaText {
    param(
        $Target,
        [string] $Username,
        [string] $Password,
        [string] $SqlText,
        [string] $Database = ""
    )

    $psi = New-MariaProcessStartInfo `
        -Target $Target `
        -Username $Username `
        -Password $Password `
        -Database $Database `
        -RedirectInput

    $p = [System.Diagnostics.Process]::new()
    $p.StartInfo = $psi
    [void]$p.Start()

    $stdoutTask = $p.StandardOutput.ReadToEndAsync()
    $stderrTask = $p.StandardError.ReadToEndAsync()

    $inputError = $null
    try {
        $p.StandardInput.Write($SqlText)
    } catch {
        $inputError = $_.Exception.Message
    } finally {
        try {
            $p.StandardInput.Close()
        } catch {
            if ([string]::IsNullOrWhiteSpace($inputError)) {
                $inputError = $_.Exception.Message
            }
        }
    }

    $p.WaitForExit()
    $stdout = $stdoutTask.GetAwaiter().GetResult()
    $stderr = $stderrTask.GetAwaiter().GetResult()

    if ($p.ExitCode -ne 0 -or -not [string]::IsNullOrWhiteSpace($inputError)) {
        $combined = if (-not [string]::IsNullOrWhiteSpace($stderr)) {
            $stderr.Trim()
        } else {
            [string]$inputError
        }
        $safeError = Protect-CpfSecretText $combined @($Password)
        throw "MariaDB 실행 실패 module=$($Target.moduleKey) host=$($Target.host):$($Target.port) user=$Username database=$Database exit=$($p.ExitCode) error=$safeError"
    }

    return $stdout
}

function Sql-HexLiteral {
    param([string] $Value)
    $hex = [Convert]::ToHexString([Text.Encoding]::UTF8.GetBytes($Value))
    return "CONVERT(0x$hex USING utf8mb4)"
}

function Quote-Db {
    param([string] $Identifier)
    return "``$Identifier``"
}

function Quote-UserHost {
    param([string] $Username, [string] $HostPart)
    $u = $Username.Replace("'", "''")
    $h = $HostPart.Replace("'", "''")
    return "'$u'@'$h'"
}

function Get-UseSections {
    param([string] $Text)
    $matches = [regex]::Matches(
        $Text,
        '(?im)^[ \t]*USE[ \t]+`?([A-Za-z][A-Za-z0-9_$#]*)`?[ \t]*;[ \t]*$'
    )
    $list = New-Object System.Collections.Generic.List[object]
    for ($i = 0; $i -lt $matches.Count; $i++) {
        $start = $matches[$i].Index
        $end = if ($i + 1 -lt $matches.Count) { $matches[$i + 1].Index } else { $Text.Length }
        $list.Add([pscustomobject]@{
            logicalDatabase = $matches[$i].Groups[1].Value
            text = $Text.Substring($start, $end - $start)
        })
    }
    return $list.ToArray()
}

function Render-LogicalDatabaseNames {
    param([string] $Sql)
    $rendered = $Sql
    foreach ($logical in $logicalToKey.Keys) {
        $key = $logicalToKey[$logical]
        $target = $moduleProfiles[$key]
        if ($target.enabled) {
            $physical = $target.databaseName
            $rendered = [regex]::Replace(
                $rendered,
                "(?<![A-Za-z0-9_])" + [regex]::Escape($logical) + "(?![A-Za-z0-9_])",
                [System.Text.RegularExpressions.MatchEvaluator]{ param($m) $physical }
            )
        }
    }
    return $rendered
}

function Get-ModuleSql {
    param([string] $File, [string] $LogicalDatabase, [switch] $StripBaseline)
    $text = [IO.File]::ReadAllText($File, [Text.Encoding]::UTF8)
    if ($StripBaseline) {
        $marker = "-- Product Seed의 마지막 단계까지 성공한 CPF 소유 Schema만 공식 Baseline으로 기록합니다."
        $idx = $text.IndexOf($marker, [StringComparison]::Ordinal)
        if ($idx -ge 0) { $text = $text.Substring(0, $idx) }
    }
    $sections = @(Get-UseSections $text | Where-Object { $_.logicalDatabase -eq $LogicalDatabase })
    if ($sections.Count -eq 0) { return "" }
    $joined = ($sections | ForEach-Object { $_.text }) -join "`n"
    return Render-LogicalDatabaseNames $joined
}


function Get-ExpectedTableColumns {
    param([string] $Sql)

    $rows = New-Object System.Collections.Generic.List[object]
    $matches = [regex]::Matches(
        $Sql,
        '(?is)CREATE\s+TABLE\s+IF\s+NOT\s+EXISTS\s+`?([A-Za-z][A-Za-z0-9_]*)`?\s*\((.*?)\)\s*ENGINE='
    )
    foreach ($match in $matches) {
        $tableName = $match.Groups[1].Value
        $body = $match.Groups[2].Value
        $columns = New-Object System.Collections.Generic.List[string]
        foreach ($lineRaw in ($body -split '\r?\n')) {
            $line = $lineRaw.Trim()
            $columnMatch = [regex]::Match(
                $line,
                '^`?([A-Za-z][A-Za-z0-9_]*)`?\s+(BIGINT|INT|INTEGER|SMALLINT|TINYINT|MEDIUMINT|VARCHAR|CHAR|DATE|DATETIME|TIMESTAMP|LONGTEXT|MEDIUMTEXT|TEXT|DECIMAL|NUMERIC|JSON|BLOB|LONGBLOB|DOUBLE|FLOAT|BOOLEAN|VARBINARY|BINARY)\b',
                [System.Text.RegularExpressions.RegexOptions]::IgnoreCase
            )
            if ($columnMatch.Success) {
                $columns.Add($columnMatch.Groups[1].Value)
            }
        }
        $rows.Add([pscustomobject]@{
            tableName = $tableName
            columns = @($columns.ToArray())
        })
    }
    return @($rows.ToArray())
}

function Assert-MariaSchemaColumns {
    param(
        $Target,
        [string] $DatabaseName,
        [object[]] $ExpectedTableColumns
    )

    foreach ($table in $ExpectedTableColumns) {
        $dbEscaped = $DatabaseName.Replace("'", "''")
        $tableEscaped = ([string]$table.tableName).Replace("'", "''")
        $actualText = Invoke-MariaText $Target $Target.adminUsername $Target.adminPassword @"
SELECT column_name
FROM information_schema.columns
WHERE table_schema = '$dbEscaped'
  AND table_name = '$tableEscaped'
ORDER BY ordinal_position;
"@
        $actual = @($actualText -split '\r?\n' | Where-Object { $_ })
        $expected = @($table.columns)
        $missing = @($expected | Where-Object { $_ -notin $actual })
        $unexpected = @($actual | Where-Object { $_ -notin $expected })
        if ($missing.Count -gt 0 -or $unexpected.Count -gt 0) {
            throw "Schema drift 감지. Fresh DDL로 덮어쓰지 않고 migration이 필요합니다. module=$($Target.moduleKey) table=$($table.tableName) missing=$($missing -join ',') unexpected=$($unexpected -join ',')"
        }
    }
}

function Get-ExpectedTables {
    param([string] $Sql)
    return @([regex]::Matches(
        $Sql,
        '(?im)CREATE[ \t]+TABLE[ \t]+IF[ \t]+NOT[ \t]+EXISTS[ \t]+`?([A-Za-z][A-Za-z0-9_]*)`?'
    ) | ForEach-Object { $_.Groups[1].Value } | Sort-Object -Unique)
}

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = "미검증"
    profilePath = $ProfilePath
    profileName = [string]$profile.profileName
    seedMode = $SeedMode
    selectedModules = $selectedKeys
    modules = [ordered]@{}
}

try {
    foreach ($key in $selectedKeys) {
        $t = $moduleProfiles[$key]
        Write-Host "[$key] vendor=$($t.vendor) host=$($t.host):$($t.port) database=$($t.databaseName)"

        if ($RequireRun) {
            Test-MariaConnection $t $t.adminUsername $t.adminPassword
            Write-Host "[$key] admin connection preflight=PASS"
        }

        $installSql = Get-ModuleSql $installFile $t.logicalDatabase
        if ([string]::IsNullOrWhiteSpace($installSql)) {
            throw "Module DDL section이 없습니다: module=$key logicalDatabase=$($t.logicalDatabase)"
        }
        $expectedTables = @(Get-ExpectedTables $installSql)
        $expectedTableColumns = @(Get-ExpectedTableColumns $installSql)
        if ($expectedTables.Count -eq 0) {
            throw "Module expected table을 추출할 수 없습니다: module=$key"
        }

        $dbQuoted = Quote-Db $t.databaseName
        $migrationAccount = Quote-UserHost $t.migrationUsername $t.migrationUserHost
        $runtimeAccount = Quote-UserHost $t.runtimeUsername $t.runtimeUserHost
        # CONCAT() 안의 SQL 문자열에 계정 literal을 다시 삽입하므로 작은따옴표를 한 번 더 escape한다.
        # 예: 'cpf_migration'@'%' -> ''cpf_migration''@''%''
        $migrationAccountDynamic = $migrationAccount.Replace("'", "''")
        $runtimeAccountDynamic = $runtimeAccount.Replace("'", "''")
        $migPwd = Sql-HexLiteral $t.migrationPassword
        $runPwd = Sql-HexLiteral $t.runtimePassword

        $provisionSql = @"
CREATE DATABASE IF NOT EXISTS $dbQuoted CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
SET @cpf_mig_pwd = $migPwd;
SET @cpf_run_pwd = $runPwd;
SET @cpf_sql = CONCAT('CREATE USER IF NOT EXISTS $migrationAccountDynamic IDENTIFIED BY ', QUOTE(@cpf_mig_pwd));
PREPARE cpf_stmt FROM @cpf_sql; EXECUTE cpf_stmt; DEALLOCATE PREPARE cpf_stmt;
SET @cpf_sql = CONCAT('ALTER USER $migrationAccountDynamic IDENTIFIED BY ', QUOTE(@cpf_mig_pwd));
PREPARE cpf_stmt FROM @cpf_sql; EXECUTE cpf_stmt; DEALLOCATE PREPARE cpf_stmt;
GRANT ALL PRIVILEGES ON $dbQuoted.* TO $migrationAccount;
SET @cpf_sql = CONCAT('CREATE USER IF NOT EXISTS $runtimeAccountDynamic IDENTIFIED BY ', QUOTE(@cpf_run_pwd));
PREPARE cpf_stmt FROM @cpf_sql; EXECUTE cpf_stmt; DEALLOCATE PREPARE cpf_stmt;
SET @cpf_sql = CONCAT('ALTER USER $runtimeAccountDynamic IDENTIFIED BY ', QUOTE(@cpf_run_pwd));
PREPARE cpf_stmt FROM @cpf_sql; EXECUTE cpf_stmt; DEALLOCATE PREPARE cpf_stmt;
GRANT SELECT, INSERT, UPDATE, DELETE, EXECUTE ON $dbQuoted.* TO $runtimeAccount;
SET @cpf_mig_pwd = NULL; SET @cpf_run_pwd = NULL; SET @cpf_sql = NULL;
FLUSH PRIVILEGES;
"@
        if ($RequireRun) {
            [void](Invoke-MariaText $t $t.adminUsername $t.adminPassword $provisionSql)

            $actualBeforeText = Invoke-MariaText $t $t.adminUsername $t.adminPassword @"
SELECT table_name
FROM information_schema.tables
WHERE table_schema = '$($t.databaseName.Replace("'","''"))'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;
"@
            $actualBefore = @($actualBeforeText -split '\r?\n' | Where-Object { $_ })
            $existingExpected = @($actualBefore | Where-Object { $_ -in $expectedTables })
            if ($existingExpected.Count -gt 0 -and $existingExpected.Count -ne $expectedTables.Count) {
                throw "부분 설치 DB를 감지했습니다. 자동 보완/Reset하지 않습니다: module=$key expected=$($expectedTables.Count) existing=$($existingExpected.Count)"
            }

            if ($existingExpected.Count -eq 0) {
                Write-Host "[$key] ddl=fresh-install expectedTables=$($expectedTables.Count)"
                $renderedInstall = "USE $dbQuoted;`n" + $installSql
                [void](Invoke-MariaText $t $t.migrationUsername $t.migrationPassword $renderedInstall $t.databaseName)
            } else {
                Assert-MariaSchemaColumns $t $t.databaseName $expectedTableColumns
                Write-Host "[$key] ddl=skip-existing-complete schemaDrift=NONE expectedTables=$($expectedTables.Count)"
            }

            $seedPlans = switch ($SeedMode) {
                "none" {
                    @()
                }
                "product" {
                    @([pscustomobject]@{ name = "product"; enabled = $true; file = $productSeedFile })
                }
                "all" {
                    @(
                        [pscustomobject]@{ name = "product"; enabled = $true; file = $productSeedFile },
                        [pscustomobject]@{ name = "optionalSample"; enabled = $true; file = $optionalSampleSeedFile },
                        [pscustomobject]@{ name = "test"; enabled = $true; file = $testSeedFile }
                    )
                }
                default {
                    @(
                        [pscustomobject]@{ name = "product"; enabled = $t.productSeed; file = $productSeedFile },
                        [pscustomobject]@{ name = "optionalSample"; enabled = $t.optionalSampleSeed; file = $optionalSampleSeedFile },
                        [pscustomobject]@{ name = "test"; enabled = $t.testSeed; file = $testSeedFile }
                    )
                }
            }
            foreach ($seedPlan in $seedPlans) {
                if (-not $seedPlan.enabled) { continue }
                $seedSql = Get-ModuleSql $seedPlan.file $t.logicalDatabase
                if ([string]::IsNullOrWhiteSpace($seedSql)) { continue }

                Write-Host "[$key] seed=$($seedPlan.name)"
                $seedSql = "USE $dbQuoted;`n" + $seedSql
                [void](Invoke-MariaText $t $t.migrationUsername $t.migrationPassword $seedSql $t.databaseName)
            }

            $actualText = Invoke-MariaText $t $t.adminUsername $t.adminPassword @"
SELECT table_name
FROM information_schema.tables
WHERE table_schema = '$($t.databaseName.Replace("'","''"))'
  AND table_type = 'BASE TABLE'
ORDER BY table_name;
"@
            $actual = @($actualText -split '\r?\n' | Where-Object { $_ })
            $missing = @($expectedTables | Where-Object { $_ -notin $actual })
            if ($missing.Count -gt 0) {
                throw "tables 검증 실패. module=$key 누락=$($missing -join ', ')"
            }
            Assert-MariaSchemaColumns $t $t.databaseName $expectedTableColumns

            $probeTable = $expectedTables[0]
            [void](Invoke-MariaText $t $t.runtimeUsername $t.runtimePassword "SELECT COUNT(*) FROM ``$probeTable``;" $t.databaseName)
        }

        $result.modules[$key] = [ordered]@{
            status = if ($RequireRun) { "완료" } else { "미검증" }
            vendor = $t.vendor
            host = $t.host
            port = $t.port
            databaseName = $t.databaseName
            schemaName = $t.schemaName
            domainName = $t.domainName
            systemCode = $t.systemCode
            moduleName = $t.moduleName
            expectedTableCount = $expectedTables.Count
            migrationUsername = $t.migrationUsername
            runtimeUsername = $t.runtimeUsername
        }
    }

    if ($RequireRun -and $moduleProfiles["core"].enabled) {
        $core = $moduleProfiles["core"]
        $baselineTableText = Invoke-MariaText $core $core.adminUsername $core.adminPassword @"
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = '$($core.databaseName.Replace("'","''"))'
  AND table_name = 'cpf_schema_installation';
"@
        if ([int](($baselineTableText -split '\r?\n' | Where-Object { $_ } | Select-Object -First 1)) -eq 1) {
            $values = @()
            foreach ($key in $selectedKeys) {
                $t = $moduleProfiles[$key]
                $values += "('$($t.databaseName)', '$($t.systemCode)', '$($t.vendor.ToUpperInvariant())', '1.0.0-SNAPSHOT', 'CPF_PROFILE_INSTALL_V1', 'PRODUCT_SEEDED', 'CPF_INSTALLER', 'CPF_INSTALLER')"
            }
            $baselineSql = @"
INSERT INTO cpf_schema_installation (
    schema_name, system_code, database_vendor, product_version,
    baseline_key, install_state, created_by, updated_by
) VALUES
$($values -join ",`n")
ON DUPLICATE KEY UPDATE
    system_code=VALUES(system_code),
    database_vendor=VALUES(database_vendor),
    product_version=VALUES(product_version),
    baseline_key=VALUES(baseline_key),
    install_state=VALUES(install_state),
    updated_by=VALUES(updated_by),
    updated_at=CURRENT_TIMESTAMP(3);
"@
            [void](Invoke-MariaText $core $core.migrationUsername $core.migrationPassword $baselineSql $core.databaseName)
        } else {
            Write-Host "CPF baseline registry=SKIP (core baseline table not installed yet)"
        }
    }

    $result.status = if ($RequireRun) { "완료" } else { "미검증" }
}
catch {
    $result.status = "실패"
    $result.error = $_.Exception.Message
    throw
}
finally {
    $result.finishedAt = (Get-Date).ToString("o")
    $resultPath = Join-Path $ResultDir "database-profile-install-result.sanitized.json"
    [IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 50) + "`n", $Utf8NoBom)
    Write-Host "Sanitized result: $resultPath"
}

if (-not $RequireRun) {
    Write-Host "CPF DB Profile plan 검증 완료. 실제 DB는 변경하지 않았습니다."
} else {
    Write-Host "CPF DB Profile 기반 설치/검증 완료."
}
