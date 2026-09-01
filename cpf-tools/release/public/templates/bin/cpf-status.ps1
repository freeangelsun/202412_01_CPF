param(
    [Parameter(Mandatory = $false)][string] $Target,
    [Parameter(ValueFromRemainingArguments = $true)][string[]] $ArgsFromCli
)
# Thin wrapper. 실행 엔진은 cpf-cli.jar 이 단독 소유하며 이 파일은 인자 전달만 한다.
$ErrorActionPreference = 'Stop'
$Utf8 = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $Utf8; [Console]::OutputEncoding = $Utf8; $OutputEncoding = $Utf8
$Root = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$forwarded = @('runtime', 'status')
if (-not [string]::IsNullOrWhiteSpace($Target)) { $forwarded += @('--target', $Target) }
if ($ArgsFromCli) { $forwarded += $ArgsFromCli }
& (Join-Path $PSScriptRoot 'cpf.ps1') @forwarded
exit $LASTEXITCODE
