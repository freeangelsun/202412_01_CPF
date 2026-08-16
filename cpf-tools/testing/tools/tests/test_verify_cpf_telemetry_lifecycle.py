from __future__ import annotations

import importlib.util
import json
import shutil
import tempfile
from pathlib import Path

import pytest

REPO = Path(__file__).resolve().parents[4]
SCRIPT = REPO / "cpf-tools/verification/tools/verify-cpf-telemetry-lifecycle.py"
spec = importlib.util.spec_from_file_location("telemetry_lifecycle", SCRIPT)
module = importlib.util.module_from_spec(spec)
assert spec.loader is not None
spec.loader.exec_module(module)

FIXTURE_FILES = (
    "cpf-tools/runtime/profiles/cpf-telemetry-profile.json",
    "cpf-starters/platform-operations/observability/otlp/src/main/java/com/cpf/platform/operations/observability/otlp/CpfOtlpProperties.java",
    "cpf-starters/platform-operations/observability/otlp/src/main/java/com/cpf/platform/operations/observability/otlp/CpfOtlpAutoConfiguration.java",
    "cpf-starters/platform-operations/observability/src/main/java/com/cpf/starter/platform/operations/observability/CpfObservationSupport.java",
    "cpf-starters/platform-operations/observability/src/main/java/com/cpf/platform/operations/observability/api/logging/CpfStructuredLogger.java",
    "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmObservabilityService.java",
)


def fixture() -> Path:
    root = Path(tempfile.mkdtemp())
    for rel in FIXTURE_FILES:
        src = REPO / rel
        dst = root / rel
        dst.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(src, dst)
    return root


def test_valid():
    module.verify(fixture())


def test_missing_sensitive_pattern_fails():
    root = fixture()
    path = root / "cpf-tools/runtime/profiles/cpf-telemetry-profile.json"
    data = json.loads(path.read_text(encoding="utf-8"))
    data["sensitiveAttributeDenyPatterns"] = [
        value for value in data["sensitiveAttributeDenyPatterns"] if "token" not in value.lower()
    ]
    path.write_text(json.dumps(data), encoding="utf-8")
    with pytest.raises(module.ContractError):
        module.verify(root)


def test_unbounded_queue_fails():
    root = fixture()
    path = root / "cpf-starters/platform-operations/observability/otlp/src/main/java/com/cpf/platform/operations/observability/otlp/CpfOtlpAutoConfiguration.java"
    text = path.read_text(encoding="utf-8")
    # Removing the bounded processor contract must make the verifier fail.
    mutated = text.replace("BatchSpanProcessor.builder(", "BatchSpanProcessor.unbounded(", 1)
    assert mutated != text
    path.write_text(mutated, encoding="utf-8")
    with pytest.raises(module.ContractError):
        module.verify(root)


def test_schema_drift_fails():
    root = fixture()
    path = root / "cpf-tools/runtime/profiles/cpf-telemetry-profile.json"
    data = json.loads(path.read_text(encoding="utf-8"))
    data["schemaUrl"] = "https://opentelemetry.io/schemas/1.29.0"
    path.write_text(json.dumps(data), encoding="utf-8")
    with pytest.raises(module.ContractError):
        module.verify(root)


def test_missing_span_id_consumer_contract_fails():
    root = fixture()
    path = root / "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmObservabilityService.java"
    text = path.read_text(encoding="utf-8")
    mutated = text.replace("SPAN_ID", "SPAN_KEY")
    assert mutated != text
    path.write_text(mutated, encoding="utf-8")
    with pytest.raises(module.ContractError):
        module.verify(root)


def test_missing_trace_lookup_consumer_contract_fails():
    root = fixture()
    path = root / "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmObservabilityService.java"
    text = path.read_text(encoding="utf-8")
    mutated = text.replace("traceByTraceId(", "traceByTraceIdRemoved(", 1)
    assert mutated != text
    path.write_text(mutated, encoding="utf-8")
    with pytest.raises(module.ContractError):
        module.verify(root)
