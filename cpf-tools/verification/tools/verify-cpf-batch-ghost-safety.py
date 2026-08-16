#!/usr/bin/env python3
"""Fail-closed contract for BAT ghost execution and expired-lock operations."""
from __future__ import annotations

import argparse
from pathlib import Path
import re
import sys

VENDORS = ("oracle", "postgresql", "mariadb")


def require(text: str, token: str, rel: str, errors: list[str]) -> None:
    if token not in text:
        errors.append(f"{rel}: required token missing: {token}")


def normalized_sql(text: str) -> str:
    normalized = re.sub(r"\s+", " ", text.strip().lower())
    normalized = re.sub(
        r"(?<!\w)(?:systimestamp|current_timestamp(?:\(3\))?)(?!\w)",
        "<current-timestamp>",
        normalized,
    )
    return normalized


def verify(root: Path) -> None:
    errors: list[str] = []
    service_rel = "cpf-batch/control-plane/src/main/java/com/cpf/batch/control/compat/BatchOperationsCompatibilityService.java"
    controller_rel = "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmBatchController.java"
    api_rel = "cpf-admin/frontend/src/features/batch-runtime-control/api.ts"
    workbench_rel = "cpf-admin/frontend/src/features/batch-runtime-control/BatchOperationsWorkbench.vue"
    required = (service_rel, controller_rel, api_rel, workbench_rel)
    for rel in required:
        if not (root / rel).is_file():
            errors.append(f"{rel}: source missing")
    if errors:
        raise ValueError("\n".join(errors))

    service = (root / service_rel).read_text(encoding="utf-8")
    for token in (
        'exactOne("compat-lock-for-update", lockKey)',
        'requireExpiredLock(before, lockKey)',
        'requireSingleMutation(changed, "expired lock release", lockKey)',
        'exactOne("compat-execution-lock", executionId)',
        'requireGhostCandidate(before, executionId)',
        'exactOne("compat-lock-expired-for-job-for-update", jobId)',
        'requireSingleMutation(changed, "ghost lock release", lockKey)',
        'requireSingleMutation(changed, "ghost execution transition", String.valueOf(executionId))',
        'GHOST_ACTIVE_STATUSES',
        'last_heartbeat_at',
    ):
        require(service, token, service_rel, errors)
    if 'jdbc.update(sql.required("compat-lock-delete-expired"), lockKey);\n            audit(' in service:
        errors.append(f"{service_rel}: expired lock mutation can be audited without exact row-count validation")
    if 'jdbc.update(sql.required("compat-lock-delete-job-expired"), jobId);\n            } else' in service:
        errors.append(f"{service_rel}: ghost lock release ignores changed row count")
    if 'jdbc.update(\n                        sql.required("compat-execution-finish-ghost"),' in service and 'int changed = jdbc.update(' not in service:
        errors.append(f"{service_rel}: ghost execution transition ignores changed row count")

    controller = (root / controller_rel).read_text(encoding="utf-8")
    require(controller, '"BATCH_GHOST_" + result.get("action")', controller_rel, errors)
    if 'result.get("actionType")' in controller:
        errors.append(f"{controller_rel}: audit action uses a response field that BAT owner does not return")

    api = (root / api_rel).read_text(encoding="utf-8")
    require(api, "['FAIL', 'ABANDON', 'RELEASE_LOCK']", api_rel, errors)
    # 다른 Batch/Center-Cut 기능의 RECONCILE 문자열은 허용하되 Ghost action 집합에 노출되면 실패한다.
    ghost_reconcile = bool(re.search(
        r"actGhostExecution.*?(?:\[|includes\().*?[\"\']RECONCILE[\"\']", api, re.S))
    if ghost_reconcile:
        errors.append(f"{api_rel}: unsupported RECONCILE ghost action is exposed")

    workbench = (root / workbench_rel).read_text(encoding="utf-8")
    require(workbench, 'actionType:"ABANDON"', workbench_rel, errors)
    if 'actionType:"RECONCILE"' in workbench:
        errors.append(f"{workbench_rel}: workbench invokes unsupported RECONCILE action")

    finish_sql: list[str] = []
    lock_sql: list[str] = []
    for vendor in VENDORS:
        base = Path("cpf-tools/db/vendor") / vendor / "runtime/bat/repository"
        finish_rel = str(base / "compat-execution-finish-ghost.sql")
        lock_rel = str(base / "compat-lock-expired-for-job-for-update.sql")
        for rel in (finish_rel, lock_rel):
            if not (root / rel).is_file():
                errors.append(f"{rel}: SQL missing")
        if not (root / finish_rel).is_file() or not (root / lock_rel).is_file():
            continue
        finish = (root / finish_rel).read_text(encoding="utf-8")
        lock = (root / lock_rel).read_text(encoding="utf-8")
        for token in (
            "execution_status IN ('RUNNING', 'CLAIMED', 'CLAIMING')",
            "last_heartbeat_at IS NOT NULL",
            "updated_by = ?",
            "WHERE execution_id = ?",
        ):
            require(finish, token, finish_rel, errors)
        for token in ("WHERE job_id = ?", "expire_at < CURRENT_TIMESTAMP(3)", "FOR UPDATE"):
            require(lock, token, lock_rel, errors)
        finish_sql.append(normalized_sql(finish))
        lock_sql.append(normalized_sql(lock))

    if len(set(finish_sql)) > 1:
        errors.append("3DB compat-execution-finish-ghost.sql semantic drift")
    if len(set(lock_sql)) > 1:
        errors.append("3DB compat-lock-expired-for-job-for-update.sql semantic drift")

    if errors:
        raise ValueError("\n".join(errors))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    args = parser.parse_args()
    try:
        verify(Path(args.root).resolve())
    except ValueError as exc:
        print(f"[FAIL] CPF BAT ghost safety contract\n{exc}", file=sys.stderr)
        return 1
    print("[PASS] CPF BAT ghost safety contract rowLock=true heartbeatRecheck=true exactMutation=true vendors=3")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
