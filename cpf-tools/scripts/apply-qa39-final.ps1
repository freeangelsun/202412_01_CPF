[CmdletBinding()]
param()

$ErrorActionPreference = 'Stop'
$repoRoot = (Get-Location).Path

function Invoke-Checked {
    param(
        [Parameter(Mandatory = $true)][string]$Label,
        [Parameter(Mandatory = $true)][scriptblock]$Command
    )
    Write-Host "`n[$Label]" -ForegroundColor Cyan
    & $Command
    if ($LASTEXITCODE -ne 0) {
        throw "$Label failed (exit=$LASTEXITCODE)"
    }
}

$required = @(
    'cpf-docs/work/CPF_DELETE_ONE_LINE.ps1.txt',
    'cpf-tools/verification/qa39/verify-qa39-canonical-starter-closure.py',
    'cpf-tools/verification/qa39/verify-cpf-provider-conformance.py',
    'gradlew.bat'
)
foreach ($path in $required) {
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        throw "Required file is missing: $path"
    }
}

Write-Host "`n[1/6] Apply approved exact-path deletions" -ForegroundColor Cyan
Invoke-Expression (Get-Content -Raw -LiteralPath 'cpf-docs/work/CPF_DELETE_ONE_LINE.ps1.txt')

Write-Host "`n[2/6] Confirm delete manifest closure" -ForegroundColor Cyan
$remaining = Get-Content -LiteralPath 'cpf-docs/work/CPF_DELETE_MANIFEST.txt' |
    Where-Object { $_ -and -not $_.StartsWith('#') -and (Test-Path -LiteralPath $_) }
if ($remaining) {
    throw "Delete targets remain:`n$($remaining -join "`n")"
}

Invoke-Checked -Label '3/6 git diff --check' -Command { git diff --check }
Invoke-Checked -Label '4/6 QA39 canonical starter closure' -Command {
    python .\cpf-tools\verification\qa39\verify-qa39-canonical-starter-closure.py
}
Invoke-Checked -Label '5/6 CPF provider conformance' -Command {
    python .\cpf-tools\verification\qa39\verify-cpf-provider-conformance.py
}
Invoke-Checked -Label '6/6 Gradle QA39 gates' -Command {
    .\gradlew.bat checkQa39CanonicalStarterClosure checkCpfProviderConformance --no-daemon
}

Write-Host "`n[Git status]" -ForegroundColor Cyan
git -c core.quotepath=false status --short --branch
if ($LASTEXITCODE -ne 0) {
    throw "git status failed (exit=$LASTEXITCODE)"
}

Write-Host "`nCPF QA39 APPLY + DELETE + LOW-COST VALIDATION PASSED" -ForegroundColor Green
