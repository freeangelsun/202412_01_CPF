param([Parameter(Mandatory=$true)][string]$ReleaseDir,[Parameter(Mandatory=$true)][string]$PublicKey,[string]$VulnerabilityReport)
$ErrorActionPreference='Stop'
$branch=(git rev-parse --abbrev-ref HEAD).Trim();if($branch-ne'master'){throw "Commercial release requires master branch: $branch"}
if((git status --porcelain)){throw 'Commercial release requires a clean worktree'}
$manifest=Get-Content (Join-Path $ReleaseDir 'cpf-release-manifest.json') -Raw|ConvertFrom-Json
if($manifest.platformVersion-match'SNAPSHOT|rc\.'){throw "Commercial release requires final semantic version: $($manifest.platformVersion)"}
$sbom=Get-Content (Join-Path $ReleaseDir 'cpf-sbom.json') -Raw|ConvertFrom-Json
$unresolved=@($sbom.components|Where-Object{ -not $_.licenses -or @($_.licenses|Where-Object{$_.id -eq 'NOASSERTION' -or $_.id -eq 'UNRESOLVED'}).Count -gt 0 })
if($unresolved.Count-gt0){throw "Commercial release has unresolved licenses: $($unresolved.Count)"}
& (Join-Path $PSScriptRoot 'verify-release-artifacts.ps1') -ReleaseDir $ReleaseDir -PublicKey $PublicKey
if($LASTEXITCODE-ne0){throw 'Release signature verification failed'}
if(-not $VulnerabilityReport -or -not(Test-Path $VulnerabilityReport)){throw 'Commercial release requires current vulnerability report'}
$v=Get-Content $VulnerabilityReport -Raw|ConvertFrom-Json
$critical=[int]($v.critical ?? 0);$high=[int]($v.high ?? 0)
if($critical-gt0 -or $high-gt0){throw "High/Critical vulnerability gate failed: critical=$critical high=$high"}
Write-Host 'Commercial release gate: PASS'
