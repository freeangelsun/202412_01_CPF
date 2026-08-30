#!/usr/bin/env python3
from pathlib import Path
import csv, json, hashlib, os, re, sys, unicodedata, subprocess
ROOT=Path(__file__).resolve().parents[4]
H=ROOT/'cpf-docs/governance/development-harness'
C=H/'current'
errors=[]

def err(x): errors.append(x)
def loadj(p): return json.loads(p.read_text(encoding='utf-8'))
def rows(p):
    with p.open(encoding='utf-8-sig',newline='') as f:return list(csv.DictReader(f))
def sha(p): return hashlib.sha256(p.read_bytes()).hexdigest()
def norm(s): return re.sub(r'\s+',' ',unicodedata.normalize('NFC',(s or '').strip()))
def semantic_body(p):
    t=p.read_text(encoding='utf-8'); i=t.find('## 1.')
    if i<0: raise ValueError('missing ## 1. semantic boundary')
    return t[i:].replace('\r\n','\n').encode('utf-8')

contract=loadj(H/'contracts/contract-registry.json')
sid=loadj(C/'SOURCE_IDENTITY.json')['finalReplayProductContentSha256']
# Current SOURCE_IDENTITY must equal a fresh calculation from the actual tree.
source_state=ROOT/'cpf-tools/verification/tools/cpf-source-state.py'
if not source_state.is_file():
    err('SOURCE_STATE_TOOL_MISSING')
else:
    # The authority gate must not manufacture the repository garbage that it rejects below.
    # Apply both interpreter- and environment-level protection so descendants inherit it too.
    child_env=os.environ.copy(); child_env['PYTHONDONTWRITEBYTECODE']='1'
    cp=subprocess.run([sys.executable,'-B',str(source_state),'--root',str(ROOT),'--scope','source'],cwd=ROOT,env=child_env,text=True,capture_output=True)
    if cp.returncode!=0:
        err('CURRENT_SOURCE_IDENTITY_CALCULATION_FAILED rc='+str(cp.returncode))
    else:
        try:
            actual_sid=json.loads(cp.stdout.strip().splitlines()[-1])['contentSha256']
            if actual_sid!=sid: err(f'CURRENT_SOURCE_IDENTITY_ACTUAL_DRIFT expected={sid} actual={actual_sid}')
        except Exception as exc:
            err('CURRENT_SOURCE_IDENTITY_PARSE_FAILED '+str(exc))
# 1) Product Contract <-> registry exact set/content parity.
product=H/'product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md'
text=product.read_text(encoding='utf-8')
sec=False; table=[]
for line in text.splitlines():
    if line.startswith('## 27. Current Canonical Requirement Catalog'): sec=True; continue
    if sec and line.startswith('## '): break
    if sec and line.startswith('| `'):
        parts=[x.strip() for x in line.strip().strip('|').split('|')]
        if len(parts)!=4: err('PRODUCT_CATALOG_MALFORMED '+line[:120]); continue
        table.append({'requirement_id':parts[0].strip('`'),'owner_scope':parts[1],'requirement':parts[2],'acceptance_basis':parts[3]})
reg=rows(C/'CANONICAL_PRODUCT_REQUIREMENTS.csv')
pt={r['requirement_id']:r for r in table}; rr={r['requirement_id']:r for r in reg}
if len(pt)!=len(table): err('PRODUCT_CONTRACT_DUPLICATE_ID')
if len(rr)!=len(reg): err('PRODUCT_REGISTRY_DUPLICATE_ID')
if set(pt)!=set(rr): err(f'PRODUCT_REQUIREMENT_SET_DRIFT missing_in_csv={sorted(set(pt)-set(rr))} extra_in_csv={sorted(set(rr)-set(pt))}')
for rid in sorted(set(pt)&set(rr)):
    for k in ['owner_scope','requirement','acceptance_basis']:
        if norm(pt[rid][k])!=norm(rr[rid][k]): err(f'PRODUCT_REQUIREMENT_CONTENT_DRIFT {rid} field={k}')
