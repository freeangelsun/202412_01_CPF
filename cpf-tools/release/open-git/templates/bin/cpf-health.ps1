param([Parameter(ValueFromRemainingArguments=$true)][string[]]$Args)
& (Join-Path $PSScriptRoot 'cpf.ps1') runtime health @Args
exit $LASTEXITCODE
