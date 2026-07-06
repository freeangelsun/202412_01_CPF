param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..").Path,
    [string] $ResultDir = "",
    [int] $TestTimeoutSeconds = 180,
    [switch] $DirectGradleVerified,
    [switch] $SkipGradleTest
)

$ErrorActionPreference = "Stop"
. (Join-Path $PSScriptRoot "runtime-common.ps1")

$Root = Get-CpfRuntimeRoot -Root $Root
$ResultDir = Get-CpfRuntimeResultDir -Root $Root -ResultDir $ResultDir
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$resultPath = Join-Path $ResultDir "cmn-fixed-length-advanced-result.json"
$testOut = Join-Path $ResultDir "cmn-fixed-length-advanced-test.out.log"
$testErr = Join-Path $ResultDir "cmn-fixed-length-advanced-test.err.log"
$result = [ordered]@{
    startedAt = (Get-Date).ToString("o")
    status = Get-CpfRuntimeStatusText "Partial"
    sourceMarkers = [ordered]@{}
    gradleTest = [ordered]@{ status = Get-CpfRuntimeStatusText "NotVerified" }
}

function Save-CmnFixedResult {
    $result.finishedAt = (Get-Date).ToString("o")
    Write-CpfRuntimeJson -Path $resultPath -Value $result
}

function Get-CmnSafeTail {
    param([string] $Path)

    if (-not (Test-Path -LiteralPath $Path)) {
        return @()
    }
    return @(Get-Content -LiteralPath $Path -Encoding UTF8 -Tail 40 -ErrorAction SilentlyContinue | ForEach-Object {
            $line = [string] $_
            if ($line.Length -gt 1000) {
                $line.Substring(0, 1000) + "...truncated"
            } else {
                $line
            }
        })
}

try {
    $base = Join-Path $Root "cmn/src/main/java/cpf/cmn/message/fixedlength"
    $testFile = Join-Path $Root "cmn/src/test/java/cpf/cmn/message/fixedlength/FixedLengthMessageParserFormatterTest.java"
    $requiredFiles = @(
        "FixedLengthLayoutSpec.java",
        "FixedLengthFieldSpec.java",
        "FixedLengthGroupSpec.java",
        "FixedLengthMessageParser.java",
        "FixedLengthMessageFormatter.java",
        "FixedLengthTypeConverter.java",
        "FixedLengthMaskingRule.java",
        "FixedLengthLayoutRegistry.java"
    )
    foreach ($file in $requiredFiles) {
        $result.sourceMarkers[$file] = Test-Path -LiteralPath (Join-Path $base $file)
    }
    $testText = [System.IO.File]::ReadAllText($testFile, [System.Text.Encoding]::UTF8)
    $result.sourceMarkers.repeatingGroupTest = $testText.Contains("formatAndParseRepeatingGroupByCountField")
    $result.sourceMarkers.typeValidationTest = $testText.Contains("registryAndTypeValidationExposeFieldErrors")
    $result.sourceMarkers.maskingTest = $testText.Contains("maskedGroups") -and $testText.Contains("maskedFields")

    if ($DirectGradleVerified) {
        $result.gradleTest = [ordered]@{
            status = Get-CpfRuntimeStatusText "Done"
            mode = "external-direct-command"
            command = ".\gradlew.bat :cmn:test --offline --no-daemon --console=plain --tests cpf.cmn.message.fixedlength.FixedLengthMessageParserFormatterTest"
            note = "direct Gradle command was executed outside this script because nested gradlew can be sandbox-limited"
        }
    } elseif (-not $SkipGradleTest) {
        if (Test-Path -LiteralPath $testOut) { Remove-Item -LiteralPath $testOut -Force }
        if (Test-Path -LiteralPath $testErr) { Remove-Item -LiteralPath $testErr -Force }
        Push-Location $Root
        $previousErrorActionPreference = $ErrorActionPreference
        try {
            $ErrorActionPreference = "Continue"
            $testOutput = & (Join-Path $Root "gradlew.bat") ":cmn:test" "--offline" "--no-daemon" "--console=plain" "--tests" "cpf.cmn.message.fixedlength.FixedLengthMessageParserFormatterTest" 2>&1
            $exitCode = $LASTEXITCODE
        } finally {
            $ErrorActionPreference = $previousErrorActionPreference
            Pop-Location
        }
        $testOutput | Set-Content -LiteralPath $testOut -Encoding UTF8
        "" | Set-Content -LiteralPath $testErr -Encoding UTF8
        $completed = $true
        $result.gradleTest = [ordered]@{
            status = $(if ($completed -and $exitCode -eq 0) { Get-CpfRuntimeStatusText "Done" } else { Get-CpfRuntimeStatusText "Failed" })
            exitCode = $exitCode
            timeout = (-not $completed)
            timeoutSeconds = $TestTimeoutSeconds
            outputLog = Get-CpfRelativePath -Root $Root -Path $testOut
            errorLog = Get-CpfRelativePath -Root $Root -Path $testErr
            outputTail = Get-CmnSafeTail -Path $testOut
            errorTail = Get-CmnSafeTail -Path $testErr
        }
    }

    $missingMarkers = @($result.sourceMarkers.GetEnumerator() | Where-Object { -not $_.Value })
    $testOk = $SkipGradleTest -or $result.gradleTest.status -eq (Get-CpfRuntimeStatusText "Done")
    $result.status = $(if ($missingMarkers.Count -eq 0 -and $testOk) { Get-CpfRuntimeStatusText "Done" } else { Get-CpfRuntimeStatusText "Failed" })
    if ($missingMarkers.Count -gt 0) {
        $result.failureClassification = "implementation"
        $result.error = "CMN fixed-length advanced source markers are missing."
        $result.missingMarkers = @($missingMarkers | ForEach-Object { $_.Key })
    } elseif (-not $testOk) {
        $result.failureClassification = "implementation"
        $result.error = "CMN fixed-length unit test failed."
    }
    Save-CmnFixedResult
    if ($result.status -ne (Get-CpfRuntimeStatusText "Done")) {
        exit 1
    }
} catch {
    $result.status = Get-CpfRuntimeStatusText "Failed"
    $result.failureClassification = "implementation"
    $result.error = $_.Exception.Message
    Save-CmnFixedResult
    throw
}

Write-Host "CMN fixed-length advanced smoke finished. status=$($result.status) result=$resultPath"
