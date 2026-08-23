from __future__ import annotations

import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[3]


class BatchKafkaRuntimeDependencyContractTest(unittest.TestCase):
    def test_default_kafka_transport_packages_boot_infrastructure_owner(self) -> None:
        runtime_build = (ROOT / "cpf-batch/runtime/build.gradle").read_text(encoding="utf-8")
        self.assertIn(
            "implementation 'org.springframework.boot:spring-boot-starter-kafka'",
            runtime_build,
        )
        for executable in ("worker", "control-plane", "scheduler", "center-cut"):
            properties = ROOT / f"cpf-batch/{executable}/src/main/resources/application.properties"
            self.assertTrue(properties.is_file(), str(properties))
            self.assertIn(
                "cpf.batch.remote.transport=kafka",
                properties.read_text(encoding="utf-8"),
                executable,
            )


if __name__ == "__main__":
    unittest.main()
