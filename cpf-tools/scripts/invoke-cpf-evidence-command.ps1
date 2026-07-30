param(
    [Parameter(Mandatory = $true)][string] $Name,
    [Parameter(Mandatory = $true)][string[]] $RequirementIds,
    [Parameter(Mandatory = $true)][string] $Executable,
    [string[]] $ArgumentList = @(),
    [Parameter(Mandatory = $true)][string] $SanitizedCommand,
    [string] $Profile = 'default',
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $EvidenceId = '',
    [string] $ExpectedResult = 'exitCode=0',
    [switch] $AllowFailure
)

$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
if ($PSVersionTable.PSVersion.Major -lt 7) { throw 'pwsh 7 이상이 필요합니다.' }
$Root = (Resolve-Path -LiteralPath $Root).Path
$sourceSha = (& git -C $Root rev-parse HEAD).Trim().ToLowerInvariant()
if ($sourceSha -notmatch '^[0-9a-f]{40}$') { throw "Git source SHA를 확인할 수 없습니다: $sourceSha" }
if ([string]::IsNullOrWhiteSpace($EvidenceId)) {
    $safeName = ($Name.ToLowerInvariant() -replace '[^a-z0-9가-힣]+','-').Trim('-')
    $EvidenceId = "EV-$((Get-Date).ToString('yyyyMMdd-HHmmss'))-$safeName"
}
if ($SanitizedCommand -match '(?i)(password|passwd|secret|token|api[-_]?key)\s*[=:]\s*\S+') {
    throw 'SanitizedCommand에 민감정보 값이 포함되어 있습니다. 환경변수 또는 <redacted>로 바꾸십시오.'
}

$evidenceRoot = Join-Path $Root 'cpf-docs/evidence/current'
$logRoot = Join-Path $evidenceRoot 'logs'
New-Item -ItemType Directory -Force -Path $logRoot | Out-Null
$logPath = Join-Path $logRoot "$EvidenceId.log"
$jsonPath = Join-Path $evidenceRoot "$EvidenceId.json"
$startedAt = [DateTimeOffset]::Now
$exitCode = 0
$rawLines = [System.Collections.Generic.List[string]]::new()

Push-Location $Root
try {
    & $Executable @ArgumentList 2>&1 | ForEach-Object {
        $line = [string]$_
        Write-Host $line
        $rawLines.Add($line) | Out-Null
    }
    if ($null -ne $LASTEXITCODE) { $exitCode = [int]$LASTEXITCODE }
} catch {
    $exitCode = 1
    $rawLines.Add($_.Exception.ToString()) | Out-Null
} finally {
    Pop-Location
}
$finishedAt = [DateTimeOffset]::Now

# Evidence log에 흔한 credential 형태가 남지 않도록 값 부분을 제거합니다.
$sanitizedLines = foreach ($line in $rawLines) {
    $line `
        -replace '(?i)(password|passwd|secret|token|api[-_]?key)(\s*[=:]\s*)[^\s,;]+','$1$2<redacted>' `
        -replace '(?i)(authorization:\s*bearer\s+)[A-Za-z0-9._~+/=-]+','$1<redacted>' `
        -replace '(?i)(jdbc:[^\s]+[?&](?:password|user)=)[^&\s]+','$1<redacted>'
}
[IO.File]::WriteAllLines($logPath,$sanitizedLines,[Text.UTF8Encoding]::new($false))
$logHash = (Get-FileHash -LiteralPath $logPath -Algorithm SHA256).Hash.ToLowerInvariant()
$status = if ($exitCode -eq 0) { '완료' } else { '실패' }
$record = [ordered]@{
    evidenceId = $EvidenceId
    sourceSha = $sourceSha
    requirementIds = @($RequirementIds)
    command = $SanitizedCommand
    executable = $Executable
    profile = $Profile
    environment = [ordered]@{
        os = [System.Runtime.InteropServices.RuntimeInformation]::OSDescription
        powershell = $PSVersionTable.PSVersion.ToString()
        javaHome = $env:JAVA_HOME
    }
    startedAt = $startedAt.ToString('o')
    finishedAt = $finishedAt.ToString('o')
    durationMs = [math]::Round(($finishedAt - $startedAt).TotalMilliseconds)
    expected = $ExpectedResult
    actual = "exitCode=$exitCode"
    exitCode = $exitCode
    status = $status
    logPath = $logPath.Substring($Root.Length + 1).Replace('\\','/')
    logSha256 = $logHash
    sensitiveDataRemoved = $true
}
[IO.File]::WriteAllText($jsonPath,($record | ConvertTo-Json -Depth 20),[Text.UTF8Encoding]::new($false))
$indexPath = Join-Path $Root 'cpf-docs/evidence/CPF_EVIDENCE_INDEX.md'
if (-not (Test-Path -LiteralPath $indexPath -PathType Leaf)) { throw "Evidence index가 없습니다: $indexPath" }
$relativeJson = $jsonPath.Substring($Root.Length + 1).Replace('\','/')
$ids = ($RequirementIds -join '<br>')
$row = "| `$EvidenceId` | $ids | $status | `$sourceSha` | `$SanitizedCommand` | `$relativeJson` |"
$indexText = Get-Content -LiteralPath $indexPath -Raw -Encoding UTF8
$escapedId = [regex]::Escape($EvidenceId)
$indexText = [regex]::Replace($indexText, "(?m)^\|\s*`?$escapedId`?\s*\|.*(?:\r?\n)?", '')
if (-not $indexText.EndsWith([Environment]::NewLine)) { $indexText += [Environment]::NewLine }
$indexText += $row + [Environment]::NewLine
[IO.File]::WriteAllText($indexPath,$indexText,[Text.UTF8Encoding]::new($false))
Write-Host "Evidence: $relativeJson status=$status sourceSha=$sourceSha"
if ($exitCode -ne 0 -and -not $AllowFailure) { exit $exitCode }
exit 0
