param([string]$Root=(Resolve-Path "$PSScriptRoot\..\..").Path)
$ErrorActionPreference='Stop'
$Root=(Resolve-Path -LiteralPath $Root).Path
$errors=[System.Collections.Generic.List[string]]::new()

foreach($relative in @(
 'cpf-tools/db/source',
 'docker-compose.local.yml',
 'cpf-core/src/main/java/com/cpf/core/common/batch',
 'cpf-core/src/test/java/com/cpf/core/common/batch',
 'cpf-core/src/main/java/com/cpf/core/config/CpfBatchAutoConfiguration.java',
 'cpf-core/src/main/java/com/cpf/core/config/CpfCenterCutAutoConfiguration.java',
 'cpf-biz-admin/frontend/src/features/console.ts',
 'cpf-biz-admin/frontend/src/features/directory',
 'cpf-biz-admin/frontend/src/features/access',
 'cpf-biz-admin/frontend/src/features/approval',
 'cpf-biz-admin/frontend/src/features/support',
 'cpf-admin/frontend/src/features/observability',
 'cpf-admin/frontend/src/features/platform',
 'cpf-admin/frontend/src/features/business',
 'cpf-admin/frontend/src/features/access',
 'cpf-admin/frontend/src/features/reference',
 'cpf-admin/src/main/java/com/cpf/admin/opr/dto/AdmBusinessDayRequest.java'
)){
    if(Test-Path(Join-Path $Root $relative)){$errors.Add("obsolete artifact: $relative")}
}

$surfacePolicyPath=Join-Path $Root 'cpf-tools/governance/cpf-product-surface-policy.json'
if(-not(Test-Path -LiteralPath $surfacePolicyPath -PathType Leaf)){
    throw "product surface policy missing: $surfacePolicyPath"
}
$surfacePolicy=Get-Content -LiteralPath $surfacePolicyPath -Raw -Encoding UTF8 | ConvertFrom-Json -Depth 20
$fixedCpfRoots=@($surfacePolicy.moduleOwners |
    Where-Object { [string]$_.prefix -match '^cpf-[^/]+/$' -and [string]$_.owner -ne 'generated-domain' } |
    ForEach-Object { ([string]$_.prefix).TrimEnd('/') } |
    Sort-Object -Unique)
$settings=Get-Content -LiteralPath (Join-Path $Root 'settings.gradle') -Raw -Encoding UTF8
$generatedIdentities=[System.Collections.Generic.List[object]]::new()
$generatedCandidates=@(Get-ChildItem -LiteralPath $Root -Directory -Filter 'cpf-*' |
    Where-Object { $_.Name -notin $fixedCpfRoots })
