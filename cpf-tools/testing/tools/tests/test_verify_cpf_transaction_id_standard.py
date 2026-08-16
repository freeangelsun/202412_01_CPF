from __future__ import annotations
import importlib.util,shutil,tempfile,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[4];SCRIPT=ROOT/"cpf-tools/verification/tools/verify-cpf-transaction-id-standard.py"
def load():s=importlib.util.spec_from_file_location("g",SCRIPT);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
class T(unittest.TestCase):
 def root(self,bad=False):
  td=tempfile.TemporaryDirectory();r=Path(td.name)
  for rel in ("cpf-starters/web/src/main/java/com/cpf/web/runtime/CpfWebContextFilter.java","cpf-starters/web/src/main/java/com/cpf/web/context/CpfHttpInboundContextAdapter.java"):
   q=r/rel;q.parent.mkdir(parents=True,exist_ok=True);shutil.copy2(ROOT/rel,q)
  q=r/"cpf-admin/src/main/java/x/C.java";q.parent.mkdir(parents=True,exist_ok=True);q.write_text("@RestController @GetMapping "+("String globalId;" if bad else ""),encoding="utf-8")
  return td,r
 def test_pass_with_fallback(self):td,r=self.root();self.addCleanup(td.cleanup);self.assertEqual("PASS",load().verify(r)["status"])
 def test_legacy_fails(self):td,r=self.root(True);self.addCleanup(td.cleanup);self.assertRaises(Exception,load().verify,r)
if __name__=="__main__":unittest.main()
