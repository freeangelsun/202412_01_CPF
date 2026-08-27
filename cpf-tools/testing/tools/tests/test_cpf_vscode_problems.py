import json, subprocess, sys, tempfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[4]
TOOL=ROOT/'cpf-tools/verification/tools/verify-cpf-vscode-problems.py'

def run(rows):
    with tempfile.TemporaryDirectory() as d:
        p=Path(d)/'p.json'; p.write_text(json.dumps(rows),encoding='utf-8')
        return subprocess.run([sys.executable,str(TOOL),'--input',str(p)],capture_output=True,text=True)

def test_zero_error_warning_passes():
    assert run([{'severity':2,'message':'info'}]).returncode==0

def test_error_and_warning_fail():
    r=run([{'severity':8,'message':'e'},{'severity':4,'message':'w'}]); assert r.returncode==1; assert '"errors": 1' in r.stdout and '"warnings": 1' in r.stdout

def test_20260827_vscode_diagnostic_regressions_are_not_reintroduced():
    forbidden = {
        'cpf-admin/src/main/java/com/cpf/admin/opr/capability/AdmCapabilityManagementController.java': [
            'Comparator.comparing(InstanceView::systemId)',
            'Comparator.comparing(IssueView::systemId)',
            'Comparator.comparing(CapabilityView::starterArtifactId)',
        ],
        'cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmOperationPermissionProjectionService.java': [
            'Comparator.comparing(OperationRoute::operationId)',
            '.thenComparing(OperationRoute::httpMethod)',
            '.thenComparing(OperationRoute::path)',
        ],
        'cpf-backoffice-web/src/main/java/com/cpf/backoffice/web/shared/routing/BackofficeOperationRouteCatalog.java': [
            'Comparator.comparingInt(Route::staticSegments)',
        ],
        'cpf-batch/api/src/main/java/com/cpf/batch/api/BatchCanonicalDigest.java': [
            'import java.util.Comparator;',
        ],
        'cpf-batch/runtime/src/main/java/com/cpf/batch/execution/CpfAnnotatedBatchStepHandler.java': [
            'Comparator.comparingInt(StepDescriptor::order)',
            'Comparator.comparing(JobDescriptor::jobId)',
        ],
        'cpf-starters/base/runtime/src/main/java/com/cpf/starter/runtime/CpfRuntimeCapabilityInventory.java': [
            'Comparator.comparing(URL::toString)',
        ],
        'cpf-starters/integration/fixed-length/src/main/java/com/cpf/integration/fixedlength/api/CpfFixedLengthLayoutRegistry.java': [
            'Comparator.comparing(CpfFixedLengthLayout::layoutId)',
        ],
        'cpf-starters/integration/http/src/main/java/com/cpf/integration/http/internal/domaincall/CpfConfiguredDomainBindingResolver.java': [
            '.map(java.util.Map.Entry::getValue)',
        ],
        'cpf-starters/messaging/jms/src/main/java/com/cpf/messaging/jms/JmsCpfBrokerBridgeAdapter.java': [
            'Comparator.comparing(CpfBrokerBridgeMessage::createdAt)',
        ],
        'cpf-starters/messaging/kafka/src/main/java/com/cpf/messaging/kafka/KafkaCpfBrokerBridgeAdapter.java': [
            'Comparator.comparing(CpfBrokerBridgeMessage::createdAt)',
        ],
        'cpf-starters/messaging/kafka/src/test/java/com/cpf/messaging/kafka/KafkaCpfBrokerClientTest.java': [
            'import static org.mockito.ArgumentMatchers.any;',
        ],
        'cpf-starters/messaging/rabbitmq/src/main/java/com/cpf/messaging/rabbitmq/RabbitCpfBrokerBridgeAdapter.java': [
            'Comparator.comparing(CpfBrokerBridgeMessage::createdAt)',
        ],
        'cpf-starters/messaging/reliability/jdbc/src/main/java/com/cpf/messaging/reliability/api/jdbc/internal/CpfBrokerBridgeAdapter.java': [
            'Comparator.comparing(CpfBrokerBridgeMessage::createdAt)',
        ],
        'cpf-starters/platform-operations/health/src/main/java/com/cpf/platform/operations/health/CpfRuntimeHealthRegistryMemory.java': [
            'Comparator.comparing(CpfRuntimeHealth::systemId)',
        ],
        'cpf-starters/platform-operations/observability/src/main/java/com/cpf/platform/operations/observability/internal/logging/DynamicTransactionLogLevelService.java': [
            'Comparator.comparing(DynamicLogLevelRule::createdAt)',
        ],
        'cpf-starters/platform-operations/observability/src/main/java/com/cpf/starter/platform/operations/observability/LocalCpfRemoteLogArtifactAdapter.java': [
            'Comparator.comparing(CpfRemoteLogArtifact::modifiedAt)',
        ],
        'cpf-starters/security/session/valkey/src/main/java/com/cpf/security/session/valkey/ValkeyCpfSessionOperations.java': [
            'Comparator.comparing(CpfSessionSnapshot::lastAccessedAt)',
        ],
        'cpf-starters/web/src/main/java/com/cpf/web/api/CpfHttpHeaders.java': [
            '@SuppressWarnings({"unchecked", "rawtypes"})',
        ],
    }
    for rel, patterns in forbidden.items():
        text=(ROOT/rel).read_text(encoding='utf-8')
        for pattern in patterns:
            assert pattern not in text, f'VSCode regression reintroduced: {rel}: {pattern}'
    record=(ROOT/'cpf-common/src/main/java/com/cpf/common/template/CmnTemplateRecord.java').read_text(encoding='utf-8')
    assert 'public enum Status { @Deprecated DRAFT, @Deprecated APPROVED, @Deprecated RETIRED }' in record
