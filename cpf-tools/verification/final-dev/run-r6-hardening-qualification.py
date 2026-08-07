#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

SHA40=re.compile(r'^[0-9a-f]{40}$')
class HardeningError(RuntimeError): pass

def git(root:Path,*args:str)->str:
    cp=subprocess.run(['git','-C',str(root),*args],text=True,capture_output=True,check=False)
    if cp.returncode!=0: raise HardeningError(f"git {' '.join(args)} failed: {cp.stderr.strip()}")
    return cp.stdout.strip()

def invoke(gate_id:str,cmd:list[str],cwd:Path,out:Path,rows:list[dict],failures:list[str],env:dict[str,str]|None=None)->bool:
    stdout=out/f'{gate_id}.stdout.log';stderr=out/f'{gate_id}.stderr.log'
    try:
        cp=subprocess.run(cmd,cwd=cwd,env=env,text=True,capture_output=True,check=False)
        code=cp.returncode;out_text=cp.stdout or '';err_text=cp.stderr or ''
    except OSError as exc:
        code=127;out_text='';err_text=f'{type(exc).__name__}: {exc}'
    stdout.write_text(out_text,encoding='utf-8');stderr.write_text(err_text,encoding='utf-8')
    status='PASS' if code==0 else 'FAIL'
    rows.append({'id':gate_id,'command':' '.join(cmd),'exitCode':code,'status':status,'stdout':stdout.name,'stderr':stderr.name})
    if code!=0: failures.append(f'{gate_id}(exit={code})')
    return code==0

def add_flag(gate_id:str,condition:bool,message:str,rows:list[dict],failures:list[str],**extra)->bool:
    status='PASS' if condition else 'FAIL';row={'id':gate_id,'status':status,'exitCode':0 if condition else 1,**extra}
    if not condition: row['reason']=message;failures.append(gate_id)
    rows.append(row);return condition

def _brace_block(source:str, brace:int)->str:
    if brace < 0: return ''
    depth=0
    for i in range(brace,len(source)):
        c=source[i]
        if c=='{': depth+=1
        elif c=='}':
            depth-=1
            if depth==0: return source[brace:i+1]
    return source[brace:]

def _operation_method_block(source:str, operation_id:str)->str:
    marker=re.search(r'operationId\s*=\s*["\']'+re.escape(operation_id)+r'["\']',source)
    if not marker: return ''
    brace=source.find('{',marker.end())
    if brace<0: return source[marker.start():marker.end()+1024]
    return source[marker.start():brace]+_brace_block(source,brace)

def _named_method_block(source:str, method_name:str)->str:
    # Avoid field calls/comments by requiring a Java-like declaration prefix before the method name.
    pattern=re.compile(r'(?m)^[ \t]*(?:@[\w.()"=, {}]+\s*)*(?:public|protected|private|final|static|synchronized|[A-Z][\w<>?, .\[\]]*)[^{;\n]*\b'+re.escape(method_name)+r'\s*\([^;{}]*\)\s*(?:throws [^{]+)?\{')
    m=pattern.search(source)
    if not m: return ''
    brace=source.find('{',m.start())
    return source[m.start():brace]+_brace_block(source,brace)

def _java_index(root:Path)->dict[str,Path]:
    index={}
    roots=[root/'cpf-admin/src/main/java',root/'cpf-core/src/main/java',root/'cpf-batch/src/main/java',root/'cpf-biz-admin/src/main/java',root/'cpf-gateway/src/main/java',root/'cpf-starters/platform-operations/feature-flag-openfeature/src/main/java']
    for base in roots:
        if not base.is_dir(): continue
        for path in base.rglob('*.java'):
            index.setdefault(path.stem,path)
    return index

