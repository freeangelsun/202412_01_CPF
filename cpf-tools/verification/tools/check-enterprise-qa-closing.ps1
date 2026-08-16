param([string]$Root=(Resolve-Path "$PSScriptRoot\..\..\..").Path)
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
$Root=(Resolve-Path -LiteralPath $Root).Path
$python=(Get-Command python -ErrorAction Stop).Source
function Invoke-Required([string]$Name,[string]$Relative,[string[]]$Args=@()){
    $path=Join-Path $Root $Relative
    if(-not(Test-Path -LiteralPath $path -PathType Leaf)){throw "required gate missing: $Relative"}
    Write-Host "[CPF][CLOSING] START $Name"
    if($path.EndsWith('.py',[StringComparison]::OrdinalIgnoreCase)){& $python $path @Args}else{& pwsh -NoProfile -File $path @Args}
    if($LASTEXITCODE -ne 0){throw "$Name failed: exit=$LASTEXITCODE"}
    Write-Host "[CPF][CLOSING] PASS $Name"
}
Invoke-Required 'NXT3 current static closure' 'cpf-tools/verification/nxt3/run_nxt3_final_all.py' @('--root',$Root)
Invoke-Required 'Runtime Control public boundary' 'cpf-tools/runtime/tools/check-runtime-control-public-boundary.ps1' @('-Root',$Root)
Invoke-Required 'Notification portable SQL' 'cpf-tools/db/verification/check-notification-portable-sql.ps1' @('-Root',$Root)
Invoke-Required 'Local runtime topology' 'cpf-tools/runtime/tools/check-local-runtime-topology.ps1' @('-Root',$Root)
Invoke-Required 'Text control characters' 'cpf-tools/verification/tools/check-text-control-characters.ps1' @('-Root',$Root)
Invoke-Required 'Migration checksums' 'cpf-tools/db/verification/check-migration-checksums.ps1' @('-Root',$Root)
Invoke-Required 'Spring Batch sequence contract' 'cpf-tools/verification/tools/check-spring-batch-sequence-contract.ps1' @('-Root',$Root)
Write-Host '[PASS] CPF current QA closing static gate'
