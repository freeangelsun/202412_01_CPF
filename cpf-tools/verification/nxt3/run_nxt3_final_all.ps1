# 모든 Gate를 끝까지 실행해 실패를 한 번에 수집하는 Windows thin launcher입니다.
param([string]$RepositoryRoot='.',[switch]$IncludeGradle)
$root=[IO.Path]::GetFullPath($RepositoryRoot)
$py=(Get-Command python -ErrorAction SilentlyContinue)
if($null -eq $py){$py=(Get-Command py -ErrorAction Stop); $currentizeArgs=@('-3',(Join-Path $root 'cpf-tools\verification\nxt3\run_nxt3_final_all.py'),'--root',$root)}else{$currentizeArgs=@((Join-Path $root 'cpf-tools\verification\nxt3\run_nxt3_final_all.py'),'--root',$root)}
if($IncludeGradle){$currentizeArgs += '--include-gradle'}
& $py.Source @currentizeArgs
