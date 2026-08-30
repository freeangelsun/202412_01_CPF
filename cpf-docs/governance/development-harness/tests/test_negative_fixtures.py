#!/usr/bin/env python3
from pathlib import Path
import csv,json,tempfile,shutil,subprocess,sys,os
ROOT=Path(__file__).resolve().parents[4]; H=ROOT/'cpf-docs/governance/development-harness'
c=json.loads((H/'contracts/contract-registry.json').read_text(encoding='utf-8'))
checks=[]
_NEG_GROUP=os.environ.get('CPF_HARNESS_NEGATIVE_GROUP','ALL').strip().upper() or 'ALL'
_NEG_GROUPS={
    'BASE': {
        'mutation_empty_requirement_registry','mutation_self_migration','mutation_role_false_pass','mutation_vscode_nonzero',
    },
    'AUTH_A': {
        'mutation_drop_single_canonical_requirement','mutation_product_contract_semantic_damage','mutation_canonical_trace_loss',
        'mutation_current_authority_source_identity_drift','mutation_control_false_pass_without_evidence',
    },
    'AUTH_B': {
        'mutation_harness_package_garbage','mutation_current_package_projection_stale','mutation_handover_registry_alias_loss',
        'mutation_repository_python_cache_reentry',
        'mutation_transitive_migration_terminal_missing','mutation_deprecated_active_reference_reentry',
    },
    'STRENGTH': {
        'mutation_harness_strength_tracking_reduction','mutation_harness_strength_evidence_reduction',
        'mutation_protected_retain_delete_reentry','mutation_session_manifest_missing','mutation_toolchain_exact_host_patch_reentry','mutation_toolchain_exact_java_host_major_reentry',
    },
}
def _enabled(name):
    if _NEG_GROUP=='ALL': return True
    if _NEG_GROUP=='BASE': return not name.startswith('mutation_') or name in _NEG_GROUPS['BASE']
    return name in _NEG_GROUPS.get(_NEG_GROUP,set())

def record(name,ok,detail=''):
    if not _enabled(name): return
    checks.append((name,ok,detail)); print(('PASS' if ok else 'FAIL'),name,detail,flush=True)
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

