#!/usr/bin/env python3
import json,re,sys
from pathlib import Path
H=Path(__file__).resolve().parents[1]; ROOT=H.parents[2]; ERR=[]
def fail(m): ERR.append(m)
def load(p):
    try:return json.loads(p.read_text(encoding='utf-8'))
    except Exception as e: fail(f'invalid json {p}: {e}'); return {}
def parse_set(text,name):
    m=re.search(rf'private static final Set<String> {name} = Set\.of\((.*?)\);',text,re.S)
    if not m: fail('CpfCli set missing '+name); return []
    return re.findall(r'"([^"]+)"',m.group(1))
base=load(H/'SOURCE_BASELINE.json'); inv=load(H/'SOURCE_CAPABILITY_INVENTORY.json')
# Current product source identity is read from Source, never hardcoded here.
sidp=ROOT/'cpf-docs/work/current/SOURCE_IDENTITY.json'
if not sidp.is_file(): fail('current SOURCE_IDENTITY.json missing')
else:
    sid=load(sidp); cur=str(sid.get('currentSource',{}).get('contentSha256','')).upper()
    if not cur: fail('current source identity empty')
    if str(base.get('productSourceIdentity','')).upper()!=cur: fail('SOURCE_BASELINE product identity drift')
    if str(inv.get('productSourceIdentity','')).upper()!=cur: fail('SOURCE_CAPABILITY_INVENTORY product identity drift')
# CLI surface is parsed from canonical Java source at runtime.
clips=list(ROOT.rglob('CpfCli.java')); clip=clips[0] if len(clips)==1 else None
if clip is None: fail('current CpfCli.java authority could not be uniquely discovered')
else:
    cli=clip.read_text(encoding='utf-8'); pub=parse_set(cli,'PUBLIC'); internal=parse_set(cli,'INTERNAL_NAMESPACES')
    rec=base.get('actualCliSurface',{})
    if pub!=list(rec.get('publicCommands',[])): fail(f'public CLI drift current={pub} baseline={rec.get("publicCommands")}')
    if internal!=list(rec.get('internalNamespaces',[])): fail('internal CLI namespace drift')
# Starter/public surface is source-derived.
cats=list(ROOT.rglob('cpf-starter-catalog.json')); catp=cats[0] if len(cats)==1 else None
if catp is None: fail('current starter catalog could not be uniquely discovered')
else:
    cat=load(catp); cur=list(cat.get('publicDeveloperArtifacts',[]))
    recorded=[x.get('artifactId') for x in base.get('publicUserSelectableArtifacts',[]) if isinstance(x,dict)]
    if cur!=recorded: fail(f'publicDeveloperArtifacts drift current={len(cur)} baseline={len(recorded)}')
    if list(cat.get('publicProfiles',[]))!=list(base.get('publicProfiles',[])): fail('publicProfiles drift')
    cg=[x.get('id') if isinstance(x,dict) else x for x in cat.get('capabilityGroups',[])]
    if cg!=list(base.get('capabilityGroups',[])): fail('capabilityGroups drift')
# Inventory sample basenames must exist in current source.
java_by_name={}
for p in ROOT.rglob('*.java'): java_by_name[p.name]=java_by_name.get(p.name,0)+1
refs=0
for category in inv.get('apiPackageCategories',[]):
    for s in category.get('samples',[]):
        refs+=1
        if s not in java_by_name: fail(f'inventory sample missing: {category.get("category")}/{s}')
if ERR:
    print('SOURCE_ALIGNMENT=FAIL COUNT='+str(len(ERR))); [print('-',e) for e in ERR]; raise SystemExit(1)
print('SOURCE_ALIGNMENT=PASS'); print('INVENTORY_SAMPLE_REFS='+str(refs)); print('PUBLIC_DEVELOPER_ARTIFACTS='+str(len(base.get('publicUserSelectableArtifacts',[]))))
