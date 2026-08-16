#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, sys
from pathlib import Path

REQ_SUPPORT = ["health","metrics","logs","trace","effectiveConfig","failure","audit","dynamicConfig","runtimeControl","recovery"]
REQ_COMMON = ["OPERATIONS","LOG_TRACE","FAILURE_RECOVERY","CONFIG_POLICY","AUDIT_CHANGE"]


def parse_props(path: Path) -> dict[str,str]:
    out={}
    if not path.exists(): return out
    for line in path.read_text(encoding='utf-8').splitlines():
        line=line.strip()
        if not line or line.startswith('#') or '=' not in line: continue
        k,v=line.split('=',1); out[k.strip()]=v.strip()
    return out


def main() -> int:
    ap=argparse.ArgumentParser()
    ap.add_argument('--root', default='.')
    ap.add_argument('--catalog')
    args=ap.parse_args()
    root=Path(args.root).resolve()
    catalog_path=Path(args.catalog).resolve() if args.catalog else root/'cpf-tools/generator/contracts/cpf-starter-catalog.json'
    errors=[]
    try: data=json.loads(catalog_path.read_text(encoding='utf-8'))
    except Exception as e:
        print(f'FAIL catalog: {e}'); return 1
    public=[m for m in data.get('modules',[]) if m.get('visibility')=='public']
    if not public: errors.append('public starter catalog is empty')
    ids=set()
    for m in public:
        artifact=m.get('artifactId',''); mg=m.get('management')
        if not isinstance(mg,dict):
            errors.append(f'{artifact}: management contract missing'); continue
        mid=mg.get('id','')
        if not mid: errors.append(f'{artifact}: management.id missing')
        elif mid in ids: errors.append(f'{artifact}: duplicate management.id={mid}')
        ids.add(mid)
        for key in ('capabilityId','provider','category','dedicatedWorkflow','commonAreas','supports'):
            if key not in mg: errors.append(f'{artifact}: management.{key} missing')
        common=mg.get('commonAreas') or []
        if sorted(common)!=sorted(REQ_COMMON): errors.append(f'{artifact}: commonAreas must cover canonical ADM areas')
        supports=mg.get('supports') or {}
        for k in REQ_SUPPORT:
            if k not in supports or not isinstance(supports[k],bool): errors.append(f'{artifact}: supports.{k} must be boolean')
        project_path=m.get('projectPath','')
        # Resolve physical path from settings/catalog mapping using conventional project path -> cpf-starters/... mapping from module dir.
        # Existing descriptor is compatibility evidence; generated build metadata is the canonical future path.
        candidates=list(root.glob(f'cpf-starters/**/src/main/resources/META-INF/cpf/runtime-capability.properties'))
        found=None
        for c in candidates:
            p=parse_props(c)
            if p.get('starterArtifactId')==artifact: found=(c,p); break
        if found is None:
            errors.append(f'{artifact}: source runtime-capability.properties missing')
        else:
            c,p=found
            expected={
                'id':str(mg.get('id','')),
                'starterArtifactId':artifact,
                'capability':str(mg.get('capabilityId','')),
                'provider':str(mg.get('provider','')),
                'managementCategory':str(mg.get('category','')),
                'dedicatedWorkflow':str(bool(mg.get('dedicatedWorkflow'))).lower(),
                'commonAreas':','.join(mg.get('commonAreas') or []),
            }
            for k,v in expected.items():
                if p.get(k)!=v: errors.append(f'{artifact}: {c.relative_to(root)} {k}={p.get(k)!r} expected {v!r}')
            for k in REQ_SUPPORT:
                pv=p.get(f'supports.{k}')
                ev=str(bool((mg.get('supports') or {}).get(k))).lower()
                if pv!=ev: errors.append(f'{artifact}: {c.relative_to(root)} supports.{k}={pv!r} expected {ev!r}')
    build=(root/'cpf-tools/build/cpf-root-conventions.gradle').read_text(encoding='utf-8')
    for token in ('generateCpfRuntimeCapabilityMetadata','cpfStarterManagementByProject','runtime-capability.properties'):
        if token not in build: errors.append(f'root conventions missing automatic metadata generation token: {token}')
    inv=(root/'cpf-starters/base/runtime/src/main/java/com/cpf/starter/runtime/CpfRuntimeCapabilityInventory.java').read_text(encoding='utf-8')
    for token in ('starterMeta.','supports','commonAreas','dedicatedWorkflow'):
        if token not in inv: errors.append(f'runtime capability inventory missing {token}')
    health=(root/'cpf-starters/platform-operations/health/src/main/java/com/cpf/platform/operations/health/CpfHealthAutoConfiguration.java')
    if not health.exists(): errors.append('health autoconfiguration missing')
    else:
        hs=health.read_text(encoding='utf-8')
        for token in ('systemCode','domainCode','application','instanceId'):
            if token not in hs: errors.append(f'health runtime identity missing {token}')
    controller=root/'cpf-admin/src/main/java/com/cpf/admin/opr/capability/AdmCapabilityManagementController.java'
    ui=root/'cpf-admin/frontend/src/features/capabilities/CapabilityFleetPage.vue'
    routes=root/'cpf-admin/frontend/src/app/routes.ts'
    auth=root/'cpf-admin/src/main/java/com/cpf/admin/opr/filter/AdmApiAuthFilter.java'
    checks={controller:['admCapabilityManagementOverview','admCapabilityManagementIssues','starterId','capabilityId','provider'],
            ui:['admCapabilityManagementOverview','admCapabilityManagementIssues','starterId','capabilityId','provider'],
            routes:['CAPABILITY_FLEET','operations','traceLog','failureRecovery','configPolicy','auditChange'],
            auth:['/adm/api/capability-management','CAPABILITY_FLEET_READ']}
    for path,tokens in checks.items():
        if not path.exists(): errors.append(f'missing {path.relative_to(root)}'); continue
        txt=path.read_text(encoding='utf-8')
        for token in tokens:
            if token not in txt: errors.append(f'{path.relative_to(root)} missing {token}')
    seed=root/'cpf-tools/db/canonical/seed-model.json'
    st=seed.read_text(encoding='utf-8') if seed.exists() else ''
    for token in ('CAPABILITY_FLEET','CAPABILITY_FLEET_READ','/adm/api/capability-management/**'):
        if token not in st: errors.append(f'canonical ADM seed missing {token}')
    for vendor in ('mariadb','postgresql','oracle'):
        for rel in (f'cpf-tools/db/vendor/{vendor}/migration/flyway/admDB/V109__adm_capability_management.sql',
                    f'cpf-tools/db/vendor/{vendor}/rollback/admDB/R109__adm_capability_management.sql'):
            p=root/rel
            if not p.exists(): errors.append(f'missing {rel}')
            elif 'CAPABILITY_FLEET' not in p.read_text(encoding='utf-8'): errors.append(f'{rel}: capability migration marker missing')
    if errors:
        print('FAIL CPF capability management contract')
        for e in errors: print(' - '+e)
        return 1
    print(f'PASS CPF capability management publicStarters={len(public)} automaticRegistration=YES admCommonIA=YES')
    return 0

if __name__=='__main__': sys.exit(main())
