param([Parameter(Mandatory=$true)][string]$Manifest)
$ErrorActionPreference='Stop'
$here=Split-Path -Parent $MyInvocation.MyCommand.Path
$h=Split-Path -Parent $here
$qa=Get-Content -Raw -LiteralPath (Join-Path $h 'quality-acceptance.json') -Encoding utf8 | ConvertFrom-Json -Depth 100
$m=Get-Content -Raw -LiteralPath $Manifest -Encoding utf8 | ConvertFrom-Json -Depth 100
$errs=New-Object System.Collections.Generic.List[string]
if($m.harnessVersion-ne '2.9.0'){$errs.Add('manifest harnessVersion must be 2.9.0')}
$agg='FINAL_ACCEPTANCE_AGGREGATOR_PASS'
$required=@($qa.stages|Where-Object{$_.required -eq $true})
foreach($s in $required){
 if($s.id-eq $agg){continue}
 $st=$m.gates.($s.id)
 if($st-ne 'PASS'){$errs.Add("required gate $($s.id) is $st, must be PASS")}
 $ev=@($m.gateEvidence.($s.id))
 if($ev.Count-eq 0){$errs.Add("gate evidence missing for $($s.id)")}
}
if(@($m.unresolvedCriticalFindings).Count-ne 0){$errs.Add('unresolvedCriticalFindings must be empty')}
if(@($m.unresolvedFindings).Count-ne 0){$errs.Add('unresolvedFindings must be empty')}
if(-not $m.manualReviewer){$errs.Add('manualReviewer missing')}
if(-not $m.reviewedAt){$errs.Add('reviewedAt missing')}
if(@($m.evidenceRefs).Count-eq 0){$errs.Add('global evidenceRefs empty')}
$targets=@($m.targetArtifacts); $reviews=@($m.artifactReviews)
if($targets.Count-eq 0){$errs.Add('targetArtifacts empty')}
foreach($aid in $targets){
 $e=$reviews|Where-Object{$_.artifactId-eq $aid}|Select-Object -First 1
 if(-not $e){$errs.Add("artifact review missing: $aid");continue}
 if(!(Test-Path -LiteralPath $e.reviewFile -PathType Leaf)){$errs.Add("artifact review file missing: $aid -> $($e.reviewFile)");continue}
 $r=Get-Content -Raw -LiteralPath $e.reviewFile -Encoding utf8|ConvertFrom-Json -Depth 100
 if($r.approvalState-notin @('VISUAL_QA_APPROVED','USER_APPROVED')){$errs.Add("$aid approvalState not approved: $($r.approvalState)")}
 foreach($p in $r.manualGates.PSObject.Properties){if($p.Value-notin @('PASS','NOT_APPLICABLE')){$errs.Add("$aid manual gate $($p.Name)=$($p.Value)")}}
 if(@($r.unresolvedCriticalFindings).Count-ne 0){$errs.Add("$aid unresolvedCriticalFindings not empty")}
 if(@($r.evidenceRefs).Count-eq 0){$errs.Add("$aid evidenceRefs empty")}
 foreach($p in $r.layoutChecks.PSObject.Properties){if($p.Value -is [int] -or $p.Value -is [long] -or $p.Value -is [double]){if([double]$p.Value-ne 0){$errs.Add("$aid hard-fail metric $($p.Name)=$($p.Value)")}}}
}
if($m.finalStatus-eq 'PASS' -and $m.gates.($agg)-ne 'PASS'){$errs.Add('finalStatus PASS declared before aggregator PASS')}
if($errs.Count){Write-Host "FINAL_ACCEPTANCE=FAIL COUNT=$($errs.Count)";$errs|ForEach-Object{Write-Host "- $_"};exit 1}
Write-Host 'FINAL_ACCEPTANCE=PASS';Write-Host 'FINAL_ACCEPTANCE_AGGREGATOR_PASS=PASS';Write-Host "REQUIRED_GATES=$($required.Count)";Write-Host "TARGET_ARTIFACTS=$($targets.Count)"
