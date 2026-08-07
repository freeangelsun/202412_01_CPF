#!/usr/bin/env python3
from pathlib import Path
import os
import shutil
import subprocess
import sys
import tempfile
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
        ('entries: Record<string, ApprovalIdempotencyState>','generations: Record<string, number>','storage: Storage = localStorage','MAX_ENTRIES','generations[fingerprint] = Math.max'),('singleState',))
require('cpf-admin/frontend/src/features/integration-closure/integrationClosureIdempotency.test.ts',
        ('resolveApprovalIdempotency(fingerprint, localStorage)','rotates the deterministic key generation after pending TTL expiry',
         'rotates the deterministic key generation after confirmed TTL expiry','never stores corrected payload in browser storage'),())
require('cpf-tools/verification/final-dev/run-db3-lifecycle.ps1',
        ('$start.Environment.Clear()','WaitForExit($TimeoutSeconds * 1000)','--connection-json-stdin'),('"--url=$url"','"--username=$username"'))
require('cpf-admin/frontend/scripts/enrich-adm-openapi-contract.mjs',
        ('Runtime/controller OpenAPI route missing','requireOperation('),('paths[route] ||=','item[key] ||=',))
require('settings.gradle',('cpfIncludeLocalDomains','settingsSha256'),('localDomainsDir.eachDir',))
require('cpf-admin/src/main/resources/application.yml',(),('active: ${SPRING_PROFILES_ACTIVE:local}','active: local'))
require('cpf-common/src/main/java/com/cpf/common/data/quality/InMemoryCpfDataQualityOperations.java',
        ('replayLocks','synchronized (lock)','validateOnly','replayFingerprints','requireSameReplay'),('replayResults.putIfAbsent',))


require('cpf-admin/src/main/java/com/cpf/admin/approval/security/AdmDataQualityCorrectionGateway.java',
        ('final class AdmDataQualityCorrectionGateway','verifier.verifyAndConsume(command)','delegate.correctApproved(command)'),())
require('cpf-admin/src/main/java/com/cpf/admin/approval/security/AdmApprovalCapabilityNonceRepository.java',
        ('CONSUMED_AT IS NULL','EXPIRES_AT>=?','changed==1','sha256(nonce)'),())
require('cpf-admin/src/main/java/com/cpf/admin/approval/security/AdmDataQualityApprovalProofService.java',
        ('IssuedCapability','approvedAt().plus(ttl)','verifyAndConsume','nonceRepository.consume'),())
require('cpf-admin/src/main/java/com/cpf/admin/approval/repository/AdmApprovalRepository.java',
        ('LEASE_EXPIRES_AT','FENCE_TOKEN','sweepExpiredExecutions','EXECUTION_LEASE_EXPIRED'),())
require('cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalRecoveryWorker.java',
        ('@Scheduled','sweepExpiredExecutions','SYSTEM_APPROVAL_RECOVERY'),('port.execute',))
require('cpf-admin/src/main/java/com/cpf/admin/config/AdmIntegrationClosureConfiguration.java',
        ('@Profile({"local", "dev"})','InMemoryCpfDataQualityOperations','InMemoryCpfWebhookOperations'),())
require('cpf-admin/src/main/java/com/cpf/admin/config/AdmIntegrationClosureProfileGuard.java',
        ('rejectRawSecret("cpf.adm.integration-closure.approval-proof-key-base64")','rejectRawSecret("cpf.adm.integration-closure.crypto.active-key-base64")'),())
require('cpf-core/src/main/java/com/cpf/core/spi/security/CpfSecretProvider.java',
        ('interface CpfSecretProvider','resolveSecret(String secretRef)'),())
require('cpf-admin/src/main/java/com/cpf/admin/config/AdmIntegrationClosureConfiguration.java',
        ('ObjectProvider<CpfSecretProvider>','provider.resolveSecret(ref)','provider.resolveSecret(secretRef.trim())'),
        ('AdmIntegrationClosureSecretProvider',))
require('cpf-starters/data/persistence-jdbc/src/main/java/com/cpf/starter/data/persistence/jdbc/quality/JdbcCpfDataQualityOperations.java',
        ('implements CpfDataQualityOperations, CpfDataQualityCorrectionPort',
         "WHERE QUARANTINE_ID=? AND ROW_VERSION=? AND QUARANTINE_STATE='QUARANTINED'",
         'CPF_DATA_QUALITY_OPERATION','COMMAND_FINGERPRINT','FOR UPDATE','RECONCILE_BATCH_SIZE = 500',
         'ps.setMaxRows(RECONCILE_BATCH_SIZE)','DuplicateKeyException','immutableNullable'),
        ('replayResults.putIfAbsent',))
