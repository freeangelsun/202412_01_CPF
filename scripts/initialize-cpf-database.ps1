param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [ValidateSet("mariadb", "mysql", "postgresql", "oracle", "sqlserver")]
    [string] $Vendor = $env:CPF_DB_VENDOR,
    [string] $HostName = $env:CPF_DB_HOST,
    [string] $Port = $env:CPF_DB_PORT,
    [string] $RootUsername = $env:CPF_DB_ROOT_USERNAME,
    [string] $RootPassword = $env:CPF_DB_ROOT_PASSWORD,
    [string] $MigrationPassword = $env:CPF_DB_MIGRATION_PASSWORD,
    [string] $AppPassword = $env:CPF_DB_APP_PASSWORD,
    [string] $ClientPath = $env:CPF_MARIADB_CLI,
    [string] $ResultDir = "",
    [switch] $RequireRun
)

if ($PSVersionTable.PSVersion.Major -lt 7) {
    throw "CPF DB 초기화는 UTF-8 및 안전한 Process Argument 처리를 위해 pwsh 7 이상이 필요합니다."
}

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"

if ([string]::IsNullOrWhiteSpace($Vendor)) { $Vendor = "mariadb" }
if ([string]::IsNullOrWhiteSpace($HostName)) { $HostName = "127.0.0.1" }
if ([string]::IsNullOrWhiteSpace($Port)) { $Port = "3306" }
if ([string]::IsNullOrWhiteSpace($RootUsername)) { $RootUsername = "root" }
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build\db-install"
}

$StatusDone = "완료"
$StatusNotVerified = "미검증"
$StatusFailed = "실패"
$OwnedSchemas = @("cpfDB", "cmnDB", "admDB", "bzaDB", "batDB", "mbrDB", "accDB", "refDB", "exsDB")
$SchemaFiles = @(
    "specs/sql/10_cpf_schema.sql",
    "specs/sql/20_cmn_schema.sql",
    "specs/sql/30_adm_schema.sql",
    "specs/sql/35_bat_schema.sql",
    "specs/sql/40_business_modules_schema.sql",
    "specs/sql/45_external_schema.sql"
)

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "$Vendor-initialize-result.sanitized.json"
$logPath = Join-Path $ResultDir "$Vendor-initialize.sanitized.log"
if (Test-Path -LiteralPath $logPath) {
    Remove-Item -LiteralPath $logPath -Force
}

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = $StatusNotVerified
    vendor = $Vendor
    host = $HostName
    port = $Port
    rootUsername = $RootUsername
    rootPasswordProvided = -not [string]::IsNullOrWhiteSpace($RootPassword)
    migrationPasswordProvided = -not [string]::IsNullOrWhiteSpace($MigrationPassword)
    appPasswordProvided = -not [string]::IsNullOrWhiteSpace($AppPassword)
    resourcePack = $null
    client = $null
    initialSchemaCount = $null
    installationMode = $null
    sqlFiles = @()
    steps = [ordered]@{}
    checks = [ordered]@{}
    blocker = $null
    error = $null
    logPath = $logPath
}

function Save-Result {
    $result.finishedAt = (Get-Date).ToString("o")
    [System.IO.File]::WriteAllText(
        $resultPath,
        ($result | ConvertTo-Json -Depth 30) + [Environment]::NewLine,
        [System.Text.UTF8Encoding]::new($false)
    )
}

function Add-SafeLog {
    param([string] $Message)

    $safe = ConvertTo-SafeMessage $Message
    Add-Content -LiteralPath $logPath -Encoding utf8NoBOM -Value "[$((Get-Date).ToString("o"))] $safe"
}

function ConvertTo-SafeMessage {
    param([string] $Message)

    if ([string]::IsNullOrWhiteSpace($Message)) { return $Message }
    foreach ($secret in @($RootPassword, $MigrationPassword, $AppPassword)) {
        if (-not [string]::IsNullOrWhiteSpace($secret)) {
            $Message = $Message.Replace($secret, "****")
        }
    }
    return $Message
}

