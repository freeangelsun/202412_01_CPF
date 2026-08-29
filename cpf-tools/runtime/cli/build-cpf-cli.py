#!/usr/bin/env python3
from __future__ import annotations
import argparse, hashlib, re, shutil, subprocess, tempfile
from pathlib import Path

def run(cmd, cwd):
    cp=subprocess.run(cmd,cwd=cwd,text=True,encoding='utf-8',errors='replace',capture_output=True)
    if cp.returncode:
        raise SystemExit(f'CPF_CLI_BUILD=FAIL rc={cp.returncode}\n{cp.stdout}{cp.stderr}')
    return cp

def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--root',type=Path,default=Path.cwd()); ap.add_argument('--profile',choices=['INTERNAL','PUBLIC'],default='INTERNAL'); ap.add_argument('--source-identity'); ap.add_argument('--version')
    ns=ap.parse_args(); root=ns.root.resolve()
    javac=shutil.which('javac'); jar=shutil.which('jar')
    if not javac or not jar: raise SystemExit('CPF_CLI_BUILD=FAIL javac/jar required')
    javac_version=run([javac,'-version'],root).stdout.strip()
    match=re.search(r'(?:javac\s+)?(\d+)(?:[.\s-]|$)',javac_version)
    if not match or int(match.group(1)) != 25:
        raise SystemExit(f'CPF_CLI_BUILD=FAIL Java 25 javac required actual={javac_version or "UNKNOWN"}')
    source_identity=ns.source_identity
    if not source_identity:
        cp=run([shutil.which('python3') or shutil.which('python'),'cpf-tools/verification/tools/cpf-source-state.py','--root',str(root),'--scope','source'],root)
        import json; source_identity=json.loads(cp.stdout)['contentSha256']
    version=ns.version
    if not version:
        props=(root/'gradle/cpf-platform.properties').read_text(encoding='utf-8-sig').splitlines()
        version=next((x.split('=',1)[1].strip() for x in props if x.strip().startswith('platformVersion=')),'UNKNOWN')
    sources=[root/'cpf-tools/runtime/cli/java/CpfCli.java',root/'cpf-tools/runtime/bootstrap/CpfBootstrap.java',root/'cpf-tools/runtime/cli/java/CpfGeneratorLauncher.java']
    missing=[str(p.relative_to(root)) for p in sources if not p.is_file()]
    if missing: raise SystemExit(f'CPF_CLI_BUILD=FAIL missing={missing}')
    target=root/'cpf-tools/runtime/cli/lib/cpf-cli.jar'; target.parent.mkdir(parents=True,exist_ok=True)
    with tempfile.TemporaryDirectory(prefix='cpf-cli-build-') as td:
        td=Path(td); classes=td/'classes'; classes.mkdir()
        run([javac,'--release','25','-encoding','UTF-8','-Xlint:all','-Werror','-d',str(classes),*[str(p) for p in sources]],root)
        (classes/'cpf-cli.properties').write_text(f'version={version}\nsourceIdentitySha256={source_identity}\ncapabilityProfile={ns.profile}\nrequiredJavaFeature=25\n',encoding='utf-8',newline='\n')
        run([jar,'--create','--file',str(target),'--main-class','CpfCli','-C',str(classes),'.'],root)
    digest=hashlib.sha256(target.read_bytes()).hexdigest()
    (target.with_suffix('.jar.sha256')).write_text(f'{digest}  cpf-cli.jar\n',encoding='ascii',newline='\n')
    print(f'CPF_CLI_BUILD=PASS profile={ns.profile} sourceIdentity={source_identity} jar={target} sha256={digest}')
if __name__=='__main__': main()