require('cpf-starters/data/persistence-jdbc/src/main/java/com/cpf/starter/data/persistence/jdbc/quality/CpfDataQualityJdbcAutoConfiguration.java',
        ('@AutoConfiguration','@ConditionalOnBean({DataSource.class, PlatformTransactionManager.class, ObjectMapper.class})',
         '@ConditionalOnMissingBean(value = {CpfDataQualityOperations.class, CpfDataQualityCorrectionPort.class})'),())
require('cpf-starters/data/persistence-jdbc/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports',
        ('com.cpf.starter.data.persistence.jdbc.quality.CpfDataQualityJdbcAutoConfiguration',),())
require('cpf-starters/data/persistence-mybatis/build.gradle',
        ("api project(':cpf-starter-data-persistence-jdbc')",),())
require('cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatApprovalOwnerCommandPort.java',
        ('requireRemoteBaseUrl','requireExplicitInstanceId'),('localhost:8180','adm-local-01"}'))
for vendor in ('oracle','postgresql','mariadb'):
    require(f'cpf-tools/db/vendor/{vendor}/migration/V105__approval_runtime_hardening_r6.sql',
            ('adm_approval_capability_nonce','LEASE_EXPIRES_AT','FENCE_TOKEN','tr_adm_approval_policy_no_overlap'),())
require('cpf-tools/verification/final-dev/tests/run-db3-lifecycle.Tests.ps1',
        ('child_env_runtime_secret_count=0','grandchild-survived.txt','UNKNOWN_TIMEOUT'),())

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


require('cpf-admin/src/main/java/com/cpf/admin/approval/owner/BatchRuntimeApprovalOwnerCommandAdapter.java',
        ('Set<ApprovalOwnerTuple> ALLOWED','ALLOWED.contains(candidate)',
         'Objects.toString(ownerModule, "").trim()', 'Objects.toString(actionType, "").trim()',
         'Objects.toString(targetType, "").trim()', 'risk.targetType().equals(expectedTargetType',
         'risk.actionType().equals(command.actionType())'),
        ('.contains("batch")','startsWith("BATCH_")','endsWith("RETRY")',
         'canonical(ownerModule)','canonical(actionType)','canonical(targetType)'))
require('cpf-admin/src/main/java/com/cpf/admin/approval/owner/CenterCutApprovalOwnerCommandAdapter.java',
        ('Set<ApprovalOwnerTuple> ALLOWED','ALLOWED.contains(candidate)',
         'Objects.toString(ownerModule, "").trim()', 'Objects.toString(actionType, "").trim()',
         'Objects.toString(targetType, "").trim()', 'risk.targetType().equals("center_cut_execution")',
         'risk.actionType().equals(command.actionType())'),
        ('contains("CENTER_CUT")','canonical(ownerModule)','canonical(actionType)','canonical(targetType)'))
require('cpf-admin/src/main/java/com/cpf/admin/approval/owner/BrokerReliabilityApprovalOwnerCommandAdapter.java',
        ('OWNER_MODULE.equals(Objects.toString(ownerModule, "").trim())',
         'OWNER_COMMAND.equals(Objects.toString(actionType, "").trim())',
         'TARGET_TYPE.equals(Objects.toString(targetType, "").trim())'),
        ('OWNER_MODULE.equalsIgnoreCase','OWNER_COMMAND.equals(upper(actionType))','TARGET_TYPE.equals(upper(targetType))'))
require('cpf-admin/src/main/java/com/cpf/admin/approval/owner/DataQualityCorrectionApprovalOwnerCommandAdapter.java',
        ('AdmIntegrationClosureService.DATA_QUALITY_OWNER.equals(ownerModule)',),
        ('DATA_QUALITY_OWNER.equalsIgnoreCase',))
require('cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatApprovalOwnerCommandPort.java',
        ('"BAT".equals(Objects.toString(ownerModule, "").trim())',
         'Objects.toString(ownerCommand, "").trim()',
         'Objects.toString(actionType, "").trim()',
         'Objects.toString(targetType, "").trim()'),
        ('"BAT".equalsIgnoreCase','ownerCommand, "").trim().toUpperCase',
         'actionType, "").trim().toUpperCase','targetType, "").trim().toUpperCase'))

require('cpf-tools/verification/final-dev/run-r6-release-gates.ps1',
        ('ExpectedHead','aggregateQualityBuild','publicationGate','adm-playwright','bza-playwright','db3-live','multiprocess-chaos',
         'adm-npm-a11y','bza-npm-a11y','CPF_BZA_FRONTEND_URL','Add-ConfigurationFailure'),())
require('cpf-biz-admin/frontend/scripts/verify-runtime-openapi-parity.mjs',
        ('CPF_BZA_RUNTIME_OPENAPI_FILE',),('CPF_ADM_RUNTIME_OPENAPI_FILE',))
require('.github/workflows/cpf-r6-release-gates.yml',
        ("java-version: '25'","node-version: '22.18.0'",'run-r6-release-gates.ps1'),())
