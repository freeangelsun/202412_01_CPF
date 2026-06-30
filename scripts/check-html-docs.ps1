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

function ConvertTo-PlainText {
    param([string] $Html)

    $withoutTags = [System.Text.RegularExpressions.Regex]::Replace($Html, "<[^>]+>", "")
    return [System.Net.WebUtility]::HtmlDecode($withoutTags).Trim()
}

function New-UnicodeText {
    param([int[]] $CodePoints)

    return -join ($CodePoints | ForEach-Object { [char] $_ })
}

function Get-TableCells {
    param([string] $RowHtml)

    $cells = New-Object System.Collections.Generic.List[string]
    $matches = [System.Text.RegularExpressions.Regex]::Matches(
        $RowHtml,
        "<td\b[^>]*>(.*?)</td>",
        [System.Text.RegularExpressions.RegexOptions]::IgnoreCase -bor [System.Text.RegularExpressions.RegexOptions]::Singleline
    )
    foreach ($match in $matches) {
        $cells.Add((ConvertTo-PlainText $match.Groups[1].Value))
    }
    return $cells
}

function Test-AllowedStatusValue {
    param(
        [string] $Status,
        [string] $Context
    )

    $statusDone = New-UnicodeText @(0xC644, 0xB8CC)
    $statusPartial = New-UnicodeText @(0xBD80, 0xBD84, 0x20, 0xAD6C, 0xD604)
    $statusNotImplemented = New-UnicodeText @(0xBBF8, 0xAD6C, 0xD604)
    $statusNotVerified = New-UnicodeText @(0xBBF8, 0xAC80, 0xC99D)
    $statusFailed = New-UnicodeText @(0xC2E4, 0xD328)
    $statusNeedsReview = New-UnicodeText @(0xC7AC, 0xD655, 0xC778, 0x20, 0xD544, 0xC694)
    $allowedStatuses = @($statusDone, $statusPartial, $statusNotImplemented, $statusNotVerified, $statusFailed, $statusNeedsReview)
    $forbiddenStatuses = @(
        (New-UnicodeText @(0xC2E4, 0xD658, 0xACBD, 0x20, 0xD544, 0xC694)),
        (New-UnicodeText @(0xC644, 0xB8CC, 0x20, 0xD6C4, 0xBCF4)),
        (New-UnicodeText @(0xD655, 0xC778, 0x20, 0xD544, 0xC694)),
        (New-UnicodeText @(0xB300, 0xAE30)),
        (New-UnicodeText @(0xC9C4, 0xD589, 0xC911)),
        (New-UnicodeText @(0xBCF4, 0xB958))
    )

    if ($forbiddenStatuses -contains $Status) {
        Add-Failure "feature matrix forbidden status value [$Status]: $Context"
        return
    }

    if ($allowedStatuses -notcontains $Status) {
        Add-Failure "feature matrix unknown status value [$Status]: $Context"
    }
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

$featureMatrixFileName = (New-UnicodeText @(0xAE30, 0xB2A5, 0x5F, 0xAD6C, 0xD604, 0x5F, 0xB9E4, 0xD2B8, 0xB9AD, 0xC2A4)) + ".html"
$featureMatrixPath = Join-Path $specsRoot $featureMatrixFileName
if (-not (Test-Path -LiteralPath $featureMatrixPath)) {
    Add-Failure "feature matrix html document missing"
} else {
    $matrixText = [System.IO.File]::ReadAllText($featureMatrixPath, [System.Text.Encoding]::UTF8)
    $rows = [System.Text.RegularExpressions.Regex]::Matches(
        $matrixText,
        "<tr\b[^>]*>(.*?)</tr>",
        [System.Text.RegularExpressions.RegexOptions]::IgnoreCase -bor [System.Text.RegularExpressions.RegexOptions]::Singleline
    )

    foreach ($row in $rows) {
        $cells = Get-TableCells $row.Groups[1].Value
        if ($cells.Count -eq 0) {
            continue
        }

        if ($cells.Count -eq 2) {
            Test-AllowedStatusValue $cells[0] "status-definition-table"
        } elseif ($cells.Count -eq 5) {
            Test-AllowedStatusValue $cells[2] "feature-status-table:$($cells[0])/$($cells[1])"
        } elseif ($cells.Count -eq 4) {
            if ($cells[0] -match '^\d+$') {
                Test-AllowedStatusValue $cells[2] "next-action-table:$($cells[1])"
            } else {
                Test-AllowedStatusValue $cells[1] "verification-status-table:$($cells[0])"
            }
        }
    }
}

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Host $_ }
    exit 1
}

Write-Host "HTML docs check passed."
