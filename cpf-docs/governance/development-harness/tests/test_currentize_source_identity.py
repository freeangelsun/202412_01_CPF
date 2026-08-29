from __future__ import annotations
import importlib.util
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[1] / "validators" / "currentize_source_identity.py"
spec = importlib.util.spec_from_file_location("currentize_source_identity", SCRIPT)
assert spec and spec.loader
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


def test_historical_reviewer_provenance_is_never_rewritten(tmp_path: Path):
    p = tmp_path / "CODEX_FINDING_CLOSURE.csv"
    p.write_text("work_item_id,source_identity\nWP-X,old\n", encoding="utf-8")
    assert module.rewrite_csv_source_identity(p, "new") is False
    assert "old" in p.read_text(encoding="utf-8")


def test_current_authority_source_identity_is_rewritten(tmp_path: Path):
    p = tmp_path / "CURRENT_WORK_ITEM_REGISTRY.csv"
    p.write_text("work_item_id,source_identity\nWP-X,old\n", encoding="utf-8")
    assert module.rewrite_csv_source_identity(p, "new") is True
    assert ",new" in p.read_text(encoding="utf-8-sig")
