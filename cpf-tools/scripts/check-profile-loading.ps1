param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..\..").Path "build/runtime-smoke")
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"

$profiles = @("local", "dev", "stg", "prod")
$fixedModules = @(
    [ordered]@{ project = "cpf-admin"; config = "adm"; code = "ADM"; generated = $false; productionProfile = $true },
    [ordered]@{ project = "cpf-biz-admin"; config = "bza"; code = "BZA"; generated = $false; productionProfile = $true },
    [ordered]@{ project = "cpf-reference"; config = "ref"; code = "REF"; generated = $false; productionProfile = $true }
)
$batchRuntimes = @(
    [ordered]@{ project = "cpf-batch/control-server"; role = "CONTROL_SERVER"; sharedRuntime = $true },
    [ordered]@{ project = "cpf-batch/scheduler"; role = "SCHEDULER"; sharedRuntime = $true },
    [ordered]@{ project = "cpf-batch/worker"; role = "WORKER"; sharedRuntime = $true },
    [ordered]@{ project = "cpf-batch/center-cut-runner"; role = "CENTER_CUT_RUNNER"; sharedRuntime = $true },
    [ordered]@{ project = "cpf-batch/host-agent"; role = "HOST_AGENT"; sharedRuntime = $false }
)
$failures = New-Object System.Collections.Generic.List[object]
$checks = New-Object System.Collections.Generic.List[object]

function Add-Check {
    param([string] $Name, [string] $Status, [string] $Detail)
    [void]$checks.Add([pscustomobject]@{
        name = $Name
        status = $Status
        detail = $Detail
    })
}

