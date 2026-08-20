#!/usr/bin/env python3
"""Legacy BZA verifier compatibility entrypoint. Delegates to the canonical Backoffice/MBW contract."""
from __future__ import annotations
import argparse, importlib.util, sys
from pathlib import Path

class ContractError(RuntimeError):
    pass

def _load_current(root: Path):
    script=root/'cpf-tools/verification/tools/verify-cpf-backoffice-route-contract.py'
    spec=importlib.util.spec_from_file_location('cpf_backoffice_route_contract',script)
    if spec is None or spec.loader is None: raise ContractError(f'cannot load canonical Backoffice verifier: {script}')
    module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module);return module

def validate(root: Path):
    root=root.resolve()
    for retired in ('cpf-biz-admin','cpf-biz-channel','cpf-biz-frontend'):
        if (root/retired).exists(): raise ContractError(f'retired BZA product root remains: {retired}')
    module=_load_current(root)
    try: return module.validate(root)
    except module.ContractError as exc: raise ContractError(str(exc)) from exc

def main()->int:
    p=argparse.ArgumentParser(description='Compatibility delegation to canonical Backoffice/MBW route contract')
    p.add_argument('--root',type=Path,default=Path.cwd());a=p.parse_args();r=validate(a.root)
    print(f"BZA_COMPAT_DELEGATION=PASS canonical=BACKOFFICE/MBW state={r['state']} backendOperations={r['operations']} webRoutes={r['routes']} referenceRoutes={r['referenceRoutes']}")
    return 0

if __name__=='__main__':
    try: raise SystemExit(main())
    except ContractError as exc:
        print(f'BZA_COMPAT_DELEGATION=FAIL {exc}',file=sys.stderr);raise SystemExit(1)
