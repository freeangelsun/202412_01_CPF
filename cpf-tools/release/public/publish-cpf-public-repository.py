#!/usr/bin/env python3
"""CPF Public Developer Workspace release preparation driver.

This tool never commits or pushes. It prepares an isolated Maven-compatible binary
repository, a default-deny public staging tree, and a persistent fresh public clone
outside the private CPF root. The final clone is left READY_TO_COMMIT for explicit
user review and manual commit/push.
"""
from __future__ import annotations
import argparse, json, os, shutil, subprocess, sys
from datetime import datetime
from pathlib import Path

class PublishError(RuntimeError): pass

# Public Developer Workspace provides Windows/Linux entry points. The published
# generator must therefore contain at least the mainstream x64 distribution for
# both operating systems before a public release can be declared ready.
REQUIRED_GENERATOR_CLASSIFIERS = ("windows-x64", "linux-x64")

def run(cmd:list[str],cwd:Path,capture:bool=False,env:dict|None=None)->str:
    print('[CPF][PUBLIC][RUN]',' '.join(cmd),flush=True)
    cp=subprocess.run(cmd,cwd=cwd,text=True,capture_output=capture,env=env,check=False)
    if cp.returncode:
        if capture:
            sys.stderr.write(cp.stdout); sys.stderr.write(cp.stderr)
        raise PublishError(f'command failed exit={cp.returncode}: {cmd}')
    return cp.stdout.strip() if capture else ''

def require_clean_git(root:Path)->str:
    git=shutil.which('git')
    if not git: raise PublishError('git executable unavailable')
    if run([git,'rev-parse','--is-inside-work-tree'],root,True).lower()!='true':
        raise PublishError('private source is not a Git working tree')
    dirty=run([git,'status','--porcelain=v1','--untracked-files=all'],root,True)
    if dirty: raise PublishError('private source working tree must be clean before public release preparation')
    return run([git,'rev-parse','HEAD'],root,True)

def read_platform_version(root:Path)->str:
    path=root/'gradle/cpf-platform.properties'
    if not path.is_file(): raise PublishError(f'platform properties missing: {path}')
    for line in path.read_text(encoding='utf-8-sig').splitlines():
        if line.strip().startswith('platformVersion='):
            value=line.split('=',1)[1].strip()
            if value: return value
    raise PublishError('platformVersion missing')

def private_gates(root:Path,python:str)->None:
    gates=[
      [python,'cpf-tools/release/tools/verify-cpf-publication-starter-closure.py','--root','.','--require-physical'],
      [python,'cpf-tools/verification/tools/verify-cpf-frontend-consumer-closure.py','--root','.'],
      [python,'cpf-tools/verification/tools/verify-cpf-optional-surface-contract.py','--root','.'],
      [python,'cpf-tools/verification/verify_common_product_service_dx.py','.'],
      [python,'cpf-tools/verification/tools/verify-cpf-education-active-surface.py','--root','.'],
      [python,'cpf-tools/verification/tools/verify-cpf-edu-executable-coverage.py','--root','.'],
      [python,'cpf-tools/verification/nxt3/cpf_nxt3_generator_gate.py','--root','.'],
    ]
    for gate in gates: run(gate,root)

def private_build_and_publication(root:Path,binary_repo:Path,version:str)->None:
    wrapper=root/('gradlew.bat' if os.name=='nt' else 'gradlew')
    if not wrapper.is_file(): raise PublishError('Gradle wrapper missing')
    if os.name!='nt': wrapper.chmod(wrapper.stat().st_mode|0o111)
    binary_repo.mkdir(parents=True,exist_ok=False)
    run([str(wrapper),'clean','cpfBuild','qualityGate','cpfTest','publicationGate',
         'publishCpfVerifiedLocalPlatformArtifacts','--continue','--no-daemon',
         f'-PcpfArtifactMode=LOCAL_DEV',f'-PcpfPublicBinaryRepository={binary_repo}',
         f'-PcpfPlatformVersion={version}'],root)
    bom=binary_repo/'com/cpf/cpf-platform-bom'/version/f'cpf-platform-bom-{version}.pom'
    if not bom.is_file(): raise PublishError(f'isolated binary publication missing CPF BOM: {bom}')


def _generator_distribution_files(directory:Path,version:str,classifier:str)->tuple[Path,Path,Path]:
    stem=f'cpf-generator-cli-{version}-{classifier}'
    return directory/(stem+'.zip'), directory/(stem+'.zip.sha256'), directory/(stem+'.json')

def _verify_generator_distribution(directory:Path,version:str,classifier:str)->tuple[Path,Path,Path]:
    archive,checksum,manifest=_generator_distribution_files(directory,version,classifier)
    for path in (archive,checksum,manifest):
        if not path.is_file():
            raise PublishError(f'generator distribution missing classifier={classifier}: {path}')
    import hashlib
    digest=hashlib.sha256(archive.read_bytes()).hexdigest()
    expected=checksum.read_text(encoding='ascii').strip().split()[0].lower()
    if digest!=expected:
        raise PublishError(f'generator checksum mismatch classifier={classifier} expected={expected} actual={digest}')
    data=json.loads(manifest.read_text(encoding='utf-8'))
    if data.get('artifactId')!='cpf-generator-cli' or data.get('version')!=version or data.get('classifier')!=classifier:
        raise PublishError(f'generator manifest coordinate mismatch classifier={classifier}: {manifest}')
    if data.get('sha256')!=digest:
        raise PublishError(f'generator manifest checksum mismatch classifier={classifier}: {manifest}')
    return archive,checksum,manifest

