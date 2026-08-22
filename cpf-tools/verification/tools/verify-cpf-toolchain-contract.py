#!/usr/bin/env python3
from __future__ import annotations
import argparse,json,re
from pathlib import Path

def main():
 ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--mutation-self-test',action='store_true'); a=ap.parse_args(); root=Path(a.root).resolve(); f=[]
 expected_node='>=22.18.0 <25'; expected_npm='10.9.2'; docker_node='22.18.0'
 for rel in ['cpf-admin/frontend/package.json','cpf-backoffice-web/frontend/package.json']:
  p=root/rel; data=json.loads(p.read_text(encoding='utf-8'))
  if (data.get('engines') or {}).get('node')!=expected_node: f.append(f'{rel}:node')
  if (data.get('engines') or {}).get('npm')!=expected_npm: f.append(f'{rel}:npm')
  if data.get('packageManager')!=f'npm@{expected_npm}': f.append(f'{rel}:packageManager')
 droot=root/'cpf-tools/environment/docker-development-test'; corpus='\n'.join(p.read_text(encoding='utf-8',errors='ignore') for p in droot.rglob('*') if p.is_file())
 if 'node:22.16.0' in corpus or 'node22.16.0' in corpus: f.append('docker-stale-node-22.16')
 if f'node:{docker_node}-bookworm' not in corpus: f.append('docker-node-22.18-image-missing')
 prereq=(root/'cpf-tools/verification/verify_runtime_prerequisites.py').read_text(encoding='utf-8',errors='ignore')
 for tok in ['(22,18,0)','(25,0,0)',"n!='10.9.2'"]:
  if tok not in prereq: f.append('runtime-prerequisite-missing:'+tok)
 mut=[]
 if a.mutation_self_test:
  mutated=corpus.replace('22.18.0','22.16.0',1)
  mut.append('PASS' if 'node:22.16.0' in mutated or 'node22.16.0' in mutated else 'FAIL')
  if mut!=['PASS']: f.append('mutation-self-test-failed')
 p={'status':'PASS' if not f else 'FAIL','node':expected_node,'npm':expected_npm,'dockerNode':docker_node,'findings':f,'mutation':mut}; print(json.dumps(p,ensure_ascii=False,indent=2)); return 0 if not f else 1
if __name__=='__main__': raise SystemExit(main())
