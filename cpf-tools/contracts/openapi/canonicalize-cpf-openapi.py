#!/usr/bin/env python3
"""Canonicalize runtime OpenAPI without Git-SHA self references and enforce CPF product contracts."""
from __future__ import annotations
import sys as _cpf_sys

# Windows cp949 콘솔에서 한글 진단 메시지가 깨지지 않도록 자기 출력 스트림을 UTF-8 로 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    if hasattr(_cpf_stream, 'reconfigure'):
        _cpf_stream.reconfigure(encoding='utf-8')
import argparse,hashlib,json,re,sys
from pathlib import Path
HTTP_METHODS={'get','post','put','patch','delete','head','options','trace'}
ERROR_CODES=('401','403','404','409','429','500','503')
MODULE_PREFIX={'ADM':'/adm/api/','MBW':'/api/v1/backoffice/'}
class ContractError(RuntimeError):pass

def canonicalize(spec:dict,module:str,release:bool=False)->tuple[dict,list[str]]:
    module=module.upper()
    if module not in MODULE_PREFIX:raise ContractError(f'unsupported module={module}')
    if str(spec.get('openapi','')).split('.')[0] != '3':raise ContractError('OpenAPI 3.x required')
    for key in ('x-cpf-source-sha','x-cpf-result-sha'):spec.pop(key,None)
    spec.pop('servers',None)
    components=spec.setdefault('components',{})
    schemas=components.setdefault('schemas',{})
    schemas.setdefault('CpfApiError',{
        'type':'object','required':['status','code','message','timestamp'],
        'properties':{
            'status':{'type':'integer','format':'int32'},'code':{'type':'string'},'message':{'type':'string'},
            'transactionId':{'type':'string'},'timestamp':{'type':'string','format':'date-time'},
            'details':{'type':'object','additionalProperties':True}
        }
    })
    descriptions={'401':'Authentication required','403':'Permission or data-scope denied','404':'Resource not found',
                  '409':'State, version, or idempotency conflict','429':'Rate limit exceeded',
                  '500':'Unexpected server error','503':'Dependency unavailable or partial failure'}
    responses=components.setdefault('responses',{})
    for code in ERROR_CODES:
        responses.setdefault(f'Cpf{code}',{'description':descriptions[code],'content':{'application/json':{'schema':{'$ref':'#/components/schemas/CpfApiError'}}}})
    schemes=components.setdefault('securitySchemes',{})
    if module == 'ADM':
        schemes.setdefault('cpfSession',{'type':'apiKey','in':'cookie','name':'JSESSIONID','description':'Same-origin CPF administrator session'})
        public_security=[{'cpfSession':[]}]
    else:
        schemes.setdefault('cpfBearer',{'type':'http','scheme':'bearer','bearerFormat':'JWT','description':'Backoffice Web/BFF authenticated channel credential for MBW Domain'})
        public_security=[{'cpfBearer':[]}]
    ids=[];warnings=[];public_operations=0;empty_success=[]
    prefix=MODULE_PREFIX[module]
    for path,item in sorted((spec.get('paths') or {}).items()):
        if not isinstance(item,dict):continue
        for method,operation in sorted(item.items()):
            method=method.lower()
            if method not in HTTP_METHODS:continue
            if not isinstance(operation,dict) or not operation.get('operationId'):
                raise ContractError(f'operationId missing: {method.upper()} {path}')
            operation_id=str(operation['operationId'])
            # CPF 업무 Operation ID 정본 표기는 MBW_AUTH_LOGIN / EDU_LOCAL_MEMBER_PROCESS 처럼
            # 밑줄을 쓴다(@CpfOnlineTransaction / X-Target-Operation-Id / Operation Catalog 동일).
            # ADM 은 springdoc 이 method 이름에서 만든 camelCase 를 쓴다. 둘 다 정본이다.
            # 이 규칙이 실제로 막아야 하는 것은 springdoc 이 한 @Operation 을 여러 경로에
            # 매핑했을 때 붙이는 중복 회피 접미사(admPageAdminPage_1 / getAdmReadiness_1)다.
            # 밑줄 전체를 금지하면 정상 업무 Operation 96건이 함께 막힌다.
            if not re.fullmatch(r'[A-Za-z][A-Za-z0-9_]{5,}',operation_id) or re.search(r'_\d+$',operation_id):
                raise ContractError(f'invalid operationId={operation_id}')
            ids.append(operation_id)
            operation_responses=operation.setdefault('responses',{})
            successes=[code for code in operation_responses if re.fullmatch(r'2\d\d',str(code))]
            if not successes:raise ContractError(f'2xx response missing: {operation_id}')
            for code in successes:
                content=(operation_responses.get(code) or {}).get('content') or {}
                for media in content.values():
                    schema=(media or {}).get('schema')
                    if schema == {} or schema is None:empty_success.append(f'{operation_id}:{code}')
            if path.startswith(prefix):
                public_operations+=1
                operation['security']=public_security
                for code in ERROR_CODES:operation_responses.setdefault(code,{'$ref':f'#/components/responses/Cpf{code}'})
    if not ids:raise ContractError('empty OpenAPI operation inventory')
    duplicates=sorted({value for value in ids if ids.count(value)>1})
    if duplicates:raise ContractError(f'duplicate operationId={duplicates[:20]}')
    if public_operations < 1:raise ContractError(f'no public {module} operations under {prefix}')
    if empty_success:
        message=f'empty success DTO schemas={empty_success[:20]} count={len(empty_success)}'
        if release:raise ContractError(message)
        warnings.append(message)
    ids_sorted=sorted(ids)
    spec['x-cpf-export-origin']='BACKEND_RUNTIME'
    spec['x-cpf-product-module']=module
    spec['x-cpf-openapi-operation-count']=len(ids)
    spec['x-cpf-public-operation-count']=public_operations
    spec['x-cpf-operation-ids-sha256']=hashlib.sha256('\n'.join(ids_sorted).encode()).hexdigest()
    spec['x-cpf-canonical-schema-version']=5
    spec['x-cpf-release-eligible']=bool(release)
    return spec,warnings

def main()->int:
    parser=argparse.ArgumentParser();parser.add_argument('--input',required=True,type=Path);parser.add_argument('--output',required=True,type=Path);parser.add_argument('--module',required=True);parser.add_argument('--release',action='store_true')
    args=parser.parse_args();spec=json.loads(args.input.read_text(encoding='utf-8-sig'));canonical,warnings=canonicalize(spec,args.module,args.release)
    args.output.parent.mkdir(parents=True,exist_ok=True)
    args.output.write_text(json.dumps(canonical,ensure_ascii=False,sort_keys=True,separators=(',',':'))+'\n',encoding='utf-8')
    for warning in warnings:print(f'[WARN] {warning}',file=sys.stderr)
    print(f"[CPF][OPENAPI][PASS] module={args.module.upper()} operations={canonical['x-cpf-openapi-operation-count']} public={canonical['x-cpf-public-operation-count']} release={args.release}")
    return 0
if __name__=='__main__':
    try:raise SystemExit(main())
    except (ContractError,json.JSONDecodeError) as error:print(f'[FAIL] {error}',file=sys.stderr);raise SystemExit(1)
