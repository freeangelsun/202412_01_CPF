[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)][string] $DomainName,
    [string] $SystemCode = '',
    [string] $DefinitionPath = '',
    [string] $OutputDir = '',
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $ResultDir = '',
    [switch] $SkipBuild
)

# Generated Project에는 영구 manifest/ownership을 두지 않는다. Canonical definition과
# 동일 Engine의 verify entrypoint가 IA, Source, DB3 및 permanent-metadata 부재를 검증한다.
$ErrorActionPreference = 'Stop'
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
. (Join-Path $Root 'cpf-tools/generator/tools/generated-domain-common.ps1')

$domain = $DomainName.Trim().ToLowerInvariant()
if ($domain -notmatch '^[a-z][a-z0-9-]{1,49}$') {
    throw 'DomainName은 영문자로 시작하는 2~50자리 소문자·숫자·하이픈이어야 합니다.'
}
$metadata = Get-CpfGeneratedDomainDefinition `
    -Root $Root `
    -DomainName $domain `
    -DefinitionPath $DefinitionPath `
    -IncludeMissing
$expectedCode = ([string]$metadata.systemCode).ToUpperInvariant()
if (-not [string]::IsNullOrWhiteSpace($SystemCode) -and
        $SystemCode.Trim().ToUpperInvariant() -ne $expectedCode) {
    throw "요청 SystemCode와 canonical definition이 다릅니다: requested=$SystemCode canonical=$expectedCode"
}
if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $OutputDir = Join-Path $Root ([string]$metadata.projectPath)
} elseif (-not [IO.Path]::IsPathRooted($OutputDir)) {
    $OutputDir = Join-Path $Root $OutputDir
}
$OutputDir = [IO.Path]::GetFullPath($OutputDir)
if (-not (Test-Path -LiteralPath $OutputDir -PathType Container)) {
    throw "Generated Domain output이 없습니다: $OutputDir"
}

$verify = Invoke-CpfCanonicalCli -Root $Root -Arguments @(
    'verify', 'domain', '--file', [string]$metadata.definitionPath, '--output', $OutputDir
)

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/reports/verify-domain/$domain"
} elseif (-not [IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$buildLogPath = Join-Path $ResultDir 'verify-domain-build.sanitized.log'
$build = [ordered]@{ executed = $false; exitCode = $null; logPath = $buildLogPath }
if (-not $SkipBuild) {
    $gradle = if ($IsLinux -or $IsMacOS) {
        Join-Path $Root 'gradlew'
    } else {
        Join-Path $Root 'gradlew.bat'
    }
    if (-not (Test-Path -LiteralPath $gradle -PathType Leaf)) {
        throw "Gradle wrapper가 없습니다: $gradle"
    }
    $build.executed = $true
    $oldPreference = $ErrorActionPreference
    try {
        $ErrorActionPreference = 'Continue'
        $output = @(& $gradle -p $OutputDir clean test assemble --no-daemon --console=plain 2>&1 |
            ForEach-Object { $_.ToString() })
        $build.exitCode = $LASTEXITCODE
    } finally {
        $ErrorActionPreference = $oldPreference
    }
    [IO.File]::WriteAllText($buildLogPath, ($output -join "`n") + "`n", $Utf8NoBom)
    if ($build.exitCode -ne 0) {
        throw "Generated Domain build가 실패했습니다: domain=$domain exitCode=$($build.exitCode) log=$buildLogPath"
    }
}

$result = [ordered]@{
    startedAt = [DateTimeOffset]::Now.ToString('o')
    finishedAt = [DateTimeOffset]::Now.ToString('o')
    status = 'DONE'
    domainName = $domain
    systemCode = $expectedCode
    projectName = [string]$metadata.projectName
    definitionPath = [string]$metadata.definitionPath
    definitionSha256 = [string]$metadata.definitionSha256
    generatorVersion = [string]$metadata.generatorVersion
    generatedProjectMetadata = 'NONE'
    verification = $verify
    build = $build
}
$resultPath = Join-Path $ResultDir 'verify-domain-result.json'
[IO.File]::WriteAllText(
    $resultPath,
    ($result | ConvertTo-Json -Depth 100) + [Environment]::NewLine,
    $Utf8NoBom)
Write-Host "domain verify passed. project=$($metadata.projectName) result=$resultPath"
