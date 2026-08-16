#!/usr/bin/env python3
"""NXT3 Repository Garbage Sweep Gate: 파일 단위 DELETE/MIGRATE/KEEP, stale reference 0."""
# Repository Garbage 판정은 파일 단위 Manifest와 보호 경로, stale reference 0을 함께 검산한다.
from __future__ import annotations
import argparse,csv,json,re
from pathlib import Path
PROTECTED=('cpf-docs/deliverables/','cpf-docs/guides/','cpf-docs/environment/docker/','cpf-tools/environment/docker-development-test/')
BAD=re.compile(r'(?i)(?:^|[_\-.])(qa\d*|fix(?:ed)?|session\d*|rev\d*|rework|checkpoint|one[-_]?shot)(?:[_\-.]|$)|(?:\.bak|\.backup|\.orig|\.rej|\.tmp|~)$')

GENERATED_CACHE_DIRS={'.gradle','.pytest_cache','node_modules','dist','out'}
def is_generated_cache_path(rel:str)->bool:
 parts=Path(rel).parts
 if any(part in GENERATED_CACHE_DIRS for part in parts): return True
 for idx,part in enumerate(parts):
  if part=='build':
   # cpf-tools/build/** is product Source; nested build directories below it are generated output.
   if idx==1 and parts[0]=='cpf-tools': continue
   return True
 return False

def main():
 ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--ledger',default='cpf-docs/work/GARBAGE_SWEEP_DECISIONS.csv'); ap.add_argument('--manifest',default='cpf-docs/work/CPF_DELETE_MANIFEST.csv'); a=ap.parse_args(); root=Path(a.root).resolve(); fail=[]
 lp=root/a.ledger; mp=root/a.manifest
 if not lp.exists(): fail.append('garbage_ledger_missing')
 if not mp.exists(): fail.append('delete_manifest_missing')
 rows=[]; dels=[]
 if lp.exists():
  with lp.open(encoding='utf-8-sig',newline='') as f: rows=list(csv.DictReader(f))
 if mp.exists():
  with mp.open(encoding='utf-8-sig',newline='') as f: dels=list(csv.DictReader(f))
 bypath={}
 for r in rows: bypath.setdefault((r.get('path') or '').replace('\\','/').strip('/'),[]).append(r)
 for r in dels:
  p=(r.get('path') or '').replace('\\','/').strip('/')
  if not p: fail.append('empty_delete_path'); continue
  if Path(p).is_absolute() or '..' in Path(p).parts: fail.append('unsafe_delete='+p)
  if any(p==x.rstrip('/') or p.startswith(x) for x in PROTECTED): fail.append('protected_delete='+p)
  target=root/p
  if target.exists() and target.is_dir(): fail.append('directory_delete_forbidden='+p)
  if r.get('status')!='READY_TO_DELETE': fail.append('delete_not_ready='+p)
  if not any((x.get('decision') or x.get('action'))=='DELETE' for x in bypath.get(p,[])): fail.append('delete_without_decision='+p)
 stale='cpf-tools/config/cpf-starter-catalog.json'
 if (root/stale).exists() and not any((x.get('decision') or x.get('action'))=='DELETE' for x in bypath.get(stale,[])): fail.append('stale_catalog_not_delete')
 # Generated caches are never allowed as deliverable Source.
 for p in root.rglob('*'):
  if not p.is_file(): continue
  rel=p.relative_to(root).as_posix(); parts=set(p.relative_to(root).parts)
  if '.git' in parts or is_generated_cache_path(rel) or any(rel==x.rstrip('/') or rel.startswith(x) for x in PROTECTED): continue
  if '__pycache__' in parts or p.suffix.lower()=='.pyc':
   if not any((x.get('decision') or x.get('action'))=='DELETE' for x in bypath.get(rel,[])): fail.append('generated_garbage_undecided='+rel)
 # old catalog active path must be 0 outside ledgers/evidence/verification/legacy source itself.
 old='cpf-tools/config/cpf-starter-catalog.json'; hits=[]
 for p in root.rglob('*'):
  if not p.is_file() or p.suffix.lower() not in {'.java','.kt','.gradle','.kts','.json','.yaml','.yml','.xml','.properties','.md','.ps1','.sh','.py','.sql','.csv','.txt'}: continue
  rel=p.relative_to(root).as_posix()
  if rel==old or rel.startswith(('cpf-docs/work/','cpf-tools/verification/nxt3/','.git/')) or '/build/' in '/'+rel: continue
  if any(rel.startswith(x) for x in PROTECTED): continue
  try: text=p.read_text(encoding='utf-8',errors='ignore')
  except Exception: continue
  if old in text: hits.append(rel)
 if hits: fail.append('stale_catalog_reference='+','.join(hits))
 status='PASS' if not fail else 'FAIL'; print(json.dumps({'gate':'NXT3_REPOSITORY_GARBAGE','status':status,'decisionCount':len(rows),'deleteCount':len(dels),'failures':sorted(set(fail))},ensure_ascii=False,indent=2)); raise SystemExit(0 if not fail else 1)
if __name__=='__main__': main()
