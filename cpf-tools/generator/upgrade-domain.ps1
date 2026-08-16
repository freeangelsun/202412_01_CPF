[CmdletBinding()] param([Parameter(Mandatory=$true)][string]$Domain,[string]$Root='')
# Upgrade는 별도 Template를 사용하지 않고 같은 Engine의 regenerate 경로를 사용한다.
if([string]::IsNullOrWhiteSpace($Root)){$Root=(Resolve-Path "$PSScriptRoot\..\..").Path}; & (Join-Path $Root 'cpf-tools\runtime\cli\cpf.bat') domain upgrade $Domain
