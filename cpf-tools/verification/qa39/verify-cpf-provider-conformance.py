#!/usr/bin/env python3
from __future__ import annotations

import re
import shutil
import subprocess
import tempfile
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
PUBLIC_SOURCES = [
    ROOT / "cpf-core/src/main/java/com/cpf/core/api/broker/CpfBrokerClient.java",
    ROOT / "cpf-core/src/main/java/com/cpf/core/api/broker/CpfBrokerPublishRequest.java",
    ROOT / "cpf-core/src/main/java/com/cpf/core/api/broker/CpfBrokerPublishResult.java",
    ROOT / "cpf-starters/notification/src/main/java/com/cpf/starter/notification/CpfNotificationProvider.java",
    ROOT / "cpf-starters/notification/src/main/java/com/cpf/starter/notification/CpfNotificationProviderStatus.java",
    ROOT / "cpf-starters/notification/src/main/java/com/cpf/starter/notification/CpfNotificationRequest.java",
    ROOT / "cpf-starters/notification/src/main/java/com/cpf/starter/notification/CpfNotificationResult.java",
]
FORBIDDEN_PUBLIC_IMPORT = re.compile(
    r"(?m)^\s*import\s+(?:com\.cpf\.(?:core\.common|starter\.[^.]+\.internal)|"
    r"org\.springframework\.(?:kafka|amqp|jms|jdbc)|org\.apache\.kafka|jakarta\.jms)\."
)


def fail(message: str) -> None:
    raise SystemExit(f"[CPF][PROVIDER-CONFORMANCE][FAIL] {message}")


def main() -> None:
    missing = [path for path in PUBLIC_SOURCES if not path.is_file()]
    if missing:
        fail("public SPI sources missing: " + ", ".join(str(path.relative_to(ROOT)) for path in missing))
    for source in PUBLIC_SOURCES:
        content = source.read_text(encoding="utf-8")
        if FORBIDDEN_PUBLIC_IMPORT.search(content):
            fail(f"OSS/internal type leaked into public SPI: {source.relative_to(ROOT)}")

    javac = shutil.which("javac")
    java = shutil.which("java")
    if not javac or not java:
        fail("javac/java is required")

    fixture = r'''
package com.customer.extension;

import com.cpf.core.api.broker.CpfBrokerClient;
import com.cpf.core.api.broker.CpfBrokerPublishRequest;
import com.cpf.core.api.broker.CpfBrokerPublishResult;
import com.cpf.starter.notification.CpfNotificationProvider;
import com.cpf.starter.notification.CpfNotificationProviderStatus;
import com.cpf.starter.notification.CpfNotificationRequest;
import com.cpf.starter.notification.CpfNotificationResult;
import java.time.Instant;
import java.util.Map;

public final class CustomerProviderConformanceFixture {
    private CustomerProviderConformanceFixture() {}

    static final class CustomerBrokerProvider implements CpfBrokerClient {
        @Override
        public CpfBrokerPublishResult enqueue(CpfBrokerPublishRequest request) {
            return new CpfBrokerPublishResult(
                    "ENQUEUED", request.messageId(), "customer-broker", request.key(),
                    Instant.parse("2026-08-02T00:00:00Z"), "accepted");
        }
    }

    static final class CustomerNotificationProvider implements CpfNotificationProvider {
        @Override
        public String channel() {
            return "CUSTOM";
        }

        @Override
        public CpfNotificationResult send(CpfNotificationRequest request) {
            return CpfNotificationResult.sentAt(
                    request.notificationId(), "customer-notification", "receipt-1",
                    Instant.parse("2026-08-02T00:00:00Z"));
        }

        @Override
        public CpfNotificationProviderStatus health() {
            return CpfNotificationProviderStatus.up();
        }
    }

    public static void main(String[] args) {
        CpfBrokerPublishRequest brokerRequest = new CpfBrokerPublishRequest(
                "message-1", "topic-1", null, new byte[]{1}, "application/octet-stream",
                "transaction-1", "segment-1", "CUSTOMER", "TARGET", "idem-1",
                Map.of("correlationId", "correlation-1"), Map.of());
        CpfBrokerPublishResult brokerResult = new CustomerBrokerProvider().enqueue(brokerRequest);
        if (!"ENQUEUED".equals(brokerResult.status())) {
            throw new IllegalStateException("broker conformance failed");
        }

        CpfNotificationRequest notificationRequest = new CpfNotificationRequest(
                "notification-1", "CUSTOM", "masked-recipient", "template-1", Map.of(),
                "idem-2", "transaction-1", null);
        CustomerNotificationProvider provider = new CustomerNotificationProvider();
        CpfNotificationResult notificationResult = provider.send(notificationRequest);
        if (!"SENT".equals(notificationResult.status())
                || !"UP".equals(provider.health().status())) {
            throw new IllegalStateException("notification conformance failed");
        }
        System.out.println("[CPF][PROVIDER-CONFORMANCE][PASS] broker+notification customer SPI");
    }
}
'''

    with tempfile.TemporaryDirectory(prefix="cpf-provider-conformance-") as temp:
        temp_root = Path(temp)
        source_root = temp_root / "src"
        classes = temp_root / "classes"
        fixture_path = source_root / "com/customer/extension/CustomerProviderConformanceFixture.java"
        fixture_path.parent.mkdir(parents=True, exist_ok=True)
        fixture_path.write_text(fixture, encoding="utf-8")
        classes.mkdir(parents=True, exist_ok=True)
        command = [javac, "-encoding", "UTF-8", "-d", str(classes)]
        command.extend(str(path) for path in PUBLIC_SOURCES)
        command.append(str(fixture_path))
        compiled = subprocess.run(command, text=True, capture_output=True)
        if compiled.returncode != 0:
            fail("customer fixture compile failed:\n" + compiled.stdout + compiled.stderr)
        executed = subprocess.run(
            [java, "-cp", str(classes), "com.customer.extension.CustomerProviderConformanceFixture"],
            text=True,
            capture_output=True,
        )
        if executed.returncode != 0:
            fail("customer fixture runtime failed:\n" + executed.stdout + executed.stderr)
        print(executed.stdout.strip())


if __name__ == "__main__":
    main()
