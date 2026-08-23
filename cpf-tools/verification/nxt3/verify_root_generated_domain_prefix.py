#!/usr/bin/env python3
"""CPF Repository Root 및 Generated Customer Domain 최소 IA 검증."""
# Root Project만 cpf-<domain> 접두사를 사용하며 Generated Domain은 online 필수 + definition에서 선택한 batch만 허용한다.
from __future__ import annotations
import argparse,json
from pathlib import Path
BASE_ROOT_FILES={'.editorconfig','.gitattributes','.gitignore','README.md','build.gradle','gradle.properties','gradlew','gradlew.bat','settings.gradle'}
BASE_ROOT_DIRS={'.git','.github','.gradle','.pytest_cache','.vscode','node_modules','out','cpf-admin','cpf-batch','cpf-backoffice','cpf-backoffice-web','cpf-core','cpf-common','cpf-docs','cpf-gateway','cpf-education','cpf-starters','cpf-tools','deploy','gradle'}
EXPECTED_GENERATED={'cpf-member':'member','cpf-external':'external'}
EPHEMERAL_DIRS={'.gradle','.pytest_cache','build','out','node_modules','__pycache__'}
FORBIDDEN_ROOT={'member','external','cpf-biz-admin','cpf-biz-channel','cpf-biz-frontend','bin','BASE_SHA.txt','FINAL_DELIVERY_README.md','APPLY_VERIFY_CONTINUE_ON_ERROR.ps1','APPLY_VERIFY_CONTINUE_ON_ERROR.sh','DELETE_FROM_MANIFEST_CONTINUE_ON_ERROR.ps1'}
def contract(path:Path)->dict[str,str]:
    values={}
    if not path.is_file(): return values
    for raw in path.read_text(encoding='utf-8-sig',errors='replace').splitlines():
        line=raw.strip()
        if line and not line.startswith(('#','!')) and '=' in line:
            key,value=line.split('=',1); values[key.strip()]=value.strip()
    return values
def main()->int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',required=True,type=Path); ap.add_argument('--evidence',type=Path); ns=ap.parse_args(); root=ns.root.resolve(); checks=[]
    def add(name,ok,detail): checks.append({'name':name,'status':'PASS' if ok else 'FAIL','detail':detail})
    names={p.name for p in root.iterdir()}; bad=sorted(names&FORBIDDEN_ROOT); add('forbidden-root-entry-zero',not bad,bad)
    optional_policy=root/'cpf-tools/governance/cpf-optional-surface-policy.json'
    optional_roots=set()
    if optional_policy.is_file():
        try:
            optional_doc=json.loads(optional_policy.read_text(encoding='utf-8-sig'))
            for item in optional_doc.get('sourceRemovableApplications',[]):
                owner=str(item.get('ownerPath') or '').replace('\\','/').strip('/')
                if owner:
                    optional_roots.add(owner.split('/',1)[0])
        except Exception as exc:
            add('optional-surface-policy-parse',False,str(exc))
    else:
        add('optional-surface-policy-present',False,str(optional_policy))
    allowed=BASE_ROOT_FILES|BASE_ROOT_DIRS|set(EXPECTED_GENERATED)|optional_roots|{'build'}; extras=sorted(names-allowed); add('unexpected-root-entry-zero',not extras,extras)
    add('optional-root-policy-closure',all((root/x).exists() for x in optional_roots if x in names),sorted(optional_roots))
    for physical,logical in EXPECTED_GENERATED.items():
        p=root/physical; definition=p/'gradle.properties'; values=contract(definition); add(f'{physical}-physical-root',p.is_dir(),str(p)); add(f'{physical}-logical-domain-name',values.get('cpf.domain.name')==logical,str(definition)); add(f'{physical}-developer-contract',values.get('cpf.domain.contractVersion')=='1',str(definition))
        metadata=[name for name in ('.cpf','cpf-domain.yaml','cpf-generator.lock.json') if (p/name).exists()]; add(f'{physical}-customer-metadata-zero',not metadata,metadata)
        batch_selected=values.get('cpf.domain.batch','false').lower()=='true'; expected={'online'}|({'batch'} if batch_selected else set()); dirs={x.name for x in p.iterdir() if x.is_dir() and x.name not in EPHEMERAL_DIRS and any(y.is_file() for y in x.rglob('*'))} if p.is_dir() else set(); add(f'{physical}-minimal-ia',dirs==expected,{'expected':sorted(expected),'actual':sorted(dirs)})
        forbidden=[x for x in ['README.md','verification',f'{logical}-api',f'{logical}-common',f'{logical}-online',f'{logical}-batch'] if (p/x).exists()]; add(f'{physical}-legacy-surface-zero',not forbidden,forbidden)
        add(f'{physical}-online-non-empty',(p/'online').is_dir() and any(x.is_file() for x in (p/'online').rglob('*')),'online'); unexpected=[x for x in ('domain','jobpack') if (p/x).is_dir() and any(y.is_file() for y in (p/x).rglob('*'))]; add(f'{physical}-unexpected-domain-jobpack-zero',not unexpected,unexpected); add(f'{physical}-batch-selection-match',((p/'batch').is_dir() and any(y.is_file() for y in (p/'batch').rglob('*')))==batch_selected,{'selected':batch_selected})
    failed=[x for x in checks if x['status']=='FAIL']; result={'gate':'CPF_ROOT_GENERATED_DOMAIN_MINIMAL_IA','status':'PASS' if not failed else 'FAIL','failedCount':len(failed),'checks':checks}
    if ns.evidence:
        ev=ns.evidence if ns.evidence.is_absolute() else root/ns.evidence; ev.parent.mkdir(parents=True,exist_ok=True); ev.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(result,ensure_ascii=False,indent=2)); return 1 if failed else 0
if __name__=='__main__': raise SystemExit(main())
