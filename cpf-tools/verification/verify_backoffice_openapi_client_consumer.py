#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, re, sys
from pathlib import Path
HTTP={'get','post','put','patch','delete','head','options'}

def fail(msg:str)->int:
    print(json.dumps({'status':'FAIL','message':msg},ensure_ascii=False)); return 1

def fn_name(operation_id:str)->str:
    parts=[p for p in re.sub(r'^MBW_','',operation_id).lower().split('_') if p]
    return parts[0]+''.join(p[:1].upper()+p[1:] for p in parts[1:])

def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); a=ap.parse_args(); root=Path(a.root).resolve()
    spec_path=root/'cpf-backoffice-web/frontend/openapi/cpf-openapi.json'
    routes_path=root/'cpf-backoffice-web/src/main/resources/backoffice-routes.tsv'
    generated_path=root/'cpf-backoffice-web/frontend/src/generated/backoffice-api.ts'
    for p in [spec_path,routes_path,generated_path]:
        if not p.is_file(): return fail(f'missing {p.relative_to(root)}')
    spec=json.loads(spec_path.read_text(encoding='utf-8'))
    backend={}
    for path,item in (spec.get('paths') or {}).items():
        for method,op in (item or {}).items():
            if method.lower() not in HTTP or not isinstance(op,dict) or not op.get('operationId'): continue
            oid=str(op['operationId']); key=(method.upper(),path,oid)
            if oid in backend: return fail(f'duplicate OpenAPI operationId: {oid}')
            backend[oid]=key
    routes={}
    for line in routes_path.read_text(encoding='utf-8-sig').splitlines():
        if not line.strip() or line.lstrip().startswith('#'): continue
        cols=line.split('\t')
        if len(cols)<3: return fail(f'invalid route row: {line}')
        method,path,oid=cols[:3]
        if oid in routes: return fail(f'duplicate route operationId: {oid}')
        routes[oid]=(method.upper(),path,oid)
    text=generated_path.read_text(encoding='utf-8')
    desc={}
    m=re.search(r'cpfBackofficeGeneratedOperations\s*=\s*(\[[\s\S]*?\])\s+as const',text)
    if not m: return fail('generated operation descriptor array missing')
    try: arr=json.loads(m.group(1))
    except Exception as e: return fail(f'invalid generated descriptor JSON: {e}')
    for row in arr:
        oid=row.get('operationId');
        if not oid or oid in desc: return fail(f'invalid/duplicate generated descriptor: {oid}')
        desc[oid]=(str(row.get('method','')).upper(),str(row.get('path','')),oid)
    functions={}
    pat=re.compile(r'export async function\s+(\w+)\((.*?)\)\s*\{\s*return invokeBackoffice\("([A-Z]+)",\s*(`[^`]+`|"[^"]+")',re.S)
    for name,args,method,path_expr in pat.findall(text):
        raw=path_expr[1:-1]
        path=re.sub(r'\$\{encodeURIComponent\((\w+)\)\}',r'{\1}',raw)
        if name in functions: return fail(f'duplicate generated function: {name}')
        functions[name]=(method,path)
    expected_names={fn_name(oid):(method,path,oid) for oid,(method,path,_) in backend.items()}
    findings=[]
    if backend!=routes: findings.append(f'backend/routes mismatch backend={len(backend)} routes={len(routes)}')
    if backend!=desc: findings.append(f'backend/descriptors mismatch backend={len(backend)} descriptors={len(desc)}')
    if set(functions)!=set(expected_names): findings.append(f'generated function set mismatch expected={len(expected_names)} actual={len(functions)}')
    for name,(method,path,oid) in expected_names.items():
        if name in functions and functions[name]!=(method,path): findings.append(f'function invocation mismatch {oid}: expected={method} {path} actual={functions[name]}')
    # Actual UI imports/calls are allowed to be a useful subset, but every imported generated function must exist.
    src=root/'cpf-backoffice-web/frontend/src'
    consumer_files=[]; consumer_names=set()
    imp=re.compile(r'import\s*\{([^}]+)\}\s*from\s*[\'\"](?:\.\./)*generated/backoffice-api[\'\"]',re.S)
    for p in src.rglob('*'):
        if not p.is_file() or p.suffix not in {'.ts','.vue'} or p==generated_path: continue
        t=p.read_text(encoding='utf-8',errors='ignore')
        hits=[]
        for block in imp.findall(t):
            hits += [x.strip().split(' as ')[0].strip() for x in block.split(',') if x.strip()]
        if hits:
            consumer_files.append(str(p.relative_to(root)))
            consumer_names.update(hits)
    missing_consumers=sorted(x for x in consumer_names if x not in functions and x!='cpfBackofficeGeneratedOperations')
    if missing_consumers: findings.append(f'consumer imports missing generated functions: {missing_consumers}')
    if not consumer_files: findings.append('no actual frontend consumer imports generated Backoffice client')
    payload={'status':'PASS' if not findings else 'FAIL','backendOperations':len(backend),'routeOperations':len(routes),'generatedDescriptors':len(desc),'generatedFunctions':len(functions),'actualConsumerFunctions':len([x for x in consumer_names if x in functions]),'actualConsumerFiles':len(consumer_files),'findings':findings}
    print(json.dumps(payload,ensure_ascii=False,indent=2))
    return 0 if not findings else 1
if __name__=='__main__': raise SystemExit(main())
