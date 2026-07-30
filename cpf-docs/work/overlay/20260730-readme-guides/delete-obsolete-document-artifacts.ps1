param(
    [string]$RootPath = (Resolve-Path (Join-Path $PSScriptRoot "..\..\..\..")).Path,
    [switch]$Approved
)

$ErrorActionPreference = "Stop"

$candidates = @(
    "cpf-docs\work\overlay\20260730-readme-guides"
)

Write-Host "현재 개발 중인 문서 지원 자료이므로 자동 삭제하지 않습니다."
Write-Host "정리 후보:"
$candidates | ForEach-Object { Write-Host " - $_" }

if (-not $Approved) {
    Write-Host "개발 담당자의 불필요 판정과 사용자 승인 후 -Approved를 지정하세요."
    exit 0
}

throw "이 Overlay에는 현재 패키지의 검증·Manifest가 포함돼 있으므로 Commit 전에는 삭제할 수 없습니다. Commit 후 별도 정리 요청서에서 대상을 다시 확정하세요."
