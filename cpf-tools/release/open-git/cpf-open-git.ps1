<#
.SYNOPSIS
    CPF Open Git Release 진입점(정본 엔진 wrapper).

.DESCRIPTION
    Open Git Release 의 생성/검증/상태/설정/Commit/Push 를 하나의 진입점으로 제공합니다.
    Release 로직은 이 스크립트가 아니라 정본 엔진 `cpf_open_git.py` 하나가 소유합니다.
    CLI / PowerShell / Gradle Task 가 모두 같은 lifecycle 을 호출합니다(Harness §29.2).

.PARAMETER Action
    build  : 공개 Release 산출물 생성 (Git 변경 없음)
    check  : 생성된 공개 Release 검증 (Git 변경 없음)
    status : 마지막 Release 상태 표시
    setup  : 정본 연동 파일 현행화
    commit : 검증 완료 Working Tree Commit (명시적 승인 필요)
    push   : 검증 완료 Commit 을 remote 로 Push (명시적 승인 필요)

.PARAMETER ConfirmGitWrite
    Commit/Push 를 명시적으로 승인합니다. 이 값이 없으면 Git Write 는 수행되지 않습니다.

.EXAMPLE
    pwsh -File cpf-open-git.ps1 -Action build
    pwsh -File cpf-open-git.ps1 -Action commit -ConfirmGitWrite
#>
param(
  # ── 사용자가 바꾸는 값 ────────────────────────────────────────────────
  [ValidateSet('build','check','status','setup','commit','push')]
  [string]$Action = 'build',

  # 저장소 루트. 기본은 이 스크립트 위치 기준 CPF 루트다.
  [string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path,

  # Open Git remote. 비우면 정본 설정을 따른다.
  [string]$Remote = '',

  # Generator 산출물 경로(선택).
  [string]$GeneratorArtifacts = '',

  # Release Profile. binary(기본) 또는 source.
  # PowerShell 자동 변수와 이름이 겹치면 경고가 나므로 실제 변수명은 ReleaseProfile 로 두고,
  # 기존 호출 호환을 위해 Alias 만 유지한다.
  [Alias('Profile')]
  [ValidateSet('binary','source')]
  [string]$ReleaseProfile = 'binary',

  # Commit 메시지(생략 시 표준 형식).
  [string]$Message = '',

  # ── Git Write 승인 ───────────────────────────────────────────────────
  # 이 스위치가 없으면 commit/push 는 fail-closed 로 중단된다(Harness §29.3).
  [switch]$ConfirmGitWrite
)

# CPF 표준 인코딩은 UTF-8 이다. 콘솔 코드페이지에 좌우되면 한글 진단이 깨진다.
$CpfUtf8ConsoleEncoding = [Text.UTF8Encoding]::new($false)
try {
    [Console]::InputEncoding = $CpfUtf8ConsoleEncoding
    [Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
    $OutputEncoding = $CpfUtf8ConsoleEncoding
} catch { }
$env:PYTHONUTF8 = '1'
$env:PYTHONIOENCODING = 'utf-8'

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest

# ── 실행 단계 1: Python 확인 ────────────────────────────────────────────
$python = Get-Command python -ErrorAction SilentlyContinue
if (-not $python) { $python = Get-Command python3 -ErrorAction SilentlyContinue }
if (-not $python) { throw 'CPF Open Git: Python 3 실행파일을 찾을 수 없습니다.' }

# ── 실행 단계 2: 정본 엔진 인자 조립 ───────────────────────────────────
$engine = Join-Path $PSScriptRoot 'cpf_open_git.py'
if (-not (Test-Path -LiteralPath $engine -PathType Leaf)) {
    throw "CPF Open Git 정본 엔진을 찾을 수 없습니다: $engine"
}
$argsList = @($engine, $Action, '--root', $Root, '--profile', $ReleaseProfile)
if (-not [string]::IsNullOrWhiteSpace($Remote))             { $argsList += @('--remote', $Remote) }
if (-not [string]::IsNullOrWhiteSpace($GeneratorArtifacts)) { $argsList += @('--generator-artifacts', $GeneratorArtifacts) }
if (-not [string]::IsNullOrWhiteSpace($Message))            { $argsList += @('--message', $Message) }
# 승인은 오직 이 한 곳에서만 엔진으로 전달된다.
if ($ConfirmGitWrite)                                       { $argsList += '--confirm-git-write' }

# ── 실행 단계 3: 실행 ──────────────────────────────────────────────────
Write-Host "[CPF][OPEN-GIT] action=$Action profile=$ReleaseProfile root=$Root gitWrite=$([bool]$ConfirmGitWrite)"
& $python.Source @argsList
$exitCode = $LASTEXITCODE

# ── 실행 단계 4: 결과 안내 ─────────────────────────────────────────────
$evidence = Join-Path $Root 'cpf-release/reports/OPEN_GIT_RELEASE_STATUS.json'
$logPath = Join-Path $Root 'cpf-release/logs/open-git-release.log'
if ($exitCode -ne 0) {
    Write-Host "CPF_OPEN_GIT=FAIL action=$Action exitCode=$exitCode"
    Write-Host "  다음 조치 : 아래 로그에서 실패 단계를 확인하고 해당 단계만 다시 수행하세요."
    Write-Host "  Evidence  : $evidence"
    Write-Host "  Log       : $logPath"
    if ($Action -in @('commit','push')) {
        Write-Host "  Git Write : 수행되지 않았습니다(fail-closed)."
    }
    exit $exitCode
}
Write-Host "CPF_OPEN_GIT=PASS action=$Action"
Write-Host "  Evidence : $evidence"
Write-Host "  Log      : $logPath"
exit 0
