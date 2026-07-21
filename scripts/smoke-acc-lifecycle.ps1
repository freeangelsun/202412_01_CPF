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

$sandbox = Join-Path $Root 'build/acc-lifecycle'
$sandboxRoot = Join-Path $sandbox 'repository'
$resultPath = Join-Path $ResultDir 'acc-lifecycle.sanitized.json'
$currentAcc = Join-Path $Root 'acc'
$result = [ordered]@{
    startedAt = (Get-Date).ToString('o')
    status = 'FAILED'
    snapshot = [ordered]@{}
    ownership = @()
    externalReferences = @()
    legacyRemovalProtection = [ordered]@{}
    delete = [ordered]@{}
    generate = [ordered]@{}
    compile = [ordered]@{}
    remove = [ordered]@{}
    parity = [ordered]@{}
    comparison = [ordered]@{}
}

function Get-Relative([string] $Base, [string] $Path) {
    $Path.Substring($Base.Length + 1).Replace('\', '/')
}

function Get-SourceFiles([string] $Base) {
    @(Get-ChildItem -LiteralPath $Base -Recurse -File | Where-Object {
        $_.FullName -notmatch '\\build\\|\\logs\\'
    })
}

function Get-TreeHash([string] $Base) {
    $lines = Get-SourceFiles $Base | Sort-Object FullName | ForEach-Object {
        $relative = Get-Relative $Base $_.FullName
        if ($relative -in @('create-domain-result.json', 'manifest/generator-ownership.json')) { return }
        "$relative=$((Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant())"
    }
    $sha = [Security.Cryptography.SHA256]::Create()
    try {
        $bytes = [Text.Encoding]::UTF8.GetBytes(($lines -join "`n"))
        ([BitConverter]::ToString($sha.ComputeHash($bytes))).Replace('-', '').ToLowerInvariant()
    } finally {
        $sha.Dispose()
    }
}

function Invoke-Script([string] $Path, [string[]] $Arguments, [bool] $AllowFailure = $false) {
    $oldPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        $output = @(& powershell -NoProfile -ExecutionPolicy Bypass -File $Path @Arguments 2>&1 | ForEach-Object { $_.ToString() })
        $exitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $oldPreference
    }
    if (-not $AllowFailure -and $exitCode -ne 0) {
        throw "스크립트 실행 실패: path=$Path, exitCode=$exitCode, output=$($output -join ' ')"
    }
    [ordered]@{ exitCode = $exitCode; output = $output }
}

function Invoke-AccGenerator {
    $arguments = @(
        '-Root', $sandboxRoot, '-ModuleCode', 'acc', '-ModuleName', 'Account',
        '-DomainIdCode', 'ACC', '-BasePackage', 'cpf.acc', '-TablePrefix', 'acc',
        '-Port', '8082', '-Online', 'Y', '-Database', 'Y', '-Batch', 'Y',
        '-External', 'Y', '-Ui', 'Y', '-BzaMenu', 'Y', '-ProductionProfile', 'Y', '-Apply'
    )
    [void](Invoke-Script (Join-Path $sandboxRoot 'scripts/create-domain.ps1') $arguments)
}

