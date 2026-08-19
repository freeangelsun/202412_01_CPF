param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..\..\..").Path "build/quality-gate")
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

$trackedAll = @(& git -C $Root -c core.quotepath=false ls-files)
if ($LASTEXITCODE -ne 0) {
    throw "git 추적 파일 목록을 읽지 못했습니다."
}

# Commit 전 cleanup 검증에서도 이미 Worktree에서 삭제된 tracked path는 현재 제품 구성에 포함하지 않는다.
# git ls-files 자체는 삭제된 tracked path도 반환하므로 실제 존재 파일 기준으로 Hygiene를 판정한다.
$tracked = @($trackedAll | Where-Object {
    Test-Path -LiteralPath (Join-Path $Root $_)
})
$deletedTrackedCount = $trackedAll.Count - $tracked.Count

# Nested official entrypoints live three directories below the repository root. Keep this
# exact inventory explicit so a future physical move cannot silently turn the default Root
# back into cpf-tools/. Top-level generator/release owners intentionally remain two levels deep.
$nestedOfficialEntrypoints = @(
    'cpf-tools/contracts/openapi/ensure-explicit-openapi-operation-ids.ps1',
    'cpf-tools/generator/tools/export-generated-domain-capability-inventory.ps1',
    'cpf-tools/generator/tools/initialize-domain-database.ps1',
    'cpf-tools/generator/tools/initialize-generated-domain-databases.ps1',
    'cpf-tools/generator/tools/remove-domain.ps1',
    'cpf-tools/generator/tools/sync-generated-domain-artifacts.ps1',
    'cpf-tools/generator/verification/check-domain-database-bootstrap-static.ps1',
    'cpf-tools/generator/verification/check-generated-domain-parity.ps1',
    'cpf-tools/generator/verification/check-generator-arbitrary-domain-parity.ps1',
    'cpf-tools/generator/verification/check-generator-golden-path.ps1',
    'cpf-tools/generator/verification/smoke-create-domain.ps1',
    'cpf-tools/generator/verification/smoke-domain-capability-matrix.ps1',
    'cpf-tools/generator/verification/smoke-generated-domain-lifecycle.ps1',
    'cpf-tools/generator/verification/smoke-remove-domain.ps1',
    'cpf-tools/generator/verification/verify-domain.ps1',
    'cpf-tools/governance/tools/check-architecture-ownership.ps1',
    'cpf-tools/governance/tools/export-architecture-inventory.ps1',
    'cpf-tools/governance/tools/export-cpf-inventory.ps1',
    'cpf-tools/governance/tools/install-codeowners.ps1',
    'cpf-tools/release/tools/promote-cpf-verified-local-artifacts.ps1',
    'cpf-tools/release/tools/verify-cpf-release-completion.ps1',
    'cpf-tools/security/tools/apply-v15-adm-api-permission-management.ps1',
    'cpf-tools/verification/openapi/check-openapi-source-coverage.ps1',
    'cpf-tools/verification/openapi/smoke-openapi.ps1'
)
if ($nestedOfficialEntrypoints.Count -ne 24 -or
        @($nestedOfficialEntrypoints | Sort-Object -Unique).Count -ne 24) {
    Add-Failure 'NESTED_ENTRYPOINT_INVENTORY' 'cpf-tools' 'Nested official entrypoint inventory must contain exactly 24 unique paths.'
}
$legacyNestedRootPatterns = @(
    '\$PSScriptRoot\\\.\.\\\.\.(?=["''])',
    'Join-Path\s+\$PSScriptRoot\s+["'']\.\./\.\.["'']'
)
$canonicalNestedRootPatterns = @(
    '\$PSScriptRoot\\\.\.\\\.\.\\\.\.',
    'Join-Path\s+\$PSScriptRoot\s+["'']\.\./\.\./\.\.["'']'
)
foreach ($relative in $nestedOfficialEntrypoints) {
    $absolute = Join-Path $Root $relative
    if (-not (Test-Path -LiteralPath $absolute -PathType Leaf)) {
        Add-Failure 'NESTED_ENTRYPOINT_MISSING' $relative 'Official nested PowerShell entrypoint is missing.'
        continue
    }
    $resolvedByDepth = [IO.Path]::GetFullPath((Join-Path (Split-Path $absolute -Parent) '../../..'))
    if (-not [string]::Equals($resolvedByDepth.TrimEnd('\','/'), $Root.TrimEnd('\','/'), [StringComparison]::OrdinalIgnoreCase)) {
        Add-Failure 'NESTED_ENTRYPOINT_DEPTH' $relative "Three-level default does not resolve to repository root: $resolvedByDepth"
    }
    $scriptText = Get-Content -LiteralPath $absolute -Raw -Encoding UTF8
    if (@($legacyNestedRootPatterns | Where-Object { $scriptText -match $_ }).Count -gt 0) {
        Add-Failure 'NESTED_ENTRYPOINT_OFF_BY_ONE' $relative 'Default Root still resolves two levels up to cpf-tools/.'
    }
    if (@($canonicalNestedRootPatterns | Where-Object { $scriptText -match $_ }).Count -eq 0) {
        Add-Failure 'NESTED_ENTRYPOINT_ROOT_CONTRACT' $relative 'Canonical three-level default Root expression is missing.'
    }
    $tokens = $null
    $parseErrors = $null
    [void][Management.Automation.Language.Parser]::ParseFile($absolute, [ref]$tokens, [ref]$parseErrors)
    foreach ($parseError in @($parseErrors)) {
        Add-Failure 'NESTED_ENTRYPOINT_PARSER' $relative $parseError.Message
    }
}

foreach ($relative in @(
    'cpf-tools/generator/create-domain.ps1',
    'cpf-tools/release/sign-release-artifacts.ps1',
    'cpf-tools/release/verify-release-artifacts.ps1'
)) {
    $absolute = Join-Path $Root $relative
    if (-not (Test-Path -LiteralPath $absolute -PathType Leaf)) {
        Add-Failure 'TOP_LEVEL_OWNER_MISSING' $relative 'Canonical top-level owner is missing.'
        continue
    }
    $scriptText = Get-Content -LiteralPath $absolute -Raw -Encoding UTF8
    if ($scriptText -notmatch '\$PSScriptRoot\\\.\.\\\.\.(?=["''])' -or
            $scriptText -match '\$PSScriptRoot\\\.\.\\\.\.\\\.\.') {
        Add-Failure 'TOP_LEVEL_OWNER_ROOT_CONTRACT' $relative 'Top-level generator/release owner must retain its correct two-level repository Root.'
    }
}

$generatorLifecycle = Get-Content -LiteralPath (Join-Path $Root 'cpf-tools/generator/verification/smoke-generated-domain-lifecycle.ps1') -Raw -Encoding UTF8
foreach ($ownerPath in @(
    'cpf-tools/generator/tools/create-domain.ps1',
    'cpf-tools/generator/verification/verify-domain.ps1',
    'cpf-tools/generator/tools/remove-domain.ps1',
    'cpf-tools/generator/tools/initialize-domain-database.ps1'
)) {
    if (-not $generatorLifecycle.Contains($ownerPath)) {
        Add-Failure 'GENERATOR_LIFECYCLE_OWNER' 'cpf-tools/generator/verification/smoke-generated-domain-lifecycle.ps1' "Canonical owner path missing: $ownerPath"
    }
}
$generatorWrapper = Get-Content -LiteralPath (Join-Path $Root 'cpf-tools/generator/tools/create-domain.ps1') -Raw -Encoding UTF8
if (-not $generatorWrapper.Contains("'create-domain.ps1'") -or $generatorWrapper.Contains("'generator/create-domain.ps1'")) {
    Add-Failure 'GENERATOR_WRAPPER_OWNER' 'cpf-tools/generator/tools/create-domain.ps1' 'Compatibility wrapper does not delegate to the adjacent canonical generator owner.'
}
$openApiCoverage = Get-Content -LiteralPath (Join-Path $Root 'cpf-tools/verification/openapi/check-openapi-source-coverage.ps1') -Raw -Encoding UTF8
if (-not $openApiCoverage.Contains('cpf-tools/contracts/openapi/ensure-explicit-openapi-operation-ids.ps1') -or
        $openApiCoverage.Contains('$PSScriptRoot/ensure-explicit-openapi-operation-ids.ps1')) {
    Add-Failure 'OPENAPI_COVERAGE_OWNER' 'cpf-tools/verification/openapi/check-openapi-source-coverage.ps1' 'OpenAPI coverage must call the canonical contracts/openapi owner.'
}
$windowsCli = Get-Content -LiteralPath (Join-Path $Root 'cpf-tools/runtime/cli/cpf.bat') -Raw -Encoding UTF8
if (-not $windowsCli.Contains('EnableDelayedExpansion') -or -not $windowsCli.Contains('exit /b !ERRORLEVEL!')) {
    Add-Failure 'WINDOWS_CLI_EXIT_PROPAGATION' 'cpf-tools/runtime/cli/cpf.bat' 'Windows CLI must propagate the Python process exit code from inside command blocks.'
}

$forbiddenPrefixes = @(
    "cpf-docs/evidence/20260722_01/",
    "cpf-core/src/main/resources/sql/vendor/",
    "cpf-biz-admin/src/main/resources/sql/vendor/",
    "cpf-core/src/main/resources/mybatis/vendor/",
    "cpf-common/src/main/resources/mybatis/vendor/",
    "cpf-member/src/main/resources/mybatis/vendor/",
    "cpf-account/src/main/resources/mybatis/vendor/",
    "cpf-education/src/main/resources/mybatis/vendor/"
)

foreach ($path in $tracked) {
    $normalized = $path.Replace('\', '/')
    $isCanonicalBuildToolSource =
        $normalized -match '^cpf-tools/build/gradle-plugin/(build\.gradle|settings\.gradle|src/(main|test)/.+)$' -or
        $normalized -match '^cpf-tools/build/platform-bom/(build\.gradle|settings\.gradle|(internal-bom|public-bom)/build\.gradle)$'

    if ($normalized -eq "docker-compose.local.yml") {
        Add-Failure "ROOT_DEPLOY_ARTIFACT" $normalized `
            "환경별 Docker Compose는 deploy/local 등 deploy 경계가 소유하며 Repository Root에 두지 않습니다."
    }
    if ($normalized.StartsWith("cpf-tools/db/source/", [System.StringComparison]::OrdinalIgnoreCase)) {
        Add-Failure "LEGACY_VENDOR_SOURCE_ROOT" $normalized `
            "Vendor canonical source는 cpf-tools/db/vendor/<vendor>/source가 소유합니다."
    }
    if ($normalized -eq "cpf-biz-frontend/src/features/console.ts") {
        Add-Failure "BZA_MONOLITHIC_CONSOLE" $normalized `
            "BZA 화면은 feature package/route registry/code splitting 구조를 사용합니다."
    }
    if ($normalized -match '^cpf-tools/db/vendor/mariadb/source/(45_external_schema|57_external_seed_data)\.sql$') {
        Add-Failure "FIXED_EXS_PLATFORM_SQL" $normalized `
            "EXS는 Generated Domain only이며 Platform 고정 Schema/Seed를 두지 않습니다."
    }

    foreach ($prefix in $forbiddenPrefixes) {
        if ($normalized.StartsWith($prefix, [System.StringComparison]::OrdinalIgnoreCase)) {
            Add-Failure "FORBIDDEN_TRANSITIONAL_RESOURCE" $normalized `
                "Central Vendor Pack/최신 Evidence 정본과 충돌하는 삭제 확정 경로입니다."
        }
    }

    if (-not $isCanonicalBuildToolSource -and
            $normalized -notmatch '/src/(main|test)/' -and
            $normalized -notmatch '/frontend/src/' -and
            $normalized -notmatch '^cpf-docs/evidence/' -and
            $normalized -notmatch '^cpf-docs/work/' -and
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

    # 공식 PostgreSQL/Oracle Pack의 기존 layout marker만 허용합니다.
    if ($normalized.EndsWith('/.gitkeep') -and
            $normalized -notmatch '^cpf-tools/db/vendor/(postgresql|oracle)/(install|migration(?:/flyway)?|provision|seed|verify|rollback)/\.gitkeep$') {
        Add-Failure "UNJUSTIFIED_GITKEEP" $normalized "빈 디렉터리 선점용 .gitkeep은 허용하지 않습니다."
    }
}

$result = [ordered]@{
    generatedAt = [DateTimeOffset]::Now.ToString("o")
    status = if ($failures.Count -eq 0) { "완료" } else { "실패" }
    trackedFileCount = $tracked.Count
    deletedTrackedCount = $deletedTrackedCount
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
    throw "Repository hygiene check FAIL: $($failures.Count)건"
}
Write-Host "Repository hygiene check passed. tracked=$($tracked.Count), deletedTracked=$deletedTrackedCount"
