param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path
)

# Console과 산출물을 UTF-8(no BOM)로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"

$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

function Read-Utf8([string] $Path) {
    return [System.IO.File]::ReadAllText($Path, [System.Text.Encoding]::UTF8).TrimEnd()
}

function Write-Utf8([string] $Path, [string] $Text) {
    [System.IO.File]::WriteAllText($Path, $Text.TrimEnd() + [Environment]::NewLine, $Utf8NoBom)
}

function Publish-CentralFile([string] $SourcePath, [string] $TargetPath) {
    if (-not (Test-Path -LiteralPath $SourcePath -PathType Leaf)) {
        throw "Central Vendor Pack source file is missing: $SourcePath"
    }
    $targetDirectory = Split-Path -Parent $TargetPath
    New-Item -ItemType Directory -Force -Path $targetDirectory | Out-Null
    [System.IO.File]::Copy($SourcePath, $TargetPath, $true)
}

function Publish-CentralDirectory([string] $SourceDirectory, [string] $TargetDirectory) {
    if (-not (Test-Path -LiteralPath $SourceDirectory -PathType Container)) {
        throw "Central Vendor Pack source directory is missing: $SourceDirectory"
    }
    foreach ($sourceFile in Get-ChildItem -LiteralPath $SourceDirectory -Recurse -File) {
        $relativePath = [System.IO.Path]::GetRelativePath($SourceDirectory, $sourceFile.FullName)
        Publish-CentralFile $sourceFile.FullName (Join-Path $TargetDirectory $relativePath)
    }
}

function Get-Section([string] $FileName) {
    $path = Join-Path $SqlRoot $FileName
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "SQL source file is missing: $path"
    }
    return @"

-- ============================================================================
-- $($mariaPlan.sourceRoot)/$FileName
-- ============================================================================
$(Read-Utf8 $path)
"@
}

function New-Bundle(
    [string] $OutputName,
    [string] $Purpose,
    [string[]] $Files
) {
    $header = @"
-- CPF generated SQL bundle: $OutputName
-- 목적: $Purpose
-- 정본은 database-source-plan.json의 mariadb.sourceRoot 아래 번호별 분리 SQL입니다.
-- 분리 SQL 변경 후 pwsh -File cpf-tools/scripts/build-all-install-sql.ps1 로 재생성합니다.
"@
    $body = $header
    foreach ($file in $Files) {
        $body += Get-Section $file
    }

    if ($OutputName -notin @("00_provision.sql") -and
            $body -match "(?im)^\s*(?:CREATE|ALTER|DROP)\s+USER\b") {
        throw "Runtime/install bundle must not manage DB users: $OutputName"
    }
    if ($OutputName -notin @("00_test_seed.sql") -and
            $body -match "(?im)^\s*DROP\s+(?:DATABASE|TABLE)\b") {
        throw "Non-destructive bundle contains DROP DATABASE/TABLE: $OutputName"
    }

    if ($OutputName -eq "00_product_seed.sql") {
        if ($body -match "(?i)\b(?:cpf|cmn|adm|bza|bat|mbr|acc|ref|exs)DB\s*\.") {
            throw "Product Seed는 다른 logical DB를 직접 참조할 수 없습니다. Owner별 USE section/source로 분리하세요."
        }
        if ($body -match "(?i)https?://(?:localhost|127\.0\.0\.1)" -or
            $body -match "(?i)'(?:localhost|127\.0\.0\.1)'") {
            throw "Product Seed에 local endpoint/host fixture가 포함되어 있습니다. Optional Seed로 이동하세요."
        }
    }

    Write-Utf8 (Join-Path $SqlRoot $OutputName) $body
}

# Bundle 구성은 database-source-plan.json을 단일 정본으로 사용합니다.
$sourcePlanPath = Join-Path $Root "cpf-tools/config/database-source-plan.json"
if (-not (Test-Path -LiteralPath $sourcePlanPath -PathType Leaf)) {
    throw "DB source plan이 없습니다: $sourcePlanPath"
}
$sourcePlan = Get-Content -LiteralPath $sourcePlanPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 20
$mariaPlan = $sourcePlan.mariadb
if ([string]::IsNullOrWhiteSpace([string]$mariaPlan.sourceRoot)) {
    throw "database-source-plan.json에 mariadb.sourceRoot가 없습니다."
}
$SqlRoot = Join-Path $Root ([string]$mariaPlan.sourceRoot)
if (-not (Test-Path -LiteralPath $SqlRoot -PathType Container)) {
    throw "MariaDB canonical vendor source root가 없습니다: $SqlRoot"
}

