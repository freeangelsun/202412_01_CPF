param(
    [string]$Root = '.',
    [string]$EvidenceDir = 'cpf-docs/evidence/current/runtime',
    [switch]$SkipExternalTools
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$results = [System.Collections.Generic.List[object]]::new()
$startedAt = (Get-Date).ToUniversalTime().ToString('o')
$overallExitCode = 0
$failureMessage = $null
$sourceSha = $null

function Invoke-CpfQa33Step {
    param(
        [Parameter(Mandatory)][string]$Name,
        [Parameter(Mandatory)][scriptblock]$Action
    )
    $started = (Get-Date).ToUniversalTime()
    $code = 0
    $errorMessage = $null
    try {
        & $Action
        $code = if ($null -eq $LASTEXITCODE) { 0 } else { [int]$LASTEXITCODE }
        if ($code -ne 0) { throw "$Name failed (exit=$code)" }
    }
    catch {
        $code = if ($code -ne 0) { $code } else { 1 }
        $errorMessage = $_.Exception.Message -replace '(?i)(password|secret|token|authorization|cookie|session)\s*[:=]\s*[^\s,;]+', '$1=***'
        throw
    }
    finally {
        $finished = (Get-Date).ToUniversalTime()
        $results.Add([ordered]@{
            name = $Name
            startedAt = $started.ToString('o')
            finishedAt = $finished.ToString('o')
            durationMillis = [long]($finished - $started).TotalMilliseconds
            exitCode = $code
            error = $errorMessage
        })
    }
}

$originalLocation = Get-Location
try {
    $rootPath = (Resolve-Path $Root).Path
    Set-Location $rootPath
    $evidencePath = Join-Path $rootPath $EvidenceDir
    New-Item -ItemType Directory -Force $evidencePath | Out-Null

    $sourceSha = (& git rev-parse HEAD).Trim()
    if ($LASTEXITCODE -ne 0 -or $sourceSha -notmatch '^[0-9a-f]{40}$') {
        throw 'QA33 runtime verification requires a valid Git HEAD.'
    }
    if ((& git status --porcelain=v1 | Out-String).Trim()) {
        throw 'QA33 runtime verification requires a clean Working Tree.'
    }

    $javaVersion = (& java -version 2>&1 | Out-String)
    if ($javaVersion -notmatch '(?m)version\s+"25(?:\.|"|\s)') {
        throw 'Java 25 is required.'
    }
    if ((& node --version).Trim() -ne 'v22.16.0') { throw 'Node v22.16.0 is required.' }
    if ((& npm.cmd --version).Trim() -ne '10.9.2') { throw 'npm 10.9.2 is required.' }

    Invoke-CpfQa33Step 'qa32-runtime-regression' {
        & pwsh -NoProfile -File cpf-tools/scripts/verify-cpf-qa32-runtime.ps1 -Root . -SkipExternalTools:$SkipExternalTools
    }
    Invoke-CpfQa33Step 'qa33-gradle-integration' {
        & .\gradlew.bat qa33IntegrationTest '-Pqa33Vendors=oracle,postgresql,mariadb' '-Pqa33Kafka=true' --no-daemon --stacktrace
    }

    foreach ($frontend in @('cpf-admin/frontend', 'cpf-biz-admin/frontend')) {
        Push-Location $frontend
        try {
            Invoke-CpfQa33Step "${frontend}-verify" { & npm.cmd run verify }
            foreach ($browser in @('chromium', 'firefox', 'webkit')) {
                Invoke-CpfQa33Step "${frontend}-playwright-$browser" {
                    & npx.cmd --no-install playwright test "--project=$browser"
                }
            }
        }
        finally { Pop-Location }
    }
    Invoke-CpfQa33Step 'qa33-frontend-closure-full' {
        & python cpf-tools/scripts/verify-cpf-qa33-frontend-closure.py --root .
    }

    if (-not $SkipExternalTools) {
        Invoke-CpfQa33Step 'qa33-final-artifact-supply-chain' {
            & pwsh -NoProfile -File cpf-tools/scripts/generate-cpf-supply-chain-evidence.ps1 -Root .
        }
    }

    $finalSha = (& git rev-parse HEAD).Trim()
    if ($finalSha -ne $sourceSha) { throw "Source SHA changed during verification: $sourceSha -> $finalSha" }
    if ((& git status --porcelain=v1 | Out-String).Trim()) {
        throw 'Working Tree changed during QA33 runtime verification.'
    }
}
catch {
    $overallExitCode = 1
    $failureMessage = $_.Exception.Message -replace '(?i)(password|secret|token|authorization|cookie|session)\s*[:=]\s*[^\s,;]+', '$1=***'
}
finally {
    try {
        if (Get-Variable -Name evidencePath -ErrorAction SilentlyContinue) {
            $evidence = [ordered]@{
                schemaVersion = 1
                sourceSha = $sourceSha
                startedAt = $startedAt
                finishedAt = (Get-Date).ToUniversalTime().ToString('o')
                exitCode = $overallExitCode
                status = if ($overallExitCode -eq 0) { 'PASS' } else { 'FAIL' }
                failureMessage = $failureMessage
                profile = 'QA33_RELEASE'
                results = $results
                sanitized = $true
            }
            $target = Join-Path $evidencePath 'qa33-runtime-evidence.sanitized.json'
            [System.IO.File]::WriteAllText(
                $target,
                ($evidence | ConvertTo-Json -Depth 10) + "`n",
                [System.Text.UTF8Encoding]::new($false))
            $hash = (Get-FileHash -Algorithm SHA256 -LiteralPath $target).Hash.ToLowerInvariant()
            [System.IO.File]::WriteAllText(
                "$target.sha256",
                "$hash  qa33-runtime-evidence.sanitized.json`n",
                [System.Text.UTF8Encoding]::new($false))
        }
    }
    finally {
        Set-Location $originalLocation
    }
}

exit $overallExitCode
