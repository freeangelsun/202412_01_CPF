#!/usr/bin/env python3
"""CPF Repository Root 및 Generated Customer Domain 최소 IA 검증."""
# Root Project만 cpf-<domain> 접두사를 사용하고 Generated Domain 하위에는 online 업무 Source만 허용한다.
from __future__ import annotations
import argparse,json,re
from pathlib import Path
BASE_ROOT_FILES={'.editorconfig','.gitattributes','.gitignore','README.md','build.gradle','gradlew','gradlew.bat','settings.gradle'}
BASE_ROOT_DIRS={'.git','.github','.gradle','.pytest_cache','.vscode','node_modules','out','cpf-admin','cpf-batch','cpf-biz-admin','cpf-core','cpf-docs','cpf-gateway','cpf-education','cpf-starters','cpf-tools','deploy','gradle'}
EXPECTED_GENERATED={'cpf-member':'member','cpf-external':'external'}
EPHEMERAL_DIRS={'.gradle','.pytest_cache','build','out','node_modules','__pycache__'}
FORBIDDEN_ROOT={'member','external','bin','BASE_SHA.txt','FINAL_DELIVERY_README.md','APPLY_VERIFY_CONTINUE_ON_ERROR.ps1','APPLY_VERIFY_CONTINUE_ON_ERROR.sh','DELETE_FROM_MANIFEST_CONTINUE_ON_ERROR.ps1'}
def domain_name(path:Path)->str|None:
    if not path.is_file(): return None
    m=re.search(r'(?m)^\s*name\s*:\s*([A-Za-z0-9_.-]+)\s*(?:#.*)?$',path.read_text(encoding='utf-8',errors='replace'))
    return m.group(1) if m else None
def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',required=True,type=Path); ap.add_argument('--evidence',type=Path); ns=ap.parse_args(); root=ns.root.resolve(); checks=[]
    def add(name,ok,detail): checks.append({'name':name,'status':'PASS' if ok else 'FAIL','detail':detail})
    names={p.name for p in root.iterdir()}; bad=sorted(names&FORBIDDEN_ROOT); add('forbidden-root-entry-zero',not bad,bad)
    allowed=BASE_ROOT_FILES|BASE_ROOT_DIRS|set(EXPECTED_GENERATED)|{'build'}; extras=sorted(names-allowed); add('unexpected-root-entry-zero',not extras,extras)
    for physical,logical in EXPECTED_GENERATED.items():
        p=root/physical; definition=root/'cpf-tools/generator/definitions'/logical/'cpf-domain.yaml'; add(f'{physical}-physical-root',p.is_dir(),str(p)); add(f'{physical}-logical-domain-name',domain_name(definition)==logical,str(definition)); add(f'{physical}-customer-metadata-zero',not (p/'.cpf').exists(),str(p/'.cpf'))
        expected={'online'}; dirs={x.name for x in p.iterdir() if x.is_dir() and x.name not in EPHEMERAL_DIRS and any(y.is_file() for y in x.rglob('*'))} if p.is_dir() else set(); add(f'{physical}-minimal-ia',dirs==expected,{'expected':sorted(expected),'actual':sorted(dirs)})
        forbidden=[x for x in ['README.md','verification','db',f'{logical}-api',f'{logical}-common',f'{logical}-online',f'{logical}-batch'] if (p/x).exists()]; add(f'{physical}-legacy-surface-zero',not forbidden,forbidden)
        add(f'{physical}-online-non-empty',(p/'online').is_dir() and any(x.is_file() for x in (p/'online').rglob('*')),'online'); add(f'{physical}-batch-domain-jobpack-absent',not any((p/x).is_dir() and any(y.is_file() for y in (p/x).rglob('*')) for x in ('batch','domain','jobpack')),['batch','domain','jobpack'])
    failed=[x for x in checks if x['status']=='FAIL']; result={'gate':'CPF_ROOT_GENERATED_DOMAIN_MINIMAL_IA','status':'PASS' if not failed else 'FAIL','failedCount':len(failed),'checks':checks}
    if ns.evidence:
        ev=ns.evidence if ns.evidence.is_absolute() else root/ns.evidence; ev.parent.mkdir(parents=True,exist_ok=True); ev.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(result,ensure_ascii=False,indent=2)); return 1 if failed else 0
if __name__=='__main__': raise SystemExit(main())

