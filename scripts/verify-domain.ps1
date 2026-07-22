param(
    [Parameter(Mandatory = $true)]
    [string] $DomainName,
    [string] $SystemCode = "",
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = "",
    [switch] $SkipBuild
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
$domain = $DomainName.Trim().ToLowerInvariant()
if ($domain -notmatch '^[a-z][a-z0-9]{1,29}$') {
    throw "DomainName은 영문자로 시작하는 2~30자리 영문 소문자·숫자여야 합니다."
}

$projectName = "cpf-$domain"
$projectDir = Join-Path $Root $projectName
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/reports/verify-domain/$domain"
} elseif (-not [System.IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir "verify-domain-result.json"
$buildLogPath = Join-Path $ResultDir "verify-domain-build.sanitized.log"
$failures = New-Object System.Collections.Generic.List[string]
$checks = New-Object System.Collections.Generic.List[object]
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = "FAILED"
    domainName = $domain
    systemCode = $SystemCode.Trim().ToUpperInvariant()
    projectName = $projectName
    buildSkipped = [bool]$SkipBuild
    checks = @()
    failures = @()
    build = [ordered]@{ executed = $false; exitCode = $null; logPath = $buildLogPath }
}

function Add-Check {
    param([string] $Name, [bool] $Passed, [string] $Detail)
    $checks.Add([ordered]@{ name = $Name; passed = $Passed; detail = $Detail }) | Out-Null
    if (-not $Passed) {
        $failures.Add("$Name - $Detail") | Out-Null
    }
}

function Test-RelativeFile {
    param([string] $RelativePath)
    return Test-Path -LiteralPath (Join-Path $projectDir $RelativePath) -PathType Leaf
}

function Save-Result {
    $result.finishedAt = (Get-Date).ToString("o")
    $result.checks = @($checks.ToArray())
    $result.failures = @($failures.ToArray())
    [System.IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 30), $Utf8NoBom)
}

