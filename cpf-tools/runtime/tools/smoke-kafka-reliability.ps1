[CmdletBinding()]
param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string] $ResultDir = '',
    [string] $ContainerName = 'cpf-kafka',
    [string] $BootstrapServer = 'localhost:19092',
    [string] $SourceIdentity = $env:CPF_SOURCE_SHA,
    [switch] $RestartOwnedContainer
)

$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
$Utf8NoBom=[Text.UTF8Encoding]::new($false)
$Root=(Resolve-Path -LiteralPath $Root).Path
if([string]::IsNullOrWhiteSpace($SourceIdentity)){
    $stateTool=Join-Path $Root 'cpf-tools/verification/tools/cpf-source-state.py'
    $stateJson=@(& python $stateTool --root $Root --scope source 2>&1)
    if($LASTEXITCODE-ne0){throw "Cannot compute Git-independent source identity: $($stateJson -join [Environment]::NewLine)"}
    $SourceIdentity=[string](($stateJson|Select-Object -Last 1)|ConvertFrom-Json).contentSha1
}
$SourceIdentity=$SourceIdentity.Trim().ToLowerInvariant()
if($SourceIdentity-notmatch'^[0-9a-f]{40}$'){throw 'SourceIdentity must be a 40-hex Git-independent content identity.'}
if([string]::IsNullOrWhiteSpace($ResultDir)){$ResultDir=Join-Path $Root 'build/runtime-smoke'}
elseif(-not[IO.Path]::IsPathRooted($ResultDir)){$ResultDir=Join-Path $Root $ResultDir}
[IO.Directory]::CreateDirectory($ResultDir)|Out-Null
$resultPath=Join-Path $ResultDir 'kafka-reliability.sanitized.json'
$docker=(Get-Command docker -ErrorAction Stop | Select-Object -First 1).Source
$topic='cpf-local-reliability-'+[guid]::NewGuid().ToString('N')
$before='before-'+[guid]::NewGuid().ToString('N')
$after='after-'+[guid]::NewGuid().ToString('N')
$result=[ordered]@{
    startedAt=(Get-Date).ToString('o')
    status='FAILED'
    sourceIdentity=$SourceIdentity
    identityPolicy='GIT_INDEPENDENT_CONTENT_SHA1'
    container=$ContainerName
    topic=$topic
    produceConsume=[ordered]@{status='NOT_EXECUTED'}
    restart=[ordered]@{status='NOT_EXECUTED';performed=$false}
    postRestart=[ordered]@{status='NOT_EXECUTED'}
}

function Invoke-Docker([string[]]$Arguments,[string]$InputText=''){
    $psi=[Diagnostics.ProcessStartInfo]::new()
    $psi.FileName=$docker
    $psi.UseShellExecute=$false
    $psi.RedirectStandardOutput=$true
    $psi.RedirectStandardError=$true
    $psi.RedirectStandardInput=$true
    foreach($arg in $Arguments){[void]$psi.ArgumentList.Add($arg)}
    $p=[Diagnostics.Process]::new();$p.StartInfo=$psi;[void]$p.Start()
    if(-not[string]::IsNullOrEmpty($InputText)){$p.StandardInput.Write($InputText)}
    $p.StandardInput.Close()
    $stdout=$p.StandardOutput.ReadToEnd();$stderr=$p.StandardError.ReadToEnd();$p.WaitForExit()
    if($p.ExitCode-ne0){throw "docker $($Arguments -join ' ') failed rc=$($p.ExitCode): $stderr"}
    return $stdout.Trim()
}

function Wait-Kafka([int]$Seconds=180){
    $deadline=(Get-Date).AddSeconds($Seconds)
    do{
        try{
            [void](Invoke-Docker @('exec',$ContainerName,'/opt/kafka/bin/kafka-topics.sh','--bootstrap-server',$BootstrapServer,'--list'))
            return
        }catch{Start-Sleep -Seconds 2}
    }while((Get-Date)-lt$deadline)
    throw "Kafka did not become ready within ${Seconds}s"
}

function Consume-All([int]$MaxMessages){
    return Invoke-Docker @('exec',$ContainerName,'/opt/kafka/bin/kafka-console-consumer.sh','--bootstrap-server',$BootstrapServer,'--topic',$topic,'--from-beginning','--max-messages',[string]$MaxMessages,'--timeout-ms','15000')
}

try{
    $running=Invoke-Docker @('inspect','-f','{{.State.Running}}',$ContainerName)
    if($running-ne'true'){throw "Kafka container is not running: $ContainerName"}
    Wait-Kafka
    [void](Invoke-Docker @('exec',$ContainerName,'/opt/kafka/bin/kafka-topics.sh','--bootstrap-server',$BootstrapServer,'--create','--if-not-exists','--topic',$topic,'--partitions','1','--replication-factor','1'))
    [void](Invoke-Docker @('exec','-i',$ContainerName,'/opt/kafka/bin/kafka-console-producer.sh','--bootstrap-server',$BootstrapServer,'--topic',$topic) ($before+"`n"))
    $first=Consume-All 1
    if($first-notmatch[regex]::Escape($before)){throw 'Kafka produced message was not consumed'}
    $result.produceConsume=[ordered]@{status='DONE';messageObserved=$true}

    if($RestartOwnedContainer){
        [void](Invoke-Docker @('restart',$ContainerName))
        Wait-Kafka
        $result.restart=[ordered]@{status='DONE';performed=$true;ownedContainerOnly=$true}
    }else{
        $result.restart=[ordered]@{status='SKIPPED_EXISTING_CONTAINER';performed=$false;ownedContainerOnly=$true}
    }

    [void](Invoke-Docker @('exec','-i',$ContainerName,'/opt/kafka/bin/kafka-console-producer.sh','--bootstrap-server',$BootstrapServer,'--topic',$topic) ($after+"`n"))
    $messages=Consume-All 2
    if($messages-notmatch[regex]::Escape($before)){throw 'Pre-restart message disappeared'}
    if($messages-notmatch[regex]::Escape($after)){throw 'Post-restart message was not consumed'}
    $result.postRestart=[ordered]@{status='DONE';preRestartMessageObserved=$true;postRestartMessageObserved=$true}
    $result.status='DONE'
}finally{
    try{[void](Invoke-Docker @('exec',$ContainerName,'/opt/kafka/bin/kafka-topics.sh','--bootstrap-server',$BootstrapServer,'--delete','--topic',$topic))}catch{}
    $result.endedAt=(Get-Date).ToString('o')
    [IO.File]::WriteAllText($resultPath,($result|ConvertTo-Json -Depth 12)+"`n",$Utf8NoBom)
}