function ConvertTo-MariaDbHexExpression {
    param([string] $Value)

    $bytes = [System.Text.Encoding]::UTF8.GetBytes($Value)
    $hex = [System.Convert]::ToHexString($bytes)
    return "CONVERT(0x$hex USING utf8mb4)"
}

function Find-MariaDbClient {
    if (-not [string]::IsNullOrWhiteSpace($ClientPath)) {
        if (Test-Path -LiteralPath $ClientPath -PathType Leaf) {
            return (Resolve-Path -LiteralPath $ClientPath).Path
        }
        $explicitCommand = Get-Command $ClientPath -ErrorAction SilentlyContinue
        if ($null -ne $explicitCommand) { return $explicitCommand.Source }
    }

    foreach ($name in @("mariadb", "mysql")) {
        $command = Get-Command $name -ErrorAction SilentlyContinue
        if ($null -ne $command) { return $command.Source }
    }
    return $null
}

function Invoke-MariaDbText {
    param(
        [string] $StepName,
        [string] $SqlText,
        [string] $Username = $RootUsername,
        [string] $Password = $RootPassword,
        [string] $Database = ""
    )

    $psi = [System.Diagnostics.ProcessStartInfo]::new()
    $psi.FileName = $result.client
    $psi.UseShellExecute = $false
    $psi.RedirectStandardInput = $true
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.StandardInputEncoding = [System.Text.Encoding]::UTF8
    $psi.StandardOutputEncoding = [System.Text.Encoding]::UTF8
    $psi.StandardErrorEncoding = [System.Text.Encoding]::UTF8
    foreach ($argument in @(
        "--protocol=TCP",
        "--host=$HostName",
        "--port=$Port",
        "--user=$Username",
        "--connect-timeout=5",
        "--default-character-set=utf8mb4",
        "--batch",
        "--raw",
        "--skip-column-names"
    )) {
        [void] $psi.ArgumentList.Add($argument)
    }
    if (-not [string]::IsNullOrWhiteSpace($Database)) {
        [void] $psi.ArgumentList.Add("--database=$Database")
    }
    if (-not [string]::IsNullOrWhiteSpace($Password)) {
        $psi.Environment["MYSQL_PWD"] = $Password
        $psi.Environment["MARIADB_PWD"] = $Password
    }

    $process = [System.Diagnostics.Process]::new()
    $process.StartInfo = $psi
    [void] $process.Start()
    $stdoutTask = $process.StandardOutput.ReadToEndAsync()
    $stderrTask = $process.StandardError.ReadToEndAsync()
    $inputError = $null
    try {
        $process.StandardInput.Write($SqlText)
    } catch {
        $inputError = $_.Exception.Message
    } finally {
        try { $process.StandardInput.Close() } catch {
            if ([string]::IsNullOrWhiteSpace($inputError)) { $inputError = $_.Exception.Message }
        }
    }
    $process.WaitForExit()
    $stdout = $stdoutTask.GetAwaiter().GetResult()
    $stderr = $stderrTask.GetAwaiter().GetResult()

    if ($process.ExitCode -ne 0 -or -not [string]::IsNullOrWhiteSpace($inputError)) {
        $safeError = ConvertTo-SafeMessage (($stderr + " " + $inputError).Trim())
        throw "MariaDB 단계 실패: step=$StepName exitCode=$($process.ExitCode) error=$safeError"
    }
    if (-not [string]::IsNullOrWhiteSpace($stderr)) {
        Add-SafeLog "$StepName stderr: $stderr"
    }
    return $stdout
}

function Invoke-MariaDbFile {
    param(
        [string] $StepName,
        [string] $RelativePath,
        [switch] $WithServicePasswords
    )

    $path = Join-Path $Root ($RelativePath -replace "/", "\")
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "SQL Resource가 없습니다: step=$StepName path=$RelativePath"
    }
    $sourcePath = (Resolve-Path -LiteralPath $path).Path.Replace("\", "/")
    $prefix = ""
    if ($WithServicePasswords) {
        $migrationExpression = ConvertTo-MariaDbHexExpression $MigrationPassword
        $appExpression = ConvertTo-MariaDbHexExpression $AppPassword
        $prefix = "SET @cpf_migration_password = $migrationExpression;`nSET @cpf_app_password = $appExpression;`n"
    }

    Add-SafeLog "SQL start: $RelativePath"
    $output = Invoke-MariaDbText -StepName $StepName -SqlText ($prefix + "SOURCE $sourcePath;`n")
    $result.sqlFiles += $RelativePath
    $result.steps[$StepName] = [ordered]@{
        status = $StatusDone
        file = $RelativePath
        outputLineCount = @($output -split "`r?`n" | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }).Count
    }
    Add-SafeLog "SQL completed: $RelativePath"
}

