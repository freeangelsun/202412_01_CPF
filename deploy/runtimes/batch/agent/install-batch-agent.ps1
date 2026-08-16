param([string]$Root='C:\cpf')
$ErrorActionPreference='Stop';$base=(Resolve-Path "$PSScriptRoot\..").Path
$services=@('cpf-batch-control-plane','cpf-batch-scheduler','cpf-batch-worker','cpf-batch-center-cut','cpf-batch-agent')
New-Item -ItemType Directory -Force -Path (Join-Path $Root 'bin')|Out-Null
Copy-Item (Join-Path $base 'bin\cpf-runtime.ps1') (Join-Path $Root 'bin\cpf-runtime.ps1') -Force
Copy-Item (Join-Path $base 'bin\cpf-service-control.ps1') (Join-Path $Root 'bin\cpf-service-control.ps1') -Force
foreach($service in $services){
 $serviceRoot=Join-Path $Root $service;foreach($d in @('releases','config','work')){New-Item -ItemType Directory -Force -Path (Join-Path $serviceRoot $d)|Out-Null};New-Item -ItemType Directory -Force -Path (Join-Path $Root "logs\$service")|Out-Null
 Copy-Item (Join-Path $base "bin\$service.ps1") (Join-Path $Root "bin\$service.ps1") -Force
 $template=Join-Path $base "config\$service.properties";$target=Join-Path $serviceRoot "config\$service.properties";if((Test-Path $template)-and-not(Test-Path $target)){Copy-Item $template $target}
}
Write-Host 'BAT managed-service Windows layout installed. Configure startup policy for Batch Agent using the approved enterprise service wrapper/task policy.'
