param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"

$textExtensions = @(
    ".bat", ".cmd", ".css", ".csv", ".gradle", ".html", ".java", ".js",
    ".json", ".md", ".properties", ".ps1", ".sql", ".txt", ".xml", ".yml", ".yaml"
)
$skipDirectories = @(
    "\.git\", "\.gradle\", "\.idea\", "\bin\", "\build\", "\out\",
    "\logs\", "\node_modules\", "\vendor\"
)
$skipFileNames = @(
    "CPF_CURRENT_WORK_REQUEST.md",
    "check-legacy-name.ps1"
)
$legacyPatterns = @(
    '(?<![A-Za-z0-9])FPS(?![A-Za-z0-9])',
    '(?<![A-Za-z0-9])fps(?![A-Za-z0-9])',
    '\bFps[A-Z][A-Za-z0-9_]*',
    '(?<![A-Za-z0-9])PFW(?![A-Za-z0-9])',
    '(?<![A-Za-z0-9])pfw(?![A-Za-z0-9])',
    '\bPfw[A-Z][A-Za-z0-9_]*',
    '(?<![A-Za-z0-9])XYZ(?![A-Za-z0-9])',
    '(?<![A-Za-z0-9])xyz(?![A-Za-z0-9])',
    '\bXyz[A-Z][A-Za-z0-9_]*',
    '(?m)^\s*(?:package|import)\s+cpf\.',
    'within\(cpf\.\.',
    'execution\(\*\s+cpf\.\.',
    '(?:basePackages|scanBasePackages)\s*=\s*"cpf\.',
    '(?:type|resultType|parameterType)="cpf\.',
    'Class\.forName\("cpf\.',
    'com\.com\.cpf',
    '\bCpfCpf[A-Za-z0-9_]*',
    'CPF_CPF_',
    'cpf_cpf_'
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
    $hasLegacyPathSegment = [regex]::IsMatch(
        $relative,
        '(^|[\\/])(?:fps|pfw|xyz)(?:[\\/]|$)|(^|[\\/])(?:Fps|Pfw|Xyz)[A-Z0-9_]|(?:^|[-_.])(?:fps|pfw|xyz)(?:[-_.]|$)|[\\/]src[\\/](?:main|test)[\\/]java[\\/]cpf[\\/]',
        [System.Text.RegularExpressions.RegexOptions]::None
    )
    if ($hasLegacyPathSegment) {
        $failures.Add("legacy path marker: $($_.FullName)")
        return
    }
    if ($textExtensions -notcontains $_.Extension.ToLowerInvariant()) {
        return
    }

    $bytes = [System.IO.File]::ReadAllBytes($_.FullName)
    $text = $utf8Strict.GetString($bytes)
    foreach ($pattern in $legacyPatterns) {
        if ([regex]::IsMatch($text, $pattern, [System.Text.RegularExpressions.RegexOptions]::None)) {
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
