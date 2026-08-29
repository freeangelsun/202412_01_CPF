param([Parameter(ValueFromRemainingArguments=$true)][string[]]$ArgsFromCli)
$ErrorActionPreference='Stop'
$Utf8 = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding=$Utf8; [Console]::OutputEncoding=$Utf8; $OutputEncoding=$Utf8
$Root=(Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$Cli=Join-Path $Root 'bin\lib\cpf-cli.jar'
if(!(Test-Path -LiteralPath $Cli -PathType Leaf)){[Console]::Error.WriteLine("[CPF][WRAPPER] cpf-cli.jar missing: $Cli"); exit 69}
$JavaVersion=(& java -version 2>&1 | Out-String).Trim()
if($LASTEXITCODE-ne0 -or $JavaVersion -notmatch '(?m)version\s+"25(?:\.|")'){[Console]::Error.WriteLine("CPF_CLI=FAIL code=CPF-CLI-JAVA-VERSION message=Java_25_required actual=$($JavaVersion -replace '[\r\n]+',' ')");exit 69}
& java '-Dfile.encoding=UTF-8' '-Dsun.stdout.encoding=UTF-8' '-Dsun.stderr.encoding=UTF-8' -jar $Cli @ArgsFromCli
exit $LASTEXITCODE
