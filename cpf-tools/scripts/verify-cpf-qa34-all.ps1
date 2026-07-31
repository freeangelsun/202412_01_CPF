[CmdletBinding()]
param(
 [string]$Root=(Get-Location).Path,[Parameter(Mandatory)][string]$EvidenceDir,
 [Parameter(Mandatory)][string]$MariaDbProfile,[Parameter(Mandatory)][string]$PostgreSqlProfile,[Parameter(Mandatory)][string]$OracleProfile,
 [Parameter(Mandatory)][string]$MariaDbUpgradeProfile,[Parameter(Mandatory)][string]$PostgreSqlUpgradeProfile,[Parameter(Mandatory)][string]$OracleUpgradeProfile,
 [Parameter(Mandatory)][string]$AdmBaseUrl,[Parameter(Mandatory)][string]$BzaBaseUrl,[Parameter(Mandatory)][string]$ApprovedRegistry,
 [Parameter(Mandatory)][string]$AdmAuthState,[Parameter(Mandatory)][string]$BzaAuthState,
 [Parameter(Mandatory)][string]$AdmPrivilegedEndpoints,[Parameter(Mandatory)][string]$BzaPrivilegedEndpoints,
 [Parameter(Mandatory)][string]$AdmRouteMatrix,[Parameter(Mandatory)][string]$BzaRouteMatrix,
 [Parameter(Mandatory)][string]$AdmFailureMatrix,[Parameter(Mandatory)][string]$BzaFailureMatrix,
 [Parameter(Mandatory)][string]$AdmSecurityFixture,[Parameter(Mandatory)][string]$BzaSecurityFixture,
 [Parameter(Mandatory)][string[]]$BackupManifestPath
)
Set-StrictMode -Version Latest;$ErrorActionPreference='Stop'
$rootPath=(Resolve-Path $Root).Path;$evidencePath=[IO.Path]::GetFullPath($EvidenceDir)
$sha=(& git -C $rootPath rev-parse HEAD).Trim();if($sha-notmatch'^[0-9a-f]{40}$'){throw'exact SHA required'}
if((& git -C $rootPath status --porcelain=v1 --untracked-files=all|Out-String).Trim()){throw'QA34 all requires clean tree'}
if(Test-Path $evidencePath){$existing=Get-ChildItem -Force $evidencePath -ErrorAction SilentlyContinue;if($existing){throw'EvidenceDir must be empty'}}else{New-Item -ItemType Directory -Force $evidencePath|Out-Null}
$started=[DateTimeOffset]::UtcNow
& python (Join-Path $rootPath 'cpf-tools/scripts/verify-cpf-qa34-source-closure.py') --root $rootPath --output (Join-Path $evidencePath 'CPF_QA34_SOURCE_CLOSURE.sanitized.json')
if($LASTEXITCODE-ne0){throw'QA34 source closure failed'}
& pwsh -NoProfile -File (Join-Path $rootPath 'cpf-tools/scripts/verify-cpf-qa34-runtime-matrix.ps1') -Root $rootPath -MariaDbProfile $MariaDbProfile -PostgreSqlProfile $PostgreSqlProfile -OracleProfile $OracleProfile -MariaDbUpgradeProfile $MariaDbUpgradeProfile -PostgreSqlUpgradeProfile $PostgreSqlUpgradeProfile -OracleUpgradeProfile $OracleUpgradeProfile -AdmBaseUrl $AdmBaseUrl -BzaBaseUrl $BzaBaseUrl -ApprovedRegistry $ApprovedRegistry -AdmAuthState $AdmAuthState -BzaAuthState $BzaAuthState -AdmPrivilegedEndpoints $AdmPrivilegedEndpoints -BzaPrivilegedEndpoints $BzaPrivilegedEndpoints -AdmRouteMatrix $AdmRouteMatrix -BzaRouteMatrix $BzaRouteMatrix -AdmFailureMatrix $AdmFailureMatrix -BzaFailureMatrix $BzaFailureMatrix -AdmSecurityFixture $AdmSecurityFixture -BzaSecurityFixture $BzaSecurityFixture -BackupManifestPath $BackupManifestPath -EvidenceOutput (Join-Path $evidencePath 'runtime/CPF_QA34_RUNTIME_MATRIX.sanitized.json') -EvidenceStage (Join-Path $evidencePath 'runtime') -KeepEvidenceStage
if($LASTEXITCODE-ne0){throw'QA34 runtime matrix failed'}
& python (Join-Path $rootPath 'cpf-tools/scripts/reclassify-cpf-qa33-exact-sha.py') --root $rootPath --evidence-root $evidencePath --expected-sha $sha --output (Join-Path $evidencePath 'CPF_20260731_QA33_RESULT_MATRIX_EXACT_SHA.csv') --summary (Join-Path $evidencePath 'CPF_20260731_QA33_RESULT_MATRIX_EXACT_SHA.summary.json') --require-all
if($LASTEXITCODE-ne0){throw'QA33 552-row exact-SHA reclassification failed'}
if((& git -C $rootPath rev-parse HEAD).Trim()-ne$sha){throw'SHA changed during QA34 all'}
if((& git -C $rootPath status --porcelain=v1 --untracked-files=all|Out-String).Trim()){throw'Source changed during QA34 all'}
$files=@(Get-ChildItem $evidencePath -Recurse -File|Sort-Object FullName|ForEach-Object{[ordered]@{path=[IO.Path]::GetRelativePath($evidencePath,$_.FullName).Replace('\','/');sha256=(Get-FileHash $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant();bytes=$_.Length}})
$index=[ordered]@{schemaVersion=3;evidenceId='QA34-CANDIDATE-INDEX';sourceSha=$sha;resultSha=$sha;sourceDirty=$false;startedAt=$started.ToString('o');finishedAt=[DateTimeOffset]::UtcNow.ToString('o');exitCode=0;requirements=@(1..19|ForEach-Object{"QA34-REQ-{0:D3}"-f$_});files=$files;sanitized=$true;releaseEligible=$false;nextAction='Independent fresh-clone review and verify-cpf-qa34-finalize.ps1 are required'}
$indexPath=Join-Path $evidencePath 'CPF_QA34_CANDIDATE_INDEX.sanitized.json';[IO.File]::WriteAllText($indexPath,($index|ConvertTo-Json -Depth 12)+"`n",[Text.UTF8Encoding]::new($false));$hash=(Get-FileHash $indexPath -Algorithm SHA256).Hash.ToLowerInvariant();[IO.File]::WriteAllText("$indexPath.sha256","$hash  CPF_QA34_CANDIDATE_INDEX.sanitized.json`n",[Text.UTF8Encoding]::new($false));Write-Host"[CPF][QA34][PASS] candidate requirements 1-19 complete; independent review remains: $evidencePath"
