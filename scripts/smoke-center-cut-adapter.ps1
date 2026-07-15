param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $DbUrl = $env:CPF_XYZ_CENTER_CUT_DB_URL,
    [string] $DbUsername = $env:CPF_XYZ_CENTER_CUT_DB_USERNAME,
    [string] $DbPassword = $env:CPF_XYZ_CENTER_CUT_DB_PASSWORD,
    [string] $DbDriver = $env:CPF_XYZ_CENTER_CUT_DB_DRIVER,
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
$ScopeName = New-UnicodeText @(0x0058, 0x0059, 0x005A, 0x0020, 0xC5C5, 0xBB34, 0x0020, 0x0044, 0x0042, 0x0020, 0xAE30, 0xBC18, 0x0020, 0x0063, 0x0065, 0x006E, 0x0074, 0x0065, 0x0072, 0x002D, 0x0063, 0x0075, 0x0074, 0x0020, 0x0061, 0x0064, 0x0061, 0x0070, 0x0074, 0x0065, 0x0072)
$ProcessArgumentPasswordPolicy = New-UnicodeText @(0xD658, 0xACBD, 0xBCC0, 0xC218, 0x0020, 0xC8FC, 0xC785, 0xB9CC, 0x0020, 0xD5C8, 0xC6A9)
$MissingPasswordReason = New-UnicodeText @(0x0043, 0x0050, 0x0046, 0x005F, 0x0058, 0x0059, 0x005A, 0x005F, 0x0043, 0x0045, 0x004E, 0x0054, 0x0045, 0x0052, 0x005F, 0x0043, 0x0055, 0x0054, 0x005F, 0x0044, 0x0042, 0x005F, 0x0050, 0x0041, 0x0053, 0x0053, 0x0057, 0x004F, 0x0052, 0x0044, 0x0020, 0xB610, 0xB294, 0x0020, 0x0043, 0x0050, 0x0046, 0x005F, 0x0044, 0x0042, 0x005F, 0x0052, 0x004F, 0x004F, 0x0054, 0x005F, 0x0050, 0x0041, 0x0053, 0x0053, 0x0057, 0x004F, 0x0052, 0x0044, 0xAC00, 0x0020, 0xC5C6, 0xC5B4, 0x0020, 0x0044, 0x0042, 0x0020, 0x0061, 0x0064, 0x0061, 0x0070, 0x0074, 0x0065, 0x0072, 0x0020, 0x0073, 0x006D, 0x006F, 0x006B, 0x0065, 0xB97C, 0x0020, 0xC2E4, 0xD589, 0xD558, 0xC9C0, 0x0020, 0xC54A, 0xC558, 0xC2B5, 0xB2C8, 0xB2E4, 0x002E)

if ([string]::IsNullOrWhiteSpace($DbUrl)) {
    $DbUrl = "jdbc:mariadb://localhost:3306/xyzDB?createDatabaseIfNotExist=true"
}
if ([string]::IsNullOrWhiteSpace($DbUsername)) {
    if (-not [string]::IsNullOrWhiteSpace($env:CPF_DB_ROOT_USERNAME)) {
        $DbUsername = $env:CPF_DB_ROOT_USERNAME
    } else {
        $DbUsername = "root"
    }
}
if ([string]::IsNullOrWhiteSpace($DbPassword) -and -not [string]::IsNullOrWhiteSpace($env:CPF_DB_ROOT_PASSWORD)) {
    $DbPassword = $env:CPF_DB_ROOT_PASSWORD
}
if ([string]::IsNullOrWhiteSpace($DbDriver)) {
    $DbDriver = "org.mariadb.jdbc.Driver"
}
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/runtime-smoke"
}

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "center-cut-adapter-result.json"
$logPath = Join-Path $ResultDir "center-cut-adapter.log"

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = $StatusNotVerified
    scope = $ScopeName
    dbUrl = $DbUrl
    dbUsername = $DbUsername
    dbPasswordProvided = -not [string]::IsNullOrWhiteSpace($DbPassword)
    resultPath = $resultPath
    logPath = $logPath
    checks = [ordered]@{
        processArgumentPassword = $ProcessArgumentPasswordPolicy
        targetProvider = "XyzCenterCutTargetRepository"
        handler = "XyzCenterCutHandler"
        service = "XyzCenterCutEducationService"
        fixture = "xyz_center_cut_fixture.sql"
    }
}

function Save-SmokeResult {
    $result.finishedAt = (Get-Date).ToString("o")
    $result | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath $resultPath -Encoding UTF8
}

