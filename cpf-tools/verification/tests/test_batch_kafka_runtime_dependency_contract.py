from __future__ import annotations

import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]


class BatchKafkaRuntimeDependencyContractTest(unittest.TestCase):
    def test_optional_kafka_transport_packages_adapter_but_is_not_the_default(self) -> None:
        runtime_build = (ROOT / "cpf-batch/runtime/build.gradle").read_text(encoding="utf-8")
        adapter_build = (ROOT / "cpf-batch/remote-kafka/build.gradle").read_text(encoding="utf-8")
        self.assertNotIn("spring-integration-kafka", runtime_build)
        self.assertNotIn("spring-boot-starter-kafka", runtime_build)
        self.assertIn(
            "implementation 'org.springframework.boot:spring-boot-starter-kafka'",
            adapter_build,
        )
        for executable in ("worker", "control-plane", "scheduler"):
            properties = ROOT / f"cpf-batch/{executable}/src/main/resources/application.properties"
            self.assertTrue(properties.is_file(), str(properties))
            self.assertIn(
                "cpf.batch.remote.transport=${CPF_BATCH_REMOTE_TRANSPORT:disabled}",
                properties.read_text(encoding="utf-8"),
                executable,
            )

        center_cut_properties = (
            ROOT / "cpf-batch/center-cut/src/main/resources/application.properties"
        ).read_text(encoding="utf-8")
        self.assertNotIn("cpf.batch.remote.transport", center_cut_properties)
        self.assertNotIn("cpf.batch.remote.kafka", center_cut_properties)


if __name__ == "__main__":
    unittest.main()
