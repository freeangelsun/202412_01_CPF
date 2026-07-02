param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $HostName = $env:CPF_DB_HOST,
    [string] $Port = $env:CPF_DB_PORT,
    [string] $RootUsername = $env:CPF_DB_ROOT_USERNAME,
    [string] $RootPassword = $env:CPF_DB_ROOT_PASSWORD,
    [string] $ClientPath = $env:CPF_MARIADB_CLI,
    [string] $ResultDir = "",
    [switch] $RequireRun
)

$ErrorActionPreference = "Stop"

function New-UnicodeText {
    param([int[]] $CodePoints)

    return -join ($CodePoints | ForEach-Object { [char] $_ })
}

$StatusDone = New-UnicodeText @(0xC644, 0xB8CC)
$StatusNotVerified = New-UnicodeText @(0xBBF8, 0xAC80, 0xC99D)
$StatusFailed = New-UnicodeText @(0xC2E4, 0xD328)

if ([string]::IsNullOrWhiteSpace($HostName)) {
    $HostName = "localhost"
}
if ([string]::IsNullOrWhiteSpace($Port)) {
    $Port = "3306"
}
if ([string]::IsNullOrWhiteSpace($RootUsername)) {
    $RootUsername = "root"
}
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/sql-smoke"
}

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "mariadb-full-install-result.json"
$logPath = Join-Path $ResultDir "mariadb-full-install.log"

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = $StatusNotVerified
    mode = "MariaDB CLI"
    host = $HostName
    port = $Port
    username = $RootUsername
    passwordProvided = -not [string]::IsNullOrWhiteSpace($RootPassword)
    client = $null
    sqlFiles = @()
    steps = [ordered]@{}
    checks = [ordered]@{}
    logPath = $logPath
}

function Save-SmokeResult {
    $result.finishedAt = (Get-Date).ToString("o")
    $result | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $resultPath -Encoding UTF8
}

function Add-Log {
    param([string] $Message)

    $line = "[$((Get-Date).ToString("o"))] $Message"
    Add-Content -LiteralPath $logPath -Encoding UTF8 -Value $line
}

function ConvertTo-SafeMessage {
    param([string] $Message)

    if ([string]::IsNullOrWhiteSpace($Message)) {
        return $Message
    }
    if (-not [string]::IsNullOrWhiteSpace($RootPassword)) {
        return $Message.Replace($RootPassword, "****")
    }
    return $Message
}

