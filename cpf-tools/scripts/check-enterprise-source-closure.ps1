$ErrorActionPreference='Stop';$root=(Resolve-Path (Join-Path $PSScriptRoot '../..')).Path
$required=@(
'cpf-core/src/main/java/com/cpf/core/api/cache/CpfCachePort.java','cpf-common/src/main/java/com/cpf/common/cache/CpfRedisCacheProvider.java',
'cpf-core/src/main/java/com/cpf/core/api/tabular/CpfTabularReader.java','cpf-admin/src/main/java/com/cpf/admin/opr/filejob/AdmFileJobController.java',
'cpf-biz-admin/frontend/src/components/CpfTreeNode.vue','cpf-biz-admin/src/main/java/com/cpf/bizadmin/auth/permission/BzaPermissionManifest.java',
'cpf-tools/runtime/cpf-local-runtime/src/main/java/com/cpf/local/runtime/CpfLocalRuntimeSafetyGuard.java','cpf-tools/runtime/cpf-local-batch-runtime/src/main/java/com/cpf/local/batch/CpfLocalBatchRuntimeSafetyGuard.java',
'cpf-tools/db/vendor/mariadb/source/migration/flyway/V69__enterprise_cache_file_job.sql','cpf-tools/db/vendor/postgresql/source/migration/flyway/V69__enterprise_cache_file_job.sql','cpf-tools/db/vendor/oracle/source/migration/flyway/V69__enterprise_cache_file_job.sql')
$missing=$required|?{-not(Test-Path (Join-Path $root $_))};if($missing){$missing|%{Write-Error "필수 구현 누락: $_"};exit 1}
$forbidden=Get-ChildItem $root -Recurse -File|?{$_.FullName -match '[\\/](build|out|target|node_modules|\.gradle)[\\/]' -or $_.Name -match '\.(log|tmp|bak|orig)$'};if($forbidden){$forbidden|%{Write-Error "Repository hygiene 위반: $($_.FullName)"};exit 1}
Write-Host '[PASS] Enterprise source closure'
