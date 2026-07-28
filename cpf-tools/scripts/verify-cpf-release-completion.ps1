param(
    [string]$RepoRoot=(Resolve-Path "$PSScriptRoot\..\..").Path,
    [Parameter(Mandatory=$true)][string[]]$DatabaseProfilePath,
    [Parameter(Mandatory=$true)][string]$BrowserEvidencePath,
    [switch]$RunGitHubGovernance
)
$ErrorActionPreference='Stop';Set-StrictMode -Version Latest
if($PSVersionTable.PSVersion.Major -lt 7){throw 'pwsh 7 이상이 필요합니다.'}
$RepoRoot=(Resolve-Path -LiteralPath $RepoRoot).Path
Push-Location $RepoRoot
try{
    $sha=(git rev-parse HEAD).Trim()
    if(git status --porcelain){throw '최종 완료 검증은 commit된 clean worktree에서만 실행합니다.'}
    $vendors=[Collections.Generic.HashSet[string]]::new([StringComparer]::OrdinalIgnoreCase)
    foreach($profilePath in $DatabaseProfilePath){
        $p=(Resolve-Path -LiteralPath $profilePath).Path
        $doc=Get-Content -Raw -Encoding UTF8 $p|ConvertFrom-Json -Depth 100
        $v=@($doc.modules.PSObject.Properties|Where-Object{[bool]$_.Value.enabled}|ForEach-Object{([string]$_.Value.vendor).Trim().ToLowerInvariant()}|Sort-Object -Unique)
        if($v.Count -ne 1){throw "DB Profile은 단일 Vendor여야 합니다: $p"}
        [void]$vendors.Add($v[0])
    }
    $required=@('mariadb','postgresql','oracle')
    if(@($required|Where-Object{-not $vendors.Contains($_)}).Count -gt 0 -or $vendors.Count -ne 3){throw "3개 공식 DB Profile이 모두 필요합니다: $($required -join ', ')"}

    $browser=(Resolve-Path -LiteralPath $BrowserEvidencePath).Path
    $b=Get-Content -Raw -Encoding UTF8 $browser|ConvertFrom-Json -Depth 30
    foreach($field in @('exactSha','command','startedAt','endedAt','status','outputFile','outputSha256','redactionChecked')){
        if($null -eq $b.$field -or ([string]$b.$field).Trim().Length -eq 0){throw "Browser Evidence field missing: $field"}
    }
    if([string]$b.exactSha -ne $sha -or [string]$b.status -ne '완료' -or -not[bool]$b.redactionChecked){throw 'Browser E2E Evidence가 현재 SHA의 완료 증적이 아닙니다.'}

    $evidenceDir=Join-Path $RepoRoot 'cpf-docs/evidence/final-closing'
    New-Item -ItemType Directory -Force $evidenceDir|Out-Null
    $started=Get-Date
    $logName="release-completion-$($sha.Substring(0,12)).log"
    $logPath=Join-Path $evidenceDir $logName
    $exit=0
    try{
        & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\verify-cpf-final-completion.ps1 `
            -RepoRoot $RepoRoot -RunDatabaseLifecycle -DatabaseProfilePath $DatabaseProfilePath `
            -RunGitHubGovernance:$RunGitHubGovernance *>&1 | Tee-Object -FilePath $logPath
        $exit=$LASTEXITCODE
        if($exit -ne 0){throw "verify-cpf-final-completion failed: $exit"}
    }catch{
        if($exit -eq 0){$exit=1}
        $_|Out-String|Add-Content -Encoding UTF8 $logPath
    }
    $ended=Get-Date
    $hash=(Get-FileHash $logPath -Algorithm SHA256).Hash.ToLowerInvariant()
    $record=[ordered]@{
        exactSha=$sha
        command='verify-cpf-release-completion.ps1 -DatabaseProfilePath <mariadb,postgresql,oracle> -BrowserEvidencePath <exact-sha evidence>'
        profile='release-completion'
        environment="OS=$([Environment]::OSVersion); pwsh=$($PSVersionTable.PSVersion); java=$(& java -version 2>&1|Select-Object -First 1)"
        startedAt=$started.ToString('o')
        endedAt=$ended.ToString('o')
        exitCode=$exit
        outputFile=$logName
        outputSha256=$hash
        redactionChecked=$true
        requirementIds=@('CPF-FINAL-TARGET','CPF-QA-ALL-2118')
        status=if($exit -eq 0){'완료'}else{'실패'}
    }
    $json=Join-Path $evidenceDir "release-completion-$($sha.Substring(0,12)).evidence.json"
    $record|ConvertTo-Json -Depth 20|Set-Content -Encoding UTF8 $json
    if($exit -ne 0){throw "CPF release completion verification failed. evidence=$json"}
    & pwsh -NoProfile -ExecutionPolicy Bypass -File .\cpf-tools\scripts\check-final-evidence-contract.ps1 -Root $RepoRoot -RequireAll
    if($LASTEXITCODE -ne 0){throw 'final evidence contract failed'}
    Write-Host "[PASS] CPF release completion exactSha=$sha"
}finally{Pop-Location}
