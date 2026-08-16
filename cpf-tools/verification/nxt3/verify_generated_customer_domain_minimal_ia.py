#!/usr/bin/env python3
"""Generated Customer Domain 관리자 Steering 최소 IA 강제 Gate."""
# 관리자 Steering에 따라 Generated Customer Domain의 물리 최소 IA와 금지 Surface를 검증한다.
from __future__ import annotations
import argparse, importlib.util, json, sys
from pathlib import Path
from generated_domain_layout import domain_surface_dirs

def load_engine(root:Path):
    p=root/'cpf-tools/generator/engine/cpf_domain_generator.py'; sys.path.insert(0,str(p.parent))
    spec=importlib.util.spec_from_file_location('cpf_domain_generator_minimal_ia_gate',p); m=importlib.util.module_from_spec(spec); sys.modules[spec.name]=m; spec.loader.exec_module(m); return m

def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',required=True,type=Path); ap.add_argument('--evidence',type=Path); ns=ap.parse_args(); root=ns.root.resolve(); eng=load_engine(root); checks=[]
    def ck(name,ok,detail=''): checks.append({'name':name,'status':'PASS' if ok else 'FAIL','detail':detail})
    domains=[]
    for out in sorted(root.glob('cpf-*')):
        definition=root/'cpf-tools/generator/definitions'/out.name.removeprefix('cpf-')/'cpf-domain.yaml'
        if not out.is_dir() or not definition.is_file(): continue
        d=eng.validate_definition(eng.load_yaml_subset(definition)); domains.append(d.name)
        ck(f'{d.name}-root-prefix',out.name==f'cpf-{d.name}',out.name)
        expected={'online'}
        actual=domain_surface_dirs(out); ck(f'{d.name}-physical-ia',actual==expected,{'expected':sorted(expected),'actual':sorted(actual)})
        forbidden=['README.md','verification','db','canonical','vendors',f'{d.name}-api',f'{d.name}-common',f'{d.name}-online',f'{d.name}-batch']
        bad=[x for x in forbidden if (out/x).exists()]; ck(f'{d.name}-forbidden-surface-zero',not bad,bad)
        ck(f'{d.name}-customer-metadata-zero',not (out/'.cpf').exists(),str(out/'.cpf'))
        for cap in sorted(expected): ck(f'{d.name}-{cap}-non-empty',any(p.is_file() for p in (out/cap).rglob('*')),cap)
        # ZIP/working copies may materialize empty directories that Git/Overlay does not track.
        # Minimal IA is therefore judged by files/consumers, not empty directory inode residue.
        settings=(out/'settings.gradle').read_text(encoding='utf-8')
        for cap in ('online','batch','domain','jobpack'):
            ck(f'{d.name}-settings-{cap}',(f"include '{cap}'" in settings)==(cap in expected),settings)
        try:
            vr=eng.verify_generated(root,definition,out,d); ck(f'{d.name}-engine-verify',vr.get('status')=='PASS',vr)
        except Exception as exc: ck(f'{d.name}-engine-verify',False,repr(exc))
    ck('retained-member-external',set(domains)>={'member','external'},domains)
    fail=[x for x in checks if x['status']=='FAIL']; result={'gate':'GENERATED_CUSTOMER_DOMAIN_MINIMAL_IA','status':'PASS' if not fail else 'FAIL','domainCount':len(domains),'failedCount':len(fail),'checks':checks}
    if ns.evidence:
        ev=ns.evidence if ns.evidence.is_absolute() else root/ns.evidence; ev.parent.mkdir(parents=True,exist_ok=True); ev.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(result,ensure_ascii=False,indent=2)); return 0 if not fail else 2
if __name__=='__main__': raise SystemExit(main())
