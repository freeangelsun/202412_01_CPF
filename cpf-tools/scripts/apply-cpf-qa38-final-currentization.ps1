[CmdletBinding()]
param(
    [Parameter(Mandatory=$true)][string]$RepositoryRoot,
    [Parameter(Mandatory=$true)][string]$OverlayRoot,
    [Parameter(Mandatory=$true)][switch]$ApproveCurrentizationAndCleanup
)
Set-StrictMode -Version Latest
$ErrorActionPreference='Stop'

function Invoke-Git {
 param([string[]]$Arguments,[switch]$AllowFailure)
 $out=@(& git -C $script:Repo @Arguments 2>&1);$code=$LASTEXITCODE
 if (-not $AllowFailure -and $code -ne 0){throw "git $($Arguments -join ' ') failed ($code): $($out -join [Environment]::NewLine)"}
 [pscustomobject]@{ExitCode=$code;Output=$out;Text=($out -join [Environment]::NewLine).Trim()}
}
function Normalize-RelativePath {param([string]$Value)
 $p=$Value.Replace('\','/').Trim()
 if ([string]::IsNullOrWhiteSpace($p) -or [IO.Path]::IsPathRooted($p) -or $p -match '(^|/)\.\.(/|$)'){throw "Unsafe relative path: $Value"}
 $p
}
function Is-Protected {param([string]$Relative)
 foreach($prefix in $script:Protected){if($Relative.StartsWith($prefix,[StringComparison]::OrdinalIgnoreCase)){return $true}}
 return $false
}
function Is-TextPath {param([string]$Relative)
 @('.md','.csv','.json','.txt','.ps1','.yml','.yaml','.gradle','.properties') -contains [IO.Path]::GetExtension($Relative).ToLowerInvariant()
}
function Add-HistorySnapshot {param([string]$HistoryText,[string]$Relative,[string]$FullPath,[string]$Reason)
 if (-not (Test-Path -LiteralPath $FullPath -PathType Leaf)){return $HistoryText}
 $sha=(Get-FileHash -LiteralPath $FullPath -Algorithm SHA256).Hash.ToLowerInvariant()
 if (-not (Is-TextPath $Relative)){return $HistoryText+"`n`n### Snapshot: $Relative`n- SHA-256: `$sha`n- $Reason`n- Binary content is in temp backup.`n"}
 $raw=Get-Content -LiteralPath $FullPath -Raw -Encoding UTF8
 return $HistoryText+"`n`n### Snapshot: $Relative`n- SHA-256: `$sha`n- $Reason`n`n````text`n$raw`n````n"
}
function Merge-MarkerSection {param([string]$TargetPath,[string]$StartMarker,[string]$EndMarker,[string]$SectionText)
 if (-not (Test-Path -LiteralPath $TargetPath -PathType Leaf)){
  New-Item -ItemType Directory -Path (Split-Path -Parent $TargetPath) -Force|Out-Null
  [IO.File]::WriteAllText($TargetPath,$SectionText.Trim()+[Environment]::NewLine,[Text.UTF8Encoding]::new($false));return
 }
 $existing=Get-Content -LiteralPath $TargetPath -Raw -Encoding UTF8
 $block=$StartMarker+[Environment]::NewLine+$SectionText.Trim()+[Environment]::NewLine+$EndMarker
 $pattern=[regex]::Escape($StartMarker)+'.*?'+[regex]::Escape($EndMarker)
 if ([regex]::IsMatch($existing,$pattern,[Text.RegularExpressions.RegexOptions]::Singleline)){
  $merged=[regex]::Replace($existing,$pattern,[Text.RegularExpressions.MatchEvaluator]{param($m)$block},[Text.RegularExpressions.RegexOptions]::Singleline)
  Write-Host "[MERGE_UPDATE_MARKER] $TargetPath"
 }else{$merged=$existing.TrimEnd()+[Environment]::NewLine+[Environment]::NewLine+$block+[Environment]::NewLine;Write-Host "[MERGE_APPEND_MARKER] $TargetPath"}
 [IO.File]::WriteAllText($TargetPath,$merged,[Text.UTF8Encoding]::new($false))
}

if (-not $ApproveCurrentizationAndCleanup){throw 'Explicit -ApproveCurrentizationAndCleanup is required.'}
$script:Repo=(Resolve-Path -LiteralPath $RepositoryRoot).Path
$overlay=(Resolve-Path -LiteralPath $OverlayRoot).Path
$script:Protected=@('cpf-docs/deliverables/','cpf-docs/guides/','cpf-docs/environment/docker/','cpf-tools/environment/docker-development-test/')
$managedReplace=@(
    'cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md',
    'cpf-docs/work/codex/qa38/CODEX_START_HERE.md',
    'cpf-docs/work/codex/qa38/CPF_CODEX_QA38_VERIFICATION_REMEDIATION_REQUEST.md',
    'cpf-docs/work/codex/qa38/STAGE_PLAN.csv',
    'cpf-docs/work/state/CPF_CODEX_CONTINUITY_STATE.md',
    'cpf-docs/work/state/CPF_CODEX_DECISION_LOG.md',
    'cpf-docs/work/history/CPF_QA37_TO_QA38_CONSOLIDATED_HISTORY.md'
)
if (-not (Test-Path -LiteralPath (Join-Path $script:Repo '.git'))){throw 'Not a Git repository root.'}
if ((Invoke-Git @('rev-parse','--abbrev-ref','HEAD')).Text -ne 'master'){throw 'Current branch must be master.'}
$remote=(Invoke-Git @('remote','get-url','origin')).Text
if ($remote -notmatch 'freeangelsun/202412_01_CPF(?:\.git)?$'){throw "Unexpected origin: $remote"}
Invoke-Git @('fetch','origin','master')|Out-Null
$baseline='2e93d92393c52b887482731b683db3c3822027b1'
if ((Invoke-Git @('merge-base','--is-ancestor',$baseline,'HEAD')-AllowFailure).ExitCode -ne 0){throw "Local HEAD does not contain baseline: $baseline"}
if ((Invoke-Git @('merge-base','--is-ancestor',$baseline,'origin/master')-AllowFailure).ExitCode -ne 0){throw "origin/master does not contain baseline: $baseline"}
if ((Invoke-Git @('merge-base','--is-ancestor','origin/master','HEAD')-AllowFailure).ExitCode -ne 0){throw 'origin/master is ahead of local HEAD.'}

$hashRelative='cpf-docs/work/manifest/CPF_QA38_FILES.sha256'
$deleteRelative='cpf-docs/work/manifest/CPF_QA38_DELETE_MANIFEST.csv'
$historyRelative='cpf-docs/work/history/CPF_QA37_TO_QA38_CONSOLIDATED_HISTORY.md'
if ($managedReplace -notcontains $historyRelative) { throw 'Internal package error: consolidated history must be SHARED_MANAGED_REPLACE.' }
$hashFile=Join-Path $overlay $hashRelative.Replace('/',[IO.Path]::DirectorySeparatorChar)
$deleteFile=Join-Path $overlay $deleteRelative.Replace('/',[IO.Path]::DirectorySeparatorChar)
if (-not (Test-Path -LiteralPath $hashFile -PathType Leaf)){throw "Missing hash file: $hashFile"}
if (-not (Test-Path -LiteralPath $deleteFile -PathType Leaf)){throw "Missing delete manifest: $deleteFile"}

$entries=New-Object System.Collections.Generic.List[object]
foreach($line in (Get-Content -LiteralPath $hashFile -Encoding UTF8)) {
 if ([string]::IsNullOrWhiteSpace($line)){continue}
 if ($line -notmatch '^([0-9a-fA-F]{64})  (.+)$'){throw "Invalid hash line: $line"}
 $expected=$Matches[1].ToLowerInvariant();$rel=Normalize-RelativePath $Matches[2]
 if (Is-Protected $rel){throw "Protected path found in overlay. Nothing changed: $rel"}
 $src=Join-Path $overlay $rel.Replace('/',[IO.Path]::DirectorySeparatorChar)
 if (-not (Test-Path -LiteralPath $src -PathType Leaf)){throw "Missing overlay file: $rel"}
 $actual=(Get-FileHash -LiteralPath $src -Algorithm SHA256).Hash.ToLowerInvariant()
 if ($actual -ne $expected){throw "Hash mismatch: $rel"}
 $entries.Add([pscustomobject]@{Relative=$rel;Source=$src;Hash=$actual})
}
$entries.Add([pscustomobject]@{Relative=$hashRelative;Source=$hashFile;Hash=(Get-FileHash -LiteralPath $hashFile -Algorithm SHA256).Hash.ToLowerInvariant()})
$overlayPaths=@($entries.Relative)

$deleteRows=Import-Csv -LiteralPath $deleteFile -Encoding UTF8
$deletePaths=New-Object System.Collections.Generic.List[string]
foreach($row in $deleteRows){
 $rel=Normalize-RelativePath $row.path
 if (Is-Protected $rel){throw "Protected path in Delete Manifest. Nothing changed: $rel"}
 if ($overlayPaths -contains $rel){throw "Overlay/Delete overlap: $rel"}
 $deletePaths.Add($rel)
 $changed=(Invoke-Git @('diff','--name-only',$baseline,'origin/master','--',$rel)).Text
 if (-not [string]::IsNullOrWhiteSpace($changed)){throw "Delete candidate changed after baseline. Nothing changed: $rel"}
}

$conflicts=New-Object System.Collections.Generic.List[string]
foreach($entry in $entries){
 if ($managedReplace -contains $entry.Relative){continue}
 $target=Join-Path $script:Repo $entry.Relative.Replace('/',[IO.Path]::DirectorySeparatorChar)
 $status=(Invoke-Git @('status','--porcelain=v1','--',$entry.Relative)).Text
 if (-not [string]::IsNullOrWhiteSpace($status)){
  if ((Test-Path -LiteralPath $target -PathType Leaf) -and ((Get-FileHash -LiteralPath $target -Algorithm SHA256).Hash.ToLowerInvariant() -eq $entry.Hash)){continue}
  if ($entry.Relative -notmatch '^cpf-docs/(quality|work/(current|review|history|handover|codex|manifest))/CPF_QA38' -and
     $entry.Relative -notmatch '^cpf-docs/work/codex/qa38/(REVIEW_INDEX|REQUIREMENT_STATUS|TEST_AND_EVIDENCE|OPEN_ISSUES|PACKAGE_MANIFEST)'){$conflicts.Add($entry.Relative)}
 }
}
if ($conflicts.Count -gt 0){throw "Non-mergeable local changes. Nothing changed.`n$(($conflicts|Sort-Object -Unique)-join[Environment]::NewLine)"}

$backup=Join-Path $env:TEMP ('cpf-qa38-currentization-backup-'+(Get-Date -Format 'yyyyMMdd_HHmmss'))
New-Item -ItemType Directory -Path $backup -Force|Out-Null
$historyOverlay=Join-Path $overlay $historyRelative.Replace('/',[IO.Path]::DirectorySeparatorChar)
$historyText=Get-Content -LiteralPath $historyOverlay -Raw -Encoding UTF8
$affected=@($overlayPaths)+@($deletePaths)
foreach($rel in ($affected | Sort-Object -Unique)){
 $target=Join-Path $script:Repo $rel.Replace('/',[IO.Path]::DirectorySeparatorChar)
 if (-not (Test-Path -LiteralPath $target -PathType Leaf)){continue}
 $backupTarget=Join-Path $backup $rel.Replace('/',[IO.Path]::DirectorySeparatorChar)
 New-Item -ItemType Directory -Path (Split-Path -Parent $backupTarget) -Force|Out-Null
 Copy-Item -LiteralPath $target -Destination $backupTarget -Force
 $status=(Invoke-Git @('status','--porcelain=v1','--',$rel)).Text
 if (($managedReplace -contains $rel) -or (($deletePaths -contains $rel) -and -not [string]::IsNullOrWhiteSpace($status))){
  $historyText=Add-HistorySnapshot -HistoryText $historyText -Relative $rel -FullPath $target -Reason 'Preserved before QA38 currentization'
 }
}

foreach($entry in $entries){
 $target=Join-Path $script:Repo $entry.Relative.Replace('/',[IO.Path]::DirectorySeparatorChar)
 New-Item -ItemType Directory -Path (Split-Path -Parent $target) -Force|Out-Null
 if ($entry.Relative -eq $historyRelative){[IO.File]::WriteAllText($target,$historyText,[Text.UTF8Encoding]::new($false));Write-Host "[APPLY_WITH_SNAPSHOTS] $($entry.Relative)"}
 else{Copy-Item -LiteralPath $entry.Source -Destination $target -Force;Write-Host "[APPLY] $($entry.Relative)"}
}

$canonicalIndex=Join-Path $script:Repo 'cpf-docs/governance/CPF_DOCUMENT_CANONICAL_INDEX.md'
Merge-MarkerSection -TargetPath $canonicalIndex -StartMarker '<!-- CPF_QA38_CURRENTIZATION_START -->' -EndMarker '<!-- CPF_QA38_CURRENTIZATION_END -->' -SectionText @'
## QA38 현행 개발·검수 정본
- Current: `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
- Detailed Request: `cpf-docs/work/current/CPF_QA38_FINAL_DEVELOPMENT_REQUIREMENTS.md`
- Requirement Matrix: `cpf-docs/quality/CPF_QA38_FINAL_REQUIREMENT_MATRIX.csv`
- Scenario Matrix: `cpf-docs/quality/CPF_QA38_FINAL_SCENARIO_MATRIX.csv`
- Starter Review: `cpf-docs/work/review/CPF_QA38_STARTER_INDEPENDENT_REVIEW.md`
- Codex Entry: `cpf-docs/work/codex/qa38/CODEX_START_HERE.md`
- History: `cpf-docs/work/history/CPF_QA37_TO_QA38_CONSOLIDATED_HISTORY.md`
과거 날짜별 문서는 활성 정본이 아니다.
'@

$pathMap=Join-Path $script:Repo 'cpf-docs/governance/CPF_CANONICAL_PATH_AND_ROLE_MAP.md'
Merge-MarkerSection -TargetPath $pathMap -StartMarker '<!-- CPF_QA38_STABLE_PATHS_START -->' -EndMarker '<!-- CPF_QA38_STABLE_PATHS_END -->' -SectionText @'
## QA38 Stable Paths
| 역할 | 경로 |
|---|---|
| Current | `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md` |
| Detailed | `cpf-docs/work/current/CPF_QA38_FINAL_DEVELOPMENT_REQUIREMENTS.md` |
| Requirement | `cpf-docs/quality/CPF_QA38_FINAL_REQUIREMENT_MATRIX.csv` |
| Scenario | `cpf-docs/quality/CPF_QA38_FINAL_SCENARIO_MATRIX.csv` |
| Starter Review | `cpf-docs/work/review/CPF_QA38_STARTER_INDEPENDENT_REVIEW.md` |
| Codex | `cpf-docs/work/codex/qa38/CODEX_START_HERE.md` |
| History | `cpf-docs/work/history/CPF_QA37_TO_QA38_CONSOLIDATED_HISTORY.md` |
| Handover | `cpf-docs/work/handover/CPF_QA38_HANDOVER.md` |
'@

$ledger=Join-Path $script:Repo 'cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md'
Merge-MarkerSection -TargetPath $ledger -StartMarker '<!-- CPF_QA38_CONTINUITY_START -->' -EndMarker '<!-- CPF_QA38_CONTINUITY_END -->' -SectionText @'
## QA38 Recovery·Currentization
- Canonical 169 유지
- RabbitMQ/AMQP·JMS 승인 없는 제외 무효
- IBM MQ/JMS 분리
- TPC Alias→EXS-TCP
- Core→Starter 30개와 Final Matrix로 승계
- 날짜별 문서는 History 흡수 후 exact 삭제
- 사용자 승인 없는 Requirement 제거 금지
'@

$deleted=0;$missing=0
foreach($rel in $deletePaths){
 $target=Join-Path $script:Repo $rel.Replace('/',[IO.Path]::DirectorySeparatorChar)
 if (Test-Path -LiteralPath $target -PathType Leaf){Remove-Item -LiteralPath $target -Force;$deleted++;Write-Host "[DELETE] $rel"}
 else{$missing++;Write-Host "[SKIP_MISSING] $rel"}
}
foreach($rel in @('cpf-docs/work/review/20260802_05','cpf-docs/work/review/20260802_06','cpf-docs/work/repository-consolidation/20260802/codex-review','cpf-docs/work/repository-consolidation/20260802')){
 if (Is-Protected $rel){throw "Protected cleanup target: $rel"}
 $dir=Join-Path $script:Repo $rel.Replace('/',[IO.Path]::DirectorySeparatorChar)
 if ((Test-Path -LiteralPath $dir -PathType Container) -and -not (Get-ChildItem -LiteralPath $dir -Force | Select-Object -First 1)){Remove-Item -LiteralPath $dir -Force;Write-Host "[REMOVE_EMPTY_DIR] $rel"}
}
Invoke-Git @('diff','--check')|Out-Null
foreach($rel in @('cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md','cpf-docs/work/current/CPF_QA38_FINAL_DEVELOPMENT_REQUIREMENTS.md','cpf-docs/work/codex/qa38/CODEX_START_HERE.md')){
 if (-not (Test-Path -LiteralPath (Join-Path $script:Repo $rel) -PathType Leaf)){throw "Post-apply active file missing: $rel"}
}
Write-Host "QA38 final currentization applied."
Write-Host "Deleted=$deleted MissingSkipped=$missing"
Write-Host "Protected paths modified=0"
Write-Host "Backup=$backup"
Write-Host "No commit or push was performed."
(Invoke-Git @('status','--short')).Output|ForEach-Object{Write-Host $_}
