[CmdletBinding()]
param(
    [string] $Root = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path,
    [Parameter(Mandatory=$true)][string] $BaseSha,
    [string] $OutputZip = (Join-Path (Get-Location) 'CPF_QA32_DEVELOPMENT_RESULT_ROOT_OVERLAY.zip')
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Invoke-GitLines([string[]] $Arguments) {
    $output = & git -C $Root @Arguments 2>&1
    if ($LASTEXITCODE -ne 0) { throw "git failed: git $($Arguments -join ' ')`n$($output -join [Environment]::NewLine)" }
    return @($output | ForEach-Object { [string] $_ })
}

$null = Invoke-GitLines @('rev-parse','--verify',"$BaseSha^{commit}")
$head = (Invoke-GitLines @('rev-parse','HEAD') | Select-Object -First 1).Trim()
$changed = @(
    Invoke-GitLines @('diff','--name-only','--diff-filter=ACMRTUXB',"$BaseSha..HEAD")
    Invoke-GitLines @('diff','--name-only','--diff-filter=ACMRTUXB')
    Invoke-GitLines @('diff','--name-only','--diff-filter=ACMRTUXB','--cached')
    Invoke-GitLines @('ls-files','--others','--exclude-standard')
) | Where-Object { -not [string]::IsNullOrWhiteSpace($_) } | Sort-Object -Unique

$requiredResults = @(
    'cpf-docs/work/review/CPF_20260730_QA32_PRE_DEVELOPMENT_REVIEW.md',
    'cpf-docs/work/review/CPF_20260730_QA32_DEVELOPMENT_COMPLETION_REPORT.md',
    'cpf-docs/quality/CPF_20260730_QA32_RESULT_MATRIX.csv',
    'cpf-docs/quality/CPF_20260730_QA32_UNRESOLVED_REGISTER.csv',
    'cpf-docs/quality/CPF_20260730_QA32_OSS_MIGRATION_RESULT.csv',
    'cpf-docs/work/handover/CPF_20260730_QA32_DEVELOPMENT_HANDOVER_RESULT.md',
    'cpf-docs/work/current/CPF_20260730_QA32_CODEX_REVIEW_READY.md'
)
foreach ($required in $requiredResults) {
    if (-not (Test-Path -LiteralPath (Join-Path $Root ($required -replace '/', [IO.Path]::DirectorySeparatorChar)) -PathType Leaf)) {
        throw "Required QA32 result file is missing: $required"
    }
}

$changed += $requiredResults
$evidenceRoot = Join-Path $Root 'cpf-docs/evidence/current'
if (Test-Path -LiteralPath $evidenceRoot -PathType Container) {
    $changed += Get-ChildItem -LiteralPath $evidenceRoot -File -Recurse | ForEach-Object {
        [IO.Path]::GetRelativePath($Root, $_.FullName).Replace('\','/')
    }
}
$changed = @($changed | Sort-Object -Unique)

$excludedPatterns = @(
    '(^|/)[.]git(/|$)', '(^|/)node_modules(/|$)', '(^|/)(build|out|dist|target|[.]gradle|[.]npm|coverage)(/|$)',
    '[.]key$', '[.]pem$', '[.]p12$', '[.]pfx$', '[.]jks$', '[.]keystore$', '[.]env$',
    '(^|/)README[^/]*$', '^cpf-docs/guides/', '^cpf-tools/README[.]md$',
    '^cpf-docs/assets/readme/', '^cpf-docs/work/overlay/20260730-readme-guides/'
)
$files = @()
foreach ($relative in $changed) {
    $normalized = $relative.Replace('\','/')
    if ($excludedPatterns | Where-Object { $normalized -match $_ }) { continue }
    $full = Join-Path $Root ($normalized -replace '/', [IO.Path]::DirectorySeparatorChar)
    if (-not (Test-Path -LiteralPath $full -PathType Leaf)) { continue }
    $files += [ordered]@{
        path = $normalized
        size = (Get-Item -LiteralPath $full).Length
        sha256 = (Get-FileHash -LiteralPath $full -Algorithm SHA256).Hash.ToLowerInvariant()
    }
}
if ($files.Count -eq 0) { throw 'No result files were selected.' }

$work = Join-Path ([IO.Path]::GetTempPath()) ("cpf-qa32-result-" + [Guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Force -Path $work | Out-Null
try {
    foreach ($entry in $files) {
        $source = Join-Path $Root ($entry.path -replace '/', [IO.Path]::DirectorySeparatorChar)
        $target = Join-Path $work ($entry.path -replace '/', [IO.Path]::DirectorySeparatorChar)
        New-Item -ItemType Directory -Force -Path (Split-Path -Parent $target) | Out-Null
        Copy-Item -LiteralPath $source -Destination $target
    }
    $manifest = [ordered]@{
        schemaVersion = 'CPF-QA32-DEVELOPMENT-RESULT-1'
        repository = 'freeangelsun/202412_01_CPF'
        baseSha = $BaseSha
        headSha = $head
        createdAt = [DateTimeOffset]::Now.ToString('o')
        fileCount = $files.Count
        files = $files
    }
    $manifestRelative = 'cpf-docs/work/manifest/CPF_20260730_QA32_DEVELOPMENT_RESULT_MANIFEST.json'
    $manifestPath = Join-Path $work ($manifestRelative -replace '/', [IO.Path]::DirectorySeparatorChar)
    New-Item -ItemType Directory -Force -Path (Split-Path -Parent $manifestPath) | Out-Null
    [IO.File]::WriteAllText($manifestPath, ($manifest | ConvertTo-Json -Depth 10), [Text.UTF8Encoding]::new($false))
    if (Test-Path -LiteralPath $OutputZip) { Remove-Item -LiteralPath $OutputZip -Force }
    Compress-Archive -Path (Join-Path $work '*') -DestinationPath $OutputZip -CompressionLevel Optimal
    $zipHash = (Get-FileHash -LiteralPath $OutputZip -Algorithm SHA256).Hash.ToLowerInvariant()
    Write-Host "CPF_QA32_RESULT_PACKAGE_PASS zip=$OutputZip files=$($files.Count + 1) sha256=$zipHash base=$BaseSha head=$head"
} finally {
    if (Test-Path -LiteralPath $work) { Remove-Item -LiteralPath $work -Recurse -Force }
}
