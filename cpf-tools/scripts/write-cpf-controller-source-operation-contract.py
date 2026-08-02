#!/usr/bin/env python3
"""Generate a pre-runtime TypeScript operation contract from explicit Java controller contracts.

This artifact keeps frontend compilation fail-closed before a runtime OpenAPI can be exported.
It never satisfies the release OpenAPI gate: the runtime exporter must replace the generated
files and marker with BACKEND_RUNTIME origin before final verification.
"""
from __future__ import annotations
import argparse, hashlib, json, re, sys
from collections import Counter
from pathlib import Path

HTTP_MAPPING=re.compile(r'@(Get|Post|Put|Patch|Delete)Mapping\s*(?:\((.*?)\))?',re.S)
REQUEST_MAPPING=re.compile(r'@RequestMapping\s*\((.*?)\)',re.S)
OPERATION=re.compile(r'@Operation\s*\((.*?)\)',re.S)
OP_ID=re.compile(r'operationId\s*=\s*"([^"]+)"')
QUOTED=re.compile(r'"([^"]*)"')
PATH_PARAMETER=re.compile(r'\{([^{}]+)\}')
METHOD_DECL=re.compile(r'(?m)^\s*(?:(?:public|protected|private)\s+)?(?:static\s+)?(?:final\s+)?(?:<[^>]+>\s*)?[\w<>, ?\[\].]+\s+(\w+)\s*\(')

class ContractError(RuntimeError): pass

def annotation_path(arguments: str|None)->str:
    if not arguments:return ''
    m=QUOTED.search(arguments);return m.group(1) if m else ''
def normalize(base:str,sub:str)->str:
    parts=[v.strip('/') for v in (base,sub) if v and v.strip('/')]
    return '/'+('/'.join(parts)) if parts else '/'
def class_base(text:str)->str:
    i=text.find('class ');prefix=text[:i if i>=0 else len(text)]
    values=list(REQUEST_MAPPING.finditer(prefix));return annotation_path(values[-1].group(1)) if values else ''
def operation_scope(text:str,end:int)->str:
    declaration=METHOD_DECL.search(text,end)
    return text[end:declaration.end()] if declaration else text[end:end+3000]
MODULES={'ADM':('cpf-admin/src/main/java','/adm/api/','CPF ADM'),'BZA':('cpf-biz-admin/src/main/java','/api/bza/','CPF BZA')}
def discover(root:Path,module:str)->list[dict[str,str]]:
    source_rel,prefix,_=MODULES[module]
    records=[]
    source=root/source_rel
    if not source.exists():raise ContractError(f'{module} source missing: {source}')
    for file in sorted(source.rglob('*.java')):
        text=file.read_text(encoding='utf-8',errors='ignore')
        if '@RestController' not in text:continue
        base=class_base(text)
        for mapping in HTTP_MAPPING.finditer(text):
            method={'Get':'GET','Post':'POST','Put':'PUT','Patch':'PATCH','Delete':'DELETE'}[mapping.group(1)]
            path=normalize(base,annotation_path(mapping.group(2)))
            if not path.startswith(prefix):continue
            scope=operation_scope(text,mapping.end());op=OPERATION.search(scope);operation_id=''
            if op:
                found=OP_ID.search(op.group(1));operation_id=found.group(1) if found else ''
            if not operation_id:raise ContractError(f'operationId missing: {file.relative_to(root)} {method} {path}')
            records.append({'method':method,'template':path,'operationId':operation_id})
    if not records:raise ContractError('no ADM public operations discovered')
    duplicates=[k for k,v in Counter(r['operationId'] for r in records).items() if v>1]
    contracts=[k for k,v in Counter((r['method'],r['template']) for r in records).items() if v>1]
    if duplicates:raise ContractError(f'duplicate operationId={duplicates[:20]}')
    if contracts:raise ContractError(f'duplicate method/path={contracts[:20]}')
    return sorted(records,key=lambda r:(r['method'],r['template'],r['operationId']))
