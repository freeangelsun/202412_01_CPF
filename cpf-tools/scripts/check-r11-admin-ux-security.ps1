[CmdletBinding()]
param([string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path)
$ErrorActionPreference='Stop'
$failures=[System.Collections.Generic.List[string]]::new()
function Fail([string]$m){$failures.Add($m)|Out-Null;Write-Host "[FAIL] $m" -ForegroundColor Red}
function Pass([string]$m){Write-Host "[PASS] $m" -ForegroundColor Green}
function Require([string]$rel){if(!(Test-Path (Join-Path $Root $rel))){Fail "Missing: $rel"}}

@(
 'cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmPermissionController.java',
 'cpf-admin/frontend/src/app/admConsoleMixin.ts',
 'cpf-admin/frontend/src/features/core/methods.ts',
 'cpf-admin/frontend/src/components/CpfCodeSelect.vue',
 'cpf-admin/frontend/src/components/CpfDateRange.vue',
 'cpf-biz-admin/frontend/src/features/auth/session.ts',
 'cpf-biz-admin/frontend/src/App.vue',
 'cpf-biz-admin/frontend/src/components/CpfCodeSelect.vue',
 'cpf-biz-admin/frontend/src/components/CpfDateRange.vue'
) | ForEach-Object { Require $_ }

$admMixin=Join-Path $Root 'cpf-admin/frontend/src/app/admConsoleMixin.ts'
if(Test-Path $admMixin){
  $text=Get-Content $admMixin -Raw
  if($text -match 'if\s*\(\s*!authorizedMenus\.length\s*\)\s*return\s+menus'){Fail 'ADM menu authorization is fail-open when authorizedMenus is empty.'}
  elseif($text -notmatch 'permissionsLoaded'){Fail 'ADM does not distinguish permission-loading state from an explicit zero-menu result.'}
  else{Pass 'ADM menu authorization is fail-closed after permission loading'}
}
$admMethods=Join-Path $Root 'cpf-admin/frontend/src/features/core/methods.ts'
if(Test-Path $admMethods){
  $text=Get-Content $admMethods -Raw
  if($text -notmatch 'permissionsLoaded\s*=\s*true'){Fail 'ADM /auth/me success path does not mark permissions loaded.'}
  else{Pass 'ADM auth/me permission loading state'}
}

$bzaSession=Join-Path $Root 'cpf-biz-admin/frontend/src/features/auth/session.ts'
$bzaApp=Join-Path $Root 'cpf-biz-admin/frontend/src/App.vue'
if((Test-Path $bzaSession) -and (Test-Path $bzaApp)){
  $session=Get-Content $bzaSession -Raw; $app=Get-Content $bzaApp -Raw
  if($session -notmatch 'function\s+hasBzaMenu' -or $session -notmatch 'function\s+hasBzaPermission'){Fail 'BZA menu/button permission contract is missing.'}
  elseif($app -notmatch 'bzaRoutes\.filter\(route=>hasBzaMenu\(route\.menuCode\)\)'){Fail 'BZA route navigation is not filtered by menu authorization.'}
  else{Pass 'BZA route/menu and button permission contract'}
}

foreach($rel in @('cpf-admin/frontend/src/components/CpfCodeSelect.vue','cpf-biz-admin/frontend/src/components/CpfCodeSelect.vue')){
 if(Test-Path (Join-Path $Root $rel)){
   $txt=Get-Content (Join-Path $Root $rel) -Raw
   if($txt -notmatch '<select'){Fail "$rel is not a select component"}
 }
}
foreach($rel in @('cpf-admin/frontend/src/components/CpfDateRange.vue','cpf-biz-admin/frontend/src/components/CpfDateRange.vue')){
 if(Test-Path (Join-Path $Root $rel)){
   $txt=Get-Content (Join-Path $Root $rel) -Raw
   if($txt -notmatch 'type="date"'){Fail "$rel does not provide date inputs"}
 }
}
if($failures.Count){throw "CPF R11 ADM/BZA UX/security gate failed ($($failures.Count))."}
Pass 'CPF R11 ADM/BZA UX/security gate completed'
