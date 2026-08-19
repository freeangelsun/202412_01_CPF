#!/usr/bin/env python3
"""Fail-closed ADM/BZA operator identity trust-boundary gate.

The gate verifies the server-authenticated operator context, browser actor-field rejection,
and mutation signatures without confusing read filters/target operator IDs with privileged actors.
"""
from __future__ import annotations
import argparse,json,re
from pathlib import Path
class GateError(RuntimeError):pass
PRIVILEGED_ALIASES=("requestUser","requestedBy","actorId","operatorIdOverride")

def read(p:Path)->str:
    if not p.is_file(): raise GateError(f"missing {p}")
    return p.read_text(encoding="utf-8-sig",errors="replace")

def mutation_signatures(text:str):
    pat=re.compile(r'@(Post|Put|Patch|Delete)Mapping[^\n]*\n(?:(?:\s*@[^\n]+\n)*)\s*public\s+[^\{]+\{',re.M)
    return [m.group(0) for m in pat.finditer(text)]

def verify(root:Path):
    root=root.resolve(); findings=[]; front_count=0; controller_count=0
    # ADM embedded BFF rejects browser-supplied privileged actor aliases.
    rel="cpf-admin/frontend/src/shared/cpfApi.ts"; p=root/rel; t=read(p); front_count += 1
    for tok in ("CLIENT_ACTOR_FIELDS","assertNoClientActor","assertNoClientActorQuery","URLSearchParams","FormData","Blob","JSON.parse"):
        if tok not in t: findings.append(f"{rel}: ADM browser actor guard missing {tok}")
    for alias in ("requestuser","requestedby","actorid","operatoridoverride"):
        if alias not in t.lower(): findings.append(f"{rel}: actor alias missing {alias}")

    # External BZA frontend is intentionally thin and never authors canonical transaction/actor headers.
    bza_front_rel="cpf-biz-frontend/src/shared/api/channelHttpClient.ts"
    bza_front=read(root/bza_front_rel); front_count += 1
    for forbidden in ("X-Transaction-Id","X-Original-System-Code","X-System-Code","X-Caller-System-Code","X-Target-System-Code","X-Target-Operation-Id"):
        if forbidden.lower() in bza_front.lower(): findings.append(f"{bza_front_rel}: external frontend must not author {forbidden}")
    channel_guard_rel="cpf-biz-channel/src/main/java/com/cpf/bzachannel/shared/protocol/CanonicalHeaderOwnershipFilter.java"
    channel_guard=read(root/channel_guard_rel)
    for tok in ("BROWSER_FORBIDDEN","SC_BAD_REQUEST","/api/bza/"):
        if tok not in channel_guard: findings.append(f"{channel_guard_rel}: BZA channel ownership guard missing {tok}")
    adm_mutator=root/'cpf-admin/frontend/src/shared/orval-mutator.ts'
    if adm_mutator.is_file():
        t=read(adm_mutator); front_count += 1
        if 'Browser actor field is forbidden' not in t and 'assertNoClientActor' not in t:
            findings.append('ADM Orval mutator does not preserve actor rejection')

    # ADM server: authentication filter + request-body compatibility guard + mandatory audit reservation.
    auth=read(root/'cpf-admin/src/main/java/com/cpf/admin/opr/filter/AdmApiAuthFilter.java')
    if 'request.setAttribute("adm.operatorId"' not in auth: findings.append('ADM auth filter does not bind server operator')
    advice=read(root/'cpf-admin/src/main/java/com/cpf/admin/opr/audit/AdmVerifiedActorRequestBodyAdvice.java')
    for tok in ('adm.operatorId','claimedActor','requestUser','requestedBy','actorId','operatorIdOverride'):
        if tok not in advice: findings.append(f'ADM body actor advice missing {tok}')
    interceptor=read(root/'cpf-admin/src/main/java/com/cpf/admin/opr/audit/AdmMandatoryAuditInterceptor.java')
    for tok in ('adm.operatorId','reserve(','requestUser'):
        if tok not in interceptor: findings.append(f'ADM mandatory audit trust contract missing {tok}')

    # BZA server context must be bound by the auth filter.
    bza_auth=read(root/'cpf-biz-admin/src/main/java/com/cpf/bizadmin/auth/filter/BzaApiAuthFilter.java')
    if 'request.setAttribute("bza.operatorId"' not in bza_auth: findings.append('BZA auth filter does not bind server operator')

    # Mutation methods must not accept a privileged actor as a query/form parameter.
    for base in ('cpf-admin/src/main/java','cpf-biz-admin/src/main/java'):
        directory=root/base
        for p in directory.rglob('*Controller.java') if directory.is_dir() else []:
            text=read(p); controller_count += 1
            for signature in mutation_signatures(text):
                for alias in PRIVILEGED_ALIASES:
                    if re.search(rf'@RequestParam(?:\([^)]*\))?\s+(?:[\w<>?,.]+\s+)*{alias}\b',signature,re.I):
                        findings.append(f'{p.relative_to(root)}: mutation accepts client privileged actor {alias}')

    # Canonical batch control surface must overwrite nested actor aliases with the verified actor.
    batch=root/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java'
    test=root/'cpf-admin/src/test/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlControllerEndpointTest.java'
    bt=read(batch)
    for tok in ('@RequestAttribute("adm.operatorId")','withServerActor','requestedBy','requestUser','actorId','operatorIdOverride'):
        if tok not in bt: findings.append(f'batch controller actor contract missing {tok}')
    tt=read(test)
    for tok in ('everyPrivilegedEndpointUsesAuthenticatedActorAndStripsNestedAliases','validationErrorsAreAlways400AndNeverUnknownResult','typedOwnerErrorsUseOneEndpointIndependentStatusMatrix','unexpectedTransportFailureIsOnlyCaseMappedToUnknownResult'):
        if tok not in tt: findings.append(f'endpoint test missing {tok}')

    result={"status":"PASS" if not findings else "FAIL","frontendGuardFiles":front_count,"controllerFileCount":controller_count,"privilegedAliases":list(PRIVILEGED_ALIASES),"findings":findings}
    if findings: raise GateError(json.dumps(result,ensure_ascii=False,indent=2))
    return result

def main():
    p=argparse.ArgumentParser();p.add_argument('--root',default='.');p.add_argument('--json-output');a=p.parse_args();root=Path(a.root).resolve()
    try:r=verify(root);code=0
    except Exception as e:
        try:r=json.loads(str(e))
        except:r={'status':'FAIL','message':str(e)}
        code=1
    text=json.dumps(r,ensure_ascii=False,indent=2)
    if a.json_output:
        o=Path(a.json_output);o=o if o.is_absolute() else root/o;o.parent.mkdir(parents=True,exist_ok=True);o.write_text(text+'\n',encoding='utf-8')
    print(text);return code
if __name__=='__main__':raise SystemExit(main())
