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

    def test_root_generated_domain_mount_propagates_current_product_composite(self):
        settings = (ROOT / "settings.gradle").read_text(encoding="utf-8-sig")

        include_guard = settings.index("if (includeGeneratedDomains) {")
        property_guard = settings.index(
            "if (!providers.gradleProperty('cpfProductCompositeRoot').isPresent())",
            include_guard,
        )
        property_write = settings.index(
            "propagatedProjectProperties.put('cpfProductCompositeRoot', rootDir.canonicalFile.absolutePath)",
            property_guard,
        )
        propagation = settings.index(
            "gradle.startParameter.setProjectProperties(propagatedProjectProperties)",
            property_write,
        )
        generated_mount = settings.index("generatedDomainRoots.each { candidate ->", propagation)
        include_build = settings.index("includeBuild(canonical)", generated_mount)

        self.assertLess(include_guard, property_guard)
        self.assertLess(property_guard, property_write)
        self.assertLess(property_write, propagation)
        self.assertLess(propagation, generated_mount)
        self.assertLess(generated_mount, include_build)

    def test_generated_domain_compile_is_owned_by_standalone_composite_gates(self):
        local_runtime = (
            ROOT / "cpf-tools/runtime/cpf-local-runtime/build.gradle"
        ).read_text(encoding="utf-8-sig")
        final_runner = (
            ROOT / "cpf-tools/verification/nxt3/cpf_nxt3_verify_all.py"
        ).read_text(encoding="utf-8-sig")

        self.assertNotIn("cpfMountGeneratedDomains", local_runtime)
        self.assertNotIn(
            'dependencies.add(\'implementation\', "${packageName}:online:1.0.0-SNAPSHOT")',
            local_runtime,
        )
        self.assertIn("-PcpfProductCompositeRoot=", final_runner)
        self.assertIn("generated_cmd", final_runner)


if __name__ == "__main__":
    unittest.main()
