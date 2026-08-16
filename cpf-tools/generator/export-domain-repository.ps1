[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^cpf-[a-z][a-z0-9-]{1,49}$')]
    [string] $DomainModule,
    [string] $SourceRoot = '',
    [string] $DefinitionFile = '',
    [ValidatePattern('^$|^[A-Z][A-Z0-9]{2}$')]
    [string] $SystemCode = '',
    [string] $DatabaseVendor = $env:CPF_DOMAIN_DB_VENDOR,
    [ValidatePattern('^$|^[0-9A-Za-z][0-9A-Za-z._+-]{0,63}$')]
    [string] $PlatformVersion = '',
    [string] $OutputRoot = 'build/domain-repositories',
    [ValidateSet('AUTO', 'LOCAL_DEV', 'REMOTE', 'OFFLINE')]
    [string] $ArtifactMode = 'AUTO',
    [string] $LocalArtifactRepository = '',
    [string] $RemoteArtifactRepository = '',
    [string] $OfflineArtifactRepository = '',
    [switch] $SkipBuild
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$frameworkRoot = (Resolve-Path "$PSScriptRoot\..\..").Path
. (Join-Path $frameworkRoot 'cpf-tools/generator/tools/generated-domain-common.ps1')
. (Join-Path $frameworkRoot 'cpf-tools/db/tools/database-profile-common.ps1')

if ([string]::IsNullOrWhiteSpace($DatabaseVendor)) { throw 'DatabaseVendor가 필요합니다. -DatabaseVendor 또는 CPF_DOMAIN_DB_VENDOR를 설정하세요.' }
$DatabaseVendor = Assert-CpfSupportedDatabaseVendor $DatabaseVendor
$source = if ([string]::IsNullOrWhiteSpace($SourceRoot)) {
    Join-Path $frameworkRoot $DomainModule
} elseif ([IO.Path]::IsPathRooted($SourceRoot)) {
    [IO.Path]::GetFullPath($SourceRoot)
} else {
    [IO.Path]::GetFullPath((Join-Path $frameworkRoot $SourceRoot))
}
if (-not (Test-Path -LiteralPath $source -PathType Container)) {
    throw "Generated Domain root가 없습니다: $source"
}
$domainName = $DomainModule.Substring(4).ToLowerInvariant()
if ([string]::IsNullOrWhiteSpace($DefinitionFile)) {
    $DefinitionFile = Join-Path $frameworkRoot "cpf-tools/generator/definitions/$domainName/cpf-domain.yaml"
} elseif (-not [IO.Path]::IsPathRooted($DefinitionFile)) {
    $DefinitionFile = Join-Path $frameworkRoot $DefinitionFile
}
$DefinitionFile = (Resolve-Path -LiteralPath $DefinitionFile).Path
$definition = Get-CpfGeneratedDomainDefinition `
        -Root $frameworkRoot `
        -DomainName $domainName `
        -DefinitionPath $DefinitionFile `
        -IncludeMissing
if ([string]$definition.projectName -cne $DomainModule -or
        [string]$definition.domainName -cne $domainName) {
    throw "Domain root와 canonical definition identity가 다릅니다: root=$DomainModule definition=$($definition.projectName)"
}
if (-not [string]::IsNullOrWhiteSpace($SystemCode) -and
        [string]$definition.systemCode -cne $SystemCode) {
    throw "요청 SystemCode와 canonical definition이 다릅니다: requested=$SystemCode canonical=$($definition.systemCode)"
}
if ([string]$definition.generatedProjectMetadata -cne 'NONE' -or
        @($definition.forbiddenPermanentMetadata).Count -ne 0) {
    throw "Generated Project에 금지된 lifecycle metadata가 있습니다: $(@($definition.forbiddenPermanentMetadata) -join ',')"
}

$canonicalVerify = Invoke-CpfCanonicalCli -Root $frameworkRoot -Arguments @(
    'verify', 'domain', '--file', $DefinitionFile, '--output', $source)
if ([string]$canonicalVerify.status -cne 'PASS') {
    throw "Canonical Generated Domain 검증이 PASS가 아닙니다: $($canonicalVerify | ConvertTo-Json -Compress -Depth 20)"
}

if ([string]::IsNullOrWhiteSpace($PlatformVersion)) {
    $platformPropertiesPath = Join-Path $frameworkRoot 'gradle/cpf-platform.properties'
    $platformVersionLine = Get-Content -LiteralPath $platformPropertiesPath -Encoding UTF8 |
            Where-Object { $_ -match '^\s*platformVersion\s*=' } |
            Select-Object -First 1
    if ($null -eq $platformVersionLine) {
        throw "CPF platformVersion 정본 값이 없습니다: $platformPropertiesPath"
    }
    $PlatformVersion = ($platformVersionLine -split '=', 2)[1].Trim()
}

$effectiveArtifactMode = if ($ArtifactMode -ne 'AUTO') {
    $ArtifactMode
} elseif (-not [string]::IsNullOrWhiteSpace($env:CPF_ARTIFACT_MODE)) {
    $env:CPF_ARTIFACT_MODE.Trim().ToUpperInvariant()
} elseif (-not [string]::IsNullOrWhiteSpace($RemoteArtifactRepository) -or
        -not [string]::IsNullOrWhiteSpace($env:CPF_ARTIFACT_REPOSITORY_URL)) {
    'REMOTE'
} else {
    'LOCAL_DEV'
}
if ($effectiveArtifactMode -notin @('LOCAL_DEV', 'REMOTE', 'OFFLINE')) {
    throw "지원하지 않는 ArtifactMode입니다: $effectiveArtifactMode"
}
if ($effectiveArtifactMode -eq 'REMOTE' -and
        [string]::IsNullOrWhiteSpace($RemoteArtifactRepository) -and
        [string]::IsNullOrWhiteSpace($env:CPF_ARTIFACT_REPOSITORY_URL)) {
    throw 'ArtifactMode=REMOTE에는 -RemoteArtifactRepository 또는 CPF_ARTIFACT_REPOSITORY_URL이 필요합니다.'
}
if ($effectiveArtifactMode -eq 'OFFLINE' -and
        [string]::IsNullOrWhiteSpace($OfflineArtifactRepository) -and
        [string]::IsNullOrWhiteSpace($env:CPF_OFFLINE_ARTIFACT_REPOSITORY)) {
    throw 'ArtifactMode=OFFLINE에는 -OfflineArtifactRepository 또는 CPF_OFFLINE_ARTIFACT_REPOSITORY가 필요합니다.'
}

$outputRootAbsolute = if ([IO.Path]::IsPathRooted($OutputRoot)) {
    [IO.Path]::GetFullPath($OutputRoot)
} else {
    [IO.Path]::GetFullPath((Join-Path $frameworkRoot $OutputRoot))
}
$target = Join-Path $outputRootAbsolute $DomainModule
if (Test-Path -LiteralPath $target) {
    throw "Standalone repository가 이미 있습니다. 사용자 Source를 덮어쓰지 않습니다: $target"
}
$stagingContainer = Join-Path $outputRootAbsolute (".$DomainModule.federation-" + [guid]::NewGuid().ToString('N'))
$stagingRoot = Join-Path $stagingContainer $DomainModule

function Write-Utf8 {
    param([Parameter(Mandatory = $true)][string] $Path, [Parameter(Mandatory = $true)][string] $Content)
    $parent = Split-Path -Parent $Path
    if (-not (Test-Path -LiteralPath $parent -PathType Container)) {
        New-Item -ItemType Directory -Force -Path $parent | Out-Null
    }
    [IO.File]::WriteAllText($Path, $Content, $Utf8NoBom)
}

function Copy-CpfTree {
    param([Parameter(Mandatory = $true)][string] $SourceRoot,
          [Parameter(Mandatory = $true)][string] $TargetRoot)
    foreach ($file in Get-ChildItem -LiteralPath $SourceRoot -Recurse -File) {
        $relative = $file.FullName.Substring($SourceRoot.Length + 1).Replace('\', '/')
        if ($relative -match '^(?:build|\.gradle|logs?)(?:/|$)') { continue }
        if ($relative -in @(
                'cpf-domain.yaml',
                'manifest/domain-manifest.json',
                'manifest/generator-ownership.json')) {
            throw "Generated Project lifecycle metadata 재도입을 거부합니다: $relative"
        }
        $destination = Join-Path $TargetRoot $relative
        $parent = Split-Path -Parent $destination
        if (-not (Test-Path -LiteralPath $parent -PathType Container)) {
            New-Item -ItemType Directory -Force -Path $parent | Out-Null
        }
        Copy-Item -LiteralPath $file.FullName -Destination $destination -Force
    }
}

function Get-GradleRepositoryScript {
    return @'
// Standalone Generated Domain의 CPF Artifact 공급원은 환경/Gradle 속성으로만 선택합니다.
allprojects {
    repositories {
        clear()
        def artifactMode = providers.gradleProperty('cpfArtifactMode')
                .orElse(providers.environmentVariable('CPF_ARTIFACT_MODE'))
                .orElse('LOCAL_DEV').get().trim().toUpperCase(Locale.ROOT)
        def localRepository = providers.gradleProperty('cpfLocalArtifactRepository')
                .orElse(providers.environmentVariable('CPF_LOCAL_ARTIFACT_REPOSITORY'))
                .orElse(new File(System.getProperty('user.home'), '.cpf/repository').absolutePath).get()
        def remoteRepository = providers.gradleProperty('cpfArtifactRepositoryUrl')
                .orElse(providers.environmentVariable('CPF_ARTIFACT_REPOSITORY_URL')).orNull
        def offlineRepository = providers.gradleProperty('cpfOfflineArtifactRepository')
                .orElse(providers.environmentVariable('CPF_OFFLINE_ARTIFACT_REPOSITORY')).orNull
        if (artifactMode == 'LOCAL_DEV') {
            maven { url = uri(localRepository); content { includeGroupByRegex 'com\\.cpf(\\..*)?' } }
        } else if (artifactMode == 'REMOTE') {
            if (!remoteRepository) throw new GradleException('REMOTE CPF Artifact repository가 필요합니다.')
            maven {
                url = uri(remoteRepository)
                content { includeGroupByRegex 'com\\.cpf(\\..*)?' }
                def cpfUser = System.getenv('CPF_ARTIFACT_REPOSITORY_USER')
                if (cpfUser) credentials {
                    username = cpfUser
                    password = System.getenv('CPF_ARTIFACT_REPOSITORY_PASSWORD')
                }
            }
        } else if (artifactMode == 'OFFLINE') {
            if (!offlineRepository) throw new GradleException('OFFLINE CPF Artifact repository가 필요합니다.')
            maven { url = uri(offlineRepository); content { includeGroupByRegex 'com\\.cpf(\\..*)?' } }
        } else {
            throw new GradleException("Unsupported CPF Artifact mode: ${artifactMode}")
        }
        mavenCentral { content { excludeGroupByRegex 'com\\.cpf(\\..*)?' } }
    }
}
'@
}

try {
    New-Item -ItemType Directory -Force -Path $stagingRoot | Out-Null
    Copy-CpfTree -SourceRoot $source -TargetRoot $stagingRoot

    foreach ($wrapper in @('gradlew', 'gradlew.bat')) {
        $wrapperSource = Join-Path $frameworkRoot $wrapper
        if (-not (Test-Path -LiteralPath $wrapperSource -PathType Leaf)) {
            throw "CPF Gradle Wrapper가 없습니다: $wrapperSource"
        }
        Copy-Item -LiteralPath $wrapperSource -Destination (Join-Path $stagingRoot $wrapper) -Force
    }
    Copy-CpfTree -SourceRoot (Join-Path $frameworkRoot 'gradle/wrapper') `
            -TargetRoot (Join-Path $stagingRoot 'gradle/wrapper')

    $gradlePropertiesPath = Join-Path $stagingRoot 'gradle.properties'
    $gradleProperties = Get-Content -LiteralPath $gradlePropertiesPath -Raw -Encoding UTF8
    if ($gradleProperties -notmatch '(?m)^cpfPlatformVersion=') {
        throw "Generated Domain cpfPlatformVersion 속성이 없습니다: $gradlePropertiesPath"
    }
    $gradleProperties = [regex]::Replace(
            $gradleProperties,
            '(?m)^cpfPlatformVersion=.*$',
            "cpfPlatformVersion=$PlatformVersion")
    Write-Utf8 -Path $gradlePropertiesPath -Content $gradleProperties

    $federationGradlePath = Join-Path $stagingRoot 'gradle/cpf-federation-repositories.gradle'
    Write-Utf8 -Path $federationGradlePath -Content (Get-GradleRepositoryScript)
    $rootBuildPath = Join-Path $stagingRoot 'build.gradle'
    $rootBuild = Get-Content -LiteralPath $rootBuildPath -Raw -Encoding UTF8
    $applyLine = "apply from: rootProject.file('gradle/cpf-federation-repositories.gradle')"
    if (-not $rootBuild.Contains($applyLine)) {
        $rootBuild = $rootBuild.TrimEnd() + [Environment]::NewLine + [Environment]::NewLine +
                $applyLine + [Environment]::NewLine
    }
    Write-Utf8 -Path $rootBuildPath -Content $rootBuild

    if ([bool]$definition.databaseEnabled) {
        $templateSource = Join-Path $frameworkRoot "cpf-tools/db/generated/domain-template/$DatabaseVendor"
        if (-not (Test-Path -LiteralPath $templateSource -PathType Container)) {
            throw "선택 Vendor Generated Domain Template가 없습니다: $templateSource"
        }
        $templateTarget = Join-Path $stagingRoot "cpf-db/generated/domain-template/$DatabaseVendor"
        New-Item -ItemType Directory -Force -Path $templateTarget | Out-Null
        Copy-CpfTree -SourceRoot $templateSource -TargetRoot $templateTarget
    }

    $verifyArguments = @(
        '-RepoRoot', $stagingRoot,
        '-FrameworkRoot', $frameworkRoot,
        '-DefinitionFile', $DefinitionFile,
        '-DomainName', $domainName,
        '-DatabaseVendor', $DatabaseVendor,
        '-ArtifactMode', $effectiveArtifactMode)
    & pwsh -NoProfile -File (Join-Path $PSScriptRoot 'verify-domain-federation.ps1') @verifyArguments
    if ($LASTEXITCODE -ne 0) { throw "Standalone federation 검증 실패: exit=$LASTEXITCODE" }

    if (-not $SkipBuild) {
        $gradle = if ($IsWindows) { Join-Path $stagingRoot 'gradlew.bat' } else { Join-Path $stagingRoot 'gradlew' }
        $gradleArguments = @(
            'clean', 'check', 'assemble',
            '--no-daemon', '--max-workers=1', '--console=plain',
            "-PcpfArtifactMode=$effectiveArtifactMode",
            "-PcpfDbVendor=$DatabaseVendor")
        if (-not [string]::IsNullOrWhiteSpace($LocalArtifactRepository)) {
            $gradleArguments += "-PcpfLocalArtifactRepository=$([IO.Path]::GetFullPath($LocalArtifactRepository))"
        }
        if (-not [string]::IsNullOrWhiteSpace($RemoteArtifactRepository)) {
            $gradleArguments += "-PcpfArtifactRepositoryUrl=$RemoteArtifactRepository"
        }
        if (-not [string]::IsNullOrWhiteSpace($OfflineArtifactRepository)) {
            $gradleArguments += "-PcpfOfflineArtifactRepository=$([IO.Path]::GetFullPath($OfflineArtifactRepository))"
        }
        Push-Location $stagingRoot
        try {
            & $gradle @gradleArguments
            if ($LASTEXITCODE -ne 0) {
                throw "Standalone Generated Domain build 실패: exit=$LASTEXITCODE"
            }
        } finally {
            Pop-Location
        }
    }

    New-Item -ItemType Directory -Force -Path $outputRootAbsolute | Out-Null
    Move-Item -LiteralPath $stagingRoot -Destination $target
    [ordered]@{
        status = 'PASS'
        repository = $target
        domainName = $domainName
        systemCode = [string]$definition.systemCode
        definitionSha256 = [string]$definition.definitionSha256
        databaseVendor = $DatabaseVendor
        artifactMode = $effectiveArtifactMode
        generatedProjectMetadata = 'NONE'
        batchCapabilitySelection = 'PROJECT_SETUP'
        buildExecuted = -not [bool]$SkipBuild
    } | ConvertTo-Json -Depth 10
} finally {
    if (Test-Path -LiteralPath $stagingContainer -PathType Container) {
        $resolvedStaging = [IO.Path]::GetFullPath($stagingContainer)
        $allowedPrefix = [IO.Path]::GetFullPath($outputRootAbsolute).TrimEnd('\', '/') +
                [IO.Path]::DirectorySeparatorChar
        if (-not $resolvedStaging.StartsWith($allowedPrefix, [StringComparison]::OrdinalIgnoreCase) -or
                (Split-Path -Leaf $resolvedStaging) -notlike ".$DomainModule.federation-*") {
            throw "Federation staging cleanup 경로가 허용 범위를 벗어났습니다: $resolvedStaging"
        }
        Remove-Item -LiteralPath $resolvedStaging -Recurse -Force
    }
}
