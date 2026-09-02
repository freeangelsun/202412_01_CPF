"""Center-Cut lease expiry is a DB-clock contract, never a JVM timezone contract."""
from __future__ import annotations

import json
import re
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
PACK_ROOT = ROOT / "cpf-tools/db/vendor"
CONTRACT = ROOT / "cpf-tools/db/metadata/bat-runtime-query-contract.json"

LEASE_STATEMENT_GROUPS = {
    "center-cut-claim": (
        "centercut-claim-find-expired-running",
        "centercut-claim-reclaim",
        "centercut-claim-insert",
        "centercut-claim-renew",
        "centercut-claim-expire",
        "centercut-claim-release-after-item-conflict",
        "centercut-claim-release-complete",
    ),
    "scheduler-leader": (
        "scheduler-leader-acquire-update",
        "scheduler-leader-insert",
        "scheduler-leader-heartbeat",
        "scheduler-leader-is-current",
    ),
}

VENDOR_CLOCK = {
    "mariadb": "UTC_TIMESTAMP(6)",
    "postgresql": "AT TIME ZONE 'UTC'",
    "oracle": "SYS_EXTRACT_UTC(SYSTIMESTAMP)",
}


def _sql(vendor: str, statement: str) -> str:
    return (PACK_ROOT / vendor / "runtime/bat/repository" / f"{statement}.sql").read_text(
        encoding="utf-8"
    )


def _uses_client_bound_lease(sql: str) -> bool:
    return re.search(r"\blease_until\s*=\s*\?", sql, flags=re.IGNORECASE) is not None


def test_batch_leases_use_selected_vendor_utc_clock_for_all_transitions() -> None:
    for vendor, clock in VENDOR_CLOCK.items():
        for lease_owner, statements in LEASE_STATEMENT_GROUPS.items():
            texts = {statement: _sql(vendor, statement) for statement in statements}
            assert all(clock in text for text in texts.values()), (
                f"{vendor}/{lease_owner}: missing UTC DB clock: {texts}"
            )
            assert not any(_uses_client_bound_lease(text) for text in texts.values()), (
                f"{vendor}/{lease_owner}: client-bound lease timestamp reintroduced"
            )
            if vendor == "mariadb":
                assert not any("CURRENT_TIMESTAMP" in text for text in texts.values())
            elif vendor == "postgresql":
                assert all(
                    text.count("CURRENT_TIMESTAMP") == text.count("AT TIME ZONE 'UTC'")
                    for text in texts.values()
                ), f"{vendor}/{lease_owner}: session-timezone clock reintroduced"
            else:
                assert not any(
                    re.search(r"(?<!SYS_EXTRACT_UTC\()SYSTIMESTAMP", text) for text in texts.values()
                ), f"{vendor}/{lease_owner}: session-timezone clock reintroduced"


def test_center_cut_claim_parameter_contract_passes_a_duration_not_client_timestamps() -> None:
    statements = {row["key"]: row for row in json.loads(CONTRACT.read_text(encoding="utf-8"))["statements"]}
    assert statements["centercut-claim-reclaim"]["parameters"] == [
        "runnerId", "poolId", "claimToken", "fencingToken", "leaseDurationMicros", "itemId"
    ]
    assert statements["centercut-claim-insert"]["parameters"] == [
        "itemId", "runnerId", "poolId", "claimToken", "leaseDurationMicros"
    ]
    assert statements["centercut-claim-renew"]["parameters"] == [
        "leaseDurationMicros", "itemId", "runnerId", "claimToken", "fencingToken"
    ]
    assert statements["scheduler-leader-acquire-update"]["parameters"] == [
        "instanceId", "leaseDurationMicros", "schedulerKey", "currentInstanceId"
    ]
    assert statements["scheduler-leader-insert"]["parameters"] == [
        "schedulerKey", "instanceId", "leaseDurationMicros"
    ]
    assert statements["scheduler-leader-heartbeat"]["parameters"] == [
        "leaseDurationMicros", "schedulerKey", "instanceId", "fencingToken"
    ]


def test_negative_mutation_client_bound_timestamp_is_detected() -> None:
    assert _uses_client_bound_lease("UPDATE BAT_CENTER_CUT_CLAIM SET lease_until = ?")
