[CmdletBinding()]
param(
    [ValidateSet('dev','test','prod')][string]$Profile='dev',
    [string]$InstanceId='cpf-batch-control-plane-dev-1',
    [int]$Port=8180,
    [string]$JavaPath='',
    [string]$JarPath='',
    [string]$StateRoot='',
    [int]$ReadinessTimeoutSeconds=90
)
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
$Root=(Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path
if([string]::IsNullOrWhiteSpace($JavaPath)){
    $java=Get-Command java -ErrorAction Stop|Select-Object -First 1
    $JavaPath=$java.Source
}
if([string]::IsNullOrWhiteSpace($JarPath)){
    $candidates=@(Get-ChildItem -LiteralPath (Join-Path $Root 'cpf-batch/control-plane/build/libs') -Filter 'cpf-batch-control-plane-*.jar' -File -ErrorAction SilentlyContinue | Sort-Object LastWriteTime -Descending)
    if($candidates.Count-ne1){throw "Expected exactly one cpf-batch-control-plane executable JAR. Build :runtime:batch:control-plane:bootJar first or pass -JarPath. count=$($candidates.Count)"}
    $JarPath=$candidates[0].FullName
}
$JarPath=(Resolve-Path -LiteralPath $JarPath).Path
if([string]::IsNullOrWhiteSpace($StateRoot)){$StateRoot=Join-Path ([IO.Path]::GetTempPath()) 'cpf-batch-runtime'}
$stateDir=Join-Path $StateRoot 'control-plane'
[IO.Directory]::CreateDirectory($stateDir)|Out-Null
$pidFile=Join-Path $stateDir ($InstanceId+'.pid')
$metaFile=Join-Path $stateDir ($InstanceId+'.json')
if(Test-Path -LiteralPath $pidFile){
    $old=[int](Get-Content -LiteralPath $pidFile -Raw)
    if(Get-Process -Id $old -ErrorAction SilentlyContinue){throw "CPF batch control-plane already running. instance=$InstanceId pid=$old"}
    Remove-Item -LiteralPath $pidFile,$metaFile -Force -ErrorAction SilentlyContinue
}
$env:SPRING_PROFILES_ACTIVE=$Profile
$env:CPF_WAS_ID=$InstanceId
$env:CPF_PORT=[string]$Port
$psi=[Diagnostics.ProcessStartInfo]::new()
$psi.FileName=$JavaPath
$psi.UseShellExecute=$false
$psi.RedirectStandardOutput=$true
$psi.RedirectStandardError=$true
$psi.CreateNoWindow=$true
$psi.StandardOutputEncoding=[Text.UTF8Encoding]::new($false)
$psi.StandardErrorEncoding=[Text.UTF8Encoding]::new($false)
foreach($arg in @('-Dfile.encoding=UTF-8','-Dsun.jnu.encoding=UTF-8','-jar',$JarPath,"--spring.profiles.active=$Profile","--server.port=$Port","--cpf.framework.was-id=$InstanceId")){[void]$psi.ArgumentList.Add($arg)}
$p=[Diagnostics.Process]::new();$p.StartInfo=$psi;[void]$p.Start()
[IO.File]::WriteAllText($pidFile,[string]$p.Id,[Text.UTF8Encoding]::new($false))
@{pid=$p.Id;profile=$Profile;instance=$InstanceId;port=$Port;jar=$JarPath;startedAt=(Get-Date).ToString('o')}|ConvertTo-Json|Set-Content -LiteralPath $metaFile -Encoding utf8NoBOM
$deadline=(Get-Date).AddSeconds($ReadinessTimeoutSeconds)
$readiness="http://127.0.0.1:$Port/actuator/health/readiness"
$ready=$false
while((Get-Date)-lt$deadline){
    if($p.HasExited){break}
    try{$r=Invoke-WebRequest -UseBasicParsing -Uri $readiness -TimeoutSec 2;if($r.StatusCode-eq200){$ready=$true;break}}catch{}
    Start-Sleep -Milliseconds 500
}
if(-not$ready){
    try{if(-not$p.HasExited){$p.Kill($true)}}catch{}
    Remove-Item -LiteralPath $pidFile,$metaFile -Force -ErrorAction SilentlyContinue
    throw "CPF batch control-plane readiness failed. profile=$Profile instance=$InstanceId port=$Port pid=$($p.Id) readiness=$readiness"
}
Write-Host "CPF_BATCH_CONTROL_PLANE_READY profile=$Profile instance=$InstanceId port=$Port pid=$($p.Id) readiness=$readiness UTF-8"
