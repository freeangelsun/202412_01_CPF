param([switch]$ConfirmLocalReset,[int]$TimeoutSeconds=300)
$ErrorActionPreference='Stop'; if(-not $ConfirmLocalReset){throw 'reset은 -ConfirmLocalReset 명시 승인이 필요합니다.'}; $root=(Resolve-Path (Join-Path $PSScriptRoot '..')).Path
& java (Join-Path $PSScriptRoot 'CpfBootstrap.java') reset --workspace $root --confirm-local-reset --timeout-seconds $TimeoutSeconds; if($LASTEXITCODE -ne 0){throw "CPF reset failed exit=$LASTEXITCODE"}
