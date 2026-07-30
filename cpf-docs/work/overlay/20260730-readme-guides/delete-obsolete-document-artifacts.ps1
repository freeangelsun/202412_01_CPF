param(
    [string]$RootPath = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..\..")).Path
)

$ErrorActionPreference = "Stop"

$archiveRoot = Join-Path $RootPath "cpf-docs\work\archive\20260730-chatgpt-direct-implementation"

$archivedPairs = @(
    @{ Active = "cpf-docs\work\current\CPF_20260730_01_FINAL_CLOSURE_REQUIREMENT_INTAKE_BASELINE.md"; Archive = "CPF_20260730_01_FINAL_CLOSURE_REQUIREMENT_INTAKE_BASELINE.md" },
    @{ Active = "cpf-docs\work\current\CPF_CHATGPT_DIRECT_FULL_IMPLEMENTATION_REQUEST_20260730.md"; Archive = "CPF_CHATGPT_DIRECT_FULL_IMPLEMENTATION_REQUEST_20260730.md" },
    @{ Active = "cpf-docs\work\current\CPF_CODEX_FINAL_REVIEW_DOCUMENT_STRATEGY.md"; Archive = "CPF_CODEX_FINAL_REVIEW_DOCUMENT_STRATEGY.md" },
    @{ Active = "cpf-docs\work\current\CPF_CODEX_FINAL_REVIEW_REQUEST.md"; Archive = "CPF_CODEX_FINAL_REVIEW_REQUEST.md" },
    @{ Active = "cpf-docs\work\handover\CPF_20260730_CHATGPT_DIRECT_IMPLEMENTATION_CHECKPOINT_HANDOVER.md"; Archive = "CPF_20260730_CHATGPT_DIRECT_IMPLEMENTATION_CHECKPOINT_HANDOVER.md" }
)

foreach ($pair in $archivedPairs) {
    $activePath = Join-Path $RootPath $pair.Active
    if (-not (Test-Path -LiteralPath $activePath)) {
        continue
    }

    $archivePath = Join-Path $archiveRoot $pair.Archive
    if (-not (Test-Path -LiteralPath $archivePath)) {
        throw "Archive 사본이 없어 삭제를 중단합니다: $archivePath"
    }

    Remove-Item -LiteralPath $activePath -Force
    Write-Host "[삭제] $($pair.Active)"
}

$rootGarbage = @(
    "CPF_20260730_OVERLAY_APPLY_README.md",
    "CPF_OVERLAY_MANIFEST.json",
    "CPF_OVERLAY_SHA256SUMS.txt"
)

foreach ($relative in $rootGarbage) {
    $target = Join-Path $RootPath $relative
    if (Test-Path -LiteralPath $target) {
        Remove-Item -LiteralPath $target -Force
        Write-Host "[삭제] $relative"
    }
}

Write-Host "문서 잔재 정리가 완료되었습니다."
