[CmdletBinding()]
param(
    [string] $DomainName = '',
    [Alias('ExpectedSystemCode')][string] $SystemCode = '',
    [string] $ModuleCode = '',
    [string] $DefinitionPath = '',
    [string] $OutputDir = '',
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $ResultDir = '',
    [switch] $DryRun,
    [switch] $PurgeDefinition
)

# Compatibility surface. Removal ownership is calculated by the canonical Engine from
# cpf-domain.yaml plus transient generation-state (or the deterministic current Template).
$ErrorActionPreference = 'Stop'
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
. (Join-Path $Root 'cpf-tools/generator/tools/generated-domain-common.ps1')

$requested = if ([string]::IsNullOrWhiteSpace($DomainName)) { $ModuleCode } else { $DomainName }
$domain = $requested.Trim().ToLowerInvariant()
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

$arguments = @('domain', 'remove', $domain, '--file', [string]$metadata.contractPath)
if (-not [string]::IsNullOrWhiteSpace($OutputDir)) {
    if (-not [IO.Path]::IsPathRooted($OutputDir)) { $OutputDir = Join-Path $Root $OutputDir }
    $arguments += @('--output', ([IO.Path]::GetFullPath($OutputDir)))
}
if (-not $DryRun) { $arguments += '--apply' }
if ($PurgeDefinition) {
    if ($DryRun) { throw '-PurgeDefinition은 실제 제거에서만 사용할 수 있습니다.' }
    $arguments += '--purge-definition'
}
$result = Invoke-CpfCanonicalCli -Root $Root -Arguments $arguments

if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "cpf-docs/work/evidence/generated/domain-generator/reports/remove-domain/$domain"
} elseif (-not [IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$resultPath = Join-Path $ResultDir 'remove-domain-result.json'
$evidence = [ordered]@{
    generatedAt = [DateTimeOffset]::Now.ToString('o')
    status = [string]$result.status
    dryRun = [bool]$DryRun
    domainName = $domain
    systemCode = $expectedCode
    contractPath = [string]$metadata.contractPath
    generatedProjectMetadata = 'ABSENT'
    ownershipSource = [string]$result.ownershipSource
    result = $result
}
[IO.File]::WriteAllText(
    $resultPath,
    ($evidence | ConvertTo-Json -Depth 100) + [Environment]::NewLine,
    $Utf8NoBom)
Write-Host "remove-domain $($result.status). domain=$domain result=$resultPath"
