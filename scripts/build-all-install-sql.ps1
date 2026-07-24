param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path
)

# Console과 산출물을 UTF-8(no BOM)로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"

$SqlRoot = Join-Path $Root "specs\sql"
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
-- specs/sql/$FileName
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
-- 정본은 specs/sql의 번호별 분리 SQL입니다.
-- 분리 SQL 변경 후 pwsh -File scripts/build-all-install-sql.ps1 로 재생성합니다.
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

    Write-Utf8 (Join-Path $SqlRoot $OutputName) $body
}

# 관리자 권한 단계: Schema와 최소 권한 계정만 생성합니다.
$provisionFiles = @(
    "01_create_databases.sql",
    "02_create_service_users.sql"
)

# Migration 권한 단계: 제품 Object만 생성하며 Seed나 Reset을 포함하지 않습니다.
$emptyInstallFiles = @(
    "10_cpf_schema.sql",
    "20_cmn_schema.sql",
    "30_adm_schema.sql",
    "35_bat_schema.sql",
    "40_business_modules_schema.sql",
    "45_external_schema.sql"
)

# Release와 함께 배포되는 idempotent 제품 기준정보입니다.
$productSeedFiles = @(
    "50_framework_seed_data.sql",
    "52_standard_execution_alias_seed.sql",
    "57_external_seed_data.sql",
    "60_adm_seed_data.sql"
)

# 명시적으로 선택해야 하는 Sample/EDU 및 Test fixture입니다.
$optionalSampleSeedFiles = @("55_cmn_seed_data.sql")
$testSeedFiles = @("70_test_data.sql")
$verifyFiles = @("99_smoke_check.sql")

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
    "사용자가 선택한 CMN Sample/EDU 데이터만 반영" `
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
Write-Host "MariaDB central lifecycle WIP mirror published to cpf-tools/db/vendor/mariadb."
