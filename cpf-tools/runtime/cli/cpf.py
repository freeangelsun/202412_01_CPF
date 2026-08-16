#!/usr/bin/env python3
"""CPF cross-platform user CLI. Windows/Linux launcher가 동일 command surface를 호출한다."""
from __future__ import annotations
import argparse, json, os, sys
from pathlib import Path

HERE=Path(__file__).resolve()
DEFAULT_ROOT=HERE.parents[3]
sys.path.insert(0,str(DEFAULT_ROOT/'cpf-tools/generator/engine'))
from cpf_domain_generator import (DomainError, generate, regenerate, diff, dry_run, load_yaml_subset, validate_definition,
                           verify_generated, verify_genericity, remove_owned, preflight, upgrade, restore,
                           SUPPORTED_VENDORS, _ddl, _migration, _seed, _rollback, _verify_sql)

VERSION='6.2.0'

def repo_root(value: str|None)->Path:
    return Path(value).resolve() if value else DEFAULT_ROOT.resolve()

def generated_root_name(domain_name:str)->str:
    # Generated Customer Domain의 물리 Root는 CPF Root naming 표준에 따라 cpf-<domain>을 사용한다.
    return domain_name if domain_name.startswith('cpf-') else f'cpf-{domain_name}'

def resolve_definition(root:Path, domain_name:str, file_value:str|None=None)->Path:
    """Generated Project 내부 metadata에 의존하지 않고 Framework 정의 또는 명시 입력을 사용한다."""
    if file_value:
        p=Path(file_value)
        p=p if p.is_absolute() else root/p
    else:
        p=root/'cpf-tools/generator/definitions'/domain_name/'cpf-domain.yaml'
    p=p.resolve()
    if not p.is_file(): raise DomainError(f'Generator definition이 없습니다. --file을 지정하세요: {p}')
    d=validate_definition(load_yaml_subset(p))
    if d.name!=domain_name: raise DomainError(f'domain 인자와 definition 불일치: {domain_name} != {d.name}')
    return p

def definition_output(root:Path, file_value:str, output_value:str|None)->tuple[Path,Path]:
    definition=Path(file_value)
    if not definition.is_absolute(): definition=(root/definition).resolve()
    if not definition.is_file(): raise DomainError(f'cpf-domain.yaml이 없습니다: {definition}')
    raw=load_yaml_subset(definition); d=validate_definition(raw)
    output=Path(output_value).resolve() if output_value and Path(output_value).is_absolute() else (root/(output_value or generated_root_name(d.name))).resolve()
    return definition,output

def print_json(value): print(json.dumps(value,ensure_ascii=False,indent=2))

def _domain_setup_definition(name:str, system_code:str, table_prefix:str, package_name:str|None, preset:str) -> str:
    package_line = f"  packageName: {package_name}\n" if package_name else ""
    return (
        "# CPF Generated Domain 정본 입력입니다. packageName 생략 시 domain.name을 package root로 사용합니다.\n"
        f"domain:\n  name: {name}\n  systemCode: {system_code}\n{package_line}"
        f"database:\n  role: CUSTOMER_BUSINESS_DB\n  tablePrefix: {table_prefix}\n"
        f"preset: {preset}\nmodules:\n  online: true\n"
        "features:\n  persistence: mybatis\n  httpClient: true\n  resilience: true\n  cache: none\n  messaging: none\n"
        "generation:\n  sampleTransaction: true\n"
    )

def _domain_setup_profile(name:str, system_code:str, vendor:str, host:str, port:int, database_name:str, schema_name:str, migration_user:str) -> dict:
    env_prefix=system_code.upper()
    return {
      "profileVersion":1,
      "profileName":f"{name}-local",
      "environment":"local",
      "description":"Generated Domain local DB binding. Source 정본과 분리하여 DB Vendor/접속정보만 보관합니다.",
      "database":{
        "vendor":vendor,
        "host":host,
        "port":port,
        "databaseName":database_name,
        "schemaName":schema_name,
        "clientPath":"",
        "migration":{
          "username":migration_user,
          "password":{"env":f"{env_prefix}_DB_MIGRATION_PASSWORD","fallbackEnv":"CPF_DB_MIGRATION_PASSWORD"}
        }
      }
    }

