[CmdletBinding()]
param(
 [string]$Root=(Get-Location).Path,
 [Parameter(Mandatory)][string]$MariaDbProfile,[Parameter(Mandatory)][string]$PostgreSqlProfile,[Parameter(Mandatory)][string]$OracleProfile,
 [Parameter(Mandatory)][string]$MariaDbUpgradeProfile,[Parameter(Mandatory)][string]$PostgreSqlUpgradeProfile,[Parameter(Mandatory)][string]$OracleUpgradeProfile,
 [Parameter(Mandatory)][string[]]$BackupManifestPath,
 [string]$EvidenceRoot='',[switch]$AllowDestructiveRollback
)
Set-StrictMode -Version Latest;$ErrorActionPreference='Stop';$Utf8NoBom=[Text.UTF8Encoding]::new($false)
$rootPath=(Resolve-Path $Root).Path;$sha=(&git -C $rootPath rev-parse HEAD).Trim();if($sha-notmatch'^[0-9a-f]{40}$'){throw'exact SHA required'};if((@(&git -C $rootPath status --porcelain=v1 --untracked-files=all)).Count-gt0){throw'clean tree required'}
if(-not$AllowDestructiveRollback){throw'QA34 DB completion requires -AllowDestructiveRollback'};if($BackupManifestPath.Count-eq0){throw'BackupManifestPath is required'}
$backups=@($BackupManifestPath|ForEach-Object{(Resolve-Path $_).Path})
if([string]::IsNullOrWhiteSpace($EvidenceRoot)){$EvidenceRoot=Join-Path ([IO.Path]::GetTempPath())("cpf-qa34-db-{0}"-f[guid]::NewGuid().ToString('N'))};$out=if([IO.Path]::IsPathRooted($EvidenceRoot)){[IO.Path]::GetFullPath($EvidenceRoot)}else{Join-Path $rootPath $EvidenceRoot};New-Item $out -ItemType Directory -Force|Out-Null
$profiles=[ordered]@{mariadb=(Resolve-Path $MariaDbProfile).Path;postgresql=(Resolve-Path $PostgreSqlProfile).Path;oracle=(Resolve-Path $OracleProfile).Path};$upgrades=[ordered]@{mariadb=(Resolve-Path $MariaDbUpgradeProfile).Path;postgresql=(Resolve-Path $PostgreSqlUpgradeProfile).Path;oracle=(Resolve-Path $OracleUpgradeProfile).Path};$versions=@(83,86,87,88,89,90,91)
$results=[Collections.Generic.List[object]]::new();$failures=[Collections.Generic.List[string]]::new();$started=[DateTimeOffset]::UtcNow
function Safe([string]$v){return($v-replace'(?i)(password|secret|token|authorization|cookie)\s*[:=]\s*\S+','$1=***')}
function Step([string]$vendor,[string]$name,[scriptblock]$action){$st=[DateTimeOffset]::UtcNow;$code=0;$message='PASS';try{&$action;if($LASTEXITCODE-and$LASTEXITCODE-ne0){throw"exit=$LASTEXITCODE"}}catch{$code=1;$message=Safe $_.Exception.Message;$script:failures.Add("${vendor}/${name}:$message")}finally{$script:results.Add([ordered]@{vendor=$vendor;name=$name;startedAt=$st.ToString('o');finishedAt=[DateTimeOffset]::UtcNow.ToString('o');exitCode=$code;result=$message})}}
function Migration([string]$vendor,[string]$profile,[string]$direction,[int]$version){
 $base="$vendor-$direction-v$version";$dry=Join-Path $out "$base-dry-run.sanitized.json";$apply=Join-Path $out "$base-apply.sanitized.json"
 Step $vendor "$direction-v$version-dry-run" {& (Join-Path $rootPath 'cpf-tools/scripts/invoke-platform-database-migration.ps1') -Root $rootPath -ProfilePath $profile -Direction $direction -MigrationVersion $version -ResultPath $dry}
 if(-not(Test-Path $dry)){return};$plan=(Get-Content $dry -Raw|ConvertFrom-Json -Depth 50).planSha256;if($plan-notmatch'^[0-9a-f]{64}$'){$failures.Add("${vendor}/${base}: dry-run plan SHA missing");return}
 Step $vendor "$direction-v$version-apply" {& (Join-Path $rootPath 'cpf-tools/scripts/invoke-platform-database-migration.ps1') -Root $rootPath -ProfilePath $profile -Direction $direction -MigrationVersion $version -ResultPath $apply -Apply -ConfirmApply -ConfirmApplicationsStopped -ConfirmRollbackReady -ExpectedPlanSha256 $plan -BackupManifestPath $backups}
}
Step 'all' 'static-token-parity' {&python (Join-Path $rootPath 'cpf-tools/scripts/verify-cpf-db-vendor-static-token-parity.py') --root $rootPath --json-report (Join-Path $out 'static-token-parity.json')}
foreach($vendor in $profiles.Keys){$profile=$profiles[$vendor];$upgrade=$upgrades[$vendor]
 Step $vendor 'clean-install' {& (Join-Path $rootPath 'cpf-tools/scripts/initialize-cpf-database.ps1') -Root $rootPath -ProfilePath $profile -All -SeedMode product -RequireRun -ResultDir (Join-Path $out "$vendor-install")}
 foreach($v in $versions){Migration $vendor $upgrade 'upgrade' $v}
 Step $vendor 'runtime-query-pack' {if($vendor-eq'mariadb'){& (Join-Path $rootPath 'cpf-tools/scripts/smoke-platform-runtime-query-packs-mariadb.ps1') -Root $rootPath -ProfilePath $profile -EvidencePath (Join-Path $out "$vendor-runtime-query.json")}else{& (Join-Path $rootPath 'cpf-tools/scripts/smoke-platform-runtime-query-packs-official-db.ps1') -Root $rootPath -ProfilePath $profile -Vendor $vendor -EvidencePath (Join-Path $out "$vendor-runtime-query.json")}}
 Step $vendor 'schema-drift' {& (Join-Path $rootPath 'cpf-tools/scripts/check-database-schema-drift.ps1') -Root $rootPath}
 Step $vendor 'live-schema-verify' {if($vendor-eq'mariadb'){& (Join-Path $rootPath 'cpf-tools/scripts/initialize-cpf-database.ps1') -Root $rootPath -ProfilePath $upgrade -All -SeedMode none -RequireRun -ResultDir (Join-Path $out "$vendor-live-verify")}else{& (Join-Path $rootPath 'cpf-tools/scripts/invoke-official-db-vendor-sql.ps1') -Vendor $vendor -Mode verify -ProfilePath $upgrade}}
 foreach($v in ($versions|Sort-Object -Descending)){Migration $vendor $upgrade 'rollback' $v};foreach($v in $versions){Migration $vendor $upgrade 'upgrade' $v}
}
$finalSha=(&git -C $rootPath rev-parse HEAD).Trim();if($finalSha-ne$sha){$failures.Add('Git SHA changed during DB matrix')};if((@(&git -C $rootPath status --porcelain=v1 --untracked-files=all)).Count-gt0){$failures.Add('Source tree changed during DB matrix')}
$e=[ordered]@{schemaVersion=3;evidenceId='QA34-DB-RUNTIME-MATRIX';sourceSha=$sha;resultSha=if($failures.Count-eq0){$sha}else{$null};branch=(&git -C $rootPath branch --show-current).Trim();sourceDirty=$false;startedAt=$started.ToString('o');finishedAt=[DateTimeOffset]::UtcNow.ToString('o');exitCode=if($failures.Count-eq0){0}else{1};requirements=@('QA34-REQ-004','QA34-REQ-010','QA34-REQ-018');vendors=@($profiles.Keys);migrationVersions=$versions;results=$results;failures=$failures;sanitized=$true;releaseEligible=($failures.Count-eq0)}
$ep=Join-Path $out 'CPF_QA34_DB_RUNTIME_MATRIX.sanitized.json';[IO.File]::WriteAllText($ep,($e|ConvertTo-Json -Depth 20)+"`n",$Utf8NoBom)
if($failures.Count-gt0){throw"QA34 DB runtime matrix failed: $($failures-join'; ')"};Write-Host"[CPF][QA34][PASS] DB matrix=$ep"
