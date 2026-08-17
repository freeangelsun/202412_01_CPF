#!/usr/bin/env python3
from pathlib import Path
import shutil,subprocess,tempfile,sys,os,json
root=Path(__file__).resolve().parents[3];base=Path(__file__).resolve().parent
with tempfile.TemporaryDirectory(prefix='cpf-frontend-runtime-') as td:
 t=Path(td);mods=[]
 for surface,stub in [('adm','adm-stubs'),('bza','bza-stubs')]:
  d=t/surface;src=d/'src';shutil.copytree(base/stub,src);shutil.copy2(base/f'{surface}-tsconfig.json',d/'tsconfig.json')
  actual=root/('cpf-admin' if surface=='adm' else 'cpf-biz-admin')/'frontend/src/shared/cpfApi.ts'
  if not actual.is_file():raise SystemExit(f'missing actual source: {actual}')
  (src/'shared').mkdir(parents=True,exist_ok=True);shutil.copy2(actual,src/'shared/cpfApi.ts')
  client_headers=root/('cpf-admin' if surface=='adm' else 'cpf-biz-admin')/'frontend/src/shared/clientHeaders.ts'
  if not client_headers.is_file():raise SystemExit(f'missing actual client header source: {client_headers}')
  shutil.copy2(client_headers,src/'shared/clientHeaders.ts')
  mutator=root/('cpf-admin' if surface=='adm' else 'cpf-biz-admin')/'frontend/src/shared/orval-mutator.ts'
  if not mutator.is_file():raise SystemExit(f'missing actual mutator source: {mutator}')
  shutil.copy2(mutator,src/'shared/orval-mutator.ts')
  c=subprocess.run(['tsc','-p',str(d/'tsconfig.json')],text=True,capture_output=True)
  if c.returncode:print(c.stdout+c.stderr);raise SystemExit(c.returncode)
  mods.extend([str(d/'dist/shared/cpfApi.js'),str(d/'dist/shared/orval-mutator.js')])
 env=os.environ.copy();env['NODE_PATH']=str(t/'adm/dist')+os.pathsep+str(t/'bza/dist')
 r=subprocess.run(['node',str(base/'harness.cjs'),mods[0],mods[2],mods[1],mods[3]],env=env,text=True,capture_output=True);print(r.stdout,end='');print(r.stderr,end='',file=sys.stderr);raise SystemExit(r.returncode)
