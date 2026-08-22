#!/usr/bin/env python3
"""Build a default-deny CPF public workspace from explicitly classified source."""
from __future__ import annotations
import argparse, fnmatch, hashlib, json, os, re, shutil, subprocess, sys, tempfile
from pathlib import Path, PurePosixPath

class PublicSurfaceError(RuntimeError): pass

def load_json(path: Path) -> dict:
    value=json.loads(path.read_text(encoding='utf-8-sig'))
    if not isinstance(value,dict): raise PublicSurfaceError(f'JSON object required: {path}')
    return value

def rel(root: Path,p:Path)->str: return p.relative_to(root).as_posix()
def sha256(p:Path)->str:
    h=hashlib.sha256()
    with p.open('rb') as f:
        for b in iter(lambda:f.read(1024*1024),b''): h.update(b)
    return h.hexdigest()

def safe_target(raw:str)->PurePosixPath:
    p=PurePosixPath(raw)
    if p.is_absolute() or '..' in p.parts or raw.startswith(('/', '\\')): raise PublicSurfaceError(f'unsafe target: {raw}')
    return p

def selected_rules(policy:dict,include_backoffice:bool):
    for rule in policy.get('sourceRules',[]):
        if rule.get('option')=='backoffice' and not include_backoffice: continue
        yield rule

def expand_rule(root:Path,rule:dict)->list[Path]:
    pattern=str(rule['pattern']).replace('\\','/')
    if pattern.endswith('/**'):
        base=root/pattern[:-3]
        return sorted(p for p in base.rglob('*') if p.is_file()) if base.is_dir() else []
    return sorted(p for p in root.glob(pattern) if p.is_file())

def target_for(root:Path,p:Path,rule:dict)->PurePosixPath:
    pattern=str(rule['pattern']).replace('\\','/')
    target=safe_target(str(rule['target']))
    if pattern.endswith('/**'):
        base=root/pattern[:-3]
        return target/PurePosixPath(p.relative_to(base).as_posix())
    if not any(token in pattern for token in ('*','?','[')) and p.is_file():
        return target
    return target/PurePosixPath(p.name)

def classify_and_copy(root:Path,staging:Path,policy:dict,include_backoffice:bool)->dict[str,str]:
    classifications={}
    for rule in selected_rules(policy,include_backoffice):
        files=expand_rule(root,rule)
        if rule.get('required') and not files: raise PublicSurfaceError(f"required public source missing: {rule['pattern']}")
        classification=str(rule['classification'])
        for source in files:
            target=target_for(root,source,rule); key=target.as_posix()
            if key in classifications: raise PublicSurfaceError(f'duplicate public target: {key}')
            dest=staging/target; dest.parent.mkdir(parents=True,exist_ok=True); shutil.copy2(source,dest)
            classifications[key]=classification
    for rule in policy.get('templateRules',[]):
        source=root/str(rule['source']); target=safe_target(str(rule['target'])); key=target.as_posix()
        if not source.is_file(): raise PublicSurfaceError(f'public template missing: {source}')
        if key in classifications: raise PublicSurfaceError(f'duplicate public target: {key}')
        dest=staging/target; dest.parent.mkdir(parents=True,exist_ok=True); shutil.copy2(source,dest)
        classifications[key]=str(rule['classification'])
    return classifications


def materialize_domain_catalogs(root:Path, staging:Path, policy:dict, classifications:dict[str,str])->None:
    """Publish canonical generated-domain definitions under a stable public catalog path.

    Physical generated projects remain under ``cpf-<domain>``.  The ``domains/<name>``
    catalog is a public bootstrap index, not a second source authority: every catalog
    definition must be byte-identical to the physical project's root cpf-domain.yaml.
    """
    for row in policy.get('mandatoryDomainCatalogs',[]):
        source_rel=str(row.get('source','')).strip(); target_rel=str(row.get('target','')).strip()
        physical=str(row.get('physicalProject','')).strip()
        if not source_rel or not target_rel or not physical:
            raise PublicSurfaceError(f'invalid mandatoryDomainCatalogs row: {row}')
        source=root/safe_target(source_rel); physical_root=root/safe_target(physical)
        if not physical_root.is_dir(): raise PublicSurfaceError(f'mandatory physical domain project missing: {physical}')
        if not source.is_file(): raise PublicSurfaceError(f'mandatory domain definition missing: {source_rel}')
        target=safe_target(target_rel); key=target.as_posix(); dest=staging/target
        if key in classifications: raise PublicSurfaceError(f'duplicate mandatory domain catalog target: {key}')
        dest.parent.mkdir(parents=True,exist_ok=True); shutil.copy2(source,dest)
        classifications[key]='PUBLIC_GENERATED_SOURCE'

def verify_domain_catalogs(staging:Path, policy:dict)->None:
    for row in policy.get('mandatoryDomainCatalogs',[]):
        target=str(row['target']).replace('\\','/'); physical=str(row['physicalProject']).replace('\\','/')
        catalog=staging/target; physical_definition=staging/physical/'cpf-domain.yaml'
        if not catalog.is_file(): raise PublicSurfaceError(f'mandatory public domain catalog missing: {target}')
        if not physical_definition.is_file(): raise PublicSurfaceError(f'mandatory public physical domain missing: {physical}/cpf-domain.yaml')
        if sha256(catalog)!=sha256(physical_definition):
            raise PublicSurfaceError(f'public domain catalog drift: {target} != {physical}/cpf-domain.yaml')