def publish_generator_distributions(root:Path,binary_repo:Path,version:str,prebuilt_dir:Path|None=None)->dict:
    """Publish validated OS-specific generator distributions into Maven layout.

    A private build agent can build its native classifier. Cross-OS artifacts are
    intentionally accepted only from an explicit prebuilt directory (for example,
    a CI matrix aggregation directory); the release driver never fabricates or
    silently substitutes a classifier.
    """
    import platform
    workspace=prebuilt_dir
    host_os=platform.system().lower()
    host_machine=platform.machine().lower()
    host_os_part='windows' if host_os.startswith('win') else ('linux' if host_os=='linux' else '')
    host_arch='x64' if host_machine in {'x86_64','amd64'} else ('arm64' if host_machine in {'aarch64','arm64'} else '')
    host_classifier=f'{host_os_part}-{host_arch}' if host_os_part and host_arch else ''
    native_build=Path(os.environ.get('CPF_GENERATOR_NATIVE_BUILD_DIR','')).expanduser() if os.environ.get('CPF_GENERATOR_NATIVE_BUILD_DIR') else None
    if native_build is None:
        native_build=binary_repo.parent/'generator-native'
    if host_classifier in REQUIRED_GENERATOR_CLASSIFIERS:
        host_prebuilt_complete=False
        if workspace is not None:
            try:
                _verify_generator_distribution(workspace,version,host_classifier)
                host_prebuilt_complete=True
            except PublishError:
                host_prebuilt_complete=False
        archive,checksum,manifest=_generator_distribution_files(native_build,version,host_classifier)
        if not host_prebuilt_complete and not (archive.is_file() and checksum.is_file() and manifest.is_file()):
            builder=root/'cpf-tools/generator/distribution/build-cpf-generator-binary.py'
            run([sys.executable,str(builder),'--root',str(root),'--output',str(native_build),'--version',version],root)
    sources=[]
    if workspace is not None:
        sources.append(workspace)
    sources.append(native_build)
    published=[]
    target=binary_repo/'com/cpf/tooling/cpf-generator-cli'/version
    target.mkdir(parents=True,exist_ok=True)
    for classifier in REQUIRED_GENERATOR_CLASSIFIERS:
        found=None
        for directory in sources:
            try:
                found=_verify_generator_distribution(directory,version,classifier); break
            except PublishError:
                continue
        if found is None:
            raise PublishError(
                'public generator distribution incomplete: required classifier=' + classifier +
                ". Build the Windows/Linux CI matrix and provide its aggregate directory via --generator-artifacts."
            )
        for source in found:
            shutil.copy2(source,target/source.name)
        published.append(classifier)
    return {'artifact':'com.cpf.tooling:cpf-generator-cli','version':version,'classifiers':published}

def canonical_public_remote(root:Path)->str:
    policy_path=root/'cpf-tools/release/public/cpf-public-surface-policy.json'
    policy=json.loads(policy_path.read_text(encoding='utf-8-sig')) if policy_path.is_file() else {}
    repository=str(policy.get('repository') or '').strip().strip('/')
    remote_env=str(policy.get('gitRemoteEnvironment') or 'CPF_PUBLIC_GIT_REMOTE').strip()
    if repository!='cpf-team/cpf-framework':
        raise PublishError('public surface policy repository must be cpf-team/cpf-framework')
    remote=os.environ.get(remote_env,'').strip()
    if not remote:
        raise PublishError(f'public Git remote is required via {remote_env}; target repository={repository}')
    validate_remote(remote, repository)
    return remote

def validate_remote(remote:str, repository:str='cpf-team/cpf-framework')->None:
    normalized=remote.rstrip('/').removesuffix('.git').lower()
    expected=repository.strip('/').lower()
    if not normalized.endswith('/'+expected) and not normalized.endswith(':'+expected):
        raise PublishError(f'public repository target must be {repository}')

def default_release_parent()->Path:
    configured=os.environ.get('CPF_PUBLIC_RELEASE_ROOT','').strip()
    if configured: return Path(configured).expanduser().resolve()
    downloads=Path.home()/'Downloads'
    return downloads if downloads.is_dir() else Path.home()

def release_root(private_root:Path,requested:str|None)->Path:
    if requested:
        root=Path(requested).expanduser().resolve()
    else:
        root=default_release_parent()/('CPF_PUBLIC_RELEASE_'+datetime.now().strftime('%Y%m%d_%H%M%S'))
    try:
        root.relative_to(private_root)
    except ValueError:
        pass
    else:
        raise PublishError('public release workspace must be outside the private CPF Git root')
    if root.exists(): raise PublishError(f'public release workspace already exists: {root}')
    return root

