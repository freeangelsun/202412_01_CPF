#!/usr/bin/env python3
"""ADM + Backoffice Web reference + Gateway consumer closure verifier."""
from __future__ import annotations
import argparse,json,re
from pathlib import Path
HTTP_CODES=(401,403,404,409,429,500,503)
def read(p:Path)->str:
    if not p.is_file(): raise AssertionError(f'missing file: {p}')
    return p.read_text(encoding='utf-8')
def all_text(root:Path,patterns=('*.java','*.ts','*.vue','*.html','*.css','*.scss')):
    out=[]
    if not root.exists(): return ''
    for pat in patterns:
        for p in root.rglob(pat):
            if p.is_file():
                try: out.append(p.read_text(encoding='utf-8'))
                except UnicodeDecodeError: pass
    return '\n'.join(out)
def main():
    ap=argparse.ArgumentParser(); ap.add_argument('--root',default='.'); ap.add_argument('--json-out'); a=ap.parse_args(); root=Path(a.root).resolve(); checks=[]
    def ok(name,cond,detail=''): checks.append((name,bool(cond),detail))
    adm_gen=read(root/'cpf-admin/frontend/src/generated/cpf-api.ts')
    bo_gen=read(root/'cpf-backoffice-web/frontend/src/generated/backoffice-api.ts')
    ok('adm_generated_client',len(adm_gen)>1000 and ('operation' in adm_gen.lower() or 'generated' in adm_gen.lower()))
    ok('backoffice_generated_client','AUTO-GENERATED' in bo_gen and 'invokeBackoffice' in bo_gen)
    web_src=all_text(root/'cpf-backoffice-web/frontend/src',patterns=('*.ts','*.vue'))
    ok('backoffice_generated_client_has_real_consumers','generated/backoffice-api' in web_src)
    channel=read(root/'cpf-backoffice-web/frontend/src/shared/api/channelHttpClient.ts')
    ok('backoffice_http_error_status','response.status' in channel and 'BackofficeHttpError' in channel)
    ok('backoffice_web_same_origin','credentials' in channel and "'include'" in channel and 'VITE_MBW_WEB_BASE_URL' in channel)
    ok('browser_does_not_assemble_canonical_headers', not any(h in channel for h in ('X-Transaction-Id','X-Original-System-Code','X-System-Code','X-Caller-System-Code','X-Target-System-Code','X-Target-Operation-Id')))
    java=all_text(root/'cpf-backoffice-web/src/main/java',patterns=('*.java',))
    for h in ('X-Transaction-Id','X-Original-System-Code','X-System-Code','X-Caller-System-Code','X-Target-System-Code','X-Target-Operation-Id'):
        ok('backoffice_bff_header_'+h.lower().replace('-','_'),h in java)
    ok('backoffice_web_no_cpf_java_import', not re.search(r'import\s+com\.cpf\.(?!backoffice\.web)',java))
    ok('backoffice_web_no_db_api', not re.search(r'\b(?:JdbcTemplate|DataSource|EntityManager|JpaRepository|SqlSession)\b',java))
    ok('backoffice_web_gateway_or_explicit_direct','GATEWAY' in java and 'DIRECT' in java and 'selectedBaseUri' in java)
    for surface in ('cpf-admin/frontend','cpf-backoffice-web/frontend'):
        idx=read(root/surface/'index.html'); ok(surface.replace('/','_')+'_no_runtime_cdn',not bool(re.search(r'(?i)(?:src|href)=["\']https?://',idx)))
    adm_state=read(root/'cpf-admin/frontend/src/shared/operationState.ts')
    for c in HTTP_CODES: ok(f'adm_http_{c}',str(c) in adm_state)
    gw_java=all_text(root/'cpf-gateway/src/main/java',patterns=('*.java',))
    ok('gateway_legacy_table_zero','cpf_gateway_' not in gw_java,str(gw_java.count('cpf_gateway_')))
    for name in ('GW_TRANSACTION','GW_ATTEMPT','GW_CONTROL_NONCE','GW_CONTROL_SECURITY_AUDIT'): ok('gateway_table_'+name,name in gw_java)
    failures=[x for x in checks if not x[1]]
    result={'status':'PASS' if not failures else 'FAIL','passCount':len(checks)-len(failures),'failCount':len(failures),'totalCount':len(checks),'checks':[{'name':n,'passed':p,'detail':d} for n,p,d in checks],'runtimeVerification':'UNVERIFIED_EXTERNAL_RUNTIME'}
    for n,p,d in checks: print(f"{'PASS' if p else 'FAIL'} {n}"+(f' :: {d}' if d else ''))
    print(f"SUMMARY pass={result['passCount']} fail={result['failCount']} total={result['totalCount']}")
    if a.json_out: Path(a.json_out).write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    raise SystemExit(1 if failures else 0)
if __name__=='__main__': main()
