param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$Root = (Resolve-Path -LiteralPath $Root).Path
. (Join-Path $Root "cpf-tools/db/tools/database-profile-common.ps1")
$supportedVendors = @(Get-CpfSupportedDatabaseVendors)
$sandbox = Join-Path $Root "build/domain-db-bootstrap-static"
$sandboxRoot = Join-Path $sandbox "repository"
$allowedCleanupRoot = [IO.Path]::GetFullPath((Join-Path $Root "build"))
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$transientRoots = [Collections.Generic.List[string]]::new()

function Assert-SafeSandboxPath([string] $Path) {
    $resolved = [IO.Path]::GetFullPath($Path)
    if (-not $resolved.StartsWith(
                $allowedCleanupRoot + [IO.Path]::DirectorySeparatorChar,
                [StringComparison]::OrdinalIgnoreCase)) {
        throw "Domain DB static sandbox가 build 경로 밖을 가리킵니다: $resolved"
    }
}

Assert-SafeSandboxPath $sandbox
try {
    if (Test-Path -LiteralPath $sandbox -PathType Container) {
        Remove-Item -LiteralPath $sandbox -Recurse -Force
    }
    New-Item -ItemType Directory -Force -Path $sandboxRoot | Out-Null
    $cli = Join-Path $Root "cpf-tools/runtime/cli/cpf.bat"
    if (-not (Test-Path -LiteralPath $cli -PathType Leaf)) {
        throw "Canonical CPF CLI가 없습니다: $cli"
    }

    $caseIdentities = @(
        [ordered]@{ domain = "qadbma"; code = "QMA" },
        [ordered]@{ domain = "qadbpg"; code = "QPG" },
        [ordered]@{ domain = "qadbor"; code = "QOR" }
    )
    if ($supportedVendors.Count -gt $caseIdentities.Count) {
        throw "Generated Domain DB static case identity가 부족합니다: vendors=$($supportedVendors.Count)"
    }
    $cases = @(
        for ($index = 0; $index -lt $supportedVendors.Count; $index++) {
            [ordered]@{
                domain = $caseIdentities[$index].domain
                code = $caseIdentities[$index].code
                vendor = $supportedVendors[$index]
                onlinePort = 18820 + ($index * 2)
                batchPort = 18821 + ($index * 2)
            }
        }
    )
    foreach ($case in $cases) {
        $definitionDirectory = Join-Path $sandbox "definitions/$($case.domain)"
        New-Item -ItemType Directory -Force -Path $definitionDirectory | Out-Null
        $definitionPath = Join-Path $definitionDirectory "cpf-domain.yaml"
        $definition = @"
domain:
  name: $($case.domain)
  systemCode: $($case.code)
  packageName: $($case.domain)
database:
  role: CUSTOMER_BUSINESS_DB
  tablePrefix: $($case.code)
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
  localOnlinePort: $($case.onlinePort)
generation:
  sampleTransaction: true
"@
        [IO.File]::WriteAllText($definitionPath, $definition.Replace("`r`n", "`n"), $Utf8NoBom)
        $projectDir = Join-Path $sandboxRoot "cpf-$($case.domain)"
        $transientRoot = Join-Path $Root "build/domain-generator/verification/cpf-$($case.domain)"
        $transientRoots.Add($transientRoot)
        & $cli domain generate --file $definitionPath --output $projectDir | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw "Generated Domain static 생성 실패: vendor=$($case.vendor)"
        }
        & $cli verify domain --file $definitionPath --output $projectDir | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw "Generated Domain static 검증 실패: vendor=$($case.vendor)"
        }

        foreach ($forbiddenMetadata in @(".cpf", "cpf-domain.yaml", "manifest/domain-manifest.json", "manifest/generator-ownership.json")) {
            if (Test-Path -LiteralPath (Join-Path $projectDir $forbiddenMetadata)) {
                throw "Generated Project 영구 lifecycle metadata 금지 위반: $forbiddenMetadata"
            }
        }

        $resultDir = Join-Path $sandbox "result-$($case.vendor)"
        & $cli db render --file $definitionPath --vendor $case.vendor --output $resultDir | Out-Null
        if ($LASTEXITCODE -ne 0) {
            throw "Generated Domain DB resource render 실패: vendor=$($case.vendor)"
        }
        $expectedFiles = @(
            "10_empty_install.sql",
            "20_product_seed.sql",
            "V1__$($case.domain)_domain.sql",
            "R1__remove_$($case.domain)_domain.sql",
            "90_verify.sql"
        )
        $actualFiles = @(Get-ChildItem -LiteralPath $resultDir -File | Sort-Object Name)
        if (($actualFiles.Name -join ",") -ne (($expectedFiles | Sort-Object) -join ",")) {
            throw "Generated Domain DB resource set 불일치: vendor=$($case.vendor) actual=$($actualFiles.Name -join ',')"
        }
        $resourceText = ($actualFiles | ForEach-Object {
            Get-Content -LiteralPath $_.FullName -Raw -Encoding UTF8
        }) -join "`n"
        foreach ($requiredToken in @(
                "$($case.code)_sample_item",
                "$($case.code)_sample_item_idem",
                "CUSTOMER_BUSINESS_DB")) {
            if (-not $resourceText.Contains($requiredToken)) {
                throw "Generated Domain DB canonical token 누락: vendor=$($case.vendor) token=$requiredToken"
            }
        }
        if ($resourceText -match "@CPF_[A-Z_]+@" -or
                $resourceText -match '(?i)CREATE\s+(?:DATABASE|USER|ROLE|SCHEMA)') {
            throw "Generated Domain이 미해결 token 또는 physical DB/principal Provision SQL을 포함합니다: vendor=$($case.vendor)"
        }
        Write-Host "Generated Domain DB static render PASS: vendor=$($case.vendor) resources=5 metadata=NONE"
    }
    Write-Host "Generated Domain DB canonical static Gate PASS: vendors=$($supportedVendors.Count) resources=$($supportedVendors.Count * 5)"
} finally {
    foreach ($transientRoot in $transientRoots) {
        if (Test-Path -LiteralPath $transientRoot -PathType Container) {
            Assert-SafeSandboxPath $transientRoot
            Remove-Item -LiteralPath $transientRoot -Recurse -Force
        }
    }
    if (Test-Path -LiteralPath $sandbox -PathType Container) {
        Assert-SafeSandboxPath $sandbox
        Remove-Item -LiteralPath $sandbox -Recurse -Force
    }
}
