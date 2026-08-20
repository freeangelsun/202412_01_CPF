[CmdletBinding()]
param(
    [string]$RepoRoot = (Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string]$OutputRoot = (Join-Path $HOME 'Downloads'),
    [string]$JavaHome = '',
    [switch]$SkipDocker,
    [switch]$SkipFrontend,
    [switch]$SkipBrowserE2E
)
$ErrorActionPreference='Stop'
Set-StrictMode -Version Latest
$RepoRoot=(Resolve-Path -LiteralPath $RepoRoot).Path
Set-Location $RepoRoot
$stamp=Get-Date -Format 'yyyyMMdd_HHmmss'
$preDir=Join-Path $OutputRoot "CPF_FINAL_PRECHECK_$stamp"
New-Item -ItemType Directory -Force -Path $preDir | Out-Null
$manifest=Join-Path $RepoRoot 'cpf-docs\deliverables\DELETE_MANIFEST.csv'
if(-not (Test-Path -LiteralPath $manifest -PathType Leaf)){ throw "DELETE_MANIFEST missing: $manifest" }
$remaining=[Collections.Generic.List[string]]::new()
Import-Csv -LiteralPath $manifest -Encoding UTF8 | Where-Object {([string]$_.approved).Trim().ToLowerInvariant() -in @('true','1','yes','y')} | ForEach-Object {
    $rel=([string]$_.path).Trim().Replace('\','/')
    if($rel){
        $p=Join-Path $RepoRoot $rel
        if(Test-Path -LiteralPath $p){$remaining.Add($rel)}
    }
}
if($remaining.Count -gt 0){
    $remaining | Set-Content -LiteralPath (Join-Path $preDir 'DELETE_NOT_APPLIED.txt') -Encoding UTF8
    throw "Delete Manifest가 아직 적용되지 않았습니다. 남은 경로=$($remaining.Count). 먼저 최종 삭제 한 줄을 실행하세요."
}
$python=(Get-Command python -ErrorAction Stop).Source
$staticLog=Join-Path $preDir 'FINAL_STATIC_GATE.log'
& $python '.\cpf-tools\verification\tools\verify-cpf-current-final.py' 2>&1 | Tee-Object -FilePath $staticLog
if($LASTEXITCODE -ne 0){ throw "FINAL_STATIC_GATE failed. log=$staticLog" }
$full=Join-Path $RepoRoot 'cpf-tools\verification\tools\run-cpf-local-full-validation.ps1'
$args=@('-NoProfile','-ExecutionPolicy','Bypass','-File',$full,'-RepoRoot',$RepoRoot,'-OutputRoot',$OutputRoot,'-FullLocal','-StrictExit')
if($JavaHome){$args+=@('-JavaHome',$JavaHome)}
if($SkipDocker){$args+='-SkipDocker'}
if($SkipFrontend){$args+='-SkipFrontend'}
if($SkipBrowserE2E){
    # FullLocal은 Browser E2E를 켜므로 Browser만 건너뛰고 싶으면 기존 orchestrator의 One-WAS/Frontend 결과를 유지하되 E2E는 환경상 SKIP으로 남긴다.
    $env:CPF_SKIP_BROWSER_E2E='true'
}
$pwsh=(Get-Command pwsh -ErrorAction SilentlyContinue | Select-Object -First 1)
if($pwsh){ & $pwsh.Source @args } else { & powershell @args }
$rc=$LASTEXITCODE
"FINAL_LOCAL_VALIDATION_EXIT_CODE=$rc" | Set-Content -LiteralPath (Join-Path $preDir 'RESULT.txt') -Encoding UTF8
if($rc -ne 0){ throw "CPF final local validation failed. Downloads의 최신 CPF_LOCAL_VALIDATION_* 결과를 확인하세요." }
Write-Host "CPF FINAL LOCAL VALIDATION PASS" -ForegroundColor Green
