#!/usr/bin/env python3
from __future__ import annotations
import csv,json,re,sys,subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
FAIL=[]; INFO={}

def fail(msg): FAIL.append(msg)
def text(p): return p.read_text(encoding='utf-8-sig',errors='ignore')

def _fallback_product_file(p:Path)->bool:
    """Keep repository source/config files while excluding generated/external work trees.

    `cpf-tools/build/**` is an intentional product-source path and must never be dropped
    merely because a path component is named `build`.
    """
    try:
        parts=p.relative_to(ROOT).parts
    except ValueError:
        return False
    if any(part in {'.git','.gradle','node_modules','__pycache__','.idea','.vscode','coverage'} for part in parts):
        return False
    if 'build' in parts and not (len(parts)>=2 and parts[0]=='cpf-tools' and parts[1]=='build'):
        return False
    if 'dist' in parts and parts[0] in {'cpf-admin','cpf-backoffice-web'}:
        return False
    return True

def _git_product_paths()->set[str]|None:
    """Use Git's tracked + untracked/non-ignored view when the verifier runs in a real checkout."""
    try:
        cp=subprocess.run(
            ['git','-C',str(ROOT),'ls-files','-co','--exclude-standard','-z'],
            stdout=subprocess.PIPE, stderr=subprocess.DEVNULL, check=True
        )
        return {x.decode('utf-8','surrogateescape').replace('\\','/') for x in cp.stdout.split(b'\0') if x}
    except Exception:
        return None

_GIT_PRODUCT_PATHS=_git_product_paths()

def product_file(p:Path)->bool:
    if not p.is_file():
        return False
    try:
        rel=p.relative_to(ROOT).as_posix()
    except ValueError:
        return False
    if _GIT_PRODUCT_PATHS is not None:
        return rel in _GIT_PRODUCT_PATHS
    return _fallback_product_file(p)

def product_files(base:Path, pattern:str='*'):
    if not base.exists():
        return []
    return [p for p in base.rglob(pattern) if product_file(p)]
manifest=ROOT/'cpf-docs/deliverables/DELETE_MANIFEST.csv'
entries=[]; approved_entries=[]; pending_entries=[]; user_approved_entries=[]
if not manifest.is_file(): fail('DELETE_MANIFEST missing')
else:
    try:
        with manifest.open(encoding='utf-8-sig', newline='') as h:
            reader=csv.DictReader(h)
            fields=set(reader.fieldnames or [])
            rows=list(reader)
        required={'path','reason','approved','precondition','lifecycle','user_execution_required','replacement_path','user_approved','user_approval_ref','user_approved_at'}
        if not required.issubset(fields):
            fail('DELETE_MANIFEST schema requires '+','.join(sorted(required)))
        for n,row in enumerate(rows,2):
            path=(row.get('path') or '').strip().replace('\\','/')
            if not path: continue
            if any(x in path for x in '*?['): fail(f'DELETE_MANIFEST wildcard:{n}:{path}')
            if path.startswith(('/', '\\')) or '..' in Path(path).parts: fail(f'DELETE_MANIFEST unsafe:{n}:{path}')
            entries.append(path)
            approved=(row.get('approved') or '').strip().lower() in {'true','1','yes','y'}
            precondition=(row.get('precondition') or '').strip()
            lifecycle=(row.get('lifecycle') or '').strip()
            user_required=(row.get('user_execution_required') or '').strip().lower() in {'true','1','yes','y'}
            replacement=(row.get('replacement_path') or '').strip().replace('\\','/')
            user_approved=(row.get('user_approved') or '').strip().lower() in {'true','1','yes','y'}
            user_approval_ref=(row.get('user_approval_ref') or '').strip()
            user_approved_at=(row.get('user_approved_at') or '').strip()
            if approved: approved_entries.append(path)
            if user_approved:
                user_approved_entries.append(path)
                if not user_approval_ref or not user_approved_at:
                    fail(f'DELETE_MANIFEST user approval metadata incomplete:{n}:{path}')
            if lifecycle=='PENDING_USER_EXECUTION':
                pending_entries.append(path)
                if not approved or precondition!='SATISFIED' or not user_required:
                    fail(f'DELETE_MANIFEST pending row must be approved + SATISFIED + user execution required:{n}:{path}')
                # Direct replacement_path may itself be a retired intermediate path.
                # Canonical migration closure owns transitive replacement resolution and SHA validation;
                # duplicating a direct-file existence check here creates a stale false failure after currentization.
            elif lifecycle=='HISTORICAL_ALREADY_ABSENT':
                if (ROOT/path).exists(): fail(f'historical delete path unexpectedly exists:{path}')
            else:
                fail(f'DELETE_MANIFEST unknown lifecycle:{n}:{lifecycle}:{path}')
    except Exception as e:
        fail(f'DELETE_MANIFEST parse:{e}')
    if len(entries)!=len(set(entries)): fail('DELETE_MANIFEST duplicate path')
