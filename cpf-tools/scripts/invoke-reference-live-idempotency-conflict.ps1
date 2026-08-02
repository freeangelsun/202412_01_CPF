[CmdletBinding()]
param(
    [string] $Root = (Resolve-Path (Join-Path $PSScriptRoot "../..")).Path,
    [string] $ProfilePath = "",
    [string] $ResultPath = "",
    [switch] $RequireRun,
    [string] $ExpectedPlanSha256 = ""
)

if ($PSVersionTable.PSVersion.Major -lt 7) { throw "pwsh 7 이상이 필요합니다." }
$ErrorActionPreference = "Stop"
Set-StrictMode -Version Latest
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
$Root = (Resolve-Path -LiteralPath $Root).Path
. (Join-Path $PSScriptRoot "database-profile-common.ps1")

function Get-CpfFileSha256([string] $Path) {
    return (Get-FileHash -LiteralPath $Path -Algorithm SHA256).Hash.ToLowerInvariant()
}
function Get-CpfTextSha256([string] $Text) {
    $algorithm = [Security.Cryptography.SHA256]::Create()
    try { return ([BitConverter]::ToString($algorithm.ComputeHash($Utf8NoBom.GetBytes($Text)))).Replace("-", "").ToLowerInvariant() }
    finally { $algorithm.Dispose() }
}
function Protect-CpfOutput([string] $Text, [string[]] $Secrets) {
    $safe = if ($null -eq $Text) { "" } else { $Text }
    foreach ($secret in $Secrets) { if (-not [string]::IsNullOrWhiteSpace($secret)) { $safe = $safe.Replace($secret, "****") } }
    return $safe
}

if ([string]::IsNullOrWhiteSpace($ProfilePath)) {
    $ProfilePath = Join-Path $Root "cpf-tools/config/database-install.default.json"
} elseif (-not [IO.Path]::IsPathRooted($ProfilePath)) {
    $ProfilePath = Join-Path $Root $ProfilePath
}
$ProfilePath = [IO.Path]::GetFullPath($ProfilePath)
if ([string]::IsNullOrWhiteSpace($ResultPath)) {
    $ResultPath = Join-Path $Root "build/db-lifecycle/reference-live-idempotency-result.sanitized.json"
} elseif (-not [IO.Path]::IsPathRooted($ResultPath)) {
    $ResultPath = Join-Path $Root $ResultPath
}

$profile = Get-CpfDatabaseProfile $ProfilePath
$contractPath = Join-Path $Root "cpf-tools/generator/contracts/reference-edu-schema-ownership-contract.json"
$contract = Get-Content -LiteralPath $contractPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 50
$liveContract = $contract.liveDbValidation
if ($null -eq $liveContract) { throw "Canonical liveDbValidation contract가 없습니다." }
$targets = @(
    $profile.modules.PSObject.Properties |
        ForEach-Object { ConvertTo-CpfModuleProfile $profile ([string]$_.Name) -SkipSecretResolution } |
        Where-Object { $_.enabled -and $_.logicalDatabase -eq "refDB" }
)
if ($targets.Count -ne 1) { throw "Profile에는 enabled refDB owner가 정확히 하나 있어야 합니다." }
$target = $targets[0]
$vendor = ([string]$target.vendor).ToLowerInvariant()
if ($vendor -notin @("mariadb", "postgresql", "oracle")) { throw "공식 Vendor가 아닙니다: $vendor" }

$testSource = Join-Path $Root ([string]$liveContract.testSource)
$repositorySource = Join-Path $Root ([string]$liveContract.repositorySource)
foreach ($path in @($testSource, $repositorySource)) {
    if (-not (Test-Path -LiteralPath $path -PathType Leaf)) { throw "Live idempotency source가 없습니다: $path" }
}
$plan = [ordered]@{
    schemaVersion = 1
    tool = "invoke-reference-live-idempotency-conflict.ps1"
    vendor = $vendor
    moduleKey = [string]$target.moduleKey
    logicalDatabase = [string]$target.logicalDatabase
    physicalDatabase = [string]$target.databaseName
    physicalSchema = [string]$target.schemaName
    contractSha256 = Get-CpfFileSha256 $contractPath
    test = [string]$liveContract.testClass
    testSourceSha256 = Get-CpfFileSha256 $testSource
    repositorySourceSha256 = Get-CpfFileSha256 $repositorySource
    assertions = @($liveContract.requiredAssertions | ForEach-Object { [string]$_ })
}
$planSha256 = Get-CpfTextSha256 ($plan | ConvertTo-Json -Depth 20 -Compress)
$result = [ordered]@{
    schemaVersion = 1
    tool = "invoke-reference-live-idempotency-conflict.ps1"
    mode = if ($RequireRun) { "RUN" } else { "DRY_RUN" }
    status = "미검증"
    generatedAt = (Get-Date).ToString("o")
    profile = [IO.Path]::GetFileName($ProfilePath)
    plan = $plan
    planSha256 = $planSha256
    testResultSha256 = ""
    gradleOutputSha256 = ""
    error = ""
}

