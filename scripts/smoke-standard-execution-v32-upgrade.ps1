param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ClientPath = $env:CPF_MARIADB_CLI,
    [string] $HostName = $(if ($env:CPF_DB_HOST) { $env:CPF_DB_HOST } else { "localhost" }),
    [string] $Port = $(if ($env:CPF_DB_PORT) { $env:CPF_DB_PORT } else { "3306" }),
    [string] $Username = $(if ($env:CPF_DB_ROOT_USERNAME) { $env:CPF_DB_ROOT_USERNAME } else { "root" }),
    [string] $Password = $env:CPF_DB_ROOT_PASSWORD,
    [string] $ResultDir = "build/runtime-smoke"
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
$Root = (Resolve-Path -LiteralPath $Root).Path
$resultRoot = if ([IO.Path]::IsPathRooted($ResultDir)) { $ResultDir } else { Join-Path $Root $ResultDir }
New-Item -ItemType Directory -Force -Path $resultRoot | Out-Null
$resultPath = Join-Path $resultRoot "standard-execution-v32-upgrade.sanitized.json"
$database = "cpf_v32_upgrade_test"
$startedAt = [DateTimeOffset]::Now

if ([string]::IsNullOrWhiteSpace($ClientPath)) {
    $command = Get-Command mariadb -ErrorAction SilentlyContinue
    if ($command) {
        $ClientPath = $command.Source
    } else {
        $candidate = "C:\Program Files\MariaDB 12.3\bin\mariadb.exe"
        if (Test-Path -LiteralPath $candidate) { $ClientPath = $candidate }
    }
}
if (-not (Test-Path -LiteralPath $ClientPath) -or [string]::IsNullOrWhiteSpace($Password)) {
    throw "MariaDB client 또는 root password가 없어 V32 업그레이드를 검증할 수 없습니다."
}

function Invoke-Sql([string] $Sql) {
    $tempRoot = Join-Path $Root "build/sql-smoke/v32"
    New-Item -ItemType Directory -Force -Path $tempRoot | Out-Null
    $tempPath = Join-Path $tempRoot ("v32-" + [Guid]::NewGuid().ToString("N") + ".sql")
    [IO.File]::WriteAllText($tempPath, $Sql, [Text.UTF8Encoding]::new($false))
    $start = [Diagnostics.ProcessStartInfo]::new()
    $start.FileName = $ClientPath
    $start.Arguments = "--protocol=tcp -h $HostName -P $Port -u $Username --ssl=0 --default-character-set=utf8mb4 --batch --raw --skip-column-names"
    $start.UseShellExecute = $false
    $start.RedirectStandardInput = $true
    $start.RedirectStandardOutput = $true
    $start.RedirectStandardError = $true
    try { $start.StandardInputEncoding = [Text.Encoding]::UTF8 } catch { }
    $start.StandardOutputEncoding = [Text.Encoding]::UTF8
    $start.StandardErrorEncoding = [Text.Encoding]::UTF8
    $start.EnvironmentVariables["MYSQL_PWD"] = $Password
    $start.EnvironmentVariables["MARIADB_PWD"] = $Password
    $process = [Diagnostics.Process]::new()
    $process.StartInfo = $start
    [void] $process.Start()
    $inputError = $null
    try {
        $sourcePath = $tempPath.Replace("\", "/")
        $process.StandardInput.Write("SOURCE $sourcePath;`n")
    } catch {
        $inputError = $_.Exception.Message
    } finally {
        try { $process.StandardInput.Close() } catch { }
    }
    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit()
    Remove-Item -LiteralPath $tempPath -Force -ErrorAction SilentlyContinue
    if ($process.ExitCode -ne 0 -or $inputError) {
        throw "MariaDB V32 upgrade SQL failed. exitCode=$($process.ExitCode) inputError=$inputError error=$stderr"
    }
    $stdout
}

$result = [ordered]@{
    startedAt = $startedAt.ToString("o")
    status = "실패"
    database = $database
    sourceMigration = "V28__standard_execution_catalog.sql"
    targetMigration = "V32__standard_execution_id_v2.sql"
    checks = [ordered]@{}
}

try {
    $v28 = [IO.File]::ReadAllText(
        (Join-Path $Root "specs/sql/migration/flyway/V28__standard_execution_catalog.sql"), [Text.Encoding]::UTF8)
    $v32 = [IO.File]::ReadAllText(
        (Join-Path $Root "specs/sql/migration/flyway/V32__standard_execution_id_v2.sql"), [Text.Encoding]::UTF8)
    $v28 = $v28.Replace("USE cpfDB;", "USE $database;")
    $v32 = $v32.Replace("USE cpfDB;", "USE $database;")
    $fixture = @"
USE $database;
INSERT INTO cpf_standard_execution (
    standard_execution_id, execution_name, execution_type, owner_domain, source_module,
    source_class, source_method, endpoint, operation_id, source_version, created_by, updated_by
) VALUES
    ('BADM-RLG-EX-0001', '구형 배치', 'BATCH', 'ADM', 'ADM', 'LegacyBatch', 'run', NULL, NULL, 'legacy', 'TEST', 'TEST'),
    ('OREF-QRY-01-0005', '구형 온라인', 'ONLINE', 'REF', 'REF', 'LegacyOnline', 'query', '/legacy', 'legacyQuery', 'legacy', 'TEST', 'TEST');
"@
    [void] (Invoke-Sql "DROP DATABASE IF EXISTS $database; CREATE DATABASE $database CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`n$v28`n$fixture`n$v32")
    $output = Invoke-Sql @"
USE $database;
SELECT COUNT(*) FROM cpf_standard_execution
WHERE standard_execution_id IN ('BADMRL0001', 'OREFQR0005');
SELECT COUNT(*) FROM cpf_standard_execution
WHERE standard_execution_id REGEXP '^[OB].*-.*$';
SELECT COUNT(*) FROM cpf_standard_execution_alias;
"@
    $lines = @($output -split "`r?`n" | Where-Object { $_ -match '^\d+$' })
    $result.checks.migratedFixtureCount = [int] $lines[0]
    $result.checks.remainingLegacyCount = [int] $lines[1]
    $result.checks.aliasCount = [int] $lines[2]
    if ($result.checks.migratedFixtureCount -ne 2 -or
        $result.checks.remainingLegacyCount -ne 0 -or
        $result.checks.aliasCount -ne 327) {
        throw "V32 업그레이드 결과가 기대값과 다릅니다."
    }
    $result.status = "완료"
} catch {
    $result.error = $_.Exception.Message.Replace($Password, "****")
    throw
} finally {
    try { [void] (Invoke-Sql "DROP DATABASE IF EXISTS $database;") } catch { }
    $result.finishedAt = [DateTimeOffset]::Now.ToString("o")
    [IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 8), [Text.UTF8Encoding]::new($false))
}

Write-Host "V32 표준 실행 ID 실DB 업그레이드 검증 통과: $resultPath"
