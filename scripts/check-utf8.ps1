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
    "CPF_NEW_REQUEST.md",
    "CPF_REQUEST.md",
    "CPF_CODEX_REQUEST_20260618_01.md",
    "CPF_STABILIZATION_REPORT.md",
    "CPF_STABILIZATION_CHANGED_FILES.txt"
)
$mojibakeLiteralChars = @(
    # UTF-8 한글이 잘못 디코딩될 때 자주 남는 Latin-1 계열 marker입니다.
    [char]0x00C2,
    [char]0x00C3,
    [char]0x00D0,
    [char]0x00EC,
    [char]0x00F0,
    [char]0x00EB,
    [char]0x00ED,
    [char]0x00EA,
    [char]0x533B,
    [char]0x5BC3,
    [char]0x5DDB,
    [char]0x63F6,
    [char]0x6E1D,
    [char]0x6FE1,
    [char]0x7B4C,
    [char]0x7344,
    [char]0x56A5,
    [char]0x8B70,
    [char]0x8E30,
    [char]0x7652,
    [char]0xF9CF,
    [char]0xF9D0,
    [char]0xFFFD
)
$mojibakeRegexPatterns = @(
    '\?[\u3130-\u318F\uA960-\uA97F\uAC00-\uD7AF]',
    '(?:\u00C3|\u00C2|\u00D0|\u00F0|\u00EC|\u00ED|\u00EB|\u00EA)[\u0080-\u00BF]',
    '[\u2464\u4E8C\u533B\u56A5\u5BC3\u5DDB\u63F6\u6E1D\u6FE1\u7344\u7652\u7B4C\u8B70\u8E30\uF9CF\uF9D0]'
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
        $hasUtf8Bom = $bytes.Length -ge 3 `
            -and $bytes[0] -eq 0xEF `
            -and $bytes[1] -eq 0xBB `
            -and $bytes[2] -eq 0xBF
        # Windows PowerShell 5.1은 BOM 없는 UTF-8 스크립트의 한글 리터럴을 시스템 코드페이지로 읽습니다.
        # 따라서 ps1만 UTF-8 BOM을 허용하고 Java, SQL, 문서 등 나머지 텍스트는 BOM을 금지합니다.
        if ($hasUtf8Bom -and $_.Extension.ToLowerInvariant() -ne ".ps1") {
            $failures.Add("utf-8 bom detected: $($_.FullName)")
            return
        }
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
