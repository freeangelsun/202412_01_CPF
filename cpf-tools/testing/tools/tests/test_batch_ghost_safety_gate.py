from __future__ import annotations

import importlib.util
from pathlib import Path

SCRIPT = Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-batch-ghost-safety.py"
SPEC = importlib.util.spec_from_file_location("ghost_gate", SCRIPT)
assert SPEC and SPEC.loader
MODULE = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(MODULE)


def test_vendor_timestamp_functions_normalize_to_same_semantics() -> None:
    oracle = "UPDATE x SET updated_at = SYSTIMESTAMP WHERE id = ?"
    postgres = "UPDATE x SET updated_at = CURRENT_TIMESTAMP WHERE id = ?"
    mariadb = "UPDATE x SET updated_at = CURRENT_TIMESTAMP(3) WHERE id = ?"
    assert MODULE.normalized_sql(oracle) == MODULE.normalized_sql(postgres)
    assert MODULE.normalized_sql(postgres) == MODULE.normalized_sql(mariadb)


def test_non_timestamp_sql_difference_is_not_hidden() -> None:
    first = "UPDATE x SET status = 'FAILED' WHERE id = ?"
    second = "UPDATE x SET status = 'DONE' WHERE id = ?"
    assert MODULE.normalized_sql(first) != MODULE.normalized_sql(second)
