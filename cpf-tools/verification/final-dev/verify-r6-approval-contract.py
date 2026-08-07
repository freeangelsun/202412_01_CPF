#!/usr/bin/env python3
from pathlib import Path
import hashlib, json, re, sys
R=Path(sys.argv[1]).resolve() if len(sys.argv)>1 else Path(__file__).resolve().parents[3]
errors=[]
def text(rel):
    p=R/rel
    if not p.is_file(): errors.append(f'missing file: {rel}'); return ''
    return p.read_text(encoding='utf-8',errors='replace')
def require(rel,*tokens):
    body=text(rel)
    for token in tokens:
        if token not in body: errors.append(f'{rel}: missing {token}')

service='cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java'
require(service,'validatePolicyActive','canonicalPolicy','resolveOwnerPort(ownerModule,ownerCommand,actionType,targetType)',
        'sanitizeDetail','AtomicBoolean replay','AdmApprovalConflictException','BREAK_GLASS_','@Size(min=8,max=128)')
body=text(service)
for forbidden in ('return new ApprovedCorrection(', 'COMMAND_PAYLOAD_SNAPSHOT AS payloadSnapshot'):
    if forbidden in body: errors.append(f'{service}: forbidden public/raw pattern {forbidden}')
require('cpf-admin/src/main/java/com/cpf/admin/approval/repository/AdmApprovalRepository.java',
        'INSERT INTO adm_approval_policy_history','insertPolicy','findRequestIdByKey')
require('cpf-admin/src/main/java/com/cpf/admin/approval/controller/AdmApprovalController.java',
        'HttpStatus.CREATED','@Validated','@Size(min=8,max=500)')
require('cpf-admin/src/main/java/com/cpf/admin/approval/controller/AdmApprovalExceptionHandler.java',
        'HttpStatus.CONFLICT','HttpStatus.UNPROCESSABLE_ENTITY')
require('cpf-admin/src/main/java/com/cpf/admin/approval/security/AdmApprovalSnapshotIntegrity.java',
        'STRICT_DUPLICATE_DETECTION','USE_BIG_INTEGER_FOR_INTS','USE_BIG_DECIMAL_FOR_FLOATS')
require('cpf-admin/src/main/java/com/cpf/admin/approval/security/AdmDataQualityApprovalProofService.java',
        'HmacSHA256','MessageDigest.isEqual')
require('cpf-core/src/main/java/com/cpf/core/spi/data/quality/CpfDataQualityCorrectionPort.java',
        'String nonce','String proof','String payloadHash')
public_ops=text('cpf-core/src/main/java/com/cpf/core/api/data/quality/CpfDataQualityOperations.java')
for forbidden in ('boolean approved','CorrectionAuthorization','correctAuthorized'):
    if forbidden in public_ops: errors.append(f'public data-quality API still exposes {forbidden}')
require('cpf-common/src/main/java/com/cpf/common/data/quality/InMemoryCpfDataQualityOperations.java',
        'approvalProofVerifier','ReplayCommand','immutableNullable','validateOnly','replayLocks','synchronized (lock)',
        'replayFingerprints','requireSameReplay')

owner_interface='cpf-admin/src/main/java/com/cpf/admin/approval/spi/AdmApprovalOwnerCommandPort.java'
require(owner_interface,'default boolean supports(String ownerModule, String ownerCommand, String actionType, String targetType)','return false;')
owner_registry=[
 'cpf-admin/src/main/java/com/cpf/admin/approval/owner/BatchJobDefinitionApprovalOwnerCommandAdapter.java',
 'cpf-admin/src/main/java/com/cpf/admin/approval/owner/BatchRuntimeApprovalOwnerCommandAdapter.java',
 'cpf-admin/src/main/java/com/cpf/admin/approval/owner/BrokerReliabilityApprovalOwnerCommandAdapter.java',
 'cpf-admin/src/main/java/com/cpf/admin/approval/owner/CenterCutApprovalOwnerCommandAdapter.java',
 'cpf-admin/src/main/java/com/cpf/admin/approval/owner/DataQualityCorrectionApprovalOwnerCommandAdapter.java',
 'cpf-admin/src/main/java/com/cpf/admin/approval/owner/GatewayApprovalOwnerCommandAdapter.java',
 'cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatApprovalOwnerCommandPort.java']
for adapter in owner_registry:
    require(adapter,'supports(String ownerModule, String ownerCommand, String actionType, String targetType)')