def _source_closure(root:Path, controller_rel:str, operation_id:str, java_index:dict[str,Path])->tuple[str,list[str]]:
    controller=root/controller_rel
    if not controller.is_file(): return '',[]
    src=controller.read_text(encoding='utf-8',errors='ignore')
    method=_operation_method_block(src,operation_id)
    closure=[method]; evidence=[controller_rel]
    # Follow operation-local helper calls within the same controller. This preserves
    # per-operation evidence without leaking unrelated methods from the whole file.
    helper_seen=set()
    helper_queue=[method]
    helper_reserved={'if','for','while','switch','return','throw','new','super','this','requireNonNull','String','List','Map','Set','Optional'}
    while helper_queue and len(helper_seen)<32:
        current=helper_queue.pop(0)
        for called in re.findall(r'(?<![.\w$])([a-zA-Z_$][\w$]*)\s*\(',current):
            if called in helper_reserved or called in helper_seen: continue
            block=_named_method_block(src,called)
            if not block: continue
            helper_seen.add(called);closure.append(block);evidence.append(controller_rel+'#'+called);helper_queue.append(block)
    # Pull request/command record definitions referenced by the method signature.
    signature=method.split('{',1)[0]
    for type_name in sorted(set(re.findall(r'\b([A-Z][A-Za-z0-9_]*(?:Request|Command|Decision|Payload|Input))\b',signature))):
        dep=java_index.get(type_name)
        if dep and dep.is_file():
            text=dep.read_text(encoding='utf-8',errors='ignore')
            closure.append(text);evidence.append(dep.relative_to(root).as_posix())
        else:
            # Nested record/class in the controller or imported outer contract class.
            nested=re.search(r'\b(?:record|class)\s+'+re.escape(type_name)+r'\b',src)
            if nested:
                b=src.find('{',nested.end())
                closure.append(src[nested.start():b]+_brace_block(src,b));evidence.append(controller_rel+'#'+type_name)
    # Follow direct collaborator method calls such as sessionService.revoke(...).
    fields={name:type_name for type_name,name in re.findall(r'\bprivate\s+final\s+([A-Z][A-Za-z0-9_<>?, ]*)\s+([a-zA-Z_$][\w$]*)\s*;',src)}
    for field,called in re.findall(r'\b([a-zA-Z_$][\w$]*)\.([a-zA-Z_$][\w$]*)\s*\(',method):
        raw_type=fields.get(field)
        if not raw_type: continue
        simple=re.sub(r'<.*','',raw_type).strip().split('.')[-1]
        dep=java_index.get(simple)
        if not dep or not dep.is_file(): continue
        text=dep.read_text(encoding='utf-8',errors='ignore');block=_named_method_block(text,called)
        closure.append(block or text);evidence.append(dep.relative_to(root).as_posix()+'#'+called)
    # Feature Flag commands delegate through a public interface; include the owned implementation boundary.
    if controller.name=='AdmFeatureFlagController.java':
        for rel in (
            'cpf-starters/platform-operations/feature-flag-openfeature/src/main/java/com/cpf/starter/platform/operations/feature/flag/openfeature/internal/CpfFeatureFlagRuntime.java',
            'cpf-starters/platform-operations/feature-flag-openfeature/src/main/java/com/cpf/starter/platform/operations/feature/flag/openfeature/internal/JdbcCpfFeatureFlagStateStore.java',
            'cpf-starters/platform-operations/feature-flag-openfeature/src/main/java/com/cpf/starter/platform/operations/feature/flag/openfeature/internal/JdbcCpfFeatureFlagAuditSink.java'):
            f=root/rel
            if f.is_file(): closure.append(f.read_text(encoding='utf-8',errors='ignore'));evidence.append(rel)
    return '\n'.join(x for x in closure if x),sorted(set(evidence))

