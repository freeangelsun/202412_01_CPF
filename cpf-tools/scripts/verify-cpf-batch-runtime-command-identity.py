#!/usr/bin/env python3
"""Fail-closed structural gate for CPF Batch runtime-command canonical identity."""
from __future__ import annotations

import argparse
import json
from pathlib import Path
import sys

IDENTITY = "cpf-batch/control-server/src/main/java/com/cpf/batch/control/RuntimeCommandIdentity.java"
EXECUTOR = "cpf-batch/control-server/src/main/java/com/cpf/batch/control/RuntimeCommandExecutor.java"
REPOSITORY = "cpf-batch/control-server/src/main/java/com/cpf/batch/control/internal/JdbcRuntimeCommandRepository.java"
ADMIN = "cpf-admin/src/main/java/com/cpf/admin/opr/batch/runtime/BatchRuntimeControlController.java"
CONTRACT = "cpf-batch/contract/src/main/java/com/cpf/batch/api/RuntimeCommand.java"

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
        "SERVER_OWNED_COMMAND_FIELDS",
        'requireCommandField(request, "targetType")',
        'requireCommandField(request, "approvalPolicyVersion")',
        "requireExpectedVersion(request)",
        "requireFutureExpiry(request)",
        '"executionState"',
        '"targetSnapshotHash"',
        '"evidenceRef"',
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
            require_order(
                normalize,
                "canonicalTargetSnapshot(targets)",
                "return new RuntimeCommand(",
                f"{IDENTITY}: normalize()",
                errors,
            )
            require_order(
                normalize,
                "requireCompatibleOptional(",
                "return new RuntimeCommand(",
                f"{IDENTITY}: normalize()",
                errors,
            )
            if "command.parameters() != null && !command.parameters().isEmpty()" not in normalize:
                errors.append(f"{IDENTITY}: unpersisted parameters must be rejected")
            for forbidden in (
                "command.executionState()",
                "command.executionAttempt()",
                "command.result()",
                "command.failureStage()",
                "command.beforeState()",
                "command.afterState()",
                "command.evidenceRef()",
            ):
                if forbidden in normalize:
                    errors.append(f"{IDENTITY}: client-owned server state is preserved: {forbidden}")
            if "CommandState.APPROVED" not in normalize:
                errors.append(f"{IDENTITY}: command must be initialized to APPROVED")
        except ValueError as failure:
            errors.append(f"{IDENTITY}: {failure}")
        try:
            assert_matches = method_body(identity, "static void assertMatches(")
            for field in (
                "command_id",
                "idempotency_key",
                "command_type",
                "target_type",
                "target_snapshot",
                "target_snapshot_hash",
                "expected_version",
                "requested_by",
                "reason_text",
                "approval_policy_version",
                "approval_request_id",
                "approved_by",
                "expires_at",
                "transaction_id",
            ):
                if f'"{field}"' not in assert_matches:
                    errors.append(f"{IDENTITY}: persisted identity comparison missing {field}")
        except ValueError as failure:
            errors.append(f"{IDENTITY}: {failure}")

    admin = sources.get(ADMIN, "")
    if admin:
        try:
            command = method_body(admin, "ResponseEntity<Map<String, Object>> command(")
            for token in (
                'requireCommandField(request, "targetType")',
                'requireCommandField(request, "approvalPolicyVersion")',
                "requireExpectedVersion(request)",
                "requireFutureExpiry(request)",
            ):
                if token not in command:
                    errors.append(f"{ADMIN}: command() missing fail-closed validation: {token}")
            require_order(
                command,
                "requireFutureExpiry(request)",
                "client.command(withServerActor(request, operatorId))",
                f"{ADMIN}: command()",
                errors,
            )
            sanitize = method_body(admin, "private static Map<String, Object> sanitizeCommandMap(")
            if "SERVER_OWNED_COMMAND_FIELDS.contains(key)" not in sanitize:
                errors.append(f"{ADMIN}: browser-controlled server fields are not stripped")
        except ValueError as failure:
            errors.append(f"{ADMIN}: {failure}")

    repository = sources.get(REPOSITORY, "")
    if repository:
        try:
            create = method_body(repository, "public Map<String,Object> create(")
            if "@Transactional" in repository:
                errors.append(f"{REPOSITORY}: duplicate replay must not query in an aborted transaction")
            require_order(
                create,
                "catch (DuplicateKeyException duplicate)",
                "find(c.idempotencyKey()).orElseThrow",
                f"{REPOSITORY}: create()",
                errors,
            )
            if "RuntimeCommandIdempotencyConflictException" not in create:
                errors.append(f"{REPOSITORY}: commandId collision must use stable conflict classification")
        except ValueError as failure:
            errors.append(f"{REPOSITORY}: {failure}")

    executor = sources.get(EXECUTOR, "")
    if executor:
        try:
            execute = method_body(executor, "public Map<String, Object> execute(")
            require_order(
                execute,
                "RuntimeCommandIdentity.normalize(command)",
                "persisted = commands.create(normalized)",
                f"{EXECUTOR}: execute()",
                errors,
            )
            require_order(
                execute,
                "RuntimeCommandIdentity.assertMatches(normalized, persisted)",
                "commands.beginExecution(command.commandId())",
                f"{EXECUTOR}: execute()",
                errors,
            )
            require_order(
                execute,
                "RuntimeCommandIdentity.assertMatches(normalized, persisted)",
                "for (String target : command.targetIds())",
                f"{EXECUTOR}: execute()",
                errors,
            )
        except ValueError as failure:
            errors.append(f"{EXECUTOR}: {failure}")

    if errors:
        raise ValueError("\n".join(errors))
    return {
        "status": "PASS",
        "verifiedSources": len(REQUIRED),
        "requiredTokens": sum(len(tokens) for tokens in REQUIRED.values()),
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--json-output")
    args = parser.parse_args()
    try:
        result = verify(Path(args.root).resolve())
    except ValueError as exc:
        print(f"[FAIL] CPF Batch runtime-command identity contract\n{exc}", file=sys.stderr)
        return 1
    if args.json_output:
        output = Path(args.json_output)
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8")
    print(
        "[PASS] CPF Batch runtime-command identity contract "
        f"sources={result['verifiedSources']} tokens={result['requiredTokens']}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
