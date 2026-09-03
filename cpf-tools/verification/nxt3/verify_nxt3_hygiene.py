#!/usr/bin/env python3
"""NXT3 Hygiene Gate. 파일 단위 Delete Manifest와 Garbage Sweep 결정을 강제한다."""
from __future__ import annotations

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass
import argparse,csv,json
from pathlib import Path
PROTECTED=('cpf-docs/deliverables/','cpf-docs/guides/','cpf-docs/assets/manuals/','cpf-docs/assets/readme/','cpf-docs/environment/docker/','cpf-tools/environment/docker-development-test/')
PROTECTED_EXACT={'cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md'}
APPROVED_TOOLS={'build','contracts','db','environment','generator','governance','release','runtime','security','supply-chain','testing','verification'}
LEGACY_TOOLS={'config','performance','product-governance','promotion','runtime-alternatives','scripts','analysis'}

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

def active_tool_directories(tools: Path) -> set[str]:
    """Return product-owned cpf-tools roots, excluding disposable interpreter caches.

    A Python entrypoint can create ``cpf-tools/__pycache__`` before the gate
    starts.  The cache is already classified as generated above, so treating it
    as an unapproved product tool root would make the gate fail because the
    verifier itself ran.  Keep the root inventory on the same classification
    contract rather than adding a second allow-list exception.
    """
    return {
        path.name
        for path in tools.iterdir()
        if path.is_dir()
        and not is_generated_cache_path((Path('cpf-tools') / path.name).as_posix())
    }

def main():
 ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); a=ap.parse_args(); r=Path(a.root).resolve(); fail=[]
 mf=r/'cpf-docs/deliverables/DELETE_MANIFEST.csv'; gf=r/'cpf-docs/governance/development-harness/current/CURRENT_GARBAGE_DECISIONS.csv'
 rows=[]; grows=[]
 if not mf.exists(): fail.append('delete_manifest_missing')
 else:
  if mf.suffix.lower()=='.csv':
   with mf.open(encoding='utf-8-sig',newline='') as f: rows=[(row.get('path') or '').replace('\\','/').strip('/') for row in csv.DictReader(f) if (row.get('path') or '').strip()]
  else:
   rows=[line.strip().replace('\\','/').strip('/') for line in mf.read_text(encoding='utf-8-sig').splitlines() if line.strip() and not line.lstrip().startswith('#')]
 if not gf.exists(): fail.append('garbage_decisions_missing')
 else:
  with gf.open(encoding='utf-8-sig',newline='') as f: grows=list(csv.DictReader(f))
 seen=set(); decided={}
 for x in grows: decided.setdefault((x.get('path') or '').replace('\\','/').strip('/'),set()).add(x.get('decision') or x.get('action'))
 for path in rows:
  if not path: fail.append('delete_manifest_empty'); continue
  if path in seen: fail.append('delete_manifest_duplicate='+path)
  seen.add(path)
  if Path(path).is_absolute() or '..' in Path(path).parts: fail.append('unsafe_delete='+path)
  target=r/path
  if (any(path==x.rstrip('/') or path.startswith(x) for x in PROTECTED) or path in PROTECTED_EXACT) and target.exists(): fail.append('protected_delete='+path)
  if target.exists() and target.is_dir(): fail.append('directory_delete_forbidden='+path)
  if not ({'DELETE','DELETE_CANDIDATE'} & decided.get(path,set())): fail.append('delete_without_garbage_decision='+path)
 tools=r/'cpf-tools'
 if tools.exists():
  active=active_tool_directories(tools)
  unexpected=active-(APPROVED_TOOLS|LEGACY_TOOLS)
  if unexpected: fail.append('unapproved_tools_root='+','.join(sorted(unexpected)))
  # legacy root는 실제 삭제 전 존재 가능하나 모든 파일이 manifest돼야 한다.
  for name in sorted(active&LEGACY_TOOLS):
   missing=[p.relative_to(r).as_posix() for p in (tools/name).rglob('*') if p.is_file() and p.relative_to(r).as_posix() not in seen]
   if missing: fail.append('legacy_tool_unmanifested='+','.join(missing[:30]))
 # Python execution caches are local runtime artifacts. They are excluded from product/managed Source identity.
 ephemeral_cache_count=0
 for p in r.rglob('*'):
  if not p.is_file(): continue
  rel=p.relative_to(r).as_posix(); parts=set(p.relative_to(r).parts)
  if '.git' in parts or any(rel==x.rstrip('/') or rel.startswith(x) for x in PROTECTED): continue
  if '__pycache__' in parts or p.suffix.lower()=='.pyc': ephemeral_cache_count+=1
 # cpf-tools/build broad ignore 금지
 gi=r/'.gitignore'
 if gi.exists():
  text=gi.read_text(encoding='utf-8',errors='replace')
  for line in text.splitlines():
   s=line.strip()
   if s and not s.startswith(('#','!')) and s in {'cpf-tools/build/','/cpf-tools/build/','cpf-tools/build/**','/cpf-tools/build/**'}: fail.append('cpf_tools_build_ignored='+s)
 if fail:
  print('CPF_NXT3_HYGIENE=FAIL'); print('\n'.join(sorted(set(fail)))); raise SystemExit(1)
 print('CPF_NXT3_HYGIENE=PASS'); print(f'file_delete_manifest={len(seen)}'); print(f'garbage_decisions={len(grows)}'); print('protected_delete=0'); print('directory_delete=0')
if __name__=='__main__': main()
