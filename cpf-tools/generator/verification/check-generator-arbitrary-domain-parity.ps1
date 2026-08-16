[CmdletBinding()]
param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $ReferenceDomain = 'lending',
    [string] $ReferenceSystemCode = 'LND',
    [string] $CandidateDomain = 'insurance',
    [string] $CandidateSystemCode = 'INS'
)

$ErrorActionPreference = 'Stop'
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
. (Join-Path $Root 'cpf-tools/generator/tools/generated-domain-common.ps1')
$ReferenceDomain = $ReferenceDomain.Trim().ToLowerInvariant()
$CandidateDomain = $CandidateDomain.Trim().ToLowerInvariant()
$ReferenceSystemCode = $ReferenceSystemCode.Trim().ToUpperInvariant()
$CandidateSystemCode = $CandidateSystemCode.Trim().ToUpperInvariant()
foreach ($identity in @(
    @{ domain=$ReferenceDomain; code=$ReferenceSystemCode },
    @{ domain=$CandidateDomain; code=$CandidateSystemCode }
)) {
    if ($identity.domain -notmatch '^[a-z][a-z0-9-]{1,49}$' -or
            $identity.code -notmatch '^[A-Z][A-Z0-9]{2}$') {
        throw "임의 Domain parity identity가 유효하지 않습니다: $($identity.domain)/$($identity.code)"
    }
}
if ($ReferenceDomain -eq $CandidateDomain -or $ReferenceSystemCode -eq $CandidateSystemCode) {
    throw '서로 다른 DomainName/SystemCode를 지정해야 합니다.'
}

$parityGate = Join-Path $Root 'cpf-tools/generator/verification/check-generated-domain-parity.ps1'
$sandbox = Join-Path $Root 'build/domain-generator/arbitrary-domain-parity'
$transientRoots = @(
    (Join-Path $Root "build/domain-generator/verification/cpf-$ReferenceDomain"),
    (Join-Path $Root "build/domain-generator/verification/cpf-$CandidateDomain")
)
function Assert-Safe([string] $Path) {
    $resolved = [IO.Path]::GetFullPath($Path)
    $allowed = [IO.Path]::GetFullPath((Join-Path $Root 'build/domain-generator')).TrimEnd('\', '/') + [IO.Path]::DirectorySeparatorChar
    if (-not $resolved.StartsWith($allowed, [StringComparison]::OrdinalIgnoreCase)) {
        throw "Generator parity sandbox가 허용 경로 밖입니다: $resolved"
    }
}
function New-ParityDefinition([string] $Domain, [string] $Code, [int] $OnlinePort) {
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
  localOnlinePort: $OnlinePort
generation:
  sampleTransaction: true
"@
    [IO.File]::WriteAllText($path, $yaml.Replace("`r`n", "`n"), $Utf8NoBom)
    return $path
}

Assert-Safe $sandbox
try {
    if (Test-Path -LiteralPath $sandbox) { Remove-Item -LiteralPath $sandbox -Recurse -Force }
    New-Item -ItemType Directory -Force -Path $sandbox | Out-Null
    $referenceDefinition = New-ParityDefinition $ReferenceDomain $ReferenceSystemCode 18960
    $candidateDefinition = New-ParityDefinition $CandidateDomain $CandidateSystemCode 18962
    $referenceOutput = Join-Path $sandbox "cpf-$ReferenceDomain"
    $candidateOutput = Join-Path $sandbox "cpf-$CandidateDomain"
    [void](Invoke-CpfCanonicalCli -Root $Root -Arguments @(
        'domain', 'generate', '--file', $referenceDefinition, '--output', $referenceOutput
    ))
    [void](Invoke-CpfCanonicalCli -Root $Root -Arguments @(
        'domain', 'generate', '--file', $candidateDefinition, '--output', $candidateOutput
    ))
    & pwsh -NoProfile -File $parityGate -Root $Root `
        -ReferenceDomain $ReferenceDomain -CandidateDomain $CandidateDomain `
        -ReferenceDefinitionPath $referenceDefinition -CandidateDefinitionPath $candidateDefinition `
        -ReferenceOutputDir $referenceOutput -CandidateOutputDir $candidateOutput
    if ($LASTEXITCODE -ne 0) { throw '두 임의 Generated Domain normalized parity가 실패했습니다.' }
    Write-Host "Arbitrary Generated Domain parity PASS: $ReferenceDomain/$ReferenceSystemCode <-> $CandidateDomain/$CandidateSystemCode"
} finally {
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
