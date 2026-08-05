#!/usr/bin/env python3
"""Fail-closed structural gate for CPF Batch Host Agent idempotency and UNKNOWN boundaries."""
from __future__ import annotations

import argparse
import json
from pathlib import Path
import sys

LEDGER = "cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/AgentCommandLedger.java"
CONTROLLER = "cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/AgentController.java"
SERVICE = "cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/internal/ServiceManager.java"
INSTALLER = "cpf-batch/host-agent/src/main/java/com/cpf/batch/agent/internal/ArtifactInstaller.java"

REQUIRED = {
    LEDGER: (
        "private static final ReentrantLock[] JVM_LOCKS",
        "FileChannel.open(",
        "FileLock ignored = channel.lock()",
        "channel.force(true)",
        "LinkOption.NOFOLLOW_LINKS",
        "COMMAND_EXECUTION_INTERRUPTED",
        "COMMAND_HANDLER_RESULT_UNKNOWN",
        "NON_TERMINAL_COMMAND_RESULT",
    ),
    SERVICE: (
        "try (ExecutorService ioExecutor = Executors.newVirtualThreadPerTaskExecutor())",
        "boolean terminated = terminateTree(process)",
        "PROCESS_TIMEOUT_TERMINATION_UNCONFIRMED",
        "return Result.unknown(\n                        -1,",
        "PROCESS_OUTPUT_DRAIN_TIMEOUT",
        "boolean unknownResult",
    ),
    INSTALLER: (
        "ARTIFACT_RELEASE_PATH_COLLISION",
        "ARTIFACT_INSTALL_PUBLICATION_ROLLED_BACK",
        "ARTIFACT_INSTALL_RESULT_UNKNOWN",
        "ARTIFACT_ROLLBACK_PUBLICATION_ROLLED_BACK",
        "ARTIFACT_ROLLBACK_RESULT_UNKNOWN",
        "restorePublication(",
        "publishConfig(configPath, previousConfigRef)",
        "LinkOption.NOFOLLOW_LINKS",
        "channel.force(true)",
    ),
    CONTROLLER: (
        "INSTALL_RESULT_UNKNOWN",
        "ROLLBACK_START_RESULT_UNKNOWN",
        "ROLLBACK_ARTIFACT_RESULT_UNKNOWN",
        "SERVICE_COMMAND_RESULT_UNKNOWN",
        "_RESPONSE_UNKNOWN",
        "COMMAND_ID_INVALID",
    ),
}

FORBIDDEN = {
    LEDGER: (
        "CommandState.FAILED,\n                        \"COMMAND_HANDLER_FAILED\"",
        "private final Map<String, Object> locks",
    ),
    SERVICE: (
        "Executors.newVirtualThreadPerTaskExecutor().submit",
        "return new Result(false,-1,\"TIMEOUT\")",
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

    ledger = sources.get(LEDGER, "")
    if ledger:
        try:
            execute = method_body(ledger, "public AgentCommandResult execute(")
            require_order(execute, "store(new Entry(fingerprint, executing));", "action.run(commandId, startedAt)",
                          f"{LEDGER}: execute()", errors)
            if "store(new Entry(fingerprint, interrupted));" not in execute:
                errors.append(f"{LEDGER}: interrupted execution must be durably normalized to UNKNOWN")
            if "CommandState.FAILED" in execute:
                errors.append(f"{LEDGER}: generic handler exception must not be collapsed to FAILED")
        except ValueError as failure:
            errors.append(f"{LEDGER}: {failure}")
        try:
            lock = method_body(ledger, "private <T> T withCommandLock(")
            require_order(lock, "FileLock ignored = channel.lock()", "return operation.run()",
                          f"{LEDGER}: withCommandLock()", errors)
        except ValueError as failure:
            errors.append(f"{LEDGER}: {failure}")

    service = sources.get(SERVICE, "")
    if service:
        try:
            execute = method_body(service, "public Result execute(")
            if "Result.unknown(" not in execute:
                errors.append(f"{SERVICE}: timeout/drain failures must return an unknown result")
            if "drain.cancel(true)" not in execute:
                errors.append(f"{SERVICE}: timeout path must cancel the output drain")
        except ValueError as failure:
            errors.append(f"{SERVICE}: {failure}")
        try:
            stopped = method_body(service, "public boolean stopped(")
            if "result.unknownResult()" not in stopped:
                errors.append(f"{SERVICE}: stopped() must reject an indeterminate status")
        except ValueError as failure:
            errors.append(f"{SERVICE}: {failure}")

    installer = sources.get(INSTALLER, "")
    if installer:
        try:
            install = method_body(installer, "public Result install(")
            require_order(install, "validate(request, service);", "download(artifactUri(request, extension)",
                          f"{INSTALLER}: install()", errors)
            require_order(install, "verifier.verifyStored(activeArtifact, current, service);", "move(part, target);",
                          f"{INSTALLER}: install()", errors)
            if "Exception restoreFailure = restorePublication(" not in install:
                errors.append(f"{INSTALLER}: install() must invoke publication compensation on failure")
            if "Files.deleteIfExists(target)" not in installer:
                errors.append(f"{INSTALLER}: publication compensation must remove the unpublished target")
        except ValueError as failure:
            errors.append(f"{INSTALLER}: {failure}")
        try:
            rollback = method_body(installer, "public String rollback(")
            for token in ("publishConfig(configPath, previousConfigRef)",
                          "stateStore.write(currentStatePath, previous)",
                          "stateStore.write(previousStatePath, current)",
                          "restoreFile(configPath, configExisted, currentConfig)"):
                if token not in rollback:
                    errors.append(f"{INSTALLER}: rollback compensation missing: {token}")
        except ValueError as failure:
            errors.append(f"{INSTALLER}: {failure}")

    controller = sources.get(CONTROLLER, "")
    if controller:
        method_contracts = {
            "private AgentCommandResult runtimeCommand(": ("CommandState.UNKNOWN_RESULT", "_RESPONSE_UNKNOWN"),
            "private AgentCommandResult serviceCommand(": ("commandResult.unknownResult()", "SERVICE_COMMAND_RESULT_UNKNOWN"),
            "AgentCommandResult install(": ("CommandState.UNKNOWN_RESULT", "INSTALL_RESULT_UNKNOWN"),
            "AgentCommandResult rollback(": ("RollbackPhase", "PARTIALLY_ROLLED_BACK", "STOP_RESULT_UNKNOWN"),
        }
        for signature, tokens in method_contracts.items():
            try:
                body = method_body(controller, signature)
                for token in tokens:
                    if token not in body:
                        errors.append(f"{CONTROLLER}: {signature} missing fail-closed token: {token}")
            except ValueError as failure:
                errors.append(f"{CONTROLLER}: {failure}")

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
        print(f"[FAIL] CPF Batch Host Agent fail-closed contract\n{exc}", file=sys.stderr)
        return 1
    if args.json_output:
        output = Path(args.json_output)
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8")
    print(f"[PASS] CPF Batch Host Agent fail-closed contract sources={result['verifiedSources']} tokens={result['requiredTokens']}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
