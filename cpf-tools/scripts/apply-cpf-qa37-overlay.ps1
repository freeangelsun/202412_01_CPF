[CmdletBinding()]
param(
  [Parameter(Mandatory=$true)][string]$OverlayRoot,
  [string]$Root=(Get-Location).Path,
  [string]$BackupRoot=(Join-Path $env:TEMP 'CPF_QA37_BACKUP_20260801_02'),
  [string]$ExpectedHead='1edd96c6dcc69b0b4d6e9e22a0709d910d7cfb04'
)
$ErrorActionPreference='Stop'
$Root=(Resolve-Path $Root).Path; $OverlayRoot=(Resolve-Path $OverlayRoot).Path
if (-not (Test-Path (Join-Path $Root '.git'))) { throw "Repository root is not a Git checkout: $Root" }
$head=(git -C $Root rev-parse HEAD).Trim(); if($LASTEXITCODE -ne 0 -or $head -ne $ExpectedHead){throw "HEAD mismatch. expected=$ExpectedHead actual=$head"}
$dirty=@(git -C $Root status --porcelain=v1 --untracked-files=all); if($LASTEXITCODE -ne 0){throw 'git status failed'}; if($dirty.Count -gt 0){$dirty|Write-Host;throw 'Working Tree must be clean before overlay application.'}
if(Test-Path $BackupRoot){throw "BackupRoot already exists: $BackupRoot"}
$protected=@('README.md','cpf-docs/guides/00_프레임워크안내.md','cpf-docs/guides/01_개발자매뉴얼.md','cpf-docs/guides/02_배치개발매뉴얼.md','cpf-docs/guides/03_ADM개발자매뉴얼.md','cpf-docs/guides/04_ADM운영자매뉴얼.md','cpf-docs/guides/05_플랫폼운영매뉴얼.md','cpf-docs/guides/90_BZA매뉴얼.md','cpf-docs/guides/91_Gateway매뉴얼.md')
$files=Get-ChildItem -LiteralPath $OverlayRoot -Recurse -File
$rows=@(); New-Item -ItemType Directory -Path (Join-Path $BackupRoot 'files') -Force|Out-Null
foreach($file in $files){
  $relative=$file.FullName.Substring($OverlayRoot.Length).TrimStart('\','/').Replace('\','/')
  if($protected -contains $relative){throw "Protected document is present in overlay: $relative"}
  if($relative -match '(^|/)(node_modules|dist|coverage|playwright-report|test-results|__pycache__)(/|$)' -or $relative -match '\.(log|tmp|pyc|zip)$'){throw "Generated/forbidden artifact in overlay: $relative"}
  $target=Join-Path $Root $relative
  if(Test-Path -LiteralPath $target){
    $backup=Join-Path (Join-Path $BackupRoot 'files') $relative;New-Item -ItemType Directory -Path (Split-Path $backup) -Force|Out-Null;Copy-Item -LiteralPath $target -Destination $backup -Force
    $rows += [pscustomobject]@{path=$relative; existed=$true}
  } else {$rows += [pscustomobject]@{path=$relative; existed=$false}}
}
@{baselineSha=$ExpectedHead;root=$Root;createdUtc=(Get-Date).ToUniversalTime().ToString('o');files=$rows}|ConvertTo-Json -Depth 5|Set-Content -LiteralPath (Join-Path $BackupRoot 'rollback-manifest.json') -Encoding utf8
try {
  foreach($file in $files){$relative=$file.FullName.Substring($OverlayRoot.Length).TrimStart('\','/');$target=Join-Path $Root $relative;New-Item -ItemType Directory -Path (Split-Path $target) -Force|Out-Null;Copy-Item -LiteralPath $file.FullName -Destination $target -Force}
  git -C $Root diff --check; if($LASTEXITCODE -ne 0){throw 'git diff --check failed'}
  python (Join-Path $Root 'cpf-tools/scripts/verify-cpf-qa37-source-closure.py') --root $Root
  if($LASTEXITCODE -ne 0){throw "QA37 merged-root source closure failed (exit=$LASTEXITCODE)"}
  Write-Host "[CPF][QA37][PASS] overlay applied; backup=$BackupRoot"
} catch {
  & (Join-Path $OverlayRoot 'cpf-tools/scripts/rollback-cpf-qa37-overlay.ps1') -BackupRoot $BackupRoot -Root $Root
  throw
}
