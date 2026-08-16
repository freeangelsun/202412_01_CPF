$ErrorActionPreference='Stop';$root=(Resolve-Path (Join-Path $PSScriptRoot '../../..')).Path;$errors=@()
$rules=@(
 @{Pattern='org\.springframework\.data\.redis|io\.lettuce\.core';Allowed='cpf-common[\\/]src[\\/]main[\\/]java[\\/]com[\\/]cpf[\\/]common[\\/]cache'},
 @{Pattern='org\.apache\.kafka|KafkaProducer|KafkaConsumer';Allowed='adapter|broker|mqe'},
 @{Pattern='java\.net\.http\.HttpClient|new RestTemplate\(';Allowed='http|servicecall|gateway|adapter'},
 @{Pattern='com\.jcraft\.jsch|org\.apache\.sshd';Allowed='filetransfer|sftp|adapter'} )
Get-ChildItem $root -Recurse -File -Filter *.java|?{$_.FullName -notmatch '[\\/](build|out|target)[\\/]'}|%{$rel=$_.FullName.Substring($root.Length+1);$text=Get-Content $_.FullName -Raw -Encoding UTF8;foreach($r in $rules){if($text -match $r.Pattern -and $rel -notmatch $r.Allowed){$errors+="$rel => $($r.Pattern)"}}}
if($errors){$errors|%{Write-Error "Direct client boundary 위반: $_"};exit 1};Write-Host '[PASS] Direct client boundary'
