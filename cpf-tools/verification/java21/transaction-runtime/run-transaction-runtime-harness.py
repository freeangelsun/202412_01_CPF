#!/usr/bin/env python3
from pathlib import Path
import shutil,subprocess,tempfile,sys
root=Path(__file__).resolve().parents[4]
base=Path(__file__).resolve().parent
actual=[
 root/'cpf-core/src/main/java/com/cpf/core/api/transaction/CpfTransactionIds.java',
 root/'cpf-starters/web/src/main/java/com/cpf/web/api/CpfHeaders.java',
 root/'cpf-starters/web/src/main/java/com/cpf/web/context/CpfHttpHeaderNames.java',
 root/'cpf-starters/web/src/main/java/com/cpf/web/context/CpfHttpHeaderCatalog.java',
 root/'cpf-starters/web/src/main/java/com/cpf/web/context/CpfHttpHeaderSpec.java',
 root/'cpf-starters/web/src/main/java/com/cpf/web/context/CpfHeaderCompatibility.java',
 root/'cpf-starters/web/src/main/java/com/cpf/web/context/CpfHeaderDirection.java',
 root/'cpf-starters/web/src/main/java/com/cpf/web/context/CpfHeaderLogPolicy.java',
 root/'cpf-starters/web/src/main/java/com/cpf/web/context/CpfHeaderMutationPolicy.java',
 root/'cpf-starters/web/src/main/java/com/cpf/web/context/CpfHeaderPropagationScope.java',
 root/'cpf-starters/web/src/main/java/com/cpf/web/context/CpfHeaderTrustLevel.java',
 root/'cpf-starters/web/src/main/java/com/cpf/web/context/CpfHeaderValidationException.java',
]
with tempfile.TemporaryDirectory(prefix='cpf-transaction-java21-') as td:
 t=Path(td);src=t/'src';classes=t/'classes';shutil.copytree(base/'stubs-current',src);classes.mkdir()
 for p in actual:
  if not p.is_file(): raise SystemExit(f'missing actual source: {p}')
  rel=Path(str(p).split('src/main/java/',1)[1]);q=src/rel;q.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(p,q)
 sources=sorted(str(p) for p in src.rglob('*.java'))
 c=subprocess.run(['javac','--release','21','-encoding','UTF-8','-d',str(classes),*sources],text=True,capture_output=True)
 if c.returncode: print(c.stdout+c.stderr);raise SystemExit(c.returncode)
 r=subprocess.run(['java','-cp',str(classes),'com.cpf.web.api.TransactionHeaderHarness'],text=True,capture_output=True)
 print(r.stdout,end='');print(r.stderr,end='',file=sys.stderr);raise SystemExit(r.returncode)