function Add-Failure {
    param([string] $Name, [string] $Detail)
    [void]$failures.Add([pscustomobject]@{
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

function Get-GeneratedProfileModules {
    $result = [System.Collections.Generic.List[object]]::new()
    $candidateDirectories = @(Get-ChildItem -LiteralPath $Root -Directory | Where-Object {
        (Test-Path -LiteralPath (Join-Path $_.FullName "manifest/domain-manifest.json") -PathType Leaf) -or
        (Test-Path -LiteralPath (Join-Path $_.FullName "manifest/generator-ownership.json") -PathType Leaf)
    })

    foreach ($directory in $candidateDirectories) {
        $domainManifestPath = Join-Path $directory.FullName "manifest/domain-manifest.json"
        $ownershipManifestPath = Join-Path $directory.FullName "manifest/generator-ownership.json"
        if (-not (Test-Path -LiteralPath $domainManifestPath -PathType Leaf) -or
                -not (Test-Path -LiteralPath $ownershipManifestPath -PathType Leaf)) {
            Add-Failure "GENERATED_DOMAIN_MANIFEST_PAIR_$($directory.Name.ToUpperInvariant())" `
                    "Generated Domain은 domain-manifest.json과 generator-ownership.json을 모두 가져야 합니다: $($directory.Name)"
            continue
        }

        try {
            $domain = (Read-Text $domainManifestPath) | ConvertFrom-Json -ErrorAction Stop
            $ownership = (Read-Text $ownershipManifestPath) | ConvertFrom-Json -ErrorAction Stop
        } catch {
            Add-Failure "GENERATED_DOMAIN_MANIFEST_JSON_$($directory.Name.ToUpperInvariant())" `
                    "Generated Domain manifest JSON을 읽을 수 없습니다: $($directory.Name) :: $($_.Exception.Message)"
            continue
        }

        $identityErrors = [System.Collections.Generic.List[string]]::new()
        if ([string]$domain.domainType -cne "GENERATED_DOMAIN") {
            $identityErrors.Add("domainType=GENERATED_DOMAIN") | Out-Null
        }
        if ([string]$domain.dependencyModel -cne "root-project" -or
                [string]$ownership.dependencyModel -cne "root-project") {
            $identityErrors.Add("dependencyModel=root-project") | Out-Null
        }
        if ([string]$domain.projectName -cne $directory.Name -or
                [string]$ownership.projectName -cne $directory.Name) {
            $identityErrors.Add("projectName=$($directory.Name)") | Out-Null
        }
        foreach ($propertyName in @("moduleName", "domainName", "systemCode", "packageName")) {
            $domainValue = [string]$domain.$propertyName
            $ownershipValue = [string]$ownership.$propertyName
            if ([string]::IsNullOrWhiteSpace($domainValue) -or $domainValue -cne $ownershipValue) {
                $identityErrors.Add("$propertyName identity match") | Out-Null
            }
        }
        if ([string]$domain.packageName -notmatch '^com\.cpf\.[a-z][a-z0-9]*(?:\.[a-z][a-z0-9]*)*$') {
            $identityErrors.Add("packageName under com.cpf") | Out-Null
        }
        if ($identityErrors.Count -gt 0) {
            Add-Failure "GENERATED_DOMAIN_IDENTITY_$($directory.Name.ToUpperInvariant())" `
                    "Generated Domain manifest identity가 유효하지 않습니다: $($directory.Name) :: $($identityErrors -join ', ')"
            continue
        }

        $productionProfile = $false
        if ($null -ne $domain.capabilities -and $domain.capabilities.PSObject.Properties.Name -contains "productionProfile") {
            $productionProfile = $domain.capabilities.productionProfile -eq $true
        } elseif ($domain.PSObject.Properties.Name -contains "productionProfileEnabled") {
            $productionProfile = $domain.productionProfileEnabled -eq $true
        }

        $result.Add([ordered]@{
            project = $directory.Name
            config = [string]$domain.domainName
            code = ([string]$domain.systemCode).ToUpperInvariant()
            generated = $true
            productionProfile = $productionProfile
        }) | Out-Null
    }
    return @($result.ToArray() | Sort-Object project)
}

$generatedModules = @(Get-GeneratedProfileModules)
$modules = @($fixedModules + $generatedModules)

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
        "application-$moduleConfig.yml",
        "application-$moduleConfig-" + '${spring.profiles.active:local}' + ".yml"
    )
    if (-not $module.generated) {
        $requiredImports += @(
            "application-cmn.yml",
            'application-cmn-${spring.profiles.active:local}.yml'
        )
    }
    foreach ($import in $requiredImports) {
        if ($applicationText -notlike "*$import*") {
            Add-Failure "CONFIG_IMPORT_$moduleUpper" "Missing config import [$import] in $resourceRoot/application.yml"
        }
    }

    Test-File "$resourceRoot/application-$moduleConfig.yml" "MODULE_PROFILE_BASE_$moduleUpper" | Out-Null
    if (-not $module.generated -or $module.productionProfile) {
        foreach ($profile in $profiles) {
            Test-File "$resourceRoot/application-$moduleConfig-$profile.yml" "MODULE_PROFILE_$($moduleUpper)_$($profile.ToUpperInvariant())" | Out-Null
        }
    } else {
        Add-Check "MODULE_PROFILES_$moduleUpper" "DONE" `
                "Generated Domain productionProfile=false; optional local/dev/stg/prod files are not required."
    }

    $moduleFiles = @(Get-ChildItem -LiteralPath (Join-Path $Root $resourceRoot) -File -Filter "application-$moduleConfig*.yml" -ErrorAction SilentlyContinue)
    $joinedText = $applicationText + "`n" + (($moduleFiles | ForEach-Object { Read-Text $_.FullName }) -join "`n")
    $moduleIdPresent = $joinedText -match "\$\{$($moduleUpper)_MODULE_ID:"
    $serverPortRequired = -not $module.generated -or $module.productionProfile
    $serverPortPresent = $joinedText -match "\$\{$($moduleUpper)_SERVER_PORT"
    if (-not $moduleIdPresent -or ($serverPortRequired -and -not $serverPortPresent)) {
        $expectedRuntimeMarkers = if ($serverPortRequired) {
            "$($moduleUpper)_MODULE_ID and $($moduleUpper)_SERVER_PORT"
        } else {
            "$($moduleUpper)_MODULE_ID"
        }
        Add-Failure "MODULE_PREFIX_RUNTIME_$moduleUpper" "Module config must expose $expectedRuntimeMarkers placeholders."
    } else {
        Add-Check "MODULE_PREFIX_RUNTIME_$moduleUpper" "DONE" "$moduleUpper required runtime placeholders found."
    }

    if ($joinedText -match "(?i)(private-key|access-token|refresh-token|client-secret)\s*:\s*[^`r`n\$\{]") {
        Add-Failure "MODULE_SECRET_LITERAL_$moduleUpper" "Module profile has a secret-like literal value."
    }
}

# BAT는 삭제된 단일 cpf-batch/src 실행 모듈이 아니라 역할별 실행 모듈과
# runtime-common의 공유 설정으로 구성됩니다. Generated Domain인 EXS는 고정 profile 대상이 아닙니다.
$batchRuntimeConfig = "cpf-batch/runtime-common/src/main/resources/application-bat-runtime.yml"
if (Test-File $batchRuntimeConfig "BAT_RUNTIME_SHARED_CONFIG") {
    $batchRuntimeText = Read-Text (Join-Path $Root $batchRuntimeConfig)
    foreach ($marker in @('${CPF_DB_VENDOR:mariadb}', '${BAT_DATABASE_URL:', '${BAT_DATABASE_USERNAME:', '${BAT_DATABASE_PASSWORD:')) {
        if ($batchRuntimeText -notlike "*$marker*") {
            Add-Failure "BAT_RUNTIME_SHARED_CONTRACT" "공유 BAT Runtime 설정 marker가 없습니다: $marker"
        }
    }
}
foreach ($runtime in $batchRuntimes) {
    $resourceRoot = "$($runtime.project)/src/main/resources"
    $relativePath = "$resourceRoot/application.yml"
    $checkSuffix = ($runtime.role -replace "_", "")
    if (-not (Test-File $relativePath "APPLICATION_YML_BAT_$checkSuffix")) {
        continue
    }
    $applicationText = Read-Text (Join-Path $Root $relativePath)
    $requiredMarkers = @(
        "application-cpf.yml",
        'application-cpf-${spring.profiles.active:local}.yml',
        "module-id: BAT",
        ("role: {0}" -f $runtime.role)
    )
    if ($runtime.sharedRuntime) {
        $requiredMarkers += @(
            "application-bat-runtime.yml",
            "application-cmn.yml",
            'application-cmn-${spring.profiles.active:local}.yml'
        )
    }
    foreach ($marker in $requiredMarkers) {
        if ($applicationText -notlike "*$marker*") {
            Add-Failure "BAT_RUNTIME_CONFIG_$checkSuffix" "BAT 역할 설정 marker가 없습니다: $relativePath :: $marker"
        }
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
    checkedModules = @($modules | ForEach-Object { $_.project }) +
        @($batchRuntimes | ForEach-Object { $_.project }) +
        @("cpf-gateway")
    generatedDomains = @($generatedModules | ForEach-Object { $_.project })
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
