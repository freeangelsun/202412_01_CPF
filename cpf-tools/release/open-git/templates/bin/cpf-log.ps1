param([Parameter(ValueFromRemainingArguments=$true)][string[]]$Args)
& (Join-Path $PSScriptRoot 'cpf.ps1') runtime log @Args
exit $LASTEXITCODE
