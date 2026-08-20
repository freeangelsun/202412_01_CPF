#!/usr/bin/env python3
"""CPF cross-platform user CLI. Windows/Linux launcher가 동일 command surface를 호출한다."""
from __future__ import annotations
import argparse, json, os, shutil, sys, tempfile, uuid
from pathlib import Path

HERE=Path(__file__).resolve()
if getattr(sys, 'frozen', False):
    _embedded=Path(getattr(sys, '_MEIPASS')).resolve()/'cpf-generator-resources'
    os.environ.setdefault('CPF_GENERATOR_RESOURCE_ROOT',str(_embedded))
    sys.path.insert(0,str(_embedded/'cpf-tools/generator/engine'))
    DEFAULT_ROOT=Path.cwd().resolve()
else:
    DEFAULT_ROOT=HERE.parents[3]
    sys.path.insert(0,str(DEFAULT_ROOT/'cpf-tools/generator/engine'))
from cpf_domain_generator import (DomainError, generate, regenerate, diff, dry_run, load_yaml_subset, validate_definition,
                           verify_generated, verify_genericity, remove_owned, preflight, upgrade, restore,
                           SUPPORTED_VENDORS, _ddl, _migration, _seed, _rollback, _verify_sql)

VERSION='6.4.0'

def repo_root(value: str|None)->Path:
    return Path(value).resolve() if value else DEFAULT_ROOT.resolve()

def generated_root_name(domain_name:str)->str:
    # Generated Customer Domain의 물리 Root는 CPF Root naming 표준에 따라 cpf-<domain>을 사용한다.
    return domain_name if domain_name.startswith('cpf-') else f'cpf-{domain_name}'

def workspace_definition_root(root:Path)->Path:
    public_root=root/'domains'
    return public_root if public_root.is_dir() else root/'cpf-tools/generator/definitions'


def _golden_domain_definition(name:str, system_code:str, batch:bool)->str:
    table_prefix=system_code
    return (
        '# CPF Generated Business Domain의 source-controlled Canonical Definition입니다.\n'
        'domain:\n'
        f'  name: {name}\n'
        f'  systemCode: {system_code}\n'
        'database:\n'
        '  role: CUSTOMER_BUSINESS_DB\n'
        f'  tablePrefix: {table_prefix}\n'
        'preset: standard-enterprise\n'
        'modules:\n'
        '  online: true\n'
        f'  batch: {str(batch).lower()}\n'
        'features:\n'
        '  persistence: mybatis\n'
        '  httpClient: true\n'
        '  resilience: true\n'
        '  cache: none\n'
        '  messaging: none\n'
        'domainDependencies:\n'
        'externalClients:\n'
        'generation:\n'
        '  sampleTransaction: true\n'
    )


def create_workspace_domain(root:Path,name:str,system_code:str,batch:bool)->dict:
    name=name.strip().lower(); system_code=system_code.strip().upper()
    if not name or not system_code: raise DomainError('domain name/systemCode가 필요합니다.')
    definitions=workspace_definition_root(root); definitions.mkdir(parents=True,exist_ok=True)
    canonical=definitions/name/'cpf-domain.yaml'
    final_output=root/generated_root_name(name)
    if canonical.exists(): raise DomainError(f'Domain definition이 이미 존재합니다: {canonical}')
    if final_output.exists(): raise DomainError(f'Generated Domain project가 이미 존재합니다: {final_output}')
    body=_golden_domain_definition(name,system_code,batch)
    temp_parent=root/'build'/'domain-generator'/'new'; temp_parent.mkdir(parents=True,exist_ok=True)
    with tempfile.TemporaryDirectory(prefix='cpf-domain-new-',dir=temp_parent) as td:
        stage=Path(td); definition=stage/'cpf-domain.yaml'; definition.write_text(body,encoding='utf-8',newline='\n')
        stage_output=stage/generated_root_name(name)
        generated=generate(root,definition,stage_output)
        if generated.get('verify',{}).get('status')!='PASS': raise DomainError(f'Generated Domain verification failed: {name}')
        canonical.parent.mkdir(parents=True,exist_ok=True); canonical.write_text(body,encoding='utf-8',newline='\n')
        try:
            shutil.move(str(stage_output),str(final_output))
            verified=generate(root,canonical,final_output)
        except Exception:
            if final_output.exists(): shutil.rmtree(final_output,ignore_errors=True)
            if canonical.exists(): canonical.unlink()
            try: canonical.parent.rmdir()
            except OSError: pass
            raise
    return {'status':'PASS','action':'DOMAIN_NEW','domain':name,'systemCode':system_code,'batch':batch,'definition':str(canonical),'project':str(final_output),'generator':verified}


