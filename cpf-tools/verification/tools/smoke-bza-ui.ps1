param(
  [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
  [string] $ResultDir = "",
  [string] $FrontendRoot = "",
  [string] $BackofficeFrontendUrl = $(if ([string]::IsNullOrWhiteSpace($env:CPF_BACKOFFICE_FRONTEND_URL)) { 'http://127.0.0.1:5173/' } else { $env:CPF_BACKOFFICE_FRONTEND_URL }),
  [switch] $BrowserClick,
  [switch] $RequireBrowserClick
)
$ErrorActionPreference='Stop'; Set-StrictMode -Version Latest
$delegate=Join-Path $Root 'cpf-tools/verification/tools/smoke-backoffice-ui.ps1'
if(-not(Test-Path -LiteralPath $delegate -PathType Leaf)){throw "Canonical Backoffice smoke verifier missing: $delegate"}
$args=@('-NoProfile','-ExecutionPolicy','Bypass','-File',$delegate,'-Root',$Root,'-BackofficeFrontendUrl',$BackofficeFrontendUrl)
if(-not[string]::IsNullOrWhiteSpace($ResultDir)){$args+=@('-ResultDir',$ResultDir)}
if(-not[string]::IsNullOrWhiteSpace($FrontendRoot)){$args+=@('-FrontendRoot',$FrontendRoot)}
if($BrowserClick){$args+='-BrowserClick'};if($RequireBrowserClick){$args+='-RequireBrowserClick'}
& powershell @args
if($LASTEXITCODE -ne 0){throw "Canonical Backoffice smoke verifier failed exit=$LASTEXITCODE"}
Write-Host "Legacy BZA UI smoke entry delegated to current Backoffice/MBW verifier"
