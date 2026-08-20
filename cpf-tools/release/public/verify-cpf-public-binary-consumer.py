#!/usr/bin/env python3
"""Public Binary Repository를 clean Public Workspace + isolated Gradle cache에서 소비 검증합니다."""
from __future__ import annotations
import argparse, json, os, re, shutil, subprocess, tempfile
from pathlib import Path

class ConsumerError(RuntimeError): pass
FORBIDDEN = (re.compile(r'\bmavenLocal\s*\('), re.compile(r'cpf-tools[/\\]'), re.compile(r'cpf-starters[/\\]'), re.compile(r'cpf-core[/\\]'))

def scan_workspace(stage:Path)->None:
    for p in stage.rglob('*'):
        if not p.is_file() or p.suffix.lower() not in {'.gradle','.kts','.properties','.yml','.yaml','.md'}: continue
        text=p.read_text(encoding='utf-8',errors='replace')
        for rx in FORBIDDEN:
            if rx.search(text): raise ConsumerError(f'forbidden private/local consumer token pattern={rx.pattern} file={p.relative_to(stage)}')

def run(root:Path,repository:Path,version:str,source_identity:str)->dict:
    if not repository.is_dir(): raise ConsumerError(f'public binary repository missing: {repository}')
    verifier=root/'cpf-tools/release/public/verify-cpf-public-binary-repository.py'
    cp=subprocess.run([os.fspath(Path(os.sys.executable)),os.fspath(verifier),'--root',os.fspath(root),'--repository',os.fspath(repository),'--version',version],cwd=root,text=True,capture_output=True)
    if cp.returncode: raise ConsumerError('public binary repository verifier failed: '+(cp.stdout+cp.stderr)[-4000:])
    with tempfile.TemporaryDirectory(prefix='cpf-public-consumer-') as td:
        base=Path(td); stage=base/'workspace'; cache=base/'gradle-home'
        prepare=root/'cpf-tools/release/public/prepare-cpf-public-workspace.py'
        env=os.environ.copy(); env.update({'CPF_VERSION':version,'CPF_MAVEN_REPOSITORY_URL':repository.resolve().as_uri(),'GRADLE_USER_HOME':os.fspath(cache)})
        cp=subprocess.run([os.fspath(Path(os.sys.executable)),os.fspath(prepare),'--root',os.fspath(root),'--staging',os.fspath(stage),'--source-identity',source_identity],cwd=root,env=env,text=True,capture_output=True)
        if cp.returncode: raise ConsumerError('public workspace staging failed: '+(cp.stdout+cp.stderr)[-4000:])
        scan_workspace(stage)
        gradle=stage/('gradlew.bat' if os.name=='nt' else 'gradlew')
        if os.name!='nt': gradle.chmod(gradle.stat().st_mode|0o111)
        # Public Workspace의 모든 composite domain이 동일 isolated repository만 사용합니다.
        cp=subprocess.run([os.fspath(gradle),'cpfVerify','--no-daemon','--stacktrace'],cwd=stage,env=env,text=True,capture_output=True)
        if cp.returncode: raise ConsumerError('isolated public consumer build failed: '+(cp.stdout+cp.stderr)[-8000:])
        if not cache.is_dir(): raise ConsumerError('isolated Gradle cache was not used')
        return {'status':'PASS','version':version,'sourceIdentity':source_identity,'isolatedGradleCache':True,'mavenLocal':False,'privateSourceDependency':False}

def main()->int:
    ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--repository',required=True);ap.add_argument('--version',required=True);ap.add_argument('--source-identity',required=True);a=ap.parse_args()
    try:r=run(Path(a.root).resolve(),Path(a.repository).resolve(),a.version,a.source_identity);c=0
    except Exception as e:r={'status':'FAIL','message':str(e)};c=1
    print(json.dumps(r,ensure_ascii=False));return c
if __name__=='__main__':raise SystemExit(main())
