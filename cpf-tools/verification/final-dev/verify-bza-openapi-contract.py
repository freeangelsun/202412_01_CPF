#!/usr/bin/env python3
import json,re,sys
from pathlib import Path
root=Path(sys.argv[1] if len(sys.argv)>1 else '.').resolve()
spec=json.loads((root/'cpf-biz-admin/frontend/openapi/cpf-openapi.json').read_text(encoding='utf-8'))
retired={'bzaBackofficeFindApprovals','bzaBackofficeFindApproval','bzaBackofficeCreateApproval','bzaBackofficeActApproval'}
active={op.get('operationId') for p in spec['paths'].values() for m,op in p.items() if m.lower() in {'get','post','put','patch','delete'} and isinstance(op,dict)}
errors=[]
if retired & active: errors.append('retired operation remains active: '+','.join(sorted(retired&active)))
controller=(root/'cpf-biz-admin/src/main/java/com/cpf/bizadmin/backoffice/controller/BzaBackofficeController.java').read_text(encoding='utf-8')
if controller.count('@Hidden') < 4 or 'HttpStatus.GONE' not in controller: errors.append('backend hidden+410 compatibility incomplete')
for rel in ['cpf-biz-admin/frontend/src/generated/cpf-api.ts','cpf-biz-admin/frontend/src/generated/orval/cpf-api.ts','cpf-biz-admin/frontend/src/generated/cpf-operation-contract.ts','cpf-biz-admin/frontend/src/generated/bza-route-operation-contract.ts']:
 txt=(root/rel).read_text(encoding='utf-8')
 for oid in retired:
  if oid in txt: errors.append(f'{rel}: retired client symbol {oid}')
for path,item in spec['paths'].items():
 for method,op in item.items():
  if method.lower() not in {'get','post','put','patch','delete'}: continue
  rs=op.get('responses',{})
  for code in ('401','403','429','500','503'):
   if code not in rs: errors.append(f'{method} {path} missing {code}')
  if '{' in path and '404' not in rs: errors.append(f'{method} {path} missing 404')
  if method.lower() in {'post','put','patch','delete'}:
   for code in ('409','422'):
    if code not in rs: errors.append(f'{method} {path} missing {code}')
if 'CpfApiError' not in spec.get('components',{}).get('schemas',{}): errors.append('CpfApiError schema missing')
if errors:
 print('FAIL '+'; '.join(errors[:20]));sys.exit(1)
print(f'PASS BZA OpenAPI activeOperations={len(active)} retired=4 standardErrors=bound generatedClient=clean')
