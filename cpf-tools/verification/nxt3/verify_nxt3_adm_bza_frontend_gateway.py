#!/usr/bin/env python3
# CPF 개발/검증 Source이며 최신 Requirement와 실패 누적 검증 계약을 따릅니다.
from __future__ import annotations
import argparse, json, re, sys
from pathlib import Path

HTTP_CODES = (401,403,404,409,429,500,503)

def read(p: Path) -> str:
    if not p.is_file():
        raise AssertionError(f"missing file: {p}")
    return p.read_text(encoding="utf-8")

def all_text(root: Path, patterns=("*.java","*.ts","*.vue","*.html","*.css","*.scss")) -> str:
    parts=[]
    if not root.exists(): return ""
    for pat in patterns:
        for p in root.rglob(pat):
            if p.is_file():
                try: parts.append(p.read_text(encoding="utf-8"))
                except UnicodeDecodeError: pass
    return "\n".join(parts)

def main() -> int:
    ap=argparse.ArgumentParser()
    ap.add_argument("--root", default=".")
    ap.add_argument("--json-out")
    args=ap.parse_args(); root=Path(args.root).resolve()
    checks=[]
    def ok(name, cond, detail=""):
        checks.append((name,bool(cond),detail))

    adm_gen=read(root/'cpf-admin/frontend/src/generated/cpf-api.ts')
    bza_gen=read(root/'cpf-biz-frontend/src/generated/bza-api.ts')
    ok('adm_generated_client', len(adm_gen)>1000 and ('operation' in adm_gen.lower() or 'generated' in adm_gen.lower()))
    ok('bza_generated_client', 'AUTO-GENERATED' in bza_gen and 'invokeBza' in bza_gen)

    maintenance=read(root/'cpf-admin/frontend/src/features/maintenance/MaintenancePage.vue')
    registry=read(root/'cpf-admin/frontend/src/features/service-registry/ServiceRegistryPage.vue')
    approval=read(root/'cpf-admin/frontend/src/shared/serviceRegistryApproval.ts')
    ok('approval_helper_generated_client', 'admApprovalRequest' in approval and 'admServiceRegistryFindInstances' in approval)
    ok('maintenance_no_direct_mutation', 'admMaintenanceExecuteAction' not in maintenance and 'requestServiceInstanceApproval' in maintenance)
    ok('registry_no_direct_mutation', 'admServiceRegistryChangeInstanceState' not in registry and 'requestServiceInstanceApproval' in registry)
    ok('approval_version_fence', 'expectedVersion' in approval and 'targetId' in approval and 'SERVICE_INSTANCE_' in approval)

    adm_state=read(root/'cpf-admin/frontend/src/shared/operationState.ts')
    bza_http=read(root/'cpf-biz-frontend/src/shared/api/channelHttpClient.ts')
    for c in HTTP_CODES:
        ok(f'adm_http_{c}', str(c) in adm_state)
    ok('bza_http_error_status', 'response.status' in bza_http and 'BzaHttpError' in bza_http)
    ok('bza_channel_only_consumer', 'VITE_BZA_CHANNEL_BASE_URL' in bza_http and 'fetch(' in bza_http)

    for surface in ('cpf-admin/frontend','cpf-biz-frontend'):
        idx=read(root/surface/'index.html')
        external=bool(re.search(r'(?i)(?:src|href)=["\']https?://',idx))
        ok(surface.replace('/','_')+'_no_runtime_cdn', not external)

    gw_java=all_text(root/'cpf-gateway/src/main/java', patterns=('*.java',))
    ok('gateway_legacy_table_zero', 'cpf_gateway_' not in gw_java, str(gw_java.count('cpf_gateway_')))
    required_gw=('GW_TRANSACTION','GW_ATTEMPT','GW_CONTROL_NONCE','GW_CONTROL_SECURITY_AUDIT')
    for name in required_gw: ok('gateway_table_'+name, name in gw_java)

    adm_gateway=all_text(root/'cpf-admin/src/main/java/com/cpf/admin/opr/gateway', patterns=('*.java',))
    ok('adm_gateway_no_internal_package', 'com.cpf.gateway.internal' not in adm_gateway)
    ok('adm_gateway_no_jdbc', 'JdbcTemplate' not in adm_gateway)
    ok('adm_gateway_no_table_sql', 'cpf_gateway_' not in adm_gateway and not re.search(r'(?i)\b(?:from|into|update|delete\s+from)\s+GW_',adm_gateway))
    ok('adm_gateway_public_api_only', 'com.cpf.gateway.api.' in adm_gateway)
    ok('adm_gateway_remote_http_owner', '/internal/v1/gateway/registry' in adm_gateway and 'WebClient' in adm_gateway)
    ok('adm_gateway_hmac', 'CpfGatewayControlSigner' in adm_gateway and 'SIGNATURE' in adm_gateway and 'NONCE' in adm_gateway)
    ok('adm_gateway_timeouts', 'overallTimeout' in adm_gateway and 'responseTimeout' in adm_gateway and 'CONNECT_TIMEOUT_MILLIS' in adm_gateway)

    adm_cfg=read(root/'cpf-admin/src/main/resources/application.yml')
    for key in ('base-url','shared-secret','key-id','audience','connect-timeout-millis','response-timeout-millis','overall-timeout-millis'):
        ok('adm_gateway_config_'+key, key+':' in adm_cfg)
    ok('adm_default_cpfdb', '/cpfDB' in adm_cfg and '/admDB' not in adm_cfg)

    failures=[x for x in checks if not x[1]]
    result={
        "status": "PASS" if not failures else "FAIL",
        "passCount": len(checks)-len(failures),
        "failCount": len(failures),
        "totalCount": len(checks),
        "checks": [{"name":n,"passed":passed,"detail":detail} for n,passed,detail in checks],
        "runtimeVerification": "UNVERIFIED_EXTERNAL_RUNTIME"
    }
    for name,passed,detail in checks:
        print(f"{'PASS' if passed else 'FAIL'} {name}" + (f" :: {detail}" if detail else ''))
    print(f"SUMMARY pass={result['passCount']} fail={result['failCount']} total={result['totalCount']}")
    if args.json_out:
        Path(args.json_out).write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    return 1 if failures else 0

if __name__=='__main__':
    raise SystemExit(main())
