param(
    [string] $Root = (Resolve-Path "$PSScriptRoot\..\..").Path,
    [string] $ResultDir = (Join-Path (Resolve-Path "$PSScriptRoot\..\..").Path "build/quality-gate"),
    [string] $ExpectedSha = ''
)

$Utf8 = [System.Text.UTF8Encoding]::new($false)
[Console]::InputEncoding = $Utf8
[Console]::OutputEncoding = $Utf8
$OutputEncoding = $Utf8
$ErrorActionPreference = 'Stop'
Set-StrictMode -Version Latest
$Root = (Resolve-Path -LiteralPath $Root).Path
if ([string]::IsNullOrWhiteSpace($ExpectedSha)) { $ExpectedSha = (& git -C $Root rev-parse HEAD).Trim() }
if ($ExpectedSha -notmatch '^[0-9a-fA-F]{40}$') { throw "ExpectedSha must be a full SHA: $ExpectedSha" }
$ExpectedSha = $ExpectedSha.ToLowerInvariant()
if (-not [System.IO.Path]::IsPathRooted($ResultDir)) { $ResultDir = Join-Path $Root $ResultDir }
New-Item -ItemType Directory -Force -Path $ResultDir | Out-Null

$targetPath = Join-Path $Root 'cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md'
$ledgerPath = Join-Path $Root 'cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md'
$reviewPath = Join-Path $Root 'cpf-docs/work/review/20260724_02/CPF_MASTER_REQUIREMENT_AND_SOURCE_REVIEW.md'
$evidenceIndexPath = Join-Path $Root 'cpf-docs/evidence/CPF_EVIDENCE_INDEX.md'
$currentEvidenceRoot = Join-Path $Root 'cpf-docs/evidence/current'
$fullQaLedgerPath = Join-Path $Root 'cpf-tools/verification/20260729_02/CPF_FINAL_QA_MASTER_LEDGER_20260729.csv'
$failures = [System.Collections.Generic.List[string]]::new()
foreach ($required in @($targetPath,$ledgerPath,$reviewPath,$evidenceIndexPath)) {
    if (-not (Test-Path -LiteralPath $required  -PathType Leaf)) { $failures.Add("정합성 입력 파일이 없습니다: $required") | Out-Null }
}
if ($failures.Count -gt 0) { throw ($failures -join [Environment]::NewLine) }

$targetText = [IO.File]::ReadAllText($targetPath,[Text.Encoding]::UTF8)
$ledgerText = [IO.File]::ReadAllText($ledgerPath,[Text.Encoding]::UTF8)
$reviewText = [IO.File]::ReadAllText($reviewPath,[Text.Encoding]::UTF8)
$evidenceIndexText = [IO.File]::ReadAllText($evidenceIndexPath,[Text.Encoding]::UTF8)

$section = [regex]::Match($targetText,'(?s)## 22\. Requirement Catalog(?<body>.*?)(?:\r?\n## 23\.|\z)')
if (-not $section.Success) { $failures.Add('Final Target Requirement Catalog section이 없습니다.') | Out-Null }
$canonical = [Collections.Generic.List[string]]::new(); $seen=@{}
if ($section.Success) {
    foreach ($m in [regex]::Matches($section.Groups['body'].Value,'`(?<id>[A-Z][A-Z0-9]+(?:-[A-Z0-9]+)+)`')) {
        $id=$m.Groups['id'].Value; if(-not $seen.ContainsKey($id)){$seen[$id]=$true;$canonical.Add($id)|Out-Null}
    }
}
$targetCountMatch=[regex]::Match($targetText,'Canonical Requirement Count:\s*\*{0,2}(?<count>\d+)')
$ledgerCountMatch=[regex]::Match($ledgerText,'Canonical Count:\s*\*{0,2}(?<count>\d+)')
if(-not $targetCountMatch.Success -or -not $ledgerCountMatch.Success){$failures.Add('Canonical Count 선언을 찾을 수 없습니다.')|Out-Null}
else{
    $targetCount=[int]$targetCountMatch.Groups['count'].Value;$ledgerCount=[int]$ledgerCountMatch.Groups['count'].Value
    if($canonical.Count -ne $targetCount){$failures.Add("Final Target Count 불일치 declared=$targetCount actual=$($canonical.Count)")|Out-Null}
    if($ledgerCount -ne $targetCount){$failures.Add("Final Target/Ledger Count 불일치 target=$targetCount ledger=$ledgerCount")|Out-Null}
}

$allowedStatuses=@('완료','부분 구현','미구현','미검증','실패','재확인 필요');$reviewMap=@{}
foreach($line in($reviewText-split'\r?\n')){
    if($line-match'^\|\s*`(?<id>[A-Z][A-Z0-9]+(?:-[A-Z0-9]+)+)`\s*\|\s*(?<status>[^|]+?)\s*\|'){
        $id=$Matches.id;$status=$Matches.status.Trim()
        if($allowedStatuses -notcontains $status){$failures.Add("허용되지 않은 상태: $id=$status")|Out-Null}
        if($reviewMap.ContainsKey($id)){$failures.Add("Review Requirement 중복: $id")|Out-Null}else{$reviewMap[$id]=$status}
    }
}
foreach($id in $canonical){if(-not $reviewMap.ContainsKey($id)){$failures.Add("Review Requirement 누락: $id")|Out-Null}}
foreach($id in $reviewMap.Keys){if($canonical -notcontains $id){$failures.Add("Review에 비정본 Requirement가 있습니다: $id")|Out-Null}}

