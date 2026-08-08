#!/usr/bin/env python3
"""Compile and run the CENTRAL-FINAL-035 JDBC runtime harness against Oracle/PostgreSQL/MariaDB."""
from __future__ import annotations
import argparse, os, pathlib, shutil, subprocess, sys, tempfile

def main() -> int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--root', required=True)
    ap.add_argument('--driver-jar', required=True, help='JDBC driver jar for the selected official vendor')
    ap.add_argument('--db-url', required=True); ap.add_argument('--db-user', required=True); ap.add_argument('--db-password', required=True)
    ap.add_argument('--table', default='cpf_ref_online_abcd')
    a=ap.parse_args(); root=pathlib.Path(a.root).resolve(); driver=pathlib.Path(a.driver_jar).resolve()
    if not driver.is_file(): print('FAIL JDBC driver jar missing',file=sys.stderr); return 2
    javac=shutil.which('javac'); java=shutil.which('java')
    if not javac or not java: print('UNVERIFIED javac/java unavailable',file=sys.stderr); return 2
    src=[root/'cpf-reference/src/main/java/com/cpf/reference/online/integrated/OnlineAbcdReferenceFlow.java',
         root/'cpf-reference/src/main/java/com/cpf/reference/online/integrated/OnlineAbcdJdbcRuntimeHarness.java']
    if any(not p.is_file() for p in src): print('FAIL online JDBC harness source missing',file=sys.stderr); return 1
    with tempfile.TemporaryDirectory(prefix='cpf-online-jdbc-') as td:
        out=pathlib.Path(td)
        cp=subprocess.run([javac,'-encoding','UTF-8','-d',str(out),*map(str,src)],text=True,capture_output=True)
        if cp.returncode: print(cp.stdout+cp.stderr,file=sys.stderr); return cp.returncode
        env=os.environ.copy(); env.update({'CPF_REF_DB_URL':a.db_url,'CPF_REF_DB_USER':a.db_user,'CPF_REF_DB_PASSWORD':a.db_password,'CPF_REF_ONLINE_TABLE':a.table})
        sep=';' if os.name=='nt' else ':'
        run=subprocess.run([java,'-ea','-cp',str(out)+sep+str(driver),'com.cpf.reference.online.integrated.OnlineAbcdJdbcRuntimeHarness'],env=env,text=True,capture_output=True)
        sys.stdout.write(run.stdout); sys.stderr.write(run.stderr); return run.returncode
if __name__=='__main__': raise SystemExit(main())
