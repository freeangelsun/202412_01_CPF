param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $ProfilePath = "",
    [string] $ResultDir = "",
    [ValidateSet("profile", "product", "none", "all")]
    [string] $SeedMode = "product",
    [switch] $RequireRun
)

# 기존 자동화 파일명은 호환을 위해 유지하지만 DB lifecycle을 재구현하지 않습니다.
# 정본은 profile 기반 initialize-cpf-database.ps1의
# Provision -> Empty Install -> Seed -> Verify 단일 경로입니다.
if ($PSVersionTable.PSVersion.Major -lt 7) {
    throw "CPF DB 검증은 pwsh 7 이상이 필요합니다."
}

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$Root = (Resolve-Path -LiteralPath $Root).Path
if ([string]::IsNullOrWhiteSpace($ProfilePath)) {
    $ProfilePath = Join-Path $Root "cpf-tools/config/database-install.default.json"
} elseif (-not [IO.Path]::IsPathRooted($ProfilePath)) {
    $ProfilePath = Join-Path $Root $ProfilePath
}
$ProfilePath = [IO.Path]::GetFullPath($ProfilePath)
if (-not (Test-Path -LiteralPath $ProfilePath -PathType Leaf)) {
    throw "DB Profile을 찾을 수 없습니다: $ProfilePath"
}
if ([string]::IsNullOrWhiteSpace($ResultDir)) {
    $ResultDir = Join-Path $Root "build/sql-smoke"
} elseif (-not [IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
$ResultDir = [IO.Path]::GetFullPath($ResultDir)
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$resultPath = Join-Path $ResultDir "mariadb-full-install-result.sanitized.json"
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = "미검증"
    compatibilityEntrypoint = "smoke-mariadb-full-install.ps1"
    canonicalEntrypoint = "cpf-tools/scripts/initialize-cpf-database.ps1"
    lifecycle = @("provision", "empty-install", "seed", "verify")
    profile = $ProfilePath.Substring($Root.Length).TrimStart("\", "/")
    seedMode = $SeedMode
    requireRun = [bool] $RequireRun
    legacyFixedDomainChecks = $false
}

function Save-Result {
    $result.finishedAt = (Get-Date).ToString("o")
    [IO.File]::WriteAllText(
        $resultPath,
        ($result | ConvertTo-Json -Depth 30),
        [Text.UTF8Encoding]::new($false))
}

try {
    $pwsh = (Get-Process -Id $PID).Path
    if ($RequireRun) {
        & $pwsh -NoProfile -ExecutionPolicy Bypass `
            -File (Join-Path $PSScriptRoot "initialize-cpf-database.ps1") `
            -Root $Root `
            -ProfilePath $ProfilePath `
            -ResultDir $ResultDir `
            -All `
            -SeedMode $SeedMode `
            -RequireRun
        if ($LASTEXITCODE -ne 0) {
            throw "Canonical MariaDB Provision/Install/Seed/Verify가 실패했습니다."
        }
        $result.status = "완료"
        $result.canonicalResult = "database-profile-install-result.sanitized.json"
    } else {
        & $pwsh -NoProfile -ExecutionPolicy Bypass `
            -File (Join-Path $PSScriptRoot "check-database-profile-standard.ps1") `
            -Root $Root `
            -ProfilePath $ProfilePath
        if ($LASTEXITCODE -ne 0) {
            throw "DB Profile 정적 Gate가 실패했습니다."
        }
        & $pwsh -NoProfile -ExecutionPolicy Bypass `
            -File (Join-Path $PSScriptRoot "check-official-db-vendor-readiness.ps1") `
            -Root $Root
        if ($LASTEXITCODE -ne 0) {
            throw "Official DB Vendor readiness Gate가 실패했습니다."
        }
        $result.status = "미검증"
        $result.staticValidation = "완료"
        $result.reason = "실제 MariaDB lifecycle은 -RequireRun에서만 실행합니다."
    }
} catch {
    $result.status = "실패"
    $result.errorType = $_.Exception.GetType().Name
    $result.error = $_.Exception.Message
    Save-Result
    throw
}

Save-Result
Write-Host "MariaDB canonical lifecycle wrapper finished. status=$($result.status) result=$resultPath"
