param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = "",
    [string] $ModuleCode = "pym",
    [string] $ModuleName = "Lending"
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path

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
$previewDir = Join-Path $Root "build/domain-generator/$ModuleCode"
$verificationDir = Join-Path $Root "build/domain-generator-verification/$ModuleCode"
$moduleClassName = $ModuleCode.Substring(0, 1).ToUpperInvariant() + $ModuleCode.Substring(1).ToLowerInvariant()
$featureClassPrefix = "${ModuleName}Reference"
$featurePath = "cpf/$ModuleCode/reference"

function Save-Result {
    param([object] $Result)
    [System.IO.File]::WriteAllText($resultPath, ($Result | ConvertTo-Json -Depth 30), $Utf8NoBom)
}

function Invoke-CreateDomain {
    param(
        [switch] $DryRun,
        [switch] $GeneratePatch
    )

    $sourceScript = Join-Path $Root "scripts/create-domain.ps1"
    $runtimeScriptDir = Join-Path $Root "build/domain-generator-runtime"
    $runtimeScript = Join-Path $runtimeScriptDir "create-domain.ps1"
    New-Item -ItemType Directory -Force -Path $runtimeScriptDir | Out-Null
    $sourceText = [System.IO.File]::ReadAllText($sourceScript, [System.Text.Encoding]::UTF8)
    [System.IO.File]::WriteAllText($runtimeScript, $sourceText, [System.Text.UTF8Encoding]::new($true))

    $arguments = @(
        "-NoProfile",
        "-ExecutionPolicy", "Bypass",
        "-File", $runtimeScript,
        "-Root", $Root,
        "-ModuleCode", $ModuleCode,
        "-ModuleName", $ModuleName,
        "-DomainIdCode", $ModuleCode.ToUpperInvariant(),
        "-BasePackage", "cpf.$ModuleCode",
        "-TablePrefix", $ModuleCode,
        "-Port", "8188",
        "-Online", "Y",
        "-Batch", "Y",
        "-BzaMenu", "Y"
    )
    if ($DryRun) {
        $arguments += "-DryRun"
    }
    if ($GeneratePatch) {
        $arguments += "-GeneratePatch"
    }

    $output = & powershell @arguments
    if ($LASTEXITCODE -ne 0) {
        throw "create-domain script failed. exitCode=$LASTEXITCODE"
    }
    return ([string]::Join("`n", @($output)) | ConvertFrom-Json)
}

