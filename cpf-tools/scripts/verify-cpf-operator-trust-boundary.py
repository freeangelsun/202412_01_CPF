#!/usr/bin/env python3
"""Fail-closed verification for server-owned operator identity.

Privileged ADM/BZA commands must derive their actor from the authenticated server
request attribute. Browser supplied aliases are rejected recursively in the UI and
removed recursively before Batch owner calls.
"""
from __future__ import annotations

import argparse
import json
import pathlib
import re
import sys
from typing import Iterable

ACTOR_FIELDS = ("requestedBy", "requestUser", "actorId", "operatorId", "operatorIdOverride")
ALLOWED_FRONTEND_FILES = {
    "cpf-admin/frontend/src/shared/cpfApi.ts",
    "cpf-biz-admin/frontend/src/shared/cpfApi.ts",
}
TEXT_SUFFIXES = {".ts", ".tsx", ".js", ".vue"}

class GateError(RuntimeError):
    pass

def read(path: pathlib.Path) -> str:
    try:
        return path.read_text(encoding="utf-8")
    except (OSError, UnicodeError) as exc:
        raise GateError(f"cannot read {path}: {exc}") from exc

def require_file(root: pathlib.Path, rel: str) -> pathlib.Path:
    path = root / rel
    if not path.is_file():
        raise GateError(f"required file is missing: {rel}")
    return path

def require_dir(root: pathlib.Path, rel: str) -> pathlib.Path:
    path = root / rel
    if not path.is_dir():
        raise GateError(f"required directory is missing: {rel}")
    return path

def executable_typescript(text: str) -> str:
    text = re.sub(r"\binterface\s+\w+(?:\s+extends\s+[^\{]+)?\s*\{.*?\}", "", text, flags=re.S)
    return re.sub(r"\btype\s+\w+(?:<[^;=]+>)?\s*=.*?;", "", text, flags=re.S)

def browser_actor_patterns(field: str) -> Iterable[str]:
    escaped = re.escape(field)
    return (
        rf"\b{escaped}\s*:", rf"[?&]{escaped}=", rf"\.set\(\s*['\"]{escaped}['\"]",
        rf"\[['\"]{escaped}['\"]\]\s*=", rf"\.{escaped}\s*=",
    )

def verify_frontend(root: pathlib.Path, violations: list[str]) -> int:
    admin_root = require_dir(root, "cpf-admin/frontend/src")
    bza_root = require_dir(root, "cpf-biz-admin/frontend/src")
    for rel in sorted(ALLOWED_FRONTEND_FILES):
        require_file(root, rel)
    checked = 0
    for base in (admin_root, bza_root):
        for path in sorted(p for p in base.rglob("*") if p.is_file() and p.suffix in TEXT_SUFFIXES):
            checked += 1
            rel = path.relative_to(root).as_posix()
            text = read(path)
            executable = executable_typescript(text)
            if rel in ALLOWED_FRONTEND_FILES:
                for field in ACTOR_FIELDS:
                    if re.search(rf"['\"]{re.escape(field)}['\"]", text) is None:
                        violations.append(f"{rel}: deny-list is missing {field}")
                for token in ("CLIENT_ACTOR_FIELDS", "assertNoClientActor", "Array.isArray", "Object.entries", "assertNoClientActorQuery"):
                    if token not in text:
                        violations.append(f"{rel}: recursive actor guard token missing: {token}")
                if text.count("assertNoClientActorQuery(target)") < 5:
                    violations.append(f"{rel}: not every URL entry point applies the actor query guard")
                generated = text[text.find("export async function cpfGeneratedRequest"):text.find("export async function admApi")]
                if "CLIENT_ACTOR_FIELDS.has(key)" not in generated:
                    violations.append(f"{rel}: generated-client params bypass actor query guard")
                continue
            for field in ACTOR_FIELDS:
                if any(re.search(pattern, executable) for pattern in browser_actor_patterns(field)):
                    violations.append(f"{rel}: browser actor field is forbidden: {field}")
    if checked == 0:
        violations.append("frontend actor verification scanned zero source files")
    return checked