INFO['deleteManifestCount']=len(entries)
INFO['approvedDeleteManifestCount']=len(approved_entries)
INFO['pendingUserExecutionDeleteCount']=len(pending_entries)
INFO['userApprovedDeleteCount']=len(user_approved_entries)
# Internal approval is not equivalent to a deletion already applied to the source tree.
# Final source verification therefore inspects the physical tree only; pending paths must
# be absent in a Fresh Replay before the Canonical Final Gate can PASS.
def survives(p:Path)->bool:
    return p.is_file()

# EDU exact 35 is a physical first-level feature-package contract.
base=ROOT/'cpf-education/src/main/java/com/cpf/education'
online_expected={'basiccrud','querypaging','common','validation','internalservice','domaincall','externalrest','fixedlength','transactionrequired','transactionrequiresnew','externalsideeffect','ondemandbatch','centercut','cache','messaging','file','securityaudit','recovery','concurrency','webhook'}
batch_expected={'tasklet','chunk','flatfile','partition','centercut','scheduler','restart','distributedworker','shellcommand','conditionalflow','chunktransaction','requiresnew','steptransaction','externalcall','ondemand'}
def physical_groups(category_root:Path)->set[str]:
    result=set()
    if category_root.is_dir():
        for d in category_root.iterdir():
            if d.is_dir() and any(survives(p) for p in product_files(d,'*.java')):
                result.add(d.name)
    return result
online_features=physical_groups(base/'online')
batch_features=physical_groups(base/'batch')
INFO['eduOnline']=len(online_features); INFO['eduBatch']=len(batch_features)
if online_features!=online_expected: fail(f'EDU online physical groups mismatch missing={sorted(online_expected-online_features)} extra={sorted(online_features-online_expected)}')
if batch_features!=batch_expected: fail(f'EDU batch physical groups mismatch missing={sorted(batch_expected-batch_features)} extra={sorted(batch_features-batch_expected)}')
# surviving EDU root dirs (logical after manifest)
roots=[]
if base.is_dir():
    for d in base.iterdir():
        if not d.is_dir(): continue
        if any(survives(p) for p in product_files(d)): roots.append(d.name)
if set(roots)!={'online','batch'}: fail(f'EDU final package roots={sorted(roots)}')
# legacy ids in surviving EDU only
legacy=[re.compile(r'EDU-DEV-\d+'),re.compile(r'EDU-BAT-\d+'),re.compile(r'EDU-ADM-\d+'),re.compile(r'EDU-BZA-\d+'),re.compile(r'EDU-GW-\d+'),re.compile(r'EDU-OPS-\d+')]
for p in product_files(ROOT/'cpf-education'):
    if not survives(p) or p.stat().st_size>2_000_000: continue
    t=text(p)
    for pat in legacy:
        if pat.search(t): fail(f'legacy EDU id:{p.relative_to(ROOT)}:{pat.pattern}')
