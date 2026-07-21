param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = ""
)

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
$resultPath = Join-Path $ResultDir 'domain-capability-matrix.sanitized.json'
$matrix = @(
    [ordered]@{ code = 'dcn'; name = 'CoreOnly'; database = 'N'; vendor = 'mariadb'; batch = 'N'; external = 'N'; ui = 'N' },
    [ordered]@{ code = 'dby'; name = 'DatabaseOnly'; database = 'Y'; vendor = 'mariadb'; batch = 'N'; external = 'N'; ui = 'N' },
    [ordered]@{ code = 'bty'; name = 'BatchEnabled'; database = 'Y'; vendor = 'mariadb'; batch = 'Y'; external = 'N'; ui = 'N' },
    [ordered]@{ code = 'exy'; name = 'ExternalEnabled'; database = 'Y'; vendor = 'mariadb'; batch = 'N'; external = 'Y'; ui = 'N' },
    [ordered]@{ code = 'uiy'; name = 'UiEnabled'; database = 'Y'; vendor = 'mariadb'; batch = 'N'; external = 'N'; ui = 'Y' },
    [ordered]@{ code = 'pgy'; name = 'PostgresqlDatabase'; database = 'Y'; vendor = 'postgresql'; batch = 'N'; external = 'N'; ui = 'N' },
    [ordered]@{ code = 'ory'; name = 'OracleDatabase'; database = 'Y'; vendor = 'oracle'; batch = 'N'; external = 'N'; ui = 'N' },
    [ordered]@{ code = 'sqy'; name = 'SqlserverDatabase'; database = 'Y'; vendor = 'sqlserver'; batch = 'N'; external = 'N'; ui = 'N' }
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
        '-NoProfile', '-ExecutionPolicy', 'Bypass', '-File', (Join-Path $Root 'scripts/create-domain.ps1'),
        '-Root', $Root, '-OutputDir', $OutputDir,
        '-ModuleCode', $Case.code, '-ModuleName', $Case.name,
        '-DomainIdCode', $Case.code.ToUpperInvariant(), '-TablePrefix', $Case.code,
        '-Port', '8280', '-Online', 'Y', '-Database', $Case.database,
        '-DatabaseVendor', $Case.vendor,
        '-Batch', $Case.batch, '-External', $Case.external, '-Ui', $Case.ui,
        '-BzaMenu', 'N', '-ProductionProfile', 'N'
    )
    $output = & powershell @arguments 2>&1
    if ($LASTEXITCODE -ne 0) {
        throw "생성기 실행에 실패했습니다. code=$($Case.code), output=$($output -join ' ')"
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

    foreach ($case in $matrix) {
        $moduleDir = Join-Path $sandbox $case.code
        Invoke-Generator $case $moduleDir

        Assert-Exists $moduleDir "src/main/java/cpf/$($case.code)/config/$($case.name)DataSourceConfig.java" ($case.database -eq 'Y')
        Assert-Exists $moduleDir "src/main/resources/mybatis/mapper/$($case.code)/reference/$($case.name)ReferenceMapper.xml" ($case.database -eq 'Y')
        Assert-Exists $moduleDir "sql/Vxx__$($case.code)_domain.sql" ($case.database -eq 'Y')
        Assert-Exists $moduleDir "src/main/java/cpf/$($case.code)/reference/batch/$($case.name)ReferenceBatchConfig.java" ($case.batch -eq 'Y')
        Assert-Exists $moduleDir "src/main/java/cpf/$($case.code)/reference/adapter/remote/Remote$($case.name)ReferenceQueryProxy.java" ($case.external -eq 'Y')
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
                (Join-Path $moduleDir "src/main/resources/application-$($case.code).yml"),
                [Text.Encoding]::UTF8
            )
            foreach ($marker in $vendorMarkers[$case.vendor]) {
                if (($buildText.IndexOf($marker, [StringComparison]::OrdinalIgnoreCase) -lt 0) -and
                        ($resourceText.IndexOf($marker, [StringComparison]::OrdinalIgnoreCase) -lt 0)) {
                    throw "DB 벤더 산출물 표식이 없습니다. code=$($case.code), vendor=$($case.vendor), marker=$marker"
                }
            }
            $manifest = Get-Content -LiteralPath (Join-Path $moduleDir 'manifest/domain-manifest.json') -Raw -Encoding UTF8 | ConvertFrom-Json
            if ($manifest.databaseVendor -ne $case.vendor) {
                throw "도메인 manifest DB 벤더가 일치하지 않습니다. code=$($case.code), expected=$($case.vendor), actual=$($manifest.databaseVendor)"
            }
        }
        if (($case.batch -eq 'N') -and $buildText -match 'starter-batch') {
            throw "Batch 미선택 조합에 Batch 의존성이 생성됐습니다. code=$($case.code)"
        }
        $result.combinations += [ordered]@{
            code = $case.code.ToUpperInvariant()
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
        "include 'pfw'",
        "project(':pfw').projectDir = file('${rootForGradle}/pfw')"
    )
    foreach ($case in $matrix) {
        $modulePath = (Join-Path $sandbox $case.code).Replace('\', '/')
        $settings += "include '$($case.code)'"
        $settings += "project(':$($case.code)').projectDir = file('$modulePath')"
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
        $tasks += ":$($case.code):test"
        $tasks += ":$($case.code):bootJar"
        $tasks += ":$($case.code):bootWar"
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
    $compileLog = Join-Path $ResultDir 'domain-capability-matrix-compile.sanitized.log'
    [IO.File]::WriteAllText($compileLog, (($output -join "`n") + "`n"), $Utf8NoBom)
    $result.compile = [ordered]@{ status = if ($exitCode -eq 0) { 'DONE' } else { 'FAILED' }; exitCode = $exitCode; taskCount = $tasks.Count }
    if ($exitCode -ne 0) {
        throw "capability matrix compile/test/package가 실패했습니다. log=$compileLog"
    }

    $parityCase = $matrix[0]
    $firstPath = Join-Path $sandbox $parityCase.code
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
