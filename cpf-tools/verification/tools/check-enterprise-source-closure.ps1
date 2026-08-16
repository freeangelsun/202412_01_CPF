$ErrorActionPreference='Stop';$root=(Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
$required=@(
'cpf-starters/data/src/main/java/com/cpf/data/cache/api/CpfCachePort.java','cpf-starters/data/cache/redis/src/main/java/com/cpf/data/cache/redis/RedisCpfCachePort.java',
'cpf-starters/file/src/main/java/com/cpf/file/tabular/api/CpfTabularReader.java','cpf-admin/src/main/java/com/cpf/admin/opr/filejob/AdmFileJobController.java',
'cpf-biz-admin/frontend/src/components/CpfTreeNode.vue','cpf-biz-admin/src/main/java/com/cpf/bizadmin/auth/permission/BzaPermissionManifest.java',
'cpf-tools/runtime/cpf-local-runtime/src/main/java/com/cpf/local/runtime/CpfLocalRuntimeSafetyGuard.java','cpf-tools/runtime/cpf-local-batch-runtime/src/main/java/com/cpf/local/batch/CpfLocalBatchRuntimeSafetyGuard.java',
'cpf-tools/db/vendor/mariadb/source/migration/flyway/V69__enterprise_cache_file_job.sql',
'cpf-tools/db/vendor/postgresql/migration/flyway/cpfDB/V69__enterprise_cache_invalidation.sql',
'cpf-tools/db/vendor/postgresql/migration/flyway/admDB/V69__adm_async_file_job.sql',
'cpf-tools/db/vendor/oracle/migration/flyway/cpfDB/V69__enterprise_cache_invalidation.sql',
'cpf-tools/db/vendor/oracle/migration/flyway/admDB/V69__adm_async_file_job.sql')
$missing=$required|?{-not(Test-Path (Join-Path $root $_))};if($missing){$missing|%{Write-Error "필수 구현 누락: $_"};exit 1}
$forbidden=Get-ChildItem $root -Recurse -File|?{
    $relative=[IO.Path]::GetRelativePath($root,$_.FullName).Replace('\','/')
    $generatedDirectory=$relative -match '(^|/)(out|target|node_modules|\.gradle)(/|$)'
    $generatedBuild=$relative -match '(^|/)build/' -and -not $relative.StartsWith('cpf-tools/build/')
    $generatedDirectory -or $generatedBuild -or $_.Name -match '\.(log|tmp|bak|orig)$'
};if($forbidden){$forbidden|%{Write-Error "Repository hygiene 위반: $($_.FullName)"};exit 1}
Write-Host '[PASS] CPF current source closure'