function Invoke-Scalar {
    param(
        [string] $StepName,
        [string] $SqlText,
        [string] $Username = $RootUsername,
        [string] $Password = $RootPassword,
        [string] $Database = ""
    )

    $output = Invoke-MariaDbText -StepName $StepName -SqlText ($SqlText.TrimEnd(";") + ";`n") `
        -Username $Username -Password $Password -Database $Database
    $line = @($output -split "`r?`n" | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }) | Select-Object -First 1
    return ([string] $line).Trim()
}

function Get-OutputLines {
    param([string] $StepName, [string] $SqlText)

    $output = Invoke-MariaDbText -StepName $StepName -SqlText ($SqlText.TrimEnd(";") + ";`n")
    return @($output -split "`r?`n" | ForEach-Object { $_.Trim() } | Where-Object { $_ })
}

function Get-ExpectedSchemaObjects {
    $tables = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
    $constraints = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
    $indexes = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)

    foreach ($relativePath in $SchemaFiles) {
        $path = Join-Path $Root ($relativePath -replace "/", "\")
        $text = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)
        $tableMatches = [regex]::Matches(
            $text,
            "CREATE\s+TABLE\s+IF\s+NOT\s+EXISTS\s+([A-Za-z0-9_]+)\s*\((?<body>.*?)\)\s*ENGINE\s*=",
            [System.Text.RegularExpressions.RegexOptions]::IgnoreCase -bor
            [System.Text.RegularExpressions.RegexOptions]::Singleline
        )
        foreach ($tableMatch in $tableMatches) {
            $precedingText = $text.Substring(0, $tableMatch.Index)
            $useMatches = [regex]::Matches(
                $precedingText,
                "(?im)^\s*USE\s+([A-Za-z0-9_]+)\s*;"
            )
            if ($useMatches.Count -eq 0) {
                throw "Schema SQL에서 USE 문을 찾지 못했습니다: $relativePath"
            }
            $schemaName = $useMatches[$useMatches.Count - 1].Groups[1].Value
            $tableName = $tableMatch.Groups[1].Value
            $body = $tableMatch.Groups["body"].Value
            [void] $tables.Add("$schemaName|$tableName")

            if ($body -match "(?i)\bPRIMARY\s+KEY\b") {
                [void] $constraints.Add("$schemaName|$tableName|PRIMARY")
                [void] $indexes.Add("$schemaName|$tableName|PRIMARY")
            }
            foreach ($match in [regex]::Matches($body, "(?i)\bCONSTRAINT\s+([A-Za-z0-9_]+)\s+")) {
                $name = $match.Groups[1].Value
                [void] $constraints.Add("$schemaName|$tableName|$name")
                $tail = $body.Substring($match.Index, [Math]::Min(120, $body.Length - $match.Index))
                if ($tail -match "(?i)^CONSTRAINT\s+[A-Za-z0-9_]+\s+UNIQUE\b") {
                    [void] $indexes.Add("$schemaName|$tableName|$name")
                }
            }
            foreach ($match in [regex]::Matches($body, "(?i)\bUNIQUE\s+KEY\s+([A-Za-z0-9_]+)\s*\(")) {
                $name = $match.Groups[1].Value
                [void] $constraints.Add("$schemaName|$tableName|$name")
                [void] $indexes.Add("$schemaName|$tableName|$name")
            }
            foreach ($match in [regex]::Matches($body, "(?i)\bINDEX\s+([A-Za-z0-9_]+)\s*\(")) {
                [void] $indexes.Add("$schemaName|$tableName|$($match.Groups[1].Value)")
            }
        }
    }
    return [pscustomobject]@{
        Tables = $tables
        Constraints = $constraints
        Indexes = $indexes
    }
}

