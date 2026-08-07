[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)][ValidatePattern('^[0-9a-fA-F]{40}$')][string]$ExpectedHead,
    [string]$EvidenceDir = 'build/evidence/r6-release',
    [switch]$Release,
    [switch]$RunDb3,
    [switch]$RunBrowser,
    [switch]$RunMultiprocess
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$root = (& git -C $PSScriptRoot rev-parse --show-toplevel).Trim()
if ($LASTEXITCODE -ne 0) { throw 'Repository root resolution failed' }
$head = (& git -C $root rev-parse HEAD).Trim().ToLowerInvariant()
if ($head -ne $ExpectedHead.ToLowerInvariant()) { throw "ExpectedHead mismatch expected=$ExpectedHead actual=$head" }
if ($Release -and (-not $RunDb3 -or -not $RunBrowser -or -not $RunMultiprocess)) {
    throw 'Release qualification requires -RunDb3 -RunBrowser -RunMultiprocess. Optional release gates are prohibited.'
}
$dirty = @(& git -C $root status --porcelain --untracked-files=all)
if ($LASTEXITCODE -ne 0) { throw 'git status failed' }
if ($dirty.Count -gt 0) { throw ("Release qualification requires a clean exact-SHA tree. Dirty paths:`n" + ($dirty -join "`n")) }

$out = if ([IO.Path]::IsPathRooted($EvidenceDir)) { $EvidenceDir } else { Join-Path $root $EvidenceDir }
New-Item -ItemType Directory -Force -Path $out | Out-Null
foreach ($flag in @('CPF_R6_BROWSER_CHROMIUM_PASSED','CPF_R6_BROWSER_FIREFOX_PASSED','CPF_R6_BROWSER_WEBKIT_PASSED','CPF_R6_DB3_PASSED','CPF_R6_MULTIPROCESS_PASSED','CPF_R6_NETWORK_CHAOS_PASSED','CPF_R6_BROKER_CHAOS_PASSED','CPF_R6_TOOLCHAIN_PASSED','CPF_R6_BUILD_PUBLICATION_PASSED')) {
    [Environment]::SetEnvironmentVariable($flag, $null, 'Process')
}
$ledger = [Collections.Generic.List[object]]::new()
$failed = [Collections.Generic.List[string]]::new()

function Get-FileHashOrNull([string]$Path) {
    if (-not (Test-Path -LiteralPath $Path)) { return $null }
    return (Get-FileHash -Algorithm SHA256 -LiteralPath $Path).Hash.ToLowerInvariant()
}
function Invoke-Gate {
    param([string]$Id,[string]$File,[string[]]$Arguments,[string]$WorkingDirectory=$root)
    $started = [DateTimeOffset]::UtcNow
    $stdout = Join-Path $out "$Id.stdout.log"
    $stderr = Join-Path $out "$Id.stderr.log"
    $exitCode = -1
    $launchError = $null
    try {
        $process = Start-Process -FilePath $File -ArgumentList $Arguments -WorkingDirectory $WorkingDirectory -NoNewWindow -Wait -PassThru -RedirectStandardOutput $stdout -RedirectStandardError $stderr
        $exitCode = $process.ExitCode
    } catch {
        $launchError = $_.Exception.Message
        Set-Content -LiteralPath $stderr -Value $launchError -Encoding utf8NoBOM
    }
    $status = if ($exitCode -eq 0) { 'PASS' } else { 'FAIL' }
    $ledger.Add([ordered]@{
        id=$Id; command=($File+' '+($Arguments -join ' ')); workingDirectory=$WorkingDirectory;
        startedAt=$started.ToString('O'); finishedAt=[DateTimeOffset]::UtcNow.ToString('O');
        exitCode=$exitCode; status=$status; launchError=$launchError;
        stdout=[IO.Path]::GetFileName($stdout); stderr=[IO.Path]::GetFileName($stderr);
        stdoutSha256=Get-FileHashOrNull $stdout; stderrSha256=Get-FileHashOrNull $stderr
    })
    if ($exitCode -ne 0) { $failed.Add("$Id(exit=$exitCode)") }
    return ($exitCode -eq 0)
}
function Add-ConfigurationFailure {
    param([string]$Id,[string]$Actual)
    $failed.Add("$Id(config)")
    $ledger.Add([ordered]@{id=$Id;command='configuration precondition';workingDirectory=$root;startedAt=[DateTimeOffset]::UtcNow.ToString('O');finishedAt=[DateTimeOffset]::UtcNow.ToString('O');exitCode=1;status='FAIL';actual=$Actual})
}