def sync_workspace_domains(root:Path)->dict:
    definitions=workspace_definition_root(root)
    if not definitions.is_dir(): raise DomainError(f'Workspace Domain Catalog가 없습니다: {definitions}')
    results=[]
    for definition in sorted(definitions.glob('*/cpf-domain.yaml')):
        d=validate_definition(load_yaml_subset(definition)); output=root/generated_root_name(d.name)
        if not output.is_dir():
            result=generate(root,definition,output)
        else:
            lock=definition.parent/'cpf-generator.lock.json'
            if lock.is_file(): result=upgrade(root,definition,output)
            else: result=generate(root,definition,output)
        results.append({'domain':d.name,'status':result.get('status'),'project':str(output)})
    return {'status':'PASS','action':'DOMAIN_SYNC','count':len(results),'results':results}

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

def _yaml_bool(value: bool) -> str:
    return "true" if value else "false"


def _parse_domain_dependency_arg(value: str) -> tuple[str, str, tuple[str, ...]]:
    parts=value.split(':',2)
    if len(parts)!=3:
        raise DomainError("--domain-dependency는 name:SYSTEM:operation1,operation2 형식이어야 합니다.")
    name,system,ops=parts[0].strip().lower(),parts[1].strip().upper(),tuple(x.strip() for x in parts[2].split(',') if x.strip())
    if not name or not system or not ops:
        raise DomainError("--domain-dependency 값이 비어 있습니다.")
    return name,system,ops


def _parse_external_client_arg(value: str) -> tuple[str,str,str]:
    parts=[x.strip() for x in value.split(':',2)]
    if len(parts)!=3 or not all(parts):
        raise DomainError("--external-client는 name:client-id:capability 형식이어야 합니다.")
    return parts[0].lower(),parts[1],parts[2]


def _domain_setup_definition(
        name:str, system_code:str, table_prefix:str, package_name:str|None, preset:str,
        online:bool, batch:bool, persistence:str, http_client:bool, resilience:bool,
        cache:str, messaging:str, object_storage:str, security_profile:str,
        sample_transaction:bool, local_online_port:int|None,
        dependencies:list[tuple[str,str,tuple[str,...]]], external_clients:list[tuple[str,str,str]]) -> str:
    package_line = f"  packageName: {package_name}\n" if package_name else ""
    lines=[
      "# CPF Generated Business Domain의 source-controlled Canonical Definition입니다.",
      "# DB Vendor/Host/계정/Secret은 local/environment binding profile이 소유합니다.",
      "domain:", f"  name: {name}", f"  systemCode: {system_code}",
    ]
    if package_name: lines.append(f"  packageName: {package_name}")
    lines += [
      "database:", "  role: CUSTOMER_BUSINESS_DB", f"  tablePrefix: {table_prefix}",
      f"preset: {preset}", "modules:", f"  online: {_yaml_bool(online)}", f"  batch: {_yaml_bool(batch)}",
      "features:", f"  persistence: {persistence}", f"  httpClient: {_yaml_bool(http_client)}",
      f"  resilience: {_yaml_bool(resilience)}", f"  cache: {cache}", f"  messaging: {messaging}",
      f"  objectStorage: {object_storage}", f"  securityProfile: {security_profile}",
      "domainDependencies:",
    ]
    for dep_name,dep_system,operations in dependencies:
        lines += [f"  {dep_name}:", f"    systemCode: {dep_system}", "    operations:"]
        lines += [f"      - {operation}" for operation in operations]
    lines += ["externalClients:"]
    for client_name,client_id,capability in external_clients:
        lines += [f"  {client_name}:", f"    id: {client_id}", f"    capability: {capability}"]
    if local_online_port is not None:
        lines += ["runtime:", f"  localOnlinePort: {local_online_port}"]
    lines += ["generation:", f"  sampleTransaction: {_yaml_bool(sample_transaction)}"]
    return "\n".join(lines)+"\n"


