#!/usr/bin/env python3
import shutil, subprocess, tempfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
GATE=ROOT/'cpf-tools/scripts/verify-cpf-qa32-primary-engines.py'

def run(case, mutate):
    with tempfile.TemporaryDirectory() as td:
        dst=Path(td)/'repo'; shutil.copytree(ROOT,dst)
        mutate(dst)
        p=subprocess.run(['python',str(GATE),'--root',str(dst)],capture_output=True,text=True)
        if p.returncode==0: raise SystemExit(f'negative fixture did not fail: {case}')
        print(f'PASS {case}: exit={p.returncode}')
run('legacy gateway', lambda r:(r/'cpf-gateway/src/main/java/com/cpf/gateway/service/CpfGatewayProxyService.java').parent.mkdir(parents=True,exist_ok=True) or (r/'cpf-gateway/src/main/java/com/cpf/gateway/service/CpfGatewayProxyService.java').write_text('class X{}'))
run('browser token', lambda r:(r/'cpf-admin/frontend/src/bad.ts').write_text("localStorage.setItem('accessToken','x')"))
run('db checksum drift', lambda r:(r/'cpf-tools/db/vendor/postgresql/migration/flyway/batDB/V82__spring_batch_primary_control_link.sql').write_text('drift'))
print('QA32_NEGATIVE_FIXTURES_PASS')
