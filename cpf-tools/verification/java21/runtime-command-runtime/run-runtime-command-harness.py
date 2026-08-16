#!/usr/bin/env python3
from __future__ import annotations
import argparse,shutil,subprocess
from pathlib import Path

def main()->int:
 ap=argparse.ArgumentParser();ap.add_argument('--root',required=True);a=ap.parse_args()
 root=Path(a.root).resolve();base=root/'cpf-tools/verification/java21/runtime-command-runtime';out=base/'build/classes'
 shutil.rmtree(base/'build',ignore_errors=True);out.mkdir(parents=True)
 sources=[
  root/'cpf-batch/control-plane/src/main/java/com/cpf/batch/control/RuntimeCommandExecutionException.java',
  root/'cpf-batch/control-plane/src/main/java/com/cpf/batch/control/RuntimeCommandExecutor.java',
  *sorted((base/'stubs').rglob('*.java')),*sorted((base/'src').rglob('*.java'))]
 missing=[str(p) for p in sources if not p.is_file()]
 if missing:raise SystemExit('missing sources: '+', '.join(missing))
 cc=['javac','-encoding','UTF-8','-source','21','-target','21','-d',str(out),*map(str,sources)]
 print('COMPILE',' '.join(cc));subprocess.run(cc,check=True)
 rc=['java','-cp',str(out),'com.cpf.batch.control.RuntimeCommandExecutorHarness']
 print('RUN',' '.join(rc));subprocess.run(rc,check=True);return 0
if __name__=='__main__':raise SystemExit(main())
