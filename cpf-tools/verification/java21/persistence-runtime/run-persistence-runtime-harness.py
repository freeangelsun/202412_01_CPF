#!/usr/bin/env python3
from pathlib import Path
import shutil,subprocess,tempfile,sys
root=Path(__file__).resolve().parents[4];base=Path(__file__).resolve().parent
with tempfile.TemporaryDirectory(prefix='cpf-persistence-java21-') as td:
 t=Path(td);src=t/'src';classes=t/'classes';shutil.copytree(base/'stubs',src);classes.mkdir()
 for n in ('CpfConnectionPoolRuntimeApplier.java','CpfDbReadRoutingRuntimeApplier.java'):
  p=root/'cpf-starters/data/persistence-jdbc/src/main/java/com/cpf/starter/data/persistence/jdbc/runtimecontrol'/n
  if not p.is_file():raise SystemExit(f'missing actual source: {p}')
  rel=Path(str(p).split('src/main/java/',1)[1]);q=src/rel;q.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(p,q)
 ss=sorted(str(p) for p in src.rglob('*.java'));c=subprocess.run(['javac','--release','21','-encoding','UTF-8','-d',str(classes),*ss],text=True,capture_output=True)
 if c.returncode:print(c.stdout+c.stderr);raise SystemExit(c.returncode)
 r=subprocess.run(['java','-cp',str(classes),'com.cpf.starter.data.persistence.jdbc.runtimecontrol.PersistenceRuntimeHarness'],text=True,capture_output=True);print(r.stdout,end='');print(r.stderr,end='',file=sys.stderr);raise SystemExit(r.returncode)
