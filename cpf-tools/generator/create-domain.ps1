[CmdletBinding()]
param(
    [string]$Root='',
    [Alias('Definition')][string]$DefinitionFile='',
    [string]$OutputDir='',
    [string]$DomainName='',
    [string]$SystemCode='',
    [string]$ModuleName='',
    [string]$PackageName='',
    [ValidateSet('CUSTOMER_BUSINESS_DB')][string]$BusinessDatabaseRole='CUSTOMER_BUSINESS_DB',
    [string]$TablePrefix='',
    [ValidateSet('none','jdbc','mybatis','jpa')][string]$Persistence='mybatis',
    [switch]$DryRun,
    [switch]$GeneratePatch,
    [switch]$Apply,
    [string]$DatabaseVendor=''
)
# Legacy PowerShell surface는 입력 Adapter일 뿐이며 Source/SQL Template를 소유하지 않는다.
$ErrorActionPreference='Stop'
if([string]::IsNullOrWhiteSpace($Root)){$Root=(Resolve-Path "$PSScriptRoot\..\..").Path}else{$Root=(Resolve-Path -LiteralPath $Root).Path}
$cli=Join-Path $Root 'cpf-tools\runtime\cli\cpf.cmd'
if(-not (Test-Path -LiteralPath $cli -PathType Leaf)){throw "CPF canonical CLI가 없습니다: $cli"}
$temp=$null
try {
    if([string]::IsNullOrWhiteSpace($DefinitionFile)){
        foreach($required in @('DomainName','SystemCode','TablePrefix')){
            if([string]::IsNullOrWhiteSpace((Get-Variable -Name $required -ValueOnly))){throw "$required 값 또는 -DefinitionFile이 필요합니다."}
        }
        if(-not [string]::IsNullOrWhiteSpace($ModuleName)){ Write-Warning 'ModuleName은 현재 Generated Domain 입력 계약에서 제거되었습니다. domain.name이 물리 Root와 Module naming을 결정합니다.' }
        if(-not [string]::IsNullOrWhiteSpace($DatabaseVendor)){
            throw 'DatabaseVendor는 Generated Domain 입력에서 제거되었습니다. DB Vendor는 cpf domain setup --db-vendor 또는 별도 Runtime DB Profile에서 선택하십시오.'
        }
        $temp=Join-Path ([IO.Path]::GetTempPath()) ('cpf-domain-'+[guid]::NewGuid().ToString('N')+'.yaml')
        @"
# Legacy Adapter가 생성한 일회성 cpf-domain.yaml. Canonical Engine만 Template를 소유한다.
domain:
  name: $DomainName
  systemCode: $SystemCode$(if([string]::IsNullOrWhiteSpace($PackageName)){''}else{"`n  packageName: $PackageName"})
database:
  role: CUSTOMER_BUSINESS_DB
  tablePrefix: $TablePrefix
preset: standard-enterprise
modules:
  online: true
features:
  persistence: $Persistence
  httpClient: true
  resilience: true
  cache: none
  messaging: none
generation:
  sampleTransaction: true
"@ | Set-Content -LiteralPath $temp -Encoding UTF8
        $DefinitionFile=$temp
    }
    $cmd=@('dev','domain')
    if($GeneratePatch){$cmd+=@('diff','--file',$DefinitionFile)}
    elseif($DryRun){$cmd+=@('dry-run','--file',$DefinitionFile)}
    else{$cmd+=@('generate','--file',$DefinitionFile)}
    if(-not [string]::IsNullOrWhiteSpace($OutputDir)){$cmd+=@('--output',$OutputDir)}
    & $cli @cmd
    $code=$LASTEXITCODE
    if($code -ne 0){throw "CPF canonical generator 실패: rc=$code"}
} finally {
    if($null -ne $temp -and (Test-Path -LiteralPath $temp)){Remove-Item -LiteralPath $temp -Force -ErrorAction SilentlyContinue}
}
