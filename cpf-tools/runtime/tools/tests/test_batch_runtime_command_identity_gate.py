from __future__ import annotations

import importlib.util
from pathlib import Path
import shutil

SCRIPT = Path(__file__).resolve().parents[1] / "verify-cpf-batch-runtime-command-identity.py"
SPEC = importlib.util.spec_from_file_location("batch_runtime_identity_gate", SCRIPT)
assert SPEC and SPEC.loader
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


def fixture(tmp_path: Path) -> Path:
    source_root = Path(__file__).resolve().parents[4]
    for rel in MODULE.REQUIRED:
        source = source_root / rel
        target = tmp_path / rel
        target.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(source, target)
    return tmp_path


def assert_fails(root: Path, text: str) -> None:
    try:
        MODULE.verify(root)
    except ValueError as exc:
        assert text.lower() in str(exc).lower()
    else:
        raise AssertionError("fail-open runtime-command identity fixture passed")


def test_current_runtime_command_identity_contract_passes(tmp_path: Path) -> None:
    result = MODULE.verify(fixture(tmp_path))
    assert result["status"] == "PASS"
    assert result["verifiedSources"] == len(MODULE.REQUIRED)


def test_case_insensitive_persisted_lookup_removal_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.IDENTITY
    path.write_text(path.read_text(encoding="utf-8").replace("equalsIgnoreCase(key)", "equals(key)", 1), encoding="utf-8")
    assert_fails(root, "equalsIgnoreCase")


def test_payload_comparison_removal_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.IDENTITY
    path.write_text(path.read_text(encoding="utf-8").replace(
        'compareString(persisted, "target_snapshot", command.targetSnapshot(), false);',
        '// target snapshot comparison removed',
        1,
    ), encoding="utf-8")
    assert_fails(root, "target_snapshot")


def test_assertion_after_execution_begin_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.EXECUTOR
    text = path.read_text(encoding="utf-8")
    assertion = "        RuntimeCommandIdentity.assertMatches(normalized, persisted);\n"
    text = text.replace(assertion, "", 1)
    marker = "        Aggregate aggregate = new Aggregate();\n"
    text = text.replace(marker, assertion + marker, 1)
    path.write_text(text, encoding="utf-8")
    assert_fails(root, "must precede")


def test_unpersisted_parameter_rejection_removal_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.IDENTITY
    path.write_text(path.read_text(encoding="utf-8").replace(
        "command.parameters() != null && !command.parameters().isEmpty()",
        "false",
        1,
    ), encoding="utf-8")
    assert_fails(root, "parameters")


def test_direct_lowercase_map_lookup_reintroduction_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.EXECUTOR
    path.write_text(path.read_text(encoding="utf-8").replace(
        "RuntimeCommandIdentity.assertMatches(normalized, persisted);",
        'RuntimeCommandIdentity.assertMatches(normalized, persisted);\n        persisted.get("command_id");',
        1,
    ), encoding="utf-8")
    assert_fails(root, "forbidden")


def test_transactional_duplicate_replay_reintroduction_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.REPOSITORY
    path.write_text(path.read_text(encoding="utf-8").replace(
        "    public Map<String,Object> create(RuntimeCommand c) {",
        "    @Transactional\n    public Map<String,Object> create(RuntimeCommand c) {",
        1,
    ), encoding="utf-8")
    assert_fails(root, "transaction")


def test_primary_key_conflict_classification_removal_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.REPOSITORY
    path.write_text(path.read_text(encoding="utf-8").replace(
        "new RuntimeCommandIdempotencyConflictException(",
        "new IllegalStateException(",
        1,
    ))
    assert_fails(root, "RuntimeCommandIdempotencyConflictException")


def test_persistence_dispatch_removal_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.EXECUTOR
    path.write_text(path.read_text(encoding="utf-8").replace(
        "persisted = commands.create(normalized);",
        "persisted = Map.of();",
        1,
    ), encoding="utf-8")
    assert_fails(root, "commands.create")


def test_client_execution_state_preservation_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.IDENTITY
    path.write_text(path.read_text(encoding="utf-8").replace(
        "CommandState.APPROVED,",
        "command.executionState(),",
        1,
    ), encoding="utf-8")
    assert_fails(root, "client-owned server state")


def test_browser_request_target_field_reintroduction_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.REQUEST
    path.write_text(path.read_text(encoding="utf-8") + "\npublic String targetType;\n", encoding="utf-8")
    assert_fails(root, "targetType")


def test_browser_command_direct_client_dispatch_reintroduction_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.ADMIN
    text = path.read_text(encoding="utf-8")
    marker = "            return ResponseEntity.accepted().body(approvalService.execute(approvalRequestId, body.reason, operatorId));"
    path.write_text(text.replace(marker, "            client.command(body);\n" + marker, 1), encoding="utf-8")
    assert_fails(root, "client.command")


def test_contract_negative_version_guard_removal_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.CONTRACT
    path.write_text(path.read_text(encoding="utf-8").replace("expectedVersion < 0", "false", 1), encoding="utf-8")
    assert_fails(root, "expectedVersion < 0")
