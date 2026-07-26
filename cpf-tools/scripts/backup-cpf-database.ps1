[CmdletBinding()]
param(
    [ValidateSet('mariadb','mysql','postgresql','oracle','sqlserver')][string]$Vendor='mariadb',
    [Parameter(Mandatory)][string]$Database,
    [string]$Host='127.0.0.1', [int]$Port=3306, [string]$User='root',
    [string]$OutputDirectory='cpf-docs/work/evidence/backup', [string]$Root='.'
)
$ErrorActionPreference='Stop'
$rootPath=(Resolve-Path $Root).Path
$plan=Get-Content (Join-Path $rootPath 'cpf-tools/config/database-source-plan.json') -Raw | ConvertFrom-Json
$status=$plan.vendorSourceStatus.$Vendor.status
if($status -ne 'implemented'){ throw "DB vendor '$Vendor' is $status. 다른 Vendor SQL을 복사하여 우회하지 않습니다." }
if($Vendor -ne 'mariadb'){ throw "현재 backup implementation은 MariaDB만 제공합니다." }
$tool=Get-Command mariadb-dump -ErrorAction SilentlyContinue
if(-not $tool){ $tool=Get-Command mysqldump -ErrorAction SilentlyContinue }
if(-not $tool){ throw 'mariadb-dump/mysqldump를 찾을 수 없습니다.' }
$out=Join-Path $rootPath $OutputDirectory; New-Item -ItemType Directory -Force -Path $out|Out-Null
$started=(Get-Date).ToUniversalTime(); $stamp=$started.ToString('yyyyMMddTHHmmssZ')
$backup=Join-Path $out "$Database-$stamp.sql"
$args=@('--single-transaction','--routines','--events','--triggers','--hex-blob','--host', $Host,'--port',"$Port",'--user',$User,$Database)
# Password argument는 의도적으로 지원하지 않는다. client credential mechanism을 사용한다.
& $tool.Source @args | Set-Content -Encoding UTF8 $backup
if($LASTEXITCODE -ne 0){ throw "backup command failed: exit=$LASTEXITCODE" }
$hash=(Get-FileHash $backup -Algorithm SHA256).Hash.ToLowerInvariant(); $finished=(Get-Date).ToUniversalTime()
$baseCommit=(git -C $rootPath rev-parse HEAD 2>$null); if(-not $baseCommit){$baseCommit='UNKNOWN'}
$manifest=[ordered]@{schemaVersion=1;vendor=$Vendor;database=$Database;host=$Host;port=$Port;user=$User;backupFile=(Split-Path $backup -Leaf);sha256=$hash;baseCommit=$baseCommit;startedAt=$started.ToString('o');finishedAt=$finished.ToString('o');containsSensitiveData=$true;credentialEmbedded=$false;handling='RESTRICTED - DB backup may contain production-sensitive data'}
$manifestPath="$backup.manifest.json"; $manifest|ConvertTo-Json -Depth 5|Set-Content -Encoding UTF8 $manifestPath
Write-Host "BACKUP_OK file=$backup sha256=$hash manifest=$manifestPath"