try {
    if (-not (Test-Path -LiteralPath $currentAcc -PathType Container)) {
        throw '현재 ACC 모듈이 없습니다.'
    }
    if (Test-Path -LiteralPath $sandbox) {
        Remove-Item -LiteralPath $sandbox -Recurse -Force
    }
    New-Item -ItemType Directory -Force -Path (Join-Path $sandboxRoot 'scripts') | Out-Null
    New-Item -ItemType Directory -Force -Path (Join-Path $sandboxRoot 'specs/sql') | Out-Null
    Copy-Item -LiteralPath (Join-Path $Root 'scripts/create-domain.ps1') -Destination (Join-Path $sandboxRoot 'scripts/create-domain.ps1')
    Copy-Item -LiteralPath (Join-Path $Root 'scripts/remove-domain.ps1') -Destination (Join-Path $sandboxRoot 'scripts/remove-domain.ps1')
    [IO.File]::WriteAllText((Join-Path $sandboxRoot 'settings.gradle'), "rootProject.name = 'cpf-acc-lifecycle'`r`n", $Utf8NoBom)

    $snapshotFiles = Get-SourceFiles $currentAcc
    $snapshot = @($snapshotFiles | ForEach-Object {
        $relative = Get-Relative $currentAcc $_.FullName
        [ordered]@{ path = $relative; sha256 = (Get-FileHash -LiteralPath $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant() }
    })
    $result.snapshot = [ordered]@{ fileCount = $snapshot.Count; treeSha256 = Get-TreeHash $currentAcc; files = $snapshot }
    $result.ownership = @($snapshot | ForEach-Object {
        $owner = if ($_.path -match '^src/(main|test)/java/cpf/acc/account/') {
            'business-owned'
        } elseif ($_.path -match '^(src/.+/reference/|src/.+/common/|manifest/|smoke/|build.gradle$|README.md$)') {
            'generator-owned-candidate'
        } elseif ($_.path -match '^src/main/resources/application-acc-(local|dev|stg|prod)\.yml$') {
            'operations-owned'
        } else {
            'user-owned-review'
        }
        [ordered]@{ path = $_.path; owner = $owner }
    })

    $scanExtensions = @('.java', '.xml', '.yml', '.yaml', '.json', '.sql', '.gradle', '.ps1', '.md')
    $result.externalReferences = @(Get-ChildItem -LiteralPath $Root -Recurse -File | Where-Object {
        $_.FullName -notlike "$currentAcc*" -and $_.FullName -notmatch '\\.git\\|\\build\\|\\specs\\evidence\\' -and
        $scanExtensions -contains $_.Extension.ToLowerInvariant()
    } | ForEach-Object {
        $text = [IO.File]::ReadAllText($_.FullName, [Text.Encoding]::UTF8)
        if ($text -match 'cpf\.acc\.|project\([''"]:acc[''"]\)|include\s+[''"]acc[''"]') {
            Get-Relative $Root $_.FullName
        }
    } | Sort-Object -Unique)

    $legacyPath = Join-Path $sandboxRoot 'acc'
    Copy-Item -LiteralPath $currentAcc -Destination $legacyPath -Recurse
    $legacyResultDir = Join-Path $sandbox 'legacy-remove'
    $legacyAttempt = Invoke-Script (Join-Path $sandboxRoot 'scripts/remove-domain.ps1') @(
        '-Root', $sandboxRoot, '-ModuleCode', 'acc', '-DryRun', '-ResultDir', $legacyResultDir) $true
    if ($legacyAttempt.exitCode -eq 0) {
        throw 'generator ownership manifest가 없는 기존 ACC 제거가 차단되지 않았습니다.'
    }
    $result.legacyRemovalProtection = [ordered]@{ status = 'DONE'; exitCode = $legacyAttempt.exitCode; reason = '소유권 manifest 부재로 자동 제거 차단' }

    Remove-Item -LiteralPath $legacyPath -Recurse -Force
    $settingsText = [IO.File]::ReadAllText((Join-Path $sandboxRoot 'settings.gradle'), [Text.Encoding]::UTF8)
    $result.delete = [ordered]@{ status = if (-not (Test-Path $legacyPath) -and $settingsText -notmatch "include 'acc'") { 'DONE' } else { 'FAILED' }; residualCount = 0 }
    if ($result.delete.status -ne 'DONE') { throw 'ACC 삭제 후 잔존 항목이 있습니다.' }

    Invoke-AccGenerator
    $generatedAcc = Join-Path $sandboxRoot 'acc'
    $generatedFiles = Get-SourceFiles $generatedAcc
    $emptyDirectories = @(Get-ChildItem -LiteralPath $generatedAcc -Recurse -Directory | Where-Object { @(Get-ChildItem -LiteralPath $_.FullName -Force).Count -eq 0 })
    $placeholderFiles = @($generatedFiles | Where-Object { $_.Name -match 'placeholder|\.keep$' })
    if ($emptyDirectories.Count -gt 0 -or $placeholderFiles.Count -gt 0) {
        throw '순수 생성 ACC에 빈 package 또는 placeholder가 남았습니다.'
    }
    $firstHash = Get-TreeHash $generatedAcc
    $result.generate = [ordered]@{ status = 'DONE'; fileCount = $generatedFiles.Count; treeSha256 = $firstHash; emptyDirectoryCount = 0; placeholderCount = 0 }

    $verification = Join-Path $sandbox 'verification'
    New-Item -ItemType Directory -Force -Path $verification | Out-Null
    $rootForGradle = $Root.Replace('\', '/')
    $accForGradle = $generatedAcc.Replace('\', '/')
    $settings = @"
pluginManagement {
    repositories { gradlePluginPortal(); mavenCentral() }
    plugins {
        id 'org.springframework.boot' version '3.4.13'
        id 'io.spring.dependency-management' version '1.1.7'
    }
}
rootProject.name = 'cpf-acc-lifecycle-verification'
include 'pfw', 'acc'
project(':pfw').projectDir = file('$rootForGradle/pfw')
project(':acc').projectDir = file('$accForGradle')
"@
    $build = @"
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
    [IO.File]::WriteAllText((Join-Path $verification 'settings.gradle'), $settings, $Utf8NoBom)
    [IO.File]::WriteAllText((Join-Path $verification 'build.gradle'), $build, $Utf8NoBom)
    $java = Join-Path $env:JAVA_HOME 'bin/java.exe'
    $wrapper = Join-Path $Root 'gradle/wrapper/gradle-wrapper.jar'
    $oldPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        $compileOutput = @(& $java '-Dorg.gradle.appname=gradlew' -jar $wrapper -p $verification ':acc:test' ':acc:bootJar' ':acc:bootWar' --no-daemon --console=plain 2>&1 | ForEach-Object { $_.ToString() })
        $compileExit = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $oldPreference
    }
    [IO.File]::WriteAllText((Join-Path $ResultDir 'acc-lifecycle-compile.sanitized.log'), (($compileOutput -join "`n") + "`n"), $Utf8NoBom)
    $result.compile = [ordered]@{ status = if ($compileExit -eq 0) { 'DONE' } else { 'FAILED' }; exitCode = $compileExit; tasks = @(':acc:test', ':acc:bootJar', ':acc:bootWar') }
    if ($compileExit -ne 0) { throw '순수 생성 ACC 컴파일·테스트·패키징이 실패했습니다.' }

    $dryRunDir = Join-Path $sandbox 'generated-remove-dry-run'
    [void](Invoke-Script (Join-Path $sandboxRoot 'scripts/remove-domain.ps1') @(
        '-Root', $sandboxRoot, '-ModuleCode', 'acc', '-DryRun', '-ResultDir', $dryRunDir))
    $dryRun = Get-Content -LiteralPath (Join-Path $dryRunDir 'remove-domain-result.json') -Raw -Encoding UTF8 | ConvertFrom-Json
    if ($dryRun.status -ne 'READY') { throw '순수 생성 ACC 제거 dry-run이 READY가 아닙니다.' }
    $actualDir = Join-Path $sandbox 'generated-remove-actual'
    [void](Invoke-Script (Join-Path $sandboxRoot 'scripts/remove-domain.ps1') @(
        '-Root', $sandboxRoot, '-ModuleCode', 'acc', '-ResultDir', $actualDir))
    if (Test-Path -LiteralPath $generatedAcc) { throw '순수 생성 ACC 실제 제거 후 디렉터리가 남았습니다.' }
    $result.remove = [ordered]@{ status = 'DONE'; dryRunStatus = $dryRun.status; residualCount = 0 }

    Invoke-AccGenerator
    $secondHash = Get-TreeHash (Join-Path $sandboxRoot 'acc')
    if ($firstHash -ne $secondHash) { throw 'ACC remove→generate 재생성 결과가 최초 순수 생성과 다릅니다.' }
    $result.parity = [ordered]@{ status = 'DONE'; firstSha256 = $firstHash; secondSha256 = $secondHash }

    $freshPaths = @(Get-SourceFiles (Join-Path $sandboxRoot 'acc') | ForEach-Object { Get-Relative (Join-Path $sandboxRoot 'acc') $_.FullName })
    $currentPaths = @($snapshot | ForEach-Object { $_.path })
    $result.comparison = [ordered]@{
        status = 'DONE'
        commonFileCount = @($currentPaths | Where-Object { $freshPaths -contains $_ }).Count
        currentOnly = @($currentPaths | Where-Object { $freshPaths -notcontains $_ })
        generatedOnly = @($freshPaths | Where-Object { $currentPaths -notcontains $_ })
    }
    $result.status = 'DONE'
} finally {
    $result.endedAt = (Get-Date).ToString('o')
    [IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 30), $Utf8NoBom)
    if (Test-Path -LiteralPath $sandbox) {
        Remove-Item -LiteralPath $sandbox -Recurse -Force
    }
}

Write-Host 'ACC 실제 삭제·생성 lifecycle 검증 완료'
