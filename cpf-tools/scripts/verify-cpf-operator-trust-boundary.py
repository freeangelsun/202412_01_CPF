#!/usr/bin/env python3
"""Fail-closed verification for server-owned operator identity.

Browser payload/query fields must never be trusted as the actor for privileged ADM/BZA
operations. Controllers must reconstruct commands with the authenticated request
attribute (adm.operatorId) before calling owner ports.
"""
from __future__ import annotations

import argparse
import pathlib
import re
import sys

FORBIDDEN_FRONTEND_FIELDS = ("requestedBy", "requestUser", "actorId", "operatorIdOverride")
ALLOWED_FRONTEND_FILES = {
    "cpf-admin/frontend/src/shared/cpfApi.ts",  # contains the explicit deny-list and guard only
}


def read(path: pathlib.Path) -> str:
    return path.read_text(encoding="utf-8")


def fail(message: str) -> None:
    raise ValueError(message)


def verify(root: pathlib.Path) -> None:
    violations: list[str] = []
    for base in (root / "cpf-admin/frontend/src", root / "cpf-biz-admin/frontend/src"):
        if not base.exists():
            continue
        for path in base.rglob("*"):
            if path.suffix not in {".ts", ".vue", ".js", ".tsx"}:
                continue
            rel = path.relative_to(root).as_posix()
            text = read(path)
            # Response DTO/type declarations may expose audited server actor fields for display.
            # Strip declarations before checking executable browser payload/query construction.
            executable_text = re.sub(r"\binterface\s+\w+(?:\s+extends\s+[^\{]+)?\s*\{.*?\}", "", text, flags=re.S)
            executable_text = re.sub(r"\btype\s+\w+(?:<[^;=]+>)?\s*=.*?;", "", executable_text, flags=re.S)
            if rel in ALLOWED_FRONTEND_FILES:
                for field in FORBIDDEN_FRONTEND_FIELDS:
                    if field not in text:
                        violations.append(f"{rel}: deny-list is missing {field}")
                if "assertNoClientActor" not in text:
                    violations.append(f"{rel}: recursive actor-field guard is missing")
                continue
            for field in FORBIDDEN_FRONTEND_FIELDS:
                # Text in user-facing explanatory strings is allowed, but object/query field syntax is not.
                patterns = [
                    rf"\b{re.escape(field)}\s*:",
                    rf"[?&]{re.escape(field)}=",
                    rf"\.set\(\s*['\"]{re.escape(field)}['\"]",
                    rf"\[['\"]{re.escape(field)}['\"]\]\s*=",
                    rf"\.{re.escape(field)}\s*=",
                ]
                if any(re.search(pattern, executable_text) for pattern in patterns):
                    violations.append(f"{rel}: Browser actor field is forbidden: {field}")

    controller_root = root / "cpf-admin/src/main/java/com/cpf/admin/opr/controller"
    if controller_root.exists():
        helper_pattern = re.compile(
            r"private\s+String\s+requestUser\s*\(HttpServletRequest\s+request,\s*String\s+fallback\)\s*\{(?P<body>.*?)\}",
            re.S,
        )
        for controller_path in controller_root.rglob("*.java"):
            controller_text = read(controller_path)
            rel = controller_path.relative_to(root).as_posix()
            for match in helper_pattern.finditer(controller_text):
                body = match.group("body")
                if "return requireOperator(request);" not in body:
                    violations.append(f"{rel}: requestUser helper must use requireOperator(request)")
                if re.search(r"return\s+(?:fallback|value)\b", body):
                    violations.append(f"{rel}: client actor fallback is forbidden")
            if "request.setRequestUser(requestUser);" in controller_text:
                violations.append(f"{rel}: raw requestUser is stored before server actor reconstruction")

    controller = root / "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmRuntimeControlController.java"
    if controller.exists():
        text = read(controller)
        raw_types = (
            "@RequestBody CpfRuntimeChangeCommand",
            "@RequestBody CpfRuntimeGroupCommand",
            "@RequestBody CpfRuntimeGroupMemberCommand",
        )
        for token in raw_types:
            if token in text:
                violations.append(f"{controller.relative_to(root)}: raw client-owned command accepted: {token}")
        required = (
            'request.getAttribute("adm.operatorId")',
            "body.toCommand(operator)",
            "new CpfRuntimeChangeCommand",
            "new CpfRuntimeGroupCommand",
            "new CpfRuntimeGroupMemberCommand",
        )
        for token in required:
            if token not in text:
                violations.append(f"{controller.relative_to(root)}: server actor injection token missing: {token}")

    batch = root / "cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java"
    if batch.exists():
        text = read(batch)
        for token in ('command.remove("requestedBy")', 'command.remove("requestUser")', 'command.put("requestedBy", operatorId)'):
            if token not in text:
                violations.append(f"{batch.relative_to(root)}: trusted batch actor reconstruction missing: {token}")

    gateway = root / "cpf-admin/src/main/java/com/cpf/admin/opr/gateway/AdmGatewayRegistryController.java"
    if gateway.exists():
        text = read(gateway)
        if "String operator=operator(request)" not in text or ",operator)" not in text:
            violations.append(f"{gateway.relative_to(root)}: Gateway mutation does not reconstruct server actor")

    if violations:
        fail("\n".join(violations))


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    args = parser.parse_args()
    try:
        verify(pathlib.Path(args.root).resolve())
    except ValueError as exc:
        print(f"CPF operator trust boundary FAIL\n{exc}", file=sys.stderr)
        return 1
    print("CPF operator trust boundary PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
