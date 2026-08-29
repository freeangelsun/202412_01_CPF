#!/usr/bin/env python3
import json,re,subprocess,sys
from pathlib import Path
H=Path(__file__).resolve().parents[1]
def main():
    errs=[]
    hex64=re.compile(r'(?<![A-Fa-f0-9])[A-Fa-f0-9]{64}(?![A-Fa-f0-9])')
    for p in (H/'validators').glob('*.py'):
        if p.name==Path(__file__).name: continue
        txt=p.read_text(encoding='utf-8')
        if hex64.search(txt): errs.append('hardcoded 64-hex identity in '+p.name)
    for rel in ['README_PRODUCT_COMPLETENESS_STANDARD.md','VISUAL_HUMAN_QUALITY_STANDARD.md','FALSE_GREEN_PREVENTION_STANDARD.md','readme-product-completeness.json','visual-human-quality.json','false-green-policy.json']:
        if not (H/rel).is_file(): errs.append('missing '+rel)
    qa=json.loads((H/'quality-acceptance.json').read_text(encoding='utf-8'))
    required={s['id'] for s in qa.get('stages',[]) if s.get('required')}
    for gid in ['README_PRODUCT_COMPLETENESS_PASS','README_ARCHITECTURE_COMPLETENESS_PASS','README_EXPLANATION_DEPTH_PASS','README_DEVELOPER_GOLDEN_PATH_PASS','VISUAL_SEMANTIC_QUALITY_PASS','CURRENT_ARTIFACT_SHA_REVIEW_PASS','USER_FINDING_CLOSURE_PASS','FALSE_GREEN_PREVENTION_PASS']:
        if gid not in required: errs.append('required gate missing '+gid)
    r=subprocess.run([sys.executable,str(H/'validators/validate_quality_fixtures.py')],capture_output=True,text=True)
    if r.returncode: errs.append('negative fixtures failed: '+r.stdout+r.stderr)
    if errs:
        print('FALSE_GREEN_PREVENTION=FAIL COUNT='+str(len(errs)))
        for e in errs: print('-',e)
        return 1
    print('FALSE_GREEN_PREVENTION=PASS')
    print('FALSE_GREEN_PREVENTION_PASS=PASS')
    return 0
if __name__=='__main__': raise SystemExit(main())
