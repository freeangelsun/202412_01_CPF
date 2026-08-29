#!/usr/bin/env python3
from pathlib import Path
import csv,json,tempfile,shutil,subprocess,sys,os
ROOT=Path(__file__).resolve().parents[4]; H=ROOT/'cpf-docs/governance/development-harness'
c=json.loads((H/'contracts/contract-registry.json').read_text(encoding='utf-8'))
checks=[]
def record(name,ok,detail=''):
    checks.append((name,ok,detail)); print(('PASS' if ok else 'FAIL'),name,detail)
record('forbid_not_executed','NOT_EXECUTED' in c['forbiddenCompletionSignals'])
record('forbid_unknown','UNKNOWN' in c['forbiddenCompletionSignals'])
record('forbid_fail','FAIL' in c['forbiddenCompletionSignals'])
record('profiles_nonempty',bool(c['environmentProfiles']))
record('db_vendors_nonempty',bool(c['officialDbVendors']))
record('roles_three_way',set(c['roles'])=={'DEVGPT','INDEPENDENT_REVIEWER','QA'})
record('codex_claude_same_role',{'Codex','Claude'}.issubset(set(c['roles']['INDEPENDENT_REVIEWER']['equivalentActors'])))
record('required_completion_evidence',len(c['requiredCompletionEvidence'])>=8)
record('required_test_evidence',len(c['requiredTestEvidence'])>=10)
record('reviewer_vscode_zero_gate_fields',set(c['independentReviewerSourceModificationEvidence']) >= {'source_modified','vscode_fresh_import','vscode_scope','vscode_problems_json','vscode_error_count','vscode_warning_count'})
with (H/'current/CANONICAL_PRODUCT_REQUIREMENTS.csv').open(encoding='utf-8-sig',newline='') as f: n=sum(1 for _ in csv.DictReader(f))
record('non_vacuous_product_registry',n>0)

# Mutation tests: copy only Harness and create a minimal root layout so validator resolves replacement paths.
def run_mut(name, mutate, expected_fragment):
    with tempfile.TemporaryDirectory(prefix='cpf-harness-neg-') as td:
        root=Path(td); target=root/'cpf-docs/governance/development-harness'; target.parent.mkdir(parents=True); shutil.copytree(H,target,copy_function=shutil.copy2)
        # migration replacements can point inside harness; any external replacement gets a harmless file placeholder.
        with (target/'CANONICAL_MIGRATION_MAP.csv').open(encoding='utf-8-sig',newline='') as f: mm=list(csv.DictReader(f))
        for r in mm:
            p=root/r['new_path']; p.parent.mkdir(parents=True,exist_ok=True)
            if not p.exists() and not str(p).startswith(str(target)+os.sep): p.write_text('fixture',encoding='utf-8')
        mutate(target)
        cp=subprocess.run([sys.executable,str(target/'validators/validate_development_harness.py')],cwd=root,text=True,capture_output=True)
        ok=cp.returncode!=0 and expected_fragment in (cp.stdout+cp.stderr)
        record(name,ok,('rc='+str(cp.returncode)+' expected='+expected_fragment))

def mut_empty_req(h):
    p=h/'current/CANONICAL_PRODUCT_REQUIREMENTS.csv'; data=p.read_bytes(); p.unlink(); p.write_bytes(data)
    with p.open(encoding='utf-8-sig',newline='') as f: hdr=next(csv.reader(f))
    with p.open('w',encoding='utf-8-sig',newline='') as f: csv.writer(f).writerow(hdr)
run_mut('mutation_empty_requirement_registry',mut_empty_req,'PRODUCT_REQUIREMENT_REGISTRY_EMPTY_OR_DUPLICATE')

def mut_self_migration(h):
    p=h/'CANONICAL_MIGRATION_MAP.csv'; data=p.read_bytes(); p.unlink(); p.write_bytes(data)
    with p.open(encoding='utf-8-sig',newline='') as f: dr=csv.DictReader(f); hdr=dr.fieldnames; rr=list(dr)
    rr[0]['new_path']=rr[0]['old_path']
    with p.open('w',encoding='utf-8-sig',newline='') as f: w=csv.DictWriter(f,fieldnames=hdr);w.writeheader();w.writerows(rr)
run_mut('mutation_self_migration',mut_self_migration,'SELF_MIGRATION')

def mut_false_pass(h):
    p=h/'current/ROLE_EXECUTION_LEDGER.csv'; data=p.read_bytes(); p.unlink(); p.write_bytes(data)
    with p.open(encoding='utf-8-sig',newline='') as f: dr=csv.DictReader(f); hdr=dr.fieldnames; rr=list(dr)
    rr[0]['execution_status']='PASS'; rr[0]['completion_reason']=''; rr[0]['command']=''; rr[0]['evidence']=''
    with p.open('w',encoding='utf-8-sig',newline='') as f: w=csv.DictWriter(f,fieldnames=hdr);w.writeheader();w.writerows(rr)
run_mut('mutation_role_false_pass',mut_false_pass,'FALSE_PASS')

