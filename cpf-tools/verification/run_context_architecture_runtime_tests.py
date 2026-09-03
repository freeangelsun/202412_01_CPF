#!/usr/bin/env python3
"""최신 Context Architecture의 독립 Runtime Gate들을 한 번에 실행합니다."""

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass
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
