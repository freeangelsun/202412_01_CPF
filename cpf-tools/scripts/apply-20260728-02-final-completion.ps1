param(
    [Parameter(Mandatory = $true)][string]$ProjectRoot,
    [switch]$AllowDirty
)

$ErrorActionPreference = "Stop"
$ExpectedHead = "ecaddd581a88ede22b63116effd61313744b3fbe"
$OverlayRoot = (Resolve-Path (Join-Path $PSScriptRoot "..\..")).Path
$ProjectRoot = (Resolve-Path $ProjectRoot).Path

function Invoke-Git([string[]]$Arguments) {
    $output = & git -C $ProjectRoot @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) { throw "git $($Arguments -join ' ') failed: $output" }
    return ($output | Out-String).Trim()
}

if (-not (Test-Path (Join-Path $ProjectRoot ".git"))) {
    throw "Git repository가 아닙니다: $ProjectRoot"
}

$head = Invoke-Git @("rev-parse", "HEAD")
if ($head -ne $ExpectedHead) {
    throw "기준 Commit 불일치. expected=$ExpectedHead actual=$head"
}

$status = Invoke-Git @("status", "--porcelain")
if (-not $AllowDirty -and -not [string]::IsNullOrWhiteSpace($status)) {
    throw "작업트리가 Clean하지 않습니다. 변경을 정리하거나 명시적으로 -AllowDirty를 사용하십시오.`n$status"
}

$verify = Join-Path $OverlayRoot "cpf-tools\verification\20260728_02\verify-final-overlay.ps1"
& pwsh -ExecutionPolicy Bypass -File $verify -OverlayRoot $OverlayRoot
if ($LASTEXITCODE -ne 0) { throw "Overlay 적용 전 검증 실패" }

Get-ChildItem -Path $OverlayRoot -Recurse -File | ForEach-Object {
    $relative = $_.FullName.Substring($OverlayRoot.Length).TrimStart('\', '/')
    $target = Join-Path $ProjectRoot $relative
    $targetDir = Split-Path -Parent $target
    if (-not (Test-Path $targetDir)) { New-Item -ItemType Directory -Path $targetDir -Force | Out-Null }
    Copy-Item -LiteralPath $_.FullName -Destination $target -Force
}

$obsolete = @(
    "cpf-docs\work\current\CPF_INTERMEDIATE_FILE_INVENTORY_20260728.md",
    "cpf-docs\work\current\CPF_INTERMEDIATE_NOT_FINAL_20260728.md",
    "cpf-docs\work\current\CPF_INTERMEDIATE_REMAINING_WORK_20260728.md",
    "cpf-docs\work\current\CPF_INTERMEDIATE_ROOT_OVERLAY_GUIDE_20260728.md",
    "cpf-docs\work\current\CPF_INTERMEDIATE_SHA256SUMS_20260728.txt",
    "cpf-docs\work\current\CPF_INTERMEDIATE_VALIDATION_LEDGER_20260728.md",
    "cpf-docs\work\current\CPF_REMAINING_REQUIREMENT_MATRIX_20260727.md",
    "cpf-docs\work\handover\CPF_CHATGPT_TO_CODEX_HANDOVER_20260728.md",
    "cpf-docs\work\handover\CPF_FINAL_COMPLETION_PACKAGE_MANIFEST_20260728.md"
)
foreach ($relative in $obsolete) {
    $path = Join-Path $ProjectRoot $relative
    if (Test-Path $path) { Remove-Item -LiteralPath $path -Force }
}

& pwsh -ExecutionPolicy Bypass -File (Join-Path $ProjectRoot "cpf-tools\scripts\verify-20260728-02-final-completion.ps1") -ProjectRoot $ProjectRoot
if ($LASTEXITCODE -ne 0) { throw "Overlay 적용 후 검증 실패" }

Write-Host "CPF 20260728_02 final completion overlay applied." -ForegroundColor Green
Write-Host "Commit/Push는 수행하지 않았습니다. 아래 변경을 검토하십시오." -ForegroundColor Yellow
& git -C $ProjectRoot status --short
