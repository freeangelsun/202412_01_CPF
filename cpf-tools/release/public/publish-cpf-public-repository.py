#!/usr/bin/env python3
"""Fail-closed CPF public repository publication driver.

No public Git write occurs until private gates, public staging, leakage checks and
clean public consumer builds are all successful.
"""
from __future__ import annotations
import argparse, json, os, shutil, subprocess, sys, tempfile
from pathlib import Path

class PublishError(RuntimeError): pass

def run(cmd:list[str],cwd:Path,capture:bool=False)->str:
    print('[CPF][PUBLIC][RUN]',' '.join(cmd),flush=True)
    cp=subprocess.run(cmd,cwd=cwd,text=True,capture_output=capture,check=False)
    if cp.returncode:
        if capture:
            sys.stderr.write(cp.stdout); sys.stderr.write(cp.stderr)
        raise PublishError(f'command failed exit={cp.returncode}: {cmd}')
    return cp.stdout.strip() if capture else ''

def require_clean_git(root:Path)->str:
    git=shutil.which('git');
    if not git: raise PublishError('git executable unavailable')
    inside=run([git,'rev-parse','--is-inside-work-tree'],root,True)
    if inside.lower()!='true': raise PublishError('private source is not a Git working tree')
    dirty=run([git,'status','--porcelain=v1','--untracked-files=all'],root,True)
    if dirty: raise PublishError('private source working tree must be clean before public publication')
    return run([git,'rev-parse','HEAD'],root,True)

def private_gates(root:Path,python:str)->None:
    gates=[
      [python,'cpf-tools/release/tools/verify-cpf-publication-starter-closure.py','--root','.','--require-physical'],
      [python,'cpf-tools/verification/tools/verify-cpf-bza-route-contract.py','--root','.'],
      [python,'cpf-tools/verification/tools/verify-cpf-frontend-consumer-closure.py','--root','.'],
      [python,'cpf-tools/verification/tools/verify-cpf-optional-surface-contract.py','--root','.'],
      [python,'cpf-tools/verification/verify_common_product_service_dx.py','.'],
      [python,'cpf-tools/verification/tools/verify-cpf-education-active-surface.py','--root','.'],
      [python,'cpf-tools/verification/tools/verify-cpf-edu-executable-coverage.py','--root','.'],
    ]
    for gate in gates: run(gate,root)

def private_build_and_publication(root:Path)->None:
    wrapper=root/('gradlew.bat' if os.name=='nt' else 'gradlew')
    if not wrapper.is_file(): raise PublishError('Gradle wrapper missing')
    if os.name!='nt': wrapper.chmod(wrapper.stat().st_mode|0o111)
    run([str(wrapper),'clean','cpfBuild','qualityGate','cpfTest','publicationGate','publishToMavenLocal','--continue','--no-daemon'],root)

def clear_clone_worktree(clone:Path)->None:
    for child in clone.iterdir():
        if child.name=='.git': continue
        if child.is_dir() and not child.is_symlink(): shutil.rmtree(child)
        else: child.unlink()

def sync_tree(source:Path,target:Path)->None:
    for item in source.iterdir():
        dest=target/item.name
        if item.is_dir(): shutil.copytree(item,dest)
        else: shutil.copy2(item,dest)

def canonical_public_remote(root:Path)->str:
    policy_path=root/'cpf-tools/release/public/cpf-public-surface-policy.json'
    if not policy_path.is_file():
        raise PublishError('public surface policy missing')
    policy=json.loads(policy_path.read_text(encoding='utf-8-sig'))
    repository=str(policy.get('repository') or '').strip().strip('/')
    if repository!='cpf-team/cpf-framework':
        raise PublishError('public surface policy repository must be cpf-team/cpf-framework')
    return 'https://' + 'github.com/' + repository + '.git'

def validate_remote(remote:str)->None:
    normalized=remote.rstrip('/').removesuffix('.git').lower()
    if not normalized.endswith('github.com/cpf-team/cpf-framework'):
        raise PublishError('public repository target must be cpf-team/cpf-framework')

def publish(root:Path,remote:str,push:bool,exclude_bza:bool,commit_message:str)->dict:
    python=sys.executable
    source_sha=require_clean_git(root)
    private_gates(root,python)
    private_build_and_publication(root)
    validate_remote(remote)
    git=shutil.which('git') or 'git'
    with tempfile.TemporaryDirectory(prefix='cpf-public-release-') as td:
        temp=Path(td); staging=temp/'staging'; clone=temp/'repository'
        prepare=root/'cpf-tools/release/public/prepare-cpf-public-workspace.py'
        command=[python,str(prepare),'--root',str(root),'--staging',str(staging),'--source-identity',source_sha,'--verify-build']
        if not exclude_bza: command.append('--include-bza')
        run(command,root)
        ready=json.loads((staging/'.cpf-public/READY.json').read_text(encoding='utf-8'))
        if ready.get('status')!='PASS': raise PublishError('public staging is not READY')
        run([git,'clone','--no-tags',remote,str(clone)],temp)
        public_dirty=run([git,'status','--porcelain=v1','--untracked-files=all'],clone,True)
        if public_dirty: raise PublishError('fresh public clone unexpectedly dirty')
        clear_clone_worktree(clone); sync_tree(staging,clone)
        # Re-run the public workspace verification in the exact clone that may be pushed.
        if os.name=='nt': run(['pwsh','-NoProfile','-File',str(clone/'tools/verify-public-workspace.ps1')],clone)
        else: run(['bash',str(clone/'tools/verify-public-workspace.sh')],clone)
        run([git,'add','-A'],clone)
        run([git,'diff','--cached','--check'],clone)
        changed=run([git,'diff','--cached','--name-only'],clone,True).splitlines()
        if not changed:
            return {'status':'PASS','result':'NO_CHANGES','sourceSha':source_sha,'pushExecuted':False,'fileCount':ready.get('fileCount')}
        if not push:
            return {'status':'PASS','result':'READY_NOT_PUSHED','sourceSha':source_sha,'pushExecuted':False,'changedFiles':len(changed),'fileCount':ready.get('fileCount')}
        run([git,'commit','-m',commit_message or f'Publish CPF public workspace from {source_sha[:12]}'],clone)
        branch=run([git,'branch','--show-current'],clone,True)
        if not branch: raise PublishError('public clone current branch unavailable')
        run([git,'push','origin',f'HEAD:{branch}'],clone)
        return {'status':'PASS','result':'PUSHED','sourceSha':source_sha,'pushExecuted':True,'branch':branch,'changedFiles':len(changed),'fileCount':ready.get('fileCount')}

def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--remote'); ap.add_argument('--push',action='store_true'); ap.add_argument('--exclude-bza',action='store_true'); ap.add_argument('--commit-message',default='')
    a=ap.parse_args(); root=Path(a.root).resolve(); remote=a.remote or canonical_public_remote(root)
    try: result=publish(root,remote,a.push,a.exclude_bza,a.commit_message); code=0
    except Exception as e: result={'status':'FAIL','message':str(e),'pushExecuted':False}; code=1
    print(json.dumps(result,ensure_ascii=False)); return code
if __name__=='__main__': raise SystemExit(main())
