from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[4]
SCRIPT = ROOT / "cpf-tools/runtime/tools/smoke-bat-two-worker-runtime.ps1"
DB_BACKED_BATCH_EXECUTABLES = (
    ROOT / "cpf-batch/control-plane/build.gradle",
    ROOT / "cpf-batch/scheduler/build.gradle",
    ROOT / "cpf-batch/worker/build.gradle",
    ROOT / "cpf-batch/center-cut/build.gradle",
)
WORKER_APPLICATION = ROOT / "cpf-batch/worker/src/main/resources/application.yml"
WORKER_PROPERTIES = ROOT / "cpf-batch/worker/src/main/java/com/cpf/batch/worker/WorkerOperationalProperties.java"


def test_two_worker_runtime_supports_docker_mariadb_client_without_secret_in_argv():
    text = SCRIPT.read_text(encoding="utf-8")
    assert "ValidateSet('Auto','Host','Docker')" in text
    assert "MariaDbContainer = 'cpf-mariadb'" in text
    assert "MARIADB_ROOT_PASSWORD" in text
    assert "docker" in text.lower()
    assert "--password=" not in text
    assert "CPF_DB_ROOT_PASSWORD or CPF_ADMIN_PASSWORD is required" in text
    assert "DatabaseName" in text
    assert "DatabaseName is not a safe MariaDB identifier" in text
    assert "jdbc:mariadb://${HostName}:${Port}/${DatabaseName}" in text
    assert "RuntimeUsername" in text
    assert "RuntimePassword" in text
    assert "Batch Worker runtime database credentials are required" in text
    assert "DbVendor=mariadb" in text
    assert "cpf-tools/db/vendor/mariadb" in text
    assert "Canonical MariaDB Vendor Pack is missing" in text
    assert "CPF_DOMAIN_PERSISTENCE_PROVIDER" in text
    assert "CPF_DB_VENDOR" in text
    assert "CPF_DB_RESOURCE_ROOT" in text
    assert "-Xmx512m" in text
    assert "-XX:HeapDumpPath=" in text
    assert "-XX:ErrorFile=" in text
    assert "ReadToEndAsync()" in text
    assert "worker.stdout.log" in text
    assert "worker.stderr.log" in text
    assert "exited before registration ExitCode=" in text
    assert "Assert-WorkersRunning" in text


def test_two_worker_runtime_uses_canonical_case_sensitive_batch_tables():
    text = SCRIPT.read_text(encoding="utf-8")
    for table in ("BAT_WORKER", "BAT_JOB", "BAT_EXECUTION", "BAT_EXECUTION_LEASE"):
        assert table in text
    assert not re.search(r"(?i)\b(?:from|into|update|delete\s+from|join)\s+(?:\$\{DatabaseName\}\.)?bat_(?:worker|job|execution|execution_lease)\b", text.replace("BAT_", "CANONICAL_"))


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


def test_two_worker_runtime_still_proves_crash_unknown_and_no_blind_retry():
    text = SCRIPT.read_text(encoding="utf-8")
    assert "UNKNOWN_RESULT" in text
    assert "UNKNOWN_RESULT was blindly retried" in text
    assert "blindRetryCount=0" in text
    assert "distinctWorkers=2" in text


def test_two_worker_runtime_launches_real_control_plane_and_registers_job_pack():
    text = SCRIPT.read_text(encoding="utf-8")
    assert "Start-ControlPlane" in text
    assert "cpf-batch/control-plane/build/libs" in text
    assert "/actuator/health/liveness" in text
    assert "BAT_JOB_PACK" in text
    assert "BAT_JOB_PACK_JOB" in text
    assert "jobPackCount" in text
    assert "127.0.0.1:65534" not in text
    assert "$psi.Environment['CPF_BATCH_CONTROL_BASE_URL']=\"http://127.0.0.1:$ControlPlanePort\"" in text
