# canonical entrypoint 는 bin/cpf.ps1 하나다. 이 script 는 하위 호환 thin wrapper 이며
# 자체 명령 해석을 하지 않는다. 자체 해석을 넣으면 OS 사이 의미가 갈라진다.
# PowerShell 사용자는 -Target 을 기대한다. canonical CLI 의 --target 으로 넘긴다.
# 위치 인자(cpf start admin)도 정본 CLI 가 그대로 받는다. 이 wrapper 는 하위 호환 경로다.
param(
    [Alias('t')]
    [string] $Target,
    [Parameter(ValueFromRemainingArguments=$true)][string[]] $Rest
)
$forwarded = @()
if ($Target) { $forwarded += @('--target', $Target) }
if ($Rest) { $forwarded += $Rest }
& (Join-Path $PSScriptRoot 'cpf.ps1') log @forwarded
exit $LASTEXITCODE
