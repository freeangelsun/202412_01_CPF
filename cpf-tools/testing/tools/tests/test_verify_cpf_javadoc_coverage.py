from __future__ import annotations
import importlib.util
from pathlib import Path
import tempfile
import unittest
SCRIPT=Path(__file__).resolve().parents[4] / "cpf-tools/verification/tools/verify-cpf-javadoc-coverage.py"
spec=importlib.util.spec_from_file_location("javadoc_gate",SCRIPT); module=importlib.util.module_from_spec(spec); assert spec and spec.loader; spec.loader.exec_module(module)
class JavaDocCoverageTest(unittest.TestCase):
    def fixture(self,source:str):
        root=Path(tempfile.mkdtemp()); rel="cpf-core/src/main/java/com/cpf/core/api/SampleApi.java"
        p=root/rel; p.parent.mkdir(parents=True); p.write_text(source,encoding="utf-8")
        m=root/"manifest.txt"; m.write_text(rel+"\n",encoding="utf-8"); return root,m
    def test_valid(self):
        root,m=self.fixture("/** 계약 설명입니다. */\npublic interface SampleApi {}\n"); module.verify(root,m)
    def test_missing_rejected(self):
        root,m=self.fixture("public interface SampleApi {}\n")
        with self.assertRaises(ValueError): module.verify(root,m)
    def test_annotations_after_javadoc(self):
        root,m=self.fixture("/** 운영 API입니다. */\n@Deprecated\npublic interface SampleApi {}\n"); module.verify(root,m)
if __name__=="__main__": unittest.main()
