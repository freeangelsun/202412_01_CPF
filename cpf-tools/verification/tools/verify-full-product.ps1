param(
    [string]$Root=(Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [switch]$StaticOnly,
    [switch]$WithDatabase,
    [switch]$WithGeneratorLifecycle,
    [switch]$WithBrowser,
    [switch]$RequireAll,
    [string]$Profile='local',
    [string]$EvidenceOutput=''
)
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
$Root=(Resolve-Path -LiteralPath $Root).Path
$started=[DateTimeOffset]::Now
$stamp=$started.ToString('yyyyMMdd_HHmmss')
$raw=Join-Path ([IO.Path]::GetTempPath()) "cpf_full_verify_${stamp}.raw.log"
$utf8=[Text.UTF8Encoding]::new($false)
[IO.File]::WriteAllText($raw,"CPF_FULL_PRODUCT_VERIFY`nSTARTED_AT=$($started.ToString('o'))`nROOT=$Root`nPROFILE=$Profile`n`n",$utf8)
$results=[Collections.Generic.List[object]]::new()
$allOk=$true
$worktreeBefore=@(git -C $Root status --porcelain=v1)

function Raw([string]$Text){[IO.File]::AppendAllText($raw,$Text+"`n",$utf8)}
function Result([string]$Name,[string]$Status,[string]$Detail){
    $results.Add([pscustomobject]@{Name=$Name;Status=$Status;Detail=$Detail})
    $prefix=if($Status-eq'PASS'){'[PASS]'}elseif($Status-eq'SKIPPED'){'[SKIP]'}else{'[FAIL]'}
    Write-Host "$prefix $Name - $Detail";Raw "$prefix $Name - $Detail"
}
function Native([string]$Name,[string]$File,[string[]]$Args,[string]$Working=$Root){
    Raw "--- COMMAND $Name ---";Raw "WORKDIR=$Working";Raw ("COMMAND="+$File+" "+($Args-join' '))
    Push-Location $Working
    try{
        $output=@(& $File @Args 2>&1);$code=$LASTEXITCODE
        if($null-eq$code){$code=if($?){0}else{1}}
        $output|ForEach-Object{$line=$_.ToString();Write-Host $line;Raw $line}
        Raw "EXIT_CODE=$code"
        if($code-ne0){Result $Name 'FAIL' "exit=$code";return $false}
        Result $Name 'PASS' 'completed';return $true
    }catch{Raw ("EXCEPTION="+$_.Exception.Message);Result $Name 'FAIL' $_.Exception.Message;return $false}
    finally{Pop-Location;Raw "--- END $Name ---`n"}
}
function Pwsh([string]$Name,[string]$Relative,[string[]]$Args=@()){
    $path=Join-Path $Root $Relative
    if(-not(Test-Path $path)){Result $Name 'FAIL' "missing $Relative";return $false}
    return Native $Name 'pwsh' (@('-NoProfile','-File',$path)+$Args)
}
function OptionalGate([string]$Name,[string]$Relative){
    if(Test-Path(Join-Path $Root $Relative)){
        if(-not(Pwsh $Name $Relative @('-Root',$Root))){$script:allOk=$false}
    }else{Result $Name 'SKIPPED' "gate not present: $Relative"}
}
function Skip([string]$Name,[string]$Why){Result $Name 'SKIPPED' $Why}

try{
    if(-not(Native 'Git diff check' 'git' @('-C',$Root,'diff','--check'))){$allOk=$false}

    foreach($gate in @(
        @('Work context','cpf-tools/verification/tools/check-work-context.ps1'),
        @('Enterprise source closure','cpf-tools/verification/tools/check-enterprise-source-closure.ps1'),
        @('Repository hygiene preflight','cpf-tools/verification/tools/check-repository-hygiene.ps1'),
        @('Frontend route/local asset','cpf-tools/verification/tools/check-frontend-route-targets.ps1'),
        @('Source documentation/OpenAPI','cpf-tools/verification/tools/check-source-documentation-standard.ps1'),
        @('Architecture ownership','cpf-tools/governance/tools/check-architecture-ownership.ps1'),
        @('Core/BAT ownership','cpf-tools/verification/tools/check-core-owner-boundary.ps1'),
        @('Service-call boundary','cpf-tools/verification/tools/check-service-call-boundary.ps1'),
        @('Repository hygiene','cpf-tools/verification/tools/check-repository-hygiene.ps1'),
        @('Document links','cpf-tools/verification/tools/check-document-links.ps1'),
        @('UTF-8','cpf-tools/verification/tools/check-utf8.ps1'),
        @('Java 25 standard','cpf-tools/verification/tools/check-java25-standard.ps1'),
        @('Legacy name','cpf-tools/verification/tools/check-legacy-name.ps1'),
        @('Legacy BAT migration','cpf-tools/db/verification/check-legacy-batch-migration.ps1'),
        @('OpenAPI coverage','cpf-tools/verification/openapi/check-openapi-source-coverage.ps1'),
        @('Sample standard','cpf-tools/verification/tools/check-sample-standard.ps1'),
        @('Sample coverage','cpf-tools/verification/tools/check-sample-coverage.ps1'),
        @('Transaction ID','cpf-tools/verification/tools/check-transaction-id-standard.ps1'),
        @('SQL canonical','cpf-tools/db/verification/check-sql-canonical.ps1'),
        @('SQL standard','cpf-tools/db/verification/check-sql-standard.ps1'),
        @('Spring Batch 6 sequence contract','cpf-tools/verification/tools/check-spring-batch-sequence-contract.ps1'),
        @('DB vendor parity','cpf-tools/db/verification/check-db-vendor-pack-parity.ps1'),
        @('Offline DB resource pack','cpf-tools/db/verification/check-offline-db-resource-pack.ps1'),
        @('Official DB vendor readiness','cpf-tools/db/verification/check-official-db-vendor-readiness.ps1'),
        @('Runtime query contract integrity','cpf-tools/db/verification/check-query-contract-integrity.ps1'),
        @('ADM/MBW data safety','cpf-tools/verification/tools/check-admin-data-safety.ps1'),
        @('Data-safety schema contract','cpf-tools/verification/tools/check-data-safety-schema-contract.ps1'),
        @('Enterprise QA closing','cpf-tools/verification/tools/check-enterprise-qa-closing.ps1'),
        @('Migration checksum immutable','cpf-tools/db/verification/check-migration-checksums.ps1'),
        @('DB profile/generated domain','cpf-tools/db/verification/check-database-profile-standard.ps1')
    )){
        OptionalGate $gate[0] $gate[1]
    }
    if(-not(Native 'Logging DX' 'python' @((Join-Path $Root 'cpf-tools/verification/verify_logging_dx.py')))){$allOk=$false}

    # Verification은 read-only입니다. DB/Generated Domain sync는 명시적 maintenance 단계에서만 실행합니다.

    if(-not $StaticOnly){
        $gradle=Join-Path $Root 'gradlew.bat'
        if(Test-Path $gradle){
            if(-not(Native 'Gradle clean test assemble' $gradle @('clean','test','assemble','--no-daemon'))){$allOk=$false}
        }else{Result 'Gradle clean test assemble' 'FAIL' 'gradlew.bat missing';$allOk=$false}

        foreach($fe in @('cpf-admin/frontend','cpf-backoffice-web/frontend')){
            $dir=Join-Path $Root $fe
            if(-not(Test-Path(Join-Path $dir 'package.json'))){Result "$fe frontend" 'FAIL' 'package.json missing';$allOk=$false;continue}
            if(-not(Native "$fe npm test" 'npm.cmd' @('test','--','--run') $dir)){$allOk=$false}
            if(-not(Native "$fe npm build" 'npm.cmd' @('run','build') $dir)){$allOk=$false}
        }
    }else{
        Skip 'Gradle clean test assemble' '-StaticOnly'
        Skip 'ADM/MBW npm test/build' '-StaticOnly'
    }

    if($WithGeneratorLifecycle){
        if(-not(Pwsh 'Arbitrary Generated Domain parity' 'cpf-tools/generator/verification/check-generator-arbitrary-domain-parity.ps1' @('-Root',$Root))){$allOk=$false}
    }else{Skip 'Generated Domain lifecycle' 'use -WithGeneratorLifecycle'}

    if($WithDatabase){
        if(-not(Pwsh 'CPF all DB initialize/verify' 'cpf-tools/db/tools/initialize-cpf-database.ps1' @('-All','-RequireRun'))){$allOk=$false}
        if(-not(Pwsh 'Data-safety migration recovery' 'cpf-tools/db/verification/smoke-data-safety-migration-recovery.ps1' @('-Root',$Root,'-RequireRun'))){$allOk=$false}
    }else{Skip 'CPF all DB initialize/verify' 'use -WithDatabase'}

    if($WithBrowser){
        foreach($script in @('cpf-tools/verification/tools/smoke-adm-ui.ps1','cpf-tools/verification/tools/smoke-backoffice-ui.ps1')){
            if(Test-Path(Join-Path $Root $script)){
                if(-not(Pwsh "Browser smoke: $script" $script @('-Root',$Root))){$allOk=$false}
            }else{Result "Browser smoke: $script" 'FAIL' 'script missing';$allOk=$false}
        }
    }else{Skip 'ADM/MBW browser smoke' 'use -WithBrowser'}

    # 실행으로 생성된 root garbage가 없는지 마지막에 재검증한다.
    foreach($post in @(
        @('Post-run source closure','cpf-tools/verification/tools/check-enterprise-source-closure.ps1'),
        @('Post-run repository hygiene','cpf-tools/verification/tools/check-repository-hygiene.ps1')
    )){
        if(Test-Path(Join-Path $Root $post[1])){
            if(-not(Pwsh $post[0] $post[1] @('-Root',$Root))){$allOk=$false}
        }
    }

    Write-Host '';Write-Host '=== CPF Full Product Verification Summary ==='
    $results|Format-Table -AutoSize
    $worktreeAfter=@(git -C $Root status --porcelain=v1)
    if((Compare-Object $worktreeBefore $worktreeAfter).Count -ne 0){
        Result 'Verification worktree immutability' 'FAIL' 'verification changed product worktree';$allOk=$false
    }else{Result 'Verification worktree immutability' 'PASS' 'unchanged'}
    $failed=@($results|Where-Object Status -eq 'FAIL').Count
    $skipped=@($results|Where-Object Status -eq 'SKIPPED').Count

    $finished=[DateTimeOffset]::Now
    Raw "FINISHED_AT=$($finished.ToString('o'))";Raw "FAILED=$failed";Raw "SKIPPED=$skipped";Raw "REQUIRE_ALL=$RequireAll"
    foreach($r in $results){Raw "$($r.Status)|$($r.Name)|$($r.Detail)"}

    $exit=0
    if(-not $allOk -or $failed-gt0){$exit=1}
    elseif($RequireAll -and $skipped-gt0){$exit=2}
    elseif($skipped-gt0){$exit=3}

    if([string]::IsNullOrWhiteSpace($EvidenceOutput)){
        $EvidenceOutput="cpf-docs/work/evidence/current/full-verification-${stamp}.sanitized.log"
    }
    $writer=Join-Path $Root 'cpf-tools/verification/tools/write-sanitized-evidence.ps1'
    if(Test-Path $writer){
        $status=if($exit-eq0){'완료'}elseif($exit-eq2 -or $exit-eq3){'미검증'}else{'실패'}
        $reason=if($skipped-gt0){"$skipped verification group(s) skipped."}else{''}
        $command=if([string]::IsNullOrWhiteSpace($MyInvocation.Line)){'verify-full-product.ps1'}else{$MyInvocation.Line.Trim()}
        $reproduce="pwsh -File .\cpf-tools\verification\tools\verify-full-product.ps1 -Root `"$Root`" -WithDatabase -WithGeneratorLifecycle -WithBrowser -RequireAll -Profile `"$Profile`""
        & pwsh -NoProfile -File $writer `
            -EvidenceId "FULL-PRODUCT-$stamp" -Status $status -Command $command `
            -OutputPath $EvidenceOutput -ExitCode $exit -SourceLog $raw -Profile $Profile `
            -Reason $reason -ReproduceCommand $reproduce -Skipped $skipped -Failures $failed -Root $Root
        if($LASTEXITCODE-ne0){Write-Error "Evidence writer failed. exit=$LASTEXITCODE";if($exit-eq0){$exit=1}}
    }else{
        Write-Error "Evidence writer missing: $writer"
        if($exit-eq0){$exit=1}
    }

    if($exit-eq2){Write-Error "RequireAll=true but $skipped verification group(s) were skipped.";exit 2}
    if($exit-eq3){Write-Warning "CPF development verification PARTIAL. skipped=$skipped evidence=$EvidenceOutput";exit 3}
    if($exit-ne0){Write-Error "CPF full verification FAILED ($failed failure(s)).";exit 1}
    Write-Host "CPF full verification PASS. skipped=0 evidence=$EvidenceOutput"
    exit 0
} finally {
    Remove-Item $raw -Force -ErrorAction SilentlyContinue
}
