#!/usr/bin/env python3
"""Verify CPF progress denominator and print deterministic current progress."""
from __future__ import annotations
import argparse, csv, sys
from collections import Counter
from pathlib import Path

STAGES=("analysis","source_consumer","test_integration","gate_evidence")
GROUPS={
 "Core Slimming":("NXT-ARCH-","NXT-PARITY-","NXT-CORE-FREEZE-"),
 "Utility/Foundation":("NXT-UTIL-","NXT-TXID-","NXT-EXEC-"),
 "Starter Portfolio":("NXT-OWN-","NXT-HEALTH-","NXT-OPS-","NXT-JPA-","NXT-PERSIST-","NXT-SEC-","NXT-SESSION-","NXT-STORE-","NXT-EVENT-","NXT-GQL-","NXT-RT-","NXT-LOCK-","NXT-AI-","NXT-DX-"),
 "Consumer":("NXT-FE-","NXT-GEN-002","NXT-CMN-007"),
 "Generator":("NXT-GEN-","NXT-CMN-008"),
 "Test/Harness":("NXT-TESTKIT-","NXT-EVD-002","NXT-QA-"),
 "Documentation":("NXT-DOC-","NXT-HYG-006"),
 "Hygiene/Delete Manifest":("NXT-HYG-",),
}

def points(rows): return sum(int(r[s]) for r in rows for s in STAGES)
def percent(n,d): return 0.0 if d==0 else n*100.0/d

def main():
 ap=argparse.ArgumentParser(); ap.add_argument('--csv',required=True); ap.add_argument('--expected-canonical',type=int,default=63); ns=ap.parse_args()
 p=Path(ns.csv); rows=list(csv.DictReader(p.open(encoding='utf-8-sig',newline='')))
 failures=[]
 ids=[r.get('requirement_id','').strip() for r in rows]
 dup=[x for x,c in Counter(ids).items() if x and c>1]
 if dup: failures.append('duplicate_ids='+','.join(sorted(dup)))
 if any(not x for x in ids): failures.append('blank_requirement_id')
 for r in rows:
  for s in STAGES:
   if r.get(s) not in {'0','1'}: failures.append(f"invalid_stage:{r.get('requirement_id')}:{s}:{r.get(s)!r}")
 canonical=[r for r in rows if r.get('category')=='CANONICAL']
 if len(canonical)!=ns.expected_canonical: failures.append(f'canonical_count={len(canonical)} expected={ns.expected_canonical}')
 allp,alld=points(rows),len(rows)*len(STAGES); cp,cd=points(canonical),len(canonical)*len(STAGES)
 print(f'PROGRESS_ROWS={len(rows)}')
 print(f'PROGRESS_CANONICAL_ROWS={len(canonical)}')
 print(f'PROGRESS_ALL={allp}/{alld} {percent(allp,alld):.2f}%')
 print(f'PROGRESS_CANONICAL={cp}/{cd} {percent(cp,cd):.2f}%')
 for name,prefixes in GROUPS.items():
  rs=[r for r in canonical if any(r['requirement_id'].startswith(x) for x in prefixes)]
  pnt,den=points(rs),len(rs)*len(STAGES)
  print(f'PROGRESS_GROUP {name}={pnt}/{den} {percent(pnt,den):.2f}% requirements={len(rs)}')
 incomplete=sum(1 for r in canonical if any(r[s]=='0' for s in STAGES))
 completed=sum(1 for r in canonical if all(r[s]=='1' for s in STAGES))
 print(f'PROGRESS_CANONICAL_FULLY_COMPLETED={completed}')
 print(f'PROGRESS_CANONICAL_INCOMPLETE={incomplete}')
 if failures:
  for f in failures: print('PROGRESS_FAILURE='+f)
  print('REQUIREMENT_PROGRESS_GATE=FAIL'); return 1
 print('REQUIREMENT_PROGRESS_GATE=PASS'); return 0
if __name__=='__main__': raise SystemExit(main())
