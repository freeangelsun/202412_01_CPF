#!/usr/bin/env python3
import subprocess,sys
from pathlib import Path
H=Path(__file__).resolve().parents[1]
V=H/'validators'
def run(args):
    print('RUN>',' '.join(map(str,args)))
    r=subprocess.run(args,cwd=H.parents[2]);
    if r.returncode: raise SystemExit(r.returncode)
def main():
    if len(sys.argv)<2:
        print('ALL_GATES=FAIL final acceptance manifest path required'); return 2
    manifest=Path(sys.argv[1]).resolve()
    readme=(H.parents[2]/'README.md')
    for script,args in [
        ('validate_harness.py',[]),('validate_quality_fixtures.py',[]),('validate_readme.py',[str(readme)]),('validate_docx_artifacts.py',[]),('validate_reader_task_coverage.py',[])]:
        run([sys.executable,str(V/script),*args])
    run([sys.executable,str(V/'validate_final_acceptance.py'),str(manifest)])
    print('ALL_GATES=PASS')
    return 0
if __name__=='__main__': sys.exit(main())
