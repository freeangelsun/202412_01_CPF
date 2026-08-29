[CmdletBinding()] param([string]$Root='')
# 두 공식 Root와 임의 Domain 검증을 동일 Verification Entry로 연결한다.
if([string]::IsNullOrWhiteSpace($Root)){$Root=(Resolve-Path "$PSScriptRoot\..\..").Path}; & (Join-Path $Root 'cpf-tools\runtime\cli\cpf.cmd') verify all