def sync_public_surface(staging:Path,clone:Path)->None:
    """Make fresh public clone match staging, preserving only .git.

    This function is intentionally scoped to a validated fresh clone and never acts
    on the private CPF root. Public Surface Policy is the final file-set owner.
    """
    if not (clone/'.git').is_dir(): raise PublishError('refusing sync: target is not a Git clone')
    staging_files={p.relative_to(staging).as_posix() for p in staging.rglob('*') if p.is_file()}
    for p in sorted((x for x in clone.rglob('*') if x.is_file() and '.git' not in x.relative_to(clone).parts),reverse=True):
        rel=p.relative_to(clone).as_posix()
        if rel not in staging_files: p.unlink()
    for d in sorted((x for x in clone.rglob('*') if x.is_dir() and x.name!='.git'),reverse=True):
        if '.git' in d.relative_to(clone).parts: continue
        try: d.rmdir()
        except OSError: pass
    for source in sorted(x for x in staging.rglob('*') if x.is_file()):
        rel=source.relative_to(staging); dest=clone/rel; dest.parent.mkdir(parents=True,exist_ok=True); shutil.copy2(source,dest)

def publish(root:Path,remote:str,exclude_backoffice:bool,output_root:str|None,generator_artifacts:str|None=None)->dict:
    python=sys.executable
    source_sha=require_clean_git(root)
    version=read_platform_version(root)
    private_gates(root,python)
    validate_remote(remote)
    release=release_root(root,output_root)
    release.mkdir(parents=True)
    binary_repo=release/'binary-repository'; staging=release/'staging'; clone=release/'clone'; logs=release/'logs'; logs.mkdir()
    private_build_and_publication(root,binary_repo,version)
    generator_result=publish_generator_distributions(root,binary_repo,version,Path(generator_artifacts).resolve() if generator_artifacts else None)
    # Binary Repository는 Java/BOM/Starter뿐 아니라 OS별 Generator까지 모두 publish된 뒤 한 번에 검증합니다.
    verifier=root/'cpf-tools/release/public/verify-cpf-public-binary-repository.py'
    run([python,str(verifier),'--root',str(root),'--repository',str(binary_repo),'--version',version],root)
    env=dict(os.environ)
    env['CPF_MAVEN_REPOSITORY_URL']=binary_repo.as_uri()
    env['CPF_VERSION']=version
    prepare=root/'cpf-tools/release/public/prepare-cpf-public-workspace.py'
    command=[python,str(prepare),'--root',str(root),'--staging',str(staging),'--source-identity',source_sha,'--verify-build']
    if not exclude_backoffice: command.append('--include-backoffice')
    run(command,root,env=env)
    ready=json.loads((staging/'.cpf-public/READY.json').read_text(encoding='utf-8'))
    if ready.get('status')!='PASS': raise PublishError('public staging is not READY')
    git=shutil.which('git') or 'git'
    run([git,'clone','--no-tags',remote,str(clone)],release)
    if run([git,'status','--porcelain=v1','--untracked-files=all'],clone,True):
        raise PublishError('fresh public clone unexpectedly dirty')
    sync_public_surface(staging,clone)
    verifier=clone/'tools'/('verify-public-workspace.ps1' if os.name=='nt' else 'verify-public-workspace.sh')
    if os.name=='nt': run(['pwsh','-NoProfile','-File',str(verifier)],clone,env=env)
    else: run(['bash',str(verifier)],clone,env=env)
    run([git,'add','-A'],clone)
    run([git,'diff','--cached','--check'],clone)
    changed=run([git,'diff','--cached','--name-only'],clone,True).splitlines()
    result={
      'status':'PASS','result':'READY_TO_COMMIT' if changed else 'NO_CHANGES',
      'sourceSha':source_sha,'platformVersion':version,'pushExecuted':False,'commitExecuted':False,
      'releaseRoot':str(release),'binaryRepository':str(binary_repo),'staging':str(staging),'clone':str(clone),
      'changedFiles':len(changed),'publicFileCount':ready.get('fileCount'),'generatorDistribution':generator_result
    }
    (release/'PUBLIC_RELEASE_READY.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    return result

def main()->int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--root',default='.')
    ap.add_argument('--remote')
    ap.add_argument('--exclude-backoffice',action='store_true')
    ap.add_argument('--output-root')
    ap.add_argument('--generator-artifacts',help='Directory containing CI-matrix cpf-generator-cli distributions for all required classifiers')
    a=ap.parse_args(); root=Path(a.root).resolve(); remote=a.remote or canonical_public_remote(root)
    try: result=publish(root,remote,a.exclude_backoffice,a.output_root,a.generator_artifacts); code=0
    except Exception as e: result={'status':'FAIL','message':str(e),'pushExecuted':False,'commitExecuted':False}; code=1
    print(json.dumps(result,ensure_ascii=False)); return code

if __name__=='__main__': raise SystemExit(main())