function Assert-ExpectedSet {
    param(
        [string] $CheckName,
        [System.Collections.Generic.HashSet[string]] $Expected,
        [string[]] $Actual
    )

    $actualSet = [System.Collections.Generic.HashSet[string]]::new(
        [System.StringComparer]::OrdinalIgnoreCase
    )
    foreach ($item in $Actual) { [void] $actualSet.Add($item) }
    $missing = @($Expected | Where-Object { -not $actualSet.Contains($_) } | Sort-Object)
    $result.checks[$CheckName] = [ordered]@{
        status = if ($missing.Count -eq 0) { $StatusDone } else { $StatusFailed }
        expectedCount = $Expected.Count
        actualCount = $actualSet.Count
        missing = $missing
    }
    if ($missing.Count -gt 0) {
        throw "$CheckName 검증 실패. 누락=$($missing -join ', ')"
    }
}

function Test-SchemaContracts {
    $expected = Get-ExpectedSchemaObjects
    $schemaList = ($OwnedSchemas | ForEach-Object { "'$_'" }) -join ","
    $actualTables = Get-OutputLines "verify-tables" @"
SELECT CONCAT(table_schema, '|', table_name)
FROM information_schema.tables
WHERE table_schema IN ($schemaList)
  AND table_type = 'BASE TABLE'
"@
    $actualConstraints = Get-OutputLines "verify-constraints" @"
SELECT CONCAT(constraint_schema, '|', table_name, '|', constraint_name)
FROM information_schema.table_constraints
WHERE constraint_schema IN ($schemaList)
"@
    $actualIndexes = Get-OutputLines "verify-indexes" @"
SELECT DISTINCT CONCAT(table_schema, '|', table_name, '|', index_name)
FROM information_schema.statistics
WHERE table_schema IN ($schemaList)
"@
    Assert-ExpectedSet "tables" $expected.Tables $actualTables
    Assert-ExpectedSet "constraints" $expected.Constraints $actualConstraints
    Assert-ExpectedSet "indexes" $expected.Indexes $actualIndexes

    $cmnTableCount = [int] (Invoke-Scalar "verify-cmn-one-table" @"
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = 'cmnDB'
  AND table_type = 'BASE TABLE'
  AND table_name = 'cmn_sample_item'
"@)
    $cmnOtherCount = [int] (Invoke-Scalar "verify-cmn-no-other-table" @"
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = 'cmnDB'
  AND table_type = 'BASE TABLE'
  AND table_name <> 'cmn_sample_item'
"@)
    if ($cmnTableCount -ne 1 -or $cmnOtherCount -ne 0) {
        throw "cmnDB 단일 선택형 Table 계약 위반: cmn_sample_item=$cmnTableCount other=$cmnOtherCount"
    }
    $result.checks["cmnOneTable"] = [ordered]@{
        status = $StatusDone
        cmnSampleItemCount = $cmnTableCount
        otherTableCount = $cmnOtherCount
    }
}

function Test-ProductSeed {
    $installationCount = [int] (Invoke-Scalar "verify-installation-baseline" @"
SELECT COUNT(*)
FROM cpfDB.cpf_schema_installation
WHERE database_vendor = 'MARIADB'
  AND product_version = '1.0.0-SNAPSHOT'
  AND baseline_key = 'CPF_MARIADB_EMPTY_INSTALL_V1'
  AND install_state = 'PRODUCT_SEEDED'
"@)
    if ($installationCount -ne $OwnedSchemas.Count) {
        throw "CPF 설치 Baseline 행 수가 올바르지 않습니다: expected=$($OwnedSchemas.Count) actual=$installationCount"
    }

    $seedChecks = [ordered]@{
        cpfCode = "SELECT COUNT(*) FROM cpfDB.cpf_code"
        cpfResponseCode = "SELECT COUNT(*) FROM cpfDB.cpf_response_code"
        # 기본 관리자 Credential은 Product Seed에 넣지 않고 별도 일회성 Bootstrap 절차가 소유합니다.
        admRole = "SELECT COUNT(*) FROM admDB.adm_role"
        batJob = "SELECT COUNT(*) FROM batDB.bat_job"
        exsInstitution = "SELECT COUNT(*) FROM exsDB.exs_institution"
    }
    $counts = [ordered]@{}
    foreach ($entry in $seedChecks.GetEnumerator()) {
        $count = [int] (Invoke-Scalar "verify-seed-$($entry.Key)" $entry.Value)
        if ($count -le 0) { throw "필수 Product Seed가 없습니다: $($entry.Key)" }
        $counts[$entry.Key] = $count
    }
    $result.checks["productSeed"] = [ordered]@{
        status = $StatusDone
        installationRows = $installationCount
        rowCounts = $counts
    }
}

function Test-ServiceAccounts {
    $accountTargets = [ordered]@{
        cpf_app = @("cpfDB", "cpf_schema_installation")
        cpf_cmn_app = @("cmnDB", "cmn_sample_item")
        cpf_adm_app = @("admDB", "adm_operator")
        cpf_bat_app = @("batDB", "bat_job")
        cpf_ref_app = @("refDB", "ref_sample_item")
        cpf_exs_app = @("exsDB", "exs_institution")
        cpf_mbr_app = @("mbrDB", "mbr_member")
        cpf_bza_app = @("bzaDB", "bza_admin_user")
        cpf_acc_app = @("accDB", "acc_account")
    }
    foreach ($entry in $accountTargets.GetEnumerator()) {
        [void] (Invoke-Scalar "verify-login-$($entry.Key)" `
            "SELECT COUNT(*) FROM $($entry.Value[0]).$($entry.Value[1])" `
            -Username $entry.Key -Password $AppPassword -Database $entry.Value[0])
    }

    $appGrantees = ($accountTargets.Keys | ForEach-Object { "'''$_''@''localhost'''" }) -join ","
    $appDdlPrivilegeCount = [int] (Invoke-Scalar "verify-app-no-ddl" @"
SELECT COUNT(*)
FROM information_schema.schema_privileges
WHERE grantee IN ($appGrantees)
  AND privilege_type IN ('CREATE', 'ALTER', 'DROP', 'INDEX', 'REFERENCES')
"@)
    if ($appDdlPrivilegeCount -ne 0) {
        throw "Runtime Application 계정에 DDL 권한이 있습니다: count=$appDdlPrivilegeCount"
    }
    $result.checks["serviceAccounts"] = [ordered]@{
        status = $StatusDone
        appLoginCount = $accountTargets.Count
        appDdlPrivilegeCount = $appDdlPrivilegeCount
    }
}

try {
    & (Join-Path $Root "scripts\select-db-vendor-resources.ps1") `
        -Root $Root -Vendor $Vendor -ResultDir (Join-Path $ResultDir "selection") -RequireExecutable | Out-Null

    $manifestPath = Join-Path $Root "specs\sql\vendor-resource-manifest.json"
    $manifest = Get-Content -Raw -Encoding UTF8 -LiteralPath $manifestPath | ConvertFrom-Json
    $pack = $manifest.vendors.$Vendor
    $result.resourcePack = [ordered]@{
        status = [string] $pack.status
        client = [string] $pack.client
        lifecycle = $pack.lifecycle
    }
    if ($Vendor -ne "mariadb") {
        throw "현재 실제 초기화 실행이 검증된 Vendor는 MariaDB뿐입니다: vendor=$Vendor"
    }

    $result.client = Find-MariaDbClient
    if ([string]::IsNullOrWhiteSpace($result.client)) {
        $result.blocker = "MariaDB CLI를 찾지 못했습니다."
        if ($RequireRun) { throw $result.blocker }
        Save-Result
        Write-Warning "$($result.blocker) 결과: $resultPath"
        return
    }
    $serverVersion = Invoke-Scalar "connection" "SELECT VERSION()"
    $result.steps["connection"] = [ordered]@{
        status = $StatusDone
        serverVersion = $serverVersion
    }

    $schemaList = ($OwnedSchemas | ForEach-Object { "'$_'" }) -join ","
    $initialSchemaCount = [int] (Invoke-Scalar "schema-preflight" @"
SELECT COUNT(*)
FROM information_schema.schemata
WHERE schema_name IN ($schemaList)
"@)
    $result.initialSchemaCount = $initialSchemaCount

    if ($initialSchemaCount -eq 0) {
        if ([string]::IsNullOrWhiteSpace($MigrationPassword) -or [string]::IsNullOrWhiteSpace($AppPassword)) {
            $result.blocker = "Fresh Install에는 CPF_DB_MIGRATION_PASSWORD와 CPF_DB_APP_PASSWORD가 필요합니다."
            if ($RequireRun) { throw $result.blocker }
            Save-Result
            Write-Warning "$($result.blocker) 결과: $resultPath"
            return
        }
        $result.installationMode = "FRESH_EMPTY_INSTALL"
        Invoke-MariaDbFile "provision" ([string] $pack.lifecycle.provision) -WithServicePasswords
        $provisionedSchemaCount = [int] (Invoke-Scalar "verify-provisioned-schemas" @"
SELECT COUNT(*)
FROM information_schema.schemata
WHERE schema_name IN ($schemaList)
"@)
        if ($provisionedSchemaCount -ne $OwnedSchemas.Count) {
            throw "CPF Schema Provision 실패: expected=$($OwnedSchemas.Count) actual=$provisionedSchemaCount"
        }
        $result.checks["schemas"] = [ordered]@{
            status = $StatusDone
            expectedCount = $OwnedSchemas.Count
            actualCount = $provisionedSchemaCount
        }

        Invoke-MariaDbFile "empty-install" ([string] $pack.lifecycle.emptyInstall)
        Test-SchemaContracts
        Invoke-MariaDbFile "product-seed" ([string] $pack.lifecycle.productSeed)
        Test-ProductSeed
    } elseif ($initialSchemaCount -eq $OwnedSchemas.Count) {
        if ([string]::IsNullOrWhiteSpace($AppPassword)) {
            $result.blocker = "기존 Baseline의 Runtime 권한 검증에는 CPF_DB_APP_PASSWORD가 필요합니다."
            if ($RequireRun) { throw $result.blocker }
            Save-Result
            Write-Warning "$($result.blocker) 결과: $resultPath"
            return
        }
        $result.installationMode = "VERIFY_EXISTING_BASELINE"
        $baselineCount = [int] (Invoke-Scalar "existing-baseline" @"
SELECT COUNT(*)
FROM cpfDB.cpf_schema_installation
WHERE database_vendor = 'MARIADB'
  AND product_version = '1.0.0-SNAPSHOT'
  AND baseline_key = 'CPF_MARIADB_EMPTY_INSTALL_V1'
  AND install_state = 'PRODUCT_SEEDED'
"@)
        if ($baselineCount -ne $OwnedSchemas.Count) {
            throw "CPF Schema는 모두 존재하지만 공식 설치 Baseline이 일치하지 않습니다. Reset/Reinstall을 자동 수행하지 않습니다."
        }
        Test-SchemaContracts
        Test-ProductSeed
    } else {
        throw "CPF Schema가 부분적으로 존재합니다: expected=0 또는 $($OwnedSchemas.Count), actual=$initialSchemaCount. 자동 Reset/보완을 수행하지 않습니다."
    }

    Test-ServiceAccounts
    Invoke-MariaDbFile "verify" ([string] $pack.lifecycle.verify)

    $result.status = $StatusDone
    Add-SafeLog "CPF $Vendor initialization completed."
    Save-Result
    Write-Host "CPF MariaDB Provision -> Empty Install -> Product Seed -> Verify 완료"
    Write-Host "Sanitized result: $resultPath"
} catch {
    $safeError = ConvertTo-SafeMessage $_.Exception.Message
    $result.status = $StatusFailed
    $result.error = $safeError
    if ([string]::IsNullOrWhiteSpace($result.blocker)) { $result.blocker = $safeError }
    Add-SafeLog "FAILED: $safeError"
    Save-Result
    throw "$safeError (sanitized result: $resultPath)"
}
