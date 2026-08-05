#!/usr/bin/env python3
"""Fail-closed product edition/multi-tenant/plugin/package prototype policy gate."""
from __future__ import annotations
import argparse, json
from pathlib import Path

class GateError(RuntimeError): pass

def require(condition: bool, message: str) -> None:
    if not condition: raise GateError(message)

def load(path: Path) -> dict:
    try: data=json.loads(path.read_text(encoding='utf-8-sig'))
    except (OSError, json.JSONDecodeError) as exc: raise GateError(f'invalid policy catalog: {exc}') from exc
    require(isinstance(data,dict),'policy catalog root must be object')
    return data

def verify(root: Path) -> dict:
    rel=Path('cpf-tools/product-governance/product-capability-policy.json')
    p=root/rel; require(p.is_file(),f'missing {rel}')
    c=load(p)
    require(c.get('baselinePolicy')=='GIT_HEAD_RUNTIME' and c.get('baselineSha')=='RUNTIME_GIT_HEAD','stale static baseline is forbidden')
    require(c.get('policyMaturity')=='PROTOTYPE_ONLY_NOT_GA','unresolved policy must not be exposed as GA')
    require(c.get('commercialPolicyResolved') is False,'commercial policy must remain explicitly unresolved')
    require(c.get('defaultFailClosed') is True,'product policy must fail closed')
    editions=c.get('editions'); require(isinstance(editions,list) and editions,'editions must be non-empty')
    ids=[e.get('id') for e in editions if isinstance(e,dict)]
    require(len(ids)==len(editions) and all(isinstance(x,str) and x for x in ids),'edition id missing')
    require(len(ids)==len(set(ids)),'duplicate edition id')
    for e in editions:
        require(e.get('licensePolicy')=='UNRESOLVED','edition license policy must not be fabricated')
        require(e.get('unsupportedCapabilityAction')=='REJECT','unsupported capability must be rejected')
        require(e.get('runtimeDependencyOnCommercialPolicy') is False,'technical runtime must not depend on unresolved commercial policy')
    mt=c.get('multiTenantPrototype') or {}
    required_isolation={'tenant-context','data','configuration','permission','secret','quota','audit'}
    require(mt.get('maturity')=='OPT_IN_PROTOTYPE' and mt.get('enabledByDefault') is False,'multi-tenant must remain opt-in prototype')
    require(mt.get('contextRequired') is True and mt.get('crossTenantAccess')=='DENY','tenant context/cross-tenant boundary must fail closed')
    require(set(mt.get('isolationDimensions') or [])==required_isolation,'multi-tenant isolation dimensions incomplete or unexpected')
    require(mt.get('unknownTenantAction')=='REJECT_AND_AUDIT','unknown tenant must reject and audit')
    plugin=c.get('pluginPrototype') or {}
    require(plugin.get('maturity')=='OPT_IN_PROTOTYPE' and plugin.get('enabledByDefault') is False,'plugin must remain opt-in prototype')
    require(plugin.get('spiOnly') is True and plugin.get('internalPackageAccess')=='DENY','plugin must use SPI and deny internal package access')
    require(plugin.get('signatureRequired') is True and plugin.get('unsignedPluginAction')=='REJECT_AND_AUDIT','unsigned plugin must reject and audit')
    require(plugin.get('permissionModel')=='DENY_BY_DEFAULT','plugin permission must deny by default')
    require(plugin.get('compatibilityContractRequired') is True and plugin.get('isolatedClassLoaderRequired') is True,'plugin compatibility/isolation required')
    pkg=c.get('capabilityPackagePrototype') or {}
    require(pkg.get('maturity')=='OPT_IN_PROTOTYPE' and pkg.get('enabledByDefault') is False,'package must remain opt-in prototype')
    for key in ('signedManifestRequired','exactDependencyClosureRequired','installPlanRequired','upgradePlanRequired','rollbackPlanRequired','compatibilityMatrixRequired'):
        require(pkg.get(key) is True,f'package {key} must be true')
    require(pkg.get('licensePolicy')=='UNRESOLVED','package license policy must not be fabricated')
    sec=c.get('securityAndCompatibility') or {}
    expected={'crossTenant':'DENY','unsignedPlugin':'DENY','incompatiblePackage':'DENY','permissionBypass':'DENY','secretsInPackage':'DENY'}
    for key,value in expected.items(): require(sec.get(key)==value,f'security boundary {key} must be {value}')
    require(sec.get('rollbackRequired') is True and sec.get('mixedVersionRequiresEvidence') is True,'rollback/mixed-version evidence required')
    consumers=c.get('requiredConsumers') or []
    for rel_consumer in consumers:
        require(isinstance(rel_consumer,str) and (root/rel_consumer).is_file(),f'declared consumer missing: {rel_consumer}')
    runner=(root/'cpf-tools/scripts/run-cpf-full-qa-validation.ps1').read_text(encoding='utf-8-sig')
    require('PRODUCT_GOVERNANCE' in runner and 'verify-cpf-product-governance.py' in runner,'full QA pipeline does not consume product governance gate')
    return {'status':'PASS','policyMaturity':c['policyMaturity'],'editionCount':len(editions),'multiTenantIsolationDimensions':len(required_isolation),'pluginSignatureRequired':True,'packageRollbackRequired':True,'consumers':consumers}

def main()->int:
    ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--json-output');a=ap.parse_args();root=Path(a.root).resolve()
    try: result=verify(root); code=0
    except Exception as exc: result={'status':'FAIL','message':str(exc)};code=1
    if a.json_output:
        out=Path(a.json_output);out=out if out.is_absolute() else root/out;out.parent.mkdir(parents=True,exist_ok=True);out.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(json.dumps(result,ensure_ascii=False));return code
if __name__=='__main__': raise SystemExit(main())
