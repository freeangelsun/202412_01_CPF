param([Parameter(Mandatory=$true)][string]$Name,[Parameter(Mandatory=$true)][string]$SystemCode,[switch]$Batch,[Parameter(ValueFromRemainingArguments=$true)][string[]]$SetupArgs)
$ErrorActionPreference='Stop'; $root=(Resolve-Path (Join-Path $PSScriptRoot '..')).Path; $args=@('--root',$root,'domain','setup','--name',$Name,'--system-code',$SystemCode); if($Batch){$args+='--batch'}; if($SetupArgs){$args+=$SetupArgs}
& java (Join-Path $PSScriptRoot 'CpfGeneratorLauncher.java') @args; if($LASTEXITCODE -ne 0){throw "CPF domain setup failed exit=$LASTEXITCODE"}
