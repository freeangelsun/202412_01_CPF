#!/usr/bin/env python3
"""Fail-closed gate for the canonical 20 Online + 15 Batch CPF education Golden Path."""
from __future__ import annotations
import argparse, json, re
from pathlib import Path

ONLINE = {
 'basiccrud','querypaging','common','validation','internalservice','domaincall','externalrest','fixedlength',
 'transaction.required','transaction.requiresnew','externalsideeffect','ondemandbatch','centercut','cache','messaging',
 'file','securityaudit','recovery','concurrency','webhook'
}
BATCH = {'tasklet','chunk','flatfile','partition','centercut','scheduler','restart','distributedworker','shellcommand','conditionalflow','chunktransaction','requiresnew','steptransaction','externalcall','ondemand'}
FORBIDDEN_IMPORT = re.compile(r'^import\s+.*(?:\.internal\.|\.impl\.|\.provider\.internal\.|runtime\.internal\.)', re.M)
FORBIDDEN_NAME = re.compile(r'^(?:Online|Batch)\d+|(?:Example|Sample|Demo)(?:\.java)?$', re.I)
CATALOG = 'cpf-education/src/main/resources/education/cpf-education-canonical-35.json'
DELETE_MANIFEST = 'cpf-docs/work/current/DELETE_MANIFEST.txt'

def deleted(root: Path) -> set[str]:
    p=root/DELETE_MANIFEST
    if not p.exists(): return set()
    return {line.strip().replace('\\','/') for line in p.read_text(encoding='utf-8').splitlines() if line.strip() and not line.lstrip().startswith('#')}

def active_java(root: Path, base: Path, deleted_paths: set[str]):
    out=[]
    for p in base.rglob('*.java'):
        rel=p.relative_to(root).as_posix()
        if rel not in deleted_paths: out.append(p)
    return out

def package_key(p: Path, base: Path, category: str) -> str:
    rel=p.relative_to(base).parts
    if category=='online' and rel and rel[0]=='transaction' and len(rel)>1:
        return 'transaction.'+rel[1]
    return rel[0] if rel else ''

def main():
    ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');a=ap.parse_args();root=Path(a.root).resolve();errors=[]
    dp=deleted(root)
    java_root=root/'cpf-education/src/main/java/com/cpf/education'
    test_root=root/'cpf-education/src/test/java/com/cpf/education'
    if not java_root.is_dir(): errors.append('canonical Education Java root missing')
    else:
        online_files=active_java(root,java_root/'online',dp)
        batch_files=active_java(root,java_root/'batch',dp)
        online_keys={package_key(p,java_root/'online','online') for p in online_files}
        batch_keys={package_key(p,java_root/'batch','batch') for p in batch_files}
        if online_keys != ONLINE: errors.append('online feature packages mismatch missing='+','.join(sorted(ONLINE-online_keys))+' extra='+','.join(sorted(online_keys-ONLINE)))
        if batch_keys != BATCH: errors.append('batch feature packages mismatch missing='+','.join(sorted(BATCH-batch_keys))+' extra='+','.join(sorted(batch_keys-BATCH)))
        flat=[p for p in online_files+batch_files if len(p.relative_to(java_root/('online' if '/online/' in p.as_posix() else 'batch')).parts)==1]
        if flat: errors.append('flat canonical Java remains: '+','.join(p.name for p in flat[:20]))
        for p in online_files+batch_files:
            if re.match(r'^(?:Online|Batch)\d+',p.name) or re.search(r'(?:Example|Sample|Demo)\.java$',p.name,re.I): errors.append('non-functional Java name: '+p.relative_to(root).as_posix())
            text=p.read_text(encoding='utf-8',errors='replace')
            if FORBIDDEN_IMPORT.search(text): errors.append('internal/raw implementation import: '+p.relative_to(root).as_posix())
    cp=root/CATALOG
    if not cp.exists(): errors.append('canonical 35 catalog missing')
    else:
        try: data=json.loads(cp.read_text(encoding='utf-8'))
        except Exception as e: errors.append('catalog JSON invalid: '+str(e));data={}
        examples=data.get('examples',[]) if isinstance(data,dict) else []
        if len(examples)!=35: errors.append(f'catalog example count must be 35, actual={len(examples)}')
        if sum(1 for x in examples if x.get('category')=='online')!=20: errors.append('catalog online count must be 20')
        if sum(1 for x in examples if x.get('category')=='batch')!=15: errors.append('catalog batch count must be 15')
        ids=[x.get('id') for x in examples]
        if len(ids)!=len(set(ids)): errors.append('duplicate EDU catalog id')
        for x in examples:
            pkg=x.get('package',''); primary=x.get('primaryClass',''); test=x.get('testClass','')
            source_dir=root/'cpf-education/src/main/java'/Path(pkg.replace('.','/'))
            matches=list(source_dir.rglob(primary+'.java')) if source_dir.exists() and primary else []
            matches=[m for m in matches if m.relative_to(root).as_posix() not in dp]
            if len(matches)!=1: errors.append(f"{x.get('id')}: primary class not exactly one: {pkg}.{primary}")
            test_matches=list(test_root.rglob(test+'.java')) if test_root.exists() and test else []
            test_matches=[m for m in test_matches if m.relative_to(root).as_posix() not in dp]
            if len(test_matches)!=1: errors.append(f"{x.get('id')}: test class not exactly one: {test}")
    for e in errors: print('[FAIL]',e)
    if errors: return 1
    print('CPF_EDUCATION_ACTIVE_SURFACE=PASS online=20 batch=15 flat=0 numeric=0 internal_import=0 catalog=35')
    return 0
if __name__=='__main__': raise SystemExit(main())
