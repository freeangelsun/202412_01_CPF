#!/usr/bin/env python3
import json,sys
from pathlib import Path
H=Path(__file__).resolve().parents[1]
ROOT=H.parents[2]
ERR=[]
def fail(m): ERR.append(m)
def load(p):
    try:return json.loads(p.read_text(encoding='utf-8'))
    except Exception as e: fail(f'invalid json {p}: {e}'); return {}
base=load(H/'SOURCE_BASELINE.json')
inv=load(H/'SOURCE_CAPABILITY_INVENTORY.json')
catp=ROOT/'cpf-tools/generator/contracts/cpf-starter-catalog.json'
if not catp.is_file(): fail('starter catalog missing')
else:
    cat=load(catp)
    cur=list(cat.get('publicDeveloperArtifacts',[]))
    recorded=[x.get('artifactId') for x in base.get('publicUserSelectableArtifacts',[]) if isinstance(x,dict)]
    if cur!=recorded: fail(f'publicDeveloperArtifacts drift current={len(cur)} baseline={len(recorded)} missing={sorted(set(cur)-set(recorded))} extra={sorted(set(recorded)-set(cur))}')
    cp=list(cat.get('publicProfiles',[])); bp=list(base.get('publicProfiles',[]))
    if cp!=bp: fail('publicProfiles drift')
    cg=[x.get('id') if isinstance(x,dict) else x for x in cat.get('capabilityGroups',[])]
    if cg!=list(base.get('capabilityGroups',[])): fail('capabilityGroups drift')
# Inventory sample basenames must still exist in actual source.
java_by_name={}
for p in ROOT.rglob('*.java'):
    java_by_name.setdefault(p.name,0); java_by_name[p.name]+=1
refs=0
for c in inv.get('apiPackageCategories',[]):
    for s in c.get('samples',[]):
        refs+=1
        if s not in java_by_name: fail(f'inventory sample missing: {c.get("category")}/{s}')
# Canonical source identity must be current supplied ZIP identity, not stale earlier session.
expected_name='CPF_FULL_SOURCE_FOR_NEXT_QA_20260828_191735.zip'
expected_sha='34D692419F7701EBC58439B00F0A5111DBBE629BC8C25F46ED17DB875D4E3EA5'
if base.get('sourceZip')!=expected_name or str(base.get('sha256','')).upper()!=expected_sha: fail('SOURCE_BASELINE stale source identity')
if inv.get('sourceZip')!=expected_name or str(inv.get('sourceSha256','')).upper()!=expected_sha: fail('SOURCE_CAPABILITY_INVENTORY stale source identity')
# Required documentation paths/canonical requirements/catalog must exist.
for rel in ['cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md','cpf-tools/generator/contracts/cpf-starter-catalog.json','README.md']:
    if not (ROOT/rel).is_file(): fail('required source missing '+rel)
if ERR:
    print('SOURCE_ALIGNMENT=FAIL COUNT='+str(len(ERR)))
    for e in ERR: print('-',e)
    raise SystemExit(1)
print('SOURCE_ALIGNMENT=PASS')
print('INVENTORY_SAMPLE_REFS='+str(refs))
print('PUBLIC_DEVELOPER_ARTIFACTS='+str(len(base.get('publicUserSelectableArtifacts',[]))))
