from __future__ import annotations

import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
EXECUTABLES = ("worker", "control-plane", "scheduler")


class BatchKafkaNamedProviderContractTest(unittest.TestCase):
    def test_kafka_starter_publishes_client_and_named_binding_auto_configurations(self) -> None:
        imports = (
            ROOT
            / "cpf-starters/messaging/kafka/src/main/resources/META-INF/spring"
            / "org.springframework.boot.autoconfigure.AutoConfiguration.imports"
        ).read_text(encoding="utf-8").splitlines()
        self.assertEqual(1, imports.count("com.cpf.messaging.kafka.CpfKafkaAutoConfiguration"))
        self.assertEqual(1, imports.count("com.cpf.messaging.kafka.CpfKafkaBindingAutoConfiguration"))

    def test_every_adapter_capable_batch_executable_defaults_to_no_kafka(self) -> None:
        for executable in EXECUTABLES:
            with self.subTest(executable=executable):
                root = ROOT / "cpf-batch" / executable
                build = (root / "build.gradle").read_text(encoding="utf-8")
                properties = (root / "src/main/resources/application.properties").read_text(encoding="utf-8")
                self.assertIn("project(':runtime:batch:remote-kafka')", build)
                self.assertIn(
                    "cpf.batch.remote.transport=${CPF_BATCH_REMOTE_TRANSPORT:disabled}",
                    properties,
                )
                self.assertIn(
                    "cpf.messaging.kafka.default-binding=${CPF_MESSAGING_KAFKA_DEFAULT_BINDING:false}",
                    properties,
                )
                self.assertIn(
                    "cpf.messaging.reliability.enabled=${CPF_MESSAGING_RELIABILITY_ENABLED:false}",
                    properties,
                )

    def test_center_cut_does_not_select_or_directly_package_kafka(self) -> None:
        root = ROOT / "cpf-batch/center-cut"
        build = (root / "build.gradle").read_text(encoding="utf-8")
        properties = (root / "src/main/resources/application.properties").read_text(encoding="utf-8")
        java = "\n".join(
            path.read_text(encoding="utf-8")
            for path in (root / "src/main/java").rglob("*.java")
        )
        for forbidden in (
            "project(':starters:messaging:kafka')",
            "project(':runtime:batch:remote-kafka')",
            "spring-integration-kafka",
        ):
            self.assertNotIn(forbidden, build)
        for forbidden in ("cpf.batch.remote.transport", "cpf.batch.remote.kafka", "cpf.messaging.kafka"):
            self.assertNotIn(forbidden, properties)
        for forbidden in ("org.springframework.kafka", "com.cpf.messaging.kafka", "Kafka"):
            self.assertNotIn(forbidden, java)


if __name__ == "__main__":
    unittest.main()
