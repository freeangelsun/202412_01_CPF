#!/usr/bin/env python3
"""두 공식 Root/제3 Domain/Lifecycle/Public Boundary를 동일 Canonical Engine으로 검증한다."""
from __future__ import annotations

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass
import argparse, importlib.util, json, shutil, sys, tempfile
from pathlib import Path


def load_engine(root: Path):
    p=root/'cpf-tools/generator/engine/cpf_domain_generator.py'
    engine_dir=str(p.parent)
    if engine_dir not in sys.path: sys.path.insert(0,engine_dir)
    spec=importlib.util.spec_from_file_location('cpf_domain_generator',p)
    m=importlib.util.module_from_spec(spec); sys.modules[spec.name]=m; spec.loader.exec_module(m)
    return m


def yaml_text(domain: str, system: str, pkg: str, prefix: str) -> str:
    return f'''# Fresh Genericity 검증 입력이며 Secret/DB Vendor 실제 값은 포함하지 않는다.\ndomain:\n  name: {domain}\n  systemCode: {system}\n  packageName: {pkg}\ndatabase:\n  role: CUSTOMER_BUSINESS_DB\n  tablePrefix: {prefix}\npreset: standard-enterprise\nmodules:\n  online: true\nfeatures:\n  persistence: mybatis\n  httpClient: true\n  resilience: true\n  cache: none\n  messaging: none\ngeneration:\n  sampleTransaction: true\n'''


