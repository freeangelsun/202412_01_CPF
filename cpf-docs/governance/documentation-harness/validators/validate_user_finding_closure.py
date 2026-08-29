#!/usr/bin/env python3
import argparse,json,sys
from pathlib import Path
H=Path(__file__).resolve().parents[1]; ROOT=H.parents[2]
def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--findings',default=str(ROOT/'cpf-docs/deliverables/documentation/CURRENT_USER_FINDINGS.json')); a=ap.parse_args(); p=Path(a.findings)
    if not p.is_file(): print('USER_FINDING_CLOSURE=FAIL evidence missing'); return 1
    d=json.loads(p.read_text(encoding='utf-8')); errs=[]
    for f in d.get('findings',[]):
        if f.get('status')!='CLOSED': errs.append(str(f.get('id'))+': not CLOSED')
        if f.get('artifactFixed') is not True: errs.append(str(f.get('id'))+': artifactFixed false')
        for k in ['harnessRule','validator','negativeFixture','finalGate']:
            if not f.get(k): errs.append(str(f.get('id'))+': '+k+' missing')
    if int(d.get('openFindings',0))!=0: errs.append('openFindings != 0')
    if errs: print('USER_FINDING_CLOSURE=FAIL COUNT='+str(len(errs))); [print('-',e) for e in errs]; return 1
    print('USER_FINDING_CLOSURE=PASS'); return 0
if __name__=='__main__': raise SystemExit(main())
