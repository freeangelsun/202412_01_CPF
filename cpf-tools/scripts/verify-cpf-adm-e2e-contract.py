#!/usr/bin/env python3
"""Fail-closed static contract for the exhaustive ADM browser suite."""
from __future__ import annotations
import argparse,csv,re,sys
from pathlib import Path
class ContractError(RuntimeError):pass

def require(text:str,token:str,label:str)->None:
    if token not in text:raise ContractError(f'{label} missing token={token}')
def validate(root:Path)->dict:
    matrix=root/'cpf-docs/quality/CPF_20260801_ADM_ROUTE_INTERACTION_MATRIX.csv'
    config=root/'cpf-admin/frontend/playwright.config.ts'
    route_spec=root/'cpf-admin/frontend/e2e/adm-route-contract.spec.ts'
    state_spec=root/'cpf-admin/frontend/e2e/adm-route-error-states.spec.ts'
    api=root/'cpf-admin/frontend/src/shared/cpfApi.ts'
    package=root/'cpf-admin/frontend/package.json'
    for path in (matrix,config,route_spec,state_spec,api,package):
        if not path.is_file():raise ContractError(f'missing {path.relative_to(root)}')
    with matrix.open(encoding='utf-8-sig',newline='') as handle:rows=list(csv.DictReader(handle))
    if len(rows)!=59:raise ContractError(f'route baseline drift expected=59 actual={len(rows)}')
    ids=[r['route_id'] for r in rows];paths=[r['path'] for r in rows]
    if len(set(ids))!=59 or len(set(paths))!=59:raise ContractError('route id/path duplicates')
    for row in rows:
        statuses=set(filter(None,row['required_error_statuses'].split(';')))
        if statuses!={'401','403','404','409','429','500','503'}:raise ContractError(f"mandatory status drift route={row['route_id']} statuses={sorted(statuses)}")
        if row['development_status']!='완료':raise ContractError(f"route development not complete: {row['route_id']}")
    config_text=config.read_text(encoding='utf-8')
    for browser in ('chromium','firefox','webkit'):require(config_text,f'name: "{browser}"','playwright project')
    require(config_text,'CPF_ADM_E2E_STORAGE_STATE','authenticated storage state')
    require(config_text,'CPF_ADM_E2E_RELEASE','release fail-closed mode')
    route_text=route_spec.read_text(encoding='utf-8')
    for token in ('data-route-id','.adm-sidebar button.active','x-cpf-operation-id','expectedOperations','routes.length !== 59'):
        require(route_text,token,'route E2E')
    state_text=state_spec.read_text(encoding='utf-8')
    for status in ('401','403','404','409','429','500','503'):require(state_text,status,'error-state E2E')
    require(state_text,'**/adm/api/**','backend state interception')
    api_text=api.read_text(encoding='utf-8')
    if api_text.count('X-CPF-Operation-Id')<3:raise ContractError('query/mutation/raw-response operation identity propagation incomplete')
    package_text=package.read_text(encoding='utf-8')
    for token in ('"test:e2e"','"test:a11y"'):require(package_text,token,'package browser command')
    return {'routes':len(rows),'browsers':3,'mandatoryStatuses':7}
def main()->int:
    ap=argparse.ArgumentParser();ap.add_argument('--root',type=Path,default=Path.cwd());args=ap.parse_args()
    result=validate(args.root.resolve());print(f"[PASS] ADM E2E contract routes={result['routes']} browsers={result['browsers']} statuses={result['mandatoryStatuses']}");return 0
if __name__=='__main__':
    try:raise SystemExit(main())
    except ContractError as error:print(f'[FAIL] {error}',file=sys.stderr);raise SystemExit(1)