def mut_vscode_false_pass(h):
    p=h/'current/ROLE_EXECUTION_LEDGER.csv'; data=p.read_bytes(); p.unlink(); p.write_bytes(data)
    with p.open(encoding='utf-8-sig',newline='') as f: dr=csv.DictReader(f); hdr=dr.fieldnames; rr=list(dr)
    x=next(r for r in rr if r['role']=='INDEPENDENT_REVIEWER')
    for k in c['requiredCompletionEvidence']: x[k]='fixture'
    x['execution_status']='PASS';x['source_modified']='true';x['vscode_fresh_import']='true';x['vscode_scope']='all';x['vscode_problems_json']='fixture.json';x['vscode_error_count']='1';x['vscode_warning_count']='0'
    with p.open('w',encoding='utf-8-sig',newline='') as f: w=csv.DictWriter(f,fieldnames=hdr);w.writeheader();w.writerows(rr)
run_mut('mutation_vscode_nonzero',mut_vscode_false_pass,'FALSE_PASS_VSCODE_DIAGNOSTIC')

print(f'NEGATIVE_FIXTURES_BASE={sum(1 for x in checks if x[1])}/{len(checks)} PASS')

# Authority/semantic negative mutations: the Harness must fail even when its basic structure still looks valid.
def run_auth_mut(name, mutate, expected_fragment):
    with tempfile.TemporaryDirectory(prefix='cpf-harness-auth-neg-') as td:
        root=Path(td); target=root/'cpf-docs/governance/development-harness'; target.parent.mkdir(parents=True); shutil.copytree(H,target,copy_function=shutil.copy2)
        # split master part paths resolve from repository root; materialize placeholders by copying only referenced parts from Harness.
        mutate(target)
        cp=subprocess.run([sys.executable,str(target/'validators/validate_harness_authority.py')],cwd=root,text=True,capture_output=True)
        ok=cp.returncode!=0 and expected_fragment in (cp.stdout+cp.stderr)
        record(name,ok,'rc='+str(cp.returncode)+' expected='+expected_fragment)

def mut_drop_one_canonical(h):
    p=h/'current/CANONICAL_PRODUCT_REQUIREMENTS.csv'
    with p.open(encoding='utf-8-sig',newline='') as f: dr=csv.DictReader(f); hdr=dr.fieldnames; rr=list(dr)
    rr=rr[1:]
    with p.open('w',encoding='utf-8-sig',newline='') as f: w=csv.DictWriter(f,fieldnames=hdr);w.writeheader();w.writerows(rr)
run_auth_mut('mutation_drop_single_canonical_requirement',mut_drop_one_canonical,'PRODUCT_REQUIREMENT_SET_DRIFT')

def mut_product_text(h):
    p=h/'product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md'; t=p.read_text(encoding='utf-8'); p.write_text(t.replace('CPF를 샘플이나 공통 라이브러리가 아닌','CPF를 임시 샘플 프레임워크로',1),encoding='utf-8')
run_auth_mut('mutation_product_contract_semantic_damage',mut_product_text,'PRODUCT_CONTRACT_UNAPPROVED_DRIFT')

def mut_trace_drop(h):
    p=h/'current/CANONICAL_REQUIREMENT_TRACE.csv'
    with p.open(encoding='utf-8-sig',newline='') as f: dr=csv.DictReader(f); hdr=dr.fieldnames; rr=list(dr)
    with p.open('w',encoding='utf-8-sig',newline='') as f: w=csv.DictWriter(f,fieldnames=hdr);w.writeheader();w.writerows(rr[:-1])
run_auth_mut('mutation_canonical_trace_loss',mut_trace_drop,'CANONICAL_TRACE_SET_DRIFT')

def mut_identity(h):
    p=h/'current/CURRENT_WORK_ITEM_REGISTRY.csv'
    with p.open(encoding='utf-8-sig',newline='') as f: dr=csv.DictReader(f); hdr=dr.fieldnames; rr=list(dr)
    rr[0]['source_identity']='0'*64
    with p.open('w',encoding='utf-8-sig',newline='') as f: w=csv.DictWriter(f,fieldnames=hdr);w.writeheader();w.writerows(rr)
run_auth_mut('mutation_current_authority_source_identity_drift',mut_identity,'AUTHORITY_SOURCE_IDENTITY_DRIFT')

def mut_control_false_pass(h):
    p=h/'current/CONTROL_EXECUTION_LEDGER.csv'
    with p.open(encoding='utf-8-sig',newline='') as f: dr=csv.DictReader(f); hdr=dr.fieldnames; rr=list(dr)
    rr[0]['status']='PASS';rr[0]['evidence']=''
    with p.open('w',encoding='utf-8-sig',newline='') as f: w=csv.DictWriter(f,fieldnames=hdr);w.writeheader();w.writerows(rr)
run_auth_mut('mutation_control_false_pass_without_evidence',mut_control_false_pass,'CONTROL_FALSE_PASS')

def mut_harness_garbage(h):
    p=h/'.pytest_cache';p.mkdir();(p/'garbage.txt').write_text('x',encoding='utf-8')
run_auth_mut('mutation_harness_package_garbage',mut_harness_garbage,'HARNESS_GARBAGE')

failed=[x for x in checks if not x[1]]
print(f'NEGATIVE_FIXTURES_FINAL={len(checks)-len(failed)}/{len(checks)} PASS')
raise SystemExit(1 if failed else 0)
