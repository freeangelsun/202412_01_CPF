import importlib.util,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).resolve().parents[1]/'verify-cpf-owner-boundaries.py'
def load():s=importlib.util.spec_from_file_location('g',SCRIPT);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
class T(unittest.TestCase):
 def fixture(self,bad=False):
  td=tempfile.TemporaryDirectory();r=Path(td.name)
  (r/'settings.gradle').write_text("include ':cpf-core', ':cpf-common', ':cpf-batch'",encoding='utf-8');(r/'build.gradle').write_text('',encoding='utf-8')
  for m in ('cpf-core','cpf-common','cpf-batch'):(r/m/'src/main/java/x').mkdir(parents=True);(r/m/'build.gradle').write_text('',encoding='utf-8')
  (r/'cpf-core/src/main/java/x/A.java').write_text('package x;\n'+('import com.cpf.batch.internal.Bad;\n' if bad else ''),encoding='utf-8')
  return td,r
 def test_pass(self):td,r=self.fixture();self.addCleanup(td.cleanup);self.assertEqual('PASS',load().verify(r)['status'])
 def test_sparse_snapshot_fails_closed(self):
  with tempfile.TemporaryDirectory() as d:self.assertRaises(Exception,load().verify,Path(d))
 def test_internal_boundary_fails(self):td,r=self.fixture(True);self.addCleanup(td.cleanup);self.assertRaises(Exception,load().verify,r)
if __name__=='__main__':unittest.main()
