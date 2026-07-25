param([string]$Root=(Resolve-Path "$PSScriptRoot\..\..").Path)
$ErrorActionPreference='Stop'; Set-StrictMode -Version Latest
function Check-Pack([string]$Dir){
  $manifest=Join-Path $Dir 'checksums.sha256'; if(-not(Test-Path $manifest)){throw "checksum manifest missing: $manifest"}
  $seen=@{}; $entries=@{}
  foreach($line in Get-Content -LiteralPath $manifest){if($line -match '^([0-9a-fA-F]{64}) \*(V(\d+)__.+\.sql)$'){$hash=$Matches[1].ToLower();$file=$Matches[2];$ver=[int]$Matches[3];if($seen.ContainsKey($ver)){throw "duplicate Flyway version V$ver in $manifest"};$seen[$ver]=$file;$entries[$file]=$hash}}
  foreach($file in Get-ChildItem -LiteralPath $Dir -Filter 'V*.sql' -File){
    if($file.Name -notmatch '^V(\d+)__'){throw "invalid migration name: $($file.Name)"};$ver=[int]$Matches[1]
    if(-not $entries.ContainsKey($file.Name)){throw "migration missing from checksum manifest: $($file.FullName)"}
    $actual=(Get-FileHash -LiteralPath $file.FullName -Algorithm SHA256).Hash.ToLower();if($actual-ne$entries[$file.Name]){throw "migration checksum mismatch: $($file.FullName)"}
  }
  Write-Host "[PASS] migration checksum pack: $Dir"
}
$source=Join-Path $Root 'cpf-tools/db/vendor/mariadb/source/migration/flyway';$runtime=Join-Path $Root 'cpf-tools/db/vendor/mariadb/migration/flyway'
Check-Pack $source;Check-Pack $runtime
# 현재 canonical source 파일은 runtime lifecycle과 byte-identical 해야 합니다.
foreach($f in Get-ChildItem -LiteralPath $source -Filter 'V*.sql' -File){$r=Join-Path $runtime $f.Name;if(-not(Test-Path $r)){throw "runtime lifecycle missing canonical migration: $($f.Name)"};if((Get-FileHash $f.FullName -Algorithm SHA256).Hash-ne(Get-FileHash $r -Algorithm SHA256).Hash){throw "source/runtime migration drift: $($f.Name)"}}
Write-Host '[PASS] MariaDB source/runtime migration parity'

# Rollback도 canonical source에 존재하는 파일은 runtime vendor pack에 같은 내용으로 존재해야 합니다.
$sourceRollback=Join-Path $Root 'cpf-tools/db/vendor/mariadb/source/migration/rollback'
$runtimeRollback=Join-Path $Root 'cpf-tools/db/vendor/mariadb/rollback'
if(Test-Path $sourceRollback){
  if(-not(Test-Path $runtimeRollback)){throw "runtime rollback directory missing: $runtimeRollback"}
  foreach($f in Get-ChildItem -LiteralPath $sourceRollback -Filter '*.sql' -File){
    $r=Join-Path $runtimeRollback $f.Name
    if(-not(Test-Path $r)){throw "runtime rollback missing canonical artifact: $($f.Name)"}
    if((Get-FileHash $f.FullName -Algorithm SHA256).Hash-ne(Get-FileHash $r -Algorithm SHA256).Hash){throw "source/runtime rollback drift: $($f.Name)"}
  }
  Write-Host '[PASS] MariaDB source/runtime rollback parity'
}
