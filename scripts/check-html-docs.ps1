param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path
)

$ErrorActionPreference = "Stop"

# specs 문서와 루트 안정화 리포트는 브라우저에서 바로 확인 가능한 HTML이어야 하므로 Markdown 잔재를 실패로 처리합니다.
$specsRoot = Join-Path $Root "specs"
$failures = New-Object System.Collections.Generic.List[string]

if (-not (Test-Path -LiteralPath $specsRoot)) {
    throw "specs directory not found: $specsRoot"
}

Get-ChildItem -LiteralPath $specsRoot -Recurse -File -Filter "*.md" | ForEach-Object {
    $failures.Add("specs 하위 md 문서 잔재: $($_.FullName.Substring($Root.Length))")
}

$htmlTargets = New-Object System.Collections.Generic.List[System.IO.FileInfo]
Get-ChildItem -LiteralPath $specsRoot -Recurse -File -Filter "*.html" | ForEach-Object {
    $htmlTargets.Add($_)
}

$reportPath = Join-Path $Root "CPF_STABILIZATION_REPORT.html"
if (Test-Path -LiteralPath $reportPath) {
    $htmlTargets.Add((Get-Item -LiteralPath $reportPath))
}

$markdownReportPath = Join-Path $Root "CPF_STABILIZATION_REPORT.md"
if (Test-Path -LiteralPath $markdownReportPath) {
    $failures.Add("루트 markdown 리포트 잔재: \CPF_STABILIZATION_REPORT.md")
}

$changedFilesPath = Join-Path $Root "CPF_STABILIZATION_CHANGED_FILES.txt"
if (Test-Path -LiteralPath $changedFilesPath) {
    $failures.Add("수정파일 목록 산출물 잔재: \CPF_STABILIZATION_CHANGED_FILES.txt")
}

$htmlTargets | ForEach-Object {
    $relative = $_.FullName.Substring($Root.Length)
    $text = [System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8)

    foreach ($required in @("<!doctype html", "<html", "<head", "<title", "<body", "<main", "<section")) {
        if ($text.ToLowerInvariant().IndexOf($required) -lt 0) {
            $failures.Add("html required marker missing $($required): $relative")
        }
    }
    if ($text -match '```') {
        $failures.Add("markdown code fence remains: $relative")
    }
    if ($text -match '(?m)^\s{0,3}#{1,6}\s+\S') {
        $failures.Add("markdown heading remains: $relative")
    }
    if ($text -match '(?m)^\s*\|.+\|\s*$') {
        $failures.Add("markdown table remains: $relative")
    }
    if ($text -match '\[[^\]]+\]\([^)]+\.md\)') {
        $failures.Add("markdown md link remains: $relative")
    }
    if ($text -match '\.md\b') {
        $failures.Add("md file reference remains: $relative")
    }
}

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Host $_ }
    exit 1
}

Write-Host "HTML docs check passed."
