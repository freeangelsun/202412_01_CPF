param(
    [string]$Root = '.',
    [string]$OutputDir = 'cpf-docs/evidence/current/supply-chain',
    [string[]]$ArtifactPaths = @(),
    [switch]$AllowDirty
)

Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

function Invoke-Checked {
    param([string]$Name, [string]$FilePath, [string[]]$Arguments)
    Write-Host "[CPF][SUPPLY-CHAIN] START $Name"
    & $FilePath @Arguments
    if ($LASTEXITCODE -ne 0) { throw "$Name failed (exit=$LASTEXITCODE)" }
    Write-Host "[CPF][SUPPLY-CHAIN] PASS  $Name"
}

function Require-Tool([string]$Name) {
    $command = Get-Command $Name -ErrorAction SilentlyContinue
    if (-not $command) { throw "$Name executable is required" }
    return $command.Source
}

function Get-ToolEvidence([string]$Name, [string]$Path, [string[]]$VersionArguments) {
    $output = (& $Path @VersionArguments 2>&1 | Out-String).Trim()
    if ($LASTEXITCODE -ne 0) { throw "$Name version failed" }
    $binaryHash = if (Test-Path -LiteralPath $Path -PathType Leaf) {
        (Get-FileHash -Algorithm SHA256 -LiteralPath $Path).Hash.ToLowerInvariant()
    } else {
        throw "$Name executable path is not a file: $Path"
    }
    return [ordered]@{
        path = [System.IO.Path]::GetFullPath($Path)
        sha256 = $binaryHash
        version = $output
    }
}

function Get-TreeHash([string]$Path) {
    if (Test-Path -LiteralPath $Path -PathType Leaf) {
        return (Get-FileHash -Algorithm SHA256 -LiteralPath $Path).Hash.ToLowerInvariant()
    }
    $lines = Get-ChildItem -Recurse -File -LiteralPath $Path | Sort-Object FullName | ForEach-Object {
        $relative = [System.IO.Path]::GetRelativePath((Resolve-Path $Path).Path, $_.FullName).Replace('\\','/')
        $hash = (Get-FileHash -Algorithm SHA256 -LiteralPath $_.FullName).Hash.ToLowerInvariant()
        "$hash  $relative"
    }
    if (-not $lines) { throw "Final artifact directory is empty: $Path" }
    $temp = [System.IO.Path]::GetTempFileName()
    try {
        [System.IO.File]::WriteAllLines($temp, $lines, [System.Text.UTF8Encoding]::new($false))
        return (Get-FileHash -Algorithm SHA256 -LiteralPath $temp).Hash.ToLowerInvariant()
    }
    finally { Remove-Item -Force -ErrorAction SilentlyContinue $temp }
}

