[CmdletBinding()]
param([string]$InstanceId='cpf-batch-worker-dev-1',[string]$StateRoot='',[int]$TimeoutSeconds=20)
$ErrorActionPreference='Stop';Set-StrictMode -Version Latest
if([string]::IsNullOrWhiteSpace($StateRoot)){$StateRoot=Join-Path ([IO.Path]::GetTempPath()) 'cpf-batch-runtime'}
$stateDir=Join-Path $StateRoot 'worker';$pidFile=Join-Path $stateDir ($InstanceId+'.pid');$metaFile=Join-Path $stateDir ($InstanceId+'.json')
if(-not(Test-Path -LiteralPath $pidFile -PathType Leaf)){Write-Host 'CPF_BATCH_WORKER_STOP already-stopped';return}
$raw=(Get-Content -LiteralPath $pidFile -Raw).Trim();if($raw-notmatch '^\d+$'){throw 'Invalid pid file'};$pidValue=[int]$raw
$p=Get-Process -Id $pidValue -ErrorAction SilentlyContinue
if($p){
    try{$p.CloseMainWindow()|Out-Null}catch{}
    try{if(-not$p.WaitForExit($TimeoutSeconds*1000)){$p.Kill($true);$p.WaitForExit()}}catch{if(Get-Process -Id $pidValue -ErrorAction SilentlyContinue){throw}}
}
Remove-Item -LiteralPath $pidFile,$metaFile -Force -ErrorAction SilentlyContinue
Write-Host 'CPF_BATCH_WORKER_STOP=PASS'
