from __future__ import annotations

import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[4]
INSTALLER = ROOT / "cpf-tools/environment/docker-development-test/CPF_도커_개발테스트환경_전체설치.ps1"
COMPOSE = ROOT / "cpf-tools/environment/docker-development-test/compose.yml"


class DockerInstallerConfigSyncTest(unittest.TestCase):
    def test_base_runtime_files_are_refreshed_from_current_workspace(self) -> None:
        source = INSTALLER.read_text(encoding="utf-8-sig")
        block = source.split("foreach ($name in $baseRuntimeFiles)", 1)[1].split("$ownedFiles", 1)[0]
        self.assertIn("Copy-Item -LiteralPath $source -Destination $destination -Force", block)
        self.assertNotIn("Test-Path -LiteralPath $destination", block)
        self.assertIn("Secrets and persistent volumes", block)

    def test_oracle_uses_the_image_owned_healthcheck(self) -> None:
        source = COMPOSE.read_text(encoding="utf-8-sig")
        oracle = source.split("  oracle:", 1)[1].split("\nvolumes:", 1)[0]
        self.assertNotIn("healthcheck:", oracle)
        self.assertIn("이미지가 자체 정의한 official Healthcheck", oracle)


if __name__ == "__main__":
    unittest.main()
