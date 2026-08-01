#!/usr/bin/env python3
"""Fail-closed ADM/BZA controller operationId and mutation permission contract gate."""
from __future__ import annotations
import argparse,csv,json,re,sys
from collections import Counter
from pathlib import Path

HTTP_MAPPING=re.compile(r'@(Get|Post|Put|Patch|Delete)Mapping\s*(?:\((.*?)\))?',re.S)
REQUEST_MAPPING=re.compile(r'@RequestMapping\s*\((.*?)\)',re.S)
OPERATION=re.compile(r'@Operation\s*\((.*?)\)',re.S)
OP_ID=re.compile(r'operationId\s*=\s*"([^"]+)"')
QUOTED=re.compile(r'"([^"]*)"')
METHOD_DECL=re.compile(r'(?m)^\s*(?:(?:public|protected|private)\s+)?(?:static\s+)?(?:final\s+)?(?:<[^>]+>\s*)?[\w<>, ?\[\].]+\s+(\w+)\s*\(')
MUTATING={'POST','PUT','PATCH','DELETE'}
PUBLIC_PREFIXES=('/adm/api/','/api/bza/')
PUBLIC_MUTATION_EXCEPTIONS={('POST','/adm/api/auth/login'),('POST','/adm/api/auth/logout'),('POST','/api/bza/auth/login'),('POST','/api/bza/auth/logout'),('POST','/api/bza/auth/refresh'),('POST','/api/bza/auth/sessions/{sessionId}/revoke'),('POST','/api/bza/auth/password/change')}
SECURITY_ANNOTATIONS=('@PreAuthorize','@Secured','@RolesAllowed','@CpfPermission','@CpfAuthorize')

class ContractError(RuntimeError): pass

def annotation_path(arguments:str|None)->str:
    if not arguments:return ''
    match=QUOTED.search(arguments)
    return match.group(1) if match else ''

def normalize(base:str,sub:str)->str:
    parts=[part.strip('/') for part in (base,sub) if part and part.strip('/')]
    return '/'+('/'.join(parts)) if parts else '/'

def class_base(text:str)->str:
    controller=text.find('class ')
    prefix=text[:controller if controller>=0 else len(text)]
    matches=list(REQUEST_MAPPING.finditer(prefix))
    return annotation_path(matches[-1].group(1)) if matches else ''

def operation_scope(text:str,mapping_end:int)->tuple[str,str]:
    declaration=METHOD_DECL.search(text,mapping_end)
    if not declaration:return text[mapping_end:mapping_end+2500],'<unknown>'
    return text[mapping_end:declaration.end()],declaration.group(1)

def security_sources(root:Path)->str:
    chunks=[]
    for module in ('cpf-admin','cpf-biz-admin'):
        source=root/module/'src/main/java'
        if not source.exists():continue
        for file in source.rglob('*.java'):
            lower=file.name.lower()
            if any(token in lower for token in ('authfilter','security','permission','authorization')):
                chunks.append(file.read_text(encoding='utf-8'))
    return '\n'.join(chunks)

def path_tokens(path:str)->tuple[str,...]:
    literal=path.split('/{',1)[0].rstrip('/')
    values=[path,literal]
    if literal.count('/')>=3: values.append('/'.join(literal.split('/')[:4]))
    return tuple(value for value in values if value)

def bza_manifest(root:Path)->dict:
    path=root/'cpf-tools/db/metadata/bza-permission-manifest.json'
    if not path.exists():return {}
    try:
        data=json.loads(path.read_text(encoding='utf-8'))
    except (OSError,json.JSONDecodeError) as error:
        raise ContractError(f'BZA permission manifest unreadable: {error}') from error
    if data.get('schemaVersion')!=2:return {}
    return data

def bza_permission_mapped(root:Path,verb:str,path:str)->bool:
    if not path.startswith('/api/bza/') or path.startswith('/api/bza/auth/'):
        return False
    data=bza_manifest(root)
    resources=data.get('apiResourceGroups') or {}
    actions=data.get('actionRules') or []
    relative=path[len('/api/bza/'):].strip('/').lower()
    prefixes=sorted((str(key).strip('/').lower() for key in resources),key=len,reverse=True)
    if not any(relative==prefix or relative.startswith(prefix+'/') for prefix in prefixes):return False
    return any(str(rule.get('method','')).strip() and str(rule.get('actionCode','')).strip() for rule in actions if isinstance(rule,dict))

def permission_mapped(root:Path,verb:str,path:str,scope:str,security_text:str)->bool:
    if any(annotation in scope for annotation in SECURITY_ANNOTATIONS):return True
    if bza_permission_mapped(root,verb,path):return True
    mappings=set(re.findall(r'BUTTON_BY_METHOD_PATH_PREFIX\.put\(\s*"(GET|POST|PUT|PATCH|DELETE) ([^"]+)"',security_text))
    for mapped_verb,mapped_path in mappings:
        if mapped_verb!=verb:continue
        if path==mapped_path or path.startswith(mapped_path.rstrip('/')+'/') or mapped_path.startswith(path.rstrip('/')+'/'):
            return True
    return False

