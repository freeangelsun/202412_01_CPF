[CmdletBinding()]
param(
    [ValidateSet('mariadb','mysql','postgresql','oracle','sqlserver')][string]$Vendor='mariadb',
    [string]$Root='.',
    [switch]$RequireImplemented
)
$ErrorActionPreference='Stop'
$root=(Resolve-Path $Root).Path
$catalog=Get-Content (Join-Path $root 'cpf-tools/db/metadata/default-metadata-catalog.json') -Raw -Encoding UTF8 | ConvertFrom-Json
$status=[string]$catalog.vendorImplementations.$Vendor
if($RequireImplemented -and $status -ne 'implemented'){throw "vendor '$Vendor' metadata source is $status"}
if($status -ne 'implemented'){Write-Host "METADATA_VENDOR_NOT_IMPLEMENTED vendor=$Vendor";exit 0}

$seedPath=Join-Path $root "cpf-tools/db/vendor/$Vendor/source/50_framework_seed_data.sql"
if(-not (Test-Path -LiteralPath $seedPath -PathType Leaf)){throw "metadata seed is missing: $seedPath"}
$seed=Get-Content $seedPath -Raw -Encoding UTF8
$missing=New-Object System.Collections.Generic.List[string]

foreach($g in $catalog.codeGroups.psobject.Properties){
    $group=[string]$g.Name
    if($seed -notmatch [regex]::Escape("'$group'")){$missing.Add("group:$group");continue}
    foreach($v in $g.Value){
        $value=[string]$v
        # 같은 INSERT row 안에서 group/value가 함께 존재해야 다른 그룹의 동일 value로 오판하지 않습니다.
        $pairPattern="(?m)^.*'"+[regex]::Escape($group)+"'.*'"+[regex]::Escape($value)+"'.*$"
        if($seed -notmatch $pairPattern){$missing.Add("code:$group:$value")}
    }
}
foreach($v in $catalog.requiredMessages){if($seed -notmatch [regex]::Escape("'$v'")){$missing.Add("message:$v")}}
foreach($v in $catalog.requiredResponseCodes){if($seed -notmatch [regex]::Escape("'$v'")){$missing.Add("response:$v")}}
foreach($v in $catalog.requiredConfigs){if($seed -notmatch [regex]::Escape("'$v'")){$missing.Add("config:$v")}}
if($missing.Count -gt 0){$missing|ForEach-Object{Write-Error $_};throw "default metadata missing=$($missing.Count)"}
Write-Host "DEFAULT_METADATA_VERIFY_PASS vendor=$Vendor required=$($catalog.codeGroups.psobject.Properties.Count + $catalog.requiredMessages.Count + $catalog.requiredResponseCodes.Count + $catalog.requiredConfigs.Count)"
