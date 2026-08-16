param(
    [string]$Root=(Resolve-Path "$PSScriptRoot\..\..\..").Path,
    [string]$ResultDir=(Join-Path (Resolve-Path "$PSScriptRoot\..\..\..").Path 'build/quality-gate'),
    [string]$ExpectedSha=''
)
$ErrorActionPreference='Stop'; Set-StrictMode -Version Latest
$Root=(Resolve-Path -LiteralPath $Root).Path
if([string]::IsNullOrWhiteSpace($ExpectedSha)){$ExpectedSha=(& git -C $Root rev-parse HEAD).Trim()}
if($ExpectedSha -notmatch '^[0-9a-fA-F]{40}$'){throw "ExpectedSha must be a full SHA: $ExpectedSha"}
if(-not[IO.Path]::IsPathRooted($ResultDir)){$ResultDir=Join-Path $Root $ResultDir}; New-Item -ItemType Directory -Force $ResultDir|Out-Null

$required=@(
 'cpf-docs/work/current/CPF_REQUIREMENT_MASTER.csv',
 'cpf-docs/work/REQUIREMENT_STATUS.csv',
 'cpf-docs/work/QA_FINDING_REVALIDATION.csv',
 'cpf-docs/work/TEST_AND_EVIDENCE.md',
 'cpf-docs/work/OPEN_ISSUES.md',
 'cpf-docs/work/REVIEW_INDEX.md',
 'cpf-docs/work/current/CODEX_FINAL_VALIDATION_RESULT.md',
 'cpf-docs/work/current/CPF_CODEX_REVALIDATION_SCOPE.md'
)
$fail=[Collections.Generic.List[string]]::new()
foreach($rel in $required){if(-not(Test-Path -LiteralPath (Join-Path $Root $rel) -PathType Leaf)){$fail.Add("current input missing: $rel")|Out-Null}}

$master=@(); $status=@(); $qa=@()
if(Test-Path -LiteralPath (Join-Path $Root 'cpf-docs/work/current/CPF_REQUIREMENT_MASTER.csv')){$master=@(Import-Csv -LiteralPath (Join-Path $Root 'cpf-docs/work/current/CPF_REQUIREMENT_MASTER.csv'))}
if(Test-Path -LiteralPath (Join-Path $Root 'cpf-docs/work/REQUIREMENT_STATUS.csv')){$status=@(Import-Csv -LiteralPath (Join-Path $Root 'cpf-docs/work/REQUIREMENT_STATUS.csv'))}
if(Test-Path -LiteralPath (Join-Path $Root 'cpf-docs/work/QA_FINDING_REVALIDATION.csv')){$qa=@(Import-Csv -LiteralPath (Join-Path $Root 'cpf-docs/work/QA_FINDING_REVALIDATION.csv'))}

if($master.Count -eq 0){$fail.Add('CPF_REQUIREMENT_MASTER index is empty')|Out-Null}
else{
  $logical=@($master|ForEach-Object{[int64]$_.logical_record_count}|Select-Object -Unique)
  if($logical.Count-ne1 -or $logical[0]-le0){$fail.Add('CPF_REQUIREMENT_MASTER logical_record_count is inconsistent')|Out-Null}
  foreach($row in $master){
    $part=Join-Path $Root $row.part_path
    if(-not(Test-Path -LiteralPath $part -PathType Leaf)){$fail.Add("requirement part missing: $($row.part_path)")|Out-Null}
  }
}
if($status.Count-eq0){$fail.Add('REQUIREMENT_STATUS is empty')|Out-Null}
$badStatus=@($status|Where-Object{$_.'개발GPT_전체상태'-eq'실패'}); if($badStatus.Count){$fail.Add("developer failed requirements remain: $($badStatus.Count)")|Out-Null}
if($qa.Count-eq0){$fail.Add('QA_FINDING_REVALIDATION is empty')|Out-Null}
foreach($row in $qa){
  foreach($ev in (($row.evidence_paths -split ';')|Where-Object{-not [string]::IsNullOrWhiteSpace($_)})){
    if($ev -match '^(N/A|UNVERIFIED|NONE)$'){continue}
    $p=Join-Path $Root $ev
    if(-not(Test-Path -LiteralPath $p -PathType Leaf)){$fail.Add("QA finding evidence missing: $($row.finding_id) -> $ev")|Out-Null}
  }
}
$result=[ordered]@{
  generatedAt=[DateTimeOffset]::Now.ToString('o'); sourceSha=$ExpectedSha;
  status=if($fail.Count){'실패'}else{'완료'};
  requirementMasterIndexRows=$master.Count;
  logicalRequirementCount=if($master.Count){[int64]$master[0].logical_record_count}else{0};
  requirementProjectionRows=$status.Count; qaFindingRows=$qa.Count;
  failureCount=$fail.Count; failures=@($fail)
}
$out=Join-Path $ResultDir 'current-requirement-evidence-consistency.json'; [IO.File]::WriteAllText($out,($result|ConvertTo-Json -Depth 10),[Text.UTF8Encoding]::new($false))
if($fail.Count){$fail|ForEach-Object{Write-Host "FAIL $_"}; exit 1}
Write-Host "[PASS] Current Requirement/Evidence consistency. logical=$($result.logicalRequirementCount) projection=$($status.Count) qa=$($qa.Count)"