$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    moduleCode = $ModuleCode
    dryRun = [ordered]@{}
    generate = [ordered]@{}
    generatePatch = [ordered]@{}
    requiredFiles = @()
    requiredPatchFiles = @()
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
        "src/main/resources/application.yml",
        "src/main/resources/application-${ModuleCode}.yml",
        "src/main/java/cpf/$ModuleCode/${moduleClassName}Application.java",
        "src/main/java/cpf/$ModuleCode/config/${ModuleName}DataSourceConfig.java",
        "src/main/java/cpf/$ModuleCode/config/${ModuleName}MyBatisConfig.java",
        "src/main/java/$featurePath/controller/${featureClassPrefix}Controller.java",
        "src/main/java/$featurePath/facade/${featureClassPrefix}Facade.java",
        "src/main/java/$featurePath/port/${featureClassPrefix}QueryPort.java",
        "src/main/java/$featurePath/adapter/local/Local${featureClassPrefix}QueryAdapter.java",
        "src/main/java/$featurePath/adapter/remote/Remote${featureClassPrefix}QueryProxy.java",
        "src/main/java/$featurePath/batch/${featureClassPrefix}BatchConfig.java",
        "src/main/java/cpf/$ModuleCode/config/${ModuleName}BatchRepositoryConfig.java",
        "src/main/java/$featurePath/service/${featureClassPrefix}Service.java",
        "src/main/java/$featurePath/repository/${featureClassPrefix}Repository.java",
        "src/main/java/$featurePath/dto/${featureClassPrefix}SearchRequest.java",
        "src/main/java/$featurePath/validation/${featureClassPrefix}SearchValidator.java",
        "src/test/java/$featurePath/service/${featureClassPrefix}ServiceTest.java",
        "src/main/resources/mybatis/mapper/$ModuleCode/reference/${featureClassPrefix}Mapper.xml",
        "smoke/smoke-${ModuleCode}.ps1",
        "sql/Vxx__${ModuleCode}_domain.sql"
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

    if (Test-Path -LiteralPath $previewDir) {
        Remove-Item -LiteralPath $previewDir -Recurse -Force
    }
    $result.generatePatch = Invoke-CreateDomain -GeneratePatch

    foreach ($relative in $required) {
        $path = Join-Path $previewDir $relative
        $exists = Test-Path -LiteralPath $path
        if (-not $exists) {
            throw "create-domain generate-patch base file is missing. path=$path"
        }
    }
    $requiredPatch = @(
        "patch-candidates/apply-order.md",
        "patch-candidates/settings.gradle.patch",
        "patch-candidates/sql/40_business_modules_schema.${ModuleCode}.candidate.sql",
        "patch-candidates/sql/50_framework_seed.${ModuleCode}.candidate.sql",
        "patch-candidates/sql/60_adm_seed.${ModuleCode}.candidate.sql",
        "patch-candidates/sql/70_bza_menu_seed.${ModuleCode}.candidate.sql",
        "patch-candidates/sql/99_smoke_check.${ModuleCode}.candidate.sql",
        "patch-candidates/sql/migration/Vxx__${ModuleCode}_domain.sql",
        "patch-candidates/smoke-${ModuleCode}.ps1"
    )
    foreach ($relative in $requiredPatch) {
        $path = Join-Path $previewDir $relative
        $exists = Test-Path -LiteralPath $path
        $result.requiredPatchFiles += [ordered]@{
            path = $path.Substring($Root.Length).TrimStart('\', '/')
            exists = $exists
        }
        if (-not $exists) {
            throw "create-domain patch candidate is missing. path=$path"
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

    if (Test-Path -LiteralPath $verificationDir) {
        Remove-Item -LiteralPath $verificationDir -Recurse -Force
    }
    New-Item -ItemType Directory -Force -Path $verificationDir | Out-Null
    $rootForGradle = $Root.Replace("\", "/")
    $previewForGradle = $previewDir.Replace("\", "/")
    $settings = @"
pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
    plugins {
        id 'org.springframework.boot' version '3.4.13'
        id 'io.spring.dependency-management' version '1.1.7'
    }
}

rootProject.name = 'cpf-generated-domain-verification'
include 'pfw', 'cmn', '$ModuleCode'
project(':pfw').projectDir = file('${rootForGradle}/pfw')
project(':cmn').projectDir = file('${rootForGradle}/cmn')
project(':$ModuleCode').projectDir = file('$previewForGradle')
"@
    $rootBuild = @"
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.4.13' apply false
    id 'io.spring.dependency-management' version '1.1.7' apply false
}

ext.cpfJavaVersion = (findProperty('cpfJavaVersion') ?: System.getenv('CPF_JAVA_VERSION') ?: '25')
        .toString()
        .toInteger()

allprojects {
    repositories {
        mavenCentral()
    }
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'io.spring.dependency-management'

    dependencyManagement {
        imports {
            mavenBom 'org.springframework.boot:spring-boot-dependencies:3.4.13'
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
    $compileRawLogPath = Join-Path $Root "build/runtime-smoke/create-domain-compile.raw.log"
    $compileLogPath = Join-Path $ResultDir "create-domain-compile.sanitized.log"
    # 회사 단말 정책이 중첩 PowerShell의 배치 파일 실행을 차단할 수 있으므로
    # wrapper jar를 Java 25 프로세스로 직접 실행해 플랫폼별 shell 차이를 제거합니다.
    $previousErrorActionPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = "Continue"
        $compileOutputLines = @(& $javaExecutable "-Dorg.gradle.appname=gradlew" -jar $gradleWrapperJar -p $verificationDir `
                ":${ModuleCode}:test" ":${ModuleCode}:bootJar" ":${ModuleCode}:bootWar" `
                --no-daemon --console=plain 2>&1 | ForEach-Object { $_.ToString() })
        $compileExitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }
    $compileOutput = ($compileOutputLines -join "`n") + "`n"
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $compileRawLogPath) | Out-Null
    [System.IO.File]::WriteAllText($compileRawLogPath, $compileOutput, $Utf8NoBom)
    $compileStatus = if ($compileExitCode -eq 0) { $StatusDone } else { $StatusFailed }
    & (Join-Path $Root "scripts/write-sanitized-evidence.ps1") `
        -EvidenceId "CREATE_DOMAIN_COMPILE" `
        -Status $compileStatus `
        -Command ".\gradlew.bat :${ModuleCode}:test :${ModuleCode}:bootJar :${ModuleCode}:bootWar" `
        -OutputPath $compileLogPath `
        -ExitCode $compileExitCode `
        -SourceLog $compileRawLogPath `
        -Root $Root
    $result.compile = [ordered]@{
        status = $compileStatus
        exitCode = $compileExitCode
        logPath = $compileLogPath.Substring($Root.Length).TrimStart('\', '/')
        testTask = ":${ModuleCode}:test"
        bootJarTask = ":${ModuleCode}:bootJar"
        bootWarTask = ":${ModuleCode}:bootWar"
    }
    if ($compileExitCode -ne 0) {
        throw "generated domain compile/test/bootJar failed. log=$compileLogPath"
    }

    $applicationClass = Join-Path $previewDir "build/classes/java/main/cpf/$ModuleCode/${moduleClassName}Application.class"
    if (-not (Test-Path -LiteralPath $applicationClass -PathType Leaf)) {
        throw "generated domain application class is missing. path=$applicationClass"
    }
    $classBytes = [System.IO.File]::ReadAllBytes($applicationClass)
    if ($classBytes.Length -lt 8) {
        throw "generated domain application class is invalid. path=$applicationClass"
    }
    $classMajor = ([int]$classBytes[6] * 256) + [int]$classBytes[7]
    if ($classMajor -ne 69) {
        throw "generated domain class major must be 69. actual=$classMajor"
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

    # 검증 산출물은 저장소에 남기지 않고 결과 메타데이터와 컴파일 로그만 증적으로 보존합니다.
    foreach ($temporaryDirectory in @($previewDir, $verificationDir)) {
        if (Test-Path -LiteralPath $temporaryDirectory) {
            $resolvedTemporary = [System.IO.Path]::GetFullPath($temporaryDirectory)
            $allowedRoot = [System.IO.Path]::GetFullPath((Join-Path $Root "build"))
            if (-not $resolvedTemporary.StartsWith($allowedRoot, [System.StringComparison]::OrdinalIgnoreCase)) {
                throw "생성기 임시 폴더가 build 경로 밖에 있어 정리할 수 없습니다. path=$resolvedTemporary"
            }
            Remove-Item -LiteralPath $resolvedTemporary -Recurse -Force
        }
    }
    $result.cleanup.previewRemoved = -not (Test-Path -LiteralPath $previewDir)
    $result.cleanup.verificationRemoved = -not (Test-Path -LiteralPath $verificationDir)

    $result.status = $StatusDone
    $result.finishedAt = (Get-Date).ToString("o")
    Save-Result $result
    Write-Host "create-domain smoke passed. result=$resultPath"
} catch {
    $result.status = $StatusFailed
    $result.error = $_.Exception.Message
    $result.finishedAt = (Get-Date).ToString("o")
    Save-Result $result
    throw
}
