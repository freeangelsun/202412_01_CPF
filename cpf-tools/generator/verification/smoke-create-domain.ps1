[CmdletBinding()]
param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $ResultDir = '',
    [string] $DomainName = 'lending',
    [string] $SystemCode = 'LND',
    [string] $ModuleName = '',
    [string] $PackageName = '',
    [string] $SchemaName = '',
    [string] $TablePrefix = '',
    [ValidateSet('mariadb', 'postgresql', 'oracle')][string] $DatabaseVendor = 'mariadb',
    [switch] $SkipBuild
)

$ErrorActionPreference = 'Stop'
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
. (Join-Path $Root 'cpf-tools/generator/tools/generated-domain-common.ps1')
$domain = $DomainName.Trim().ToLowerInvariant()
$code = $SystemCode.Trim().ToUpperInvariant()
if ($domain -notmatch '^[a-z][a-z0-9-]{1,49}$') { throw 'DomainName 형식이 올바르지 않습니다.' }
if ($code -notmatch '^[A-Z][A-Z0-9]{2}$') { throw 'SystemCode는 정확히 3자리 대문자/숫자여야 합니다.' }
if ([string]::IsNullOrWhiteSpace($PackageName)) { $PackageName = "$domain" }
if ($PackageName -eq 'com.cpf' -or $PackageName.StartsWith('com.cpf.')) {
    throw 'Generated Customer Domain은 com.cpf.* package를 소유할 수 없습니다.'
}
if ([string]::IsNullOrWhiteSpace($TablePrefix)) { $TablePrefix = $code }
$TablePrefix = $TablePrefix.Trim().ToUpperInvariant()
if (-not [string]::IsNullOrWhiteSpace($ModuleName)) {
    Write-Warning 'ModuleName은 canonical 입력에서 제거되었습니다. domain.name이 Module identity를 결정합니다.'
}
if (-not [string]::IsNullOrWhiteSpace($SchemaName)) {
    Write-Warning 'SchemaName은 Generated Source 입력이 아니라 DB Runtime binding입니다.'
}

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root 'build/runtime-smoke'
} elseif (-not [IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir 'create-domain-result.sanitized.json'
$sandbox = Join-Path $Root "build/domain-generator/smoke-create-$domain"
$definitionDir = Join-Path $sandbox 'definition'
$definitionPath = Join-Path $definitionDir 'cpf-domain.yaml'
$project = Join-Path $sandbox "cpf-$domain"
$dbOutput = Join-Path $sandbox "db3/$DatabaseVendor"
$transient = Join-Path $Root "build/domain-generator/verification/cpf-$domain"

function Assert-SafeSandbox([string] $Path) {
    $resolved = [IO.Path]::GetFullPath($Path)
    $allowed = [IO.Path]::GetFullPath((Join-Path $Root 'build/domain-generator')).TrimEnd('\', '/') + [IO.Path]::DirectorySeparatorChar
    if (-not $resolved.StartsWith($allowed, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Generator smoke sandbox가 허용 경로 밖입니다: $resolved"
    }
}
Assert-SafeSandbox $sandbox
if (Test-Path -LiteralPath $sandbox) { Remove-Item -LiteralPath $sandbox -Recurse -Force }
New-Item -ItemType Directory -Force -Path $definitionDir | Out-Null
$definition = @"
domain:
  name: $domain
  systemCode: $code
  packageName: $PackageName
database:
  role: CUSTOMER_BUSINESS_DB
  tablePrefix: $TablePrefix
preset: standard-enterprise
modules:
  online: true
features:
  persistence: mybatis
  httpClient: true
  resilience: true
  cache: none
  messaging: none
runtime:
  localOnlinePort: 18860
generation:
  sampleTransaction: true
"@
[IO.File]::WriteAllText($definitionPath, $definition.Replace("`r`n", "`n"), $Utf8NoBom)

$result = [ordered]@{
    startedAt = [DateTimeOffset]::Now.ToString('o')
    status = 'FAILED'
    domainName = $domain
    systemCode = $code
    definitionPath = $definitionPath
    generatedProjectMetadata = 'NONE'
    databaseVendor = $DatabaseVendor
    build = [ordered]@{ executed = $false; exitCode = $null }
}
try {
    $result.dryRun = Invoke-CpfCanonicalCli -Root $Root -Arguments @(
        'domain', 'dry-run', '--file', $definitionPath, '--output', $project
    )
    $result.generate = Invoke-CpfCanonicalCli -Root $Root -Arguments @(
        'domain', 'generate', '--file', $definitionPath, '--output', $project
    )
    $result.verify = Invoke-CpfCanonicalCli -Root $Root -Arguments @(
        'verify', 'domain', '--file', $definitionPath, '--output', $project
    )
    $result.idempotent = Invoke-CpfCanonicalCli -Root $Root -Arguments @(
        'domain', 'generate', '--file', $definitionPath, '--output', $project
    )
    $result.diff = Invoke-CpfCanonicalCli -Root $Root -Arguments @(
        'domain', 'diff', '--file', $definitionPath, '--output', $project
    )
    $result.database = Invoke-CpfCanonicalCli -Root $Root -Arguments @(
        'db', 'render', '--file', $definitionPath, '--vendor', $DatabaseVendor, '--output', $dbOutput
    )
    if (-not [bool]$result.diff.clean) { throw 'Idempotent generate 후 canonical diff가 clean이 아닙니다.' }
    foreach ($forbidden in @('.cpf', 'cpf-domain.yaml', 'generator.lock', 'manifest/domain-manifest.json', 'manifest/generator-ownership.json')) {
        if (Test-Path -LiteralPath (Join-Path $project $forbidden)) {
            throw "Generated Project 영구 metadata 금지 위반: $forbidden"
        }
    }
    $dbFiles = @(Get-ChildItem -LiteralPath $dbOutput -File | Sort-Object Name)
    if ($dbFiles.Count -ne 5) { throw "Generated Domain DB3 resource 수가 5가 아닙니다: $($dbFiles.Count)" }
    if (-not $SkipBuild) {
        $gradle = if ($IsLinux -or $IsMacOS) { Join-Path $Root 'gradlew' } else { Join-Path $Root 'gradlew.bat' }
        $result.build.executed = $true
        $oldPreference = $ErrorActionPreference
        try {
            $ErrorActionPreference = 'Continue'
            $buildOutput = @(& $gradle -p $project test assemble --no-daemon --console=plain 2>&1 |
                ForEach-Object { $_.ToString() })
            $result.build.exitCode = $LASTEXITCODE
        } finally {
            $ErrorActionPreference = $oldPreference
        }
        $buildLog = Join-Path $ResultDir 'create-domain-build.sanitized.log'
        [IO.File]::WriteAllText($buildLog, ($buildOutput -join "`n") + "`n", $Utf8NoBom)
        $result.build.logPath = $buildLog
        if ($result.build.exitCode -ne 0) { throw "Generated Domain build 실패: exitCode=$($result.build.exitCode)" }
    }
    $result.status = 'DONE'
} catch {
    $result.failure = $_.Exception.Message
    throw
} finally {
    $result.finishedAt = [DateTimeOffset]::Now.ToString('o')
    [IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 100) + [Environment]::NewLine, $Utf8NoBom)
    if (Test-Path -LiteralPath $sandbox) {
        Assert-SafeSandbox $sandbox
        Remove-Item -LiteralPath $sandbox -Recurse -Force
    }
    if (Test-Path -LiteralPath $transient) {
        Assert-SafeSandbox $transient
        Remove-Item -LiteralPath $transient -Recurse -Force
    }
}
Write-Host "create-domain smoke PASS. domain=$domain result=$resultPath"
