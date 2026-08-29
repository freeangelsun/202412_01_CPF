param([Parameter(ValueFromRemainingArguments=$true)][string[]]$ArgsFromCli)
$ErrorActionPreference='Stop'
$Utf8=[System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding=$Utf8;[Console]::OutputEncoding=$Utf8;$OutputEncoding=$Utf8
$Cli=Join-Path $PSScriptRoot 'lib\cpf-cli.jar'
$Java=if($env:JAVA_HOME){Join-Path $env:JAVA_HOME 'bin\java.exe'}else{'java.exe'}
if(!(Test-Path -LiteralPath $Cli -PathType Leaf)){[Console]::Error.WriteLine("CPF_CLI=FAIL code=CPF-CLI-JAR-MISSING message=$Cli");exit 69}
$JavaVersion=(& $Java -version 2>&1 | Out-String).Trim()
if($LASTEXITCODE-ne0 -or $JavaVersion -notmatch '(?m)version\s+"25(?:\.|")'){[Console]::Error.WriteLine("CPF_CLI=FAIL code=CPF-CLI-JAVA-VERSION message=Java_25_required actual=$($JavaVersion -replace '[\r\n]+',' ')");exit 69}
& $Java '-Dfile.encoding=UTF-8' '-Dstdout.encoding=UTF-8' '-Dstderr.encoding=UTF-8' -jar $Cli @ArgsFromCli
exit $LASTEXITCODE