# canonical catalog
cat=ROOT/'cpf-tools/governance/cpf-edu-executable-catalog.json'
try:
    d=json.loads(text(cat))
    if (d.get('featureCount'),d.get('onlineCount'),d.get('batchCount'))!=(35,20,15): fail('EDU governance catalog count mismatch')
except Exception as e: fail(f'EDU governance catalog parse:{e}')
# public Java file name + duplicate product FQCN
fq={}; mismatch=[]
exclude_prefix=('cpf-tools/verification/java21/','cpf-tools/runtime/tools/tests/runtime-fixtures/')
for p in product_files(ROOT,'*.java'):
    if not survives(p): continue
    rp=p.relative_to(ROOT).as_posix(); t=text(p)
    pm=re.search(r'(?m)^\s*package\s+([\w.]+)\s*;',t)
    cm=re.search(r'(?m)^\s*public\s+(?:abstract\s+|final\s+|sealed\s+|non-sealed\s+)?(?:class|interface|record|enum|@interface)\s+(\w+)',t)
    if cm and p.name!=cm.group(1)+'.java': mismatch.append(f'{rp}:{cm.group(1)}')
    if pm and cm and not rp.startswith(exclude_prefix): fq.setdefault(pm.group(1)+'.'+cm.group(1),[]).append(rp)
if mismatch: fail('public Java filename mismatch:'+str(mismatch[:20]))
dups={k:v for k,v in fq.items() if len(v)>1}
if dups: fail('duplicate public FQCN:'+str(list(dups.items())[:20]))
# annotation single definition
anns=[p for p in product_files(ROOT,'CpfOnlineTransaction.java') if survives(p)]
if len(anns)!=1: fail(f'CpfOnlineTransaction definitions={len(anns)}')
# method-level operation parity. Annotation ownership stops at the previous member/method boundary;
# never scan an arbitrary 20-line window because that double-counts the prior method annotation.
pairs=0;mism=[];pair_ids=[]
method_re=re.compile(r'\b(public|protected)\s+[\w<>, ?\[\].]+\s+\w+\s*\(')
for p in product_files(ROOT,'*.java'):
    if not survives(p): continue
    lines=text(p).splitlines()
    previous_method=-1
    for i,line in enumerate(lines):
        if not method_re.search(line): continue
        block='\n'.join(lines[previous_method+1:i])
        previous_method=i
        a=re.findall(r'@CpfOnlineTransaction\s*\((.*?)\)',block,re.S)
        o=re.findall(r'@Operation\s*\((.*?)\)',block,re.S)
        if not (a and o): continue
        am=re.search(r'operationId\s*=\s*"([^"]+)"',a[-1]); om=re.search(r'operationId\s*=\s*"([^"]+)"',o[-1])
        if am and om:
            pairs+=1; pair_ids.append(am.group(1))
            if am.group(1)!=om.group(1): mism.append((p.relative_to(ROOT).as_posix(),am.group(1),om.group(1)))
INFO['operationPairs']=pairs; INFO['uniqueOperationIds']=len(set(pair_ids))
if len(pair_ids)!=len(set(pair_ids)):
    dup=sorted({x for x in pair_ids if pair_ids.count(x)>1})
    fail('duplicate paired operationId:'+str(dup[:20]))
if mism: fail('operationId/OpenAPI mismatch:'+str(mism[:20]))
# management boundary
for mod in ('cpf-admin','cpf-gateway'):
    tx=[]; internal=[]
    for p in product_files(ROOT/mod,'*.java'):
        if not survives(p): continue
        t=text(p)
        if re.search(r'(?m)^\s*@CpfOnlineTransaction\b',t): tx.append(p.relative_to(ROOT).as_posix())
        for imp in re.findall(r'(?m)^\s*import\s+(com\.cpf\.core\.[\w.]+);',t):
            if '.internal.' in imp: internal.append((p.relative_to(ROOT).as_posix(),imp))
    if tx: fail(f'{mod} management @CpfOnlineTransaction:{tx[:20]}')
    if internal: fail(f'{mod} core.internal import:{internal[:20]}')
