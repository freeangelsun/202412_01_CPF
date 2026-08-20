#!/usr/bin/env python3
"""Build the canonical CPF Generator as a self-contained OS binary.

The generator implementation remains cpf_domain_generator.py. This packaging step does
not own generation semantics; it only freezes the canonical CLI + resources so Public
Developer Workspace users do not need Python installed.
"""
from __future__ import annotations
import argparse, hashlib, json, os, platform, shutil, subprocess, sys, tempfile, zipfile
from pathlib import Path

class DistributionError(RuntimeError): pass

def classifier() -> str:
    os_name=platform.system().lower()
    if os_name.startswith('win'): os_part='windows'
    elif os_name=='linux': os_part='linux'
    else: raise DistributionError(f'unsupported generator distribution OS: {platform.system()}')
    machine=platform.machine().lower()
    if machine in {'x86_64','amd64'}: arch='x64'
    elif machine in {'aarch64','arm64'}: arch='arm64'
    else: raise DistributionError(f'unsupported generator distribution architecture: {machine}')
    return f'{os_part}-{arch}'

def sha256(path:Path)->str:
    h=hashlib.sha256()
    with path.open('rb') as stream:
        for chunk in iter(lambda:stream.read(1024*1024),b''): h.update(chunk)
    return h.hexdigest()

def copy_resources(root:Path,target:Path)->None:
    mappings=[
      ('cpf-tools/generator/engine', 'cpf-tools/generator/engine'),
      ('cpf-tools/generator/contracts', 'cpf-tools/generator/contracts'),
      ('cpf-tools/generator/config', 'cpf-tools/generator/config'),
      ('cpf-tools/db/generated/domain-template', 'cpf-tools/db/generated/domain-template'),
      ('gradle/cpf-stack.properties', 'gradle/cpf-stack.properties'),
    ]
    for source_rel,target_rel in mappings:
        src=root/source_rel; dst=target/target_rel
        if src.is_dir(): shutil.copytree(src,dst)
        elif src.is_file(): dst.parent.mkdir(parents=True,exist_ok=True); shutil.copy2(src,dst)
        else: raise DistributionError(f'generator resource missing: {source_rel}')

def run(root:Path,output:Path,version:str)->dict:
    try: import PyInstaller  # noqa: F401
    except Exception as exc: raise DistributionError('PyInstaller is required only on private release build agents') from exc
    output.mkdir(parents=True,exist_ok=True)
    cls=classifier(); exe_name='cpf-generator.exe' if cls.startswith('windows-') else 'cpf-generator'
    with tempfile.TemporaryDirectory(prefix='cpf-generator-dist-') as td:
        stage=Path(td); resources=stage/'cpf-generator-resources'; copy_resources(root,resources)
        dist=stage/'dist'; work=stage/'work'; spec=stage/'spec';
        entry=root/'cpf-tools/runtime/cli/cpf.py'
        sep=';' if os.name=='nt' else ':'
        cmd=[sys.executable,'-m','PyInstaller','--noconfirm','--clean','--onefile','--name','cpf-generator',
             '--distpath',str(dist),'--workpath',str(work),'--specpath',str(spec),
             '--add-data',f'{resources}{sep}cpf-generator-resources',str(entry)]
        cp=subprocess.run(cmd,cwd=root,text=True,capture_output=True)
        if cp.returncode:
            raise DistributionError(f'PyInstaller failed exit={cp.returncode}: {cp.stdout[-2000:]} {cp.stderr[-2000:]}')
        executable=dist/exe_name
        if not executable.is_file(): raise DistributionError(f'generator executable missing: {executable}')
        zip_name=f'cpf-generator-cli-{version}-{cls}.zip'; zip_path=output/zip_name
        with zipfile.ZipFile(zip_path,'w',compression=zipfile.ZIP_DEFLATED) as zf:
            zf.write(executable,exe_name)
        digest=sha256(zip_path); (output/(zip_name+'.sha256')).write_text(digest+'\n',encoding='ascii')
        manifest={'schemaVersion':1,'artifactId':'cpf-generator-cli','version':version,'classifier':cls,
                  'archive':zip_name,'sha256':digest,'canonicalEngine':'cpf-tools/generator/engine/cpf_domain_generator.py'}
        (output/f'cpf-generator-cli-{version}-{cls}.json').write_text(json.dumps(manifest,indent=2)+'\n',encoding='utf-8')
        return {'status':'PASS',**manifest,'output':str(zip_path)}

def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--output',required=True); ap.add_argument('--version',required=True)
    a=ap.parse_args()
    try: result=run(Path(a.root).resolve(),Path(a.output).resolve(),a.version); code=0
    except Exception as exc: result={'status':'FAIL','message':str(exc)}; code=1
    print(json.dumps(result,ensure_ascii=False)); return code
if __name__=='__main__': raise SystemExit(main())
