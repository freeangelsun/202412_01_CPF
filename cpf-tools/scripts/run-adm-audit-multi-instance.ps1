[CmdletBinding()]
param(
 [string]$Root=(Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path,
 [int]$PortA=18081,[int]$PortB=18082,
 [string]$LogDir='cpf-docs/evidence/runtime/audit-multi-instance'
)
$ErrorActionPreference='Stop'
function Need([string]$Name){$v=[Environment]::GetEnvironmentVariable($Name);if([string]::IsNullOrWhiteSpace($v)){throw "필수 환경변수 누락: $Name"};return $v}
$rootPath=(Resolve-Path -LiteralPath $Root).Path;$logRoot=Join-Path $rootPath $LogDir;New-Item -ItemType Directory -Force -Path $logRoot|Out-Null
$dbUrl=Need 'CPF_AUDIT_DB_URL';$dbUser=Need 'CPF_AUDIT_DB_USER';$dbPassword=Need 'CPF_AUDIT_DB_PASSWORD';$writePath=Need 'CPF_AUDIT_WRITE_PATH';$queryPath=Need 'CPF_AUDIT_QUERY_PATH'
$stamp=Get-Date -Format 'yyyyMMdd_HHmmss';$logA=Join-Path $logRoot "instanceA_$stamp.log";$logB=Join-Path $logRoot "instanceB_$stamp.log";$resultLog=Join-Path $logRoot "result_$stamp.json"
function Start-Adm([string]$Instance,[int]$Port,[string]$Log){
 $gradle=Join-Path $rootPath 'gradlew.bat'
 $gradleArgs=":cpf-admin:bootRun --no-daemon --args=--server.port=$Port --cpf.instance-id=$Instance --spring.datasource.url=$dbUrl --spring.datasource.username=$dbUser --spring.datasource.password=$dbPassword"
 $cmdArgs="/d /s /c `"`"$gradle`" $gradleArgs`""
 Start-Process -FilePath 'cmd.exe' -ArgumentList $cmdArgs -WorkingDirectory $rootPath -RedirectStandardOutput $Log -RedirectStandardError "$Log.err" -PassThru
}
function Await([int]$Port){for($i=0;$i -lt 90;$i++){try{Invoke-WebRequest -UseBasicParsing -Uri "http://127.0.0.1:$Port/actuator/health" -TimeoutSec 2|Out-Null;return}catch{Start-Sleep 1}};throw "ADM 기동 확인 실패 port=$Port"}
function Write-Audit([int]$Port,[string]$Execution,[int]$Index){
 $body=@{transactionId="CPF-AUDIT-$Execution-$Index";executionId=$Execution;action='R4_MULTI_INSTANCE';result='SUCCESS';secret='MASK_ME'}|ConvertTo-Json
 Invoke-RestMethod -Method Post -Uri "http://127.0.0.1:$Port$writePath" -ContentType 'application/json' -Body $body|Out-Null
}
$a=$null;$b=$null;$restart=$null
try{
 $a=Start-Adm 'R4-A' $PortA $logA;$b=Start-Adm 'R4-B' $PortB $logB;Await $PortA;Await $PortB
 1..50|ForEach-Object{Write-Audit $PortA 'A' $_;Write-Audit $PortB 'B' $_}
 Stop-Process -Id $a.Id -Force;Start-Sleep 2
 51..100|ForEach-Object{Write-Audit $PortB 'B' $_}
 $restart=Start-Adm 'R4-A-RESTART' $PortA $logA;Await $PortA
 101..120|ForEach-Object{Write-Audit $PortA 'A' $_}
 $records=Invoke-RestMethod -Method Get -Uri "http://127.0.0.1:$PortB$queryPath"
 $json=$records|ConvertTo-Json -Depth 20
 if($json -match 'MASK_ME'){throw 'Audit 응답에 Secret 원문 노출'}
 $items=@($records);$ids=@($items|ForEach-Object{$_.transactionId});$unique=@($ids|Sort-Object -Unique)
 if($ids.Count -ne $unique.Count){throw "Audit 중복 검출 count=$($ids.Count) unique=$($unique.Count)"}
 if($ids.Count -lt 220){throw "Audit 누락 검출 expected>=220 actual=$($ids.Count)"}
 [ordered]@{status='PASS';instanceAPid=$a.Id;instanceBPid=$b.Id;restartPid=$restart.Id;recordCount=$ids.Count;uniqueCount=$unique.Count;secretLeak=$false;sourceHead=(& git -C $rootPath rev-parse HEAD).Trim()}|ConvertTo-Json|Set-Content -LiteralPath $resultLog -Encoding utf8
 Get-Content -LiteralPath $resultLog
} finally {
 foreach($p in @($a,$b,$restart)){if($null-ne $p -and -not $p.HasExited){Stop-Process -Id $p.Id -Force -ErrorAction SilentlyContinue}}
}
