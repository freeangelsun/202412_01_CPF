#!/usr/bin/env python3
"""Verify durable, fail-closed ADM audit recording."""
from __future__ import annotations
import argparse
from pathlib import Path
import sys


def verify(root: Path) -> None:
    service_rel = "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmAuditLogService.java"
    delivery_rel = "cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmAuditDeliveryService.java"
    service_path = root / service_rel
    delivery_path = root / delivery_rel
    errors: list[str] = []
    if not service_path.is_file(): errors.append(f"{service_rel}: source missing")
    if not delivery_path.is_file(): errors.append(f"{delivery_rel}: source missing")
    if errors: raise ValueError("\n".join(errors))
    service = service_path.read_text(encoding="utf-8")
    delivery = delivery_path.read_text(encoding="utf-8")
    required_service = [
        "delivery.enrichReservation(mandatory,c);",
        "if(mandatory==null){delivery.record(c,after,diff);return;}",
        "throw new IllegalStateException(\"ADM 감사 로그 조회 실패.",
    ]
    for token in required_service:
        if token not in service: errors.append(f"{service_rel}: required token missing: {token}")
    if "ADM mandatory audit 상세 보강 실패" in service or "catch(RuntimeException ex){log.warn" in service:
        errors.append(f"{service_rel}: mandatory audit enrichment failure is swallowed")
    for token in ["long id = reserve(command);", "recoverStaleRequested", "OPERATION_STATUS='UNKNOWN'", "markRetry(id, ex)"]:
        if token not in delivery: errors.append(f"{delivery_rel}: durable recovery token missing: {token}")
    if errors: raise ValueError("\n".join(errors))


def main() -> int:
    p=argparse.ArgumentParser(); p.add_argument("--root",default="."); a=p.parse_args()
    try: verify(Path(a.root).resolve())
    except ValueError as exc:
        print(f"[FAIL] CPF audit fail-closed contract\n{exc}",file=sys.stderr); return 1
    print("[PASS] CPF audit fail-closed contract durableReservation=true enrichmentFailClosed=true")
    return 0
if __name__=="__main__": raise SystemExit(main())
