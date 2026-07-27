param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^cpf-[a-z][a-z0-9-]{1,30}$')]
    [string] $DomainModule,
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[A-Z][A-Z0-9]{2}$')]
    [string] $SystemCode,
    [ValidatePattern('^[0-9A-Za-z][0-9A-Za-z._+-]{0,63}$')]
    [string] $PlatformVersion = "1.0.0-SNAPSHOT",
    [string] $OutputRoot = "build/domain-repositories",
    [switch] $SkipBuild
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$repositoryRoot = (Resolve-Path "$PSScriptRoot\..\..").Path
$source = Join-Path $repositoryRoot $DomainModule
if (-not (Test-Path -LiteralPath $source -PathType Container)) {
    throw "Domain module not found: $source"
}

$domainManifestPath = Join-Path $source "manifest/domain-manifest.json"
if (-not (Test-Path -LiteralPath $domainManifestPath -PathType Leaf)) {
    throw "Generated Domain metadata가 없습니다: $domainManifestPath"
}
$domainMetadata = Get-Content -LiteralPath $domainManifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
$domainName = [string]$domainMetadata.domainName
if ($DomainModule -ne "cpf-$domainName") {
    throw "Domain module과 metadata domainName이 일치하지 않습니다."
}
if ([string]$domainMetadata.systemCode -ne $SystemCode) {
    throw "SystemCode와 metadata가 일치하지 않습니다."
}
if ([string]$domainMetadata.domainType -ne "GENERATED_DOMAIN") {
    throw "Generated Domain만 독립 Repository로 export할 수 있습니다."
}

$sourceInternalImports = @(Get-ChildItem -LiteralPath $source -Recurse -File -Include *.java,*.gradle,*.kts |
        Select-String -Pattern 'com\.cpf\.core\.common\.')
if ($sourceInternalImports.Count -gt 0) {
    $sourceInternalImports | Format-Table Path, LineNumber, Line -AutoSize
    throw "Domain source가 cpf-core internal implementation을 참조합니다."
}

$outputRootAbsolute = if ([IO.Path]::IsPathRooted($OutputRoot)) {
    [IO.Path]::GetFullPath($OutputRoot)
} else {
    [IO.Path]::GetFullPath((Join-Path $repositoryRoot $OutputRoot))
}
$target = Join-Path $outputRootAbsolute "cpf-domain-$domainName"
if (Test-Path -LiteralPath $target) {
    throw "Standalone repository already exists. 사용자 소유 코드를 보호하기 위해 덮어쓰지 않습니다: $target"
}
$stagingRoot = Join-Path $outputRootAbsolute (".cpf-domain-$domainName.staging-" + [guid]::NewGuid().ToString("N"))
$stagingModule = Join-Path $stagingRoot $DomainModule

function Write-Utf8 {
    param([string] $Path, [string] $Content)
    $parent = Split-Path -Parent $Path
    if (-not (Test-Path -LiteralPath $parent)) {
        New-Item -ItemType Directory -Force -Path $parent | Out-Null
    }
    [IO.File]::WriteAllText($Path, $Content, $Utf8NoBom)
}

