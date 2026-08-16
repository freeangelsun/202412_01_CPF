#!/usr/bin/env python3
"""Fail-closed structural gate for CPF Batch runtime-command canonical identity and approval boundary."""
from __future__ import annotations

import argparse
import json
from pathlib import Path
import re

IDENTITY = "cpf-batch/control-plane/src/main/java/com/cpf/batch/control/RuntimeCommandIdentity.java"
EXECUTOR = "cpf-batch/control-plane/src/main/java/com/cpf/batch/control/RuntimeCommandExecutor.java"
REPOSITORY = "cpf-batch/control-plane/src/main/java/com/cpf/batch/control/internal/JdbcRuntimeCommandRepository.java"
ADMIN = "cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java"
REQUEST = "cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeCommandRequest.java"
APPROVAL = "cpf-admin/src/main/java/com/cpf/admin/approval/service/AdmApprovalService.java"
CONTRACT = "cpf-batch/api/src/main/java/com/cpf/batch/api/RuntimeCommand.java"

REQUIRED = {
    IDENTITY: (
        "BATCH_RUNTIME_COMMAND_PARAMETERS_NOT_PERSISTED",
        "canonicalTargetSnapshot(targets)",
        "sha256(snapshot)",
        "equalsIgnoreCase(key)",
        "BATCH_RUNTIME_COMMAND_PERSISTED_IDENTITY_INCOMPLETE",
        "BATCH_RUNTIME_COMMAND_IDEMPOTENCY_CONFLICT",
        'compareString(persisted, "target_snapshot"',
        'compareString(persisted, "approval_request_id"',
        'compareInstant(persisted, "expires_at"',
        "CommandState.APPROVED",
    ),
    EXECUTOR: (
        "RuntimeCommand normalized = RuntimeCommandIdentity.normalize(command);",
        "persisted = commands.create(normalized);",
        "catch (RuntimeCommandIdempotencyConflictException conflict)",
        "BATCH_RUNTIME_COMMAND_PERSISTENCE_UNKNOWN",
        "RuntimeCommandIdentity.assertMatches(normalized, persisted);",
        "command = normalized;",
        "Approved command expiry is required",
    ),
    REPOSITORY: (
        "catch (DuplicateKeyException duplicate)",
        "find(c.idempotencyKey()).orElseThrow",
        "RuntimeCommandIdempotencyConflictException",
        "Runtime command insert completed but persisted identity is unavailable",
    ),
    ADMIN: (
        '@RequestAttribute("adm.operatorId") String operatorId',
        "approvalRequestId is required",
        "Long.parseLong(body.approvalRequestId.trim())",
        "approvalService.execute(approvalRequestId, body.reason, operatorId)",
        "approvalRequestId must be numeric",
    ),
    REQUEST: (
        "public String approvalRequestId;",
        "public String reason;",
        "실제 target/command/actor는 승인 Snapshot",
    ),
    APPROVAL: (
        'if(!"APPROVED".equals(string(doc,"approvalStatus")))',
        "verifySnapshotOrAudit(id,doc,operator,\"APPROVED\")",
        "repository.reserveExecution(id,approvedVersion,commandRequestId,operator)",
        "repository.findReservedExecutionCommand(id,commandRequestId)",
        "snapshotIntegrity.verify(reserved)",
        "port.execute(approvedCommand(id,commandRequestId,reserved,operator,executionReason))",
        "result=new AdmApprovedOperationResult(AdmApprovalExecutionStatus.UNKNOWN",
        "port.reconcile(approvedCommand(id,commandRequestId,reserved,operator,reconciliationReason))",
    ),
    CONTRACT: (
        "expectedVersion < 0",
        "executionAttempt < 0",
    ),
}

FORBIDDEN = {
    EXECUTOR: (
        'persisted.get("command_id")',
        "if (!command.commandId().equals",
    ),
    REPOSITORY: (
        "@Transactional",
        "find(c.idempotencyKey()).orElseThrow();",
    ),
    REQUEST: (
        "targetType",
        "targetIds",
        "commandType",
        "expectedVersion",
        "approvedBy",
        "approvalPolicyVersion",
        "expiresAt",
        "requestedBy",
        "operatorId",
    ),
}


def method_body(text: str, signature: str) -> str:
    start = text.find(signature)
    if start < 0:
        raise ValueError(f"method signature missing: {signature}")
    brace = text.find("{", start)
    if brace < 0:
        raise ValueError(f"method body missing: {signature}")
    depth = 0
    for index in range(brace, len(text)):
        char = text[index]
        if char == "{":
            depth += 1
        elif char == "}":
            depth -= 1
            if depth == 0:
                return text[brace + 1:index]
    raise ValueError(f"unterminated method body: {signature}")


def require_order(body: str, first: str, second: str, label: str, errors: list[str]) -> None:
    first_at = body.find(first)
    second_at = body.find(second)
    if first_at < 0:
        errors.append(f"{label}: missing {first}")
    if second_at < 0:
        errors.append(f"{label}: missing {second}")
    if first_at >= 0 and second_at >= 0 and first_at > second_at:
        errors.append(f"{label}: {first} must precede {second}")


