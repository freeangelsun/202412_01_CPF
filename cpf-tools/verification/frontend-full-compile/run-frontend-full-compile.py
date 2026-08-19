#!/usr/bin/env python3
from __future__ import annotations
import argparse, pathlib, shutil, subprocess, sys

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", required=True)
    parser.add_argument("--tsc", default="tsc")
    args = parser.parse_args()
    root = pathlib.Path(args.root).resolve()
    config = root / "cpf-tools/verification/frontend-full-compile/tsconfig.json"
    if not config.is_file():
        print(f"CPF_FRONTEND_FULL_COMPILE_FAIL missing={config}", file=sys.stderr)
        return 2
    tsc = shutil.which(args.tsc)
    if not tsc:
        print(f"CPF_FRONTEND_FULL_COMPILE_FAIL tsc_not_found={args.tsc}", file=sys.stderr)
        return 2
    proc = subprocess.run([tsc, "--pretty", "false", "--project", str(config)], cwd=root, text=True)
    if proc.returncode:
        print(f"CPF_FRONTEND_FULL_COMPILE_FAIL exit={proc.returncode}", file=sys.stderr)
        return proc.returncode
    adm = sum(1 for _ in (root / "cpf-admin/frontend/src").rglob("*.ts"))
    bza = sum(1 for _ in (root / "cpf-biz-frontend/src").rglob("*.ts"))
    print(f"CPF_FRONTEND_FULL_COMPILE_PASS adm_ts={adm} bza_ts={bza}")
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
