param([string]$Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path)
$ErrorActionPreference = 'Stop'
$failures = [System.Collections.Generic.List[string]]::new()
function Fail([string]$m) { $failures.Add($m) }

function Resolve-LazyImports([string]$appName,[string]$routeFile) {
    $routePath=Join-Path $Root $routeFile
    if(-not(Test-Path $routePath)){Fail "$appName routes missing: $routeFile";return @()}
    $text=Get-Content $routePath -Raw
    $imports=[regex]::Matches($text,'import\([''"]([^''"]+\.vue)[''"]\)')|ForEach-Object{$_.Groups[1].Value}
    $base=Split-Path $routePath -Parent
    foreach($imp in $imports){$resolved=[IO.Path]::GetFullPath((Join-Path $base $imp));if(-not(Test-Path $resolved)){Fail "$appName lazy page missing: $imp"}}
    return @($imports)
}

$admRoutePath=Join-Path $Root 'cpf-admin/frontend/src/app/routes.ts'
$admRouteText=Get-Content $admRoutePath -Raw
$admRouteEntries = [regex]::Matches(
    $admRouteText,
    '(?m)^\s*"([^"]+)":\s*\{\s*routeId:\s*"([^"]+)"'
)
$admRouteIds = $admRouteEntries | ForEach-Object { $_.Groups[2].Value }
foreach($entry in $admRouteEntries){
    if($entry.Groups[1].Value -ne $entry.Groups[2].Value){
        Fail "ADM registry key/routeId mismatch key=$($entry.Groups[1].Value) routeId=$($entry.Groups[2].Value)"
    }
}
$admImports=Resolve-LazyImports 'ADM' 'cpf-admin/frontend/src/app/routes.ts'
$admState=Get-Content (Join-Path $Root 'cpf-admin/frontend/src/state/createAdmState.ts') -Raw
$admMenuIds=[regex]::Matches($admState,'\{\s*id:\s*"([^"]+)"\s*,\s*menuId:')|ForEach-Object{$_.Groups[1].Value}|Sort-Object -Unique
if(($admRouteIds|Sort-Object -Unique).Count -ne $admRouteIds.Count){Fail 'ADM route ids duplicated'}
if($admRouteIds.Count -ne $admImports.Count){Fail "ADM route/import mismatch route=$($admRouteIds.Count) import=$($admImports.Count)"}
foreach($id in $admMenuIds){if($admRouteIds -notcontains $id){Fail "ADM menu has no feature route: $id"}}
foreach($id in $admRouteIds){if($admMenuIds -notcontains $id){Fail "ADM feature route has no menu: $id"}}

$bzaRoutePath=Join-Path $Root 'cpf-biz-admin/frontend/src/app/routes.ts'
$bzaText=Get-Content $bzaRoutePath -Raw
$bzaIds=[regex]::Matches($bzaText,'\bid:\s*"([^"]+)"')|ForEach-Object{$_.Groups[1].Value}
$bzaImports=Resolve-LazyImports 'BZA' 'cpf-biz-admin/frontend/src/app/routes.ts'
if(($bzaIds|Sort-Object -Unique).Count -ne $bzaIds.Count){Fail 'BZA route ids duplicated'}
if($bzaIds.Count -ne $bzaImports.Count){Fail "BZA route/import mismatch route=$($bzaIds.Count) import=$($bzaImports.Count)"}

foreach($src in @('cpf-admin/frontend/src','cpf-biz-admin/frontend/src')){
 $path=Join-Path $Root $src
 $external=Get-ChildItem $path -Recurse -File -Include *.vue,*.ts,*.css | Select-String -Pattern '@import\s+url\(|<link[^>]+https?://|src=["'']https?://' -AllMatches
 if($external){Fail "$src contains external runtime asset/CDN reference"}
}
$stale=@(
'cpf-admin/frontend/src/features/observability/AdmObservabilityPanels.vue','cpf-admin/frontend/src/features/platform/AdmPlatformPanels.vue','cpf-admin/frontend/src/features/business/AdmBusinessPanels.vue','cpf-admin/frontend/src/features/batch/AdmBatchPanels.vue','cpf-admin/frontend/src/features/access/AdmAccessPanels.vue',
'cpf-biz-admin/frontend/src/features/console.ts','cpf-biz-admin/frontend/src/features/directory/DirectoryPage.vue','cpf-biz-admin/frontend/src/features/access/AccessPage.vue','cpf-biz-admin/frontend/src/features/approval/ApprovalPage.vue','cpf-biz-admin/frontend/src/features/support/SupportPage.vue')
foreach($rel in $stale){if(Test-Path(Join-Path $Root $rel)){Fail "stale coarse frontend remains: $rel"}}
if($failures.Count){$failures|ForEach-Object{Write-Error $_};throw "Frontend feature coverage failed: $($failures.Count)"}
Write-Host "Frontend feature route coverage PASS. ADM=$($admRouteIds.Count), BZA=$($bzaIds.Count)"
