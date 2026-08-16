[CmdletBinding()]
param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $ResultDir = ''
)

$ErrorActionPreference = 'Stop'
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
. (Join-Path $Root 'cpf-tools/generator/tools/generated-domain-common.ps1')
$remove = Join-Path $Root 'cpf-tools/generator/tools/remove-domain.ps1'
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root 'build/runtime-smoke'
} elseif (-not [IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir 'remove-domain-smoke.sanitized.json'
$sandbox = Join-Path $Root 'build/domain-generator/remove-domain-smoke'
$transientRoots = @(
    (Join-Path $Root 'build/domain-generator/verification/cpf-removeguard'),
    (Join-Path $Root 'build/domain-generator/verification/cpf-removeclean')
)
function Assert-Safe([string] $Path) {
    $resolved = [IO.Path]::GetFullPath($Path)
    $allowed = [IO.Path]::GetFullPath((Join-Path $Root 'build/domain-generator')).TrimEnd('\', '/') + [IO.Path]::DirectorySeparatorChar
    if (-not $resolved.StartsWith($allowed, [StringComparison]::OrdinalIgnoreCase)) {
        throw "remove smoke path가 허용 경로 밖입니다: $resolved"
    }
}
Assert-Safe $sandbox
if (Test-Path -LiteralPath $sandbox) { Remove-Item -LiteralPath $sandbox -Recurse -Force }
New-Item -ItemType Directory -Force -Path $sandbox | Out-Null

function New-Definition([string] $Domain, [string] $Code, [int] $Port) {
    $directory = Join-Path $sandbox "definitions/$Domain"
    New-Item -ItemType Directory -Force -Path $directory | Out-Null
    $path = Join-Path $directory 'cpf-domain.yaml'
    $yaml = @"
domain:
  name: $Domain
  systemCode: $Code
  packageName: $Domain
database:
  role: CUSTOMER_BUSINESS_DB
  tablePrefix: $Code
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
  localOnlinePort: $Port
generation:
  sampleTransaction: true
"@
    [IO.File]::WriteAllText($path, $yaml.Replace("`r`n", "`n"), $Utf8NoBom)
    return $path
}

$result = [ordered]@{
    startedAt = [DateTimeOffset]::Now.ToString('o')
    status = 'FAILED'
    generatedProjectMetadata = 'NONE'
    cleanDryRun = $null
    changedFileBlocked = $false
    actualRemove = $null
}
try {
    $guardDefinition = New-Definition 'removeguard' 'RMG' 18580
    $guardProject = Join-Path $sandbox 'cpf-removeguard'
    [void](Invoke-CpfCanonicalCli -Root $Root -Arguments @(
        'domain', 'generate', '--file', $guardDefinition, '--output', $guardProject
    ))
    & $remove -Root $Root -DomainName removeguard -SystemCode RMG -DefinitionPath $guardDefinition `
        -OutputDir $guardProject -DryRun -ResultDir (Join-Path $sandbox 'reports/removeguard')
    $result.cleanDryRun = Get-Content -LiteralPath (Join-Path $sandbox 'reports/removeguard/remove-domain-result.json') `
        -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 100
    $ownedFile = Get-ChildItem -LiteralPath $guardProject -Recurse -File -Filter '*.java' | Select-Object -First 1
    [IO.File]::AppendAllText($ownedFile.FullName, "`n// user modification guard`n", $Utf8NoBom)
    $blocked = $false
    try {
        & $remove -Root $Root -DomainName removeguard -SystemCode RMG -DefinitionPath $guardDefinition `
            -OutputDir $guardProject -DryRun -ResultDir (Join-Path $sandbox 'reports/removeguard-modified')
    } catch {
        $blocked = $true
    }
    if (-not $blocked) { throw '사용자 변경 Source remove가 차단되지 않았습니다.' }
    $result.changedFileBlocked = $true

    $cleanDefinition = New-Definition 'removeclean' 'RMC' 18582
    $cleanProject = Join-Path $sandbox 'cpf-removeclean'
    [void](Invoke-CpfCanonicalCli -Root $Root -Arguments @(
        'domain', 'generate', '--file', $cleanDefinition, '--output', $cleanProject
    ))
    & $remove -Root $Root -DomainName removeclean -SystemCode RMC -DefinitionPath $cleanDefinition `
        -OutputDir $cleanProject -ResultDir (Join-Path $sandbox 'reports/removeclean')
    $result.actualRemove = Get-Content -LiteralPath (Join-Path $sandbox 'reports/removeclean/remove-domain-result.json') `
        -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 100
    if (Test-Path -LiteralPath $cleanProject) { throw '실제 제거 후 Generated Project가 남았습니다.' }
    $result.status = 'DONE'
} catch {
    $result.failure = $_.Exception.Message
    throw
} finally {
    $result.finishedAt = [DateTimeOffset]::Now.ToString('o')
    [IO.File]::WriteAllText($resultPath, ($result | ConvertTo-Json -Depth 100) + [Environment]::NewLine, $Utf8NoBom)
    if (Test-Path -LiteralPath $sandbox) {
        Assert-Safe $sandbox
        Remove-Item -LiteralPath $sandbox -Recurse -Force
    }
    foreach ($transient in $transientRoots) {
        if (Test-Path -LiteralPath $transient) {
            Assert-Safe $transient
            Remove-Item -LiteralPath $transient -Recurse -Force
        }
    }
}
Write-Host "remove-domain smoke PASS. evidence=$resultPath"