def verify_controllers(root: pathlib.Path, violations: list[str]) -> int:
    controller_root = require_dir(root, "cpf-admin/src/main/java/com/cpf/admin/opr/controller")
    controllers = sorted(controller_root.rglob("*.java"))
    if not controllers:
        violations.append("ADM controller actor verification scanned zero Java files")
    helper_pattern = re.compile(
        r"private\s+String\s+requestUser\s*\(HttpServletRequest\s+request,\s*String\s+fallback\)\s*\{(?P<body>.*?)\}", re.S)
    raw_param = re.compile(r"@RequestParam(?:\([^)]*\))?\s+String\s+(?:requestedBy|requestUser|actorId|operatorId|operatorIdOverride)\b")
    for path in controllers:
        rel = path.relative_to(root).as_posix()
        text = read(path)
        if raw_param.search(text):
            violations.append(f"{rel}: privileged actor must not be accepted as a request parameter")
        for match in helper_pattern.finditer(text):
            body = match.group("body")
            if "return requireOperator(request);" not in body or re.search(r"return\s+(?:fallback|value)\b", body):
                violations.append(f"{rel}: client actor fallback is forbidden")
        if "request.setRequestUser(requestUser);" in text:
            violations.append(f"{rel}: raw requestUser is stored before server actor reconstruction")

    runtime_rel = "cpf-admin/src/main/java/com/cpf/admin/opr/controller/AdmRuntimeControlController.java"
    runtime = read(require_file(root, runtime_rel))
    for token in ("@RequestBody CpfRuntimeChangeCommand", "@RequestBody CpfRuntimeGroupCommand", "@RequestBody CpfRuntimeGroupMemberCommand"):
        if token in runtime:
            violations.append(f"{runtime_rel}: raw client-owned command accepted: {token}")
    for token in ('request.getAttribute("adm.operatorId")', "body.toCommand(operator)", "new CpfRuntimeChangeCommand", "new CpfRuntimeGroupCommand", "new CpfRuntimeGroupMemberCommand"):
        if token not in runtime:
            violations.append(f"{runtime_rel}: server actor injection token missing: {token}")

    batch_rel = "cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java"
    batch = read(require_file(root, batch_rel))
    required_batch = (
        "CLIENT_ACTOR_FIELDS", "sanitizeCommandMap", "sanitizeCommandValue", "instanceof Map<?, ?>",
        "instanceof List<?>", 'command.put("requestedBy", operatorId)',
    ) + tuple(f'"{field}"' for field in ACTOR_FIELDS)
    for token in required_batch:
        if token not in batch:
            violations.append(f"{batch_rel}: recursive trusted actor reconstruction missing: {token}")
    if re.search(r"command\.put\(\s*\"(?:requestUser|actorId|operatorId|operatorIdOverride)\"", batch):
        violations.append(f"{batch_rel}: non-canonical actor is injected into BAT owner command")
    for call in (
        "client.saveJobDefinition(withServerActor(request, operatorId))",
        "client.transitionJobDefinition(jobId, version, withServerActor(request, operatorId))",
        "client.command(withServerActor(request, operatorId))",
        "client.createPlan(withServerActor(request, operatorId))",
    ):
        if call not in batch:
            violations.append(f"{batch_rel}: privileged BAT mutation bypasses server actor reconstruction: {call}")

    gateway_rel = "cpf-admin/src/main/java/com/cpf/admin/opr/gateway/AdmGatewayRegistryController.java"
    gateway = read(require_file(root, gateway_rel))
    if "String operator=operator(request)" not in gateway or ',operator)' not in gateway:
        violations.append(f"{gateway_rel}: Gateway mutation does not reconstruct server actor")
    return len(controllers)

def verify(root: pathlib.Path) -> dict[str, object]:
    if not root.is_dir():
        raise GateError(f"repository root does not exist: {root}")
    violations: list[str] = []
    frontend_count = verify_frontend(root, violations)
    controller_count = verify_controllers(root, violations)
    if violations:
        raise GateError("\n".join(violations))
    return {"status": "PASS", "frontendSourceCount": frontend_count, "controllerSourceCount": controller_count, "actorFields": list(ACTOR_FIELDS)}

def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--json-output")
    args = parser.parse_args()
    try:
        result = verify(pathlib.Path(args.root).resolve())
    except GateError as exc:
        result = {"status": "FAIL", "message": str(exc)}
        if args.json_output:
            pathlib.Path(args.json_output).write_text(json.dumps(result, ensure_ascii=False, indent=2)+"\n", encoding="utf-8")
        print(f"CPF operator trust boundary FAIL\n{exc}", file=sys.stderr)
        return 1
    if args.json_output:
        pathlib.Path(args.json_output).write_text(json.dumps(result, ensure_ascii=False, indent=2)+"\n", encoding="utf-8")
    print("CPF operator trust boundary PASS")
    print(json.dumps(result, ensure_ascii=False, sort_keys=True))
    return 0

if __name__ == "__main__":
    raise SystemExit(main())
