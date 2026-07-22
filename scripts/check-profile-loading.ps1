param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "build/runtime-smoke")
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"

$profiles = @("local", "dev", "stg", "prod")
$modules = @(
    [ordered]@{ project = "cpf-member"; config = "mbr"; code = "MBR" },
    [ordered]@{ project = "cpf-admin"; config = "adm"; code = "ADM" },
    [ordered]@{ project = "cpf-batch"; config = "bat"; code = "BAT" },
    [ordered]@{ project = "cpf-biz-admin"; config = "bza"; code = "BZA" },
    [ordered]@{ project = "cpf-reference"; config = "ref"; code = "REF" },
    [ordered]@{ project = "cpf-account"; config = "acc"; code = "ACC" },
    [ordered]@{ project = "cpf-external"; config = "external"; code = "EXS" }
)
$failures = New-Object System.Collections.Generic.List[object]
$checks = New-Object System.Collections.Generic.List[object]

function Add-Check {
    param([string] $Name, [string] $Status, [string] $Detail)
    $checks.Add([pscustomobject]@{
        name = $Name
        status = $Status
        detail = $Detail
    })
}

function Add-Failure {
    param([string] $Name, [string] $Detail)
    $failures.Add([pscustomobject]@{
        name = $Name
        detail = $Detail
    })
    Add-Check $Name "FAILED" $Detail
}

function Read-Text {
    param([string] $Path)
    return [System.IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8)
}

function Test-File {
    param([string] $RelativePath, [string] $Name)
    $path = Join-Path $Root $RelativePath
    if (-not (Test-Path -LiteralPath $path)) {
        Add-Failure $Name "Missing file: $RelativePath"
        return $false
    }
    Add-Check $Name "DONE" $RelativePath
    return $true
}

foreach ($profile in $profiles) {
    Test-File "cpf-core/src/main/resources/application-cpf-$profile.yml" "CPF_PROFILE_$($profile.ToUpperInvariant())" | Out-Null
    Test-File "cpf-common/src/main/resources/application-cmn-$profile.yml" "CMN_PROFILE_$($profile.ToUpperInvariant())" | Out-Null
}
Test-File "cpf-core/src/main/resources/application-cpf.yml" "CPF_PROFILE_BASE" | Out-Null
Test-File "cpf-common/src/main/resources/application-cmn.yml" "CMN_PROFILE_BASE" | Out-Null

$commonFiles = @(
    "cpf-core/src/main/resources/application-cpf.yml",
    "cpf-common/src/main/resources/application-cmn.yml"
) + ($profiles | ForEach-Object {
    "cpf-core/src/main/resources/application-cpf-$_.yml"
}) + ($profiles | ForEach-Object {
    "cpf-common/src/main/resources/application-cmn-$_.yml"
})

foreach ($relativePath in $commonFiles) {
    $path = Join-Path $Root $relativePath
    if (-not (Test-Path -LiteralPath $path)) {
        continue
    }
    $text = Read-Text $path
    if ($text -match "spring:\s*\r?\n\s*application:" -or $text -match "server:\s*\r?\n\s*port:" -or $text -match "profiles:\s*\r?\n\s*active:") {
        Add-Failure "COMMON_CONFIG_DOES_NOT_OWN_RUNTIME_$relativePath" "CPF/CMN config must not force application name, server port, or active profile."
    }
}

