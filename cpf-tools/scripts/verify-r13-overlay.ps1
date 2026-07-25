param(
    [switch] $StaticOnly,
    [switch] $WithTests
)

$ErrorActionPreference = "Stop"
$Root = (Resolve-Path "$PSScriptRoot\..\..").Path
Set-Location $Root
$pwsh = (Get-Process -Id $PID).Path

function Invoke-Checked {
    param([string]$Name, [scriptblock]$Action)
    Write-Host "[R13] START $Name"
    & $Action
    if ($LASTEXITCODE -ne 0) { throw "$Name failed. exit=$LASTEXITCODE" }
    Write-Host "[R13] PASS  $Name"
}

Invoke-Checked "R13 static hardening" {
    & $pwsh -NoProfile -ExecutionPolicy Bypass -File ".\cpf-tools\scripts\check-r13-product-hardening.ps1" -Root $Root
}
Invoke-Checked "Migration checksum" {
    & $pwsh -NoProfile -ExecutionPolicy Bypass -File ".\cpf-tools\scripts\check-migration-checksums.ps1"
}

if (-not $StaticOnly) {
    Invoke-Checked "Version consistency" { .\gradlew.bat verifyVersionConsistency --no-daemon }
    Invoke-Checked "Contract compatibility" { .\gradlew.bat checkContractCompatibility --no-daemon }
    Invoke-Checked "Release metadata" { .\gradlew.bat validateReleaseMetadata --no-daemon }
}
if ($WithTests) {
    Invoke-Checked "R13 owner module tests" {
        .\gradlew.bat :cpf-core:test :cpf-common:test :cpf-admin:test :cpf-member:test :cpf-batch:test --no-daemon
    }
}

$dirty = git status --porcelain
if ($LASTEXITCODE -ne 0) { throw "git status failed." }
if ($dirty) {
    Write-Host "[R13] NOTE verification completed with existing worktree changes. Review git status before commit."
}
Write-Host "[R13] Verification command set completed. Runtime/DB/browser/multi-instance scenarios are separate integrated evidence and are not implied by this script."