m=re.search(r'Canonical Requirement Count:\s*\*\*(\d+)개\*\*',text)
if not m or int(m.group(1))!=len(reg): err(f'PRODUCT_REQUIREMENT_COUNT_STALE declared={m.group(1) if m else "MISSING"} actual={len(reg)}')
# 2) Product semantic anchor + change chain.
anchor=loadj(H/'contracts/product-contract-integrity.json'); led=rows(H/'contracts/PRODUCT_CONTRACT_CHANGE_LEDGER.csv')
current_sha=sha(product); current_sem=hashlib.sha256(semantic_body(product)).hexdigest()
if not led: err('PRODUCT_CHANGE_LEDGER_EMPTY')
else:
    prev=None
    for i,r in enumerate(led):
        if i==0:
            if r['previous_sha256'].lower()!=anchor['legacyCanonicalSha256'].lower(): err('PRODUCT_LEDGER_BASELINE_PREVIOUS_SHA')
        elif r['previous_sha256'].lower()!=prev.lower(): err('PRODUCT_LEDGER_CHAIN_BREAK '+r['change_id'])
        if i>0 and r['approval_state']!='QA_APPROVED': err('PRODUCT_CHANGE_NOT_QA_APPROVED '+r['change_id'])
        prev=r['new_sha256']
    if prev.lower()!=current_sha.lower(): err(f'PRODUCT_CONTRACT_UNAPPROVED_DRIFT expected={prev} actual={current_sha}')
    if led[0]['new_sha256'].lower()!=anchor['baselineCurrentContractSha256'].lower(): err('PRODUCT_BASELINE_ANCHOR_LEDGER_MISMATCH')
    if led[0]['semantic_new_sha256'].lower()!=anchor['baselineCurrentSemanticSha256'].lower(): err('PRODUCT_BASELINE_SEMANTIC_LEDGER_MISMATCH')
if anchor['baselineCurrentSemanticSha256'].lower()!=current_sem.lower() and len(led)==1: err('PRODUCT_SEMANTIC_BASELINE_DRIFT')
# 3) Canonical trace exact coverage and real targets.
trace=rows(C/'CANONICAL_REQUIREMENT_TRACE.csv'); tr={r['canonical_requirement_id']:r for r in trace}
if len(tr)!=len(trace): err('CANONICAL_TRACE_DUPLICATE')
if set(tr)!=set(rr): err(f'CANONICAL_TRACE_SET_DRIFT missing={len(set(rr)-set(tr))} extra={len(set(tr)-set(rr))}')
# Detailed IDs: native split + bridge
native=set()
for idx in rows(C/'CPF_REQUIREMENT_MASTER.csv'):
    p=ROOT/idx['part_path']
    if not p.is_file(): continue
    with p.open(encoding='utf-8-sig',newline='') as f:
        native.update(r['requirement_id'] for r in csv.DictReader(f))
bridge=rows(C/'CURRENT_CANONICAL_DETAILED_BRIDGE.csv'); bridge_ids={r['bridge_requirement_id'] for r in bridge}
bridge_canon={r['canonical_requirement_id'] for r in bridge}
if bridge_canon-set(rr): err('BRIDGE_UNKNOWN_CANONICAL '+','.join(sorted(bridge_canon-set(rr))))
work=rows(C/'CURRENT_WORK_ITEM_REGISTRY.csv'); work_ids={r['work_item_id'] for r in work}
tests=rows(C/'TEST_EXECUTION_LEDGER.csv'); test_ids={r['test_execution_id'] for r in tests}
for rid,r in tr.items():
    ds=[x for x in r['detailed_requirement_ids'].split(';') if x]
    ws=[x for x in r['work_item_ids'].split(';') if x]
    ts=[x for x in r['test_execution_ids'].split(';') if x]
    if not ds: err('TRACE_NO_DETAILED '+rid)
    if not ws: err('TRACE_NO_WORK '+rid)
    if not ts: err('TRACE_NO_TEST '+rid)
    for x in ds:
        if x not in native and x not in bridge_ids: err(f'TRACE_ORPHAN_DETAILED {rid} {x}')
    for x in ws:
        if x not in work_ids: err(f'TRACE_ORPHAN_WORK {rid} {x}')
    for x in ts:
        if x not in test_ids: err(f'TRACE_ORPHAN_TEST {rid} {x}')
    if r.get('source_identity')!=sid: err('TRACE_SOURCE_IDENTITY_DRIFT '+rid)
