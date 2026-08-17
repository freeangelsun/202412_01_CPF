#!/usr/bin/env python3
from __future__ import annotations
import json,re,sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
FAIL=[]; INFO={}

def fail(msg): FAIL.append(msg)
def text(p): return p.read_text(encoding='utf-8-sig',errors='ignore')
manifest=ROOT/'cpf-docs/work/current/DELETE_MANIFEST.txt'
entries=[]
if not manifest.is_file(): fail('DELETE_MANIFEST missing')
else:
    for n,line in enumerate(text(manifest).splitlines(),1):
        s=line.strip()
        if not s or s.startswith('#'): continue
        if any(x in s for x in '*?['): fail(f'DELETE_MANIFEST wildcard:{n}:{s}')
        if s.startswith(('/', '\\')) or '..' in Path(s).parts: fail(f'DELETE_MANIFEST unsafe:{n}:{s}')
        entries.append(s)
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
# EDU exact 35 in final state
base=ROOT/'cpf-education/src/main/java/com/cpf/education'
on=[p for p in (base/'online').glob('Online??*Example.java') if survives(p)] if (base/'online').is_dir() else []
ba=[p for p in (base/'batch').glob('Batch??*Example.java') if survives(p)] if (base/'batch').is_dir() else []
INFO['eduOnline']=len(on); INFO['eduBatch']=len(ba)
if len(on)!=20 or len(ba)!=15: fail(f'EDU count online={len(on)} batch={len(ba)}')
# surviving EDU root dirs (logical after manifest)
roots=[]
if base.is_dir():
    for d in base.iterdir():
        if not d.is_dir(): continue
        if any(survives(p) for p in d.rglob('*') if p.is_file()): roots.append(d.name)
if set(roots)!={'online','batch'}: fail(f'EDU final package roots={sorted(roots)}')
# legacy ids in surviving EDU only
legacy=[re.compile(r'EDU-DEV-\d+'),re.compile(r'EDU-BAT-\d+'),re.compile(r'EDU-ADM-\d+'),re.compile(r'EDU-BZA-\d+'),re.compile(r'EDU-GW-\d+'),re.compile(r'EDU-OPS-\d+')]
for p in (ROOT/'cpf-education').rglob('*'):
    if not p.is_file() or not survives(p) or p.stat().st_size>2_000_000: continue
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
for p in ROOT.rglob('*.java'):
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
anns=[p for p in ROOT.rglob('CpfOnlineTransaction.java') if survives(p)]
if len(anns)!=1: fail(f'CpfOnlineTransaction definitions={len(anns)}')
# method-level operation parity
pairs=0;mism=[]
for p in ROOT.rglob('*.java'):
    if not survives(p): continue
    lines=text(p).splitlines()
    for i,line in enumerate(lines):
        if re.search(r'\b(public|protected)\s+[\w<>, ?\[\].]+\s+\w+\s*\(',line):
            block='\n'.join(lines[max(0,i-20):i])
            a=re.findall(r'@CpfOnlineTransaction\s*\((.*?)\)',block,re.S); o=re.findall(r'@Operation\s*\((.*?)\)',block,re.S)
            if a and o:
                am=re.search(r'operationId\s*=\s*"([^"]+)"',a[-1]); om=re.search(r'operationId\s*=\s*"([^"]+)"',o[-1])
                if am and om:
                    pairs+=1
                    if am.group(1)!=om.group(1): mism.append((p.relative_to(ROOT).as_posix(),am.group(1),om.group(1)))
INFO['operationPairs']=pairs
if mism: fail('operationId/OpenAPI mismatch:'+str(mism[:20]))
# management boundary
for mod in ('cpf-admin','cpf-biz-admin','cpf-gateway'):
    tx=[]; internal=[]
    for p in (ROOT/mod).rglob('*.java'):
        if not survives(p): continue
        t=text(p)
        if re.search(r'(?m)^\s*@CpfOnlineTransaction\b',t): tx.append(p.relative_to(ROOT).as_posix())
        for imp in re.findall(r'(?m)^\s*import\s+(com\.cpf\.core\.[\w.]+);',t):
            if '.internal.' in imp: internal.append((p.relative_to(ROOT).as_posix(),imp))
    if tx: fail(f'{mod} management @CpfOnlineTransaction:{tx[:20]}')
    if internal: fail(f'{mod} core.internal import:{internal[:20]}')
# instance canonical keys
for p in list((ROOT/'deploy').rglob('*'))+list((ROOT/'cpf-tools').rglob('*')):
    if not p.is_file() or not survives(p) or p.stat().st_size>2_000_000: continue
    if p.suffix.lower() not in {'.py','.ps1','.sh','.cmd','.bat','.env','.yml','.yaml','.json','.properties','.md'}: continue
    legacy_key='CPF_'+'INSTANCE_ID'
    if legacy_key in text(p): fail(f'legacy runtime instance env key:{p.relative_to(ROOT)}')
# generated IA
if not (ROOT/'cpf-member/online').is_dir() or not (ROOT/'cpf-member/batch').is_dir(): fail('cpf-member must contain online+batch')
if not (ROOT/'cpf-external/online').is_dir(): fail('cpf-external online missing')
if (ROOT/'cpf-external/batch').exists() and any((ROOT/'cpf-external/batch').rglob('*')): fail('cpf-external batch must be absent for batch=false fixture')
# Generator current policy descriptions
for rel in ('cpf-tools/generator/contracts/cpf-domain.schema.json','cpf-docs/development/GENERATOR_GUIDE.md','cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md'):
    t=text(ROOT/rel)
    bad=('Batch는 Generator가 만들지','Generated Runtime은 `online/` 하나','Batch는 Generated Domain 산출물이 아님','online 업무 Source만 생성')
    for b in bad:
        if b in t: fail(f'stale Generated Domain policy:{rel}:{b}')
# JSON parse current product/config; runtime output blobs excluded
for p in ROOT.rglob('*.json'):
    rp=p.relative_to(ROOT).as_posix()
    if not survives(p) or rp.startswith('cpf-tools/environment/docker-development-test/output/'): continue
    try: json.loads(text(p))
    except Exception as e: fail(f'JSON parse:{rp}:{e}')
print(json.dumps({'status':'PASS' if not FAIL else 'FAIL','info':INFO,'failures':FAIL},ensure_ascii=False,indent=2))
sys.exit(0 if not FAIL else 1)
