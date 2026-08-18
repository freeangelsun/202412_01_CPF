#!/usr/bin/env python3
from pathlib import Path
import shutil,subprocess,tempfile,sys,json
root=Path(__file__).resolve().parents[4]
base=Path(__file__).resolve().parent
with tempfile.TemporaryDirectory(prefix='cpf-controller-java21-') as td:
 t=Path(td);src=t/'src';classes=t/'classes';shutil.copytree(base/'stubs',src);classes.mkdir()
 actual=[root/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java',root/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchControlClientException.java',root/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeCommandRequest.java',root/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeDeploymentPlanRequest.java']
 for p in actual:
  if not p.is_file():raise SystemExit(f'missing actual source: {p}')
  q=src/p.relative_to(root/'cpf-admin/src/main/java');q.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(p,q)
 sources=sorted(str(p) for p in src.rglob('*.java'))
 c=subprocess.run(['javac','--release','21','-encoding','UTF-8','-d',str(classes),*sources],text=True,capture_output=True)
 if c.returncode:print(c.stdout+c.stderr);raise SystemExit(c.returncode)
 r=subprocess.run(['java','-cp',str(classes),'com.cpf.admin.opr.batch.runtime.BatchRuntimeControllerHarness'],text=True,capture_output=True)
 print(r.stdout,end='');print(r.stderr,end='',file=sys.stderr);raise SystemExit(r.returncode)
