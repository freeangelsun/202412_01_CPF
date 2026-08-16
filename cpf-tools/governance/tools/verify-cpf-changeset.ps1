[CmdletBinding()] param([Parameter(Mandatory)][string]$Manifest,[Parameter(Mandatory)][string]$ExpectedSourceEnvironment,[Parameter(Mandatory)][string]$ExpectedTargetEnvironment,[switch]$AllowDifferentBaseCommit,[string]$Root='.')
$ErrorActionPreference='Stop';$root=(Resolve-Path $Root).Path;$m=Get-Content (Resolve-Path $Manifest) -Raw|ConvertFrom-Json
if($m.sourceEnvironment -ne $ExpectedSourceEnvironment){throw 'sourceEnvironment mismatch'};if($m.targetEnvironment -ne $ExpectedTargetEnvironment){throw 'targetEnvironment mismatch'}
$current=(git -C $root rev-parse HEAD 2>$null);if(-not $AllowDifferentBaseCommit -and $current -and $m.baseCommit -ne $current){throw "baseCommit mismatch: $($m.baseCommit) != $current"}
foreach($f in $m.files){$p=Join-Path $root $f.path;if(-not(Test-Path $p)){throw "missing changeset file: $($f.path)"};$h=(Get-FileHash $p -Algorithm SHA256).Hash.ToLowerInvariant();if($h -ne ([string]$f.sha256).ToLowerInvariant()){throw "hash mismatch: $($f.path)"}}
Write-Host "CHANGESET_VERIFY_PASS id=$($m.changeSetId)"
