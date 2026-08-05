#!/usr/bin/env python3
"""Fail-closed structural gate for CPF Batch ownership, fencing and partial-failure boundaries."""
from __future__ import annotations

import argparse
import json
from pathlib import Path
import sys

CHECKS = {
    "cpf-batch/scheduler/src/main/java/com/cpf/batch/scheduler/SchedulerCoordinator.java": (
        "AtomicBoolean electionInProgress",
        "JdbcSchedulerLeaderRepository.Lease current = lease.get();",
        "if (!electionInProgress.compareAndSet(false, true)) {",
    ),
    "cpf-batch/worker/src/main/java/com/cpf/batch/worker/ApprovedFileExecutor.java": (
        ".cpf-claim.lock",
        "FileChannel.open",
        "BATCH_FILE_CLAIM_FENCE_CONFLICT",
        "writeFenceToken(channel, token)",
    ),
    "cpf-batch/worker/src/main/java/com/cpf/batch/worker/ApprovedShellExecutor.java": (
        "Thread stdinWriter",
        "terminateProcessTree",
        "BATCH_SHELL_STDIN_INCOMPLETE",
    ),
    "cpf-batch/center-cut-runner/src/main/java/com/cpf/batch/centercut/runner/CenterCutTargetGenerator.java": (
        "BATCH_CENTER_CUT_PROVIDER_PAGE_LIMIT_EXCEEDED",
        "BATCH_CENTER_CUT_DUPLICATE_BUSINESS_KEY",
        "BATCH_CENTER_CUT_NON_ADVANCING_CURSOR",
    ),
    "cpf-batch/center-cut-runner/src/main/java/com/cpf/batch/centercut/runner/internal/JdbcCenterCutClaimRepository.java": (
        "TransactionTemplate",
        "status.setRollbackOnly()",
        "claimWithinTransaction(",
    ),
    "cpf-batch/execution-runtime/src/main/java/com/cpf/batch/execution/CpfSpringBatchExecutionControl.java": (
        "fencing.assertCurrent(jobId, cpfExecutionId, fencingToken);",
        "BATCH_RECOVER_RESPONSE_UNKNOWN",
        "operator.recover(previous)",
    ),
    "cpf-batch/execution-runtime/src/main/java/com/cpf/batch/execution/CpfBatchKafkaInboundBridge.java": (
        "String attemptOwnerId=attemptOwnerId();",
        "UUID.randomUUID()",
        "ledger.complete(direction,envelope.messageId(),attemptOwnerId)",
    ),
}

FORBIDDEN = {
    "cpf-batch/execution-runtime/src/main/java/com/cpf/batch/execution/CpfBatchKafkaInboundBridge.java": (
        "ledger.complete(direction,envelope.messageId(),ownerId)",
        "ledger.fail(direction,envelope.messageId(),ownerId",
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


def verify(root: Path) -> dict[str, object]:
    errors: list[str] = []
    verified = 0
    for rel, tokens in CHECKS.items():
        path = root / rel
        if not path.is_file():
            errors.append(f"{rel}: source missing")
            continue
        text = path.read_text(encoding="utf-8")
        for token in tokens:
            if token not in text:
                errors.append(f"{rel}: required token missing: {token}")
        for token in FORBIDDEN.get(rel, ()):
            if token in text:
                errors.append(f"{rel}: stale-owner transition reintroduced: {token}")

        if rel.endswith("CpfSpringBatchExecutionControl.java"):
            try:
                recover = method_body(text, "public BatchExecutionLink recover(")
                for token in (
                        "fencing.assertCurrent(jobId, cpfExecutionId, fencingToken);",
                        "operator.recover(previous)",
                        "ledger.recordUnknown(cpfExecutionId, \"BATCH_RECOVER_RESPONSE_UNKNOWN\""):
                    if token not in recover:
                        errors.append(f"{rel}: recover() contract missing: {token}")
                if recover.find("fencing.assertCurrent") > recover.find("operator.recover(previous)"):
                    errors.append(f"{rel}: recover() fencing must precede operator recovery")
            except ValueError as failure:
                errors.append(f"{rel}: {failure}")

        if rel.endswith("CpfBatchKafkaInboundBridge.java"):
            try:
                accept = method_body(text, "private boolean accept(")
                for token in (
                        "String attemptOwnerId=attemptOwnerId();",
                        "ledger.claim(direction,envelope.messageId(),envelope.payloadSha256(),envelope.expiresAt(),attemptOwnerId)",
                        "ledger.complete(direction,envelope.messageId(),attemptOwnerId)",
                        "ledger.fail(direction,envelope.messageId(),attemptOwnerId"):
                    if token not in accept:
                        errors.append(f"{rel}: accept() attempt fencing missing: {token}")
            except ValueError as failure:
                errors.append(f"{rel}: {failure}")
        verified += 1
    if errors:
        raise ValueError("\n".join(errors))
    return {"status": "PASS", "verifiedSources": verified, "checks": sum(map(len, CHECKS.values()))}


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--json-output")
    args = parser.parse_args()
    try:
        result = verify(Path(args.root).resolve())
    except ValueError as exc:
        print(f"[FAIL] CPF Batch execution fencing contract\n{exc}", file=sys.stderr)
        return 1
    if args.json_output:
        output = Path(args.json_output)
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8")
    print(f"[PASS] CPF Batch execution fencing contract sources={result['verifiedSources']} checks={result['checks']}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
