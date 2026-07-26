param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^cpf-[a-z][a-z0-9-]{1,30}$')]
    [string] $DomainModule,
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^[A-Z]{3}$')]
    [string] $SystemCode,
    [string] $PlatformVersion = "1.0.0-SNAPSHOT",
    [string] $OutputRoot = "build\domain-repositories",
    [switch] $SkipBuild
)
$ErrorActionPreference = "Stop"
$repo = (Resolve-Path "$PSScriptRoot\..\..").Path
$source = Join-Path $repo $DomainModule
if (-not (Test-Path $source)) { throw "Domain module not found: $source" }

$forbidden = Get-ChildItem $source -Recurse -File -Include *.java,*.gradle,*.kts |
    Select-String -Pattern 'com\.cpf\.core\.common\.|project\s*\(\s*[''"]:cpf-(core|common|batch)'
if ($forbidden) {
    $forbidden | Format-Table Path, LineNumber, Line -AutoSize
    throw "Domain federation boundary violation."
}

$domainName = $DomainModule.Substring(4)
$target = Join-Path (Join-Path $repo $OutputRoot) "cpf-domain-$domainName"
if (Test-Path $target) { Remove-Item $target -Recurse -Force }
New-Item -ItemType Directory -Force -Path $target | Out-Null
Copy-Item $source (Join-Path $target $DomainModule) -Recurse -Force

$settings = @"
pluginManagement {
    repositories {
        def cpfPluginRepo = System.getenv('CPF_ARTIFACT_REPOSITORY_URL')
        if (cpfPluginRepo) {
            maven {
                url = uri(cpfPluginRepo)
                credentials {
                    username = System.getenv('CPF_ARTIFACT_REPOSITORY_USER')
                    password = System.getenv('CPF_ARTIFACT_REPOSITORY_PASSWORD')
                }
            }
        }
        gradlePluginPortal()
        mavenCentral()
    }
}
rootProject.name = 'cpf-domain-$domainName'
include '$DomainModule'
"@
Set-Content -Encoding UTF8 -Path (Join-Path $target "settings.gradle") -Value $settings

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
                credentials {
                    username = System.getenv('CPF_ARTIFACT_REPOSITORY_USER')
                    password = System.getenv('CPF_ARTIFACT_REPOSITORY_PASSWORD')
                }
            }
        }
        mavenCentral()
    }
}
"@
Set-Content -Encoding UTF8 -Path (Join-Path $target "build.gradle") -Value $rootBuild

$buildFile = Join-Path $target "$DomainModule\build.gradle"
$buildText = Get-Content $buildFile -Raw
$buildText = $buildText -replace "implementation\s+project\(':cpf-core'\)", "implementation platform('com.cpf:cpf-bom:$PlatformVersion')`r`n    implementation 'com.cpf.core:cpf-core:$PlatformVersion'"
$buildText = $buildText -replace "implementation\s+project\(':cpf-common'\)", "implementation 'com.cpf.common:cpf-common:$PlatformVersion'"
$buildText = $buildText -replace "implementation\s+project\(':cpf-batch[^']*'\)", "implementation 'com.cpf.batch:cpf-batch-contract:$PlatformVersion'"
$buildText = $buildText -replace "rootProject\.file\('cpf-tools/db/vendor/mariadb'\)", "rootProject.file('cpf-db/mariadb')"
Set-Content -Encoding UTF8 -Path $buildFile -Value $buildText

$vendorTemplate = Join-Path $repo "cpf-tools\db\vendor\mariadb\domain-template"
if (Test-Path $vendorTemplate) {
    $vendorTarget = Join-Path $target "cpf-db\mariadb"
    New-Item -ItemType Directory -Force -Path $vendorTarget | Out-Null
    Copy-Item $vendorTemplate (Join-Path $vendorTarget "domain-template") -Recurse -Force
}
foreach ($wrapper in @("gradlew","gradlew.bat")) {
    $sourceWrapper = Join-Path $repo $wrapper
    if (Test-Path $sourceWrapper) { Copy-Item $sourceWrapper (Join-Path $target $wrapper) -Force }
}
$wrapperDir = Join-Path $repo "gradle\wrapper"
if (Test-Path $wrapperDir) {
    New-Item -ItemType Directory -Force -Path (Join-Path $target "gradle") | Out-Null
    Copy-Item $wrapperDir (Join-Path $target "gradle\wrapper") -Recurse -Force
}

[ordered]@{
    repositoryName = "cpf-domain-$domainName"
    domainModule = $DomainModule
    systemCode = $SystemCode
    platformVersion = $PlatformVersion
    generatedAt = (Get-Date).ToUniversalTime().ToString("o")
    dependencyModel = "published-artifact"
    internalCoreImportAllowed = $false
} | ConvertTo-Json -Depth 10 |
    Set-Content -Encoding UTF8 -Path (Join-Path $target "cpf-domain-manifest.json")

if (-not $SkipBuild) {
    Push-Location $target
    try {
        if ($IsWindows) { & .\gradlew.bat clean test --no-daemon }
        else { & ./gradlew clean test --no-daemon }
        if ($LASTEXITCODE -ne 0) { throw "Standalone domain repository build failed." }
    } finally { Pop-Location }
}
Write-Host "Standalone domain repository: $target"
