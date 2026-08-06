#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, re, sys
from collections import Counter
from pathlib import Path


def fail(errors, message): errors.append(message)
def main() -> int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--overlay-only',action='store_true'); args=ap.parse_args()
    root=Path(args.root).resolve(); errors=[]
    cat_path=root/'cpf-tools/generator/contracts/cpf-starter-catalog.json'
    if not cat_path.is_file(): print(f'FAIL missing {cat_path}'); return 2
    data=json.loads(cat_path.read_text(encoding='utf-8')); modules=data.get('modules',[]); layout=data.get('targetPhysicalLayout',{})
    if layout.get('moduleCount') != len(modules): fail(errors,f'moduleCount={layout.get("moduleCount")} actual={len(modules)}')
    if layout.get('profileCount') != sum(m.get('visibility')=='public' for m in modules): fail(errors,'profileCount mismatch')
    if layout.get('capabilityGroupCount') != len(data.get('capabilityGroups',[])): fail(errors,'capabilityGroupCount mismatch')
    for field in ('projectPath','ownerPath','artifactId','configPrefix'):
        c=Counter(str(m.get(field,'')) for m in modules); bad=[k for k,v in c.items() if not k or v>1]
        if bad: fail(errors,f'duplicate/blank {field}: {bad}')
    gav=Counter(f"{m.get('groupId')}:{m.get('artifactId')}" for m in modules)
    if any(v>1 for v in gav.values()): fail(errors,'duplicate GAV')
    for m in modules:
        vis=m.get('visibility'); kind=m.get('kind')
        if vis=='public' and (kind!='starter-profile' or m.get('ownerGroup')!='profiles' or m.get('internalRole')!='public-profile'): fail(errors,f'invalid public partition {m.get("artifactId")}')
        if vis=='internal' and kind!='internal-starter': fail(errors,f'invalid internal partition {m.get("artifactId")}')
        path=root/str(m.get('ownerPath'))
        if not (path/'build.gradle').is_file() and not args.overlay_only: fail(errors,f'missing physical module {path}')
    removed=set(data.get('removedArtifactIds',[])); active={m.get('artifactId') for m in modules}
    if removed & active: fail(errors,f'removed artifacts active: {sorted(removed&active)}')
    internal=(root/'cpf-tools/build/platform-bom/internal-bom/build.gradle').read_text(encoding='utf-8')
    public=(root/'cpf-tools/build/platform-bom/public-bom/build.gradle').read_text(encoding='utf-8')
    if re.search(r'internalModules\.size\(\)\s*[!=]=\s*\d+',internal): fail(errors,'hard-coded internal leaf count remains')
    if re.search(r'publicModules\.size\(\)\s*[!=]=\s*\d+',public): fail(errors,'hard-coded public leaf count remains')
    for text,name in ((internal,'internal BOM'),(public,'public BOM')):
        for marker in ('missing','extra','duplicate'):
            if marker not in text: fail(errors,f'{name} lacks {marker} equality check')
    if errors:
        print('\n'.join('FAIL '+e for e in errors)); return 1
    print(f'PASS modules={len(modules)} public={sum(m.get("visibility")=="public" for m in modules)} internal={sum(m.get("visibility")=="internal" for m in modules)}')
    return 0
if __name__=='__main__': raise SystemExit(main())
