#!/usr/bin/env python3
"""Fail-closed static/render gate for NXT2-DBVENDOR-001."""
from __future__ import annotations
import argparse, hashlib, json, re, subprocess, sys
from pathlib import Path, PurePosixPath

OFFICIAL={'mariadb','postgresql','oracle'}
OWNER_PREFIX={'common':'CMN_','admin':'ADM_','batch':'BAT_','gateway':'GW_','security':'SEC_','platform-operations':'OPS_','cpf':'CPF_','backoffice':'MBW_'}


def load(path:Path): return json.loads(path.read_text(encoding='utf-8-sig'))


CHECKSUM_LINE=re.compile(r'^([0-9a-fA-F]{64}) ([ *])(.+)$')


def verify_appended_checksum_index(root:Path, baseline:str, index_path:str, added_paths:set[str], fail):
    baseline_blob=subprocess.run(
        ['git','-C',str(root),'show',f'{baseline}:{index_path}'],capture_output=True)
    current_blob=subprocess.run(
        ['git','-C',str(root),'show',f'HEAD:{index_path}'],capture_output=True)
    if baseline_blob.returncode or current_blob.returncode:
        fail(f'cannot read checksum index history: {index_path}')
        return
    before=baseline_blob.stdout; after=current_blob.stdout
    if before==after or not after.startswith(before):
        fail(f'checksum index baseline is not a strict byte prefix: {index_path}')
        return
    if before and not before.endswith(b'\n'):
        fail(f'checksum index baseline does not end at a line boundary: {index_path}')
        return
    suffix=after[len(before):]
    if not suffix.endswith(b'\n'):
        fail(f'appended checksum index entries must end with a newline: {index_path}')
        return
    try:
        baseline_lines=before.decode('utf-8').splitlines()
        appended_lines=suffix.decode('utf-8').splitlines()
    except UnicodeDecodeError:
        fail(f'checksum index must be UTF-8: {index_path}')
        return
    if not appended_lines or any(not line.strip() for line in appended_lines):
        fail(f'checksum index append contains an empty line: {index_path}')
        return
    seen_names=set()
    for line in baseline_lines:
        match=CHECKSUM_LINE.fullmatch(line)
        if not match:
            fail(f'invalid baseline checksum line: {index_path}: {line}')
            continue
        filename=match.group(3)
        if filename in seen_names:
            fail(f'duplicate baseline checksum filename: {index_path}: {filename}')
        seen_names.add(filename)
    index_parent=PurePosixPath(index_path).parent
    for line in appended_lines:
        match=CHECKSUM_LINE.fullmatch(line)
        if not match:
            fail(f'invalid appended checksum line: {index_path}: {line}')
            continue
        expected_hash=match.group(1).lower(); filename=match.group(3)
        relative=PurePosixPath(filename)
        if ('\\' in filename or relative.is_absolute() or len(relative.parts)!=1 or
                relative.as_posix()!=filename or any(part in ('','.','..') for part in relative.parts)):
            fail(f'unsafe appended checksum filename: {index_path}: {filename}')
            continue
        if filename in seen_names:
            fail(f'duplicate checksum filename: {index_path}: {filename}')
            continue
        seen_names.add(filename)
        referenced=(index_parent/relative).as_posix()
        if not referenced.lower().endswith('.sql'):
            fail(f'appended checksum entry must reference SQL: {index_path}: {filename}')
            continue
        if referenced not in added_paths:
            baseline_file=subprocess.run(
                ['git','-C',str(root),'show',f'{baseline}:{referenced}'],capture_output=True)
            head_file=subprocess.run(
                ['git','-C',str(root),'show',f'HEAD:{referenced}'],capture_output=True)
            if (baseline_file.returncode or head_file.returncode or
                    baseline_file.stdout!=head_file.stdout):
                fail(f'appended checksum entry must reference added or baseline-unchanged SQL: {index_path}: {referenced}')
                continue
        current_file=root/Path(*PurePosixPath(referenced).parts)
        if not current_file.is_file():
            fail(f'appended checksum entry references a missing file: {index_path}: {referenced}')
            continue
        actual_hash=hashlib.sha256(current_file.read_bytes()).hexdigest()
        if actual_hash!=expected_hash:
            fail(f'appended checksum hash mismatch: {index_path}: {referenced} expected={expected_hash} actual={actual_hash}')


