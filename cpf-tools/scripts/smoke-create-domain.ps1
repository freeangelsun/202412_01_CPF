param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $ResultDir = "",
    [string] $DomainName = "lending",
    [string] $SystemCode = "LND",
    [string] $ModuleName = "Lending",
    [string] $PackageName = "",
    [string] $SchemaName = "",
    [string] $TablePrefix = "",
    [string] $DatabaseVendor = "mariadb"
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
. (Join-Path $Root "cpf-tools/scripts/database-profile-common.ps1")
$DatabaseVendor = Assert-CpfSupportedDatabaseVendor $DatabaseVendor
$cpfStackPropertiesPath = Join-Path $Root "gradle/cpf-stack.properties"
if (-not (Test-Path -LiteralPath $cpfStackPropertiesPath -PathType Leaf)) {
    throw "CPF Stack 정본이 없습니다: $cpfStackPropertiesPath"
}
$cpfStackProperties = @{}
foreach ($line in Get-Content -LiteralPath $cpfStackPropertiesPath -Encoding UTF8) {
    $trimmed = $line.Trim()
    if ([string]::IsNullOrWhiteSpace($trimmed) -or $trimmed.StartsWith("#")) { continue }
    $separator = $trimmed.IndexOf("=")
    if ($separator -le 0) { continue }
    $cpfStackProperties[$trimmed.Substring(0, $separator).Trim()] =
            $trimmed.Substring($separator + 1).Trim()
}
$cpfJavaVersion = [int]$cpfStackProperties["javaVersion"]
$springBootVersion = [string]$cpfStackProperties["springBootVersion"]
$dependencyManagementVersion = [string]$cpfStackProperties["springDependencyManagementVersion"]
if ($cpfJavaVersion -le 0 -or
        [string]::IsNullOrWhiteSpace($springBootVersion) -or
        [string]::IsNullOrWhiteSpace($dependencyManagementVersion)) {
    throw "CPF Stack 정본의 Java/Spring plugin version이 유효하지 않습니다."
}

function New-UnicodeText {
    param([int[]] $CodePoints)
    return -join ($CodePoints | ForEach-Object { [char] $_ })
}

