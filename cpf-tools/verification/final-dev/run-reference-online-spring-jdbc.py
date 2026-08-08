#!/usr/bin/env python3
"""Run the cpf-reference Spring REQUIRED + live DB3 Online A->B->C/D harness."""
from pathlib import Path
import argparse, os, subprocess, sys

def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--root',type=Path,default=Path('.')); ap.add_argument('--vendor',choices=['oracle','postgresql','mariadb'],required=True); args=ap.parse_args()
    root=args.root.resolve()
    required=['CPF_REF_DB_URL','CPF_REF_DB_USER','CPF_REF_DB_PASSWORD']
    missing=[x for x in required if not os.environ.get(x)]
    if missing:
        print('UNVERIFIED runtime environment missing: '+','.join(missing)); return 3
    gradle=root/('gradlew.bat' if os.name=='nt' else 'gradlew')
    if not gradle.exists(): print('FAIL gradle wrapper missing'); return 2
    cmd=[str(gradle),':cpf-reference:runOnlineAbcdSpringJdbcHarness',f'-PcpfDbVendor={args.vendor}','--no-daemon','--stacktrace']
    p=subprocess.run(cmd,cwd=root,env=os.environ.copy())
    return p.returncode
if __name__=='__main__': raise SystemExit(main())
