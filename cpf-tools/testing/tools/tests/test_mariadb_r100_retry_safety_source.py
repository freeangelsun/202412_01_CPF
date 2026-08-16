from __future__ import annotations

import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[4]
SQL = ROOT / "cpf-tools/db/vendor/mariadb/rollback/R100__bat_operation_request_ledger.sql"
HELPER = "cpf_assert_empty_bat_operation_request_r100"


def test_failed_nonempty_rollback_can_be_retried_after_reconciliation() -> None:
    text = SQL.read_text(encoding="utf-8-sig")
    create = re.search(rf"(?is)\bCREATE\s+PROCEDURE\s+`?{HELPER}`?\b", text)
    assert create, "helper CREATE PROCEDURE missing"
    pre_cleanup = re.search(
        rf"(?is)\bDROP\s+PROCEDURE\s+IF\s+EXISTS\s+`?{HELPER}`?\s*;",
        text[: create.start()],
    )
    assert pre_cleanup, (
        "failed CALL leaves the helper procedure behind; retry must clean it before CREATE PROCEDURE"
    )
    call = re.search(rf"(?is)\bCALL\s+`?{HELPER}`?\s*\(\s*\)", text)
    final_cleanup = re.search(rf"(?is)\bDROP\s+PROCEDURE\s+`?{HELPER}`?\b", text[create.end():])
    assert call and final_cleanup