def verify_immutable_migration_history(root:Path, checksum:dict, fail):
    """Verify the frozen baseline while permitting committed append-only migrations."""
    baseline=(checksum.get('baselineCommit') or '').strip()
    if not re.fullmatch(r'[0-9a-fA-F]{40}',baseline):
        fail('immutable migration baselineCommit must be a full Git SHA')
        return
    commit=subprocess.run(
        ['git','-C',str(root),'cat-file','-e',f'{baseline}^{{commit}}'],
        text=True,capture_output=True)
    if commit.returncode:
        fail(f'immutable migration baseline commit is unavailable: {baseline}')
        return
    ancestor=subprocess.run(
        ['git','-C',str(root),'merge-base','--is-ancestor',baseline,'HEAD'],
        text=True,capture_output=True)
    if ancestor.returncode:
        fail(f'immutable migration baseline is not an ancestor of HEAD: {baseline}')
        return
    for tr in checksum.get('trees') or []:
        path=tr['path']; expected=tr['gitTreeSha']
        baseline_tree=subprocess.run(
            ['git','-C',str(root),'rev-parse',f'{baseline}:{path}'],
            text=True,capture_output=True)
        actual=baseline_tree.stdout.strip()
        if baseline_tree.returncode or actual!=expected:
            fail(f'immutable migration baseline tree mismatch: {path} expected={expected} actual={actual}')
            continue
        delta=subprocess.run(
            ['git','-C',str(root),'diff','--name-status','--no-renames',baseline,'HEAD','--',path],
            text=True,capture_output=True)
        if delta.returncode:
            fail(f'cannot compare immutable migration history: {path}: {delta.stderr.strip()}')
        else:
            changes=[]
            for line in delta.stdout.splitlines():
                parts=line.split('\t',1)
                if len(parts)!=2:
                    fail(f'unparseable historical migration change: {line}')
                    continue
                changes.append((parts[0],parts[1]))
            added_paths={changed_path for status,changed_path in changes if status=='A'}
            for status,changed_path in changes:
                if status=='A':
                    continue
                if status=='M' and PurePosixPath(changed_path).name=='checksums.sha256':
                    verify_appended_checksum_index(root,baseline,changed_path,added_paths,fail)
                    continue
                fail(f'non-append historical migration change since baseline: {status}\t{changed_path}')
        dirty=subprocess.run(
            ['git','-C',str(root),'status','--porcelain=v1','--untracked-files=all','--',path],
            text=True,capture_output=True)
        if dirty.returncode:
            fail(f'cannot inspect historical migration working tree: {path}: {dirty.stderr.strip()}')
        elif dirty.stdout.strip():
            for line in dirty.stdout.splitlines():
                fail(f'uncommitted historical migration modification: {line}')

