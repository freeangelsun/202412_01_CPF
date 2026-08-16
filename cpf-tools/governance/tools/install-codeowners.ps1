param(
 [string]$RepoRoot=(Resolve-Path "$PSScriptRoot\..\..\..").Path,
 [Parameter(Mandatory=$true)][string]$PlatformCoreOwner,
 [Parameter(Mandatory=$true)][string]$BatchOwner,
 [Parameter(Mandatory=$true)][string]$SecurityOwner,
 [Parameter(Mandatory=$true)][string]$DatabaseOwner,
 [Parameter(Mandatory=$true)][string]$ReleaseOwner,
 [Parameter(Mandatory=$true)][string]$DeploymentOwner,
 [Parameter(Mandatory=$true)][string]$DomainOwner
)
$ErrorActionPreference='Stop'
$owners=@($PlatformCoreOwner,$BatchOwner,$SecurityOwner,$DatabaseOwner,$ReleaseOwner,$DeploymentOwner,$DomainOwner)
foreach($owner in $owners){if($owner-notmatch'^@[A-Za-z0-9_.-]+(?:/[A-Za-z0-9_.-]+)?$'){throw "Invalid GitHub user/team owner: $owner"}}
$dir=Join-Path $RepoRoot '.github';New-Item -ItemType Directory -Force -Path $dir|Out-Null
$content=@"
/cpf-core/            $PlatformCoreOwner $SecurityOwner
/cpf-common/          $PlatformCoreOwner
/cpf-batch/           $BatchOwner $SecurityOwner
/cpf-admin/           $PlatformCoreOwner
/cpf-biz-admin/       $DomainOwner
/cpf-tools/db/        $DatabaseOwner $PlatformCoreOwner
/cpf-tools/release/   $ReleaseOwner $SecurityOwner
/cpf-tools/generator/ $PlatformCoreOwner $DomainOwner
/deploy/              $DeploymentOwner $SecurityOwner
"@
Set-Content -Encoding UTF8 (Join-Path $dir 'CODEOWNERS') $content
Write-Host 'CODEOWNERS generated locally. Review and commit only with explicit repository-owner approval.'
