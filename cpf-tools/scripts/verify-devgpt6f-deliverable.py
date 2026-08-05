#!/usr/bin/env python3
"""Fail-closed validator for the DEVGPT-6F root overlay ZIP."""
from __future__ import annotations
import argparse,csv,hashlib,io,json,re,zipfile
from pathlib import PurePosixPath

PROTECTED=(
    'cpf-docs/deliverables/','cpf-docs/guides/','cpf-docs/environment/docker/',
    'cpf-tools/environment/docker-development-test/'
)
FORBIDDEN_PARTS={'__pycache__','.pytest_cache','node_modules','.gradle','build'}
EXPECTED={
 'scope/WORK_ITEM_SCOPE.csv':224,
 'scope/CANONICAL_REQUIREMENT_SCOPE.csv':58,
 'scope/CPF_FR_SCOPE.csv':5658,
 'scope/CPF_SC_SCOPE.csv':7878,
 'scope/ENGINEERING_GATE_SCOPE.csv':21,
 'review/WORK_ITEM_DEVELOPMENT_REVIEW.csv':224,
 'review/REQUIREMENT_DEVELOPMENT_REVIEW.csv':5658,
 'review/SCENARIO_DEVELOPMENT_REVIEW.csv':7878,
 'review/ENGINEERING_GATE_RESULT.csv':21,
}
BASE='cpf-docs/work/current/development-session-results/DEV-20260805-R01/DEVGPT-6F/REV-001/'
SECRET_PATTERNS=(
 re.compile(rb'AKIA[0-9A-Z]{16}'),
 re.compile(rb'-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----'),
 re.compile(rb'(?i)(?:password|secret|token)\s*[:=]\s*["\'][^"\']{12,}["\']'),
)

def rows(data:bytes)->list[dict[str,str]]:
    text=data.decode('utf-8-sig')
    return list(csv.DictReader(io.StringIO(text)))

def norm(name:str)->str:
    if '\\' in name or name.startswith('/') or re.match(r'^[A-Za-z]:',name):
        raise ValueError(f'non root-relative path: {name}')
    p=PurePosixPath(name)
    if any(x in {'','.', '..'} for x in p.parts):
        raise ValueError(f'unsafe path: {name}')
    return p.as_posix()

def validate(zip_path:str)->dict:
    errors=[]
    with zipfile.ZipFile(zip_path) as z:
        infos=[i for i in z.infolist() if not i.is_dir()]
        names=[]
        for i in infos:
            try:n=norm(i.filename)
            except ValueError as e:errors.append(str(e));continue
            names.append(n)
            if n.startswith(PROTECTED):errors.append(f'protected path: {n}')
            if n.endswith('.class') or any(part in FORBIDDEN_PARTS for part in PurePosixPath(n).parts):
                errors.append(f'generated/cache artifact: {n}')
        if len(names)!=len(set(names)):errors.append('duplicate ZIP entries')
        name_set=set(names)
        manifest_path=BASE+'PACKAGE_MANIFEST.json'
        if manifest_path not in name_set:
            errors.append('missing PACKAGE_MANIFEST.json')
            manifest={'files':[]}
        else:
            try:manifest=json.loads(z.read(manifest_path))
            except Exception as e:errors.append(f'invalid manifest: {e}');manifest={'files':[]}
        declared={x.get('path'):x for x in manifest.get('files',[])}
        expected_entries=set(declared)|{manifest_path}
        if name_set!=expected_entries:
            errors.append(f'manifest/ZIP entry mismatch missing={sorted(expected_entries-name_set)[:5]} extra={sorted(name_set-expected_entries)[:5]}')
        for p,m in declared.items():
            try:p2=norm(p)
            except ValueError as e:errors.append(str(e));continue
            if p2 not in name_set:continue
            data=z.read(p2)
            if len(data)!=m.get('size'):errors.append(f'size mismatch: {p2}')
            if hashlib.sha256(data).hexdigest()!=m.get('sha256'):errors.append(f'hash mismatch: {p2}')
        if manifest.get('baselineSha')!='09dd686c5ae0826594b9c5e1f871d95d95d3ce1c':errors.append('baseline mismatch')
        for rel,count in EXPECTED.items():
            p=BASE+rel
            if p not in name_set:errors.append(f'missing ledger: {p}');continue
            got=len(rows(z.read(p)))
            if got!=count:errors.append(f'row count {p}: {got} != {count}')
        delete_path=BASE+'DELETE_MANIFEST.csv'
        if delete_path not in name_set:errors.append('missing DELETE_MANIFEST.csv')
        elif rows(z.read(delete_path)):errors.append('DELETE_MANIFEST has data rows')
        # Evidence paths must resolve to a packaged file, not merely a non-empty string.
        for rel in ('review/WORK_ITEM_DEVELOPMENT_REVIEW.csv','review/REQUIREMENT_DEVELOPMENT_REVIEW.csv','review/SCENARIO_DEVELOPMENT_REVIEW.csv','review/ENGINEERING_GATE_RESULT.csv'):
            p=BASE+rel
            if p not in name_set:continue
            for idx,row in enumerate(rows(z.read(p)),2):
                ev=row.get('evidence_path','')
                if not ev:errors.append(f'missing evidence field {p}:{idx}');continue
                for item in filter(None,(x.strip() for x in ev.split(';'))):
                    try:item=norm(item)
                    except ValueError as e:errors.append(f'{p}:{idx}: {e}');continue
                    if item not in name_set:errors.append(f'missing evidence file {p}:{idx}: {item}')
        for n in names:
            if n.endswith(('.java','.py','.ps1','.json','.csv','.md','.txt','.gradle','.properties')):
                data=z.read(n)
                for pat in SECRET_PATTERNS:
                    if pat.search(data):errors.append(f'possible secret: {n}');break
    result={'zip':zip_path,'valid':not errors,'errors':errors,'entryCount':len(names)}
    if errors:raise SystemExit(json.dumps(result,ensure_ascii=False,indent=2))
    return result

def main()->int:
    ap=argparse.ArgumentParser();ap.add_argument('--zip',required=True);ap.add_argument('--json-out')
    a=ap.parse_args(); result=validate(a.zip)
    text=json.dumps(result,ensure_ascii=False,indent=2)
    print(text)
    if a.json_out:
        from pathlib import Path
        Path(a.json_out).write_text(text+'\n',encoding='utf-8')
    return 0
if __name__=='__main__':raise SystemExit(main())
