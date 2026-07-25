[CmdletBinding()]
param([string]$Root = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path)
$ErrorActionPreference='Stop'
$required = @(
 'cpf-common/src/main/java/com/cpf/common/cde/service/CodeCacheService.java',
 'cpf-common/src/main/java/com/cpf/common/cfg/service/ConfigCacheService.java',
 'cpf-common/src/main/java/com/cpf/common/msg/service/MessageCacheService.java',
 'cpf-common/src/main/java/com/cpf/common/msg/service/ResponseCodeCacheService.java',
 'cpf-common/src/main/java/com/cpf/common/calendar/CmnCalendarService.java',
 'cpf-core/src/main/java/com/cpf/core/api/page/CpfPage.java',
 'cpf-core/src/main/java/com/cpf/core/api/page/CpfCursorPage.java',
 'cpf-core/src/main/java/com/cpf/core/api/fixedlength/CpfFixedLengthLayoutRegistry.java'
)
$missing=@($required | Where-Object { !(Test-Path (Join-Path $Root $_)) })
if($missing){ throw "Common capability files missing:`n$($missing -join "`n")" }

# ADM은 다른 Owner의 DataSource/JdbcTemplate/TransactionManager를 구성하거나 직접 소비하지 않습니다.
$adm = Join-Path $Root 'cpf-admin\src\main\java'
if(Test-Path $adm){
    $patterns = @(
      '(?i)@Qualifier\("(bat|ref|mbr|acc|bza|exs)(DataSource|JdbcTemplate|TransactionManager)"',
      '(?i)\b(bat|ref|mbr|acc|bza|exs)(DataSource|JdbcTemplate|TransactionManager)\b',
      '(?i)spring\.datasource\.(bat|ref|mbr|acc|bza|exs)'
    )
    foreach($pattern in $patterns){
        $hits = Get-ChildItem $adm -Recurse -File -Filter '*.java' | Select-String -Pattern $pattern
        if($hits){ throw "ADM direct owner DB access detected:`n$($hits | ForEach-Object {"$($_.Path):$($_.LineNumber) $($_.Line.Trim())"} | Out-String)" }
    }
}

$ownerAdminRequired = @(
 'cpf-core/src/main/java/com/cpf/core/api/admin/CpfOwnerAdminOperationsPort.java',
 'cpf-core/src/main/java/com/cpf/core/api/admin/CpfOwnerAdminQuery.java',
 'cpf-core/src/main/java/com/cpf/core/api/admin/CpfOwnerAdminCommand.java',
 'cpf-member/src/main/java/com/cpf/member/operation/MbrOwnerAdminOperationsService.java',
 'cpf-member/src/main/java/com/cpf/member/operation/MbrAdminOperationsController.java',
 'cpf-admin/src/main/java/com/cpf/admin/opr/member/RemoteMbrOwnerAdminOperationsAdapter.java'
)
$missingOwnerAdmin=@($ownerAdminRequired | Where-Object { !(Test-Path (Join-Path $Root $_)) })
if($missingOwnerAdmin){ throw "Owner Admin Operations boundary files missing:`n$($missingOwnerAdmin -join "`n")" }

$staleAdmBat = @(
 'cpf-admin/src/main/java/com/cpf/admin/config/AdmBatchRepositoryConfig.java',
 'cpf-admin/src/main/java/com/cpf/admin/opr/service/CpfBatchScheduleService.java',
 'cpf-admin/src/main/java/com/cpf/admin/opr/service/CpfBatchExecutionTargetService.java',
 'cpf-admin/src/main/java/com/cpf/admin/opr/service/CpfBatchScheduler.java'
) | Where-Object { Test-Path (Join-Path $Root $_) }
if($staleAdmBat){ throw "ADM still owns BAT runtime artifacts:`n$($staleAdmBat -join "`n")" }


# Repository Root에는 과거 작업 ZIP의 안내/manifest 찌꺼기가 남아 있으면 안 됩니다.
$misplacedRootArtifacts = @(
  'APPLY_README.md','APPLY_CORRECTION.md','APPLY_BOUNDARY_FIX.md','APPLY_OWNER_DB_FIX.md',
  'MANIFEST.txt','MANIFEST.sha256','PATCH_MANIFEST.txt'
) | Where-Object { Test-Path (Join-Path $Root $_) }
if($misplacedRootArtifacts){ throw "Misplaced patch artifacts remain in repository root:`n$($misplacedRootArtifacts -join "`n")" }

# ADM의 Health/Download/Observability도 Owner DB를 직접 읽지 않고 public Port를 사용해야 합니다.
$ownerConsumerFiles = @(
  'cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmHealthController.java',
  'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmDownloadService.java',
  'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmObservabilityService.java'
)
foreach($rel in $ownerConsumerFiles){
  $path = Join-Path $Root $rel
  if(!(Test-Path $path)){ throw "Required ADM owner consumer missing: $rel" }
  $text = Get-Content $path -Raw
  if($text -match '(?i)(mbr|bat|ref)JdbcTemplate'){ throw "ADM owner consumer still injects owner JdbcTemplate: $rel" }
}
# Caches must expose refresh/invalidation mechanisms rather than immortal local maps.
$cacheFiles = @(
 'cpf-common/src/main/java/com/cpf/common/cde/service/CodeCacheService.java',
 'cpf-common/src/main/java/com/cpf/common/cfg/service/ConfigCacheService.java',
 'cpf-common/src/main/java/com/cpf/common/msg/service/MessageCacheService.java'
)
foreach($rel in $cacheFiles){
    $text = Get-Content (Join-Path $Root $rel) -Raw
    if($text -notmatch '(refresh|reload|clear|evict)'){ throw "No refresh/invalidation surface in $rel" }
}
Write-Host '[PASS] Common capability + Owner DB/Operations boundary baseline' -ForegroundColor Green
