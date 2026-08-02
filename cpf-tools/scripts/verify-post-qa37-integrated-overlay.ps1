param(
    [string]$Root = (Get-Location).Path,
    [int]$ExpectedCanonicalCount = 169
)

$ErrorActionPreference = "Stop"
Set-Location -LiteralPath $Root

if (-not (Test-Path -LiteralPath ".git")) {
    throw "Repository Root가 아닙니다: $Root"
}

$protectedPaths = @(
    "cpf-docs/deliverables",
    "cpf-docs/guides",
    "cpf-docs/environment/docker",
    "cpf-tools/environment/docker-development-test"
)

& git diff --check
if ($LASTEXITCODE -ne 0) {
    throw "git diff --check 실패"
}

$protectedChanges = @(& git -c core.quotepath=false status --porcelain=v1 --untracked-files=all -- $protectedPaths)
if ($LASTEXITCODE -ne 0) {
    throw "보호 경로 상태 조회 실패"
}
if ($protectedChanges.Count -gt 0) {
    $protectedChanges | ForEach-Object { Write-Host $_ }
    throw "타 GPT 전담 보호 경로에 Working Tree 변경이 있습니다."
}

$target = ".\cpf-docs\governance\CPF_FINAL_TARGET_REQUIREMENTS.md"
if (-not (Test-Path -LiteralPath $target)) {
    throw "Final Target 문서를 찾을 수 없습니다: $target"
}

$content = Get-Content -LiteralPath $target -Raw -Encoding utf8
$startMarker = "## 22. 상세 Requirement Catalog"
$endMarker = "## 23. Legacy Alias Mapping"
$start = $content.IndexOf($startMarker, [System.StringComparison]::Ordinal)
$end = $content.IndexOf($endMarker, [System.StringComparison]::Ordinal)

if ($start -lt 0 -or $end -le $start) {
    throw "Canonical Catalog 구간을 찾을 수 없습니다."
}

$catalog = $content.Substring($start, $end - $start)
$ids = @(
    [regex]::Matches($catalog, '(?m)^\| `([A-Z][A-Z0-9-]+)` \|') |
        ForEach-Object { $_.Groups[1].Value } |
        Sort-Object -Unique
)

if ($ids.Count -ne $ExpectedCanonicalCount) {
    throw "Canonical Requirement Count 불일치: actual=$($ids.Count), expected=$ExpectedCanonicalCount"
}

$requiredIds = @(
    "ARCH-STARTER",
    "DB-FRESH",
    "EVENT-MQ",
    "EVENT-JMS",
    "EVENT-IBM-MQ",
    "EVENT-AMQP",
    "EXS-TCP"
)
$missing = @($requiredIds | Where-Object { $_ -notin $ids })
if ($missing.Count -gt 0) {
    throw "복구 Requirement 누락: $($missing -join ', ')"
}

$manifestPath = ".\cpf-docs\work\manifest\CPF_20260802_05_POST_QA37_PACKAGE_MANIFEST.json"
if (-not (Test-Path -LiteralPath $manifestPath)) {
    throw "Package Manifest를 찾을 수 없습니다."
}
$manifest = Get-Content -LiteralPath $manifestPath -Raw -Encoding utf8 | ConvertFrom-Json
$manifestPaths = @($manifest.files | ForEach-Object { [string]$_.path })

foreach ($path in $manifestPaths) {
    $normalized = $path.Replace('\', '/')
    if (
        $normalized.StartsWith("cpf-docs/deliverables/") -or
        $normalized.StartsWith("cpf-docs/guides/") -or
        $normalized.StartsWith("cpf-docs/environment/docker/") -or
        $normalized.StartsWith("cpf-tools/environment/docker-development-test/")
    ) {
        throw "Package Manifest가 보호 경로를 포함합니다: $normalized"
    }
    if (-not (Test-Path -LiteralPath (Join-Path $Root $path))) {
        throw "Package 파일 누락: $path"
    }
}

$head = (& git rev-parse HEAD).Trim()
$remote = (& git rev-parse origin/master).Trim()
if ($LASTEXITCODE -ne 0) {
    throw "Git SHA 조회 실패"
}

Write-Host "POST-QA37 Overlay Verification PASS"
Write-Host "  HEAD                       : $head"
Write-Host "  origin/master              : $remote"
Write-Host "  Canonical Requirement Count: $($ids.Count)"
Write-Host "  Protected Path Changes     : 0"
Write-Host "  Package Files              : $($manifestPaths.Count)"