def _domain_setup_profile(
        name:str, system_code:str, vendor:str, host:str, port:int,
        database_name:str, service_name:str, schema_name:str,
        migration_user:str, runtime_user:str, migration_secret_env:str, runtime_secret_env:str) -> dict:
    db:dict[str,object]={
      "vendor":vendor,
      "host":host,
      "port":port,
      "logicalDatabase":system_code.lower()+"DB",
      "schemaName":schema_name,
      "migration":{
        "username":migration_user,
        "password":{"env":migration_secret_env}
      },
      "runtime":{
        "username":runtime_user,
        "password":{"env":runtime_secret_env}
      }
    }
    if vendor=="oracle": db["serviceName"]=service_name or database_name
    else: db["databaseName"]=database_name
    return {
      "profileVersion":2,
      "profileName":f"{name}-local",
      "environment":"local",
      "domain":{"name":name,"systemCode":system_code},
      "database":db,
      "sourceControlled":False,
      "secretPolicy":"ENV_REFERENCE_ONLY"
    }


def setup_workspace_domain(root:Path, ns) -> dict:
    name=ns.name.strip().lower(); system_code=ns.system_code.strip().upper(); table_prefix=(ns.table_prefix or system_code).strip().upper()
    package_name=ns.package_name.strip() if ns.package_name else None
    online=not ns.no_online
    batch=bool(ns.batch)
    if not online and not batch: raise DomainError("--no-online 사용 시 --batch가 필요합니다.")

    # Preset defaults + explicit override. Artifact 이름이 아니라 Capability만 입력합니다.
    preset_defaults={
      'minimal':dict(persistence='none',http_client=False,resilience=False,cache='none',messaging='none',object_storage='none',security_profile='resource-server',sample_transaction=False),
      'standard-enterprise':dict(persistence='mybatis',http_client=True,resilience=True,cache='none',messaging='none',object_storage='none',security_profile='resource-server',sample_transaction=True),
      'full-enterprise':dict(persistence='mybatis',http_client=True,resilience=True,cache='valkey',messaging='kafka',object_storage='s3',security_profile='resource-server',sample_transaction=True),
      'custom':dict(persistence='none',http_client=False,resilience=False,cache='none',messaging='none',object_storage='none',security_profile='resource-server',sample_transaction=False),
    }
    selected=preset_defaults[ns.preset].copy()
    for key,attr in [('persistence','persistence'),('cache','cache'),('messaging','messaging'),('object_storage','object_storage'),('security_profile','security_profile')]:
        value=getattr(ns,attr,None)
        if value is not None: selected[key]=value
    if ns.http_client is not None: selected['http_client']=ns.http_client
    if ns.resilience is not None: selected['resilience']=ns.resilience
    if ns.sample_transaction is not None: selected['sample_transaction']=ns.sample_transaction
    if selected['sample_transaction'] and not online: raise DomainError("sample transaction은 Online Runtime이 필요합니다.")

    dependencies=[_parse_domain_dependency_arg(x) for x in (ns.domain_dependency or [])]
    external_clients=[_parse_external_client_arg(x) for x in (ns.external_client or [])]
    definition_body=_domain_setup_definition(
        name,system_code,table_prefix,package_name,ns.preset,online,batch,
        selected['persistence'],selected['http_client'],selected['resilience'],selected['cache'],selected['messaging'],selected['object_storage'],selected['security_profile'],selected['sample_transaction'],ns.local_online_port,
        dependencies,external_clients)

    definition_root=workspace_definition_root(root)
    canonical=Path(ns.definition_output).resolve() if ns.definition_output else definition_root/name/'cpf-domain.yaml'
    output=Path(ns.output).resolve() if ns.output else root/generated_root_name(name)
    if canonical.exists() and not ns.sync:
        raise DomainError(f"Domain definition이 이미 존재합니다: {canonical}; 변경은 --sync 또는 domain sync를 사용하세요.")
    if output.exists() and not ns.sync:
        raise DomainError(f"Generated Domain project가 이미 존재합니다: {output}; 변경은 --sync 또는 domain sync를 사용하세요.")

    profile=None
    profile_payload=None
    if selected['persistence']!='none':
        if not ns.vendor: raise DomainError("persistence 사용 시 --vendor가 필요합니다.")
        if not ns.database_name and ns.vendor!='oracle': raise DomainError(f"{ns.vendor} persistence는 --database-name이 필요합니다.")
        if ns.vendor=='oracle' and not (ns.service_name or ns.database_name): raise DomainError("oracle persistence는 --service-name 또는 --database-name이 필요합니다.")
        if not ns.schema_name: raise DomainError("persistence 사용 시 --schema-name이 필요합니다.")
        default_ports={'mariadb':3306,'postgresql':5432,'oracle':1521}; port=ns.port or default_ports[ns.vendor]
        if not (1 <= port <= 65535): raise DomainError(f"DB port 범위가 올바르지 않습니다: {port}")
        migration_user=ns.migration_user or f"cpf_{system_code.lower()}_migration"
        runtime_user=ns.runtime_user or f"cpf_{system_code.lower()}_runtime"
        if migration_user==runtime_user: raise DomainError("Migration 계정과 Runtime 계정은 분리해야 합니다.")
        migration_secret=ns.migration_secret_env or f"{system_code}_DB_MIGRATION_PASSWORD"
        runtime_secret=ns.runtime_secret_env or f"{system_code}_DB_RUNTIME_PASSWORD"
        profile=Path(ns.db_profile_output).resolve() if ns.db_profile_output else root/'build'/'cpf-local'/name/'cpf-db-profile.local.json'
        profile_payload=_domain_setup_profile(name,system_code,ns.vendor,ns.host,port,ns.database_name or '',ns.service_name or '',ns.schema_name,migration_user,runtime_user,migration_secret,runtime_secret)
    elif any([ns.vendor,ns.database_name,ns.service_name,ns.schema_name,ns.migration_user,ns.runtime_user]):
        raise DomainError("features.persistence=none이면 DB Binding 입력을 함께 사용할 수 없습니다.")

    # 모든 validation/generator dry-run을 실제 쓰기 전에 끝냅니다.
    with tempfile.TemporaryDirectory(prefix=f"cpf-domain-setup-{name}-") as td:
        stage=Path(td)
        staged_def=stage/'cpf-domain.yaml'; staged_def.write_text(definition_body,encoding='utf-8',newline='\n')
        d=validate_definition(load_yaml_subset(staged_def))
        planned=dry_run(root,staged_def,root/generated_root_name(name)) if not output.exists() else {'status':'EXISTING'}
        preview={
          'domain':{'name':d.name,'systemCode':d.system_code,'packageName':d.package_name,'online':d.online,'batch':d.batch},
          'database':profile_payload['database'] if profile_payload else None,
          'capabilities':{'persistence':d.persistence,'cache':d.cache,'messaging':d.messaging,'objectStorage':d.object_storage,'httpClient':d.http_client,'resilience':d.resilience,'securityProfile':d.security_profile},
          'domainDependencies':[{'name':x.name,'systemCode':x.system_code,'operations':list(x.operations)} for x in d.domain_dependencies],
          'externalClients':[{'name':x.name,'id':x.client_id,'capability':x.capability} for x in d.external_clients],
          'selectionSummary':planned.get('selectionSummary')
        }
        if ns.preview:
            return {'status':'PREVIEW','preview':preview,'definition':str(canonical),'dbProfile':str(profile) if profile else None,'output':str(output)}

        canonical.parent.mkdir(parents=True,exist_ok=True)
        previous_definition=canonical.read_bytes() if canonical.exists() else None
        previous_profile=profile.read_bytes() if profile and profile.exists() else None
        canonical.write_text(definition_body,encoding='utf-8',newline='\n')
        if profile and profile_payload:
            profile.parent.mkdir(parents=True,exist_ok=True)
            profile.write_text(json.dumps(profile_payload,ensure_ascii=False,indent=2)+'\n',encoding='utf-8',newline='\n')
        try:
            if output.exists():
                generated=upgrade(root,canonical,output)
            else:
                generated=generate(root,canonical,output)
        except Exception:
            if previous_definition is None:
                canonical.unlink(missing_ok=True)
                try: canonical.parent.rmdir()
                except OSError: pass
            else: canonical.write_bytes(previous_definition)
            if profile:
                if previous_profile is None: profile.unlink(missing_ok=True)
                else: profile.write_bytes(previous_profile)
            raise
    return {
      'status':'PASS','action':'DOMAIN_SETUP','domain':name,'systemCode':system_code,
      'definition':str(canonical),'dbProfile':str(profile) if profile else None,'project':str(output),
      'preview':preview,'sourceGeneration':generated.get('status'),
      'localDb':'NOT_EXECUTED','runtime':'NOT_EXECUTED',
      'next':{'bootstrap':'cpf-bootstrap','sync':'cpf domain sync'}
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
    setup=dsub.add_parser('setup',help='Domain Identity + DB Binding + Capability + Integration을 한 번에 구성')
    setup.add_argument('--name',required=True); setup.add_argument('--system-code',required=True); setup.add_argument('--table-prefix')
    setup.add_argument('--package-name'); setup.add_argument('--preset',default='standard-enterprise',choices=['minimal','standard-enterprise','full-enterprise','custom'])
    setup.add_argument('--no-online',action='store_true'); setup.add_argument('--batch',action='store_true'); setup.add_argument('--local-online-port',type=int)
    setup.add_argument('--persistence',choices=['none','jdbc','mybatis','jpa']); setup.add_argument('--cache',choices=['none','caffeine','redis','valkey'])
    setup.add_argument('--messaging',choices=['none','kafka','rabbitmq','jms','ibm-mq']); setup.add_argument('--object-storage',choices=['none','s3'])
    setup.add_argument('--security-profile',choices=['resource-server','browser-session-valkey','service-identity','oidc'])
    setup.add_argument('--http-client',dest='http_client',action=argparse.BooleanOptionalAction,default=None)
    setup.add_argument('--resilience',action=argparse.BooleanOptionalAction,default=None)
    setup.add_argument('--sample-transaction',dest='sample_transaction',action=argparse.BooleanOptionalAction,default=None)
    setup.add_argument('--vendor',choices=SUPPORTED_VENDORS); setup.add_argument('--host',default='127.0.0.1'); setup.add_argument('--port',type=int)
    setup.add_argument('--database-name'); setup.add_argument('--service-name'); setup.add_argument('--schema-name')
    setup.add_argument('--migration-user'); setup.add_argument('--runtime-user'); setup.add_argument('--migration-secret-env'); setup.add_argument('--runtime-secret-env')
    setup.add_argument('--domain-dependency',action='append',help='name:SYSTEM:operation1,operation2'); setup.add_argument('--external-client',action='append',help='name:client-id:capability')
    setup.add_argument('--definition-output'); setup.add_argument('--db-profile-output'); setup.add_argument('--output'); setup.add_argument('--preview',action='store_true'); setup.add_argument('--sync',action='store_true')
    regen=dsub.add_parser('regenerate'); regen.add_argument('domain'); regen.add_argument('--file'); regen.add_argument('--output')
    allgen=dsub.add_parser('generate-all'); allgen.add_argument('--definitions-root'); allgen.add_argument('--output-root')
    upgrade_parser=dsub.add_parser('upgrade'); upgrade_parser.add_argument('domain'); upgrade_parser.add_argument('--file'); upgrade_parser.add_argument('--output')
    restore_parser=dsub.add_parser('restore'); restore_parser.add_argument('--file',required=True); restore_parser.add_argument('--output')
    rem=dsub.add_parser('remove'); rem.add_argument('domain'); rem.add_argument('--file'); rem.add_argument('--output'); rem.add_argument('--apply',action='store_true',help='현재 Generator 입력과 동일한 Seed Source만 안전하게 제거'); rem.add_argument('--purge-definition',action='store_true',help='명시 승인 시 cpf-domain.yaml 정의까지 제거하여 선택 Domain을 완전히 해제')
    newp=dsub.add_parser('new',help='Public Workspace에 신규 Business Domain을 생성하고 자동 편입')
    newp.add_argument('--name',required=True); newp.add_argument('--system-code',required=True); newp.add_argument('--batch',action='store_true')
    dsub.add_parser('sync',help='Workspace Canonical Definition과 Generated Domain을 안전하게 동기화')

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
        if ns.command=='new': print_json(create_workspace_domain(root,ns.name,ns.system_code,ns.batch)); return 0
        if ns.command=='sync': print_json(sync_workspace_domains(root)); return 0
        if ns.command=='setup': print_json(setup_workspace_domain(root,ns)); return 0
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
