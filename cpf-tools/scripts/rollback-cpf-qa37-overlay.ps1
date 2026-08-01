[CmdletBinding(SupportsShouldProcess=$true,ConfirmImpact='High')]
param([Parameter(Mandatory=$true)][string]$BackupRoot,[string]$Root=(Get-Location).Path)
$ErrorActionPreference='Stop';$Root=(Resolve-Path $Root).Path;$manifestPath=Join-Path $BackupRoot 'rollback-manifest.json'
if(-not (Test-Path $manifestPath)){throw "Rollback manifest missing: $manifestPath"}
$m=Get-Content -Raw -LiteralPath $manifestPath|ConvertFrom-Json
foreach($row in @($m.files)){
 $target=Join-Path $Root ([string]$row.path)
 if([bool]$row.existed){$backup=Join-Path (Join-Path $BackupRoot 'files') ([string]$row.path);if(-not(Test-Path $backup)){throw "Backup file missing: $backup"};New-Item -ItemType Directory -Path (Split-Path $target) -Force|Out-Null;Copy-Item -LiteralPath $backup -Destination $target -Force}
 elseif(Test-Path $target){Remove-Item -LiteralPath $target -Force}
}
git -C $Root diff --check;if($LASTEXITCODE -ne 0){throw 'git diff --check failed after rollback'}
Write-Host '[CPF][QA37][PASS] overlay rollback completed. Backup directory was preserved.'
