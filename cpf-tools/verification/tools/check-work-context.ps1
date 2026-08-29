param([string]$Root=(Resolve-Path "$PSScriptRoot\..\..\..").Path)
$ErrorActionPreference="Stop"
Set-StrictMode -Version Latest
$Root=(Resolve-Path -LiteralPath $Root).Path
$required=@(
 "cpf-docs/governance/development-harness/product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md",
 "cpf-docs/governance/CPF_DOCUMENT_CANONICAL_INDEX.md",
 "cpf-docs/governance/CPF_CANONICAL_PATH_AND_ROLE_MAP.md",
 "cpf-docs/governance/development-harness/current/CPF_CURRENT_WORK_REQUEST.md",
 "cpf-docs/governance/development-harness/current/REQUIREMENT_STATUS.csv",
 "cpf-docs/deliverables/OPEN_ISSUES.md",
 "cpf-docs/deliverables/TEST_AND_EVIDENCE.md",
 "cpf-docs/deliverables/DELETE_MANIFEST.csv"
)
$missing=@($required|Where-Object{-not(Test-Path (Join-Path $Root $_))})
if($missing.Count){throw "작업 시작 필수 Current 문서 누락: $($missing -join ', ')"}
$head=(& git -C $Root rev-parse HEAD 2>$null)
$branch=(& git -C $Root branch --show-current 2>$null)
$status=@(& git -C $Root status --short)
Write-Host "CPF CURRENT WORK CONTEXT"
Write-Host "HEAD=$head BRANCH=$branch"
$required|ForEach-Object{Write-Host " - $_"}
if($status.Count){Write-Warning "Working Tree 변경이 있습니다. 실제 Local Source를 실행 대상으로 사용하고 변경을 덮어쓰지 마십시오."; $status|ForEach-Object{Write-Host $_}}
$target=Get-Content (Join-Path $Root "cpf-docs/governance/development-harness/product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md") -Raw
foreach($keyword in @('Core Platform Framework','Canonical Requirement Count:','CPF-SYSTEM6','GEN-SETUP','EDU-CANONICAL')){
    if($target -notmatch [regex]::Escape($keyword)){throw "Final Target 정본 핵심 표식 누락: $keyword"}
}
Write-Host "Work context gate PASS. Source를 Target에 맞추며 Target을 Source에 맞춰 약화하지 마십시오."
