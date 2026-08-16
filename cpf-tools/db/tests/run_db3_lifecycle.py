#!/usr/bin/env python3
"""CPF DB3 lifecycle runtime harness.

This harness is fail-honest: missing client/connection input is UNVERIFIED, never PASS.
Actual lifecycle commands can be injected per vendor by environment so the same Scenario IDs execute in CI/local secured environments.
"""
from __future__ import annotations
import argparse, json, os, shlex, subprocess
from datetime import datetime, timezone
from pathlib import Path

VENDORS=('mariadb','postgresql','oracle')

def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--vendor',choices=VENDORS); ap.add_argument('--evidence'); args=ap.parse_args()
    root=Path(args.root).resolve(); contract=json.loads((root/'cpf-tools/db/canonical/db3-lifecycle-scenarios.json').read_text(encoding='utf-8-sig'))
    vendors=(args.vendor,) if args.vendor else VENDORS; results=[]
    for vendor in vendors:
        envkey='CPF_DB3_'+vendor.upper()+'_COMMAND'
        command=os.environ.get(envkey,'').strip()
        for scenario in contract['scenarios']:
            row={'vendor':vendor,'scenarioId':scenario['id'],'steps':scenario['steps'],'executedAt':datetime.now(timezone.utc).isoformat()}
            if not command:
                row.update(status='UNVERIFIED',reason=f'missing {envkey}')
            else:
                p=subprocess.run(command,shell=True,cwd=root,text=True,capture_output=True,env={**os.environ,'CPF_DB3_VENDOR':vendor,'CPF_DB3_SCENARIO_ID':scenario['id']})
                row.update(status='PASS' if p.returncode==0 else 'FAIL',exitCode=p.returncode,stdout=p.stdout[-8000:],stderr=p.stderr[-8000:])
            results.append(row)
    evidence={'schemaVersion':1,'contract':'CPF_DB3_LIFECYCLE_EVIDENCE','results':results}
    out=Path(args.evidence) if args.evidence else root/'cpf-docs/work/evidence/current/DB3_LIFECYCLE.json'
    out.parent.mkdir(parents=True,exist_ok=True); out.write_text(json.dumps(evidence,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    bad=[r for r in results if r['status']=='FAIL']; unv=[r for r in results if r['status']=='UNVERIFIED']
    print(f'CPF_DB3_LIFECYCLE_RESULT=' + ('FAIL' if bad else ('UNVERIFIED' if unv else 'PASS')))
    print(f'pass={sum(r["status"]=="PASS" for r in results)} fail={len(bad)} unverified={len(unv)} evidence={out}')
    raise SystemExit(1 if bad else (2 if unv else 0))
if __name__=='__main__': main()
