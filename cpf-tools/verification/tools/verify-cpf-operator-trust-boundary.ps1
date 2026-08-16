param([string]$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot "../../..")).Path, [string]$JsonOutput = "")
$python = Get-Command python -ErrorAction Stop
$argsList = @((Join-Path $PSScriptRoot "verify-cpf-operator-trust-boundary.py"), "--root", $ProjectRoot)
if ($JsonOutput) { $argsList += @("--json-output", $JsonOutput) }
& $python.Source @argsList
if ($LASTEXITCODE -ne 0) { throw "CPF operator trust boundary verification failed." }
