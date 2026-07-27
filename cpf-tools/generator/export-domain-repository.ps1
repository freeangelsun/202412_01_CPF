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
$cpfStackPropertiesPath = Join-Path $repositoryRoot "gradle/cpf-stack.properties"
if (-not (Test-Path -LiteralPath $cpfStackPropertiesPath -PathType Leaf)) {
    throw "CPF Stack 정본이 없습니다: $cpfStackPropertiesPath"
}
$cpfStackProperties = @{}
foreach ($line in Get-Content -LiteralPath $cpfStackPropertiesPath -Encoding UTF8) {
    $trimmed = $line.Trim()
    if ([string]::IsNullOrWhiteSpace($trimmed) -or $trimmed.StartsWith('#')) { continue }
    $index = $trimmed.IndexOf('=')
    if ($index -le 0) { continue }
    $cpfStackProperties[$trimmed.Substring(0, $index).Trim()] = $trimmed.Substring($index + 1).Trim()
}
$springBootVersion = [string]$cpfStackProperties['springBootVersion']
$dependencyManagementVersion = [string]$cpfStackProperties['springDependencyManagementVersion']
$javaVersion = [string]$cpfStackProperties['javaVersion']
if ([string]::IsNullOrWhiteSpace($springBootVersion) -or
        [string]::IsNullOrWhiteSpace($dependencyManagementVersion) -or
        [string]::IsNullOrWhiteSpace($javaVersion)) {
    throw "CPF Stack 정본 값이 유효하지 않습니다: $cpfStackPropertiesPath"
}
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
    def artifactMode = providers.gradleProperty('cpfArtifactMode')
            .orElse(providers.environmentVariable('CPF_ARTIFACT_MODE'))
            .orElse(providers.gradleProperty('cpfArtifactRepositoryUrl').map { 'REMOTE' })
            .orElse(providers.environmentVariable('CPF_ARTIFACT_REPOSITORY_URL').map { 'REMOTE' })
            .orElse('LOCAL_DEV').get().trim().toUpperCase(Locale.ROOT)
    def remoteRepo = providers.gradleProperty('cpfArtifactRepositoryUrl')
            .orElse(providers.environmentVariable('CPF_ARTIFACT_REPOSITORY_URL')).orNull
    def localRepo = providers.gradleProperty('cpfLocalArtifactRepository')
            .orElse(providers.environmentVariable('CPF_LOCAL_ARTIFACT_REPOSITORY'))
            .orElse(new File(System.getProperty('user.home'), '.cpf/repository').absolutePath).get()
    def offlineRepo = providers.gradleProperty('cpfOfflineArtifactRepository')
            .orElse(providers.environmentVariable('CPF_OFFLINE_ARTIFACT_REPOSITORY')).orNull
    if (!(artifactMode in ['LOCAL_DEV','REMOTE','OFFLINE'])) throw new GradleException("Unsupported CPF artifact mode: `${artifactMode}")
    if (artifactMode == 'REMOTE' && !remoteRepo) throw new GradleException('CPF_ARTIFACT_MODE=REMOTE requires cpfArtifactRepositoryUrl or CPF_ARTIFACT_REPOSITORY_URL.')
    if (artifactMode == 'OFFLINE' && !offlineRepo) throw new GradleException('CPF_ARTIFACT_MODE=OFFLINE requires cpfOfflineArtifactRepository or CPF_OFFLINE_ARTIFACT_REPOSITORY.')
    if (artifactMode == 'LOCAL_DEV' || artifactMode == 'OFFLINE') {
        def fileRepo = artifactMode == 'OFFLINE' ? offlineRepo : localRepo
        def manifest = new File(fileRepo, '_cpf/manifests/$PlatformVersion.json')
        if (!manifest.isFile()) throw new GradleException("CPF artifact manifest is missing or publication is incomplete: `${manifest}")
        def manifestJson = new groovy.json.JsonSlurper().parse(manifest)
        if (manifestJson.platformVersion != '$PlatformVersion' || manifestJson.promotionState != 'PROMOTED') {
            throw new GradleException("CPF artifact manifest is not a promoted $PlatformVersion set: `${manifest}")
        }
    }
    repositories {
        if (artifactMode == 'REMOTE') {
            maven {
                url = uri(remoteRepo)
                content { includeGroupByRegex 'com\\.cpf(\\..*)?' }
                def cpfRepoUser = System.getenv('CPF_ARTIFACT_REPOSITORY_USER')
                if (cpfRepoUser) credentials { username = cpfRepoUser; password = System.getenv('CPF_ARTIFACT_REPOSITORY_PASSWORD') }
            }
        } else if (artifactMode == 'OFFLINE') {
            maven { url = uri(offlineRepo); content { includeGroupByRegex 'com\\.cpf(\\..*)?' } }
        } else {
            maven { url = uri(localRepo); content { includeGroupByRegex 'com\\.cpf(\\..*)?' } }
        }
        gradlePluginPortal { content { excludeGroupByRegex 'com\\.cpf(\\..*)?' } }
        mavenCentral { content { excludeGroupByRegex 'com\\.cpf(\\..*)?' } }
    }
}
rootProject.name = 'cpf-domain-$domainName'
include '$DomainModule'
"@
    Write-Utf8 -Path (Join-Path $stagingRoot "settings.gradle") -Content $settings
    $stackTarget = Join-Path $stagingRoot "gradle/cpf-stack.properties"
    $stackTargetParent = Split-Path -Parent $stackTarget
    if (-not (Test-Path -LiteralPath $stackTargetParent)) { New-Item -ItemType Directory -Force -Path $stackTargetParent | Out-Null }
    Copy-Item -LiteralPath $cpfStackPropertiesPath -Destination $stackTarget -Force

    $rootBuild = @"
