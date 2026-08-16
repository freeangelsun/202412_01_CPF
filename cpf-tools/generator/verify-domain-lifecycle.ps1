[CmdletBinding()] param([string]$Root='')
# Lifecycle Gate는 dry-run/diff/regenerate/remove/restore/user-change 보호를 한 번에 검증한다.
if([string]::IsNullOrWhiteSpace($Root)){$Root=(Resolve-Path "$PSScriptRoot\..\..").Path}
$py=(Get-Command python -ErrorAction SilentlyContinue); if($null -eq $py){$py=(Get-Command py -ErrorAction Stop); & $py.Source -3 (Join-Path $Root 'cpf-tools\verification\nxt3\cpf_nxt3_generator_gate.py') --root $Root}else{& $py.Source (Join-Path $Root 'cpf-tools\verification\nxt3\cpf_nxt3_generator_gate.py') --root $Root}
