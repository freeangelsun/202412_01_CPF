#!/usr/bin/env python3
"""Static fail-closed contract for ADM↔BAT owner calls."""
from __future__ import annotations

import sys as _cpf_sys

# CPF 표준 인코딩은 UTF-8 이다. 호출자의 콘솔 코드페이지(Windows cp949 등)에 좌우되면
# 한글 출력이 깨져 진단 메시지를 읽을 수 없다. 진입점이 스스로 출력 스트림을 고정한다.
for _cpf_stream in (_cpf_sys.stdout, _cpf_sys.stderr):
    try:
        _cpf_stream.reconfigure(encoding='utf-8')
    except (AttributeError, ValueError):
        pass
import argparse
from pathlib import Path
import sys


def require(text: str, token: str, rel: str, errors: list[str]) -> None:
    if token not in text:
        errors.append(f"{rel}: required token missing: {token}")


def verify(root: Path) -> None:
    errors: list[str] = []
    controller_rel = "cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java"
    client_rel = "cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlClient.java"
    adapter_rel = "cpf-admin/src/main/java/com/cpf/admin/opr/batch/RemoteCpfBatchOperationsAdapter.java"
    internal_rel = "cpf-batch/control-plane/src/main/java/com/cpf/batch/control/compat/BatInternalOperationsController.java"
    for rel in (controller_rel, client_rel, adapter_rel, internal_rel):
        if not (root / rel).is_file():
            errors.append(f"{rel}: source missing")
    if errors:
        raise ValueError("\n".join(errors))

    controller = (root / controller_rel).read_text(encoding="utf-8")
    require(controller, "ResponseEntity<Map<String, Object>> instances", controller_rel, errors)
    require(controller, "ResponseEntity.status(503)", controller_rel, errors)
    require(controller, '"stale", true', controller_rel, errors)
    require(controller, '"partial", true', controller_rel, errors)
    if 'Map<String, Object> instances(' in controller and 'ResponseEntity<Map<String, Object>> instances(' not in controller:
        errors.append(f"{controller_rel}: instance failure can be returned as HTTP 200")

    client = (root / client_rel).read_text(encoding="utf-8")
    require(client, '"BAT_OWNER_EMPTY_RESPONSE"', client_rel, errors)
    if "value == null ? new CpfDataRow()" in client:
        errors.append(f"{client_rel}: null owner response is converted to an empty row")
    require(client, "if (value == null)", client_rel, errors)

    adapter = (root / adapter_rel).read_text(encoding="utf-8")
    require(adapter, "BAT Owner 목록 응답 본문이 없습니다.", adapter_rel, errors)
    require(adapter, "BAT Owner 상세 응답 본문이 없습니다.", adapter_rel, errors)
    require(adapter, "if (result.unknown())", adapter_rel, errors)
    require(adapter, "if (!result.success())", adapter_rel, errors)

    internal = (root / internal_rel).read_text(encoding="utf-8")
    require(internal, "actorResolver.actor(request", internal_rel, errors)
    require(internal, 'textOrNull(payload,"requestUser")', internal_rel, errors)

    if errors:
        raise ValueError("\n".join(errors))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    args = parser.parse_args()
    try:
        verify(Path(args.root).resolve())
    except ValueError as exc:
        print(f"[FAIL] CPF batch fail-closed contract\n{exc}", file=sys.stderr)
        return 1
    print("[PASS] CPF batch fail-closed contract ownerErrorsSeparated=true nullResponsesRejected=true")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
