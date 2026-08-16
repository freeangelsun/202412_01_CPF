#!/usr/bin/env python3
# CPF 개발/검증 Source이며 최신 Requirement와 실패 누적 검증 계약을 따릅니다.
from __future__ import annotations
import argparse,json
from pathlib import Path

def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',type=Path,required=True); ap.add_argument('--evidence',type=Path); ns=ap.parse_args(); root=ns.root.resolve()
    catalog=json.loads((root/'cpf-tools/generator/contracts/cpf-starter-catalog.json').read_text(encoding='utf-8'))
    modules=catalog.get('modules',[]); by_project={m.get('projectPath'):m for m in modules}; checks=[]
    ids=[m.get('artifactId') for m in modules]; projects=[m.get('projectPath') for m in modules]
    checks.append({'name':'artifact-id-unique','status':'PASS' if len(ids)==len(set(ids)) else 'FAIL','detail':len(ids)})
    checks.append({'name':'project-path-unique','status':'PASS' if len(projects)==len(set(projects)) else 'FAIL','detail':len(projects)})
    for slot,providers in sorted(catalog.get('providerSlots',{}).items()):
        for name,v in sorted(providers.items()):
            pp=v.get('projectPath'); m=by_project.get(pp); expected=f"{m.get('groupId')}:{m.get('artifactId')}" if m else None
            legacy_project=':' + str(m.get('legacyArtifactId')) if m and m.get('legacyArtifactId') else None
            ok=bool(m and v.get('coordinate')==expected and (legacy_project is None or pp!=legacy_project))
            checks.append({'name':f'provider-slot:{slot}:{name}','status':'PASS' if ok else 'FAIL','detail':{'slot':v,'module':m}})
    failed=[x for x in checks if x['status']=='FAIL']; result={'gate':'NXT3_STARTER_PROVIDER_SLOT_CATALOG','status':'PASS' if not failed else 'FAIL','failedCount':len(failed),'checks':checks}
    if ns.evidence:
        ev=ns.evidence if ns.evidence.is_absolute() else root/ns.evidence; ev.parent.mkdir(parents=True,exist_ok=True); ev.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(result,ensure_ascii=False,indent=2)); return 1 if failed else 0
if __name__=='__main__': raise SystemExit(main())
