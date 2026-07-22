param(
    [Parameter(Mandatory = $true)]
    [string] $DomainName,
    [string] $SystemCode = "",
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $DatabaseHost = $(if ($env:CPF_DOMAIN_DB_HOST) { $env:CPF_DOMAIN_DB_HOST } else { "localhost" }),
    [int] $DatabasePort = 0,
    [string] $DatabaseName = "",
    [string] $DatabaseUsername = $env:CPF_DOMAIN_DB_USERNAME,
    [string] $DatabasePassword = $env:CPF_DOMAIN_DB_PASSWORD,
    [string] $ClientPath = "",
    [string] $ResultDir = "",
    [switch] $Apply
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
$domain = $DomainName.Trim().ToLowerInvariant()
if ($domain -notmatch '^[a-z][a-z0-9]{1,29}$') {
    throw "DomainName은 영문자로 시작하는 2~30자리 영문 소문자·숫자여야 합니다."
}

$projectName = "cpf-$domain"
$projectDir = Join-Path $Root $projectName
$manifestPath = Join-Path $projectDir "manifest/domain-manifest.json"
if (-not (Test-Path -LiteralPath $manifestPath -PathType Leaf)) {
    throw "주제영역 manifest가 없습니다: $projectName/manifest/domain-manifest.json"
}
$manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
if (-not [bool]$manifest.databaseEnabled) {
    throw "DB capability를 선택하지 않은 주제영역은 DB 초기화를 수행할 수 없습니다."
}
$manifestCode = ([string]$manifest.systemCode).ToUpperInvariant()
if (-not [string]::IsNullOrWhiteSpace($SystemCode) -and $manifestCode -ne $SystemCode.Trim().ToUpperInvariant()) {
    throw "요청 SystemCode와 manifest의 SystemCode가 다릅니다."
}

$vendor = ([string]$manifest.databaseVendor).ToLowerInvariant()
$vendorDefaults = @{
    mariadb = [ordered]@{ port = 3306; database = "${domain}DB"; clients = @('mariadb', 'mysql') }
    postgresql = [ordered]@{ port = 5432; database = "${domain}db"; clients = @('psql') }
    oracle = [ordered]@{ port = 1521; database = 'FREEPDB1'; clients = @('sqlplus') }
    sqlserver = [ordered]@{ port = 1433; database = "${domain}DB"; clients = @('sqlcmd') }
}
if (-not $vendorDefaults.ContainsKey($vendor)) {
    throw "지원하지 않는 DB Vendor입니다: $vendor"
}
$defaults = $vendorDefaults[$vendor]
if ($DatabasePort -le 0) { $DatabasePort = [int]$defaults.port }
if ([string]::IsNullOrWhiteSpace($DatabaseName)) { $DatabaseName = [string]$defaults.database }
if ([string]::IsNullOrWhiteSpace($DatabaseUsername)) { $DatabaseUsername = "cpf_${domain}_migration" }

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/reports/domain-db-init/$domain"
} elseif (-not [System.IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "domain-db-init-result.json"
$logPath = Join-Path $ResultDir "domain-db-init.sanitized.log"
$sqlRelativePath = "sql/Vxx__${domain}_domain.sql"
$sqlPath = Join-Path $projectDir $sqlRelativePath
if (-not (Test-Path -LiteralPath $sqlPath -PathType Leaf)) {
    throw "주제영역 migration SQL이 없습니다: $projectName/$sqlRelativePath"
}

function Find-DatabaseClient {
    if (-not [string]::IsNullOrWhiteSpace($ClientPath)) {
        if (Test-Path -LiteralPath $ClientPath -PathType Leaf) {
            return (Resolve-Path -LiteralPath $ClientPath).Path
        }
        $configured = Get-Command $ClientPath -ErrorAction SilentlyContinue
        if ($null -ne $configured) { return $configured.Source }
        throw "지정한 DB CLI를 찾을 수 없습니다: $ClientPath"
    }
    foreach ($name in $defaults.clients) {
        $command = Get-Command $name -ErrorAction SilentlyContinue
        if ($null -ne $command) { return $command.Source }
    }
    return $null
}

function Join-Arguments {
    param([string[]] $Values)
    return ($Values | ForEach-Object {
        if ($_ -match '[\s"]') { '"' + ($_.Replace('\', '\\').Replace('"', '\"')) + '"' } else { $_ }
    }) -join ' '
}

function Mask-Text {
    param([string] $Value)
    if ($null -eq $Value) { return "" }
    if (-not [string]::IsNullOrWhiteSpace($DatabasePassword)) {
        return $Value.Replace($DatabasePassword, '****')
    }
    return $Value
}

$result = [ordered]@{
    startedAt = (Get-Date).ToString('o')
    status = if ($Apply) { 'FAILED' } else { 'READY' }
    applied = [bool]$Apply
    domainName = $domain
    systemCode = $manifestCode
    projectName = $projectName
    databaseVendor = $vendor
    databaseHost = $DatabaseHost
    databasePort = $DatabasePort
    databaseName = $DatabaseName
    databaseUsername = $DatabaseUsername
    passwordProvided = -not [string]::IsNullOrWhiteSpace($DatabasePassword)
    sql = [ordered]@{
        path = "$projectName/$sqlRelativePath"
        sha256 = (Get-FileHash -LiteralPath $sqlPath -Algorithm SHA256).Hash.ToLowerInvariant()
    }
    client = $null
    exitCode = $null
    logPath = $logPath
}

try {
    if (-not $Apply) {
        [System.IO.File]::WriteAllText($logPath, "DB 초기화 계획 검증 완료: 실제 SQL은 실행하지 않았습니다.`n", $Utf8NoBom)
        Write-Host "domain DB init plan ready. project=$projectName vendor=$vendor result=$resultPath"
        return
    }
    if ([string]::IsNullOrWhiteSpace($DatabasePassword)) {
        throw "-Apply 실행에는 migration 계정 비밀번호가 필요합니다. 비밀번호는 환경변수에서 전달하는 것을 권장합니다."
    }
    if ($DatabasePassword -match '[\r\n]') {
        throw "DB 비밀번호에는 줄바꿈을 사용할 수 없습니다."
    }
    $client = Find-DatabaseClient
    if ([string]::IsNullOrWhiteSpace($client)) {
        throw "DB Vendor용 CLI를 찾지 못했습니다. vendor=$vendor"
    }
    $result.client = [System.IO.Path]::GetFileName($client)

    $psi = [System.Diagnostics.ProcessStartInfo]::new()
    $psi.FileName = $client
    $psi.UseShellExecute = $false
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    $psi.CreateNoWindow = $true
    $inputText = $null
    switch ($vendor) {
        'mariadb' {
            $psi.RedirectStandardInput = $true
            $psi.EnvironmentVariables['MYSQL_PWD'] = $DatabasePassword
            $psi.EnvironmentVariables['MARIADB_PWD'] = $DatabasePassword
            $psi.Arguments = Join-Arguments @(
                '--protocol=tcp', '-h', $DatabaseHost, '-P', [string]$DatabasePort,
                '-u', $DatabaseUsername, "--database=$DatabaseName",
                '--default-character-set=utf8mb4', '--batch', '--raw'
            )
            $inputText = [System.IO.File]::ReadAllText($sqlPath, [System.Text.Encoding]::UTF8)
        }
        'postgresql' {
            $psi.EnvironmentVariables['PGPASSWORD'] = $DatabasePassword
            $psi.Arguments = Join-Arguments @(
                '-X', '-v', 'ON_ERROR_STOP=1', '-h', $DatabaseHost, '-p', [string]$DatabasePort,
                '-U', $DatabaseUsername, '-d', $DatabaseName, '-f', $sqlPath
            )
        }
        'oracle' {
            if ($DatabaseUsername -match '["/@]' -or $DatabasePassword -match '"') {
                throw "Oracle DB 초기화 계정과 비밀번호에는 큰따옴표, 슬래시, @를 사용할 수 없습니다."
            }
            $psi.RedirectStandardInput = $true
            $psi.Arguments = '-S /nolog'
            $inputText = "WHENEVER SQLERROR EXIT SQL.SQLCODE`nCONNECT $DatabaseUsername/`"$DatabasePassword`"@$DatabaseHost`:$DatabasePort/$DatabaseName`n" +
                    [System.IO.File]::ReadAllText($sqlPath, [System.Text.Encoding]::UTF8) + "`nEXIT SUCCESS`n"
        }
        'sqlserver' {
            $psi.EnvironmentVariables['SQLCMDPASSWORD'] = $DatabasePassword
            $psi.Arguments = Join-Arguments @(
                '-S', "$DatabaseHost,$DatabasePort", '-U', $DatabaseUsername,
                '-d', $DatabaseName, '-i', $sqlPath, '-b', '-C', '-f', '65001'
            )
        }
    }

    $process = [System.Diagnostics.Process]::new()
    $process.StartInfo = $psi
    [void]$process.Start()
    if ($psi.RedirectStandardInput) {
        $process.StandardInput.Write($inputText)
        $process.StandardInput.Close()
    }
    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit()
    $result.exitCode = $process.ExitCode
    $safeLog = "STDOUT`n$(Mask-Text $stdout)`nSTDERR`n$(Mask-Text $stderr)`n"
    [System.IO.File]::WriteAllText($logPath, $safeLog, $Utf8NoBom)
    if ($process.ExitCode -ne 0) {
        throw "DB 초기화 SQL 실행이 실패했습니다. vendor=$vendor, exitCode=$($process.ExitCode), log=$logPath"
    }
    $result.status = 'DONE'
} catch {
    $result.failure = Mask-Text $_.Exception.Message
    throw
} finally {
    $result.finishedAt = (Get-Date).ToString('o')
    [System.IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 20), $Utf8NoBom)
}

Write-Host "domain DB init completed. project=$projectName vendor=$vendor result=$resultPath"
