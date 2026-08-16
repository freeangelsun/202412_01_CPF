from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
SCRIPT = ROOT / "cpf-tools/verification/verify_business_framework_crosscut.py"


def test_crosscut_gate_is_fail_closed_and_batch_is_not_generated_domain_runtime():
    text = SCRIPT.read_text(encoding="utf-8")
    assert 'sys.exit(0 if overall == "PASS" else 1)' in text
    assert 'for runtime in ["online", "batch"]' not in text
    assert 'GEN-BATCH-CAPABILITY-SEPARATE' in text
    assert 'NO-GENERATED-BATCH' in text
