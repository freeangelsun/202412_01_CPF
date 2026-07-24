param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "build/quality-gate")
)

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"

$Root = (Resolve-Path -LiteralPath $Root).Path
if (-not [System.IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$failures = [System.Collections.Generic.List[object]]::new()

function Add-Failure([string] $Rule, [string] $Path, [string] $Detail) {
    $failures.Add([ordered]@{ rule = $Rule; path = $Path; detail = $Detail }) | Out-Null
}

$tracked = @(& git -C $Root ls-files)
if ($LASTEXITCODE -ne 0) {
    throw "git 추적 파일 목록을 읽지 못했습니다."
}

$forbiddenPrefixes = @(
    "cpf-docs/evidence/20260722_01/",
    "cpf-core/src/main/resources/sql/vendor/",
    "cpf-biz-admin/src/main/resources/sql/vendor/",
    "cpf-core/src/main/resources/mybatis/vendor/",
    "cpf-common/src/main/resources/mybatis/vendor/",
    "cpf-member/src/main/resources/mybatis/vendor/",
    "cpf-account/src/main/resources/mybatis/vendor/",
    "cpf-reference/src/main/resources/mybatis/vendor/"
)

foreach ($path in $tracked) {
    $normalized = $path.Replace('\', '/')

    foreach ($prefix in $forbiddenPrefixes) {
        if ($normalized.StartsWith($prefix, [System.StringComparison]::OrdinalIgnoreCase)) {
            Add-Failure "FORBIDDEN_TRANSITIONAL_RESOURCE" $normalized `
                "Central Vendor Pack/최신 Evidence 정본과 충돌하는 삭제 확정 경로입니다."
        }
    }

    if ($normalized -notmatch '/src/(main|test)/' -and
            $normalized -notmatch '^cpf-docs/evidence/' -and
            $normalized -match '(^|/)(build|bin|out|target|logs?|tmp|temp|work)/') {
        Add-Failure "TRACKED_RUNTIME_ARTIFACT" $normalized `
            "build·로그·임시 산출물은 제품 Source로 추적하지 않습니다."
    }
    if ($normalized -notmatch '^cpf-docs/evidence/' -and
            ($normalized -match '(^|/)patch-candidates/' -or
             $normalized -match '(^|/)create-domain-result[^/]*\.json$')) {
        Add-Failure "GENERATOR_WORK_ARTIFACT_IN_PRODUCT" $normalized `
            "생성 후보와 결과는 build/reports 또는 정제 Evidence에만 둡니다."
    }
    if ($normalized -match '(?i)\.(old|bak|copy)$') {
        Add-Failure "BACKUP_COPY_TRACKED" $normalized "백업·사본 파일은 버전관리에서 제거합니다."
    }
    if ($normalized -match '/deploy/inventory/.*\.candidate\.json$' -or
            $normalized -match '/sql/Vxx__') {
        Add-Failure "UNRESOLVED_CANDIDATE_TRACKED" $normalized `
            "확정되지 않은 candidate 또는 Vxx SQL을 제품 Source에 두지 않습니다."
    }

    # Vendor Pack의 미구현 상태 표시용 .gitkeep은 허용하되 다른 빈 디렉터리 선점은 금지합니다.
    if ($normalized.EndsWith('/.gitkeep') -and
            $normalized -notmatch '^cpf-tools/db/vendor/(mysql|postgresql|oracle|sqlserver)/(install|migration|provision|seed|verify|rollback)/\.gitkeep$') {
        Add-Failure "UNJUSTIFIED_GITKEEP" $normalized "빈 디렉터리 선점용 .gitkeep은 허용하지 않습니다."
    }
}

$result = [ordered]@{
    generatedAt = [DateTimeOffset]::Now.ToString("o")
    status = if ($failures.Count -eq 0) { "완료" } else { "실패" }
    trackedFileCount = $tracked.Count
    failureCount = $failures.Count
    failures = @($failures)
}
$resultPath = Join-Path $ResultDir "repository-hygiene.sanitized.json"
[System.IO.File]::WriteAllText(
    $resultPath,
    ($result | ConvertTo-Json -Depth 10),
    [System.Text.UTF8Encoding]::new($false))

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Host "FAIL [$($_.rule)] $($_.path)" }
    exit 1
}
Write-Host "Repository hygiene check passed. tracked=$($tracked.Count)"