function Join-ProcessArguments {
    param([string[]] $Arguments)

    return ($Arguments | ForEach-Object {
        if ($_ -match '[\s"]') {
            '"' + ($_.Replace('\', '\\').Replace('"', '\"')) + '"'
        } else {
            $_
        }
    }) -join " "
}

function Find-MariaDbClient {
    if (-not [string]::IsNullOrWhiteSpace($ClientPath)) {
        if (Test-Path -LiteralPath $ClientPath) {
            return (Resolve-Path -LiteralPath $ClientPath).Path
        }
        $command = Get-Command $ClientPath -ErrorAction SilentlyContinue
        if ($null -ne $command) {
            return $command.Source
        }
    }

    foreach ($name in @("mariadb", "mysql")) {
        $command = Get-Command $name -ErrorAction SilentlyContinue
        if ($null -ne $command) {
            return $command.Source
        }
    }

    $installRoots = @("C:\Program Files", "C:\Program Files (x86)")
    foreach ($installRoot in $installRoots) {
        if (-not (Test-Path -LiteralPath $installRoot)) {
            continue
        }
        foreach ($productFilter in @("MariaDB*", "MySQL*")) {
            $productDirs = @(Get-ChildItem -LiteralPath $installRoot -Directory -Filter $productFilter -ErrorAction SilentlyContinue | Sort-Object FullName)
            foreach ($productDir in $productDirs) {
                foreach ($clientName in @("mariadb.exe", "mysql.exe")) {
                    $candidate = Join-Path $productDir.FullName "bin\$clientName"
                    if (Test-Path -LiteralPath $candidate) {
                        return (Resolve-Path -LiteralPath $candidate).Path
                    }
                }
            }
        }
    }

    return $null
}

function Invoke-MariaDbText {
    param(
        [string] $StepName,
        [string] $SqlText
    )

    $psi = [System.Diagnostics.ProcessStartInfo]::new()
    $psi.FileName = $result.client
    $psi.UseShellExecute = $false
    $psi.RedirectStandardInput = $true
    $psi.RedirectStandardOutput = $true
    $psi.RedirectStandardError = $true
    try {
        $psi.StandardInputEncoding = [System.Text.Encoding]::UTF8
    } catch {
        # Windows PowerShell 5.1에는 StandardInputEncoding이 없으므로 SOURCE 파일 실행으로 우회합니다.
    }
    $psi.StandardOutputEncoding = [System.Text.Encoding]::UTF8
    $psi.StandardErrorEncoding = [System.Text.Encoding]::UTF8
    $arguments = @(
        "--protocol=tcp",
        "-h",
        $HostName,
        "-P",
        $Port,
        "-u",
        $RootUsername,
        "--ssl=0",
        "--default-character-set=utf8mb4",
        "--batch",
        "--raw",
        "--skip-column-names"
    )
    if (-not [string]::IsNullOrWhiteSpace($RootPassword)) {
        $arguments += "--password=$RootPassword"
        $psi.EnvironmentVariables["MYSQL_PWD"] = $RootPassword
        $psi.EnvironmentVariables["MARIADB_PWD"] = $RootPassword
    }
    $psi.Arguments = Join-ProcessArguments $arguments

    $process = [System.Diagnostics.Process]::new()
    $process.StartInfo = $psi
    [void] $process.Start()
    $inputError = $null
    try {
        $process.StandardInput.Write($SqlText)
    } catch {
        $inputError = $_.Exception.Message
    } finally {
        try {
            $process.StandardInput.Close()
        } catch {
            if ([string]::IsNullOrWhiteSpace($inputError)) {
                $inputError = $_.Exception.Message
            }
        }
    }
    $stdout = $process.StandardOutput.ReadToEnd()
    $stderr = $process.StandardError.ReadToEnd()
    $process.WaitForExit()

    if ($process.ExitCode -ne 0 -or -not [string]::IsNullOrWhiteSpace($inputError)) {
        throw "MariaDB SQL step failed. step=$StepName exitCode=$($process.ExitCode) inputError=$(ConvertTo-SafeMessage $inputError) error=$(ConvertTo-SafeMessage $stderr)"
    }

    if (-not [string]::IsNullOrWhiteSpace($stderr)) {
        Add-Log "$StepName stderr: $(ConvertTo-SafeMessage $stderr)"
    }
    return $stdout
}

function Invoke-MariaDbFile {
    param(
        [string] $StepName,
        [string] $RelativePath
    )

    $path = Join-Path $Root $RelativePath
    if (-not (Test-Path -LiteralPath $path)) {
        throw "SQL file not found. step=$StepName path=$RelativePath"
    }

    Add-Log "SQL start: $RelativePath"
    $result.sqlFiles += $RelativePath
    $sourcePath = ((Resolve-Path -LiteralPath $path).Path).Replace("\", "/")
    $output = Invoke-MariaDbText -StepName $StepName -SqlText "SOURCE $sourcePath;`n"
    $result.steps[$StepName] = [ordered]@{
        status = $StatusDone
        file = $RelativePath
        outputLines = @($output -split "`r?`n" | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }).Count
    }
    Add-Log "SQL completed: $RelativePath"
    return $output
}

function Invoke-Scalar {
    param(
        [string] $StepName,
        [string] $SqlText
    )

    $output = Invoke-MariaDbText -StepName $StepName -SqlText $SqlText
    $line = @($output -split "`r?`n" | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } | Select-Object -First 1)
    if ($line.Count -eq 0) {
        return $null
    }
    return $line[0].Trim()
}

try {
    Set-Content -LiteralPath $logPath -Encoding UTF8 -Value "CPF MariaDB full install smoke"
    $result.client = Find-MariaDbClient
    if ([string]::IsNullOrWhiteSpace($result.client)) {
        $result.status = $StatusNotVerified
        $result.reason = "mysql or mariadb CLI was not found. Configure PATH or CPF_MARIADB_CLI."
        Save-SmokeResult
        if ($RequireRun) {
            throw $result.reason
        }
        Write-Host "MariaDB full install smoke skipped: CLI not found. result=$resultPath"
        exit 0
    }

    if ([string]::IsNullOrWhiteSpace($RootPassword)) {
        $result.status = $StatusNotVerified
        $result.reason = "CPF_DB_ROOT_PASSWORD or -RootPassword was not provided, so DB connection was not attempted."
        Save-SmokeResult
        if ($RequireRun) {
            throw $result.reason
        }
        Write-Host "MariaDB full install smoke skipped: password not provided. result=$resultPath"
        exit 0
    }

    Invoke-MariaDbFile -StepName "allInstallAndSmoke" -RelativePath "specs/sql/00_all_install_and_smoke.sql" | Out-Null
    Invoke-MariaDbFile -StepName "smokeCheck" -RelativePath "specs/sql/99_smoke_check.sql" | Out-Null
    Invoke-MariaDbFile -StepName "frameworkSeedRepeat" -RelativePath "specs/sql/50_framework_seed_data.sql" | Out-Null

    $result.checks.batCenterCutTableCount = [int] (Invoke-Scalar -StepName "batCenterCutTableCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = 'pfwDB'
  AND table_name IN ('bat_center_cut_job', 'bat_center_cut_parameter', 'bat_center_cut_item', 'bat_center_cut_result');
"@)
    $result.checks.legacyPfwCenterCutTableCount = [int] (Invoke-Scalar -StepName "legacyPfwCenterCutTableCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = 'pfwDB'
  AND table_name IN ('pfw_center_cut_job', 'pfw_center_cut_parameter', 'pfw_center_cut_item', 'pfw_center_cut_result');
"@)
    $result.checks.centerCutSeedCount = [int] (Invoke-Scalar -StepName "centerCutSeedCount" -SqlText @"
SELECT COUNT(*)
FROM pfwDB.bat_center_cut_job
WHERE center_cut_job_id = 'CPF_BAT_CENTER_CUT_JOB';
"@)
    $result.checks.centerCutParameterSeedCount = [int] (Invoke-Scalar -StepName "centerCutParameterSeedCount" -SqlText @"
SELECT COUNT(*)
FROM pfwDB.bat_center_cut_parameter
WHERE center_cut_job_id = 'CPF_BAT_CENTER_CUT_JOB';
"@)
    $result.checks.xyzCenterCutTableCount = [int] (Invoke-Scalar -StepName "xyzCenterCutTableCount" -SqlText @"
SELECT COUNT(*)
FROM information_schema.tables
WHERE table_schema = 'xyzDB'
  AND table_name IN ('xyz_center_cut_sample_target', 'xyz_center_cut_sample_result');
"@)
    $result.checks.xyzCenterCutSeedCount = [int] (Invoke-Scalar -StepName "xyzCenterCutSeedCount" -SqlText @"
SELECT COUNT(*)
FROM xyzDB.xyz_center_cut_sample_target
WHERE center_cut_job_id = 'CPF_XYZ_CENTER_CUT_SAMPLE_JOB';
"@)

    if ($result.checks.batCenterCutTableCount -ne 4) {
        throw "bat_center_cut_* table count mismatch. actual=$($result.checks.batCenterCutTableCount)"
    }
    if ($result.checks.legacyPfwCenterCutTableCount -ne 0) {
        throw "legacy pfw_center_cut_* tables remain. actual=$($result.checks.legacyPfwCenterCutTableCount)"
    }
    if ($result.checks.centerCutSeedCount -lt 1) {
        throw "CPF_BAT_CENTER_CUT_JOB seed is missing."
    }
    if ($result.checks.xyzCenterCutTableCount -ne 2) {
        throw "xyz_center_cut_sample_* table count mismatch. actual=$($result.checks.xyzCenterCutTableCount)"
    }
    if ($result.checks.xyzCenterCutSeedCount -lt 4) {
        throw "CPF_XYZ_CENTER_CUT_SAMPLE_JOB target seed is missing."
    }

    $result.status = $StatusDone
    Save-SmokeResult
    Write-Host "MariaDB full install smoke passed. result=$resultPath"
} catch {
    $result.status = $StatusFailed
    $result.error = ConvertTo-SafeMessage $_.Exception.Message
    Save-SmokeResult
    Write-Error $result.error
    exit 1
}
