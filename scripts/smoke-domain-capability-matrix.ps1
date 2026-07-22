param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = ""
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = 'Stop'
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root 'build/runtime-smoke'
} elseif (-not [IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$sandbox = Join-Path $Root 'build/domain-capability-matrix'
$runtimeGeneratorPath = Join-Path $sandbox 'runtime/create-domain.ps1'
$resultPath = Join-Path $ResultDir 'domain-capability-matrix.sanitized.json'
$matrix = @(
    [ordered]@{ domain = 'coreonly'; code = 'DCO'; name = 'CoreOnly'; database = 'N'; vendor = 'mariadb'; batch = 'N'; external = 'N'; messaging = 'N'; file = 'N'; security = 'N'; ui = 'N' },
    [ordered]@{ domain = 'databaseonly'; code = 'DBO'; name = 'DatabaseOnly'; database = 'Y'; vendor = 'mariadb'; batch = 'N'; external = 'N'; messaging = 'N'; file = 'N'; security = 'N'; ui = 'N' },
    [ordered]@{ domain = 'batchenabled'; code = 'BTE'; name = 'BatchEnabled'; database = 'Y'; vendor = 'mariadb'; batch = 'Y'; external = 'N'; messaging = 'N'; file = 'N'; security = 'N'; ui = 'N' },
    [ordered]@{ domain = 'externalenabled'; code = 'EXE'; name = 'ExternalEnabled'; database = 'Y'; vendor = 'mariadb'; batch = 'N'; external = 'Y'; messaging = 'N'; file = 'N'; security = 'N'; ui = 'N' },
    [ordered]@{ domain = 'messagingenabled'; code = 'MSE'; name = 'MessagingEnabled'; database = 'Y'; vendor = 'mariadb'; batch = 'N'; external = 'N'; messaging = 'Y'; file = 'N'; security = 'N'; ui = 'N' },
    [ordered]@{ domain = 'fileenabled'; code = 'FLE'; name = 'FileEnabled'; database = 'Y'; vendor = 'mariadb'; batch = 'N'; external = 'N'; messaging = 'N'; file = 'Y'; security = 'N'; ui = 'N' },
    [ordered]@{ domain = 'securityenabled'; code = 'SCE'; name = 'SecurityEnabled'; database = 'Y'; vendor = 'mariadb'; batch = 'N'; external = 'N'; messaging = 'N'; file = 'N'; security = 'Y'; ui = 'N' },
    [ordered]@{ domain = 'uienabled'; code = 'UIE'; name = 'UiEnabled'; database = 'Y'; vendor = 'mariadb'; batch = 'N'; external = 'N'; messaging = 'N'; file = 'N'; security = 'N'; ui = 'Y' },
    [ordered]@{ domain = 'postgresqlsample'; code = 'PGS'; name = 'PostgresqlSample'; database = 'Y'; vendor = 'postgresql'; batch = 'N'; external = 'N'; messaging = 'N'; file = 'N'; security = 'N'; ui = 'N' },
    [ordered]@{ domain = 'oraclesample'; code = 'ORS'; name = 'OracleSample'; database = 'Y'; vendor = 'oracle'; batch = 'N'; external = 'N'; messaging = 'N'; file = 'N'; security = 'N'; ui = 'N' },
    [ordered]@{ domain = 'sqlserversample'; code = 'SQS'; name = 'SqlserverSample'; database = 'Y'; vendor = 'sqlserver'; batch = 'N'; external = 'N'; messaging = 'N'; file = 'N'; security = 'N'; ui = 'N' }
)
$result = [ordered]@{
    startedAt = (Get-Date).ToString('o')
    status = 'FAILED'
    combinations = @()
    compile = [ordered]@{}
    parity = [ordered]@{}
}

function Invoke-Generator([hashtable] $Case, [string] $OutputDir) {
    $arguments = @(
        '-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', $runtimeGeneratorPath,
        '-Root', $Root, '-OutputDir', $OutputDir,
        '-DomainName', $Case.domain, '-SystemCode', $Case.code, '-ModuleName', $Case.name,
        '-TablePrefix', $Case.domain,
        '-Port', '8280', '-Online', 'Y', '-Database', $Case.database,
        '-DatabaseVendor', $Case.vendor,
        '-Batch', $Case.batch, '-External', $Case.external,
        '-Messaging', $Case.messaging, '-File', $Case.file, '-SecurityAudit', $Case.security,
        '-Ui', $Case.ui,
        '-BzaMenu', 'N', '-ProductionProfile', 'N'
    )
    $output = & powershell @arguments 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "생성기 실행에 실패했습니다. domain=$($Case.domain), output=$($output -join ' ')"
    }
}

function Assert-Exists([string] $Base, [string] $Relative, [bool] $Expected) {
    $actual = Test-Path -LiteralPath (Join-Path $Base $Relative)
    if ($actual -ne $Expected) {
        throw "capability 파일 조건이 일치하지 않습니다. path=$Relative, expected=$Expected, actual=$actual"
    }
}

function Get-TreeHash([string] $Base) {
    $lines = Get-ChildItem -LiteralPath $Base -Recurse -File | Sort-Object FullName | ForEach-Object {
        $relative = $_.FullName.Substring($Base.Length + 1).Replace('\', '/')
        if ($relative -match '^build/' -or
                $relative -in @('create-domain-result.json', 'manifest/generator-ownership.json')) {
            return
        }
        "$relative=$((Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant())"
    }
    $sha = [Security.Cryptography.SHA256]::Create()
    try {
        $bytes = [Text.Encoding]::UTF8.GetBytes(($lines -join "`n"))
        return ([BitConverter]::ToString($sha.ComputeHash($bytes))).Replace('-', '').ToLowerInvariant()
    } finally {
        $sha.Dispose()
    }
}

try {
    if (Test-Path -LiteralPath $sandbox) {
        Remove-Item -LiteralPath $sandbox -Recurse -Force
    }
    New-Item -ItemType Directory -Force -Path $sandbox | Out-Null
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $runtimeGeneratorPath) | Out-Null
    # Windows PowerShell 5.1이 BOM 없는 UTF-8 스크립트를 ANSI로 해석하지 않도록 실행 사본에 BOM을 붙입니다.
    $generatorText = [IO.File]::ReadAllText((Join-Path $Root 'scripts/create-domain.ps1'), [Text.Encoding]::UTF8)
    [IO.File]::WriteAllText($runtimeGeneratorPath, $generatorText, [Text.UTF8Encoding]::new($true))

    foreach ($case in $matrix) {
        $projectName = "cpf-$($case.domain)"
        $moduleDir = Join-Path $sandbox $projectName
        Invoke-Generator $case $moduleDir

        $javaBase = "src/main/java/com/cpf/$($case.domain)"
        $testBase = "src/test/java/com/cpf/$($case.domain)"
        Assert-Exists $moduleDir "$javaBase/config/$($case.name)DataSourceConfig.java" ($case.database -eq 'Y')
        Assert-Exists $moduleDir "src/main/resources/mybatis/mapper/$($case.domain)/reference/$($case.name)ReferenceMapper.xml" ($case.database -eq 'Y')
        Assert-Exists $moduleDir "sql/Vxx__$($case.domain)_domain.sql" ($case.database -eq 'Y')
        Assert-Exists $moduleDir "$javaBase/reference/batch/$($case.name)ReferenceBatchConfig.java" ($case.batch -eq 'Y')
        Assert-Exists $moduleDir "$javaBase/reference/adapter/remote/Remote$($case.name)ReferenceQueryProxy.java" ($case.external -eq 'Y')
        Assert-Exists $moduleDir "$javaBase/reference/messaging/$($case.name)EventPublisher.java" ($case.messaging -eq 'Y')
        Assert-Exists $moduleDir "$testBase/reference/messaging/$($case.name)EventPublisherTest.java" ($case.messaging -eq 'Y')
        Assert-Exists $moduleDir "$javaBase/reference/file/$($case.name)FileTransferService.java" ($case.file -eq 'Y')
        Assert-Exists $moduleDir "$testBase/reference/file/$($case.name)FileTransferServiceTest.java" ($case.file -eq 'Y')
        Assert-Exists $moduleDir "$javaBase/reference/security/$($case.name)OperationGuard.java" ($case.security -eq 'Y')
        Assert-Exists $moduleDir "ui/src/features/reference/$($case.name)ReferencePage.vue" ($case.ui -eq 'Y')
        Assert-Exists $moduleDir 'manifest/generator-ownership.json' $true

        $buildText = [IO.File]::ReadAllText((Join-Path $moduleDir 'build.gradle'), [Text.Encoding]::UTF8)
        if (($case.database -eq 'N') -and $buildText -match 'mybatis|flyway|mariadb') {
            throw "DB 미선택 조합에 DB 의존성이 생성됐습니다. code=$($case.code)"
        }
        if ($case.database -eq 'Y') {
            $vendorMarkers = [ordered]@{
                mariadb = @('org.flywaydb:flyway-mysql', 'org.mariadb.jdbc:mariadb-java-client', 'jdbc:mariadb:')
                postgresql = @('org.flywaydb:flyway-database-postgresql', 'org.postgresql:postgresql', 'jdbc:postgresql:')
                oracle = @('org.flywaydb:flyway-database-oracle', 'com.oracle.database.jdbc:ojdbc11', 'jdbc:oracle:')
                sqlserver = @('org.flywaydb:flyway-sqlserver', 'com.microsoft.sqlserver:mssql-jdbc', 'jdbc:sqlserver:')
            }
            $resourceText = [IO.File]::ReadAllText(
                (Join-Path $moduleDir "src/main/resources/application-$($case.domain).yml"),
                [Text.Encoding]::UTF8
            )
            $expectedJndiName = "java:comp/env/jdbc/cpf$($case.name)DataSource"
            if (-not $resourceText.Contains($expectedJndiName)) {
                throw "표준 JNDI 이름이 생성되지 않았습니다. domain=$($case.domain), expected=$expectedJndiName"
            }
            foreach ($marker in $vendorMarkers[$case.vendor]) {
                if (($buildText.IndexOf($marker, [StringComparison]::OrdinalIgnoreCase) -lt 0) -and
                        ($resourceText.IndexOf($marker, [StringComparison]::OrdinalIgnoreCase) -lt 0)) {
                    throw "DB 벤더 산출물 표식이 없습니다. domain=$($case.domain), vendor=$($case.vendor), marker=$marker"
                }
            }
            $manifest = Get-Content -LiteralPath (Join-Path $moduleDir 'manifest/domain-manifest.json') -Raw -Encoding UTF8 | ConvertFrom-Json
            if ($manifest.databaseVendor -ne $case.vendor -or $manifest.domainName -ne $case.domain -or $manifest.systemCode -ne $case.code) {
                throw "도메인 manifest DB 벤더가 일치하지 않습니다. code=$($case.code), expected=$($case.vendor), actual=$($manifest.databaseVendor)"
            }
        }
        if (($case.batch -eq 'N') -and $buildText -match 'starter-batch') {
            throw "Batch 미선택 조합에 Batch 의존성이 생성됐습니다. code=$($case.code)"
        }
        $result.combinations += [ordered]@{
            domainName = $case.domain
            systemCode = $case.code
            projectName = $projectName
            database = $case.database
            databaseVendor = $case.vendor
            batch = $case.batch
            external = $case.external
            ui = $case.ui
            generatedFileCount = @(Get-ChildItem -LiteralPath $moduleDir -Recurse -File).Count
            status = 'DONE'
        }
    }

    $rootForGradle = $Root.Replace('\', '/')
    $settings = @(
        "pluginManagement {",
        "    repositories { gradlePluginPortal(); mavenCentral() }",
        "    plugins {",
        "        id 'org.springframework.boot' version '3.4.13'",
        "        id 'io.spring.dependency-management' version '1.1.7'",
        "    }",
        "}",
        "rootProject.name = 'cpf-domain-capability-matrix'",
        "include 'cpf-core'",
        "project(':cpf-core').projectDir = file('${rootForGradle}/cpf-core')"
    )
    foreach ($case in $matrix) {
        $projectName = "cpf-$($case.domain)"
        $modulePath = (Join-Path $sandbox $projectName).Replace('\', '/')
        $settings += "include '$projectName'"
        $settings += "project(':$projectName').projectDir = file('$modulePath')"
    }
    [IO.File]::WriteAllText((Join-Path $sandbox 'settings.gradle'), (($settings -join "`r`n") + "`r`n"), $Utf8NoBom)
    $rootBuild = @"
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.4.13' apply false
    id 'io.spring.dependency-management' version '1.1.7' apply false
}
ext.cpfJavaVersion = 25
allprojects { repositories { mavenCentral() } }
subprojects {
    apply plugin: 'java'
    apply plugin: 'io.spring.dependency-management'
    dependencyManagement { imports { mavenBom 'org.springframework.boot:spring-boot-dependencies:3.4.13' } }
    dependencies {
        compileOnly 'org.projectlombok:lombok:1.18.46'
        annotationProcessor 'org.projectlombok:lombok:1.18.46'
        testImplementation 'org.springframework.boot:spring-boot-starter-test'
        testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
    }
}
"@
    [IO.File]::WriteAllText((Join-Path $sandbox 'build.gradle'), $rootBuild, $Utf8NoBom)

    $tasks = @()
    foreach ($case in $matrix) {
        $projectName = "cpf-$($case.domain)"
        $tasks += ":${projectName}:test"
        $tasks += ":${projectName}:bootJar"
        $tasks += ":${projectName}:bootWar"
    }
    $java = Join-Path $env:JAVA_HOME 'bin/java.exe'
    $wrapper = Join-Path $Root 'gradle/wrapper/gradle-wrapper.jar'
    $previousErrorActionPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        $output = @(& $java '-Dorg.gradle.appname=gradlew' -jar $wrapper -p $sandbox @tasks --no-daemon --console=plain 2>&1 | ForEach-Object { $_.ToString() })
        $exitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $previousErrorActionPreference
    }
    $compileRawLog = Join-Path $sandbox 'domain-capability-matrix-compile.raw.log'
    $compileLog = Join-Path $ResultDir 'domain-capability-matrix-compile.sanitized.log'
    [IO.File]::WriteAllText($compileRawLog, (($output -join "`n") + "`n"), $Utf8NoBom)
    & (Join-Path $Root 'scripts/write-sanitized-evidence.ps1') `
        -Root $Root `
        -EvidenceId 'DOMAIN_CAPABILITY_MATRIX_COMPILE' `
        -Status $(if ($exitCode -eq 0) { '완료' } else { '실패' }) `
        -Command '.\gradlew.bat checkDomainCapabilityMatrix --no-daemon' `
        -OutputPath $compileLog `
        -SourceLog $compileRawLog `
        -ExitCode $exitCode `
        -Profile 'generator-sandbox' `
        -ReproduceCommand '.\gradlew.bat checkDomainCapabilityMatrix --no-daemon'
    $result.compile = [ordered]@{ status = if ($exitCode -eq 0) { 'DONE' } else { 'FAILED' }; exitCode = $exitCode; taskCount = $tasks.Count }
    if ($exitCode -ne 0) {
        throw "capability matrix compile/test/package가 실패했습니다. log=$compileLog"
    }

    $parityCase = $matrix[0]
    $firstPath = Join-Path $sandbox "cpf-$($parityCase.domain)"
    $firstHash = Get-TreeHash $firstPath
    Remove-Item -LiteralPath $firstPath -Recurse -Force
    Invoke-Generator $parityCase $firstPath
    $secondHash = Get-TreeHash $firstPath
    if ($firstHash -ne $secondHash) {
        throw '동일 입력 재생성 결과의 tree hash가 다릅니다.'
    }
    $result.parity = [ordered]@{ status = 'DONE'; firstSha256 = $firstHash; secondSha256 = $secondHash }
    $result.status = 'DONE'
} finally {
    $result.endedAt = (Get-Date).ToString('o')
    [IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 20), $Utf8NoBom)
    if (Test-Path -LiteralPath $sandbox) {
        Remove-Item -LiteralPath $sandbox -Recurse -Force
    }
}

Write-Host "도메인 capability matrix 검증 완료: combinations=$($matrix.Count), parity=DONE"
