#!/usr/bin/env python3
from pathlib import Path
import os,subprocess,sys
ROOT=Path(__file__).resolve().parents[4]; H=ROOT/'cpf-docs/governance/development-harness'; py=sys.executable
steps=['validators/validate_session_merge_protocol.py','validators/validate_current_product_conformance.py','validators/validate_harness_strength_regression.py','validators/validate_control_registry.py','validators/validate_harness_authority.py','validators/validate_migration_semantic_closure.py','validators/validate_development_harness.py','validators/validate_split_datasets.py','tests/test_negative_fixtures.py','validators/generate_detailed_review.py']
fail=[]
for s in steps:
    if s=='tests/test_negative_fixtures.py':
        # Same negative mutations, split into independent partitions and executed in parallel.
        # This changes execution scheduling only; no mutation, expected failure, or validator is removed.
        procs=[]
        for group in ('BASE','AUTH_A','AUTH_B','STRENGTH'):
            env=os.environ.copy(); env['CPF_HARNESS_NEGATIVE_GROUP']=group
            procs.append((group,subprocess.Popen([py,'-B',str(H/s)],cwd=ROOT,env=env,text=True,stdout=subprocess.PIPE,stderr=subprocess.STDOUT)))
        partition_fail=[]
        for group,proc in procs:
            out,_=proc.communicate()
            if out: print(out,end='' if out.endswith('\n') else '\n')
            print(f'[HARNESS] {s} group={group} rc={proc.returncode}')
            if proc.returncode: partition_fail.append(group)
        if partition_fail: fail.append(s+':'+','.join(partition_fail))
        continue
    cp=subprocess.run([py,'-B',str(H/s)],cwd=ROOT)
    print(f'[HARNESS] {s} rc={cp.returncode}')
    if cp.returncode: fail.append(s)
print('DEVELOPMENT_HARNESS_FINAL_GATE='+('PASS' if not fail else 'FAIL')+' failed='+str(fail))
raise SystemExit(1 if fail else 0)
