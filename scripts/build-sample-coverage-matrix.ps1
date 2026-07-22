param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $OutputPath = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "specs/generated/sample-coverage-matrix.md"),
    [string] $EvidencePath = "cpf-docs/evidence/20260722_01/edu-sample-final.sanitized.log"
)

# PowerShell 5.1과 Java/Gradle 사이의 한글 입출력 인코딩을 UTF-8로 고정합니다.
$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding

$ErrorActionPreference = "Stop"
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$rows = New-Object System.Collections.Generic.List[string]
$sequenceByArea = @{}

foreach ($moduleInfo in @(
        [ordered]@{ code = 'REF'; project = 'cpf-reference'; source = 'cpf-reference/src/main/java/com/cpf/reference' },
        [ordered]@{ code = 'BAT'; project = 'cpf-batch'; source = 'cpf-batch/src/main/java/com/cpf/batch/edu' }
    )) {
    $module = $moduleInfo.code
    $sourceRoot = Join-Path $Root $moduleInfo.source
    foreach ($file in @(Get-ChildItem -LiteralPath $sourceRoot -Recurse -File -Filter "*EducationSample.java" | Sort-Object FullName)) {
        $relativeSource = $file.FullName.Substring($Root.Length + 1).Replace('\', '/')
        $relativeUnderEdu = $file.FullName.Substring($sourceRoot.Length).TrimStart('\')
        $segments = $relativeUnderEdu -split '\\'
        $featureArea = if ($segments.Count -gt 1) { $segments[0].ToUpperInvariant() } else { "GENERAL" }
        $sequenceKey = "$module|$featureArea"
        $currentSequence = if ($sequenceByArea.ContainsKey($sequenceKey)) { [int]$sequenceByArea[$sequenceKey] } else { 0 }
        $sequenceByArea[$sequenceKey] = 1 + $currentSequence
        $sampleId = "{0}-EDU-{1}-{2:D3}" -f $module, $featureArea, $sequenceByArea[$sequenceKey]
        $packageMatch = [regex]::Match(
            [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8),
            '(?m)^package\s+([^;]+);')
        $testRelative = $relativeSource.Replace('/src/main/java/', '/src/test/java/').Replace('.java', 'Test.java')
        $status = if (Test-Path -LiteralPath (Join-Path $Root $testRelative.Replace('/', '\'))) { "완료" } else { "미구현" }
        $testPath = if ($status -eq "완료") { $testRelative } else { "미구현" }
        $evidence = if ($status -eq "완료") { $EvidencePath } else { "없음" }
        $notes = if ($status -eq "완료") {
            "REF/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다."
        } else {
            "대응 테스트가 없어 완료 처리하지 않았습니다."
        }
        $rows.Add("| $sampleId | $module | $($packageMatch.Groups[1].Value) | $featureArea | $($file.BaseName) | $relativeSource | $testPath | $evidence | unit-test | false | false | $status | $notes |") | Out-Null
    }
}

$header = @(
    "# CPF EDU 샘플 검증 매트릭스",
    "",
    "> 범용 EDU 소유자는 REF와 BAT입니다. CPF는 엔진·포트·계약 테스트, CMN은 공통 helper·단위 테스트, MBR·ADM·BZA는 업무 또는 운영 코드를 소유합니다.",
    "",
    "| sampleId | module | package | featureArea | sampleName | sourcePath | testPath | evidencePath | validationLevel | runtimeRequired | runtimeExecuted | status | notes |",
    "|---|---|---|---|---|---|---|---|---|---|---|---|---|"
)
[System.IO.Directory]::CreateDirectory((Split-Path -Parent $OutputPath)) | Out-Null
[System.IO.File]::WriteAllLines($OutputPath, @($header + $rows.ToArray()), $utf8NoBom)
Write-Host "Sample coverage matrix generated: rows=$($rows.Count) path=$OutputPath"
