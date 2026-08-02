param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $ProfilePath = "",
    [ValidateSet("cpf", "bza", "ref")]
    [string[]] $Module = @("cpf", "bza", "ref"),
    [string] $EvidencePath = ""
)

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest

$Root = (Resolve-Path -LiteralPath $Root).Path
if ([string]::IsNullOrWhiteSpace($ProfilePath)) {
    $ProfilePath = Join-Path $Root "cpf-tools\config\database-install.default.json"
}
$ProfilePath = (Resolve-Path -LiteralPath $ProfilePath).Path
. (Join-Path $Root "cpf-tools\scripts\database-profile-common.ps1")

$syncScript = Join-Path $Root "cpf-tools\scripts\sync-platform-runtime-query-packs.ps1"
& $syncScript -Root $Root -Check | Out-Null

$profile = Get-CpfDatabaseProfile -Path $ProfilePath
$contractPath = Join-Path $Root "cpf-tools\db\metadata\platform-runtime-query-contract.json"
$contract = Get-Content -Raw -Encoding UTF8 -LiteralPath $contractPath | ConvertFrom-Json
$selectedModules = @($Module | Sort-Object -CaseSensitive -Unique)
$failures = [System.Collections.Generic.List[object]]::new()
$moduleResults = [System.Collections.Generic.List[object]]::new()
$startedAt = [DateTimeOffset]::Now

