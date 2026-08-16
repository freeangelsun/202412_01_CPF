[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)][string]$Domain,
    [string]$Root='',
    [switch]$Apply,
    [switch]$PurgeDefinition
)
# 기본 remove는 생성기 소유 Source만 안전하게 제거합니다.
# -PurgeDefinition은 "이 Domain을 더 이상 사용하지 않음"을 명시한 경우에만 정본 정의까지 제거합니다.
if([string]::IsNullOrWhiteSpace($Root)){$Root=(Resolve-Path "$PSScriptRoot\..\..").Path}
$cmd=@('domain','remove',$Domain)
if($Apply){$cmd+='--apply'}
if($PurgeDefinition){$cmd+='--purge-definition'}
& (Join-Path $Root 'cpf-tools\runtime\cli\cpf.bat') @cmd
