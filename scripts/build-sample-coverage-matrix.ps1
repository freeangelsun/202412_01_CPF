param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $OutputPath = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "specs/sample-coverage-matrix.md"),
    [string] $EvidencePath = "specs/evidence/20260714_01/edu-sample-final.sanitized.log"
)

$ErrorActionPreference = "Stop"
$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$rows = New-Object System.Collections.Generic.List[string]
$sequenceByArea = @{}

foreach ($module in @("xyz", "bat")) {
    $sourceRoot = Join-Path $Root "$module/src/main/java/cpf/$module/edu"
    foreach ($file in @(Get-ChildItem -LiteralPath $sourceRoot -Recurse -File -Filter "*EducationSample.java" | Sort-Object FullName)) {
        $relativeSource = $file.FullName.Substring($Root.Length + 1).Replace('\', '/')
        $relativeUnderEdu = $file.FullName.Substring($sourceRoot.Length).TrimStart('\')
        $segments = $relativeUnderEdu -split '\\'
        $featureArea = if ($segments.Count -gt 1) { $segments[0].ToUpperInvariant() } else { "GENERAL" }
        $sequenceKey = "$module|$featureArea"
        $currentSequence = if ($sequenceByArea.ContainsKey($sequenceKey)) { [int]$sequenceByArea[$sequenceKey] } else { 0 }
        $sequenceByArea[$sequenceKey] = 1 + $currentSequence
        $sampleId = "{0}-EDU-{1}-{2:D3}" -f $module.ToUpperInvariant(), $featureArea, $sequenceByArea[$sequenceKey]
        $packageMatch = [regex]::Match(
            [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8),
            '(?m)^package\s+([^;]+);')
        $testRelative = $relativeSource.Replace('/src/main/java/', '/src/test/java/').Replace('.java', 'Test.java')
        $status = if (Test-Path -LiteralPath (Join-Path $Root $testRelative.Replace('/', '\'))) { "완료" } else { "미구현" }
        $testPath = if ($status -eq "완료") { $testRelative } else { "미구현" }
        $evidence = if ($status -eq "완료") { $EvidencePath } else { "없음" }
        $notes = if ($status -eq "완료") {
            "XYZ/BAT 소유권 기준의 실제 샘플과 대응 테스트입니다."
        } else {
            "대응 테스트가 없어 완료 처리하지 않았습니다."
        }
        $rows.Add("| $sampleId | $($module.ToUpperInvariant()) | $($packageMatch.Groups[1].Value) | $featureArea | $($file.BaseName) | $relativeSource | $testPath | $evidence | unit-test | false | false | $status | $notes |") | Out-Null
    }
}

$header = @(
    "# CPF EDU 샘플 검증 매트릭스",
    "",
    "> 범용 EDU 소유자는 XYZ와 BAT뿐입니다. PFW는 엔진/포트/계약 테스트, CMN은 helper/unit test, ACC/MBR/EXS/ADM/BIZADM은 reference/운영 코드만 소유합니다.",
    "",
    "| sampleId | module | package | featureArea | sampleName | sourcePath | testPath | evidencePath | validationLevel | runtimeRequired | runtimeExecuted | status | notes |",
    "|---|---|---|---|---|---|---|---|---|---|---|---|---|"
)
[System.IO.File]::WriteAllLines($OutputPath, @($header + $rows.ToArray()), $utf8NoBom)
Write-Host "Sample coverage matrix generated: rows=$($rows.Count) path=$OutputPath"
