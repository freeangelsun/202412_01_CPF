param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path
)

$ErrorActionPreference = "Stop"

$specsRoot = Join-Path $Root "specs"
$failures = New-Object System.Collections.Generic.List[string]

function Add-Failure {
    param([string] $Message)
    $failures.Add($Message)
}

function Get-RelativePath {
    param([System.IO.FileInfo] $File)
    return $File.FullName.Substring($Root.Length)
}

if (-not (Test-Path -LiteralPath $specsRoot)) {
    throw "specs directory not found: $specsRoot"
}

# 상세 가이드는 HTML로 관리합니다. specs 하위 Markdown 문서가 생기면 문서 체계가 다시 흩어지므로 실패 처리합니다.
Get-ChildItem -LiteralPath $specsRoot -Recurse -File -Filter "*.md" | ForEach-Object {
    Add-Failure "specs markdown document remains: $($_.FullName.Substring($Root.Length))"
}

$htmlTargets = New-Object System.Collections.Generic.List[System.IO.FileInfo]
Get-ChildItem -LiteralPath $specsRoot -Recurse -File -Filter "*.html" | ForEach-Object {
    $htmlTargets.Add($_)
}

$reportPath = Join-Path $Root "CPF_STABILIZATION_REPORT.html"
if (Test-Path -LiteralPath $reportPath) {
    $htmlTargets.Add((Get-Item -LiteralPath $reportPath))
}

if (Test-Path -LiteralPath (Join-Path $Root "CPF_STABILIZATION_REPORT.md")) {
    Add-Failure "root markdown report remains: \CPF_STABILIZATION_REPORT.md"
}

if (Test-Path -LiteralPath (Join-Path $Root "CPF_STABILIZATION_CHANGED_FILES.txt")) {
    Add-Failure "changed-files artifact remains: \CPF_STABILIZATION_CHANGED_FILES.txt"
}

$htmlTargets | ForEach-Object {
    $relative = Get-RelativePath $_
    $text = [System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8)
    $lowerText = $text.ToLowerInvariant()
    $firstNonBlankLine = ($text -split "\r?\n" | Where-Object { $_.Trim().Length -gt 0 } | Select-Object -First 1)

    if ($null -eq $firstNonBlankLine) {
        Add-Failure "html document is empty: $relative"
        return
    }

    if ($firstNonBlankLine.TrimStart().StartsWith("#")) {
        Add-Failure "html document starts with markdown heading: $relative"
    }

    foreach ($required in @("<!doctype html", "<html", "<head", "<title", "<body", "<main", "<section")) {
        if ($lowerText.IndexOf($required) -lt 0) {
            Add-Failure "html required marker missing $($required): $relative"
        }
    }

    if ($lowerText -notmatch '<meta\s+charset\s*=\s*["'']?utf-8["'']?') {
        Add-Failure "html utf-8 charset marker missing: $relative"
    }

    if ($text -match '```') {
        Add-Failure "markdown code fence remains: $relative"
    }
    if ($text -match '(?m)^\s{0,3}#{1,6}\s+\S') {
        Add-Failure "markdown heading remains: $relative"
    }
    if ($text -match '(?m)^\s*\|.+\|\s*$') {
        Add-Failure "markdown table remains: $relative"
    }
    if ($text -match '\[[^\]]+\]\([^)]+\.md\)') {
        Add-Failure "markdown md link remains: $relative"
    }
    if ($text -match '\.md\b') {
        Add-Failure "md file reference remains: $relative"
    }
}

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Host $_ }
    exit 1
}

Write-Host "HTML docs check passed."
