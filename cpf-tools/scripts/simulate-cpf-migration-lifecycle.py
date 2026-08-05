#!/usr/bin/env python3
"""Deterministic fail-closed CPF migration lifecycle simulator.

This is the approved substitute when live Oracle/PostgreSQL/MariaDB runtimes are unavailable.
UNKNOWN is never retried, rolled back, or promoted to success until an explicit identity-bound
reconciliation decides whether the statement was applied.
"""
from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
from typing import Any

TERMINAL_SUCCESS = {"APPLIED", "RECONCILED_APPLIED"}
REAPPLYABLE = {"PENDING", "ROLLED_BACK", "RECONCILED_NOT_APPLIED"}


def plan_hash(operations: list[str]) -> str:
    return hashlib.sha256("\n".join(operations).encode("utf-8")).hexdigest()


def identity(operation: str, plan_sha256: str) -> str:
    return hashlib.sha256(f"{plan_sha256}|{operation}".encode("utf-8")).hexdigest()


def _validate_operations(operations: list[str]) -> None:
    if not operations or len(set(operations)) != len(operations):
        raise ValueError("operations must be non-empty and unique")
    if any(not isinstance(operation, str) or not operation.strip() for operation in operations):
        raise ValueError("every operation must be a non-blank string")


def simulate(operations: list[str], fail_at: int | None = None) -> dict[str, Any]:
    _validate_operations(operations)
    if fail_at is not None and not 0 <= fail_at < len(operations):
        raise ValueError("fail_at is outside the migration plan")
    digest = plan_hash(operations)
    states = {operation: "PENDING" for operation in operations}
    events: list[dict[str, Any]] = []
    for index, operation in enumerate(operations):
        if fail_at is not None and index == fail_at:
            states[operation] = "UNKNOWN"
            events.append({
                "operation": operation,
                "identitySha256": identity(operation, digest),
                "action": "APPLY",
                "status": "UNKNOWN",
            })
            break
        states[operation] = "APPLIED"
        events.append({
            "operation": operation,
            "identitySha256": identity(operation, digest),
            "action": "APPLY",
            "status": "APPLIED",
        })
    return {
        "schemaVersion": 2,
        "planSha256": digest,
        "operations": list(operations),
        "states": states,
        "events": events,
        "reconcileRequired": "UNKNOWN" in states.values(),
        "blocked": "UNKNOWN" in states.values(),
    }


def reconcile(state: dict[str, Any], outcome: str, operation: str | None = None) -> dict[str, Any]:
    normalized = outcome.strip().upper()
    if normalized not in {"APPLIED", "NOT_APPLIED"}:
        raise ValueError("reconcile outcome must be APPLIED or NOT_APPLIED")
    unknown = [name for name, value in state["states"].items() if value == "UNKNOWN"]
    if not unknown:
        raise ValueError("there is no UNKNOWN operation to reconcile")
    target = operation or unknown[0]
    if target not in unknown:
        raise ValueError("reconcile target is not UNKNOWN")
    if len(unknown) > 1 and operation is None:
        raise ValueError("operation is required when multiple UNKNOWN states exist")
    state["states"][target] = "RECONCILED_APPLIED" if normalized == "APPLIED" else "RECONCILED_NOT_APPLIED"
    state["events"].append({
        "operation": target,
        "identitySha256": identity(target, state["planSha256"]),
        "action": "RECONCILE",
        "status": normalized,
    })
    state["reconcileRequired"] = "UNKNOWN" in state["states"].values()
    state["blocked"] = state["reconcileRequired"]
    return state


def rollback(state: dict[str, Any]) -> dict[str, Any]:
    if "UNKNOWN" in state["states"].values():
        raise ValueError("rollback is blocked until UNKNOWN is reconciled")
    for operation in reversed(state["operations"]):
        current = state["states"][operation]
        if current in TERMINAL_SUCCESS:
            state["states"][operation] = "ROLLED_BACK"
            state["events"].append({
                "operation": operation,
                "identitySha256": identity(operation, state["planSha256"]),
                "action": "ROLLBACK",
                "status": "ROLLED_BACK",
            })
    state["blocked"] = False
    return state


def reapply(state: dict[str, Any]) -> dict[str, Any]:
    if "UNKNOWN" in state["states"].values():
        raise ValueError("reapply is blocked until UNKNOWN is reconciled")
    for operation in state["operations"]:
        current = state["states"][operation]
        if current in REAPPLYABLE:
            state["states"][operation] = "APPLIED"
            state["events"].append({
                "operation": operation,
                "identitySha256": identity(operation, state["planSha256"]),
                "action": "REAPPLY",
                "status": "APPLIED",
            })
    state["reconcileRequired"] = False
    state["blocked"] = False
    return state


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--plan", type=Path, required=True)
    parser.add_argument("--fail-at", type=int)
    parser.add_argument("--reconcile-outcome", choices=("APPLIED", "NOT_APPLIED"))
    parser.add_argument("--reconcile-operation")
    parser.add_argument("--rollback", action="store_true")
    parser.add_argument("--reapply", action="store_true")
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    raw = json.loads(args.plan.read_text(encoding="utf-8"))
    operations = raw["operations"] if isinstance(raw, dict) else raw
    state = simulate(operations, args.fail_at)
    if args.reconcile_outcome:
        state = reconcile(state, args.reconcile_outcome, args.reconcile_operation)
    if args.rollback:
        state = rollback(state)
    if args.reapply:
        state = reapply(state)
    text = json.dumps(state, ensure_ascii=False, indent=2, sort_keys=True) + "\n"
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(text, encoding="utf-8")
    print(text, end="")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (OSError, ValueError, KeyError, json.JSONDecodeError) as error:
        print(json.dumps({"status": "FAIL", "error": type(error).__name__, "message": str(error)}), file=__import__("sys").stderr)
        raise SystemExit(1)
