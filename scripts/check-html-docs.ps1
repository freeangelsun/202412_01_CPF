param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path
)

$ErrorActionPreference = "Stop"

# specs 문서는 브라우저에서 바로 확인 가능한 HTML이어야 하므로 Markdown 잔재를 실패로 처리합니다.
$specsRoot = Join-Path $Root "specs"
$failures = New-Object System.Collections.Generic.List[string]

if (-not (Test-Path -LiteralPath $specsRoot)) {
    throw "specs directory not found: $specsRoot"
}

Get-ChildItem -LiteralPath $specsRoot -Recurse -File -Filter "*.md" | ForEach-Object {
    $failures.Add("specs 하위 md 문서 잔재: $($_.FullName.Substring($Root.Length))")
}

Get-ChildItem -LiteralPath $specsRoot -Recurse -File -Filter "*.html" | ForEach-Object {
    $relative = $_.FullName.Substring($Root.Length)
    $text = [System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8)

    foreach ($required in @("<!doctype html", "<html", "<head", "<body")) {
        if ($text.ToLowerInvariant().IndexOf($required) -lt 0) {
            $failures.Add("html required marker missing $($required): $relative")
        }
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
