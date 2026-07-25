param(
    [string]$Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [switch]$StaticOnly,
    [switch]$WithDatabase,
    [switch]$WithGeneratorLifecycle,
    [switch]$WithBrowser,
    [switch]$RequireAll,
    [string]$Profile = "local",
    [string]$EvidenceOutput = ""
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Root = (Resolve-Path -LiteralPath $Root).Path
$results = [System.Collections.Generic.List[object]]::new()
$startedAt = [DateTimeOffset]::Now
$runStamp = $startedAt.ToString('yyyyMMdd_HHmmss')
$rawLog = Join-Path ([System.IO.Path]::GetTempPath()) "cpf_r8_verify_${runStamp}.raw.log"
$utf8 = [System.Text.UTF8Encoding]::new($false)
[System.IO.File]::WriteAllText($rawLog, "CPF_R8_FULL_VERIFY_RAW`nSTARTED_AT=$($startedAt.ToString('o'))`nROOT=$Root`nPROFILE=$Profile`nCOMPUTER=$env:COMPUTERNAME`nOS=$([System.Environment]::OSVersion.VersionString)`n`n", $utf8)

function Append-Raw([string]$Text) {
    [System.IO.File]::AppendAllText($rawLog, $Text + "`n", $utf8)
}
function Add-Result([string]$Name,[string]$Status,[string]$Detail) {
    $results.Add([pscustomobject]@{Name=$Name;Status=$Status;Detail=$Detail})
    $prefix = if ($Status -eq 'PASS') {'[PASS]'} elseif ($Status -eq 'SKIPPED') {'[SKIP]'} else {'[FAIL]'}
    $line = "$prefix $Name - $Detail"
    Write-Host $line
    Append-Raw $line
}
function Run-Native([string]$Name,[string]$File,[string[]]$Args,[string]$WorkingDirectory=$Root) {
    $commandText = "$File " + ($Args -join ' ')
    Append-Raw "--- COMMAND: $Name ---"
    Append-Raw "WORKING_DIRECTORY=$WorkingDirectory"
    Append-Raw "COMMAND=$commandText"
    Push-Location $WorkingDirectory
    try {
        $output = @(& $File @Args 2>&1)
        $code = $LASTEXITCODE
        if ($null -eq $code) { $code = if ($?) {0} else {1} }
        foreach($line in $output) {
            $text = $line.ToString()
            Write-Host $text
            Append-Raw $text
        }
        Append-Raw "EXIT_CODE=$code"
        if ($code -ne 0) { Add-Result $Name 'FAIL' "exit=$code"; return $false }
        Add-Result $Name 'PASS' 'completed'; return $true
    } catch {
        Append-Raw ("EXCEPTION=" + $_.Exception.Message)
        Add-Result $Name 'FAIL' $_.Exception.Message
        return $false
    } finally {
        Pop-Location
        Append-Raw "--- END COMMAND: $Name ---`n"
    }
}
function Run-Pwsh([string]$Name,[string]$Script,[string[]]$Args=@()) {
    $path = Join-Path $Root $Script
    if (-not (Test-Path $path)) { Add-Result $Name 'FAIL' "missing $Script"; return $false }
    return Run-Native $Name 'pwsh' (@('-NoProfile','-ExecutionPolicy','Bypass','-File',$path) + $Args)
}
function Skip([string]$Name,[string]$Why) { Add-Result $Name 'SKIPPED' $Why }

$allOk = $true
if (-not (Run-Native 'Git diff check' 'git' @('-C',$Root,'diff','--check'))) { $allOk = $false }
foreach ($gate in @(
    @('R8 cleanup','cpf-tools/scripts/check-r8-cleanup.ps1'),
    @('R8 requirement catalog','cpf-tools/scripts/check-r8-requirement-review.ps1'),
    @('R8 approval security','cpf-tools/scripts/check-r8-approval-security.ps1'),
    @('Frontend feature coverage','cpf-tools/scripts/check-frontend-feature-route-coverage.ps1'),
    @('Core/BAT ownership','cpf-tools/scripts/check-core-owner-boundary.ps1'),
    @('Architecture ownership','cpf-tools/scripts/check-architecture-ownership.ps1'),
    @('Full layer taxonomy','cpf-tools/scripts/check-full-layer-taxonomy.ps1'),
    @('Service-call boundary','cpf-tools/scripts/check-service-call-boundary.ps1'),
    @('Runtime config standard','cpf-tools/scripts/check-runtime-config-standard.ps1'),
    @('Profile loading','cpf-tools/scripts/check-profile-loading.ps1'),
    @('Packaged runtime resources','cpf-tools/scripts/check-packaged-runtime-resources.ps1'),
    @('Repository hygiene','cpf-tools/scripts/check-repository-hygiene.ps1'),
    @('Document links','cpf-tools/scripts/check-document-links.ps1'),
    @('Evidence path existence','cpf-tools/scripts/check-evidence-path-existence.ps1'),
    @('Report/matrix/evidence consistency','cpf-tools/scripts/check-report-matrix-evidence-consistency.ps1'),
    @('UTF-8','cpf-tools/scripts/check-utf8.ps1'),
    @('Java format','cpf-tools/scripts/check-java-format.ps1'),
    @('Java 25 standard','cpf-tools/scripts/check-java25-standard.ps1'),
    @('Legacy-name guard','cpf-tools/scripts/check-legacy-name.ps1'),
    @('Spring event usage','cpf-tools/scripts/check-spring-event-usage.ps1'),
    @('Log management standard','cpf-tools/scripts/check-log-management-standard.ps1'),
    @('Modern frontend standard','cpf-tools/scripts/check-modern-frontend.ps1'),
    @('OpenAPI source coverage','cpf-tools/scripts/check-openapi-source-coverage.ps1'),
    @('Sample standard','cpf-tools/scripts/check-sample-standard.ps1'),
    @('Sample coverage','cpf-tools/scripts/check-sample-coverage.ps1'),
    @('Security seed standard','cpf-tools/scripts/check-security-seed-standard.ps1'),
    @('Transaction ID standard','cpf-tools/scripts/check-transaction-id-standard.ps1'),
    @('SQL canonical','cpf-tools/scripts/check-sql-canonical.ps1'),
    @('SQL standard','cpf-tools/scripts/check-sql-standard.ps1'),
    @('DB vendor-pack parity','cpf-tools/scripts/check-db-vendor-pack-parity.ps1'),
    @('Generated-domain parity','cpf-tools/scripts/check-generated-domain-parity.ps1'),
    @('DB profile/generated domain','cpf-tools/scripts/check-database-profile-standard.ps1')
)) {
    if (-not (Run-Pwsh $gate[0] $gate[1] @('-Root',$Root))) { $allOk = $false }
}
if (-not (Run-Pwsh 'DB artifact synchronization' 'cpf-tools/scripts/sync-database-artifacts.ps1' @('-Root',$Root))) { $allOk = $false }

if (-not $StaticOnly) {
    $gradle = Join-Path $Root 'gradlew.bat'
    if (Test-Path $gradle) {
        if (-not (Run-Native 'Gradle clean test assemble' $gradle @('clean','test','assemble','--no-daemon'))) { $allOk = $false }
    } else { Add-Result 'Gradle clean test assemble' 'FAIL' 'gradlew.bat missing'; $allOk = $false }

    foreach ($fe in @('cpf-admin/frontend','cpf-biz-admin/frontend')) {
        $dir = Join-Path $Root $fe
        if (-not (Test-Path (Join-Path $dir 'package.json'))) { Add-Result "$fe frontend" 'FAIL' 'package.json missing'; $allOk=$false; continue }
        if (-not (Run-Native "$fe npm test" 'npm.cmd' @('test','--','--run') $dir)) { $allOk = $false }
        if (-not (Run-Native "$fe npm build" 'npm.cmd' @('run','build') $dir)) { $allOk = $false }
    }
} else {
    Skip 'Gradle clean test assemble' '-StaticOnly'
    Skip 'ADM/BZA npm test/build' '-StaticOnly'
}

if ($WithGeneratorLifecycle) {
    if (-not (Run-Pwsh 'Generated Domain lifecycle' 'cpf-tools/scripts/smoke-generated-domain-lifecycle.ps1' @('-Root',$Root))) { $allOk = $false }
} else { Skip 'Generated Domain lifecycle' 'use -WithGeneratorLifecycle' }

if ($WithDatabase) {
    if (-not (Run-Pwsh 'CPF all DB initialize/verify' 'cpf-tools/scripts/initialize-cpf-database.ps1' @('-All','-RequireRun'))) { $allOk = $false }
} else { Skip 'CPF all DB initialize/verify' 'use -WithDatabase' }

if ($WithBrowser) {
    $browserScripts = @('cpf-tools/scripts/smoke-adm-ui.ps1','cpf-tools/scripts/smoke-bza-ui.ps1')
    foreach ($script in $browserScripts) {
        if (-not (Run-Pwsh "Browser smoke: $script" $script @('-Root',$Root))) { $allOk = $false }
    }
} else { Skip 'ADM/BZA browser smoke' 'use -WithBrowser' }

# Build/Test/Runtime 실행으로 repository root에 garbage/log가 새로 생기지 않았는지 마지막에 다시 확인합니다.
if (-not (Run-Pwsh 'Post-run R8 cleanup' 'cpf-tools/scripts/check-r8-cleanup.ps1' @('-Root',$Root))) { $allOk = $false }
if (-not (Run-Pwsh 'Post-run repository hygiene' 'cpf-tools/scripts/check-repository-hygiene.ps1' @('-Root',$Root))) { $allOk = $false }

Write-Host ''
Write-Host '=== CPF Full Product Verification Summary ==='
$results | Format-Table -AutoSize
$failed = @($results | Where-Object Status -eq 'FAIL').Count
$skipped = @($results | Where-Object Status -eq 'SKIPPED').Count
$finishedAt = [DateTimeOffset]::Now
Append-Raw "FINISHED_AT=$($finishedAt.ToString('o'))"
Append-Raw "FAILED=$failed"
Append-Raw "SKIPPED=$skipped"
Append-Raw "REQUIRE_ALL=$RequireAll"
Append-Raw "--- RESULT TABLE ---"
foreach($r in $results){ Append-Raw "$($r.Status)|$($r.Name)|$($r.Detail)" }

$finalExit = 0
if ($RequireAll -and $skipped -gt 0) { $finalExit = 2 }
elseif (-not $allOk -or $failed -gt 0) { $finalExit = 1 }

# 실제 실행 결과를 정제 Evidence로 보존합니다. SKIPPED가 있으면 전체 제품 검증은 미검증입니다.
try {
    if ([string]::IsNullOrWhiteSpace($EvidenceOutput)) {
        $EvidenceOutput = "cpf-docs/work/review/20260725_02/evidence/full-verification-${runStamp}.sanitized.log"
    }
    $writer = Join-Path $Root 'cpf-tools/scripts/write-sanitized-evidence.ps1'
    if (Test-Path $writer) {
        $evidenceStatus = if ($finalExit -ne 0) { '실패' } elseif ($skipped -gt 0) { '미검증' } else { '완료' }
        $reason = if ($evidenceStatus -eq '미검증') { "$skipped verification group(s) skipped; rerun with all required switches." } else { '' }
        $reproduce = "pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\verify-full-product.ps1 -Root `"$Root`" -WithDatabase -WithGeneratorLifecycle -WithBrowser -RequireAll -Profile `"$Profile`""
        $command = if ([string]::IsNullOrWhiteSpace($MyInvocation.Line)) { 'verify-full-product.ps1' } else { $MyInvocation.Line.Trim() }
        & pwsh -NoProfile -ExecutionPolicy Bypass -File $writer `
            -EvidenceId "R8-FULL-VERIFY-$runStamp" `
            -Status $evidenceStatus `
            -Command $command `
            -OutputPath $EvidenceOutput `
            -ExitCode $finalExit `
            -SourceLog $rawLog `
            -Profile $Profile `
            -Reason $reason `
            -ReproduceCommand $reproduce `
            -Skipped $skipped `
            -Failures $failed `
            -Root $Root
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Evidence writer failed. exit=$LASTEXITCODE"
            if ($finalExit -eq 0) { $finalExit = 1 }
        }
    } else {
        Write-Error "Evidence writer missing: $writer"
        if ($finalExit -eq 0) { $finalExit = 1 }
    }
} finally {
    Remove-Item $rawLog -Force -ErrorAction SilentlyContinue
}

if ($finalExit -eq 2) { Write-Error "RequireAll=true but $skipped verification group(s) were skipped."; exit 2 }
if ($finalExit -ne 0) { Write-Error "CPF full verification FAILED ($failed failure(s))."; exit 1 }
Write-Host "CPF full verification PASS. skipped=$skipped evidence=$EvidenceOutput"
exit 0
