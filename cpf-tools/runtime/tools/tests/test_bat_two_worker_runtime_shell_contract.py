from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[4]
SCRIPT = ROOT / "cpf-tools/runtime/tools/smoke-bat-two-worker-runtime.ps1"
DB_BACKED_BATCH_EXECUTABLES = (
    ROOT / "cpf-batch/control-plane/build.gradle",
    ROOT / "cpf-batch/scheduler/build.gradle",
    ROOT / "cpf-batch/worker/build.gradle",
    ROOT / "cpf-batch/center-cut/build.gradle",
    ROOT / "cpf-batch/agent/build.gradle",
)
WORKER_APPLICATION = ROOT / "cpf-batch/worker/src/main/resources/application.yml"
WORKER_PROPERTIES = ROOT / "cpf-batch/worker/src/main/java/com/cpf/batch/worker/WorkerOperationalProperties.java"


def test_two_worker_runtime_supports_installed_docker_assets_without_secret_in_argv():
    text = SCRIPT.read_text(encoding="utf-8")
    assert "ValidateSet('Auto','Host','Docker')" in text
    assert "MariaDbContainer = 'cpf-mariadb'" in text
    assert "KafkaContainer = 'cpf-kafka'" in text
    assert "MARIADB_ROOT_PASSWORD" in text
    assert "/opt/kafka/bin/kafka-topics.sh" in text
    assert "--create','--if-not-exists" in text
    assert "--delete','--if-exists" in text
    assert "docker pull" not in text.lower()
    assert "docker build" not in text.lower()
    assert "--password=" not in text
    assert "CPF_DB_ROOT_PASSWORD or CPF_ADMIN_PASSWORD is required" in text
    assert "DatabaseName is not a safe MariaDB identifier" in text
    assert "jdbc:mariadb://${HostName}:${Port}/${DatabaseName}" in text
    assert "RuntimeUsername" in text and "RuntimePassword" in text
    assert "Batch Worker runtime database credentials are required" in text
    assert "DbVendor=mariadb" in text
    assert "cpf-tools/db/vendor/mariadb" in text
    assert "Canonical MariaDB Vendor Pack is missing" in text
    assert "CPF_DOMAIN_PERSISTENCE_PROVIDER" in text
    assert "CPF_DB_VENDOR" in text and "CPF_DB_RESOURCE_ROOT" in text
    assert "[IO.Path]::GetTempPath()" in text
    assert "-Xmx512m" in text
    assert "-XX:HeapDumpPath=" in text and "-XX:ErrorFile=" in text
    assert "ReadToEndAsync()" in text
    assert "exited before verification ExitCode=" in text
    assert "Assert-WorkersRunning" in text


def test_two_worker_runtime_uses_current_manager_kafka_worker_spring_batch_path():
    text = SCRIPT.read_text(encoding="utf-8")
    required = (
        "Start-Runtime 'bat-smoke-center-cut'",
        "cpf-batch/center-cut",
        "cpf.batch.remote.kafka.request-topic",
        "cpf.batch.remote.kafka.reply-topic-prefix",
        "cpf.batch.remote.kafka.consumer-group",
        "cpf.batch.remote.kafka.manager-instance-id",
        "CPF_BAT_DIAGNOSTIC_TOKEN",
        "/internal/v1/center-cut/diagnostic/executions",
        "BAT_REMOTE_MESSAGE_LEDGER",
        "BAT_SB_JOB_INSTANCE",
        "BAT_SB_JOB_EXECUTION",
        "BAT_SB_STEP_EXECUTION",
        "BAT_SB_STEP_EXECUTION_CONTEXT",
        "cpfRemotePartitionWorkerStep",
        "distinctWorkers=2",
    )
    for value in required:
        assert value in text
    for retired in ("BAT_EXECUTION_LEASE", "INSERT INTO BAT_EXECUTION(", "INSERT INTO BAT_JOB("):
        assert retired not in text
    assert not re.search(
        r"(?i)\b(?:from|into|update|delete\s+from|join)\s+(?:\$\{DatabaseName\}\.)?bat_(?:worker|remote_message_ledger|sb_job_instance|sb_job_execution|sb_step_execution|sb_step_execution_context)\b",
        text.replace("BAT_", "CANONICAL_"),
    )


