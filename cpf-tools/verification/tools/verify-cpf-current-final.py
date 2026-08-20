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
entries=[]
if not manifest.is_file(): fail('DELETE_MANIFEST missing')
else:
    try:
        with manifest.open(encoding='utf-8-sig', newline='') as h:
            rows=list(csv.DictReader(h))
        if not rows or 'path' not in (rows[0].keys() if rows else []):
            fail('DELETE_MANIFEST schema requires path column')
        for n,row in enumerate(rows,2):
            s=(row.get('path') or '').strip()
            if not s: continue
            if any(x in s for x in '*?['): fail(f'DELETE_MANIFEST wildcard:{n}:{s}')
            if s.startswith(('/', '\\')) or '..' in Path(s).parts: fail(f'DELETE_MANIFEST unsafe:{n}:{s}')
            entries.append(s)
    except Exception as e:
        fail(f'DELETE_MANIFEST parse:{e}')
    if len(entries)!=len(set(entries)): fail('DELETE_MANIFEST duplicate path')
    protected=('cpf-docs/deliverables/','cpf-docs/guides/','cpf-docs/environment/docker/','cpf-tools/environment/docker-development-test/')
    for s in entries:
        if s.startswith(protected): fail(f'protected delete path:{s}')
INFO['deleteManifestCount']=len(entries)
deleted=set(entries)

def survives(p:Path)->bool:
    try:r=p.relative_to(ROOT).as_posix()
    except ValueError:return False
    return r not in deleted
# EDU exact 35 is feature-package based. Numeric flat Example classes are legacy and must be removed via manifest.
base=ROOT/'cpf-education/src/main/java/com/cpf/education'
online_expected={'basiccrud','querypaging','common','validation','internalservice','domaincall','externalrest','fixedlength','transaction','externalsideeffect','ondemandbatch','centercut','cache','messaging','file','securityaudit','recovery','concurrency','webhook'}
# transaction owns two canonical feature subpackages: required + requiresnew. Count those separately.
online_features=[]
if (base/'online').is_dir():
    for name in sorted(online_expected-{'transaction'}):
        d=base/'online'/name
        if d.is_dir() and any(survives(p) for p in product_files(d,'*.java')): online_features.append(name)
    for name in ('required','requiresnew'):
        d=base/'online'/'transaction'/name
        if d.is_dir() and any(survives(p) for p in product_files(d,'*.java')): online_features.append('transaction.'+name)
batch_expected={'tasklet','chunk','flatfile','partition','centercut','scheduler','restart','distributedworker','shellcommand','conditionalflow','chunktransaction','requiresnew','steptransaction','externalcall','ondemand'}
batch_features=[]
if (base/'batch').is_dir():
    for name in sorted(batch_expected):
        d=base/'batch'/name
        if d.is_dir() and any(survives(p) for p in product_files(d,'*.java')): batch_features.append(name)
INFO['eduOnline']=len(online_features); INFO['eduBatch']=len(batch_features)
if len(online_features)!=20 or len(batch_features)!=15: fail(f'EDU count online={len(online_features)} batch={len(batch_features)}')
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
# generated IA
if not (ROOT/'cpf-member/online').is_dir() or not (ROOT/'cpf-member/batch').is_dir(): fail('cpf-member must contain online+batch')
if not (ROOT/'cpf-external/online').is_dir(): fail('cpf-external online missing')
if (ROOT/'cpf-external/batch').exists() and product_files(ROOT/'cpf-external/batch'): fail('cpf-external batch must be absent for batch=false fixture')
# Generator current policy descriptions
for rel in ('cpf-tools/generator/contracts/cpf-domain.schema.json','cpf-docs/development/GENERATOR_GUIDE.md','cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md'):
    t=text(ROOT/rel)
    bad=('Batch는 Generator가 만들지','Generated Runtime은 `online/` 하나','Batch는 Generated Domain 산출물이 아님','online 업무 Source만 생성')
    for b in bad:
        if b in t: fail(f'stale Generated Domain policy:{rel}:{b}')
# JSON parse current product/config; runtime output blobs excluded
for p in product_files(ROOT,'*.json'):
    rp=p.relative_to(ROOT).as_posix()
    if not survives(p) or rp.startswith('cpf-tools/environment/docker-development-test/output/'): continue
    try: json.loads(text(p))
    except Exception as e: fail(f'JSON parse:{rp}:{e}')
print(json.dumps({'status':'PASS' if not FAIL else 'FAIL','info':INFO,'failures':FAIL},ensure_ascii=False,indent=2))
sys.exit(0 if not FAIL else 1)
