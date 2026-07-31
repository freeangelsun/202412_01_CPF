#!/usr/bin/env python3
from __future__ import annotations

import argparse
from pathlib import Path


def main() -> int:
    parser = argparse.ArgumentParser(description="Verify the QA34 batch protocol outbound security contract.")
    parser.add_argument("--root", default=".")
    args = parser.parse_args()
    root = Path(args.root).resolve()

    files = {
        "registry": root / "cpf-batch/worker/src/main/java/com/cpf/batch/worker/BatchRuntimeExecutorRegistry.java",
        "policy": root / "cpf-batch/worker/src/main/java/com/cpf/batch/worker/BatchOutboundHttpPolicy.java",
        "transport": root / "cpf-batch/worker/src/main/java/com/cpf/batch/worker/PinnedBatchHttpTransport.java",
        "properties": root / "cpf-batch/worker/src/main/java/com/cpf/batch/worker/WorkerOperationalProperties.java",
        "test": root / "cpf-batch/worker/src/test/java/com/cpf/batch/worker/BatchOutboundHttpPolicyTest.java",
    }
    missing = [str(path.relative_to(root)) for path in files.values() if not path.is_file()]
    if missing:
        raise SystemExit("missing: " + ", ".join(missing))

    text = {name: path.read_text(encoding="utf-8-sig") for name, path in files.items()}
    checks = [
        ("disabled-default", "private boolean enabled;", text["properties"]),
        ("dns-pin", "BATCH_OUTBOUND_DNS_PIN_REQUIRED", text["policy"]),
        ("mixed-dns-deny", "BATCH_OUTBOUND_MIXED_DNS_RESPONSE_DENIED", text["policy"]),
        ("metadata-deny", "BATCH_OUTBOUND_METADATA_ADDRESS_DENIED", text["policy"]),
        ("cidr-allowlist", "allowedCidrs", text["properties"]),
        ("pinned-connect", "target.address()", text["transport"]),
        ("sni", "SNIHostName(target.host())", text["transport"]),
        ("redirect-deny", "BATCH_OUTBOUND_REDIRECT_DENIED", text["transport"]),
        ("request-cap", "BATCH_OUTBOUND_REQUEST_SIZE_EXCEEDED", text["policy"]),
        ("response-cap", "BATCH_OUTBOUND_RESPONSE_SIZE_EXCEEDED", text["transport"]),
        ("header-injection-deny", "BATCH_OUTBOUND_HEADER_INJECTION_DENIED", text["registry"]),
        ("idempotency", "X-Cpf-Idempotency-Key", text["registry"]),
        ("reconcile", "X-Cpf-Reconcile-Key", text["registry"]),
        ("unknown-result", "PROTOCOL_TIMEOUT_UNKNOWN", text["registry"]),
        ("negative-test", "dnsPinMismatchAndMetadataAddressFailClosed", text["test"]),
    ]
    failed = [name for name, token, body in checks if token not in body]
    legacy = ["HttpRequest.newBuilder(uri)", "BodyHandlers.ofString"]
    protocol_offset = text["registry"].find("private ExecutionResult executeProtocol")
    protocol_body = text["registry"][protocol_offset:] if protocol_offset >= 0 else text["registry"]
    failed += ["legacy:" + token for token in legacy if token in protocol_body]
    if failed:
        raise SystemExit("batch outbound contract failed: " + ", ".join(failed))
    print("CPF batch outbound policy: PASS")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
