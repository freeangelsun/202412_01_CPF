[CmdletBinding()]
param(
    [string]$Root=(Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string]$SourceSha='',
    [string]$EvidenceRoot='build/cpf-db-runtime-matrix',
    [string]$RuntimeManifestPath=$env:CPF_DB_RUNTIME_MANIFEST,
    [ValidateSet('all','mariadb','postgresql','oracle')]
    [string]$Vendor='all',
    [ValidateSet('Static','Auto','Host','Docker')]
    [string]$ClientAdapter='Auto',
    [switch]$RequireRuntime,
    [switch]$AllowDestructiveRollback,
    [switch]$VerifierOwnedIsolation
)
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
$rootPath=(Resolve-Path -LiteralPath $Root).Path
$evidence=if([IO.Path]::IsPathRooted($EvidenceRoot)){[IO.Path]::GetFullPath($EvidenceRoot)}else{[IO.Path]::GetFullPath((Join-Path $rootPath $EvidenceRoot))}
New-Item -ItemType Directory -Force -Path $evidence | Out-Null
if([string]::IsNullOrWhiteSpace($SourceSha)){
    $candidate=(git -C $rootPath rev-parse HEAD 2>$null)
    if($candidate){$SourceSha=([string]$candidate).Trim()}
}
if($SourceSha -notmatch '^[0-9a-f]{40}$'){throw 'Exact source SHA is required for DB runtime matrix.'}
$staticReport=Join-Path $evidence 'migration-lifecycle-static.json'
& python (Join-Path $rootPath 'cpf-tools/db/verify_migration_lifecycle.py') --root $rootPath --source-sha $SourceSha --report $staticReport
if($LASTEXITCODE -ne 0){throw "DB lifecycle static contract failed: $LASTEXITCODE"}
$officialVendors=@('mariadb','postgresql','oracle')
$vendors=if($Vendor -eq 'all'){$officialVendors}else{@($Vendor)}
$requiredEnv=@{
  mariadb=@('CPF_MARIADB_HOST','CPF_MARIADB_PORT','CPF_MARIADB_DATABASE','CPF_MARIADB_USER','CPF_MARIADB_PASSWORD')
  postgresql=@('CPF_PG_HOST','CPF_PG_PORT','CPF_PG_DATABASE','CPF_PG_USER','CPF_PG_PASSWORD')
  oracle=@('CPF_ORACLE_USER','CPF_ORACLE_PASSWORD','CPF_ORACLE_CONNECT')
}
$runtimeManifest=$null
$runtimeManifestSha256=$null
if(-not [string]::IsNullOrWhiteSpace($RuntimeManifestPath)){
    $manifestCandidate=if([IO.Path]::IsPathRooted($RuntimeManifestPath)){$RuntimeManifestPath}else{Join-Path $rootPath $RuntimeManifestPath}
    if(-not(Test-Path -LiteralPath $manifestCandidate -PathType Leaf)){throw "CPF_DB_RUNTIME_MANIFEST file not found: $manifestCandidate"}
    try{$runtimeManifest=Get-Content -LiteralPath $manifestCandidate -Raw -Encoding UTF8|ConvertFrom-Json -Depth 30}catch{throw "CPF_DB_RUNTIME_MANIFEST is invalid JSON: $($_.Exception.Message)"}
    if([int]$runtimeManifest.schemaVersion-ne1){throw 'CPF_DB_RUNTIME_MANIFEST schemaVersion must be 1.'}
    if($null-eq$runtimeManifest.vendors){throw 'CPF_DB_RUNTIME_MANIFEST vendors object is required.'}
    $actualVendors=@($runtimeManifest.vendors.PSObject.Properties.Name)
    if((@($actualVendors)-join ',')-cne(@($officialVendors)-join ',')){throw "CPF_DB_RUNTIME_MANIFEST vendors must preserve official order: $($officialVendors-join ',')"}
    foreach($vendor in $vendors){
        $environment=$runtimeManifest.vendors.$vendor.environment
        if($null-eq$environment){throw "CPF_DB_RUNTIME_MANIFEST vendor environment is required: $vendor"}
        $actualNames=@($environment.PSObject.Properties.Name)
        $unexpected=@($actualNames|Where-Object{$_-notin$requiredEnv[$vendor]})
        $missingSpecs=@($requiredEnv[$vendor]|Where-Object{$_-notin$actualNames})
        if($unexpected.Count-gt0-or$missingSpecs.Count-gt0){throw "CPF_DB_RUNTIME_MANIFEST environment keys mismatch vendor=$vendor missing=$($missingSpecs-join ',') unexpected=$($unexpected-join ',')"}
        foreach($name in $requiredEnv[$vendor]){
            $spec=$environment.$name
            if($spec-is[string]-or$null-eq$spec){throw "CPF_DB_RUNTIME_MANIFEST environment entry must be an object: vendor=$vendor key=$name"}
            $envProperty=$spec.PSObject.Properties['env'];$valueProperty=$spec.PSObject.Properties['value']
            $hasEnv=$null-ne$envProperty-and-not[string]::IsNullOrWhiteSpace([string]$envProperty.Value)
            $hasValue=$null-ne$valueProperty-and-not[string]::IsNullOrWhiteSpace([string]$valueProperty.Value)
            if($hasEnv-eq$hasValue){throw "CPF_DB_RUNTIME_MANIFEST entry requires exactly one env or value: vendor=$vendor key=$name"}
            if($hasValue-and$name-match'(?i)(PASSWORD|SECRET|TOKEN)'){throw "CPF_DB_RUNTIME_MANIFEST inline secret is forbidden: vendor=$vendor key=$name"}
        }
    }
    $runtimeManifestSha256=(Get-FileHash -LiteralPath $manifestCandidate -Algorithm SHA256).Hash.ToLowerInvariant()
}
$result=[ordered]@{schemaVersion=1;sourceSha=$SourceSha;startedAt=([DateTimeOffset]::UtcNow.ToString('o'));runtimeManifest=[ordered]@{configured=($null-ne$runtimeManifest);sha256=$runtimeManifestSha256};vendors=@();sanitized=$true}
foreach($vendor in $vendors){
    $previousEnvironment=@{}
    try{
        if($VerifierOwnedIsolation){
            $adminPassword=[Environment]::GetEnvironmentVariable('CPF_ADMIN_PASSWORD','Process')
            if([string]::IsNullOrWhiteSpace($adminPassword)){
                $result.vendors += [ordered]@{vendor=$vendor;status='UNVERIFIED_EXTERNAL_RUNTIME';missingEnvironment=@('CPF_ADMIN_PASSWORD');verifierOwnedIsolation=$true}
                continue
            }
            $isolatedRoot=Join-Path $evidence "isolated-$vendor"
            & pwsh -NoProfile -File (Join-Path $rootPath 'cpf-tools/db/verification/invoke-cpf-db-verifier-owned-lifecycle.ps1') -Vendor $vendor -Root $rootPath -SourceSha $SourceSha -EvidenceRoot $isolatedRoot
            $rc=$LASTEXITCODE
            $isolatedResultPath=Join-Path $isolatedRoot 'verifier-owned-lifecycle.json'
            $isolatedStatus='MISSING_RESULT'
            if(Test-Path -LiteralPath $isolatedResultPath -PathType Leaf){try{$isolatedStatus=[string](Get-Content -LiteralPath $isolatedResultPath -Raw -Encoding UTF8|ConvertFrom-Json -Depth 50).status}catch{$isolatedStatus='INVALID_RESULT'}}
            $status=if($rc-eq0 -and $isolatedStatus-eq'PASS'){'PASS'}else{'FAIL'}
            $result.vendors += [ordered]@{vendor=$vendor;status=$status;verifierOwnedIsolation=$true;resultPath=$isolatedResultPath}
            continue
        }
        if($null-ne$runtimeManifest){
            $environment=$runtimeManifest.vendors.$vendor.environment
            foreach($name in $requiredEnv[$vendor]){
                $previousEnvironment[$name]=[Environment]::GetEnvironmentVariable($name)
                $spec=$environment.$name
                $envProperty=$spec.PSObject.Properties['env']
                $value=if($null-ne$envProperty){[Environment]::GetEnvironmentVariable([string]$envProperty.Value)}else{[string]$spec.value}
                [Environment]::SetEnvironmentVariable($name,$value)
            }
        }
        $missing=@($requiredEnv[$vendor] | Where-Object {[string]::IsNullOrWhiteSpace([Environment]::GetEnvironmentVariable($_))})
        if($missing.Count -gt 0){
            $result.vendors += [ordered]@{vendor=$vendor;status='UNVERIFIED_EXTERNAL_RUNTIME';missingEnvironment=$missing}
            continue
        }
        $stages=@('FreshInstall','Upgrade')
        if($AllowDestructiveRollback){$stages+='RollbackReapply'}
        $stageResults=@()
        foreach($stage in $stages){
            $stageStem="$vendor-$($stage.ToLowerInvariant())"
            $stagePlan=Join-Path $evidence "$stageStem.lifecycle-plan.json"
            $stageResult=Join-Path $evidence "$stageStem.lifecycle-result.json"
            $stageLogDir=Join-Path $evidence "$stageStem.logs"
            & pwsh -NoProfile -File (Join-Path $rootPath 'cpf-tools/db/tools/run-db-vendor-lifecycle.ps1') `
                -Vendor $vendor -Mode $stage -Root $rootPath -ClientAdapter $ClientAdapter -LogDir $stageLogDir `
                -LifecyclePlanPath $stagePlan -ResultPath $stageResult
            $rc=$LASTEXITCODE
            $lifecycleStatus='MISSING_RESULT'
            if(Test-Path -LiteralPath $stageResult -PathType Leaf){
                try{$lifecycleStatus=[string](Get-Content -LiteralPath $stageResult -Raw -Encoding UTF8|ConvertFrom-Json -Depth 30).status}catch{$lifecycleStatus='INVALID_RESULT'}
            }
            $stageStatus=if($rc-ne0){'FAIL'}elseif($lifecycleStatus-eq'SUCCEEDED'){'PASS'}elseif($lifecycleStatus-eq'PLANNED'){'UNVERIFIED_EXTERNAL_RUNTIME'}else{'FAIL'}
            $stageResults += [ordered]@{stage=$stage;exitCode=$rc;status=$stageStatus;lifecycleStatus=$lifecycleStatus;resultPath=$stageResult}
            if($stageStatus-eq'FAIL'){break}
        }
        $vendorStatus=if(@($stageResults|Where-Object status -eq 'FAIL').Count-gt0){'FAIL'}elseif(@($stageResults|Where-Object status -eq 'UNVERIFIED_EXTERNAL_RUNTIME').Count-gt0){'UNVERIFIED_EXTERNAL_RUNTIME'}else{'PASS'}
        $result.vendors += [ordered]@{vendor=$vendor;status=$vendorStatus;stages=$stageResults}
    }finally{
        foreach($name in $previousEnvironment.Keys){[Environment]::SetEnvironmentVariable($name,$previousEnvironment[$name])}
    }
}
$result.finishedAt=[DateTimeOffset]::UtcNow.ToString('o')
$result.status=if(@($result.vendors|Where-Object status -eq 'FAIL').Count -gt 0){'FAIL'}elseif(@($result.vendors|Where-Object status -eq 'UNVERIFIED_EXTERNAL_RUNTIME').Count -gt 0){'UNVERIFIED_EXTERNAL_RUNTIME'}else{'PASS'}
$out=Join-Path $evidence 'db-runtime-matrix.json'
$result|ConvertTo-Json -Depth 20|Set-Content -LiteralPath $out -Encoding UTF8
if($result.status -eq 'FAIL'){throw 'DB runtime matrix failed.'}
if($RequireRuntime -and $result.status -ne 'PASS'){throw 'DB runtime matrix requires all official vendor runtimes.'}
Write-Host "[CPF][DB][MATRIX][$($result.status)] $out"
