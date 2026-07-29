#!/usr/bin/env python3
from pathlib import Path
import csv, json, re, subprocess, tempfile, sys, hashlib

root = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else Path(__file__).resolve().parents[2]
errors=[]; checks=[]

def ok(name, detail=''):
    checks.append((name,'PASS',detail))
def fail(name, detail):
    errors.append((name,detail)); checks.append((name,'FAIL',detail))

files=[p for p in root.rglob('*') if p.is_file()]
ok('overlay-file-count',str(len(files)))

# Required checkpoint files
required=[
 'cpf-docs/quality/qa-20260729/CPF_QA_387_SCENARIO_CLOSURE_MATRIX_20260729_04.csv',
 'cpf-docs/quality/qa-20260729/CPF_QA_387_SCENARIO_CLOSURE_TRACKER_20260729_04.xlsx',
 'cpf-docs/work/current/CPF_CHATGPT_NEXT_SESSION_DEVELOPMENT_HANDOVER_20260729_04.md',
 'cpf-docs/work/current/CPF_CODEX_QA_387_VALIDATION_REQUEST_20260729_04.md',
 'cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md',
 'cpf-core/src/main/java/com/cpf/core/api/cache/CpfCachePort.java',
 'cpf-core/src/main/java/com/cpf/core/api/tabular/CpfTabularReader.java',
 'cpf-common/src/main/java/com/cpf/common/cache/CpfRedisCacheProvider.java',
 'cpf-admin/src/main/java/com/cpf/admin/opr/filejob/AdmFileJobService.java',
 'cpf-biz-admin/frontend/src/components/CpfTreeNode.vue',
 'cpf-local-runtime/src/main/java/com/cpf/local/runtime/CpfLocalRuntimeSafetyGuard.java',
]
missing=[x for x in required if not (root/x).exists()]
if missing: fail('required-artifacts','; '.join(missing))
else: ok('required-artifacts',str(len(required)))

# Scenario contract
mp=root/'cpf-docs/quality/qa-20260729/CPF_QA_387_SCENARIO_CLOSURE_MATRIX_20260729_04.csv'
try:
    with mp.open(encoding='utf-8-sig',newline='') as f: rows=list(csv.DictReader(f))
    ids=[r['id'] for r in rows]
    if len(rows)!=387: fail('scenario-count',str(len(rows)))
    elif len(set(ids))!=387: fail('scenario-id-unique',f'unique={len(set(ids))}')
    elif any(r['development_preparation_status']!='완료' or r['execution_status']!='미검증' for r in rows):
        fail('scenario-status-contract','definition must be 완료 and unexecuted result must remain 미검증')
    else:
        ok('scenario-count','387'); ok('scenario-id-unique','387'); ok('scenario-status-contract','완료/미검증')
except Exception as e: fail('scenario-matrix-read',repr(e))

# Java package/path and compile public core API
java_files=list(root.rglob('*.java'))
path_errors=[]
for p in java_files:
    t=p.read_text(encoding='utf-8',errors='replace')
    m=re.search(r'^package\s+([\w.]+);',t,re.M)
    if not m: path_errors.append(f'no package: {p.relative_to(root)}'); continue
    suffix=str(Path(*m.group(1).split('.'))/p.name).replace('\\','/')
    if not str(p).replace('\\','/').endswith(suffix): path_errors.append(f'path mismatch: {p.relative_to(root)} -> {m.group(1)}')
if path_errors: fail('java-package-path','; '.join(path_errors[:20]))
else: ok('java-package-path',str(len(java_files)))

core_api_dirs=[root/'cpf-core/src/main/java/com/cpf/core/api/cache',root/'cpf-core/src/main/java/com/cpf/core/api/tabular']
core_api_files=[p for d in core_api_dirs if d.exists() for p in d.rglob('*.java')]
if core_api_files:
    with tempfile.TemporaryDirectory() as td:
        cmd=['javac','--release','21','-d',td]+[str(p) for p in core_api_files]
        cp=subprocess.run(cmd,capture_output=True,text=True)
        if cp.returncode: fail('cpf-core-cache-tabular-api-javac',cp.stderr[-4000:])
        else: ok('cpf-core-cache-tabular-api-javac',f'{len(core_api_files)} source files')

