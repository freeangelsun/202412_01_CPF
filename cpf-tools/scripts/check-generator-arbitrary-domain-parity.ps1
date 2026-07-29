param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $ReferenceDomain = "lending",
    [string] $ReferenceSystemCode = "LND",
    [string] $CandidateDomain = "insurance",
    [string] $CandidateSystemCode = "INS"
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$Root = (Resolve-Path -LiteralPath $Root).Path

foreach ($identity in @(
        [ordered]@{ domain = $ReferenceDomain; code = $ReferenceSystemCode },
        [ordered]@{ domain = $CandidateDomain; code = $CandidateSystemCode })) {
    $identity.domain = ([string]$identity.domain).Trim().ToLowerInvariant()
    $identity.code = ([string]$identity.code).Trim().ToUpperInvariant()
    if ($identity.domain -notmatch '^[a-z][a-z0-9]{1,29}$' -or
            $identity.code -notmatch '^[A-Z][A-Z0-9]{2}$') {
        throw "임의 Domain parity identity가 유효하지 않습니다: $($identity.domain)/$($identity.code)"
    }
}
$ReferenceDomain = $ReferenceDomain.Trim().ToLowerInvariant()
$ReferenceSystemCode = $ReferenceSystemCode.Trim().ToUpperInvariant()
$CandidateDomain = $CandidateDomain.Trim().ToLowerInvariant()
$CandidateSystemCode = $CandidateSystemCode.Trim().ToUpperInvariant()
if ($ReferenceDomain.Equals($CandidateDomain, [StringComparison]::OrdinalIgnoreCase) -or
        $ReferenceSystemCode.Equals($CandidateSystemCode, [StringComparison]::OrdinalIgnoreCase)) {
    throw "서로 다른 DomainName/SystemCode를 지정해야 합니다."
}

$generator = Join-Path $Root "cpf-tools/generator/create-domain.ps1"
$parityGate = Join-Path $Root "cpf-tools/scripts/check-generated-domain-parity.ps1"
$contractRoot = Join-Path $Root "cpf-tools/generator/contracts"
$vendorRoot = Join-Path $Root "cpf-tools/db/vendor"
foreach ($requiredPath in @($generator, $parityGate, $contractRoot, $vendorRoot)) {
    if (-not (Test-Path -LiteralPath $requiredPath)) {
        throw "임의 Domain parity 필수 경로가 없습니다: $requiredPath"
    }
}

$sandbox = Join-Path $Root "build/generator-arbitrary-domain-parity"
$sandboxRoot = Join-Path $sandbox "repository"
$allowedCleanupRoot = [IO.Path]::GetFullPath((Join-Path $Root "build"))

function Assert-SafeSandboxPath([string] $Path) {
    $resolved = [IO.Path]::GetFullPath($Path)
    if (-not $resolved.StartsWith(
                $allowedCleanupRoot + [IO.Path]::DirectorySeparatorChar,
                [StringComparison]::OrdinalIgnoreCase)) {
        throw "Generator parity sandbox가 build 경로 밖을 가리킵니다: $resolved"
    }
}

function Invoke-Generator(
    [string] $Domain,
    [string] $Code,
    [int] $Port
) {
    & pwsh -NoProfile -ExecutionPolicy Bypass -File $generator `
        -Root $sandboxRoot `
        -DomainName $Domain `
        -SystemCode $Code `
        -Port $Port `
        -Capabilities "database local-call" `
        -Apply | Out-Null
    if ($LASTEXITCODE -ne 0) {
        throw "임의 Generated Domain 생성 실패: $Domain/$Code"
    }
}

Assert-SafeSandboxPath $sandbox
try {
    if (Test-Path -LiteralPath $sandbox -PathType Container) {
        Remove-Item -LiteralPath $sandbox -Recurse -Force
    }
    New-Item -ItemType Directory -Force -Path $sandboxRoot | Out-Null
    [IO.File]::WriteAllText(
            (Join-Path $sandboxRoot "settings.gradle"),
            "rootProject.name = 'cpf-generator-arbitrary-domain-parity'`n",
            [Text.UTF8Encoding]::new($false))

    $sandboxGradleRoot = Join-Path $sandboxRoot "gradle"
    New-Item -ItemType Directory -Force -Path $sandboxGradleRoot | Out-Null
    Copy-Item -LiteralPath (Join-Path $Root "gradle/cpf-stack.properties") `
        -Destination (Join-Path $sandboxGradleRoot "cpf-stack.properties")

    $sandboxContractRoot = Join-Path $sandboxRoot "cpf-tools/generator/contracts"
    New-Item -ItemType Directory -Force -Path $sandboxContractRoot | Out-Null
    Copy-Item -LiteralPath (Join-Path $contractRoot "central-domain-template-contract.json") `
        -Destination $sandboxContractRoot
    Copy-Item -LiteralPath (Join-Path $contractRoot "domain-metadata.schema.json") `
        -Destination $sandboxContractRoot

    # CPF 공식 지원 DB Vendor 3종만 Golden Template parity 대상으로 사용합니다.
    foreach ($vendor in @("mariadb", "postgresql", "oracle")) {
        $targetVendorRoot = Join-Path $sandboxRoot "cpf-tools/db/vendor/$vendor"
        New-Item -ItemType Directory -Force -Path $targetVendorRoot | Out-Null
        Copy-Item -LiteralPath (Join-Path $vendorRoot "$vendor/domain-template") `
            -Destination $targetVendorRoot `
            -Recurse
    }

    Invoke-Generator $ReferenceDomain $ReferenceSystemCode 18981
    Invoke-Generator $CandidateDomain $CandidateSystemCode 18982

    & pwsh -NoProfile -ExecutionPolicy Bypass -File $parityGate `
        -Root $sandboxRoot `
        -ReferenceDomain $ReferenceDomain `
        -CandidateDomain $CandidateDomain | Out-Host
    if ($LASTEXITCODE -ne 0) {
        throw "두 임의 Generated Domain normalized parity가 실패했습니다."
    }

    Write-Host "Arbitrary Generated Domain parity PASS: $ReferenceDomain/$ReferenceSystemCode <-> $CandidateDomain/$CandidateSystemCode"
} finally {
    if (Test-Path -LiteralPath $sandbox -PathType Container) {
        Assert-SafeSandboxPath $sandbox
        Remove-Item -LiteralPath $sandbox -Recurse -Force
    }
}
