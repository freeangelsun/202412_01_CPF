param([string]$Root = (Resolve-Path "$PSScriptRoot\..\..").Path)
$ErrorActionPreference='Stop'
$failures=[System.Collections.Generic.List[string]]::new()
function Fail([string]$m){$failures.Add($m)}
$files=@(
 'cpf-admin/src/main/java/com/cpf/admin/approval/controller/AdmApprovalController.java',
 'cpf-biz-admin/src/main/java/com/cpf/bizadmin/approval/controller/BzaApprovalPolicyController.java',
 'cpf-biz-admin/src/main/java/com/cpf/bizadmin/directory/controller/BzaDirectoryController.java',
 'cpf-biz-admin/src/main/java/com/cpf/bizadmin/sample/sequence/BzaSequenceSampleController.java'
)
foreach($rel in $files){
 $p=Join-Path $Root $rel
 if(-not(Test-Path $p)){Fail "approval/security controller missing: $rel";continue}
 $t=Get-Content $p -Raw
 if($t -match '@RequestAttribute\([^\)]*(?:adm|bza)\.operatorId[^\)]*required\s*=\s*false'){Fail "operator identity is optional: $rel"}
 if($t -match 'operatorId\s*==\s*null\s*\?\s*"SYSTEM"'){Fail "dangerous SYSTEM fallback remains: $rel"}
}
$adm=Join-Path $Root 'cpf-admin/src/main/java/com/cpf/admin/approval/controller/AdmApprovalController.java'
if(Test-Path $adm){
 $t=Get-Content $adm -Raw
 if($t -notmatch '@RequestAttribute\("adm\.operatorId"\)'){Fail 'ADM Approval mutation API must require adm.operatorId.'}
}
$bzaAuth=Join-Path $Root 'cpf-biz-admin/src/main/java/com/cpf/bizadmin/auth/repository/BzaAuthRepository.java'
if(Test-Path $bzaAuth){
 $t=Get-Content $bzaAuth -Raw
 if($t -notmatch 'bza_user_role'){Fail 'BZA authentication does not consume effective multi-role mappings.'}
}
if($failures.Count){$failures|ForEach-Object{Write-Error $_};throw "R8 approval security gate failed: $($failures.Count)"}
Write-Host 'R8 approval/security gate PASS.'
