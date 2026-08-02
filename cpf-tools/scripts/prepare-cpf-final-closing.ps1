param(
    [string]$Root=(Resolve-Path "$PSScriptRoot\..\..").Path,
    [switch]$AllowDirty
)
$ErrorActionPreference='Stop';Set-StrictMode -Version Latest
if($PSVersionTable.PSVersion.Major -lt 7){throw 'pwsh 7 이상이 필요합니다.'}
$Root=(Resolve-Path -LiteralPath $Root).Path
Push-Location $Root
try{
    if(-not(Test-Path '.git' -PathType Container)){throw "Git repository root가 아닙니다: $Root"}
    $remote=(git remote get-url origin 2>$null)
    if($LASTEXITCODE -ne 0 -or $remote -notmatch 'freeangelsun/202412_01_CPF'){throw "기준 Repository가 아닙니다: $remote"}
    $baseSha=(git rev-parse HEAD).Trim()
    if(-not $AllowDirty -and (git status --porcelain)){throw '작업 전 worktree가 dirty입니다. 기존 변경을 정리하거나 -AllowDirty를 명시하십시오.'}

    # 현재 요청 정본에 과거 완료 보고를 남기지 않는다. History/Evidence 자체는 삭제하지 않는다.
    foreach($stale in @(
        'cpf-docs/work/current/CPF_CHATGPT_FINAL_COMPLETION_REPORT_20260728.md',
        'cpf-docs/work/current/CPF_PHASE1_55_PROGRESS_20260728.md',
        'cpf-docs/work/current/CPF_REMAINING_REQUIREMENT_MATRIX_20260727.md'
    )){if(Test-Path $stale -PathType Leaf){Remove-Item $stale -Force;Write-Host "[REMOVED STALE CURRENT] $stale"}}

    & pwsh -NoProfile -File .\cpf-tools\scripts\build-all-install-sql.ps1 -Root $Root
    if($LASTEXITCODE -ne 0){throw 'build-all-install-sql failed'}
    & pwsh -NoProfile -File .\cpf-tools\scripts\sync-database-artifacts.ps1 -Root $Root
    if($LASTEXITCODE -ne 0){throw 'sync-database-artifacts failed'}
    & pwsh -NoProfile -File .\cpf-tools\scripts\generate-migration-checksums.ps1 -Root $Root -Apply
    if($LASTEXITCODE -ne 0){throw 'generate-migration-checksums failed'}

    foreach($gate in @(
        'check-runtime-control-public-boundary.ps1',
        'check-runtime-capability-consumers.ps1',
        'check-notification-portable-sql.ps1',
        'check-local-runtime-topology.ps1',
        'check-migration-checksums.ps1',
        'check-enterprise-qa-closing.ps1'
    )){
        Write-Host "==> $gate"
        & pwsh -NoProfile -File (Join-Path '.\cpf-tools\scripts' $gate) -Root $Root
        if($LASTEXITCODE -ne 0){throw "$gate failed"}
    }
    git diff --check
    if($LASTEXITCODE -ne 0){throw 'git diff --check failed'}
    $summary=[ordered]@{
        baseSha=$baseSha
        preparedAt=(Get-Date).ToString('o')
        dirtyFiles=@(git status --short)
        commitCreated=$false
        pushPerformed=$false
        next='Review diff, run verify-cpf-release-completion.ps1, then user commits/pushes.'
    }
    $out='cpf-tools/verification/20260728_04/PREPARE_RESULT.sanitized.json'
    $summary|ConvertTo-Json -Depth 10|Set-Content -Encoding UTF8 $out
    Write-Host "[PASS] final closing preparation complete. baseSha=$baseSha"
    Write-Host "Result: $out"
}finally{Pop-Location}
