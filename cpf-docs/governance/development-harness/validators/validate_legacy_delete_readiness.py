#!/usr/bin/env python3
from pathlib import Path
import csv, hashlib, json, re, subprocess, sys
ROOT=Path(__file__).resolve().parents[4]
H=ROOT/'cpf-docs/governance/development-harness'
C=H/'current'
errors=[]

def rows(p):
    with p.open(encoding='utf-8-sig',newline='') as f:
        return list(csv.DictReader(f))

def sha(p):
    return hashlib.sha256(p.read_bytes()).hexdigest()

def err(msg):
    errors.append(msg)

def run_gate(rel):
    cp=subprocess.run([sys.executable,str(H/rel)],cwd=ROOT,text=True,capture_output=True)
    if cp.returncode:
        err(f'REQUIRED_GATE_FAIL {rel} rc={cp.returncode}')
        if cp.stdout: print(cp.stdout,end='' if cp.stdout.endswith('\n') else '\n')
        if cp.stderr: print(cp.stderr,end='' if cp.stderr.endswith('\n') else '\n')

# Deletion readiness is intentionally independent from whole-source identity so unrelated parallel
# source/documentation work cannot authorize or block a legacy cleanup. It still runs every
# deletion-specific structural/semantic/strength gate and never promotes project completion.
for rel in (
    'validators/validate_current_product_conformance.py',
    'validators/validate_harness_strength_regression.py',
    'validators/validate_migration_semantic_closure.py',
    'validators/validate_development_harness.py',
):
    run_gate(rel)

req=rows(C/'CANONICAL_PRODUCT_REQUIREMENTS.csv')
if len(req)!=218 or len({r.get('requirement_id','') for r in req})!=218:
    err(f'CANONICAL_REQUIREMENT_SET_BAD rows={len(req)} unique={len({r.get("requirement_id","") for r in req})}')

dm=rows(H/'DELETE_MANIFEST.csv')
mm=rows(H/'CANONICAL_MIGRATION_MAP.csv')
mp={r['old_path']:r for r in mm}
protected_prefixes=(
    'cpf-docs/deliverables/','cpf-docs/guides/','cpf-docs/environment/docker/',
    'cpf-tools/environment/docker-development-test/','cpf-docs/governance/documentation-harness/'
)
expected={r['old_path'] for r in mm if r.get('delete_eligible')=='true'}
actual={r['path'] for r in dm}
if actual!=expected:
    err(f'DELETE_EXECUTABLE_SET_DRIFT missing={len(expected-actual)} extra={len(actual-expected)}')
for r in dm:
    rel=r['path'].replace('\\','/').lstrip('/')
    if rel.startswith(protected_prefixes): err('PROTECTED_PATH_IN_DELETE_MANIFEST '+rel)
    if r.get('approved')!='true' or r.get('user_approved')!='true' or r.get('delete_eligible')!='true':
        err('DELETE_NOT_APPROVED_OR_ELIGIBLE '+rel)
    if r.get('semantic_status')!='PASS' or r.get('precondition')!='HARNESS_AUTHORITY_AND_MIGRATION_SEMANTIC_GATE_PASS':
        err('DELETE_SEMANTIC_PRECONDITION_BAD '+rel)
    repl=ROOT/r.get('replacement_path','')
    if not repl.is_file(): err('DELETE_REPLACEMENT_MISSING '+rel+' -> '+r.get('replacement_path',''))
    p=ROOT/rel
    if p.is_file():
        expected_sha=r.get('expected_sha256','').lower()
        actual_sha=sha(p)
        if expected_sha=='already_missing' or expected_sha!=actual_sha:
            err(f'DELETE_SHA256_DRIFT {rel} expected={expected_sha} actual={actual_sha}')

# Active source/docs must not re-introduce deprecated canonical/current authority paths.
deprecated=[
 'cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md',
 'cpf-docs/work/current/CPF_CANONICAL_DEVELOPMENT_CLOSURE_INVENTORY.csv',
 'cpf-docs/work/current/CPF_DEVELOPMENT_QA_CLOSURE.csv',
 'cpf-docs/work/current/CPF_DEVELOPER_REQUIREMENT_REVIEW.csv',
 'cpf-docs/work/current/CPF_DEVELOPER_REQUIREMENT_REVIEW.md',
 'cpf-docs/work/current/CPF_DEVELOPMENT_COMPLETION_REVIEW.csv',
 'cpf-docs/work/current/CPF_DEVELOPMENT_COMPLETION_REVIEW.md',
]
scan_roots=[ROOT/'cpf-tools',ROOT/'cpf-docs/development',ROOT/'cpf-docs/governance/documentation-harness']
for sr in scan_roots:
    if not sr.exists(): continue
    for p in sr.rglob('*'):
        if not p.is_file() or p.suffix.lower() not in {'.py','.ps1','.sh','.md','.json','.txt','.yml','.yaml','.csv','.gradle','.kts','.java'}: continue
        try: body=p.read_text(encoding='utf-8-sig')
        except Exception: continue
        for dep in deprecated:
            if dep in body: err('DEPRECATED_ACTIVE_REFERENCE '+p.relative_to(ROOT).as_posix()+' -> '+dep)

# Detailed Requirement source_basis must stay on the Current Product Contract.
for idx in rows(C/'CPF_REQUIREMENT_MASTER.csv'):
    p=ROOT/idx['part_path']
    if not p.is_file(): err('DETAILED_PART_MISSING '+idx.get('part_path','')); continue
    with p.open(encoding='utf-8-sig',newline='') as f:
        for rr in csv.DictReader(f):
            if 'CPF_FINAL_TARGET_REQUIREMENTS.md::' in (rr.get('source_basis') or ''):
                err('DEPRECATED_DETAILED_SOURCE_BASIS '+rr.get('requirement_id',''))

if errors:
    for e in errors[:300]: print('FAIL',e)
    print(f'LEGACY_DELETE_READINESS=FAIL ERRORS={len(errors)}')
    raise SystemExit(1)
print(f'LEGACY_DELETE_READINESS=PASS CANONICAL=218 DELETE={len(dm)} PROTECTED_IN_DELETE=0 MIGRATION={len(mm)}')
