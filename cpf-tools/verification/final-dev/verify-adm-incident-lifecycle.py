#!/usr/bin/env python3
from pathlib import Path
import argparse, json, re, sys
ACTUAL = [
'admIncidentFindIncidents','admIncidentFindMaintenance','admIncidentFindPolicies','admIncidentIngestSignal',
'admIncidentCreateMaintenance','admIncidentUpdateMaintenance','admIncidentCreatePolicy','admIncidentUpdatePolicy',
'admIncidentFindIncident','admIncidentAcknowledge','admIncidentEscalate','admIncidentReopen','admIncidentResolve','admIncidentFindTimeline']
PHANTOM=['admIncidentCreateIncident','admIncidentTransitionIncident']

def check(root:Path):
    files={
      'routes':root/'cpf-admin/frontend/src/app/routes.ts',
      'spec':root/'cpf-admin/frontend/openapi/cpf-openapi.json',
      'client':root/'cpf-admin/frontend/src/generated/cpf-api.ts',
      'page':root/'cpf-admin/frontend/src/features/operations/ErrorWorkbenchPage.vue',
      'panel':root/'cpf-admin/frontend/src/features/operations/IncidentLifecyclePanel.vue'}
    errs=[]
    for k,p in files.items():
        if not p.exists(): errs.append(f'missing:{k}:{p}')
    if errs:return errs
    routes=files['routes'].read_text(); client=files['client'].read_text(); page=files['page'].read_text(); panel=files['panel'].read_text()
    spec=json.loads(files['spec'].read_text())
    active=[]
    for path,item in spec.get('paths',{}).items():
      for method,op in item.items():
        if method.lower() in {'get','post','put','patch','delete'} and isinstance(op,dict) and op.get('operationId','').startswith('admIncident'):
          active.append(op['operationId'])
    for bad in PHANTOM:
      if bad in routes: errs.append('phantom-route:'+bad)
      if bad in active: errs.append('phantom-openapi:'+bad)
      if re.search(rf'export async function\s+{re.escape(bad)}\b',client): errs.append('phantom-generated:'+bad)
    if '<IncidentLifecyclePanel />' not in page or "import IncidentLifecyclePanel" not in page: errs.append('page-panel-not-wired')
    for op in ACTUAL:
      if op not in active: errs.append('active-missing:'+op)
      if not re.search(rf'export async function\s+{re.escape(op)}\b',client): errs.append('generated-missing:'+op)
      # Import/name plus invocation, not route metadata only.
      if op not in panel: errs.append('panel-missing:'+op)
      if not re.search(rf'\b{re.escape(op)}(?:<[^>]+>)?\s*\(',panel): errs.append('panel-no-call:'+op)
    if 'admMutation(' in panel or 'admInvokeOperation(' in panel or 'admApi<' in panel or 'fetch(' in panel:
      errs.append('panel-raw-bypass')
    return errs

def main():
  ap=argparse.ArgumentParser();ap.add_argument('--root',required=True);ap.add_argument('--self-test',action='store_true');a=ap.parse_args();root=Path(a.root)
  errs=check(root)
  if errs:
    print('[CPF][ADM-INCIDENT][FAIL]',*errs,sep='\n');return 1
  if a.self_test:
    p=root/'cpf-admin/frontend/src/features/operations/IncidentLifecyclePanel.vue'; orig=p.read_text()
    # Remove one real call while leaving import/name present; semantic invocation check must catch it.
    mutated=orig.replace('admIncidentAcknowledge<Row>(', 'missingIncidentAcknowledge<Row>(', 1);p.write_text(mutated)
    detected=bool(check(root));p.write_text(orig)
    if not detected:
      print('[CPF][ADM-INCIDENT][FAIL] mutation-not-detected');return 1
  print('[CPF][ADM-INCIDENT][PASS] activeLifecycle=14 phantom=0 generatedConsumer=true selfTest='+str(a.self_test).lower());return 0
if __name__=='__main__':sys.exit(main())
