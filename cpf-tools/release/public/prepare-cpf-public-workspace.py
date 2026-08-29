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


def read_properties(path:Path)->dict[str,str]:
    values={}
    if not path.is_file(): return values
    for raw in path.read_text(encoding='utf-8-sig').splitlines():
        line=raw.strip()
        if not line or line.startswith('#') or '=' not in line: continue
        key,value=line.split('=',1); values[key.strip()]=value.strip()
    return values

def discover_domain_projects(root:Path)->list[dict]:
    """Discover current Generated/Customer Domain roots from the Developer Contract.

    Domain absence is a normal NOT_SELECTED state. Fixed names such as member/external are
    never required by the release projection. Prebuilt backoffice remains an explicit option.
    """
    rows=[]
    for project in sorted(p for p in root.glob('cpf-*') if p.is_dir()):
        contract=project/'gradle.properties'; values=read_properties(contract)
        if values.get('cpf.domain.contractVersion')!='1': continue
        name=values.get('cpf.domain.name','').strip(); system=values.get('cpf.domain.systemCode','').strip()
        mode=values.get('cpf.domain.generationMode','generated').strip().lower()
        if not name or not system or project.name!=f'cpf-{name}':
            raise PublicSurfaceError(f'invalid Domain Developer contract: {contract}')
        rows.append({'project':project,'name':name,'systemCode':system,'generationMode':mode,'values':values})
    names=[r['name'] for r in rows]; codes=[r['systemCode'] for r in rows]
    if len(names)!=len(set(names)) or len(codes)!=len(set(codes)):
        raise PublicSurfaceError(f'duplicate Domain identity: names={names} systemCodes={codes}')
    return rows

def copy_domain_tree(root:Path,staging:Path,row:dict,classifications:dict[str,str])->None:
    project=row['project']; classification='PUBLIC_GENERATED_SOURCE'
    forbidden_parts={'build','.gradle','node_modules','dist','.git','__pycache__','.pytest_cache'}
    forbidden_names={'cpf-domain.yaml','cpf-generator.lock.json'}
    for source in sorted(p for p in project.rglob('*') if p.is_file()):
        relative=source.relative_to(project)
        if any(part in forbidden_parts for part in relative.parts) or source.name in forbidden_names: continue
        target=PurePosixPath(project.name)/PurePosixPath(relative.as_posix()); key=target.as_posix()
        if key in classifications: continue
        dest=staging/target; dest.parent.mkdir(parents=True,exist_ok=True); shutil.copy2(source,dest); classifications[key]=classification

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
    discovered=discover_domain_projects(root)
    discovered_by_root={row['project'].name: row for row in discovered}
    for rule in selected_rules(policy,include_backoffice):
        pattern=str(rule.get('pattern','')).replace('\\','/')
        first=pattern.split('/',1)[0]
        discovered_row=discovered_by_root.get(first)
        if rule.get('legacyDynamicDomainRule') or (discovered_row is not None and discovered_row['generationMode']!='prebuilt'):
            continue
        files=expand_rule(root,rule)
        if rule.get('required') and not files: raise PublicSurfaceError(f"required public source missing: {rule['pattern']}")
        classification=str(rule['classification'])
        for source in files:
            target=target_for(root,source,rule); key=target.as_posix()
            if key in classifications: raise PublicSurfaceError(f'duplicate public target: {key}')
            dest=staging/target; dest.parent.mkdir(parents=True,exist_ok=True); shutil.copy2(source,dest)
            classifications[key]=classification
    for row in discovered:
        if row['generationMode']=='prebuilt': continue
        copy_domain_tree(root,staging,row,classifications)
    for rule in policy.get('templateRules',[]):
        source=root/str(rule['source']); target=safe_target(str(rule['target'])); key=target.as_posix()
        if not source.is_file(): raise PublicSurfaceError(f'public template missing: {source}')
        if key in classifications: raise PublicSurfaceError(f'duplicate public target: {key}')
        dest=staging/target; dest.parent.mkdir(parents=True,exist_ok=True); shutil.copy2(source,dest)
        classifications[key]=str(rule['classification'])
    return classifications


def verify_domain_projects(staging:Path, policy:dict)->None:
    """Validate every selected current Domain; zero Domains is a valid workspace."""
    seen_names=set(); seen_codes=set()
    for project in sorted(p for p in staging.glob('cpf-*') if p.is_dir()):
        contract=project/'gradle.properties'; values=read_properties(contract)
        if values.get('cpf.domain.contractVersion')!='1': continue
        name=values.get('cpf.domain.name',''); code=values.get('cpf.domain.systemCode','')
        if project.name!=f'cpf-{name}' or not code: raise PublicSurfaceError(f'public Domain Developer contract mismatch: {contract}')
        if name in seen_names or code in seen_codes: raise PublicSurfaceError(f'duplicate public Domain identity: {name}/{code}')
        seen_names.add(name); seen_codes.add(code)
        for forbidden in ('cpf-domain.yaml','cpf-generator.lock.json'):
            if (project/forbidden).exists(): raise PublicSurfaceError(f'forbidden Generator metadata in public Domain: {project.name}/{forbidden}')

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
    domains=[]
    for project in sorted(p for p in staging.glob('cpf-*') if p.is_dir()):
        values=read_properties(project/'gradle.properties')
        if values.get('cpf.domain.contractVersion')=='1' and values.get('cpf.domain.generationMode','generated')!='prebuilt': domains.append(project)
    for project in domains:
        run([str(gradlew),'-p',str(project),'clean','build','--no-daemon'],staging)
    if include_backoffice and (staging/'cpf-backoffice-web').is_dir():
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
    # Project the canonical developer capability taxonomy into the customer workspace config.
    catalog_path=root/'cpf-tools/generator/contracts/cpf-starter-catalog.json'
    workspace_cfg=staging/'config/cpf-workspace.properties'
    if catalog_path.is_file() and workspace_cfg.is_file():
        catalog=load_json(catalog_path)
        capabilities=[str(row.get('id','')).strip() for row in catalog.get('developerCapabilities',[]) if str(row.get('id','')).strip()]
        text=workspace_cfg.read_text(encoding='utf-8-sig')
        line='cpf.workspace.capabilities='+','.join(capabilities)
        if re.search(r'(?m)^cpf[.]workspace[.]capabilities=.*$',text): text=re.sub(r'(?m)^cpf[.]workspace[.]capabilities=.*$',line,text)
        else: text=text.rstrip()+'\n'+line+'\n'
        workspace_cfg.write_text(text,encoding='utf-8',newline='\n')
    verify_domain_projects(staging,policy)
    rows=verify_staging(staging,policy,classifications)
    write_metadata(staging,rows,policy,source_identity,include_backoffice)
    # Re-scan metadata explicitly under release classification without weakening initial default-deny source copy.
    for p in (staging/'.cpf-public').iterdir(): classifications[rel(staging,p)]='PUBLIC_RELEASE_METADATA'
    final_rows=verify_staging(staging,policy,classifications)
    if verify_build: verify_builds(staging,include_backoffice)
    result={'status':'PASS','fileCount':len(final_rows),'includeBackoffice':include_backoffice,'staging':str(staging),'sourceIdentity':source_identity}
    # READY.json is copied into the public projection and must remain portable.
    # Keep the local staging path only in the in-process return value.
    public_ready={key:value for key,value in result.items() if key!='staging'}
    (staging/'.cpf-public/READY.json').write_text(json.dumps(public_ready,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    verify_domain_projects(staging,policy)
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
