param(
    [string]$Root = '.',
    [Parameter(Mandatory)][string]$AdmOpenApiFile,
    [Parameter(Mandatory)][string]$BzaOpenApiFile,
    [string]$Registry,
    [string]$EvidenceDir = 'build/qa33-frontend-evidence',
    [switch]$SkipBrowserInstall
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Invoke-Checked([string]$Name, [scriptblock]$Action) {
    Write-Host "[CPF][QA33][FRONTEND] START $Name"
    & $Action
    $code = if ($null -eq $LASTEXITCODE) { 0 } else { [int]$LASTEXITCODE }
    if ($code -ne 0) { throw "$Name failed (exit=$code)" }
    Write-Host "[CPF][QA33][FRONTEND] PASS  $Name"
}

function Get-CpfDirectoryDigest([string]$Directory, [string[]]$ExcludedRelativePaths = @()) {
    $base = (Resolve-Path $Directory).Path
    $excluded = @{}
    foreach ($item in $ExcludedRelativePaths) { $excluded[$item.Replace('\\','/')] = $true }
    $lines = [System.Collections.Generic.List[string]]::new()
    foreach ($file in Get-ChildItem -LiteralPath $base -Recurse -Force -File | Sort-Object FullName) {
        $relative = [System.IO.Path]::GetRelativePath($base, $file.FullName).Replace('\\','/')
        if ($excluded.ContainsKey($relative)) { continue }
        $hash = (Get-FileHash -Algorithm SHA256 -LiteralPath $file.FullName).Hash.ToLowerInvariant()
        $lines.Add("$relative`t$hash")
    }
    $payload = [System.Text.Encoding]::UTF8.GetBytes(($lines -join "`n") + "`n")
    $algorithm = [System.Security.Cryptography.SHA256]::Create()
    try { return [Convert]::ToHexString($algorithm.ComputeHash($payload)).ToLowerInvariant() }
    finally { $algorithm.Dispose() }
}

function Write-CpfJson([string]$Path, [object]$Value) {
    $parent = Split-Path -Parent $Path
    if ($parent) { New-Item -ItemType Directory -Force $parent | Out-Null }
    [System.IO.File]::WriteAllText(
        $Path,
        ($Value | ConvertTo-Json -Depth 12) + "`n",
        [System.Text.UTF8Encoding]::new($false))
}

$original = Get-Location
$oldRegistry = $env:npm_config_registry
try {
    $rootPath = (Resolve-Path $Root).Path
    Set-Location $rootPath
    $sourceSha = (& git rev-parse HEAD).Trim()
    if ($LASTEXITCODE -ne 0 -or $sourceSha -notmatch '^[0-9a-f]{40}$') {
        throw 'Frontend preparation requires a valid Git HEAD.'
    }
    if ((& git status --porcelain=v1 | Out-String).Trim()) {
        throw 'Frontend preparation requires a clean Working Tree.'
    }

    $node = (& node --version).Trim()
    $npm = (& npm.cmd --version).Trim()
    if ($node -ne 'v22.16.0') { throw "Node v22.16.0 is required; actual=$node" }
    if ($npm -ne '10.9.2') { throw "npm 10.9.2 is required; actual=$npm" }
    if ($Registry) { $env:npm_config_registry = $Registry }

    $resolvedEvidence = Join-Path $rootPath $EvidenceDir
    New-Item -ItemType Directory -Force $resolvedEvidence | Out-Null
    $canonicalRoot = Join-Path $rootPath 'build/qa33-openapi'
    New-Item -ItemType Directory -Force $canonicalRoot | Out-Null

    $env:CPF_SOURCE_SHA = $sourceSha

    $targets = @(
        @{ Name = 'ADM'; Key = 'adm'; Dir = 'cpf-admin/frontend'; OpenApi = $AdmOpenApiFile },
        @{ Name = 'BZA'; Key = 'bza'; Dir = 'cpf-biz-admin/frontend'; OpenApi = $BzaOpenApiFile }
    )
    foreach ($target in $targets) {
        $targetStartedAt = (Get-Date).ToUniversalTime().ToString('o')
        $directory = Join-Path $rootPath $target.Dir
        $openApi = (Resolve-Path $target.OpenApi).Path
        $canonicalOpenApi = Join-Path $canonicalRoot "$($target.Key)-openapi.json"
        Copy-Item -Force $openApi $canonicalOpenApi
        $openApiHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $canonicalOpenApi).Hash.ToLowerInvariant()

        Push-Location $directory
        try {
            $cache = Join-Path $rootPath "build/npm-cache-$($target.Key)"
            Remove-Item -Recurse -Force -ErrorAction SilentlyContinue $cache
            $lockArguments = @('install', '--package-lock-only', '--ignore-scripts', '--no-audit', '--no-fund', '--cache', $cache)
            Invoke-Checked "$($target.Name) lock regeneration" { & npm.cmd @lockArguments }
            Invoke-Checked "$($target.Name) clean install" { & npm.cmd ci --no-audit --no-fund --cache $cache }

            $env:CPF_OPENAPI_FILE = $canonicalOpenApi
            Invoke-Checked "$($target.Name) Orval generation" { & npm.cmd run generate:api }

            $generatedDirectory = Join-Path $directory 'src/generated'
            if (-not (Test-Path -LiteralPath $generatedDirectory -PathType Container)) {
                throw "$($target.Name) generated client directory is missing: $generatedDirectory"
            }
            $generatedHash = Get-CpfDirectoryDigest $generatedDirectory @('source-sha.json')
            $marker = [ordered]@{
                schemaVersion = 1
                sourceSha = $sourceSha
                openApiSha256 = $openApiHash
                generatedClientSha256 = $generatedHash
                generator = 'orval'
                nodeVersion = $node
                npmVersion = $npm
                sanitized = $true
            }
            Write-CpfJson (Join-Path $generatedDirectory 'source-sha.json') $marker

            Invoke-Checked "$($target.Name) generated hash" { & npm.cmd run verify:generated }
            $generatedPathSpec = ":(top)$($target.Dir)/src/generated"
            $openApiPathSpec = ":(top)$($target.Dir)/openapi/cpf-openapi.json"
            Invoke-Checked "$($target.Name) generated git drift" {
                & git diff --exit-code -- $generatedPathSpec $openApiPathSpec
            }
            $artifactDrift = (& git status --porcelain=v1 -- package-lock.json src/generated | Out-String).Trim()
            if ($artifactDrift) {
                throw "$($target.Name) lock/generated artifacts changed. Apply and commit the generated artifacts, then rerun verification.`n$artifactDrift"
            }

            Invoke-Checked "$($target.Name) lint" { & npm.cmd run lint }
            Invoke-Checked "$($target.Name) typecheck" { & npm.cmd run typecheck }
            Invoke-Checked "$($target.Name) unit test" { & npm.cmd run test }
            Invoke-Checked "$($target.Name) build" { & npm.cmd run build }
            if (-not $SkipBrowserInstall) {
                Invoke-Checked "$($target.Name) Playwright install" { & npx.cmd playwright install chromium firefox webkit }
            }
            foreach ($browser in @('chromium','firefox','webkit')) {
                Invoke-Checked "$($target.Name) Playwright $browser" { & npx.cmd --no-install playwright test "--project=$browser" }
            }

            $distDirectory = Join-Path $directory 'dist'
            if (-not (Test-Path -LiteralPath $distDirectory -PathType Container)) {
                throw "$($target.Name) browser bundle directory is missing: $distDirectory"
            }
            $evidence = [ordered]@{
                schemaVersion = 1
                application = $target.Name
                sourceSha = $sourceSha
                startedAt = $targetStartedAt
                finishedAt = (Get-Date).ToUniversalTime().ToString('o')
                exitCode = 0
                nodeVersion = $node
                npmVersion = $npm
                packageLockSha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath (Join-Path $directory 'package-lock.json')).Hash.ToLowerInvariant()
                openApiSha256 = $openApiHash
                generatedClientSha256 = $generatedHash
                browserBundleSha256 = Get-CpfDirectoryDigest $distDirectory
                browsers = @('chromium','firefox','webkit')
                sanitized = $true
            }
            $evidencePath = Join-Path $resolvedEvidence "$($target.Key)-frontend-evidence.sanitized.json"
            Write-CpfJson $evidencePath $evidence
            $evidenceHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $evidencePath).Hash.ToLowerInvariant()
            [System.IO.File]::WriteAllText(
                "$evidencePath.sha256",
                "$evidenceHash  $([System.IO.Path]::GetFileName($evidencePath))`n",
                [System.Text.UTF8Encoding]::new($false))
        }
        finally {
            Remove-Item Env:CPF_OPENAPI_FILE -ErrorAction SilentlyContinue
            Pop-Location
        }
    }

    $finalSha = (& git rev-parse HEAD).Trim()
    if ($finalSha -ne $sourceSha) { throw "Source SHA changed during frontend verification: $sourceSha -> $finalSha" }
    if ((& git status --porcelain=v1 | Out-String).Trim()) {
        throw 'Working Tree changed during frontend verification.'
    }
}
finally {
    if ($null -eq $oldRegistry) { Remove-Item Env:npm_config_registry -ErrorAction SilentlyContinue }
    else { $env:npm_config_registry = $oldRegistry }
    Set-Location $original
}