def quote(value:str)->str:return json.dumps(value,ensure_ascii=False)
def write(root:Path,module:str,output:Path,records:list[dict[str,str]],openapi_output:Path|None=None)->None:
    output.mkdir(parents=True,exist_ok=True)
    ids=' | '.join(quote(r['operationId']) for r in records)
    rows=',\n'.join(f"  {{ method: {quote(r['method'])}, template: {quote(r['template'])}, operationId: {quote(r['operationId'])} }}" for r in records)
    contract=f'''// Generated from explicit Java controller annotations for pre-runtime compilation.
// Release verification requires replacement from canonical BACKEND_RUNTIME OpenAPI.
export type CpfOperationId = {ids};
export interface CpfOperationDescriptor {{ method: string; template: string; operationId: CpfOperationId; }}
export const cpfOperationDescriptors: readonly CpfOperationDescriptor[] = [
{rows}
] as const;
function matchesTemplate(template: string, pathname: string): boolean {{
  const expected=template.split("/"); const actual=pathname.split("/");
  if(expected.length!==actual.length)return false;
  return expected.every((segment,index)=>(segment.startsWith("{{")&&segment.endsWith("}}"))||segment===actual[index]);
}}
export function resolveCpfOperation(method: string, rawUrl: string): CpfOperationDescriptor {{
  const pathname=new URL(rawUrl,window.location.origin).pathname;
  const normalizedMethod=method.trim().toUpperCase();
  const found=cpfOperationDescriptors.find(value=>value.method===normalizedMethod&&matchesTemplate(value.template,pathname));
  if(!found)throw new Error(`CPF controller operation is not registered: ${{normalizedMethod}} ${{pathname}}`);
  return found;
}}
'''
    (output/'cpf-operation-contract.ts').write_text(contract,encoding='utf-8')
    lines=['// Pre-runtime generated compatibility client. Runtime OpenAPI generation must replace this file.','import { cpfGeneratedRequest } from "../shared/cpfApi";','export interface CpfGeneratedRequestOptions { data?: unknown; signal?: AbortSignal; headers?: HeadersInit; path?: Record<string,string|number>; query?: Record<string,unknown>; }','function renderPath(template:string,values:Record<string,string|number>={}):string{return template.replace(/\\{([^}]+)\\}/g,(_,name)=>{const value=values[name];if(value===undefined||value===null||String(value).trim()==="")throw new Error(`Missing path parameter: ${name}`);return encodeURIComponent(String(value));});}']
    for r in records:
        lines.append(f'export async function {r["operationId"]}<T=unknown>(options:CpfGeneratedRequestOptions={{}}):Promise<T>{{return cpfGeneratedRequest<T>({{url:renderPath({quote(r["template"])},options.path),method:{quote(r["method"])},data:options.data,params:options.query,signal:options.signal,headers:options.headers}});}}')
    (output/'cpf-api.ts').write_text('\n'.join(lines)+'\n',encoding='utf-8')
    orval_dir=output/'orval';orval_dir.mkdir(parents=True,exist_ok=True)
    (orval_dir/'cpf-api.ts').write_text('// CONTROLLER_SOURCE_PRE_RUNTIME adapter. @tanstack/vue-query is owned by shared cpfApi.\nexport * from "../cpf-api";\n',encoding='utf-8')
    if openapi_output is not None:
        paths={}
        for record in records:
            method=record['method'].lower()
            status='200'
            operation={
                'operationId':record['operationId'],
                'responses':{status:{'description':'Controller source contract response','content':{'application/json':{'schema':{'$ref':'#/components/schemas/CpfControllerSourceResponse'}}}}},
            }
            parameters=list(dict.fromkeys(PATH_PARAMETER.findall(record['template'])))
            if parameters:
                operation['parameters']=[{
                    'name':name,
                    'in':'path',
                    'required':True,
                    'schema':{'type':'string'},
                } for name in parameters]
            paths.setdefault(record['template'],{})[method]=operation
        spec={
            'openapi':'3.1.0',
            'info':{'title':f'{MODULES[module][2]} controller source pre-runtime contract','version':'0.0.0-pre-runtime'},
            'paths':paths,
            'components':{'schemas':{'CpfControllerSourceResponse':{'type':'object','additionalProperties':True}}},
            'x-cpf-export-origin':'CONTROLLER_SOURCE_PRE_RUNTIME',
            'x-cpf-product-module':module,
            'x-cpf-openapi-operation-count':len(records),
            'x-cpf-public-operation-count':len(records),
            'x-cpf-canonical-schema-version':4,
            'x-cpf-release-eligible':False,
        }
        openapi_output.parent.mkdir(parents=True,exist_ok=True)
        openapi_output.write_text(json.dumps(spec,ensure_ascii=False,sort_keys=True,separators=(',',':'))+'\n',encoding='utf-8')
    def sha256_file(path:Path)->str:return hashlib.sha256(path.read_bytes()).hexdigest()
    root=output.parents[1]
    openapi_path=openapi_output if openapi_output is not None else root/'openapi/cpf-openapi.json'
    config_path=root/'orval.config.ts';lock_path=root/'package-lock.json';package_path=root/'package.json'
    for required in (openapi_path,config_path,lock_path,package_path):
        if not required.is_file():raise ContractError(f'marker input missing: {required}')
    package=json.loads(package_path.read_text(encoding='utf-8'))
    generated_files=[]
    for file in sorted(output.rglob('*.ts')):
        generated_files.append({'path':file.relative_to(root).as_posix(),'sha256':sha256_file(file)})
    operation_ids=sorted(r['operationId'] for r in records)
    marker={
        'schemaVersion':3,'identityPolicy':'TRACKED_HASHES_RELEASE_SHA_IN_EVIDENCE',
        'origin':'CONTROLLER_SOURCE_PRE_RUNTIME','releaseEligible':False,'requiredReplacementOrigin':'BACKEND_RUNTIME',
        'openApiPath':openapi_path.relative_to(root).as_posix(),'openApiSha256':sha256_file(openapi_path),
        'openApiOperationCount':len(operation_ids),'openApiOperationIdsSha256':hashlib.sha256('\n'.join(operation_ids).encode()).hexdigest(),
        'generator':{'name':'controller-source-contract','version':'1'},
        'generatorConfigPath':config_path.relative_to(root).as_posix(),'generatorConfigSha256':sha256_file(config_path),
        'packageLockPath':lock_path.relative_to(root).as_posix(),'packageLockSha256':sha256_file(lock_path),
        'nodeRequirement':package.get('engines',{}).get('node'),'npmRequirement':package.get('engines',{}).get('npm'),
        'generatedFiles':generated_files,
        'generatedFileSetSha256':hashlib.sha256('\n'.join(f"{i['path']}:{i['sha256']}" for i in generated_files).encode()).hexdigest(),
        'sanitized':True,
    }
    if not marker['nodeRequirement'] or not marker['npmRequirement']:raise ContractError('Node/npm exact requirement missing')
    (output/'.cpf-openapi-source.json').write_text(json.dumps(marker,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
def main()->int:
    ap=argparse.ArgumentParser();ap.add_argument('--root',type=Path,default=Path.cwd());ap.add_argument('--module',choices=sorted(MODULES),default='ADM');ap.add_argument('--output',type=Path);ap.add_argument('--openapi-output',type=Path);args=ap.parse_args()
    root=args.root.resolve();default_output=Path('cpf-admin/frontend/src/generated' if args.module=='ADM' else 'cpf-biz-admin/frontend/src/generated');raw_output=args.output or default_output;output=raw_output if raw_output.is_absolute() else root/raw_output
    records=discover(root,args.module);openapi_output=None if args.openapi_output is None else (args.openapi_output if args.openapi_output.is_absolute() else root/args.openapi_output);write(root,args.module,output,records,openapi_output)
    print(f'[PASS] pre-runtime controller operation contract module={args.module} operations={len(records)} output={output}')
    return 0
if __name__=='__main__':
    try:raise SystemExit(main())
    except ContractError as e:print(f'[FAIL] {e}',file=sys.stderr);raise SystemExit(1)
