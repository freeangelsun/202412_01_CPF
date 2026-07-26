param([string]$Root=(Resolve-Path "$PSScriptRoot\..\..").Path,[switch]$Apply)
$ErrorActionPreference='Stop';Set-StrictMode -Version Latest
if(-not $Apply){throw 'Checksum 갱신은 maintenance 작업입니다. 명시적으로 -Apply를 지정하십시오.'}
function Version([string]$Name){if($Name -notmatch '^V(\d+)__.+\.sql$'){throw "invalid migration: $Name"};return [int]$Matches[1]}
function HashLine([IO.FileInfo]$File){$h=(Get-FileHash $File.FullName -Algorithm SHA256).Hash.ToLower();return "$h *$($File.Name)"}
function Update-CanonicalSource([string]$Dir){
  $manifest=Join-Path $Dir 'checksums.sha256';$byVersion=@{}
  if(Test-Path $manifest){foreach($line in Get-Content $manifest){if($line-match '^([0-9a-fA-F]{64}) \*(V(\d+)__.+\.sql)$'){$v=[int]$Matches[3];if($byVersion.ContainsKey($v)){throw "duplicate source checksum version V$v"};$byVersion[$v]=$line}}}
  foreach($f in Get-ChildItem $Dir -Filter 'V*.sql' -File){$byVersion[(Version $f.Name)]=HashLine $f}
  [IO.File]::WriteAllLines($manifest,@($byVersion.Keys|Sort-Object|ForEach-Object{$byVersion[$_]}),[Text.UTF8Encoding]::new($false));Write-Host "[UPDATED] $manifest"
}
function Rebuild-Runtime([string]$Dir){
  $seen=@{};$lines=@();foreach($f in Get-ChildItem $Dir -Filter 'V*.sql' -File|Sort-Object{Version $_.Name}){$v=Version $f.Name;if($seen.ContainsKey($v)){throw "duplicate runtime Flyway version V${v}: $($seen[$v]), $($f.Name)"};$seen[$v]=$f.Name;$lines+=HashLine $f}
  $manifest=Join-Path $Dir 'checksums.sha256';[IO.File]::WriteAllLines($manifest,$lines,[Text.UTF8Encoding]::new($false));Write-Host "[UPDATED] $manifest"
}
Update-CanonicalSource (Join-Path $Root 'cpf-tools/db/vendor/mariadb/source/migration/flyway')
Rebuild-Runtime (Join-Path $Root 'cpf-tools/db/vendor/mariadb/migration/flyway')