def discover(root:Path)->list[dict[str,str]]:
    records=[]
    roots=[root/'cpf-admin/src/main/java/com/cpf/admin/opr',root/'cpf-biz-admin/src/main/java/com/cpf/bizadmin']
    for source in roots:
        if not source.exists():continue
        for file in sorted(source.rglob('*.java')):
            text=file.read_text(encoding='utf-8')
            if '@RestController' not in text:continue
            base=class_base(text)
            for mapping in HTTP_MAPPING.finditer(text):
                verb={'Get':'GET','Post':'POST','Put':'PUT','Patch':'PATCH','Delete':'DELETE'}[mapping.group(1)]
                path=normalize(base,annotation_path(mapping.group(2)))
                if not path.startswith(PUBLIC_PREFIXES):continue
                scope,method=operation_scope(text,mapping.end())
                operation=OPERATION.search(scope);operation_id=''
                if operation:
                    op_match=OP_ID.search(operation.group(1))
                    if op_match:operation_id=op_match.group(1)
                records.append({
                    'module':'cpf-admin' if '/cpf/admin/' in file.as_posix() else 'cpf-biz-admin',
                    'source':file.relative_to(root).as_posix(),'method':method,'http_method':verb,'path':path,
                    'operation_id':operation_id,'mutating':'true' if verb in MUTATING else 'false','scope':scope,
                })
    return records

def validate(root:Path,strict:bool)->tuple[list[dict[str,str]],list[str],list[str]]:
    records=discover(root);errors=[];warnings=[];security_text=security_sources(root)
    if not records:errors.append('no ADM/BZA public controller operations discovered')
    ids=[record['operation_id'] for record in records if record['operation_id']]
    duplicate_ids=[key for key,count in Counter(ids).items() if count>1]
    for operation_id in duplicate_ids:errors.append(f'duplicate operationId={operation_id}')
    key_counts=Counter((record['http_method'],record['path']) for record in records)
    for key,count in key_counts.items():
        if count>1:errors.append(f'duplicate HTTP contract={key[0]} {key[1]} count={count}')
    for record in records:
        label=f"{record['source']}:{record['method']} {record['http_method']} {record['path']}"
        if not record['operation_id']:errors.append(f'{label}: operationId missing')
        elif not re.fullmatch(r'[A-Za-z][A-Za-z0-9]{5,}',record['operation_id']):errors.append(f"{label}: invalid operationId={record['operation_id']}")
        if record['path'].startswith('/api/bza/') and not record['path'].startswith('/api/bza/auth/') and not bza_permission_mapped(root,record['http_method'],record['path']):
            errors.append(f'{label}: BZA permission manifest resource/action mapping missing')
        if record['mutating']=='true' and (record['http_method'],record['path']) not in PUBLIC_MUTATION_EXCEPTIONS and not permission_mapped(root,record['http_method'],record['path'],record['scope'],security_text):
            (errors if strict else warnings).append(f'{label}: mutation permission mapping missing')
    return records,errors,warnings

def write_report(path:Path,records:list[dict[str,str]])->None:
    path.parent.mkdir(parents=True,exist_ok=True)
    fields=['module','source','method','http_method','path','operation_id','mutating']
    with path.open('w',encoding='utf-8',newline='') as handle:
        writer=csv.DictWriter(handle,fieldnames=fields);writer.writeheader();writer.writerows({key:r[key] for key in fields} for r in records)

def main()->int:
    parser=argparse.ArgumentParser();parser.add_argument('--root',type=Path,default=Path.cwd());parser.add_argument('--strict',action='store_true');parser.add_argument('--report',type=Path)
    args=parser.parse_args();root=args.root.resolve();records,errors,warnings=validate(root,args.strict)
    if args.report:write_report(args.report,records)
    for message in errors:print('[FAIL]',message)
    for message in warnings:print('[WARN]',message)
    mutations=sum(record['mutating']=='true' for record in records)
    if errors:
        print(f'[FAIL] controller permission contract operations={len(records)} mutations={mutations} errors={len(errors)} warnings={len(warnings)} strict={args.strict}')
        return 1
    print(f'[PASS] controller permission contract operations={len(records)} operationIds={len(records)} mutations={mutations} warnings={len(warnings)} strict={args.strict}')
    return 0
if __name__=='__main__':
    try:raise SystemExit(main())
    except ContractError as error:print(f'[FAIL] {error}',file=sys.stderr);raise SystemExit(1)
