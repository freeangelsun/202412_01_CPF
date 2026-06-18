param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [switch] $CheckMojibake
)

$ErrorActionPreference = "Stop"

$textExtensions = @(
    ".bat", ".cmd", ".css", ".csv", ".gradle", ".html", ".java", ".js",
    ".json", ".md", ".properties", ".ps1", ".sql", ".txt", ".xml", ".yml", ".yaml"
)
$skipDirectories = @(
    "\.git\", "\.gradle\", "\.idea\", "\.vscode\", "\bin\", "\build\", "\out\",
    "\logs\", "\node_modules\", "\vendor\"
)
$skipFileNames = @(
    "CPF_CODEX_REQUEST_20260618_01.md",
    "CPF_STABILIZATION_REPORT.md",
    "CPF_STABILIZATION_CHANGED_FILES.txt"
)
$mojibakeLiteralChars = @(
    [char]0x00EC,
    [char]0x00EB,
    [char]0x00ED,
    [char]0x00EA,
    [char]0x63F6,
    [char]0x7B4C,
    [char]0x7344,
    [char]0x56A5,
    [char]0x7652,
    [char]0xF9CF,
    [char]0xFFFD
)
$mojibakeRegexPatterns = @(
    '\?[\u3130-\u318F\uA960-\uA97F\uAC00-\uD7AF]',
    '[\u2464\u4E8C\u56A5\u63F6\u7344\u7652\u7B4C\uF9CF]'
)
$utf8Strict = [System.Text.UTF8Encoding]::new($false, $true)
$failures = New-Object System.Collections.Generic.List[string]

Get-ChildItem -LiteralPath $Root -Recurse -File | ForEach-Object {
    $relative = $_.FullName.Substring($Root.Length)
    foreach ($directory in $skipDirectories) {
        if ($relative.Contains($directory)) {
            return
        }
    }
    if ($textExtensions -notcontains $_.Extension.ToLowerInvariant()) {
        return
    }
    if ($skipFileNames -contains $_.Name) {
        return
    }

    try {
        $bytes = [System.IO.File]::ReadAllBytes($_.FullName)
        $text = $utf8Strict.GetString($bytes)
        if ($CheckMojibake) {
            foreach ($pattern in $mojibakeLiteralChars) {
                if ($text.Contains($pattern)) {
                    $failures.Add("mojibake marker '$pattern': $($_.FullName)")
                    break
                }
            }
            foreach ($pattern in $mojibakeRegexPatterns) {
                if ([regex]::IsMatch($text, $pattern)) {
                    $failures.Add("mojibake regex '$pattern': $($_.FullName)")
                    break
                }
            }
        }
    } catch {
        $failures.Add("invalid utf-8: $($_.FullName) - $($_.Exception.Message)")
    }
}

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Host $_ }
    exit 1
}

Write-Host "UTF-8 check passed."
if (-not $CheckMojibake) {
    Write-Host "Run with -CheckMojibake to also fail on common mojibake markers."
}
