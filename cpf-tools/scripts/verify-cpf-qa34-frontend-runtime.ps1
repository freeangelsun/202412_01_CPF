[CmdletBinding()]
param(
    [string] $Root = (Get-Location).Path,
    [Parameter(Mandatory)] [string] $AdmBaseUrl,
    [Parameter(Mandatory)] [string] $BzaBaseUrl,
    [Parameter(Mandatory)] [string] $ApprovedRegistry,
    [Parameter(Mandatory)] [string] $AdmAuthState,
    [Parameter(Mandatory)] [string] $BzaAuthState,
    [Parameter(Mandatory)] [string] $AdmPrivilegedEndpoints,
    [Parameter(Mandatory)] [string] $BzaPrivilegedEndpoints,
    [Parameter(Mandatory)] [string] $AdmRouteMatrix,
    [Parameter(Mandatory)] [string] $BzaRouteMatrix,
    [Parameter(Mandatory)] [string] $AdmFailureMatrix,
    [Parameter(Mandatory)] [string] $BzaFailureMatrix,
    [Parameter(Mandatory)] [string] $AdmSecurityFixture,
    [Parameter(Mandatory)] [string] $BzaSecurityFixture,
    [string] $EvidenceOutput,
    [switch] $KeepWorkspace
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'
$rootPath = (Resolve-Path $Root).Path
$sourceSha = (& git -C $rootPath rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0 -or $sourceSha -notmatch '^[0-9a-f]{40}$') {
    throw 'QA34 Frontend runtime requires an exact Git SHA.'
}
if ((@(& git -C $rootPath status --porcelain=v1 --untracked-files=all)).Count -gt 0) {
    throw 'QA34 Frontend runtime requires a clean Working Tree.'
}
if ((& node --version).Trim() -ne 'v22.16.0') {
    throw 'Node v22.16.0 is required.'
}
$npmCommand = if (Get-Command npm.cmd -ErrorAction SilentlyContinue) { 'npm.cmd' } else { 'npm' }
if ((& $npmCommand --version).Trim() -ne '10.9.2') {
    throw 'npm 10.9.2 is required.'
}
if ($ApprovedRegistry -notmatch '^https://') {
    throw 'Approved npm Registry must use HTTPS.'
}

$inputs = @(
    $AdmAuthState, $BzaAuthState,
    $AdmRouteMatrix, $BzaRouteMatrix,
    $AdmFailureMatrix, $BzaFailureMatrix,
    $AdmSecurityFixture, $BzaSecurityFixture
)
foreach ($inputPath in $inputs) {
    if (-not (Test-Path -LiteralPath $inputPath -PathType Leaf)) {
        throw "Required Frontend runtime fixture is missing: $inputPath"
    }
}

$work = Join-Path ([IO.Path]::GetTempPath()) ("cpf-qa34-frontend-{0}" -f [guid]::NewGuid().ToString('N'))
New-Item -ItemType Directory -Path $work -Force | Out-Null
if ([string]::IsNullOrWhiteSpace($EvidenceOutput)) {
    $EvidenceOutput = Join-Path $work 'CPF_QA34_FRONTEND_3_BROWSER.sanitized.json'
}
$EvidenceOutput = [IO.Path]::GetFullPath($EvidenceOutput)
$startedAt = [DateTimeOffset]::UtcNow
$results = [System.Collections.Generic.List[object]]::new()
$failures = [System.Collections.Generic.List[string]]::new()

function Sanitize([string] $Value) {
    if ($null -eq $Value) { return '' }
    return $Value -replace '(?i)(password|secret|token|authorization|cookie)\s*[:=]\s*\S+', '$1=***'
}

function Invoke-ProcessStep(
    [string] $Module,
    [string] $Name,
    [string] $Executable,
    [string[]] $Arguments,
    [hashtable] $Environment,
    [string] $WorkingDirectory
) {
    $stepStart = [DateTimeOffset]::UtcNow
    $stdoutPath = Join-Path $work "$Module-$Name.stdout.log"
    $stderrPath = Join-Path $work "$Module-$Name.stderr.log"
    $processInfo = [Diagnostics.ProcessStartInfo]::new()
    $processInfo.FileName = $Executable
    $processInfo.WorkingDirectory = $WorkingDirectory
    $processInfo.UseShellExecute = $false
    $processInfo.RedirectStandardOutput = $true
    $processInfo.RedirectStandardError = $true
    foreach ($entry in $Environment.GetEnumerator()) {
        $processInfo.Environment[$entry.Key] = [string] $entry.Value
    }
    foreach ($argument in $Arguments) {
        [void] $processInfo.ArgumentList.Add($argument)
    }
    $process = [Diagnostics.Process]::Start($processInfo)
    $stdoutTask = $process.StandardOutput.ReadToEndAsync()
    $stderrTask = $process.StandardError.ReadToEndAsync()
    $process.WaitForExit()
    $stdout = $stdoutTask.GetAwaiter().GetResult()
    $stderr = $stderrTask.GetAwaiter().GetResult()
    [IO.File]::WriteAllText($stdoutPath, $stdout, [Text.UTF8Encoding]::new($false))
    [IO.File]::WriteAllText($stderrPath, $stderr, [Text.UTF8Encoding]::new($false))
    $results.Add([ordered]@{
        module = $Module
        name = $Name
        command = "$Executable $($Arguments -join ' ')"
        startedAt = $stepStart.ToString('o')
        finishedAt = [DateTimeOffset]::UtcNow.ToString('o')
        exitCode = $process.ExitCode
        stdoutSha256 = (Get-FileHash -LiteralPath $stdoutPath -Algorithm SHA256).Hash.ToLowerInvariant()
        stderrSha256 = (Get-FileHash -LiteralPath $stderrPath -Algorithm SHA256).Hash.ToLowerInvariant()
    })
    if ($process.ExitCode -ne 0) {
        throw "$Module/$Name failed (exit=$($process.ExitCode)): $(Sanitize $stderr)"
    }
    return $stdout
}

function Invoke-NpmStep([hashtable] $Module, [string] $Name, [string[]] $Arguments) {
    $frontend = Join-Path $rootPath "$($Module.Name)/frontend"
    $environment = @{
        CPF_SOURCE_SHA = $sourceSha
        CPF_EXPECTED_SOURCE_SHA = $sourceSha
        npm_config_registry = $ApprovedRegistry
        npm_config_cache = $Module.NpmCache
        npm_config_audit = 'false'
        npm_config_fund = 'false'
    }
    if ($Name.StartsWith('playwright-')) {
        $environment.CPF_FRONTEND_URL = $Module.BaseUrl
        $environment.CPF_E2E_AUTH_STATE = $Module.AuthState
        $environment.CPF_E2E_PRIVILEGED_ENDPOINTS = $Module.PrivilegedEndpoints
        $environment.CPF_E2E_ROUTE_MATRIX = $Module.RouteMatrix
        $environment.CPF_E2E_FAILURE_MATRIX = $Module.FailureMatrix
        $environment.CPF_E2E_SECURITY_FIXTURE = $Module.SecurityFixture
        $environment.CPF_E2E_RELEASE = 'true'
    }
    Invoke-ProcessStep $Module.Name $Name $npmCommand $Arguments $environment $frontend
}

$modules = @(
    @{
        Name = 'cpf-admin'
        Code = 'ADM'
        BaseUrl = $AdmBaseUrl
        AuthState = (Resolve-Path $AdmAuthState).Path
        PrivilegedEndpoints = $AdmPrivilegedEndpoints
        RouteMatrix = (Resolve-Path $AdmRouteMatrix).Path
        FailureMatrix = (Resolve-Path $AdmFailureMatrix).Path
        SecurityFixture = (Resolve-Path $AdmSecurityFixture).Path
        NpmCache = (Join-Path $work 'npm-cache-adm')
    },
    @{
        Name = 'cpf-biz-admin'
        Code = 'BZA'
        BaseUrl = $BzaBaseUrl
        AuthState = (Resolve-Path $BzaAuthState).Path
        PrivilegedEndpoints = $BzaPrivilegedEndpoints
        RouteMatrix = (Resolve-Path $BzaRouteMatrix).Path
        FailureMatrix = (Resolve-Path $BzaFailureMatrix).Path
        SecurityFixture = (Resolve-Path $BzaSecurityFixture).Path
        NpmCache = (Join-Path $work 'npm-cache-bza')
    }
)

try {
    foreach ($module in $modules) {
        $frontend = Join-Path $rootPath "$($module.Name)/frontend"
        New-Item -ItemType Directory -Path $module.NpmCache -Force | Out-Null
        Remove-Item -Recurse -Force -ErrorAction SilentlyContinue `
            (Join-Path $frontend 'node_modules'), `
            (Join-Path $frontend 'dist'), `
            (Join-Path $frontend 'coverage'), `
            (Join-Path $frontend 'test-results'), `
            (Join-Path $frontend 'playwright-report')

        try {
            $openApiEvidence = Join-Path $work "$($module.Name)-openapi.sanitized.json"
            & (Join-Path $rootPath 'cpf-tools/scripts/export-cpf-backend-openapi.ps1') `
                -Root $rootPath `
                -Module $module.Code `
                -BaseUrl $module.BaseUrl `
                -Release `
                -EvidenceOutput $openApiEvidence
            if ($LASTEXITCODE -ne 0) { throw "$($module.Name) Runtime OpenAPI export failed" }

            $registryOutput = Invoke-NpmStep $module 'registry' @('config', 'get', 'registry')
            if ($registryOutput.Trim().TrimEnd('/') -ne $ApprovedRegistry.Trim().TrimEnd('/')) {
                throw "$($module.Name) registry mismatch: expected=$ApprovedRegistry actual=$($registryOutput.Trim())"
            }
            Invoke-NpmStep $module 'npm-ci' @('ci', '--ignore-scripts')
            Invoke-NpmStep $module 'approved-install-scripts' @('rebuild', 'esbuild', 'vue-demi', '--foreground-scripts')
            Invoke-NpmStep $module 'generate-api' @('run', 'generate:api')

            & git -C $rootPath diff --exit-code -- `
                "$($module.Name)/frontend/openapi" `
                "$($module.Name)/frontend/src/generated" `
                "$($module.Name)/frontend/package-lock.json"
            if ($LASTEXITCODE -ne 0) {
                throw "$($module.Name) generated client or lockfile drift"
            }

            foreach ($task in @('verify:lock', 'verify:installed', 'verify:consumer', 'lint', 'typecheck', 'test', 'build')) {
                Invoke-NpmStep $module $task @('run', $task)
            }
            foreach ($browser in @('chromium', 'firefox', 'webkit')) {
                Invoke-NpmStep $module "playwright-$browser" @('exec', '--', 'playwright', 'test', "--project=$browser")
            }
        } catch {
            $failures.Add("$($module.Name):$(Sanitize $_.Exception.Message)")
        }
    }

    try {
        & java (Join-Path $rootPath 'cpf-tools/scripts/Qa39Tool.java') 'browser-contract' '--root' $rootPath
        if ($LASTEXITCODE -ne 0) { throw 'Browser contract gate failed' }
    } catch {
        $failures.Add("browser-contract:$(Sanitize $_.Exception.Message)")
    }

    $finalSha = (& git -C $rootPath rev-parse HEAD).Trim()
    if ($finalSha -ne $sourceSha) {
        $failures.Add('Git SHA changed during Frontend verification.')
    }
    if ((@(& git -C $rootPath status --porcelain=v1 --untracked-files=all)).Count -gt 0) {
        $failures.Add('Frontend verification changed tracked source.')
    }

    $fixtureHashes = @{}
    foreach ($path in $inputs) {
        $fixtureHashes[[IO.Path]::GetFileName($path)] = (Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant()
    }
    $evidence = [ordered]@{
        schemaVersion = 3
        evidenceId = 'QA34-FRONTEND-3-BROWSER'
        sourceSha = $sourceSha
        resultSha = $sourceSha
        branch = (& git -C $rootPath branch --show-current).Trim()
        sourceDirty = $false
        registry = $ApprovedRegistry
        nodeVersion = (& node --version).Trim()
        npmVersion = (& $npmCommand --version).Trim()
        npmCacheMode = 'EMPTY_TEMP_PER_MODULE'
        startedAt = $startedAt.ToString('o')
        finishedAt = [DateTimeOffset]::UtcNow.ToString('o')
        exitCode = if ($failures.Count -eq 0) { 0 } else { 1 }
        requirements = @('QA34-REQ-004', 'QA34-REQ-005', 'QA34-REQ-006', 'QA34-REQ-007', 'QA34-REQ-008', 'QA34-REQ-009')
        results = $results
        fixtureSha256 = $fixtureHashes
        failures = @($failures)
        sanitized = $true
        releaseEligible = $failures.Count -eq 0
    }
    New-Item -ItemType Directory -Path (Split-Path $EvidenceOutput) -Force | Out-Null
    [IO.File]::WriteAllText(
        $EvidenceOutput,
        ($evidence | ConvertTo-Json -Depth 15) + "`n",
        [Text.UTF8Encoding]::new($false))

    if ($failures.Count -gt 0) {
        throw "QA34 Frontend runtime failed: $($failures -join '; ')"
    }
    Write-Host "[CPF][QA34][PASS] frontend runtime evidence=$EvidenceOutput"
} finally {
    foreach ($module in $modules) {
        $frontend = Join-Path $rootPath "$($module.Name)/frontend"
        Remove-Item -Recurse -Force -ErrorAction SilentlyContinue `
            (Join-Path $frontend 'node_modules'), `
            (Join-Path $frontend 'dist'), `
            (Join-Path $frontend 'coverage'), `
            (Join-Path $frontend 'test-results'), `
            (Join-Path $frontend 'playwright-report')
    }
    if (-not $KeepWorkspace) {
        Remove-Item -Recurse -Force -ErrorAction SilentlyContinue $work
    }
}
