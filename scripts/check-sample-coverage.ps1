param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $MatrixPath = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "specs/generated/sample-coverage-matrix.md"),
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "build/runtime-smoke")
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
$failures = New-Object System.Collections.Generic.List[string]
$expectedSamples = @(
    Get-ChildItem -LiteralPath (Join-Path $Root "cpf-reference/src/main/java/com/cpf/reference") -Recurse -File -Filter "*EducationSample.java"
    Get-ChildItem -LiteralPath (Join-Path $Root "cpf-batch/src/main/java/com/cpf/batch/edu") -Recurse -File -Filter "*EducationSample.java"
)
$forbiddenSamples = @(
    Get-ChildItem -LiteralPath $Root -Recurse -File -Filter "*EducationSample.java" |
        Where-Object {
            $_.FullName -notmatch '\\build\\' -and
            $_.FullName -notmatch '\\cpf-reference\\src\\main\\java\\com\\cpf\\reference\\' -and
            $_.FullName -notmatch '\\cpf-batch\\src\\main\\java\\com\\cpf\\batch\\edu\\'
        }
)
foreach ($file in $forbiddenSamples) {
    $failures.Add("REF/BAT 외 범용 EDU 소스: $($file.FullName.Substring($Root.Length + 1))") | Out-Null
}

if (-not (Test-Path -LiteralPath $MatrixPath)) {
    $failures.Add("샘플 매트릭스가 없습니다: $MatrixPath") | Out-Null
    $matrixText = ""
} else {
    $matrixText = [System.IO.File]::ReadAllText($MatrixPath, [System.Text.Encoding]::UTF8)
}

foreach ($source in $expectedSamples) {
    $relativeSource = $source.FullName.Substring($Root.Length + 1).Replace('\', '/')
    $relativeTest = $relativeSource.Replace('/src/main/java/', '/src/test/java/').Replace('.java', 'Test.java')
    if (-not (Test-Path -LiteralPath (Join-Path $Root $relativeTest.Replace('/', '\')))) {
        $failures.Add("EDU 대응 테스트 누락: $relativeTest") | Out-Null
    }
    if (-not $matrixText.Contains("| $relativeSource |")) {
        $failures.Add("매트릭스 sourcePath 누락: $relativeSource") | Out-Null
    }
    if (-not $matrixText.Contains("| $relativeTest |")) {
        $failures.Add("매트릭스 testPath 누락: $relativeTest") | Out-Null
    }
}

$matrixSourcePaths = @([regex]::Matches($matrixText, '\|\s*((?:cpf-reference|cpf-batch)/src/main/java/[^|]+EducationSample\.java)\s*\|') |
    ForEach-Object { $_.Groups[1].Value.Trim() })
foreach ($sourcePath in $matrixSourcePaths) {
    if (-not (Test-Path -LiteralPath (Join-Path $Root $sourcePath.Replace('/', '\')))) {
        $failures.Add("매트릭스의 소스 파일이 없습니다: $sourcePath") | Out-Null
    }
}
if ($matrixSourcePaths.Count -ne $expectedSamples.Count) {
    $failures.Add("매트릭스 행과 실제 EDU 수가 다릅니다: matrix=$($matrixSourcePaths.Count), source=$($expectedSamples.Count)") | Out-Null
}
foreach ($duplicate in @($matrixSourcePaths | Group-Object | Where-Object Count -gt 1)) {
    $failures.Add("매트릭스 중복 sourcePath: $($duplicate.Name)") | Out-Null
}

$evidenceMatches = @([regex]::Matches($matrixText, '\|\s*(cpf-docs/evidence/[^|]+)\s*\|') |
    ForEach-Object { $_.Groups[1].Value.Trim() } | Select-Object -Unique)
foreach ($evidencePath in $evidenceMatches) {
    if (-not (Test-Path -LiteralPath (Join-Path $Root $evidencePath.Replace('/', '\')))) {
        $failures.Add("EDU 증적 파일이 없습니다: $evidencePath") | Out-Null
    }
}

New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null
$result = [ordered]@{
    generatedAt = [DateTimeOffset]::Now.ToString('o')
    status = if ($failures.Count -eq 0) { 'DONE' } else { 'FAILED' }
    ownership = [ordered]@{ general = 'REF'; batch = 'BAT' }
    sampleCount = $expectedSamples.Count
    matrixRowCount = $matrixSourcePaths.Count
    forbiddenSampleCount = $forbiddenSamples.Count
    failureCount = $failures.Count
    failures = @($failures)
}
$resultPath = Join-Path $ResultDir 'sample-coverage-result.sanitized.json'
[System.IO.File]::WriteAllText(
    $resultPath,
    ($result | ConvertTo-Json -Depth 8),
    (New-Object System.Text.UTF8Encoding($false)))

Write-Host "Sample coverage status=$($result.status) samples=$($expectedSamples.Count) matrix=$($matrixSourcePaths.Count) evidence=$resultPath"
if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Host "FAIL $_" }
    exit 1
}
