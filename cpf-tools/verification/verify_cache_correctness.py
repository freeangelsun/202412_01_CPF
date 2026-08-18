#!/usr/bin/env python3
import argparse, os, shutil, subprocess, tempfile
from pathlib import Path

def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.')
    root=Path(ap.parse_args().root).resolve(); javac=shutil.which('javac'); java=shutil.which('java')
    if not javac or not java:
        print('CPF_CACHE_CORRECTNESS=UNVERIFIED reason=java-not-found'); raise SystemExit(2)
    api=root/'cpf-starters/data/src/main/java/com/cpf/data/cache/api'
    common=root/'cpf-starters/data/cache/spring-data-redis/src/main/java/com/cpf/data/cache/rediscommon'
    sources=[str(p) for p in api.glob('*.java')]
    sources += [str(root/'cpf-starters/base/runtime/src/main/java/com/cpf/foundation/runtime/CpfInstanceIdentity.java')]
    sources += [str(common/'CpfCacheInvalidationProperties.java'),str(common/'CpfCacheInvalidationSubjectKey.java'),str(common/'CpfCacheInvalidationCoordinator.java')]
    sources += [str(root/'cpf-tools/verification/harness/cache/CpfCacheCorrectnessHarness.java')]
    with tempfile.TemporaryDirectory(prefix='cpf_cache_correctness_') as td:
        cp=subprocess.run([javac,'-encoding','UTF-8','-d',td,*sources],capture_output=True,text=True)
        if cp.returncode:
            print('CPF_CACHE_CORRECTNESS=FAIL stage=javac'); print(cp.stdout+cp.stderr); raise SystemExit(1)
        run=subprocess.run([java,'-cp',td,'CpfCacheCorrectnessHarness'],capture_output=True,text=True)
        print((run.stdout+run.stderr).strip())
        if run.returncode: raise SystemExit(run.returncode)
if __name__=='__main__': main()
