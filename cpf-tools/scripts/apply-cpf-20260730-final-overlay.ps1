param(
    [string] $RepoRoot = (Resolve-Path (Join-Path $PSScriptRoot '../..')).Path,
    [string] $ExpectedBaseSha = 'fae7aa9643f646db4bcbcf665d13b8f3b809e8c8',
    [switch] $AllowDifferentBase,
    [switch] $ApplyGeneratedDomainDatabaseArtifacts,
    [switch] $RunLowCostGates
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
if ($PSVersionTable.PSVersion.Major -lt 7) { throw 'PowerShell 7 이상이 필요합니다.' }
$RepoRoot = (Resolve-Path -LiteralPath $RepoRoot).Path
$branch = (& git -C $RepoRoot branch --show-current).Trim()
$head = (& git -C $RepoRoot rev-parse HEAD).Trim().ToLowerInvariant()
if ($LASTEXITCODE -ne 0 -or $head -notmatch '^[0-9a-f]{40}$') { throw 'Git HEAD를 확인할 수 없습니다.' }
if ($branch -ne 'master') { throw "master Branch에서만 적용할 수 있습니다. current=$branch" }
if (-not $AllowDifferentBase -and $head -ne $ExpectedBaseSha.ToLowerInvariant()) {
    throw "Overlay 기준 SHA가 다릅니다. expected=$ExpectedBaseSha actual=$head. 최신 변경을 먼저 재검수하십시오."
}

$required = @(
    'cpf-tools/db/canonical/platform-schema.json',
    'cpf-tools/scripts/sync-database-artifacts.ps1',
    'cpf-tools/scripts/verify-cpf-20260730-full-implementation.ps1',
    'cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md',
    'cpf-docs/work/handover/CPF_20260730_CHATGPT_DIRECT_IMPLEMENTATION_FINAL_HANDOVER.md'
)
foreach ($path in $required) {
    if (-not (Test-Path -LiteralPath (Join-Path $RepoRoot $path) -PathType Leaf)) {
        throw "Overlay 파일이 없습니다. ZIP을 Repository Root에 먼저 풀어야 합니다: $path"
    }
}

# 과거 Active 요청서와 체크포인트는 Archive 사본이 포함된 경우에만 제거한다.
$archiveRoot = Join-Path $RepoRoot 'cpf-docs/work/archive/20260730-chatgpt-direct-implementation'
if (-not (Test-Path -LiteralPath $archiveRoot -PathType Container)) {
    throw "Archive가 없어 Stale 문서를 안전하게 제거할 수 없습니다: $archiveRoot"
}
$stalePaths = @(
    'cpf-docs/work/current/CPF_20260730_01_FINAL_CLOSURE_REQUIREMENT_INTAKE_BASELINE.md',
    'cpf-docs/work/current/CPF_CHATGPT_DIRECT_FULL_IMPLEMENTATION_REQUEST_20260730.md',
    'cpf-docs/work/current/CPF_CODEX_FINAL_REVIEW_DOCUMENT_STRATEGY.md',
    'cpf-docs/work/current/CPF_CODEX_FINAL_REVIEW_REQUEST.md',
    'cpf-docs/work/handover/CPF_20260730_CHATGPT_DIRECT_IMPLEMENTATION_CHECKPOINT_HANDOVER.md',
    'CPF_20260730_OVERLAY_APPLY_README.md',
    'CPF_OVERLAY_MANIFEST.json',
    'CPF_OVERLAY_SHA256SUMS.txt'
)
foreach ($relative in $stalePaths) {
    $target = Join-Path $RepoRoot $relative
    if (Test-Path -LiteralPath $target) { Remove-Item -LiteralPath $target -Force }
}

$sync = Join-Path $RepoRoot 'cpf-tools/scripts/sync-database-artifacts.ps1'
$syncArgs = @('-NoProfile','-ExecutionPolicy','Bypass','-File',$sync,'-Root',$RepoRoot)
if ($ApplyGeneratedDomainDatabaseArtifacts) { $syncArgs += '-ApplyGeneratedDomains' }
& pwsh @syncArgs
if ($LASTEXITCODE -ne 0) { throw "Canonical DB Artifact 동기화가 실패했습니다. exit=$LASTEXITCODE" }

& git -C $RepoRoot diff --check
if ($LASTEXITCODE -ne 0) { throw 'git diff --check가 실패했습니다.' }

$structureGate = Join-Path $RepoRoot 'cpf-tools/scripts/verify-cpf-20260730-overlay-structure.ps1'
& pwsh -NoProfile -ExecutionPolicy Bypass -File $structureGate -Root $RepoRoot
if ($LASTEXITCODE -ne 0) { throw "Overlay 구조 Gate가 실패했습니다. exit=$LASTEXITCODE" }

if ($RunLowCostGates) {
    $gradle = if ($IsWindows) { Join-Path $RepoRoot 'gradlew.bat' } else { Join-Path $RepoRoot 'gradlew' }
    & $gradle -p $RepoRoot verifyCpfFinalSourceGates checkRuntimeQueryContracts checkSqlCanonical --no-daemon
    if ($LASTEXITCODE -ne 0) { throw "저비용 Source Gate가 실패했습니다. exit=$LASTEXITCODE" }
}

Write-Host "CPF 20260730 final overlay apply PASS. baseSha=$head"
Write-Host '다음 단계: 변경을 검토하고 사용자가 Source Commit한 뒤 exact-SHA 전체 검증을 실행하십시오.'
