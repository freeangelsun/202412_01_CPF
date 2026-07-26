[CmdletBinding()]
param(
 [Parameter(Mandatory)][string]$Database,[string]$Host='127.0.0.1',[int]$Port=3306,[string]$User='root',
 [string]$VerifySql,[switch]$RunPlatformVerify,[string]$Root='.',[string]$EvidenceDirectory='cpf-docs/work/evidence/dr'
)
$ErrorActionPreference='Stop'; if($VerifySql -and $RunPlatformVerify){throw '-VerifySql과 -RunPlatformVerify는 동시에 사용할 수 없습니다.'}
$rootPath=(Resolve-Path $Root).Path; $tool=Get-Command mariadb -ErrorAction SilentlyContinue; if(-not $tool){$tool=Get-Command mysql -ErrorAction SilentlyContinue}; if(-not $tool){throw 'mariadb/mysql client를 찾을 수 없습니다.'}
$started=(Get-Date).ToUniversalTime(); $mode='ISOLATED_BASELINE'; $query='SELECT COUNT(*) AS table_count FROM information_schema.tables WHERE table_schema=DATABASE();'
if($VerifySql){$mode='CUSTOM_SQL'; $query=Get-Content (Resolve-Path $VerifySql) -Raw}
elseif($RunPlatformVerify){$mode='FULL_PLATFORM'; $query=Get-Content (Join-Path $rootPath 'cpf-tools/db/vendor/mariadb/verify/00_verify.sql') -Raw}
$result=$query | & $tool.Source --batch --raw --host $Host --port $Port --user $User $Database 2>&1
$exit=$LASTEXITCODE; $finished=(Get-Date).ToUniversalTime(); $status=if($exit -eq 0){'PASS'}else{'FAIL'}
if($mode -eq 'ISOLATED_BASELINE' -and $exit -eq 0){ $nums=($result -join "`n") -split '\D+' | Where-Object {$_}; if(-not $nums -or [int64]$nums[-1] -le 0){$status='FAIL'} }
$out=Join-Path $rootPath $EvidenceDirectory; New-Item -ItemType Directory -Force -Path $out|Out-Null
$sha=(git -C $rootPath rev-parse HEAD 2>$null); if(-not $sha){$sha='UNKNOWN'}
$e=[ordered]@{schemaVersion=1;baseCommit=$sha;database=$Database;mode=$mode;startedAt=$started.ToString('o');finishedAt=$finished.ToString('o');durationMs=[math]::Round(($finished-$started).TotalMilliseconds);status=$status;exitCode=$exit;result=($result -join "`n")}
$path=Join-Path $out ("dr-restore-{0}.json" -f $started.ToString('yyyyMMddTHHmmssZ')); $e|ConvertTo-Json -Depth 6|Set-Content -Encoding UTF8 $path
if($status -ne 'PASS'){throw "DR restore verification failed. evidence=$path"}; Write-Host "DR_VERIFY_PASS mode=$mode evidence=$path"
