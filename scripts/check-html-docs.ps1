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

$script:StatusDone = New-UnicodeText @(0xC644, 0xB8CC)
$script:StatusPartial = New-UnicodeText @(0xBD80, 0xBD84, 0x20, 0xAD6C, 0xD604)
$script:StatusNotImplemented = New-UnicodeText @(0xBBF8, 0xAD6C, 0xD604)
$script:StatusNotVerified = New-UnicodeText @(0xBBF8, 0xAC80, 0xC99D)
$script:StatusFailed = New-UnicodeText @(0xC2E4, 0xD328)
$script:StatusNeedsReview = New-UnicodeText @(0xC7AC, 0xD655, 0xC778, 0x20, 0xD544, 0xC694)
$script:AllowedStatuses = @(
    $script:StatusDone,
    $script:StatusPartial,
    $script:StatusNotImplemented,
    $script:StatusNotVerified,
    $script:StatusFailed,
    $script:StatusNeedsReview
)
$script:ForbiddenStatusValues = @(
    (New-UnicodeText @(0xC2E4, 0xD658, 0xACBD, 0x20, 0xD544, 0xC694)),
    (New-UnicodeText @(0xC644, 0xB8CC, 0x20, 0xD6C4, 0xBCF4)),
    (New-UnicodeText @(0xD655, 0xC778, 0x20, 0xD544, 0xC694)),
    (New-UnicodeText @(0xC9C4, 0xD589, 0xC911)),
    (New-UnicodeText @(0xBCF4, 0xB958)),
    (New-UnicodeText @(0xB300, 0xAE30)),
    (New-UnicodeText @(0xC791, 0xC5C5, 0xC911)),
    (New-UnicodeText @(0xC131, 0xACF5)),
    (New-UnicodeText @(0xAC80, 0xD1A0, 0xC911))
)
$script:RequiredCheckIds = @(
    "edu-mapper-db-slice",
    "mariadb-full-install",
    "adm-runtime",
    "adm-permission-runtime",
    "openapi-runtime",
    "adm-browser-click",
    "standard-header-e2e",
    "redis-kafka-mq-broker",
    "quality-gate",
    "check-html-docs",
    "check-feature-evidence",
    "check-utf8"
)

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

function Get-AttributeValue {
    param(
        [string] $Tag,
        [string] $Name
    )

    $pattern = "\b" + [System.Text.RegularExpressions.Regex]::Escape($Name) + "\s*=\s*[""']([^""']+)[""']"
    $match = [System.Text.RegularExpressions.Regex]::Match(
        $Tag,
        $pattern,
        [System.Text.RegularExpressions.RegexOptions]::IgnoreCase
    )
    if ($match.Success) {
        return [System.Net.WebUtility]::HtmlDecode($match.Groups[1].Value)
    }
    return $null
}

function Test-AllowedStatusValue {
    param(
        [string] $Status,
        [string] $Context
    )

    if ($script:ForbiddenStatusValues -contains $Status) {
        Add-Failure "feature matrix forbidden status value [$Status]: $Context"
        return
    }

    if ($script:AllowedStatuses -notcontains $Status) {
        Add-Failure "feature matrix unknown status value [$Status]: $Context"
    }
}

function Test-StatusCell {
    param(
        [string] $CellTag,
        [string] $CellBody,
        [string] $Context,
        [bool] $RequireMarker
    )

    $displayText = ConvertTo-PlainText $CellBody
    $dataStatus = Get-AttributeValue $CellTag "data-status"
    $classValue = Get-AttributeValue $CellTag "class"
    $hasStatusClass = $false
    if ($classValue -ne $null) {
        $hasStatusClass = ($classValue -split "\s+") -contains "status"
    }

    if ($dataStatus -ne $null) {
        Test-AllowedStatusValue $dataStatus "$Context:data-status"
        Test-AllowedStatusValue $displayText "$Context:display"
        if ($dataStatus -ne $displayText) {
            Add-Failure "status marker mismatch [$Context]: data-status=[$dataStatus], display=[$displayText]"
        }
        if (-not $hasStatusClass) {
            Add-Failure "status marker class missing [$Context]"
        }
        return
    }

    if ($RequireMarker -or ($script:AllowedStatuses -contains $displayText) -or ($script:ForbiddenStatusValues -contains $displayText)) {
        Add-Failure "status cell data-status missing [$Context]: [$displayText]"
    }
}

