param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = "",
    [string] $ModuleCode = "pym",
    [string] $ModuleName = "Lending"
)

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

function New-UnicodeText {
    param([int[]] $CodePoints)
    return -join ($CodePoints | ForEach-Object { [char] $_ })
}

$StatusDone = New-UnicodeText @(0xC644, 0xB8CC)
$StatusFailed = New-UnicodeText @(0xC2E4, 0xD328)

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/runtime-smoke"
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "create-domain-result.json"
$previewDir = Join-Path $Root "build/domain-generator/$ModuleCode"
$verificationDir = Join-Path $Root "build/domain-generator-verification/$ModuleCode"

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
        "-BasePackage", "cpf.$ModuleCode",
        "-TablePrefix", $ModuleCode
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
        "src/main/resources/application.yml",
        "src/main/resources/application-${ModuleCode}.yml",
        "src/main/java/cpf/$ModuleCode/${ModuleName}Application.java",
        "src/main/java/cpf/$ModuleCode/controller/${ModuleName}Controller.java",
        "src/main/java/cpf/$ModuleCode/facade/${ModuleName}Facade.java",
        "src/main/java/cpf/$ModuleCode/service/${ModuleName}Service.java",
        "src/main/java/cpf/$ModuleCode/repository/${ModuleName}Repository.java",
        "src/main/java/cpf/$ModuleCode/dto/${ModuleName}SearchRequest.java",
        "src/main/java/cpf/$ModuleCode/validation/${ModuleName}SearchValidator.java",
        "src/test/java/cpf/$ModuleCode/service/${ModuleName}ServiceTest.java",
        "src/main/resources/mybatis/mapper/$ModuleCode/${ModuleName}Mapper.xml",
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

ext.cpfJavaVersion = (findProperty('cpfJavaVersion') ?: System.getenv('CPF_JAVA_VERSION') ?: '21')
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
        compileOnly 'org.projectlombok:lombok:1.18.32'
        annotationProcessor 'org.projectlombok:lombok:1.18.32'
        testCompileOnly 'org.projectlombok:lombok:1.18.32'
        testAnnotationProcessor 'org.projectlombok:lombok:1.18.32'
        testImplementation 'org.springframework.boot:spring-boot-starter-test'
        testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
    }
}
"@
    [System.IO.File]::WriteAllText((Join-Path $verificationDir "settings.gradle"), $settings, $Utf8NoBom)
    [System.IO.File]::WriteAllText((Join-Path $verificationDir "build.gradle"), $rootBuild, $Utf8NoBom)
    $gradleWrapper = Join-Path $Root "gradlew.bat"
    $compileLogPath = Join-Path $ResultDir "create-domain-compile.log"
    # 중첩 PowerShell의 Process.Start는 로컬 보안 정책에 따라 cmd.exe 실행이 차단될 수 있다.
    # 현재 프로세스에서 래퍼를 직접 실행하면 동일한 Gradle 검증을 수행하면서 종료 코드를 정확히 보존한다.
    $previousErrorActionPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = "Continue"
        $compileOutputLines = @(& $gradleWrapper -p $verificationDir `
                ":${ModuleCode}:test" ":${ModuleCode}:bootJar" `
                --offline --no-daemon --console=plain 2>&1 | ForEach-Object { $_.ToString() })
        $compileExitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }
    $compileOutput = ($compileOutputLines -join "`n") + "`n"
    [System.IO.File]::WriteAllText($compileLogPath, $compileOutput, $Utf8NoBom)
    $result.compile = [ordered]@{
        status = if ($compileExitCode -eq 0) { $StatusDone } else { $StatusFailed }
        exitCode = $compileExitCode
        logPath = $compileLogPath.Substring($Root.Length).TrimStart('\', '/')
        testTask = ":${ModuleCode}:test"
        bootJarTask = ":${ModuleCode}:bootJar"
    }
    if ($compileExitCode -ne 0) {
        throw "generated domain compile/test/bootJar failed. log=$compileLogPath"
    }

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
