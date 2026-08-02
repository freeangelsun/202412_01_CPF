#!/usr/bin/env python3
import shutil, subprocess, tempfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
GATE=ROOT/'cpf-tools/scripts/verify-cpf-qa32-primary-engines.py'
POLICY_SWITCH='-Execution'+'Policy'

TRANSIENT_SOURCE_NAMES = (
    '.git', '.gradle', '.idea', '.vscode',
    'build', 'node_modules', '__pycache__',
)

def copy_source_tree(destination):
    """Copy only repository source; active tool caches can be locked on Windows."""
    shutil.copytree(
        ROOT,
        destination,
        ignore=shutil.ignore_patterns(*TRANSIENT_SOURCE_NAMES, '*.pyc', '*.pyo'),
    )

def invoke_gate(root):
    return subprocess.run(
        ['python', str(GATE), '--root', str(root)],
        capture_output=True,
        text=True,
    )

def run(case, mutate):
    with tempfile.TemporaryDirectory() as td:
        dst=Path(td)/'repo'; copy_source_tree(dst)
        mutate(dst)
        p=invoke_gate(dst)
        if p.returncode==0: raise SystemExit(f'negative fixture did not fail: {case}')
        print(f'PASS {case}: exit={p.returncode}')

baseline=invoke_gate(ROOT)
if baseline.returncode != 0:
    raise SystemExit(
        'QA32 primary-engine baseline must pass before negative fixtures run:\n'
        + baseline.stdout + baseline.stderr
    )
run('legacy gateway', lambda r:(r/'cpf-gateway/src/main/java/com/cpf/gateway/service/CpfGatewayProxyService.java').parent.mkdir(parents=True,exist_ok=True) or (r/'cpf-gateway/src/main/java/com/cpf/gateway/service/CpfGatewayProxyService.java').write_text('class X{}'))
run('browser token', lambda r:(r/'cpf-admin/frontend/src/bad.ts').write_text("localStorage.setItem('accessToken','x')"))
run('db checksum drift', lambda r:(r/'cpf-tools/db/vendor/postgresql/migration/flyway/batDB/V82__spring_batch_primary_control_link.sql').write_text('drift'))
run('Gradle PowerShell policy bypass', lambda r:(r/'qa32-policy-bypass.gradle').write_text(
    f"commandLine 'pwsh', '-NoProfile', '{POLICY_SWITCH}', 'Bypass', '-File', 'gate.ps1'\n",
    encoding='utf-8'))
run('PowerShell array policy bypass', lambda r:(r/'qa32-policy-bypass.ps1').write_text(
    f"$arguments = @('-NoProfile','{POLICY_SWITCH}','Bypass','-File','gate.ps1')\n",
    encoding='utf-8'))
run('Python list policy bypass', lambda r:(r/'qa32-policy-bypass.py').write_text(
    f"arguments = ['pwsh', '-NoProfile', '{POLICY_SWITCH}', 'Bypass', '-File', 'gate.ps1']\n",
    encoding='utf-8'))
print('QA32_NEGATIVE_FIXTURES_PASS')