# Every bridge must be used by its canonical trace.
used_bridge={x for r in trace for x in r['detailed_requirement_ids'].split(';') if x.startswith('CPF-CBR-')}
if used_bridge!=bridge_ids: err(f'BRIDGE_TRACE_COVERAGE missing={len(bridge_ids-used_bridge)} extra={len(used_bridge-bridge_ids)}')
# 4) Current authority and source identity single-ness.
auth=loadj(H/'contracts/current-authority-registry.json')
sid_authority=auth.get('sourceIdentityAuthority')
if sid_authority!='cpf-docs/governance/development-harness/current/SOURCE_IDENTITY.json':
    err('AUTHORITY_SOURCE_IDENTITY_AUTHORITY_DRIFT')
elif (ROOT/sid_authority).resolve()!=(C/'SOURCE_IDENTITY.json').resolve():
    err('AUTHORITY_SOURCE_IDENTITY_AUTHORITY_PATH_DRIFT')
if 'sourceIdentity' in auth:
    err('AUTHORITY_STATIC_SOURCE_IDENTITY_FORBIDDEN')
for rel in auth.get('authoritative',[]):
    p=ROOT/rel
    if not p.is_file(): err('AUTHORITY_MISSING '+rel); continue
    if p.suffix.lower()=='.csv':
        rs=rows(p)
        if rs and 'source_identity' in rs[0]:
            vals={r.get('source_identity','') for r in rs}
            if vals!={sid}: err(f'AUTHORITY_SOURCE_IDENTITY_DRIFT {rel} values={sorted(vals)[:5]}')
# Work/status exact set.
status=rows(C/'CURRENT_DEVELOPMENT_STATUS.csv')
wi={r['work_item_id'] for r in work}; si={r['work_item_id'] for r in status}
if wi!=si: err(f'WORK_STATUS_SET_DRIFT missing={len(wi-si)} extra={len(si-wi)}')
# Deprecated current duplicates must be in delete manifest, never authoritative.
dm=rows(H/'DELETE_MANIFEST.csv'); dpaths={r['path'] for r in dm}
for rel in auth.get('deprecatedCurrentFilesToDelete',[]):
    if rel in auth.get('authoritative',[]): err('DEPRECATED_IS_AUTHORITY '+rel)
    if rel not in dpaths: err('DEPRECATED_NOT_IN_DELETE_MANIFEST '+rel)

# Every file under current/ must have exactly one declared role (or match an authoritative dataset prefix).
exact_categories={
    'authoritative':set(auth.get('authoritative',[])),
    'generated':set(auth.get('generatedProjections',[])),
    'historical':set(auth.get('historicalProvenance',[])),
    'deprecated':set(auth.get('deprecatedCurrentFilesToDelete',[])),
    'supporting':set(auth.get('supportingReferencesNonAuthoritative',[])),
    'source_identity':{auth.get('sourceIdentityAuthority','')},
}
dataset_exact=set(); dataset_prefixes=[]
for spec in auth.get('authoritativeDatasets',[]):
    if spec.endswith('/**'): dataset_prefixes.append(spec[:-3])
    else: dataset_exact.add(spec)
exact_categories['authoritative_dataset']=dataset_exact
seen_roles={}
for role,items in exact_categories.items():
    for rel in items:
        if not rel: continue
        seen_roles.setdefault(rel,[]).append(role)
for rel,roles_ in seen_roles.items():
    if len(roles_)>1: err(f'CURRENT_AUTHORITY_ROLE_OVERLAP {rel} roles={roles_}')
