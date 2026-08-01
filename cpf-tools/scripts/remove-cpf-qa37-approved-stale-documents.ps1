[CmdletBinding(SupportsShouldProcess=$true, ConfirmImpact='High')]
param(
  [string]$Root=(Get-Location).Path,
  [string]$Manifest='cpf-docs/work/manifest/CPF_20260801_QA37_DELETE_MANIFEST.txt',
  [string]$BackupRoot=(Join-Path $env:TEMP ('CPF_QA37_STALE_DOC_BACKUP_'+(Get-Date -Format 'yyyyMMdd_HHmmss'))),
  [switch]$ConfirmRemoval
)
$ErrorActionPreference='Stop'
$Root=(Resolve-Path -LiteralPath $Root).Path
if(-not $ConfirmRemoval){throw 'Explicit -ConfirmRemoval is required.'}
$manifestPath=Join-Path $Root $Manifest
if(-not (Test-Path -LiteralPath $manifestPath -PathType Leaf)){throw "Delete Manifest missing: $manifestPath"}
$protected=@(
 'CPF_CUSTOMER_MANUAL_EDU_IMPLEMENTATION_REQUIREMENTS.md',
 'CPF_20260801_QA37_EDU_SOURCE_CLOSURE_AND_RECOVERY_REQUEST.md',
 'CPF_20260801_QA37_DEVELOPMENT_GPT_PROMPT.md',
 'CPF_FINAL_TARGET_REQUIREMENTS.md','README.md','cpf-docs/guides/'
)
$targets=@(Get-Content -LiteralPath $manifestPath -Encoding utf8 | ForEach-Object {$_.Trim()} | Where-Object {$_ -and -not $_.StartsWith('#')})
if($targets.Count -eq 0){throw 'Delete Manifest is empty.'}
if(($targets | Sort-Object -Unique).Count -ne $targets.Count){throw 'Delete Manifest contains duplicates.'}
foreach($relative in $targets){
  $normalized=$relative.Replace('\\','/')
  if(-not $normalized.StartsWith('cpf-docs/work/current/')){throw "Delete target outside approved current-work scope: $relative"}
  if($normalized.Contains('../') -or [IO.Path]::IsPathRooted($relative)){throw "Unsafe delete target: $relative"}
  foreach($token in $protected){if($normalized.Contains($token)){throw "Protected delete target: $relative"}}
}
New-Item -ItemType Directory -Path $BackupRoot -Force | Out-Null
$removed=@()
foreach($relative in $targets){
  $target=Join-Path $Root $relative
  if(-not (Test-Path -LiteralPath $target -PathType Leaf)){continue}
  $backup=Join-Path $BackupRoot $relative
  New-Item -ItemType Directory -Path (Split-Path -Parent $backup) -Force | Out-Null
  Copy-Item -LiteralPath $target -Destination $backup -Force
  if($PSCmdlet.ShouldProcess($target,'Remove approved stale tracked document')){
    Remove-Item -LiteralPath $target -Force
    $removed += $relative
  }
}
@{root=$Root;manifest=$Manifest;backupRoot=$BackupRoot;removed=$removed;removedCount=$removed.Count;createdUtc=(Get-Date).ToUniversalTime().ToString('o')} |
 ConvertTo-Json -Depth 5 | Set-Content -LiteralPath (Join-Path $BackupRoot 'delete-result.json') -Encoding utf8
Write-Host "[CPF][QA37][STALE-DOC][PASS] removed=$($removed.Count) backup=$BackupRoot"
