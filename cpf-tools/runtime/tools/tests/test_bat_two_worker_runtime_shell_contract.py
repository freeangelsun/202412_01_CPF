from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
SCRIPT=ROOT/'runtime/tools/smoke-bat-two-worker-runtime.ps1'
MARIADB_SCHEMA=ROOT/'db/vendor/mariadb/source/10_cpf_schema.sql'

def text(): return SCRIPT.read_text(encoding='utf-8-sig')

def test_two_worker_runtime_is_kafka_free_and_preserves_five_batch_roles():
    t=text()
    forbidden=['KafkaContainer','kafka-topics.sh','spring.kafka','cpf.batch.remote','BAT_REMOTE_MESSAGE_LEDGER WHERE','remote-diagnostic']
    # information_schema negative assertion may name retired table exactly; it is intentionally allowed.
    for token in forbidden:
        assert token not in t
    for role in ['control-plane','scheduler','worker-1','center-cut','agent','worker-2','domain']:
        assert role in t
    assert "kafkaUsed=$false" in t

def test_runtime_uses_db_claim_lease_fencing_and_center_cut_domain_invocation():
    t=text()
    required=['BAT_CENTER_CUT_EXECUTION','BAT_CENTER_CUT_ITEM','BAT_CENTER_CUT_CLAIM','fencing_token',
              'CPF_BAT_CENTER_CUT_JOB','systemCode=[string]$targetDomain.systemCode','operationId=\'ping\'',
              '/api/v1/batch/center-cut/executions','OPS_SERVICE_ENDPOINT']
    for token in required: assert token in t

def test_runtime_proves_multi_instance_drain_resume_kill_unknown_and_reconcile():
    t=text()
    for token in ['/internal/v1/worker/drain','/internal/v1/worker/resume','Stop-Process',
                  'UNKNOWN_RESULT','blindly retried','reconcile-unknown','fencing takeover','OPS_RUNTIME_INSTANCE_STATE']:
        assert token in t
    assert 'BAT_RUNTIME_INSTANCE WHERE instance_id LIKE' not in t

def test_runtime_fails_closed_on_prerequisites_and_untriggered_unknown_is_not_faked():
    t=text()
    assert 'Java 25 is required' in t
    assert 'MariaDB container is not running' in t
    assert 'NOT_TRIGGERED' in t
    assert 'harness did not fabricate one' in t
    assert "exit 1" in t


def test_child_jvms_receive_db_and_agent_secrets_only_via_environment():
    """Runtime JVM command line에는 JDBC/MAC secret이 남으면 안 된다."""
    t = text()
    start_role = t.split('function Start-Role', 1)[1].split('function BatHeaders', 1)[0]
    assert 'CPF_PLATFORM_DB_PASSWORD=$DbPassword' in start_role
    assert 'CPF_DATA_PERSISTENCE_JDBC_ROLE_DATASOURCES_CPF_PLATFORM_DB_PASSWORD=$DbPassword' in start_role
    assert '["${domainDatasourcePrefix}_DATASOURCE_PASSWORD"]=$DbPassword' in start_role
    assert 'CPF_AGENT_ARTIFACT_STATE_MAC_KEY_BASE64' in start_role
    assert 'CPF_AGENT_COMMAND_LEDGER_ROOT' in start_role
    assert '-Environment $childEnvironment' in start_role
    assert '"--cpf.data.persistence.jdbc.role-datasources.cpf-platform-db.password=$DbPassword"' not in start_role
    assert '[Security.Cryptography.RandomNumberGenerator]::GetBytes(32)' in t


def test_generated_domain_target_is_discovered_and_registered_before_startup():
    t = text()
    assert 'Get-CpfGeneratedDomainInventory' in t
    assert 'cpf-member/online' not in t
    assert 'MBR_DATASOURCE_' not in t
    assert "Start-Role 'domain'" in t
    assert t.index('Generated Domain service registry pre-registration') < t.index("Start-Role 'domain'")
    assert '$serviceId=[string]$targetDomain.systemCode' in t
    assert '-SERVICE-$runId' not in t
    assert 'service_type,owner_module_code,description,use_yn' in t
    assert 'endpoint_name,endpoint_type,base_url,context_path' in t
    for retired_column in ('service_status', 'protocol_type', 'endpoint_status', 'health_status'):
        assert retired_column not in t
    # Runtime Agent owns the rich service-instance projection. The harness must only seed the
    # service/endpoint precondition and must not guess the evolving OPS_SERVICE_INSTANCE schema.
    assert 'INSERT INTO ${DatabaseName}.OPS_SERVICE_INSTANCE' not in t


def test_domain_registry_precondition_uses_current_canonical_mariadb_columns():
    schema = MARIADB_SCHEMA.read_text(encoding='utf-8-sig')
    for column in ('service_type', 'owner_module_code', 'description', 'use_yn',
                   'endpoint_name', 'endpoint_type', 'base_url', 'context_path'):
        assert column in schema


def test_bat_headers_carry_cpf_standard_transaction_headers():
    """BAT 전용 헤더만 보내면 CPF 공통 Header Filter 가 거부한다.

    실측: worker drain 호출이 EXTERNAL_HEADER_REQUIRED(ECPF900002, header=X-Transaction-Id)로
    HTTP 400 을 받아 Batch two-worker runtime 이 전부 실패했다. 다른 smoke harness 와 동일하게
    CPF 표준 거래 헤더를 함께 실어야 한다.
    """
    t = SCRIPT.read_text(encoding='utf-8-sig')
    for header in ('X-Transaction-Id', 'X-Trace-Id', 'X-Request-Type',
                   'X-User-Id', 'X-Client-Id', 'X-Client-Version', 'X-Caller-Service'):
        assert f"'{header}'" in t, f"BatHeaders must send {header}"


def test_bat_headers_transaction_id_matches_canonical_length():
    """transactionId 는 canonical 34자 계약(yyyyMMddHHmmssfff + system 3 + caller 7 + seq 7)이다."""
    import re
    t = SCRIPT.read_text(encoding='utf-8-sig')
    m = re.search(r'\$transactionId="\{0:yyyyMMddHHmmssfff\}([A-Za-z0-9]+)\{1:(0+)\}"', t)
    assert m, 'canonical transactionId format not found'
    body, sequence = m.group(1), m.group(2)
    assert len(body) + len(sequence) + 17 == 34, (
        f"transactionId length must be 34: 17+{len(body)}+{len(sequence)}")
