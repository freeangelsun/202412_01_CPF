#!/usr/bin/env python3
"""Verify BZA route, status, generated API and browser trust-boundary contracts."""
from __future__ import annotations
import argparse,json,re,sys
from pathlib import Path
class ContractError(RuntimeError):pass

def validate(root:Path)->dict:
    routes=root/'cpf-biz-admin/frontend/src/app/routes.ts';router=root/'cpf-biz-admin/frontend/src/app/router.ts';app=root/'cpf-biz-admin/frontend/src/App.vue'
    api=root/'cpf-biz-admin/frontend/src/shared/cpfApi.ts';spec=root/'cpf-biz-admin/frontend/openapi/cpf-openapi.json';marker=root/'cpf-biz-admin/frontend/src/generated/.cpf-openapi-source.json'
    for p in (routes,router,app,api,spec,marker):
        if not p.is_file():raise ContractError(f'missing {p.relative_to(root)}')
    route_text=routes.read_text(encoding='utf-8');router_text=router.read_text(encoding='utf-8');app_text=app.read_text(encoding='utf-8');api_text=api.read_text(encoding='utf-8')
    route_ids=re.findall(r'\{ id:"([^"]+)"',route_text)
    if len(route_ids)!=26 or len(set(route_ids))!=26:raise ContractError(f'BZA route baseline drift expected=26 actual={len(route_ids)}')
    if 'BzaRouteId | undefined' not in route_text or 'return undefined' not in route_text:raise ContractError('unknown BZA route must remain undefined')
    if 'allowed[0]' in app_text or 'router.replace({name:selected.id})' in app_text:raise ContractError('BZA silent dashboard/menu fallback remains')
    for status in ('forbidden','feature-disabled','lazy-load-failure','not-found'):
        if f'name: "{status}"' not in router_text:raise ContractError(f'missing status route={status}')
    if 'bzaRouter.onError' not in router_text:raise ContractError('lazy-load failure handler missing')
    for token in ('X-CPF-Operation-Id','X-XSRF-TOKEN','Authorization','same-origin','CLIENT_ACTOR_FIELDS'):
        if token not in api_text:raise ContractError(f'BZA browser trust boundary missing token={token}')
    openapi=json.loads(spec.read_text(encoding='utf-8'));m=json.loads(marker.read_text(encoding='utf-8'))
    if openapi.get('x-cpf-product-module')!='BZA' or openapi.get('x-cpf-export-origin')!='CONTROLLER_SOURCE_PRE_RUNTIME':raise ContractError('BZA pre-runtime OpenAPI identity drift')
    if int(openapi.get('x-cpf-openapi-operation-count',0))<1:raise ContractError('BZA operation inventory empty')
    if m.get('schemaVersion')!=3 or m.get('origin')!='CONTROLLER_SOURCE_PRE_RUNTIME' or m.get('releaseEligible') is not False:raise ContractError('BZA generated marker contract drift')
    return {'routes':len(route_ids),'operations':openapi['x-cpf-openapi-operation-count']}
def main()->int:
    ap=argparse.ArgumentParser();ap.add_argument('--root',type=Path,default=Path.cwd());args=ap.parse_args();result=validate(args.root.resolve());print(f"[PASS] BZA route contract routes={result['routes']} operations={result['operations']} silentFallback=0");return 0
if __name__=='__main__':
    try:raise SystemExit(main())
    except ContractError as e:print(f'[FAIL] {e}',file=sys.stderr);raise SystemExit(1)
