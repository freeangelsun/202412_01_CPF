[CmdletBinding()]
param([Parameter(ValueFromRemainingArguments=$true)][string[]]$Arguments)
# Windows/Jenkins thin wrapper. Python 정본과 동일한 install/start/stop/status 의미를 사용합니다.
$Root=(Resolve-Path "$PSScriptRoot\..\..").Path
python (Join-Path $Root 'deploy\tools\cpf-instance.py') @Arguments
if($LASTEXITCODE -ne 0){exit $LASTEXITCODE}
