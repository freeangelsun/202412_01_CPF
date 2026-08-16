#!/usr/bin/env python3
"""Fail-closed static closure for CPF ADM dangerous-action approval boundaries."""
from pathlib import Path
import json, sys

ROOT=Path(__file__).resolve().parents[3]
checks=[]
def read(rel):
    p=ROOT/rel
    return p.read_text(encoding='utf-8') if p.exists() else ''
def require(name, cond, detail):
    checks.append((name, bool(cond), detail))

def hidden_fail_closed(controller, operation_tokens):
    text=read(controller)
    return all(token in text for token in operation_tokens) and '@Hidden' in text and 'PRECONDITION_REQUIRED' in text

# Direct public mutation surfaces that must be retired in favor of Approval Engine Owner Commands.
file_job=read('cpf-admin/src/main/java/com/cpf/admin/opr/filejob/AdmFileJobController.java')
require('file-job-direct-retired', file_job.count('@Hidden') >= 5 and 'PRECONDITION_REQUIRED' in file_job and 'Approval 요청·독립 승인·Owner Command' in file_job, 'File Job mutations hidden + 428')
require('feature-flag-kill-direct-retired', hidden_fail_closed('cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmFeatureFlagController.java',
        ['FEATURE_FLAG_KILL_SWITCH']), 'Kill switch hidden + 428')
secret=read('cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmSecretController.java')
require('secret-rotate-direct-retired', '@Hidden' in secret and 'PRECONDITION_REQUIRED' in secret and 'Secret Rotation' in secret and 'Owner Command' in secret, 'Secret rotation hidden + 428')
require('cache-direct-retired', hidden_fail_closed('cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmCacheController.java',
        ['CACHE_* Owner Command']), 'Cache Refresh/Evict/Reconcile hidden + 428')
require('dynamic-log-direct-retired', hidden_fail_closed('cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmDynamicLogLevelController.java',
        ['DYNAMIC_LOG_* Owner Command']), 'Dynamic log register/remove hidden + 428')

adapters={
 'file-job-owner':'cpf-admin/src/main/java/com/cpf/admin/opr/filejob/AdmFileJobApprovalOwnerCommandAdapter.java',
 'feature-flag-owner':'cpf-admin/src/main/java/com/cpf/admin/approval/owner/FeatureFlagApprovalOwnerCommandAdapter.java',
 'secret-owner':'cpf-admin/src/main/java/com/cpf/admin/approval/owner/SecretApprovalOwnerCommandAdapter.java',
 'cache-owner':'cpf-admin/src/main/java/com/cpf/admin/approval/owner/CacheApprovalOwnerCommandAdapter.java',
 'dynamic-log-owner':'cpf-admin/src/main/java/com/cpf/admin/approval/owner/DynamicLogLevelApprovalOwnerCommandAdapter.java',
 'service-registry-owner':'cpf-admin/src/main/java/com/cpf/admin/approval/owner/ServiceRegistryApprovalOwnerCommandAdapter.java',
 'runtime-control-owner':'cpf-admin/src/main/java/com/cpf/admin/approval/owner/RuntimeControlApprovalOwnerCommandAdapter.java',
 'gateway-owner':'cpf-admin/src/main/java/com/cpf/admin/approval/owner/GatewayApprovalOwnerCommandAdapter.java',
}
for name, path in adapters.items():
    text=read(path)
    require(name, 'implements AdmApprovalOwnerCommandPort' in text and 'supports(' in text and 'requestedBy()' in text and 'approvedBy()' in text,
            f'{path} exact owner tuple + SoD')

# Existing controlled paths remain protected; do not force a duplicate approval implementation.
runtime=read('cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmRuntimeControlController.java')
require('runtime-cancel-rollback-direct-blocked', 'RUNTIME_CONTROL_CANCEL' in runtime and 'RUNTIME_CONTROL_ROLLBACK' in runtime and 'PRECONDITION_REQUIRED' in runtime,
        'Runtime cancel/rollback require Approval Engine')
gateway=read('cpf-admin/src/main/java/com/cpf/admin/opr/gateway/AdmGatewayRegistryController.java')
require('gateway-dangerous-state-owner-only', 'APPROVED' in gateway and 'ACTIVE' in gateway and 'BLOCKED' in gateway and 'RETIRED' in gateway and 'Approval Owner' in gateway,
        'Gateway dangerous state transitions route to Approval Owner')
batch=read('cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmBatchApprovalService.java')
require('batch-approval-service-integrity', all(x in batch for x in ['assertIndependentApproval','command_payload_hash','expire_at','idempotencyKey','expectedVersion']),
        'Batch retains dedicated approval/CAS/idempotency boundary')

# Browser must request approval rather than invoke retired direct mutations.
front='\n'.join(read(p) for p in [
 'cpf-admin/frontend/src/features/file-jobs/FileJobsPage.vue',
 'cpf-admin/frontend/src/features/feature-flags/FeatureFlagsPage.vue',
 'cpf-admin/frontend/src/features/secrets/SecretsPage.vue',
 'cpf-admin/frontend/src/app/methods/referenceMethods.ts',
 'cpf-admin/frontend/src/app/methods/observabilityMethods.ts',
 'cpf-admin/frontend/src/app/methods/routeClosureMethods.ts'])
require('frontend-approval-consumers', front.count('admApprovalRequest') >= 5 and 'CPF-DATA-CACHE' in front and 'CPF-PLATFORM-OBSERVABILITY' in front,
        'Dangerous UIs create Approval Engine requests')

openapi_path=ROOT/'cpf-admin/frontend/openapi/cpf-openapi.json'
if openapi_path.exists():
    doc=json.loads(openapi_path.read_text(encoding='utf-8'))
    public={op.get('operationId') for item in doc.get('paths',{}).values() for op in item.values() if isinstance(op,dict)}
    retired={'admFileJobApply','admFileJobRetry','admFileJobCancel','admFileJobRollback','admFileJobResolveUnknown',
             'admFeatureFlagSetKillSwitch','admSecretRotate','admCacheRefresh','admCacheEvictKey','admCacheEvictNamespace','admCacheReconcile',
             'admDynamicLogLevelRegister','admDynamicLogLevelRemove'}
    require('openapi-retired-dangerous-operations-absent', not (public & retired), f'public retired intersection={sorted(public & retired)}')
else:
    require('openapi-retired-dangerous-operations-absent', False, 'ADM OpenAPI missing')

failed=[c for c in checks if not c[1]]
for name, ok, detail in checks:
    print(f"{'PASS' if ok else 'FAIL'} {name}: {detail}")
print(f"CPF_ADM_DANGEROUS_APPROVAL_BOUNDARY={'PASS' if not failed else 'FAIL'} checks={len(checks)} failed={len(failed)}")
sys.exit(1 if failed else 0)