# JSON/Vue
json_errors=[]
for p in root.rglob('*.json'):
    try: json.loads(p.read_text(encoding='utf-8'))
    except Exception as e: json_errors.append(f'{p.relative_to(root)}: {e}')
if json_errors: fail('json-parse','; '.join(json_errors[:20]))
else: ok('json-parse',str(len(list(root.rglob('*.json')))))
vue_errors=[]
for p in root.rglob('*.vue'):
    t=p.read_text(encoding='utf-8',errors='replace')
    if len(re.findall(r'^<template(?:\s|>)',t,re.M))!=1: vue_errors.append(f'template: {p.relative_to(root)}')
    if not re.search(r'^<script(?:\s|>)',t,re.M): vue_errors.append(f'script: {p.relative_to(root)}')
if vue_errors: fail('vue-sfc-structure','; '.join(vue_errors))
else: ok('vue-sfc-structure',str(len(list(root.rglob('*.vue')))))

# Vendor parity
miss=[]
for v in ['mariadb','postgresql','oracle']:
    for d,names in [('flyway',['V69__enterprise_cache_file_job.sql','V70__bza_action_permission_hardening.sql']),('rollback',['R69__enterprise_cache_file_job.sql','R70__bza_action_permission_hardening.sql'])]:
        for n in names:
            p=root/f'cpf-tools/db/vendor/{v}/source/migration/{d}/{n}'
            if not p.exists(): miss.append(str(p.relative_to(root)))
if miss: fail('db-vendor-parity','; '.join(miss))
else: ok('db-vendor-parity','Oracle/PostgreSQL/MariaDB V69/V70/R69/R70')

# Hygiene and safety
bad=[]
for p in files:
    rel=str(p.relative_to(root)).replace('\\','/')
    if p.suffix.lower() in {'.class','.jar','.tmp','.bak','.zip'}: bad.append(rel)
    if p.suffix.lower()=='.log' and not rel.startswith('cpf-docs/evidence/'): bad.append(rel)
    if '/build/' in '/'+rel or '/tmp/' in '/'+rel: bad.append(rel)
if bad: fail('repository-hygiene','; '.join(sorted(set(bad))[:40]))
else: ok('repository-hygiene','no build/log/tmp/archive payload')

text_ext={'.ps1','.yml','.yaml','.gradle','.java','.sql','.json'}
excluded=[]; external=[]; secrets=[]
for p in files:
    if p.suffix.lower() not in text_ext: continue
    t=p.read_text(encoding='utf-8',errors='ignore')
    rel=str(p.relative_to(root))
    if p.name != 'verify-20260729-04-intermediate-overlay.py' and re.search(r'\b(?:mssql|sqlserver|mysql|h2)\b',t,re.I): excluded.append(rel)
    if p.suffix.lower() in {'.vue','.ts','.html'} and re.search(r'https?://(?:cdn|fonts\.|unpkg|jsdelivr)',t,re.I): external.append(rel)
    if re.search(r'AKIA[0-9A-Z]{16}|-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----',t): secrets.append(rel)
if excluded: fail('official-db-policy','; '.join(excluded[:20]))
else: ok('official-db-policy','Oracle/PostgreSQL/MariaDB only')
if external: fail('external-runtime-assets','; '.join(external[:20]))
else: ok('external-runtime-assets','0')
if secrets: fail('secret-patterns','; '.join(secrets[:20]))
else: ok('secret-patterns','0')

for name,status,detail in checks:
    print(f'[{status}] {name}: {detail}')
if errors:
    print(f'RESULT=FAIL errors={len(errors)}')
    sys.exit(1)
print(f'RESULT=PASS checks={len(checks)}')
