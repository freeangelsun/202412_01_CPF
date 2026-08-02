param(
    [string]$Root = (Get-Location).Path
)

$ErrorActionPreference = "Stop"
Set-Location -LiteralPath $Root

& pwsh -NoProfile -File ".\cpf-tools\scripts\verify-post-qa37-integrated-overlay.ps1" -Root $Root
if ($LASTEXITCODE -ne 0) {
    throw "Overlay 검증 실패"
}

$manifestPath = ".\cpf-docs\work\manifest\CPF_20260802_05_POST_QA37_PACKAGE_MANIFEST.json"
$manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding utf8 | ConvertFrom-Json
$paths = @($manifest.files | ForEach-Object { [string]$_.path })

if ($paths.Count -eq 0) {
    throw "Stage 대상 파일이 없습니다."
}

& git add -- $paths
if ($LASTEXITCODE -ne 0) {
    throw "git add 실패"
}

& git diff --cached --check
if ($LASTEXITCODE -ne 0) {
    throw "Staged diff 검증 실패"
}

$protectedPaths = @(
    "cpf-docs/deliverables",
    "cpf-docs/guides",
    "cpf-docs/environment/docker",
    "cpf-tools/environment/docker-development-test"
)
$protectedStaged = @(& git -c core.quotepath=false diff --cached --name-status -- $protectedPaths)
if ($protectedStaged.Count -gt 0) {
    $protectedStaged | ForEach-Object { Write-Host $_ }
    throw "보호 경로가 Stage되었습니다."
}

Write-Host "POST-QA37 files staged safely:"
& git -c core.quotepath=false diff --cached --name-status