foreach($candidate in $generatedCandidates){
    $manifestPath=Join-Path $candidate.FullName 'manifest/domain-manifest.json'
    $ownershipPath=Join-Path $candidate.FullName 'manifest/generator-ownership.json'
    if(-not(Test-Path -LiteralPath $manifestPath -PathType Leaf) -or
            -not(Test-Path -LiteralPath $ownershipPath -PathType Leaf)){
        $errors.Add("unknown CPF root without Generator manifest pair: $($candidate.Name)")
        continue
    }
    try{
        $manifest=Get-Content -LiteralPath $manifestPath -Raw -Encoding UTF8 |
            ConvertFrom-Json -ErrorAction Stop
        $ownership=Get-Content -LiteralPath $ownershipPath -Raw -Encoding UTF8 |
            ConvertFrom-Json -ErrorAction Stop
    }catch{
        $errors.Add("invalid Generated Domain manifest JSON: $($candidate.Name) :: $($_.Exception.Message)")
        continue
    }
    $identityErrors=[System.Collections.Generic.List[string]]::new()
    if([string]$manifest.domainType -cne 'GENERATED_DOMAIN'){
        $identityErrors.Add('domainType') | Out-Null
    }
    if([string]$manifest.dependencyModel -cne 'root-project' -or
            [string]$ownership.dependencyModel -cne 'root-project'){
        $identityErrors.Add('dependencyModel') | Out-Null
    }
    foreach($propertyName in @(
            'projectName','moduleCode','moduleName','domainName',
            'systemCode','packageName','schemaName','tablePrefix')){
        $manifestValue=[string]$manifest.$propertyName
        $ownershipValue=[string]$ownership.$propertyName
        if([string]::IsNullOrWhiteSpace($manifestValue) -or $manifestValue -cne $ownershipValue){
            $identityErrors.Add($propertyName) | Out-Null
        }
    }
    if([string]$manifest.projectName -cne $candidate.Name -or
            [string]$ownership.moduleDirectory -cne $candidate.Name -or
            [string]$ownership.outputDirectory -cne $candidate.Name){
        $identityErrors.Add('directory') | Out-Null
    }
    if([string]$manifest.systemCode -cnotmatch '^[A-Z][A-Z0-9]{2}$' -or
            [string]$manifest.domainName -cnotmatch '^[a-z][a-z0-9]{1,29}$' -or
            [string]$manifest.packageName -cnotmatch '^com\.cpf\.[a-z][a-z0-9]*(?:\.[a-z][a-z0-9]*)*$'){
        $identityErrors.Add('canonical-format') | Out-Null
    }
    $escapedProject=[regex]::Escape($candidate.Name)
    if($settings -notmatch "(?m)^\s*include(?:\s*\()?[^`r`n]*['`"]:?$escapedProject['`"]"){
        $identityErrors.Add('settings-include') | Out-Null
    }
    if($identityErrors.Count){
        $errors.Add("Generated Domain identity mismatch: $($candidate.Name) [$($identityErrors -join ', ')]")
        continue
    }
    $generatedIdentities.Add([pscustomobject]@{
        projectName=$candidate.Name
        systemCode=[string]$manifest.systemCode
        packageName=[string]$manifest.packageName
    }) | Out-Null
}
foreach($propertyName in @('systemCode','packageName')){
    $duplicates=@($generatedIdentities | Group-Object $propertyName | Where-Object Count -gt 1)
    if($duplicates.Count){
        $errors.Add("duplicate Generated Domain $propertyName`: $(($duplicates.Name | Sort-Object) -join ', ')")
    }
}

$legacyBatchRefs=@(Get-ChildItem $Root -Recurse -File -Filter '*.java' -ErrorAction SilentlyContinue |
    Select-String -Pattern 'com\.cpf\.core\.common\.batch\.' -ErrorAction SilentlyContinue)
if($legacyBatchRefs.Count){
    $preview=($legacyBatchRefs | Select-Object -First 20 | ForEach-Object { "$($_.Path):$($_.LineNumber)" }) -join ', '
    $errors.Add("legacy core batch reference remains: $($legacyBatchRefs.Count) hit(s) [$preview]")
}

foreach($dir in @('logs','tmp','temp')){
    $path=Join-Path $Root $dir
    if(Test-Path $path){
        $tracked=@(& git -C $Root ls-files -- $dir 2>$null)
        if($tracked.Count -eq 0){$errors.Add("untracked root runtime directory: $dir")}
    }
}
$garbage=@(Get-ChildItem $Root -File -Force -ErrorAction SilentlyContinue |
    Where-Object {$_.Name -match '\.(zip|log|tmp|bak)$' -or $_.Name -match '^(APPLY|PATCH|EVIDENCE)_'} |
    Where-Object {$_.Name -ne 'README.md'})
if($garbage.Count){$errors.Add("root garbage file: " + (($garbage.Name|Sort-Object) -join ', '))}
if($errors.Count){$errors|ForEach-Object{Write-Host " - $_"};throw "R10 cleanup gate FAIL: $($errors.Count)건"}
Write-Host 'R10 repository cleanup gate PASS.'
