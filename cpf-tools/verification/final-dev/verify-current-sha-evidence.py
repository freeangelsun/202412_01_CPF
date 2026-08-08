#!/usr/bin/env python3
from __future__ import annotations
import argparse,json,re,subprocess,sys
from pathlib import Path
FORBIDDEN=("cd5baccb02245a980e5998aa0dc9bac579fc019f","3aa1dd12f8a5938d33feb6ed598b3dd2442bf2e2")
class E(RuntimeError): pass
def req(v,m):
    if not v: raise E(m)
def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--root',type=Path,default=Path('.')); ap.add_argument('--expected-head',required=True); a=ap.parse_args(); root=a.root.resolve(); expected=a.expected_head.lower()
    req(re.fullmatch(r'[0-9a-f]{40}',expected) is not None,'expected head must be 40 hex chars')
    try:
        actual=subprocess.check_output(['git','-C',str(root),'rev-parse','HEAD'],text=True).strip().lower()
        req(actual==expected,f'HEAD mismatch expected={expected} actual={actual}')
    except FileNotFoundError:
        raise E('git unavailable')
    # Product verification scripts must not embed prior release SHAs as PASS assumptions.
    for p in (root/'cpf-tools/verification').rglob('*'):
        if not p.is_file() or p.suffix.lower() not in {'.py','.ps1','.sh','.json','.csv','.md'}: continue
        txt=p.read_text(encoding='utf-8',errors='ignore').lower()
        for old in FORBIDDEN:
            req(old not in txt,f'hard-coded prior SHA in verifier: {p.relative_to(root)}')
    # Release summary writer must bind every gate to sourceSha/current head.
    release=(root/'cpf-tools/verification/final-dev/run-r6-release-gates.ps1').read_text(encoding='utf-8')
    req('sourceSha=$head' in release,'release evidence rows are not sourceSha-bound')
    req('ExpectedHead mismatch' in release,'release gate does not fail closed on HEAD mismatch')
    print(f'[CPF][FINAL][EVIDENCE-SHA][PASS] sourceSha={actual}')
if __name__=='__main__':
    try: main()
    except E as e: print('[CPF][FINAL][EVIDENCE-SHA][FAIL] '+str(e),file=sys.stderr); raise SystemExit(1)
