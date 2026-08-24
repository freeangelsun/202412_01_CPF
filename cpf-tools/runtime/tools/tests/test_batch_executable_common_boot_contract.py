from pathlib import Path


ROOT = Path(__file__).resolve().parents[4]
EXECUTABLES = {
    "control-plane": "CONTROL_PLANE",
    "scheduler": "SCHEDULER",
    "worker": "WORKER",
    "center-cut": "CENTER_CUT",
    "agent": "AGENT",
}
APPLICATION_MAINS = {
    "control-plane": "com/cpf/batch/control/BatchControlPlaneApplication.java",
    "scheduler": "com/cpf/batch/scheduler/BatchSchedulerApplication.java",
    "worker": "com/cpf/batch/worker/BatchWorkerApplication.java",
    "center-cut": "com/cpf/batch/centercut/runner/CenterCutApplication.java",
    "agent": "com/cpf/batch/agent/BatchAgentApplication.java",
}


def test_all_five_batch_executables_import_the_shared_runtime_contract():
    for module, role in EXECUTABLES.items():
        application = (
            ROOT / "cpf-batch" / module / "src/main/resources/application.yml"
        ).read_text(encoding="utf-8")

        assert application.count("optional:classpath:application-bat-runtime.yml") == 1, module
        assert "module-id: BAT" in application, module
        assert f"role: {role}" in application, module

        main = (
            ROOT / "cpf-batch" / module / "src/main/java" / APPLICATION_MAINS[module]
        ).read_text(encoding="utf-8")
        assert "RuntimeCommonConfiguration.class" in main, module
        assert "BatDataSourceConfiguration.class" in main, module


def test_shared_runtime_contract_externalizes_the_platform_database_and_secret():
    shared = (
        ROOT / "cpf-batch/runtime-support/src/main/resources/application-bat-runtime.yml"
    ).read_text(encoding="utf-8")

    for marker in (
        "CPF_PLATFORM_DB_URL",
        "CPF_PLATFORM_DB_USERNAME",
        "CPF_PLATFORM_DB_PASSWORD",
        "CPF_PLATFORM_DB_DRIVER",
        "CPF_DB_VENDOR",
    ):
        assert marker in shared
    assert "password: ${CPF_PLATFORM_DB_PASSWORD:}" in shared


def test_agent_requires_external_integrity_secret_and_persistent_ledger_path():
    application = (
        ROOT / "cpf-batch/agent/src/main/resources/application.yml"
    ).read_text(encoding="utf-8")
    properties = (
        ROOT
        / "cpf-batch/agent/src/main/java/com/cpf/batch/agent/AgentProperties.java"
    ).read_text(encoding="utf-8")

    assert "artifact-state-mac-key-base64: ${CPF_AGENT_ARTIFACT_STATE_MAC_KEY_BASE64}" in application
    assert "command-ledger-root: ${CPF_AGENT_COMMAND_LEDGER_ROOT}" in application
    assert 'private String commandLedgerRoot = "./data/agent-command-ledger";' not in properties


def test_agent_has_one_canonical_runtime_state_provider():
    provider_root = ROOT / "cpf-batch/agent/src/main/java/com/cpf/batch/agent"
    providers = sorted(path.name for path in provider_root.glob("*RuntimeStateProvider.java"))

    assert providers == ["AgentRuntimeStateProvider.java"]
