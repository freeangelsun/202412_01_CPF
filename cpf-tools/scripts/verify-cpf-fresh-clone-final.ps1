[CmdletBinding()]
param(
    [string]$Root = (Get-Location).Path,
    [Parameter(Mandatory)][ValidatePattern('^[0-9a-f]{40}$')][string]$ExpectedSha,
    [string]$Remote = 'origin',
    [string]$EvidenceOutput = '',
    [switch]$SkipFrontend,
    [switch]$SkipRuntime
)
Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$Utf8NoBom = [Text.UTF8Encoding]::new($false)
if ($SkipFrontend -or $SkipRuntime) {
    throw 'Final fresh-clone gate does not permit frontend/runtime skips. Use the final plan environment blocker contract instead.'
}
$rootPath = (Resolve-Path -LiteralPath $Root).Path
$sourceSha = (& git -C $rootPath rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0) { throw "git rev-parse HEAD failed(exit=$LASTEXITCODE)" }
if ($sourceSha -ne $ExpectedSha) { throw "exact SHA mismatch expected=$ExpectedSha actual=$sourceSha" }
$dirty = @(& git -C $rootPath status --porcelain=v1 --untracked-files=all)
if ($LASTEXITCODE -ne 0) { throw "git status failed(exit=$LASTEXITCODE)" }
if ($dirty.Count -gt 0) { throw "Fresh-clone gate source must be clean.`n$($dirty -join [Environment]::NewLine)" }
& git -C $rootPath cat-file -e "$ExpectedSha^{commit}"
if ($LASTEXITCODE -ne 0) { throw 'Expected commit is unavailable.' }
$tmp = Join-Path ([IO.Path]::GetTempPath()) ("cpf-fresh-clone-{0}" -f [guid]::NewGuid().ToString('N'))
$clone = Join-Path $tmp 'repo'
$evidenceDir = Join-Path $tmp 'evidence'
New-Item -ItemType Directory -Path $tmp,$evidenceDir -Force | Out-Null
$started = [DateTimeOffset]::UtcNow
$steps = [Collections.Generic.List[object]]::new()
$failures = [Collections.Generic.List[string]]::new()
function Invoke-Step([string]$Name, [scriptblock]$Action) {
    $st = [DateTimeOffset]::UtcNow
    $code = 0
    $msg = 'PASS'
    try {
        & $Action
        if ($LASTEXITCODE -and $LASTEXITCODE -ne 0) { throw "exit=$LASTEXITCODE" }
    }
    catch {
        $code = 1
        $msg = $_.Exception.Message -replace '(?i)(password|secret|token|authorization|cookie|credential)\s*[:=]\s*\S+', '$1=***'
        $script:failures.Add("$Name`: $msg") | Out-Null
    }
    finally {
        $script:steps.Add([ordered]@{
            name = $Name
            startedAt = $st.ToString('o')
            finishedAt = [DateTimeOffset]::UtcNow.ToString('o')
            exitCode = $code
            result = $msg
        }) | Out-Null
    }
}
Invoke-Step 'fresh-clone-exact-sha' {
    $remoteUrl = (& git -C $rootPath remote get-url $Remote).Trim()
    if ($LASTEXITCODE -ne 0 -or -not $remoteUrl) { throw "Remote not found: $Remote" }
    & git clone --no-checkout --filter=blob:none $remoteUrl $clone
    if ($LASTEXITCODE -ne 0) { throw "git clone failed(exit=$LASTEXITCODE)" }
    & git -C $clone checkout --detach $ExpectedSha
    if ($LASTEXITCODE -ne 0) { throw "git checkout failed(exit=$LASTEXITCODE)" }
    if ((@(& git -C $clone status --porcelain=v1 --untracked-files=all)).Count -gt 0) { throw 'Fresh clone is dirty immediately after checkout.' }
}
if ($failures.Count -eq 0) {
    Invoke-Step 'validated-final-plan-once' {
        $executor = Join-Path $clone 'cpf-tools/scripts/invoke-cpf-final-verification-plan.py'
        if (-not (Test-Path -LiteralPath $executor -PathType Leaf)) { throw 'Final plan executor is missing.' }
        & python $executor --root $clone --expected-sha $ExpectedSha --evidence-dir $evidenceDir --release
        if ($LASTEXITCODE -ne 0) { throw "final plan failed(exit=$LASTEXITCODE)" }
    }
    Invoke-Step 'fresh-clone-remains-clean' {
        if ((@(& git -C $clone status --porcelain=v1 --untracked-files=all)).Count -gt 0) { throw 'Validation changed fresh clone.' }
        if ((& git -C $clone rev-parse HEAD).Trim() -ne $ExpectedSha) { throw 'Fresh clone HEAD changed.' }
    }
}
if ([string]::IsNullOrWhiteSpace($EvidenceOutput)) {
    $EvidenceOutput = Join-Path $tmp 'CPF_SELF_DEV_030_FRESH_CLONE_FINAL.sanitized.json'
}
$e = [ordered]@{
    schemaVersion = 1
    evidenceId = 'CPF-SELF-DEV-030-FRESH-CLONE-FINAL'
    sourceSha = $ExpectedSha
    resultSha = if ($failures.Count -eq 0) { $ExpectedSha } else { $null }
    startedAt = $started.ToString('o')
    finishedAt = [DateTimeOffset]::UtcNow.ToString('o')
    exitCode = if ($failures.Count -eq 0) { 0 } else { 1 }
    steps = $steps
    failures = $failures
    delegatedEvidenceDirectory = $evidenceDir
    sanitized = $true
    releaseEligible = ($failures.Count -eq 0)
}
[IO.File]::WriteAllText([IO.Path]::GetFullPath($EvidenceOutput), ($e | ConvertTo-Json -Depth 20) + "`n", $Utf8NoBom)
if ($failures.Count -gt 0) { throw "Fresh clone final gate failed: $($failures -join '; ')" }
Write-Host "[CPF][PASS] fresh-clone final gate evidence=$EvidenceOutput"
