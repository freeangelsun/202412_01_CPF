param([string]$RepoRoot = (Resolve-Path "$PSScriptRoot\..\..").Path)
$ErrorActionPreference = "Stop"
$errors = @()
$domainDirs = Get-ChildItem $RepoRoot -Directory | Where-Object {
    $_.Name -match '^cpf-(member|account|reference|[a-z][a-z0-9-]+)$' -and
    $_.Name -notin @('cpf-core','cpf-common','cpf-admin','cpf-biz-admin','cpf-batch','cpf-gateway','cpf-tools','cpf-docs')
}
foreach ($dir in $domainDirs) {
    $hits = Get-ChildItem $dir.FullName -Recurse -File -Include *.java,*.gradle,*.kts |
        Select-String -Pattern 'com\.cpf\.core\.common\.'
    if ($hits) { $errors += $hits }
}
if ($errors.Count -gt 0) {
    $errors | Format-Table Path,LineNumber,Line -AutoSize
    throw "Generated/Business Domain imports cpf-core internal implementation."
}
Write-Host "Domain federation boundary check: PASS"
