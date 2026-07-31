param(
    [Parameter(Mandatory=$false)][string]$ProjectRoot = ".",
    [Parameter(Mandatory=$false)][string]$OverlayRoot = "."
)
$ErrorActionPreference = "Stop"
$project = (Resolve-Path -LiteralPath $ProjectRoot).Path
$overlay = (Resolve-Path -LiteralPath $OverlayRoot).Path
$manifestPath = Join-Path $overlay "cpf-docs/work/manifest/CPF_20260731_GUIDE_R2_MANIFEST.json"
if (-not (Test-Path -LiteralPath $manifestPath)) { throw "Guide R2 manifest not found: $manifestPath" }
$manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 | ConvertFrom-Json
$currentSha = (git -C $project rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0) { throw "git SHA resolution failed" }
git -C $project merge-base --is-ancestor $manifest.baseSha $currentSha
if ($LASTEXITCODE -ne 0) {
    throw "Current HEAD $currentSha is not a descendant of Guide base $($manifest.baseSha)."
}
foreach ($entry in $manifest.files) {
    $source = Join-Path $overlay ($entry.path.Replace('/', [IO.Path]::DirectorySeparatorChar))
    if (-not (Test-Path -LiteralPath $source -PathType Leaf)) { throw "Overlay file missing: $($entry.path)" }
    $hash = (Get-FileHash -LiteralPath $source -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($hash -ne $entry.sha256) { throw "Hash mismatch: $($entry.path)" }
    $target = Join-Path $project ($entry.path.Replace('/', [IO.Path]::DirectorySeparatorChar))
    New-Item -ItemType Directory -Force (Split-Path -Parent $target) | Out-Null
    Copy-Item -LiteralPath $source -Destination $target -Force
}
$manifestTarget = Join-Path $project "cpf-docs/work/manifest/CPF_20260731_GUIDE_R2_MANIFEST.json"
New-Item -ItemType Directory -Force (Split-Path -Parent $manifestTarget) | Out-Null
Copy-Item -LiteralPath $manifestPath -Destination $manifestTarget -Force
Write-Host "CPF Guide R2 overlay applied: $($manifest.files.Count + 1) files"
Write-Host "Git commit/push was not performed."
