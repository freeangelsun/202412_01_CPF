param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path
)

$ErrorActionPreference = "Stop"

# 포맷터 전체를 강제하지 못하는 환경에서도, 리뷰 전에 반드시 잡아야 하는 최소 기준만 검사합니다.
$skipDirectories = @(
    "\.git\", "\.gradle\", "\.idea\", "\.vscode\", "\build\", "\out\", "\target\", "\generated\"
)
$failures = New-Object System.Collections.Generic.List[string]

Get-ChildItem -LiteralPath $Root -Recurse -File -Filter "*.java" | ForEach-Object {
    $relative = $_.FullName.Substring($Root.Length)
    foreach ($directory in $skipDirectories) {
        if ($relative.Contains($directory)) {
            return
        }
    }

    $text = [System.IO.File]::ReadAllText($_.FullName, [System.Text.Encoding]::UTF8)
    $lines = $text -split "`r?`n"

    if ($text -match "(?m)^[ \t]*package\s+[^;]+;[ \t]+import\s+") {
        $failures.Add("package/import 사이 줄바꿈 누락: $relative")
    }
    if ($text -match "(?m)^[ \t]*import\s+[^;]+;[ \t]+import\s+") {
        $failures.Add("import 여러 개가 한 줄에 붙어 있음: $relative")
    }
    if ($text -match "(?m)^\s*(@[A-Za-z0-9_.]+(?:\([^\r\n]*\))?)[ \t]+(public|protected|private|class|interface|enum|record)\b") {
        $failures.Add("annotation과 선언부 사이 줄바꿈 누락: $relative")
    }

    for ($index = 0; $index -lt $lines.Count; $index++) {
        $line = $lines[$index]
        if ($line.Length -gt 220) {
            $lineNumber = $index + 1
            $failures.Add("비정상 장문 라인($lineNumber): $relative")
        }
    }
}

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Host $_ }
    exit 1
}

Write-Host "Java format check passed."
