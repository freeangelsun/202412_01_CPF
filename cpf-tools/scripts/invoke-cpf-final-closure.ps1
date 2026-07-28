param(
    [string]$RepoRoot = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [Parameter(Mandatory = $true)][string[]]$DatabaseProfilePath,
    [switch]$RunGitHubGovernance
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
if ($PSVersionTable.PSVersion.Major -lt 7) { throw 'pwsh 7 이상이 필요합니다.' }
$RepoRoot = (Resolve-Path -LiteralPath $RepoRoot).Path
if ($DatabaseProfilePath.Count -ne 3) { throw 'MariaDB/PostgreSQL/Oracle Profile을 각각 하나씩 총 3개 전달해야 합니다.' }

$startedAt = [DateTimeOffset]::Now
$head = (& git -C $RepoRoot rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($head)) { throw 'Git HEAD 확인 실패' }
$dirty = @(& git -C $RepoRoot status --porcelain)
if ($dirty.Count -gt 0) { throw '최종 Closure 검증은 Clean Worktree에서만 실행합니다.' }

$evidenceDir = Join-Path $RepoRoot 'cpf-docs/evidence/final-closing'
New-Item -ItemType Directory -Path $evidenceDir -Force | Out-Null
$stamp = $startedAt.ToString('yyyyMMdd-HHmmss')
$logName = "$stamp-final-closure.log"
$resultName = "$stamp-final-closure.evidence.json"
$logPath = Join-Path $evidenceDir $logName
$resultPath = Join-Path $evidenceDir $resultName
$status = '실패'
$failure = $null
$exitCode = 1

try {
    & pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'verify-cpf-final-completion.ps1') `
        -RepoRoot $RepoRoot -RunDatabaseLifecycle -DatabaseProfilePath $DatabaseProfilePath `
        -RunGitHubGovernance:$RunGitHubGovernance *>&1 | Tee-Object -FilePath $logPath
    $exitCode = $LASTEXITCODE
    if ($exitCode -ne 0) { throw "final completion gate failed: $exitCode" }
    $status = '완료'
} catch {
    $failure = $_.Exception.Message
    if (-not (Test-Path -LiteralPath $logPath)) {
        Set-Content -LiteralPath $logPath -Value $failure -Encoding UTF8
    }
    throw
} finally {
    $endedAt = [DateTimeOffset]::Now
    if (Test-Path -LiteralPath $logPath) {
        $logText = Get-Content -LiteralPath $logPath -Raw -Encoding UTF8
        $logText = $logText `
            -replace '(?im)(password|passwd|secret|token|authorization|private[_ -]?key)\s*[:=]\s*\S+', '$1=[REDACTED]' `
            -replace '(?im)bearer\s+[A-Za-z0-9._~+/=-]+', 'Bearer [REDACTED]'
        Set-Content -LiteralPath $logPath -Value $logText -Encoding UTF8
    }
    $outputHash = (Get-FileHash -LiteralPath $logPath -Algorithm SHA256).Hash.ToLowerInvariant()
    $profileNames = @($DatabaseProfilePath | ForEach-Object { Split-Path -Leaf $_ })
    [ordered]@{
        schemaVersion = 1
        repository = 'freeangelsun/202412_01_CPF'
        branch = 'master'
        exactSha = $head
        command = 'invoke-cpf-final-closure.ps1 -DatabaseProfilePath <mariadb>,<postgresql>,<oracle> -RunGitHubGovernance'
        profile = ($profileNames -join ',')
        environment = [ordered]@{
            machine = $env:COMPUTERNAME
            os = [Environment]::OSVersion.VersionString
            pwsh = $PSVersionTable.PSVersion.ToString()
            java = (& java -version 2>&1 | Out-String).Trim()
        }
        startedAt = $startedAt.ToString('o')
        endedAt = $endedAt.ToString('o')
        exitCode = $exitCode
        outputFile = $logName
        outputSha256 = $outputHash
        redactionChecked = $true
        requirementIds = @('CPF-FINAL-TARGET','CPF-QA-BASELINE-2118','CPF-QA-MERGED-2715','CPF-QA-RUNTIME-CONTROL-SUPPLEMENT','CPF-QA-ARCH-UI-HYGIENE-20260729')
        status = $status
        failure = $failure
    } | ConvertTo-Json -Depth 10 | Set-Content -LiteralPath $resultPath -Encoding UTF8
}

& pwsh -NoProfile -ExecutionPolicy Bypass -File (Join-Path $PSScriptRoot 'check-final-evidence-contract.ps1') `
    -Root $RepoRoot -EvidenceDir $evidenceDir -RequireAll
if ($LASTEXITCODE -ne 0) { throw "final evidence contract failed: $LASTEXITCODE" }
Write-Host "[PASS] CPF final closure evidence: $resultPath"
