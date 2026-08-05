[CmdletBinding()]
param(
 [string]$Root=(Get-Location).Path,
 [Parameter(Mandatory)][string]$MariaDbProfile,[Parameter(Mandatory)][string]$PostgreSqlProfile,[Parameter(Mandatory)][string]$OracleProfile,
 [Parameter(Mandatory)][string]$MariaDbUpgradeProfile,[Parameter(Mandatory)][string]$PostgreSqlUpgradeProfile,[Parameter(Mandatory)][string]$OracleUpgradeProfile,
 [Parameter(Mandatory)][string[]]$BackupManifestPath,
 [string]$EvidenceRoot='',
 [ValidateRange(0,2147483647)][int]$UpgradeBaselineVersion=82,
 [Parameter(Mandatory)][string[]]$BackupRestoreEvidencePath,
 [Parameter(Mandatory)][string[]]$PitrEvidencePath,
 [switch]$AllowDestructiveRollback
)
Set-StrictMode -Version Latest;$ErrorActionPreference='Stop';$Utf8NoBom=[Text.UTF8Encoding]::new($false)
$rootPath=(Resolve-Path $Root).Path;$sha=(&git -C $rootPath rev-parse HEAD).Trim();if($sha-notmatch'^[0-9a-f]{40}$'){throw'exact SHA required'};if((@(&git -C $rootPath status --porcelain=v1 --untracked-files=all)).Count-gt0){throw'clean tree required'}
if(-not$AllowDestructiveRollback){throw'QA34 DB completion requires -AllowDestructiveRollback'};if($BackupManifestPath.Count -eq 0){throw'BackupManifestPath is required'}
$backups=@($BackupManifestPath|ForEach-Object{(Resolve-Path $_).Path})
if([string]::IsNullOrWhiteSpace($EvidenceRoot)){$EvidenceRoot=Join-Path ([IO.Path]::GetTempPath())("cpf-qa34-db-{0}"-f[guid]::NewGuid().ToString('N'))};$out=if([IO.Path]::IsPathRooted($EvidenceRoot)){[IO.Path]::GetFullPath($EvidenceRoot)}else{Join-Path $rootPath $EvidenceRoot};New-Item $out -ItemType Directory -Force|Out-Null
$profiles=[ordered]@{mariadb=(Resolve-Path $MariaDbProfile).Path;postgresql=(Resolve-Path $PostgreSqlProfile).Path;oracle=(Resolve-Path $OracleProfile).Path};$upgrades=[ordered]@{mariadb=(Resolve-Path $MariaDbUpgradeProfile).Path;postgresql=(Resolve-Path $PostgreSqlUpgradeProfile).Path;oracle=(Resolve-Path $OracleUpgradeProfile).Path}
$pythonCommand=Get-Command python -ErrorAction SilentlyContinue;if($null-eq$pythonCommand){$pythonCommand=Get-Command python3 -ErrorAction SilentlyContinue};if($null-eq$pythonCommand){throw'Python 3 executable is required'}
$lifecycleReport=Join-Path $out 'migration-lifecycle.sanitized.json'
& $pythonCommand.Source (Join-Path $rootPath 'cpf-tools/db/verify_migration_lifecycle.py') --root $rootPath --source-sha $sha --report $lifecycleReport
if($LASTEXITCODE -ne 0){throw"Migration lifecycle discovery failed: exit=$LASTEXITCODE report=$lifecycleReport"}
$lifecycle=Get-Content -LiteralPath $lifecycleReport -Raw -Encoding UTF8|ConvertFrom-Json -Depth 100
if([string]$lifecycle.result -ne 'PASS' -or -not [bool]$lifecycle.sanitized){throw'Migration lifecycle report is not a sanitized PASS'}
$versionsByVendor=[ordered]@{};$rollbackVersionsByVendor=[ordered]@{};$forwardRecoveryVersionsByVendor=[ordered]@{}
foreach($vendor in $profiles.Keys){
 $vendorMigrations=@($lifecycle.migrations|Where-Object{[string]$_.vendor -eq $vendor -and [int]$_.version -gt $UpgradeBaselineVersion})
 $versionsByVendor[$vendor]=@($vendorMigrations|ForEach-Object{[int]$_.version}|Sort-Object -Unique)
 $rollbackVersionsByVendor[$vendor]=@($vendorMigrations|Where-Object{[string]$_.strategy -eq 'ROLLBACK'}|ForEach-Object{[int]$_.version}|Sort-Object -Unique)
 $forwardRecoveryVersionsByVendor[$vendor]=@($vendorMigrations|Where-Object{[string]$_.strategy -eq 'FORWARD_RECOVERY'}|ForEach-Object{[int]$_.version}|Sort-Object -Unique)
 if(@($versionsByVendor[$vendor]).Count -eq 0){throw"No migration versions discovered above baseline: vendor=$vendor baseline=$UpgradeBaselineVersion"}
}
function Validate-RuntimeEvidence([string]$Kind,[string[]]$Paths,[string[]]$AllowedStatuses){
 $resolved=[Collections.Generic.List[object]]::new()
 foreach($requested in $Paths){
  $path=(Resolve-Path -LiteralPath $requested).Path;$hash=(Get-FileHash -LiteralPath $path -Algorithm SHA256).Hash.ToLowerInvariant();$e=Get-Content -LiteralPath $path -Raw -Encoding UTF8|ConvertFrom-Json -Depth 100
  if(-not [bool]$e.sanitized){throw"$Kind evidence is not sanitized: $path"}
  $rawEvidence=Get-Content -LiteralPath $path -Raw -Encoding UTF8
  if($rawEvidence -match '(?i)"(?:password|secret|token|authorization|cookie)"\s*:\s*"(?!\*{3,}|REDACTED|MASKED)[^"]+"'){throw"$Kind evidence contains a credential-bearing value: $path"}
  if([string]$e.sourceSha -ne $sha){throw"$Kind evidence SHA mismatch: path=$path evidence=$($e.sourceSha) source=$sha"}
  if([string]$e.vendor -notin @($profiles.Keys)){throw"$Kind evidence vendor is invalid: path=$path vendor=$($e.vendor)"}
  if([string]$e.status -notin $AllowedStatuses){throw"$Kind evidence status is not successful: path=$path status=$($e.status)"}
  if([string]::IsNullOrWhiteSpace([string]$e.approvalReference)){throw"$Kind evidence approvalReference is missing: $path"}
  if($Kind -eq 'PITR' -and ([string]$e.operation -ne 'PITR' -or [string]$e.mode -ne 'EXECUTE')){throw"PITR evidence must be an executed PITR result: $path"}
  $resolved.Add([ordered]@{vendor=[string]$e.vendor;path=$path;sha256=$hash;status=[string]$e.status})
 }
 foreach($vendor in $profiles.Keys){if(@($resolved|Where-Object { [string]$_.vendor -eq $vendor }).Count -ne 1){throw"$Kind evidence must contain exactly one result per vendor: vendor=$vendor"}}
 return @($resolved)
}
$backupRestoreEvidence=Validate-RuntimeEvidence 'BACKUP_RESTORE' $BackupRestoreEvidencePath @('PASS','COMPLETED')
$pitrEvidence=Validate-RuntimeEvidence 'PITR' $PitrEvidencePath @('COMPLETED')
$results=[Collections.Generic.List[object]]::new();$failures=[Collections.Generic.List[string]]::new();$started=[DateTimeOffset]::UtcNow
function Safe([string]$v){return($v-replace'(?i)(password|secret|token|authorization|cookie)\s*[:=]\s*\S+','$1=***')}
function Step([string]$vendor,[string]$name,[scriptblock]$action){$st=[DateTimeOffset]::UtcNow;$code=0;$message='PASS';try{&$action;if($LASTEXITCODE-and$LASTEXITCODE-ne0){throw"exit=$LASTEXITCODE"}}catch{$code=1;$message=Safe $_.Exception.Message;$script:failures.Add("${vendor}/${name}:$message")}finally{$script:results.Add([ordered]@{vendor=$vendor;name=$name;startedAt=$st.ToString('o');finishedAt=[DateTimeOffset]::UtcNow.ToString('o');exitCode=$code;result=$message})}}
function Migration([string]$vendor,[string]$profile,[string]$direction,[int]$version){
 $base="$vendor-$direction-v$version";$dry=Join-Path $out "$base-dry-run.sanitized.json";$apply=Join-Path $out "$base-apply.sanitized.json"
 Step $vendor "$direction-v$version-dry-run" {& (Join-Path $rootPath 'cpf-tools/scripts/invoke-platform-database-migration.ps1') -Root $rootPath -ProfilePath $profile -Direction $direction -MigrationVersion $version -ResultPath $dry}
 if(-not(Test-Path $dry)){return};$plan=(Get-Content $dry -Raw|ConvertFrom-Json -Depth 50).planSha256;if($plan-notmatch'^[0-9a-f]{64}$'){$failures.Add("${vendor}/${base}: dry-run plan SHA missing");return}
 Step $vendor "$direction-v$version-apply" {& (Join-Path $rootPath 'cpf-tools/scripts/invoke-platform-database-migration.ps1') -Root $rootPath -ProfilePath $profile -Direction $direction -MigrationVersion $version -ResultPath $apply -Apply -ConfirmApply -ConfirmApplicationsStopped -ConfirmRollbackReady -ExpectedPlanSha256 $plan -BackupManifestPath $backups}
}
Step 'all' 'static-token-parity' {& java (Join-Path $rootPath 'cpf-tools/scripts/Qa39Tool.java') 'db-static-token-parity' '--root' $rootPath '--json-report' (Join-Path $out 'static-token-parity.json')}
foreach($vendor in $profiles.Keys){$profile=$profiles[$vendor];$upgrade=$upgrades[$vendor];$versions=@($versionsByVendor[$vendor]);$rollbackVersions=@($rollbackVersionsByVendor[$vendor]);$forwardRecoveryVersions=@($forwardRecoveryVersionsByVendor[$vendor])
 Step $vendor 'clean-install' {& (Join-Path $rootPath 'cpf-tools/scripts/initialize-cpf-database.ps1') -Root $rootPath -ProfilePath $profile -All -SeedMode product -RequireRun -ResultDir (Join-Path $out "$vendor-install")}
 foreach($v in $versions){Migration $vendor $upgrade 'upgrade' $v}
 Step $vendor 'runtime-query-pack' {if($vendor-eq'mariadb'){& (Join-Path $rootPath 'cpf-tools/scripts/smoke-platform-runtime-query-packs-mariadb.ps1') -Root $rootPath -ProfilePath $profile -EvidencePath (Join-Path $out "$vendor-runtime-query.json")}else{& (Join-Path $rootPath 'cpf-tools/scripts/smoke-platform-runtime-query-packs-official-db.ps1') -Root $rootPath -ProfilePath $profile -Vendor $vendor -EvidencePath (Join-Path $out "$vendor-runtime-query.json")}}
 Step $vendor 'schema-drift' {& (Join-Path $rootPath 'cpf-tools/scripts/check-database-schema-drift.ps1') -Root $rootPath}
 Step $vendor 'live-schema-verify' {if($vendor-eq'mariadb'){& (Join-Path $rootPath 'cpf-tools/scripts/initialize-cpf-database.ps1') -Root $rootPath -ProfilePath $upgrade -All -SeedMode none -RequireRun -ResultDir (Join-Path $out "$vendor-live-verify")}else{& (Join-Path $rootPath 'cpf-tools/scripts/invoke-official-db-vendor-sql.ps1') -Vendor $vendor -Mode verify -ProfilePath $upgrade}}
 foreach($v in ($rollbackVersions|Sort-Object -Descending)){Migration $vendor $upgrade 'rollback' $v};foreach($v in $rollbackVersions){Migration $vendor $upgrade 'upgrade' $v}
 if($forwardRecoveryVersions.Count -gt 0){Step $vendor 'forward-recovery-evidence' {if(@($backupRestoreEvidence|Where-Object { [string]$_.vendor -eq $vendor }).Count -ne 1-or @($pitrEvidence|Where-Object { [string]$_.vendor -eq $vendor }).Count -ne 1){throw"Forward recovery evidence is incomplete: vendor=$vendor"}}}
}
$finalSha=(&git -C $rootPath rev-parse HEAD).Trim();if($finalSha-ne$sha){$failures.Add('Git SHA changed during DB matrix')};if((@(&git -C $rootPath status --porcelain=v1 --untracked-files=all)).Count-gt0){$failures.Add('Source tree changed during DB matrix')}
$e=[ordered]@{schemaVersion=4;evidenceId='QA34-DB-RUNTIME-MATRIX';sourceSha=$sha;resultSha=if($failures.Count -eq 0){$sha}else{$null};branch=(&git -C $rootPath branch --show-current).Trim();sourceDirty=$false;startedAt=$started.ToString('o');finishedAt=[DateTimeOffset]::UtcNow.ToString('o');exitCode=if($failures.Count -eq 0){0}else{1};requirements=@('QA34-REQ-004','QA34-REQ-010','QA34-REQ-018');vendors=@($profiles.Keys);upgradeBaselineVersion=$UpgradeBaselineVersion;migrationVersionsByVendor=$versionsByVendor;rollbackVersionsByVendor=$rollbackVersionsByVendor;forwardRecoveryVersionsByVendor=$forwardRecoveryVersionsByVendor;backupRestoreEvidence=$backupRestoreEvidence;pitrEvidence=$pitrEvidence;results=$results;failures=$failures;sanitized=$true;releaseEligible=($failures.Count -eq 0)}
$ep=Join-Path $out 'CPF_QA34_DB_RUNTIME_MATRIX.sanitized.json';[IO.File]::WriteAllText($ep,($e|ConvertTo-Json -Depth 20)+"`n",$Utf8NoBom)
if($failures.Count-gt0){throw"QA34 DB runtime matrix failed: $($failures-join'; ')"};Write-Host"[CPF][QA34][PASS] DB matrix=$ep"
