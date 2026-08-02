#!/usr/bin/env python3
from __future__ import annotations
import argparse,hashlib,json,re,sys
from pathlib import Path
VENDORS=('oracle','postgresql','mariadb')
CORE_TABLES=('CPF_EDU_OPERATION','CPF_EDU_OPERATION_TARGET','CPF_EDU_OPERATION_AUDIT','CPF_EDU_OUTBOX','CPF_EDU_BUSINESS_RECORD','CPF_EDU_LEASE','CPF_EDU_COUNTERPARTY_REQUEST')
BATCH_TABLES=('CPF_REF_BAT_JOB_EXECUTION','CPF_REF_BAT_CHECKPOINT','CPF_REF_BAT_TARGET_RESULT')
FORBIDDEN=('CPF_EDU_','CPF_REF_BAT_','REFERENCE_EDU','MANUAL_EDU','REFERENCE_BATCH_EDU')

def fail(m): print('[CPF][QA37][DB-GENERATOR][FAIL] '+m,file=sys.stderr);raise SystemExit(1)
def canonical(sql:str,tables:tuple[str,...])->dict[str,tuple[str,...]]:
    result={}
    for table in tables:
        m=re.search(r'CREATE\s+TABLE\s+'+table+r'\s*\((.*?)\)\s*;',sql,re.I|re.S)
        if not m: fail('table missing '+table)
        cols=[];depth=0;part=''
        for ch in m.group(1)+',':
            if ch=='(': depth+=1
            elif ch==')': depth-=1
            if ch==',' and depth==0:
                line=part.strip();part=''
                if line and not re.match(r'CONSTRAINT\b',line,re.I): cols.append(re.split(r'\s+',line)[0].upper())
            else: part+=ch
        result[table]=tuple(cols)
    return result
def normalized_ddl(sql:str)->str:
    s=re.sub(r'^--.*$','',sql,flags=re.M)
    s=s.upper().replace('VARCHAR2','VARCHAR').replace('NUMBER(19)','BIGINT').replace('NUMBER(10)','INTEGER').replace('TIMESTAMP(6)','TIMESTAMP').replace('CLOB','TEXT')
    return re.sub(r'\s+',' ',s).strip()
def check_digest(manifest:Path,files:list[Path])->None:
    entries={}
    for line_number,raw_line in enumerate(manifest.read_text(encoding='utf-8').splitlines(),1):
        line=raw_line.strip()
        if not line or line.startswith('#'):continue
        match=re.fullmatch(r'([0-9a-fA-F]{64})\s+\*?([^\s]+)',line)
        if not match:fail(f'checksum manifest format: {manifest}:{line_number}')
        if match.group(2) in entries:fail(f'checksum manifest duplicate: {manifest}:{match.group(2)}')
        entries[match.group(2)]=match.group(1).lower()
    for p in files:
        digest=hashlib.sha256(p.read_bytes()).hexdigest()
        if entries.get(p.name)!=digest:fail(f'checksum drift: {p}')
def check_pack(base:Path,vendor:str)->None:
    pack=json.loads((base/'pack.json').read_text(encoding='utf-8'))
    core=pack.get('operationLedger',{})
    if core.get('logicalDatabase')!='refDB' or core.get('ownerModule')!='cpf-reference' or core.get('generatedDomainAllowed') is not False:fail(vendor+' core pack ownership')
    if set(core.get('tables',[]))!=set(CORE_TABLES) or core.get('tableCount')!=len(CORE_TABLES):fail(vendor+' core pack tables')
    batch=pack.get('referenceBatchPack',{})
    if batch.get('logicalDatabase')!='refDB' or batch.get('ownerArtifact')!='cpf-reference' or batch.get('optional') is not True:fail(vendor+' batch pack ownership')
    if batch.get('generatedDomainAllowed') is not False or set(batch.get('tables',[]))!=set(BATCH_TABLES):fail(vendor+' batch pack tables')
    if batch.get('featureToggle')!='cpf.reference.features.batch.enabled':fail(vendor+' batch feature toggle')
    required={'canonicalSource','freshInstall','migration','rollback','runtimeQueries','verify','checksumManifest'}
    if not required.issubset(batch):fail(vendor+' batch lifecycle metadata')

