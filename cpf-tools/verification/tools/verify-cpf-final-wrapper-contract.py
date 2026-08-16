#!/usr/bin/env python3
"""Static fail-closed verifier for the exact-SHA wrappers and their dependencies."""
from __future__ import annotations
import argparse,re,sys
from pathlib import Path
SUPPORTED={'.ps1','.py','.mjs','.js'}
QUOTED_PATH=re.compile(r"['\"](cpf-[^'\"]+\.(?:ps1|py|mjs|js))['\"]")

def fail(msg): print('[FAIL]',msg,file=sys.stderr); raise SystemExit(1)
def main():
 p=argparse.ArgumentParser();p.add_argument('--root',default='.');a=p.parse_args();root=Path(a.root).resolve()
 wrappers=[
  root/'cpf-tools/verification/tools/verify-cpf-final-readonly.ps1',
  root/'cpf-tools/verification/tools/verify-cpf-fresh-clone-final.ps1',
  root/'cpf-tools/db/verification/verify-cpf-approved-baseline-three-db.ps1',
  root/'cpf-tools/contracts/openapi/sync-cpf-runtime-openapi-generated-client.ps1',
 ]
 for wrapper in wrappers:
  if not wrapper.is_file(): fail(f'wrapper missing: {wrapper.relative_to(root)}')
  text=wrapper.read_text(encoding='utf-8-sig')
  if 'Set-StrictMode -Version Latest' not in text or "$ErrorActionPreference" not in text: fail(f'fail-closed preamble missing: {wrapper.name}')
  if wrapper.suffix.lower() not in SUPPORTED: fail(f'unsupported wrapper extension: {wrapper.name}')
  for rel in QUOTED_PATH.findall(text):
   dependency=root/rel
   if not dependency.is_file(): fail(f'{wrapper.name} calls missing script: {rel}')
  if wrapper.name in {'verify-cpf-final-readonly.ps1','verify-cpf-fresh-clone-final.ps1'}:
   for token in ['status --porcelain=v1 --untracked-files=all','rev-parse HEAD']:
    if token not in text: fail(f'{wrapper.name} missing read-only identity guard: {token}')
 readonly=wrappers[0].read_text(encoding='utf-8-sig')
 for token in ["'.ps1'", "'.py'", "'.mjs'", "'.js'", 'Assert-GateInventory']:
  if token not in readonly: fail(f'extension-aware wrapper token missing: {token}')
 print(f'[PASS] final wrapper contract wrappers={len(wrappers)} dependencyCalls=validated')
if __name__=='__main__':main()
