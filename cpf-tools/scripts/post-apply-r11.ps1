[CmdletBinding()]
param(
  [switch]$CleanupWhatIf,
  [switch]$IncludeBuildArtifacts,
  [switch]$RunBuild,
  [string[]]$Modules=@('cpf-core','cpf-common','cpf-reference','cpf-batch','cpf-gateway')
)
$ErrorActionPreference='Stop'
$here=$PSScriptRoot
$consumerGate=Join-Path $here 'verify-r11-util-consumers.ps1'
$cleanup=Join-Path $here 'cleanup-r11-obsolete.ps1'
$verify=Join-Path $here 'verify-r11-source-product.ps1'
& $consumerGate
if($CleanupWhatIf){
  & $cleanup -WhatIf -IncludeBuildArtifacts:$IncludeBuildArtifacts
  Write-Host '[INFO] Cleanup WhatIf only. Product verification was not executed.' -ForegroundColor Yellow
  exit 0
}
& $cleanup -IncludeBuildArtifacts:$IncludeBuildArtifacts
& $verify -RunBuild:$RunBuild -Modules $Modules
Write-Host '[PASS] CPF R11 post-apply consumer/cleanup/source gates completed.' -ForegroundColor Green
