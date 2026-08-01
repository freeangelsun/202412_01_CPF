#!/usr/bin/env python3
from __future__ import annotations
import argparse,re,sys
from pathlib import Path
ENTRY=re.compile(r'^\s*"(?P<id>[^"]+)": \{ routeId: "(?P=id)", path: "(?P<path>[^"]+)", menuId: "(?P<menu>[^"]+)".*?riskLevel: "(?P<risk>[^"]+)".*?featureFlag: "(?P<flag>[^"]+)".*?expectedOperationIds: \[(?P<ops>.*?)\].*?import\("(?P<component>[^"]+)"\)',re.M)

def fail(msg): print('[FAIL]',msg,file=sys.stderr); raise SystemExit(1)
def main():
 p=argparse.ArgumentParser();p.add_argument('--root',default='.');p.add_argument('--allow-overlay-missing-components',action='store_true');a=p.parse_args();root=Path(a.root).resolve()
 routes=root/'cpf-admin/frontend/src/app/routes.ts';app=root/'cpf-admin/frontend/src/App.vue';contract=root/'cpf-admin/frontend/src/components/page-contract/pageContract.ts';boundary=root/'cpf-admin/frontend/src/components/page-contract/AdmCommercialPageBoundary.vue'
 for f in (routes,app,contract,boundary):
  if not f.is_file(): fail(f'required page contract file missing: {f.relative_to(root)}')
 text=routes.read_text(encoding='utf-8'); entries=list(ENTRY.finditer(text))
 if len(entries)!=59: fail(f'commercial route contract cardinality must be 59: {len(entries)}')
 ids=set(); paths=set(); missing=[]
 for m in entries:
  d=m.groupdict()
  if d['id'] in ids or d['path'] in paths: fail(f'duplicate route identity/path: {d["id"]} {d["path"]}')
  ids.add(d['id']);paths.add(d['path'])
  if d['flag']!=f'adm.route.{d["id"]}.enabled': fail(f'feature flag drift: {d["id"]}')
  component=(routes.parent/d['component']).resolve()
  if not component.is_file(): missing.append((d['id'],component))
 if missing and not a.allow_overlay_missing_components: fail('route components missing: '+', '.join(i for i,_ in missing[:20]))
 apptext=app.read_text(encoding='utf-8')
 for token in ['<AdmCommercialPageBoundary','aria-live="polite"','<RouterView']:
  if token not in apptext: fail(f'App commercial boundary missing token: {token}')
 ctext=contract.read_text(encoding='utf-8')
 for token in ['401, 403, 404, 409, 429, 500, 503','requiresServerPaging','requiresActionConfirmation','preservesContext','confirmRiskAction']:
  if token not in ctext: fail(f'page contract missing: {token}')
 btext=boundary.read_text(encoding='utf-8')
 for token in ['onErrorCaptured','role="alert"','Correlation ID','aria-busy']:
  if token not in btext: fail(f'page boundary missing: {token}')
 print(f'[PASS] ADM commercial page contract routes={len(entries)} missingComponents={len(missing)} failureStatuses=7')
if __name__=='__main__':main()