plugins { id 'base' }
def cpfStackProperties = new Properties()
file('gradle/cpf-stack.properties').withInputStream { cpfStackProperties.load(it) }
ext.cpfPlatformVersion = '$PlatformVersion'
ext.cpfJavaVersion = cpfStackProperties.getProperty('javaVersion').toInteger()
ext.cpfSpringBootVersion = cpfStackProperties.getProperty('springBootVersion')
allprojects {
    repositories {
        def artifactMode = providers.gradleProperty('cpfArtifactMode')
                .orElse(providers.environmentVariable('CPF_ARTIFACT_MODE'))
                .orElse(providers.gradleProperty('cpfArtifactRepositoryUrl').map { 'REMOTE' })
                .orElse(providers.environmentVariable('CPF_ARTIFACT_REPOSITORY_URL').map { 'REMOTE' })
                .orElse('LOCAL_DEV').get().trim().toUpperCase(Locale.ROOT)
        def remoteRepo = providers.gradleProperty('cpfArtifactRepositoryUrl')
                .orElse(providers.environmentVariable('CPF_ARTIFACT_REPOSITORY_URL')).orNull
        def localRepo = providers.gradleProperty('cpfLocalArtifactRepository')
                .orElse(providers.environmentVariable('CPF_LOCAL_ARTIFACT_REPOSITORY'))
                .orElse(new File(System.getProperty('user.home'), '.cpf/repository').absolutePath).get()
        def offlineRepo = providers.gradleProperty('cpfOfflineArtifactRepository')
                .orElse(providers.environmentVariable('CPF_OFFLINE_ARTIFACT_REPOSITORY')).orNull
        if (artifactMode == 'REMOTE') {
            if (!remoteRepo) throw new GradleException('CPF_ARTIFACT_MODE=REMOTE requires cpfArtifactRepositoryUrl or CPF_ARTIFACT_REPOSITORY_URL.')
            maven {
                url = uri(remoteRepo)
                content { includeGroupByRegex 'com\\.cpf(\\..*)?' }
                def cpfRepoUser = System.getenv('CPF_ARTIFACT_REPOSITORY_USER')
                if (cpfRepoUser) credentials { username = cpfRepoUser; password = System.getenv('CPF_ARTIFACT_REPOSITORY_PASSWORD') }
            }
        } else if (artifactMode == 'OFFLINE') {
            if (!offlineRepo) throw new GradleException('CPF_ARTIFACT_MODE=OFFLINE requires cpfOfflineArtifactRepository or CPF_OFFLINE_ARTIFACT_REPOSITORY.')
            maven { url = uri(offlineRepo); content { includeGroupByRegex 'com\\.cpf(\\..*)?' } }
        } else if (artifactMode == 'LOCAL_DEV') {
            maven { url = uri(localRepo); content { includeGroupByRegex 'com\\.cpf(\\..*)?' } }
        } else {
            throw new GradleException("Unsupported CPF artifact mode: `${artifactMode}")
        }
        mavenCentral { content { excludeGroupByRegex 'com\\.cpf(\\..*)?' } }
    }
}
"@
    Write-Utf8 -Path (Join-Path $stagingRoot "build.gradle") -Content $rootBuild
    Write-Utf8 -Path (Join-Path $stagingRoot "gradle.properties") -Content "cpfJavaVersion=$javaVersion`n"

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
            "`$1 version '$springBootVersion'")
    $buildText = [regex]::Replace(
            $buildText,
            "(?m)^(\s*id\s+'io\.spring\.dependency-management')\s*$",
            "`$1 version '$dependencyManagementVersion'")
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
