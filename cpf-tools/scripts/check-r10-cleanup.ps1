param([string]$Root=(Resolve-Path "$PSScriptRoot\..\..").Path)
$ErrorActionPreference='Stop'
$Root=(Resolve-Path -LiteralPath $Root).Path
$errors=[System.Collections.Generic.List[string]]::new()

foreach($relative in @(
 'cpf-external',
 'cpf-tools/db/source',
 'docker-compose.local.yml',
 'cpf-core/src/main/java/com/cpf/core/common/batch',
 'cpf-core/src/main/java/com/cpf/core/config/CpfBatchAutoConfiguration.java',
 'cpf-core/src/main/java/com/cpf/core/config/CpfCenterCutAutoConfiguration.java',
 'cpf-biz-admin/frontend/src/features/console.ts',
 'cpf-biz-admin/frontend/src/features/directory',
 'cpf-biz-admin/frontend/src/features/access',
 'cpf-biz-admin/frontend/src/features/approval',
 'cpf-biz-admin/frontend/src/features/support',
 'cpf-admin/frontend/src/features/observability',
 'cpf-admin/frontend/src/features/platform',
 'cpf-admin/frontend/src/features/business',
 'cpf-admin/frontend/src/features/access',
 'cpf-admin/frontend/src/features/reference',
 'cpf-admin/src/main/java/com/cpf/admin/opr/dto/AdmBusinessDayRequest.java'
)){
    if(Test-Path(Join-Path $Root $relative)){$errors.Add("obsolete artifact: $relative")}
}
foreach($dir in @('logs','tmp','temp')){
    $path=Join-Path $Root $dir
    if(Test-Path $path){
        $tracked=@(& git -C $Root ls-files -- $dir 2>$null)
        if($tracked.Count -eq 0){$errors.Add("untracked root runtime directory: $dir")}
    }
}
$garbage=@(Get-ChildItem $Root -File -Force -ErrorAction SilentlyContinue |
    Where-Object {$_.Name -match '\.(zip|log|tmp|bak)$' -or $_.Name -match '^(APPLY|PATCH|EVIDENCE)_'} |
    Where-Object {$_.Name -ne 'README.md'})
if($garbage.Count){$errors.Add("root garbage file: " + (($garbage.Name|Sort-Object) -join ', '))}
if($errors.Count){$errors|ForEach-Object{Write-Host " - $_"};throw "R10 cleanup gate FAIL: $($errors.Count)건"}
Write-Host 'R10 repository cleanup gate PASS.'
