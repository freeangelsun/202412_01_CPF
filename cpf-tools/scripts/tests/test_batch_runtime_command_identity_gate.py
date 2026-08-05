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
    source_root = Path(__file__).resolve().parents[3]
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
    assert result["verifiedSources"] == 5


def test_case_insensitive_persisted_lookup_removal_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.IDENTITY
    path.write_text(path.read_text().replace("equalsIgnoreCase(key)", "equals(key)", 1))
    assert_fails(root, "equalsIgnoreCase")


def test_payload_comparison_removal_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.IDENTITY
    path.write_text(path.read_text().replace(
        'compareString(persisted, "target_snapshot", command.targetSnapshot(), false);',
        '// target snapshot comparison removed',
        1,
    ))
    assert_fails(root, "target_snapshot")


def test_assertion_after_execution_begin_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.EXECUTOR
    text = path.read_text()
    assertion = "        RuntimeCommandIdentity.assertMatches(normalized, persisted);\n"
    text = text.replace(assertion, "", 1)
    marker = "        Aggregate aggregate = new Aggregate();\n"
    text = text.replace(marker, assertion + marker, 1)
    path.write_text(text)
    assert_fails(root, "must precede")


def test_unpersisted_parameter_rejection_removal_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.IDENTITY
    path.write_text(path.read_text().replace(
        "command.parameters() != null && !command.parameters().isEmpty()",
        "false",
        1,
    ))
    assert_fails(root, "parameters")


def test_direct_lowercase_map_lookup_reintroduction_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.EXECUTOR
    path.write_text(path.read_text().replace(
        "RuntimeCommandIdentity.assertMatches(normalized, persisted);",
        'RuntimeCommandIdentity.assertMatches(normalized, persisted);\n        persisted.get("command_id");',
        1,
    ))
    assert_fails(root, "forbidden")


def test_transactional_duplicate_replay_reintroduction_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.REPOSITORY
    path.write_text(path.read_text().replace(
        "    public Map<String,Object> create(RuntimeCommand c) {",
        "    @Transactional\n    public Map<String,Object> create(RuntimeCommand c) {",
        1,
    ))
    assert_fails(root, "transaction")


def test_primary_key_conflict_classification_removal_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.REPOSITORY
    path.write_text(path.read_text().replace(
        "new RuntimeCommandIdempotencyConflictException(",
        "new IllegalStateException(",
        1,
    ))
    assert_fails(root, "RuntimeCommandIdempotencyConflictException")


def test_persistence_dispatch_removal_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.EXECUTOR
    path.write_text(path.read_text().replace(
        "persisted = commands.create(normalized);",
        "persisted = Map.of();",
        1,
    ))
    assert_fails(root, "commands.create")


def test_client_execution_state_preservation_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.IDENTITY
    path.write_text(path.read_text().replace(
        "CommandState.APPROVED,",
        "command.executionState(),",
        1,
    ))
    assert_fails(root, "client-owned server state")


def test_admin_expected_version_validation_removal_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.ADMIN
    text = path.read_text()
    command_at = text.index('ResponseEntity<Map<String, Object>> command(')
    token_at = text.index('            requireExpectedVersion(request);', command_at)
    text = text[:token_at] + text[token_at + len('            requireExpectedVersion(request);\n'):]
    path.write_text(text)
    assert_fails(root, "requireExpectedVersion")


def test_admin_expiry_validation_removal_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.ADMIN
    text = path.read_text()
    command_at = text.index('ResponseEntity<Map<String, Object>> command(')
    token_at = text.index('            requireFutureExpiry(request);', command_at)
    text = text[:token_at] + text[token_at + len('            requireFutureExpiry(request);\n'):]
    path.write_text(text)
    assert_fails(root, "requireFutureExpiry")


def test_contract_negative_version_guard_removal_fails(tmp_path: Path) -> None:
    root = fixture(tmp_path)
    path = root / MODULE.CONTRACT
    path.write_text(path.read_text().replace("expectedVersion < 0", "false", 1))
    assert_fails(root, "expectedVersion < 0")
