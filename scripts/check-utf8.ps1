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
    "\.git\", "\.gradle\", "\.idea\", "\.vscode\", "\build\", "\out\",
    "\logs\", "\node_modules\", "\vendor\"
)
$mojibakePatterns = @(
    [char]0x63F6,
    [char]0x7B4C,
    [char]0x7344,
    [char]0x56A5,
    [char]0x7652,
    [char]0xF9CF,
    [char]0xFFFD
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

    try {
        $bytes = [System.IO.File]::ReadAllBytes($_.FullName)
        $text = $utf8Strict.GetString($bytes)
        if ($CheckMojibake) {
            foreach ($pattern in $mojibakePatterns) {
                if ($text.Contains($pattern)) {
                    $failures.Add("mojibake marker '$pattern': $($_.FullName)")
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
