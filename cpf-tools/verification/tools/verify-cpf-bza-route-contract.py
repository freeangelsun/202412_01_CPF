#!/usr/bin/env python3
"""Verify the final BZA architecture: optional CPF domain + DB-less Spring channel + external reference frontend."""
from __future__ import annotations
import argparse, json, re, sys
from pathlib import Path
class ContractError(RuntimeError): pass

def read(p:Path)->str:
    if not p.is_file(): raise ContractError(f'missing {p}')
    return p.read_text(encoding='utf-8-sig')

def validate(root:Path)->dict:
    domain=root/'cpf-biz-admin'; channel=root/'cpf-biz-channel'; frontend=root/'cpf-biz-frontend'
    if not any(p.is_dir() for p in (domain,channel,frontend)):
        return {'state':'ABSENT','routes':0,'referenceRoutes':0,'operations':0}
    operations=0
    if domain.is_dir():
        build=read(domain/'build.gradle')
        for token in ('frontendBuild','frontendInstall','frontendVerify','srcDir(frontend','generated/frontend/static/bza'):
            if token in build: raise ContractError(f'cpf-biz-admin still embeds frontend: {token}')
        page=domain/'src/main/java/com/cpf/bizadmin/backoffice/controller/BzaPageController.java'
        if page.is_file() and re.search(r'@(Controller|GetMapping|RequestMapping)',read(page)):
            raise ContractError('cpf-biz-admin still exposes embedded browser page mapping')
        spec=domain/'openapi/cpf-openapi.json'
        if spec.is_file():
            doc=json.loads(read(spec)); operations=int(doc.get('x-cpf-openapi-operation-count',0) or 0)
            if operations < 1:
                operations=sum(1 for item in doc.get('paths',{}).values() for method,op in item.items()
                               if method.upper() in {'GET','POST','PUT','PATCH','DELETE'} and isinstance(op,dict) and op.get('operationId'))
    channel_routes=0
    if channel.is_dir():
        build=read(channel/'build.gradle').lower()
        forbidden_build=('project(', 'cpf-starter', 'com.cpf:', 'jdbc', 'jpa', 'mybatis', 'mariadb', 'postgresql', 'oracle')
        leaked=[x for x in forbidden_build if x in build]
        if leaked: raise ContractError(f'BZA Channel is not DB-less/pure Spring: {leaked}')
        framework_imports=[]
        for p in (channel/'src').rglob('*.java'):
            for line in read(p).splitlines():
                if line.startswith('import com.cpf.') and '.bzachannel.' not in line:
                    framework_imports.append(f'{p.relative_to(root)}:{line}')
        if framework_imports: raise ContractError(f'BZA Channel CPF Java dependency leak: {framework_imports[:5]}')
        source='\n'.join(read(p) for p in (channel/'src/main/java').rglob('*.java'))
        required=('gatewayBaseUri','directBaseUri','TARGET_OPERATION_ID','ORIGINAL_SYSTEM_CODE','CALLER_SYSTEM_CODE','TARGET_SYSTEM_CODE')
        for token in required:
            if token not in source: raise ContractError(f'BZA Channel contract missing {token}')
        if 'builder.header(BzaCanonicalHeaders.SYSTEM_CODE' in source:
            raise ContractError('BZA external Channel must not write receiver-owned X-System-Code')
        client_source=read(channel/'src/main/java/com/cpf/bzachannel/shared/client/BusinessApiHttpClient.java')
        if 'properties.selectedBaseUri()' not in client_source:
            raise ContractError('BZA client must use exactly one pre-selected upstream and never implement runtime fallback')
        catalog=channel/'src/main/resources/bza-routes.tsv'
        rows=[line.split('\t') for line in read(catalog).splitlines() if line and not line.startswith('#')]
        if any(len(r)!=3 for r in rows): raise ContractError('invalid BZA route catalog')
        if len({(r[0],r[1]) for r in rows}) != len(rows): raise ContractError('duplicate BZA channel route')
        channel_routes=len(rows)
        if domain.is_dir() and operations and channel_routes != operations:
            raise ContractError(f'BZA Channel/OpenAPI operation drift channel={channel_routes} backend={operations}')
    reference_routes=0
    if frontend.is_dir():
        routes=read(frontend/'src/router/index.ts')
        reference_routes=len(re.findall(r"\bpath\s*:\s*['\"]",routes))
        if reference_routes != 4: raise ContractError(f'BZA reference frontend must expose 4 representative routes, actual={reference_routes}')
        generator=read(frontend/'scripts/generate-reference-client.mjs')
        if 'cpf-openapi.json' not in generator or 'OpenAPI operations missing' not in generator:
            raise ContractError('BZA reference frontend is not OpenAPI-generated-client driven')
        api=read(frontend/'src/shared/api/channelHttpClient.ts')
        if '/api/bza' not in '\n'.join(read(p) for p in (frontend/'src').rglob('*') if p.is_file() and p.suffix in {'.ts','.vue'}):
            raise ContractError('BZA frontend has no Channel API consumer')
        if any(x in api.lower() for x in ('jdbc','datasource','mybatis')):
            raise ContractError('BZA frontend contains DB concern')
    return {'state':'PRESENT','routes':channel_routes,'referenceRoutes':reference_routes,'operations':operations}

def main()->int:
    ap=argparse.ArgumentParser();ap.add_argument('--root',type=Path,default=Path.cwd());a=ap.parse_args()
    r=validate(a.root.resolve());print(f"BZA_BOUNDARY_CONTRACT=PASS state={r['state']} backendOperations={r['operations']} channelRoutes={r['routes']} referenceRoutes={r['referenceRoutes']} dbLess=1 cpfJavaDependency=0");return 0
if __name__=='__main__':
    try: raise SystemExit(main())
    except ContractError as e: print(f'BZA_BOUNDARY_CONTRACT=FAIL {e}',file=sys.stderr); raise SystemExit(1)