def test_two_worker_runtime_counts_only_canonical_partition_qualified_worker_steps():
    text = SCRIPT.read_text(encoding="utf-8")
    canonical = "^cpfRemotePartitionWorkerStep:partition-[0-9]+$"
    assert canonical in text
    assert text.count("REGEXP '$workerStepNameRegex'") == 2
    assert "STEP_NAME='cpfRemotePartitionWorkerStep'" not in text
    assert "s.STEP_NAME='cpfRemotePartitionWorkerStep'" not in text


def test_every_database_backed_batch_executable_packages_managed_db3_drivers():
    coordinates = (
        "runtimeOnly 'org.mariadb.jdbc:mariadb-java-client'",
        "runtimeOnly 'org.postgresql:postgresql'",
        "runtimeOnly 'com.oracle.database.jdbc:ojdbc11'",
    )
    for build_file in DB_BACKED_BATCH_EXECUTABLES:
        text = build_file.read_text(encoding="utf-8")
        assert text.count("implementation project(':starters:data:jdbc')") == 1, build_file
        for coordinate in coordinates:
            assert text.count(coordinate) == 1, (build_file, coordinate)


def test_worker_empty_operation_catalog_uses_typed_java_defaults_not_yaml_scalars():
    application = WORKER_APPLICATION.read_text(encoding="utf-8")
    properties = WORKER_PROPERTIES.read_text(encoding="utf-8")
    assert "scripts: {}" not in application
    assert "path-aliases: {}" not in application
    assert "private Map<String, ShellDefinition> scripts = new LinkedHashMap<>()" in properties
    assert "private Map<String, PathAlias> pathAliases = new LinkedHashMap<>()" in properties


def test_two_worker_runtime_proves_drain_resume_crash_unknown_and_no_blind_retry():
    text = SCRIPT.read_text(encoding="utf-8")
    assert "/internal/v1/worker/drain" in text
    assert "/internal/v1/worker/resume" in text
    assert "drainingWorkerNewClaimCount=0" in text
    assert "remainingWorkerClaimed=$true" in text
    assert "UNKNOWN_RESULT" in text
    assert "UNKNOWN_RESULT was blindly retried" in text
    assert "blindRetryCount=0" in text
    assert "Stop-Process -Id $processes[$crashProcessName].Id -Force" in text


def test_two_worker_runtime_launches_real_control_plane_and_registers_job_pack():
    text = SCRIPT.read_text(encoding="utf-8")
    assert "Start-Runtime 'bat-smoke-control-plane'" in text
    assert "cpf-batch/control-plane" in text
    assert "/actuator/health/liveness" in text
    assert "BAT_JOB_PACK" in text and "BAT_JOB_PACK_JOB" in text
    assert "jobPackCount" in text
    assert "127.0.0.1:65534" not in text
    assert "$Psi.Environment['CPF_BATCH_CONTROL_BASE_URL'] = \"http://127.0.0.1:$ControlPlanePort\"" in text


def test_batch_executables_use_canonical_spring_batch_repository_prefix():
    for module in ("control-plane", "center-cut", "scheduler", "worker"):
        path = ROOT / "cpf-batch" / module / "src" / "main" / "resources" / "application.properties"
        properties = path.read_text(encoding="utf-8")
        assert "spring.batch.jdbc.initialize-schema=never" in properties
        assert "spring.batch.jdbc.table-prefix=BAT_SB_" in properties
        assert "spring.batch.jdbc.table-prefix=BATCH_" not in properties