# Evidence index rows must point to a machine-readable evidence JSON. String-only mentions no longer count.
$evidenceByRequirement=@{}
foreach($line in($evidenceIndexText-split'\r?\n')){
    if($line -notmatch'^\|') { continue }
    $cols=@($line.Trim('|')-split'\|'|ForEach-Object{$_.Trim()})
    if($cols.Count -lt 6 -or $cols[0] -match'^Evidence|^-+$'){continue}
    $evidenceId=$cols[0].Trim('`');$ids=@([regex]::Matches($cols[1],'[A-Z][A-Z0-9]+(?:-[A-Z0-9]+)+')|ForEach-Object{$_.Value})
    $status=$cols[2];$sha=([regex]::Match($cols[3],'[0-9a-fA-F]{40}')).Value.ToLowerInvariant();$path=$cols[5].Trim('`')
    foreach($id in $ids){
        if(-not $evidenceByRequirement.ContainsKey($id)){$evidenceByRequirement[$id]=[Collections.Generic.List[object]]::new()}
        $evidenceByRequirement[$id].Add([pscustomobject]@{evidenceId=$evidenceId;status=$status;sha=$sha;path=$path})|Out-Null
    }
}

$doneIds=@($canonical|Where-Object{$reviewMap[$_] -eq '완료'})
foreach($id in $doneIds){
    if(-not $evidenceByRequirement.ContainsKey($id)){$failures.Add("완료 Requirement에 구조화 Evidence가 없습니다: $id")|Out-Null;continue}
    $valid=@($evidenceByRequirement[$id]|Where-Object{$_.status -eq '완료' -and $_.sha -eq $ExpectedSha})
    if($valid.Count -eq 0){$failures.Add("완료 Requirement Evidence가 current SHA/PASS가 아닙니다: $id expected=$ExpectedSha")|Out-Null;continue}
    foreach($entry in $valid){
        $evidencePath=Join-Path $Root $entry.path
        if(-not(Test-Path -LiteralPath $evidencePath -PathType Leaf)){$failures.Add("Evidence 파일이 없습니다: $id :: $($entry.path)")|Out-Null;continue}
        try{$doc=Get-Content -LiteralPath $evidencePath -Raw -Encoding UTF8|ConvertFrom-Json -Depth 100}catch{$failures.Add("Evidence JSON parse failed: $($entry.path)")|Out-Null;continue}
        if(([string]$doc.sourceSha).ToLowerInvariant() -ne $ExpectedSha){$failures.Add("Evidence sourceSha mismatch: $($entry.path)")|Out-Null}
        if([int]$doc.exitCode -ne 0 -or [string]$doc.status -ne '완료'){$failures.Add("Evidence is not successful: $($entry.path)")|Out-Null}
        if([string]::IsNullOrWhiteSpace([string]$doc.command)){$failures.Add("Evidence command missing: $($entry.path)")|Out-Null}
        if(-not [bool]$doc.sensitiveDataRemoved){$failures.Add("Evidence sensitive-data confirmation missing: $($entry.path)")|Out-Null}
    }
}

# The merged full-QA ledger is validated row-by-row only for rows already claimed complete.
$qaCompleted=0
if(Test-Path -LiteralPath $fullQaLedgerPath -PathType Leaf){
    $qaRows=@(Import-Csv -LiteralPath $fullQaLedgerPath)
    foreach($row in $qaRows){
        if($allowedStatuses -notcontains $row.closing_status){$failures.Add("Full QA status invalid: $($row.id)=$($row.closing_status)")|Out-Null;continue}
        if($row.closing_status -ne '완료'){continue}
        $qaCompleted++
        if([string]::IsNullOrWhiteSpace($row.closing_evidence)){$failures.Add("Full QA completed row missing evidence: $($row.id)")|Out-Null;continue}
        if($row.closing_evidence -notmatch'[0-9a-fA-F]{40}'){$failures.Add("Full QA completed row missing exact SHA: $($row.id)")|Out-Null}
        elseif(([regex]::Match($row.closing_evidence,'[0-9a-fA-F]{40}').Value.ToLowerInvariant()) -ne $ExpectedSha){$failures.Add("Full QA completed row stale SHA: $($row.id)")|Out-Null}
    }
}

$result=[ordered]@{generatedAt=[DateTimeOffset]::Now.ToString('o');sourceSha=$ExpectedSha;status=if($failures.Count -eq 0){'완료'}else{'실패'};canonicalRequirementCount=$canonical.Count;reviewRequirementCount=$reviewMap.Count;completedRequirementCount=$doneIds.Count;fullQaCompletedCount=$qaCompleted;failureCount=$failures.Count;failures=@($failures)}
$output=Join-Path $ResultDir 'report-matrix-evidence-consistency.sanitized.json'
[IO.File]::WriteAllText($output,($result|ConvertTo-Json -Depth 20),$Utf8)
if($failures.Count -gt 0){$failures|ForEach-Object{Write-Host "FAIL $_"};exit 1}
Write-Host "[PASS] Requirement/Review/Evidence semantic consistency. canonical=$($canonical.Count) completed=$($doneIds.Count) sha=$ExpectedSha"
