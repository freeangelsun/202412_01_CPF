from __future__ import annotations

import importlib.util
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[1] / "verify-cpf-split-master-dataset.py"
SPEC = importlib.util.spec_from_file_location("split_master", SCRIPT)
assert SPEC and SPEC.loader
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


def test_requirement_id_contract_accepts_product_and_gate_rows() -> None:
    pattern = MODULE.INDEXES["requirement"][2]
    assert pattern.fullmatch("CPF-FR-000001")
    assert pattern.fullmatch("CPF-GATE-00")
    assert not pattern.fullmatch("CPF-UNKNOWN-01")