def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); args=ap.parse_args()
    root=Path(args.root).resolve(); db=root/'cpf-tools/db'; errs=[]
    def fail(x): errs.append(x)
    required=[
      db/'canonical/platform-schema.json',db/'canonical/seed-model.json',db/'canonical/platform-non-table-objects.json',
      db/'canonical/logical-datatypes.json',db/'canonical/migration-intent-catalog.json',db/'canonical/vendor-overrides.json',
      db/'canonical/immutable-migration-checksums.json',db/'canonical/db3-lifecycle-scenarios.json',db/'canonical/generated-domain-schema.json',db/'render_vendor_pack.py',db/'render_generated_domain_template.py']
    for p in required:
        if not p.is_file(): fail('missing '+p.relative_to(root).as_posix())
    if errs:
        print('CPF_DB_CANONICAL_RENDER_GATE=FAIL'); [print(f'{i:03d} {e}') for i,e in enumerate(errs,1)]; raise SystemExit(1)
    schema=load(required[0]); seed=load(required[1]); nto=load(required[2]); overrides=load(required[5]); checksum=load(required[6]); scenarios=load(required[7]); generated_domain=load(required[8])
    vendors=set(schema.get('canonicalPolicy',{}).get('officialVendors') or [])
    if vendors!=OFFICIAL: fail(f'official vendor mismatch: {sorted(vendors)}')
    if len(schema.get('tables') or []) != schema.get('tableCount'): fail('canonical tableCount must match tables length')
    names=[]
    for t in schema['tables']:
        target=t.get('targetTableName') or t.get('name'); names.append(target)
        for k in ('currentLogicalDatabase','currentName','logicalOwner','targetDatabaseRole','targetTableName','migrationOwner','runtimeConsumer','atomicityClass'):
            if not t.get(k): fail(f'{target}: missing {k}')
        prefix=OWNER_PREFIX.get(t.get('logicalOwner'))
        if prefix and not target.upper().startswith(prefix): fail(f'{target}: owner prefix mismatch for {t.get("logicalOwner")}')
    if len(names)!=len(set(names)): fail('duplicate target table name')
    # Foreign keys may not cross physical database roles after cpfDB/mbwDB/reference consolidation.
    by_source={t.get('name'):t for t in schema['tables']}
    by_source.update({t.get('currentName'):t for t in schema['tables'] if t.get('currentName')})
    by_source.update({t.get('targetTableName'):t for t in schema['tables'] if t.get('targetTableName')})
    for t in schema['tables']:
        for fk in t.get('foreignKeys') or []:
            ref=by_source.get(fk.get('refTable'))
            if ref is None:
                fail(f"{t.get('targetTableName')}: FK {fk.get('name')} target missing: {fk.get('refTable')}")
                continue
            if t.get('targetDatabaseRole') != ref.get('targetDatabaseRole'):
                fail(f"cross-role FK forbidden: {t.get('targetTableName')}[{t.get('targetDatabaseRole')}] -> {ref.get('targetTableName')}[{ref.get('targetDatabaseRole')}]")
    # Canonical seed tables must resolve to canonical targets after consolidation.
    known=set(names)
    aliases={t.get('name'):t['targetTableName'] for t in schema['tables']}; aliases.update({t.get('currentName'):t['targetTableName'] for t in schema['tables']})
    for st in seed.get('statements') or []:
        if st.get('statementKind')=='insert':
            tab=(st.get('tableName') or st.get('table') or '').split('.')[-1]
            mapped=aliases.get(tab,tab)
            if mapped not in known: fail(f'seed target not canonical: {tab} -> {mapped}')
    # Explicit overrides are highly constrained.
    seen=set()
    for o in overrides.get('overrides') or []:
        miss=[k for k in ('canonicalId','vendor','owner','reason','testId') if not o.get(k)]
        if miss: fail(f'override missing fields {miss}: {o}')
        if o.get('vendor') not in OFFICIAL: fail(f'override unsupported vendor: {o.get("vendor")}')
        key=(o.get('canonicalId'),o.get('vendor'))
        if key in seen: fail(f'duplicate override: {key}')
        seen.add(key)
    # Generated drift check for all three vendors.
    cp=subprocess.run([sys.executable,str(db/'render_vendor_pack.py'),'--root',str(root),'--check'],text=True,capture_output=True)
    if cp.returncode: fail('generated drift: '+(cp.stdout+cp.stderr).strip())
    gd=subprocess.run([sys.executable,str(db/'render_generated_domain_template.py'),'--root',str(root),'--check'],text=True,capture_output=True)
    if gd.returncode: fail('generated-domain template drift: '+(gd.stdout+gd.stderr).strip())
    # Generated current packs have the same canonical versions and no orphan vendor.
    generated=db/'generated/current'
    if set(p.name for p in generated.iterdir() if p.is_dir())!=OFFICIAL: fail('generated/current must contain exactly three official vendor directories')
    for v in OFFICIAL:
        m=load(generated/v/'manifest.json')
        if m.get('vendor')!=v or not m.get('generated'): fail(f'{v}: invalid generated manifest')
        for a in ('cpf-platform-schema.sql','cpf-platform-seed.sql','cpf-platform-verify.sql','cpf-platform-rollback.sql','backoffice-schema.sql','backoffice-seed.sql','backoffice-verify.sql','backoffice-rollback.sql','reference-fixture-schema.sql','reference-fixture-seed.sql','reference-fixture-verify.sql','reference-fixture-rollback.sql','non-table-objects.sql'):
            if not (generated/v/a).is_file(): fail(f'{v}: missing {a}')
    # Generated role boundaries must preserve the three canonical database roles.
    role_expected={'CPF_PLATFORM_DB','CUSTOMER_BUSINESS_DB','REFERENCE_FIXTURE'}
    actual={t.get('targetDatabaseRole') for t in schema['tables']}
    if actual != role_expected: fail(f'canonical role set mismatch: {sorted(actual)}')
    forbidden={
      'mariadb':(r'GENERATED\s+BY\s+DEFAULT\s+AS\s+IDENTITY',r'\bBYTEA\b',r'\bVARCHAR2\b'),
      'postgresql':(r'\bAUTO_INCREMENT\b',r'\bENGINE\s*=',r'\bVARCHAR2\b',r'\bVARBINARY\b',r'\bFROM\s+dual\b'),
      'oracle':(r'\bAUTO_INCREMENT\b',r'\bENGINE\s*=',r'\bBYTEA\b',r'\bVARBINARY\b',r'\bON\s+DUPLICATE\s+KEY\b',r'\bON\s+CONFLICT\b',r'\bLIMIT\s+\d+\b')
    }
    for v in OFFICIAL:
        for f in (generated/v).glob('*.sql'):
            txt=f.read_text(encoding='utf-8-sig',errors='replace')
            for pat in forbidden[v]:
                if re.search(pat,txt,flags=re.I): fail(f'{v}: forbidden dialect syntax {pat}: {f.name}')
        seed_text='\n'.join((generated/v/n).read_text(encoding='utf-8-sig') for n in ('cpf-platform-seed.sql','backoffice-seed.sql','reference-fixture-seed.sql'))
        for marker in ('CPF_SEED_VARIABLE_DEPENDENT','CPF_SEED_CANONICAL_UPSERT','TODO','UNVERIFIED'):
            if marker in seed_text: fail(f'{v}: non-executable seed marker remains: {marker}')
    # Generated Domain has one canonical schema and three rendered lifecycle templates; runtime dialect is Data-owned.
    if generated_domain.get('businessDatabaseRole')!='CUSTOMER_BUSINESS_DB': fail('generated-domain canonical database role must be CUSTOMER_BUSINESS_DB')
    gd_root=db/'generated/domain-template'
    if set(p.name for p in gd_root.iterdir() if p.is_dir())!=OFFICIAL: fail('generated/domain-template must contain exactly three official vendor directories')
    contract_path=root/'cpf-tools/generator/contracts/central-domain-template-contract.json'
    if not contract_path.is_file(): fail('missing central-domain-template-contract.json')
    else:
        contract=load(contract_path); packed=json.dumps(contract,ensure_ascii=False)
        if 'cpf-tools/db/generated/domain-template/{vendor}' not in packed: fail('generator contract does not consume generated-domain canonical template')
        if 'cpf-starters/data/persistence/src/main/resources/cpf-generated-domain-dialect/{vendor}' not in packed: fail('generated-domain runtime dialect is not Data-owned')
        if 'cpf-tools/db/vendor/{vendor}/domain-template' in packed: fail('generator contract still consumes manual vendor domain-template')
    vp_path=db/'vendor-pack-manifest.json'
    if not vp_path.is_file(): fail('missing vendor-pack-manifest.json')
    else:
        vp=load(vp_path)
        if set(vp.get('supportedVendors') or vp.get('officialVendors') or [])!=OFFICIAL: fail('vendor-pack manifest official vendor set mismatch')
        packed=json.dumps(vp,ensure_ascii=False)
        if 'cpf-tools/db/generated/current/{vendor}' not in packed and 'generated/current' not in packed: fail('vendor-pack manifest does not declare generated current snapshot authority')
    # Historical migrations: freeze the declared baseline and permit only committed additions after it.
    if (root/'.git').exists():
        verify_immutable_migration_history(root,checksum,fail)
    # Runtime scenario parity contract.
    if set(scenarios.get('officialVendors') or [])!=OFFICIAL: fail('DB3 lifecycle vendor set mismatch')
    for s in scenarios.get('scenarios') or []:
        if not s.get('sameScenarioForAllVendors') or not s.get('id') or not s.get('steps'): fail(f'invalid lifecycle scenario: {s}')
    # Raw vendor branch ban in business/control/generated source. SQL/tooling dialect owners are excluded intentionally.
    scan_roots=['cpf-starters/common','cpf-admin','cpf-backoffice/online','cpf-batch','cpf-gateway','cpf-education','cpf-tools/generator/golden']
    branch=re.compile(r'(?i)(?:if|else\s+if|switch|case|equals|contains|startswith)[^\n]{0,120}\b(?:mariadb|postgresql|oracle)\b|\b(?:mariadb|postgresql|oracle)\b[^\n]{0,120}(?:if|switch|case|equals|contains)')
    for rr in scan_roots:
        rp=root/rr
        if not rp.exists(): continue
        for f in rp.rglob('*'):
            if not f.is_file() or f.suffix.lower() not in {'.java','.kt','.groovy','.ps1','.sh','.sql','.xml'}: continue
            txt=f.read_text(encoding='utf-8-sig',errors='replace')
            if branch.search(txt): fail(f'raw vendor branch outside DB/Data dialect owner: {f.relative_to(root)}')
    # Generator option must expose exactly official vendors; legacy DB names may not be generated.
    gen=(root/'cpf-tools/generator/create-domain.ps1')
    if gen.is_file():
        txt=gen.read_text(encoding='utf-8-sig',errors='replace')
        for marker in ('${TablePrefix}DB','mbrDB','CPF_SCHEMA_NAME=mbrDB','CPF_DATABASE_NAME=mbrDB'):
            if marker in txt: fail(f'generator legacy domain database marker: {marker}')
    if errs:
        print('CPF_DB_CANONICAL_RENDER_GATE=FAIL')
        for i,e in enumerate(errs,1): print(f'{i:03d} {e}')
        raise SystemExit(1)
    print('CPF_DB_CANONICAL_RENDER_GATE=PASS')
    print(f'tables={len(schema["tables"])} seeds={len(seed.get("statements") or [])} vendors={len(OFFICIAL)} overrides={len(overrides.get("overrides") or [])}')

if __name__=='__main__': main()