try {
    Add-Check "PROJECT_DIRECTORY" (Test-Path -LiteralPath $projectDir -PathType Container) $projectName
    if ($failures.Count -gt 0) {
        throw "검증 대상 모듈 디렉터리가 없습니다."
    }

    $manifestPath = Join-Path $projectDir "manifest/domain-manifest.json"
    $ownershipPath = Join-Path $projectDir "manifest/generator-ownership.json"
    Add-Check "DOMAIN_MANIFEST" (Test-Path -LiteralPath $manifestPath -PathType Leaf) "manifest/domain-manifest.json"
    Add-Check "GENERATOR_OWNERSHIP" (Test-Path -LiteralPath $ownershipPath -PathType Leaf) "manifest/generator-ownership.json"
    if ($failures.Count -gt 0) {
        throw "필수 Generator manifest가 없습니다."
    }

    $manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
    $ownership = Get-Content -LiteralPath $ownershipPath -Raw -Encoding UTF8 | ConvertFrom-Json
    $manifestCode = ([string]$manifest.systemCode).ToUpperInvariant()
    $expectedCode = if ([string]::IsNullOrWhiteSpace($SystemCode)) { $manifestCode } else { $SystemCode.Trim().ToUpperInvariant() }
    $result.systemCode = $manifestCode

    Add-Check "DOMAIN_NAME" ([string]$manifest.domainName -eq $domain) "manifest=$($manifest.domainName), expected=$domain"
    Add-Check "PROJECT_NAME" ([string]$manifest.projectName -eq $projectName) "manifest=$($manifest.projectName), expected=$projectName"
    Add-Check "SYSTEM_CODE" ($manifestCode -eq $expectedCode -and $manifestCode -match '^[A-Z][A-Z0-9]{2}$') "manifest=$manifestCode, expected=$expectedCode"
    Add-Check "BASE_PACKAGE" ([string]$manifest.basePackage -eq "com.cpf.$domain") "manifest=$($manifest.basePackage)"

    $settingsPath = Join-Path $Root "settings.gradle"
    $settingsText = if (Test-Path -LiteralPath $settingsPath) {
        [System.IO.File]::ReadAllText($settingsPath, [System.Text.Encoding]::UTF8)
    } else { "" }
    $doubleQuotedInclude = 'include "' + $projectName + '"'
    $settingsIncluded = $settingsText.Contains("include '$projectName'") -or
            $settingsText.Contains($doubleQuotedInclude)
    Add-Check "SETTINGS_INCLUDE" $settingsIncluded "settings.gradle include $projectName"

    $packagePath = ([string]$manifest.basePackage).Replace('.', '/')
    Add-Check "PACKAGE_ROOT" (Test-Path -LiteralPath (Join-Path $projectDir "src/main/java/$packagePath") -PathType Container) $packagePath
    $wrongPackages = @(Get-ChildItem -LiteralPath (Join-Path $projectDir "src") -Recurse -File -Filter "*.java" -ErrorAction SilentlyContinue | Where-Object {
        $text = [System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8)
        $text -match '(?m)^package\s+([^;]+);' -and $Matches[1] -notlike "com.cpf.$domain*"
    } | ForEach-Object { $_.FullName.Substring($projectDir.Length + 1).Replace('\', '/') })
    Add-Check "PACKAGE_OWNERSHIP" ($wrongPackages.Count -eq 0) (($wrongPackages | Select-Object -First 10) -join ', ')

    $capabilityRules = @(
        [ordered]@{ name = 'database'; enabled = [bool]$manifest.databaseEnabled; paths = @("sql/Vxx__${domain}_domain.sql"); patterns = @('DataSourceConfig.java', 'MyBatisConfig.java', 'mybatis/mapper') },
        [ordered]@{ name = 'batch'; enabled = [bool]$manifest.batchEnabled; paths = @(); patterns = @('BatchConfig.java', 'BatchRepositoryConfig.java') },
        [ordered]@{ name = 'external'; enabled = [bool]$manifest.externalEnabled; paths = @(); patterns = @('/adapter/remote/') },
        [ordered]@{ name = 'messaging'; enabled = [bool]$manifest.messagingEnabled; paths = @(); patterns = @('/messaging/') },
        [ordered]@{ name = 'file'; enabled = [bool]$manifest.fileEnabled; paths = @(); patterns = @('/file/') },
        [ordered]@{ name = 'securityAudit'; enabled = [bool]$manifest.securityAuditEnabled; paths = @(); patterns = @('/security/') },
        [ordered]@{ name = 'ui'; enabled = [bool]$manifest.uiEnabled; paths = @(); patterns = @('ui/src/') }
    )
    $sourcePaths = @(Get-ChildItem -LiteralPath $projectDir -Recurse -File | ForEach-Object {
        $_.FullName.Substring($projectDir.Length + 1).Replace('\', '/')
    } | Where-Object { $_ -notmatch '^build/' })
    foreach ($rule in $capabilityRules) {
        $matches = @($sourcePaths | Where-Object {
            $candidate = $_
            ($rule.paths -contains $candidate) -or @($rule.patterns | Where-Object { $candidate -like "*$_*" }).Count -gt 0
        })
        if ($rule.enabled) {
            Add-Check "CAPABILITY_$($rule.name.ToUpperInvariant())" ($matches.Count -gt 0) "enabled, files=$($matches.Count)"
        } else {
            Add-Check "CAPABILITY_$($rule.name.ToUpperInvariant())_ABSENT" ($matches.Count -eq 0) "disabled, residue=$($matches -join ', ')"
        }
    }

    if ([bool]$manifest.databaseEnabled) {
        $vendor = ([string]$manifest.databaseVendor).ToLowerInvariant()
        $buildText = [System.IO.File]::ReadAllText((Join-Path $projectDir "build.gradle"), [System.Text.Encoding]::UTF8)
        $vendorMarker = @{
            mariadb = 'org.mariadb.jdbc:mariadb-java-client'
            postgresql = 'org.postgresql:postgresql'
            oracle = 'com.oracle.database.jdbc:ojdbc11'
            sqlserver = 'com.microsoft.sqlserver:mssql-jdbc'
        }[$vendor]
        Add-Check "DATABASE_VENDOR" (-not [string]::IsNullOrWhiteSpace($vendorMarker) -and $buildText.Contains($vendorMarker)) "vendor=$vendor, dependency=$vendorMarker"
        $applicationPath = Join-Path $projectDir "src/main/resources/application-${domain}.yml"
        $applicationText = [System.IO.File]::ReadAllText($applicationPath, [System.Text.Encoding]::UTF8)
        $expectedJndiName = "java:comp/env/jdbc/cpf$([string]$manifest.displayName)DataSource"
        Add-Check "DATASOURCE_JNDI_NAME" $applicationText.Contains($expectedJndiName) "expected=$expectedJndiName"
    }

    $drift = New-Object System.Collections.Generic.List[string]
    foreach ($ownedFile in @($ownership.createdFiles)) {
        $path = Join-Path $projectDir ([string]$ownedFile.path)
        if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
            $drift.Add("missing:$($ownedFile.path)") | Out-Null
            continue
        }
        $currentHash = (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant()
        if ($currentHash -ne ([string]$ownedFile.sha256).ToLowerInvariant()) {
            $drift.Add("changed:$($ownedFile.path)") | Out-Null
        }
    }
    Add-Check "GENERATOR_FILE_INTEGRITY" ($drift.Count -eq 0) (($drift | Select-Object -First 20) -join ', ')

    $emptyDirectories = @(Get-ChildItem -LiteralPath $projectDir -Recurse -Directory | Where-Object {
        $relativeDirectory = $_.FullName.Substring($projectDir.Length + 1).Replace('\', '/')
        $relativeDirectory -notmatch '^build(?:/|$)' -and @(Get-ChildItem -LiteralPath $_.FullName -Force).Count -eq 0
    })
    $placeholderFiles = @($sourcePaths | Where-Object { $_ -match '(?i)(^|/)(placeholder|\.keep)(\.|$)' })
    Add-Check "NO_EMPTY_DIRECTORY" ($emptyDirectories.Count -eq 0) "count=$($emptyDirectories.Count)"
    Add-Check "NO_PLACEHOLDER" ($placeholderFiles.Count -eq 0) (($placeholderFiles | Select-Object -First 10) -join ', ')

    if (-not $SkipBuild) {
        $gradle = if ($IsLinux -or $IsMacOS) { Join-Path $Root "gradlew" } else { Join-Path $Root "gradlew.bat" }
        if (-not (Test-Path -LiteralPath $gradle -PathType Leaf)) {
            Add-Check "GRADLE_WRAPPER" $false $gradle
        } else {
            $result.build.executed = $true
            $arguments = @(
                ":${projectName}:clean", ":${projectName}:test",
                ":${projectName}:bootJar", ":${projectName}:bootWar",
                '--no-daemon', '--console=plain'
            )
            $oldPreference = $ErrorActionPreference
            try {
                $ErrorActionPreference = 'Continue'
                $output = @(& $gradle @arguments 2>&1 | ForEach-Object { $_.ToString() })
                $exitCode = $LASTEXITCODE
            } finally {
                $ErrorActionPreference = $oldPreference
            }
            [System.IO.File]::WriteAllText($buildLogPath, (($output -join "`n") + "`n"), $Utf8NoBom)
            $result.build.exitCode = $exitCode
            $result.build.tasks = $arguments[0..3]
            Add-Check "GRADLE_BUILD" ($exitCode -eq 0) "exitCode=$exitCode"
            $jarCount = @(Get-ChildItem -LiteralPath (Join-Path $projectDir 'build/libs') -File -Filter '*.jar' -ErrorAction SilentlyContinue).Count
            $warCount = @(Get-ChildItem -LiteralPath (Join-Path $projectDir 'build/libs') -File -Filter '*.war' -ErrorAction SilentlyContinue).Count
            Add-Check "PACKAGE_JAR" ($jarCount -gt 0) "count=$jarCount"
            Add-Check "PACKAGE_WAR" ($warCount -gt 0) "count=$warCount"
        }
    }

    if ($failures.Count -eq 0) {
        $result.status = "DONE"
    }
} finally {
    Save-Result
}

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Error $_ }
    exit 1
}

Write-Host "domain verify passed. project=$projectName result=$resultPath"