function Copy-TreeWithoutBuildOutput {
    param([string] $SourceRoot, [string] $TargetRoot)
    foreach ($file in Get-ChildItem -LiteralPath $SourceRoot -Recurse -File) {
        $relative = $file.FullName.Substring($SourceRoot.Length + 1).Replace('\', '/')
        if ($relative -match '^(?:build|\.gradle|logs?)(?:/|$)') {
            continue
        }
        $destination = Join-Path $TargetRoot $relative
        $parent = Split-Path -Parent $destination
        if (-not (Test-Path -LiteralPath $parent)) {
            New-Item -ItemType Directory -Force -Path $parent | Out-Null
        }
        Copy-Item -LiteralPath $file.FullName -Destination $destination -Force
    }
}

try {
    New-Item -ItemType Directory -Force -Path $stagingModule | Out-Null
    Copy-TreeWithoutBuildOutput -SourceRoot $source -TargetRoot $stagingModule

    $settings = @"
pluginManagement {
    repositories {
        def cpfPluginRepo = System.getenv('CPF_ARTIFACT_REPOSITORY_URL')
        if (cpfPluginRepo) {
            maven {
                url = uri(cpfPluginRepo)
                def cpfRepoUser = System.getenv('CPF_ARTIFACT_REPOSITORY_USER')
                if (cpfRepoUser) {
                    credentials {
                        username = cpfRepoUser
                        password = System.getenv('CPF_ARTIFACT_REPOSITORY_PASSWORD')
                    }
                }
            }
        }
        def cpfLocalPluginRepo = System.getenv('CPF_LOCAL_ARTIFACT_REPOSITORY') ?:
                new File(System.getProperty('user.home'), '.cpf/repository').absolutePath
        maven { url = uri(cpfLocalPluginRepo) }
        gradlePluginPortal()
        mavenCentral()
    }
}
rootProject.name = 'cpf-domain-$domainName'
include '$DomainModule'
"@
    Write-Utf8 -Path (Join-Path $stagingRoot "settings.gradle") -Content $settings

    $rootBuild = @"
plugins { id 'base' }
ext.cpfPlatformVersion = '$PlatformVersion'
ext.cpfJavaVersion = 25
allprojects {
    repositories {
        def cpfRepo = System.getenv('CPF_ARTIFACT_REPOSITORY_URL')
        if (cpfRepo) {
            maven {
                url = uri(cpfRepo)
                def cpfRepoUser = System.getenv('CPF_ARTIFACT_REPOSITORY_USER')
                if (cpfRepoUser) {
                    credentials {
                        username = cpfRepoUser
                        password = System.getenv('CPF_ARTIFACT_REPOSITORY_PASSWORD')
                    }
                }
            }
        }
        def cpfLocalRepo = System.getenv('CPF_LOCAL_ARTIFACT_REPOSITORY') ?:
                new File(System.getProperty('user.home'), '.cpf/repository').absolutePath
        maven { url = uri(cpfLocalRepo) }
        mavenCentral()
    }
}
"@
    Write-Utf8 -Path (Join-Path $stagingRoot "build.gradle") -Content $rootBuild

    $buildFile = Join-Path $stagingModule "build.gradle"
    $buildText = Get-Content -LiteralPath $buildFile -Raw -Encoding UTF8
    $publishedDependencies = @"
implementation platform('com.cpf:cpf-bom:$PlatformVersion')
    implementation 'com.cpf.core:cpf-core:$PlatformVersion'
"@
    $buildText = [regex]::Replace(
            $buildText,
            "implementation\s+project\(':cpf-core'\)",
            $publishedDependencies)
    $buildText = [regex]::Replace(
            $buildText,
            "implementation\s+project\(':cpf-common'\)",
            "implementation 'com.cpf.common:cpf-common:$PlatformVersion'")
    $buildText = [regex]::Replace(
            $buildText,
            "implementation\s+project\(':cpf-batch[^']*'\)",
            "implementation 'com.cpf.batch:cpf-batch-contract:$PlatformVersion'")
    $buildText = [regex]::Replace(
            $buildText,
            "(?m)^(\s*id\s+'org\.springframework\.boot')\s*$",
            "`$1 version '3.4.13'")
    $buildText = [regex]::Replace(
            $buildText,
            "(?m)^(\s*id\s+'io\.spring\.dependency-management')\s*$",
            "`$1 version '1.1.7'")
    $buildText = $buildText.Replace(
            '${rootProject.projectDir}/cpf-tools/db/vendor',
            '${rootProject.projectDir}/cpf-db/vendor')
    $buildText = $buildText.Replace(
            "rootProject.file('cpf-tools/db/vendor/mariadb')",
            "rootProject.file('cpf-db/vendor/mariadb')")
    Write-Utf8 -Path $buildFile -Content $buildText

    $databaseEnabled = [bool]$domainMetadata.databaseEnabled
    $databaseVendor = ([string]$domainMetadata.databaseVendor).ToLowerInvariant()
    if ($databaseEnabled) {
        if ($databaseVendor -notin @("mariadb", "mysql", "postgresql", "oracle", "sqlserver")) {
            throw "지원하지 않는 Generated Domain databaseVendor입니다: $databaseVendor"
        }
        $vendorTemplate = Join-Path $repositoryRoot "cpf-tools/db/vendor/$databaseVendor/domain-template"
        if (-not (Test-Path -LiteralPath $vendorTemplate -PathType Container)) {
            throw "Central Domain Template이 없습니다: $vendorTemplate"
        }
        $vendorTarget = Join-Path $stagingRoot "cpf-db/vendor/$databaseVendor/domain-template"
        New-Item -ItemType Directory -Force -Path $vendorTarget | Out-Null
        Copy-TreeWithoutBuildOutput -SourceRoot $vendorTemplate -TargetRoot $vendorTarget

        $templateInventory = @(Get-ChildItem -LiteralPath $vendorTemplate -Recurse -File |
                Sort-Object FullName |
                ForEach-Object {
                    [ordered]@{
                        path = $_.FullName.Substring($vendorTemplate.Length + 1).Replace('\', '/')
                        sha256 = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
                    }
                })
        $templateSnapshot = [ordered]@{
            contractVersion = "1.0"
            vendor = $databaseVendor
            source = "cpf-tools/db/vendor/$databaseVendor/domain-template"
            selectedVendorOnly = $true
            sourceTreeMutation = $false
            files = $templateInventory
        }
        Write-Utf8 -Path (Join-Path $stagingRoot "cpf-db/domain-template-snapshot.json") `
            -Content (($templateSnapshot | ConvertTo-Json -Depth 20) + [Environment]::NewLine)

        $databaseToolTarget = Join-Path $stagingRoot "cpf-db/tools"
        New-Item -ItemType Directory -Force -Path $databaseToolTarget | Out-Null
        foreach ($databaseTool in @(
                "initialize-domain-database.ps1",
                "database-profile-common.ps1")) {
            $databaseToolSource = Join-Path $repositoryRoot "cpf-tools/scripts/$databaseTool"
            if (-not (Test-Path -LiteralPath $databaseToolSource -PathType Leaf)) {
                throw "Generated Domain DB Tool source가 없습니다: $databaseToolSource"
            }
            Copy-Item -LiteralPath $databaseToolSource `
                -Destination (Join-Path $databaseToolTarget $databaseTool) -Force
        }
        $databaseWrapper = @'
param([Parameter(ValueFromRemainingArguments = $true)][object[]] $DatabaseArgs)
$ErrorActionPreference = "Stop"
$repositoryRoot = (Resolve-Path "$PSScriptRoot\..").Path
$target = Join-Path $PSScriptRoot "tools/initialize-domain-database.ps1"
$templateRoot = Join-Path $PSScriptRoot "vendor"
& pwsh -NoProfile -ExecutionPolicy Bypass -File $target `
    -Root $repositoryRoot `
    -TemplateRoot $templateRoot `
    @DatabaseArgs
exit $LASTEXITCODE
'@
        Write-Utf8 -Path (Join-Path $stagingRoot "cpf-db/initialize-domain-database.ps1") `
            -Content $databaseWrapper
    }

    foreach ($wrapper in @("gradlew", "gradlew.bat")) {
        $sourceWrapper = Join-Path $repositoryRoot $wrapper
        if (Test-Path -LiteralPath $sourceWrapper -PathType Leaf) {
            Copy-Item -LiteralPath $sourceWrapper -Destination (Join-Path $stagingRoot $wrapper) -Force
        }
    }
    $wrapperDir = Join-Path $repositoryRoot "gradle/wrapper"
    if (Test-Path -LiteralPath $wrapperDir -PathType Container) {
        Copy-TreeWithoutBuildOutput -SourceRoot $wrapperDir -TargetRoot (Join-Path $stagingRoot "gradle/wrapper")
    }

    $finalForbidden = @(Get-ChildItem -LiteralPath $stagingRoot -Recurse -File -Include *.java,*.gradle,*.kts |
            Select-String -Pattern @(
                'com\.cpf\.core\.common\.',
                'project\s*\(\s*[''"]:cpf-(?:core|common|batch)(?::|[''"])',
                '(?i)(?:^|[/\\])cpf-tools[/\\]db[/\\]vendor'
            ))
    if ($finalForbidden.Count -gt 0) {
        $finalForbidden | Format-Table Path, LineNumber, Line -AutoSize
        throw "Standalone Domain federation boundary violation."
    }

    $repositoryManifest = [ordered]@{
        repositoryName = "cpf-domain-$domainName"
        domainModule = $DomainModule
        domainName = $domainName
        systemCode = $SystemCode
        platformVersion = $PlatformVersion
        databaseVendor = if ($databaseEnabled) { $databaseVendor } else { $null }
        generatedAt = (Get-Date).ToUniversalTime().ToString("o")
        dependencyModel = "published-artifact"
        internalCoreImportAllowed = $false
        rootProjectDependencyAllowed = $false
        centralDomainTemplateSnapshot = if ($databaseEnabled) {
            "cpf-db/domain-template-snapshot.json"
        } else {
            $null
        }
    }
    Write-Utf8 -Path (Join-Path $stagingRoot "cpf-domain-manifest.json") `
        -Content (($repositoryManifest | ConvertTo-Json -Depth 20) + [Environment]::NewLine)

    $ownedFiles = @(Get-ChildItem -LiteralPath $stagingRoot -Recurse -File |
            Sort-Object FullName |
            ForEach-Object {
                [ordered]@{
                    path = $_.FullName.Substring($stagingRoot.Length + 1).Replace('\', '/')
                    sha256 = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant()
                }
            })
    $repositoryOwnership = [ordered]@{
        ownershipVersion = "1.0"
        domainName = $domainName
        systemCode = $SystemCode
        repositoryName = "cpf-domain-$domainName"
        generatedFiles = $ownedFiles
        userCodeProtection = "changed/generated 외 파일이 있으면 공식 remove가 차단됩니다."
    }
    Write-Utf8 -Path (Join-Path $stagingRoot "cpf-domain-ownership.json") `
        -Content (($repositoryOwnership | ConvertTo-Json -Depth 30) + [Environment]::NewLine)

    New-Item -ItemType Directory -Force -Path $outputRootAbsolute | Out-Null
    Move-Item -LiteralPath $stagingRoot -Destination $target
} finally {
    if (Test-Path -LiteralPath $stagingRoot) {
        $resolvedStaging = [IO.Path]::GetFullPath($stagingRoot)
        $resolvedOutputRoot = [IO.Path]::GetFullPath($outputRootAbsolute).TrimEnd('\', '/') +
                [IO.Path]::DirectorySeparatorChar
        if (-not $resolvedStaging.StartsWith($resolvedOutputRoot, [StringComparison]::OrdinalIgnoreCase)) {
            throw "Staging cleanup 경로가 OutputRoot 밖입니다: $resolvedStaging"
        }
        Remove-Item -LiteralPath $resolvedStaging -Recurse -Force
    }
}

if (-not $SkipBuild) {
    Push-Location $target
    try {
        $gradle = if ($IsWindows) { ".\gradlew.bat" } else { "./gradlew" }
        & $gradle clean test --no-daemon --console=plain
        if ($LASTEXITCODE -ne 0) {
            throw "Standalone domain repository build failed. exitCode=$LASTEXITCODE"
        }
    } finally {
        Pop-Location
    }
}
Write-Host "Standalone domain repository: $target"
