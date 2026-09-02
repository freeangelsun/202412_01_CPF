"""Targeted Runtime work units must preserve, not weaken, full-lifecycle guarantees."""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
SCRIPT = ROOT / "cpf-tools/verification/tools/run-cpf-runtime-work-unit.ps1"


def text() -> str:
    return SCRIPT.read_text(encoding="utf-8")


def test_work_units_are_named_full_lifecycle_segments_with_source_and_evidence_contracts() -> None:
    source = text()
    for token in (
            "BATCH_TWO_WORKER", "GATEWAY_BATCH", "BATCH_AND_GATEWAY",
            "RUNTIME_DB_PREP", "BATCH_TWO_WORKER_CRASH_UNKNOWN", "GATEWAY_BATCH_RUNTIME",
            "RUNTIME_DB_CLEANUP", "work-unit-result.json", "sourceIdentity",
            "SOURCE_IDENTITY_AFTER", "cpf-source-state.py"):
        assert token in source
    assert "Full Runtime 163" in source


def test_work_unit_cleanup_and_source_drift_fail_the_segment() -> None:
    source = text()
    assert "$cleanupOk = Invoke-Unit-Stage 'RUNTIME_DB_CLEANUP'" in source
    assert "if (-not $cleanupOk)" in source
    assert "$allPassed = $false" in source
    assert "$sourceIdentityAfter -ne $sourceIdentity" in source
    assert "sourceIdentityAfter = $sourceIdentityAfter" in source
