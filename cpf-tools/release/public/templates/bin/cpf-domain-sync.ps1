param()
$ErrorActionPreference='Stop'; $root=(Resolve-Path (Join-Path $PSScriptRoot '..')).Path
& java (Join-Path $PSScriptRoot 'CpfGeneratorLauncher.java') --root $root domain sync; if($LASTEXITCODE -ne 0){throw "CPF domain sync failed exit=$LASTEXITCODE"}
