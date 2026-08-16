#!/usr/bin/env python3
"""최신 Context Architecture의 독립 Runtime Gate들을 한 번에 실행합니다."""
from pathlib import Path
import subprocess, sys
ROOT=Path(__file__).resolve().parents[2]
HERE=ROOT/'cpf-tools/verification'
CHECKS=[
 'run_context_runtime_lifecycle_tests.py',
 'run_batch_context_runtime_tests.py',
 'run_message_context_runtime_tests.py',
 'run_integration_context_runtime_tests.py',
 'run_security_context_runtime_tests.py',
]
def main():
    failures=[]
    for name in CHECKS:
        cp=subprocess.run([sys.executable,str(HERE/name)],text=True,capture_output=True)
        print('=== '+name+' ===')
        if cp.stdout: print(cp.stdout.rstrip())
        if cp.stderr: print(cp.stderr.rstrip(),file=sys.stderr)
        if cp.returncode: failures.append(name+':'+str(cp.returncode))
    print('CPF_CONTEXT_ARCH_RUNTIME='+('PASS' if not failures else 'FAIL'))
    print('failures='+str(len(failures)))
    for x in failures: print(x)
    return 0 if not failures else 1
if __name__=='__main__': raise SystemExit(main())
