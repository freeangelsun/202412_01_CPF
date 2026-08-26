from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
SCRIPT=ROOT/'runtime/tools/smoke-bat-two-worker-runtime.ps1'

def text(): return SCRIPT.read_text(encoding='utf-8-sig')

def test_two_worker_runtime_is_kafka_free_and_preserves_five_batch_roles():
    t=text()
    forbidden=['KafkaContainer','kafka-topics.sh','spring.kafka','cpf.batch.remote','BAT_REMOTE_MESSAGE_LEDGER WHERE','remote-diagnostic']
    # information_schema negative assertion may name retired table exactly; it is intentionally allowed.
    for token in forbidden:
        assert token not in t
    for role in ['control-plane','scheduler','worker-1','center-cut','agent','worker-2','member']:
        assert role in t
    assert "kafkaUsed=$false" in t

def test_runtime_uses_db_claim_lease_fencing_and_center_cut_domain_invocation():
    t=text()
    required=['BAT_CENTER_CUT_EXECUTION','BAT_CENTER_CUT_ITEM','BAT_CENTER_CUT_CLAIM','fencing_token',
              'CPF_BAT_CENTER_CUT_JOB','systemCode=\'MBR\'','operationId=\'ping\'',
              '/api/v1/batch/center-cut/executions','OPS_SERVICE_INSTANCE']
    for token in required: assert token in t

def test_runtime_proves_multi_instance_drain_resume_kill_unknown_and_reconcile():
    t=text()
    for token in ['/internal/v1/worker/drain','/internal/v1/worker/resume','Stop-Process',
                  'UNKNOWN_RESULT','blindly retried','reconcile-unknown','fencing takeover','BAT_RUNTIME_INSTANCE']:
        assert token in t

def test_runtime_fails_closed_on_prerequisites_and_untriggered_unknown_is_not_faked():
    t=text()
    assert 'Java 25 is required' in t
    assert 'MariaDB container is not running' in t
    assert 'NOT_TRIGGERED' in t
    assert 'harness did not fabricate one' in t
    assert "exit 1" in t
