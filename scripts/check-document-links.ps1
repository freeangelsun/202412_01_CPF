param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "build/quality-gate")
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$failures = New-Object System.Collections.Generic.List[object]
$checked = New-Object System.Collections.Generic.List[object]

function Add-Target {
    param(
        [string] $Source,
        [string] $Target
    )

    $candidate = $Target.Trim().Trim('<', '>')
    if ([string]::IsNullOrWhiteSpace($candidate) `
            -or $candidate.StartsWith('#') `
            -or $candidate.StartsWith('/') `
            -or $candidate.StartsWith('//') `
            -or $candidate.StartsWith('${') `
            -or $candidate -match '^[a-zA-Z][a-zA-Z0-9+.-]*:') {
        return
    }

    $candidate = ($candidate -split '[?#]', 2)[0]
    if ([string]::IsNullOrWhiteSpace($candidate) -or $candidate.Contains('*')) {
        return
    }

    try {
        $candidate = [System.Uri]::UnescapeDataString($candidate)
    } catch {
        $failures.Add([pscustomobject]@{ source = $Source; target = $Target; reason = "URL 디코딩 실패" }) | Out-Null
        return
    }

    if ([System.IO.Path]::IsPathRooted($candidate)) {
        $failures.Add([pscustomobject]@{ source = $Source; target = $Target; reason = "로컬 절대 경로 사용" }) | Out-Null
        return
    }

    $sourceDirectory = Split-Path -Parent (Join-Path $Root $Source)
    $resolved = [System.IO.Path]::GetFullPath((Join-Path $sourceDirectory $candidate))
    $exists = Test-Path -LiteralPath $resolved
    $checked.Add([pscustomobject]@{
        source = $Source
        target = $Target
        resolved = $resolved.Substring($Root.Length).TrimStart('\').Replace('\', '/')
        exists = $exists
    }) | Out-Null
    if (-not $exists) {
        $failures.Add([pscustomobject]@{ source = $Source; target = $Target; reason = "대상 파일 없음" }) | Out-Null
    }
}

$documentFiles = @(& git -C $Root -c core.quotepath=false ls-files -- '*.md' '*.html') |
    Where-Object { Test-Path -LiteralPath (Join-Path $Root $_) }

foreach ($source in $documentFiles) {
    $text = [System.IO.File]::ReadAllText((Join-Path $Root $source), [System.Text.Encoding]::UTF8)
    foreach ($match in [regex]::Matches($text, '(?m)(?<!\!)\[[^\]]*\]\((?<target>[^)]+)\)')) {
        $target = $match.Groups['target'].Value.Trim()
        if ($target -match '^(?<path><[^>]+>|\S+)(?:\s+["''][^"'']*["''])?$') {
            Add-Target $source $Matches['path']
        }
    }
    foreach ($match in [regex]::Matches($text, '(?i)(?:href|src)\s*=\s*["''](?<target>[^"'']+)["'']')) {
        Add-Target $source $match.Groups['target'].Value
    }
}

$status = if ($failures.Count -eq 0) { "완료" } else { "실패" }
$result = [ordered]@{
    generatedAt = (Get-Date).ToString('o')
    status = $status
    documentCount = $documentFiles.Count
    localLinkCount = $checked.Count
    failureCount = $failures.Count
    failures = $failures
    checked = $checked
}
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
[System.IO.File]::WriteAllText(
    (Join-Path $ResultDir 'document-link-check.sanitized.json'),
    ($result | ConvertTo-Json -Depth 10),
    $Utf8NoBom)

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Error ("문서 링크 오류: {0} -> {1} ({2})" -f $_.source, $_.target, $_.reason) }
    exit 1
}

Write-Host ("Document link check passed: documents={0}, localLinks={1}" -f $documentFiles.Count, $checked.Count)
