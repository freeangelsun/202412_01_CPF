[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)][ValidatePattern('^[0-9a-fA-F]{40}$')][string]$ExpectedHead,
    [string]$EvidenceDir = 'build/evidence/r6-release',
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
$out = if ([IO.Path]::IsPathRooted($EvidenceDir)) { $EvidenceDir } else { Join-Path $root $EvidenceDir }
New-Item -ItemType Directory -Force -Path $out | Out-Null
$ledger = [Collections.Generic.List[object]]::new()
function Invoke-Gate {
    param([string]$Id,[string]$File,[string[]]$Arguments,[string]$WorkingDirectory=$root)
    $started = [DateTimeOffset]::UtcNow
    $stdout = Join-Path $out "$Id.stdout.log"; $stderr = Join-Path $out "$Id.stderr.log"
    $process = Start-Process -FilePath $File -ArgumentList $Arguments -WorkingDirectory $WorkingDirectory -NoNewWindow -Wait -PassThru -RedirectStandardOutput $stdout -RedirectStandardError $stderr
    $ledger.Add([ordered]@{id=$Id;command=($File+' '+($Arguments -join ' '));startedAt=$started.ToString('O');finishedAt=[DateTimeOffset]::UtcNow.ToString('O');exitCode=$process.ExitCode;status=$(if($process.ExitCode -eq 0){'PASS'}else{'FAIL'});stdout=[IO.Path]::GetFileName($stdout);stderr=[IO.Path]::GetFileName($stderr)})
    if ($process.ExitCode -ne 0) { throw "Gate failed: $Id exit=$($process.ExitCode)" }
}
try {
    Invoke-Gate 'python-r6-contract' 'python' @('cpf-tools/verification/final-dev/verify-r6-approval-contract.py',$root)
    Invoke-Gate 'python-r6-behavior' 'python' @('cpf-tools/verification/final-dev/verify-r6-behavior-contracts.py',$root)
    Invoke-Gate 'python-db3-contract' 'python' @('cpf-tools/verification/final-dev/verify-db3-runner-contract.py')
    Invoke-Gate 'python-qa38' 'python' @('cpf-tools/verification/qa38/verify-qa38-structure.py','.')
    Invoke-Gate 'python-qa39' 'python' @('cpf-tools/verification/qa39/verify-qa39-canonical-starter-closure.py')
    Invoke-Gate 'gradle-version' (Join-Path $root 'gradlew.bat') @('--version')
    Invoke-Gate 'gradle-clean-build' (Join-Path $root 'gradlew.bat') @('--no-daemon','--max-workers=1','clean','build','--stacktrace')
    Invoke-Gate 'gradle-quality-publication' (Join-Path $root 'gradlew.bat') @('--no-daemon','--max-workers=1','aggregateQualityBuild','publicationGate','--stacktrace')
    Invoke-Gate 'npm-ci' 'npm.cmd' @('ci') (Join-Path $root 'cpf-admin/frontend')
    Invoke-Gate 'npm-verify' 'npm.cmd' @('run','verify') (Join-Path $root 'cpf-admin/frontend')
    if ($RunBrowser) { Invoke-Gate 'npm-playwright' 'npm.cmd' @('run','test:e2e') (Join-Path $root 'cpf-admin/frontend') }
    if ($RunDb3) { Invoke-Gate 'db3-live' 'pwsh' @('-NoProfile','-File','cpf-tools/verification/final-dev/run-db3-lifecycle.ps1','-ExpectedHead',$head,'-EvidenceDir',(Join-Path $out 'db3')) }
    if ($RunMultiprocess) { Invoke-Gate 'multiprocess-chaos' 'pwsh' @('-NoProfile','-File','cpf-tools/verification/final-dev/run-multiprocess-chaos.ps1','-ExpectedHead',$head) }
}
finally {
    $summary=[ordered]@{protocol='CPF-R6-RELEASE-GATES-1';expectedHead=$ExpectedHead.ToLowerInvariant();actualHead=$head;createdAt=[DateTimeOffset]::UtcNow.ToString('O');gates=$ledger}
    $summary | ConvertTo-Json -Depth 8 | Set-Content -LiteralPath (Join-Path $out 'r6-release-summary.json') -Encoding utf8NoBOM
}
