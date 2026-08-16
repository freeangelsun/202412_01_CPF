[CmdletBinding()]
param(
    [string] $Root = (Resolve-Path (Join-Path $PSScriptRoot "../../..")).Path
)

$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
$runner = Join-Path $Root "cpf-tools/db/tools/invoke-reference-database-lifecycle.ps1"
$liveRunner = Join-Path $Root "cpf-tools/verification/tools/invoke-reference-live-idempotency-conflict.ps1"
$contractVerifier = Join-Path $Root "cpf-tools/db/verification/verify-reference-db-lifecycle-contract.py"
$verifyGenerator = Join-Path $Root "cpf-tools/generator/generate-reference-db-verify.py"
$defaultProfilePath = Join-Path $Root "cpf-tools/db/config/database-install.default.json"

function Assert-CpfReferenceGate {
    param([bool] $Condition, [string] $Message)
    if (-not $Condition) { throw "[FAIL] $Message" }
    Write-Host "[PASS] $Message"
}

function Get-CpfRelativePath {
    param([string] $Path)
    return ([IO.Path]::GetRelativePath($Root, [IO.Path]::GetFullPath($Path))).Replace("\", "/")
}

foreach ($path in @($runner, $liveRunner, $contractVerifier, $verifyGenerator, $defaultProfilePath)) {
    Assert-CpfReferenceGate (Test-Path -LiteralPath $path -PathType Leaf) "Required lifecycle artifact가 존재한다: $(Get-CpfRelativePath $path)"
}

$tokens = $null
$parseErrors = $null
[void][Management.Automation.Language.Parser]::ParseFile($runner, [ref]$tokens, [ref]$parseErrors)
Assert-CpfReferenceGate ($parseErrors.Count -eq 0) "Reference DB lifecycle runner PowerShell syntax가 유효하다."
$tokens = $null
$parseErrors = $null
[void][Management.Automation.Language.Parser]::ParseFile($liveRunner, [ref]$tokens, [ref]$parseErrors)
Assert-CpfReferenceGate ($parseErrors.Count -eq 0) "Reference live idempotency runner PowerShell syntax가 유효하다."

& python $verifyGenerator --root $Root
if ($LASTEXITCODE -ne 0) { throw "Reference DB verify generated artifact drift gate 실패" }
& python $contractVerifier --root $Root
if ($LASTEXITCODE -ne 0) { throw "Reference DB lifecycle canonical contract gate 실패" }

$tempRoot = Join-Path ([IO.Path]::GetTempPath()) ("cpf-reference-lifecycle-gate-" + [guid]::NewGuid().ToString("N"))
[IO.Directory]::CreateDirectory($tempRoot) | Out-Null
try {
    $defaultProfile = Get-Content -LiteralPath $defaultProfilePath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 50
    foreach ($vendor in @("mariadb", "postgresql", "oracle")) {
        $profile = $defaultProfile | ConvertTo-Json -Depth 50 | ConvertFrom-Json -Depth 50
        $profile.modules.reference.vendor = $vendor
        $profile.modules.reference.port = @{ mariadb = 3306; postgresql = 5432; oracle = 1521 }[$vendor]
        $profile.modules.reference.databaseName = "refLifecycleFixture"
        $profile.modules.reference.schemaName = "refLifecycleSchema"
        $profile.modules.reference.clientPath = ""
        $profilePath = Join-Path $tempRoot "$vendor-profile.json"
        [IO.File]::WriteAllText($profilePath, ($profile | ConvertTo-Json -Depth 50) + "`n", $Utf8NoBom)

        $cases = @(
            @{ action = "fresh-install"; from = "baseline"; target = "coreAndBatch" },
            @{ action = "upgrade"; from = "baseline"; target = "coreAndBatch"; migrationVersions = @(93, 94) },
            @{ action = "rollback"; from = "coreAndBatch"; target = "baseline"; migrationVersions = @(94, 93) },
            @{ action = "verify"; from = "baseline"; target = "coreAndBatch"; operations = 3 },
            @{ action = "runtime-query"; from = "baseline"; target = "coreAndBatch"; operations = 2 }
        )
        foreach ($case in $cases) {
            $resultPath = Join-Path $tempRoot "$vendor-$($case.action).json"
            & $runner `
                -Root $Root `
                -ProfilePath $profilePath `
                -Action $case.action `
                -FromState $case.from `
                -TargetState $case.target `
                -ResultPath $resultPath
            $resultText = Get-Content -LiteralPath $resultPath -Raw -Encoding UTF8
            $result = $resultText | ConvertFrom-Json -Depth 60
            Assert-CpfReferenceGate ($result.mode -eq "DRY_RUN" -and $result.status -eq "미검증") "$vendor/$($case.action)는 DB를 실행하지 않는 dry-run이다."
            Assert-CpfReferenceGate ($result.plan.vendor -eq $vendor) "$vendor/$($case.action)는 선택 Vendor pack만 사용한다."
            Assert-CpfReferenceGate ($result.planSha256 -match '^[0-9a-f]{64}$') "$vendor/$($case.action)는 검토 가능한 plan SHA-256을 생성한다."
            if ($case.ContainsKey("operations")) {
                Assert-CpfReferenceGate (@($result.plan.operations).Count -eq [int]$case.operations) "$vendor/$($case.action) lifecycle operation 수가 정확하다."
            }
            if ($case.ContainsKey("migrationVersions")) {
                $versions = @($result.plan.migrationPlan.versions | ForEach-Object { [int]$_ })
                Assert-CpfReferenceGate (($versions -join ',') -eq (@($case.migrationVersions) -join ',')) "$vendor/$($case.action) V/U version 순서가 정확하다."
            }
            if ($case.action -eq "fresh-install") {
                Assert-CpfReferenceGate (@($result.plan.operations | Where-Object { $_.role -eq "baseline-initializer" }).Count -eq 1) "$vendor fresh-install은 공식 baseline initializer를 사용한다."
                Assert-CpfReferenceGate (@($result.plan.operations | Where-Object { $_.role -eq "overlay-install" }).Count -eq 2) "$vendor fresh-install은 core/batch install pack을 선택한다."
            }
            Assert-CpfReferenceGate (-not $resultText.Contains("CPF_FIXTURE_SECRET")) "$vendor/$($case.action) sanitized result에 secret이 없다."
        }

        $runtime = Get-Content -LiteralPath (Join-Path $tempRoot "$vendor-runtime-query.json") -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 60
        foreach ($operation in @($runtime.plan.operations)) {
            Assert-CpfReferenceGate ($operation.fileSha256 -match '^[0-9a-f]{64}$') "$vendor runtime query source hash가 존재한다."
            Assert-CpfReferenceGate ($operation.renderedSha256 -match '^[0-9a-f]{64}$') "$vendor runtime query rendered hash가 존재한다."
            Assert-CpfReferenceGate ($operation.fileSha256 -ne $operation.renderedSha256) "$vendor runtime bind가 실행용 probe literal로 렌더링된다."
        }

        $liveResultPath = Join-Path $tempRoot "$vendor-live-idempotency.json"
        & $liveRunner -Root $Root -ProfilePath $profilePath -ResultPath $liveResultPath
        $liveResultText = Get-Content -LiteralPath $liveResultPath -Raw -Encoding UTF8
        $liveResult = $liveResultText | ConvertFrom-Json -Depth 30
        Assert-CpfReferenceGate ($liveResult.mode -eq "DRY_RUN" -and $liveResult.status -eq "미검증") "$vendor live idempotency 기본 실행은 DB를 조회/변경하지 않는다."
        Assert-CpfReferenceGate ($liveResult.plan.vendor -eq $vendor) "$vendor live idempotency test driver가 Vendor 선택을 따른다."
        Assert-CpfReferenceGate (@($liveResult.plan.assertions).Count -eq 4) "$vendor live idempotency는 replay/conflict/single-row/cleanup을 검증한다."
        Assert-CpfReferenceGate ($liveResult.plan.testSourceSha256 -match '^[0-9a-f]{64}$') "$vendor live idempotency test source hash가 존재한다."
        Assert-CpfReferenceGate (-not $liveResultText.Contains("CPF_FIXTURE_SECRET")) "$vendor live idempotency sanitized result에 secret이 없다."
    }
    Write-Host "[PASS] CPF Reference DB lifecycle 3-vendor static/fixture contract"
} finally {
    $resolvedTempRoot = [IO.Path]::GetFullPath($tempRoot)
    $systemTempRoot = [IO.Path]::GetFullPath([IO.Path]::GetTempPath())
    if ($resolvedTempRoot.StartsWith($systemTempRoot, [StringComparison]::OrdinalIgnoreCase) -and
        (Split-Path -Leaf $resolvedTempRoot).StartsWith("cpf-reference-lifecycle-gate-", [StringComparison]::Ordinal)) {
        Remove-Item -LiteralPath $resolvedTempRoot -Recurse -Force -ErrorAction SilentlyContinue
    }
}
