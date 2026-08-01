from __future__ import annotations
import importlib.util
from pathlib import Path
import tempfile
import unittest

SCRIPT = Path(__file__).parents[1] / "verify-cpf-protected-doc-boundary.py"
spec = importlib.util.spec_from_file_location("protected_doc_boundary", SCRIPT)
module = importlib.util.module_from_spec(spec)
assert spec and spec.loader
spec.loader.exec_module(module)

class ProtectedDocBoundaryTest(unittest.TestCase):
    def fixture(self) -> tuple[Path, Path, Path]:
        root = Path(tempfile.mkdtemp())
        manifest = root / "changed.txt"
        manifest.write_text("cpf-admin/src/main/java/Example.java\n", encoding="utf-8")
        handover = root / "handover.md"
        handover.write_text("\n".join(module.REQUIRED_HANDOVER_TOKENS), encoding="utf-8")
        return root, manifest, handover

    def test_valid(self):
        root, manifest, handover = self.fixture()
        module.verify(root, manifest, handover)

    def test_readme_rejected(self):
        root, manifest, handover = self.fixture()
        manifest.write_text("README.md\n", encoding="utf-8")
        with self.assertRaises(ValueError):
            module.verify(root, manifest, handover)

    def test_linked_guide_rejected(self):
        root, manifest, handover = self.fixture()
        manifest.write_text("cpf-docs/guide/CPF_RUNTIME_GUIDE.md\n", encoding="utf-8")
        with self.assertRaises(ValueError):
            module.verify(root, manifest, handover)

    def test_handover_rule_required(self):
        root, manifest, handover = self.fixture()
        handover.write_text("README is complete", encoding="utf-8")
        with self.assertRaises(ValueError):
            module.verify(root, manifest, handover)

if __name__ == "__main__":
    unittest.main()
