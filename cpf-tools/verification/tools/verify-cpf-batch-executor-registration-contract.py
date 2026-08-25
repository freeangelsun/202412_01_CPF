#!/usr/bin/env python3
from pathlib import Path
import sys
root=Path(__file__).resolve().parents[3]
checks=[]
def need(rel,*tokens):
    p=root/rel
    text=p.read_text(encoding='utf-8') if p.exists() else ''
    missing=[t for t in tokens if t not in text]
    checks.append((rel,not missing,missing))
need('cpf-batch/api/src/main/java/com/cpf/batch/api/BatchJobDefinition.java',
     'FILE_WATCH','CENTER_CUT','FILE_WATCH:<approvedPathAlias>','CENTER_CUT:<centerCutJobId>','centerCutJobId()')
need('cpf-batch/worker/src/main/java/com/cpf/batch/worker/SpringBatchWorkerStepHandler.java',
     'case FILE_WATCH -> fileWatch','files.awaitReady','FILE_WATCH_READY')
need('cpf-batch/control-plane/src/main/java/com/cpf/batch/control/centercut/CenterCutBatchStepHandler.java',
     'ExecutorType.CENTER_CUT','CpfCenterCutOperations','operations.launch','CENTER_CUT_EXECUTION_CREATED')
need('cpf-batch/control-plane/src/main/java/com/cpf/batch/control/job/BatchJobDefinitionService.java',
     'centercut-job-find-active','CENTER_CUT target job does not exist','CENTER_CUT target job is disabled')
need('cpf-admin/frontend/src/features/batch-job-packs/BatchJobPacksPage.vue',
     "value:'CENTER_CUT'","value:'FILE_WATCH'",'fetchCenterCutJobs','fetchPathAliases','감시 상대경로','승인 Script Catalog Key')
need('cpf-admin/frontend/src/features/batch-runtime-control/api.ts','admCenterCutFindJobs','admParameterReferenceSearch','fetchPathAliases')
need('cpf-batch/center-cut/src/main/resources/application.yml','CPF_SYSTEM_CODE:CEC','CENTER_CUT_RUNNER')
need('cpf-batch/center-cut/src/main/java/com/cpf/batch/centercut/runner/CenterCutApplication.java','fromCenterCutEnvironment')
fail=[x for x in checks if not x[1]]
for rel,ok,missing in checks:
    print(('PASS' if ok else 'FAIL'),rel,('' if ok else 'missing='+','.join(missing)))
print(f'CPF_BATCH_EXECUTOR_REGISTRATION_CONTRACT={"PASS" if not fail else "FAIL"} checks={len(checks)} failures={len(fail)}')
sys.exit(1 if fail else 0)
