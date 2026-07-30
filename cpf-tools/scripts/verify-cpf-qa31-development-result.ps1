[CmdletBinding()]
param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $BaseSha = '9594c8d5d9b1127a4e2694d0ec2f4add9475fc7e',
    [ValidateSet('report','full')][string] $Mode = 'report',
    [string] $ExpectedSha,
    [switch] $RequireClean,
    [switch] $RequireExactHeadEvidence,
    [switch] $RequireIntegratedClosure
)
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
if ($PSVersionTable.PSVersion.Major -lt 7) { throw 'QA31 Gate requires pwsh 7+.' }
$Root = (Resolve-Path -LiteralPath $Root).Path
$sourceSha = (& git -C $Root rev-parse HEAD).Trim()
if ($RequireClean -and @(& git -C $Root status --porcelain).Count -ne 0) { throw 'Clean working tree required.' }
$python = Get-Command python -ErrorAction SilentlyContinue
if (-not $python) { $python = Get-Command py -ErrorAction SilentlyContinue }
if (-not $python) { throw 'Python 3 is required for CPF QA31 Gate.' }
$script = Join-Path $Root 'cpf-tools/scripts/verify-cpf-qa31-development.py'
$output = Join-Path $Root 'cpf-docs/evidence/current/qa31-development-gate.json'
if (-not (Test-Path -LiteralPath $script -PathType Leaf)) { throw "QA31 Python gate is missing: $script" }
if ($Mode -eq 'full') {
    $selfTest = Join-Path $Root 'cpf-docs/evidence/current/qa31-development-gate-self-test.json'
    & $python.Source $script --self-test --output $selfTest
    if ($LASTEXITCODE -ne 0) { throw "CPF QA31 gate self-test failed (exit=$LASTEXITCODE)." }
}
$args = @($script, '--root', $Root, '--base-sha', $BaseSha, '--mode', $Mode, '--output', $output)
if ($ExpectedSha) { $args += @('--expected-sha', $ExpectedSha) }
if ($RequireExactHeadEvidence) { $args += '--require-exact' }
if ($RequireIntegratedClosure) { $args += '--require-integrated-closure' }
& $python.Source @args
if ($LASTEXITCODE -ne 0) { throw "CPF QA31 development gate failed (exit=$LASTEXITCODE)." }
$eduScript = Join-Path $Root 'cpf-tools/scripts/verify-cpf-reference-qa31-coverage.py'
$eduOutput = Join-Path $Root 'cpf-docs/evidence/current/qa31-reference-coverage.json'
& $python.Source $eduScript --root $Root --output $eduOutput --source-sha $sourceSha
if ($LASTEXITCODE -ne 0) { throw "CPF Reference QA31 coverage gate failed (exit=$LASTEXITCODE)." }
$bzaScript = Join-Path $Root 'cpf-tools/scripts/verify-cpf-bza-qa31-coverage.py'
$bzaOutput = Join-Path $Root 'cpf-docs/evidence/current/qa31-bza-coverage.json'
& $python.Source $bzaScript --root $Root --output $bzaOutput --source-sha $sourceSha
if ($LASTEXITCODE -ne 0) { throw "CPF BZA QA31 coverage gate failed (exit=$LASTEXITCODE)." }
Write-Host "CPF QA31 development gate PASS. report=$output edu=$eduOutput bza=$bzaOutput"