# Session Merge Protocol negative mutation is exercised from a known-positive isolated fixture.
# This prevents an already-pending real session from making the mutation look green by accident.
def _session_merge_missing_manifest_mutation():
    import hashlib
    root=Path(tempfile.mkdtemp(prefix='cpf-session-merge-negative-'))
    try:
        h=root/'cpf-docs/governance/development-harness'
        (h/'current').mkdir(parents=True)
        d=h/'evidence/claude/current/sessions/FIXTURE_SESSION'
        d.mkdir(parents=True)
        (h/'current/CURRENT_WORK_ITEM_REGISTRY.csv').write_text('work_item_id\nWP-H00\n',encoding='utf-8-sig')
        key='FIXTURE_SESSION'
        digest=hashlib.sha256(key.encode('utf-8')).hexdigest()
        (h/'CPF_DEVELOPMENT_HARNESS.md').write_text(
            '### Current Merge Control State\n\nstate: current/CURRENT_MERGE_CONTROL_STATE.json\n\n## 21. roles\n',
            encoding='utf-8'
        )
        (h/'current/CURRENT_MERGE_CONTROL_STATE.json').write_text(json.dumps({
            'merge_protocol_version':'1','merge_baseline_source_identity':'1'*64,'last_merged_session_key':key,
            'merged_session_set_digest':digest,'pending_session_keys':'NONE','conflict_session_keys':'NONE',
            'last_merge_review_at':'2026-08-30T14:00:00+09:00','last_merge_reviewer_session_key':'HARNESS_FIXTURE'
        },ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
        report=d/'SESSION_REPORT.md'
        report.write_text('# Report\n\n## WI-WP-H00\n',encoding='utf-8')
        ev=d/'evidence.txt'
        ev.write_text('PASS\n',encoding='utf-8')
        rel=lambda x: x.relative_to(root).as_posix()
        manifest={
          'schemaVersion':1,'sessionKey':key,'role':'CLAUDE','startedAt':'2026-08-30T14:00:00+09:00','endedAt':'2026-08-30T14:01:00+09:00',
          'sourceIdentity':'1'*64,'sourceBasis':'fixture','registrySha256AtStart':'2'*64,
          'reportPath':rel(report),'reportSha256':hashlib.sha256(report.read_bytes()).hexdigest(),
          'workItems':[{'workItemId':'WP-H00','proposedStatus':'SOURCE_FIXED','evidence':[rel(ev)],'reportAnchor':'WI-WP-H00',
                        'acceptanceMapping':{'prerequisiteSource':'fixture','requiredEnvironment':'fixture','actualEnvironment':'fixture'}}],
          'evidenceFiles':[{'path':rel(ev),'sha256':hashlib.sha256(ev.read_bytes()).hexdigest(),'purpose':'fixture'}],
          'gitWriteExecuted':False,'mergeStatus':'MERGED','mergedBySessionKey':'HARNESS_FIXTURE','mergedAt':'2026-08-30T14:02:00+09:00','mergeTargetSourceIdentity':'1'*64,
          'pendingReasons':[],'conflicts':[],'rerunConditions':[]}
        mf=d/'SESSION_MANIFEST.json'
        mf.write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
        env=os.environ.copy()
        env['CPF_HARNESS_ROOT']=str(h)
        env['CPF_REPOSITORY_ROOT']=str(root)
        validator=H/'validators/validate_session_merge_protocol.py'
        before=subprocess.run([sys.executable,'-B',str(validator)],cwd=ROOT,env=env,text=True,capture_output=True)
        mf.unlink()
        after=subprocess.run([sys.executable,'-B',str(validator)],cwd=ROOT,env=env,text=True,capture_output=True)
        return before.returncode==0 and after.returncode!=0 and 'MANIFEST_MISSING:FIXTURE_SESSION' in (after.stdout+after.stderr)
    finally:
        shutil.rmtree(root,ignore_errors=True)
if _enabled('mutation_session_manifest_missing'):
    record('mutation_session_manifest_missing',_session_merge_missing_manifest_mutation(),'positive fixture -> missing manifest must fail closed')

# Executable Delete Manifest must contain only approved/delete-eligible paths. Protected-retain
# provenance lives only in Migration Map/Semantic Ledger so unrelated protected documentation
# changes cannot block or be targeted by legacy cleanup.
with (H/'DELETE_MANIFEST.csv').open(encoding='utf-8-sig',newline='') as f:
    _delete_rows=list(csv.DictReader(f))
_protected_prefixes=('cpf-docs/deliverables/','cpf-docs/guides/','cpf-docs/environment/docker/','cpf-tools/environment/docker-development-test/','cpf-docs/governance/documentation-harness/')
record('delete_manifest_all_rows_executable', all(r.get('delete_eligible')=='true' and r.get('approved')=='true' and r.get('user_approved')=='true' for r in _delete_rows))
record('delete_manifest_protected_retain_zero', not any(r.get('path','').startswith(_protected_prefixes) for r in _delete_rows))

# 대용량 Current Dataset(약 220MB)을 mutation마다 byte-copy하면 로컬/CI에서 검증 자체가
# timeout될 수 있다. 검수 강도는 그대로 유지하고 동일 파일시스템에서는 hard-link clone을
# 사용한 뒤 mutation 대상 파일만 copy-on-write로 분리한다. hard-link가 지원되지 않으면
# shutil.copy2로 자동 fallback한다.
def _link_or_copy(src, dst):
    try:
        os.link(src, dst)
        return dst
    except OSError:
        return shutil.copy2(src, dst)

def _clone_harness(target: Path):
    shutil.copytree(H, target, copy_function=_link_or_copy)

def _detach(target: Path, *relative_paths: str):
    for rel in relative_paths:
        p=target/rel
        if not p.exists() or not p.is_file():
            continue
        data=p.read_bytes()
        p.unlink()
        p.write_bytes(data)

# Mutation tests use one reusable hard-link fixture. This preserves the exact mutation/validator
# strength while avoiding 220MB Harness tree traversal for every single negative case.
_MUTABLE_RELATIVE_PATHS=(
    'current/CANONICAL_PRODUCT_REQUIREMENTS.csv',
    'CANONICAL_MIGRATION_MAP.csv',
    'current/ROLE_EXECUTION_LEDGER.csv',
    'product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md',
    'current/CANONICAL_REQUIREMENT_TRACE.csv',
    'current/CURRENT_WORK_ITEM_REGISTRY.csv',
    'current/CONTROL_EXECUTION_LEDGER.csv',
    'current/PACKAGE_MANIFEST.json',
    'current/CPF_DEVELOPMENT_HANDOVER.md',
    'contracts/contract-registry.json',
    'DELETE_MANIFEST.csv',
)
_NEG_ROOT=Path(tempfile.mkdtemp(prefix='cpf-harness-negative-shared-'))
_NEG_TARGET=_NEG_ROOT/'cpf-docs/governance/development-harness'
_NEG_TARGET.parent.mkdir(parents=True,exist_ok=True)
_clone_harness(_NEG_TARGET)
# Strength validator also consumes the canonical toolchain compatibility policy outside Harness.
# Copy that single authority into the isolated negative fixture so mutations test the rule itself,
# not an unrelated missing-file condition.
_policy_src=ROOT/'cpf-tools/verification/contracts/cpf-toolchain-compatibility.json'
_policy_dst=_NEG_ROOT/'cpf-tools/verification/contracts/cpf-toolchain-compatibility.json'
_policy_dst.parent.mkdir(parents=True,exist_ok=True)
shutil.copy2(_policy_src,_policy_dst)

# migration replacements can point outside Harness; create only harmless placeholders required
# for the migration structure validator. They never count as product evidence.
with (_NEG_TARGET/'CANONICAL_MIGRATION_MAP.csv').open(encoding='utf-8-sig',newline='') as f:
    _NEG_MIGRATION_ROWS=list(csv.DictReader(f))
for _r in _NEG_MIGRATION_ROWS:
    _p=_NEG_ROOT/_r['new_path']; _p.parent.mkdir(parents=True,exist_ok=True)
    if not _p.exists() and not str(_p).startswith(str(_NEG_TARGET)+os.sep):
        _p.write_text('fixture',encoding='utf-8')

def _restore_negative_fixture():
    for rel in _MUTABLE_RELATIVE_PATHS:
        src=H/rel; dst=_NEG_TARGET/rel
        if not src.exists():
            if dst.exists():
                if dst.is_dir(): shutil.rmtree(dst)
                else: dst.unlink()
            continue
        dst.parent.mkdir(parents=True,exist_ok=True)
        if dst.exists():
            if dst.is_dir(): shutil.rmtree(dst)
            else: dst.unlink()
        shutil.copy2(src,dst)
    garbage=_NEG_TARGET/'.pytest_cache'
    if garbage.exists(): shutil.rmtree(garbage)
    deprecated_fixture=_NEG_ROOT/'cpf-tools/deprecated-reentry-fixture.py'
    if deprecated_fixture.exists(): deprecated_fixture.unlink()

def run_mut(name, mutate, expected_fragment):
    if not _enabled(name): return
    _restore_negative_fixture()
    mutate(_NEG_TARGET)
    cp=subprocess.run([sys.executable,'-B',str(_NEG_TARGET/'validators/validate_development_harness.py')],cwd=_NEG_ROOT,text=True,capture_output=True)
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
    if not _enabled(name): return
    _restore_negative_fixture()
    mutate(_NEG_TARGET)
    cp=subprocess.run([sys.executable,'-B',str(_NEG_TARGET/'validators/validate_harness_authority.py')],cwd=_NEG_ROOT,text=True,capture_output=True)
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


def mut_repository_python_cache(h):
    # GARBAGE-0637: regeneratable Python bytecode 는 Harness payload 밖에서도 product source 에 남으면 안 된다.
    d=_NEG_ROOT/'cpf-tools/__pycache__'; d.mkdir(parents=True,exist_ok=True)
    (d/'stale_module.cpython-313.pyc').write_bytes(b'cpf-stale-bytecode')
run_auth_mut('mutation_repository_python_cache_reentry',mut_repository_python_cache,'REPOSITORY_PYTHON_CACHE')


def mut_package_projection_stale(h):
    p=h/'current/PACKAGE_MANIFEST.json'; data=json.loads(p.read_text(encoding='utf-8')); data['currentSourceIdentity']='0'*64; p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
run_auth_mut('mutation_current_package_projection_stale',mut_package_projection_stale,'CURRENT_PACKAGE_SOURCE_IDENTITY_STALE')

def mut_handover_registry_alias_loss(h):
    p=h/'current/CURRENT_WORK_ITEM_REGISTRY.csv'
    with p.open(encoding='utf-8-sig',newline='') as f: dr=csv.DictReader(f); hdr=dr.fieldnames; rr=list(dr)
    for r in rr:
        aliases=[x for x in r.get('handover_aliases','').split(';') if x and x!='WP-R01.21']
        r['handover_aliases']=';'.join(aliases)
    with p.open('w',encoding='utf-8-sig',newline='') as f: w=csv.DictWriter(f,fieldnames=hdr);w.writeheader();w.writerows(rr)
run_auth_mut('mutation_handover_registry_alias_loss',mut_handover_registry_alias_loss,'HANDOVER_REGISTRY_CONSISTENCY')

def mut_transitive_migration_terminal_missing(h):
    p=h/'CANONICAL_MIGRATION_MAP.csv'
    with p.open(encoding='utf-8-sig',newline='') as f: dr=csv.DictReader(f); hdr=dr.fieldnames; rr=list(dr)
    rr[0]['new_path']='cpf-docs/governance/development-harness/missing-terminal.md'
    with p.open('w',encoding='utf-8-sig',newline='') as f: w=csv.DictWriter(f,fieldnames=hdr);w.writeheader();w.writerows(rr)
run_auth_mut('mutation_transitive_migration_terminal_missing',mut_transitive_migration_terminal_missing,'MIGRATION_TRANSITIVE_TERMINAL_MISSING')

def mut_deprecated_active_reference_reentry(h):
    root=h.parents[2]; p=root/'cpf-tools/deprecated-reentry-fixture.py'; p.parent.mkdir(parents=True,exist_ok=True); p.write_text("OLD='cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md'\n",encoding='utf-8')
run_auth_mut('mutation_deprecated_active_reference_reentry',mut_deprecated_active_reference_reentry,'DEPRECATED_ACTIVE_REFERENCE')


# Harness 현행화 자체도 regression 대상이다. 기존 범위/증거 강도를 낮추면 FAIL해야 한다.
def run_strength_mut(name, mutate, expected_fragment):
    if not _enabled(name): return
    _restore_negative_fixture()
    mutate(_NEG_TARGET)
    cp=subprocess.run([sys.executable,'-B',str(_NEG_TARGET/'validators/validate_harness_strength_regression.py')],cwd=_NEG_ROOT,text=True,capture_output=True)
    ok=cp.returncode!=0 and expected_fragment in (cp.stdout+cp.stderr)
    record(name,ok,'rc='+str(cp.returncode)+' expected='+expected_fragment)

def mut_tracking_scope_reduced(h):
    p=h/'current/CURRENT_WORK_ITEM_REGISTRY.csv'
    with p.open(encoding='utf-8-sig',newline='') as f: dr=csv.DictReader(f); hdr=dr.fieldnames; rr=list(dr)
    for i,r in enumerate(rr):
        if r.get('item_role','TRACKING')=='TRACKING': rr.pop(i); break
    with p.open('w',encoding='utf-8-sig',newline='') as f: w=csv.DictWriter(f,fieldnames=hdr);w.writeheader();w.writerows(rr)
run_strength_mut('mutation_harness_strength_tracking_reduction',mut_tracking_scope_reduced,'TRACKING_WORK_REDUCED')

def mut_toolchain_exact_host_patch(h):
    root=h.parents[2]; p=root/'cpf-tools/verification/contracts/cpf-toolchain-compatibility.json'
    data=json.loads(p.read_text(encoding='utf-8'));data['tools']['npm']['exactPatchRequired']=True
    p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
run_strength_mut('mutation_toolchain_exact_host_patch_reentry',mut_toolchain_exact_host_patch,'HOST_EXACT_PATCH_PIN_REINTRODUCED')

def mut_toolchain_exact_java_host_major(h):
    root=h.parents[2]; p=root/'cpf-tools/verification/contracts/cpf-toolchain-compatibility.json'
    data=json.loads(p.read_text(encoding='utf-8'));data['tools']['java']['maxMajor']=25
    p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
run_strength_mut('mutation_toolchain_exact_java_host_major_reentry',mut_toolchain_exact_java_host_major,'HOST_JAVA_EXACT_MAJOR_PIN_REINTRODUCED')


def mut_test_evidence_reduced(h):
    p=h/'contracts/contract-registry.json'; data=json.loads(p.read_text(encoding='utf-8'))
    data['requiredTestEvidence']=data['requiredTestEvidence'][:-1]
    p.write_text(json.dumps(data,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
run_strength_mut('mutation_harness_strength_evidence_reduction',mut_test_evidence_reduced,'HARNESS_STRENGTH_REDUCED requiredTestEvidence')

def mut_protected_retain_delete_reentry(h):
    p=h/'DELETE_MANIFEST.csv'
    with p.open(encoding='utf-8-sig',newline='') as f: dr=csv.DictReader(f); hdr=dr.fieldnames; rr=list(dr)
    rr.append({
        'path':'cpf-docs/deliverables/PROTECTED_RETAIN_FIXTURE.md','type':'FILE','approved':'false','user_approved':'false',
        'precondition':'PROTECTED_PATH_RETAIN','lifecycle':'PROTECTED_RETAIN',
        'replacement_path':'cpf-docs/governance/development-harness/CPF_DEVELOPMENT_HARNESS.md',
        'expected_sha256':'0'*64,'semantic_status':'PASS','delete_eligible':'false','reason':'negative fixture'
    })
    with p.open('w',encoding='utf-8-sig',newline='') as f: w=csv.DictWriter(f,fieldnames=hdr);w.writeheader();w.writerows(rr)
run_mut('mutation_protected_retain_delete_reentry',mut_protected_retain_delete_reentry,'DELETE_MIGRATION_PATH_SET_MISMATCH')

failed=[x for x in checks if not x[1]]
print(f'NEGATIVE_FIXTURES_FINAL={len(checks)-len(failed)}/{len(checks)} PASS group={_NEG_GROUP}',flush=True)
# Explicitly remove the reusable scratch fixture before interpreter shutdown. This avoids
# platform-dependent TemporaryDirectory finalizer delays while keeping repository garbage=0.
shutil.rmtree(_NEG_ROOT, ignore_errors=True)
raise SystemExit(1 if failed else 0)