function ConvertTo-MariaStringLiteral {
    param([Parameter(Mandatory = $true)][string] $Value)
    return $Value.Replace("\", "\\").Replace("'", "''")
}

function ConvertTo-PreparedSql {
    param(
        [Parameter(Mandatory = $true)][string] $Sql,
        [Parameter(Mandatory = $true)][string] $ParameterStyle
    )
    $prepared = $Sql
    if ($ParameterStyle -ceq "SPRING_NAMED") {
        $prepared = [regex]::Replace(
            $prepared,
            "(?<!:):[A-Za-z][A-Za-z0-9_]*",
            "?")
    }
    $prepared = $prepared.Replace("%s", "?")
    return $prepared.Trim()
}

function Add-MariaTlsArguments {
    param(
        [Parameter(Mandatory = $true)][Diagnostics.ProcessStartInfo] $ProcessInfo,
        [Parameter(Mandatory = $true)][string] $SslMode,
        [string] $SslCaPath = ""
    )
    switch ($SslMode) {
        "disabled" {
            [void] $ProcessInfo.ArgumentList.Add("--ssl=0")
        }
        "preferred" {
            # MariaDB Client 기본 TLS negotiation을 사용합니다.
        }
        "required" {
            [void] $ProcessInfo.ArgumentList.Add("--ssl=1")
            [void] $ProcessInfo.ArgumentList.Add("--ssl-verify-server-cert=0")
        }
        "verify-full" {
            if ([string]::IsNullOrWhiteSpace($SslCaPath) -or
                    -not (Test-Path -LiteralPath $SslCaPath -PathType Leaf)) {
                throw "sslMode=verify-full에는 유효한 sslCaPath가 필요합니다."
            }
            [void] $ProcessInfo.ArgumentList.Add("--ssl=1")
            [void] $ProcessInfo.ArgumentList.Add("--ssl-verify-server-cert")
            [void] $ProcessInfo.ArgumentList.Add("--ssl-ca=$SslCaPath")
        }
        default {
            throw "지원하지 않는 sslMode입니다: $SslMode"
        }
    }
}

function Test-PreparedStatement {
    param(
        [Parameter(Mandatory = $true)][object] $Connection,
        [Parameter(Mandatory = $true)][string] $ModuleCode,
        [Parameter(Mandatory = $true)][string] $Key,
        [Parameter(Mandatory = $true)][string] $SqlText
    )
    $escapedSql = ConvertTo-MariaStringLiteral $SqlText
    $inputSql = @"
SET NAMES utf8mb4;
SET @cpf_runtime_sql = '$escapedSql';
PREPARE cpf_runtime_stmt FROM @cpf_runtime_sql;
DEALLOCATE PREPARE cpf_runtime_stmt;
"@
    $processInfo = [Diagnostics.ProcessStartInfo]::new()
    $processInfo.FileName = [string] $Connection.clientPath
    $processInfo.UseShellExecute = $false
    $processInfo.RedirectStandardInput = $true
    $processInfo.RedirectStandardOutput = $true
    $processInfo.RedirectStandardError = $true
    $processInfo.CreateNoWindow = $true
    foreach ($argument in @(
        "--protocol=TCP",
        "--host=$($Connection.host)",
        "--port=$($Connection.port)",
        "--user=$($Connection.username)",
        "--database=$($Connection.databaseName)",
        "--batch",
        "--raw",
        "--skip-column-names",
        "--default-character-set=utf8mb4",
        "--connect-timeout=5"
    )) {
        [void] $processInfo.ArgumentList.Add($argument)
    }
    Add-MariaTlsArguments `
        -ProcessInfo $processInfo `
        -SslMode ([string] $Connection.sslMode) `
        -SslCaPath ([string] $Connection.sslCaPath)
    $processInfo.Environment["MYSQL_PWD"] = [string] $Connection.password
    $processInfo.Environment["MARIADB_PWD"] = [string] $Connection.password

    $process = [Diagnostics.Process]::Start($processInfo)
    try {
        $process.StandardInput.Write($inputSql)
        $process.StandardInput.Close()
        [void] $process.StandardOutput.ReadToEnd()
        $errorText = $process.StandardError.ReadToEnd()
        $process.WaitForExit()
        if ($process.ExitCode -eq 0) {
            return $true
        }
        $safeError = ($errorText -replace "\r?\n", " ").Trim()
        if ($safeError.Length -gt 500) {
            $safeError = $safeError.Substring(0, 500)
        }
        $failures.Add([ordered]@{
            module = $ModuleCode
            key = $Key
            error = $safeError
        })
        return $false
    } finally {
        if (-not $process.HasExited) {
            $process.Kill($true)
        }
        $process.Dispose()
    }
}

function Read-MyBatisStatements {
    param([Parameter(Mandatory = $true)][string] $Path)
    $settings = [System.Xml.XmlReaderSettings]::new()
    $settings.DtdProcessing = [System.Xml.DtdProcessing]::Ignore
    $settings.XmlResolver = $null
    $reader = [System.Xml.XmlReader]::Create($Path, $settings)
    try {
        $document = [System.Xml.XmlDocument]::new()
        $document.XmlResolver = $null
        $document.Load($reader)
        return @(
            $document.SelectNodes("/mapper/insert|/mapper/update|/mapper/select") |
                ForEach-Object {
                    [ordered]@{
                        key = "TransactionSegmentMapper.$($_.GetAttribute('id'))"
                        sql = [regex]::Replace(
                            ([string] $_.InnerText).Trim(),
                            '#\{[A-Za-z][A-Za-z0-9_]*\}',
                            "?")
                    }
                }
        )
    } finally {
        $reader.Dispose()
    }
}

foreach ($moduleCode in $selectedModules) {
    $moduleContract = @($contract.modules | Where-Object {
        [string] $_.module -ceq $moduleCode
    })
    if ($moduleContract.Count -ne 1) {
        throw "Runtime Query contract에서 Module을 찾을 수 없습니다: $moduleCode"
    }
    $moduleContract = $moduleContract[0]
    $profileModuleKey = [string] $moduleContract.profileModuleKey
    $profileProperty = $profile.modules.PSObject.Properties[$profileModuleKey]
    if ($null -eq $profileProperty -or -not [bool] $profileProperty.Value.enabled) {
        throw "DB Profile의 Module이 활성화되어 있지 않습니다: $profileModuleKey"
    }
    $rawModule = $profileProperty.Value
    $normalizedModule = ConvertTo-CpfModuleProfile `
        -Profile $profile `
        -ModuleKey $profileModuleKey `
        -SkipSecretResolution
    if ([string] $normalizedModule.vendor -cne "mariadb") {
        throw (
            "이 Runtime Query 실행 Smoke는 MariaDB Profile만 지원합니다: " +
            "module=$moduleCode vendor=$($normalizedModule.vendor)"
        )
    }
    if (-not (Test-Path -LiteralPath $normalizedModule.clientPath -PathType Leaf)) {
        throw "MariaDB Client가 없습니다: $($normalizedModule.clientPath)"
    }
    $allowDevDefault = ([string] $profile.environment).ToLowerInvariant() -in @(
        "development", "dev", "local"
    ) -and [bool] $profile.policy.allowInlineDevDefaults
    $runtimePassword = Resolve-CpfProfileSecret `
        -SecretSpec $rawModule.runtime.password `
        -DisplayName "$profileModuleKey.runtime.password" `
        -AllowDevDefault $allowDevDefault
    $connection = [pscustomobject]@{
        clientPath = [string] $normalizedModule.clientPath
        host = [string] $normalizedModule.host
        port = [int] $normalizedModule.port
        databaseName = [string] $normalizedModule.databaseName
        username = [string] $rawModule.runtime.username
        password = $runtimePassword
        sslMode = [string] $normalizedModule.sslMode
        sslCaPath = [string] $normalizedModule.sslCaPath
    }
    $preparedCount = 0
    $repositoryCount = 0
    $myBatisCount = 0
    try {
        if (-not (Test-PreparedStatement `
            -Connection $connection `
            -ModuleCode $moduleCode `
            -Key "runtime-account-connection" `
            -SqlText "SELECT 1")) {
            $moduleResults.Add([pscustomobject][ordered]@{
                module = $moduleCode
                profileModuleKey = $profileModuleKey
                database = [string] $normalizedModule.databaseName
                repositoryStatements = @($moduleContract.statements).Count
                preparedRepositoryStatements = 0
                preparedMyBatisStatements = 0
            })
            continue
        }

        $repositoryRoot = Join-Path $Root (
            "cpf-tools\db\vendor\mariadb\runtime\$moduleCode\repository")
        foreach ($statement in @($moduleContract.statements | Sort-Object key)) {
            $key = [string] $statement.key
            $sqlPath = Join-Path $repositoryRoot "$key.sql"
            if (-not (Test-Path -LiteralPath $sqlPath -PathType Leaf)) {
                $failures.Add([ordered]@{
                    module = $moduleCode
                    key = $key
                    error = "Generated MariaDB Runtime Query SQL이 없습니다."
                })
                continue
            }
            $sqlText = [System.IO.File]::ReadAllText(
                $sqlPath,
                [System.Text.Encoding]::UTF8)
            $preparedSql = ConvertTo-PreparedSql `
                -Sql $sqlText `
                -ParameterStyle ([string] $moduleContract.parameterStyle)
            if (Test-PreparedStatement `
                -Connection $connection `
                -ModuleCode $moduleCode `
                -Key $key `
                -SqlText $preparedSql) {
                $preparedCount++
                $repositoryCount++
            }
        }

        if ($moduleCode -ceq "cpf") {
            $mapperPath = Join-Path $Root (
                "cpf-tools\db\vendor\mariadb\runtime\cpf\mybatis\logging\" +
                "TransactionSegmentMapper.xml")
            foreach ($mapperStatement in Read-MyBatisStatements -Path $mapperPath) {
                if (Test-PreparedStatement `
                    -Connection $connection `
                    -ModuleCode $moduleCode `
                    -Key ([string] $mapperStatement.key) `
                    -SqlText ([string] $mapperStatement.sql)) {
                    $preparedCount++
                    $myBatisCount++
                }
            }
        }
    } finally {
        $connection.password = $null
        $runtimePassword = $null
    }
    $moduleResults.Add([pscustomobject][ordered]@{
        module = $moduleCode
        profileModuleKey = $profileModuleKey
        database = [string] $normalizedModule.databaseName
        repositoryStatements = @($moduleContract.statements).Count
        preparedRepositoryStatements = $repositoryCount
        preparedMyBatisStatements = $myBatisCount
        preparedStatements = $preparedCount
    })
}

$expectedRepositoryCount = (
    $contract.modules |
        Where-Object { [string] $_.module -in $selectedModules } |
        ForEach-Object { @($_.statements).Count } |
        Measure-Object -Sum
).Sum
$actualRepositoryCount = (
    $moduleResults |
        Measure-Object -Property preparedRepositoryStatements -Sum
).Sum
if ($null -eq $actualRepositoryCount) {
    $actualRepositoryCount = 0
}
$actualMyBatisCount = (
    $moduleResults |
        Measure-Object -Property preparedMyBatisStatements -Sum
).Sum
if ($null -eq $actualMyBatisCount) {
    $actualMyBatisCount = 0
}
$result = [ordered]@{
    status = if ($failures.Count -eq 0 -and
            [int] $actualRepositoryCount -eq [int] $expectedRepositoryCount) {
        "완료"
    } else {
        "실패"
    }
    vendor = "mariadb"
    profile = [string] $profile.profileName
    verification = "SERVER_SIDE_PREPARE"
    selectedModules = $selectedModules
    contractRepositoryStatements = [int] $expectedRepositoryCount
    preparedRepositoryStatements = [int] $actualRepositoryCount
    preparedMyBatisStatements = [int] $actualMyBatisCount
    modules = @($moduleResults)
    failures = @($failures)
    limitations = @(
        "PREPARE는 문법, Table/Column 해석과 Runtime 계정 접근만 검증하며 DML을 실행하지 않습니다.",
        "PostgreSQL/Oracle 실제 DB Runtime은 별도 환경 검증이 필요합니다.",
        "BZA STAGED Query 9개는 Pack에 존재하지만 Java Consumer 전환 전입니다."
    )
    startedAt = $startedAt.ToString("o")
    finishedAt = [DateTimeOffset]::Now.ToString("o")
    secretPersisted = $false
}
$json = $result | ConvertTo-Json -Depth 10
if (-not [string]::IsNullOrWhiteSpace($EvidencePath)) {
    $evidenceDirectory = Split-Path -Parent $EvidencePath
    if (-not [string]::IsNullOrWhiteSpace($evidenceDirectory)) {
        [System.IO.Directory]::CreateDirectory($evidenceDirectory) | Out-Null
    }
    [System.IO.File]::WriteAllText(
        $EvidencePath,
        $json + "`n",
        [System.Text.UTF8Encoding]::new($false))
}
$json
if ($failures.Count -gt 0 -or
        [int] $actualRepositoryCount -ne [int] $expectedRepositoryCount) {
    throw (
        "MariaDB CPF Platform Runtime Query Pack server-side prepare failed: " +
        "failures=$($failures.Count) " +
        "repository=$actualRepositoryCount/$expectedRepositoryCount"
    )
}
