[CmdletBinding()]
param(
 [string]$Root=(Get-Location).Path,
 [Parameter(Mandatory)][string]$MariaDbProfile,
 [Parameter(Mandatory)][string]$PostgreSqlProfile,
 [Parameter(Mandatory)][string]$OracleProfile,
 [Parameter(Mandatory)][string]$MariaDbUpgradeProfile,
 [Parameter(Mandatory)][string]$PostgreSqlUpgradeProfile,
 [Parameter(Mandatory)][string]$OracleUpgradeProfile,
 [Parameter(Mandatory)][string[]]$BackupManifestPath,
 [Parameter(Mandatory)][string]$ApprovedRegistry,
 [Parameter(Mandatory)][string]$AdmBaseUrl,
 [Parameter(Mandatory)][string]$BzaBaseUrl,
 [Parameter(Mandatory)][string]$AdmAuthState,
 [Parameter(Mandatory)][string]$BzaAuthState,
 [Parameter(Mandatory)][string]$AdmPrivilegedEndpoints,
 [Parameter(Mandatory)][string]$BzaPrivilegedEndpoints,
 [Parameter(Mandatory)][string]$AdmRouteMatrix,
 [Parameter(Mandatory)][string]$BzaRouteMatrix,
 [Parameter(Mandatory)][string]$AdmFailureMatrix,
 [Parameter(Mandatory)][string]$BzaFailureMatrix,
 [Parameter(Mandatory)][string]$AdmSecurityFixture,
 [Parameter(Mandatory)][string]$BzaSecurityFixture,
 [string[]]$ArtifactPaths=@(),
 [string]$EvidenceOutput='',
 [switch]$AllowDestructiveDbLifecycle
)
Set-StrictMode -Version Latest
$ErrorActionPreference='Stop'
$Utf8NoBom=[Text.UTF8Encoding]::new($false)
$rootPath=(Resolve-Path $Root).Path
$sha=(&git -C $rootPath rev-parse HEAD).Trim()
if($sha-notmatch'^[0-9a-f]{40}$'){throw 'exact SHA required'}
if((@(&git -C $rootPath status --porcelain=v1 --untracked-files=all)).Count-gt0){throw 'clean tree required before final validation'}
if(-not$AllowDestructiveDbLifecycle){throw 'QA39 requires -AllowDestructiveDbLifecycle after backup manifests are reviewed'}
if([string]::IsNullOrWhiteSpace($EvidenceOutput)){$EvidenceOutput=Join-Path $rootPath 'cpf-docs/evidence/qa39/runtime/CPF_QA39_FINAL_VALIDATION.sanitized.json'}
$stage=Join-Path ([IO.Path]::GetTempPath())("cpf-qa39-final-{0}"-f[guid]::NewGuid().ToString('N'))
New-Item $stage -ItemType Directory -Force|Out-Null
$results=[Collections.Generic.List[object]]::new();$failures=[Collections.Generic.List[string]]::new();$started=[DateTimeOffset]::UtcNow
function Safe([string]$v){return($v-replace'(?i)(password|secret|token|authorization|cookie)\s*[:=]\s*\S+','$1=***')}
function Step([string]$name,[scriptblock]$action){$st=[DateTimeOffset]::UtcNow;$code=0;$message='PASS';try{&$action;if($LASTEXITCODE-and$LASTEXITCODE-ne0){throw "exit=$LASTEXITCODE"}}catch{$code=1;$message=Safe $_.Exception.Message;$script:failures.Add("${name}:$message")}finally{$script:results.Add([ordered]@{name=$name;started_at=$st.ToString('o');finished_at=[DateTimeOffset]::UtcNow.ToString('o');exit_code=$code;result=$message})}}
Step 'low-cost-gates' {& (Join-Path $rootPath 'cpf-tools/verification/qa39/invoke-qa39-low-cost-gates.ps1') -Root $rootPath}
Step 'java25-fresh-cache-build-test-publication' {& (Join-Path $rootPath 'cpf-tools/scripts/verify-cpf-qa34-java-build.ps1') -Root $rootPath -EvidenceOutput (Join-Path $stage 'java25.sanitized.json')}
Step 'generator-all-combinations' {& (Join-Path $rootPath 'cpf-tools/scripts/check-generator-golden-path.ps1') -Root $rootPath; & (Join-Path $rootPath 'cpf-tools/scripts/check-generator-arbitrary-domain-parity.ps1') -Root $rootPath}
Step 'frontend-three-browser' {& (Join-Path $rootPath 'cpf-tools/scripts/verify-cpf-qa34-frontend-runtime.ps1') -Root $rootPath -ApprovedRegistry $ApprovedRegistry -AdmBaseUrl $AdmBaseUrl -BzaBaseUrl $BzaBaseUrl -AdmAuthState $AdmAuthState -BzaAuthState $BzaAuthState -AdmPrivilegedEndpoints $AdmPrivilegedEndpoints -BzaPrivilegedEndpoints $BzaPrivilegedEndpoints -AdmRouteMatrix $AdmRouteMatrix -BzaRouteMatrix $BzaRouteMatrix -AdmFailureMatrix $AdmFailureMatrix -BzaFailureMatrix $BzaFailureMatrix -AdmSecurityFixture $AdmSecurityFixture -BzaSecurityFixture $BzaSecurityFixture -EvidenceOutput (Join-Path $stage 'frontend.sanitized.json')}
Step 'three-vendor-db-lifecycle' {& (Join-Path $rootPath 'cpf-tools/scripts/invoke-cpf-qa34-db-runtime-matrix.ps1') -Root $rootPath -MariaDbProfile $MariaDbProfile -PostgreSqlProfile $PostgreSqlProfile -OracleProfile $OracleProfile -MariaDbUpgradeProfile $MariaDbUpgradeProfile -PostgreSqlUpgradeProfile $PostgreSqlUpgradeProfile -OracleUpgradeProfile $OracleUpgradeProfile -BackupManifestPath $BackupManifestPath -AllowDestructiveRollback -EvidenceRoot (Join-Path $stage 'db')}
Step 'runtime-fault-matrix' {& (Join-Path $rootPath 'cpf-tools/scripts/verify-cpf-qa34-runtime-matrix.ps1') -Root $rootPath -MariaDbProfile $MariaDbProfile -PostgreSqlProfile $PostgreSqlProfile -OracleProfile $OracleProfile -MariaDbUpgradeProfile $MariaDbUpgradeProfile -PostgreSqlUpgradeProfile $PostgreSqlUpgradeProfile -OracleUpgradeProfile $OracleUpgradeProfile -BackupManifestPath $BackupManifestPath -ApprovedRegistry $ApprovedRegistry -AdmBaseUrl $AdmBaseUrl -BzaBaseUrl $BzaBaseUrl -AdmAuthState $AdmAuthState -BzaAuthState $BzaAuthState -AdmPrivilegedEndpoints $AdmPrivilegedEndpoints -BzaPrivilegedEndpoints $BzaPrivilegedEndpoints -AdmRouteMatrix $AdmRouteMatrix -BzaRouteMatrix $BzaRouteMatrix -AdmFailureMatrix $AdmFailureMatrix -BzaFailureMatrix $BzaFailureMatrix -AdmSecurityFixture $AdmSecurityFixture -BzaSecurityFixture $BzaSecurityFixture -ArtifactPaths $ArtifactPaths -EvidenceOutput (Join-Path $stage 'runtime.sanitized.json')}
Step 'supply-chain' {& (Join-Path $rootPath 'cpf-tools/scripts/generate-cpf-supply-chain-evidence.ps1') -Root $rootPath -OutputDir (Join-Path $stage 'supply-chain') -ArtifactPaths $ArtifactPaths}
if((&git -C $rootPath rev-parse HEAD).Trim()-ne$sha){$failures.Add('SHA changed during final validation')}
if((@(&git -C $rootPath status --porcelain=v1 --untracked-files=all)).Count-gt0){$failures.Add('Working Tree changed during final validation')}
$artifacts=@(Get-ChildItem $stage -Recurse -File|ForEach-Object{[ordered]@{path=[IO.Path]::GetRelativePath($stage,$_.FullName).Replace('\','/');sha256=(Get-FileHash $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant();bytes=$_.Length}})
$e=[ordered]@{schema_version=1;evidence_id='QA39-FINAL-VALIDATION';source_sha=$sha;result=if($failures.Count-eq0){'PASS'}else{'FAIL'};exit_code=if($failures.Count-eq0){0}else{1};started_at=$started.ToString('o');finished_at=[DateTimeOffset]::UtcNow.ToString('o');requirements=@(45..64|ForEach-Object{'QA39-{0:D3}'-f$_});results=$results;failures=$failures;artifacts=$artifacts;sanitized=$true}
New-Item (Split-Path $EvidenceOutput) -ItemType Directory -Force|Out-Null
[IO.File]::WriteAllText($EvidenceOutput,($e|ConvertTo-Json -Depth 30)+"`n",$Utf8NoBom)
if($failures.Count-gt0){throw "QA39 final validation failed: $($failures-join'; ')"}
Write-Host "[CPF][QA39][PASS] final validation evidence=$EvidenceOutput"