foreach ($module in $modules) {
    $moduleUpper = $module.code
    $moduleConfig = $module.config
    $resourceRoot = "$($module.project)/src/main/resources"
    $applicationPath = Join-Path $Root "$resourceRoot/application.yml"
    if (-not (Test-File "$resourceRoot/application.yml" "APPLICATION_YML_$moduleUpper")) {
        continue
    }
    $applicationText = Read-Text $applicationPath
    $requiredImports = @(
        "application-cpf.yml",
        'application-cpf-${spring.profiles.active:local}.yml',
        "application-cmn.yml",
        'application-cmn-${spring.profiles.active:local}.yml',
        "application-$moduleConfig.yml",
        "application-$moduleConfig-" + '${spring.profiles.active:local}' + ".yml"
    )
    foreach ($import in $requiredImports) {
        if ($applicationText -notlike "*$import*") {
            Add-Failure "CONFIG_IMPORT_$moduleUpper" "Missing config import [$import] in $resourceRoot/application.yml"
        }
    }

    Test-File "$resourceRoot/application-$moduleConfig.yml" "MODULE_PROFILE_BASE_$moduleUpper" | Out-Null
    foreach ($profile in $profiles) {
        Test-File "$resourceRoot/application-$moduleConfig-$profile.yml" "MODULE_PROFILE_$($moduleUpper)_$($profile.ToUpperInvariant())" | Out-Null
    }

    $moduleFiles = @(Get-ChildItem -LiteralPath (Join-Path $Root $resourceRoot) -File -Filter "application-$moduleConfig*.yml" -ErrorAction SilentlyContinue)
    $joinedText = ($moduleFiles | ForEach-Object { Read-Text $_.FullName }) -join "`n"
    if ($joinedText -notmatch "\$\{$($moduleUpper)_MODULE_ID:" -or $joinedText -notmatch "\$\{$($moduleUpper)_SERVER_PORT") {
        Add-Failure "MODULE_PREFIX_RUNTIME_$moduleUpper" "Module profile must expose $($moduleUpper)_MODULE_ID and $($moduleUpper)_SERVER_PORT placeholders."
    } else {
        Add-Check "MODULE_PREFIX_RUNTIME_$moduleUpper" "DONE" "$moduleUpper runtime placeholders found."
    }

    if ($joinedText -match "(?i)(private-key|access-token|refresh-token|client-secret)\s*:\s*[^`r`n\$\{]") {
        Add-Failure "MODULE_SECRET_LITERAL_$moduleUpper" "Module profile has a secret-like literal value."
    }
}

# Gateway는 CPF가 소유하는 선택 실행 모듈이므로 업무 모듈 profile 파일을 복제하지 않습니다.
# 대신 CPF profile import와 Gateway 전용 실행·DB 환경변수 계약을 별도로 검증합니다.
$gatewayPath = Join-Path $Root "cpf-gateway/src/main/resources/application.yml"
if (Test-File "cpf-gateway/src/main/resources/application.yml" "APPLICATION_YML_GATEWAY") {
    $gatewayText = Read-Text $gatewayPath
    foreach ($marker in @(
        "application-cpf.yml",
        'application-cpf-${SPRING_PROFILES_ACTIVE:local}.yml',
        '${GWY_MODULE_ID:GWY}',
        '${GWY_INSTANCE_ID:',
        '${GWY_WAS_ID:',
        '${GWY_SERVER_PORT:8070}',
        '${GWY_DATASOURCE_URL:',
        '${GWY_DATASOURCE_USERNAME:',
        '${GWY_DATASOURCE_PASSWORD:'
    )) {
        if ($gatewayText -notlike "*$marker*") {
            Add-Failure "GATEWAY_RUNTIME_CONTRACT" "Gateway 설정 marker가 없습니다: $marker"
        }
    }
    if (@($failures | Where-Object { $_.name -eq "GATEWAY_RUNTIME_CONTRACT" }).Count -eq 0) {
        Add-Check "GATEWAY_RUNTIME_CONTRACT" "DONE" "CPF profile import와 Gateway 실행 환경변수 계약을 확인했습니다."
    }
}

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "profile-loading-result.sanitized.json"
$result = [pscustomobject]@{
    generatedAt = (Get-Date).ToString("o")
    status = $(if ($failures.Count -eq 0) { "DONE" } else { "FAILED" })
    failureCount = $failures.Count
    checkedModules = @($modules | ForEach-Object { $_.project })
    checkedProfiles = $profiles
    failures = @($failures.ToArray())
    checks = @($checks.ToArray())
}
$json = $result | ConvertTo-Json -Depth 8
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
[System.IO.File]::WriteAllText($resultPath, $json, $utf8NoBom)

Write-Host "Profile loading check status=$($result.status) failures=$($failures.Count) evidence=$resultPath"
if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Host "FAIL [$($_.name)] $($_.detail)" }
    exit 1
}
