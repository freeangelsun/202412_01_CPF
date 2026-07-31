#!/usr/bin/env python3
import argparse, json
from pathlib import Path
HTTP_METHODS={'get','post','put','patch','delete','head','options','trace'}
p=argparse.ArgumentParser();p.add_argument('--input',required=True);p.add_argument('--output',required=True);p.add_argument('--module',required=True);a=p.parse_args()
spec=json.loads(Path(a.input).read_text(encoding='utf-8-sig'))
for key in ['x-cpf-source-sha','x-cpf-result-sha']:
    spec.pop(key,None)
spec.pop('servers',None)
ids=[]
for path, item in (spec.get('paths') or {}).items():
    for method, op in (item or {}).items():
        if method.lower() not in HTTP_METHODS: continue
        if not isinstance(op,dict) or not op.get('operationId'):
            raise SystemExit(f'operationId missing: {method.upper()} {path}')
        ids.append(op['operationId'])
if len(ids)!=len(set(ids)): raise SystemExit('duplicate operationId')
if not ids: raise SystemExit('empty OpenAPI operation inventory')
spec['x-cpf-export-origin']='BACKEND_RUNTIME'
spec['x-cpf-product-module']=a.module.upper()
spec['x-cpf-openapi-operation-count']=len(ids)
Path(a.output).parent.mkdir(parents=True,exist_ok=True)
Path(a.output).write_text(json.dumps(spec,ensure_ascii=False,sort_keys=True,separators=(',',':'))+'\n',encoding='utf-8')
print(f'CPF canonical OpenAPI: PASS operations={len(ids)}')
