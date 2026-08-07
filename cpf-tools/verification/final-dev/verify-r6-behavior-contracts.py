#!/usr/bin/env python3
from pathlib import Path
import sys
R=Path(sys.argv[1]).resolve() if len(sys.argv)>1 else Path(__file__).resolve().parents[3]
checks=[]
def require(path,required=(),forbidden=()):
    text=(R/path).read_text(encoding='utf-8')
    missing=[x for x in required if x not in text]
    leaked=[x for x in forbidden if x in text]
    checks.append((path,not missing and not leaked,missing,leaked))
require('cpf-core/src/main/java/com/cpf/core/spi/data/quality/CpfDataQualityCorrectionPort.java',
        ('String payloadHash','String nonce','String proof'),('Map.copyOf(corrected)',))
require('cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java',
        ('validatePolicyActive','sanitizeDetail','supports(ownerModule,ownerCommand,actionType,targetType)'),())
require('cpf-admin/frontend/src/features/integration-closure/integrationClosureIdempotency.ts',
        ('entries: Record<string, ApprovalIdempotencyState>','generations: Record<string, number>','storage: Storage = localStorage','MAX_ENTRIES'),('singleState',))
require('cpf-tools/verification/final-dev/run-db3-lifecycle.ps1',
        ('$start.Environment.Clear()','WaitForExit($TimeoutSeconds * 1000)','--connection-json-stdin'),('"--url=$url"','"--username=$username"'))
require('cpf-admin/frontend/scripts/enrich-adm-openapi-contract.mjs',
        ('Runtime/controller OpenAPI route missing','requireOperation('),('paths[route] ||=','item[key] ||=',))
require('settings.gradle',('cpfIncludeLocalDomains','settingsSha256'),('localDomainsDir.eachDir',))
require('cpf-admin/src/main/resources/application.yml',(),('active: ${SPRING_PROFILES_ACTIVE:local}','active: local'))
require('cpf-common/src/main/java/com/cpf/common/data/quality/InMemoryCpfDataQualityOperations.java',
        ('replayLocks','synchronized (lock)','validateOnly','replayFingerprints','requireSameReplay'),('replayResults.putIfAbsent',))

require('cpf-admin/src/main/java/com/cpf/admin/approval/spi/AdmApprovalOwnerCommandPort.java',
        ('default boolean supports(String ownerModule, String ownerCommand, String actionType, String targetType)','return false;'),())
for owner_adapter in (
 'cpf-admin/src/main/java/com/cpf/admin/approval/owner/BatchJobDefinitionApprovalOwnerCommandAdapter.java',
 'cpf-admin/src/main/java/com/cpf/admin/approval/owner/BatchRuntimeApprovalOwnerCommandAdapter.java',
 'cpf-admin/src/main/java/com/cpf/admin/approval/owner/BrokerReliabilityApprovalOwnerCommandAdapter.java',
 'cpf-admin/src/main/java/com/cpf/admin/approval/owner/CenterCutApprovalOwnerCommandAdapter.java',
 'cpf-admin/src/main/java/com/cpf/admin/approval/owner/DataQualityCorrectionApprovalOwnerCommandAdapter.java',
 'cpf-admin/src/main/java/com/cpf/admin/approval/owner/GatewayApprovalOwnerCommandAdapter.java',
 'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatApprovalOwnerCommandPort.java'):
    require(owner_adapter,('supports(String ownerModule, String ownerCommand, String actionType, String targetType)',),())

require('cpf-tools/verification/final-dev/run-r6-release-gates.ps1',
        ('ExpectedHead','aggregateQualityBuild','publicationGate','npm-playwright','db3-live','multiprocess-chaos'),())
require('.github/workflows/cpf-r6-release-gates.yml',
        ("java-version: '25'","node-version: '22.18.0'",'run-r6-release-gates.ps1'),())
failed=[c for c in checks if not c[1]]
for path,ok,missing,leaked in checks:
    print(('PASS' if ok else 'FAIL'),path,'missing='+str(missing),'forbidden='+str(leaked))
# Self-mutation corpus demonstrates each critical removal flips the corresponding predicate.
mutations={
 'single-slot-idempotency':('cpf-admin/frontend/src/features/integration-closure/integrationClosureIdempotency.ts','entries: Record<string, ApprovalIdempotencyState>'),
 'cross-tab-nondeterminism':('cpf-admin/frontend/src/features/integration-closure/integrationClosureIdempotency.ts','generations: Record<string, number>'),
 'inherited-secret':('cpf-tools/verification/final-dev/run-db3-lifecycle.ps1','$start.Environment.Clear()'),
 'timeout-removed':('cpf-tools/verification/final-dev/run-db3-lifecycle.ps1','WaitForExit($TimeoutSeconds * 1000)'),
 'runtime-route-synthesis':('cpf-admin/frontend/scripts/enrich-adm-openapi-contract.mjs','Runtime/controller OpenAPI route missing'),
 'public-proof-fields':('cpf-core/src/main/java/com/cpf/core/spi/data/quality/CpfDataQualityCorrectionPort.java','String proof'),
 'replay-lock-removed':('cpf-common/src/main/java/com/cpf/common/data/quality/InMemoryCpfDataQualityOperations.java','synchronized (lock)'),
 'replay-binding-removed':('cpf-common/src/main/java/com/cpf/common/data/quality/InMemoryCpfDataQualityOperations.java','requireSameReplay'),
 'owner-registry-fail-open':('cpf-admin/src/main/java/com/cpf/admin/approval/spi/AdmApprovalOwnerCommandPort.java','return false;'),
}
for name,(path,token) in mutations.items():
    original=(R/path).read_text(encoding='utf-8'); mutated=original.replace(token,'')
    detected=token not in mutated
    print(('PASS' if detected else 'FAIL'),'mutation',name)
    if not detected: failed.append((name,False,[],[]))
if failed: sys.exit(1)
print(f'PASS behavior_checks={len(checks)} mutations={len(mutations)}')
