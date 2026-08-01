#!/usr/bin/env python3
"""Fail-closed approved DB baseline metadata and immutable migration hash verifier."""
from __future__ import annotations
import argparse,hashlib,json,re,subprocess,sys
from pathlib import Path

OFFICIAL=('mariadb','postgresql','oracle')
ENTRY=re.compile(r'^([0-9a-f]{64}) \*(V(\d+)__[^/\\]+\.sql)$')
SHA40=re.compile(r'^[0-9a-f]{40}$')

class BaselineError(RuntimeError): pass

def run(root:Path,*args:str,check:bool=True)->bytes:
    process=subprocess.run(['git','-C',str(root),*args],stdout=subprocess.PIPE,stderr=subprocess.PIPE)
    if check and process.returncode:
        raise BaselineError(f"git {' '.join(args)} failed exit={process.returncode}: {process.stderr.decode('utf-8','replace').strip()}")
    return process.stdout

def load_metadata(root:Path)->tuple[Path,dict]:
    path=root/'cpf-tools/db/metadata/CPF_BASELINE_MIGRATION_CHECKSUMS_B894157.json'
    if not path.is_file():raise BaselineError(f'approved baseline metadata missing: {path}')
    try:data=json.loads(path.read_text(encoding='utf-8'))
    except (OSError,json.JSONDecodeError) as error:raise BaselineError(f'invalid approved baseline metadata: {error}') from error
    if data.get('schemaVersion')!=1:raise BaselineError('approved baseline schemaVersion must be 1')
    baseline=str(data.get('baseCommit','')).strip().lower()
    if not SHA40.fullmatch(baseline):raise BaselineError('approved baseline exact SHA is invalid')
    packs=data.get('packs')
    if not isinstance(packs,dict) or not packs:raise BaselineError('approved baseline packs are missing')
    vendors={str(key).split('/',1)[0].lower() for key in packs}
    if vendors!=set(OFFICIAL):raise BaselineError(f'approved baseline vendors must be exactly {OFFICIAL}, actual={sorted(vendors)}')
    if any(token in json.dumps(data).lower() for token in ('mysql','mssql','sqlserver','h2')):raise BaselineError('unsupported vendor leaked into approved baseline metadata')
    return path,data

def parse_entries(data:dict)->list[dict]:
    records=[];seen=set()
    for pack,values in sorted(data['packs'].items()):
        if not isinstance(values,list) or not values:raise BaselineError(f'baseline pack is empty: {pack}')
        vendor,_,logical=pack.partition('/')
        if vendor not in OFFICIAL:raise BaselineError(f'unsupported baseline pack vendor: {pack}')
        for raw in values:
            match=ENTRY.fullmatch(str(raw).strip())
            if not match:raise BaselineError(f'invalid baseline checksum entry: pack={pack} entry={raw}')
            sha256,name,version=match.group(1),match.group(2),int(match.group(3))
            key=(pack,name)
            if key in seen:raise BaselineError(f'duplicate baseline checksum entry: {pack}/{name}')
            seen.add(key);records.append({'pack':pack,'vendor':vendor,'logical':logical,'name':name,'version':version,'sha256':sha256})
    return records

def candidate_paths(paths:list[str],record:dict)->list[str]:
    vendor_token=f'/vendor/{record["vendor"]}/'
    logical=record['logical'].lower()
    candidates=[]
    for path in paths:
        normalized='/'+path.replace('\\','/').lstrip('/')
        if not normalized.endswith('/'+record['name']):continue
        if vendor_token not in normalized.lower():continue
        if record['vendor']!='mariadb' and logical and f'/{logical}/' not in normalized.lower():continue
        candidates.append(path)
    return candidates

def verify_git(root:Path,data:dict,records:list[dict],require_clean:bool=True)->dict:
    baseline=data['baseCommit'].lower()
    run(root,'cat-file','-e',f'{baseline}^{{commit}}')
    if require_clean:
        dirty=run(root,'status','--porcelain=v1','--untracked-files=all').decode().strip()
        if dirty:raise BaselineError(f'approved baseline verification requires clean tree: {dirty}')
    baseline_paths=run(root,'ls-tree','-r','--name-only',baseline).decode('utf-8').splitlines()
    head_paths=run(root,'ls-tree','-r','--name-only','HEAD').decode('utf-8').splitlines()
    verified=[]
    for record in records:
        baseline_candidates=candidate_paths(baseline_paths,record)
        if len(baseline_candidates)!=1:raise BaselineError(f'baseline migration path resolution must be unique: pack={record["pack"]} file={record["name"]} candidates={baseline_candidates}')
        path=baseline_candidates[0]
        baseline_bytes=run(root,'show',f'{baseline}:{path}')
        actual=hashlib.sha256(baseline_bytes).hexdigest()
        if actual!=record['sha256']:raise BaselineError(f'baseline migration hash mismatch: {record["pack"]}/{record["name"]}')
        if path not in head_paths:raise BaselineError(f'historical migration removed from HEAD: {path}')
        head_bytes=run(root,'show',f'HEAD:{path}')
        head_sha=hashlib.sha256(head_bytes).hexdigest()
        if head_sha!=record['sha256']:raise BaselineError(f'historical migration modified after approved baseline: {path}')
        verified.append({'pack':record['pack'],'path':path,'sha256':actual})
    return {'baselineSha':baseline,'verifiedMigrationCount':len(verified),'verified':verified}

def validate(root:Path,git_check:bool=True,require_clean:bool=True)->dict:
    metadata_path,data=load_metadata(root);records=parse_entries(data)
    result={'metadataPath':metadata_path.relative_to(root).as_posix(),'metadataSha256':hashlib.sha256(metadata_path.read_bytes()).hexdigest(),'packCount':len(data['packs']),'migrationCount':len(records),'vendors':list(OFFICIAL)}
    if git_check:result.update(verify_git(root,data,records,require_clean))
    return result

def main()->int:
    parser=argparse.ArgumentParser();parser.add_argument('--root',type=Path,default=Path.cwd());parser.add_argument('--metadata-only',action='store_true');parser.add_argument('--allow-dirty',action='store_true');parser.add_argument('--json-report',type=Path)
    args=parser.parse_args();root=args.root.resolve();result=validate(root,not args.metadata_only,not args.allow_dirty)
    if args.json_report:
        args.json_report.parent.mkdir(parents=True,exist_ok=True);args.json_report.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(f"[PASS] approved baseline metadata vendors={len(result['vendors'])} packs={result['packCount']} migrations={result['migrationCount']} gitVerified={not args.metadata_only}")
    return 0
if __name__=='__main__':
    try:raise SystemExit(main())
    except BaselineError as error:print(f'[FAIL] {error}',file=sys.stderr);raise SystemExit(1)
