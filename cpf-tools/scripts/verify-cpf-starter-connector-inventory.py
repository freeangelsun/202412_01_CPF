#!/usr/bin/env python3
"""Validate exact-SHA GitHub connector starter inventory without claiming local build/runtime coverage."""
from __future__ import annotations
import argparse,json,re,sys
from pathlib import Path
class GateError(RuntimeError): pass
def verify(root:Path, inventory:Path, expected_sha:str)->dict:
    catalog_path=root/'cpf-tools/generator/contracts/cpf-starter-catalog.json'
    mirror=root/'cpf-tools/config/cpf-starter-catalog.json'
    if not catalog_path.is_file() or not mirror.is_file(): raise GateError('starter catalog or mirror missing')
    if catalog_path.read_bytes()!=mirror.read_bytes(): raise GateError('starter catalog mirror drift')
    catalog=json.loads(catalog_path.read_text(encoding='utf-8-sig'))
    data=json.loads(inventory.read_text(encoding='utf-8-sig'))
    if data.get('status')!='PASS': raise GateError('inventory status is not PASS')
    if data.get('baselineSha')!=expected_sha: raise GateError('inventory baseline mismatch')
    prov=data.get('provenance') or {}
    if not prov.get('tree_unchanged') or prov.get('current_baseline_sha')!=expected_sha:
        raise GateError('exact-SHA tree provenance missing')
    if prov.get('inventory_tree_sha')!=prov.get('current_tree_sha') or data.get('starterTreeSha')!=prov.get('current_tree_sha'):
        raise GateError('starter tree SHA mismatch')
    modules=catalog.get('modules') or []
    inv=data.get('modules') or []
    expected={m.get('ownerPath') or m.get('physicalPath') or m.get('path') for m in modules}
    actual={m.get('ownerPath') for m in inv}
    if None in expected or expected!=actual: raise GateError(f'module path mismatch missing={sorted(expected-actual)} extra={sorted(actual-expected)}')
    if data.get('catalogModuleCount')!=len(modules) or data.get('inventoryModuleCount')!=len(inv): raise GateError('module count mismatch')
    if catalog.get('baselinePolicy')!='GIT_HEAD_RUNTIME' or catalog.get('baselineSha')!='RUNTIME_GIT_HEAD': raise GateError('catalog baseline policy is stale')
    for row in inv:
        owner=row['ownerPath']; build=row.get('buildFile',''); blob=row.get('blobSha','')
        if build!=f'{owner}/build.gradle': raise GateError(f'{owner}: build file mismatch')
        if not re.fullmatch(r'[0-9a-f]{40}',blob): raise GateError(f'{owner}: invalid blob SHA')
    return {'status':'PASS','baselineSha':expected_sha,'starterTreeSha':data['starterTreeSha'],'moduleCount':len(modules),'physicalBuildFilesCheckedViaConnector':True,'localPackageScan':False,'localGradleBuild':False}
def main()->int:
    p=argparse.ArgumentParser();p.add_argument('--root',default='.');p.add_argument('--inventory',required=True);p.add_argument('--expected-sha',required=True);a=p.parse_args()
    try:r=verify(Path(a.root).resolve(),Path(a.inventory).resolve(),a.expected_sha);code=0
    except Exception as e:r={'status':'FAIL','message':str(e)};code=1
    print(json.dumps(r,ensure_ascii=False));return code
if __name__=='__main__': raise SystemExit(main())