# instance canonical keys
for p in product_files(ROOT/'deploy')+product_files(ROOT/'cpf-tools'):
    if not survives(p) or p.stat().st_size>2_000_000: continue
    if p.suffix.lower() not in {'.py','.ps1','.sh','.cmd','.bat','.env','.yml','.yaml','.json','.properties','.md'}: continue
    legacy_key='CPF_'+'INSTANCE_ID'
    if legacy_key in text(p): fail(f'legacy runtime instance env key:{p.relative_to(ROOT)}')
# Generated/Customer Domain IA — current Developer Contract discovery; zero selected domains is valid.
def _domain_props(path):
    out={}
    for raw in path.read_text(encoding='utf-8-sig').splitlines():
        line=raw.strip()
        if not line or line.startswith('#') or '=' not in line: continue
        k,v=line.split('=',1); out[k.strip()]=v.strip()
    return out
selected_domains=[]
for project in sorted(p for p in ROOT.glob('cpf-*') if p.is_dir()):
    gp=project/'gradle.properties'
    if not gp.is_file(): continue
    props=_domain_props(gp)
    if props.get('cpf.domain.contractVersion')!='1': continue
    selected_domains.append(project.name)
    name=props.get('cpf.domain.name',''); code=props.get('cpf.domain.systemCode','')
    if project.name!=f'cpf-{name}' or not code: fail(f'invalid Domain Developer contract:{project.name}')
    online=props.get('cpf.domain.online','true').lower()=='true'; batch=props.get('cpf.domain.batch','false').lower()=='true'
    if online and not (project/'online').is_dir(): fail(f'{project.name} online selected but missing')
    if (not online) and (project/'online').exists() and product_files(project/'online'): fail(f'{project.name} online not selected but source exists')
    if batch and not (project/'batch').is_dir(): fail(f'{project.name} batch selected but missing')
    if (not batch) and (project/'batch').exists() and product_files(project/'batch'): fail(f'{project.name} batch not selected but source exists')
INFO['selectedDomainCount']=len(selected_domains)
INFO['selectedDomains']=selected_domains
# Generator current policy descriptions
for rel in ('cpf-tools/generator/contracts/cpf-domain.schema.json','cpf-docs/development/GENERATOR_GUIDE.md','cpf-docs/governance/development-harness/product/CPF_PRODUCT_ARCHITECTURE_AND_REQUIREMENTS.md'):
    t=text(ROOT/rel)
    bad=('Batch는 Generator가 만들지','Generated Runtime은 `online/` 하나','Batch는 Generated Domain 산출물이 아님','online 업무 Source만 생성')
    for b in bad:
        if b in t: fail(f'stale Generated Domain policy:{rel}:{b}')
# Spring/Java wiring hygiene (IDE/static contract that must stay warning-free).
_hygiene = ROOT/'cpf-tools/verification/verify_spring_java_hygiene.py'
if _hygiene.is_file():
    cp=subprocess.run([sys.executable,str(_hygiene)],cwd=ROOT,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
    if cp.returncode!=0:
        fail('Spring Java hygiene:'+cp.stdout.strip().replace('\n',' | '))
else:
    fail('Spring Java hygiene verifier missing')

# JSON parse current product/config; runtime output blobs excluded
for p in product_files(ROOT,'*.json'):
    rp=p.relative_to(ROOT).as_posix()
    if not survives(p) or rp.startswith('cpf-tools/environment/docker-development-test/output/'): continue
    try: json.loads(text(p))
    except Exception as e: fail(f'JSON parse:{rp}:{e}')
print(json.dumps({'status':'PASS' if not FAIL else 'FAIL','info':INFO,'failures':FAIL},ensure_ascii=False,indent=2))
sys.exit(0 if not FAIL else 1)
