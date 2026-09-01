param([Parameter(ValueFromRemainingArguments=$true)][string[]]$Args)
& (Join-Path $PSScriptRoot 'cpf.ps1') runtime stop @Args
exit $LASTEXITCODE