def main():
    ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--mode',choices=('auto','overlay','merged'),default='auto');a=ap.parse_args();root=Path(a.root).resolve()
    mode=('merged' if (root/'.git').exists() else 'overlay') if a.mode=='auto' else a.mode
    core_signatures={};core_norm={};batch_signatures={};batch_norm={}
    for v in VENDORS:
        base=root/f'cpf-tools/db/vendor/{v}'
        core={
          'source':base/'source/57_reference_edu_operation_ledger.sql','install':base/'install/01_reference_edu_operation_ledger.sql',
          'migration':base/'migration/flyway/refDB/V93__manual_edu_135_operation_ledger.sql','rollback':base/'rollback/refDB/U93__manual_edu_135_operation_ledger.sql',
          'runtime':base/'runtime/ref/manual_edu_135_operation_queries.sql','verify':base/'verify/93_verify_manual_edu_135_operation_ledger.sql'}
        batch={
          'source':base/'source/58_reference_batch_job_pack.sql','install':base/'install/02_reference_batch_job_pack.sql',
          'migration':base/'migration/flyway/refDB/V94__reference_batch_job_pack.sql','rollback':base/'rollback/refDB/U94__reference_batch_job_pack.sql',
          'runtime':base/'runtime/ref/reference_batch_job_queries.sql','verify':base/'verify/94_verify_reference_batch_job_pack.sql'}
        checksum=base/'migration/flyway/refDB/checksums.sha256'
        for group in (core,batch):
            for role,p in group.items():
                if not p.is_file():fail(f'{v} {role} missing: {p.relative_to(root)}')
        if not checksum.is_file() or not (base/'pack.json').is_file():fail(v+' checksum/pack missing')
        core_sql=core['migration'].read_text(encoding='utf-8');batch_sql=batch['migration'].read_text(encoding='utf-8')
        core_signatures[v]=canonical(core_sql,CORE_TABLES);batch_signatures[v]=canonical(batch_sql,BATCH_TABLES)
        core_norm[v]=normalized_ddl(core_sql);batch_norm[v]=normalized_ddl(batch_sql)
        for role in ('source','install'):
            if normalized_ddl(core[role].read_text(encoding='utf-8'))!=core_norm[v]:fail(f'{v} core {role} drift')
            # V94 has a one-line upgrade comment; normalized function removes comments.
            if normalized_ddl(batch[role].read_text(encoding='utf-8'))!=batch_norm[v]:fail(f'{v} batch {role} drift')
        core_rollback=core['rollback'].read_text(encoding='utf-8').upper()
        core_drop=('CPF_EDU_COUNTERPARTY_REQUEST','CPF_EDU_BUSINESS_RECORD','CPF_EDU_OUTBOX','CPF_EDU_OPERATION_AUDIT','CPF_EDU_OPERATION_TARGET','CPF_EDU_LEASE','CPF_EDU_OPERATION')
        positions=[]
        for t in core_drop:
            m=re.search(r'(?m)^DROP TABLE '+re.escape(t)+r';?\s*$',core_rollback)
            if not m:fail(v+' core rollback missing '+t)
            positions.append(m.start())
        if positions!=sorted(positions):fail(v+' core rollback order')
        batch_rollback=batch['rollback'].read_text(encoding='utf-8').upper()
        batch_drop=('CPF_REF_BAT_TARGET_RESULT','CPF_REF_BAT_CHECKPOINT','CPF_REF_BAT_JOB_EXECUTION')
        positions=[]
        for t in batch_drop:
            m=re.search(r'(?m)^DROP TABLE '+re.escape(t)+r';?\s*$',batch_rollback)
            if not m:fail(v+' batch rollback missing '+t)
            positions.append(m.start())
        if positions!=sorted(positions):fail(v+' batch rollback order')
        check_digest(checksum,[core['migration'],batch['migration']])
        for label,group,tables in [('core',core,CORE_TABLES),('batch',batch,BATCH_TABLES)]:
            runtime=group['runtime'].read_text(encoding='utf-8').upper();verify=group['verify'].read_text(encoding='utf-8').upper()
            if any(t not in runtime for t in tables):fail(f'{v} {label} runtime query incomplete')
            if any(t not in verify for t in tables):fail(f'{v} {label} verify incomplete')
        check_pack(base,v)
    if len({tuple(core_signatures[v].items()) for v in VENDORS})!=1:fail('core vendor column/order parity')
    if len({tuple(batch_signatures[v].items()) for v in VENDORS})!=1:fail('batch vendor column/order parity')
    if len(set(core_norm.values()))!=1:fail('core vendor normalized semantic parity')
    if len(set(batch_norm.values()))!=1:fail('batch vendor normalized semantic parity')
    centralp=root/'cpf-tools/generator/contracts/central-domain-template-contract.json';ownershipp=root/'cpf-tools/generator/contracts/reference-edu-schema-ownership-contract.json'
    if not centralp.is_file() or not ownershipp.is_file():fail('generator ownership contract missing')
    central=json.loads(centralp.read_text(encoding='utf-8'));ownership=json.loads(ownershipp.read_text(encoding='utf-8'))
    if central.get('physicalTableContract')!={'totalTables':2,'businessTableCount':1,'supportLedgerCount':1,'additionalTablesAllowed':False}:fail('generated-domain physical table contract must remain 2')
    exclusion=central.get('referenceOwnedExclusions',{})
    if exclusion.get('generatedDomainAllowed') is not False or not set(('CPF_EDU_','CPF_REF_BAT_')).issubset(exclusion.get('forbiddenTablePrefixes',[])):fail('central REF-only exclusions missing')
    if ownership.get('ownerModule')!='cpf-reference' or ownership.get('logicalDatabase')!='refDB' or ownership.get('generatedDomainAllowed') is not False:fail('REF schema ownership contract')
    if not set(('CPF_EDU_','CPF_REF_BAT_')).issubset(ownership.get('forbiddenGeneratedDomainPrefixes',[])):fail('ownership prefix exclusions')
    propagation=ownership.get('queryChangePropagation',[])
    required_steps={'canonical-refdb-source','oracle-vendor-pack','postgresql-vendor-pack','mariadb-vendor-pack','install','upgrade','rollback','runtime-query','verify','checksum','generator-exclusion-contract','generated-domain-negative-test'}
    if not required_steps.issubset(propagation):fail('query/generator propagation contract incomplete')
    batch_meta=(ownership.get('featurePacks') or {}).get('batch',{})
    if batch_meta.get('optional') is not True or batch_meta.get('ownerPackage')!='com.cpf.reference.batch':fail('batch ownership contract incomplete')
    for v in VENDORS:
        base=root/f'cpf-tools/db/vendor/{v}/domain-template'
        if mode=='merged':
            for rel in central['requiredTemplates']:
                if not (base/rel).is_file():fail(f'base Golden Template missing {base.relative_to(root)}/{rel}')
        if base.exists():
            for p in base.rglob('*'):
                if p.is_file() and any(token in p.read_text(encoding='utf-8',errors='ignore').upper() for token in FORBIDDEN):fail('generated-domain template contains REF token: '+str(p.relative_to(root)))
    generated_root=root/'cpf-tools/generator/templates'
    if generated_root.exists():
        for p in generated_root.rglob('*'):
            if p.is_file() and any(t in p.read_text(encoding='utf-8',errors='ignore').upper() for t in FORBIDDEN):fail('generator template contains REF token: '+str(p.relative_to(root)))
    create=root/'cpf-tools/generator/create-domain.ps1'
    if create.is_file() and any(t in create.read_text(encoding='utf-8',errors='ignore').upper() for t in FORBIDDEN):fail('create-domain.ps1 links generated domains to REF schema')
    if list((root/'cpf-reference').rglob('*.sql')):fail('module-local SQL found under cpf-reference')
    print(f'[CPF][QA37][DB-GENERATOR][PASS] mode={mode} vendors=3 coreTables=7 batchTables=3 generatedDomainTables=2 generatedDomainRefLinks=0')
if __name__=='__main__':main()
