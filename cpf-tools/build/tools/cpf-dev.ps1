param(
  [Parameter(Position=0)][string]$Action='help',
  [Parameter(ValueFromRemainingArguments=$true)][string[]]$ArgsFromCli
)
# Compatibility wrapper. Official Tooling Interface is `cpf`.
$ErrorActionPreference='Stop';$root=(Resolve-Path (Join-Path $PSScriptRoot '..\..\..')).Path;$cli=Join-Path $root 'cpf-tools\runtime\cli\cpf.ps1'
$map=@{
  'help'=@('help');'build'=@('build');'test'=@('test');'verify-fast'=@('verify','all');'verify-targeted'=@('dev','targeted-test');'verify-full'=@('dev','full-validation');'run-local'=@('run');'run-batch'=@('dev','run-batch');'status'=@('status');'stop'=@('stop');'modules'=@('dev','modules');'resource'=@('dev','resource')
}
if(!$map.ContainsKey($Action)){Write-Error "CPF_DEV=FAIL unsupported action=$Action; use 'cpf help'";exit 2}
& $cli @($map[$Action]) @ArgsFromCli
exit $LASTEXITCODE