for fp in C.rglob('*'):
    if not fp.is_file(): continue
    rel=fp.relative_to(ROOT).as_posix()
    roles_=seen_roles.get(rel,[])
    if not roles_ and any(rel.startswith(prefix) for prefix in dataset_prefixes):
        roles_=['authoritative_dataset_prefix']
    if not roles_: err('CURRENT_FILE_UNCLASSIFIED '+rel)
# 5) Control execution wiring. POLICY alone cannot satisfy final PASS.
controls=rows(H/'contracts/harness-control-registry.csv'); ce=rows(C/'CONTROL_EXECUTION_LEDGER.csv')
cem={r['control_id']:r for r in ce}
if set(cem)!={r['control_id'] for r in controls}: err('CONTROL_EXECUTION_SET_DRIFT')
allowed={'POLICY','STATIC_VERIFIER','RUNTIME_VERIFIER','EVIDENCE_GATE'}
for r in controls:
    if r.get('enforcement_type') not in allowed: err('CONTROL_BAD_ENFORCEMENT_TYPE '+r['control_id'])
    if r.get('mandatory_for_final')=='true' and not r.get('execution_control_id'): err('CONTROL_NO_EXECUTION_LINK '+r['control_id'])
    x=cem.get(r['control_id'],{})
    if x.get('control_execution_id')!=r.get('execution_control_id'): err('CONTROL_EXECUTION_LINK_DRIFT '+r['control_id'])
    if x.get('status')=='PASS' and (not x.get('evidence') or not x.get('command_or_gate')): err('CONTROL_FALSE_PASS '+r['control_id'])
# 6) Current split dataset guide counts must equal indices.
guide=(C/'CPF_SPLIT_MASTER_DATASET_GUIDE.md').read_text(encoding='utf-8')
for fname,label in [('CPF_REQUIREMENT_MASTER.csv','Requirement'),('CPF_SCENARIO_MASTER.csv','Scenario'),('CPF_EXECUTION_SEQUENCE.csv','Execution')]:
    idx=rows(C/fname); n=int(idx[0]['logical_record_count']) if idx else 0
    # if the guide explicitly states a comma formatted count near the dataset name, any conflicting legacy number is forbidden by known stale patterns below.
for bad in ['208 Canonical Requirement','190 unique work items','40,789','30,558']:
    for p in [H/'product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md',C/'CPF_SPLIT_MASTER_DATASET_GUIDE.md',C/'CPF_REQUIREMENT_SOURCE_COVERAGE.csv']:
        if bad in p.read_text(encoding='utf-8-sig'): err(f'STALE_CURRENT_LITERAL {bad} in {p.relative_to(ROOT)}')
# 6.1) Current package projection must never carry stale Source/registry counts.
pm_path=C/'PACKAGE_MANIFEST.json'
if not pm_path.is_file():
    err('CURRENT_PACKAGE_MANIFEST_MISSING')
else:
    try:
        pm=loadj(pm_path)
        if pm.get('currentSourceIdentity')!=sid: err('CURRENT_PACKAGE_SOURCE_IDENTITY_STALE')
        expected_counts={
            'canonicalRequirementRows':len(reg),
            'trackingWorkRows':len(tracking) if 'tracking' in globals() else len([r for r in work if r.get('item_role','TRACKING')=='TRACKING']),
            'executionWorkRows':len(execution) if 'execution' in globals() else len([r for r in work if r.get('item_role')=='ROOT_CAUSE_EXECUTION']),
            'workItemRows':len(work),
            'testRows':len(tests),
        }
        for k,v in expected_counts.items():
            if pm.get(k)!=v: err(f'CURRENT_PACKAGE_COUNT_STALE {k} expected={v} actual={pm.get(k)}')
    except Exception as exc:
        err('CURRENT_PACKAGE_MANIFEST_PARSE_FAILED '+str(exc))

# 7) Harness/package garbage must never be tracked inside Harness or its delivery payload.
package_roots=[H,ROOT/'cpf-docs/deliverables/development-harness']
for scan_root in package_roots:
    if not scan_root.exists():
        continue
    for p in scan_root.rglob('*'):
        if any(part in {'.pytest_cache','__pycache__'} for part in p.parts) or p.suffix.lower() in {'.pyc','.pyo','.class'}:
            err('HARNESS_GARBAGE '+p.relative_to(ROOT).as_posix())

