param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path
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
    "CPF_NEW_REQUEST.md",
    "CPF_REQUEST.md",
    "CPF_CODEX_REQUEST_20260618_01.md",
    "CPF_STABILIZATION_REPORT.html",
    "CPF_STABILIZATION_REPORT.md",
    "CPF_STABILIZATION_CHANGED_FILES.txt",
    "check-legacy-name.ps1"
)
$legacyPatterns = @(
    "FPS",
    "Fps",
    "fps",
    "X-Fps",
    "X-FPS",
    "x-fps",
    "com.fps",
    "kr.fps",
    "package fps",
    "import fps",
    "source=fps",
    "fpsDB",
    "fps-db",
    "fps_",
    "_fps",
    "@Fps",
    "FpsWebClient",
    "FpsTransaction",
    "FpsWorkflow",
    "FpsBusinessException"
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
    if ($skipFileNames -contains $_.Name) {
        return
    }
    if ($relative -match '(?i)(^|[\\/])[^\\/]*fps[^\\/]*$') {
        $failures.Add("legacy path marker: $($_.FullName)")
        return
    }
    if ($textExtensions -notcontains $_.Extension.ToLowerInvariant()) {
        return
    }

    $bytes = [System.IO.File]::ReadAllBytes($_.FullName)
    $text = $utf8Strict.GetString($bytes)
    foreach ($pattern in $legacyPatterns) {
        if ($text.Contains($pattern)) {
            $failures.Add("legacy marker '$pattern': $($_.FullName)")
            break
        }
    }
}

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Host $_ }
    exit 1
}

Write-Host "Legacy name marker check passed."