require('cpf-admin/src/main/java/com/cpf/admin/config/AdmIntegrationClosureConfiguration.java',
        '@ConditionalOnMissingBean(value = {CpfDataQualityOperations.class, CpfDataQualityCorrectionPort.class})','CpfDataQualityCorrectionPort correctionPort')
require('cpf-admin/src/main/java/com/cpf/admin/config/AdmIntegrationClosureProfileGuard.java',
        'explicit active profile','forbidden in prod/stg')
require('cpf-admin/src/main/resources/application.yml','application-adm-${spring.profiles.active:}.yml')
app=text('cpf-admin/src/main/resources/application.yml')
if re.search(r'(?m)^\s*active:\s*(?:local|\$\{SPRING_PROFILES_ACTIVE:local\})\s*$',app): errors.append('local profile default remains')
require('cpf-admin/frontend/src/features/integration-closure/integrationClosureIdempotency.ts',
        'entries: Record<string, ApprovalIdempotencyState>','generations: Record<string, number>','storage: Storage = localStorage','MAX_ENTRIES','state: "pending" | "confirmed"')
require('cpf-admin/frontend/src/shared/strictJsonObject.ts','중복 키','MAX_SAFE_INTEGER','정밀도 손실')
require('cpf-admin/frontend/src/shared/operationPermissions.ts','permissions.has(operationId)')
require('cpf-admin/frontend/src/features/approvals/ApprovalsPage.vue','parseStrictJsonObject','requestKey=crypto.randomUUID()','idempotencyKey=crypto.randomUUID()')
require('cpf-admin/frontend/scripts/enrich-adm-openapi-contract.mjs',
        'Runtime/controller OpenAPI route missing','AdmApprovalCreateRequest','Approval request created','minimum: 1')
require('settings.gradle','cpfIncludeLocalDomains','settingsSha256')
require('cpf-admin/build.gradle','inputs.dir(frontendDir.dir(\'scripts\'))','nodeVersionProvider','npmVersionProvider')
require('cpf-tools/verification/final-dev/run-db3-lifecycle.ps1',
        '--connection-json-stdin','$start.Environment.Clear()','WaitForExit($TimeoutSeconds * 1000)','Kill($true)')

require('cpf-tools/verification/final-dev/run-r6-release-gates.ps1',
        'ExpectedHead','aggregateQualityBuild','publicationGate','npm-playwright','db3-live','multiprocess-chaos')
require('.github/workflows/cpf-r6-release-gates.yml',
        "java-version: '25'","node-version: '22.18.0'",'run-r6-release-gates.ps1','actions/upload-artifact@v4')

layouts=[]
for vendor in ('mariadb','postgresql','oracle'):
    base=R/f'cpf-tools/db/vendor/{vendor}'
    expected=[
      'source/19_approval_integrity_r6.sql','install/08_approval_integrity_r6.sql',
      'migration/V104__approval_integrity_r6.sql','rollback/R104__approval_integrity_r6.sql',
      'verify/104_verify_approval_integrity_r6.sql','runtime/adm/approval_integrity_queries.sql','pack.json']
    missing=[rel for rel in expected if not (base/rel).is_file()]
    if missing: errors.append(f'{vendor}: missing DB lifecycle {missing}')
    else:
        pack=json.loads((base/'pack.json').read_text(encoding='utf-8'))
        entry=pack.get('approvalIntegrityR6Pack',{})
        if entry.get('tableCount') != 1 or entry.get('officialVendor') is not True: errors.append(f'{vendor}: pack contract drift')
        ddl=(base/'source/19_approval_integrity_r6.sql').read_text(encoding='utf-8')
        for token in ('adm_approval_policy_history','CHANGE_REASON','AFTER_HASH','OPERATOR_ID'):
            if token not in ddl: errors.append(f'{vendor}: source DDL missing {token}')
        layouts.append(set(expected))
if layouts and any(layout != layouts[0] for layout in layouts[1:]): errors.append('DB3 lifecycle layout mismatch')

restore=R/'cpf-docs/assets/manuals/cpf-document-quality-r9.svg'
if not restore.is_file(): errors.append('protected SVG restoration missing')
elif hashlib.sha256(restore.read_bytes()).hexdigest()!='2979b5f65e7b8ace8a735cd5eae501c6b60cc851be2f31fd441383e7a2d498d5': errors.append('protected SVG restoration hash mismatch')

if errors:
    for error in errors: print('FAIL',error)
    raise SystemExit(1)
print('PASS R6 approval/source/db/config/frontend contract checks')
