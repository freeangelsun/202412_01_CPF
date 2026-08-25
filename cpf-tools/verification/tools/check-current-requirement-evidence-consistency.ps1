param(
    [string]$Root=(Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string]$ResultDir=(Join-Path (Resolve-Path "$PSScriptRoot\..\..\..").Path 'build/quality-gate')
)
$ErrorActionPreference='Stop'; Set-StrictMode -Version Latest
if($PSVersionTable.PSVersion.Major -lt 7){throw 'CPF Current Requirement/Evidence consistency gate requires pwsh 7+.'}
$Root=(Resolve-Path -LiteralPath $Root).Path
if(-not[IO.Path]::IsPathRooted($ResultDir)){$ResultDir=Join-Path $Root $ResultDir}; [IO.Directory]::CreateDirectory($ResultDir)|Out-Null
$python=(Get-Command python -ErrorAction Stop).Source
$stateTool=Join-Path $Root 'cpf-tools/verification/tools/cpf-source-state.py'
$stateJson=Join-Path $ResultDir 'current-source-identity.json'
& $python $stateTool --root $Root --scope source --summary-output $stateJson | Out-Null
if($LASTEXITCODE -ne 0){throw 'source identity computation failed'}
$source=Get-Content -Raw -LiteralPath $stateJson | ConvertFrom-Json
$required=@(
 'cpf-docs/work/current/CPF_REQUIREMENT_MASTER.csv',
 'cpf-docs/work/REQUIREMENT_STATUS.csv',
 'cpf-docs/work/current/CPF_DEVELOPMENT_QA_CLOSURE.csv',
 'cpf-docs/deliverables/TEST_AND_EVIDENCE.md',
 'cpf-docs/deliverables/OPEN_ISSUES.md'
)
$fail=[Collections.Generic.List[string]]::new(); foreach($rel in $required){if(-not(Test-Path -LiteralPath (Join-Path $Root $rel) -PathType Leaf)){$fail.Add("current input missing: $rel")|Out-Null}}
$master=@();$status=@();$qa=@()
if(Test-Path (Join-Path $Root $required[0])){$master=@(Import-Csv (Join-Path $Root $required[0]))}
if(Test-Path (Join-Path $Root $required[1])){$status=@(Import-Csv (Join-Path $Root $required[1]))}
if(Test-Path (Join-Path $Root $required[2])){$qa=@(Import-Csv (Join-Path $Root $required[2]))}
if($status.Count-ne208){$fail.Add("requirement projection rows must be 208 actual=$($status.Count)")|Out-Null}
if($qa.Count-ne63){$fail.Add("developer QA closure rows must be 63 actual=$($qa.Count)")|Out-Null}
$closed=@($qa|Where-Object{$_.closure_state-eq'CLOSED'});$blocked=@($qa|Where-Object{$_.closure_state-eq'BLOCKED_EXTERNAL'})
foreach($row in $qa){
 if($row.source_identity_sha256-ne$source.contentSha256){$fail.Add("stale finding source identity: $($row.finding_key)")|Out-Null}
 foreach($ev in (($row.evidence_paths -split ';')|Where-Object{-not[string]::IsNullOrWhiteSpace($_)})){if(-not(Test-Path -LiteralPath (Join-Path $Root $ev) -PathType Leaf)){$fail.Add("finding evidence missing: $($row.finding_key) -> $ev")|Out-Null}}
 if($row.closure_state-eq'BLOCKED_EXTERNAL' -and [string]::IsNullOrWhiteSpace($row.external_blocker)){$fail.Add("external blocker reason missing: $($row.finding_key)")|Out-Null}
}
$result=[ordered]@{generatedAt=[DateTimeOffset]::Now.ToString('o');sourceIdentitySha256=$source.contentSha256;status=if($fail.Count){'실패'}else{'완료'};requirementMasterIndexRows=$master.Count;requirementProjectionRows=$status.Count;developerFindingRows=$qa.Count;closedFindings=$closed.Count;blockedExternalFindings=$blocked.Count;failureCount=$fail.Count;failures=@($fail)}
$out=Join-Path $ResultDir 'current-requirement-evidence-consistency.json';[IO.File]::WriteAllText($out,($result|ConvertTo-Json -Depth 10),[Text.UTF8Encoding]::new($false))
if($fail.Count){$fail|%{Write-Host "FAIL $_"};exit 1}
Write-Host "[PASS] Current Requirement/Evidence consistency requirements=$($status.Count) findings=$($qa.Count) closed=$($closed.Count) blockedExternal=$($blocked.Count)"
