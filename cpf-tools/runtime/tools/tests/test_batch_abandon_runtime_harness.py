from pathlib import Path


def test_batch_abandon_harness_excludes_product_duplicate_stubs():
    root = Path(__file__).resolve().parents[4]
    script = root / "cpf-tools/verification/java21/batch-abandon-runtime/run-batch-abandon-harness.py"
    text = script.read_text(encoding="utf-8")
    assert "product_fqcns" in text
    assert "if fqcn not in product_fqcns" in text
    assert "com.cpf.batch.api.BatchControlState" in text
    assert "com.cpf.batch.execution.CpfBatchUnknownResultException" in text
