#!/usr/bin/env python3
from pathlib import Path
import shutil,subprocess,tempfile,sys
root=Path(__file__).resolve().parents[4];base=Path(__file__).resolve().parent
with tempfile.TemporaryDirectory(prefix='cpf-network-java21-') as td:
 t=Path(td);src=t/'src';classes=t/'classes';shutil.copytree(base/'stubs',src);classes.mkdir()
 paths=[root/'cpf-starters/integration/http-client/src/main/java/com/cpf/core/common/http/CpfServiceEndpointRegistry.java',root/'cpf-starters/integration/http-client/src/main/java/com/cpf/core/common/http/CpfPinnedHttpConnectorFactory.java',root/'cpf-core/src/main/java/com/cpf/core/api/security/network/CpfNetworkEndpointPolicy.java']
 for p in paths:
  if not p.is_file():raise SystemExit(f'missing actual source: {p}')
  marker='src/main/java/';rel=Path(str(p).split(marker,1)[1]);q=src/rel;q.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(p,q)
 ss=sorted(str(p) for p in src.rglob('*.java'));c=subprocess.run(['javac','--release','21','-encoding','UTF-8','-d',str(classes),*ss],text=True,capture_output=True)
 if c.returncode:print(c.stdout+c.stderr);raise SystemExit(c.returncode)
 r=subprocess.run(['java','-cp',str(classes),'com.cpf.core.common.http.NetworkRegistryHarness'],text=True,capture_output=True);print(r.stdout,end='');print(r.stderr,end='',file=sys.stderr);raise SystemExit(r.returncode)
