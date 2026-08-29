param([Parameter(ValueFromRemainingArguments=$true)][string[]]$ArgsFromCli)
$ErrorActionPreference='Stop'
$Utf8 = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding=$Utf8; [Console]::OutputEncoding=$Utf8; $OutputEncoding=$Utf8
$Root=(Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$Cli=Join-Path $Root 'bin\lib\cpf-cli.jar'
if(!(Test-Path -LiteralPath $Cli -PathType Leaf)){Write-Error "[CPF][WRAPPER] cpf-cli.jar missing: $Cli"; exit 69}
& java '-Dfile.encoding=UTF-8' '-Dsun.stdout.encoding=UTF-8' '-Dsun.stderr.encoding=UTF-8' -jar $Cli @ArgsFromCli
exit $LASTEXITCODE