def main()->int:
    p=argparse.ArgumentParser(prog='cpf',description='Core Platform Framework cross-platform CLI')
    p.add_argument('--version',action='version',version=f'cpf {VERSION}')
    p.add_argument('--root',help='CPF repository root')
    sub=p.add_subparsers(dest='group',required=True)

    domain=sub.add_parser('domain',help='Generated Customer Domain lifecycle')
    dsub=domain.add_subparsers(dest='command',required=True)
    for command in ('generate','add','dry-run','diff','validate'):
        sp=dsub.add_parser(command); sp.add_argument('--file',required=True); sp.add_argument('--output')
    setup=dsub.add_parser('setup',help='Domain 정본과 분리된 local DB profile을 함께 준비')
    setup.add_argument('--name',required=True); setup.add_argument('--system-code',required=True); setup.add_argument('--table-prefix',required=True)
    setup.add_argument('--package-name'); setup.add_argument('--preset',default='standard-enterprise',choices=['minimal','standard-enterprise','full-enterprise','custom'])
    setup.add_argument('--vendor',required=True,choices=SUPPORTED_VENDORS); setup.add_argument('--host',default='127.0.0.1'); setup.add_argument('--port',type=int)
    setup.add_argument('--database-name',required=True); setup.add_argument('--schema-name',default=''); setup.add_argument('--migration-user')
    setup.add_argument('--definition-output'); setup.add_argument('--db-profile-output')
    regen=dsub.add_parser('regenerate'); regen.add_argument('domain'); regen.add_argument('--file'); regen.add_argument('--output')
    allgen=dsub.add_parser('generate-all'); allgen.add_argument('--definitions-root'); allgen.add_argument('--output-root')
    upgrade_parser=dsub.add_parser('upgrade'); upgrade_parser.add_argument('domain'); upgrade_parser.add_argument('--file'); upgrade_parser.add_argument('--output')
    restore_parser=dsub.add_parser('restore'); restore_parser.add_argument('--file',required=True); restore_parser.add_argument('--output')
    rem=dsub.add_parser('remove'); rem.add_argument('domain'); rem.add_argument('--file'); rem.add_argument('--output'); rem.add_argument('--apply',action='store_true',help='현재 Generator 입력과 동일한 Seed Source만 안전하게 제거'); rem.add_argument('--purge-definition',action='store_true',help='명시 승인 시 cpf-domain.yaml 정의까지 제거하여 선택 Domain을 완전히 해제')

    db=sub.add_parser('db',help='Generated Domain DB3 renderer')
    dbsub=db.add_subparsers(dest='command',required=True)
    render=dbsub.add_parser('render'); render.add_argument('--file',required=True); render.add_argument('--vendor',required=True,choices=SUPPORTED_VENDORS); render.add_argument('--output')

    verify=sub.add_parser('verify',help='CPF verification entrypoints')
    vsub=verify.add_subparsers(dest='command',required=True)
    vsub.add_parser('generator')
    va=vsub.add_parser('domain'); va.add_argument('--file',required=True); va.add_argument('--output')
    vsub.add_parser('all')

    ns=p.parse_args(); root=repo_root(ns.root)
    if ns.group=='domain':
        if ns.command=='setup':
            name=ns.name.strip().lower(); system_code=ns.system_code.strip().upper(); table_prefix=ns.table_prefix.strip().upper()
            package_name=ns.package_name.strip() if ns.package_name else None
            setup_root=root/'build'/'domain-generator'/'setup'/name
            definition=(Path(ns.definition_output) if ns.definition_output else setup_root/'cpf-domain.yaml')
            profile=(Path(ns.db_profile_output) if ns.db_profile_output else setup_root/'cpf-db-profile.local.json')
            definition=definition if definition.is_absolute() else root/definition
            profile=profile if profile.is_absolute() else root/profile
            definition.parent.mkdir(parents=True,exist_ok=True); profile.parent.mkdir(parents=True,exist_ok=True)
            definition.write_text(_domain_setup_definition(name,system_code,table_prefix,package_name,ns.preset),encoding='utf-8',newline='\n')
            d=validate_definition(load_yaml_subset(definition))
            default_ports={'mariadb':3306,'postgresql':5432,'oracle':1521}; port=ns.port or default_ports[ns.vendor]
            if not (1 <= port <= 65535): raise DomainError(f'DB port 범위가 올바르지 않습니다: {port}')
            migration_user=ns.migration_user or f'cpf_{name.replace("-","_")}_migration'
            profile.write_text(json.dumps(_domain_setup_profile(name,system_code,ns.vendor,ns.host,port,ns.database_name,ns.schema_name,migration_user),ensure_ascii=False,indent=2)+'\n',encoding='utf-8',newline='\n')
            print_json({'status':'PASS','domain':d.name,'packageName':d.package_name,'definition':str(definition),'dbProfile':str(profile),'databaseVendor':ns.vendor,'next':{'generate':f'cpf domain generate --file {definition}','dbLifecycle':f'initialize-domain-database -ProfilePath {profile}'}}); return 0
        if ns.command in ('generate','add','dry-run','diff','validate'):
            definition,output=definition_output(root,ns.file,ns.output)
            if ns.command in {'generate','add'}: print_json(generate(root,definition,output)); return 0
            if ns.command=='dry-run': print_json(dry_run(root,definition,output)); return 0
            if ns.command=='diff': print_json(diff(root,definition,output)); return 0
            print_json(preflight(root,definition,output)); return 0
        if ns.command=='generate-all':
            definitions_root=Path(ns.definitions_root).resolve() if ns.definitions_root else root/'cpf-tools/generator/definitions'
            output_root=Path(ns.output_root).resolve() if ns.output_root else root
            defs=sorted(p for p in definitions_root.glob('*/cpf-domain.yaml') if p.is_file())
            if not defs: raise DomainError(f'생성할 Generated Domain definition이 없습니다: {definitions_root}')
            results=[]
            for definition in defs:
                d=validate_definition(load_yaml_subset(definition)); results.append(generate(root,definition,output_root/generated_root_name(d.name)))
            print_json({'status':'PASS','count':len(results),'results':results}); return 0
        if ns.command in ('regenerate','upgrade'):
            output=((Path(ns.output) if Path(ns.output).is_absolute() else root/Path(ns.output)).resolve() if ns.output else (root/generated_root_name(ns.domain)).resolve()); definition=resolve_definition(root,ns.domain,ns.file)
            action=upgrade if ns.command=='upgrade' else regenerate
            print_json(action(root,definition,output)); return 0
        if ns.command=='restore':
            definition,output=definition_output(root,ns.file,ns.output); print_json(restore(root,definition,output)); return 0
        if ns.command=='remove':
            definition=resolve_definition(root,ns.domain,ns.file)
            output=((Path(ns.output) if Path(ns.output).is_absolute() else root/Path(ns.output)).resolve() if ns.output else (root/generated_root_name(ns.domain)).resolve())
            print_json(remove_owned(root,definition,output,apply=ns.apply,purge_definition=ns.purge_definition)); return 0
    if ns.group=='db' and ns.command=='render':
        definition=Path(ns.file); definition=definition if definition.is_absolute() else root/definition
        d=validate_definition(load_yaml_subset(definition.resolve())); vendor=ns.vendor
        out=Path(ns.output).resolve() if ns.output else root/'build'/'domain-generator'/'verification'/generated_root_name(d.name)/'db3'/vendor
        out.mkdir(parents=True,exist_ok=True)
        files={
          '10_empty_install.sql':_ddl(root,d,vendor),
          '20_product_seed.sql':_seed(root,d,vendor),
          f'V1__{d.name}_domain.sql':_migration(root,d,vendor),
          f'R1__remove_{d.name}_domain.sql':_rollback(root,d,vendor),
          '90_verify.sql':_verify_sql(root,d,vendor),
        }
        for name,content in files.items(): (out/name).write_text(content,encoding='utf-8',newline='\n')
        print_json({'status':'PASS','vendor':vendor,'output':str(out),'files':sorted(files)}); return 0
    if ns.group=='verify':
        if ns.command=='generator': print_json(verify_genericity(root/'cpf-tools/generator')); return 0
        if ns.command=='domain':
            definition,output=definition_output(root,ns.file,ns.output); d=validate_definition(load_yaml_subset(definition)); print_json(verify_generated(root,definition,output,d)); return 0
        if ns.command=='all':
            generic=verify_genericity(root/'cpf-tools/generator'); results={'generator':generic,'domains':[]}
            definitions_root=root/'cpf-tools/generator/definitions'
            for definition in sorted(definitions_root.glob('*/cpf-domain.yaml')):
                d=validate_definition(load_yaml_subset(definition)); child=root/generated_root_name(d.name)
                if not child.is_dir(): continue
                results['domains'].append(verify_generated(root,definition,child,d))
            results['status']='PASS' if generic['status']=='PASS' and all(x['status']=='PASS' for x in results['domains']) else 'FAIL'
            print_json(results); return 0 if results['status']=='PASS' else 2
    raise DomainError('지원하지 않는 명령입니다.')

if __name__=='__main__':
    try: raise SystemExit(main())
    except (DomainError,OSError,ValueError) as exc:
        print(f'CPF_CLI=FAIL {exc}',file=sys.stderr); raise SystemExit(2)