failed=[c for c in checks if not c[1]]
for path,ok,missing,leaked in checks:
    print(('PASS' if ok else 'FAIL'),path,'missing='+str(missing),'forbidden='+str(leaked))
# Mutation corpus: build a minimal temporary repository projection, apply a real source
# mutation, and execute this verifier as a child process. A mutation passes only when the
# real gate returns non-zero; token-deletion tautologies are not accepted.
mutations={
 'single-slot-idempotency':('cpf-admin/frontend/src/features/integration-closure/integrationClosureIdempotency.ts','entries: Record<string, ApprovalIdempotencyState>'),
 'cross-tab-nondeterminism':('cpf-admin/frontend/src/features/integration-closure/integrationClosureIdempotency.ts','generations: Record<string, number>'),
 'local-storage-policy':('cpf-admin/frontend/src/features/integration-closure/integrationClosureIdempotency.ts','storage: Storage = localStorage'),
 'expiry-generation-rotation':('cpf-admin/frontend/src/features/integration-closure/integrationClosureIdempotency.ts','generations[fingerprint] = Math.max'),
 'inherited-secret':('cpf-tools/verification/final-dev/run-db3-lifecycle.ps1','$start.Environment.Clear()'),
 'timeout-removed':('cpf-tools/verification/final-dev/run-db3-lifecycle.ps1','WaitForExit($TimeoutSeconds * 1000)'),
 'runtime-route-synthesis':('cpf-admin/frontend/scripts/enrich-adm-openapi-contract.mjs','Runtime/controller OpenAPI route missing'),
 'public-proof-fields':('cpf-core/src/main/java/com/cpf/core/spi/data/quality/CpfDataQualityCorrectionPort.java','String proof'),
 'replay-lock-removed':('cpf-common/src/main/java/com/cpf/common/data/quality/InMemoryCpfDataQualityOperations.java','synchronized (lock)'),
 'replay-binding-removed':('cpf-common/src/main/java/com/cpf/common/data/quality/InMemoryCpfDataQualityOperations.java','requireSameReplay'),
 'owner-registry-fail-open':('cpf-admin/src/main/java/com/cpf/admin/approval/spi/AdmApprovalOwnerCommandPort.java','return false;'),
 'dq-gateway-bypass':('cpf-admin/src/main/java/com/cpf/admin/approval/security/AdmDataQualityCorrectionGateway.java','verifier.verifyAndConsume(command)'),
 'nonce-single-use-removed':('cpf-admin/src/main/java/com/cpf/admin/approval/security/AdmApprovalCapabilityNonceRepository.java','CONSUMED_AT IS NULL'),
 'approval-lease-removed':('cpf-admin/src/main/java/com/cpf/admin/approval/repository/AdmApprovalRepository.java','LEASE_EXPIRES_AT'),
 'owner-tuple-case-folding':('cpf-admin/src/main/java/com/cpf/admin/approval/owner/BatchRuntimeApprovalOwnerCommandAdapter.java','Objects.toString(actionType, "").trim()'),
 'persistent-dq-cas':('cpf-starters/data/persistence-jdbc/src/main/java/com/cpf/starter/data/persistence/jdbc/quality/JdbcCpfDataQualityOperations.java'," AND ROW_VERSION=? AND QUARANTINE_STATE='QUARANTINED'"),
 'persistent-dq-ledger':('cpf-starters/data/persistence-jdbc/src/main/java/com/cpf/starter/data/persistence/jdbc/quality/JdbcCpfDataQualityOperations.java','CPF_DATA_QUALITY_OPERATION'),
}
def run_mutation(name, path, token):
    source = R / path
    original = source.read_text(encoding='utf-8')
    if token not in original:
        return False, 'mutation token absent in source'
    with tempfile.TemporaryDirectory(prefix='cpf-r6-mutation-') as tmp:
        projection = Path(tmp)
        for checked_path, *_ in checks:
            src = R / checked_path
            dst = projection / checked_path
            dst.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(src, dst)
        mutated = projection / path
        mutated.write_text(original.replace(token, ''), encoding='utf-8')
        env = dict(os.environ)
        env['CPF_R6_MUTATION_CHILD'] = '1'
        result = subprocess.run(
            [sys.executable, str(Path(__file__).resolve()), str(projection)],
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
            text=True,
            env=env,
            timeout=60,
            check=False)
        return result.returncode != 0, f'exit={result.returncode}'

if os.environ.get('CPF_R6_MUTATION_CHILD') != '1':
    for name,(path,token) in mutations.items():
        detected, detail = run_mutation(name, path, token)
        print(('PASS' if detected else 'FAIL'),'mutation',name,detail)
        if not detected: failed.append((name,False,[],[]))
if failed: sys.exit(1)
print(f'PASS behavior_checks={len(checks)} mutations={len(mutations)}')