def verify_command_matrix(root:Path,out:Path)->dict:
    spec_path=root/'cpf-admin/frontend/openapi/cpf-openapi.json'
    if not spec_path.is_file(): raise HardeningError('ADM OpenAPI source missing for command reliability matrix')
    spec=json.loads(spec_path.read_text(encoding='utf-8-sig'));schemas=spec.get('components',{}).get('schemas',{})
    java_index=_java_index(root);mutations=[];seen=set();findings=[]
    non_mutating_post={
        'admFeatureFlagEvaluate','admBatchJobDefinitionValidate','admRuntimeControlPreviewTargets',
        'admIntegrationDataQualityValidate','admDownloadDownloadCsv','admOperatorRawContact'
    }
    def props(obj:dict)->set[str]:
        result=set();rb=obj.get('requestBody',{})
        for media in (rb.get('content',{}) if isinstance(rb,dict) else {}).values():
            sch=media.get('schema',{}) if isinstance(media,dict) else {}
            if '$ref' in sch: sch=schemas.get(str(sch['$ref']).split('/')[-1],{})
            result.update((sch.get('properties') or {}).keys())
        for param in obj.get('parameters',[]) or []:
            if isinstance(param,dict) and param.get('name'): result.add(str(param['name']))
        return result
    for path,item in (spec.get('paths') or {}).items():
        if not isinstance(item,dict): continue
        for method,obj in item.items():
            if method.upper() not in {'POST','PUT','PATCH','DELETE'} or not isinstance(obj,dict): continue
            oid=str(obj.get('operationId','')).strip();p=props(obj);responses=set(map(str,(obj.get('responses') or {}).keys()))
            if not oid: findings.append(f'{method.upper()} {path}: operationId missing');continue
            if oid in seen: findings.append(f'duplicate mutation operationId: {oid}')
            seen.add(oid);controller=str(obj.get('x-cpf-controller-source') or '')
            if not controller: findings.append(f'{oid}: x-cpf-controller-source missing')
            closure,evidence_paths=_source_closure(root,controller,oid,java_index) if controller else ('',[])
            low=closure.lower()
            evidence={
                'reason':('reason' in p) or bool(re.search(r'\breason\b',low)),
                'audit':bool(re.search(r'\baudit\w*\b|\.record\s*\(|event_type|audit_id',low)),
                'version':bool({'expectedVersion','version'} & p) or any(t in low for t in ('expectedversion','compareandset','optimistic',' for update','revision')),
                'idempotency':bool({'idempotencyKey','requestId'} & p) or any(t in low for t in ('idempotencykey','duplicate convergence','conflicting pending override','request_status=\'approved\'','"revokeD"'.lower())),
                'transaction':any(t in low for t in ('@transactional','transaction.required','executeWithoutResult'.lower(),' for update')),
                'recovery':any(t in low for t in ('unknown_result','unknown','reconcile','retry','rollback','compensat')),
            }
            tier='STANDARD_MUTATION';controls=[k for k,v in evidence.items() if v]
            if oid in non_mutating_post:
                tier='READ_OR_VALIDATION_POST'
            elif oid=='admAuthLogin':
                tier='SESSION_LOGIN'
                # Login is authentication/session creation; reason/CAS is not applicable, but audit/security source must exist.
                if 'auth' not in low and 'session' not in low: findings.append(f'{oid}: authentication/session source evidence missing')
            elif oid=='admAuthLogout':
                tier='SESSION_LOGOUT'
                if not evidence['recovery'] or 'revokedbsession' not in low: findings.append(f'{oid}: DB-backed revoke/UNKNOWN recovery evidence missing')
            elif path.startswith('/adm/api/approvals'):
                tier='APPROVAL_LEDGER'
                for code in ('400','401','403','404','409','429','500','503'):
                    if code not in responses: findings.append(f'{oid}: approval error response missing {code}')
            elif path.startswith('/adm/api/center-cut'):
                tier='IDEMPOTENT_APPROVED_COMMAND'
                for required in ('idempotencyKey','approvalRequestId','reason'):
                    if required not in p: findings.append(f'{oid}: center-cut reliability field missing {required}')
                if '409' not in responses or '503' not in responses: findings.append(f'{oid}: center-cut conflict/unavailable response missing')
            elif '/data-quality/' in path and ('correction' in path or path.endswith('/replay')):
                tier='DQ_APPROVAL_OR_REPLAY'
                if not evidence['reason']: findings.append(f'{oid}: DQ mutation reason missing')
                if '409' not in responses: findings.append(f'{oid}: DQ conflict response missing')
            elif path.startswith('/adm/api/runtime-control') and any(v in oid.lower() for v in ('create','save','delete','cancel','rollback','change')):
                tier='RUNTIME_CONTROL'
                if not evidence['reason']: findings.append(f'{oid}: runtime-control reason missing')
                if not evidence['recovery']: findings.append(f'{oid}: runtime-control recovery/UNKNOWN evidence missing')
            elif path.startswith('/adm/api/file-jobs/') and any(v in oid.lower() for v in ('apply','cancel','retry','rollback','resolve')):
                tier='FILE_JOB_COMMAND'
                if not evidence['reason'] or ('approvalId' not in p and 'approvalid' not in low): findings.append(f'{oid}: file-job approval/reason missing')
                if not evidence['recovery']: findings.append(f'{oid}: file-job recovery evidence missing')
            elif path.startswith('/adm/api/platform/feature-flags'):
                tier='FEATURE_FLAG_COMMAND'
                if not evidence['reason'] or not evidence['audit'] or not evidence['transaction']: findings.append(f'{oid}: feature-flag reason/audit/transaction evidence missing')
                store=(root/'cpf-starters/platform-operations/feature-flag-openfeature/src/main/java/com/cpf/starter/platform/operations/feature/flag/openfeature/internal/JdbcCpfFeatureFlagStateStore.java')
                text=store.read_text(encoding='utf-8',errors='ignore') if store.is_file() else ''
                required_tokens=('conflicting pending override already exists','"APPROVED".equals(q.status)','"REVOKED".equals(status)','desired.equals(rows.getFirst())')
                if not all(t in text for t in required_tokens): findings.append(f'{oid}: feature-flag duplicate-convergence source contract missing')
            else:
                # For ordinary persisted commands: accountable reason + audit + at least one concurrency/dedup/transaction boundary.
                if not evidence['reason']: findings.append(f'{oid}: command reason evidence missing')
                if not evidence['audit']: findings.append(f'{oid}: command audit evidence missing')
                if not (evidence['version'] or evidence['idempotency'] or evidence['transaction']): findings.append(f'{oid}: command concurrency/idempotency/transaction evidence missing')
            mutations.append({'operationId':oid,'method':method.upper(),'path':path,'tier':tier,'controls':controls,'evidence':evidence,'evidencePaths':evidence_paths,'responses':sorted(responses),'controllerSource':controller})
    result={'schemaVersion':2,'source':'cpf-admin/frontend/openapi/cpf-openapi.json','mutationOperationCount':len(mutations),'status':'PASS' if not findings else 'FAIL','findings':findings,'operations':mutations}
    (out/'command-reliability-matrix.json').write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    if findings: raise HardeningError('command reliability matrix failures: '+ '; '.join(findings[:20]))
    return result

