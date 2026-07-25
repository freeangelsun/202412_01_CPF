param([string]$Root = (Resolve-Path "$PSScriptRoot\..\..").Path)
$ErrorActionPreference = 'Stop'
$failures = [System.Collections.Generic.List[string]]::new()
function Fail([string]$m) { $failures.Add($m) }

$mustNotExist = @(
    'docker-compose.local.yml',
    'cpf-tools/db/source',
    'cpf-biz-admin/frontend/src/features/console.ts',
    'cpf-admin/frontend/src/features/observability/AdmObservabilityPanels.vue',
    'cpf-admin/frontend/src/features/platform/AdmPlatformPanels.vue',
    'cpf-admin/frontend/src/features/business/AdmBusinessPanels.vue',
    'cpf-admin/frontend/src/features/batch/AdmBatchPanels.vue',
    'cpf-admin/frontend/src/features/access/AdmAccessPanels.vue',
    'cpf-admin/frontend/src/features/members/MembersPage.vue',
    'cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmMemberController.java',
    'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmMemberOperationService.java',
    'cpf-biz-admin/frontend/src/features/directory/DirectoryPage.vue',
    'cpf-biz-admin/frontend/src/features/access/AccessPage.vue',
    'cpf-biz-admin/frontend/src/features/approval/ApprovalPage.vue',
    'cpf-biz-admin/frontend/src/features/support/SupportPage.vue',
    'cpf-admin/src/main/java/com/cpf/admin/opr/service/CpfBatchScheduler.java',
    'cpf-admin/src/main/java/com/cpf/admin/opr/service/CpfBatchScheduleService.java',
    'cpf-admin/src/main/java/com/cpf/admin/opr/service/CpfBatchExecutionTargetService.java',
    'cpf-admin/src/main/java/com/cpf/admin/opr/dto/CpfBatchScheduleCandidate.java'
)
foreach ($rel in $mustNotExist) {
    if (Test-Path (Join-Path $Root $rel)) { Fail "stale artifact remains: $rel" }
}

$required = @(
    'deploy/local/docker-compose.local.yml',
    'cpf-tools/db/vendor/mariadb/source/10_cpf_schema.sql',
    'cpf-tools/db/vendor/mariadb/source/35_bat_schema.sql',
    'cpf-tools/db/vendor/mariadb/source/40_business_modules_schema.sql',
    'cpf-admin/frontend/src/app/routes.ts',
    'cpf-biz-admin/frontend/src/app/routes.ts',
    'cpf-batch/src/main/java/com/cpf/batch/runtime/BatBatchLauncher.java',
    'cpf-batch/src/main/java/com/cpf/batch/runtime/centercut/BatCenterCutRunner.java'
)
foreach ($rel in $required) {
    if (-not (Test-Path (Join-Path $Root $rel))) { Fail "required canonical artifact missing: $rel" }
}

$rootLog = Join-Path $Root 'logs'
if (Test-Path $rootLog) {
    $trackedLike = Get-ChildItem $rootLog -Recurse -File -ErrorAction SilentlyContinue | Where-Object { $_.Name -notin @('.gitkeep') }
    if ($trackedLike) { Fail 'root logs/ contains runtime residue; logs belong outside repository root.' }
}

if ($failures.Count -gt 0) {
    $failures | ForEach-Object { Write-Error $_ }
    throw "R8 repository cleanup gate failed: $($failures.Count) issue(s)."
}
Write-Host 'R8 repository cleanup gate PASS.'