function ConvertTo-SafeMessage {
    param([string] $Message)

    if ([string]::IsNullOrWhiteSpace($Message)) {
        return $Message
    }
    if (-not [string]::IsNullOrWhiteSpace($DbPassword)) {
        return $Message.Replace($DbPassword, "****")
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

function Find-GradleCommand {
    $wrapper = Join-Path $Root "gradlew.bat"
    $userProfile = $env:USERPROFILE
    if (-not [string]::IsNullOrWhiteSpace($userProfile)) {
        $distributionRoot = Join-Path $userProfile ".gradle\wrapper\dists\gradle-8.11.1-bin"
        if (Test-Path -LiteralPath $distributionRoot) {
            $candidate = Get-ChildItem -LiteralPath $distributionRoot -Recurse -Filter "gradle.bat" -ErrorAction SilentlyContinue |
                    Where-Object { $_.FullName -like "*\gradle-8.11.1\bin\gradle.bat" } |
                    Select-Object -First 1
            if ($null -ne $candidate) {
                return $candidate.FullName
            }
        }
    }
    return $wrapper
}

try {
    if ([string]::IsNullOrWhiteSpace($DbPassword)) {
        $result.reason = $MissingPasswordReason
        Save-SmokeResult
        if ($RequireRun) {
            throw $result.reason
        }
        Write-Host "Center-cut adapter smoke skipped: password not provided. result=$resultPath"
        exit 0
    }

    $gradle = Find-GradleCommand
    if (-not (Test-Path -LiteralPath $gradle)) {
        throw "Gradle 실행 파일을 찾을 수 없습니다."
    }
    $result.gradleCommand = $gradle

    $arguments = @(
        ":xyz:test",
        "--offline",
        "--no-daemon",
        "--console=plain",
        "--tests",
        "cpf.xyz.edu.centercut.XyzCenterCutAdapterTest",
        "--rerun-tasks"
    )

    $argumentText = Join-ProcessArguments $arguments
    if ($argumentText.Contains($DbPassword)) {
        throw "Gradle process arguments contain a raw DB password."
    }

    $previousEnv = @{
        CPF_XYZ_CENTER_CUT_DB_TEST = $env:CPF_XYZ_CENTER_CUT_DB_TEST
        CPF_XYZ_CENTER_CUT_DB_URL = $env:CPF_XYZ_CENTER_CUT_DB_URL
        CPF_XYZ_CENTER_CUT_DB_USERNAME = $env:CPF_XYZ_CENTER_CUT_DB_USERNAME
        CPF_XYZ_CENTER_CUT_DB_PASSWORD = $env:CPF_XYZ_CENTER_CUT_DB_PASSWORD
        CPF_XYZ_CENTER_CUT_DB_DRIVER = $env:CPF_XYZ_CENTER_CUT_DB_DRIVER
    }

    try {
        $env:CPF_XYZ_CENTER_CUT_DB_TEST = "true"
        $env:CPF_XYZ_CENTER_CUT_DB_URL = $DbUrl
        $env:CPF_XYZ_CENTER_CUT_DB_USERNAME = $DbUsername
        $env:CPF_XYZ_CENTER_CUT_DB_PASSWORD = $DbPassword
        $env:CPF_XYZ_CENTER_CUT_DB_DRIVER = $DbDriver

        Push-Location $Root
        try {
            $output = & $gradle @arguments 2>&1
            $exitCode = $LASTEXITCODE
        } finally {
            Pop-Location
        }
    } finally {
        foreach ($entry in $previousEnv.GetEnumerator()) {
            Set-Item -Path "Env:$($entry.Key)" -Value $entry.Value -ErrorAction SilentlyContinue
            if ($null -eq $entry.Value) {
                Remove-Item -Path "Env:$($entry.Key)" -ErrorAction SilentlyContinue
            }
        }
    }

    $safeOutput = ConvertTo-SafeMessage ($output -join [Environment]::NewLine)
    Set-Content -LiteralPath $logPath -Encoding UTF8 -Value $safeOutput
    $result.exitCode = $exitCode
    $result.logContainsRawPassword = $safeOutput.Contains($DbPassword)

    if ($exitCode -eq 0 -and -not $result.logContainsRawPassword) {
        $result.status = $StatusDone
        $result.reason = "XYZ center-cut DB adapter test passed and raw DB password was not found in the smoke log."
    } else {
        $result.status = $StatusFailed
        $result.reason = "XYZ center-cut DB adapter test failed or smoke log contains raw DB password."
    }

    Save-SmokeResult
    Write-Host "Center-cut adapter smoke finished. status=$($result.status) result=$resultPath"
    if ($RequireRun -and $result.status -ne $StatusDone) {
        throw $result.reason
    }
} catch {
    $result.status = $StatusFailed
    $result.error = ConvertTo-SafeMessage $_.Exception.Message
    Save-SmokeResult
    Write-Error $result.error
    exit 1
}
