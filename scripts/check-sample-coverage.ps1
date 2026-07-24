param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $MatrixPath = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "build/generated/sample-coverage-matrix.md"),
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "build/quality-gate")
)

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"

$Root = (Resolve-Path -LiteralPath $Root).Path
if (-not [System.IO.Path]::IsPathRooted($MatrixPath)) { $MatrixPath = Join-Path $Root $MatrixPath }
if (-not [System.IO.Path]::IsPathRooted($ResultDir)) { $ResultDir = Join-Path $Root $ResultDir }

$failures = [System.Collections.Generic.List[string]]::new()
$expected = [System.Collections.Generic.List[object]]::new()
foreach ($sourceRootRelative in @(
        "cpf-reference/src/main/java/com/cpf/reference",
        "cpf-batch/src/main/java/com/cpf/batch/edu"
    )) {
    $sourceRoot = Join-Path $Root $sourceRootRelative
    if (-not (Test-Path -LiteralPath $sourceRoot -PathType Container)) { continue }
    foreach ($file in Get-ChildItem -LiteralPath $sourceRoot -Recurse -File -Filter "*EducationSample.java") {
        $relativeSource = $file.FullName.Substring($Root.Length + 1).Replace('\', '/')
        $relativeTest = $relativeSource.Replace('/src/main/java/', '/src/test/java/').Replace('.java', 'Test.java')
        $expected.Add([ordered]@{ source = $relativeSource; test = $relativeTest }) | Out-Null
    }
}

$forbidden = @(
    Get-ChildItem -LiteralPath $Root -Recurse -File -Filter "*EducationSample.java" |
        Where-Object {
            $_.FullName -notmatch '[\\/]build[\\/]' -and
            $_.FullName -notmatch '[\\/]cpf-reference[\\/]src[\\/]main[\\/]java[\\/]com[\\/]cpf[\\/]reference[\\/]' -and
            $_.FullName -notmatch '[\\/]cpf-batch[\\/]src[\\/]main[\\/]java[\\/]com[\\/]cpf[\\/]batch[\\/]edu[\\/]'
        }
)
foreach ($file in $forbidden) {
    $failures.Add("REF/BAT 외 범용 EDU Source: $($file.FullName.Substring($Root.Length + 1))") | Out-Null
}

if (-not (Test-Path -LiteralPath $MatrixPath -PathType Leaf)) {
    $failures.Add("build/generated Sample Matrix가 없습니다. buildSampleCoverageMatrix를 먼저 실행하세요: $MatrixPath") | Out-Null
    $matrixText = ""
} else {
    $matrixText = [System.IO.File]::ReadAllText($MatrixPath, [System.Text.Encoding]::UTF8)
}

foreach ($row in $expected) {
    if (-not (Test-Path -LiteralPath (Join-Path $Root $row.test.Replace('/', [System.IO.Path]::DirectorySeparatorChar)))) {
        $failures.Add("EDU 대응 Test 누락: $($row.test)") | Out-Null
    }
    if (-not $matrixText.Contains("| $($row.source) |")) {
        $failures.Add("Generated Matrix sourcePath 누락: $($row.source)") | Out-Null
    }
    if ($row.test -ne "미구현" -and -not $matrixText.Contains("| $($row.test) |")) {
        $failures.Add("Generated Matrix testPath 누락: $($row.test)") | Out-Null
    }
}

$matrixSources = @(
    [regex]::Matches(
        $matrixText,
        '\|\s*((?:cpf-reference|cpf-batch)/src/main/java/[^|]+EducationSample\.java)\s*\|') |
        ForEach-Object { $_.Groups[1].Value.Trim() }
)
if ($matrixSources.Count -ne $expected.Count) {
    $failures.Add("Generated Matrix 행과 실제 EDU 수가 다릅니다: matrix=$($matrixSources.Count) source=$($expected.Count)") | Out-Null
}
foreach ($duplicate in @($matrixSources | Group-Object | Where-Object Count -gt 1)) {
    $failures.Add("Generated Matrix 중복 sourcePath: $($duplicate.Name)") | Out-Null
}

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$result = [ordered]@{
    generatedAt = [DateTimeOffset]::Now.ToString("o")
    status = if ($failures.Count -eq 0) { "완료" } else { "실패" }
    completionClaim = $false
    sampleCount = $expected.Count
    matrixRowCount = $matrixSources.Count
    forbiddenSampleCount = $forbidden.Count
    failureCount = $failures.Count
    failures = @($failures)
}
$resultPath = Join-Path $ResultDir "sample-coverage-result.sanitized.json"
[System.IO.File]::WriteAllText(
    $resultPath,
    ($result | ConvertTo-Json -Depth 8),
    [System.Text.UTF8Encoding]::new($false))

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Host "FAIL $_" }
    exit 1
}
Write-Host "Sample coverage structural check passed. Runtime completion is not claimed."
