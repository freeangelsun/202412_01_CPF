#!/usr/bin/env python3
"""Fail closed if BAT runtime commands can bypass central optimistic-version fencing.

Canonical architecture:
Browser approval id -> immutable approval risk snapshot -> BAT owner adapter ->
JdbcRuntimeRegistry adapter -> CpfManagedRuntimeRegistry -> CpfRuntimeControlPlaneRepository exact CAS.
Batch DB owns telemetry/capacity only; central OPS_RUNTIME_INSTANCE_STATE owns lifecycle/version.
"""
from __future__ import annotations
import argparse,json,re
from pathlib import Path
class VersionError(RuntimeError): pass

def read(p:Path)->str:
    if not p.is_file(): raise VersionError(f'missing {p}')
    return p.read_text(encoding='utf-8-sig',errors='replace')

def method(text:str,name:str)->str:
    for m in re.finditer(rf'\b{re.escape(name)}\s*\(',text):
        paren=text.find('(',m.start()); depth=0; close=-1
        for i in range(paren,len(text)):
            if text[i]=='(': depth+=1
            elif text[i]==')':
                depth-=1
                if depth==0: close=i; break
        if close<0: continue
        k=close+1
        while k<len(text) and text[k].isspace(): k+=1
        if k>=len(text) or text[k]!='{': continue
        op=k; depth=0
        for i in range(op,len(text)):
            if text[i]=='{': depth+=1
            elif text[i]=='}':
                depth-=1
                if depth==0: return text[op+1:i]
    raise VersionError(f'method missing {name}')

def verify(root:Path)->dict:
    root=root.resolve()
    cp=root/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java'
    rq=root/'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeCommandRequest.java'
    ap=root/'cpf-admin/src/main/java/com/cpf/admin/approval/owner/BatchRuntimeApprovalOwnerCommandAdapter.java'
    rc=root/'cpf-batch/api/src/main/java/com/cpf/batch/api/RuntimeCommand.java'
    ba=root/'cpf-batch/control-plane/src/main/java/com/cpf/batch/control/internal/JdbcRuntimeRegistry.java'
    port=root/'cpf-starters/platform-operations/src/main/java/com/cpf/platform/operations/runtimecontrol/api/CpfManagedRuntimeRegistry.java'
    provider=root/'cpf-starters/platform-operations/runtime-control/src/main/java/com/cpf/platform/operations/runtimecontrol/internal/CpfJdbcManagedRuntimeRegistry.java'
    repo=root/'cpf-starters/platform-operations/runtime-control/src/main/java/com/cpf/platform/operations/runtimecontrol/internal/CpfRuntimeControlPlaneRepository.java'
    c,request,adapter,contract,batch,port_text,prov,rep = map(read,(cp,rq,ap,rc,ba,port,provider,repo))
    command=method(c,'command'); execute=method(adapter,'executeRuntimeCommand'); batch_update=method(batch,'updateDesiredState'); central_update=method(rep,'updateManagedDesiredState')
    checks={
      'browser_command_requires_approval_id':'approvalRequestId' in command and 'approvalService.execute' in command,
      'browser_request_does_not_accept_target_or_version':all(t not in request for t in ('expectedVersion','targetIds','commandType','approvedBy')),
      'approval_snapshot_version_required':'risk.expectedVersion() == null' in execute or 'risk.expectedVersion()==null' in execute,
      'approval_owner_forwards_exact_snapshot_version':'request.put("expectedVersion", risk.expectedVersion())' in execute,
      'runtime_command_rejects_negative':bool(re.search(r'expectedVersion\s*<\s*0',contract)),
      'batch_registry_is_central_adapter':'CpfManagedRuntimeRegistry' in batch and 'private final CpfManagedRuntimeRegistry central' in batch,
      'batch_registry_forwards_exact_version':bool(re.search(r'central\.updateDesiredState\s*\(\s*instanceId\s*,\s*desired\.name\(\)\s*,\s*expectedVersion\s*\)',batch_update,re.S)),
      'batch_registry_does_not_own_lifecycle_sql': 'OPS_RUNTIME_INSTANCE_STATE' not in batch and 'control_row_version' not in batch,
      'central_port_has_versioned_update':bool(re.search(r'long\s+updateDesiredState\s*\([^)]*long\s+expectedVersion',port_text,re.S)),
      'provider_forwards_exact_version':bool(re.search(r'repository\.updateManagedDesiredState\s*\(\s*instanceId\s*,\s*desiredState\s*,\s*expectedVersion\s*\)',prov,re.S)),
      'central_repo_rejects_negative':bool(re.search(r'expectedVersion\s*<\s*0',central_update)),
      'central_repo_exact_sql_cas':'control_row_version=?' in central_update and 'expectedVersion' in central_update,
      'central_repo_no_current_fallback':not bool(re.search(r'expectedVersion\s*>\s*0\s*\?',central_update)),
      'central_repo_conflict_reports_current':'CpfRuntimeVersionConflictException' in central_update and 'current.controlVersion()' in central_update,
    }
    missing=[k for k,v in checks.items() if not v]
    result={'status':'PASS' if not missing else 'FAIL','files':[p.relative_to(root).as_posix() for p in (cp,rq,ap,rc,ba,port,provider,repo)],'checks':checks,'findings':missing,'architecture':'BROWSER_APPROVAL_ID -> IMMUTABLE_APPROVAL_SNAPSHOT -> BAT_ADAPTER -> CENTRAL_RUNTIME_REGISTRY -> EXACT_VERSION_CAS'}
    if missing: raise VersionError(json.dumps(result,ensure_ascii=False,indent=2))
    return result

def main()->int:
    p=argparse.ArgumentParser(); p.add_argument('--root',default='.'); p.add_argument('--json-output'); a=p.parse_args()
    try:r=verify(Path(a.root)); code=0
    except Exception as e:
        try:r=json.loads(str(e))
        except:r={'status':'FAIL','message':str(e)}
        code=1
    text=json.dumps(r,ensure_ascii=False,indent=2)
    if a.json_output:
        o=Path(a.json_output); o=o if o.is_absolute() else Path(a.root).resolve()/o; o.parent.mkdir(parents=True,exist_ok=True); o.write_text(text+'\n',encoding='utf-8')
    print(text); return code
if __name__=='__main__': raise SystemExit(main())
