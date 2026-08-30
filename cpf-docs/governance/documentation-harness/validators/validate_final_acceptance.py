#!/usr/bin/env python3
import json,sys
from pathlib import Path
H=Path(__file__).resolve().parents[1]
ROOT=H.parents[2]
QA=json.loads((H/'quality-acceptance.json').read_text(encoding='utf-8'))
AGG='FINAL_ACCEPTANCE_AGGREGATOR_PASS'
ALLOWED_APPROVAL={'VISUAL_QA_APPROVED','USER_APPROVED'}
FORBIDDEN={'AUTOMATED_PASS_ONLY','NOT_EXECUTED','BLOCKED','UNKNOWN','SKIPPED','PARTIAL','WAIVED','DRAFT','PENDING','PENDING_AGGREGATOR',''}

def load_json(p):
    try:return json.loads(p.read_text(encoding='utf-8'))
    except Exception as e: raise RuntimeError(f'invalid json {p}: {e}')

def resolve(ref,base):
    p=Path(ref)
    if not p.is_absolute():
        # Prefer repository-relative, then manifest-relative.
        q=ROOT/p
        if q.exists(): return q
        q=base/p
        if q.exists(): return q
    return p

def main():
    if len(sys.argv)<2:
        print('FINAL_ACCEPTANCE=FAIL manifest path required'); return 2
    mp=Path(sys.argv[1]).resolve()
    if not mp.is_file(): print('FINAL_ACCEPTANCE=FAIL manifest missing '+str(mp)); return 2
    try:m=load_json(mp)
    except Exception as e: print('FINAL_ACCEPTANCE=FAIL '+str(e)); return 2
    errs=[]
    if m.get('harnessVersion')!='2.15.4': errs.append('manifest harnessVersion must be 2.15.4')
    stages={x['id']:x for x in QA.get('stages',[]) if x.get('required')}
    gates=m.get('gates',{})
    evidence=m.get('gateEvidence',{})
    # Every required gate except aggregator must already be PASS with evidence.
    for gid,st in stages.items():
        if gid==AGG: continue
        status=gates.get(gid,'')
        if status!='PASS': errs.append(f'required gate {gid} is {status or "MISSING"}, must be PASS')
        refs=evidence.get(gid,[])
        if not isinstance(refs,list) or not refs: errs.append(f'gate evidence missing for {gid}')
        else:
            for ref in refs:
                if not isinstance(ref,str) or not ref.strip(): errs.append(f'empty evidence ref for {gid}'); continue
                rp=resolve(ref,mp.parent)
                if not rp.exists(): errs.append(f'evidence ref not found for {gid}: {ref}')
    # Explicit no-truncation / no-total-cap assertions and three review passes.
    if m.get('contentTruncationForLength') is not False: errs.append('contentTruncationForLength must be false')
    if m.get('totalDocumentSizeCapApplied') is not False: errs.append('totalDocumentSizeCapApplied must be false')
    rp=m.get('reviewPasses',{})
    for k in ['scanPass','detailPass','readerPass']:
        if rp.get(k)!='PASS': errs.append(f'reviewPasses.{k} must be PASS')
    # No prohibited final state coercion.
    if m.get('finalStatus') in FORBIDDEN-{'PENDING_AGGREGATOR','NOT_EXECUTED'}:
        errs.append('forbidden finalStatus '+str(m.get('finalStatus')))
    if m.get('finalStatus')=='PASS' and gates.get(AGG)!='PASS': errs.append('finalStatus PASS declared before aggregator PASS')
    # Findings must be zero.
    for key in ['unresolvedCriticalFindings','unresolvedFindings']:
        if m.get(key): errs.append(f'{key} must be empty')
    # Target artifacts need review manifests.
    targets=m.get('targetArtifacts',[])
    reviews=m.get('artifactReviews',[])
    if not targets: errs.append('targetArtifacts empty')
    review_by={x.get('artifactId'):x for x in reviews if isinstance(x,dict)}
    for aid in targets:
        entry=review_by.get(aid)
        if not entry: errs.append(f'artifact review missing: {aid}'); continue
        ref=entry.get('reviewFile','')
        rp=resolve(ref,mp.parent)
        if not ref or not rp.is_file(): errs.append(f'artifact review file missing: {aid} -> {ref}'); continue
        try:r=load_json(rp)
        except Exception as e: errs.append(str(e)); continue
        if r.get('harnessVersion')!='2.15.4': errs.append(f'{aid}: review harnessVersion mismatch')
        if r.get('approvalState') not in ALLOWED_APPROVAL: errs.append(f'{aid}: approvalState {r.get("approvalState")} not approved')
        if r.get('unresolvedCriticalFindings'): errs.append(f'{aid}: unresolvedCriticalFindings not empty')
        mg=r.get('manualGates',{})
        for k,v in mg.items():
            if v not in ('PASS','NOT_APPLICABLE'):
                errs.append(f'{aid}: manual gate {k}={v}')
        if not r.get('evidenceRefs'): errs.append(f'{aid}: evidenceRefs empty')
        traces=r.get('readerTaskTraces',[])
        if not isinstance(traces,list) or len(traces)<3: errs.append(f'{aid}: readerTaskTraces must contain at least 3 concrete task traces')
        for ti,tr in enumerate(traces):
            if not isinstance(tr,dict): errs.append(f'{aid}: readerTaskTrace #{ti+1} must be structured object'); continue
            for k in ['task','startSection','selection','action','resultCheck','evidenceRef']:
                if not str(tr.get(k,'')).strip(): errs.append(f'{aid}: readerTaskTrace #{ti+1} missing {k}')
        de=r.get('dimensionEvidence',{})
        for dim in ['reader_actionability','selection_to_action','working_example_fit','failure_recovery_closure','visual_comfort','information_hierarchy','flat_list_density','heavy_block_rhythm']:
            refs=de.get(dim,[]) if isinstance(de,dict) else []
            if not isinstance(refs,list) or not refs: errs.append(f'{aid}: dimensionEvidence missing {dim}')
        # Every hard-fail count in layoutChecks must be 0/false/null-safe.
        for k,v in r.get('layoutChecks',{}).items():
            if k=='majorHeadingSingleLine':
                if v is False: errs.append(f'{aid}: {k}=false')
                continue
            if isinstance(v,(int,float)) and v!=0: errs.append(f'{aid}: hard-fail metric {k}={v}')
            if isinstance(v,bool) and v is True and k.lower().endswith(('fail','violation')): errs.append(f'{aid}: hard-fail metric {k}=true')
    # High-quality human review contract: mandatory gates, two-pass evidence, scores.
    score_cfg=QA.get('manualVisualScore',{})
    required_manual=['readerTaskFit','readerTaskCompleteness','tableSemanticFit','tableProportionRender','headingVerticalRhythm','embeddedFigure','humanVisual','regression','clickThrough','contentCoverageNotTruncated','informationArchitecture','freshEyesTwoPass','flexibleTableLayout','longDocumentNavigation','selectionToAction','workingExampleFit','visualComfort','heavyBlockRhythm','flatListGrouping','failureRecoveryClosure']
    for aid in targets:
        entry=review_by.get(aid)
        if not entry: continue
        ref=entry.get('reviewFile','')
        rp0=resolve(ref,mp.parent)
        if not rp0.is_file(): continue
        r=load_json(rp0)
        mg=r.get('manualGates',{})
        for k in required_manual:
            if mg.get(k) not in ('PASS','NOT_APPLICABLE'): errs.append(f'{aid}: mandatory manual gate {k}={mg.get(k,"MISSING")}')
        is_readme=('README' in str(aid).upper()) or ('README' in str(r.get('artifact','')).upper())
        if is_readme:
            for k in ['readmeScanability','readmeNaturalValue','readmeBrochure','readmeAiTextCompanion']:
                if mg.get(k)!='PASS': errs.append(f'{aid}: README manual gate {k}={mg.get(k,"MISSING")}')
        if not r.get('scanPassEvidence'): errs.append(f'{aid}: scanPassEvidence empty')
        if not r.get('detailPassEvidence'): errs.append(f'{aid}: detailPassEvidence empty')
        if not r.get('readerPassEvidence'): errs.append(f'{aid}: readerPassEvidence empty')
        scores=r.get('manualVisualScores',{})
        vals=[]
        for dim in score_cfg.get('dimensions',[]):
            if dim.startswith('readme_') and not is_readme: continue
            v=scores.get(dim)
            if not isinstance(v,(int,float)): errs.append(f'{aid}: manualVisualScore missing {dim}'); continue
            if v < float(score_cfg.get('minimumEach',4)): errs.append(f'{aid}: manualVisualScore {dim}={v} below minimum')
            vals.append(float(v))
        if vals and sum(vals)/len(vals) < float(score_cfg.get('minimumAverage',4.6)):
            errs.append(f'{aid}: manualVisualScore average {sum(vals)/len(vals):.2f} below {score_cfg.get("minimumAverage")}')
        if len(vals)>=10:
            from collections import Counter
            c=Counter(vals); same=max(c.values())
            if same/len(vals)>=0.80 and not str(r.get('scoreUniformityJustification','')).strip():
                errs.append(f'{aid}: >=80% manual scores are identical without scoreUniformityJustification')
    # Global evidence list also required.
    if not m.get('evidenceRefs'): errs.append('global evidenceRefs empty')
    # Reviewer identity/time for manual gate accountability.
    if not str(m.get('manualReviewer','')).strip(): errs.append('manualReviewer missing')
    if not str(m.get('reviewedAt','')).strip(): errs.append('reviewedAt missing')
    if errs:
        print('FINAL_ACCEPTANCE=FAIL COUNT='+str(len(errs)))
        for e in errs: print('-',e)
        return 1
    print('FINAL_ACCEPTANCE=PASS')
    print('FINAL_ACCEPTANCE_AGGREGATOR_PASS=PASS')
    print('REQUIRED_GATES='+str(len(stages)))
    print('TARGET_ARTIFACTS='+str(len(targets)))
    return 0
if __name__=='__main__': sys.exit(main())
