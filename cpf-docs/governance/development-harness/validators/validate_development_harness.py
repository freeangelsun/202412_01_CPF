#!/usr/bin/env python3
from pathlib import Path
import csv,json,hashlib,re,sys,unicodedata
ROOT=Path(__file__).resolve().parents[4]
H=ROOT/'cpf-docs/governance/development-harness'

def fail(msg): print('FAIL',msg); return 1

def loadj(rel): return json.loads((H/rel).read_text(encoding='utf-8'))

def main():
    errors=[]; c=loadj('contracts/contract-registry.json')
    required=['CPF_DEVELOPMENT_HARNESS.md','SOURCE_IDENTITY.json','HANDOVER.md','OPEN_ISSUES.md','contracts/contract-registry.json','contracts/harness-control-registry.csv','contracts/external-reference-registry.json','standards/DEVELOPMENT_EXECUTION_CORE_POLICY.md','product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md','current/CANONICAL_PRODUCT_REQUIREMENTS.csv','current/CURRENT_WORK_ITEM_REGISTRY.csv','current/CURRENT_DEVELOPMENT_STATUS.csv','current/ROLE_EXECUTION_LEDGER.csv','current/TEST_EXECUTION_LEDGER.csv','current/CURRENT_GARBAGE_DECISIONS.csv','current/CURRENT_DETAILED_REVIEW.md','current/CURRENT_WORK_REQUEST_AND_STATUS.md','current/CODEX_CLAUDE_REVIEW_REQUEST.md','current/LEGACY_EVIDENCE_SEMANTIC_REGISTRY.jsonl','current/CANONICAL_REQUIREMENT_TRACE.csv','current/CURRENT_CANONICAL_DETAILED_BRIDGE.csv','current/CONTROL_EXECUTION_LEDGER.csv','contracts/current-authority-registry.json','contracts/product-contract-integrity.json','contracts/PRODUCT_CONTRACT_CHANGE_LEDGER.csv','CANONICAL_MIGRATION_MAP.csv','CANONICAL_MIGRATION_SEMANTIC_LEDGER.csv','DELETE_MANIFEST.csv']
    for x in required:
        if not (H/x).is_file(): errors.append('MISSING '+x)
    # canonical registry unique/non-empty; expected count comes from file, not literal
    try:
        with (H/'current/CANONICAL_PRODUCT_REQUIREMENTS.csv').open(encoding='utf-8-sig',newline='') as f: rr=list(csv.DictReader(f))
        ids=[r['requirement_id'] for r in rr]
        if not ids or len(ids)!=len(set(ids)): errors.append('PRODUCT_REQUIREMENT_REGISTRY_EMPTY_OR_DUPLICATE')
    except Exception as e: errors.append('PRODUCT_REQUIREMENT_REGISTRY '+str(e))
    # role ledger coverage = each work item x all role registry keys
    work_registry=H/'current/CURRENT_WORK_ITEM_REGISTRY.csv'
    if work_registry.exists():
        with work_registry.open(encoding='utf-8-sig',newline='') as f: inv=list(csv.DictReader(f))
    else:
        with (H/'current/CPF_CANONICAL_DEVELOPMENT_CLOSURE_INVENTORY.csv').open(encoding='utf-8-sig',newline='') as f: inv=list(csv.DictReader(f))
    with (H/'current/ROLE_EXECUTION_LEDGER.csv').open(encoding='utf-8-sig',newline='') as f: led=list(csv.DictReader(f))
    roles=set(c['roles']); expected={(r['work_item_id'],role) for r in inv for role in roles}; actual={(r['work_item_id'],r['role']) for r in led}
    if expected!=actual: errors.append(f'ROLE_LEDGER_COVERAGE missing={len(expected-actual)} extra={len(actual-expected)}')
    required_ev=set(c['requiredCompletionEvidence'])
    for r in led:
        if r['execution_status']=='PASS':
            missing=[k for k in required_ev if not r.get(k,'').strip()]
            if missing: errors.append(f'FALSE_PASS {r["work_item_id"]}/{r["role"]} missing={missing}')
            if r['role']=='INDEPENDENT_REVIEWER' and r.get('source_modified','').strip().lower()=='true':
                vs_required=c['independentReviewerSourceModificationEvidence']
                miss=[k for k in vs_required if not r.get(k,'').strip()]
                if miss: errors.append(f'FALSE_PASS_VSCODE {r["work_item_id"]} missing={miss}')
                if r.get('vscode_fresh_import','').strip().lower()!='true': errors.append(f'FALSE_PASS_VSCODE_FRESH {r["work_item_id"]}')
                if r.get('vscode_error_count','').strip()!='0' or r.get('vscode_warning_count','').strip()!='0': errors.append(f'FALSE_PASS_VSCODE_DIAGNOSTIC {r["work_item_id"]} error={r.get("vscode_error_count")} warning={r.get("vscode_warning_count")}')
    # Dedicated test execution ledger coverage and false PASS checks
    with (H/'current/TEST_EXECUTION_LEDGER.csv').open(encoding='utf-8-sig',newline='') as f: tests=list(csv.DictReader(f))
    by_work={}
    for r in tests: by_work.setdefault(r['work_item_id'],[]).append(r)
    work_ids={r['work_item_id'] for r in inv}
    if set(by_work)!=work_ids: errors.append(f'TEST_LEDGER_COVERAGE missing={len(work_ids-set(by_work))} extra={len(set(by_work)-work_ids)}')
    req_test_ev=set(c['requiredTestEvidence'])
    for r in tests:
        if r['status']=='PASS':
            missing=[k for k in req_test_ev if not r.get(k,'').strip()]
            if missing: errors.append(f'FALSE_TEST_PASS {r["test_execution_id"]} missing={missing}')
            ep=(r.get('evidence') or '').strip()
            if ep:
                epath=ROOT/ep
                if not epath.is_file(): errors.append(f'TEST_EVIDENCE_MISSING {r["test_execution_id"]} {ep}')
                elif r.get('evidence_sha256','').strip() and hashlib.sha256(epath.read_bytes()).hexdigest().lower()!=r['evidence_sha256'].strip().lower(): errors.append(f'TEST_EVIDENCE_SHA_MISMATCH {r["test_execution_id"]}')
        if r.get('mandatory')=='true' and r['status']=='NOT_APPLICABLE': errors.append(f'MANDATORY_TEST_NOT_APPLICABLE {r["test_execution_id"]}')
    # Completion consistency: overall complete cannot outrun role/test/runtime evidence.
    with (H/'current/CURRENT_DEVELOPMENT_STATUS.csv').open(encoding='utf-8-sig',newline='') as f: status_rows=list(csv.DictReader(f))
    role_by={(r['work_item_id'],r['role']):r for r in led}
    for srow in status_rows:
        wid=srow['work_item_id']; complete=srow.get('overall_status')=='완료'
        if srow.get('verification_status')=='완료':
            mandatory=[x for x in by_work.get(wid,[]) if x.get('mandatory')=='true']
            if not mandatory or any(x.get('status')!='PASS' for x in mandatory): errors.append(f'VERIFICATION_COMPLETE_WITHOUT_TEST_PASS {wid}')
        if complete:
            if srow.get('development_status')!='완료' or srow.get('verification_status')!='완료' or srow.get('runtime_status') not in {'PASS','NOT_APPLICABLE'}: errors.append(f'FALSE_OVERALL_COMPLETE_STATUS {wid}')
            for role in c['roles']:
                if role_by.get((wid,role),{}).get('execution_status')!='PASS': errors.append(f'FALSE_OVERALL_COMPLETE_ROLE {wid}/{role}')
            if any(x.get('mandatory')=='true' and x.get('status')!='PASS' for x in by_work.get(wid,[])): errors.append(f'FALSE_OVERALL_COMPLETE_TEST {wid}')
    # migration coverage and no duplicate old/new canonical authority
    authority=loadj('contracts/current-authority-registry.json')
    allowed_harness_delete=set(authority.get('deprecatedCurrentFilesToDelete',[]))
    with (H/'CANONICAL_MIGRATION_MAP.csv').open(encoding='utf-8-sig',newline='') as f: mm=list(csv.DictReader(f))
    for r in mm:
        if r['migration_status'] not in {'MAPPED','MERGED','SUPERSEDED'} or not r['new_path']:
            errors.append('UNMAPPED '+r.get('old_path',''))
        if r.get('old_path','')==r.get('new_path',''):
            errors.append('SELF_MIGRATION '+r.get('old_path',''))
        if r.get('old_path','').startswith('cpf-docs/governance/development-harness/') and r.get('old_path','') not in allowed_harness_delete:
            errors.append('DELETE_CURRENT_HARNESS_AUTHORITY_FORBIDDEN '+r.get('old_path',''))
        cur=r.get('new_path',''); seen={r.get('old_path','')}
        mm_by={x['old_path']:x for x in mm}
        while cur in mm_by:
            if cur in seen:
                errors.append('MIGRATION_TRANSITIVE_CYCLE '+r.get('old_path','')); break
            seen.add(cur); cur=mm_by[cur].get('new_path','')
        if cur and not (ROOT/cur).exists():
            errors.append('MIGRATION_REPLACEMENT_MISSING '+r.get('old_path','')+' -> '+cur)
    with (H/'DELETE_MANIFEST.csv').open(encoding='utf-8-sig',newline='') as f: dm=list(csv.DictReader(f))
    expected_delete={r['old_path'] for r in mm if r.get('delete_eligible')=='true'}
    if expected_delete!={r['path'] for r in dm}: errors.append('DELETE_MIGRATION_PATH_SET_MISMATCH')
    protected_prefixes=('cpf-docs/deliverables/','cpf-docs/guides/','cpf-docs/environment/docker/','cpf-tools/environment/docker-development-test/','cpf-docs/governance/documentation-harness/')
    for r in dm:
        protected=r.get('path','').startswith(protected_prefixes)
        if protected:
            errors.append('PROTECTED_PATH_PRESENT_IN_DELETE_MANIFEST '+r.get('path',''))
        elif r.get('approved')!='true' or r.get('user_approved')!='true' or r.get('delete_eligible')!='true':
            errors.append('DELETE_NOT_APPROVED '+r.get('path',''))
        if r.get('path','').startswith('cpf-docs/governance/development-harness/') and r.get('path','') not in allowed_harness_delete: errors.append('DELETE_CURRENT_HARNESS_AUTHORITY_FORBIDDEN '+r.get('path',''))
    # Fail fast on structural/closure violations before expensive UTF-8 full Harness scan.
    if errors:
        for e in errors[:200]: print('FAIL',e)
        print(f'HARNESS_SELF_ACCEPTANCE=FAIL ERRORS={len(errors)}'); return 1
    # Legacy evidence is semantic context only. Copied past-source evidence must never live under current evidence roots.
    legacy_registry=H/'current/LEGACY_EVIDENCE_SEMANTIC_REGISTRY.jsonl'
    try:
        legacy_records=[json.loads(x) for x in legacy_registry.read_text(encoding='utf-8').splitlines() if x.strip()]
        if any(r.get('authoritative_for_current_pass') is not False or r.get('migration_disposition')!='SEMANTIC_CONTEXT_ONLY' for r in legacy_records):
            errors.append('LEGACY_EVIDENCE_AUTHORITY_VIOLATION')
        legacy_paths={r.get('old_path','') for r in legacy_records}
        mapped_legacy={r.get('old_path','') for r in mm if r.get('old_path','').startswith('cpf-docs/work/evidence/')}
        if legacy_paths!=mapped_legacy: errors.append(f'LEGACY_EVIDENCE_REGISTRY_COVERAGE missing={len(mapped_legacy-legacy_paths)} extra={len(legacy_paths-mapped_legacy)}')
    except Exception as e: errors.append('LEGACY_EVIDENCE_REGISTRY '+str(e))
    for role_root in ['devgpt/current','independent-reviewer/current','qa/current','platform/current']:
        d=H/'evidence'/role_root
        if not d.is_dir(): errors.append('CURRENT_EVIDENCE_ROOT_MISSING '+role_root)
    # No migrated legacy evidence file may be copied as a current PASS candidate. README placeholders are allowed.
    legacy_destinations={r.get('new_path','') for r in mm if r.get('old_path','').startswith('cpf-docs/work/evidence/')}
    if any(x!= 'cpf-docs/governance/development-harness/current/LEGACY_EVIDENCE_SEMANTIC_REGISTRY.jsonl' for x in legacy_destinations):
        errors.append('LEGACY_EVIDENCE_DIRECT_COPY_MAPPING_FORBIDDEN')
    # current-only Harness directory names
    for p in (ROOT/'cpf-docs/governance').iterdir():
        if p.is_dir() and p.name!='development-harness':
            low=p.name.lower()
            if any(x in low for x in c['currentOnly']['forbidHarnessNamePatterns']): errors.append('STALE_HARNESS_DIR '+p.as_posix())
    # UTF-8/NFC for Harness text
    text_ext={'.md','.txt','.csv','.json','.yml','.yaml','.py','.ps1','.sh'}
    seen={}
    for p in H.rglob('*'):
        if not p.is_file(): continue
        n=unicodedata.normalize('NFC',p.relative_to(H).as_posix())
        if n in seen and seen[n]!=p: errors.append('NFC_DUP '+n)
        seen[n]=p
        if p.suffix.lower() in text_ext:
            try: s=p.read_text(encoding='utf-8')
            except Exception as e: errors.append('UTF8 '+p.as_posix()+':'+str(e)); continue
            if '\x00' in s or '\x08' in s: errors.append('CONTROL_CHAR '+p.as_posix())
    if errors:
        for e in errors[:200]: print('FAIL',e)
        print(f'HARNESS_SELF_ACCEPTANCE=FAIL ERRORS={len(errors)}'); return 1
    print(f'HARNESS_SELF_ACCEPTANCE=PASS REQUIREMENTS={len(rr)} WORK_ITEMS={len(inv)} ROLE_ROWS={len(led)} MIGRATIONS={len(mm)}')
    return 0
if __name__=='__main__': raise SystemExit(main())
