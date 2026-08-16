param([string]$ProjectRoot=(Resolve-Path (Join-Path $PSScriptRoot "../../..")).Path,[string]$JsonOutput="")
$python=Get-Command python -ErrorAction Stop
$argsList=@((Join-Path $PSScriptRoot "verify-cpf-network-policy-consumers.py"),"--root",$ProjectRoot)
if($JsonOutput){$argsList+=@("--json-output",$JsonOutput)}
& $python.Source @argsList
if($LASTEXITCODE-ne 0){throw "CPF shared network policy verification failed."}
