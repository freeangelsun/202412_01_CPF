#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, shutil, subprocess, sys, tempfile
from pathlib import Path
class GateError(RuntimeError): pass

def read(root, rel):
    p=root/rel
    if not p.is_file(): raise GateError(f'missing source: {rel}')
    return p.read_text(encoding='utf-8')
def require(ok,msg):
    if not ok: raise GateError(msg)
def main():
    ap=argparse.ArgumentParser();ap.add_argument('--root',type=Path,default=Path('.'));ap.add_argument('--self-test',action='store_true');a=ap.parse_args();root=a.root.resolve()
    wf=read(root,'.github/workflows/cpf-r6-release-gates.yml')
    require('CPF_ADM_FRONTEND_URL: ${{ vars.CPF_ADM_FRONTEND_URL }}' in wf,'ADM frontend canonical variable missing')
    require('CPF_FRONTEND_URL: ${{ vars.CPF_ADM_FRONTEND_URL }}' not in wf,'legacy ADM frontend variable survives workflow')
    ctl=read(root,'cpf-reference/src/main/java/com/cpf/reference/edu/runtime/api/EduCapabilityController.java')
    for forbidden in ('@RequestHeader("X-Cpf-Actor-Id")','@RequestHeader("X-Cpf-Roles")','@RequestHeader("X-Cpf-Data-Scope")'):
        require(forbidden not in ctl,'caller-provided EDU authority survives: '+forbidden)
    require('Authentication authentication' in ctl and 'security(authentication)' in ctl,'framework-owned EDU security context missing')
    registry=read(root,'cpf-reference/src/main/java/com/cpf/reference/edu/runtime/application/EduCapabilityRegistry.java')
    require('\"reference-operations\",4,' in registry,'reference-operations executable count must be 4 after R6J architecture decision')
    contributor=read(root,'cpf-reference/src/main/java/com/cpf/reference/optional/operations/config/ReferenceOperationsCapabilityContributor.java')
    for retained in ('EduAdm02Handler','EduAdm03Handler','EduAdm04Handler','EduAdm07Handler'):
        require(('new '+retained+'()') in contributor,'retained ADM extension missing: '+retained)
    for disabled in ('EduAdm01Handler','EduAdm05Handler','EduAdm06Handler','EduAdm08Handler','EduAdm09Handler','EduAdm10Handler','EduAdm11Handler','EduAdm12Handler','EduAdm13Handler','EduAdm14Handler','EduAdm15Handler','EduAdm16Handler','EduAdm17Handler'):
        require(('new '+disabled+'()') not in contributor,'non-extension ADM remains executable: '+disabled)
    import json
    catalog=json.loads(read(root,'cpf-reference/src/main/resources/edu/manual-135-catalog.json'))
    features=catalog.get('features',[])
    require(len(features)==135,'manual EDU topic count must remain 135')
    require(sum(1 for f in features if f.get('executable',True))==122,'executable EDU capability count must be 122')
    adm=[f for f in features if f.get('requirementId','').startswith('EDU-ADM-')]
    counts={k:sum(1 for f in adm if f.get('architectureDecision')==k) for k in ('PRODUCT_ADM','EXTENSION_SAMPLE','MERGE_EDU')}
    require(counts=={'PRODUCT_ADM':9,'EXTENSION_SAMPLE':4,'MERGE_EDU':4},'EDU-ADM architecture decision distribution drift: '+str(counts))
    require(all(f.get('executable')==(f.get('architectureDecision')=='EXTENSION_SAMPLE') for f in adm),'EDU-ADM executable flag does not follow architecture decision')
    proc=read(root,'cpf-reference/src/main/java/com/cpf/reference/edu/runtime/consumer/process/ProcessEduBusinessConsumer.java')
    require('childEnvironment.clear()' in proc,'PROCESS environment is not cleared')
    require('json.writeValue(stdin, command.payload())' in proc,'PROCESS minimal stdin IPC missing')
    require('CPF_EDU_PAYLOAD_FILE' not in proc,'PROCESS temp payload file survives')
    script=read(root,'cpf-reference/src/main/scripts/edu/invoke-reference-edu.ps1')
    require('[Console]::In.ReadToEnd()' in script and 'CPF_EDU_PAYLOAD_FILE' not in script,'PROCESS script must consume stdin only')
    routes=read(root,'cpf-biz-admin/frontend/src/app/routes.ts')
    retired=('bzaBackofficeFindApprovals','bzaBackofficeCreateApproval','bzaBackofficeFindApproval','bzaBackofficeActApproval')
    for op in retired: require(op not in routes,'retired HTTP 410 operation remains active route metadata: '+op)
    inbox=read(root,'cpf-biz-admin/frontend/src/features/approval-inbox/ApprovalInboxPage.vue')
    require('hasBzaPermission("APPROVAL","DECIDE")' in inbox and 'v-if="canDecide"' in inbox,'BZA approval action permission projection missing')
    for rel,permission,guard in (
        ('cpf-biz-admin/frontend/src/features/approval-submissions/ApprovalSubmissionsPage.vue','hasBzaPermission("APPROVAL","WRITE")','canWrite'),
        ('cpf-biz-admin/frontend/src/features/approval-policies/ApprovalPoliciesPage.vue','hasBzaPermission("APPROVAL","WRITE")','canWrite'),
        ('cpf-biz-admin/frontend/src/features/approval-delegations/ApprovalDelegationsPage.vue','hasBzaPermission("APPROVAL","WRITE")','canWrite')):
        page=read(root,rel)
        require(permission in page and guard in page, 'BZA mutation permission projection missing: '+rel)
    risky_ids=('FEATURE_FLAG_OVERRIDE_REQUEST','FEATURE_FLAG_OVERRIDE_APPROVE','FEATURE_FLAG_OVERRIDE_REVOKE',
               'FEATURE_FLAG_KILL_SWITCH','OPENAPI_REFRESH','RESILIENCE_POLICY_REQUEST','RESILIENCE_POLICY_APPROVE','RESILIENCE_POLICY_REJECT')
    for vendor in ('oracle','postgresql','mariadb'):
        perm_sql=read(root,f'cpf-tools/db/vendor/{vendor}/source/23_adm_risky_action_permissions_r6j.sql')
        for action_id in risky_ids:
            require(action_id in perm_sql, f'{vendor} risky ADM action permission missing: {action_id}')
        if vendor == 'oracle':
            require("'OPENAPI_REFRESH' BUTTON_ID" in perm_sql and "'/adm/api/openapi/refresh' API_PATH" in perm_sql,
                    'oracle OPENAPI_REFRESH button/API contract missing')
            require("'ADM_ADMIN' ROLE_ID, 'OPENAPI_REFRESH' BUTTON_ID, 'Y' ALLOW_YN" in perm_sql
                    and "'ADM_OPERATOR' ROLE_ID, 'OPENAPI_REFRESH' BUTTON_ID, 'N' ALLOW_YN" in perm_sql,
                    'oracle risky ADM permission must be explicit default-deny outside ADM_ADMIN')
        else:
            require("('OPENAPI_REFRESH','OPENAPI_OPERATIONS','REFRESH'" in perm_sql,
                    f'{vendor} OPENAPI_REFRESH button contract missing')
            require("('API_OPENAPI_REFRESH','OPENAPI_OPERATIONS','POST','/adm/api/openapi/refresh'" in perm_sql,
                    f'{vendor} OPENAPI_REFRESH API contract missing')
            require("('ADM_ADMIN','OPENAPI_REFRESH','Y'" in perm_sql and "('ADM_OPERATOR','OPENAPI_REFRESH','N'" in perm_sql,
                    f'{vendor} risky ADM permission must be explicit default-deny outside ADM_ADMIN')
    batch_reconcile=read(root,'cpf-admin/src/main/java/com/cpf/admin/approval/owner/BatchJobDefinitionApprovalOwnerCommandAdapter.java')
    require('public AdmApprovedOperationResult reconcile' in batch_reconcile and 'port.state(' in batch_reconcile,'BAT job definition reconcile must observe owner state without replay')
    gateway_reconcile=read(root,'cpf-admin/src/main/java/com/cpf/admin/approval/owner/GatewayApprovalOwnerCommandAdapter.java')
    require('public AdmApprovedOperationResult reconcile' in gateway_reconcile and 'registry.findBindings' in gateway_reconcile,'Gateway reconcile must observe owner state without replay')
    batch_runtime=read(root,'cpf-admin/src/main/java/com/cpf/admin/approval/owner/BatchRuntimeApprovalOwnerCommandAdapter.java')
    require('Set.of("SUCCEEDED", "SUCCESS", "COMPLETED").contains(state)' in batch_runtime,
            'BAT reconcile must only promote terminal successful owner states')
    require('Set.of("SUCCEEDED", "SUCCESS", "COMPLETED", "REQUESTED"' not in batch_runtime,
            'BAT reconcile incorrectly promotes non-terminal REQUESTED/ACCEPTED/RUNNING')
    broker_reconcile=read(root,'cpf-admin/src/main/java/com/cpf/admin/approval/owner/BrokerReliabilityApprovalOwnerCommandAdapter.java')
    require('Set.of("REPLAYED", "DELIVERED", "RESOLVED", "SUCCEEDED").contains(state)' in broker_reconcile,
            'Broker reconcile must require terminal replay observation')
    require('Set.of("REPLAY_REQUESTED", "REPLAYING"' not in broker_reconcile,
            'Broker reconcile incorrectly promotes non-terminal replay states')
    center_reconcile=read(root,'cpf-admin/src/main/java/com/cpf/admin/approval/owner/CenterCutApprovalOwnerCommandAdapter.java')
    require('owner.observe(risk.targetId())' in center_reconcile and 'failedCount' in center_reconcile and 'unknownCount' in center_reconcile,
            'Center-Cut reconcile must use owner observation counters')
    for vendor in ('oracle','postgresql','mariadb'):
        cc_sql=read(root,f'cpf-tools/db/vendor/{vendor}/runtime/bat/repository/centercut-reconcile-load-execution.sql').lower()
        require('failure_count as failed_count' in cc_sql and 'unknown_count' in cc_sql,
                f'{vendor} Center-Cut observation SQL must expose failure/unknown counters')
    obs=read(root,'cpf-tools/verification/final-dev/run-r6-observability-qualification.py')
    require("STORES = ('metric', 'log', 'trace', 'alert', 'audit')" in obs,'authoritative observability stores missing')
    require("CPF-R6J-OBSERVABILITY-AUTHORITATIVE-RECORDS" in obs,'authoritative observability protocol missing')
    require('only concrete records count' in obs,'observability must reject self-attested boolean proof')
    catalog=json.loads(read(root,'cpf-reference/src/main/resources/edu/manual-135-catalog.json'))
    features=catalog.get('features',[])
    require(len(features)==135,'EDU catalog count drift')
    adm=[f for f in features if str(f.get('requirementId','')).startswith('EDU-ADM-')]
    counts={k:sum(1 for f in adm if f.get('architectureDecision')==k) for k in ('PRODUCT_ADM','EXTENSION_SAMPLE','MERGE_EDU')}
    require(counts=={'PRODUCT_ADM':9,'EXTENSION_SAMPLE':4,'MERGE_EDU':4},'EDU ADM architecture distribution drift: '+str(counts))
    require(sum(1 for f in features if f.get('executable',True))==122,'EDU executable count must be 122 after ADM reclassification')
    retained={f.get('requirementId') for f in adm if f.get('architectureDecision')=='EXTENSION_SAMPLE' and f.get('executable',True)}
    require(retained=={'EDU-ADM-02','EDU-ADM-03','EDU-ADM-04','EDU-ADM-07'},'retained ADM extension set drift: '+str(retained))
    for f in adm:
        decision=f.get('architectureDecision')
        if decision=='PRODUCT_ADM':
            require(f.get('executable') is False and f.get('productOwnerModule')=='cpf-admin' and bool(f.get('productSurface')),'PRODUCT_ADM catalog ownership incomplete: '+f.get('requirementId','?'))
        elif decision=='MERGE_EDU':
            require(f.get('executable') is False and bool(f.get('mergedIntoRequirementId')),'MERGE_EDU redirect incomplete: '+f.get('requirementId','?'))
    registry=read(root,'cpf-reference/src/main/java/com/cpf/reference/edu/runtime/application/EduCapabilityRegistry.java')
    require('"reference-operations",4' in registry,'reference-operations runtime count must be 4')
    contributor=read(root,'cpf-reference/src/main/java/com/cpf/reference/optional/operations/config/ReferenceOperationsCapabilityContributor.java')
    for rid_class in ('EduAdm02Handler','EduAdm03Handler','EduAdm04Handler','EduAdm07Handler'):
        require(rid_class in contributor,'retained ADM extension handler missing: '+rid_class)
    for retired_class in ('EduAdm01Handler','EduAdm05Handler','EduAdm06Handler','EduAdm08Handler','EduAdm09Handler','EduAdm10Handler','EduAdm11Handler','EduAdm12Handler','EduAdm13Handler','EduAdm14Handler','EduAdm15Handler','EduAdm16Handler','EduAdm17Handler'):
        require(('new '+retired_class+'()') not in contributor,'non-extension ADM handler remains executable: '+retired_class)
    spool=read(root,'cpf-core/src/main/java/com/cpf/core/common/logging/file/CpfFileLogRecoverySpool.java')
    require('cpfRecoveryChecksum' in spool and '#cpf-spool:' not in spool,'file-log recovery must preserve structured JSON')
    require('createSecureDirectory' in spool and 'secureFile' in spool,'file-log recovery spool permission hardening missing')
    require('terminalLoss' in spool and 'quarantine' in spool and 'baseBackoffMillis' in spool,'file-log recovery observability/retry contract missing')
    writer=read(root,'cpf-core/src/main/java/com/cpf/core/common/logging/file/CpfFileLogWriter.java')
    require('implements CpfFileLogRuntimeStatus, AutoCloseable' in writer and 'recoverySpool.enqueue' in writer and 'public void close()' in writer,'file-log writer durable recovery lifecycle missing')
    for rel,token in {
      'cpf-admin/src/main/java/com/cpf/admin/approval/owner/BatchJobDefinitionApprovalOwnerCommandAdapter.java':'port.state(',
      'cpf-admin/src/main/java/com/cpf/admin/approval/owner/BatchRuntimeApprovalOwnerCommandAdapter.java':'public AdmApprovedOperationResult reconcile',
      'cpf-admin/src/main/java/com/cpf/admin/approval/owner/BrokerReliabilityApprovalOwnerCommandAdapter.java':'public AdmApprovedOperationResult reconcile',
      'cpf-admin/src/main/java/com/cpf/admin/approval/owner/CenterCutApprovalOwnerCommandAdapter.java':'public AdmApprovedOperationResult reconcile',
      'cpf-admin/src/main/java/com/cpf/admin/approval/owner/GatewayApprovalOwnerCommandAdapter.java':'registry.findBindings',
      'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatApprovalOwnerCommandPort.java':'public AdmApprovedOperationResult reconcile'
    }.items():
        body=read(root,rel); require(token in body,'observation reconcile missing: '+rel)
    if a.self_test:
        # Real mutation execution: copy the complete supplied root, inject one defect at a time,
        # execute this same gate as a child process, and require a non-zero result.
        mutations={
          'edu-auth':('cpf-reference/src/main/java/com/cpf/reference/edu/runtime/api/EduCapabilityController.java',
                      'Authentication authentication', '@RequestHeader("X-Cpf-Actor-Id") String actor'),
          'edu-architecture':('cpf-reference/src/main/java/com/cpf/reference/optional/operations/config/ReferenceOperationsCapabilityContributor.java',
                              'new EduAdm07Handler());', 'new EduAdm07Handler(), new EduAdm08Handler());'),
          'process-env':('cpf-reference/src/main/java/com/cpf/reference/edu/runtime/consumer/process/ProcessEduBusinessConsumer.java',
                         'childEnvironment.clear()', '// MUTATION: inherited environment'),
          'bza-permission':('cpf-biz-admin/frontend/src/features/approval-inbox/ApprovalInboxPage.vue',
                            'hasBzaPermission("APPROVAL","DECIDE")', 'true'),
          'adm-risk-permission':('cpf-tools/db/vendor/postgresql/source/23_adm_risky_action_permissions_r6j.sql',
                            "('OPENAPI_REFRESH','OPENAPI_OPERATIONS','REFRESH'", "('OPENAPI_REFRESH_REMOVED','OPENAPI_OPERATIONS','REFRESH'"),
          'workflow-url':('.github/workflows/cpf-r6-release-gates.yml',
                          'CPF_ADM_FRONTEND_URL: ${{ vars.CPF_ADM_FRONTEND_URL }}',
                          'CPF_FRONTEND_URL: ${{ vars.CPF_ADM_FRONTEND_URL }}'),
          'observability':('cpf-tools/verification/final-dev/run-r6-observability-qualification.py',
                            "CPF-R6J-OBSERVABILITY-AUTHORITATIVE-RECORDS",
                            "CPF-R6J-OBSERVABILITY-SELF-ATTESTED"),
          'edu-catalog':('cpf-reference/src/main/resources/edu/manual-135-catalog.json',
                            '"architectureDecision": "EXTENSION_SAMPLE"',
                            '"architectureDecision": "PRODUCT_ADM"'),
          'spool-structured':('cpf-core/src/main/java/com/cpf/core/common/logging/file/CpfFileLogRecoverySpool.java',
                            'cpfRecoveryChecksum', '#cpf-spool:'),
          'bat-terminal-state':('cpf-admin/src/main/java/com/cpf/admin/approval/owner/BatchRuntimeApprovalOwnerCommandAdapter.java',
                            'Set.of("SUCCEEDED", "SUCCESS", "COMPLETED").contains(state)',
                            'Set.of("SUCCEEDED", "SUCCESS", "COMPLETED", "REQUESTED").contains(state)'),
          'broker-terminal-state':('cpf-admin/src/main/java/com/cpf/admin/approval/owner/BrokerReliabilityApprovalOwnerCommandAdapter.java',
                            'Set.of("REPLAYED", "DELIVERED", "RESOLVED", "SUCCEEDED").contains(state)',
                            'Set.of("REPLAY_REQUESTED", "REPLAYING", "REPLAYED", "DELIVERED", "RESOLVED", "SUCCEEDED").contains(state)'),
          'centercut-observation':('cpf-tools/db/vendor/postgresql/runtime/bat/repository/centercut-reconcile-load-execution.sql',
                            'failure_count AS failed_count,',
                            'failure_count AS omitted_failed_count,'),
        }
        this_gate=Path(__file__).resolve()
        for name,(rel,old,new) in mutations.items():
            with tempfile.TemporaryDirectory(prefix=f'cpf-r6j-mutation-{name}-') as td:
                mutated_root=Path(td)/'root'
                shutil.copytree(root,mutated_root)
                target=mutated_root/rel
                source=target.read_text(encoding='utf-8')
                require(old in source,f'self-test fixture absent: {name}')
                target.write_text(source.replace(old,new),encoding='utf-8')
                child=subprocess.run([sys.executable,str(this_gate),'--root',str(mutated_root)],
                                     capture_output=True,text=True,timeout=60)
                require(child.returncode!=0,
                        f'real mutation was not killed by gate: {name}; stdout={child.stdout[-500:]} stderr={child.stderr[-500:]}')
    print('[CPF][R6J][PASS] rework contracts selfTest='+str(a.self_test).lower())
if __name__=='__main__':
    try: main()
    except GateError as e: print('[CPF][R6J][FAIL] '+str(e)); raise SystemExit(1)
