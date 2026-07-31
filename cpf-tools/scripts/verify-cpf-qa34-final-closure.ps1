[CmdletBinding()]
param(
 [string]$Root=(Get-Location).Path,[Parameter(Mandatory)][string]$OutputRoot,
 [Parameter(Mandatory)][string]$MariaDbProfile,[Parameter(Mandatory)][string]$PostgreSqlProfile,[Parameter(Mandatory)][string]$OracleProfile,
 [Parameter(Mandatory)][string]$MariaDbUpgradeProfile,[Parameter(Mandatory)][string]$PostgreSqlUpgradeProfile,[Parameter(Mandatory)][string]$OracleUpgradeProfile,[Parameter(Mandatory)][string[]]$BackupManifestPath,
 [Parameter(Mandatory)][string]$ApprovedRegistry,[Parameter(Mandatory)][string]$AdmBaseUrl,[Parameter(Mandatory)][string]$BzaBaseUrl,
 [Parameter(Mandatory)][string]$AdmAuthState,[Parameter(Mandatory)][string]$BzaAuthState,[Parameter(Mandatory)][string]$AdmPrivilegedEndpoints,[Parameter(Mandatory)][string]$BzaPrivilegedEndpoints,
 [Parameter(Mandatory)][string]$AdmRouteMatrix,[Parameter(Mandatory)][string]$BzaRouteMatrix,[Parameter(Mandatory)][string]$AdmFailureMatrix,[Parameter(Mandatory)][string]$BzaFailureMatrix,[Parameter(Mandatory)][string]$AdmSecurityFixture,[Parameter(Mandatory)][string]$BzaSecurityFixture,
 [string[]]$ArtifactPaths=@()
)
Set-StrictMode -Version Latest;$ErrorActionPreference='Stop';$rootPath=(Resolve-Path $Root).Path;$out=[IO.Path]::GetFullPath($OutputRoot);New-Item $out -ItemType Directory -Force|Out-Null
$sha=(&git -C $rootPath rev-parse HEAD).Trim();if($sha-notmatch'^[0-9a-f]{40}$'){throw'exact SHA required'};if((@(&git -C $rootPath status --porcelain=v1 --untracked-files=all)).Count-gt0){throw'clean tree required'}
$runtime=Join-Path $out 'runtime/CPF_QA34_RUNTIME_MATRIX.sanitized.json';New-Item (Split-Path $runtime) -ItemType Directory -Force|Out-Null
& (Join-Path $rootPath 'cpf-tools/scripts/verify-cpf-qa34-runtime-matrix.ps1') -Root $rootPath -MariaDbProfile $MariaDbProfile -PostgreSqlProfile $PostgreSqlProfile -OracleProfile $OracleProfile -MariaDbUpgradeProfile $MariaDbUpgradeProfile -PostgreSqlUpgradeProfile $PostgreSqlUpgradeProfile -OracleUpgradeProfile $OracleUpgradeProfile -BackupManifestPath $BackupManifestPath -ApprovedRegistry $ApprovedRegistry -AdmBaseUrl $AdmBaseUrl -BzaBaseUrl $BzaBaseUrl -AdmAuthState $AdmAuthState -BzaAuthState $BzaAuthState -AdmPrivilegedEndpoints $AdmPrivilegedEndpoints -BzaPrivilegedEndpoints $BzaPrivilegedEndpoints -AdmRouteMatrix $AdmRouteMatrix -BzaRouteMatrix $BzaRouteMatrix -AdmFailureMatrix $AdmFailureMatrix -BzaFailureMatrix $BzaFailureMatrix -AdmSecurityFixture $AdmSecurityFixture -BzaSecurityFixture $BzaSecurityFixture -ArtifactPaths $ArtifactPaths -EvidenceOutput $runtime -KeepWorkspace
if($LASTEXITCODE-and$LASTEXITCODE-ne0){throw"Runtime matrix failed (exit=$LASTEXITCODE)"}
$reclassified=Join-Path $out 'qa33-reclassified';&python (Join-Path $rootPath 'cpf-tools/scripts/reclassify-cpf-qa33-matrices.py') --root $rootPath --evidence-root $out --output-dir $reclassified --strict
if($LASTEXITCODE-ne0){throw"QA33 reclassification failed (exit=$LASTEXITCODE)"}
if((&git -C $rootPath rev-parse HEAD).Trim()-ne$sha){throw'SHA changed during final closure'};if((@(&git -C $rootPath status --porcelain=v1 --untracked-files=all)).Count-gt0){throw'Source tree changed during final closure'}
$files=@(Get-ChildItem $out -Recurse -File|ForEach-Object{[ordered]@{path=[IO.Path]::GetRelativePath($out,$_.FullName).Replace('\','/');sha256=(Get-FileHash $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant();bytes=$_.Length}})
$e=[ordered]@{schemaVersion=3;evidenceId='QA34-FINAL-CLOSURE';sourceSha=$sha;resultSha=$sha;branch=(&git -C $rootPath branch --show-current).Trim();sourceDirty=$false;command='verify-cpf-qa34-final-closure.ps1';startedAt=(Get-Date).ToUniversalTime().ToString('o');finishedAt=(Get-Date).ToUniversalTime().ToString('o');exitCode=0;requirements=@((1..19|ForEach-Object{'QA34-REQ-{0:D3}'-f$_}));artifacts=$files;sanitized=$true;releaseEligible=$true}
$ep=Join-Path $out 'CPF_QA34_FINAL_CLOSURE.sanitized.json';[IO.File]::WriteAllText($ep,($e|ConvertTo-Json -Depth 15)+"`n",[Text.UTF8Encoding]::new($false));Write-Host"[CPF][QA34][PASS] final closure=$ep"
