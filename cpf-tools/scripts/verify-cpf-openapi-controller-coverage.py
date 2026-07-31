#!/usr/bin/env python3
import argparse,json,re
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--root',default='.');p.add_argument('--module',required=True);p.add_argument('--openapi',required=True);a=p.parse_args()
root=Path(a.root).resolve(); module=root/a.module
spec=json.loads((root/a.openapi).read_text(encoding='utf-8-sig'))
operations={}
for path,item in (spec.get('paths') or {}).items():
  for method,op in (item or {}).items():
    if method.lower() in {'get','post','put','patch','delete','head','options','trace'}:
      oid=(op or {}).get('operationId')
      if not oid: raise SystemExit(f'OpenAPI operationId missing: {method} {path}')
      operations[oid]=(method.upper(),path)
source_ids=set(); mapping_methods=0; missing=[]
for java in module.rglob('*.java'):
  text=java.read_text(encoding='utf-8',errors='ignore')
  if '@RestController' not in text and '@Controller' not in text: continue
  # Mapping annotation followed by a method declaration; require explicit @Operation(operationId=...).
  pattern=re.compile(r'((?:@[A-Za-z0-9_.]+(?:\([^)]*(?:\([^)]*\)[^)]*)*\))?\s*)+)\s*(?:public|protected)\s+[^{;]+\(',re.S)
  for m in pattern.finditer(text):
    annotations=m.group(1)
    if not re.search(r'@(Get|Post|Put|Patch|Delete|Request)Mapping\b',annotations): continue
    mapping_methods+=1
    oid=re.search(r'@Operation\s*\([^)]*operationId\s*=\s*"([^"]+)"',annotations,re.S)
    if not oid: missing.append(f'{java.relative_to(root)}:{text.count(chr(10),0,m.start())+1}')
    else: source_ids.add(oid.group(1))
if missing: raise SystemExit('Controller mapping without explicit operationId: '+', '.join(missing[:20]))
not_exported=sorted(source_ids-set(operations))
if not_exported: raise SystemExit('Controller operations missing from runtime OpenAPI: '+', '.join(not_exported[:30]))
if len(operations)<mapping_methods: raise SystemExit(f'OpenAPI operation count smaller than controller mappings: {len(operations)} < {mapping_methods}')
print(f'CPF OpenAPI/controller coverage: PASS mappings={mapping_methods} explicit={len(source_ids)} exported={len(operations)}')
