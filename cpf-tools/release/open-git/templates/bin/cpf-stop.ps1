# PowerShell 사용자는 -Target 을 기대한다. 정본 CLI 는 --target 을 받으므로 여기서 옮겨준다.
# 이 변환이 없으면 README 대로 복사한 명령이 'target is required' 로 실패한다.
param(
    [Alias('t')]
    [string] $Target,
    [Parameter(ValueFromRemainingArguments=$true)][string[]] $Rest
)
$forwarded = @()
if ($Target) { $forwarded += @('--target', $Target) }
if ($Rest) { $forwarded += $Rest }
& (Join-Path $PSScriptRoot 'cpf.ps1') runtime stop @forwarded
exit $LASTEXITCODE