$original = Get-Location
try {
    $rootPath = (Resolve-Path $Root).Path
    Set-Location $rootPath
    $sha = (& git rev-parse HEAD).Trim()
    if ($LASTEXITCODE -ne 0 -or $sha -notmatch '^[0-9a-f]{40}$') { throw 'git SHA resolution failed' }
    if (-not $AllowDirty -and ((& git status --porcelain=v1 | Out-String).Trim())) {
        throw 'Supply-chain Evidence requires a clean Working Tree.'
    }

    $gradle = Join-Path $rootPath 'gradlew.bat'
    $ort = Require-Tool 'ort'
    $syft = Require-Tool 'syft'
    $grype = Require-Tool 'grype'
    $python = Require-Tool 'python'

    $resolvedOutput = Join-Path $rootPath $OutputDir
    Remove-Item -Recurse -Force -ErrorAction SilentlyContinue $resolvedOutput
    New-Item -ItemType Directory -Force $resolvedOutput | Out-Null

    Invoke-Checked 'CycloneDX' $gradle @('cyclonedxBom', '--no-daemon', '--stacktrace')

    if ($ArtifactPaths.Count -eq 0) {
        $ArtifactPaths = @(
            Get-ChildItem -Recurse -File -Path @('cpf-*/build/libs/*.jar','cpf-*/build/libs/*.war','cpf-*/build/distributions/*.zip') -ErrorAction SilentlyContinue |
                Where-Object { $_.Name -notmatch '(-plain|-sources|-javadoc)\\.' } |
                ForEach-Object { $_.FullName }
            Get-ChildItem -Directory -Path @('cpf-admin/frontend/dist','cpf-biz-admin/frontend/dist') -ErrorAction SilentlyContinue |
                ForEach-Object { $_.FullName }
        )
    }
    $ArtifactPaths = @($ArtifactPaths | ForEach-Object { (Resolve-Path $_).Path } | Sort-Object -Unique)
    if ($ArtifactPaths.Count -eq 0) { throw 'No final deployable artifact was found for Syft/Grype scan.' }

    $ortDir = Join-Path $resolvedOutput 'ort'
    Invoke-Checked 'ORT analyze' $ort @('analyze', '-i', $rootPath, '-o', $ortDir)
    $analyzer = Get-ChildItem -Recurse -File $ortDir | Where-Object { $_.Name -match 'analyzer-result.*\\.(yml|yaml|json)$' } | Select-Object -First 1
    if (-not $analyzer) { throw 'ORT analyzer result was not generated.' }

    $rules = Join-Path $rootPath 'cpf-tools/supply-chain/ort/evaluator.rules.kts'
    if (-not (Test-Path -LiteralPath $rules)) { throw 'ORT evaluator rules are required.' }
    $evaluationDir = Join-Path $resolvedOutput 'ort-evaluation'
    Invoke-Checked 'ORT evaluate' $ort @('evaluate', '-i', $analyzer.FullName, '-o', $evaluationDir, '--rules-file', $rules)
    $evaluation = Get-ChildItem -Recurse -File $evaluationDir | Where-Object { $_.Name -match 'evaluation-result.*\\.(yml|yaml|json)$' } | Select-Object -First 1
    if (-not $evaluation) { throw 'ORT evaluation result was not generated.' }
    $reportDir = Join-Path $resolvedOutput 'ort-report'
    Invoke-Checked 'ORT report' $ort @('report', '-i', $evaluation.FullName, '-o', $reportDir, '-f', 'WebApp,NoticeTemplate')

    $records = @()
    $index = 0
    foreach ($artifact in $ArtifactPaths) {
        $index++
        $name = [System.IO.Path]::GetFileName($artifact)
        if ([string]::IsNullOrWhiteSpace($name)) { $name = "artifact-$index" }
        $safeName = ($name -replace '[^A-Za-z0-9._-]', '_')
        $sbom = Join-Path $resolvedOutput "$index-$safeName.syft.cdx.json"
        $grypeReport = Join-Path $resolvedOutput "$index-$safeName.grype.json"
        Invoke-Checked "Syft $name" $syft @($artifact, '-o', "cyclonedx-json=$sbom")
        Invoke-Checked "Grype $name" $grype @("sbom:$sbom", '-o', 'json', '--file', $grypeReport, '--fail-on', 'high')
        Invoke-Checked "License policy $name" $python @(
            'cpf-tools/scripts/verify-cpf-supply-chain.py', '--root', $rootPath,
            '--sbom', $sbom, '--release')
        $records += [ordered]@{
            artifactPath = [System.IO.Path]::GetRelativePath($rootPath, $artifact).Replace('\\','/')
            artifactSha256 = Get-TreeHash $artifact
            sbomPath = [System.IO.Path]::GetRelativePath($rootPath, $sbom).Replace('\\','/')
            sbomSha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $sbom).Hash.ToLowerInvariant()
            vulnerabilityReportPath = [System.IO.Path]::GetRelativePath($rootPath, $grypeReport).Replace('\\','/')
            vulnerabilityReportSha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $grypeReport).Hash.ToLowerInvariant()
        }
    }

    $java = Require-Tool 'java'
    $toolEvidence = [ordered]@{
        ort = Get-ToolEvidence 'ort' $ort @('version')
        syft = Get-ToolEvidence 'syft' $syft @('version')
        grype = Get-ToolEvidence 'grype' $grype @('version')
        python = Get-ToolEvidence 'python' $python @('--version')
        java = Get-ToolEvidence 'java' $java @('-version')
        gradleWrapper = [ordered]@{
            path = [System.IO.Path]::GetRelativePath($rootPath, $gradle).Replace('\','/')
            sha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $gradle).Hash.ToLowerInvariant()
            wrapperJarSha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath (Join-Path $rootPath 'gradle/wrapper/gradle-wrapper.jar')).Hash.ToLowerInvariant()
            version = (& $gradle --version --no-daemon 2>&1 | Out-String).Trim()
        }
    }
    if ($LASTEXITCODE -ne 0) { throw 'Gradle version failed' }

    $evidence = [ordered]@{
        schemaVersion = 2
        sourceSha = $sha
        generatedAt = (Get-Date).ToUniversalTime().ToString('o')
        tools = $toolEvidence
        ortAnalyzerSha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $analyzer.FullName).Hash.ToLowerInvariant()
        ortEvaluationSha256 = (Get-FileHash -Algorithm SHA256 -LiteralPath $evaluation.FullName).Hash.ToLowerInvariant()
        artifacts = $records
        sanitized = $true
    }
    $indexPath = Join-Path $resolvedOutput 'CPF_SUPPLY_CHAIN_EVIDENCE_INDEX.sanitized.json'
    [System.IO.File]::WriteAllText(
        $indexPath,
        ($evidence | ConvertTo-Json -Depth 10) + "`n",
        [System.Text.UTF8Encoding]::new($false))
    $indexHash = (Get-FileHash -Algorithm SHA256 -LiteralPath $indexPath).Hash.ToLowerInvariant()
    [System.IO.File]::WriteAllText(
        "$indexPath.sha256",
        "$indexHash  CPF_SUPPLY_CHAIN_EVIDENCE_INDEX.sanitized.json`n",
        [System.Text.UTF8Encoding]::new($false))
}
finally {
    Set-Location $original
}
