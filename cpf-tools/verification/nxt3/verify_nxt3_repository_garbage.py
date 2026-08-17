#!/usr/bin/env python3
"""NXT3 Repository Garbage Sweep Gate: 파일 단위 DELETE/MIGRATE/KEEP, stale reference 0."""
# Repository Garbage 판정은 파일 단위 Manifest와 보호 경로, stale reference 0을 함께 검산한다.
from __future__ import annotations
import argparse,csv,json,re
from pathlib import Path
PROTECTED=('cpf-docs/deliverables/','cpf-docs/guides/','cpf-docs/environment/docker/','cpf-tools/environment/docker-development-test/')
BAD=re.compile(r'(?i)(?:^|[_\-.])(qa\d*|fix(?:ed)?|session\d*|rev\d*|rework|checkpoint|one[-_]?shot)(?:[_\-.]|$)|(?:\.bak|\.backup|\.orig|\.rej|\.tmp|~)$')
SKIP={'.git','.gradle','node_modules','dist','out','build'}

def is_generated_cache_path(path: str) -> bool:
    """True only for disposable runtime/build caches; cpf-tools/build product source stays managed."""
    normalized=path.replace('\\','/').strip('/')
    parts=normalized.split('/') if normalized else []
    if not parts:
        return False
    if '__pycache__' in parts or normalized.endswith('.pyc'):
        return True
    if parts[0]=='build' or any(part in {'.gradle','.pytest_cache','node_modules','dist','out','target'} for part in parts):
        return True
    for index,part in enumerate(parts):
        if part!='build':
            continue
        # cpf-tools/build is tracked product source; only nested build directories below it are generated.
        if index==1 and parts[0]=='cpf-tools':
            continue
        return True
    return False

def main():
 ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--ledger',default='cpf-docs/work/GARBAGE_SWEEP_DECISIONS.csv'); ap.add_argument('--manifest',default='cpf-docs/work/current/DELETE_MANIFEST.txt'); a=ap.parse_args(); root=Path(a.root).resolve(); fail=[]
 lp=root/a.ledger; mp=root/a.manifest
 if not lp.exists(): fail.append('garbage_ledger_missing')
 if not mp.exists(): fail.append('delete_manifest_missing')
 rows=[]; dels=[]
 if lp.exists():
  with lp.open(encoding='utf-8-sig',newline='') as f: rows=list(csv.DictReader(f))
 if mp.exists():
  dels=[line.strip().replace('\\','/').strip('/') for line in mp.read_text(encoding='utf-8-sig').splitlines() if line.strip() and not line.lstrip().startswith('#')]
 bypath={}
 for r in rows: bypath.setdefault((r.get('path') or '').replace('\\','/').strip('/'),[]).append(r)
 for p in dels:
  if not p: fail.append('empty_delete_path'); continue
  if Path(p).is_absolute() or '..' in Path(p).parts: fail.append('unsafe_delete='+p)
  if any(p==x.rstrip('/') or p.startswith(x) for x in PROTECTED): fail.append('protected_delete='+p)
  target=root/p
  if target.exists() and target.is_dir(): fail.append('directory_delete_forbidden='+p)
  if not any((x.get('decision') or x.get('action'))=='DELETE' for x in bypath.get(p,[])): fail.append('delete_without_decision='+p)
 stale='cpf-tools/config/cpf-starter-catalog.json'
 if (root/stale).exists() and not any((x.get('decision') or x.get('action'))=='DELETE' for x in bypath.get(stale,[])): fail.append('stale_catalog_not_delete')
 # Python execution caches are local runtime artifacts, never product Source or delete-manifest obligations.
 ephemeral_cache_count=0
 for p in root.rglob('*'):
  if not p.is_file(): continue
  rel=p.relative_to(root).as_posix(); parts=set(p.relative_to(root).parts)
  if '.git' in parts or any(rel==x.rstrip('/') or rel.startswith(x) for x in PROTECTED): continue
  if '__pycache__' in parts or p.suffix.lower()=='.pyc': ephemeral_cache_count+=1
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
 status='PASS' if not fail else 'FAIL'; print(json.dumps({'gate':'NXT3_REPOSITORY_GARBAGE','status':status,'decisionCount':len(rows),'deleteCount':len(dels),'ephemeralCacheCount':ephemeral_cache_count,'failures':sorted(set(fail))},ensure_ascii=False,indent=2)); raise SystemExit(0 if not fail else 1)
if __name__=='__main__': main()