def main(argv=None) -> int:
    ap=argparse.ArgumentParser(); ap.add_argument('--root',required=True); a=ap.parse_args(argv)
    root=Path(a.root).resolve(); eng=load_engine(root); checks=[]
    def ck(name,ok,detail=''): checks.append({'name':name,'status':'PASS' if ok else 'FAIL','detail':detail})

    # 공식 Root는 같은 Engine의 verify/diff를 사용하며 수동 Golden Source를 허용하지 않는다.
    for name in ('cpf-member','cpf-external'):
        out=root/name; logical=name.removeprefix('cpf-'); definition=out/'gradle.properties'
        try:
            d=eng.load_domain_contract(definition)
            vr=eng.verify_generated(root,definition,out,d)
            ck('VERIFY_'+name.upper(),vr.get('status')=='PASS',json.dumps(vr,ensure_ascii=False))
            dr=eng.diff(root,definition,out)
            ck('IDEMPOTENT_DIFF_'+name.upper(),dr.get('clean') is True,json.dumps(dr,ensure_ascii=False))
            contract=definition.read_text(encoding='utf-8-sig')
            ck('DOMAIN_INPUT_VENDOR_FREE_'+name.upper(),'cpf.domain.vendor=' not in contract,definition.as_posix())
            ck('DIRECT_DOMAIN_PACKAGE_'+name.upper(),d.package_name==d.name,d.package_name)
            forbidden=[entry for entry in ('cpf-domain.yaml','cpf-generator.lock.json','.cpf') if (out/entry).exists()]
            ck('GENERATOR_METADATA_ZERO_'+name.upper(),not forbidden,forbidden)
            legacy_paths=[p.relative_to(out).as_posix() for p in out.rglob('*') if p.is_file() and 'com/customer/' in p.as_posix()]
            legacy_text=[]
            for p in out.rglob('*'):
                if p.is_file() and p.stat().st_size < 2_000_000:
                    try:
                        if 'com.customer.' in p.read_text(encoding='utf-8-sig',errors='ignore'): legacy_text.append(p.relative_to(out).as_posix())
                    except OSError: pass
            ck('NO_COM_CUSTOMER_'+name.upper(),not legacy_paths and not legacy_text,json.dumps({'paths':legacy_paths,'text':legacy_text},ensure_ascii=False))
            build_text='\n'.join(p.read_text(encoding='utf-8-sig',errors='ignore') for p in out.rglob('build.gradle'))
            ck('DB_VENDOR_FAIL_CLOSED_'+name.upper(),
               "providers.environmentVariable('CPF_DB_VENDOR')" in build_text and ".orElse('mariadb')" not in build_text and 'DB Vendor가 지정되지 않았습니다' in build_text,
               'Gradle property/env explicit selection')
        except Exception as exc:
            ck('VERIFY_'+name.upper(),False,repr(exc)); ck('IDEMPOTENT_DIFF_'+name.upper(),False,'verify exception')

    generic=eng.verify_genericity(root/'cpf-tools/generator')
    ck('GENERATOR_GENERICITY',generic.get('status')=='PASS',json.dumps(generic,ensure_ascii=False))

    with tempfile.TemporaryDirectory(prefix='cpf-gen-gate-') as td:
        stage=Path(td); repo=stage/'repo'; repo.mkdir()
        # Engine이 Catalog/Stack을 읽을 수 있도록 정본 파일만 동일 경로로 복사한다.
        for rel in ['cpf-tools/generator/contracts/cpf-domain.schema.json','cpf-tools/generator/contracts/cpf-starter-catalog.json','gradle/cpf-stack.properties']:
            src=root/rel; dst=repo/rel; dst.parent.mkdir(parents=True,exist_ok=True); shutil.copy2(src,dst)
        # Arbitrary Domain은 Canonical DB renderer까지 실제 소비하므로 DB3 template pack도 격리 Repository에 복사한다.
        db_templates = root/'cpf-tools/db/generated/domain-template'
        shutil.copytree(db_templates, repo/'cpf-tools/db/generated/domain-template')
        definition=stage/'ledger.yaml'; definition.write_text(yaml_text('ledger','LDG','ledger','LDG'),encoding='utf-8')
        out=repo/'cpf-ledger'
        try:
            dry=eng.dry_run(repo,definition,out); ck('ARBITRARY_DRY_RUN',dry.get('status')=='DRY_RUN_PASS',json.dumps(dry,ensure_ascii=False)[:4000])
            gen=eng.generate(repo,definition,out); ck('ARBITRARY_GENERATE',gen.get('status')=='GENERATED',json.dumps(gen,ensure_ascii=False)[:4000])
            d=eng.validate_definition(eng.load_yaml_subset(definition))
            vr=eng.verify_generated(repo,definition,out,d); ck('ARBITRARY_VERIFY',vr.get('status')=='PASS',json.dumps(vr,ensure_ascii=False))
            idem=eng.generate(repo,definition,out); ck('ARBITRARY_IDEMPOTENT',idem.get('status')=='IDEMPOTENT',json.dumps(idem,ensure_ascii=False)[:4000])
            plan=eng.remove_owned(repo,definition,out,apply=False); ck('REMOVE_PLAN_SAFE',plan.get('status')=='PLANNED_DELETE_MANIFEST' and plan.get('safeToRemove') is True,json.dumps(plan,ensure_ascii=False)[:4000])
            # Generator-owned Source 사용자 변경 시 regenerate/remove가 fail-closed여야 한다.
            target=out/'online/src/main/java/ledger/sample/controller/SampleTransactionController.java'
            target.write_text(target.read_text(encoding='utf-8')+'\n// 사용자 소유 변경 검증\n',encoding='utf-8',newline='\n')
            try: eng.regenerate(repo,definition,out); protected=False
            except Exception: protected=True
            ck('USER_MODIFICATION_PROTECTED',protected)
            # 변경을 되돌리고 regenerate -> remove -> restore 전체 lifecycle을 수행한다.
            target.write_text(target.read_text(encoding='utf-8').replace('\n// 사용자 소유 변경 검증\n',''),encoding='utf-8',newline='\n')
            regen=eng.regenerate(repo,definition,out); ck('REGENERATE',regen.get('status')=='REGENERATED',json.dumps(regen,ensure_ascii=False)[:4000])
            rem=eng.remove_owned(repo,definition,out,apply=False)
            for rel in rem.get('deleteCandidates',[]):
                candidate=out/rel
                if candidate.is_file(): candidate.unlink()
            for directory in sorted((p for p in out.rglob('*') if p.is_dir()),key=lambda p:len(p.parts),reverse=True):
                try: directory.rmdir()
                except OSError: pass
            remaining={p.name for p in out.iterdir()} if out.is_dir() else set(); ck('REMOVE_MANIFEST_REPLAY_GENERATOR_OWNED',rem.get('status')=='PLANNED_DELETE_MANIFEST' and not remaining,json.dumps({'remove':rem,'remaining':sorted(remaining)},ensure_ascii=False)[:4000])
            restore=eng.restore(repo,definition,out); ck('RESTORE',restore.get('status')=='RESTORED',json.dumps(restore,ensure_ascii=False)[:4000])
        except Exception as exc:
            ck('ARBITRARY_LIFECYCLE_EXCEPTION',False,repr(exc))

        bad1=stage/'bad-package.yaml'; bad1.write_text(yaml_text('badone','BAD','com.cpf.bad','BAD'),encoding='utf-8')
        try: eng.validate_definition(eng.load_yaml_subset(bad1)); bad_package=False
        except Exception: bad_package=True
        ck('RESERVED_PACKAGE_FAIL_FAST',bad_package)
        bad2=stage/'bad-prefix.yaml'; bad2.write_text(yaml_text('badtwo','BDT','badtwo','bad'),encoding='utf-8')
        try: eng.validate_definition(eng.load_yaml_subset(bad2)); bad_prefix=False
        except Exception: bad_prefix=True
        ck('LOWERCASE_PREFIX_FAIL_FAST',bad_prefix)
        bad3=stage/'bad-vendor.yaml'; bad3.write_text(yaml_text('badthree','BDR','badthree','BDR').replace('  tablePrefix: BDR','  tablePrefix: BDR\n  vendor: oracle'),encoding='utf-8')
        try: eng.validate_definition(eng.load_yaml_subset(bad3)); vendor_block=False
        except Exception: vendor_block=True
        ck('DOMAIN_VENDOR_SELECTION_BLOCKED',vendor_block)

    # 한 Engine/Schema/CLI Surface만 유지하는지 물리 파일을 확인한다.
    ck('SINGLE_CANONICAL_ENGINE',(root/'cpf-tools/generator/engine/cpf_domain_generator.py').is_file() and not (root/'cpf-tools/generator/core/domain_engine.py').exists())
    ck('SCHEMA_PRESENT',(root/'cpf-tools/generator/contracts/cpf-domain.schema.json').is_file())
    ck('CLI_WINDOWS_LINUX',(root/'cpf-tools/runtime/cli/cpf.cmd').is_file() and (root/'cpf-tools/runtime/cli/cpf').is_file())

    fail=[x for x in checks if x['status']=='FAIL']
    result={'status':'PASS' if not fail else 'FAIL','pass':len(checks)-len(fail),'fail':len(fail),'checks':checks}
    print(json.dumps(result,ensure_ascii=False,indent=2)); return 0 if not fail else 2

if __name__=='__main__': raise SystemExit(main())
