from __future__ import annotations

import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]
EXECUTABLES = ("worker", "control-plane", "scheduler", "center-cut")


class BatchKafkaNamedProviderContractTest(unittest.TestCase):
    def test_kafka_starter_publishes_client_and_named_binding_auto_configurations(self) -> None:
        imports = (
            ROOT
            / "cpf-starters/messaging/kafka/src/main/resources/META-INF/spring"
            / "org.springframework.boot.autoconfigure.AutoConfiguration.imports"
        ).read_text(encoding="utf-8").splitlines()
        self.assertEqual(1, imports.count("com.cpf.messaging.kafka.CpfKafkaAutoConfiguration"))
        self.assertEqual(1, imports.count("com.cpf.messaging.kafka.CpfKafkaBindingAutoConfiguration"))

    def test_every_kafka_batch_executable_packages_and_selects_the_provider(self) -> None:
        for executable in EXECUTABLES:
            with self.subTest(executable=executable):
                root = ROOT / "cpf-batch" / executable
                build = (root / "build.gradle").read_text(encoding="utf-8")
                properties = (root / "src/main/resources/application.properties").read_text(encoding="utf-8")
                self.assertIn("project(':starters:messaging:kafka')", build)
                self.assertIn("cpf.batch.remote.transport=kafka", properties)
                self.assertIn(
                    "cpf.messaging.kafka.default-binding=${CPF_MESSAGING_KAFKA_DEFAULT_BINDING:true}",
                    properties,
                )


if __name__ == "__main__":
    unittest.main()
