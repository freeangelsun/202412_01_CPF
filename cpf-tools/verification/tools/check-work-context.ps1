param([string]$Root=(Resolve-Path "$PSScriptRoot\..\..\..").Path)
$ErrorActionPreference="Stop"
Set-StrictMode -Version Latest
$Root=(Resolve-Path -LiteralPath $Root).Path
$required=@(
 "cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md",
 "cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md",
 "cpf-docs/work/current/CPF_DEVELOPER_GPT_NEXT_WORK_INSTRUCTION.md",
 "cpf-docs/work/current/CPF_REQUIREMENT_MASTER.csv",
 "cpf-docs/work/REQUIREMENT_STATUS.csv",
 "cpf-docs/work/OPEN_ISSUES.md",
 "cpf-docs/work/TEST_AND_EVIDENCE.md",
 "cpf-docs/work/HANDOVER.md",
 "cpf-docs/work/current/CODEX_FINAL_RUNTIME_VALIDATION_INSTRUCTION.md",
 "cpf-docs/work/current/CODEX_FINAL_VALIDATION_RESULT.md",
 "cpf-docs/work/current/CPF_CODEX_REVALIDATION_SCOPE.md"
)
$missing=@($required|Where-Object{-not(Test-Path (Join-Path $Root $_))})
if($missing.Count){throw "작업 시작 필수 문서 누락: $($missing -join ', ')"}
$head=(& git -C $Root rev-parse HEAD 2>$null)
$branch=(& git -C $Root branch --show-current 2>$null)
$status=@(& git -C $Root status --short)
Write-Host "CPF WORK CONTEXT"
Write-Host "HEAD=$head BRANCH=$branch"
Write-Host "Required canonical work documents:"
$required|ForEach-Object{Write-Host " - $_"}

if($status.Count){Write-Warning "Worktree 변경이 있습니다. 다른 PC/작업 산출물을 덮어쓰지 마십시오."; $status|ForEach-Object{Write-Host $_}}
$target=Get-Content (Join-Path $Root "cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md") -Raw
foreach($keyword in @('Core Platform Framework','Generated Domain','Evidence')){
    if($target -notmatch [regex]::Escape($keyword)){throw "Final Target 정본 핵심 표식 누락: $keyword"}
}
Write-Host "Work context gate PASS. 위 문서를 실제로 읽고 Requirement/Owner/Consumer/검증 범위를 정한 뒤 구현하십시오."
