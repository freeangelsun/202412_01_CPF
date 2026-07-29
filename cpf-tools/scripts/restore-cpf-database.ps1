[CmdletBinding()]
param(
    [string]$Vendor='mariadb',
    [Parameter(Mandatory)][string]$Database, [Parameter(Mandatory)][string]$BackupFile,
    [string]$Host='127.0.0.1',[int]$Port=3306,[string]$User='root',
    [switch]$ConfirmRestore,[switch]$AllowMissingManifest,[string]$Root='.'
)
$ErrorActionPreference='Stop'; if(-not $ConfirmRestore){throw 'Restore는 -ConfirmRestore 명시가 필요합니다.'}
$rootPath=(Resolve-Path $Root).Path
. (Join-Path $rootPath 'cpf-tools/scripts/database-profile-common.ps1')
$Vendor=Assert-CpfSupportedDatabaseVendor $Vendor
$plan=Get-Content (Join-Path $rootPath 'cpf-tools/config/database-source-plan.json') -Raw|ConvertFrom-Json
if($plan.vendorSourceStatus.$Vendor.status -ne 'implemented'){throw "DB vendor '$Vendor'는 구현되지 않았습니다."}
if($Vendor -ne 'mariadb'){throw '현재 restore implementation은 MariaDB만 제공합니다.'}
$backup=(Resolve-Path $BackupFile).Path; $manifestPath="$backup.manifest.json"
if(Test-Path $manifestPath){
  $m=Get-Content $manifestPath -Raw|ConvertFrom-Json
  if($m.vendor -ne $Vendor){throw "manifest vendor mismatch: $($m.vendor) != $Vendor"}
  if($m.database -ne $Database){throw "manifest database mismatch: $($m.database) != $Database"}
  $actual=(Get-FileHash $backup -Algorithm SHA256).Hash.ToLowerInvariant(); if($actual -ne ([string]$m.sha256).ToLowerInvariant()){throw 'backup SHA-256 mismatch'}
} elseif(-not $AllowMissingManifest){ throw 'backup manifest가 없습니다. Legacy 복구만 -AllowMissingManifest로 명시하십시오.' }
$tool=Get-Command mariadb -ErrorAction SilentlyContinue
# MariaDB 배포판에 따라 호환 mysql client 이름만 제공될 수 있습니다.
if(-not $tool){$tool=Get-Command mysql -ErrorAction SilentlyContinue}
if(-not $tool){throw 'mariadb client(또는 호환 mysql client)를 찾을 수 없습니다.'}
Get-Content -Raw $backup | & $tool.Source --host $Host --port $Port --user $User $Database
if($LASTEXITCODE -ne 0){throw "restore command failed: exit=$LASTEXITCODE"}
Write-Host "RESTORE_OK database=$Database backup=$backup"
