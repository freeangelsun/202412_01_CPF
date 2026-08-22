#!/usr/bin/env python3
from __future__ import annotations
import argparse,json,re,shutil,subprocess,tempfile
from pathlib import Path
FORBIDDEN=['localhost','127.0.0.1','::1','unknown','local','dev','test','prod']
def main():
 ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); a=ap.parse_args(); root=Path(a.root).resolve(); src=root/'cpf-starters/base/runtime/src/main/java/com/cpf/foundation/runtime/CpfInstanceIdentity.java'; f=[]
 text=src.read_text(encoding='utf-8')
 for x in FORBIDDEN:
  if f'"{x}"' not in text: f.append('forbidden-missing:'+x)
 java=shutil.which('java'); javac=shutil.which('javac'); checks=[]
 if java and javac:
  with tempfile.TemporaryDirectory(prefix='cpf-instance-id-') as td:
   t=Path(td); pkg=t/'com/cpf/foundation/runtime'; pkg.mkdir(parents=True); shutil.copy2(src,pkg/src.name)
   h=pkg/'CpfInstanceIdentityHarness.java'; h.write_text('''package com.cpf.foundation.runtime; public final class CpfInstanceIdentityHarness { public static void main(String[] a){ int ok=0; for(String x:a){ try{CpfInstanceIdentity.resolveInstanceId(x,"host-valid"); System.out.println("ACCEPT:"+x);}catch(IllegalStateException e){ok++;}} if(ok!=a.length) throw new IllegalStateException("forbidden accepted "+(a.length-ok)); if(!"MBR01".equals(CpfInstanceIdentity.resolveInstanceId("MBR01","host-valid"))) throw new IllegalStateException("explicit precedence"); if(!"host-valid".equals(CpfInstanceIdentity.resolveInstanceId(null,"host-valid"))) throw new IllegalStateException("hostname fallback"); System.out.println("PASS checks="+(ok+2)); }}''',encoding='utf-8')
   cp=subprocess.run([javac,'-d',str(t),str(pkg/src.name),str(h)],text=True,capture_output=True)
   if cp.returncode: f.append('javac:'+cp.stderr[-1000:])
   else:
    rp=subprocess.run([java,'-cp',str(t),'com.cpf.foundation.runtime.CpfInstanceIdentityHarness',*FORBIDDEN],text=True,capture_output=True); checks.append(rp.stdout.strip())
    if rp.returncode: f.append('runtime:'+rp.stdout+rp.stderr)
 else: f.append('java-toolchain-not-available')
 p={'status':'PASS' if not f else 'FAIL','checks':checks,'findings':f}; print(json.dumps(p,ensure_ascii=False,indent=2)); return 0 if not f else 1
if __name__=='__main__': raise SystemExit(main())
