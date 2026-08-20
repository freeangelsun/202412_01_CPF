param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..\..\..").Path "build/runtime-smoke")
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
    [ordered]@{ project = "cpf-backoffice/online"; config = "bza"; code = "BZA"; generated = $false; productionProfile = $true },
    [ordered]@{ project = "cpf-education"; config = "edu"; code = "EDU"; generated = $false; productionProfile = $true }
)
$batchRuntimes = @(
    [ordered]@{ project = "cpf-batch/control-plane"; role = "CONTROL_PLANE"; sharedRuntime = $true },
    [ordered]@{ project = "cpf-batch/scheduler"; role = "SCHEDULER"; sharedRuntime = $true },
    [ordered]@{ project = "cpf-batch/worker"; role = "WORKER"; sharedRuntime = $true },
    [ordered]@{ project = "cpf-batch/center-cut"; role = "CENTER_CUT"; sharedRuntime = $true },
    [ordered]@{ project = "cpf-batch/agent"; role = "AGENT"; sharedRuntime = $false }
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

# Public Starter Profile의 물리/조합 정본은 canonical application-starters.yml입니다.
# cpf-core/cpf-common은 더 이상 application-cpf/application-cmn profile resource를 소유하지 않습니다.
$starterCatalogRelative = 'cpf-tools/generator/contracts/cpf-starter-catalog.json'
$starterCatalogPath = Join-Path $Root $starterCatalogRelative
if (Test-File $starterCatalogRelative 'STARTER_PROFILE_CATALOG') {
    try {
        $starterCatalog = (Read-Text $starterCatalogPath) | ConvertFrom-Json -ErrorAction Stop
        $expectedProfiles = [ordered]@{
            'web-api' = 'cpf-starter-web-api'
            'secure-api' = 'cpf-starter-secure-api'
            'bff' = 'cpf-starter-bff'
            'event' = 'cpf-starter-event'
            'batch' = 'cpf-starter-batch'
        }
        $actualProfileNames = @($starterCatalog.profiles.PSObject.Properties.Name | Sort-Object)
        $expectedProfileNames = @($expectedProfiles.Keys | Sort-Object)
        if (($actualProfileNames -join '|') -cne ($expectedProfileNames -join '|')) {
            Add-Failure 'STARTER_PROFILE_SET' "Canonical Profile set mismatch. expected=$($expectedProfileNames -join ','), actual=$($actualProfileNames -join ',')"
        } else {
            Add-Check 'STARTER_PROFILE_SET' 'DONE' ($expectedProfileNames -join ',')
        }
        foreach ($profileId in $expectedProfiles.Keys) {
            $profile = $starterCatalog.profiles.$profileId
            $artifactId = [string]$profile.artifactId
            if ($artifactId -cne [string]$expectedProfiles[$profileId]) {
                Add-Failure "STARTER_PROFILE_ARTIFACT_$($profileId.ToUpperInvariant().Replace('-','_'))" "artifactId mismatch: $artifactId"
                continue
            }
            $module = @($starterCatalog.modules | Where-Object { [string]$_.artifactId -ceq $artifactId })
            if ($module.Count -ne 1) {
                Add-Failure "STARTER_PROFILE_OWNER_$($profileId.ToUpperInvariant().Replace('-','_'))" "Canonical module owner count must be 1: $artifactId"
                continue
            }
            $ownerPath = [string]$module[0].ownerPath
            $buildRelative = "$ownerPath/build.gradle"
            if (-not (Test-File $buildRelative "STARTER_PROFILE_BUILD_$($profileId.ToUpperInvariant().Replace('-','_'))")) { continue }
            # 이번 Gate는 physical/profile identity를 공통 검증하고, Batch IA 변경으로 영향받은
            # batch profile의 Runtime Composition을 Canonical Catalog와 exact 대조합니다.
            if ($profileId -ceq 'batch') {
                $buildText = Read-Text (Join-Path $Root $buildRelative)
                foreach ($runtimeProject in @($profile.runtimeProjects)) {
                    $runtimeProjectText = [string]$runtimeProject
                    if ($buildText -notlike "*project('$runtimeProjectText')*") {
                        Add-Failure 'STARTER_PROFILE_COMPOSITION_BATCH' "Batch Profile build missing runtime project: $runtimeProjectText"
                    }
                }
            }
        }
    } catch {
        Add-Failure 'STARTER_PROFILE_CATALOG_JSON' "Canonical Starter Catalog을 읽을 수 없습니다: $($_.Exception.Message)"
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
    $requiredBaseImport = "application-$moduleConfig.yml"
    if ($applicationText -notlike "*$requiredBaseImport*") {
        Add-Failure "CONFIG_IMPORT_$moduleUpper" "Missing module base config import [$requiredBaseImport] in $resourceRoot/application.yml"
    }
    $profileImportPrefix = "application-$moduleConfig-" + '${spring.profiles.active:'
    if ($applicationText -notlike "*$profileImportPrefix*") {
        Add-Failure "CONFIG_IMPORT_$moduleUpper" "Missing module environment config import [$profileImportPrefix...] in $resourceRoot/application.yml"
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

# BAT는 역할별 실행 모듈과 runtime-support의 공유 정책/데이터소스 구성으로 구성됩니다.
# 제거된 runtime-common 리소스 파일을 다시 요구하지 않고 canonical runtime-support 소유권을 검증합니다.
$batchRuntimeSupportFiles = @(
    'cpf-batch/runtime-support/build.gradle',
    'cpf-batch/runtime-support/src/main/java/com/cpf/batch/runtime/RuntimeCommonConfiguration.java',
    'cpf-batch/runtime-support/src/main/java/com/cpf/batch/runtime/BatDataSourceConfiguration.java'
)
foreach ($relativePath in $batchRuntimeSupportFiles) {
    [void](Test-File $relativePath 'BAT_RUNTIME_SUPPORT_CONTRACT')
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
        "module-id: BAT",
        ("role: {0}" -f $runtime.role)
    )
    foreach ($marker in $requiredMarkers) {
        if ($applicationText -notlike "*$marker*") {
            Add-Failure "BAT_RUNTIME_CONFIG_$checkSuffix" "BAT 역할 설정 marker가 없습니다: $relativePath :: $marker"
        }
    }
}

# Gateway는 CPF가 소유하는 선택 실행 모듈이므로 업무 모듈 profile 파일을 복제하지 않습니다.
# 대신 Gateway 전용 실행·DB 환경변수 계약을 별도로 검증합니다.
$gatewayPath = Join-Path $Root "cpf-gateway/src/main/resources/application.yml"
if (Test-File "cpf-gateway/src/main/resources/application.yml" "APPLICATION_YML_GATEWAY") {
    $gatewayText = Read-Text $gatewayPath
    foreach ($marker in @(
        '${GWY_MODULE_ID:GWY}',
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
        Add-Check "GATEWAY_RUNTIME_CONTRACT" "DONE" "Gateway 실행 환경변수 계약을 확인했습니다."
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