def verify_release_performance_profile(path:Path)->dict:
    raw=json.loads(path.read_text(encoding='utf-8-sig'))
    if raw.get('schema_version')!='1.1' or not isinstance(raw.get('workloads'),list): raise HardeningError('release performance profile schema_version=1.1/workloads[] required')
    enabled=[w for w in raw['workloads'] if isinstance(w,dict) and w.get('enabled') is True]
    ids={str(w.get('id','')) for w in enabled}
    soak=[w for w in enabled if isinstance(w.get('duration_seconds'),(int,float)) and w['duration_seconds']>=600]
    load=[w for w in enabled if isinstance(w.get('iterations'),int) and w['iterations']>=1000 and int(w.get('concurrency',0))>=10]
    required_ids={'broker-backpressure','batch-reconcile','resource-budget'}
    findings=[]
    if not load: findings.append('load workload >=1000 iterations/concurrency>=10 missing')
    if not soak: findings.append('soak workload duration_seconds>=600 missing')
    if not required_ids.issubset(ids): findings.append('semantic workloads missing: '+','.join(sorted(required_ids-ids)))
    for w in enabled:
        if int(w.get('sample_reservoir',0))<100 or int(w.get('sample_reservoir',0))>100000: findings.append(f"{w.get('id')}: bounded sample_reservoir invalid")
    if findings: raise HardeningError('; '.join(findings))
    return {'enabled':len(enabled),'load':len(load),'soak':len(soak),'semantic':sorted(required_ids)}

