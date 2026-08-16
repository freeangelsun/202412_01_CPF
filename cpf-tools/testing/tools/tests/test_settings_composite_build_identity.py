from __future__ import annotations

import re
import unittest
from pathlib import Path


ROOT = Path(__file__).resolve().parents[4]


class SettingsCompositeBuildIdentityTest(unittest.TestCase):
    def test_physical_composite_builds_preserve_canonical_root_build_identity(self):
        settings = (ROOT / "settings.gradle").read_text(encoding="utf-8-sig")
        root_build = (ROOT / "build.gradle").read_text(encoding="utf-8-sig")
        expected = {
            "cpf-tools/build/gradle-plugin": "cpf-gradle-plugin",
            "cpf-tools/build/platform-bom": "cpf-platform-bom",
        }

        for physical_path, canonical_name in expected.items():
            with self.subTest(composite=canonical_name):
                declaration = re.compile(
                    rf"includeBuild\s*\(\s*['\"]{re.escape(physical_path)}['\"]\s*\)\s*"
                    rf"\{{[^}}]*\bname\s*=\s*['\"]{re.escape(canonical_name)}['\"]",
                    re.DOTALL,
                )
                self.assertRegex(settings, declaration)
                self.assertIn("apply from: file('cpf-tools/build/cpf-root-conventions.gradle')", root_build)


if __name__ == "__main__":
    unittest.main()
