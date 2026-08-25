#!/usr/bin/env python3
"""CPF cross-platform user CLI. Windows/Linux launcher가 동일 command surface를 호출한다."""
from __future__ import annotations
import argparse, json, os, shutil, sys, tempfile, uuid, subprocess
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
                           verify_generated, verify_prebuilt_domain, verify_genericity, remove_owned, preflight, upgrade, restore,
                           SUPPORTED_VENDORS, _ddl, _migration, _seed, _rollback, _verify_sql,
                           managed_generator_root, load_domain_contract)
from cpf_customer_library_generator import (CustomerLibraryError, create_library, attach_library, sync_libraries, verify_library)

VERSION='6.4.0'

def repo_root(value: str|None)->Path:
    return Path(value).resolve() if value else DEFAULT_ROOT.resolve()

def generated_root_name(domain_name:str)->str:
    # Generated Customer Domain의 물리 Root는 CPF Root naming 표준에 따라 cpf-<domain>을 사용한다.
    return domain_name if domain_name.startswith('cpf-') else f'cpf-{domain_name}'

def canonical_domain_root(root:Path, domain_name:str)->Path:
    """Developer-Facing Source와 Gradle Domain 계약이 함께 사는 canonical root입니다."""
    return root/generated_root_name(domain_name)

def workspace_definitions(root:Path)->list[Path]:
    contracts=[]
    for path in root.glob('cpf-*/gradle.properties'):
        if path.is_file() and 'cpf.domain.contractVersion=' in path.read_text(encoding='utf-8-sig'):
            contracts.append(path)
    return sorted(contracts,key=lambda path:path.parent.name)


def _normalize_business_features(values, *, domain_name:str, fallback=None) -> list[str]:
    import re
    raw=list(values or fallback or ['sample'])
    result=[]
    for value in raw:
        feature=str(value).strip().lower().replace('-', '_')
        if not re.fullmatch(r'[a-z][a-z0-9_]{1,49}',feature):
            raise DomainError(f'Business Feature 형식 오류: {value}')
        if feature==domain_name:
            raise DomainError(f'Business Feature는 Domain 이름과 동일하게 사용할 수 없습니다: {feature}')
        if feature in result:
            raise DomainError(f'Business Feature 중복: {feature}')
        result.append(feature)
    if not result:
        raise DomainError('Business Feature는 1개 이상 필요합니다.')
    return result


