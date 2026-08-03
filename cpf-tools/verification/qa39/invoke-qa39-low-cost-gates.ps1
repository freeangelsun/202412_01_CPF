[CmdletBinding()]
param([string]$Root=(Get-Location).Path)
Set-StrictMode -Version Latest
$ErrorActionPreference='Stop'
$rootPath=(Resolve-Path -LiteralPath $Root).Path
$tool=Join-Path $rootPath 'cpf-tools\scripts\Qa39Tool.java'
if(-not(Test-Path -LiteralPath $tool -PathType Leaf)){throw "QA39 Java verification tool is missing: $tool"}
$java=Get-Command java.exe -CommandType Application -ErrorAction SilentlyContinue|Select-Object -First 1
if(-not$java){$java=Get-Command java -CommandType Application -ErrorAction SilentlyContinue|Select-Object -First 1}
if(-not$java){throw 'Java runtime was not found. CPF QA39 final low-cost gates require the project JDK only; Python is not used.'}
& $java.Source $tool 'low-cost' '--root' $rootPath
if($LASTEXITCODE-ne0){throw "QA39 Java low-cost gates failed: $LASTEXITCODE"}