$provisionFiles = @($mariaPlan.provisionFiles)
$emptyInstallFiles = @($mariaPlan.emptyInstallFiles)
$productSeedFiles = @($mariaPlan.productSeedFiles)
$optionalSampleSeedFiles = @($mariaPlan.optionalSampleSeedFiles)
$testSeedFiles = @($mariaPlan.testSeedFiles)
$verifyFiles = @($mariaPlan.verifyFiles)

foreach ($forbiddenFixedGeneratedSource in @("45_external_schema.sql", "57_external_seed_data.sql")) {
    if ($forbiddenFixedGeneratedSource -in $emptyInstallFiles -or
        $forbiddenFixedGeneratedSource -in $productSeedFiles -or
        $forbiddenFixedGeneratedSource -in $optionalSampleSeedFiles) {
        throw "Generated Domain 고정 SQL이 source plan에 포함되어 있습니다: $forbiddenFixedGeneratedSource"
    }
}

New-Bundle "00_provision.sql" `
    "관리자 권한으로 Schema와 migration/runtime 최소 권한 계정을 명시적으로 Provision" `
    $provisionFiles
New-Bundle "00_empty_install.sql" `
    "빈 Schema에 제품 Object만 비파괴 설치" `
    $emptyInstallFiles
New-Bundle "00_product_seed.sql" `
    "제품 필수 기준정보만 idempotent 반영" `
    $productSeedFiles
New-Bundle "00_optional_sample_seed.sql" `
    "사용자가 선택한 CMN/REF/로컬 Runtime Sample 데이터만 반영" `
    $optionalSampleSeedFiles
New-Bundle "00_test_seed.sql" `
    "격리된 Test 환경에서만 fixture 반영" `
    $testSeedFiles
New-Bundle "00_verify.sql" `
    "설치 Object와 제품 Seed를 변경 없이 검증" `
    $verifyFiles

# 기존 소비자용 편의 bundle도 Provision/Optional/Test/Reset을 포함하지 않습니다.
New-Bundle "00_all_install.sql" `
    "제품 Object 설치 후 제품 Seed 반영(Provision/Optional/Test/Reset 제외)" `
    ($emptyInstallFiles + $productSeedFiles)
New-Bundle "00_all_install_and_smoke.sql" `
    "제품 Object 설치, 제품 Seed 반영, read-only Verify(Provision/Optional/Test/Reset 제외)" `
    ($emptyInstallFiles + $productSeedFiles + $verifyFiles)

$centralMariaRoot = Join-Path $Root "cpf-tools\db\vendor\mariadb"
$centralLifecycleFiles = [ordered]@{
    "00_provision.sql" = "provision\00_provision.sql"
    "00_empty_install.sql" = "install\00_empty_install.sql"
    "00_product_seed.sql" = "seed\00_product_seed.sql"
    "00_optional_sample_seed.sql" = "seed\00_optional_sample_seed.sql"
    "00_test_seed.sql" = "seed\00_test_seed.sql"
    "00_verify.sql" = "verify\00_verify.sql"
}
foreach ($entry in $centralLifecycleFiles.GetEnumerator()) {
    Publish-CentralFile `
        (Join-Path $SqlRoot $entry.Key) `
        (Join-Path $centralMariaRoot $entry.Value)
}
Publish-CentralDirectory `
    (Join-Path $SqlRoot "migration\flyway") `
    (Join-Path $centralMariaRoot "migration\flyway")
Publish-CentralDirectory `
    (Join-Path $SqlRoot "migration\rollback") `
    (Join-Path $centralMariaRoot "rollback")

Write-Host "CPF SQL bundles rebuilt without implicit reset or test seed."
Write-Host "MariaDB canonical vendor source and lifecycle pack synchronized under cpf-tools/db/vendor/mariadb."
