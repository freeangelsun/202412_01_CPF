param([string]$EvidenceRoot='cpf-tools/verification')
$ErrorActionPreference='Stop';$root=(Resolve-Path (Join-Path $PSScriptRoot '../..')).Path;$path=Join-Path $root $EvidenceRoot
$required=@('baseline_sha','command','environment','started_at','finished_at','result','redaction');$errors=@()
if(Test-Path $path){Get-ChildItem $path -Recurse -File -Include *evidence*.md,*evidence*.json,VALIDATION_LEDGER.md|%{$text=Get-Content $_.FullName -Raw -Encoding UTF8;if($text.Trim() -match '^(PASS|SUCCESS|OK)$'){$errors+="one-line evidence: $($_.FullName)";return};foreach($key in $required){if($text -notmatch "(?i)$key|"+(@{baseline_sha='기준 SHA';command='실행 명령';environment='환경';started_at='시작';finished_at='종료';result='결과';redaction='민감정보'}[$key])){$errors+="missing $key: $($_.FullName)"}}}}
if($errors){$errors|%{Write-Error $_};exit 1};Write-Host '[PASS] Evidence metadata contract'
