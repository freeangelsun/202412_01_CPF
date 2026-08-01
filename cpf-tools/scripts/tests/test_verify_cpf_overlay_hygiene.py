from __future__ import annotations
import importlib.util
import tempfile
import unittest
from pathlib import Path
SCRIPT = Path(__file__).resolve().parents[1] / "verify-cpf-overlay-hygiene.py"
spec = importlib.util.spec_from_file_location("overlay_hygiene", SCRIPT)
module = importlib.util.module_from_spec(spec); assert spec and spec.loader; spec.loader.exec_module(module)

class OverlayHygieneTest(unittest.TestCase):
    def test_clean_tree_passes(self):
        with tempfile.TemporaryDirectory() as d:
            root=Path(d); (root/'src').mkdir(); (root/'src/a.txt').write_text('clean\n',encoding='utf-8')
            _, errors=module.verify(root); self.assertEqual(errors,[])
    def test_generated_artifact_is_rejected(self):
        with tempfile.TemporaryDirectory() as d:
            root=Path(d); (root/'build').mkdir(); (root/'build/x.txt').write_text('x',encoding='utf-8')
            _, errors=module.verify(root); self.assertTrue(errors)
    def test_private_key_is_rejected(self):
        with tempfile.TemporaryDirectory() as d:
            root=Path(d); (root/'secret.txt').write_text('-----BEGIN ' + 'PRIVATE KEY-----\n',encoding='utf-8')
            _, errors=module.verify(root); self.assertTrue(errors)
    def test_trailing_whitespace_is_rejected(self):
        with tempfile.TemporaryDirectory() as d:
            root=Path(d); (root/'bad.txt').write_text('bad  \n',encoding='utf-8')
            _, errors=module.verify(root); self.assertTrue(errors)
if __name__ == '__main__': unittest.main()
