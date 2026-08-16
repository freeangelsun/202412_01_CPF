[CmdletBinding()]
param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $ResultDir = '',
    [switch] $SkipBuild
)

# Current schema combinations are exercised through one canonical Engine. This gate does not
# carry a fixed supported-domain list; case names are disposable arbitrary-domain fixtures.
$ErrorActionPreference = 'Stop'
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
. (Join-Path $Root 'cpf-tools/generator/tools/generated-domain-common.ps1')
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root 'build/runtime-smoke'
} elseif (-not [IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir 'domain-capability-matrix.sanitized.json'
$sandbox = Join-Path $Root 'build/domain-generator/capability-matrix'
$transientRoots = [Collections.Generic.List[string]]::new()
$cases = @(
    [ordered]@{ domain='matrixminimal'; code='MXM'; preset='minimal'; persistence='none'; sample=$false; http=$false; resilience=$false; cache='none'; messaging='none'; objectStorage='none'; security='resource-server' },
    [ordered]@{ domain='matrixcustom'; code='MXC'; preset='custom'; persistence='mybatis'; sample=$true; http=$false; resilience=$false; cache='none'; messaging='none'; objectStorage='none'; security='service-identity' },
    [ordered]@{ domain='matrixstandard'; code='MXS'; preset='standard-enterprise'; persistence='mybatis'; sample=$true; http=$true; resilience=$true; cache='none'; messaging='none'; objectStorage='none'; security='resource-server' },
    [ordered]@{ domain='matrixfull'; code='MXF'; preset='full-enterprise'; persistence='mybatis'; sample=$true; http=$true; resilience=$true; cache='valkey'; messaging='kafka'; objectStorage='s3'; security='browser-session-valkey' }
)
$vendors = @('mariadb', 'postgresql', 'oracle')

function Bool-Text([bool] $Value) { if ($Value) { return 'true' }; return 'false' }
function Assert-SafeSandbox([string] $Path) {
    $resolved = [IO.Path]::GetFullPath($Path)
    $allowed = [IO.Path]::GetFullPath((Join-Path $Root 'build/domain-generator')).TrimEnd('\', '/') + [IO.Path]::DirectorySeparatorChar
    if (-not $resolved.StartsWith($allowed, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Capability sandbox가 허용 경로 밖입니다: $resolved"
    }
}
Assert-SafeSandbox $sandbox
if (Test-Path -LiteralPath $sandbox) { Remove-Item -LiteralPath $sandbox -Recurse -Force }
New-Item -ItemType Directory -Force -Path $sandbox | Out-Null

$result = [ordered]@{
    startedAt = [DateTimeOffset]::Now.ToString('o')
    status = 'FAILED'
    generatedProjectMetadata = 'NONE'
    combinations = @()
    build = [ordered]@{ executed = $false; exitCode = $null }
}
try {
    $index = 0
    foreach ($case in $cases) {
        $definitionDir = Join-Path $sandbox "definitions/$($case.domain)"
        $definitionPath = Join-Path $definitionDir 'cpf-domain.yaml'
        $project = Join-Path $sandbox "cpf-$($case.domain)"
        New-Item -ItemType Directory -Force -Path $definitionDir | Out-Null
        $sample = Bool-Text $case.sample
        $http = Bool-Text $case.http
        $resilience = Bool-Text $case.resilience
        $onlinePort = 18720 + ($index * 2)
        $definition = @"
domain:
  name: $($case.domain)
  systemCode: $($case.code)
  packageName: $($case.domain)
database:
  role: CUSTOMER_BUSINESS_DB
  tablePrefix: $($case.code)
preset: $($case.preset)
modules:
  online: true
features:
  persistence: $($case.persistence)
  httpClient: $http
  resilience: $resilience
  cache: $($case.cache)
  messaging: $($case.messaging)
  objectStorage: $($case.objectStorage)
  securityProfile: $($case.security)
runtime:
  localOnlinePort: $onlinePort
generation:
  sampleTransaction: $sample
"@
        [IO.File]::WriteAllText($definitionPath, $definition.Replace("`r`n", "`n"), $Utf8NoBom)
        $transientRoots.Add((Join-Path $Root "build/domain-generator/verification/cpf-$($case.domain)")) | Out-Null
        $dryRun = Invoke-CpfCanonicalCli -Root $Root -Arguments @(
            'domain', 'dry-run', '--file', $definitionPath, '--output', $project
        )
        $generate = Invoke-CpfCanonicalCli -Root $Root -Arguments @(
            'domain', 'generate', '--file', $definitionPath, '--output', $project
        )
        $verify = Invoke-CpfCanonicalCli -Root $Root -Arguments @(
            'verify', 'domain', '--file', $definitionPath, '--output', $project
        )
        foreach ($forbidden in @('.cpf', 'cpf-domain.yaml', 'generator.lock', 'manifest/domain-manifest.json', 'manifest/generator-ownership.json')) {
            if (Test-Path -LiteralPath (Join-Path $project $forbidden)) {
                throw "Generated Project 영구 metadata 금지 위반: domain=$($case.domain) path=$forbidden"
            }
        }
        if (-not (Test-Path -LiteralPath (Join-Path $project 'online') -PathType Container)) {
            throw "online module 누락: $($case.domain)"
        }
        foreach ($forbiddenModule in @('batch','domain','jobpack')) {
            if (Test-Path -LiteralPath (Join-Path $project $forbiddenModule)) {
                throw "Generated Domain에 금지된 module이 생성되었습니다: domain=$($case.domain) module=$forbiddenModule"
            }
        }
        $dbResults = @()
        if ($case.sample) {
            foreach ($vendor in $vendors) {
                $dbOutput = Join-Path $sandbox "db3/$($case.domain)/$vendor"
                $dbResult = Invoke-CpfCanonicalCli -Root $Root -Arguments @(
                    'db', 'render', '--file', $definitionPath, '--vendor', $vendor, '--output', $dbOutput
                )
                if (@(Get-ChildItem -LiteralPath $dbOutput -File).Count -ne 5) {
                    throw "DB3 resource 수 불일치: domain=$($case.domain) vendor=$vendor"
                }
                $dbResults += $dbResult
            }
        }
        $result.combinations += [ordered]@{
            domainName = $case.domain
            systemCode = $case.code
            preset = $case.preset
            dryRunStatus = [string]$dryRun.status
            generateStatus = [string]$generate.status
            verifyStatus = [string]$verify.status
            databaseVendors = $dbResults
        }
        $index++
    }

    if (-not $SkipBuild) {
        $representative = Join-Path $sandbox 'cpf-matrixstandard'
        $gradle = if ($IsLinux -or $IsMacOS) { Join-Path $Root 'gradlew' } else { Join-Path $Root 'gradlew.bat' }
        $result.build.executed = $true
        $oldPreference = $ErrorActionPreference
        try {
            $ErrorActionPreference = 'Continue'
            $buildOutput = @(& $gradle -p $representative test assemble --no-daemon --console=plain 2>&1 |
                ForEach-Object { $_.ToString() })
            $result.build.exitCode = $LASTEXITCODE
        } finally {
            $ErrorActionPreference = $oldPreference
        }
        $buildLog = Join-Path $ResultDir 'domain-capability-matrix-build.sanitized.log'
        [IO.File]::WriteAllText($buildLog, ($buildOutput -join "`n") + "`n", $Utf8NoBom)
        $result.build.logPath = $buildLog
        if ($result.build.exitCode -ne 0) { throw "Capability 대표 build 실패: exitCode=$($result.build.exitCode)" }
    }
    $result.status = 'DONE'
} catch {
    $result.failure = $_.Exception.Message
    throw
} finally {
    $result.finishedAt = [DateTimeOffset]::Now.ToString('o')
    [IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 100) + [Environment]::NewLine, $Utf8NoBom)
    foreach ($transient in $transientRoots) {
        if (Test-Path -LiteralPath $transient) {
            Assert-SafeSandbox $transient
            Remove-Item -LiteralPath $transient -Recurse -Force
        }
    }
    if (Test-Path -LiteralPath $sandbox) {
        Assert-SafeSandbox $sandbox
        Remove-Item -LiteralPath $sandbox -Recurse -Force
    }
}
Write-Host "Generated Domain capability matrix PASS. combinations=$($result.combinations.Count) result=$resultPath"