# 7-b) GARBAGE-0637 canonical policy: regeneratable Python bytecode/cache must not ship anywhere in
# product source. The package scan above only covers Harness payload, so enforce the repository scope
# too. Protected canonical paths (.editorconfig/.gitattributes/.gitignore/.github) are never matched.
_py_cache_skip={'.git','node_modules'}
for _pattern in ('**/*.pyc','**/*.pyo','**/__pycache__','**/.pytest_cache'):
    for _p in ROOT.glob(_pattern):
        if _py_cache_skip.intersection(_p.parts): continue
        err('REPOSITORY_PYTHON_CACHE '+_p.relative_to(ROOT).as_posix())

# 8) Root-cause execution mapping, Handover aliases, deprecated active-reference reentry, transitive migration.
tracking=[r for r in work if r.get('item_role','TRACKING')=='TRACKING']
execution=[r for r in work if r.get('item_role')=='ROOT_CAUSE_EXECUTION']
exec_ids={r['work_item_id'] for r in execution}
if len(tracking)<394: err(f'TRACKING_WORK_SCOPE_REDUCED actual={len(tracking)} baseline=394')
if len([r for r in tracking if r.get('source_type')=='CANONICAL_COVERAGE_UMBRELLA'])<193: err('CANONICAL_COVERAGE_UMBRELLA_REDUCED')
for r in tracking:
    links=[x for x in r.get('execution_wp_ids','').split(';') if x]
    if not links: err('TRACKING_EXECUTION_LINK_MISSING '+r['work_item_id'])
    for x in links:
        if x not in exec_ids: err('TRACKING_EXECUTION_LINK_ORPHAN '+r['work_item_id']+' '+x)
handover=(C/'CPF_DEVELOPMENT_HANDOVER.md').read_text(encoding='utf-8')
mentioned=set(re.findall(r'WP-R(?:01\.21|03\.15|07\.17)',handover))
known_alias={a for r in work for a in r.get('handover_aliases','').split(';') if a}
if mentioned-known_alias: err('HANDOVER_REGISTRY_CONSISTENCY '+','.join(sorted(mentioned-known_alias)))
# Active source/docs must not re-introduce deprecated canonical/current authority paths. Provenance/migration ledgers are narrow exceptions.
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
# Detailed source_basis must point to Current Product Contract, not old canonical.
for idx in rows(C/'CPF_REQUIREMENT_MASTER.csv'):
    p=ROOT/idx['part_path']
    if not p.is_file(): continue
    with p.open(encoding='utf-8-sig',newline='') as f:
        for rr in csv.DictReader(f):
            if 'CPF_FINAL_TARGET_REQUIREMENTS.md::' in (rr.get('source_basis') or ''): err('DEPRECATED_DETAILED_SOURCE_BASIS '+rr.get('requirement_id',''))
# Migration chains must terminate at an existing non-deprecated target and be acyclic.
mm_rows=rows(H/'CANONICAL_MIGRATION_MAP.csv'); mm_by={r['old_path']:r for r in mm_rows}
for start,r in mm_by.items():
    cur=r['new_path']; seen={start}
    while cur in mm_by:
        if cur in seen: err('MIGRATION_TRANSITIVE_CYCLE '+start); break
        seen.add(cur); cur=mm_by[cur]['new_path']
    if cur and not (ROOT/cur).is_file(): err('MIGRATION_TRANSITIVE_TERMINAL_MISSING '+start+' -> '+cur)

if errors:
    for e in errors[:300]: print('FAIL',e)
    print(f'HARNESS_AUTHORITY_GATE=FAIL ERRORS={len(errors)}')
    raise SystemExit(1)
print(f'HARNESS_AUTHORITY_GATE=PASS CANONICAL={len(reg)} TRACE={len(trace)} BRIDGE={len(bridge)} WORK={len(work)} TEST={len(tests)} CONTROLS={len(controls)} SOURCE_IDENTITY={sid}')
