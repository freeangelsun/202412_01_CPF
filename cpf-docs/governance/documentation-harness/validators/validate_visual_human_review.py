#!/usr/bin/env python3
import argparse,json,hashlib,sys
from pathlib import Path
H=Path(__file__).resolve().parents[1]; ROOT=H.parents[2]
def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest().upper()
def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--review',default=str(ROOT/'cpf-docs/deliverables/documentation/HUMAN_VISUAL_REVIEW.json')); ap.add_argument('--asset-root',default=str(ROOT)); a=ap.parse_args()
    review=Path(a.review); assetroot=Path(a.asset_root); cfg=json.loads((H/'visual-human-quality.json').read_text(encoding='utf-8')); errs=[]
    if not review.is_file(): print('VISUAL_HUMAN_REVIEW=FAIL review evidence missing'); return 1
    d=json.loads(review.read_text(encoding='utf-8')); visuals=d.get('visuals',[])
    if not d.get('reviewer') or not d.get('reviewedAt'): errs.append('reviewer/reviewedAt missing')
    grammars=[]
    for v in visuals:
        p=assetroot/v.get('artifactPath','')
        if not p.is_file(): errs.append('asset missing '+str(v.get('artifactPath'))); continue
        if sha(p)!=str(v.get('artifactSha256','')).upper(): errs.append('stale review SHA '+str(v.get('artifactPath')))
        for fld in ['scanPass','detailPass','originalSizeReviewed']:
            if v.get(fld) is not True: errs.append(f'{fld} not PASS '+str(v.get('artifactPath')))
        metrics=v.get('metrics',{})
        for k in cfg['hardZeroMetrics']:
            if int(metrics.get(k,0))!=0: errs.append(f'{k}={metrics.get(k)} '+str(v.get('artifactPath')))
        surfaces=set(v.get('surfacesReviewed',[])); required=set(cfg['requiredSurfacesForReadme'] if v.get('usage')=='README' else cfg['requiredSurfacesForEmbeddedDocumentFigure'])
        if not required.issubset(surfaces): errs.append('embedded surface review incomplete '+str(v.get('artifactPath')))
        grammars.append(str(v.get('geometryFingerprint','')).strip())
    if len(visuals)>=5:
        distinct=len(set(x for x in grammars if x)); min_g=cfg['grammar']['minimumDistinctGrammarsForFiveOrMoreVisuals']
        if distinct<min_g: errs.append(f'distinct geometry fingerprints {distinct}<{min_g}')
        if any(a==b and a for a,b in zip(grammars,grammars[1:])): errs.append('adjacent same geometry fingerprint')
    if errs:
        print('VISUAL_HUMAN_REVIEW=FAIL COUNT='+str(len(errs))); [print('-',e) for e in errs]; return 1
    print('VISUAL_HUMAN_REVIEW=PASS VISUALS='+str(len(visuals))); print('CURRENT_ARTIFACT_SHA_REVIEW_PASS=PASS'); print('VISUAL_SEMANTIC_QUALITY_PASS=PASS'); return 0
if __name__=='__main__': raise SystemExit(main())
