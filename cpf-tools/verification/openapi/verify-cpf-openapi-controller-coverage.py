#!/usr/bin/env python3
"""Compare exact controller HTTP method/path/operationId contracts with runtime OpenAPI."""
from __future__ import annotations
import argparse,json,re,sys
from collections import Counter
from pathlib import Path

HTTP_METHODS={'get':'GET','post':'POST','put':'PUT','patch':'PATCH','delete':'DELETE','head':'HEAD','options':'OPTIONS','trace':'TRACE'}
HTTP_MAPPING=re.compile(r'@(Get|Post|Put|Patch|Delete)Mapping\s*(?:\((.*?)\))?',re.S)
REQUEST_MAPPING=re.compile(r'@RequestMapping\s*\((.*?)\)',re.S)
OPERATION=re.compile(r'@Operation\s*\((.*?)\)',re.S)
OP_ID=re.compile(r'operationId\s*=\s*"([^"]+)"')
QUOTED=re.compile(r'"([^"]*)"')
METHOD_DECL=re.compile(r'(?m)^\s*(?:(?:public|protected|private)\s+)?(?:static\s+)?(?:final\s+)?(?:<[^>]+>\s*)?[\w<>, ?\[\].]+\s+(\w+)\s*\(')
MODULES={'cpf-admin':('/adm/api/','cpf-admin/src/main/java'),'cpf-backoffice':('/api/v1/backoffice/','cpf-backoffice/online/src/main/java')}
class CoverageError(RuntimeError):pass

def annotation_path(arguments:str|None)->str:
    if not arguments:return ''
    match=QUOTED.search(arguments);return match.group(1) if match else ''
def normalize(base:str,sub:str)->str:
    values=[value.strip('/') for value in (base,sub) if value and value.strip('/')]
    return '/'+('/'.join(values)) if values else '/'
def class_base(text:str)->str:
    controller=text.find('class ');prefix=text[:controller if controller>=0 else len(text)]
    matches=list(REQUEST_MAPPING.finditer(prefix));return annotation_path(matches[-1].group(1)) if matches else ''
def operation_scope(text:str,mapping_end:int)->tuple[str,str]:
    declaration=METHOD_DECL.search(text,mapping_end)
    if not declaration:return text[mapping_end:mapping_end+3000],'<unknown>'
    return text[mapping_end:declaration.end()],declaration.group(1)
def source_contracts(root:Path,module:str)->list[dict]:
    prefix,source_rel=MODULES[module];source=root/source_rel
    if not source.exists():raise CoverageError(f'module source missing: {source_rel}')
    records=[]
    for file in sorted(source.rglob('*.java')):
        text=file.read_text(encoding='utf-8',errors='ignore')
        if not any(annotation in text for annotation in ('@RestController', '@CpfController', '@CpfRestController')):continue
        base=class_base(text)
        for mapping in HTTP_MAPPING.finditer(text):
            verb={'Get':'GET','Post':'POST','Put':'PUT','Patch':'PATCH','Delete':'DELETE'}[mapping.group(1)]
            path=normalize(base,annotation_path(mapping.group(2)))
            # Product API coverage excludes static page controllers and non-product paths.
            if not path.startswith(prefix):continue
            # @Hidden is normally declared before the mapping annotation. Inspect only the current
            # method annotation block (after the previous method/field terminator), not arbitrary
            # preceding text, so a previous hidden method cannot suppress the next public one.
            before=text[:mapping.start()]
            boundary=max(before.rfind('}'),before.rfind(';'))
            annotation_block=before[boundary+1:]
            if re.search(r'@Hidden\b',annotation_block):continue
            scope,method=operation_scope(text,mapping.end())
            if re.search(r'@Hidden\b',scope):continue
            operation=OPERATION.search(scope);operation_id=''
            if operation:
                match=OP_ID.search(operation.group(1));operation_id=match.group(1) if match else ''
            records.append({'source':file.relative_to(root).as_posix(),'line':text.count('\n',0,mapping.start())+1,'methodName':method,'httpMethod':verb,'path':path,'operationId':operation_id})
    return records
def openapi_contracts(path:Path)->list[dict]:
    if not path.is_file():raise CoverageError(f'OpenAPI missing: {path}')
    try:spec=json.loads(path.read_text(encoding='utf-8-sig'))
    except (OSError,json.JSONDecodeError) as error:raise CoverageError(f'OpenAPI invalid: {error}') from error
    records=[]
    for template,item in (spec.get('paths') or {}).items():
        if not isinstance(item,dict):continue
        for method,operation in item.items():
            verb=HTTP_METHODS.get(method.lower())
            if not verb:continue
            operation=operation or {};records.append({'httpMethod':verb,'path':template,'operationId':operation.get('operationId','')})
    return records
def validate(root:Path,module:str,openapi:Path)->tuple[list[dict],list[dict]]:
    if module not in MODULES:raise CoverageError(f'unsupported module={module}')
    source=source_contracts(root,module);exported=openapi_contracts(openapi if openapi.is_absolute() else root/openapi)
    if not source:raise CoverageError(f'no public source controller operations: module={module}')
    if not exported:raise CoverageError('empty runtime OpenAPI')
    missing_ids=[r for r in source if not r['operationId']]
    if missing_ids:raise CoverageError('controller operationId missing: '+', '.join(f"{r['source']}:{r['line']} {r['httpMethod']} {r['path']}" for r in missing_ids[:30]))
    source_ids=[r['operationId'] for r in source];exported_ids=[r['operationId'] for r in exported if r['operationId']]
    duplicates=[v for v,c in Counter(source_ids).items() if c>1]+[v for v,c in Counter(exported_ids).items() if c>1]
    if duplicates:raise CoverageError(f'duplicate operationId={sorted(set(duplicates))[:30]}')
    exported_by_id={r['operationId']:r for r in exported if r['operationId']}
    failures=[]
    for record in source:
        actual=exported_by_id.get(record['operationId'])
        if not actual:failures.append(f"not exported {record['operationId']} {record['httpMethod']} {record['path']}")
        elif (actual['httpMethod'],actual['path'])!=(record['httpMethod'],record['path']):failures.append(f"contract drift {record['operationId']} source={record['httpMethod']} {record['path']} openapi={actual['httpMethod']} {actual['path']}")
    if failures:raise CoverageError('runtime OpenAPI/controller mismatch: '+'; '.join(failures[:30]))
    prefix=MODULES[module][0]
    public_exported=[r for r in exported if r['path'].startswith(prefix)]
    if len(public_exported)<len(source):raise CoverageError(f'OpenAPI public operation count smaller than source: {len(public_exported)} < {len(source)}')
    return source,exported

def main()->int:
    parser=argparse.ArgumentParser();parser.add_argument('--root',default='.',type=Path);parser.add_argument('--module',required=True,choices=sorted(MODULES));parser.add_argument('--openapi',required=True,type=Path)
    args=parser.parse_args();source,exported=validate(args.root.resolve(),args.module,args.openapi)
    print(f'[PASS] OpenAPI/controller exact coverage module={args.module} source={len(source)} exported={len(exported)}')
    return 0
if __name__=='__main__':
    try:raise SystemExit(main())
    except CoverageError as error:print(f'[FAIL] {error}',file=sys.stderr);raise SystemExit(1)