def _golden_domain_definition(name:str,system_code:str,batch:bool,business_features:list[str]|None=None)->str:
    table_prefix=system_code.upper(); business_features=_normalize_business_features(business_features,domain_name=name)
    feature_lines=''.join(f'  - {feature}\n' for feature in business_features)
    return (
        '# CPF Domain create/setup의 일회성 Generator 입력입니다. 출력 Root에는 저장하지 않습니다.\n'
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
        'businessFeatures:\n'
        + feature_lines +
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


def create_workspace_domain(root:Path,name:str,system_code:str,batch:bool,business_features:list[str]|None=None)->dict:
    name=name.strip().lower(); system_code=system_code.strip().upper()
    if not name or not system_code: raise DomainError('domain name/systemCode가 필요합니다.')
    final_output=canonical_domain_root(root,name)
    if final_output.exists(): raise DomainError(f'Generated Domain project가 이미 존재합니다: {final_output}')
    business_features=_normalize_business_features(business_features,domain_name=name)
    body=_golden_domain_definition(name,system_code,batch,business_features)
    temp_parent=managed_generator_root(root)/'new'; temp_parent.mkdir(parents=True,exist_ok=True)
    # 먼저 외부 staging definition으로 dry-run하여 validation을 끝내고, 성공한 경우에만 canonical root를 생성합니다.
    with tempfile.TemporaryDirectory(prefix='cpf-domain-new-',dir=temp_parent) as td:
        stage=Path(td); definition=stage/'cpf-domain.yaml'; definition.write_text(body,encoding='utf-8',newline='\n')
        planned=dry_run(root,definition,final_output)
        if planned.get('status')!='DRY_RUN_PASS': raise DomainError(f'Generated Domain preflight failed: {name}')
    try:
        with tempfile.TemporaryDirectory(prefix='cpf-domain-new-apply-',dir=temp_parent) as td:
            definition=Path(td)/'cpf-domain.yaml'; definition.write_text(body,encoding='utf-8',newline='\n')
            verified=generate(root,definition,final_output)
        if verified.get('verify',{}).get('status')!='PASS': raise DomainError(f'Generated Domain verification failed: {name}')
    except Exception:
        if final_output.exists(): shutil.rmtree(final_output,ignore_errors=True)
        raise
    return {'status':'PASS','action':'DOMAIN_NEW','domain':name,'systemCode':system_code,'batch':batch,'businessFeatures':business_features,'developerContract':str(final_output/'gradle.properties'),'project':str(final_output),'generator':verified}


def _delete_approved_legacy_metadata(output:Path,candidates:list[str])->list[str]:
    allowed={'cpf-domain.yaml','cpf-generator.lock.json'}
    normalized=sorted(set(str(value).replace('\\','/') for value in candidates))
    if any(value not in allowed for value in normalized):
        raise DomainError(f'승인 범위를 벗어난 Generated metadata 삭제 후보입니다: {normalized}')
    removed=[]
    for relative in normalized:
        target=(output/relative).resolve()
        if target.parent != output.resolve():
            raise DomainError(f'Generated Root 밖의 삭제 경로를 거부합니다: {target}')
        if target.is_file():
            target.unlink()
            removed.append(relative)
    return removed


def sync_workspace_domains(root:Path,approve_generated_delete:bool=False)->dict:
    definitions=workspace_definitions(root)
    if not definitions: raise DomainError(f'Generated Domain gradle.properties 계약이 없습니다: {root}')
    results=[]
    for definition in definitions:
        d=load_domain_contract(definition); output=root/generated_root_name(d.name)
        if d.generation_mode == 'prebuilt':
            result=generate(root,definition,output)
            if result.get('status')=='VERIFICATION_PENDING_DELETE' and approve_generated_delete:
                removed=_delete_approved_legacy_metadata(output,list(result.get('deleteCandidates',[])))
                result=generate(root,definition,output)
                result['removed']=removed
                result['mutated']=bool(removed)
        elif not output.is_dir():
            result=generate(root,definition,output)
        else:
            try:
                result=upgrade(root,definition,output,apply_delete=approve_generated_delete)
            except DomainError as exc:
                if 'transient generation-state가 없습니다' not in str(exc): raise
                current=diff(root,definition,output)
                if not current.get('clean'):
                    raise DomainError(f'Fresh clone의 현재 Source가 Developer Contract/Template과 exact-match가 아닙니다: {current}')
                candidates=list(current.get('legacyMetadataFiles',[]))
                if candidates and not approve_generated_delete:
                    result={'domain':d.name,'status':'VERIFICATION_PENDING_DELETE','output':str(output),
                            'deleteCandidates':candidates,'deletePrecondition':'EXPLICIT_APPROVAL_REQUIRED','mutated':False}
                else:
                    removed=_delete_approved_legacy_metadata(output,candidates) if candidates else []
                    result=regenerate(root,definition,output)
                    result['removed']=removed
                    result['mutated']=bool(removed or result.get('restored'))
        results.append({'domain':d.name,'status':result.get('status'),'project':str(output),
                        'changed':result.get('changed',[]),'added':result.get('added',[]),
                        'deleteCandidates':result.get('deleteCandidates',[]),'mutated':result.get('mutated',result.get('status') not in {'VERIFICATION_PENDING_DELETE'})})
    pending=any(row['status']=='VERIFICATION_PENDING_DELETE' for row in results)
    return {'status':'VERIFICATION_PENDING_DELETE' if pending else 'PASS','action':'DOMAIN_SYNC',
            'approvedGeneratedDelete':approve_generated_delete,'count':len(results),'results':results}

def resolve_definition(root:Path, domain_name:str, file_value:str|None=None)->Path:
    """Developer-Facing gradle.properties 계약 또는 명시적인 일회성 입력을 사용한다."""
    if file_value:
        p=Path(file_value)
        p=p if p.is_absolute() else root/p
    else:
        p=canonical_domain_root(root,domain_name)/'gradle.properties'
    p=p.resolve()
    if not p.is_file(): raise DomainError(f'Generator definition이 없습니다. --file을 지정하세요: {p}')
    d=load_domain_contract(p)
    if d.name!=domain_name: raise DomainError(f'domain 인자와 definition 불일치: {domain_name} != {d.name}')
    return p

def definition_output(root:Path, file_value:str, output_value:str|None)->tuple[Path,Path]:
    definition=Path(file_value)
    if not definition.is_absolute(): definition=(root/definition).resolve()
    if not definition.is_file(): raise DomainError(f'Domain 입력 계약이 없습니다: {definition}')
    d=load_domain_contract(definition)
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
        sample_transaction:bool, local_online_port:int|None, business_features:list[str],
        dependencies:list[tuple[str,str,tuple[str,...]]], external_clients:list[tuple[str,str,str]]) -> str:
    package_line = f"  packageName: {package_name}\n" if package_name else ""
    lines=[
      "# CPF Domain setup의 일회성 Generator 입력입니다. 출력 Root에는 저장하지 않습니다.",
      "# DB Vendor/Host/계정/Secret은 local/environment binding profile이 소유합니다.",
      "domain:", f"  name: {name}", f"  systemCode: {system_code}",
    ]
    if package_name: lines.append(f"  packageName: {package_name}")
    lines += [
      "database:", "  role: CUSTOMER_BUSINESS_DB", f"  tablePrefix: {table_prefix}",
      f"preset: {preset}", "modules:", f"  online: {_yaml_bool(online)}", f"  batch: {_yaml_bool(batch)}",
      "businessFeatures:", *[f"  - {feature}" for feature in business_features],
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


def _validated_secret_env_ref(value:str|None, default_value:str) -> str:
    candidate=(value or default_value).strip()
    # Secret 원문을 "env reference" 이름으로 오인하지 않도록 환경변수 식별자만 허용합니다.
    import re
    if not re.fullmatch(r"[A-Z][A-Z0-9_]{2,127}", candidate):
        raise DomainError("Secret은 원문이 아니라 대문자 ENV reference 이름으로만 입력해야 합니다.")
    return candidate


def _dependency_tuples(d) -> list[tuple[str,str,tuple[str,...]]]:
    return [(x.name,x.system_code,tuple(x.operations)) for x in d.domain_dependencies]


def _external_client_tuples(d) -> list[tuple[str,str,str]]:
    return [(x.name,x.client_id,x.capability) for x in d.external_clients]


def _semantic_setup_snapshot(d) -> dict:
    return {
      'domain':{'name':d.name,'systemCode':d.system_code,'packageName':d.package_name},
      'database':{'role':d.database_role,'tablePrefix':d.table_prefix},
      'preset':d.preset,
      'modules':{'online':d.online,'batch':d.batch},
      'businessFeatures':list(d.business_features),
      'features':{'persistence':d.persistence,'httpClient':d.http_client,'resilience':d.resilience,
                  'cache':d.cache,'messaging':d.messaging,'objectStorage':d.object_storage,'securityProfile':d.security_profile},
      'generation':{'sampleTransaction':d.sample_transaction},
      'runtime':{'localOnlinePort':d.local_online_port},
      'domainDependencies':{x.name:{'systemCode':x.system_code,'operations':list(x.operations)} for x in d.domain_dependencies},
      'externalClients':{x.name:{'id':x.client_id,'capability':x.capability} for x in d.external_clients},
    }


def _risky_setup_changes(before:dict|None, after:dict) -> list[str]:
    if before is None: return []
    risks=[]
    for key in ('name','systemCode','packageName'):
        if before['domain'].get(key)!=after['domain'].get(key): risks.append(f'domain.{key}')
    if before['modules'].get('online') and not after['modules'].get('online'): risks.append('modules.online:disable')
    if before['modules'].get('batch') and not after['modules'].get('batch'): risks.append('modules.batch:disable')
    if before['features'].get('persistence')!=after['features'].get('persistence'): risks.append('features.persistence')
    if before['database'].get('tablePrefix')!=after['database'].get('tablePrefix'): risks.append('database.tablePrefix')
    old_features=set(before.get('businessFeatures',[])); new_features=set(after.get('businessFeatures',[]))
    risks += [f'businessFeatures.remove:{x}' for x in sorted(old_features-new_features)]
    old_deps=set(before['domainDependencies']); new_deps=set(after['domainDependencies'])
    old_clients=set(before['externalClients']); new_clients=set(after['externalClients'])
    risks += [f'domainDependencies.remove:{x}' for x in sorted(old_deps-new_deps)]
    risks += [f'externalClients.remove:{x}' for x in sorted(old_clients-new_clients)]
    if before['runtime'].get('localOnlinePort') and not after['runtime'].get('localOnlinePort'): risks.append('runtime.localOnlinePort:remove')
    return risks


def setup_workspace_domain(root:Path, ns) -> dict:
    requested_name=ns.name.strip().lower(); requested_system=ns.system_code.strip().upper()
    output=Path(ns.output).resolve() if ns.output else canonical_domain_root(root,requested_name)
    developer_contract=output/'gradle.properties'
    legacy_definition=output/'cpf-domain.yaml'
    explicit_definition=Path(ns.definition_output).resolve() if ns.definition_output else None
    existing_contract=(developer_contract if developer_contract.is_file() and 'cpf.domain.contractVersion=' in developer_contract.read_text(encoding='utf-8-sig')
                       else (explicit_definition if explicit_definition and explicit_definition.is_file()
                             else (legacy_definition if legacy_definition.is_file() else None)))
    existing=load_domain_contract(existing_contract) if existing_contract else None
    if existing is not None and not ns.sync:
        raise DomainError(f"Domain 계약이 이미 존재합니다: {existing_contract}; 변경은 --sync 또는 domain sync를 사용하세요.")
    if output.exists() and existing is None and not ns.sync:
        raise DomainError(f"Generated Domain project가 이미 존재합니다: {output}; 변경은 --sync 또는 domain sync를 사용하세요.")

    # Identity는 sync에서 임의 재계산하지 않습니다. 현재 identity와 다르면 별도 migration 없이 변경할 수 없습니다.
    name=existing.name if existing else requested_name
    system_code=existing.system_code if existing else requested_system
    if existing and requested_name != existing.name:
        raise DomainError(f"sync에서 domain.name 변경은 지원하지 않습니다: {existing.name} -> {requested_name}")
    if existing and requested_system != existing.system_code:
        raise DomainError(f"sync에서 systemCode 변경은 지원하지 않습니다: {existing.system_code} -> {requested_system}")

    table_prefix=(ns.table_prefix.strip().upper() if ns.table_prefix else (existing.table_prefix if existing else system_code))
    package_name=(ns.package_name.strip() if ns.package_name else (existing.package_name if existing else None))
    online=(ns.online if getattr(ns,'online',None) is not None else (existing.online if existing else True))
    batch=(ns.batch if getattr(ns,'batch',None) is not None else (existing.batch if existing else False))
    if not online and not batch: raise DomainError("--no-online 사용 시 --batch가 필요합니다.")

    preset_defaults={
      'minimal':dict(persistence='none',http_client=False,resilience=False,cache='none',messaging='none',object_storage='none',security_profile='resource-server',sample_transaction=False),
      'standard-enterprise':dict(persistence='mybatis',http_client=True,resilience=True,cache='none',messaging='none',object_storage='none',security_profile='resource-server',sample_transaction=True),
      'full-enterprise':dict(persistence='mybatis',http_client=True,resilience=True,cache='valkey',messaging='kafka',object_storage='s3',security_profile='resource-server',sample_transaction=True),
      'custom':dict(persistence='none',http_client=False,resilience=False,cache='none',messaging='none',object_storage='none',security_profile='resource-server',sample_transaction=False),
    }
    preset=ns.preset or (existing.preset if existing else 'standard-enterprise')
    if existing and ns.preset is None:
        selected=dict(persistence=existing.persistence,http_client=existing.http_client,resilience=existing.resilience,
                      cache=existing.cache,messaging=existing.messaging,object_storage=existing.object_storage,
                      security_profile=existing.security_profile,sample_transaction=existing.sample_transaction)
    else:
        selected=preset_defaults[preset].copy()
    for key,attr in [('persistence','persistence'),('cache','cache'),('messaging','messaging'),('object_storage','object_storage'),('security_profile','security_profile')]:
        value=getattr(ns,attr,None)
        if value is not None: selected[key]=value
    if ns.http_client is not None: selected['http_client']=ns.http_client
    if ns.resilience is not None: selected['resilience']=ns.resilience
    if ns.sample_transaction is not None: selected['sample_transaction']=ns.sample_transaction
    if selected['sample_transaction'] and not online: raise DomainError("sample transaction은 Online Runtime이 필요합니다.")

    existing_features=list(existing.business_features) if existing else ['sample']
    business_features=_normalize_business_features(
        ns.business_feature if getattr(ns,'business_feature',None) is not None else existing_features,
        domain_name=name)

    if ns.clear_domain_dependencies:
        dependencies=[]
    elif ns.domain_dependency is not None:
        dependencies=[_parse_domain_dependency_arg(x) for x in ns.domain_dependency]
    else:
        dependencies=_dependency_tuples(existing) if existing else []
    if ns.clear_external_clients:
        external_clients=[]
    elif ns.external_client is not None:
        external_clients=[_parse_external_client_arg(x) for x in ns.external_client]
    else:
        external_clients=_external_client_tuples(existing) if existing else []
    old_local_port=existing.local_online_port if existing else None
    local_online_port=(ns.local_online_port if ns.local_online_port is not None
                       else (None if ns.clear_local_online_port else old_local_port))

    definition_body=_domain_setup_definition(
        name,system_code,table_prefix,package_name,preset,online,batch,
        selected['persistence'],selected['http_client'],selected['resilience'],selected['cache'],selected['messaging'],selected['object_storage'],selected['security_profile'],selected['sample_transaction'],local_online_port,business_features,
        dependencies,external_clients)

    profile=None; profile_payload=None
    existing_profile_path=Path(ns.db_profile_output).resolve() if ns.db_profile_output else managed_generator_root(root)/'cpf-local'/name/'cpf-db-profile.local.json'
    existing_profile=json.loads(existing_profile_path.read_text(encoding='utf-8')) if existing_profile_path.is_file() else None
    if selected['persistence']!='none':
        existing_db=(existing_profile or {}).get('database',{}) if isinstance(existing_profile,dict) else {}
        vendor=ns.vendor or existing_db.get('vendor')
        if not vendor: raise DomainError("persistence 사용 시 --vendor 또는 기존 DB Profile이 필요합니다.")
        if vendor not in SUPPORTED_VENDORS: raise DomainError(f"지원하지 않는 DB Vendor입니다: {vendor}")
        host=ns.host or existing_db.get('host') or ('127.0.0.1' if existing is None else None)
        if not host: raise DomainError("persistence 사용 시 DB host가 필요합니다.")
        default_ports={'mariadb':3306,'postgresql':5432,'oracle':1521}; port=ns.port or existing_db.get('port') or default_ports[vendor]
        if not (1 <= int(port) <= 65535): raise DomainError(f"DB port 범위가 올바르지 않습니다: {port}")
        database_name=ns.database_name or existing_db.get('databaseName','')
        service_name=ns.service_name or existing_db.get('serviceName','')
        schema_name=ns.schema_name or existing_db.get('schemaName')
        if vendor!='oracle' and not database_name: raise DomainError(f"{vendor} persistence는 --database-name이 필요합니다.")
        if vendor=='oracle' and not (service_name or database_name): raise DomainError("oracle persistence는 --service-name 또는 --database-name이 필요합니다.")
        if not schema_name: raise DomainError("persistence 사용 시 --schema-name이 필요합니다.")
        old_migration=(existing_db.get('migration') or {}) if isinstance(existing_db,dict) else {}
        old_runtime=(existing_db.get('runtime') or {}) if isinstance(existing_db,dict) else {}
        migration_user=ns.migration_user or old_migration.get('username') or f"cpf_{system_code.lower()}_migration"
        runtime_user=ns.runtime_user or old_runtime.get('username') or f"cpf_{system_code.lower()}_runtime"
        if migration_user==runtime_user: raise DomainError("Migration 계정과 Runtime 계정은 분리해야 합니다.")
        old_mig_env=((old_migration.get('password') or {}).get('env') if isinstance(old_migration.get('password'),dict) else None)
        old_run_env=((old_runtime.get('password') or {}).get('env') if isinstance(old_runtime.get('password'),dict) else None)
        migration_secret=_validated_secret_env_ref(ns.migration_secret_env,old_mig_env or f"{system_code}_DB_MIGRATION_PASSWORD")
        runtime_secret=_validated_secret_env_ref(ns.runtime_secret_env,old_run_env or f"{system_code}_DB_RUNTIME_PASSWORD")
        profile=existing_profile_path
        profile_payload=_domain_setup_profile(name,system_code,vendor,host,int(port),database_name,service_name,schema_name,migration_user,runtime_user,migration_secret,runtime_secret)
    elif any([ns.vendor,ns.host,ns.port,ns.database_name,ns.service_name,ns.schema_name,ns.migration_user,ns.runtime_user,ns.migration_secret_env,ns.runtime_secret_env]):
        raise DomainError("features.persistence=none이면 DB Binding 입력을 함께 사용할 수 없습니다.")

    with tempfile.TemporaryDirectory(prefix=f"cpf-domain-setup-{name}-") as td:
        stage=Path(td); staged_def=stage/'cpf-domain.yaml'; staged_def.write_text(definition_body,encoding='utf-8',newline='\n')
        d=validate_definition(load_yaml_subset(staged_def)); after=_semantic_setup_snapshot(d); before=_semantic_setup_snapshot(existing) if existing else None
        risky=_risky_setup_changes(before,after)
        planned=dry_run(root,staged_def,root/generated_root_name(name)) if not output.exists() else {'status':'EXISTING'}
        preview={
          'domain':{'name':d.name,'systemCode':d.system_code,'packageName':d.package_name,'online':d.online,'batch':d.batch},
          'database':profile_payload['database'] if profile_payload else None,
          'capabilities':{'persistence':d.persistence,'cache':d.cache,'messaging':d.messaging,'objectStorage':d.object_storage,'httpClient':d.http_client,'resilience':d.resilience,'securityProfile':d.security_profile},
          'domainDependencies':[{'name':x.name,'systemCode':x.system_code,'operations':list(x.operations)} for x in d.domain_dependencies],
          'externalClients':[{'name':x.name,'id':x.client_id,'capability':x.capability} for x in d.external_clients],
          'riskyChanges':risky,'selectionSummary':planned.get('selectionSummary')
        }
        if ns.preview:
            return {'status':'PREVIEW','preview':preview,'developerContract':str(developer_contract),'dbProfile':str(profile) if profile else None,'output':str(output)}
        if risky and not ns.approve_risky_change:
            raise DomainError("위험 변경은 --preview로 diff를 확인한 뒤 --approve-risky-change를 명시해야 합니다: "+", ".join(risky))

        previous_profile=profile.read_bytes() if profile and profile.exists() else None
        # Generator가 실패해도 generated-owned tree가 부분 갱신되지 않도록 기존 project를 stage에 보존합니다.
        backup_project=stage/'project-backup'
        if output.exists():
            shutil.copytree(output,backup_project,dirs_exist_ok=True)
        if profile and profile_payload:
            profile.parent.mkdir(parents=True,exist_ok=True)
            profile.write_text(json.dumps(profile_payload,ensure_ascii=False,indent=2)+'\n',encoding='utf-8',newline='\n')
        try:
            materialized = output.is_dir() and any(output.iterdir())
            if materialized:
                try:
                    generated=upgrade(root,staged_def,output,apply_delete=bool(ns.approve_risky_change))
                except DomainError as exc:
                    if 'transient generation-state가 없습니다' not in str(exc) or existing_contract is None:
                        raise
                    # Fresh clone에서는 현재 Developer 계약과 Source가 exact-match일 때만 transient ownership을 재구축합니다.
                    regenerate(root,existing_contract,output)
                    generated=upgrade(root,staged_def,output,apply_delete=bool(ns.approve_risky_change))
            else:
                generated=generate(root,staged_def,output)
            if generated.get('status')=='VERIFICATION_PENDING_DELETE':
                raise DomainError('Generated 파일 삭제가 필요한 변경입니다. --preview 확인 후 --approve-risky-change를 명시하세요: '+', '.join(generated.get('deleteCandidates',[])))
        except Exception:
            if backup_project.is_dir():
                # temp working copy에서만 rollback하며 user-owned generated tree를 원상복구합니다.
                shutil.rmtree(output,ignore_errors=True); shutil.copytree(backup_project,output,dirs_exist_ok=True)
            if profile:
                if previous_profile is None: profile.unlink(missing_ok=True)
                else: profile.write_bytes(previous_profile)
            raise
    return {
      'status':'PASS','action':'DOMAIN_SETUP','domain':name,'systemCode':system_code,
      'developerContract':str(developer_contract),'dbProfile':str(profile) if profile else None,'project':str(output),
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
    dsub=domain.add_subparsers(dest='command',required=True,metavar='{create,setup,sync,diff,remove}')
    for command in ('generate','add','dry-run','diff','validate'):
        help_text = None if command == 'diff' else argparse.SUPPRESS
        sp=dsub.add_parser(command, help=help_text); sp.add_argument('--file',required=True); sp.add_argument('--output')
    setup=dsub.add_parser('setup',help='Domain Identity + DB Binding + Capability + Integration을 한 번에 구성')
    setup.add_argument('--name'); setup.add_argument('--system-code'); setup.add_argument('--interactive',action='store_true',help='누락된 Setup 값을 대화형으로 입력'); setup.add_argument('--table-prefix')
    setup.add_argument('--package-name'); setup.add_argument('--business-feature',action='append',default=None,help='업무 Feature 이름. 여러 개면 옵션을 반복 지정'); setup.add_argument('--preset',default=None,choices=['minimal','standard-enterprise','full-enterprise','custom'])
    setup.add_argument('--online',action=argparse.BooleanOptionalAction,default=None); setup.add_argument('--batch',action=argparse.BooleanOptionalAction,default=None); setup.add_argument('--local-online-port',type=int); setup.add_argument('--clear-local-online-port',action='store_true')
    setup.add_argument('--persistence',choices=['none','jdbc','mybatis','jpa']); setup.add_argument('--cache',choices=['none','caffeine','redis','valkey'])
    setup.add_argument('--messaging',choices=['none','kafka','rabbitmq','jms','ibm-mq']); setup.add_argument('--object-storage',choices=['none','s3'])
    setup.add_argument('--security-profile',choices=['resource-server','browser-session-valkey','service-identity','oidc'])
    setup.add_argument('--http-client',dest='http_client',action=argparse.BooleanOptionalAction,default=None)
    setup.add_argument('--resilience',action=argparse.BooleanOptionalAction,default=None)
    setup.add_argument('--sample-transaction',dest='sample_transaction',action=argparse.BooleanOptionalAction,default=None)
    setup.add_argument('--vendor',choices=SUPPORTED_VENDORS); setup.add_argument('--host'); setup.add_argument('--port',type=int)
    setup.add_argument('--database-name'); setup.add_argument('--service-name'); setup.add_argument('--schema-name')
    setup.add_argument('--migration-user'); setup.add_argument('--runtime-user'); setup.add_argument('--migration-secret-env'); setup.add_argument('--runtime-secret-env')
    setup.add_argument('--domain-dependency',action='append',default=None,help='name:SYSTEM:operation1,operation2'); setup.add_argument('--clear-domain-dependencies',action='store_true')
    setup.add_argument('--external-client',action='append',default=None,help='name:client-id:capability'); setup.add_argument('--clear-external-clients',action='store_true')
    setup.add_argument('--definition-output',help=argparse.SUPPRESS); setup.add_argument('--db-profile-output'); setup.add_argument('--output'); setup.add_argument('--preview',action='store_true'); setup.add_argument('--sync',action='store_true'); setup.add_argument('--approve-risky-change',action='store_true')
    regen=dsub.add_parser('regenerate',help=argparse.SUPPRESS); regen.add_argument('domain'); regen.add_argument('--file'); regen.add_argument('--output')
    allgen=dsub.add_parser('generate-all',help=argparse.SUPPRESS); allgen.add_argument('--definitions-root'); allgen.add_argument('--output-root')
    upgrade_parser=dsub.add_parser('upgrade',help=argparse.SUPPRESS); upgrade_parser.add_argument('domain'); upgrade_parser.add_argument('--file'); upgrade_parser.add_argument('--output')
    restore_parser=dsub.add_parser('restore',help=argparse.SUPPRESS); restore_parser.add_argument('--file',required=True); restore_parser.add_argument('--output')
    rem=dsub.add_parser('remove'); rem.add_argument('domain'); rem.add_argument('--file'); rem.add_argument('--output'); rem.add_argument('--apply',action='store_true',help='현재 Generator 입력과 동일한 Seed Source만 안전하게 제거'); rem.add_argument('--purge-definition',action='store_true',help=argparse.SUPPRESS)
    createp=dsub.add_parser('create',help='Public Workspace에 신규 Business Domain을 생성하고 자동 편입')
    createp.add_argument('--name',required=True); createp.add_argument('--system-code',required=True); createp.add_argument('--batch',action='store_true'); createp.add_argument('--business-feature',action='append',default=None,help='업무 Feature 이름. 여러 개면 옵션을 반복 지정')
    newp=dsub.add_parser('new',help=argparse.SUPPRESS)
    newp.add_argument('--name',required=True); newp.add_argument('--system-code',required=True); newp.add_argument('--batch',action='store_true'); newp.add_argument('--business-feature',action='append',default=None)
    sync_parser=dsub.add_parser('sync',help='Workspace Developer Domain 계약과 Generated Source를 안전하게 동기화')
    sync_parser.add_argument('--approve-generated-delete',action='store_true',
                             help='preview된 root legacy Generator metadata만 exact allowlist로 삭제')
    # argparse keeps suppressed subcommands in the positional help list. Hide legacy/advanced aliases
    # from the Golden Path while keeping them callable for compatibility.
    _hidden_domain_commands={'generate','add','dry-run','validate','regenerate','generate-all','upgrade','restore','new'}
    dsub._choices_actions[:] = [action for action in dsub._choices_actions if getattr(action,'dest',None) not in _hidden_domain_commands]

    library=sub.add_parser('library',help='고객사 공통 JAR 작업공간 생성·선택 연결')
    lsub=library.add_subparsers(dest='command',required=True)
    lcreate=lsub.add_parser('create',help='고객사 공통 Library JAR 프로젝트 생성')
    lcreate.add_argument('--name',required=True); lcreate.add_argument('--group',required=True,help='고객사 소유 Java/Maven group. 예: com.acme.shared'); lcreate.add_argument('--package',help='공통 Source Java package. 생략하면 <group>.<library_name>으로 생성'); lcreate.add_argument('--version',default='1.0.0-SNAPSHOT')
    lattach=lsub.add_parser('attach',help='필요한 Generated Domain에만 Library Dependency 연결')
    lattach.add_argument('--name',required=True); lattach.add_argument('--domain',action='append',required=True,help='연결할 Domain 이름. 여러 개면 반복 지정')
    lsub.add_parser('sync',help='Domain의 customer-libraries.properties 기준 연결 파일 재생성')
    lverify=lsub.add_parser('verify',help='고객사 공통 Library 구조/경계 검증'); lverify.add_argument('--name',required=True)

    db=sub.add_parser('db',help='Generated Domain DB3 renderer')
    dbsub=db.add_subparsers(dest='command',required=True)
    render=dbsub.add_parser('render'); render.add_argument('--file',required=True); render.add_argument('--vendor',required=True,choices=SUPPORTED_VENDORS); render.add_argument('--output')

    verify=sub.add_parser('verify',help='CPF verification entrypoints')
    vsub=verify.add_subparsers(dest='command',required=True)
    vsub.add_parser('generator')
    va=vsub.add_parser('domain'); va.add_argument('--file',required=True); va.add_argument('--output')
    vsub.add_parser('all')

    open_git=sub.add_parser('open-git',help='Open Git release package')
    open_git.add_argument('command',nargs='?',default='build',choices=['build','check','status'])

    ns=p.parse_args(); root=repo_root(ns.root)
    if ns.group=='open-git':
        tool=root/'cpf-tools/release/open-git/cpf_open_git.py'
        if not tool.is_file(): raise DomainError(f'Open Git release tool이 없습니다: {tool}')
        return subprocess.run([sys.executable,str(tool),ns.command,'--root',str(root)],cwd=root,check=False).returncode
    if ns.group=='domain':
        if ns.command in {'generate','add','dry-run','validate','regenerate','generate-all','upgrade','restore','new'}:
            print(f'[CPF][DEPRECATED] domain {ns.command} is a compatibility/advanced command; use create/setup/sync/diff/remove for new workflows.', file=sys.stderr)
        if ns.command in ('create','new'): print_json(create_workspace_domain(root,ns.name,ns.system_code,ns.batch,getattr(ns,'business_feature',None))); return 0
        if ns.command=='sync': print_json(sync_workspace_domains(root,ns.approve_generated_delete)); return 0
        if ns.command=='setup':
            if ns.interactive:
                if not sys.stdin.isatty(): raise CpfCliError('Interactive setup requires a TTY')
                if not ns.name: ns.name=input('Domain name: ').strip()
                if not ns.system_code: ns.system_code=input('System code: ').strip().upper()
                if ns.vendor is None:
                    entered=input('DB vendor [postgresql/oracle/mariadb, blank=keep/default]: ').strip().lower()
                    if entered: ns.vendor=entered
            if not ns.name or not ns.system_code:
                raise CpfCliError('domain setup requires --name and --system-code, or use --interactive')
            print_json(setup_workspace_domain(root,ns)); return 0
        if ns.command in ('generate','add','dry-run','diff','validate'):
            definition,output=definition_output(root,ns.file,ns.output)
            if ns.command in {'generate','add'}: print_json(generate(root,definition,output)); return 0
            if ns.command=='dry-run': print_json(dry_run(root,definition,output)); return 0
            if ns.command=='diff': print_json(diff(root,definition,output)); return 0
            print_json(preflight(root,definition,output)); return 0
        if ns.command=='generate-all':
            output_root=Path(ns.output_root).resolve() if ns.output_root else root
            if ns.definitions_root:
                definitions_root=Path(ns.definitions_root).resolve()
                defs=sorted(p for p in definitions_root.glob('cpf-*/cpf-domain.yaml') if p.is_file())
            else:
                definitions_root=root
                defs=workspace_definitions(root)
            if not defs: raise DomainError(f'생성할 Generated Domain definition이 없습니다: {definitions_root}')
            results=[]
            for definition in defs:
                d=load_domain_contract(definition); results.append(generate(root,definition,output_root/generated_root_name(d.name)))
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
    if ns.group=='library':
        if ns.command=='create': print_json(create_library(root,ns.name,ns.group,getattr(ns,'package',None),ns.version)); return 0
        if ns.command=='attach': print_json(attach_library(root,ns.name,ns.domain)); return 0
        if ns.command=='sync': print_json(sync_libraries(root)); return 0
        if ns.command=='verify': print_json(verify_library(root,ns.name)); return 0
    if ns.group=='db' and ns.command=='render':
        definition=Path(ns.file); definition=definition if definition.is_absolute() else root/definition
        d=load_domain_contract(definition.resolve()); vendor=ns.vendor
        out=Path(ns.output).resolve() if ns.output else managed_generator_root(root)/'verification'/generated_root_name(d.name)/'db3'/vendor
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
            definition,output=definition_output(root,ns.file,ns.output); d=load_domain_contract(definition)
            verifier=verify_prebuilt_domain if d.generation_mode=='prebuilt' else verify_generated
            print_json(verifier(root,definition,output,d)); return 0
        if ns.command=='all':
            generic=verify_genericity(root/'cpf-tools/generator'); results={'generator':generic,'domains':[]}
            for definition in workspace_definitions(root):
                d=load_domain_contract(definition); child=root/generated_root_name(d.name)
                if not child.is_dir(): continue
                verifier=verify_prebuilt_domain if d.generation_mode=='prebuilt' else verify_generated
                results['domains'].append(verifier(root,definition,child,d))
            accepted={'PASS','PREBUILT_VERIFIED'}
            results['status']='PASS' if generic['status']=='PASS' and all(x['status'] in accepted for x in results['domains']) else 'FAIL'
            print_json(results); return 0 if results['status']=='PASS' else 2
    raise DomainError('지원하지 않는 명령입니다.')

if __name__=='__main__':
    try: raise SystemExit(main())
    except (DomainError,CustomerLibraryError,OSError,ValueError) as exc:
        print(f'CPF_CLI=FAIL {exc}',file=sys.stderr); raise SystemExit(2)
