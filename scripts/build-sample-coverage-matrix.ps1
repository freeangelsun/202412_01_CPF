param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $OutputPath = (Join-Path (Resolve-Path "$PSScriptRoot\..").Path "build/generated/sample-coverage-matrix.md")
)

$CpfUtf8ConsoleEncoding = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $CpfUtf8ConsoleEncoding
[Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
$OutputEncoding = $CpfUtf8ConsoleEncoding
$ErrorActionPreference = "Stop"

$Root = (Resolve-Path -LiteralPath $Root).Path
if (-not [System.IO.Path]::IsPathRooted($OutputPath)) {
    $OutputPath = Join-Path $Root $OutputPath
}
$utf8NoBom = [System.Text.UTF8Encoding]::new($false)
$rows = [System.Collections.Generic.List[string]]::new()
$sequenceByArea = @{}

foreach ($moduleInfo in @(
        [ordered]@{ code = "REF"; source = "cpf-reference/src/main/java/com/cpf/reference" },
        [ordered]@{ code = "BAT"; source = "cpf-batch/src/main/java/com/cpf/batch/edu" }
    )) {
    $sourceRoot = Join-Path $Root $moduleInfo.source
    if (-not (Test-Path -LiteralPath $sourceRoot -PathType Container)) {
        continue
    }
    foreach ($file in @(Get-ChildItem -LiteralPath $sourceRoot -Recurse -File -Filter "*EducationSample.java" | Sort-Object FullName)) {
        $relativeSource = $file.FullName.Substring($Root.Length + 1).Replace('\', '/')
        $relativeUnderEdu = $file.FullName.Substring($sourceRoot.Length).TrimStart('\', '/')
        $segments = $relativeUnderEdu -split '[\\/]'
        $featureArea = if ($segments.Count -gt 1) { $segments[0].ToUpperInvariant() } else { "GENERAL" }
        $sequenceKey = "$($moduleInfo.code)|$featureArea"
        $sequenceByArea[$sequenceKey] = 1 + [int]($sequenceByArea[$sequenceKey] ?? 0)
        $sampleId = "{0}-EDU-{1}-{2:D3}" -f $moduleInfo.code, $featureArea, $sequenceByArea[$sequenceKey]
        $sourceText = [System.IO.File]::ReadAllText($file.FullName, [System.Text.Encoding]::UTF8)
        $packageMatch = [regex]::Match($sourceText, '(?m)^package\s+([^;]+);')
        $testRelative = $relativeSource.Replace('/src/main/java/', '/src/test/java/').Replace('.java', 'Test.java')
        $testExists = Test-Path -LiteralPath (Join-Path $Root $testRelative.Replace('/', [System.IO.Path]::DirectorySeparatorChar))
        $testPath = if ($testExists) { $testRelative } else { "미구현" }
        $status = if ($testExists) { "부분 구현" } else { "미구현" }
        $notes = if ($testExists) {
            "Source와 대응 Test 존재. 실제 실행 Evidence가 없으므로 완료 처리하지 않습니다."
        } else {
            "대응 Test가 없어 미구현입니다."
        }
        $rows.Add("| $sampleId | $($moduleInfo.code) | $($packageMatch.Groups[1].Value) | $featureArea | $($file.BaseName) | $relativeSource | $testPath | 미검증 | $status | $notes |") | Out-Null
    }
}

$header = @(
    "# CPF EDU Sample Coverage Matrix — generated",
    "",
    "> 이 파일은 build/generated 산출물이며 Git 정본이 아닙니다. 범용 EDU Owner는 REF, Batch EDU Owner는 BAT입니다.",
    "> Source/Test 존재는 Runtime 완료 근거가 아니므로 실제 실행 Evidence 전에는 완료로 표기하지 않습니다.",
    "",
    "| sampleId | module | package | featureArea | sampleName | sourcePath | testPath | runtimeEvidence | status | notes |",
    "|---|---|---|---|---|---|---|---|---|---|"
)
[System.IO.Directory]::CreateDirectory((Split-Path -Parent $OutputPath)) | Out-Null
[System.IO.File]::WriteAllLines($OutputPath, @($header + $rows.ToArray()), $utf8NoBom)
Write-Host "Sample coverage matrix generated: rows=$($rows.Count) path=$OutputPath"
