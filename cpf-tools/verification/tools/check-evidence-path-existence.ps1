param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..\..\..").Path "build/quality-gate")
)

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"

$Root = (Resolve-Path -LiteralPath $Root).Path
if (-not [System.IO.Path]::IsPathRooted($ResultDir)) {
    $ResultDir = Join-Path $Root $ResultDir
}
$ResultDir = [System.IO.Path]::GetFullPath($ResultDir)
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$Utf8NoBom = [System.Text.UTF8Encoding]::new($false)

$sourceFiles = @(
    "cpf-docs/deliverables/TEST_AND_EVIDENCE.md",
    "cpf-docs/work/current/CPF_DEVELOPMENT_QA_CLOSURE.csv",
    "cpf-docs/deliverables/OPEN_ISSUES.md",
    "cpf-docs/work/current/CPF_CODEX_REVALIDATION_SCOPE.md"
)

$failures = [System.Collections.Generic.List[string]]::new()
$references = [System.Collections.Generic.List[object]]::new()


foreach ($sourceFile in $sourceFiles) {
    $path = Join-Path $Root $sourceFile
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) {
        $failures.Add("Evidence 참조 정본 파일이 없습니다: $sourceFile") | Out-Null
        continue
    }
    $text = [System.IO.File]::ReadAllText($path, [System.Text.Encoding]::UTF8)
    foreach ($match in [regex]::Matches($text, '`(?<path>cpf-docs/work/evidence/[^`<>]+)`')) {
        $relative = $match.Groups['path'].Value.Replace('\', '/').TrimEnd('/')
        if ($relative -match '<[^>]+>' -or
                $false) {
            continue
        }
        $references.Add([ordered]@{ sourceFile = $sourceFile; evidencePath = $relative }) | Out-Null
    }
}

$unique = @($references | Sort-Object evidencePath, sourceFile -Unique)
foreach ($row in $unique) {
    $full = Join-Path $Root ($row.evidencePath.Replace('/', [System.IO.Path]::DirectorySeparatorChar))
    if (-not (Test-Path -LiteralPath $full -PathType Leaf)) {
        $failures.Add("존재하지 않는 Evidence 참조: $($row.evidencePath) source=$($row.sourceFile)") | Out-Null
        continue
    }
    if ((Get-Item -LiteralPath $full).Length -le 0) {
        $failures.Add("빈 Evidence 파일: $($row.evidencePath)") | Out-Null
    }
}

$result = [ordered]@{
    generatedAt = [DateTimeOffset]::Now.ToString("o")
    status = if ($failures.Count -eq 0) { "완료" } else { "실패" }
    checkedReferenceCount = $unique.Count
    failures = @($failures)
    references = $unique
}
$output = Join-Path $ResultDir "evidence-path-existence-check.sanitized.json"
[System.IO.File]::WriteAllText($output, ($result | ConvertTo-Json -Depth 10), $Utf8NoBom)

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Host "FAIL $_" }
    exit 1
}
Write-Host "Evidence path check passed. tracked references=$($unique.Count)"
