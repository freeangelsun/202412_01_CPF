param(
    [Parameter(Mandatory=$true)][string] $BaseSha,
    [string] $ProjectRoot = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $OutputDirectory = (Join-Path $HOME "Downloads"),
    [string] $BatchId = "QA31-DEVELOPMENT"
)
$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
if ($BaseSha -notmatch '^[0-9a-fA-F]{40}$') { throw "BaseSha must be a full SHA." }
$ProjectRoot = (Resolve-Path -LiteralPath $ProjectRoot).Path
$head = (& git -C $ProjectRoot rev-parse HEAD).Trim()
$changed = @(
    & git -C $ProjectRoot diff --name-only $BaseSha
    & git -C $ProjectRoot diff --name-only --cached
    & git -C $ProjectRoot ls-files --others --exclude-standard
) | Where-Object { $_ -and $_.Trim() } | Sort-Object -Unique

$requiredResults = @(
    "cpf-docs/work/review/CPF_20260730_QA31_PRE_DEVELOPMENT_REVIEW.md",
    "cpf-docs/work/review/CPF_20260730_QA31_DEVELOPMENT_COMPLETION_REPORT.md",
    "cpf-docs/quality/CPF_20260730_QA31_RESULT_MATRIX.csv",
    "cpf-docs/quality/CPF_20260730_QA31_UNRESOLVED_REGISTER.csv",
    "cpf-docs/work/handover/CPF_20260730_QA31_DEVELOPMENT_HANDOVER.md",
    "cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md",
    "cpf-docs/work/current/CPF_20260730_QA31_CODEX_REVIEW_READY.md",
    "cpf-docs/work/manifest/CPF_20260730_QA31_DELETE_MANIFEST.txt",
    "cpf-docs/work/manifest/CPF_20260730_QA31_REQUEST_INTEGRITY.json",
    "cpf-tools/scripts/apply-cpf-qa31-development-result.ps1",
    "cpf-tools/scripts/verify-cpf-qa31-development-result.ps1",
    "cpf-tools/scripts/verify-cpf-qa31-development.py",
    "cpf-tools/scripts/verify-cpf-reference-qa31-coverage.py",
    "cpf-tools/scripts/verify-cpf-bza-qa31-coverage.py"
)
foreach ($path in $requiredResults) {
    if (-not (Test-Path -LiteralPath (Join-Path $ProjectRoot $path) -PathType Leaf)) {
        throw "Required development result is missing: $path"
    }
}
$verifyScript = Join-Path $ProjectRoot "cpf-tools/scripts/verify-cpf-qa31-development-result.ps1"
& pwsh -NoProfile -File $verifyScript -Root $ProjectRoot -BaseSha $BaseSha -Mode full
if ($LASTEXITCODE -ne 0) { throw "QA31 development gate failed before packaging." }

$changed += $requiredResults
$currentEvidence = Join-Path $ProjectRoot "cpf-docs/evidence/current"
if (Test-Path -LiteralPath $currentEvidence -PathType Container) {
    $changed += Get-ChildItem -LiteralPath $currentEvidence -Recurse -File |
        ForEach-Object { [IO.Path]::GetRelativePath($ProjectRoot, $_.FullName).Replace('\','/') }
}
$changed = $changed | Sort-Object -Unique

$excludedPatterns = @(
    '^\.git/', '(^|/)build/', '(^|/)node_modules/', '(^|/)\.idea/', '(^|/)\.gradle/',
    '\.(pem|p12|pfx|jks|key)$'
)
$readmeGuidePatterns = @('(^|/)README[^/]*$', '^cpf-docs/guides/', '^cpf-tools/README\.md$', '^cpf-docs/assets/readme/', '^cpf-docs/work/overlay/20260730-readme-guides/')
$included = [System.Collections.Generic.List[string]]::new()
$excluded = [System.Collections.Generic.List[string]]::new()
foreach ($rel in $changed) {
    $normalized = $rel.Replace('\','/')
    if ($excludedPatterns | Where-Object { $normalized -match $_ }) {
        $excluded.Add("$normalized :: cache/secret/excluded") | Out-Null
        continue
    }
    if ($readmeGuidePatterns | Where-Object { $normalized -match $_ }) {
        $excluded.Add("$normalized :: README/Guide handled by separate AI") | Out-Null
        continue
    }
    $full = Join-Path $ProjectRoot $normalized
    if (Test-Path -LiteralPath $full -PathType Leaf) { $included.Add($normalized) | Out-Null }
}

$stamp = Get-Date -Format "yyyyMMdd_HHmmss"
$stage = Join-Path $env:TEMP "CPF_QA31_RESULT_$stamp"
Remove-Item -LiteralPath $stage -Recurse -Force -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path $stage -Force | Out-Null

$manifestRows = [System.Collections.Generic.List[object]]::new()
foreach ($rel in $included) {
    $src = Join-Path $ProjectRoot $rel
    $dst = Join-Path $stage $rel
    New-Item -ItemType Directory -Path (Split-Path $dst -Parent) -Force | Out-Null
    Copy-Item -LiteralPath $src -Destination $dst -Force
    $hash = (Get-FileHash -LiteralPath $dst -Algorithm SHA256).Hash.ToLowerInvariant()
    $manifestRows.Add([pscustomobject]@{ path=$rel; sha256=$hash; bytes=(Get-Item $dst).Length }) | Out-Null
}
$metaDir = Join-Path $stage "cpf-docs/evidence/package"
New-Item -ItemType Directory -Path $metaDir -Force | Out-Null
$metadata = [ordered]@{
    batchId = $BatchId
    generatedAt = [DateTimeOffset]::Now.ToString("o")
    baseSha = $BaseSha.ToLowerInvariant()
    headSha = $head
    workingTreeClean = ((& git -C $ProjectRoot status --porcelain).Count -eq 0)
    includedFileCount = $manifestRows.Count
    includedFiles = @($manifestRows)
    excludedFiles = @($excluded)
    readmeGuidePolicy = "Excluded; maintained by a separate AI model"
    sensitiveDataRemoved = $true
}
$metadata | ConvertTo-Json -Depth 20 | Set-Content -LiteralPath (Join-Path $metaDir "qa31-package-manifest.json") -Encoding UTF8

New-Item -ItemType Directory -Path $OutputDirectory -Force | Out-Null
$zip = Join-Path $OutputDirectory ("CPF_" + $BatchId + "_ROOT_OVERLAY_" + $stamp + ".zip")
Compress-Archive -Path (Join-Path $stage '*') -DestinationPath $zip -Force
$zipHash = (Get-FileHash -LiteralPath $zip -Algorithm SHA256).Hash.ToLowerInvariant()
Write-Host "ZIP=$zip"
Write-Host "SHA256=$zipHash"
Write-Host "FILES=$($manifestRows.Count)"
