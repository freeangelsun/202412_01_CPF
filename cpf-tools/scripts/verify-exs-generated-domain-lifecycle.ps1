param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [switch] $WithDatabase
)
$ErrorActionPreference="Stop"
$Root=(Resolve-Path -LiteralPath $Root).Path
$module=Join-Path $Root "cpf-external"
if(Test-Path $module){throw "Baseline에 cpf-external이 존재합니다. EXS는 검증 시에만 생성해야 합니다."}

$create=Join-Path $Root "cpf-tools/generator/create-domain.ps1"
$verify=Join-Path $Root "cpf-tools/scripts/verify-domain.ps1"
$remove=Join-Path $Root "cpf-tools/scripts/remove-domain.ps1"
try {
    $createArgs=@("-NoProfile","-ExecutionPolicy","Bypass","-File",$create,
      "-Root",$Root,"-DomainName","external","-SystemCode","EXS",
      "-External","Y","-Database","Y","-Online","Y","-Apply","-AllowReserved")
    if($WithDatabase){$createArgs += "-ProvisionDatabase"}
    & pwsh @createArgs
    if($LASTEXITCODE -ne 0){throw "EXS 생성 실패"}

    & pwsh -NoProfile -ExecutionPolicy Bypass -File $verify -Root $Root -DomainName external -SystemCode EXS
    if($LASTEXITCODE -ne 0){throw "EXS 검증 실패"}

    $manifest=Join-Path $module "manifest/domain-manifest.json"
    if(-not(Test-Path $manifest)){throw "EXS generator manifest 누락"}
    $m=Get-Content $manifest -Raw|ConvertFrom-Json
    if($m.domainType -ne "GENERATED_DOMAIN" -or $m.systemCode -ne "EXS"){throw "EXS Generated Domain 정본 불일치"}
    Write-Host "EXS Generated Domain create/verify PASS."
}
finally {
    if(Test-Path $module){
        & pwsh -NoProfile -ExecutionPolicy Bypass -File $remove -Root $Root -DomainName external -SystemCode EXS
        if($LASTEXITCODE -ne 0){throw "EXS 검증 후 제거 실패"}
    }
}
if(Test-Path $module){throw "EXS lifecycle 종료 후 cpf-external 잔존"}
$settings=Get-Content (Join-Path $Root "settings.gradle") -Raw
if($settings -match "cpf-external"){throw "EXS lifecycle 종료 후 settings.gradle 잔존"}
Write-Host "EXS Generated Domain lifecycle cleanup PASS."