try {
    Invoke-Gate 'python-r6-contract' 'python' @('cpf-tools/verification/final-dev/verify-r6-approval-contract.py',$root) | Out-Null
    Invoke-Gate 'python-r6-behavior' 'python' @('cpf-tools/verification/final-dev/verify-r6-behavior-contracts.py',$root) | Out-Null
    Invoke-Gate 'python-r6j-rework' 'python' @('cpf-tools/verification/final-dev/verify-r6j-rework-contract.py','--root',$root,'--self-test') | Out-Null
    Invoke-Gate 'python-r6-frontend' 'python' @('cpf-tools/verification/final-dev/verify-r6-frontend-contract.py',$root) | Out-Null
    Invoke-Gate 'python-r6-edu-consumer' 'python' @('cpf-tools/verification/final-dev/verify-r6-edu-consumer-runtime-contract.py','--root',$root,'--self-test') | Out-Null
    Invoke-Gate 'python-db3-contract' 'python' @('cpf-tools/verification/final-dev/verify-db3-runner-contract.py') | Out-Null
    Invoke-Gate 'python-qa38' 'python' @('cpf-tools/verification/qa38/verify-qa38-structure.py','.') | Out-Null
    Invoke-Gate 'python-qa39' 'python' @('cpf-tools/verification/qa39/verify-qa39-canonical-starter-closure.py') | Out-Null
    Invoke-Gate 'python-rev004' 'python' @('cpf-tools/verification/final-dev/verify-rev004-overlay.py') | Out-Null

    Invoke-Gate 'gradle-version' (Join-Path $root 'gradlew.bat') @('--version') | Out-Null
    $gradleVersionLog = Join-Path $out 'gradle-version.stdout.log'
    if (Test-Path -LiteralPath $gradleVersionLog) {
        $versionText = Get-Content -Raw -LiteralPath $gradleVersionLog
        if ($versionText -notmatch '(?m)^Gradle 9\.1(?:\.\d+)?\s*$' -or $versionText -notmatch '(?im)(JVM|Launcher JVM):\s*25(?:\.|\s|$)') {
            $failed.Add('toolchain-version(Java25/Gradle9.1)')
            $ledger.Add([ordered]@{id='toolchain-version';command='parse gradle --version';workingDirectory=$root;startedAt=[DateTimeOffset]::UtcNow.ToString('O');finishedAt=[DateTimeOffset]::UtcNow.ToString('O');exitCode=1;status='FAIL';actual='Java 25 and Gradle 9.1 are required'})
        } else {
            $env:CPF_R6_TOOLCHAIN_PASSED = 'true'
            $ledger.Add([ordered]@{id='toolchain-version';command='parse gradle --version';workingDirectory=$root;startedAt=[DateTimeOffset]::UtcNow.ToString('O');finishedAt=[DateTimeOffset]::UtcNow.ToString('O');exitCode=0;status='PASS';actual='Java25/Gradle9.1'})
        }
    }
    Invoke-Gate 'python-r6-edu135' 'python' @('cpf-tools/scripts/verify-cpf-qa37-manual-edu-135.py','--root',$root,'--compile') | Out-Null
    $buildOk = Invoke-Gate 'gradle-clean-build' (Join-Path $root 'gradlew.bat') @('--no-daemon','--max-workers=1','clean','build','--stacktrace')
    $publicationOk = Invoke-Gate 'gradle-quality-publication' (Join-Path $root 'gradlew.bat') @('--no-daemon','--max-workers=1','aggregateQualityBuild','publicationGate','--stacktrace')
    if ($buildOk -and $publicationOk) { $env:CPF_R6_BUILD_PUBLICATION_PASSED = 'true' }

    Invoke-Gate 'adm-npm-ci' 'npm.cmd' @('ci') (Join-Path $root 'cpf-admin/frontend') | Out-Null
    Invoke-Gate 'adm-npm-verify' 'npm.cmd' @('run','verify') (Join-Path $root 'cpf-admin/frontend') | Out-Null
    if ($Release) {
        $runtimeOpenApi = $env:CPF_ADM_RUNTIME_OPENAPI_FILE
        if ([string]::IsNullOrWhiteSpace($runtimeOpenApi) -or -not (Test-Path -LiteralPath $runtimeOpenApi)) {
            Add-ConfigurationFailure 'adm-runtime-openapi-input' 'CPF_ADM_RUNTIME_OPENAPI_FILE must point to the current BACKEND_RUNTIME OpenAPI export.'
        } else {
            $savedOpenApiFile = $env:CPF_OPENAPI_FILE
            try {
                $env:CPF_OPENAPI_FILE = (Resolve-Path -LiteralPath $runtimeOpenApi).Path
                Invoke-Gate 'adm-runtime-openapi-validate' 'npm.cmd' @('run','validate:openapi:release') (Join-Path $root 'cpf-admin/frontend') | Out-Null
                Invoke-Gate 'adm-runtime-openapi-parity' 'node.exe' @('scripts/verify-runtime-openapi-parity.mjs','openapi/cpf-openapi.json',$env:CPF_OPENAPI_FILE) (Join-Path $root 'cpf-admin/frontend') | Out-Null
            } finally { $env:CPF_OPENAPI_FILE = $savedOpenApiFile }
        }
    }
    Invoke-Gate 'bza-npm-ci' 'npm.cmd' @('ci') (Join-Path $root 'cpf-biz-admin/frontend') | Out-Null
    Invoke-Gate 'bza-npm-verify' 'npm.cmd' @('run','verify') (Join-Path $root 'cpf-biz-admin/frontend') | Out-Null
    if ($Release) {
        $bzaRuntimeOpenApi = $env:CPF_BZA_RUNTIME_OPENAPI_FILE
        if ([string]::IsNullOrWhiteSpace($bzaRuntimeOpenApi) -or -not (Test-Path -LiteralPath $bzaRuntimeOpenApi)) {
            Add-ConfigurationFailure 'bza-runtime-openapi-input' 'CPF_BZA_RUNTIME_OPENAPI_FILE must point to the current BACKEND_RUNTIME OpenAPI export.'
        } else {
            $savedOpenApiFile = $env:CPF_OPENAPI_FILE
            try {
                $env:CPF_OPENAPI_FILE = (Resolve-Path -LiteralPath $bzaRuntimeOpenApi).Path
                Invoke-Gate 'bza-runtime-openapi-validate' 'npm.cmd' @('run','validate:openapi:release') (Join-Path $root 'cpf-biz-admin/frontend') | Out-Null
                Invoke-Gate 'bza-runtime-openapi-parity' 'node.exe' @('scripts/verify-runtime-openapi-parity.mjs','openapi/cpf-openapi.json',$env:CPF_OPENAPI_FILE) (Join-Path $root 'cpf-biz-admin/frontend') | Out-Null
            } finally { $env:CPF_OPENAPI_FILE = $savedOpenApiFile }
        }
    }

    if ($RunBrowser) {
        $admFrontendUrl = $env:CPF_ADM_FRONTEND_URL
        $admA11yOk = $false
        $admBrowserOk = $false
        if ([string]::IsNullOrWhiteSpace($admFrontendUrl)) {
            Add-ConfigurationFailure 'adm-browser-input' 'CPF_ADM_FRONTEND_URL is required for browser validation.'
        } else {
            $admA11yOk = Invoke-Gate 'adm-npm-a11y' 'npm.cmd' @('run','test:a11y') (Join-Path $root 'cpf-admin/frontend')
            $admBrowserOk = Invoke-Gate 'adm-playwright' 'npm.cmd' @('run','test:e2e') (Join-Path $root 'cpf-admin/frontend')
        }

        $savedAuthState = $env:CPF_E2E_AUTH_STATE
        $savedPrivileged = $env:CPF_E2E_PRIVILEGED_ENDPOINTS
        $savedRouteMatrix = $env:CPF_E2E_ROUTE_MATRIX
        $savedFailureMatrix = $env:CPF_E2E_FAILURE_MATRIX
        $savedSecurityFixture = $env:CPF_E2E_SECURITY_FIXTURE
        $bzaA11yOk = $false
        $bzaBrowserOk = $false
        try {
            if ([string]::IsNullOrWhiteSpace($env:CPF_BZA_FRONTEND_URL)) {
                Add-ConfigurationFailure 'bza-browser-input' 'CPF_BZA_FRONTEND_URL is required for browser validation.'
            } elseif ([string]::IsNullOrWhiteSpace($env:CPF_BZA_E2E_AUTH_STATE)) {
                Add-ConfigurationFailure 'bza-browser-auth-input' 'CPF_BZA_E2E_AUTH_STATE is required for browser validation.'
            } else {
                $env:CPF_E2E_AUTH_STATE = $env:CPF_BZA_E2E_AUTH_STATE
                $env:CPF_E2E_PRIVILEGED_ENDPOINTS = $env:CPF_BZA_E2E_PRIVILEGED_ENDPOINTS
                $env:CPF_E2E_ROUTE_MATRIX = $env:CPF_BZA_E2E_ROUTE_MATRIX
                $env:CPF_E2E_FAILURE_MATRIX = $env:CPF_BZA_E2E_FAILURE_MATRIX
                $env:CPF_E2E_SECURITY_FIXTURE = $env:CPF_BZA_E2E_SECURITY_FIXTURE
                $bzaA11yOk = Invoke-Gate 'bza-npm-a11y' 'npm.cmd' @('run','test:a11y') (Join-Path $root 'cpf-biz-admin/frontend')
                $bzaBrowserOk = Invoke-Gate 'bza-playwright' 'npm.cmd' @('run','test:e2e') (Join-Path $root 'cpf-biz-admin/frontend')
            }
        }
        finally {
            $env:CPF_E2E_AUTH_STATE = $savedAuthState
            $env:CPF_E2E_PRIVILEGED_ENDPOINTS = $savedPrivileged
            $env:CPF_E2E_ROUTE_MATRIX = $savedRouteMatrix
            $env:CPF_E2E_FAILURE_MATRIX = $savedFailureMatrix
            $env:CPF_E2E_SECURITY_FIXTURE = $savedSecurityFixture
        }
        if ($admA11yOk -and $admBrowserOk -and $bzaA11yOk -and $bzaBrowserOk) {
            $env:CPF_R6_BROWSER_CHROMIUM_PASSED = 'true'
            $env:CPF_R6_BROWSER_FIREFOX_PASSED = 'true'
            $env:CPF_R6_BROWSER_WEBKIT_PASSED = 'true'
        }
    }
    if ($RunDb3) {
        $db3Ok = Invoke-Gate 'db3-live' 'pwsh' @('-NoProfile','-File','cpf-tools/verification/final-dev/run-db3-lifecycle.ps1','-ExpectedHead',$head,'-EvidenceDir',(Join-Path $out 'db3'))
        if ($db3Ok) { $env:CPF_R6_DB3_PASSED = 'true' }
    }
    if ($RunMultiprocess) {
        $multiOk = Invoke-Gate 'multiprocess-chaos' 'pwsh' @('-NoProfile','-File','cpf-tools/verification/final-dev/run-multiprocess-chaos.ps1','-ExpectedHead',$head,'-EvidenceDir',(Join-Path $out 'multiprocess'))
        if ($multiOk) {
            $env:CPF_R6_MULTIPROCESS_PASSED = 'true'
            $env:CPF_R6_NETWORK_CHAOS_PASSED = 'true'
            $env:CPF_R6_BROKER_CHAOS_PASSED = 'true'
        }
    }
    if ($Release) {
        Invoke-Gate 'r6-hardening' 'python' @('cpf-tools/verification/final-dev/run-r6-hardening-qualification.py','--root',$root,'--expected-head',$head,'--evidence-dir',(Join-Path $out 'hardening')) | Out-Null
    }
    # Final cleanliness is evaluated after every source/generator/hardening gate.
    Invoke-Gate 'git-diff-check' 'git' @('-C',$root,'diff','--check') | Out-Null
    Invoke-Gate 'git-tracked-diff-zero' 'git' @('-C',$root,'diff','--exit-code') | Out-Null
    $finalDirty = @(& git -C $root status --porcelain --untracked-files=all)
    if ($LASTEXITCODE -ne 0) {
        $failed.Add('git-final-status(exit)')
    } elseif ($finalDirty.Count -gt 0) {
        $failed.Add('git-final-status(dirty)')
        $ledger.Add([ordered]@{id='git-final-status';command='git status --porcelain --untracked-files=all';workingDirectory=$root;startedAt=[DateTimeOffset]::UtcNow.ToString('O');finishedAt=[DateTimeOffset]::UtcNow.ToString('O');exitCode=1;status='FAIL';actual=($finalDirty -join '; ')})
    } else {
        $ledger.Add([ordered]@{id='git-final-status';command='git status --porcelain --untracked-files=all';workingDirectory=$root;startedAt=[DateTimeOffset]::UtcNow.ToString('O');finishedAt=[DateTimeOffset]::UtcNow.ToString('O');exitCode=0;status='PASS';actual='clean'})
    }
}
finally {
    $summary=[ordered]@{
        protocol='CPF-R6-RELEASE-GATES-2'; releaseMode=[bool]$Release;
        expectedHead=$ExpectedHead.ToLowerInvariant(); actualHead=$head; cleanTreeAtStart=($dirty.Count -eq 0);
        createdAt=[DateTimeOffset]::UtcNow.ToString('O'); failedGates=@($failed); gates=$ledger
    }
    $summaryPath = Join-Path $out 'r6-release-summary.json'
    $summary | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath $summaryPath -Encoding utf8NoBOM
    $summaryHash = Get-FileHash -Algorithm SHA256 -LiteralPath $summaryPath
    "$($summaryHash.Hash.ToLowerInvariant())  r6-release-summary.json" | Set-Content -LiteralPath (Join-Path $out 'SHA256SUMS.txt') -Encoding ascii
}
if ($failed.Count -gt 0) { throw ('Release gates failed: ' + ($failed -join ', ')) }
