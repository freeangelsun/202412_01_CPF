param([string]$Root=(Resolve-Path "$PSScriptRoot\..\..").Path)
$ErrorActionPreference="Stop"
Set-StrictMode -Version Latest
$Root=(Resolve-Path -LiteralPath $Root).Path
$required=@(
 "cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md",
 "cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md",
 "cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md",
 "cpf-docs/work/current/CPF_INTEGRATED_VERIFICATION_PLAN.md",
 "cpf-docs/work/state/CPF_CODEX_DECISION_LOG.md",
 "cpf-docs/work/state/CPF_CODEX_CONTINUITY_STATE.md"
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

# 별도 세션/PC에서 남긴 가장 최근 checkpoint handover도 함께 보여 주되 정본을 대체하지 않습니다.
$stateDir=Join-Path $Root 'cpf-docs/work/state'
$latestHandover=Get-ChildItem $stateDir -File -Filter 'CPF_R*_HANDOVER.md' -ErrorAction SilentlyContinue |
    Sort-Object LastWriteTimeUtc -Descending | Select-Object -First 1
if($latestHandover){
    $relative=$latestHandover.FullName.Substring($Root.Length).TrimStart('\','/')
    Write-Host "Latest checkpoint handover: $relative"
}
if($status.Count){Write-Warning "Worktree 변경이 있습니다. 다른 PC/작업 산출물을 덮어쓰지 마십시오."; $status|ForEach-Object{Write-Host $_}}
$current=Get-Content (Join-Path $Root "cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md") -Raw
if($current -notmatch [regex]::Escape($head.Trim())){
    Write-Warning "Current Request 기준 SHA가 현재 HEAD와 다릅니다. 최신 master/현재 작업 Diff를 대조한 뒤 기준선을 갱신하십시오."
}
$target=Get-Content (Join-Path $Root "cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md") -Raw
foreach($keyword in @('Core Platform Framework','Generated Domain','Evidence')){
    if($target -notmatch [regex]::Escape($keyword)){throw "Final Target 정본 핵심 표식 누락: $keyword"}
}
Write-Host "Work context gate PASS. 위 문서를 실제로 읽고 Requirement/Owner/Consumer/검증 범위를 정한 뒤 구현하십시오."