def verify(root: Path) -> dict[str, object]:
    errors: list[str] = []
    sources: dict[str, str] = {}
    for rel, tokens in REQUIRED.items():
        path = root / rel
        if not path.is_file():
            errors.append(f"{rel}: source missing")
            continue
        text = path.read_text(encoding="utf-8")
        sources[rel] = text
        for token in tokens:
            if token not in text:
                errors.append(f"{rel}: required token missing: {token}")
        for token in FORBIDDEN.get(rel, ()):
            if token in text:
                errors.append(f"{rel}: forbidden fail-open pattern present: {token}")

    identity = sources.get(IDENTITY, "")
    if identity:
        try:
            normalize = method_body(identity, "static RuntimeCommand normalize(")
            require_order(normalize, "canonicalTargetSnapshot(targets)", "return new RuntimeCommand(", f"{IDENTITY}: normalize()", errors)
            require_order(normalize, "requireCompatibleOptional(", "return new RuntimeCommand(", f"{IDENTITY}: normalize()", errors)
            if "command.parameters() != null && !command.parameters().isEmpty()" not in normalize:
                errors.append(f"{IDENTITY}: unpersisted parameters must be rejected")
            for forbidden in (
                "command.executionState()", "command.executionAttempt()", "command.result()",
                "command.failureStage()", "command.beforeState()", "command.afterState()", "command.evidenceRef()",
            ):
                if forbidden in normalize:
                    errors.append(f"{IDENTITY}: client-owned server state is preserved: {forbidden}")
        except ValueError as failure:
            errors.append(f"{IDENTITY}: {failure}")

    admin = sources.get(ADMIN, "")
    if admin:
        try:
            command = method_body(admin, "ResponseEntity<Map<String, Object>> command(")
            for token in (
                "body.approvalRequestId", "Long.parseLong(body.approvalRequestId.trim())",
                "approvalService.execute(approvalRequestId, body.reason, operatorId)",
            ):
                if token not in command:
                    errors.append(f"{ADMIN}: command() missing approval-bound execution: {token}")
            for forbidden in (
                "client.command(", "targetType", "targetIds", "expectedVersion", "approvedBy", "expiresAt",
            ):
                if forbidden in command:
                    errors.append(f"{ADMIN}: browser command endpoint controls owner command field: {forbidden}")
            require_order(command, "Long.parseLong(body.approvalRequestId.trim())", "approvalService.execute(approvalRequestId, body.reason, operatorId)", f"{ADMIN}: command()", errors)
        except ValueError as failure:
            errors.append(f"{ADMIN}: {failure}")

    approval = sources.get(APPROVAL, "")
    if approval:
        try:
            execute = method_body(approval, "public Map<String,Object> execute(")
            require_order(execute, '"APPROVED".equals(string(doc,"approvalStatus"))', "repository.reserveExecution", f"{APPROVAL}: execute()", errors)
            require_order(execute, "snapshotIntegrity.verify(reserved)", "port.execute(approvedCommand", f"{APPROVAL}: execute()", errors)
            if "port.reconcile(" in execute:
                errors.append(f"{APPROVAL}: execute() must not reconcile/replay owner mutation")
            reconcile = method_body(approval, "public Map<String,Object> reconcile(")
            if "port.execute(" in reconcile:
                errors.append(f"{APPROVAL}: reconcile() must observe through owner reconcile, not replay execute")
            if "port.reconcile(approvedCommand" not in reconcile:
                errors.append(f"{APPROVAL}: reconcile() owner observation path missing")
        except ValueError as failure:
            errors.append(f"{APPROVAL}: {failure}")

    repository = sources.get(REPOSITORY, "")
    if repository:
        try:
            create = method_body(repository, "public Map<String,Object> create(")
            require_order(create, "catch (DuplicateKeyException duplicate)", "find(c.idempotencyKey()).orElseThrow", f"{REPOSITORY}: create()", errors)
        except ValueError as failure:
            errors.append(f"{REPOSITORY}: {failure}")

    executor = sources.get(EXECUTOR, "")
    if executor:
        # The executor is deliberately package-private: only the validated BAT controller path can dispatch it.
        if re.search(r"\bpublic\s+Map<String,\s*Object>\s+execute\s*\(", executor):
            errors.append(f"{EXECUTOR}: execute() must not be public")
        try:
            execute = method_body(executor, "Map<String, Object> execute(")
            require_order(execute, "RuntimeCommandIdentity.normalize(command)", "persisted = commands.create(normalized)", f"{EXECUTOR}: execute()", errors)
            require_order(execute, "RuntimeCommandIdentity.assertMatches(normalized, persisted)", "commands.beginExecution(command.commandId())", f"{EXECUTOR}: execute()", errors)
            require_order(execute, "RuntimeCommandIdentity.assertMatches(normalized, persisted)", "for (String target : command.targetIds())", f"{EXECUTOR}: execute()", errors)
        except ValueError as failure:
            errors.append(f"{EXECUTOR}: {failure}")

    if errors:
        raise ValueError("\n".join(errors))
    return {
        "status": "PASS",
        "verifiedSources": len(REQUIRED),
        "requiredTokens": sum(len(tokens) for tokens in REQUIRED.values()),
        "approvalBoundary": "BROWSER_APPROVAL_ID_ONLY",
        "reconcilePolicy": "NO_MUTATION_REPLAY",
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path.cwd())
    parser.add_argument("--json-output", type=Path)
    args = parser.parse_args()
    try:
        result = verify(args.root.resolve())
    except ValueError as failure:
        result = {"status": "FAIL", "error": str(failure)}
        code = 1
    else:
        code = 0
    rendered = json.dumps(result, ensure_ascii=False, indent=2)
    print(rendered)
    if args.json_output:
        args.json_output.parent.mkdir(parents=True, exist_ok=True)
        args.json_output.write_text(rendered + "\n", encoding="utf-8")
    return code


if __name__ == "__main__":
    raise SystemExit(main())
