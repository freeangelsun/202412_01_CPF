#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, re, tempfile
from pathlib import Path

IGNORED={'.git','.gradle','build','node_modules','dist','coverage','playwright-report','test-results','__pycache__'}
FRONTENDS=('cpf-admin/frontend','cpf-biz-admin/frontend')
FETCH_ALLOW={
 'cpf-admin/frontend/src/shared/orval-mutator.ts',
 'cpf-biz-admin/frontend/src/shared/orval-mutator.ts',
}

def text_files(root:Path):
 for base in FRONTENDS:
  src=root/base/'src'
  if not src.exists(): continue
  for p in src.rglob('*'):
   if p.is_file() and p.suffix.lower() in {'.ts','.vue','.js','.mjs'} and not any(x in IGNORED for x in p.parts):
    yield p

def verify(root:Path,overlay:bool)->dict:
 fail=[];checks=0
 for p in text_files(root):
  rel=p.relative_to(root).as_posix();text=p.read_text(encoding='utf-8',errors='replace')
  checks+=1
  if re.search(r'\bfetch\s*\(',text) and rel not in FETCH_ALLOW: fail.append(f'raw fetch outside mutator:{rel}')
  checks+=1
  if re.search(r'localStorage\.(?:getItem|setItem)\([^\n]*(?:token|access|refresh)',text,re.I): fail.append(f'browser token storage:{rel}')
  checks+=1
  if 'getAdmAccessToken' in text or 'clearAdmAccessToken' in text: fail.append(f'deleted browser credential API:{rel}')
  checks+=1
  if re.search(r'(?<!["\'])\b(?:requestUser|actorId|operatorIdOverride)\s*:', text):
   fail.append(f'client actor field declared:{rel}')
 adm=root/'cpf-admin/frontend/src/features/core/methods.ts'
 if adm.exists():
  t=adm.read_text(encoding='utf-8')
  for marker in ('const required = [','await item.run()','initialization.record(item.name, error, true)','initialization.complete()','this.initializationStatus = initialization.status'):
   checks+=1
   if marker not in t: fail.append(f'ADM required/optional initialization marker missing:{marker}')
 for relative, markers in {
  'cpf-admin/frontend/src/shared/cpfApi.ts': ('assertNoClientActor', 'admQuery', 'admMutation'),
  'cpf-admin/frontend/src/stores/admFeatureActionRegistry.ts': ('ownership collision', 'composeAdmFeatureActions'),
  'cpf-admin/frontend/src/features/core/methods.ts': ('admQuery', 'admMutation')
 }.items():
  path=root/relative
  for marker in markers:
   checks+=1
   if not path.is_file() or marker not in path.read_text(encoding='utf-8',errors='replace'):
    fail.append(f'frontend API/ownership boundary missing:{relative}:{marker}')
 bza=root/'cpf-biz-admin/frontend/src/features/auth/session.ts'
 checks+=1
 if bza.exists() and '../../shared/orval-mutator' not in bza.read_text(encoding='utf-8'): fail.append('BZA session is not routed through Orval mutator')
 for relative,marker in {
  'cpf-admin/frontend/src/stores/admConsoleStore.ts':'useAdmSessionStore',
  'cpf-admin/frontend/src/features/core/methods.ts':'useAdmInitializationStore',
  'cpf-admin/frontend/src/stores/admSessionStore.ts':'defineStore("adm-session"',
  'cpf-admin/frontend/src/stores/admInitializationStore.ts':'defineStore("adm-initialization"',
  'cpf-admin/frontend/src/stores/admFeatureStores.test.ts':'distinguishes required failure'
 }.items():
  path=root/relative;checks+=1
  if not path.is_file() or marker not in path.read_text(encoding='utf-8',errors='replace'): fail.append(f'ADM feature-store boundary missing:{relative}:{marker}')
 for base in FRONTENDS:
  for relative in ('orval.config.ts','scripts/write-generated-marker.mjs','scripts/verify-generated-client.mjs','src/generated/cpf-api.ts','src/generated/.cpf-openapi-source.json','src/generated/source-sha.json'):
   path=root/base/relative;checks+=1
   if not path.is_file(): fail.append(f'generated client pipeline missing:{base}/{relative}')
  package=root/base/'package.json';checks+=1
  if package.is_file():
   try:
    scripts=json.loads(package.read_text(encoding='utf-8')).get('scripts',{})
    if 'orval --config orval.config.ts' not in scripts.get('generate:api',''): fail.append(f'Orval generation script missing:{base}')
    if 'verify:lock' not in scripts.get('verify',''): fail.append(f'frontend verify does not enforce lock:{base}')
   except Exception as e: fail.append(f'frontend package parse failed:{base}:{e}')
  else: fail.append(f'frontend package missing:{base}')
 adm_generated=root/'cpf-admin/frontend/src/generated/cpf-api.ts';checks+=1
 if adm_generated.is_file():
  text=adm_generated.read_text(encoding='utf-8')
  for operation in ('getAdmAuthMe','getAdmAuthSession'):
   if operation not in text: fail.append(f'ADM generated operation missing:{operation}')
 bza_generated=root/'cpf-biz-admin/frontend/src/generated/cpf-api.ts';checks+=1
 if bza_generated.is_file():
  text=bza_generated.read_text(encoding='utf-8')
  for operation in ('getBzaAuthMe','postBzaAuthLogin','postBzaAuthLogout','postBzaPasswordChange'):
   if operation not in text: fail.append(f'BZA generated operation missing:{operation}')
 for relative,operation in {
  'cpf-admin/frontend/src/features/core/methods.ts':'getAdmAuthMe',
  'cpf-admin/frontend/src/stores/admConsoleStore.ts':'getAdmAuthSession',
  'cpf-biz-admin/frontend/src/features/auth/session.ts':'postBzaAuthLogin'
 }.items():
  path=root/relative;checks+=1
  if not path.is_file() or operation not in path.read_text(encoding='utf-8',errors='replace'):
   fail.append(f'generated client consumer missing:{relative}:{operation}')

 for base in FRONTENDS:
  route_test=root/base/'e2e/route-quality.spec.ts';checks+=1
  if not route_test.is_file(): fail.append(f'route quality E2E missing:{base}')
  else:
   route_text=route_test.read_text(encoding='utf-8')
   for marker in ('applicationRoutes','aria-sort','setViewportSize','QA33_FORCED_FAILURE','clearCookies'):
    checks+=1
    if marker not in route_text: fail.append(f'route quality marker missing:{base}:{marker}')
 if not overlay:
  for base in FRONTENDS:
   package=root/base/'package.json';lock=root/base/'package-lock.json';checks+=1
   if not package.is_file() or not lock.is_file(): fail.append(f'frontend manifest/lock missing:{base}');continue
   try:
    pkg=json.loads(package.read_text(encoding='utf-8'));lk=json.loads(lock.read_text(encoding='utf-8'))
    root_pkg=lk.get('packages',{}).get('',{})
    for section in ('dependencies','devDependencies'):
     expected=pkg.get(section,{}) or {};actual=root_pkg.get(section,{}) or {}
     if expected!=actual: fail.append(f'lock root mismatch:{base}:{section}')
     for name in expected:
      if f'node_modules/{name}' not in lk.get('packages',{}): fail.append(f'lock package entry missing:{base}:{name}')
   except Exception as e: fail.append(f'lock parse failed:{base}:{e}')
   generated=root/base/'src/generated/source-sha.json';checks+=1
   if not generated.is_file():
    fail.append(f'generated client source SHA marker missing:{base}')
   else:
    try:
     marker=json.loads(generated.read_text(encoding='utf-8'))
     head=''
     import subprocess
     resolved=subprocess.run(['git','rev-parse','HEAD'],cwd=root,capture_output=True,text=True,encoding='utf-8',errors='replace')
     if resolved.returncode==0: head=resolved.stdout.strip()
     if head and marker.get('sourceSha')!=head: fail.append(f'generated client source SHA mismatch:{base}')
     for field in ('openApiSha256','generatedClientSha256'):
      if not re.fullmatch(r'[0-9a-f]{64}',str(marker.get(field,''))): fail.append(f'generated client hash invalid:{base}:{field}')
     if marker.get('sanitized') is not True: fail.append(f'generated client marker not sanitized:{base}')
    except Exception as e: fail.append(f'generated client marker invalid:{base}:{e}')
   config=root/base/'playwright.config.ts';checks+=1
   if not config.is_file(): fail.append(f'playwright config missing:{base}')
   else:
    config_text=config.read_text(encoding='utf-8',errors='replace')
    for browser in ('chromium','firefox','webkit'):
     if browser not in config_text: fail.append(f'playwright browser project missing:{base}:{browser}')
 return {'schemaVersion':1,'overlayMode':overlay,'checks':checks,'failures':sorted(set(fail)),'status':'PASS' if not fail else 'FAIL'}

def self_test()->int:
 with tempfile.TemporaryDirectory(prefix='cpf-qa33-frontend-') as d:
  r=Path(d);p=r/'cpf-admin/frontend/src/features/x.ts';p.parent.mkdir(parents=True);p.write_text('fetch("/x")\n',encoding='utf-8')
  report=verify(r,True)
  if not any('raw fetch' in x for x in report['failures']): print(json.dumps(report,ensure_ascii=False));return 1
 print('[CPF][QA33][PASS] frontend closure negative self-test');return 0

def main()->int:
 ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--overlay',action='store_true');ap.add_argument('--json-report');ap.add_argument('--self-test',action='store_true');a=ap.parse_args()
 if a.self_test:return self_test()
 root=Path(a.root).resolve();report=verify(root,a.overlay)
 if a.json_report:
  out=Path(a.json_report);out=out if out.is_absolute() else root/out;out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
 print(json.dumps(report,ensure_ascii=False,indent=2));return 0 if report['status']=='PASS' else 1
if __name__=='__main__':raise SystemExit(main())
