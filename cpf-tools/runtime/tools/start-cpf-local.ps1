param([ValidateSet('local','dev','test','stg','prod')][string]$ResourceProfile='local',[ValidateSet('integrated','minimal','standard','full','integration')][string]$Mode='integrated',[switch]$SkipBuild)
# Full Runtime child-process UTF-8 contract. Keep the emitted byte stream UTF-8 even when pwsh is redirected.
$CpfUtf8ConsoleEncoding = [Text.UTF8Encoding]::new($false)
try {
    [Console]::InputEncoding = $CpfUtf8ConsoleEncoding
    [Console]::OutputEncoding = $CpfUtf8ConsoleEncoding
    $OutputEncoding = $CpfUtf8ConsoleEncoding
    $global:OutputEncoding = $CpfUtf8ConsoleEncoding
} catch { }
$env:PYTHONUTF8 = '1'
$env:PYTHONIOENCODING = 'utf-8'

$ErrorActionPreference='Stop';$root=(Resolve-Path (Join-Path $PSScriptRoot '..\..\..')).Path;$py=(Get-Command python -ErrorAction SilentlyContinue).Source;if(!$py){$py=(Get-Command py -ErrorAction SilentlyContinue).Source};if(!$py){throw 'CPF_LOCAL_RUNTIME=FAIL Python 3 is required'};$a=@((Join-Path $root 'cpf-tools\runtime\tools\cpf_local_runtime.py'),'start','--root',$root,'--profile',$ResourceProfile,'--mode',$Mode);if($SkipBuild){$a+='--skip-build'};& $py @a;exit $LASTEXITCODE