$StatusDone = New-UnicodeText @(0xC644, 0xB8CC)
$StatusFailed = New-UnicodeText @(0xC2E4, 0xD328)

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/runtime-smoke"
} elseif (-not [System.IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "create-domain-result.sanitized.json"
$projectName = "cpf-$DomainName"
$previewDir = Join-Path $Root "build/domain-generator/$projectName"
$verificationDir = Join-Path $Root "build/domain-generator-verification/$projectName"
$runtimeScriptDir = Join-Path $Root "build/domain-generator-runtime"
$runtimeScript = Join-Path $runtimeScriptDir "create-domain.ps1"
$compileRawLogPath = Join-Path $Root "build/runtime-smoke/create-domain-compile.raw.log"
$moduleClassName = $ModuleName
$PackageName = if ([string]::IsNullOrWhiteSpace($PackageName)) { "com.cpf.$DomainName" } else { $PackageName }
$TablePrefix = if ([string]::IsNullOrWhiteSpace($TablePrefix)) { $SystemCode.ToLowerInvariant() } else { $TablePrefix }
$SchemaName = if ([string]::IsNullOrWhiteSpace($SchemaName)) { "${TablePrefix}DB" } else { $SchemaName }
$featureClassPrefix = $ModuleName
$basePath = $PackageName.Replace('.', '/')
$featurePath = "$basePath/sampleitem"

function Save-Result {
    param([object] $Result)
    [System.IO.File]::WriteAllText($resultPath, ($Result | ConvertTo-Json -Depth 30), $Utf8NoBom)
}

function Invoke-CreateDomain {
    param(
        [switch] $DryRun,
        [switch] $GeneratePatch
    )

    $sourceScript = Join-Path $Root "cpf-tools/generator/create-domain.ps1"
    New-Item -ItemType Directory -Force -Path $runtimeScriptDir | Out-Null
    $sourceText = [System.IO.File]::ReadAllText($sourceScript, [System.Text.Encoding]::UTF8)
    [System.IO.File]::WriteAllText($runtimeScript, $sourceText, [System.Text.UTF8Encoding]::new($true))

    $arguments = @(
        "-NoProfile",
        "-File", $runtimeScript,
        "-Root", $Root,
        "-DomainName", $DomainName,
        "-SystemCode", $SystemCode,
        "-ModuleName", $ModuleName,
        "-PackageName", $PackageName,
        "-SchemaName", $SchemaName,
        "-TablePrefix", $TablePrefix,
        "-DatabaseVendor", $DatabaseVendor,
        "-Port", "8188",
        "-Online", "Y",
        "-Batch", "N",
        "-BzaMenu", "N",
        "-ProductionProfile", "N"
    )
    if ($DryRun) {
        $arguments += "-DryRun"
    }
    if ($GeneratePatch) {
        $arguments += "-GeneratePatch"
    }

    $pwshCommand = Get-Command pwsh -ErrorAction Stop
    $output = & $pwshCommand.Source @arguments
    if ($LASTEXITCODE -ne 0) {
        throw "create-domain script failed. exitCode=$LASTEXITCODE"
    }
    return ([string]::Join("`n", @($output)) | ConvertFrom-Json)
}

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    domainName = $DomainName
    systemCode = $SystemCode
    projectName = $projectName
    dryRun = [ordered]@{}
    generate = [ordered]@{}
    requiredFiles = @()
    forbiddenFiles = @()
    compile = [ordered]@{}
    cleanup = [ordered]@{}
}

try {
    $result.dryRun = Invoke-CreateDomain -DryRun

    if (Test-Path -LiteralPath $previewDir) {
        Remove-Item -LiteralPath $previewDir -Recurse -Force
    }

    $result.generate = Invoke-CreateDomain

    $required = @(
        "build.gradle",
        "README.md",
        "manifest/domain-manifest.json",
        "manifest/ownership.json",
        "manifest/generator-ownership.json",
        "manifest/standard-execution-catalog.json",
        "deploy/database/database-profile.json",
        "deploy/runtime/runtime-agent.json",
        "src/main/resources/application.yml",
        "src/main/resources/application-${DomainName}.yml",
        "src/main/resources/application-runtime-agent.yml",
        "src/main/java/$basePath/${moduleClassName}Application.java",
        "src/main/java/$basePath/common/base/${moduleClassName}BaseController.java",
        "src/main/java/$basePath/common/base/${moduleClassName}BaseService.java",
        "src/main/java/$basePath/common/contract/${moduleClassName}ApplicationFacade.java",
        "src/main/java/$basePath/common/contract/${moduleClassName}RepositoryPort.java",
        "src/main/java/$basePath/common/contract/${moduleClassName}Request.java",
        "src/main/java/$basePath/common/contract/${moduleClassName}Response.java",
        "src/main/java/$basePath/config/${ModuleName}DataSourceConfig.java",
        "src/main/java/$basePath/config/${ModuleName}MyBatisConfig.java",
        "src/test/java/$basePath/config/${ModuleName}DataSourceIsolationTest.java",
        "src/main/java/$featurePath/controller/${featureClassPrefix}Controller.java",
        "src/main/java/$featurePath/facade/${featureClassPrefix}Facade.java",
        "src/main/java/$featurePath/port/${featureClassPrefix}QueryPort.java",
        "src/main/java/$featurePath/port/${featureClassPrefix}CommandPort.java",
        "src/main/java/$featurePath/adapter/local/Local${featureClassPrefix}Adapter.java",
        "src/main/java/$featurePath/service/${featureClassPrefix}Service.java",
        "src/main/java/$featurePath/repository/${featureClassPrefix}Repository.java",
        "src/main/java/$featurePath/dto/${featureClassPrefix}SearchRequest.java",
        "src/main/java/$featurePath/dto/${featureClassPrefix}SampleCommand.java",
        "src/main/java/$featurePath/dto/${featureClassPrefix}SampleItem.java",
        "src/main/java/$featurePath/dto/${featureClassPrefix}SearchResult.java",
        "src/main/java/$featurePath/dto/${featureClassPrefix}DeleteCommand.java",
        "src/main/java/$featurePath/dto/${featureClassPrefix}DeleteResult.java",
        "src/main/java/$featurePath/dto/${featureClassPrefix}IdempotencyEntry.java",
        "src/main/java/$featurePath/validation/${featureClassPrefix}SearchValidator.java",
        "src/test/java/$featurePath/service/${featureClassPrefix}ServiceTest.java",
        "smoke/smoke-${DomainName}.ps1"
    )
    foreach ($relative in $required) {
        $path = Join-Path $previewDir $relative
        $exists = Test-Path -LiteralPath $path
        $result.requiredFiles += [ordered]@{
            path = $path.Substring($Root.Length).TrimStart('\', '/')
            exists = $exists
        }
        if (-not $exists) {
            throw "create-domain generated file is missing. path=$path"
        }
    }

    $forbidden = @(
        "patch-candidates",
        "src/main/java/$featurePath/batch",
        "src/main/java/$basePath/config/${ModuleName}BatchRepositoryConfig.java",
        "src/main/java/$featurePath/adapter/remote",
        "src/main/java/$featurePath/security",
        "src/main/java/$featurePath/messaging",
        "src/main/java/$featurePath/file",
        "src/main/resources/application-${DomainName}-prod.yml"
    )
    foreach ($relative in $forbidden) {
        $path = Join-Path $previewDir $relative
        $exists = Test-Path -LiteralPath $path
        $result.forbiddenFiles += [ordered]@{
            path = $path.Substring($Root.Length).TrimStart('\', '/')
            exists = $exists
        }
        if ($exists) {
            throw "create-domain default output contains a forbidden path. path=$path"
        }
    }

    $generatedTextFiles = @(Get-ChildItem -LiteralPath $previewDir -Recurse -File | Where-Object {
            $_.Extension -in @(".java", ".xml", ".yml", ".yaml", ".sql", ".md", ".ps1", ".gradle")
        })
    foreach ($textFile in $generatedTextFiles) {
        $content = [System.IO.File]::ReadAllText($textFile.FullName, [System.Text.Encoding]::UTF8)
        if ($content.Contains([char]0xFFFD) -or $content -match '\?{2,}') {
            throw "create-domain generated text contains mojibake marker. path=$($textFile.FullName)"
        }
    }
    $generatedManifest = Get-Content -LiteralPath (
            Join-Path $previewDir "manifest/domain-manifest.json") -Raw -Encoding UTF8 |
            ConvertFrom-Json
    if ([int]$generatedManifest.physicalTableContract.totalTables -ne 2 -or
            [int]$generatedManifest.physicalTableContract.businessTableCount -ne 1 -or
            [int]$generatedManifest.physicalTableContract.supportLedgerCount -ne 1 -or
            [bool]$generatedManifest.physicalTableContract.additionalTablesAllowed -or
            @($generatedManifest.minimalTransactionContract.requiredColumns).Count -ne 14 -or
            @($generatedManifest.idempotencyLedgerContract.requiredColumns).Count -ne 8 -or
            [string]$generatedManifest.idempotencyLedgerContract.tableRole -ne
                    "non-business-support-ledger" -or
            -not [bool]$generatedManifest.idempotencyLedgerContract.logicalDeleteReplayRequired) {
        throw "Generated Domain Sample 1개 + 비업무 Idempotency Ledger 1개 계약이 manifest에 기록되지 않았습니다."
    }

    $dataSourceConfigText = [IO.File]::ReadAllText(
            (Join-Path $previewDir "src/main/java/$basePath/config/${ModuleName}DataSourceConfig.java"),
            [Text.Encoding]::UTF8)
    if (-not $dataSourceConfigText.Contains(
                "CpfDataSources.resolve(environment, `"cpf.$DomainName.datasource`")") -or
            $dataSourceConfigText.Contains(
                'CpfDataSources.resolve(environment, "cpf.datasource")')) {
        throw "Generated Domain DataSource가 domain-specific namespace를 사용하지 않습니다."
    }
    $applicationModuleText = [IO.File]::ReadAllText(
            (Join-Path $previewDir "src/main/resources/application-${DomainName}.yml"),
            [Text.Encoding]::UTF8) -replace "`r`n?", "`n"
    if (-not $applicationModuleText.Contains(
                "  ${DomainName}:`n    datasource:") -or
            $applicationModuleText -match '(?m)^  datasource:\s*$') {
        throw "Generated Domain application YAML DataSource namespace가 격리되지 않았습니다."
    }

    if (Test-Path -LiteralPath $verificationDir) {
        Remove-Item -LiteralPath $verificationDir -Recurse -Force
    }
    New-Item -ItemType Directory -Force -Path $verificationDir | Out-Null
    $rootForGradle = $Root.Replace("\", "/")
    $previewForGradle = $previewDir.Replace("\", "/")
    $settings = @"
pluginManagement {
    includeBuild '${rootForGradle}/cpf-tools/build/gradle-plugin'
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id 'org.springframework.boot' version '$springBootVersion'
        id 'io.spring.dependency-management' version '$dependencyManagementVersion'
    }
}

rootProject.name = 'cpf-generated-domain-verification'
include 'cpf-core', 'cpf-common', '$projectName'
project(':cpf-core').projectDir = file('${rootForGradle}/cpf-core')
project(':cpf-common').projectDir = file('${rootForGradle}/cpf-common')
project(':$projectName').projectDir = file('$previewForGradle')
"@
    $rootBuild = @"
plugins {
    id 'java'
    id 'org.springframework.boot' version '$springBootVersion' apply false
    id 'io.spring.dependency-management' version '$dependencyManagementVersion' apply false
}

ext.cpfJavaVersion = (findProperty('cpfJavaVersion') ?: System.getenv('CPF_JAVA_VERSION') ?: '$cpfJavaVersion')
        .toString()
        .toInteger()
ext.cpfSpringBootVersion = '$springBootVersion'
ext.cpfCentralDbPackRoot = '${rootForGradle}/cpf-tools/db/vendor'

allprojects {
    version = '1.0.0-SNAPSHOT'

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'io.spring.dependency-management'

    dependencyManagement {
        imports {
            mavenBom 'org.springframework.boot:spring-boot-dependencies:$springBootVersion'
        }
    }

    dependencies {
        compileOnly 'org.projectlombok:lombok:1.18.46'
        annotationProcessor 'org.projectlombok:lombok:1.18.46'
        testCompileOnly 'org.projectlombok:lombok:1.18.46'
        testAnnotationProcessor 'org.projectlombok:lombok:1.18.46'
        testImplementation 'org.springframework.boot:spring-boot-starter-test'
        testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
    }

    tasks.withType(Test).configureEach {
        testLogging {
            exceptionFormat = 'full'
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
    }
}
"@
    [System.IO.File]::WriteAllText((Join-Path $verificationDir "settings.gradle"), $settings, $Utf8NoBom)
    [System.IO.File]::WriteAllText((Join-Path $verificationDir "build.gradle"), $rootBuild, $Utf8NoBom)
    $gradleWrapperJar = Join-Path $Root "gradle/wrapper/gradle-wrapper.jar"
    $javaExecutable = if ($env:JAVA_HOME -and (Test-Path -LiteralPath (Join-Path $env:JAVA_HOME "bin/java.exe"))) {
        Join-Path $env:JAVA_HOME "bin/java.exe"
    } else {
        (Get-Command java -ErrorAction Stop).Source
    }
    $compileLogPath = Join-Path $ResultDir "create-domain-compile.sanitized.log"
    # 회사 단말 정책이 중첩 PowerShell의 배치 파일 실행을 차단할 수 있으므로
    # wrapper jar를 Java 25 프로세스로 직접 실행해 플랫폼별 shell 차이를 제거합니다.
    $previousErrorActionPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = "Continue"
        $compileOutputLines = @(& $javaExecutable "-Dorg.gradle.appname=gradlew" -jar $gradleWrapperJar -p $verificationDir `
                ":${projectName}:test" ":${projectName}:bootJar" ":${projectName}:bootWar" `
                --no-daemon --max-workers=1 --console=plain 2>&1 | ForEach-Object { $_.ToString() })
        $compileExitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }
    $compileOutput = ($compileOutputLines -join "`n") + "`n"
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $compileRawLogPath) | Out-Null
    [System.IO.File]::WriteAllText($compileRawLogPath, $compileOutput, $Utf8NoBom)
    $compileStatus = if ($compileExitCode -eq 0) { $StatusDone } else { $StatusFailed }
    & (Join-Path $Root "cpf-tools/scripts/write-sanitized-evidence.ps1") `
        -EvidenceId "CREATE_DOMAIN_COMPILE" `
        -Status $compileStatus `
        -Command ".\gradlew.bat :${projectName}:test :${projectName}:bootJar :${projectName}:bootWar --no-daemon --max-workers=1" `
        -OutputPath $compileLogPath `
        -ExitCode $compileExitCode `
        -SourceLog $compileRawLogPath `
        -Root $Root
    $result.compile = [ordered]@{
        status = $compileStatus
        exitCode = $compileExitCode
        logPath = $compileLogPath.Substring($Root.Length).TrimStart('\', '/')
        testTask = ":${projectName}:test"
        bootJarTask = ":${projectName}:bootJar"
        bootWarTask = ":${projectName}:bootWar"
    }
    if ($compileExitCode -ne 0) {
        throw "generated domain compile/test/bootJar failed. log=$compileLogPath"
    }

    $applicationClass = Join-Path $previewDir "build/classes/java/main/$basePath/${moduleClassName}Application.class"
    if (-not (Test-Path -LiteralPath $applicationClass -PathType Leaf)) {
        throw "generated domain application class is missing. path=$applicationClass"
    }
    $classHeader = [byte[]]::new(8)
    $classStream = [System.IO.File]::OpenRead($applicationClass)
    $classHeaderLength = 0
    try {
        while ($classHeaderLength -lt $classHeader.Length) {
            $read = $classStream.Read(
                    $classHeader,
                    $classHeaderLength,
                    $classHeader.Length - $classHeaderLength)
            if ($read -eq 0) { break }
            $classHeaderLength += $read
        }
    }
    finally { $classStream.Dispose() }
    if ($classHeaderLength -lt 8) {
        throw "generated domain application class is invalid. path=$applicationClass"
    }
    $classMajor = ([int]$classHeader[6] * 256) + [int]$classHeader[7]
    $expectedClassMajor = $cpfJavaVersion + 44
    if ($classMajor -ne $expectedClassMajor) {
        throw "generated domain class major must be $expectedClassMajor. actual=$classMajor"
    }
    $bootJar = Get-ChildItem -LiteralPath (Join-Path $previewDir "build/libs") -File -Filter "*.jar" |
        Where-Object { $_.Name -notlike "*-plain.jar" } |
        Select-Object -First 1
    if ($null -eq $bootJar) {
        throw "generated domain bootJar is missing."
    }
    $bootWar = Get-ChildItem -LiteralPath (Join-Path $previewDir "build/libs") -File -Filter "*.war" |
        Select-Object -First 1
    if ($null -eq $bootWar) {
        throw "generated domain bootWar is missing."
    }
    $result.compile.classMajor = $classMajor
    $result.compile.bootJar = $bootJar.FullName.Substring($Root.Length).TrimStart('\', '/')
    $result.compile.bootWar = $bootWar.FullName.Substring($Root.Length).TrimStart('\', '/')
    $result.compile.javaExecutable = "JAVA_HOME/bin/java"

    $result.status = $StatusDone
    $result.finishedAt = (Get-Date).ToString("o")
    Write-Host "create-domain smoke passed. result=$resultPath"
} catch {
    $result.status = $StatusFailed
    $result.error = $_.Exception.Message
    $result.finishedAt = (Get-Date).ToString("o")
    throw
} finally {
    # 성공/실패와 무관하게 임시 Generated Domain을 build 밖에 남기지 않습니다.
    foreach ($temporaryDirectory in @($previewDir, $verificationDir, $runtimeScriptDir)) {
        if (Test-Path -LiteralPath $temporaryDirectory) {
            $resolvedTemporary = [System.IO.Path]::GetFullPath($temporaryDirectory)
            $allowedRoot = [System.IO.Path]::GetFullPath((Join-Path $Root "build"))
            if (-not $resolvedTemporary.StartsWith(
                        $allowedRoot + [IO.Path]::DirectorySeparatorChar,
                        [System.StringComparison]::OrdinalIgnoreCase)) {
                throw "생성기 임시 폴더가 build 경로 밖에 있어 정리할 수 없습니다. path=$resolvedTemporary"
            }
            Remove-Item -LiteralPath $resolvedTemporary -Recurse -Force
        }
    }
    $result.cleanup.previewRemoved = -not (Test-Path -LiteralPath $previewDir)
    $result.cleanup.verificationRemoved = -not (Test-Path -LiteralPath $verificationDir)
    $result.cleanup.runtimeScriptRemoved = -not (Test-Path -LiteralPath $runtimeScriptDir)
    if (Test-Path -LiteralPath $compileRawLogPath -PathType Leaf) {
        [System.IO.File]::Delete($compileRawLogPath)
    }
    $result.cleanup.rawLogRemoved = -not (Test-Path -LiteralPath $compileRawLogPath)
    Save-Result $result
}