def verify_staging(staging:Path,policy:dict,classifications:dict[str,str])->list[dict]:
    allowed=set(map(str,policy.get('allowedClassifications',[])))
    forbidden_prefixes=tuple(map(str,policy.get('forbiddenPathPrefixes',[])))
    forbidden_names=list(map(str,policy.get('forbiddenNamePatterns',[])))
    forbidden_content=[re.compile(x) for x in policy.get('forbiddenContentPatterns',[])]
    rows=[]
    for p in sorted(x for x in staging.rglob('*') if x.is_file()):
        r=rel(staging,p)
        c=classifications.get(r)
        if not c and r.startswith('.cpf-public/'):
            c='PUBLIC_RELEASE_METADATA'
        if not c: raise PublicSurfaceError(f'unclassified public file: {r}')
        if c not in allowed: raise PublicSurfaceError(f'unknown public classification {c}: {r}')
        if r.startswith(forbidden_prefixes): raise PublicSurfaceError(f'private/internal path leaked: {r}')
        if any(fnmatch.fnmatch(p.name,pat) for pat in forbidden_names): raise PublicSurfaceError(f'secret file name forbidden: {r}')
        if p.stat().st_size <= 4*1024*1024:
            try: text=p.read_text(encoding='utf-8')
            except UnicodeDecodeError: text=''
            for rx in forbidden_content:
                if rx.search(text): raise PublicSurfaceError(f'secret-like content forbidden: {r} pattern={rx.pattern}')
        rows.append({'path':r,'classification':c,'size':p.stat().st_size,'sha256':sha256(p)})
    extra=sorted(set(classifications)-{r['path'] for r in rows})
    if extra: raise PublicSurfaceError(f'classified target missing from staging: {extra}')
    return rows

def write_metadata(staging:Path,rows:list[dict],policy:dict,source_identity:str,include_backoffice:bool)->None:
    meta=staging/'.cpf-public'; meta.mkdir(parents=True,exist_ok=True)
    manifest={'schemaVersion':1,'policyId':policy.get('policyId'),'sourceIdentity':source_identity,'includeBackoffice':include_backoffice,'fileCount':len(rows),'files':rows}
    mp=meta/'PUBLIC_MANIFEST.json'; mp.write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    # Metadata itself is generated after classification and is explicitly public release metadata.
    checksum='\n'.join(f"{r['sha256']}  {r['path']}" for r in rows)+'\n'
    (meta/'SHA256SUMS.txt').write_text(checksum,encoding='utf-8')

def run(cmd:list[str],cwd:Path,env:dict|None=None)->None:
    print('[CPF][PUBLIC][RUN]',' '.join(cmd),flush=True)
    cp=subprocess.run(cmd,cwd=cwd,env=env,check=False)
    if cp.returncode: raise PublicSurfaceError(f'command failed exit={cp.returncode}: {cmd}')

def verify_builds(staging:Path,include_backoffice:bool)->None:
    gradlew=staging/('gradlew.bat' if os.name=='nt' else 'gradlew')
    if not gradlew.is_file(): raise PublicSurfaceError('public Gradle wrapper missing')
    if os.name!='nt': gradlew.chmod(gradlew.stat().st_mode|0o111)
    run([str(gradlew),'-p',str(staging/'cpf-member'),'clean','build','--no-daemon'],staging)
    if include_backoffice:
        channel=staging/'cpf-backoffice-web'
        run([str(gradlew),'-p',str(channel),'clean','test','build','--no-daemon'],staging)
        front=channel/'frontend'
        if front.joinpath('package.json').is_file():
            npm=shutil.which('npm.cmd' if os.name=='nt' else 'npm')
            if not npm: raise PublicSurfaceError('npm unavailable for public Backoffice Web frontend verification')
            run([npm,'ci','--ignore-scripts'],front); run([npm,'run','verify'],front)

def prepare(root:Path,staging:Path,policy_path:Path,source_identity:str,include_backoffice:bool,verify_build:bool)->dict:
    policy=load_json(policy_path)
    if policy.get('defaultPolicy')!='DENY': raise PublicSurfaceError('public surface defaultPolicy must be DENY')
    if staging.exists(): shutil.rmtree(staging)
    staging.mkdir(parents=True)
    classifications=classify_and_copy(root,staging,policy,include_backoffice)
    materialize_domain_catalogs(root,staging,policy,classifications)
    verify_domain_catalogs(staging,policy)
    rows=verify_staging(staging,policy,classifications)
    write_metadata(staging,rows,policy,source_identity,include_backoffice)
    # Re-scan metadata explicitly under release classification without weakening initial default-deny source copy.
    for p in (staging/'.cpf-public').iterdir(): classifications[rel(staging,p)]='PUBLIC_RELEASE_METADATA'
    final_rows=verify_staging(staging,policy,classifications)
    if verify_build: verify_builds(staging,include_backoffice)
    result={'status':'PASS','fileCount':len(final_rows),'includeBackoffice':include_backoffice,'staging':str(staging),'sourceIdentity':source_identity}
    (staging/'.cpf-public/READY.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    verify_domain_catalogs(staging,policy)
    verified=verify_staging(staging,policy,classifications)
    result['fileCount']=len(verified)
    return result

def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--staging',required=True); ap.add_argument('--policy'); ap.add_argument('--source-identity',required=True); ap.add_argument('--include-backoffice',action='store_true'); ap.add_argument('--verify-build',action='store_true')
    a=ap.parse_args(); root=Path(a.root).resolve(); staging=Path(a.staging).resolve(); policy=Path(a.policy).resolve() if a.policy else root/'cpf-tools/release/public/cpf-public-surface-policy.json'
    try: result=prepare(root,staging,policy,a.source_identity,a.include_backoffice,a.verify_build); code=0
    except Exception as e: result={'status':'FAIL','message':str(e)}; code=1
    print(json.dumps(result,ensure_ascii=False)); return code
if __name__=='__main__': raise SystemExit(main())
