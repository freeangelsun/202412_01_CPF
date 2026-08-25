import importlib.util,tempfile,unittest
from pathlib import Path
SCRIPT=Path(__file__).parents[1]/'verify-cpf-runtime-snapshot-versioning.py'
def load():
 s=importlib.util.spec_from_file_location('v',SCRIPT);m=importlib.util.module_from_spec(s);s.loader.exec_module(m);return m
class T(unittest.TestCase):
 def fixture(self,fixed=True):
  td=tempfile.TemporaryDirectory();r=Path(td.name);p=r/'cpf-starters/integration/http/src/main/java/com/cpf/integration/http/internal/CpfServiceEndpointRegistry.java';p.parent.mkdir(parents=True)
  equal="""if(version==old.version()){if(old.endpoints().equals(immutable))return old;throw new IllegalArgumentException(\"version collision\");}""" if fixed else ''
  p.write_text(f'''class X{{public Snapshot replaceRuntime(long version,Map endpoints){{while(true){{Snapshot old=runtime.get();if(version<old.version())throw new IllegalArgumentException();{equal}Snapshot next=new Snapshot(version,immutable);if(runtime.compareAndSet(old, next))return next;}}}}}}''')
  return td,r
 def test_fixed_passes(self):td,r=self.fixture();self.addCleanup(td.cleanup);self.assertEqual('PASS',load().verify(r)['status'])
 def test_same_version_overwrite_fails(self):td,r=self.fixture(False);self.addCleanup(td.cleanup);self.assertRaises(Exception,load().verify,r)
 def test_test_contract_exists(self):
  t=(SCRIPT.parents[3]/'cpf-starters/integration/http/src/test/java/com/cpf/integration/http/internal/CpfServiceEndpointRegistryTest.java')
  self.assertTrue(t.is_file());self.assertIn('sameVersionReplayIsIdempotentButDifferentSnapshotIsRejected',t.read_text(encoding="utf-8"))
if __name__=='__main__':unittest.main()