$liveTempRoot = ""
try {
    if ($RequireRun) {
        if ($ExpectedPlanSha256 -notmatch '^[0-9a-fA-F]{64}$' -or $ExpectedPlanSha256.ToLowerInvariant() -ne $planSha256) {
            throw "Dry-run에서 검토한 -ExpectedPlanSha256와 현재 plan이 일치해야 합니다. current=$planSha256"
        }
        $target = ConvertTo-CpfModuleProfile $profile ([string]$target.moduleKey)
        $schema = [string]$target.schemaName
        if ($schema -notmatch '^[A-Za-z][A-Za-z0-9_$#]{0,62}$') { throw "schemaName이 안전하지 않습니다." }
        $jdbcUrl = switch ($vendor) {
            "mariadb" { "jdbc:mariadb://$($target.host):$($target.port)/$($target.databaseName)?useUnicode=true&characterEncoding=UTF-8" }
            "postgresql" { "jdbc:postgresql://$($target.host):$($target.port)/$($target.databaseName)?currentSchema=$schema" }
            "oracle" { "jdbc:oracle:thin:@//$($target.host):$($target.port)/$($target.databaseName)" }
        }
        $liveTempRoot = Join-Path ([IO.Path]::GetTempPath()) ("cpf-ref-live-idempotency-" + [guid]::NewGuid().ToString("N"))
        [IO.Directory]::CreateDirectory($liveTempRoot) | Out-Null
        $testResultPath = Join-Path $liveTempRoot "test-result.sanitized.json"
        $gradle = Join-Path $Root "gradlew.bat"
        if (-not (Test-Path -LiteralPath $gradle -PathType Leaf)) { throw "Gradle wrapper가 없습니다." }

        $psi = [Diagnostics.ProcessStartInfo]::new()
        $psi.FileName = $gradle
        $psi.WorkingDirectory = $Root
        $psi.UseShellExecute = $false
        $psi.CreateNoWindow = $true
        $psi.RedirectStandardOutput = $true
        $psi.RedirectStandardError = $true
        $psi.StandardOutputEncoding = [Text.Encoding]::UTF8
        $psi.StandardErrorEncoding = [Text.Encoding]::UTF8
        foreach ($argument in @(
                ":cpf-reference:test",
                "--tests", [string]$plan.test,
                "-PcpfDbVendor=$vendor",
                "--no-daemon", "--console=plain"
            )) { [void]$psi.ArgumentList.Add($argument) }
        $psi.Environment["CPF_REF_LIVE_DB_TEST"] = "true"
        $psi.Environment["CPF_REF_LIVE_JDBC_URL"] = $jdbcUrl
        $psi.Environment["CPF_REF_LIVE_DB_USERNAME"] = [string]$target.runtimeUsername
        $psi.Environment["CPF_REF_LIVE_DB_PASSWORD"] = [string]$target.runtimePassword
        $psi.Environment["CPF_REF_LIVE_RESULT_PATH"] = $testResultPath

        $process = [Diagnostics.Process]::new()
        $process.StartInfo = $psi
        try {
            if (-not $process.Start()) { throw "Gradle live DB test process를 시작할 수 없습니다." }
            $stdoutTask = $process.StandardOutput.ReadToEndAsync()
            $stderrTask = $process.StandardError.ReadToEndAsync()
            $process.WaitForExit()
            $output = $stdoutTask.GetAwaiter().GetResult() + "`n" + $stderrTask.GetAwaiter().GetResult()
            $result.gradleOutputSha256 = Get-CpfTextSha256 $output
            if ($process.ExitCode -ne 0) {
                throw "Live idempotency Gradle test 실패: exit=$($process.ExitCode) output=$(Protect-CpfOutput $output @([string]$target.runtimePassword))"
            }
        } finally { $process.Dispose() }

        if (-not (Test-Path -LiteralPath $testResultPath -PathType Leaf)) { throw "Live idempotency sentinel result가 없습니다." }
        $testResult = Get-Content -LiteralPath $testResultPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 10
        if ($testResult.status -ne "PASS" -or
            $testResult.sameHashReplay -ne $true -or
            $testResult.differentHashConflict -ne $true -or
            [int]$testResult.rowCountBeforeCleanup -ne 1 -or
            [int]$testResult.cleanupRowCount -ne 0) {
            throw "Live idempotency sentinel contract가 실패했습니다."
        }
        $result.testResultSha256 = Get-CpfFileSha256 $testResultPath
        $result.status = "완료"
    }
} catch {
    $result.status = "실패"
    $secrets = if ($RequireRun) { @([string]$target.runtimePassword) } else { @() }
    $result.error = Protect-CpfOutput $_.Exception.Message $secrets
    throw
} finally {
    if (-not [string]::IsNullOrWhiteSpace($liveTempRoot)) {
        $resolved = [IO.Path]::GetFullPath($liveTempRoot)
        $systemTemp = [IO.Path]::GetFullPath([IO.Path]::GetTempPath())
        if ($resolved.StartsWith($systemTemp, [StringComparison]::OrdinalIgnoreCase) -and
            (Split-Path -Leaf $resolved).StartsWith("cpf-ref-live-idempotency-", [StringComparison]::Ordinal)) {
            Remove-Item -LiteralPath $resolved -Recurse -Force -ErrorAction SilentlyContinue
        }
    }
    [IO.Directory]::CreateDirectory((Split-Path -Parent $ResultPath)) | Out-Null
    [IO.File]::WriteAllText($ResultPath, ($result | ConvertTo-Json -Depth 30) + "`n", $Utf8NoBom)
    Write-Host "Sanitized live idempotency result: $ResultPath"
}

if ($RequireRun) {
    Write-Host "CPF Reference live same-key/different-hash conflict PASS. planSha256=$planSha256"
} else {
    Write-Host "CPF Reference live idempotency dry-run PASS. planSha256=$planSha256"
    Write-Host "실제 DB는 변경하거나 조회하지 않았습니다."
}
