param([Parameter(ValueFromRemainingArguments=$true)][string[]]$ArgsFromCli)
$ErrorActionPreference='Stop'
& (Join-Path $PSScriptRoot 'bin\cpf.ps1') @ArgsFromCli
exit $LASTEXITCODE
