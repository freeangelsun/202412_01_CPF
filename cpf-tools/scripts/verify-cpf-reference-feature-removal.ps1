[CmdletBinding()]
param([string]$Root=(Get-Location).Path)
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
$Root=(Resolve-Path $Root).Path
$gradle=Join-Path $Root 'gradlew.bat'
if(-not (Test-Path -LiteralPath $gradle)){throw "gradlew.bat missing: $gradle"}
$variants=@(
  @{name='batch-off'; props=@('-Pcpf.reference.features.batch.enabled=false')},
  @{name='operations-off'; props=@('-Pcpf.reference.features.operations.enabled=false')},
  @{name='backoffice-off'; props=@('-Pcpf.reference.features.backoffice.enabled=false')},
  @{name='gateway-off'; props=@('-Pcpf.reference.features.gateway.enabled=false')},
  @{name='core-only'; props=@('-Pcpf.reference.features.batch.enabled=false','-Pcpf.reference.features.operations.enabled=false','-Pcpf.reference.features.backoffice.enabled=false','-Pcpf.reference.features.gateway.enabled=false')}
)
foreach($variant in $variants){
  Write-Host "[CPF][REFERENCE][FEATURE-REMOVAL][RUN] $($variant.name)"
  $args=@('--no-daemon','--stacktrace',':cpf-reference:compileJava',':cpf-reference:printReferenceFeatureVariant','-Pcpf.reference.featureIsolationVerification=true')+$variant.props
  & $gradle @args
  if($LASTEXITCODE -ne 0){throw "reference feature removal compile failed: $($variant.name) exit=$LASTEXITCODE"}
}
Write-Host '[CPF][REFERENCE][FEATURE-REMOVAL][PASS] variants=5'
