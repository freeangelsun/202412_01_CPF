param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

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
    if ($text -match "(?m)^\s*(public|protected|private)\s+[^{;=]+[ \t]+(public|protected|private)\s+") {
        $failures.Add("여러 method/field 선언이 한 줄에 붙어 있을 가능성: $relative")
    }
    if ($text -match "(?m)^\s*(class|interface|enum|record)\s+\S+.*\}\s*(class|interface|enum|record)\s+") {
        $failures.Add("여러 type 선언이 한 줄에 붙어 있을 가능성: $relative")
    }
    if ($text -match "(?s)/\*\*\s*(?:\*\s*)*\*/") {
        $failures.Add("내용이 없는 JavaDoc: $relative")
    }
    if ($text -match "CPF\s+(?:기능 설명|처리 기준)입니다\.") {
        $failures.Add("의미 없는 임시 설명 문구: $relative")
    }

    for ($index = 0; $index -lt $lines.Count; $index++) {
        $line = $lines[$index]
        if ($line.Length -gt 220) {
            $lineNumber = $index + 1
            $failures.Add("비정상 장문 라인($lineNumber): $relative")
        }
        if ($line -match ";\s*(public|protected|private|import|class|interface|enum|record)\s+") {
            $lineNumber = $index + 1
            $failures.Add("선언/구문이 한 줄에 연속 배치됨($lineNumber): $relative")
        }
    }
}

if ($failures.Count -gt 0) {
    $failures | Sort-Object | ForEach-Object { Write-Host $_ }
    exit 1
}

Write-Host "Java format check passed."