def main()->int:
    ap=argparse.ArgumentParser();ap.add_argument('--root',default='.');ap.add_argument('--expected-head',required=True);ap.add_argument('--evidence-dir',required=True);a=ap.parse_args()
    root=Path(a.root).resolve();expected=a.expected_head.lower().strip()
    if not SHA40.fullmatch(expected): raise HardeningError('--expected-head must be 40-char SHA')
    actual=git(root,'rev-parse','HEAD').lower()
    if actual!=expected: raise HardeningError(f'HEAD mismatch expected={expected} actual={actual}')
    if git(root,'status','--porcelain=v1','--untracked-files=all'): raise HardeningError('hardening qualification requires clean tree')
    out=Path(a.evidence_dir);out=out if out.is_absolute() else root/out;out.mkdir(parents=True,exist_ok=True)
    py=sys.executable;pwsh=shutil.which('pwsh') or ('pwsh.exe' if os.name=='nt' else 'pwsh')
    rows=[];failures=[]

    # H001 Architecture / ownership + actual consumer gates.
    invoke('H001-owner-boundary',[py,'cpf-tools/scripts/verify-cpf-owner-boundaries.py','--root',str(root),'--json-output',str(out/'owner-boundary.json')],root,out,rows,failures)
    invoke('H001-publication-consumers',[py,'cpf-tools/scripts/verify-cpf-publication-starter-closure.py','--root',str(root),'--require-physical','--json-output',str(out/'consumer-publication-closure.json')],root,out,rows,failures)

    # H002 command reliability matrix.
    try:
        matrix=verify_command_matrix(root,out);rows.append({'id':'H002-command-reliability','status':'PASS','exitCode':0,'mutationOperationCount':matrix['mutationOperationCount']})
    except Exception as exc:
        rows.append({'id':'H002-command-reliability','status':'FAIL','exitCode':1,'errorType':type(exc).__name__});failures.append('H002-command-reliability')

    # H003 DB3 lifecycle is executed by the parent release runner on the exact same process/env.
    add_flag('H003-db3-lifecycle',os.getenv('CPF_R6_DB3_PASSED')=='true','successful DB3 lifecycle marker missing',rows,failures)

    # H004 final artifact supply chain.
    supply=out/'supply-chain';supply.mkdir(exist_ok=True)
    invoke('H004-supply-chain-run',[py,'cpf-tools/verification/final-dev/run-r6-supply-chain-qualification.py','--root',str(root),'--expected-head',actual,'--evidence-dir',str(supply)],root,out,rows,failures)
    invoke('H004-supply-chain-verify',[py,'cpf-tools/scripts/verify-cpf-supply-chain.py','--root',str(root),'--release','--evidence-dir',str(supply)],root,out,rows,failures)

    # H005 bounded resource, load and soak.
    perf=Path(os.getenv('CPF_R6_PERFORMANCE_PROFILE','cpf-tools/performance/cpf-r6-release-performance-profile.json'))
    perf=perf if perf.is_absolute() else root/perf
    try:
        meta=verify_release_performance_profile(perf);rows.append({'id':'H005-performance-profile','status':'PASS','exitCode':0,**meta})
    except Exception as exc:
        rows.append({'id':'H005-performance-profile','status':'FAIL','exitCode':1,'errorType':type(exc).__name__});failures.append('H005-performance-profile')
    invoke('H005-resource-probe',[py,'cpf-tools/performance/run-resource-contract.py','--output-json',str(out/'resource-budget.json')],root,out,rows,failures)
    invoke('H005-performance',[py,str(root/'cpf-tools/scripts/run-cpf-performance-contract.py'),'--profile',str(perf),'--output-json',str(out/'performance.json')],root/'cpf-tools/performance',out,rows,failures)

    # H006 observability/audit static contract + live semantic probe.
    invoke('H006-telemetry-static',[py,'cpf-tools/scripts/verify-cpf-telemetry-lifecycle.py','--root',str(root)],root,out,rows,failures)
    invoke('H006-observability-runtime',[py,'cpf-tools/verification/final-dev/run-r6-observability-qualification.py','--expected-head',actual,'--output-json',str(out/'observability-runtime.json')],root,out,rows,failures)

    # H007 threat model + real negative corpus.
    invoke('H007-threat-model',[py,'cpf-tools/scripts/verify-cpf-threat-models.py','--repo-root',str(root),'--manifest-dir',str(root/'cpf-tools/security/threat-models'),'--output-json',str(out/'threat-model.json')],root,out,rows,failures)
    invoke('H007-negative-corpus',[py,'cpf-tools/verification/final-dev/run-r6-security-negative-qualification.py','--root',str(root),'--output-json',str(out/'security-negative.json')],root,out,rows,failures)

    # H008 backup/restore DB3 + split brain/power-loss/selective rollback/reconcile/RPO-RTO probe.
    dr=out/'dr';dr.mkdir(exist_ok=True)
    invoke('H008-dr',[pwsh,'-NoProfile','-File','cpf-tools/verification/final-dev/run-r6-dr-qualification.ps1','-ExpectedHead',actual,'-EvidenceDir',str(dr)],root,out,rows,failures)

    # H009 fresh consumer three mode + fallback negative.
    consumer=out/'artifact-consumer';consumer.mkdir(exist_ok=True)
    invoke('H009-artifact-consumer',[py,'cpf-tools/verification/final-dev/run-r6-artifact-consumer-qualification.py','--root',str(root),'--expected-head',actual,'--evidence-dir',str(consumer)],root,out,rows,failures)

    # H010 create/runtime/remove/regenerate for all official DBs, then independent lifecycle evidence verifier.
    generator=out/'generator';generator.mkdir(exist_ok=True);work=out/'generator-work';work.mkdir(exist_ok=True)
    for vendor in ('oracle','postgresql','mariadb'):
        invoke(f'H010-generator-{vendor}',[pwsh,'-NoProfile','-File','cpf-tools/generator/verify-domain-lifecycle.ps1','-ExpectedSha',actual,'-WorkRoot',str(work),'-DatabaseVendor',vendor,'-EvidenceRoot',str(generator)],root,out,rows,failures)
    invoke('H010-generator-evidence',[py,'cpf-tools/scripts/verify-cpf-generator-lifecycle.py','--root',str(root),'--expected-sha',actual,'--release','--evidence-dir',str(generator)],root,out,rows,failures)

    # H011 bidirectional traceability and repository hygiene.
    result_matrix=root/'cpf-docs/work/r6i-dev/REQUIREMENT_STATUS.csv'
    invoke('H011-traceability',[py,'cpf-tools/scripts/verify-cpf-requirement-traceability.py','--root',str(root),'--result-matrix',str(result_matrix),'--expected-sha',actual,'--require-clean','--release','--json-output',str(out/'traceability.json')],root,out,rows,failures)
    invoke('H011-hygiene',[pwsh,'-NoProfile','-File','cpf-tools/scripts/check-repository-hygiene.ps1','-Root',str(root),'-ResultDir',str(out/'hygiene')],root,out,rows,failures)

    # H012 supported compatibility/failure matrix. Runtime alternatives are static; parent flags prove live matrix execution.
    invoke('H012-runtime-alternatives',[py,'cpf-tools/scripts/verify-cpf-runtime-alternatives.py','--root',str(root),'--report',str(out/'runtime-alternatives.json')],root,out,rows,failures)
    required_flags=(
        'CPF_R6_BROWSER_CHROMIUM_PASSED','CPF_R6_BROWSER_FIREFOX_PASSED','CPF_R6_BROWSER_WEBKIT_PASSED',
        'CPF_R6_DB3_PASSED','CPF_R6_MULTIPROCESS_PASSED','CPF_R6_NETWORK_CHAOS_PASSED','CPF_R6_BROKER_CHAOS_PASSED',
        'CPF_R6_TOOLCHAIN_PASSED','CPF_R6_BUILD_PUBLICATION_PASSED'
    )
    missing=[name for name in required_flags if os.getenv(name)!='true']
    add_flag('H012-compatibility-matrix',not missing,'compatibility evidence flags missing: '+','.join(missing),rows,failures,flags=list(required_flags),missing=missing)

    if git(root,'rev-parse','HEAD').lower()!=actual:
        failures.append('SOURCE_SHA_CHANGED');rows.append({'id':'source-sha-stability','status':'FAIL','exitCode':1})
    if git(root,'status','--porcelain=v1','--untracked-files=all'):
        failures.append('WORKTREE_CHANGED');rows.append({'id':'worktree-stability','status':'FAIL','exitCode':1})
    status='PASS' if not failures else 'FAIL'
    summary={'schemaVersion':1,'protocol':'CPF-R6-HARDENING-QUALIFICATION-2','sourceSha':actual,'status':status,'failureCount':len(failures),'failures':failures,'gates':rows}
    (out/'hardening-summary.json').write_text(json.dumps(summary,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    if failures: raise HardeningError('hardening failures: '+', '.join(failures[:20]))
    print(f'[CPF][R6I][HARDEN][PASS] sourceSha={actual} gates={len(rows)} evidence={out}')
    return 0
if __name__=='__main__':
    try: raise SystemExit(main())
    except HardeningError as e: print(f'[CPF][R6I][HARDEN][FAIL] {e}',file=sys.stderr);raise SystemExit(1)
