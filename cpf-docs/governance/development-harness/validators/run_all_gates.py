#!/usr/bin/env python3
from pathlib import Path
import subprocess,sys
ROOT=Path(__file__).resolve().parents[4]; H=ROOT/'cpf-docs/governance/development-harness'; py=sys.executable
steps=['validators/validate_current_product_conformance.py','validators/validate_control_registry.py','validators/validate_harness_authority.py','validators/validate_migration_semantic_closure.py','validators/validate_development_harness.py','validators/validate_split_datasets.py','tests/test_negative_fixtures.py','validators/generate_detailed_review.py']
fail=[]
for s in steps:
    cp=subprocess.run([py,str(H/s)],cwd=ROOT)
    print(f'[HARNESS] {s} rc={cp.returncode}')
    if cp.returncode: fail.append(s)
print('DEVELOPMENT_HARNESS_FINAL_GATE='+('PASS' if not fail else 'FAIL')+' failed='+str(fail))
raise SystemExit(1 if fail else 0)
