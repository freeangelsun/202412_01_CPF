#!/usr/bin/env python3
"""Verify durable, fail-closed ADM audit recording and recovery."""
from __future__ import annotations
import argparse, json, pathlib, re, sys

class GateError(RuntimeError): pass

def read(root: pathlib.Path, rel: str) -> str:
    path=root/rel
    if not path.is_file(): raise GateError(f"required file is missing: {rel}")
    try: return path.read_text(encoding="utf-8")
    except (OSError,UnicodeError) as exc: raise GateError(f"cannot read {rel}: {exc}") from exc

def require_tokens(text: str, rel: str, tokens: tuple[str,...], errors: list[str]) -> None:
    for token in tokens:
        if token not in text: errors.append(f"{rel}: required token missing: {token}")

def verify(root: pathlib.Path) -> dict[str,object]:
    service_rel="cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmAuditLogService.java"
    delivery_rel="cpf-admin/src/main/java/com/cpf/admin/opr/service/AdmAuditDeliveryService.java"
    service=read(root,service_rel); delivery=read(root,delivery_rel); errors=[]
    require_tokens(service,service_rel,(
        "delivery.enrichReservation(mandatory,c);",
        "delivery.executeAudited(c,op,after)",
        "delivery.record(c,after,diff)",
        "throw new IllegalStateException(\"ADM 감사 로그 조회 실패.",
        "throw ex;",
    ),errors)
    if "ADM mandatory audit 상세 보강 실패" in service or re.search(r"catch\s*\(RuntimeException\s+\w+\)\s*\{\s*log\.warn",service):
        errors.append(f"{service_rel}: mandatory audit failure is swallowed")
    if re.search(r"catch\s*\(DataAccessException[^)]*\)\s*\{\s*return\s+(?:List\.of\(\)|Collections\.emptyList\(\))",service,re.S):
        errors.append(f"{service_rel}: DB failure is disguised as an empty audit result")

    require_tokens(delivery,delivery_rel,(
        "PROPAGATION_REQUIRES_NEW", "long id = reserve(command);", "OPERATION_STATUS,DELIVERY_STATUS",
        "'REQUESTED','PENDING'", "recoverStaleRequested", "OPERATION_STATUS='UNKNOWN'",
        "DELIVERY_STATUS='RETRY'", "FOR UPDATE", "markRetry(id, ex)",
        "ATTEMPT_COUNT=ATTEMPT_COUNT+1", "DELIVERY_STATUS='DELIVERED'",
        "@Scheduled", "setMaxRows(RELAY_BATCH_SIZE)",
    ),errors)
    reserve_pos=delivery.find("long id = reserve(command);")
    operation_pos=delivery.find("T result = operation.get();")
    if reserve_pos < 0 or operation_pos < 0 or reserve_pos > operation_pos:
        errors.append(f"{delivery_rel}: durable reservation must be committed before owner operation")
    if "completeOperation(id, \"FAILED\"" not in delivery or "throw ex;" not in delivery:
        errors.append(f"{delivery_rel}: owner failure outcome is not preserved and rethrown")
    query_start = delivery.find("public List<Map<String, Object>> findDeliveries")
    query_end = delivery.find("public Map<String, Object> findDelivery", query_start + 1)
    query_body = delivery[query_start:query_end if query_end > query_start else len(delivery)]
    if query_start < 0 or "catch" in query_body or "return List.of()" in query_body or "emptyList()" in query_body:
        errors.append(f"{delivery_rel}: audit delivery query must not swallow DB failures")
    if errors: raise GateError("\n".join(errors))
    return {"status":"PASS","durableReservation":True,"unknownRecovery":True,"multiInstanceLock":True,"queryFailClosed":True}

def main() -> int:
    p=argparse.ArgumentParser(); p.add_argument("--root",default="."); p.add_argument("--json-output"); a=p.parse_args()
    try: result=verify(pathlib.Path(a.root).resolve())
    except GateError as exc:
        result={"status":"FAIL","message":str(exc)}
        if a.json_output: pathlib.Path(a.json_output).write_text(json.dumps(result,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
        print(f"CPF audit fail-closed contract FAIL\n{exc}",file=sys.stderr); return 1
    if a.json_output: pathlib.Path(a.json_output).write_text(json.dumps(result,ensure_ascii=False,indent=2)+"\n",encoding="utf-8")
    print("CPF audit fail-closed contract PASS"); print(json.dumps(result,sort_keys=True)); return 0
if __name__=="__main__": raise SystemExit(main())