function Test-MatrixRowStatusMarkers {
    param([string] $MatrixText)

    $rows = [System.Text.RegularExpressions.Regex]::Matches(
        $MatrixText,
        "<tr\b[^>]*>(.*?)</tr>",
        [System.Text.RegularExpressions.RegexOptions]::IgnoreCase -bor [System.Text.RegularExpressions.RegexOptions]::Singleline
    )

    foreach ($row in $rows) {
        $rowHtml = $row.Groups[1].Value
        $cells = [System.Text.RegularExpressions.Regex]::Matches(
            $rowHtml,
            "(<td\b[^>]*>)(.*?)</td>",
            [System.Text.RegularExpressions.RegexOptions]::IgnoreCase -bor [System.Text.RegularExpressions.RegexOptions]::Singleline
        )
        if ($cells.Count -eq 0) {
            continue
        }

        if ($cells.Count -eq 2) {
            Test-StatusCell $cells[0].Groups[1].Value $cells[0].Groups[2].Value "matrix-status-definition" $true
        } elseif ($cells.Count -eq 5) {
            Test-StatusCell $cells[2].Groups[1].Value $cells[2].Groups[2].Value "matrix-feature-status" $true
        } elseif ($cells.Count -eq 4) {
            $firstText = ConvertTo-PlainText $cells[0].Groups[2].Value
            if ($firstText -match '^\d+$') {
                Test-StatusCell $cells[2].Groups[1].Value $cells[2].Groups[2].Value "matrix-next-action-status" $true
            } else {
                Test-StatusCell $cells[1].Groups[1].Value $cells[1].Groups[2].Value "matrix-verification-status" $true
            }
        } else {
            for ($index = 0; $index -lt $cells.Count; $index++) {
                Test-StatusCell $cells[$index].Groups[1].Value $cells[$index].Groups[2].Value "matrix-cell-$index" $false
            }
        }
    }
}

function Get-CheckStatusMap {
    param(
        [string] $Html,
        [string] $Name
    )

    $map = @{}
    $rows = [System.Text.RegularExpressions.Regex]::Matches(
        $Html,
        "(<tr\b[^>]*data-check-id\s*=\s*[""'][^""']+[""'][^>]*>)(.*?)</tr>",
        [System.Text.RegularExpressions.RegexOptions]::IgnoreCase -bor [System.Text.RegularExpressions.RegexOptions]::Singleline
    )

    foreach ($row in $rows) {
        $rowTag = $row.Groups[1].Value
        $checkId = Get-AttributeValue $rowTag "data-check-id"
        if ([string]::IsNullOrWhiteSpace($checkId)) {
            Add-Failure "check row id missing: $Name"
            continue
        }

        $statusCells = [System.Text.RegularExpressions.Regex]::Matches(
            $row.Groups[2].Value,
            "(<td\b[^>]*class\s*=\s*[""'][^""']*\bstatus\b[^""']*[""'][^>]*>)(.*?)</td>",
            [System.Text.RegularExpressions.RegexOptions]::IgnoreCase -bor [System.Text.RegularExpressions.RegexOptions]::Singleline
        )
        if ($statusCells.Count -ne 1) {
            Add-Failure "check row status cell count invalid [${Name}:$checkId]: $($statusCells.Count)"
            continue
        }

        $cellTag = $statusCells[0].Groups[1].Value
        $cellBody = $statusCells[0].Groups[2].Value
        Test-StatusCell $cellTag $cellBody "${Name}:$checkId" $true
        $map[$checkId] = Get-AttributeValue $cellTag "data-status"
    }

    return $map
}

function Test-RequiredCheckStatusMatch {
    param(
        [string] $MatrixText,
        [string] $ReportText
    )

    $matrixStatusMap = Get-CheckStatusMap $MatrixText "feature-matrix"
    $reportStatusMap = Get-MarkdownCheckStatusMap $ReportText "stabilization-report"

    foreach ($checkId in $script:RequiredCheckIds) {
        if (-not $matrixStatusMap.ContainsKey($checkId)) {
            Add-Failure "feature matrix check id missing: $checkId"
            continue
        }
        if (-not $reportStatusMap.ContainsKey($checkId)) {
            Add-Failure "stabilization report check id missing: $checkId"
            continue
        }
        if ($matrixStatusMap[$checkId] -ne $reportStatusMap[$checkId]) {
            Add-Failure "check status mismatch [$checkId]: matrix=[$($matrixStatusMap[$checkId])], report=[$($reportStatusMap[$checkId])]"
        }
    }
}

function Get-MarkdownCheckStatusMap {
    param(
        [string] $Markdown,
        [string] $Name
    )

    $map = @{}
    foreach ($line in ($Markdown -split "\r?\n")) {
        $trimmed = $line.Trim()
        if (-not $trimmed.StartsWith("|")) {
            continue
        }
        if ($trimmed -match '^\|\s*-+') {
            continue
        }
        $cells = @($trimmed.Trim("|") -split "\|" | ForEach-Object { $_.Trim() })
        if ($cells.Count -lt 3) {
            continue
        }
        $checkId = $cells[0]
        $status = $cells[1]
        if ($script:RequiredCheckIds -contains $checkId) {
            Test-AllowedStatusValue $status "${Name}:$checkId"
            $map[$checkId] = $status
        }
    }
    return $map
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

$reportPath = Join-Path $Root "CPF_STABILIZATION_REPORT.md"
if (Test-Path -LiteralPath (Join-Path $Root "CPF_STABILIZATION_REPORT.html")) {
    Add-Failure "root html stabilization report remains: \CPF_STABILIZATION_REPORT.html"
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
    Test-MatrixRowStatusMarkers $matrixText

    if (Test-Path -LiteralPath $reportPath) {
        $reportText = [System.IO.File]::ReadAllText($reportPath, [System.Text.Encoding]::UTF8)
        Test-RequiredCheckStatusMatch $matrixText $reportText
    } else {
        Add-Failure "stabilization report markdown document missing: \CPF_STABILIZATION_REPORT.md"
    }
}

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Host $_ }
    exit 1
}

Write-Host "HTML docs check passed."
